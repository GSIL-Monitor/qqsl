package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.ProjectLogDao;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.ProjectLog;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.entity.element.Unit;
import com.hysw.qqsl.cloud.core.entity.project.CooperateVisit;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("projectLogService")
public class ProjectLogService extends BaseService<ProjectLog, Long> {
    @Autowired
    private ProjectLogDao projectLogDao;
    @Autowired
    private UnitService unitService;
    @Autowired
    private AccountService accountService;
    @Autowired
    public void setBaseDao(ProjectLogDao projectLogDao) {
        super.setBaseDao(projectLogDao);
    }

    /**
     * 保存项目编辑日志
     * @return
     */
    public void saveLog(Project project, Object object, List<String> aliases, String names, ProjectLog.Type type) {
        ProjectLog projectLog = new ProjectLog();
        for (String alias : aliases) {
            projectLog.setContent(alias);
            projectLog.setProjectId(project.getId());
            if (object instanceof Account) {
                projectLog.setAccountId(((Account) object).getId());
            }
            if (names == null && type == null) {
                projectLog.setCooperateType(alias.startsWith("1")? CooperateVisit.Type.VISIT_INVITE_ELEMENT:alias.startsWith("2")?CooperateVisit.Type.VISIT_PREPARATION_ELEMENT:alias.startsWith("3")?CooperateVisit.Type.VISIT_BUILDING_ELEMENT:CooperateVisit.Type.VISIT_MAINTENANCE_ELEMENT);
                projectLog.setType(ProjectLog.Type.ELEMENT);
            } else if (names != null && type != null) {
                projectLog.setCooperateType(alias.startsWith("1")? CooperateVisit.Type.VISIT_INVITE_FILE:alias.startsWith("2")?CooperateVisit.Type.VISIT_PREPARATION_FILE:alias.startsWith("3")?CooperateVisit.Type.VISIT_BUILDING_FILE:CooperateVisit.Type.VISIT_MAINTENANCE_FILE);
                projectLog.setType(type);
            }
            save(projectLog);
        }
    }

    /**
     * 查找最近500条记录
     * @param projectId
     * @return
     */
    public List<ProjectLog> findByProjectId(Long projectId) {
        String hql="from ProjectLog where project_id="+projectId+" order by id desc";
        List<ProjectLog> projectLogs = projectLogDao.hqlFindList(hql, 500);
        return projectLogs;
    }

    /**
     * projectLogs to   json
     * @param projectLogs
     * @return
     */
    public JSONArray projectLogsToJson(List<ProjectLog> projectLogs) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        for (ProjectLog projectLog : projectLogs) {
            jsonObject = new JSONObject();
            jsonObject.put("id", projectLog.getId());
            if (projectLog.getAccountId() != 0) {
                Account account = accountService.find(projectLog.getAccountId());
                jsonObject.put("accountName", account.getName());
            }else{
                jsonObject.put("accountName", "用户");
            }
            jsonObject.put("content", projectLog.getContent());
            jsonObject.put("cooperateType", projectLog.getCooperateType());
            jsonObject.put("type", projectLog.getType());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }
}
