package com.Reggie.test;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.ReggieApplicationBoot;
import com.itheima.reggie.bean.Dish;
import com.itheima.reggie.bean.SetmealDish;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.WXPayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootTest(classes = ReggieApplicationBoot.class)
public class ReggieTest {
    @Autowired
    SetmealDishService setmealDishService;
    @Autowired
    WXPayService wxPayService;
    @Autowired
    DishService dishService;
    @Test
    public void test(){
        QueryWrapper<SetmealDish> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.select("(select id from setmeal where category_id="+ 1413386191767674881L +") as newDB");
        Map<String, Object> list = setmealDishService.getMap(queryWrapper1);
        System.out.println(list);
    }
    @Test
    public void test1(){
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,1415580119015145474L);
        lambdaQueryWrapper.select(SetmealDish::getDishId);
        Map<String, Object> map = setmealDishService.getMap(lambdaQueryWrapper);
        Set<Map.Entry<String, Object>> entries = map.entrySet();
        for(Map.Entry<String,Object> entry:entries){
            System.out.println(entry.getValue()+".."+entry.getKey());
        }

    }
    @Test
    public void tet(){
        Map<String, String> aNative = wxPayService.createNative(1683726421417091073L + "", "1", "");
        //封装返回数据、 订单号、支付金额
        aNative.put("orderId",1683726421417091073L+"");
        aNative.put("total_fee",40+"");
        for (Map.Entry<String,String> e:aNative.entrySet()) {
            System.out.println(e.getKey()+'1'+e.getValue());
            //weixin://wxpay/bizpayurl?pr=mbKMnO5zz
        }
    }
}
