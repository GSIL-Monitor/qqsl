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
        QS("builds","泉室","qs","水源建筑物"),
        /**
         * 截水廊道
         */
        JSLD("builds","截水廊道","jsld","水源建筑物"),
        /**
         * 大口井
         */
        DKJ("builds","大口井","dkj","水源建筑物"),
        /**
         * 土井
         */
        TJ("builds","土井","tj","水源建筑物"),
        /**
         * 机井
         */
        JJ("builds","机井","jj","水源建筑物"),
        /**
         * 涝池
         */
        LC("builds","涝池","lc","水源建筑物"),
        /** 渠系建筑物 */
        /**
         * 闸
         */
        FSZ("builds","闸","z","渠系建筑物"),
        /**
         * 倒虹吸
         */
        DHX("builds","倒虹吸","dhx","渠系建筑物"),
        /**
         * 跌水
         */
        DS("builds","跌水","ds","渠系建筑物"),
        /** 消力池 */
        XIAOLC("builds","消力池","xlc","渠系建筑物"),
        /** 护坦 */
        HUT("builds","护坦","ht","渠系建筑物"),
        /** 海漫 */
        HAIM("builds","海漫","hm","渠系建筑物"),
        /**
         * 渡槽
         */
        DC("builds","渡槽","dc","渠系建筑物"),
        /**
         * 涵洞
         */
        HD("builds","涵洞","hd","渠系建筑物"),
        /**
         * 隧洞
         */
        SD("builds","隧洞","sd","渠系建筑物"),
        /**
         * 农口
         */
        NK("builds","农口","nk","渠系建筑物"),
        /**
         * 斗门
         */
        DM("builds","斗门","dm","渠系建筑物"),
        /**
         * 公路桥
         */
        GLQ("builds","公路桥","glq","渠系建筑物"),
        /**
         * 车便桥
         */
        CBQ("builds","车便桥","cbq","渠系建筑物"),
        /**
         * 各级渠道
         */
        GJQD("builds","各级渠道","gjqd","渠系建筑物"),
        /** 管道建筑物 */
        /**
         * 检查井
         */
        JCJ("builds","检查井","jcj","管道建筑物"),
        /**
         * 分水井
         */
        FSJ("builds","分水井","fsj","管道建筑物"),
        /**
         * 供水井
         */
        GSJ("builds","供水井","gsj","管道建筑物"),
        /**
         * 减压井
         */
        JYJ("builds","减压井","jyj","管道建筑物"),
        /**
         * 减压池
         */
        JYC("builds","减压池","jyc","管道建筑物"),
        /**
         * 排气井
         */
        PAIQJ("builds","排气井","pqj","管道建筑物"),
        /**
         * 放水井
         */
        FANGSJ("builds","放水井","fsj","管道建筑物"),
        /**
         * 蓄水池
         */
        XSC("builds","蓄水池","xsc","管道建筑物"),
        /**
         * 各级管道
         */
        GJGD("builds","各级管道","gjgd","管道建筑物"),
        /** 挡水建筑物 */
        /**
         * 防洪堤
         */
        FHD("line","防洪堤","fhd","挡水建筑物"),
        /**
         * 排洪渠
         */
        PHQ("line","排洪渠","phq","挡水建筑物"),
        /**
         * 挡墙
         */
        DANGQ("line","挡墙","dq","挡水建筑物"),
        /**
         * 淤地坝
         */
        YDB("builds","淤地坝","ydb","挡水建筑物"),
        /**
         * 谷坊
         */
        GF("builds","谷坊","gf","挡水建筑物"),
        /**
         * 溢洪道
         */
        @Deprecated
        YHD("builds","溢洪道","yhd","挡水建筑物"),
        /** 节水建筑物 */
        /**
         * 滴灌
         */
        DG("builds","滴灌","dg","节水建筑物"),
        /**
         * 喷头
         */
        PT("builds","喷头","pt","节水建筑物"),
        /**
         * 给水栓
         */
        JSS("builds","给水栓","gss","节水建筑物"),
        /**
         * 施肥设施
         */
        SFSS("builds","施肥设施","sfss","节水建筑物"),
        /**
         * 过滤系统
         */
        GLXT("builds","过滤系统","glxt","节水建筑物"),
        /** 地籍 */
        /** 林地 */
        LD("builds","林地","ld","地籍"),
        /** 耕地 */
        GD("builds","耕地","gd","地籍"),
        /** 草地 */
        CD("builds","草地","cd","地籍"),
        /** 居民区 */
        JMQ("builds","居民区","jmq","地籍"),
        /** 工矿区 */
        GKQ("builds","工矿区","gkq","地籍"),
        /** 电力 */
        DL("builds","电力","dl","地籍"),
        /** 次级交通 */
        CJJT("builds","次级交通","cjjt","地籍"),
        /** 河床 */
        HEC("builds","河床","hc","地籍"),
        /** 水面 */
        SHUIM("builds","水面","sm","地籍"),
        /** 水文测点 */
        /** 水位 */
        SHUIW("builds","水位","sw","水文测点"),
        /** 水文 */
        SHUIWEN("builds","水文","sw","水文测点"),
        /** 雨量 */
        YUL("builds","雨量","yl","水文测点"),
        /** 水质 */
        SHUIZ("builds","水质","sz","水文测点"),
        /** 其他 */
        /**
         * 泵站
         */
        BZ("builds","泵站","bz","其他"),
        /**
         * 电站厂房
         */
        DZCF("builds","电站厂房","dzcf","其他"),
        /**
         * 地质点
         */
        DIZD("builds","地质点","dzd","其他"),
        /** 其他 */
        TSD("builds","其他","qt","其他"),
        /** 线面 */
        /**
         * 普通点
         */
        POINT("line","普通点","ptd","线"),
        /**
         * 供水干管
         */
        GSGG("line","供水干管","gsgg","线"),
        /**
         * 供水支管
         */
        GSZG("line","供水支管","gszg","线"),
        /**
         * 供水斗管
         */
        GSDG("line","供水斗管","gsdg","线"),
        /**
         * 供水干渠
         */
        GSGQ("line","供水干渠","gsgq","线"),
        /**
         * 供水支渠
         */
        GSZQ("line","供水支渠","gszq","线"),
        /**
         * 供水斗渠
         */
        GSDQ("line","供水斗渠","ggdq","线"),
        /**
         * 排水干管
         */
        PSGG("line","排水干管","psgg","线"),
        /**
         * 排水支管
         */
        PSZG("line","排水支管","pszg","线"),
        /**
         * 排水斗管
         */
        PSDG("line","排水斗管","psdg","线"),
        /**
         * 排水干渠
         */
        PSGQ("line","排水干渠","psgq","线"),
        /**
         * 排水支渠
         */
        PSZQ("line","排水支渠","pszq","线"),
        /**
         * 排水斗渠
         */
        PSDQ("line","排水斗渠","psdq","线"),
        /**
         * 灌溉范围
         */
        GGFW("area","灌溉范围","ggfw","面"),
        /**
         * 保护范围
         */
        BHFW("area","保护范围","bhfw","面"),
        /**
         * 供水区域
         */
        GSQY("area","供水区域","gsqy","面"),
        /**
         * 治理范围
         */
        ZLFW("area","治理范围","zlfw","面"),
        /**
         * 库区淹没范围
         */
        KQYMFW("area","库区淹没范围","kqymfw","面"),
        /** 水域 */
        SHUIY("area","水域","sy","面"),
        /** 公共线面 */
        GONGGXM("line","公共线面","ggxm","线"),
        /**
         * 管井
         */
        GUANJ("builds","管井","gj","水源建筑物"),;
        //必须增加一个构造函数,变量,得到该变量的值
        private String  type="";
        private String typeC = "";
        private String abbreviate = "";
        private String buildType = "";

        CommonType(String type, String typeC,String abbreviate,String buildType) {
            this.type = type;
            this.typeC = typeC;
            this.abbreviate = abbreviate;
            this.buildType = buildType;
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

        public String getBuildType() {
            return buildType;
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

