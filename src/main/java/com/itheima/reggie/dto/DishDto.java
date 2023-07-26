package com.itheima.reggie.dto;

import com.itheima.reggie.bean.Dish;
import com.itheima.reggie.bean.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;

    private Integer saleNumber;
}
