package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.dao.DiffConnPollDao;
import com.hysw.qqsl.cloud.entity.Filter;
import com.hysw.qqsl.cloud.entity.data.DiffConnPoll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenl on 17-4-8.
 */
@Service("diffConnPollService")
public class DiffConnPollService extends BaseService<DiffConnPoll,Long> {
    @Autowired
    private DiffConnPollDao diffConnPollDao;
    @Autowired
    public void setBaseDao(DiffConnPollDao diffConnPollDao) {
        super.setBaseDao(diffConnPollDao);
    }

    public DiffConnPoll findByUserName(String userName) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("userName", userName));
        List<DiffConnPoll> diffConnPolls = diffConnPollDao.findList(0, null, filters);
        if (diffConnPolls.size() == 1) {
            return diffConnPolls.get(0);
        }
        return null;
    }
}
