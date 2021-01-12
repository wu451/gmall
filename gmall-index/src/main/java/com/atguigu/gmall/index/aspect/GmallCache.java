package com.atguigu.gmall.index.aspect;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})//注解作用位置
@Retention(RetentionPolicy.RUNTIME)//运行时注解
@Inherited//是否继承
@Documented//生成到文档
public @interface GmallCache {
    /**
     * 缓存key的前缀
     * key:prefix+":"+方法参数
     *
     * @return
     */
    String prefix() default "";

    /**
     * 指定缓存时间
     * 单位是分钟
     * 默认5分钟
     *
     * @return
     */
    int timeOut() default 5;

    /**
     * 为了反正缓存雪崩
     * 指定随机值范围
     * 默认5min
     *
     * @return
     */
    int random() default 5;

    /**
     * 为了防止缓存击穿
     * 让使用人员指定分布式的key的前缀
     * 默认lock
     * key:lock+":"+方法参数
     *
     * @return
     */
    String lock() default "lock:";
}
