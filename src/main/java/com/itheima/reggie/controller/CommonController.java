package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;

@Api(tags = "工具控制器")
@RestController
@RequestMapping("/common")
public class CommonController {
    @Value("${reggie.path}")
    private String basePath;

    @SneakyThrows
    @ApiOperation("上传图片接口")
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {//file是一个临时文件 需要转存到其他位置 否则本次请求结束就会删除
        //获取文件后缀名
        String originalFilename = file.getOriginalFilename();
        System.out.println(originalFilename);
        String s = originalFilename.substring(originalFilename.lastIndexOf("."));
        //使用UUID作为文件名 防止文件被覆盖
        String filename = UUID.randomUUID() + s;
        //创建一个目录对象
        File dir = new File(basePath);
        //判断当前目录是否存在
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //上传到指定位置
        file.transferTo(new File(basePath + filename));
        return R.success(filename);
    }

    @SneakyThrows
    @ApiOperation("下载图片接口")
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        //创建输出流
        FileInputStream inputStream = new FileInputStream(basePath + name);
        //通过响应创建输入流
        ServletOutputStream outputStream = response.getOutputStream();
        //设置响应的文本内容

        int i = 0;
        //写入数据
        byte[] bytes = new byte[1024];
        //循环读取数据
        while ((i = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, i);
            outputStream.flush();
        }
        //关闭输入输出流
        inputStream.close();
        outputStream.close();

    }

}