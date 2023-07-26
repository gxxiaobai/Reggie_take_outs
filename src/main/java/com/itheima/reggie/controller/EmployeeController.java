package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.bean.Employee;
import com.itheima.reggie.common.R;
import com.itheima.reggie.service.EmployeeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;

@Api(tags = "员工控制器")
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录方法
     * @param employee 前端传回来的username和password
     * @param request 获取请求对象
     * @return R返回结果集对象
     */
    @ApiOperation("登录接口")
    @PostMapping("/login")
    public R<Employee> login(@RequestBody Employee employee, HttpServletRequest request){
        // 1.从数据库中查询用户名是否存在
        // 1.1创建queryWrapper对象
        LambdaQueryWrapper<Employee> queryWrapper =new LambdaQueryWrapper<>();
        // 1.2添加查询条件
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        // 1.3执行查询
        Employee emp = employeeService.getOne(queryWrapper);
        // 1.4检验用户是否存在 如不存在直接返回错误信息
        if(emp==null){
            return R.error("用户名不存在!!");
        }
        // 2.校验密码是否正确
        // 2.1使用md5算法加密前端密码
        String password= DigestUtils.md5DigestAsHex(employee.getPassword().getBytes(StandardCharsets.UTF_8));
        // 2.2校验密码 如不正确返回错误信息
        if(!emp.getPassword().equals(password)){
            return R.error("密码错误，请重新登录");
        }
        // 3.检验用户状态码是否被禁用（1正常 0禁用）
        if(emp.getStatus()==0){
            return R.error("当前用户被禁用，请联系管理员处理");//状态码为0返回错误信息
        }
        // 4.将用户ID存进session
        request.getSession().setAttribute("employee",emp.getId());
        // 5.登陆成功  回显数据
        return R.success(emp);
    }

    /**
     * 退出方法
     * @param request HttpServletRequest
     * @return R返回结果集对象
     */
    @ApiOperation("退出接口")
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        HttpSession session = request.getSession();
        //移除session域中的对象
        session.removeAttribute("employee");
        return R.success("登陆成功");
    }

    /**
     * 添加员工方法
     * @param employee 前端传来的新员工信息
     * @param session HttpSession
     * @return R返回结果集对象
     */
    @ApiOperation("添加员工")
    @PostMapping
    public R<String> save(@RequestBody Employee employee,HttpSession session){
        //补全员工信息
        //补全员工密码
        String strIdNumber = String.valueOf(employee.getIdNumber());
        byte[] password = strIdNumber.substring(12, 18).getBytes(StandardCharsets.UTF_8);//截取身份证后六位作为密码
        employee.setPassword(DigestUtils.md5DigestAsHex(password));
        //保存员工
        employeeService.save(employee);
        return R.success("员工添加成功");
    }

    /**
     * 分页查询员工信息
     * @param page 当前页
     * @param pageSize 每页显示条数
     * @param name 查询的名字（模糊查询）
     * @return 返回page对象
     */
    @ApiOperation("分页查询员工信息")
    @GetMapping("/page")
    public R<Page<Employee>> page(int page,int pageSize,String name){
        //创建查询对象
        Page<Employee> pageInfo =new Page<>(page,pageSize);
        //创建查询条件对象
        LambdaQueryWrapper<Employee> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加查询条件 并且判断他name是否为空
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),Employee::getUsername,name);
        //添加排序条件 升序 资历越老越靠前
        lambdaQueryWrapper.orderByAsc(Employee::getCreateTime);
        employeeService.page(pageInfo,lambdaQueryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 修改变员工信息
     * @param employee 员工信息
     * @param session HttpSession为了补全员工修改人
     * @return R返回结果集对象
     */
    @ApiOperation("修改员工信息")
    @PutMapping
    public R<String> putStatus(@RequestBody Employee employee,HttpSession session){
        boolean update = employeeService.updateById(employee);
        if (update){
            return R.success("修改成功");
        }
        return R.error("修改失败");
    }

    /**
     * 根据id回显数据
     * @param id 前端传的id
     * @return Emp对象
     */
    @ApiOperation("根据id回显数据")
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable("id") Long id){
//        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>();
//        queryWrapper.eq(Employee::getId,id);
//        queryWrapper.select(Employee::getUsername,
//                            Employee::getId,
//                            Employee::getIdNumber,
//                            Employee::getName,
//                            Employee::getPhone,
//                            Employee::getSex);
        Employee employee = employeeService.getById(id);
        if(employee==null){
            return R.error("没有查询到对应的员工信息");
        }
        return R.success(employee);
    }

}
