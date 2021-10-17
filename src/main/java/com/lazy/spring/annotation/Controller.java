package com.lazy.spring.annotation;

import java.lang.annotation.*;

/**
 * @author lazyyedi@gamil.com
 * @creed: 我不能创造的东西，我就无法理解
 * @date 2021/10/16 下午9:52
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {
    String value() default "";
}
