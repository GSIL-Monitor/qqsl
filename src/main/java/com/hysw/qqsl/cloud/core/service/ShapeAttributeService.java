package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.ShapeAttributeDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.Shape;
import com.hysw.qqsl.cloud.core.entity.data.ShapeAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public List<ShapeAttribute> findByShape(Shape shape) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("shape", shape));
        return shapeAttributeDao.findList(0, null, filters);
    }
}
