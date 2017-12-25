package com.hysw.qqsl.cloud;

/**
 * Created by flysic on 17-4-10.
 */
public class CommonEnum {

    /**
     * 审核枚举
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

    /**
     * 属性组与属性的状态枚举
     */
    public enum Status {
        //选择
        SELECT,
        //动态
        DYNAMIC,
        //组合(常用)
        NORMAL,
        //隐藏
        HIDE
    }

    /**
     * 所有点、建筑物、线面类型
     */
    public enum CommonType {
        /** 水源建筑物 */
        /**
         * 泉室
         */
        QS,
        /**
         * 截水廊道
         */
        JSLD,
        /**
         * 大口井
         */
        DKJ,
        /**
         * 土井
         */
        TJ,
        /**
         * 机井
         */
        JJ,
        /**
         * 涝池
         */
        LC,
        /** 渠系建筑物 */
        /**
         * 闸
         */
        FSZ,
        /**
         * 倒虹吸
         */
        DHX,
        /**
         * 跌水
         */
        DS,
        /** 消力池 */
        XIAOLC,
        /** 护坦 */
        HUT,
        /** 海漫 */
        HAIM,
        /**
         * 渡槽
         */
        DC,
        /**
         * 涵洞
         */
        HD,
        /**
         * 隧洞
         */
        SD,
        /**
         * 农口
         */
        NK,
        /**
         * 斗门
         */
        DM,
        /**
         * 公路桥
         */
        GLQ,
        /**
         * 车便桥
         */
        CBQ,
        /**
         * 各级渠道
         */
        GJQD,
        /** 管道建筑物 */
        /**
         * 检查井
         */
        JCJ,
        /**
         * 分水井
         */
        FSJ,
        /**
         * 供水井
         */
        GSJ,
        /**
         * 减压井
         */
        JYJ,
        /**
         * 减压池
         */
        JYC,
        /**
         * 排气井
         */
        PAIQJ,
        /**
         * 放水井
         */
        FANGSJ,
        /**
         * 蓄水池
         */
        XSC,
        /**
         * 各级管道
         */
        GJGD,
        /** 挡水建筑物 */
        /**
         * 防洪堤
         */
        FHD,
        /**
         * 排洪渠
         */
        PHQ,
        /**
         * 挡墙
         */
        DANGQ,
        /**
         * 淤地坝
         */
        YDB,
        /**
         * 谷坊
         */
        GF,
        /**
         * 溢洪道
         */
        YHD,
        /** 节水建筑物 */
        /**
         * 滴灌
         */
        DG,
        /**
         * 喷头
         */
        PT,
        /**
         * 给水栓
         */
        JSS,
        /**
         * 施肥设施
         */
        SFSS,
        /**
         * 过滤系统
         */
        GLXT,
        /** 地籍 */
        /** 林地 */
        LD,
        /** 耕地 */
        GD,
        /** 草地 */
        CD,
        /** 居民区 */
        JMQ,
        /** 工矿区 */
        GKQ,
        /** 电力 */
        DL,
        /** 次级交通 */
        CJJT,
        /** 河床 */
        HEC,
        /** 水面 */
        SHUIM,
        /** 水文测点 */
        /** 水位 */
        SHUIW,
        /** 水文 */
        SHUIWEN,
        /** 雨量 */
        YUL,
        /** 水质 */
        SHUIZ,
        /** 其他 */
        /**
         * 泵站
         */
        BZ,
        /**
         * 电站厂房
         */
        DZCF,
        /**
         * 地质点
         */
        DIZD,
        /** 其他 */
        TSD,
        /** 线面 */
        /**
         * 普通点
         */
        POINT,
        /**
         * 供水干管
         */
        GSGG,
        /**
         * 供水支管
         */
        GSZG,
        /**
         * 供水斗管
         */
        GSDG,
        /**
         * 供水干渠
         */
        GSGQ,
        /**
         * 供水支渠
         */
        GSZQ,
        /**
         * 供水斗渠
         */
        GSDQ,
        /**
         * 排水干管
         */
        PSGG,
        /**
         * 排水支管
         */
        PSZG,
        /**
         * 排水斗管
         */
        PSDG,
        /**
         * 排水干渠
         */
        PSGQ,
        /**
         * 排水支渠
         */
        PSZQ,
        /**
         * 排水斗渠
         */
        PSDQ,
        /**
         * 灌溉范围
         */
        GGFW,
        /**
         * 保护范围
         */
        BHFW,
        /**
         * 供水区域
         */
        GSQY,
        /**
         * 治理范围
         */
        ZLFW,
        /**
         * 库区淹没范围
         */
        KQYMFW,
        /** 水域 */
        SHUIY,
        /** 公共线面 */
        GONGGXM;

        public static CommonType valueOf(int ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                throw new IndexOutOfBoundsException("Invalid ordinal");
            }
            return values()[ordinal];
        }
    }



    public enum CertifyStatus{
        /** 未认证 */
        UNAUTHEN,
        /** 认证中 */
        AUTHEN,
        /** 认证通过 */
        PASS,
        /** 认证失败 */
        NOTPASS,
        /** 即将过期 */
        EXPIRING,
        /** 已过期 */
        EXPIRED
    }

    public enum StationType{
        /** 水文站 */
        HYDROLOGIC_STATION,
        /** 雨量站 */
        RAINFALL_STATION,
        /** 水位站 */
        WATER_LEVEL_STATION,
        /** 水质站 */
        WATER_QUALITY_STATION

    }

    /**
     * 套餐类型
     */
    public enum PackageType{
        TEST,
//        EXPERIENCE,
        YOUTH,
        SUN,
        SUNRISE
    }

    /**
     * 数据服务类型
     */
    public enum GoodsType{
        PANORAMA,
        AIRSURVEY
    }

    /**
     * 站内信状态
     */
    public enum MessageStatus {
        UNREAD,
        READED
    }

}

