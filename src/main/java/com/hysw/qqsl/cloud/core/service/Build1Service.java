package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.Build1Dao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.*;
import net.sf.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leinuo on 17-4-13.
 */
@Service("build1Service")
public class Build1Service extends BaseService<Build1,Long> {
    @Autowired
    private Build1Dao build1Dao;
    @Autowired
    private FieldWorkService fieldWorkService;
    @Autowired
    private AttributeGroupService attributeGroupService;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private CoordinateService coordinateService;
    @Autowired
    private TransFromService transFromService;
    @Autowired
    public void setBaseDao(Build1Dao build1Dao) {
        super.setBaseDao( build1Dao);
    }


    public List<Build1> findByCoordinateId(Long id) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("coordinateId", id));
        return build1Dao.findList(0, null, filters);
    }
}
