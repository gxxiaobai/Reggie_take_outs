package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@Slf4j
@EnableTransactionManagement//开启事务扫描
@ServletComponentScan("com.itheima.reggie.filter")
public class ReggieApplicationBoot {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplicationBoot.class,args);
        log.info("项目启动成功...");
    }
}
