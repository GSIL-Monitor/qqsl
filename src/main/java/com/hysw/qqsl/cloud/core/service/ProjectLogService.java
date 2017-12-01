package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.ProjectLogDao;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.ProjectLog;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.entity.element.Element;
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
    public void saveLog(Project project, Object object, List<String> aliases, String object1, ProjectLog.Type type) {
        ProjectLog projectLog = new ProjectLog();
        for (String alias : aliases) {
            projectLog.setContent(covert(project.getType(),alias,object1));
            projectLog.setProjectId(project.getId());
            if (object instanceof Account) {
                projectLog.setAccountId(((Account) object).getId());
            }
            if (type == null) {
                projectLog.setCooperateType(alias.startsWith("1")? CooperateVisit.Type.VISIT_INVITE_ELEMENT:alias.startsWith("2")?CooperateVisit.Type.VISIT_PREPARATION_ELEMENT:alias.startsWith("3")?CooperateVisit.Type.VISIT_BUILDING_ELEMENT:CooperateVisit.Type.VISIT_MAINTENANCE_ELEMENT);
                projectLog.setType(ProjectLog.Type.ELEMENT);
            } else if (type != null) {
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
     * 根据项目阶段和项目id查询日志
     * @param type 项目阶段
     * @param projectId 项目id
     * @return
     */
    public ProjectLog findByCooperateType(CooperateVisit.Type type,Long projectId){
        String hql="from ProjectLog where cooperate_type="+type.ordinal()+" and project_id="+projectId+" order by id desc";
        List<ProjectLog> projectLogs = projectLogDao.hqlFindList(hql, 1);
        if (projectLogs.size() == 0) {
            return null;
        }
        return projectLogs.get(0);
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
//            jsonObject.put("id", projectLog.getId());
            if (projectLog.getAccountId() != 0) {
                Account account = accountService.find(projectLog.getAccountId());
                jsonObject.put("accountName", account.getName());
            }else{
                jsonObject.put("accountName", "用户");
            }
            jsonObject.put("content", projectLog.getContent());
//            jsonObject.put("cooperateType", projectLog.getCooperateType());
            jsonObject.put("createDate", projectLog.getCreateDate().getTime());
            jsonObject.put("type", projectLog.getType());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /**
     * alias转中文
     *
     * @param type
     * @param alias
     * @return
     */
    private String covert(Project.Type type, String alias, String object) {
        List<Unit> units = null;
        if (type == Project.Type.AGRICULTURAL_IRRIGATION) {
            units = unitService.getAgrUnits();
        }
        if (type == Project.Type.CONSERVATION) {
            units = unitService.getConUnits();
        }
        if (type == Project.Type.DRINGING_WATER) {
            units = unitService.getDriUnits();
        }
        if (type == Project.Type.FLOOD_DEFENCES) {
            units = unitService.getFloUnits();
        }
        if (type == Project.Type.HYDROPOWER_ENGINEERING) {
            units = unitService.getHydUnits();
        }
        if (type == Project.Type.WATER_SUPPLY) {
            units = unitService.getWatUnits();
        }
        String content = alias.startsWith("1") ? "招投标" : alias.startsWith("2") ? "项目前期" : alias.startsWith("3") ? "建设期" : "运营维护期";
        for (int i = 0; i < units.size(); i++) {
            for (int i1 = 0; i1 < units.get(i).getElementGroups().size(); i1++) {
                if (units.get(i).getElementGroups().get(i1).getAlias().equals(alias)) {
                    content = content + "--" + units.get(i).getName() + "--" + units.get(i).getElementGroups().get(i1).getName();
                    break;
                }
                for (Element element : units.get(i).getElementGroups().get(i1).getElements()) {
                    if (element.getAlias().equals(alias)) {
                        content = content + "--" + units.get(i).getName() + "--" + units.get(i).getElementGroups().get(i1).getName() + "--" + element.getName();
                        break;
                    }
                }
            }
        }
        content = content + "：" + object;
        return content;
    }
}
