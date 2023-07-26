package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.bean.Category;
import com.itheima.reggie.bean.Dish;
import com.itheima.reggie.bean.DishFlavor;
import com.itheima.reggie.bean.OrderDetail;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.OrderDetailService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrderDetailService orderDetailService;
    @Override
    public List<DishDto> lists(Long ids) {
        //通过id查类型
        Category category = categoryService.getById(ids);
        //获取类型名
        String name = category.getName();
        System.out.println(name);
        //添加查询条件只要dishId和他们的销量
        QueryWrapper<OrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("dish_id,sum(number) as newNum");
        //根据dishId分组
        queryWrapper.groupBy("dish_id");
        //只要菜品不要套餐
        queryWrapper.isNull("setmeal_id");
        //根据销量降序排序
        queryWrapper.orderByDesc("newNum");
        List<DishDto> dishDtos = new ArrayList<>();

        if (name.contains("热销")) {//查询热销菜品
            List<OrderDetail> list = orderDetailService.list(queryWrapper);
            for (OrderDetail orderDetail : list) {//如果这个热销菜品列表里的菜品被删掉或者被禁用就移除
                Dish dish = getById(orderDetail.getDishId());
                if(dish==null||dish.getStatus()==0){
                    list.remove(orderDetail);
                }
            }
            //获得菜品销量前三
            List<OrderDetail> orderDetails = list.subList(0, 3);
            for (OrderDetail orderDetail : orderDetails) {
                //遍历这三个热销菜品
                Long dishId = orderDetail.getDishId();
                Dish dish = getById(dishId);
                DishDto dishDto = new DishDto();
                //把销量和基础信息都封装到dto里
                dishDto.setSaleNumber(orderDetail.getNewNum());
                BeanUtils.copyProperties(dish, dishDto);
                //查询菜品口味
                LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(DishFlavor::getDishId, dish.getId());
                //把口味集合封装到dto里
                List<DishFlavor> list1 = dishFlavorService.list(wrapper);
                dishDto.setFlavors(list1);
                dishDtos.add(dishDto);
            }
            //如果是热销菜品直接返回
            return dishDtos;
        }

        //如果不是查询热销  查出当前分类所有菜品
        LambdaQueryWrapper<Dish> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getCategoryId,ids);
        wrapper.orderByAsc(Dish::getSort);
        wrapper.eq(Dish::getStatus,1);
        List<Dish> list =list(wrapper);

            //获取所有菜品 遍历菜品
        for (Dish dish : list) {
            //获取dish  id
            queryWrapper.clear();
            queryWrapper.select("dish_id,sum(number) as newNum");
            //根据dishId分组
            queryWrapper.groupBy("dish_id");
            //只要菜品不要套餐
            queryWrapper.isNull("setmeal_id");
            //根据销量降序排序
            queryWrapper.orderByDesc("newNum");
            queryWrapper.eq("dish_id", dish.getId());
            OrderDetail orderDetail = orderDetailService.getOne(queryWrapper);
            DishDto dishDto=new DishDto();
            //查询出符合条件的  封装值

            if (orderDetail != null) {
                dishDto.setSaleNumber(orderDetail.getNewNum());
            }
            LambdaQueryWrapper<DishFlavor> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.eq(DishFlavor::getDishId, dish.getId());
            List<DishFlavor> list3 = dishFlavorService.list(wrapper1);
            BeanUtils.copyProperties(dish, dishDto);
            dishDto.setFlavors(list3);
            dishDtos.add(dishDto);
        }
        return dishDtos;
    }
}
