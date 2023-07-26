package com.itheima.reggie.service.impl;


import com.github.wxpay.sdk.WXPay;
import com.itheima.reggie.service.WXPayService;
import com.itheima.reggie.config.PayConfig;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WXPayServiceImpl implements WXPayService {

    /**
     * 调用统一下单接口
     * @param out_trade_no
     * @param total_fee
     * @param goods
     * @return
     */
    @Override
    public Map<String, String> createNative(String out_trade_no, String total_fee,String goods) {
        /**
         * /企业方公众号Id
         *     public static String appId = "wx8397f8696b538317";
         *     //财付通平台的商户账号
         *     public static String partner = "1473426802";
         *     //财付通平台的商户密钥
         *     public static String partnerKey = "T6m9iK73b0kn9g5v426MKfHQH7X8rKwb";
         */
        PayConfig config = new PayConfig();
        WXPay wxpay = new WXPay(config);//

        Map<String, String> data = new HashMap<String, String>();
        data.put("out_trade_no", out_trade_no);//商户订单号
        data.put("total_fee", total_fee);//标价金额

        data.put("body", "黑马程序员");//商品描述
        data.put("fee_type", "CNY");//标价币种
        data.put("device_info", "");//设备号
        data.put("spbill_create_ip", "127.0.0.1");//终端IP
        data.put("notify_url", "http://www.example.com/wxpay/notify"); //通知地址
        data.put("trade_type", "NATIVE");  // 此处指定为扫码支付
        data.put("product_id", goods);//商品的信息
        try {
            Map<String, String> resp = wxpay.unifiedOrder(data);
            System.out.println(resp);
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询订单状态
     * @param out_trade_no
     * @return
     */
    @Override
    public Map<String, String> queryNative(String out_trade_no) {
        PayConfig config = new PayConfig();
        WXPay wxpay = new WXPay(config);
        /**
         * 封装查询订单 请求参数
         */
        Map<String, String> data = new HashMap<String, String>();
        //封住订单号
        data.put("out_trade_no", out_trade_no);

        try {
            Map<String, String> resp = wxpay.orderQuery(data);
            System.out.println(resp);
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭订单
     * @param out_trade_no
     * @return
     */
    @Override
    public Map<String, String> closeNative(String out_trade_no) {
        PayConfig config = new PayConfig();
        WXPay wxpay = new WXPay(config);
        /**
         * 封装关闭订单的请求参数
         */
        Map<String, String> data = new HashMap<String, String>();
        data.put("out_trade_no", out_trade_no);//关闭订单的订单号

        try {
            Map<String, String> resp = wxpay.closeOrder(data);
            System.out.println(resp);
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
