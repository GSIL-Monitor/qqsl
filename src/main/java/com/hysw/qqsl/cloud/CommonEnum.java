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
        QS("buildModel","泉室","qs","水源建筑物",true),
        /**
         * 截水廊道
         */
        JSLD("buildModel","截水廊道","jsld","水源建筑物",true),
        /**
         * 大口井
         */
        DKJ("buildModel","大口井","dkj","水源建筑物",true),
        /**
         * 土井
         */
        TJ("buildModel","土井","tj","水源建筑物",false),
        /**
         * 机井
         */
        JJ("buildModel","机井","jj","水源建筑物",false),
        /**
         * 涝池
         */
        LC("buildModel","涝池","lc","水源建筑物",false),
        /** 渠系建筑物 */
        /**
         * 闸
         */
        FSZ("buildModel","闸","z","渠系建筑物",true),
        /**
         * 倒虹吸
         */
        DHX("buildModel","倒虹吸","dhx","渠系建筑物",true),
        /**
         * 跌水
         */
        DS("buildModel","跌水","ds","渠系建筑物",true),
        /** 消力池 */
        XIAOLC("buildModel","消力池","xlc","渠系建筑物",true),
        /** 护坦 */
        HUT("buildModel","护坦","ht","渠系建筑物",false),
        /** 海漫 */
        HAIM("buildModel","海漫","hm","渠系建筑物",true),
        /**
         * 渡槽
         */
        DC("buildModel","渡槽","dc","渠系建筑物",true),
        /**
         * 涵洞
         */
        HD("buildModel","涵洞","hd","渠系建筑物",false),
        /**
         * 隧洞
         */
        SD("buildModel","隧洞","sd","渠系建筑物",false),
        /**
         * 农口
         */
        NK("buildModel","农口","nk","渠系建筑物",false),
        /**
         * 斗门
         */
        DM("buildModel","斗门","dm","渠系建筑物",false),
        /**
         * 公路桥
         */
        GLQ("buildModel","公路桥","glq","渠系建筑物",false),
        /**
         * 车便桥
         */
        CBQ("buildModel","车便桥","cbq","渠系建筑物",false),
        /**
         * 各级渠道
         */
        GJQD("buildModel","各级渠道","gjqd","渠系建筑物",false),
        /** 管道建筑物 */
        /**
         * 检查井
         */
        JCJ("buildModel","检查井","jcj","管道建筑物",false),
        /**
         * 分水井
         */
        FSJ("buildModel","分水井","fsj","管道建筑物",false),
        /**
         * 供水井
         */
        GSJ("buildModel","供水井","gsj","管道建筑物",false),
        /**
         * 减压井
         */
        JYJ("buildModel","减压井","jyj","管道建筑物",false),
        /**
         * 减压池
         */
        JYC("buildModel","减压池","jyc","管道建筑物",false),
        /**
         * 排气井
         */
        PAIQJ("buildModel","排气井","pqj","管道建筑物",false),
        /**
         * 放水井
         */
        FANGSJ("buildModel","放水井","fsj","管道建筑物",false),
        /**
         * 蓄水池
         */
        XSC("buildModel","蓄水池","xsc","管道建筑物",true),
        /**
         * 各级管道
         */
        GJGD("buildModel","各级管道","gjgd","管道建筑物",false),
        /** 挡水建筑物 */
        /**
         * 防洪堤
         */
        FHD("line","防洪堤","fhd","挡水建筑物",false),
        /**
         * 排洪渠
         */
        PHQ("line","排洪渠","phq","挡水建筑物",false),
        /**
         * 挡墙
         */
        DANGQ("line","挡墙","dq","挡水建筑物",true),
        /**
         * 淤地坝
         */
        YDB("buildModel","淤地坝","ydb","挡水建筑物",false),
        /**
         * 谷坊
         */
        GF("buildModel","谷坊","gf","挡水建筑物",true),
        /**
         * 溢洪道
         */
        @Deprecated
        YHD("buildModel","溢洪道","yhd","挡水建筑物",false),
        /** 节水建筑物 */
        /**
         * 滴灌
         */
        DG("buildModel","滴灌","dg","节水建筑物",false),
        /**
         * 喷头
         */
        PT("buildModel","喷头","pt","节水建筑物",false),
        /**
         * 给水栓
         */
        JSS("buildModel","给水栓","gss","节水建筑物",false),
        /**
         * 施肥设施
         */
        SFSS("buildModel","施肥设施","sfss","节水建筑物",false),
        /**
         * 过滤系统
         */
        GLXT("buildModel","过滤系统","glxt","节水建筑物",false),
        /** 地籍 */
        /** 林地 */
        LD("buildModel","林地","ld","地籍",false),
        /** 耕地 */
        GD("buildModel","耕地","gd","地籍",false),
        /** 草地 */
        CD("buildModel","草地","cd","地籍",false),
        /** 居民区 */
        JMQ("buildModel","居民区","jmq","地籍",false),
        /** 工矿区 */
        GKQ("buildModel","工矿区","gkq","地籍",false),
        /** 电力 */
        DL("buildModel","电力","dl","地籍",false),
        /** 次级交通 */
        CJJT("buildModel","次级交通","cjjt","地籍",false),
        /** 河床 */
        HEC("buildModel","河床","hc","地籍",false),
        /** 水面 */
        SHUIM("buildModel","水面","sm","地籍",false),
        /** 水文测点 */
        /** 水位 */
        SHUIW("buildModel","水位","sw","水文测点",false),
        /** 水文 */
        SHUIWEN("buildModel","水文","sw","水文测点",false),
        /** 雨量 */
        YUL("buildModel","雨量","yl","水文测点",false),
        /** 水质 */
        SHUIZ("buildModel","水质","sz","水文测点",false),
        /** 其他 */
        /**
         * 泵站
         */
        BZ("buildModel","泵站","bz","其他",false),
        /**
         * 电站厂房
         */
        DZCF("buildModel","电站厂房","dzcf","其他",false),
        /**
         * 地质点
         */
        DIZD("buildModel","地质点","dzd","其他",false),
        /** 其他 */
        TSD("buildModel","其他","qt","其他",false),
        /** 线面 */
        /**
         * 普通点
         */
        POINT("line","普通点","ptd","线",false),
        /**
         * 供水干管
         */
        GSGG("line","供水干管","gsgg","线",false),
        /**
         * 供水支管
         */
        GSZG("line","供水支管","gszg","线",false),
        /**
         * 供水斗管
         */
        GSDG("line","供水斗管","gsdg","线",false),
        /**
         * 供水干渠
         */
        GSGQ("line","供水干渠","gsgq","线",false),
        /**
         * 供水支渠
         */
        GSZQ("line","供水支渠","gszq","线",false),
        /**
         * 供水斗渠
         */
        GSDQ("line","供水斗渠","ggdq","线",false),
        /**
         * 排水干管
         */
        PSGG("line","排水干管","psgg","线",false),
        /**
         * 排水支管
         */
        PSZG("line","排水支管","pszg","线",false),
        /**
         * 排水斗管
         */
        PSDG("line","排水斗管","psdg","线",false),
        /**
         * 排水干渠
         */
        PSGQ("line","排水干渠","psgq","线",false),
        /**
         * 排水支渠
         */
        PSZQ("line","排水支渠","pszq","线",false),
        /**
         * 排水斗渠
         */
        PSDQ("line","排水斗渠","psdq","线",false),
        /**
         * 灌溉范围
         */
        GGFW("area","灌溉范围","ggfw","面",true),
        /**
         * 保护范围
         */
        BHFW("area","保护范围","bhfw","面",false),
        /**
         * 供水区域
         */
        GSQY("area","供水区域","gsqy","面",false),
        /**
         * 治理范围
         */
        ZLFW("area","治理范围","zlfw","面",false),
        /**
         * 库区淹没范围
         */
        KQYMFW("area","库区淹没范围","kqymfw","面",false),
        /** 水域 */
        SHUIY("area","水域","sy","面",false),
        /** 公共线面 */
        GONGGXM("line","公共线面","ggxm","线",false),
        /**
         * 管井
         */
        GUANJ("buildModel","管井","gj","水源建筑物",true),
        /**
         * 铺盖
         */
        PUG("buildModel","铺盖","pg","水源建筑物",true),
        /**
         * 水表井
         */
        SHUIBJ("buildModel","水表井","sbj","管道建筑物",true),
        /**
         * 渠道
         */
        QUD("line","渠道","qd","渠系建筑物",true);
        //必须增加一个构造函数,变量,得到该变量的值
        private String  type="";
        private String typeC = "";
        private String abbreviate = "";
        private String buildType = "";
        private boolean isModel = false;

        CommonType(String type, String typeC,String abbreviate,String buildType,boolean isModel) {
            this.type = type;
            this.typeC = typeC;
            this.abbreviate = abbreviate;
            this.buildType = buildType;
            this.isModel = isModel;
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

        public boolean isModel() {
            return isModel;
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
        /** 普通测站 */
        NORMAL_STATION,
        /** 水文站 */
        HYDROLOGIC_STATION,
        /** 雨量站 */
        RAINFALL_STATION,
        /** 水位站 */
        WATER_LEVEL_STATION,
        /** 水质站 */
        WATER_QUALITY_STATION,
        /** 门禁系统 */
        ACCESS_CONTROL
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

