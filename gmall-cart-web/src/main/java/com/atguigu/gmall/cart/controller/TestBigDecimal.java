package com.atguigu.gmall.cart.controller;

import java.math.BigDecimal;

/**
 * @program: gmall
 * @author: lzq
 * @create: 2020-08-17 21:33
 * description:金融计算建议使用BigDecimal
 **/
public class TestBigDecimal {
    public static void main(String[] args) {
        //初始化,基本数据类型的初始化(用字符串更精确)
        BigDecimal b1 = new BigDecimal(0.01f);
        BigDecimal b2 = new BigDecimal(0.01d);
        BigDecimal b3 = new BigDecimal("0.01");
        System.out.println(b1);
        System.out.println(b2);
        System.out.println(b3);

        //比较
        int i=b1.compareTo(b2);//1,0,-1
        System.out.println(i);

        //运算
        BigDecimal add = b1.add(b2);
        System.out.println(add);
        BigDecimal multiply = b1.multiply(b2);
        System.out.println(multiply);
        BigDecimal subtract = b1.subtract(b2);
        System.out.println(subtract);

        BigDecimal b4 = new BigDecimal("6");
        BigDecimal b5 = new BigDecimal("7");
        BigDecimal multiply1 = b4.multiply(b5);
        System.out.println(multiply1);

    }
}
