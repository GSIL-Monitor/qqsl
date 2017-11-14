package com.hysw.qqsl.cloud.annotation.util;

import java.lang.annotation.*;

/**
 * 购买服务是否过期
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IsExpire {
    String value() default "object";
}
