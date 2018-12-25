package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.BuildDynAttributeDao;
import com.hysw.qqsl.cloud.core.entity.data.BuildDynAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 * @since 2018/12/25
 */
@Service("buildDynAttributeService")
public class BuildDynAttributeService extends BaseService<BuildDynAttribute, Long> {
    @Autowired
    private BuildDynAttributeDao buildDynAttributeDao;

    @Autowired
    public void setBaseDao(BuildDynAttributeDao buildDynAttributeDao) {
        super.setBaseDao(buildDynAttributeDao);
    }


}
