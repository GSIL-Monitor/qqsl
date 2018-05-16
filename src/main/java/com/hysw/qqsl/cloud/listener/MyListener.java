package com.hysw.qqsl.cloud.listener;

import com.hysw.qqsl.cloud.core.service.EmailManager;
import com.hysw.qqsl.cloud.core.service.*;
import com.hysw.qqsl.cloud.pay.service.GoodsService;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import com.hysw.qqsl.cloud.util.ObjectJsonConvertUtils;
import com.hysw.qqsl.cloud.util.SettingUtils;
import com.hysw.qqsl.cloud.wechat.service.GetAccessTokenService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * spring容器已加载完成，就启动所有线程
 * @author leinuo  
 *
 * @date  2016年2月26日
 */
@Service
public class MyListener implements ApplicationListener<ContextRefreshedEvent>{
	Log logger = LogFactory.getLog(getClass());
	@Autowired
	private NoteManager noteManager;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ElementGroupService elementGroupService;
	@Autowired
	private InfoService infoService;
	@Autowired
	private ElementDataGroupService elementDataGroupService;
	@Autowired
	private UnitService unitService;
	@Autowired
	private ObjectJsonConvertUtils objectJsonConvertUtils;
	@Autowired
	private PositionService positionService;
	@Autowired
	private BuildGroupService buildGroupService;
	@Autowired
	private SensorService sensorService;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private PackageService packageService;
	@Autowired
	private StationService stationService;
	@Autowired
	private UserService userService;
	@Autowired
	private CertifyService certifyService;
	@Autowired
	private EmailManager emailManager;
	@Autowired
	private StorageLogService storageLogService;
	@Autowired
	private ProjectLogService projectLogService;
	@Autowired
	private PollingService pollingService;
	@Autowired
	private GetAccessTokenService getAccessTokenService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private FieldService fieldService;
	@Autowired
	private AccountManager accountManager;
    @Autowired
    private NoteService noteService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if(event.getApplicationContext().getParent() == null){
		//	new Thread(officeToPdfManager).start();
			logger.info("pdf转换兼文件上传的线程已启动");
			logger.info("获取sts凭证的线程已启动");
			new Thread(noteManager).start();
			logger.info("发送短信的线程已启动");
			new Thread(emailManager).start();
			logger.info("邮件服务线程启动");
			messageService.init();
			logger.info("初始化静态消息");
			//将所有项目写入缓存
			elementGroupService.getDriElementGroups();
			elementGroupService.getConElementGroups();
			elementGroupService.getFloElementGroups();
			elementGroupService.getHydElementGroups();
			elementGroupService.getAgrElementGroups();
			elementGroupService.getWatElementGroups();
			unitService.getAgrUnitModels();
			unitService.getConUnitModels();
			unitService.getDriUnitModels();
			unitService.getFloUnitModels();
			unitService.getWatUnitModels();
			unitService.getHydUnitModels();
			unitService.getAgrUnits();
			unitService.getConUnits();
			unitService.getHydUnits();
			unitService.getWatUnits();
			unitService.getFloUnits();
			elementDataGroupService.getElementDataSimpleGroups();
			objectJsonConvertUtils.getAgrJsonTree();
			objectJsonConvertUtils.getConJsonTree();
			objectJsonConvertUtils.getDriJsonTree();
			objectJsonConvertUtils.getFloJsonTree();
			objectJsonConvertUtils.getWatJsonTree();
			objectJsonConvertUtils.getHydJsonTree();
			buildGroupService.getCompleteBuildGroups();
			buildGroupService.getBuildsDynamic();
			certifyService.certifyCache();
			logger.info("加载认证缓存");
			userService.userCache();
			logger.info("加载用户缓存");
			packageService.packageCache();
			logger.info("加载套餐缓存");
			//将所有项目写入缓存
            projectService.projectCache();
            logger.info("项目总数为："+projectService.findAll().size());
			//刷新infos
			infoService.infosCache();
			logger.info("项目信息总数："+infoService.getInfos().size());
			elementDataGroupService.getElementDataSimpleGroups();
			logger.info("简单要素数据总数："+elementDataGroupService.getElementDataSimpleGroups().size());
			positionService.init();
			logger.info("初始化千寻账号");
			sensorService.addCodeToCache();
			logger.info("未绑定仪表加入缓存");
			goodsService.putGoodsModelInCache();
			logger.info("数据服务模板加载");
			packageService.putPackageModelInCache();
			logger.info("套餐模板加载");
			stationService.putStationModelInCache();
			logger.info("测站模板加载");
			//存储日志缓存加载
			storageLogService.buildStorageLog();
			logger.info("存储日志缓存加载完成");
			projectLogService.addNearlyWeekLog();
			logger.info("加载近一周日志缓存");
			if (!SettingUtils.getInstance().getSetting().getStatus().equals("test")) {
				packageService.packageMax();
			}
			pollingService.init();
			logger.info("初始化轮询状态");
			if (SettingUtils.getInstance().getSetting().getStatus().equals("test")) {
				getAccessTokenService.getAccessToken();
			}
			accountManager.init();
			logger.info("未确认子账户加入缓存");
//			if (SettingUtils.getInstance().getSetting().getStatus().equals("run")) {
				if (System.getProperties().getProperty("os.name").toLowerCase().contains("linux")) {
					try {
						Runtime.getRuntime().exec("/home/qqsl/krpano/./krpanotools register FXsqTqaGNSZER5dSETEm+VzQEh9sWSa5DZMFsSmMxYV9GcXs8W3R8A/mWXrGNUceXvrihmh28hfRF1ivrW0HMzEychPvNiD8B/4/ZzDaUE9Rh6Ig22aKJGDbja1/kYIqmc/VKfItRE2RTSOIbIroxOtsz626NIpxWksAAifwhpNwuPXqDQpz2sRUMBzoPqZktpkItoSenN2mKd8Klfx7pOuB6CIK3e1CDXgyndqOt2mWybLZcU/wfJVAecfxk15ghiqrzaDsbqrdABDowg==");
					} catch (IOException e) {
						e.printStackTrace();
					}
					logger.info("激活全景切图插件");
				}
				noteService.receiveMsg();
				logger.info("启动短信回执接口");
//			}
		}
	}

}
