package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.bean.Category;
import com.itheima.reggie.bean.Dish;
import com.itheima.reggie.bean.DishFlavor;
import com.itheima.reggie.bean.SetmealDish;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Api(tags = "菜品控制器")
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 获取菜品列表
     *
     * @param page     当前页
     * @param pageSize 一页显示的数据条数
     * @param name     搜索时参数 可有可无
     * @return page对象
     */
    @ApiOperation("分页获取菜品列表")
    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name) {
        //先创建page
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        //创建菜品查询条件
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //有name则查询
        lambdaQueryWrapper.like(name != null, Dish::getName, name);
        //根据创建时间降序
        lambdaQueryWrapper.orderByDesc(Dish::getCreateTime);
        //查询出相应的page信息
        dishService.page(pageInfo, lambdaQueryWrapper);
        //将dish集合信息从pageInfo中提取出来
        List<Dish> dishList = pageInfo.getRecords();

        //用流的形式遍历dish集合获得dish对象 封装给dishDto返回收集打包成dtoList
        List<DishDto> dtoList = dishList.stream().map((dish) -> {
            //获得类型id
            Long categoryId = dish.getCategoryId();
            //创建类型查询条件
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
            //查询条件，只通过id查name
            queryWrapper.eq(Category::getId, categoryId);
            queryWrapper.select(Category::getName);
            Category one = categoryService.getOne(queryWrapper);

            DishDto dishDto = new DishDto();
            //将dish赋值给dishDto
            BeanUtils.copyProperties(dish, dishDto, "flavors", "categoryName", "copies");
            //将查询出的categoryName封装到dto对象
            dishDto.setCategoryName(one.getName());

            return dishDto;
        }).collect(Collectors.toList());
        //创建dto对象
        Page<DishDto> dtoPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        dtoPage.setRecords(dtoList);
        return R.success(dtoPage);
    }

    /**
     * 回显菜品数据
     *
     * @param id 菜品id
     * @return DishDto数据传输模型
     */
    @ApiOperation("回显菜品数据")
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable("id") Long id) {
        //创建查询条件
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        //菜品口味列表查询
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);
        DishDto dishDto = new DishDto();
        dishDto.setFlavors(list);
        //根据id查dish
        Dish dish = dishService.getById(id);
        //使用工具类BeanUtils把dish赋值到数据传输模型dto里
        BeanUtils.copyProperties(dish, dishDto);
        return R.success(dishDto);
    }

    /**
     * 新增菜品及其口味
     *
     * @param dishDto 菜品前后端传输数据模型
     * @return R结果集
     */
    @ApiOperation("新增菜品及其口味")
    @Transactional//注意要开启事务
    @PostMapping
    public R<String> addDish(@RequestBody DishDto dishDto) {
        //dto模型继承了dish直接添加就好
        dishService.save(dishDto);
        //获得口味列表
        List<DishFlavor> dishFlavors = dishDto.getFlavors();
        Long dishId = dishDto.getId();//口味列表里没有id 先取出来
        //新的流遍历方法 速度较快 后面收集起来打包成list返回给原数据
        dishFlavors = dishFlavors.stream().peek((dishFlavor) -> dishFlavor.setDishId(dishId)
        ).collect(Collectors.toList());
        //批量添加
        dishFlavorService.saveBatch(dishFlavors);
        return R.success("添加成功");
    }

    /**
     * 修改菜品以及口味方法
     *
     * @param dishDto 传输模型
     * @return R结果集
     */
    @ApiOperation("修改菜品及其口味")
    @Transactional
    @PutMapping
    public R<String> put(@RequestBody DishDto dishDto) {
        dishService.updateById(dishDto);
        //因为前端有可能把之前的口味都删掉
        //所以我们直接全删掉添加新口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //前端没传dishId，遍历流加上
        List<DishFlavor> flavors = dishDto.getFlavors().stream().peek((dishFlavor -> dishFlavor.setDishId(dishDto.getId()))).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
        return R.success("修改成功");
    }

    /**
     * 停售或起售菜品（支持批量）
     *
     * @param status 前端传的需要改成的状态
     * @param ids    一个或多个id
     * @return R结果集
     */
    @ApiOperation("停售或起售菜品（支持批量）")
    @Transactional
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable("status") Integer status, @RequestParam("ids") List<String> ids) {
        //批量修改状态需要list
        List<Dish> list = new ArrayList<>();
        for (String id : ids) {
            Dish dish = new Dish();
            //赋给状态和需要修改的id
            dish.setStatus(status);
            //改成long类型
            Long idl = Long.valueOf(id);
            dish.setId(idl);
            list.add(dish);
            LambdaUpdateWrapper<SetmealDish> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SetmealDish::getDishId, idl);
            updateWrapper.set(SetmealDish::getIsDeleted, status == 0 ? 1 : 0);
            setmealDishService.update(updateWrapper);
        }
        dishService.updateBatchById(list);

        return R.success("修改状态成功");
    }

    /**
     * 获取分类下的dish列表
     *
     * @param dish 前端返回的CategoryId使用dish接收
     * @return 存菜品的List集合
     */
    @ApiOperation("获取该分类下的菜品列表")
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        return R.success(dishService.lists(dish.getCategoryId()));
    }

    /**
     * 删除菜品
     * 删除菜品口味
     * 判断菜品关联的套餐
     *
     * @param ids 菜品的id（可变参数）
     * @return R结果集
     */
    @ApiOperation("删除菜品及其口味")
    @Transactional
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<String> ids) {
        //创建查询条件
        QueryWrapper<Dish> setmealDishQueryWrapper = new QueryWrapper<>();
        //统计是否有在售菜品
        setmealDishQueryWrapper.select("count(*)").eq("status", 1);
        setmealDishQueryWrapper.in("id", ids);
        Map<String, Object> map = dishService.getMap(setmealDishQueryWrapper);
        int o = Integer.parseInt(String.valueOf(map.get("count(*)")));
        //如果有的话返回失败
        if (o!=0) {
            return R.error("删除失败，有在售菜品");
        }
        //删除与菜品关联的口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(queryWrapper);
        //将套餐明细关联的菜品删掉
        LambdaQueryWrapper<SetmealDish> updateWrapper=new LambdaQueryWrapper<>();
        updateWrapper.in(SetmealDish::getDishId,ids);
        setmealDishService.remove(updateWrapper);
        //最后删除菜品
        dishService.removeBatchByIds(ids);
        return R.success("删除成功");
    }
}
