package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.AttribeDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.Attribe;
import com.hysw.qqsl.cloud.core.entity.data.Build;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenl on 17-4-10.
 */
@Service("attribeService")
public class AttribeService extends BaseService<Attribe,Long> {
    @Autowired
    private AttribeDao attribeDao;
    @Autowired
    public void setBaseDao(AttribeDao attribeDao) {
        super.setBaseDao(attribeDao);
    }

    public List<Attribe> findByBuilds(List<Build> builds) {
        List<Filter> filters = new ArrayList<>();
        for (Build build : builds) {
            filters.add(Filter.eq("build", build));
        }
        return attribeDao.findList(0, null, filters);
    }

//    public void findByProject() {
//        List<Filter> filters = new ArrayList<>();
//        filters.add(Filter.eq(""))
//    }
}
