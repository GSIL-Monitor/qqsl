package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.SensorAttributeDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.Sensor;
import com.hysw.qqsl.cloud.core.entity.data.SensorAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @since 2018/11/14
 */
@Service("sensorAttributeService")
public class SensorAttributeService extends BaseService<SensorAttribute, Long> {
    @Autowired
    private SensorAttributeDao sensorAttributeDao;

    @Autowired
    public void setBaseDao(SensorAttributeDao sensorAttributeDao) {
        super.setBaseDao(sensorAttributeDao);
    }

    public List<SensorAttribute> findBySensor(Sensor sensor) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("sensor", sensor));
        return sensorAttributeDao.findList(0,null,filters);
    }
}
