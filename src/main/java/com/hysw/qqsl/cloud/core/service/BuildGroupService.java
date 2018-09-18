package com.hysw.qqsl.cloud.core.service;


import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.ehcache.CacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by leinuo on 17-3-29.
 * 建筑物配制文件读取
 */
@Service("buildGroupService")
public class BuildGroupService {

    Log logger = LogFactory.getLog(getClass());
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private AttributeGroupService attributeGroupService;

    Setting setting = SettingUtils.getInstance().getSetting();

}
