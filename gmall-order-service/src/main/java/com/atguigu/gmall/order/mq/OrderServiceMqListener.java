package com.atguigu.gmall.order.mq;

import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * @program: gmall
 * @author: lzq
 * @create: 2020-08-24 14:56
 * description:
 **/
@Component
public class OrderServiceMqListener {

    @Autowired
    OrderService orderService;

    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE",containerFactory = "jmsQueueListener")
    public void consumePaymentResult(MapMessage mapMessage) throws JMSException {

        String outTradeOn = mapMessage.getString("out_trade_on");

        //更新订单业务
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outTradeOn);
        orderService.updateOrder(omsOrder);

    }
}
