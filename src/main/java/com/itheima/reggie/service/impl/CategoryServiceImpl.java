package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.bean.Category;
import com.itheima.reggie.bean.Dish;
import com.itheima.reggie.bean.SetmealDish;
import com.itheima.reggie.exception.CustomException;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Override
    public boolean remove(Long id) {
        //判断这个菜品类型是一个套餐还是菜品
        LambdaQueryWrapper<Category> lambdaQueryWrapper =new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(Category::getType);
        lambdaQueryWrapper.eq(Category::getId,id);
        Category categoryType =getOne(lambdaQueryWrapper);
        //如果是菜品判断这个菜品类型里是否还有在售菜品
        if(categoryType.getType()==1){
            LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.select(Dish::getId);
            //判断是否在售
            queryWrapper.eq(Dish::getStatus,1);
            queryWrapper.eq(Dish::getCategoryId,id);
            List<Dish> list = dishService.list(queryWrapper);
            if(list.size()>0){
                throw new CustomException("删除失败，当前类型还有在售菜品");
            }
        }else {
            //如果是套餐判断这个套餐类型里是否还有在售菜品
            QueryWrapper<SetmealDish> queryWrapper1=new QueryWrapper<>();
            queryWrapper1.select("(select id from setmeal where category_id="+id+") as newDB");
            queryWrapper1.eq("is_deleted",1);
            Map<String, Object> mp = setmealDishService.getMap(queryWrapper1);
            //如果有就返回失败
            if(mp!=null){
                throw new CustomException("删除失败，当前类型还有在售菜品");
            }
        }
        return removeById(id);
    }
}
