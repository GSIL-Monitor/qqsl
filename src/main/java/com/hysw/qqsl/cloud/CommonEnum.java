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
        QS("build","泉室","qs"),
        /**
         * 截水廊道
         */
        JSLD("build","截水廊道","jsld"),
        /**
         * 大口井
         */
        DKJ("build","大口井","dkj"),
        /**
         * 土井
         */
        TJ("build","土井","tj"),
        /**
         * 机井
         */
        JJ("build","机井","jj"),
        /**
         * 涝池
         */
        LC("build","涝池","lc"),
        /** 渠系建筑物 */
        /**
         * 闸
         */
        FSZ("build","闸","z"),
        /**
         * 倒虹吸
         */
        DHX("build","倒虹吸","dhx"),
        /**
         * 跌水
         */
        DS("build","跌水","ds"),
        /** 消力池 */
        XIAOLC("build","消力池","xlc"),
        /** 护坦 */
        HUT("build","护坦","ht"),
        /** 海漫 */
        HAIM("build","海漫","hm"),
        /**
         * 渡槽
         */
        DC("build","渡槽","dc"),
        /**
         * 涵洞
         */
        HD("build","涵洞","hd"),
        /**
         * 隧洞
         */
        SD("build","隧洞","sd"),
        /**
         * 农口
         */
        NK("build","农口","nk"),
        /**
         * 斗门
         */
        DM("build","斗门","dm"),
        /**
         * 公路桥
         */
        GLQ("build","公路桥","glq"),
        /**
         * 车便桥
         */
        CBQ("build","车便桥","cbq"),
        /**
         * 各级渠道
         */
        GJQD("build","各级渠道","gjqd"),
        /** 管道建筑物 */
        /**
         * 检查井
         */
        JCJ("build","检查井","jcj"),
        /**
         * 分水井
         */
        FSJ("build","分水井","fsj"),
        /**
         * 供水井
         */
        GSJ("build","供水井","gsj"),
        /**
         * 减压井
         */
        JYJ("build","减压井","jyj"),
        /**
         * 减压池
         */
        JYC("build","减压池","jyc"),
        /**
         * 排气井
         */
        PAIQJ("build","排气井","pqj"),
        /**
         * 放水井
         */
        FANGSJ("build","放水井","fsj"),
        /**
         * 蓄水池
         */
        XSC("build","蓄水池","xsc"),
        /**
         * 各级管道
         */
        GJGD("build","各级管道","gjgd"),
        /** 挡水建筑物 */
        /**
         * 防洪堤
         */
        FHD("line","防洪堤","fhd"),
        /**
         * 排洪渠
         */
        PHQ("line","排洪渠","phq"),
        /**
         * 挡墙
         */
        DANGQ("build","挡墙","dq"),
        /**
         * 淤地坝
         */
        YDB("build","淤地坝","ydb"),
        /**
         * 谷坊
         */
        GF("build","谷坊","gf"),
        /**
         * 溢洪道
         */
        @Deprecated
        YHD("build","溢洪道","yhd"),
        /** 节水建筑物 */
        /**
         * 滴灌
         */
        DG("build","滴灌","dg"),
        /**
         * 喷头
         */
        PT("build","喷头","pt"),
        /**
         * 给水栓
         */
        JSS("build","给水栓","gss"),
        /**
         * 施肥设施
         */
        SFSS("build","施肥设施","sfss"),
        /**
         * 过滤系统
         */
        GLXT("build","过滤系统","glxt"),
        /** 地籍 */
        /** 林地 */
        LD("build","林地","ld"),
        /** 耕地 */
        GD("build","耕地","gd"),
        /** 草地 */
        CD("build","草地","cd"),
        /** 居民区 */
        JMQ("build","居民区","jmq"),
        /** 工矿区 */
        GKQ("build","工矿区","gkq"),
        /** 电力 */
        DL("build","电力","dl"),
        /** 次级交通 */
        CJJT("build","次级交通","cjjt"),
        /** 河床 */
        HEC("build","河床","hc"),
        /** 水面 */
        SHUIM("build","水面","sm"),
        /** 水文测点 */
        /** 水位 */
        SHUIW("build","水位","sw"),
        /** 水文 */
        SHUIWEN("build","水文","sw"),
        /** 雨量 */
        YUL("build","雨量","yl"),
        /** 水质 */
        SHUIZ("build","水质","sz"),
        /** 其他 */
        /**
         * 泵站
         */
        BZ("build","泵站","bz"),
        /**
         * 电站厂房
         */
        DZCF("build","电站厂房","dzcf"),
        /**
         * 地质点
         */
        DIZD("build","地质点","dzd"),
        /** 其他 */
        TSD("build","其他","qt"),
        /** 线面 */
        /**
         * 普通点
         */
        POINT("line","普通点","ptd"),
        /**
         * 供水干管
         */
        GSGG("line","供水干管","gsgg"),
        /**
         * 供水支管
         */
        GSZG("line","供水支管","gszg"),
        /**
         * 供水斗管
         */
        GSDG("line","供水斗管","gsdg"),
        /**
         * 供水干渠
         */
        GSGQ("line","供水干渠","gsgq"),
        /**
         * 供水支渠
         */
        GSZQ("line","供水支渠","gszq"),
        /**
         * 供水斗渠
         */
        GSDQ("line","供水斗渠","ggdq"),
        /**
         * 排水干管
         */
        PSGG("line","排水干管","psgg"),
        /**
         * 排水支管
         */
        PSZG("line","排水支管","pszg"),
        /**
         * 排水斗管
         */
        PSDG("line","排水斗管","psdg"),
        /**
         * 排水干渠
         */
        PSGQ("line","排水干渠","psgq"),
        /**
         * 排水支渠
         */
        PSZQ("line","排水支渠","pszq"),
        /**
         * 排水斗渠
         */
        PSDQ("line","排水斗渠","psdq"),
        /**
         * 灌溉范围
         */
        GGFW("area","灌溉范围","ggfw"),
        /**
         * 保护范围
         */
        BHFW("area","保护范围","bhfw"),
        /**
         * 供水区域
         */
        GSQY("area","供水区域","gsqy"),
        /**
         * 治理范围
         */
        ZLFW("area","治理范围","zlfw"),
        /**
         * 库区淹没范围
         */
        KQYMFW("area","库区淹没范围","kqymfw"),
        /** 水域 */
        SHUIY("area","水域","sy"),
        /** 公共线面 */
        GONGGXM("line","公共线面","ggxm");
        //必须增加一个构造函数,变量,得到该变量的值
        private String  type="";
        private String typeC = "";
        private String abbreviate = "";


        CommonType(String type, String typeC,String abbreviate) {
            this.type = type;
            this.typeC = typeC;
            this.abbreviate = abbreviate;
        }

        public String getType() {
            return type;
        }

        public String getTypeC() {
            return typeC;
        }

        public String getAbbreviate() {
            return abbreviate;
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
        WATER_QUALITY_STATION,
        /** 施工现场 */
        CONSTRUCTION
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

