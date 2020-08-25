package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.HttpclientUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: gmall
 * @author: lzq
 * @create: 2020-08-18 19:58
 * description:
 **/
@Controller
public class PassportController {

    @Reference
    UserService userService;

    @Reference
    CartService cartService;

    @RequestMapping("vlogin")
    public String vlogin(String code, HttpServletRequest request) {

        //用授权码换区access_token
        String s3="https://api.weibo.com/oauth2/access_token";
        Map<String,String> paramMap =new HashMap<>();
        paramMap.put("client_id","823059083");
        paramMap.put("client_secret","45e91388d60a2a6f1943336620e3c8f3");
        paramMap.put("redirect_uri","http://passport.gmall.com:8085/vlogin");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("code",code);

        String access_token_json = HttpclientUtil.doPost(s3, paramMap);

        Map<String,Object> access_map = JSON.parseObject(access_token_json, Map.class);
        //access_token换取用户信息
        String uid = (String) access_map.get("uid");
        String access_token = (String) access_map.get("access_token");
        String show_user_url="https://api.weibo.com/2/users/show.json?access_token="+access_token+"&uid="+uid;
        String user_json = HttpclientUtil.doGet(show_user_url);
        Map<String,Object> user_map = JSON.parseObject(user_json, Map.class);

        //将用户信息保存至数据库，用户类型设置为微博用户
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceType("2");
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(access_token);
        umsMember.setSourceUid((String) user_map.get("idstr"));
        umsMember.setCity((String) user_map.get("location"));
        umsMember.setCity((String) user_map.get("screen_name"));

        String gender= (String) user_map.get("gender");
        String i="0";
        if (gender.equals("m")){
            i="1";
        }
        umsMember.setGender(i);

        UmsMember umsCheck = new UmsMember();
        umsCheck.setSourceUid(umsMember.getSourceUid());
        UmsMember umsMemberCheck = userService.checkOauthUser(umsCheck);//检查该用户（社交用户）以前是否登陆过系统

        if (umsMemberCheck == null){
            umsMember =userService.addOauthUser(umsMember);
        }else{
            umsMember=umsMemberCheck;
        }

        //生成jwt的token，并且重定向到首页，携带该token
        String id = umsMember.getId();//rpc的主键返回策略失效会导致主键空值
        String nickname = umsMember.getNickname();
        String token=null;
        Map<String,Object> userMap=new HashMap<>();
        userMap.put("memberId",id);
        userMap.put("nickname",nickname);

        String ip = request.getHeader("x-forwarded-for");//通过nginx转发的客户端ip
        if (StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();//从reque中获取ip
            if (StringUtils.isBlank(ip)){
                ip="127.0.0.1";
            }
        }

        //按照设计的算法对参数进行加密后，生成token
        token = JwtUtil.encode("2020gmall", userMap, ip);
        //将token传入redis一份
        userService.addUserToken(token, id);

        return "redirect:http://search.gmall.com:8083/index?token="+token;
    }


    @RequestMapping("verify")
    @ResponseBody
    public String verify(String token, String currentIp){

        //通过jwt校检token真假
        Map<String,String> map=new HashMap<>();

        Map<String, Object> decode = JwtUtil.decode(token, "2020gmall", currentIp);

        if (decode!=null){
            map.put("status", "success");
            map.put("memberId", (String) decode.get("memberId"));
            map.put("nickname", (String) decode.get("nickname"));
        }else {
            map.put("status", "fail");
        }

        return JSON.toJSONString(map);
    }


    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request){

        String token="";
        //调用用户服务验证用户名和密码
        UmsMember umsMemberLogin=userService.login(umsMember);

        if (umsMemberLogin!=null){
            //登陆成功

            //用jwt制作token
            String memberId = umsMemberLogin.getId();
            String nickname = umsMemberLogin.getNickname();
            Map<String,Object> userMap=new HashMap<>();
            userMap.put("memberId",memberId);
            userMap.put("nickname",nickname);

            String ip = request.getHeader("x-forwarded-for");//通过nginx转发的客户端ip
            if (StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();//从reque中获取ip
                if (StringUtils.isBlank(ip)){
                    ip="127.0.0.1";
                }
            }

            //按照设计的算法对参数进行加密后，生成token
            token=JwtUtil.encode("2020gmall", userMap, ip);

            //将token传入redis一份
            userService.addUserToken(token, memberId);
        }else {
            //登陆失败
            token="fail";
        }

        return token;
    }

    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap map){

        map.put("ReturnUrl",ReturnUrl);
        return "index";
    }
}
