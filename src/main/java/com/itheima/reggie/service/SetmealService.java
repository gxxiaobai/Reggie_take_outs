package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.bean.Setmeal;
import com.itheima.reggie.dto.SetmealDto;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    List<SetmealDto> queryList(Long categoryId, Integer status);
}
