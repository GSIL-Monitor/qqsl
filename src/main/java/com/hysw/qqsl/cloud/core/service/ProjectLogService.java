package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.ProjectLogDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.ProjectLog;
import com.hysw.qqsl.cloud.core.entity.element.Element;
import com.hysw.qqsl.cloud.core.entity.element.Unit;
import com.hysw.qqsl.cloud.core.entity.project.CooperateVisit;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("projectLogService")
public class ProjectLogService extends BaseService<ProjectLog, Long> {
    @Autowired
    private ProjectLogDao projectLogDao;
    @Autowired
    private UnitService unitService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    public void setBaseDao(ProjectLogDao projectLogDao) {
        super.setBaseDao(projectLogDao);
    }

    /**
     * 保存项目编辑日志
     * @return
     */
    public void saveLog(Project project, Object object, Map<String,String> map, ProjectLog.Type type) {
        ProjectLog projectLog = new ProjectLog();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            projectLog.setContent(covert(project.getType(),entry.getKey(),entry.getValue()));
            projectLog.setProjectId(project.getId());
            if (object instanceof Account) {
                projectLog.setAccountId(((Account) object).getId());
            }
            if (type == null) {
                projectLog.setCooperateType(entry.getKey().startsWith("1")? CooperateVisit.Type.VISIT_INVITE_ELEMENT:entry.getKey().startsWith("2")?CooperateVisit.Type.VISIT_PREPARATION_ELEMENT:entry.getKey().startsWith("3")?CooperateVisit.Type.VISIT_BUILDING_ELEMENT:CooperateVisit.Type.VISIT_MAINTENANCE_ELEMENT);
                projectLog.setType(ProjectLog.Type.ELEMENT);
            } else if (type != null) {
                projectLog.setCooperateType(entry.getKey().startsWith("1")? CooperateVisit.Type.VISIT_INVITE_FILE:entry.getKey().startsWith("2")?CooperateVisit.Type.VISIT_PREPARATION_FILE:entry.getKey().startsWith("3")?CooperateVisit.Type.VISIT_BUILDING_FILE:CooperateVisit.Type.VISIT_MAINTENANCE_FILE);
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
        Cache cache = cacheManager.getCache("projectLogPartCache");
        net.sf.ehcache.Element element = cache.get("projectLog");
        List<ProjectLog> projectLogs = (List<ProjectLog>) element.getValue();
        ProjectLog projectLog = null;
        for (ProjectLog log : projectLogs) {
            if (log.getCooperateType() == type && log.getProjectId() == projectId) {
                if (projectLog == null) {
                    projectLog = log;
                }else{
                    if (log.getId() > projectLog.getId()) {
                        projectLog = log;
                    }
                }
            }
        }
        return projectLog;
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
                jsonObject.put("accountName", account.getName()+"("+account.getPhone()+")");
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
        String parentName;
        for (int i = 0; i < units.size(); i++) {
            for (int i1 = 0; i1 < units.get(i).getElementGroups().size(); i1++) {
                if (units.get(i).getElementGroups().get(i1).getAlias().equals(alias)) {
                    content = content + "--" + units.get(i).getName() + "--" + units.get(i).getElementGroups().get(i1).getName();
                    break;
                }
                for (Element element : units.get(i).getElementGroups().get(i1).getElements()) {
                    if (element.getAlias().equals(alias)) {
                        parentName=units.get(i).getUnitParent()!=null?units.get(i).getUnitParent().getName():"======";
                        content = content + "--"+parentName+"--" + units.get(i).getName() + "--" + units.get(i).getElementGroups().get(i1).getName() + "--" + element.getName();
                        break;
                    }
                }
            }
        }
        content = content.replace("--======--", "--");
        content = content + "：" + object;
        return content;
    }

    /**
     * 添加近一周日志缓存
     */
    public void addNearlyWeekLog(){
        Cache cache = cacheManager.getCache("projectLogPartCache");
        net.sf.ehcache.Element element=new net.sf.ehcache.Element("projectLog",findByNearlyWeekDate());
        cache.put(element);
    }

    /**
     * 查询近一周日志
     * @return
     */
    private List<ProjectLog> findByNearlyWeekDate() {
        List<Filter> filters = new LinkedList<>();
        Date newDate=new Date();
        Calendar calendar = Calendar.getInstance();  //得到日历
        calendar.setTime(newDate);//把当前时间赋给日历
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        filters.add(Filter.between("createDate", calendar.getTime(), newDate));
        return projectLogDao.findList(0, null, filters);
    }

    /**
     * 删除缓存中超过一周时间的日志
     */
    public void deleteNotNearlyWeekLog() {
        Cache cache = cacheManager.getCache("projectLogPartCache");
        net.sf.ehcache.Element element = cache.get("projectLog");
        List<ProjectLog> projectLogs = (List<ProjectLog>) element.getValue();
        Iterator<ProjectLog> iterator = projectLogs.iterator();
        Date newDate=new Date();
        Calendar calendar = Calendar.getInstance();  //得到日历
        calendar.setTime(newDate);//把当前时间赋给日历
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        ProjectLog projectLog;
        while (iterator.hasNext()) {
            projectLog = iterator.next();
            if (projectLog.getCreateDate().getTime() > calendar.getTimeInMillis()) {
                continue;
            }
            iterator.remove();
        }
        net.sf.ehcache.Element element1 = new net.sf.ehcache.Element("projectLog", projectLogs);
        cache.put(element1);
    }

    /**
     * 从缓存中获取周内日志
     * @param projectId
     * @return
     */
    public List<ProjectLog> findByProjectIdAndWeek(Long projectId) {
        Cache cache = cacheManager.getCache("projectLogPartCache");
        net.sf.ehcache.Element element = cache.get("projectLog");
        List<ProjectLog> projectLogs = (List<ProjectLog>) SettingUtils.objectCopy(element.getValue());
        Iterator<ProjectLog> it = projectLogs.iterator();
        ProjectLog projectLog;
        while (it.hasNext()) {
            projectLog = it.next();
            if (projectLog.getProjectId() == projectId) {
                continue;
            }
            it.remove();
        }
        return projectLogs;
    }

    public List<ProjectLog> findByProjectIdAndMonth(Long projectId) {
        List<Filter> filters = new LinkedList<>();
        Date newDate=new Date();
        Calendar calendar = Calendar.getInstance();  //得到日历
        calendar.setTime(newDate);//把当前时间赋给日历
        calendar.add(Calendar.MONTH, -1);
        filters.add(Filter.eq("projectId", projectId));
        filters.add(Filter.between("createDate", calendar.getTime(), newDate));
        return projectLogDao.findList(0, null, filters);
    }

    public List<ProjectLog> findByProjectIdAndThreeMonth(Long projectId) {
        List<Filter> filters = new LinkedList<>();
        Date newDate=new Date();
        Calendar calendar = Calendar.getInstance();  //得到日历
        calendar.setTime(newDate);//把当前时间赋给日历
        calendar.add(Calendar.MONTH, -3);
        filters.add(Filter.eq("projectId", projectId));
        filters.add(Filter.between("createDate", calendar.getTime(), newDate));
        return projectLogDao.findList(0, null, filters);
    }

    public List<ProjectLog> findByProjectIdAndYear(Long projectId) {
        List<Filter> filters = new LinkedList<>();
        Date newDate=new Date();
        Calendar calendar = Calendar.getInstance();  //得到日历
        calendar.setTime(newDate);//把当前时间赋给日历
        calendar.add(Calendar.YEAR, -1);
        filters.add(Filter.eq("projectId", projectId));
        filters.add(Filter.between("createDate", calendar.getTime(), newDate));
        return projectLogDao.findList(0, null, filters);
    }
}
