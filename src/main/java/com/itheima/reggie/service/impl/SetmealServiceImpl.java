package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.bean.Category;
import com.itheima.reggie.bean.OrderDetail;
import com.itheima.reggie.bean.Setmeal;
import com.itheima.reggie.bean.SetmealDish;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private OrderDetailService orderdetailService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Override
    public List<SetmealDto> queryList(Long categoryId, Integer status) {

        Category category = categoryService.getById(categoryId);
        String name = category.getName();
//        Long x=1683401911941730305L;
        QueryWrapper<OrderDetail> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("setmeal_id,sum(number) as newNum");
        queryWrapper.isNull("dish_id");
        queryWrapper.groupBy("setmeal_id");
        queryWrapper.orderByDesc("newNum");
        List<SetmealDto> setmeals=new ArrayList<>();
        if (name.contains("热销")){
            List<OrderDetail> list =orderdetailService.list(queryWrapper);
            List<OrderDetail> orderDetails = list.subList(0, 2);
            for (OrderDetail orderDetail : orderDetails) {
                Setmeal setmeal = getById(orderDetail.getSetmealId());
                if(setmeal==null){
                    continue;
                }
                SetmealDto setmealDto = new SetmealDto();
                BeanUtils.copyProperties(setmeal, setmealDto);
                setmealDto.setSaleNumber(orderDetail.getNewNum());
                setmeals.add(setmealDto);
            }
            return setmeals;
        }

        //如果是查询普通分类
        LambdaQueryWrapper<Setmeal> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Setmeal::getCategoryId,categoryId);
        wrapper.eq(Setmeal::getStatus,1);
        wrapper.orderByAsc(Setmeal::getCreateTime);
        List<Setmeal> list = list(wrapper);
        for (Setmeal setmeal : list) {
            SetmealDto setmealDto = new SetmealDto();
            queryWrapper.clear();
            queryWrapper.select("setmeal_id,sum(number) as newNum");
            queryWrapper.isNull("dish_id");
            queryWrapper.groupBy("setmeal_id");
            queryWrapper.orderByDesc("newNum");
            queryWrapper.eq("setmeal_id", setmeal.getId());
            OrderDetail oneDetail = orderdetailService.getOne(queryWrapper);
            if (oneDetail != null) {
                setmealDto.setSaleNumber(oneDetail.getNewNum());
            }
            LambdaQueryWrapper<SetmealDish> wrappers = new LambdaQueryWrapper<>();
            wrappers.eq(SetmealDish::getSetmealId, setmeal.getId());
            List<SetmealDish> list1 = setmealDishService.list(wrappers);
            setmealDto.setSetmealDishes(list1);
            BeanUtils.copyProperties(setmeal, setmealDto);
            setmeals.add(setmealDto);
        }
        return setmeals;
    }}
