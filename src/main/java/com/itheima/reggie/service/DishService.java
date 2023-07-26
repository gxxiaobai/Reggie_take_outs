package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.bean.Dish;
import com.itheima.reggie.dto.DishDto;

import java.util.List;

public interface DishService extends IService<Dish> {
    List<DishDto> lists(Long ids);
}
