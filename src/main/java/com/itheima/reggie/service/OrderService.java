package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.bean.OrderDetail;
import com.itheima.reggie.bean.Orders;
import com.itheima.reggie.bean.User;

import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Orders>  {
    Map<String,String> submitOrder(Orders orders);
    void againOrder(Orders orders);

    List<OrderDetail> getRecentOrder(String number);

    User getUser();
}
