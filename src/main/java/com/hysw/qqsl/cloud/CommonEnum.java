package com.hysw.qqsl.cloud;

import com.hysw.qqsl.cloud.core.entity.Message;

/**
 * Created by flysic on 17-4-10.
 */
public class CommonEnum {

    /**
     * 反馈状态
     */
    public enum FeedbackStatus {
        /** 待处理 */
        PENDING,
        /** 已回复 */
        REVIEWED;
        // 已提交
        //SUBMIT,
        // 预审通过
        // AUDIT,
        // 已采纳
        //ADOPTION,
        // 已实现
        //IMPLEMENT,
        // 未采纳
        //NO_ADOPTION
        public static FeedbackStatus valueOf(int ordinal) {
            if (ordinal < 0 || ordinal >= values().length) {
                throw new IndexOutOfBoundsException("Invalid ordinal");
            }
            return values()[ordinal];
        }
    }

    /**
     * 审核枚举
     */
    public enum Review {
        /** 待审核 */
        PENDING,
        /** 未通过 */
        NOTPASS,
        /** 通过 */
        PASS;
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
        QS("build","泉室"),
        /**
         * 截水廊道
         */
        JSLD("build","截水廊道"),
        /**
         * 大口井
         */
        DKJ("build","大口井"),
        /**
         * 土井
         */
        TJ("build","土井"),
        /**
         * 机井
         */
        JJ("build","机井"),
        /**
         * 涝池
         */
        LC("build","涝池"),
        /** 渠系建筑物 */
        /**
         * 闸
         */
        FSZ("build","闸"),
        /**
         * 倒虹吸
         */
        DHX("build","倒虹吸"),
        /**
         * 跌水
         */
        DS("build","跌水"),
        /** 消力池 */
        XIAOLC("build","消力池"),
        /** 护坦 */
        HUT("build","护坦"),
        /** 海漫 */
        HAIM("build","海漫"),
        /**
         * 渡槽
         */
        DC("build","渡槽"),
        /**
         * 涵洞
         */
        HD("build","涵洞"),
        /**
         * 隧洞
         */
        SD("build","隧洞"),
        /**
         * 农口
         */
        NK("build","农口"),
        /**
         * 斗门
         */
        DM("build","斗门"),
        /**
         * 公路桥
         */
        GLQ("build","公路桥"),
        /**
         * 车便桥
         */
        CBQ("build","车便桥"),
        /**
         * 各级渠道
         */
        GJQD("build","各级渠道"),
        /** 管道建筑物 */
        /**
         * 检查井
         */
        JCJ("build","检查井"),
        /**
         * 分水井
         */
        FSJ("build","分水井"),
        /**
         * 供水井
         */
        GSJ("build","供水井"),
        /**
         * 减压井
         */
        JYJ("build","减压井"),
        /**
         * 减压池
         */
        JYC("build","减压池"),
        /**
         * 排气井
         */
        PAIQJ("build","排气井"),
        /**
         * 放水井
         */
        FANGSJ("build","放水井"),
        /**
         * 蓄水池
         */
        XSC("build","蓄水池"),
        /**
         * 各级管道
         */
        GJGD("build","各级管道"),
        /** 挡水建筑物 */
        /**
         * 防洪堤
         */
        FHD("line","防洪堤"),
        /**
         * 排洪渠
         */
        PHQ("line","排洪渠"),
        /**
         * 挡墙
         */
        DANGQ("build","挡墙"),
        /**
         * 淤地坝
         */
        YDB("build","淤地坝"),
        /**
         * 谷坊
         */
        GF("build","谷坊"),
        /**
         * 溢洪道
         */
        @Deprecated
        YHD("build","溢洪道"),
        /** 节水建筑物 */
        /**
         * 滴灌
         */
        DG("build","滴灌"),
        /**
         * 喷头
         */
        PT("build","喷头"),
        /**
         * 给水栓
         */
        JSS("build","给水栓"),
        /**
         * 施肥设施
         */
        SFSS("build","施肥设施"),
        /**
         * 过滤系统
         */
        GLXT("build","过滤系统"),
        /** 地籍 */
        /** 林地 */
        LD("build","林地"),
        /** 耕地 */
        GD("build","耕地"),
        /** 草地 */
        CD("build","草地"),
        /** 居民区 */
        JMQ("build","居民区"),
        /** 工矿区 */
        GKQ("build","工矿区"),
        /** 电力 */
        DL("build","电力"),
        /** 次级交通 */
        CJJT("build","次级交通"),
        /** 河床 */
        HEC("build","河床"),
        /** 水面 */
        SHUIM("build","水面"),
        /** 水文测点 */
        /** 水位 */
        SHUIW("build","水位"),
        /** 水文 */
        SHUIWEN("build","水文"),
        /** 雨量 */
        YUL("build","雨量"),
        /** 水质 */
        SHUIZ("build","水质"),
        /** 其他 */
        /**
         * 泵站
         */
        BZ("build","泵站"),
        /**
         * 电站厂房
         */
        DZCF("build","电站厂房"),
        /**
         * 地质点
         */
        DIZD("build","地质点"),
        /** 其他 */
        TSD("build","其他"),
        /** 线面 */
        /**
         * 普通点
         */
        POINT("line","普通点"),
        /**
         * 供水干管
         */
        GSGG("line","供水干管"),
        /**
         * 供水支管
         */
        GSZG("line","供水支管"),
        /**
         * 供水斗管
         */
        GSDG("line","供水斗管"),
        /**
         * 供水干渠
         */
        GSGQ("line","供水干渠"),
        /**
         * 供水支渠
         */
        GSZQ("line","供水支渠"),
        /**
         * 供水斗渠
         */
        GSDQ("line","供水斗渠"),
        /**
         * 排水干管
         */
        PSGG("line","排水干管"),
        /**
         * 排水支管
         */
        PSZG("line","排水支管"),
        /**
         * 排水斗管
         */
        PSDG("line","排水斗管"),
        /**
         * 排水干渠
         */
        PSGQ("line","排水干渠"),
        /**
         * 排水支渠
         */
        PSZQ("line","排水支渠"),
        /**
         * 排水斗渠
         */
        PSDQ("line","排水斗渠"),
        /**
         * 灌溉范围
         */
        GGFW("area","灌溉范围"),
        /**
         * 保护范围
         */
        BHFW("area","保护范围"),
        /**
         * 供水区域
         */
        GSQY("area","供水区域"),
        /**
         * 治理范围
         */
        ZLFW("area","治理范围"),
        /**
         * 库区淹没范围
         */
        KQYMFW("area","库区淹没范围"),
        /** 水域 */
        SHUIY("area","水域"),
        /** 公共线面 */
        GONGGXM("line","公共线面");
        //必须增加一个构造函数,变量,得到该变量的值
        private String  type="";
        private String typeC = "";

        CommonType(String type, String typeC) {
            this.type = type;
            this.typeC = typeC;
        }

        public String getType() {
            return type;
        }

        public String getTypeC() {
            return typeC;
        }

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

