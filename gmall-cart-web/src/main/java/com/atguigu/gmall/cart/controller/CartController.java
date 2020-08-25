package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @program: gmall
 * @author: lzq
 * @create: 2020-08-16 22:59
 * description:
 **/
@Controller
public class CartController {

    @Reference
    CartService cartService;

    @Reference
    SkuService skuService;

    @RequestMapping("checkCart")
    @LoginRequired(loginSuccess = false)
    public String checkCart(String isChecked, String skuId, HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");
        String nickname= (String) request.getAttribute("nickname");

        // 调用服务，修改状态
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setIsChecked(isChecked);
        cartService.checkCart(omsCartItem);

        // 将最新的数据从缓存中查出，渲染给内嵌页
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        modelMap.put("cartList",omsCartItems);

        // 被勾选商品的总额
        BigDecimal totalAmount =getTotalAmount(omsCartItems);
        modelMap.put("totalAmount",totalAmount);
        return "cartListInner";
    }

    @RequestMapping("cartList")
    @LoginRequired(loginSuccess = false)
    public String cartList(String skuId, int quantity, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap){

        List<OmsCartItem> omsCartItems = new ArrayList<>();
        String memberId = (String) request.getAttribute("memberId");
        String nickname= (String) request.getAttribute("nickname");

        if (StringUtils.isNotBlank(memberId)){
            //已经登录查询db
            omsCartItems = cartService.cartList(memberId);
        }else {
            //没有登陆查询cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)){
                omsCartItems =JSON.parseArray(cartListCookie, OmsCartItem.class);

            }
        }

        for (OmsCartItem omsCartItem : omsCartItems) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        }

        modelMap.put("cartList", omsCartItems);
        BigDecimal totalAmount=getTotalAmount(omsCartItems);
        modelMap.put("totalAmount", omsCartItems);
        return "cartList";
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount = new BigDecimal("");

        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();

            if (omsCartItem.getIsChecked().equals("1")){
                totalAmount =totalAmount.add(totalPrice);
            }
        }
        return totalAmount;
    }


    @RequestMapping("addToCart")
    @LoginRequired(loginSuccess = false)
    public String addToCart(String skuId, int quantity, HttpServletRequest request, HttpServletResponse response){

        List<OmsCartItem> omsCartItemList =new ArrayList<>();
        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId, "");

        //将商品信息封装成购物车信息
        OmsCartItem omsCartItem=new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("111111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(new BigDecimal(quantity));

        //判断用户是否登陆
        String memberId = (String) request.getAttribute("memberId");
        String nickname= (String) request.getAttribute("nickname");

        if (StringUtils.isBlank(memberId)){
            //用户未登录

            //cookie里原有的购物车数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);

            if (StringUtils.isBlank(cartListCookie)){
                //cookie为空
                omsCartItemList.add(omsCartItem);
            }else{
                //cookie不为空
                omsCartItemList= JSON.parseArray(cartListCookie, OmsCartItem.class);

                //判断添加的购物车数据在cookie中是否存在
                boolean exist =if_cart_exits(omsCartItemList,omsCartItem);

                if (exist){
                    //之前添加过，更新购物车添加数量
                    for (OmsCartItem cartItem : omsCartItemList) {
                        if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                        }
                    }
                }else {
                    //之前没有添加过，新增当前的购物车
                    omsCartItemList.add(omsCartItem);
                }
            }

            //更新cookie
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItemList),60*60*72,true);

        }else {
            //用户已登录
            //从db中查出购物车数据
            OmsCartItem omsCartItemFromDb=cartService.ifCartExistByUser(memberId,skuId);

            if (omsCartItemFromDb == null){
                //该用户没有添加过当前商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname("test小明");
                omsCartItem.setQuantity(new BigDecimal(quantity));
                cartService.addCart(omsCartItem);
            }else {
                //该用户添加过当前商品
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItemFromDb);
            }

            //同步缓存
            cartService.flushCartCache(memberId);
        }

        return "redirect:/success.html";
    }

    private boolean if_cart_exits(List<OmsCartItem> omsCartItemList, OmsCartItem omsCartItem) {

        boolean flag=false;

        for (OmsCartItem cartItem : omsCartItemList) {
            String productSkuId = cartItem.getProductSkuId();

            if (productSkuId.equals(omsCartItem.getProductSkuId())){
                flag=true;
            }
        }

        return flag;
    }
}
