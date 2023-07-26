package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;


/**
 * 在添加修改时 自动补全信息
 */
@Component
@Slf4j
public class MyMetaObjectHandle implements MetaObjectHandler {
    @Autowired
    HttpSession session;
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("添加时补全信息");
        metaObject.setValue("createTime",LocalDateTime.now());
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("createUser",BaseContext.getThreadLocal());
        metaObject.setValue("updateUser",BaseContext.getThreadLocal());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("修改时补全信息");
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("updateUser",BaseContext.getThreadLocal());
    }
}
