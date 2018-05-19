package com.hysw.qqsl.hzy;

/**
 * 公用枚举
 *
 * @since 2018年5月16日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 */
public class CommonEnum {

    /**
     * 河流级别
     */
    public enum RiverLevel {
        ONE,
        TWO,
        THREE,
        FOUR
    }

    /**
     * 行政区级别
     */
    public enum RegionLevel {
        // 省
        PROVINCE,
        // 市
        CITY,
        // 县
        COUNTY,
        // 乡
        TOWN,
        // 村
        VILLAGE
    }

    /**
     * 河长级别
     */
    public enum HzLevel {
        // 总河长
        MASTER,
        // 副总河长
        SLAVE,
        // 河长
        NORMAL
    }
}
