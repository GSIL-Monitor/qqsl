package com.hysw.qqsl.cloud;

import com.alipay.api.domain.Picture;
import com.hysw.qqsl.cloud.pay.service.aliPay.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 公共参数
 *
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 * @since 2015年8月10日
 */
public final class CommonAttributes {

    /**
     * 不可实例化
     */
    private CommonAttributes() {
    }

    /**
     * 当项目数量过多时,每次的项目请求数
     */
    public static final int LIMIT = 199;
    /**
     * 系统用户角色列表
     * 角色：管理员,系统用户,web用户,移动端用户,水文模型用户
     * admin:simple,user:simple,user:system,user:hydrology,account:simple
     */
    public static final String[] ROLES = {"admin:simple", "user:system", "user:simple", "user:hydrology", "account:simple"};

    /**
     * 协同工作的编辑权限
     */
    public static final String[] COOPERATES = {"VISIT_INVITE_ELEMENT", "VISIT_INVITE_FILE", "VISIT_PREPARATION_ELEMENT", "VISIT_PREPARATION_FILE", "VISIT_BUILDING_ELEMENT", "VISIT_BUILDING_FILE", "VISIT_MAINTENANCE_ELEMENT", "VISIT_MAINTENANCE_FILE"};

    /**
     * 阿里云API秘钥
     */
    public static final String ACCESSKEY_ID = "H6JSh0Yx0cPz2cGa";
    public static final String SECRET_ACCESSKEY = "0joCPK6L1S0KLxCnOwD2Gm3wulC7vG";
    /**
     * oss服务存储地址
     */
    public static final String END_POINT = "http://oss-cn-hangzhou.aliyuncs.com";
    public static final String BUCKET_NAME = "qqsl";
    public static final String BUCKET_IMAGE = "qqslimage";
    //	public static final String[] OSSIMAGE= {"http://localhost:8080/qqsl//","http://localhost:8080/qqsl/","http://qqslimage.oss-cn-hangzhou.aliyuncs.com/"};
    public static final String[] OSSIMAGE = {"http://localhost:8080/qqsl//", "http://localhost:8080/qqsl/", "http://qqslimage.oss-cn-hangzhou.aliyuncs.com/"};
    /**
     * 支付宝相关参数
     */
    public static final String OPEN_API = "https://openapi.alipay.com/gateway.do";
    public static final String APP_ID = "2017071907807771";
    public static final String RESULT_TYPE = "json";
    public static final String CHARSET = "UTF-8";
    public static final String SIGN_TYPE = "RSA2";
    //支付宝密钥
    public static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwjm4O865ntBwxVbrOBuXEBSogH5VynB9blwHlbsJ+1H0WeqrDvNZXeqBIE19SQau0ReSkMTKbZwTtIFF23ialwncECMRn7c2FmJkmkRAVD0Xok0XKqxAu++hnBVsXjfTSNuBJmRphWzZzOH0AbiGnXmw/5Gt5OBXSoV3FO1itV4MQDJAIxQnZ1mzWHcR7E7od35FxHFKVQ142nGeQohQy4EGa3BgDui/gvZzWMK0MGEBvYTL/z3av50Bq7V8zTTVfKWwiHjEQzVjQBOg7TwyUfEd0f2l3RHM5i0g4cF9XWe+6G8GQI4ZCLYVLZ4RwOzHX4tNZ1zao5Wk7jrSlwMsfQIDAQAB";
    public static final String APP_PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDKp90Gi7270FFl7FydBWrKyhh0XLV2G0HJ/STd7nZH2HZMlvkuyIrwuDtt3NKMuuk82Ym50ttVpwNVcqEmtlj+1w8Cm6Tvs6B7MPTQrHYps99NZnSo0EvSi6JXrlnAHdCZE03gDV7kSMjdeO/3nIEdiMbnB4ZNDwX9+bDhvN0lG/yccM9phzVgkas9V4ZMOmeHzx6GPC6t1MjylPuNjtG1sSm9OVbIWEcbWZdlF2oZyLXZR596aHm+D4mb/GAOCJCpFCJpBGO6Si5O+FlGGLp7ZrF0V8Q7CJICi8AXwQ+JhjTQIr0XbEWtE0xxcYCHueY2/noAuooQWpFNe28UnybvAgMBAAECggEAP/pyyug/BBYmPHk8W84kAtV+lu3V0+2S/YPPqcjoypHJ9zAKhvyE8K4ZBPwb9JXloHJFCsdIu2e4o7dGrQQQYJPgh0A/9/TLi1jPUTnBLDU/IB5iYhEwfs3aeLfwWbiP7GOtyDgwZv2bfF/70j40fPB7auBzQ8ykZaP4dau8XURCp87NPRricSSLKMnHN3NLNRMve4aRQLmneCAOFQFtImo8mo4SxYjFoFhWDluvGncXHeJ3uw5EjJM24Beyrdb33Bm5O1cVnEUtM99vR0bt/CVov5/mB7CLvCM4A9EMLso+OBSzMEDKYpD6G5Pus294Z9kvk9hdZ5IpMVxNOepW0QKBgQDuw9kj6n/Eh2rEO2m6fTVqdfStG9ljfMSHlda76KmYyynMxTsi1wbe6kiQS8OWQ3M+tdgnSM3wfIxqwmJTYgKoOOBegUr99qGwfDSoy6OHrDxzL9Nk+aVJhi0KAps/79e5PrB72UBOObJlUks7sLSn7EfEm/CMjtkhmf9K0iI/dQKBgQDZSL/LnjUzxi8jno+i9k9qykHsyqXyBvvQAJ5lyitZ+qV3fKY2q2pckmTc8o2kMug9l2B0CtOIG2490O74Fru479+P4Bk0CEURTEkOtAouGBmHR/LkpJBNv5Gpcv9Bm7rxO4pqjqvr7UlWBwvPzEdLBRUyZiaZmyXqTMtSiFPEUwKBgCZZMmEAYvEPxugpmrunLJMiyt+a33mJKo+UU17u6X5u8xG+g9b+rk3TV0BFyu4xeysRTdxRZzI+7taezegSj9aw++hx37eWizWrXVHXEzbRRQxDHDLVneSHNmirLoBAZ2eLWBEsPZXS0oJPi2HU6c8mtggv+5y3vMwWzdgYlAOZAoGAbwz+cXvnZxG4T/UfJkPK7SJ4NSSRUbR+CJ34Vr/QDknLPdloPfK4Bp4PjNkuySf3iFsQwd4ypJKYcmGRcRx1TxzR3v/DAdPkMOYTRL+BoHNSwNBl9LOiyQnK0ZbjnM2R6u7qXHGUrpz06VHqmIaoPVBYuAx7V/BynWAoXoMshN8CgYA0a4iHa7M2tvkE7JBH6tYCDmk9ZbU1ChOMg5YgEJ5fDTNhkMJml+uvjOtl7286OXoRvMnyoGa8GrBUIu205OXaNymGfYC7ec2MO1B0Nf89CHvvo8x1ctxr4PbXVpD9waeoptdoVSUpErcuHHSVye35Cz51T0oGG04D140cxXQ4PQ==";


    /**
     * sts服务相关参数
     */
    public static final String REGION_CN_HANGZHOU = "cn-hangzhou";
    public static final String STS_API_VERSION = "2015-04-01";
    public static final String ROLE_ACCESSKEY_ID = "LTAI7XrcW7gIc4Pt";
    public static final String ROLE_ACCESSKEY_SECRET = "3PZasIKsom4JUMaH1egzqXCTaywu5P";
    public static final String ROLE_ARN = "acs:ram::30150706:role/aliyunosstokengeneratorrole";
    public static final String ROLE_SESSION_NAME = "AliyunOSSTokenGeneratorUser";

    /**
     * openoffice服务
     */
    public static final String OPENOFFICE_IP = "218.244.134.139";
    public static final int OPENOFFICE_PORT = 8100;
    public static final String OFFICE_FILE_EXTENSION = "doc,docx,xls,xlsx,ppt,pptx";
    public static final String GET_FILE = "1,11,12,13,14,15,23,24,25,3,31,4";
    /**常用图片后缀 */
    public static final String PICTURE_FILE_EXTENSION = "jpg,jpeg,png,gif";
    /**
     * 短信服务
     */
    public static final String URL = "http://sapi.253.com/msg/HttpBatchSendSM";// 应用地址
    public static final String ACCOUNT = "Hysw88888";// 账号
    public static final String PSWD = "Hysw88888";// 密码
    public static final boolean NEEDSTATUS = true;// 是否需要状态报告，需要true，不需要false
    public static final String PRODUCT = null;// 产品ID
    public static final String EXTNO = null;// 扩展码

    public static final String[] NEWDIR = {"231A", "231B", "231C", "231D", "241A", "241B",
            "241C", "241D", "232A", "232B", "232C", "242A", "242B", "242C",
            "25A",};
    public static final String[] OLDDIR = {"2311", "2312", "2313", "2314", "2411", "2412",
            "2413", "2414", "2321", "2322", "2323", "2421", "2422", "2423",
            "251",};
    /**
     * qqsl.xml文件路径
     */
    public static final String QQSL_XML_PATH = "/qqsl.xml";
    /**
     * 树形结构顶级节点
     */
    public static final String TOP_TREE_ID = "999999";

    /**
     * unitService 用于测试的验证数据
     */
    //单元别名
    public static final String ALIAS = "1,11,111,112,12,121,122,13,131,132,14,141,142,15,151,152,2,21,211,212,22,23,231,232,24,241,242,25,3,31,32,4,41";
    //单元下的复合要素名
    public static final String ALIASES = "1;11;111,111A;112,112A;12;121,121A;122,122A;13;131,131A;132,132A;14;141,141A;142,142A;15;151,151A;152,152A;"
            + "2;2,2A;21A,21B;21A,21B,21C;211A,211B;211A,211B,211C;212A,212B,212C,212D,212E;212A,212B;212A,212B,212C;22,22A;23A,23B,23C;231A,231B,231C,231D;232,232A,232B,232C;241A,241B,241C,241D;242,242A,242B,242C;25A,25B;31,31A,31B,31C,31D,31E;232;24A,24B,24C;242;3;31;32,32A;4;41";
    //单元名
    public static final String UNIT_NAME = "招投标;设计;招投标文件;委托书-合同;施工;招投标文件;委托书-合同;监理;招投标文件;委托书-合同;质检;招投标文件;委托书-合同;其他;招投标文件;委托书-合同;项目前期;外业勘测;测量;水工建筑物;"
            + "地理信息;项目建议书;可研;设计文件;报告;估算书;图册;附件-地勘报告等;批复;国土-水利-专项批复;省发改委批复;财政批复;初设(实施);设计文件;报告;概算书;图册;附件-地勘报告等;"
            + "批复;国土-水利-专项批复;省发改委批复;财政批复;施工图;施工图设计文件;审查文件;建设期;施工阶段;业主;设计;施工;监理;质检;验收阶段;运营维护期;运营阶段";
    /**
     * elementGroupService 用于测试的数据
     */
    //要素类型
    public static final String[] ELEMENT_TYPE = {"text", "number", "date_region", "select", "area", "date", "tel", "email",
            "text_area", "label", "select_text", "coordinate", "coordinate_upload", "map", "checkBox", "file_upload", "DAQ", "section_upload", "field", "design"};
    //要素数据类型
    public static final String[] DATA_TYPE = {"", "agricultural", "buoymethod", "fixedvVolume", "motor", "getBuliding", "chamber", "reservair", "pipeline",
            "other", "investment", "agrDistrictSelect", "agrStyleSelect", "agrAreaSelect", "channel", "channelBuild", "diversion", "protection", "fence", "bridge",
            "treatment", "slope", "branch", "hyd", "vegetation", "shyd", "task", "dam", "waterSaving", "box"};
    //info编号
    public static final String[] INFO_ORDER = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "0"};
    //要素描述数据
    public static final String DESCRIPTION = "CONTACTS_OWN_MASTER,company:CONTACTS_OWN_MASTER,name,NOTE:"
            + "CONTACTS_OWN_MASTER,phone,NOTE:CONTACTS_OWN_MASTER,email,NOTE:CONTACTS_DESIGN,company,NOTE:"
            + "CONTACTS_DESIGN,qualify:CONTACTS_DESIGN,depart,NOTE:CONTACTS_DESIGN,master,NOTE:"
            + "CONTACTS_DESIGN,master_phone,NOTE:CONTACTS_DESIGN,master_email,NOTE:CONTACTS_DESIGN,name,NOTE:"
            + "CONTACTS_DESIGN,phone,NOTE:CONTACTS_DESIGN,email,NOTE:CONTACTS_CON,company,NOTE:"
            + "CONTACTS_CON,qualify:CONTACTS_CON,depart,NOTE:CONTACTS_CON,master,NOTE:CONTACTS_CON,master_phone,NOTE:"
            + "CONTACTS_CON,master_email,NOTE:CONTACTS_CON,name,NOTE:CONTACTS_CON,phone,NOTE:CONTACTS_CON,email,NOTE:"
            + "CONTACTS_SUP,company,NOTE:CONTACTS_SUP,qualify:CONTACTS_SUP,depart,NOTE:CONTACTS_SUP,master,NOTE:"
            + "CONTACTS_SUP,master_phone,NOTE:CONTACTS_SUP,master_email,NOTE:CONTACTS_SUP,name,NOTE:CONTACTS_SUP,phone,NOTE:"
            + "CONTACTS_SUP,email,NOTE:CONTACTS_QC,company,NOTE:CONTACTS_QC,qualify:CONTACTS_QC,depart,NOTE:"
            + "CONTACTS_QC,master,NOTE:CONTACTS_QC,master_phone,NOTE:CONTACTS_QC,master_email,NOTE:CONTACTS_QC,name,NOTE:"
            + "CONTACTS_QC,phone,NOTE:CONTACTS_QC,email,NOTE:CONTACTS_OTHER,company,NOTE:CONTACTS_OTHER,qualify:"
            + "CONTACTS_OTHER,depart,NOTE:CONTACTS_OTHER,master,NOTE:CONTACTS_OTHER,master_phone,NOTE:"
            + "CONTACTS_OTHER,master_email,NOTE:CONTACTS_OTHER,name,NOTE:CONTACTS_OTHER,phone,NOTE:CONTACTS_OTHER,email,NOTE:"
            + "CONTACTS_OWN_NAME,name,NOTE:CONTACTS_OWN_NAME,phone,NOTE:CONTACTS_OWN_NAME,email,NOTE:introduce:"
            + "CONTACTS_OWN_NAME:CONTACTS_OWN_NAME,name,NOTE:CONTACTS_OWN_NAME,phone,NOTE:CONTACTS_OWN_NAME,email,NOTE:"
            + "CO NTACTS_OWN_NAME:CONTACTS_OWN_NAME:CONTACTS_OWN_NAME,name:CONTACTS_OWN_NAME,phone:CONTACTS_OWN_NAME,email:CONTACTS_OWN_MASTER,master";
    public static final String[] STAGEE = {"VISIT_INVITE_ELEMENT", "VISIT_INVITE_FILE", "VISIT_PREPARATION_ELEMENT", "VISIT_PREPARATION_FILE", "VISIT_BUILDING_ELEMENT", "VISIT_BUILDING_FILE", "VISIT_MAINTENANCE_ELEMENT", "VISIT_MAINTENANCE_FILE", "VISIT_VIEW"};
    public static final String[] STAGEC = {"招投标要素", "招投标文件", "项目前期要素", "项目前期文件", "建设期要素", "建设期文件", "运营期要素", "运营期文件", "查看"};
    public static final String publicKeyApplication = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCrlidjOAuMWRuCJtlcaFhIjHyvV/JKQCUh+9Mc0qNuzFsc+GqDWJEgg2F8iXXRR35eI9lcmX6b6K9Et/GctUSnT1Djrc8xSmwNKQy4sRaylNJYxDgfhkKwjt/jCjDPQ7QpBKLXJIX6MzC2qZzxHnMt4wdY3v0vV122JSs+MM8MrwIDAQAB";
    public static final String privateKeyApplication = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKuWJ2M4C4xZG4Im" +
            "2VxoWEiMfK9X8kpAJSH70xzSo27MWxz4aoNYkSCDYXyJddFHfl4j2VyZfpvor0S3\n" +
            "8Zy1RKdPUOOtzzFKbA0pDLixFrKU0ljEOB+GQrCO3+MKMM9DtCkEotckhfozMLap\n" +
            "nPEecy3jB1je/S9XXbYlKz4wzwyvAgMBAAECgYBvvZYi6CGlIXZnmNMeeZlcjZi5\n" +
            "O6W+su07p1pBP+9MvWpsgF4k8S15pfV7e60tP8jMl736rr8j2zEgqEWBDw8Ib+n4\n" +
            "t7g5ojVejTqecmEca0xespzRf0QBiTuq+VZjsJUWu2P0MRSs5B3uzyN8c/rhI9RB\n" +
            "l3lBuH0UhYTNc5wDIQJBAOVW4TKiLvfj/4lLAUecxG8jPmXJP/anMXXcP875aSUa\n" +
            "Yn7z3klNTgWR7RppeGkmUYQaNwqKcrN/Z/Q8NldVU78CQQC/iI4Ar4FWzV1X6hlG\n" +
            "E8HLJ0c99JR5YwYkKGNECaTbBopHW32gs/24SgtV//RxkmLRVC+S0nM1+zkXdo/J\n" +
            "JsMRAkB4P9ymiulrqw2itERAg09wIp+mCSbTsoi5jrmaUaYzASmh/UaRUspVG1Wi\n" +
            "IqkzIRde18wCZ+OKgIRGSIgJtMDLAkB7aA6mnm4lcXh9FpZmMoCg1pPudVHczFK+\n" +
            "pU8gHV6cyJ/7zhUpoA8P2JVpOdtT+c+1tBpa7UHSPCXS2k9WeaUhAkBT9XKM6jON\n" +
            "04JelcJbFJQw8fqCU1PY+OlSbP/dNNO3U3tizx4p2f8/jr4h/PxZL+dsDHmjv4/T\n" +
            "uikL18z/GH0U\n";
    public static final String JDBCUser = "root";
    public static final String JDBCPassword = "@qqsl@";
    public static final String appliactionKey = "KWyTISNRNvdfcsFs";
    public static final String appliactionIv = "t4lVCFAkYLuRVRpN";
    public static final String tokenKey = "H5HygRjRODb1xrDu";
    public static final String tokenIv = "TGkx2SEWdWVVYSRy";

    //    最终的账号密码
    public static final String APPID = "wx3b65c94d27a017aa";
    public static final String APPSECRET = "7a58dca5a49d838e8051d2d1212fd1cf";
    public static final String MCHID = "1448719602";
    public static final String APIKEY = "dxgjrOQeEkBg4BAHdOhEjuTpZJQdu3E6";

//    测试账号密码
//    public static final String APPID = "wx0d8493d76fa4b58d";
//    public static final String APPSECRET = "0a638eccdd73d77f317c900afd01ea55";

    /**
     * 身份证图片信息转文字信息Code
     */
    public static final String APPCODE = "e8c5ee6948ed46048c0420b3a2aa5cf6";

    //    套餐等级限制，10以上套餐仅供企业用户购买
    public static final int PROJECTLIMIT = 10;
//    坐标数据条目限制，超过限制值将不能上传和替换
    public static final int COORDINATELIMIT = 2000;
    //    企业邮箱
    public static String MYEMAILACCOUNT = "leinuo@qingqingshuili.cn";
    //    企业邮箱密码
    public static String MYEMAILPASSWORD = "Ljb608403";
    // 转换文件大小
    public static final long CONVERT_MAX_SZIE = 36 * 1024 * 1024;

}

