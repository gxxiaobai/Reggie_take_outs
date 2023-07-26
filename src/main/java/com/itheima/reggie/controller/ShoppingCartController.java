package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.bean.ShoppingCart;

import com.itheima.reggie.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Api(tags = "购物车接口")
@RestController
@RequestMapping("shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService service;


    /**
    *
    *    给购物车添加商品
    *    根据实体类存储的数据，判断添加的是套餐还是菜品  然后获取一个实体，套餐或者菜品
    *       再进行判断数据库中是否存在当前实体，如果不存在，设置他的数量为1，如果存在，取出
    *       number 进行+1操作，完成更新操作或者保存。
    *
    *  */
    @ApiOperation("给购物车添加菜品或套餐")
    @PostMapping("add")
    public R<ShoppingCart> adds(@RequestBody ShoppingCart shoppingCart) {
        shoppingCart.setUserId(BaseContext.getThreadLocal());
        System.out.println(shoppingCart);
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,BaseContext.getThreadLocal());
        if (dishId==null){
            //说明是套餐
            wrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }else {

            wrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }
        ShoppingCart one = service.getOne(wrapper);
        if (one!=null){
            Integer number = one.getNumber();
            one.setNumber(number+1);
            service.updateById(one);
        }else {
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);
            service.save(shoppingCart);
            one=shoppingCart;
        }

        return R.success(one);
    }

    /**
    * 获取购物车集合，根据BaseContext.getLocal()存储的id
    * 执行查询语句
    * 返回集合，返回number大于1的
    * */
    @ApiOperation("获取购物车数据")
    @GetMapping("list")
    public R<List<ShoppingCart>> queryShopping(){
        Long id = BaseContext.getThreadLocal();
        LambdaQueryWrapper<ShoppingCart> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,id);
        wrapper.gt(ShoppingCart::getNumber,0);
        wrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = service.list(wrapper);
        return R.success(list);
    }

    /**
    对购物车中的数量进行--操作
    根据传来的实体类中存储的数据判断存储的是套餐还是菜品
    取出菜品或者套餐的数量  执行-1操作
     */
    @ApiOperation("减购物车菜品数量")
    @PostMapping("sub")
    public R<ShoppingCart> delete(@RequestBody ShoppingCart shoppingCart){

        LambdaQueryWrapper<ShoppingCart> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,BaseContext.getThreadLocal());
        Long dishId = shoppingCart.getDishId();
        if (dishId==null){
            wrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }else {
            wrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }
        ShoppingCart one = service.getOne(wrapper);
        Integer number = one.getNumber();
        if (number==0){
            service.removeById(one);
            return R.error("不合法的操作");
        }

        one.setNumber(number-1);
        service.updateById(one);
        return R.success(one);
    }

    /**
    *   删除购物车中所有食品
    */
    @ApiOperation("清空购物车")
    @DeleteMapping("clean")
    public R<String> delete(){
        Long local = BaseContext.getThreadLocal();
        LambdaQueryWrapper<ShoppingCart> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,local);
        service.remove(wrapper);
        return R.success("删除成功");
    }

    }
