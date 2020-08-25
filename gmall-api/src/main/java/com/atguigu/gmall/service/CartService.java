package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OmsCartItem;

import java.util.List;

/**
 * @program: gmall
 * @author: lzq
 * @create: 2020-08-17 15:27
 * description:
 **/
public interface CartService {
    OmsCartItem ifCartExistByUser(String memberId, String skuId);

    void addCart(OmsCartItem omsCartItem);

    void updateCart(OmsCartItem omsCartItemFromDb);

    void flushCartCache(String memberId);

    public List<OmsCartItem> cartList(String userId);

    public void checkCart(OmsCartItem omsCartItem);
}
