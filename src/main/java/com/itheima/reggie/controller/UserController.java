package com.itheima.reggie.controller;

import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.exception.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.common.ValidateCodeUtils;
import com.itheima.reggie.bean.User;
import com.itheima.reggie.service.UserService;

import com.itheima.reggie.common.SMSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Api(tags = "用户控制器")
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService service;

    /*
    验证码校验功能   如果手机号不为空，响应验证码，并将手机号和验证码存入session
     */
    @ApiOperation("发送验证码校验")
    @PostMapping("sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        String phone = user.getPhone();
        if (phone!=null){
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
             code="1234";
            log.info("生成的验证码是{}",code);
            try {
                SendSmsResponse sendSmsResponse = SMSUtils.sendSms(phone, code);
                if (sendSmsResponse.body.getMessage().equals("OK")&&sendSmsResponse.getStatusCode()==200){
                    session.setAttribute(phone,code);
                    return R.success("验证码已发送");
                }
            } catch (Exception e) {
                session.setAttribute(phone,code);
                throw new CustomException("短信发送失败");
            }
        }
      return R.error("手机号格式错误");
    }

        /*

        登录验证功能
        使用map集合接收参数   对手机号和验证码进行校验，如果校验成功，将userId存入session
         */

    @ApiOperation("登录验证功能")
    @PostMapping("login")
    public R<User> logins(@RequestBody Map<String,String> map,HttpSession session){

        String phone1 = map.get("phone");

        String code = map.get("code");
        log.info("手机号是{}，验证码是{}",phone1,code);
        Object codeSession = session.getAttribute(phone1);
        if (codeSession!=null && codeSession.equals(code)){
            LambdaQueryWrapper<User> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone,phone1);
            User user = service.getOne(wrapper);
            if (user==null){
                 user=new User();
                user.setPhone(phone1);
                user.setName("用户"+phone1.substring(7));
                service.save(user);
                user=service.getOne(wrapper);
            }
            session.setAttribute("users",user.getId());
            session.removeAttribute(phone1);//如果验证成功，销毁session
            System.out.println(user.getName());
            return R.success(user);
        }
        return R.error("验证码错误，请重新输入！");
    }

    /**
    * 退出功能，销毁session中存储的数据
    * */
    @ApiOperation("退出功能")
    @PostMapping("loginout")
    public R<String> loginOut(HttpSession session){
        session.removeAttribute("users");
        return R.success("退出成功");
    }

    /**
     * 修改资料回显数据
     * @return RUser结果集
     */
    @ApiOperation("根据本地线程回显用户数据")
    @GetMapping("queryUser")
    public R<User> queryUser(){
        Long userId = BaseContext.getThreadLocal();
        LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId,userId);
        User one = service.getOne(queryWrapper);
        return R.success(one);
    }
    @ApiOperation("修改用户信息")
    @PutMapping
    public R<String> updateUser(@RequestBody User user){
        System.out.println(user);
        user.setId(BaseContext.getThreadLocal());
        LambdaUpdateWrapper<User> updateWrapper=new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId,user.getId());
        service.updateById(user);
        return R.success("修改成功");
    }
}
