package com.hysw.qqsl.cloud.entity;

/**
 * 审核枚举
 *
 * @since 2016年12月1３日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public enum Review {
     //　待审核，未通过，通过
     PENDING, NOTPASS, PASS;
    public static Review valueOf(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
        return values()[ordinal];
    }
}
