package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.FieldWorkPointDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.FieldWork;
import com.hysw.qqsl.cloud.core.entity.data.FieldWorkPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @since 2018/10/18
 */
@Service("fieldWorkPointService")
public class FieldWorkPointService extends BaseService<FieldWorkPoint, Long> {
    @Autowired
    private FieldWorkPointDao fieldWorkPointDao;
    @Autowired
    public void setBaseDao(FieldWorkPointDao fieldWorkPointDao){
        super.setBaseDao(fieldWorkPointDao);
    }

    public List<FieldWorkPoint> findByFieldWork(FieldWork fieldWork) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("fieldWork", fieldWork));
        return fieldWorkPointDao.findList(0,null,filters);
    }
}
