package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.BuildAttributeDao;
import com.hysw.qqsl.cloud.core.dao.ShapeAttributeDao;
import com.hysw.qqsl.cloud.core.entity.data.BuildAttribute;
import com.hysw.qqsl.cloud.core.entity.data.ShapeAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by chenl on 17-4-10.
 */
@Service("shapeAttributeService")
public class ShapeAttributeService extends BaseService<ShapeAttribute,Long> {
    @Autowired
    private ShapeAttributeDao shapeAttributeDao;
    @Autowired
    public void setBaseDao(ShapeAttributeDao shapeAttributeDao) {
        super.setBaseDao(shapeAttributeDao);
    }

}
