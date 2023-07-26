package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.bean.Category;
import com.itheima.reggie.common.R;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(tags = "套餐菜品类型")
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增方法
     *
     * @param category 前端包装好的分类对象
     * @return R结果集
     */
    @ApiOperation("添加新分类")
    @PostMapping
    public R<String> save(@RequestBody Category category) {

        categoryService.save(category);
        return R.success("添加成功");
    }

    /**
     * 获取分类列表
     *
     * @param page     当前页
     * @param pageSize 一页显示的数据条数
     * @return page对象
     */
    @ApiOperation("获取分类列表")
    @GetMapping("/page")
    public R<Page<Category>> page(int page, int pageSize) {
        Page<Category> pageInfo = new Page<>(page, pageSize);
        //根据排序规则排序
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);
        categoryService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 菜品分类修改
     *
     * @param category 前端传回的分类对象
     * @return R结果集
     */
    @ApiOperation("修改分类")
    @PutMapping
    public R<String> put(@RequestBody Category category) {
        categoryService.updateById(category);
        return R.success("修改成功");
    }

    /**
     * 删除菜品类型
     *
     * @param id 类型id
     * @return R结果集
     */
    @ApiOperation("删除分类")
    @DeleteMapping
    public R<String> deleteById(Long id) {
        categoryService.remove(id);
        return R.success("删除成功");

    }

    /**
     * 返回类型列表
     *
     * @param category
     * @return
     */
    @ApiOperation("返回类型列表")
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(category.getType() != null, Category::getType, category.getType());
        wrapper.eq(category.getId() != null, Category::getId, category.getId());
        wrapper.orderByAsc(Category::getSort);
        List<Category> list = categoryService.list(wrapper);
        if (list != null) {
            return R.success(list);
        }
        return R.error("查询失败");
    }
}
