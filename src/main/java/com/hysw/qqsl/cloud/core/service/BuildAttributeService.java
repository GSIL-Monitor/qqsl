package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.BuildAttributeDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.Build;
import com.hysw.qqsl.cloud.core.entity.data.BuildAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenl on 17-4-10.
 */
@Service("buildAttributeService")
public class BuildAttributeService extends BaseService<BuildAttribute,Long> {
    @Autowired
    private BuildAttributeDao buildAttributeDao;
    @Autowired
    public void setBaseDao(BuildAttributeDao buildAttributeDao) {
        super.setBaseDao(buildAttributeDao);
    }

}
