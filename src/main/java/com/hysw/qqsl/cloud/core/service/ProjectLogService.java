package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.ProjectLogDao;
import com.hysw.qqsl.cloud.core.entity.data.ProjectLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("projectLogService")
public class ProjectLogService extends BaseService<ProjectLog, Long> {
    @Autowired
    private ProjectLogDao projectLogDao;
    @Autowired
    public void setBaseDao(ProjectLogDao projectLogDao) {
        super.setBaseDao(projectLogDao);
    }

}
