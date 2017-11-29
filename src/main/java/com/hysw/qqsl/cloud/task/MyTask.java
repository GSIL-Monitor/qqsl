package com.hysw.qqsl.cloud.task;

import com.hysw.qqsl.cloud.core.entity.Note;
import com.hysw.qqsl.cloud.core.service.*;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import com.hysw.qqsl.cloud.pay.service.TradeService;
import com.hysw.qqsl.cloud.wechat.service.GetAccessTokenService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by leinuo on 17-1-12.
 * 定时任务类，以此替换线程
 */
@Component("myTask")
public class MyTask {

    Log logger = LogFactory.getLog(getClass());
    @Autowired
    private OssService ossService;
    @Autowired
    private OfficeToPdfManager officeToPdfManager;
    @Autowired
    private UploadCache uploadCache;

    @Autowired
    private NoteCache noteCache;
    @Autowired
    private ApplicationTokenService applicationTokenService;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private BuildService buildService;
    @Autowired
    private GetAccessTokenService getAccessTokenService;
    @Autowired
    private PackageService packageService;
//    @Autowired
//    private CertifyService certifyService;
    @Autowired
    private CertifyCache certifyCache;
    @Autowired
    private TradeService tradeService;
    @Autowired
    private StorageLogService storageLogService;

//    @Autowired
//    private CustomRealm customRealm;

   @Scheduled(fixedDelay = 60000*10 )
    public void stsTokenTask(){
       ossService.setStsToken();
       logger.info("阿里云STS刷新");
    }

    /**
     * 短信检测
     */
    @Scheduled(cron =  "0 0 10 * * *")
    public void noteBeat(){
        Note note = new Note("18661925010","检测短信");
        noteCache.add("18661925010",note);
    }

    @Scheduled(fixedDelay = 60000*10 )
    public void applicationTokenTask(){
        applicationTokenService.makeToken();
        logger.info("token刷新");
    }

    @Scheduled(fixedDelay = 60000*5 )
    public void isActivation(){
        monitorService.isActivation();
        logger.info("监测仪器激活");
    }

    @Scheduled(fixedDelay = 60000*10 )
    public void checkUsed(){
        positionService.checkIsUseds();
        logger.info("检测移动端心跳");
    }

    @Scheduled(fixedDelay = 60000*60*8 )
    public void accountTimeout(){
        positionService.accountTimeout();
        logger.info("检测千寻账号是否过期");
    }

//    @Scheduled(fixedDelay = 60000*10)
    //@Scheduled(fixedDelay = 30000)
//    public void checkOnline(){
//        UserRealm.sessionCheck();
//    }

    /**
     * 删除简单建筑物
     */
    @Scheduled(cron =  "0 0 4 * * *")
//    @Scheduled(fixedDelay = 60000*5 )
    public void deleteSimpleBuild(){
        buildService.deleteSimpleBuild();
        logger.info("删除cut为true的建筑物");
    }

    /**
     * 刷新微信token
     */
    @Scheduled(fixedDelay = 7100000 )
    public void getAccessToken(){
        getAccessTokenService.getAccessToken();
    }

    @Scheduled(cron =  "59 59 23 31 12 ?")
    public void initCurTrafficNum(){
        packageService.initCurTrafficNum();
        logger.info("初始化套餐流量");
    }

    @Scheduled(cron =  "00 00 04 ? * MON,THU")
    public void certification(){
        certifyCache.certification();
        logger.info("实名-企业进行认证");
    }

    @Scheduled(cron =  "00 00 04 1 * ?")
    public void expire(){
        certifyCache.expire();
        logger.info("身份认证与企业认证是否过期监测");
    }


    /**
     * 订单过期定时检测(订单应在两小时之内支付,超过两小时视为过期)
     */
    @Scheduled(fixedDelay = 60000*30 )
    public void tradeExpireCheck(){
        tradeService.expireCheck();
        logger.info("订单是否过期监测");
    }

    /**
     * 定时每两个小时构建一次日志
     */
    @Scheduled(fixedDelay = 60000*60*2 )
    public void storageCountLogTask(){
        storageLogService.buildStorageCountLog();
        logger.info("存储日志刷新");
    }

}
