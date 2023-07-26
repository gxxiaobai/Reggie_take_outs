package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.bean.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "地址管理")
@RestController
@RequestMapping("addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService service;


    /**
     * 添加用户收获地址
     * */
    @ApiOperation("添加用户收货地址")
    @PostMapping
    public R<String> saveAddress(@RequestBody AddressBook addressBook) {

        addressBook.setUserId(BaseContext.getThreadLocal());
        service.save(addressBook);
        return R.success("添加地址成功");
    }

    /**
     * 返回当前用户的所有地址
     */
    @ApiOperation("返回当前用户的所有地址")
    @GetMapping("list")
    public R<List<AddressBook>> queryAddress() {
        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressBook::getUserId, BaseContext.getThreadLocal());
        List<AddressBook> list = service.list(wrapper);
        for (AddressBook addressBook : list) {
            if(addressBook.getIsDefault()==1){
                return R.success(list);
            }
        }
        if(list.size()!=0){
            AddressBook addressBook = list.get(0);
            addressBook.setIsDefault(1);
            service.updateById(addressBook);
        }
        return R.success(list);
    }


    /*
     * 修改默认地址
     *1.获取要设置为默认地址的 地址id
     *2.将当前用户下所有的地址的isdefault字段设置为0
     *3.根据接受参数  获取地址对象,设置isdefault字段设置为1
     * 将
     * */
    @ApiOperation("将地址修改为默认地址")
    @PutMapping("default")
    public R<String> updateDefault(@RequestBody AddressBook addressBooks) {

        log.info("id是{}", addressBooks.getId());
        AddressBook addressBook = service.getById(addressBooks.getId());

        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(AddressBook::getIsDefault, 0);
        wrapper.eq(AddressBook::getUserId, BaseContext.getThreadLocal());
        service.update(wrapper);
        addressBook.setIsDefault(1);
        service.updateById(addressBook);
        return R.success("修改默认地址成功");
    }


    /*
     * 根据BaseContext.getLocal()获取当前用户的默认地址，并返回
     * */
    @ApiOperation("获取当前用户默认地址")
    @GetMapping("default")
    public R<AddressBook> queryDefault() {


        LambdaQueryWrapper<AddressBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AddressBook::getUserId, BaseContext.getThreadLocal());
        wrapper.eq(AddressBook::getIsDefault, 1);
        AddressBook one = service.getOne(wrapper);
        return R.success(one);
    }

    @GetMapping("{id}")
    public R<AddressBook> queryOne(@PathVariable Long id) {

        AddressBook addressBook = service.getById(id);

        return R.success(addressBook);
    }
/*
修改地址
 */
    @ApiOperation("修改用户地址")
    @PutMapping
    public R<String> updateAddress(@RequestBody AddressBook addressBook) {

        log.info("address{}", addressBook.getId());
        service.updateById(addressBook);
        return R.success("修改成功");
    }

    /*
    删除地址
    */
    @ApiOperation("删除用户地址")
    @DeleteMapping()
    public R<String> deletes(Long ids) {
        service.removeById(ids);
        return R.success("删除成功");
    }
}
