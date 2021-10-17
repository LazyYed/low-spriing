package com.lazy.spring.annotation;

import java.lang.annotation.*;

/**
 * @author lazyyedi@gamil.com
 * @creed: 我不能创造的东西，我就无法理解
 * @date 2021/10/17 下午9:01
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
    String value() default "";
}
