package com.hysw.qqsl.cloud.annotation.service;

import com.hysw.qqsl.cloud.annotation.util.PackageIsExpire;
import com.hysw.qqsl.cloud.annotation.util.StationIsExpire;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.core.service.*;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import com.hysw.qqsl.cloud.pay.service.TradeService;
import com.hysw.qqsl.cloud.util.SettingUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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
    private ProjectService projectService;
    @Autowired
    private TradeService tradeService;
    @Autowired
    private StationService stationService;

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
    @Around(value = "@annotation(com.hysw.qqsl.cloud.annotation.util.StationIsExpire)")
    public Message stationIsExpire(ProceedingJoinPoint joinPoint){
        User user = authentService.getUserFromSubject();
        Map<String, Object> map = (Map<String, Object>) SettingUtils.objectCopy(Arrays.asList(joinPoint.getArgs()).get(0));
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature)signature;
        Method targetMethod = methodSignature.getMethod();
        Annotation[] declaredAnnotations = targetMethod.getDeclaredAnnotations();
        String value = null;
        for (Annotation declaredAnnotation : declaredAnnotations) {
            if (declaredAnnotation instanceof StationIsExpire) {
                value = ((StationIsExpire) declaredAnnotation).value();
                break;
            }
        }
        if (value == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Object id = null;
        Station station = null;
        if (value.equals("station")) {
            Map<Object, Object> station1 = (Map<Object, Object>) map.get("station");
            id = station1.get("id");
        } else if (value.equals("request")) {
            Object[] args = joinPoint.getArgs();
            HttpServletRequest request= (HttpServletRequest) args[0];
            id = request.getParameter("id");
        }else if (value.equals("sensor")) {
            Map<Object, Object> sensor = (Map<Object, Object>) map.get("sensor");
            id = sensor.get("station");
        }else if(value.equals("camera")){
            Map<Object, Object> camera = (Map<Object, Object>) map.get("camera");
            id = camera.get("station");
        }else if(value.equals("object")){
            id = map.get("id");
        } else if (value.equals("instanceId")) {
            station = stationService.findByInstanceId(map.get("instanceId").toString());
        }
        if (station == null) {
            station = stationService.find(Long.valueOf(id.toString()));
        }
        if (station==null||!station.getUser().getId().equals(user.getId())) {
            return MessageService.message(Message.Type.EXIST);
        }
        if (station.getExpireDate().getTime() > System.currentTimeMillis()) {
            try {
                return (Message) joinPoint.proceed();
            } catch (Throwable e) {
                return MessageService.message(Message.Type.FAIL);
            }
        }
        return MessageService.message(Message.Type.EXPIRED);
    }

    /**
     * 套餐未过期
     * @param joinPoint
     * @return
     */
    //配置环绕通知,使用在方法aspect()上注册的切入点
    @Around(value = "@annotation(com.hysw.qqsl.cloud.annotation.util.PackageIsExpire)")
    public Message packageIsExpire(ProceedingJoinPoint joinPoint){
        User user = authentService.getUserFromSubject();
        if (user == null) {
            Signature signature = joinPoint.getSignature();
            MethodSignature methodSignature = (MethodSignature)signature;
            Method targetMethod = methodSignature.getMethod();
            Annotation[] declaredAnnotations = targetMethod.getDeclaredAnnotations();
            String value = null;
            for (Annotation declaredAnnotation : declaredAnnotations) {
                if (declaredAnnotation instanceof PackageIsExpire) {
                    value = ((PackageIsExpire) declaredAnnotation).value();
                    break;
                }
            }
            if (value == null) {
                return MessageService.message(Message.Type.FAIL);
            }
            if (value.equals("request")) {
                Object[] args = joinPoint.getArgs();
                HttpServletRequest request= (HttpServletRequest) args[0];
                String projectId = request.getParameter("projectId");
                Project project = projectService.find(Long.valueOf(projectId));
                user = project.getUser();
            }
            if (value.equals("object")) {
                Map<String, Object> map = (Map<String, Object>) SettingUtils.objectCopy(Arrays.asList(joinPoint.getArgs()).get(0));
                Object projectId = map.get("projectId");
                if (projectId == null) {
                    return MessageService.message(Message.Type.FAIL);
                }
                Project project = projectService.find(Long.valueOf(projectId.toString()));
                user = project.getUser();
            }
            if (value.equals("property")) {
                Object projectId = (joinPoint.getArgs())[0];
                if (projectId == null) {
                    return MessageService.message(Message.Type.FAIL);
                }
                Project project = projectService.find(Long.valueOf(projectId.toString()));
                user = project.getUser();
            }
        }
        Package aPackage = packageService.findByUser(user);
        if (aPackage == null) {
            return MessageService.message(Message.Type.EXIST);
        }
        if (aPackage.getExpireDate().getTime() > System.currentTimeMillis()) {
            try {
                return (Message) joinPoint.proceed();
            } catch (Throwable e) {
                return MessageService.message(Message.Type.FAIL);
            }
        }
        return MessageService.message(Message.Type.EXPIRED);
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
                return MessageService.message(Message.Type.FAIL);
            }
        }
        return MessageService.message(Message.Type.EXIST);
    }


    //配置后置返回通知,使用在方法aspect()上注册的切入点
//    @AfterReturning("aspect()")
//    public Message afterReturn(JoinPoint joinPoint){
//        if(log.isInfoEnabled()){
//            log.info("afterReturn " + joinPoint);
//        }
//        return MessageService.message(Message.Type.FAIL);
//    }

//    //配置抛出异常后通知,使用在方法aspect()上注册的切入点
//    @AfterThrowing(pointcut="aspect()", throwing="ex")
//    public void afterThrow(JoinPoint joinPoint, Exception ex){
//        if(log.isInfoEnabled()){
//            log.info("afterThrow " + joinPoint + "\t" + ex.getMessage());
//        }
//    }



}
