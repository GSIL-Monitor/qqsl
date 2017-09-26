package com.hysw.qqsl.cloud.wechat.util;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by chenl on 17-6-29.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface XStreamCDATA {
}
