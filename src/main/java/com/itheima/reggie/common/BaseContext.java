package com.itheima.reggie.common;

/**
 * 基于ThreadLocal封装的工具类 用于保存当前登录的用户id和获取存储的id
 */
public class BaseContext {
    private static final ThreadLocal<Long> THREAD_LOCAL=new ThreadLocal<>();

    /**
     * 取值方法
     * @return 当前登录用户的id
     */
    public static Long getThreadLocal() {
        return THREAD_LOCAL.get();
    }

    /**
     * 赋值方法
     * @param id 当前登录用户的id
     */
    public static void setThreadLocal(Long id) {
        THREAD_LOCAL.set(id);
    }
}
