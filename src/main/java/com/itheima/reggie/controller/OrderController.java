package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.bean.OrderDetail;
import com.itheima.reggie.bean.Orders;
import com.itheima.reggie.bean.User;
import com.itheima.reggie.service.OrderService;
import com.itheima.reggie.service.WXPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = "订单控制器")
@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private WXPayService wxPayService;


    /*
     *提交订单
     * 主要功能全部封装在service.submitOrder(orders);中
     *
     * */
    @ApiOperation("提交订单接口")
    @PostMapping("submit")
    public R<Map<String, String>> submit(@RequestBody Orders orders) {
        Map<String, String> stringStringMap = orderService.submitOrder(orders);
        log.info("--------------->");
        System.out.println(orders);
        return R.success(stringStringMap);
    }


    /**
     * 1.这是后台系统发的请求
     * 2.管理员查询所有订单
     */
    @ApiOperation("后台管理端查询所有订单")
    @GetMapping("page")
    public R<Page<Orders>> queryOrder(int page, int pageSize, Long number, String beginTime, String endTime) {
        Page<Orders> page1 = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(number != null, Orders::getNumber, number);
        wrapper.between(beginTime != null && endTime != null, Orders::getOrderTime, beginTime, endTime);
        orderService.page(page1, wrapper);
        return R.success(page1);
    }

    /**
     * 用户端发的请求
     * 用来返回订单详情    引入OrdersDto类
     * 使用Page（page,pageSize）
     * 根据当前id获取获取所有订单，
     * 遍历订单集合，根据每个订单的订单号查询订单详情，并把order复制给orderDetail，根据订单number（号）给查询订单详情，返回订单详情集合，继续
     * 封装到orderDto中
     * <p>
     * 最终使用 BeanUtils.copyProperties将page1 复制到 page2  但是不复制records属性
     * 将 orderDto集合封装到page2的records属性中   返回page2
     */
    @ApiOperation("返回订单详情")
    @GetMapping("userPage")
    public R<Page<OrdersDto>> selectOrder(int page, int pageSize) {
        Page<Orders> page1 = new Page<>(page, pageSize);
        Page<OrdersDto> page2 = new Page<>();
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Orders::getOrderTime);
        wrapper.eq(Orders::getUserId, BaseContext.getThreadLocal());
        orderService.page(page1, wrapper);
        List<Orders> records = page1.getRecords();
        List<OrdersDto> ordersDtos = new ArrayList<>();

        User user = orderService.getUser();

        for (Orders orders : records) {
            OrdersDto ordersDto = new OrdersDto();

            BeanUtils.copyProperties(orders, ordersDto);
            List<OrderDetail> recentOrder = orderService.getRecentOrder(orders.getNumber());
            ordersDto.setUserName(user.getName());
            ordersDto.setOrderDetails(recentOrder);
            ordersDtos.add(ordersDto);
        }
        BeanUtils.copyProperties(page1, page2, "records");
        page2.setRecords(ordersDtos);
        return R.success(page2);
    }


    /*管理端进性修改订单的状态  派送   完成*/
    @PutMapping
    @ApiOperation("管理端配送订单")
    public R<String> updateOrder(@RequestBody Orders orders) {

//        LambdaUpdateWrapper<Orders> wrapper=new LambdaUpdateWrapper<>();
//        wrapper.eq(Orders::getId,orders.getId());
//        wrapper.set(Orders::getStatus,orders.getStatus());
        orderService.updateById(orders);
        return R.success("修改成功");
    }


    /**
     * 再来一单功能
     * 根据BaseContext.getLocal()获取当前id，获取订单信息，根据订单号查询所有订单信息，将订单详情内容封装到购物车中
     */
    @ApiOperation("再来一单接口")
    @PostMapping("again")
    public R<String> agains(@RequestBody Orders orders) {

        orderService.againOrder(orders);
        return R.success("操作成功");
    }
    @ApiOperation("查询订单状态")
    @PostMapping("findPayStatus")
    public R<String> findPayStatus(String orderId) {
        //1、调用微信sdk查询订单状态
        Map<String, String> map = wxPayService.queryNative(orderId);
        if (
                "SUCCESS".equals(map.get("return_code")) &&
                        "SUCCESS".equals(map.get("result_code")) &&
                        "SUCCESS".equals(map.get("trade_state"))
        ) {
            //如果订单已支付, 更新数据库支付状态为已支付, 并反馈前端已支付
            Orders orders = new Orders();
            orders.setId(Long.parseLong(orderId));
            orders.setStatus(2);
            orderService.updateById(orders);
            return R.success("订单已支付");
        }

        //2、查询订单创建时间,判断订单是否已经超时
        Orders order = orderService.getById(orderId);
        LocalDateTime createTime = order.getOrderTime();
        if (new Date().getTime() - createTime.toInstant(ZoneOffset.of("+8")).toEpochMilli() >= 1000 *60 * 5) {
            //如果订单已超时,调用关闭订单的方法
            return closeOrder(orderId);

            //如果订单已超时, 更新数据库支付状态为超时未支付, 并反馈前端订单已超时
//            orderService.updateStatusTimeOut(orderId);
//            return new ResultInfo(false, "2", "订单已超时");
        }

        //3、如果订单未支付、也未超时, 直接反馈前端订单未支付
        return R.error("订单未支付");
    }

    //关闭订单
    private R<String> closeOrder(String orderId) {
        //调用微信sdk关闭订单
        Map<String, String> map = wxPayService.closeNative(orderId);
        if (map != null && "ORDERPAID".equals(map.get("err_code"))) {
            //关闭订单时,如果反馈订单已支付. 则更新数据库支付状态为已支付,并反馈前端订单已支付
            Orders orders = new Orders();
            orders.setId(Long.parseLong(orderId));
            orders.setStatus(2);
            orderService.updateById(orders);
            return R.success("订单已支付");
        }

        //只要不是支付成功,都默认为关闭成功. 更新数据库支付状态为超时未支付,并反馈前端订单已超时
        Orders orders = new Orders();
        orders.setId(Long.parseLong(orderId));
        orders.setStatus(5);
        orderService.updateById(orders);
        return R.success("订单已超时");
    }
    @ApiOperation("去支付接口")
    @GetMapping("toPay/{id}")
    public R<Map<String,String>> toPay(@PathVariable String id){
        System.out.println(id);
        Orders orders1 = orderService.getById(id);
        Map<String, String> aNative = wxPayService.createNative(orders1.getNumber() + "", "1", "");
        //封装返回数据、 订单号、支付金额
        aNative.put("orderId",orders1.getNumber()+"");
        aNative.put("total_fee",orders1.getAmount()+"");
        return R.success(aNative);
    }
}