package com.atguigu.gmall.passport.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.util.HttpclientUtil;
import sun.net.www.http.HttpClient;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: gmall
 * @author: lzq
 * @create: 2020-08-19 19:20
 * description:
 **/
public class TestOauth2 {

    public static String getCode(){

        //第一步：请求申请授权的地址
        //App key:823059083
        //App Secret：45e91388d60a2a6f1943336620e3c8f3
        //回调地址：http://passport.gmall.com:8085/vlogin
        String s1 = HttpclientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=823059083&response_type=code&redirect_uri=http://passport.gmall.com:8085/vlogin");

        System.out.println(s1);


        //第二步：接收授权码(在有效期使用)
        //授权码：901107b45ddd163dbc1ca72a1f758564
        String s2="http://passport.gmall.com:8085/vlogin?code=348bff9c28f4d515fed36cfda6d0ec95";
        return null;
    }

    public static String getAccess_token(){

        //第三步：交换access_token
        String s3="https://api.weibo.com/oauth2/access_token";//?client_id=823059083&client_secret=45e91388d60a2a6f1943336620e3c8f3&grant_type=authorization_code&redirect_uri=http://passport.gmall.com:8085/vlogin&code=901107b45ddd163dbc1ca72a1f758564";

        Map<String,String> paramMap =new HashMap<>();
        paramMap.put("client_id","823059083" );
        paramMap.put("client_secret","45e91388d60a2a6f1943336620e3c8f3" );
        paramMap.put("redirect_uri","http://passport.gmall.com:8085/vlogin" );
        paramMap.put("grant_type","authorization_code" );
        paramMap.put("code","348bff9c28f4d515fed36cfda6d0ec95" );

        String access_token_json = HttpclientUtil.doPost(s3, paramMap);

        Map<String,String> access_map = JSON.parseObject(access_token_json, Map.class);

        System.out.println(access_map.get("access_token"));

        return access_map.get("access_token");
    }

    public static Map<String,String> getUser_info(){
        //第四步：用access_token查询用户信息
        String s4="https://api.weibo.com/2/users/show.json?access_token=2.00MYeogG0VQThta082405302Phh9CC&uid=1";
        String user_json = HttpclientUtil.doGet(s4);
        Map<String,String> user_map = JSON.parseObject(user_json, Map.class);
        System.out.println(user_map.get("1"));

        return user_map;
    }


    public static void main(String[] args) {
        getCode();
        getAccess_token();
        getUser_info();

    }
}
