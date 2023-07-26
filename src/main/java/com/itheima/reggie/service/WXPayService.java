package com.itheima.reggie.service;

import java.util.Map;

public interface WXPayService {

    //创建订单
    Map<String, String> createNative(String out_trade_no, String total_fee,String goods);

    //查询订单状态
    Map<String, String> queryNative(String out_trade_no);

    //关闭订单
    Map<String, String> closeNative(String out_trade_no);
}
