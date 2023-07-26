package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(urlPatterns = "/*", filterName = "checkLoginFilter")
public class CheckLoginFilter implements Filter {
    //路径匹配器
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.info("拦截成功");
        //转换成可用的request,response
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setCharacterEncoding("utf-8");
        request.setCharacterEncoding("utf-8");
        //获取当前请求路径
        String requestURI = request.getRequestURI();
        //把要放行的路径写到数组与当前请求路径作对比
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/upload",
                "/common/download",
                "/user/login",
                "/user/sendMsg",
                "/doc.html",
                "/webjars/**/",
                "/swagger-resources",
                "/v2/api-docs"
        };

        //调用检测方法判断是否需要放行
        if(check(requestURI,urls)){
            log.info("当前路径为:{}，已经放行",requestURI);
            filterChain.doFilter(request,response);
            return;
        }
        //判断用户是否已登录
        Long empId = (Long) request.getSession().getAttribute("employee");
        if (empId!=null){
            log.info("当前用户已登录，id为：{}",empId);
            BaseContext.setThreadLocal(empId);
            filterChain.doFilter(request,response);
            return;
        }

        Long userId = (Long) ( request.getSession().getAttribute("users"));
        if (userId!=null){
            log.info("当前用户已登录，id为：{}",userId);
            BaseContext.setThreadLocal(userId);
            filterChain.doFilter(request,response);
            return;
        }
        if(requestURI.equals("/front/page/pay.html")){
            response.sendRedirect("/front/page/login.html");
        }
        //如果用户未登录
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    /**
     * 检测当前路径是否需要放行
     * @param currentUrl 当前请求路径的url
     * @param urls 需要放行的url
     * @return 匹配成功返回true
     */
    private boolean check(String currentUrl, String[] urls) {
        for (String url : urls) {
            if (PATH_MATCHER.match(url,currentUrl)) {//匹配成功时 放行
                if(currentUrl.equals("/front/page/pay.html")){
                    continue;
                }
                return true;
            }
        }
        return false;
    }
}
