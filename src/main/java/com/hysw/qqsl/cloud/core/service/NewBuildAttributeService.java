package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.NewBuildAttributeDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.NewBuild;
import com.hysw.qqsl.cloud.core.entity.data.NewBuildAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @since 2018/10/23
 */
@Service("newBuildAttributeService")
public class NewBuildAttributeService extends BaseService<NewBuildAttribute, Long> {
    @Autowired
    private NewBuildAttributeDao newBuildAttributeDao;

    @Autowired
    public void setBaseDao(NewBuildAttributeDao newBuildAttributeDao) {
        super.setBaseDao(newBuildAttributeDao);
    }

    public List<NewBuildAttribute> findByNewBuild(NewBuild newBuild1) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("build", newBuild1));
        return newBuildAttributeDao.findList(0,null,filters);
    }
}
