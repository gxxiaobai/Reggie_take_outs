package com.itheima.reggie.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.bean.Category;

public interface CategoryService extends IService<Category> {
    boolean remove(Long id);
}
