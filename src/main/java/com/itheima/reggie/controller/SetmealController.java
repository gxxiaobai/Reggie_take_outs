package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.bean.Category;
import com.itheima.reggie.bean.Dish;
import com.itheima.reggie.bean.Setmeal;
import com.itheima.reggie.bean.SetmealDish;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "套餐控制器")
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private DishService dishService;

    /**
     * 套餐分页列表
     *
     * @param page     当前页
     * @param pageSize 页显示条数
     * @param name     套餐名（可模糊）
     * @return R数据展示模型
     */
    @ApiOperation("分页套餐列表")
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        //创建查询条件 有name查name
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(name != null, Setmeal::getName, name);
        setmealService.page(pageInfo, lambdaQueryWrapper);
        //用流循环原始数据setmeal查出套餐类型赋给数据展示模型dto
        List<SetmealDto> records = pageInfo.getRecords().stream().map((setmeal) -> {
                    //创建查询条件
                    LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(Category::getId, setmeal.getCategoryId());
                    //只查name
                    queryWrapper.select(Category::getName);
                    Category one = categoryService.getOne(queryWrapper);
                    SetmealDto setmealDto = new SetmealDto();
                    //把原始数据复制到dto中
                    BeanUtils.copyProperties(setmeal, setmealDto);
                    //把套餐类型名称赋值给dto
                    setmealDto.setCategoryName(one.getName());
                    return setmealDto;
                }
        ).collect(Collectors.toList());
        Page<SetmealDto> dtoPage = new Page<>();
        //复制page中数据给dto模型
        BeanUtils.copyProperties(pageInfo, dtoPage);
        //把list数据给dtoPage
        dtoPage.setRecords(records);
        return R.success(dtoPage);
    }

    /**
     * 修改套餐回显数据
     *
     * @param id 套餐id
     * @return R传输数据模型
     */
    @ApiOperation("根据id回显套餐数据")
    @GetMapping("/{id}")
    public R<SetmealDto> add(@PathVariable Long id) {
        //先把套餐基础数据查出来
        Setmeal setmeal = setmealService.getById(id);
//        //创建类型查询条件
//        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
//        queryWrapper.eq(Category::getId,setmeal.getCategoryId());
//        //只需要name
//        queryWrapper.select(Category::getName);
//        Category one = categoryService.getOne(queryWrapper);
        //创建数据模型并把基础数据复制进去
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
//        //把类型名字赋值给dto
//        setmealDto.setCategoryName(one.getName());
        //把套餐明细查出来封装到dto
        LambdaQueryWrapper<SetmealDish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishList = setmealDishService.list(dishQueryWrapper);
        setmealDto.setSetmealDishes(setmealDishList);
        return R.success(setmealDto);
    }

    /**
     * 修改套餐
     *
     * @param setmealDto 有setmealDish 所以用传输数据模型接收
     * @return R结果集
     */
    @ApiOperation("修改套餐信息")
    @PutMapping
    @Transactional
    public R<String> put(@RequestBody SetmealDto setmealDto) {
        //创建修改条件先修改setmeal
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Setmeal::getId, setmealDto.getId());
        setmealService.update(setmealDto, updateWrapper);
        //将dto中的setmealDish集合取出来
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //创建查询条件对象 删除原来的套餐菜品（如果修改需要判断是否删除了或者增加了）
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //把setmeal的id注入到setmealDish中 然后批量加入
        List<SetmealDish> collect = setmealDishes.stream().peek(setmealDish -> {
            setmealDish.setSetmealId(setmealDto.getId());
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(collect);
        return R.success("修改成功");
    }

    /**
     * 增加套餐
     *
     * @param setmealDto 有setmealDish 所以用传输数据模型接收
     * @return R结果集
     */
    @ApiOperation("增加套餐")
    @Transactional
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        //保存基础数据
        setmealService.save(setmealDto);
        //注入setmealId
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes().stream().peek(setmealDish -> {
            setmealDish.setSetmealId(setmealDto.getId());
        }).collect(Collectors.toList());
        //批量保存套餐详情菜品
        setmealDishService.saveBatch(setmealDishes);
        return R.success("新增成功");
    }

    /**
     * 修改套餐状态的方法
     *
     * @param status 套餐要修改的状态
     * @param ids    要修改的套餐（可能是多个）
     * @return R结果集
     */
    @ApiOperation("起售或停售套餐（支持批量）")
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable("status") int status, @RequestParam("ids") List<String> ids) {
        //创建一个list集合批量修改
        List<Setmeal> list = new ArrayList<>();
        for (String id : ids) {
            Setmeal setmeal = new Setmeal();
            //赋给状态和需要修改的id
            setmeal.setStatus(status);
            //改成long类型
            Long idl = Long.valueOf(id);
            setmeal.setId(idl);
            list.add(setmeal);
        }
        setmealService.updateBatchById(list);
        return R.success("修改成功");
    }

    /**
     * 删除套餐及其明细菜品关系
     *
     * @param ids 套餐id（允许多个）
     * @return R结果集
     */
    @ApiOperation("删除套餐及其明细菜品")
    @DeleteMapping
    @Transactional
    public R<String> delete(@RequestParam("ids") List<Long> ids) {
        //创建查询条件
        QueryWrapper<Setmeal> setmealQueryWrapper = new QueryWrapper<>();
        setmealQueryWrapper.in("id", ids);
        //判断ids里是否有在售商品
        setmealQueryWrapper.eq("status", 1);
        //拼接查询统计
        setmealQueryWrapper.select("count(*)");
        Map<String, Object> map = setmealService.getMap(setmealQueryWrapper);
        int o = Integer.parseInt(String.valueOf(map.get("count(*)")));
        //如果有在售商品就返回失败
        if (o != 0) {//查到0个说明没有在售
            return R.error("删除失败，有在售套餐");
        }
        //删除套餐关系菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(queryWrapper);
        //删除套餐
        setmealService.removeBatchByIds(ids);
        return R.success("删除成功");
    }
    @ApiOperation("根据分类id查询套餐列表")
    @GetMapping("list")
    public R<List<SetmealDto>> querySetmeal(Long categoryId, Integer status) {
        List<SetmealDto> setmealDtos = setmealService.queryList(categoryId, status);
        if (setmealDtos!=null){
            return R.success(setmealDtos);
        }
        return R.error("查询失败了");

    }
    @ApiOperation("根据菜品id或套餐id查询菜品列表")
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> dish(@PathVariable("id") Long id) {
        List<DishDto> list=new ArrayList<>();
        //创建查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //根据id查询一个菜品
        queryWrapper.eq(Dish::getId, id);
        Dish one = dishService.getOne(queryWrapper);
        if (one == null) {
            //如果菜品为空说明是套餐
//            LambdaQueryWrapper<Setmeal> queryWrapper1 = new LambdaQueryWrapper<>();
//            queryWrapper1.eq(Setmeal::getId, id);
//            queryWrapper1.select(Setmeal::getId);
//            //查询套餐id
//            Setmeal one1 = setmealService.getOne(queryWrapper1);
            LambdaQueryWrapper<SetmealDish> queryWrapper2 = new LambdaQueryWrapper<>();
            //根据套餐代码查询套餐明细
            queryWrapper2.eq(SetmealDish::getSetmealId, id);
            queryWrapper2.select(SetmealDish::getDishId,SetmealDish::getCopies);
            List<SetmealDish> list1 = setmealDishService.list(queryWrapper2);
            //根据套餐明细去查菜品并返回
            list = list1.stream().map(setmealDish -> {
                queryWrapper.clear();
                queryWrapper.eq(Dish::getId, setmealDish.getDishId());
                Dish one1 = dishService.getOne(queryWrapper);
                DishDto dishDto = new DishDto();
                BeanUtils.copyProperties(one1,dishDto);
                dishDto.setCopies(setmealDish.getCopies());
                return dishDto;
            }).collect(Collectors.toList());
        }else {
            //如果是菜品直接添加
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(one,dishDto);
            list.add(dishDto);
        }
        return R.success(list);
    }
}
