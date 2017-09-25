package com.hysw.qqsl.cloud.annotation.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.data.Certify;
import com.hysw.qqsl.cloud.core.entity.data.Coordinate;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.entity.element.Position;
import com.hysw.qqsl.cloud.core.service.*;
import com.hysw.qqsl.cloud.pay.entity.PackageItem;
import com.hysw.qqsl.cloud.pay.entity.PackageModel;
import com.hysw.qqsl.cloud.pay.entity.ServeItem;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import com.hysw.qqsl.cloud.pay.service.TradeService;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 切面编程服务类
 *
 * @author chenl
 * @create 2017-08-25 上午9:39
 */
@Component
@Aspect
public class AspectService {
    @Autowired
    private AuthentService authentService;
    @Autowired
    private PackageService packageService;
    @Autowired
    private CertifyService certifyService;
    @Autowired
    private TradeService tradeService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private CoordinateService coordinateService;
    @Autowired
    private PositionService positionService;

    private final static Log log = LogFactory.getLog(AspectService.class);

    //配置切入点,该方法无方法体,主要为方便同类中其他方法使用此处配置的切入点
//    @Pointcut("execution(* cn.ysh.studio.spring.aop.service..*(..))")
//    @Pointcut("execution(* com.hysw.qqsl.cloud.core.service..*(..))")
//    @Pointcut("execution(* com.hysw.qqsl.cloud.core.service.ProjectService.createProject(..))||" +
//            "execution(* com.hysw.qqsl.cloud.core.service.ProjectService.updateProject(..))||" +
//            "execution(* com.hysw.qqsl.cloud.core.service.NoteService.addToNoteCache(..))||" +
//            "execution(* com.hysw.qqsl.cloud.core.service.ShareService.shares(..))||" +
//            "execution(* com.hysw.qqsl.cloud.core.service.CooperateService.cooperateMult(..))||" +
//            "execution(* com.hysw.qqsl.cloud.core.service.CooperateService.cooperate(..))||" +
//            "execution(* com.hysw.qqsl.cloud.core.service.ProjectService.uploadFileSize(..))||" +
//            "execution(* com.hysw.qqsl.cloud.core.service.ProjectService.isAllowUpload(..))||" +
//            "execution(* com.hysw.qqsl.cloud.core.service.PositionService.randomPosition(..))")
//    @Pointcut("execution(* com.hysw.qqsl.cloud.core.service.ProjectService.getProjects(..))")
//    @Pointcut(value = "@annotation(com.hysw.qqsl.cloud.annotation.util.IsExpire)")
//    public void aspect(){	}

    /*
     * 配置前置通知,使用在方法aspect()上注册的切入点
     * 同时接受JoinPoint切入点对象,可以没有该参数
     */
//    @Before("aspect()")
//    public void before(JoinPoint joinPoint){
//        try {
//            throw new Throwable();
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//            System.out.println(throwable.getMessage());
//        }
//    }

    //配置后置通知,使用在方法aspect()上注册的切入点
//    @After("aspect()")
//    public void after(JoinPoint joinPoint){
//        if(log.isInfoEnabled()){
//            log.info("after " + joinPoint);
//        }
//    }

    /**
     * 套餐未过期
     * @param joinPoint
     * @return
     */
    //配置环绕通知,使用在方法aspect()上注册的切入点
    @Around(value = "@annotation(com.hysw.qqsl.cloud.annotation.util.IsExpire)")
    public Message isExpire(ProceedingJoinPoint joinPoint){
        User user = authentService.getUserFromSubject();
        Package aPackage = packageService.findByUser(user);
        if (aPackage == null) {
            return new Message(Message.Type.EXIST);
        }
        if (aPackage.getExpireDate().getTime() > new Date().getTime()) {
            try {
                return (Message) joinPoint.proceed();
            } catch (Throwable e) {
                return new Message(Message.Type.FAIL);
            }
        }
        return new Message(Message.Type.EXPIRE);
    }

    /**
     * 套餐已过期
     * @param joinPoint
     * @return
     */
    //配置环绕通知,使用在方法aspect()上注册的切入点
    @Around(value = "@annotation(com.hysw.qqsl.cloud.annotation.util.IsExpired)")
    public Message isExpired(ProceedingJoinPoint joinPoint){
        User user = authentService.getUserFromSubject();
        Package aPackage = packageService.findByUser(user);
        if (aPackage == null) {
            return new Message(Message.Type.EXIST);
        }
        if (aPackage.getType() == CommonEnum.PackageType.TEST || aPackage.getExpireDate().getTime() < new Date().getTime()) {
            try {
                return (Message) joinPoint.proceed();
            } catch (Throwable e) {
                return new Message(Message.Type.FAIL);
            }
        }
        return new Message(Message.Type.EXPIRE);
    }

    /**
     * 个人认证是否通过
     * @param joinPoint
     * @return
     */
    @Around(value = "@annotation(com.hysw.qqsl.cloud.annotation.util.IsPersonalCertify)")
    public Message isPersonalCertify(ProceedingJoinPoint joinPoint){
        User user = authentService.getUserFromSubject();
        Certify certify = certifyService.findByUser(user);
        if (certify == null) {
            return new Message(Message.Type.EXIST);
        }
        if (certify.getPersonalStatus() == CommonEnum.CertifyStatus.PASS || certify.getPersonalStatus() == CommonEnum.CertifyStatus.EXPIRING) {
            try {
                return (Message) joinPoint.proceed();
            } catch (Throwable e) {
                return new Message(Message.Type.FAIL);
            }
        }
        return new Message(Message.Type.NO_CERTIFY);
    }

    /**
     * 是否通过认证（实名，企业）
     * @param joinPoint
     * @return
     */
    @Around(value = "@annotation(com.hysw.qqsl.cloud.annotation.util.IsCertify)")
    public Message isCertify(ProceedingJoinPoint joinPoint){
        User user = authentService.getUserFromSubject();
        Certify certify = certifyService.findByUser(user);
        if (certify == null) {
            return new Message(Message.Type.EXIST);
        }
        if (certify.getPersonalStatus() == CommonEnum.CertifyStatus.PASS || certify.getPersonalStatus() == CommonEnum.CertifyStatus.EXPIRING || certify.getCompanyStatus() == CommonEnum.CertifyStatus.PASS || certify.getCompanyStatus() == CommonEnum.CertifyStatus.EXPIRING) {
            try {
                return (Message) joinPoint.proceed();
            } catch (Throwable e) {
                return new Message(Message.Type.FAIL);
            }
        }
        return new Message(Message.Type.NO_CERTIFY);
    }

    /**
     * 是否有千寻账户权限
     * @param joinPoint
     * @return
     */
    @Around(value = "@annotation(com.hysw.qqsl.cloud.annotation.util.IsFindCM)")
    public Message isFindCM(ProceedingJoinPoint joinPoint){
        User user = authentService.getUserFromSubject();
        Package aPackage = packageService.findByUser(user);
        if (aPackage == null) {
            return new Message(Message.Type.EXIST);
        }
        List<Object> list = Arrays.asList(joinPoint.getArgs());
        String id = list.get(2).toString();
        Project project = projectService.find(Long.valueOf(id));
        int i = positionService.findByUserInUseds(project.getUser());
        PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
        for (PackageItem packageItem : packageModel.getPackageItems()) {
            if (packageItem.getServeItem().getType() == ServeItem.Type.FINDCM && i < packageItem.getLimitNum()) {
                try {
                    return (Message) joinPoint.proceed();
                } catch (Throwable e) {
                    return new Message(Message.Type.FAIL);
                }
            }
        }
//        没有权限(套餐不包含此功能)
        return new Message(Message.Type.NO_ALLOW);
    }

    /**
     * 企业认证是否通过
     * @param joinPoint
     * @return
     */
    @Around(value = "@annotation(com.hysw.qqsl.cloud.annotation.util.IsCompanyCertify)")
    public Message isCompanyCertify(ProceedingJoinPoint joinPoint){
        User user = authentService.getUserFromSubject();
        Certify certify = certifyService.findByUser(user);
        if (certify == null) {
            return new Message(Message.Type.EXIST);
        }
        if (certify.getCompanyStatus() == CommonEnum.CertifyStatus.PASS || certify.getCompanyStatus() == CommonEnum.CertifyStatus.EXPIRING) {
            try {
                return (Message) joinPoint.proceed();
            } catch (Throwable e) {
                return new Message(Message.Type.FAIL);
            }
        }
        return new Message(Message.Type.NO_CERTIFY);
    }

    /**
     * 是否允许创建项目
     * @param joinPoint
     * @return
     */
    @Around(value = "@annotation(com.hysw.qqsl.cloud.annotation.util.IsAllowCreateProject)")
    public Message isAllowCreateProject(ProceedingJoinPoint joinPoint){
        User user = authentService.getUserFromSubject();
        Package aPackage = packageService.findByUser(user);
        if (aPackage == null) {
            return new Message(Message.Type.EXIST);
        }
        PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
        long l = projectService.findByUser(user).size();
        for (PackageItem packageItem : packageModel.getPackageItems()) {
            if (packageItem.getServeItem().getType() == ServeItem.Type.PROJECT && l < packageItem.getLimitNum()) {
                try {
                    return (Message) joinPoint.proceed();
                } catch (Throwable e) {
                    return new Message(Message.Type.FAIL);
                }
            }
        }
        return new Message(Message.Type.NO_ALLOW);
    }

    /**
     * 是否允许创建子账号
     * @param joinPoint
     * @return
     */
    @Around(value = "@annotation(com.hysw.qqsl.cloud.annotation.util.IsAllowCreateAccount)")
    public Message isAllowCreateAccount(ProceedingJoinPoint joinPoint){
        User user = authentService.getUserFromSubject();
        Package aPackage = packageService.findByUser(user);
        if (aPackage == null) {
            return new Message(Message.Type.EXIST);
        }
        PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
        for (PackageItem packageItem : packageModel.getPackageItems()) {
            if (packageItem.getServeItem().getType() == ServeItem.Type.ACCOUNT && user.getAccounts().size() < packageItem.getLimitNum()) {
                try {
                    return (Message) joinPoint.proceed();
                } catch (Throwable e) {
                    return new Message(Message.Type.FAIL);
                }
            }
        }
        return new Message(Message.Type.NO_ALLOW);
    }


    /**
     * 是否允许上传文件
     * @param joinPoint
     * @return
     */
    @Around(value = "@annotation(com.hysw.qqsl.cloud.annotation.util.IsAllowUploadFile)")
    public Message isAllowUploadFile(ProceedingJoinPoint joinPoint){
        User user = authentService.getUserFromSubject();
        Package aPackage = packageService.findByUser(user);
        if (aPackage == null) {
            return new Message(Message.Type.EXIST);
        }
        Map<String, Object> map = (Map<String, Object>) SettingUtils.objectCopy(Arrays.asList(joinPoint.getArgs()).get(0));
        long fileSize = Long.valueOf(map.get("fileSize").toString());
        PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
        long l = aPackage.getCurSpaceNum() + fileSize;
        long l1 = aPackage.getCurTrafficNum() + fileSize;
        for (PackageItem packageItem : packageModel.getPackageItems()) {
            if (packageItem.getServeItem().getType() == ServeItem.Type.SPACE && l <= packageItem.getLimitNum() && l1 <= packageItem.getLimitNum() * 10) {
                try {
                    return (Message) joinPoint.proceed();
                } catch (Throwable e) {
                    return new Message(Message.Type.FAIL);
                }
            }
        }
        return new Message(Message.Type.NO_ALLOW);
    }


    /**
     * 是否允许下载文件
     * @param joinPoint
     * @return
     */
    @Around(value = "@annotation(com.hysw.qqsl.cloud.annotation.util.IsAllowDownLoadFile)")
    public Message isAllowDownLoadFile(ProceedingJoinPoint joinPoint){
        User user = authentService.getUserFromSubject();
        Package aPackage = packageService.findByUser(user);
        if (aPackage == null) {
            return new Message(Message.Type.EXIST);
        }
        Map<String, Object> map = (Map<String, Object>) SettingUtils.objectCopy(Arrays.asList(joinPoint.getArgs()).get(0));
        long fileSize = Long.valueOf(map.get("fileSize").toString());
        PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
        long l = aPackage.getCurTrafficNum() + fileSize;
        for (PackageItem packageItem : packageModel.getPackageItems()) {
            if (packageItem.getServeItem().getType() == ServeItem.Type.SPACE && l <= packageItem.getLimitNum() * 10) {
                try {
                    return (Message) joinPoint.proceed();
                } catch (Throwable e) {
                    return new Message(Message.Type.FAIL);
                }
            }
        }
        return new Message(Message.Type.NO_ALLOW);
    }

    /**
     * 是否达到坐标文件限制数
     * @param joinPoint
     * @return
     */
    @Around(value = "@annotation(com.hysw.qqsl.cloud.annotation.util.IsCoordinateFile)")
    public Message isCoordinateFile(ProceedingJoinPoint joinPoint){
        Object[] args = joinPoint.getArgs();
        HttpServletRequest request= (HttpServletRequest) args[0];
        String id = request.getParameter("id");
        Project project = projectService.find(Long.valueOf(id));
        if (project == null) {
            return new Message(Message.Type.EXIST);
        }
        List<Coordinate> coordinates = coordinateService.findByProject(project);
        if (coordinates.size()< CommonAttributes.COORDINATELIMIT) {
            try {
                return (Message) joinPoint.proceed();
            } catch (Throwable e) {
                return new Message(Message.Type.FAIL);
            }
        }
//        超过限制数量，返回已达到最大限制数量
        return new Message(Message.Type.OTHER);
    }

    /**
     * 是否有未支付订单
     * @param joinPoint
     * @return
     */
    @Around(value = "@annotation(com.hysw.qqsl.cloud.annotation.util.IsHaveTradeNoPay)")
    public Message isHaveTradeNoPay(ProceedingJoinPoint joinPoint){
        User user = authentService.getUserFromSubject();
        if (tradeService.checkTradeHaveNopay(user)) {
            try {
                return (Message) joinPoint.proceed();
            } catch (Throwable e) {
                return new Message(Message.Type.FAIL);
            }
        }
        return new Message(Message.Type.UNKNOWN);
    }

    /**
     * 是否拥有BIM功能
     * @param joinPoint
     * @return
     */
    @Around(value = "@annotation(com.hysw.qqsl.cloud.annotation.util.IsBIM)")
    public Message isBIM(ProceedingJoinPoint joinPoint) {
        User user = authentService.getUserFromSubject();
        Package aPackage = packageService.findByUser(user);
        if (aPackage == null) {
            return new Message(Message.Type.EXIST);
        }
        PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
        for (PackageItem packageItem : packageModel.getPackageItems()) {
            if (packageItem.getServeItem().getType() == ServeItem.Type.BIMSERVE) {
                try {
                    return (Message) joinPoint.proceed();
                } catch (Throwable e) {
                    return new Message(Message.Type.FAIL);
                }
            }
        }
//        没有权限(套餐不包含此功能)
        return new Message(Message.Type.NO_ALLOW);
    }

        /**
         * 是否有拥有全景权限，限制数是否满足要求
         * @param joinPoint
         * @return
         */
        @Around(value = "@annotation(com.hysw.qqsl.cloud.annotation.util.IsPano)")
        public Message isPano(ProceedingJoinPoint joinPoint){
            User user = authentService.getUserFromSubject();
            Package aPackage = packageService.findByUser(user);
            if (aPackage == null) {
                return new Message(Message.Type.EXIST);
            }
//                全景功能完善后，增加根据用户查询用户下全景数量的方法即可完成此功能
            PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
            for (PackageItem packageItem : packageModel.getPackageItems()) {
//                if (packageItem.getServeItem().getType() == ServeItem.Type.PANO&&packageItem.getLimitNum()) {
//                    try {
//                        return (Message) joinPoint.proceed();
//                    } catch (Throwable e) {
//                        return new Message(Message.Type.FAIL);
//                    }
//                }
            }
        //        没有权限(套餐不包含此功能)
        return new Message(Message.Type.NO_ALLOW);
    }
    //配置后置返回通知,使用在方法aspect()上注册的切入点
//    @AfterReturning("aspect()")
//    public Message afterReturn(JoinPoint joinPoint){
//        if(log.isInfoEnabled()){
//            log.info("afterReturn " + joinPoint);
//        }
//        return new Message(Message.Type.FAIL);
//    }

//    //配置抛出异常后通知,使用在方法aspect()上注册的切入点
//    @AfterThrowing(pointcut="aspect()", throwing="ex")
//    public void afterThrow(JoinPoint joinPoint, Exception ex){
//        if(log.isInfoEnabled()){
//            log.info("afterThrow " + joinPoint + "\t" + ex.getMessage());
//        }
//    }

}
