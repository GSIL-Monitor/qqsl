package com.hysw.qqsl.cloud.core.service;


import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.core.entity.XMLFileException;
import com.hysw.qqsl.cloud.core.entity.builds.AttribeGroup;
import com.hysw.qqsl.cloud.core.entity.data.Attribe;
import com.hysw.qqsl.cloud.core.entity.data.Build;
import com.hysw.qqsl.cloud.core.entity.builds.BuildGroup;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Cache;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
    private AttribeGroupService attribeGroupService;

    Setting setting = SettingUtils.getInstance().getSetting();

}
