package com.hysw.qqsl.cloud.core.service;

import java.text.SimpleDateFormat;
import java.util.*;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.QQSLException;
import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.core.entity.element.*;
import com.hysw.qqsl.cloud.core.entity.element.Element;
import com.hysw.qqsl.cloud.core.entity.project.Cooperate;
import com.hysw.qqsl.cloud.core.entity.project.CooperateVisit;
import com.hysw.qqsl.cloud.core.entity.project.Share;
import com.hysw.qqsl.cloud.core.entity.project.Stage;
import com.hysw.qqsl.cloud.pay.entity.PackageItem;
import com.hysw.qqsl.cloud.pay.entity.PackageModel;
import com.hysw.qqsl.cloud.pay.entity.ServeItem;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import com.hysw.qqsl.cloud.pay.service.GoodsService;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import com.hysw.qqsl.cloud.pay.service.TradeService;
import com.hysw.qqsl.cloud.util.ObjectJsonConvertUtils;
import net.sf.ehcache.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.dao.ProjectDao;
import com.hysw.qqsl.cloud.util.SettingUtils;

/**
 * 项目Service
 *
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 * @since 2015年8月21日
 */
@Service("projectService")
public class ProjectService extends BaseService<Project, Long> {
    Log logger = LogFactory.getLog(getClass());
    @Autowired
    private ProjectDao projectDao;
    @Autowired
    private UnitService unitService;
    @Autowired
    private InfoService infoService;
    @Autowired
    private ElementDBService elementDBService;
    @Autowired
    private OssService ossService;
    @Autowired
    private UserService userService;
    @Autowired
    private IntroduceService introduceService;
    @Autowired
    private CooperateService cooperateService;
    @Autowired
    private ElementGroupService elementGroupService;
    @Autowired
    private ElementDataGroupService elementDataGroupService;
    @Autowired
    private ElementService elementService;
    @Autowired
    private NoteService noteService;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private ObjectJsonConvertUtils objectJsonConvertUtils;
    @Autowired
    private FieldService fieldService;
    @Autowired
    private ShareService shareService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private SensorService sensorService;
    @Autowired
    private PackageService packageService;
    @Autowired
    private TradeService tradeService;
    @Autowired
    private CertifyService certifyService;
    @Autowired
    private ProjectLogService projectLogService;
    @Autowired
    private StorageLogService storageLogService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private StationService stationService;

    @Autowired
    public void setBaseDao(ProjectDao projectDao) {
        super.setBaseDao(projectDao);
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 刷新缓存
     *
     * @return
     */
    public Message refreshCache() throws Exception{
        Cache cache1 = cacheManager.getCache("authorization");
        cache1.removeAll();
        Cache cache2 = cacheManager.getCache("elementGroupCache");
        cache2.removeAll();
        Cache cache3 = cacheManager.getCache("unitsCache");
        cache3.removeAll();
        Cache cache4 = cacheManager.getCache("infosCache");
        cache4.removeAll();
        Cache cache5 = cacheManager.getCache("unitModelsCache");
        cache5.removeAll();
        Cache cache6 = cacheManager.getCache("projectCache");
        cache6.removeAll();
        Cache cache7 = cacheManager.getCache("projectAllCache");
        cache7.removeAll();
        Cache cache9 = cacheManager.getCache("elementDBCache");
        cache9.removeAll();
        Cache cache0 = cacheManager.getCache("elementDataGroupsCache");
        cache0.removeAll();
        Cache cache11 = cacheManager.getCache("packageAllCache");
        cache11.removeAll();
        Cache cache12 = cacheManager.getCache("userAllCache");
        cache12.removeAll();
        Cache cache13 = cacheManager.getCache("certifyAllCache");
        cache13.removeAll();
        Cache cache14 = cacheManager.getCache("projectLogPartCache");
        cache14.removeAll();
        elementGroupService.getDriElementGroups();
        elementGroupService.getConElementGroups();
        elementGroupService.getFloElementGroups();
        elementGroupService.getHydElementGroups();
        elementGroupService.getAgrElementGroups();
        elementGroupService.getWatElementGroups();
        unitService.getAgrUnitModels();
        unitService.getConUnitModels();
        unitService.getDriUnitModels();
        unitService.getFloUnitModels();
        unitService.getWatUnitModels();
        unitService.getHydUnitModels();
        unitService.getAgrUnits();
        unitService.getConUnits();
        unitService.getHydUnits();
        unitService.getWatUnits();
        unitService.getFloUnits();
        unitService.getDriUnits();
        elementDataGroupService.getElementDataSimpleGroups();
        objectJsonConvertUtils.getAgrJsonTree();
        objectJsonConvertUtils.getConJsonTree();
        objectJsonConvertUtils.getDriJsonTree();
        objectJsonConvertUtils.getFloJsonTree();
        objectJsonConvertUtils.getWatJsonTree();
        objectJsonConvertUtils.getHydJsonTree();
        certifyService.certifyCache();
        logger.info("加载认证缓存");
        userService.userCache();
        logger.info("加载用户缓存");
        packageService.packageCache();
        logger.info("加载套餐缓存");
        //将所有项目写入缓存
        projectCache();
        logger.info("项目总数为：" + findAll().size());
        //刷新infos
        infoService.infosCache();
        logger.info("项目信息总数：" + infoService.getInfos().size());
        elementDataGroupService.getElementDataSimpleGroups();
        logger.info("简单要素数据总数：" + elementDataGroupService.getElementDataSimpleGroups().size());
        positionService.format();
        positionService.init();
        logger.info("初始化千寻账号");
        monitorService.format();
        sensorService.addCodeToCache();
        logger.info("未绑定仪表加入缓存");
        projectLogService.addNearlyWeekLog();
        logger.info("加载近一周日志缓存");
        logger.info("缓存刷新完成");
        return new Message(Message.Type.OK);
    }

    /**
     * 获取用户下所有项目（自己的和别人分享的）
     *
     * @return
     */
    public Message getProjects(int start, User simpleUser) {
        List<Share> shares = shareService.getShares(simpleUser);
        if (shares.size() == 0) {
            return new Message(Message.Type.OK, new JSONArray(), "0,0");
        }
        int i = start;
        int end = i + CommonAttributes.LIMIT;
        if (i >= shares.size()) {
            return new Message(Message.Type.FAIL);
        }
        List<Share> shares1 = new ArrayList<>();
        if (shares.size() < CommonAttributes.LIMIT) {
            shares1.addAll(shares);
        } else {
            for (; i < shares.size(); i++) {
                shares1.add(shares.get(i));
                if (i >= end) {
                    break;
                }
            }
        }
        //转换为JSONObject
        JSONArray projectJsons = new JSONArray();
        JSONObject jsonObject;
        for (int j = 0; j < shares1.size(); j++) {
            jsonObject = makeProjectJsons(shares1.get(j).getProject(),true);
            projectJsons.add(jsonObject);
        }
        Message message = new Message(Message.Type.OK, projectJsons, start + "," + String.valueOf(shares.size()));
        return message;
    }

    /**
     * 子账号获取企业分享的项目
     *
     * @param start
     * @param account
     * @return
     */
    public Message getAccountProjects(int start, Account account) {
        List<Cooperate> cooperates = cooperateService.getCooperates(account);
        int i = start;
        int end = i + CommonAttributes.LIMIT;
        if (cooperates.size() == 0) {
            return new Message(Message.Type.OK, new JSONArray(), "0,0");
        }
        if (i >= cooperates.size()) {
            return new Message(Message.Type.FAIL);
        }
        List<Cooperate> cooperates1 = new ArrayList<>();
        if (cooperates.size() < CommonAttributes.LIMIT) {
            cooperates1.addAll(cooperates);
        } else {
            for (; i < cooperates.size(); i++) {
                cooperates1.add(cooperates.get(i));
                if (i >= end) {
                    break;
                }
            }
        }
        //转换为JSONObject
        JSONArray projectJsons = new JSONArray();
        JSONObject jsonObject;
        for (int j = 0; j < cooperates1.size(); j++) {
            jsonObject = makeProjectJsons(cooperates1.get(j).getProject(),false);
            projectJsons.add(jsonObject);
        }
        Message message = new Message(Message.Type.OK, projectJsons, start + "," + String.valueOf(cooperates.size()));
        return message;
    }

    /**
     * 根据当前访问对象构建单个项目,以及对应阶段的权限
     * @param id
     * @param object
     * @return
     */
    public Message getProjectBySubject(Long id,Object object) {
        Project project = find(id);
        JSONObject introduceJson = introduceService.buildIntroduceJson(project);
        // 构建启动项目短信内容
        JSONObject noteJson = noteService.makeProjectStartNote(project);
        JSONObject projectJson =makeProjectJson(project, false);
        projectJson.put("introduce", introduceJson);
        projectJson.put("note", noteJson);
        // 构建对应阶段的权限
        JSONObject stageJson = cooperateService.getStageJson(project,object);
        projectJson.put("stage", stageJson);
        return new Message(Message.Type.OK, projectJson);
    }


    /**
     * map转project
     *
     * @param map
     * @param user
     * @return
     * @throws QQSLException
     */
    public Project convertMap(Map<String, Object> map, User user, boolean isUpdate) throws QQSLException {
        Project project;
        String name = map.get("name").toString();
        String code = map.get("type") == null ? map.get("code").toString() : map.get("prefix").toString() + map.get("order").toString();
        Long planningId = Long.parseLong(map.get("planning").toString());
        if (planningId >= 5 || planningId < 0) {
            throw new QQSLException(planningId + "规划未知");
        }
        if (!SettingUtils.parameterRegex(code)) {
            throw new QQSLException("项目编号错误");
        }
        if (isUpdate == true) {
            project = new Project();
            Long projectId = Long.parseLong(map.get("id").toString());
            project.setName(name);
            project.setPlanning(planningId);
            project.setCode(code);
            project.setId(projectId);
            project.setUser(user);
            return project;
        }
        int index = Integer.parseInt(map.get("type").toString());
        //更新或保存项目前缀和序号字段
        userService.setPrefixOrderJson(map.get("prefix").toString(), map.get("order").toString(), user);
        if (index >= 6 || index < 0) {
            throw new QQSLException("项目类型未知");
        }
        userService.setPrefixOrderJson(map.get("prefix").toString(), map.get("order").toString(), user);
        project = makeProject(user, planningId, code, name, index);
        return project;
    }

    /**
     * 新建项目
     *
     * @param project
     * @return
     */
    public Message createProject(Project project) {
        List<Project> projects = findByCode(project.getCode(), project.getUser().getId());
        if (projects.size() != 0) {
            return new Message(Message.Type.EXIST);
        }
        infoService
                .saveInfo(project, 11, infoService.getPlanning(Integer
                        .valueOf(project.getPlanning().toString())));
        infoService.saveInfo(project, 13, "项目建议书");
        infoService.saveInfo(project, 8, "新建");
        super.save(project);
        userService.save(project.getUser());
        Share share = shareService.makeShare(project);
        JSONObject projectJson = makeProjectJsons(share.getProject(),false);
        return new Message(Message.Type.OK, projectJson);
    }

    /**
     * 建立项目
     *
     * @param user
     * @param planning
     * @param code
     * @param name
     * @param index
     * @return
     */
    private Project makeProject(User user, Long planning, String code,
                                String name, int index) {
        StringUtils.hasText(code);
        StringUtils.hasText(name);
        Project project = new Project();
        setType(project, index);
        project.setUser(user);
        project.setPlanning(planning);
        project.setCode(code);
        project.setName(name);
        String treePath = user.getId() + new Date().toString();
        treePath = DigestUtils.md5Hex(treePath);
        project.setTreePath(treePath);
        return project;
    }

    /**
     * 设置项目类型
     *
     * @param project
     * @param index
     */
    public void setType(Project project, int index) {
        if (index == 0) {
            project.setType(Project.Type.DRINGING_WATER);
            infoService.saveInfo(project, 7, "人畜饮水工程");
        } else if (index == 1) {
            project.setType(Project.Type.AGRICULTURAL_IRRIGATION);
            infoService.saveInfo(project, 7, "灌溉工程");
        } else if (index == 2) {
            project.setType(Project.Type.FLOOD_DEFENCES);
            infoService.saveInfo(project, 7, "防洪减灾工程");
        } else if (index == 3) {
            project.setType(Project.Type.CONSERVATION);
            infoService.saveInfo(project, 7, "水土保持工程");
        } else if (index == 4) {
            project.setType(Project.Type.HYDROPOWER_ENGINEERING);
            infoService.saveInfo(project, 7, "农村小水电工程");
        } else if (index == 5) {
            project.setType(Project.Type.WATER_SUPPLY);
            infoService.saveInfo(project, 7, "供水保障工程");
        } else {
            project.setType(Project.Type.DRINGING_WATER);
            infoService.saveInfo(project, 7, "人畜饮水工程");
        }
    }

    /**
     * 构建各个项目模版
     *
     * @param project
     * @return
     */
    public List<Unit> buildTemplate(Project project) {
        List<Unit> units = new ArrayList<>();
        if (project.getType().equals(Project.Type.AGRICULTURAL_IRRIGATION)) {
            units = unitService.getAgrUnitModels();
        }
        if (project.getType().equals(Project.Type.CONSERVATION)) {
            units = unitService.getConUnitModels();
        }
        if (project.getType().equals(Project.Type.DRINGING_WATER)) {
            units = unitService.getDriUnitModels();
        }
        if (project.getType().equals(Project.Type.FLOOD_DEFENCES)) {
            units = unitService.getFloUnitModels();
        }
        if (project.getType().equals(Project.Type.HYDROPOWER_ENGINEERING)) {
            units = unitService.getHydUnitModels();
        }
        if (project.getType().equals(Project.Type.WATER_SUPPLY)) {
            units = unitService.getWatUnitModels();
        }
        return units;
    }

    /**
     * 构建指定项目下的指定名字的单元
     *
     * @return
     */
    public Unit buildUnitByUnitAlias(Project project, String unitAlias) {
        if (project == null) {
            return null;
        }
        Unit unit;
        List<ElementDB> elementDBs = elementDBService.findByProject(project
                .getId());
        if (elementDBs == null || elementDBs.size() == 0) {
            unit = unitService.findUnit(unitAlias, false, project);
        } else {
            unit = unitService.findUnit(unitAlias, true, project);
        }
        return unit;
    }

    /**
     * 构建当前访问对象对该单元下复合要素权限
     *
     * @param project
     * @param unit
     * @param user
     * @param account
     */
    public void buildUnitAction(Project project, Unit unit, User user, Account account) {
        if (user != null) {
            if (user.getId().equals(project.getUser().getId())) {
                buildUnitActionToOwn(project, unit);
            } else {
                //给当前企业用户所有单元的查看权限
                buildUnitActionToOther(unit.getElementGroups());
            }
        }
        if (account != null) {
            buildUnitActionAccount(project, account, unit);
        }
    }

    /**
     * 构建子账号对当前单元的权限
     *
     * @param project
     * @param account
     */
    public void buildUnitActionAccount(Project project, Account account, Unit unit) {
        Cooperate cooperate = cooperateService.makeCooperate(project);
        String key = unit.getAlias().substring(0, 1);
        switch (key) {
            case "1":
                buildElementGroupActionAccount(cooperate.getInvite(), unit.getElementGroups(), account);
                break;
            case "2":
                buildElementGroupActionAccount(cooperate.getPreparation(), unit.getElementGroups(), account);
                break;
            case "3":
                buildElementGroupActionAccount(cooperate.getBuilding(), unit.getElementGroups(), account);
                break;
            case "4":
                buildElementGroupActionAccount(cooperate.getMaintenance(), unit.getElementGroups(), account);
                break;
            default:
                break;
        }
    }

    private void buildElementGroupActionAccount(Stage stage, List<ElementGroup> elementGroups, Account account) {
        Account commAccount = stage.getElementVisit().getAccount();
        if (stage.getElementVisit().getAccount() == null || stage.getFileVisit().getAccount() == null) {
            buildUnitActionToOther(elementGroups);
        }
        if (commAccount != null && commAccount.getId() == account.getId()) {
            buildElementGroupActionAccount(elementGroups, "element");
        }
        commAccount = stage.getFileVisit().getAccount();
        if (commAccount != null && commAccount.getId() == account.getId()) {
            buildElementGroupActionAccount(elementGroups, "file");
        }
    }

    private void buildElementGroupActionAccount(List<ElementGroup> elementGroups, String type) {
        if (elementGroups != null && elementGroups.size() > 0) {
            ElementGroup elementGroup;
            for (int i = 0; i < elementGroups.size(); i++) {
                elementGroup = elementGroups.get(i);
                if (type.equals("file")) {
                    if (elementGroup.getElements().get(0).getType()
                            .toString().contains("UPLOAD")) {
                        elementGroup.setAction(ElementGroup.Action.EDIT);
                        continue;
                    }
                } else {
                    if (!elementGroup.getElements().get(0).getType()
                            .toString().contains("UPLOAD")) {
                        elementGroup.setAction(ElementGroup.Action.EDIT);
                        continue;
                    }
                }
            }
        }
    }

    /**
     * 构建自己对当前单元的权限
     *
     * @param project
     * @param unit
     */
    private void buildUnitActionToOwn(Project project, Unit unit) {
        Cooperate cooperate = cooperateService.makeCooperate(project);
        String key = unit.getAlias().substring(0, 1);
        switch (key) {
            case "1":
                buildElementGroupActionOwn(cooperate.getInviteElementAccount(), cooperate.getInviteFileAccount(), unit.getElementGroups());
                break;
            case "2":
                buildElementGroupActionOwn(cooperate.getPreparationElementAccount(), cooperate.getPreparationFileAccount(), unit.getElementGroups());
                break;
            case "3":
                buildElementGroupActionOwn(cooperate.getBuildingElementAccount(), cooperate.getBuildingFileAccount(), unit.getElementGroups());
                break;
            case "4":
                buildElementGroupActionOwn(cooperate.getMaintenanceElementAccount(), cooperate.getMaintenanceFileAccount(), unit.getElementGroups());
                break;
            default:
                break;
        }
    }

    /**
     * 复合要素添加访问权限
     *
     * @param elementAccount
     * @param fileAccount
     * @param elementGroups
     */
    private void buildElementGroupActionOwn(Account elementAccount, Account fileAccount,
                                            List<ElementGroup> elementGroups) {
        if (elementGroups != null && elementGroups.size() > 0) {
            ElementGroup elementGroup;
            for (int i = 0; i < elementGroups.size(); i++) {
                elementGroup = elementGroups.get(i);
                if (elementAccount == null || fileAccount == null) {
                    if (elementAccount == null
                            && !elementGroup.getElements().get(0).getType()
                            .toString().contains("UPLOAD")) {
                        elementGroup.setAction(ElementGroup.Action.EDIT);
                        continue;
                    } else if (fileAccount == null
                            && elementGroup.getElements().get(0).getType()
                            .toString().contains("UPLOAD")) {
                        elementGroup.setAction(ElementGroup.Action.EDIT);
                        continue;
                    } else {
                        elementGroup.setAction(ElementGroup.Action.VIEW);
                        continue;
                    }
                } else {
                    elementGroup.setAction(ElementGroup.Action.VIEW);
                    continue;
                }
            }
        }
    }

    /**
     * 给当前企业用户所有单元的查看权限
     *
     * @param elementGroups
     */
    public void buildUnitActionToOther(List<ElementGroup> elementGroups) {
        if (elementGroups != null && elementGroups.size() > 0) {
            ElementGroup elementGroup;
            for (int i = 0; i < elementGroups.size(); i++) {
                elementGroup = elementGroups.get(i);
                elementGroup.setAction(ElementGroup.Action.VIEW);
            }
        }
    }

    /**
     * 查询用户对应的项目列表
     *
     * @param user
     * @return
     */
    public List<Project> findByUser(User user) {
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(Filter.eq("user", user.getId()));
        List<Project> projects = projectDao.findList(0, null, filters);
        return projects;
    }

    /**
     * 根据项目编号查找项目
     *
     * @param code
     * @return
     */
    public List<Project> findByCode(String code, Long userId) {
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(Filter.eq("code", code));
        filters.add(Filter.eq("user", userId));
        List<Project> projects = projectDao.findList(0, null, filters);
        return projects;
    }

    /**
     * 根据项目id删除项目
     *
     * @param id
     * @return
     */
    public Message removeById(Long id, User user) {
        Project project = find(id);
        if (project == null || project.getId() == null) {
            return new Message(Message.Type.FAIL);
        }
        if (!project.getUser().getId().equals(user.getId())) {
            return new Message(Message.Type.FAIL);
        }
        ossService.setBucketLife(project.getTreePath() + "/", "project");
        deleteProjectSpace(user,project.getCurSpaceNum());
        super.remove(project);
        return new Message(Message.Type.OK);
    }

    /**
     * 删除项目占用空间大小
     * @param user
     * @param curSpaceNum
     */
    private void deleteProjectSpace(User user, long curSpaceNum) {
        Package aPackage = packageService.findByUser(user);
        aPackage.setCurSpaceNum(aPackage.getCurSpaceNum() - curSpaceNum);
        packageService.save(aPackage);
    }

    /**
     * 项目基本信息修改
     *
     * @return
     * @throws QQSLException
     */
    public Message updateProject(Project project) {
        Project project1 = find(project.getId());
        Long userId = project.getUser().getId();
        project1.setPlanning(project.getPlanning());
        infoService.saveInfo(project1, 11, infoService.getPlanning(Integer
                .valueOf(project.getPlanning().toString())));
        // 查看是否有此code的项目
        List<Project> projects = findByCode(project.getCode(), userId);
        // 没有，就把项目的code和name更新
        if (projects.size() == 0) {
            project1.setName(project.getName());
            project1.setCode(project.getCode());
            save(project1);
            return new Message(Message.Type.OK);
        }
        // 有，就比对项目id，再更新
        if (project1.getId().equals(projects.get(0).getId())) {
            project1.setName(project.getName());
            project1.setCode(project.getCode());
            save(project1);
            return new Message(Message.Type.OK);
        } else {
            return new Message(Message.Type.EXIST);
        }
    }

    /**
     * 转换为projectJson字符串
     *
     * @param project
     * @return
     */
    public JSONObject makeProjectJsons(Project project,boolean flag) {
        JSONObject projectJson = new JSONObject();
        buildBaseInfo(project, projectJson,flag);
        JSONObject centerJson = getProjectCenter(project);
        projectJson.put("coordinateBase", centerJson);
        return projectJson;
    }

    /**
     * 单独一个项目的完整json字符串
     *
     * @param project
     * @return
     */
    public JSONObject makeProjectJson(Project project,boolean flag) {
        JSONObject introduceJson, centerJson;
        JSONObject projectJson = new JSONObject();
        buildBaseInfo(project, projectJson, flag);
        // 中心点坐标
        centerJson = getProjectCenter(project);
        if (!centerJson.isEmpty()) {
            projectJson.put("coordinateBase", centerJson);
        }
        // 项目简介
        introduceJson = introduceService.buildIntroduceJson(project);
        projectJson.put("introduce", introduceJson);
        //构建坐标数据
        JSONObject design = fieldService.field(project, Build.Source.DESIGN);
        if (!design.isEmpty()) {
            projectJson.put("coordinates", design);
        }
        JSONObject field = fieldService.field(project, Build.Source.FIELD);
        if (!field.isEmpty()) {
            projectJson.put("measures", field);
        }
        return projectJson;
    }

    /**
     * 获取项目中心坐标
     *
     * @param project
     */
    public JSONObject getProjectCenter(Project project) {
        JSONObject centerJson = new JSONObject();
        List<ElementDB> list = elementDBService.findByProjectAndAlias(project);
        if (list != null && list.size() != 0 && StringUtils.hasText(list.get(0).getValue())) {
            String s = getCenterJson(list.get(0).getValue());
            if (!s.equals("")) {
                centerJson = JSONObject.fromObject(s);
            } else {
//                logger.info("zuobiao:"+project.getId());
                return centerJson;
            }
        }
        return centerJson;
    }

    /**
     * 构建项目的基本信息
     *
     * @param project
     * @param projectJson
     */
    private void buildBaseInfo(Project project, JSONObject projectJson,boolean flag) {
        projectJson.put("id", project.getId());
        projectJson.put("createDate", project.getCreateDate().getTime());
        projectJson.put("modifyDate", project.getModifyDate().getTime());
        projectJson.put("name", project.getName());
        projectJson.put("code", project.getCode());
        projectJson.put("type", project.getType());
        projectJson.put("treePath", project.getTreePath());
        projectJson.put("buildArea", project.getBuildArea());
        projectJson.put("planning", project.getPlanning());
//        projectJson.put("logStr", project.getLogStr());
        projectJson.put("infoStr", project.getInfoStr());
        projectJson.put("shares", project.getShares());
        projectJson.put("views", project.getViews());
        setCooperate(project,projectJson,flag);
        projectJson.put("iconType", project.getIconType());
        JSONObject userJson = userService.makeSimpleUserJson(project.getUser());
        projectJson.put("user", userJson);
    }

    /**
     * 构建协同编辑状态
     *
     * @param project
     * @param projectJson
     * @param flag
     */
    private void setCooperate(Project project, JSONObject projectJson, boolean flag) {
        if (!flag) {
            return;
        }
        if (project.getCooperate() != null) {
            JSONObject jsonObject = JSONObject.fromObject(project.getCooperate());
            JSONObject jsonObject1;
            if (jsonObject.get("invite") != null) {
                jsonObject1 = (JSONObject) jsonObject.get("invite");
                if (jsonObject1 != null) {
                    if (jsonObject1.get("element") != null) {
                        addEditStatus(jsonObject1, "element", CooperateVisit.Type.VISIT_INVITE_ELEMENT, project.getId());
                    }
                    if (jsonObject1.get("file") != null) {
                        addEditStatus(jsonObject1, "file", CooperateVisit.Type.VISIT_INVITE_FILE, project.getId());
                    }
                }
            }
            if (jsonObject.get("preparation") != null) {
                jsonObject1 = (JSONObject) jsonObject.get("preparation");
                if (jsonObject1 != null) {
                    if (jsonObject1.get("element") != null) {
                        addEditStatus(jsonObject1, "element", CooperateVisit.Type.VISIT_PREPARATION_ELEMENT, project.getId());
                    }
                    if (jsonObject1.get("file") != null) {
                        addEditStatus(jsonObject1, "file", CooperateVisit.Type.VISIT_PREPARATION_FILE, project.getId());
                    }
                }
            }
            if (jsonObject.get("building") != null) {
                jsonObject1 = JSONObject.fromObject(jsonObject.get("building"));
                if (jsonObject1 != null) {
                    if (jsonObject1.get("element") != null) {
                        addEditStatus(jsonObject1, "element", CooperateVisit.Type.VISIT_BUILDING_ELEMENT, project.getId());
                    }
                    if (jsonObject1.get("file") != null) {
                        addEditStatus(jsonObject1, "file", CooperateVisit.Type.VISIT_BUILDING_FILE, project.getId());
                    }
                }
            }
            if (jsonObject.get("maintenance") != null) {
                jsonObject1 = JSONObject.fromObject(jsonObject.get("maintenance"));
                if (jsonObject1 != null) {
                    if (jsonObject1.get("element") != null) {
                        addEditStatus(jsonObject1, "element", CooperateVisit.Type.VISIT_MAINTENANCE_ELEMENT, project.getId());
                    }
                    if (jsonObject1.get("file") != null) {
                        addEditStatus(jsonObject1, "file", CooperateVisit.Type.VISIT_MAINTENANCE_FILE, project.getId());
                    }
                }
            }
            projectJson.put("cooperate", jsonObject);
        }
    }

    /**
     * 增加协同状态
     * @param jsonObject1
     * @param sign
     * @param type
     * @param id
     */
    private void addEditStatus(JSONObject jsonObject1, String sign, CooperateVisit.Type type, Long id) {
        long l = Long.valueOf(((JSONObject)jsonObject1.get(sign)).get("createTime").toString());
        ProjectLog projectLog = projectLogService.findByCooperateType(type, id);
        if (projectLog == null) {
            ((JSONObject)(jsonObject1.get(sign))).put("editStatus", false);
        }else{
            if (projectLog.getCreateDate().getTime() > l) {
                ((JSONObject)(jsonObject1.get(sign))).put("editStatus", true);
            }else{
                ((JSONObject)(jsonObject1.get(sign))).put("editStatus", false);
            }
        }
    }

    /**
     * 获取中心点坐标
     *
     * @param center
     * @return
     */
    public String getCenterJson(String center) {
        if (center == null) {
            return "";
        }
        String[] coordinatesStr = center.split(",");
        JSONObject centerJson = new JSONObject();
        if (coordinatesStr.length == 3) {
            centerJson.put("longitude", String.valueOf(coordinatesStr[0]));
            centerJson.put("latitude", String.valueOf(coordinatesStr[1]));
            centerJson.put("elevation", String.valueOf(coordinatesStr[2]));
        }
        if (centerJson.isEmpty()) {
            return "";
        }
        return centerJson.toString();
    }

    /**
     * 要素输出
     *
     * @param object
     * @param type
     * @param jsonUnits
     * @param alias
     * @return
     */
    public List<JsonUnit> getExportValues(Object object, Project.Type type, List<JsonUnit> jsonUnits, String alias) {
        List<Project> projects;
        if (object instanceof User) {
            projects = findByUserAndType((User) object, type);
        } else {
            projects = findProjectsByAccountAndType((Account) object, type);
        }
        if (projects == null || projects.size() == 0) {
            return null;
        }
        JsonUnit jsonUnit;
        List<Element> elements;
        for (Project project : projects) {
            // 构建单元
            Unit unit = unitService.findUnit(alias, true, project);
            if (unit == null) {
                continue;
            }
            elements = elementService.bulidExportElement(unit);
            jsonUnit = getJsonUnit(project.getId(), elements);
            jsonUnits.add(jsonUnit);
        }
        return jsonUnits;
    }

    /**
     * 查询企业用户某一类型的项目
     * @param user
     * @param type
     * @return
     */
    private List<Project> findByUserAndType(User user, Project.Type type) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("user", user.getId()));
        filters.add(Filter.eq("type", type.ordinal()));
        List<Project> projects = projectDao.findList(0, null, filters);
        return projects;
    }

    /**
     * 查询企业用户分配给子账号的某一类型的项目
     * @param account
     * @param type
     * @return
     */
    private List<Project> findProjectsByAccountAndType(Account account, Project.Type type) {
        List<Project> projects;
        List<Project> accProjects = new ArrayList<>();
        Project project;
        List<User> users = accountService.getUsersByAccountId(account.getId());
        if (users == null || users.size() == 0) {
            return accProjects;
        }
        projects = findByUsersAndType(users, type);
        if (projects == null || projects.size() == 0) {
            return accProjects;
        }
        for (int i = 0; i < projects.size(); i++) {
            project = projects.get(i);
            if (cooperateService.isCooperate(project, account)) {
                accProjects.add(project);
            }
        }
        return accProjects;
    }

    /**
     * 查询一些企业用户的某一类型的项目
     * @param users
     * @param type
     * @return
     */
    private List<Project> findByUsersAndType(List<User> users, Project.Type type) {
        List<Long> userIds = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            userIds.add(users.get(i).getId());
        }
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.in("user", users));
        filters.add(Filter.eq("type", type.ordinal()));
        List<Project> projects = projectDao.findList(0, null, filters);
        return projects;
    }

    /**
     * 查询企业用户分配给子账号的项目
     * @param account
     * @return
     */
    public List<Project> findProjectsByAccount(Account account) {
        List<Project> projects;
        List<Project> accProjects = new ArrayList<>();
        Project project;
        List<User> users = accountService.getUsersByAccountId(account.getId());
        if (users == null || users.size() == 0) {
            return accProjects;
        }
        projects = findByUsers(users);
        if (projects == null || projects.size() == 0) {
            return accProjects;
        }
        for (int i = 0; i < projects.size(); i++) {
            project = projects.get(i);
            if (cooperateService.isCooperate(project, account)) {
                accProjects.add(project);
            }
        }
        return accProjects;
    }

    /**
     * 查询一些企业用户的项目
     * @param users
     * @return
     */
    private List<Project> findByUsers(List<User> users) {
        List<Long> userIds = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            userIds.add(users.get(i).getId());
        }
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.in("user", users));
        List<Project> projects = projectDao.findList(0, null, filters);
        return projects;
    }

    /**
     * 根据项目建立jsonUnit
     *
     * @param id
     * @param elements
     * @return
     */
    private JsonUnit getJsonUnit(Long id, List<Element> elements) {
        JsonUnit jsonUnit = new JsonUnit();
        JsonElement jsonElement;
        jsonUnit.setProjectId(id);
        for (Element element : elements) {
            jsonElement = null;
            if (element.getValue() != null) {
                if (element.getUnit() != null) {
                    jsonElement = new JsonElement(element.getName(),
                            element.getValue() + element.getUnit());
                } else {
                    jsonElement = new JsonElement(element.getName(),
                            element.getValue());
                }
            }
            if (element.getElementDataStr() != null) {
                jsonElement = new JsonElement(element.getName(),
                        element.getElementDataStr());
            }
            if (jsonElement != null) {
                jsonUnit.getJsonElements().add(jsonElement);
            }
        }
        return jsonUnit;
    }


    /**
     * 编辑全景地图连接
     *
     * @param url
     * @param project
     */
    public void editPanoramicUrl(Object url, Project project) {
        if (url != null && StringUtils.hasText(url.toString())) {
            infoService.saveInfo(project, 14, url.toString());
        } else {
            JSONArray jsonArray = new JSONArray();
            List<JSONObject> infos = jsonArray.fromObject(project.getInfoStr());
            JSONObject infoJson;
            for (int i = 0; i < infos.size(); i++) {
                infoJson = infos.get(i);
                if (infoJson.get("order").equals("14")) {
                    infos.remove(i);
                    break;
                }
            }
            project.setInfoStr(infos.toString());
        }
        save(project);
    }

    public void projectCache() {
        List<Project> projects = projectDao.findList(0, null, null);
//        Setting setting = SettingUtils.getInstance().getSetting();
//        String status = setting.getStatus();
//        if (status.equals("run")==true || status.equals("dev")==true) {
//            for (int i = 0; i < projects.size(); i++) {
//                buildLog(projects.get(i));
//            }
//        }
        Cache cache = cacheManager.getCache("projectAllCache");
        net.sf.ehcache.Element element = new net.sf.ehcache.Element("project", projects);
        cache.put(element);
    }

//    /**
//     * 构建共享权限
//     *
//     * @param project
//     */
//    private void buildLog(Project project) {
//        List<JSONObject> logs = logService.getLogJsonsByProject(project.getId());
//        if (logs != null && !logs.isEmpty()) {
//            project.setLogStr(logs.toString());
//        }
//    }

    @Override
    public List<Project> findAll() {
        Cache cache = cacheManager.getCache("projectAllCache");
        net.sf.ehcache.Element element = cache.get("project");
        return (List<Project>) element.getValue();
    }

    @Override
    public Project find(Long id) {
        Project project = projectDao.find(id);
        return project;

    }


    /**
     * 管理获取所有项目，并构建json
     *
     * @return
     */
    public List<JSONObject> getProjectJsons() {
        List<JSONObject> projectJsons = new ArrayList<>();
        List<Project> projects = findAll();
        Project project;
        JSONObject projectJson;
        for (int i = 0; i < projects.size(); i++) {
            projectJson = new JSONObject();
            project = projects.get(i);
            buildBaseInfo(project, projectJson,true);
            projectJsons.add(projectJson);
        }
        return projectJsons;
    }

    /**
     * 上传文件大小计入项目与套餐中
     * @param map
     * @param user
     * @return
     */
    public Message uploadFileSize(Map<String, Object> map, User user) {
        Object projectId = map.get("projectId");
        Object fileSize = map.get("fileSize");
        Object fileNames = map.get("fileNames");//用于记录日志
        Object alias = map.get("alias");
        if (projectId == null || fileSize == null || fileNames == null || alias == null) {
            return new Message(Message.Type.FAIL);
        }
        Project project = find(Long.valueOf(projectId.toString()));
        Package aPackage = packageService.findByUser(user);
        if (project == null || aPackage == null) {
            return new Message(Message.Type.FAIL);
        }
        try {
            project.setCurSpaceNum(project.getCurSpaceNum() + Long.valueOf(fileSize.toString()));
            aPackage.setCurSpaceNum(aPackage.getCurSpaceNum() + Long.valueOf(fileSize.toString()));
            aPackage.setCurTrafficNum(aPackage.getCurTrafficNum()+Long.valueOf(fileSize.toString()));
        } catch (Exception e) {
            return new Message(Message.Type.FAIL);
        }
        save(project);
        packageService.save(aPackage);
        storageLogService.saveStorageLog(aPackage, "upload", fileSize);
        Map<String, String> aliases = new LinkedHashMap<>();
        aliases.put(alias.toString(),fileNames.toString());
        projectLogService.saveLog(project,user,aliases,ProjectLog.Type.FILE_UPLOAD);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileSize", fileSize);
        return new Message(Message.Type.OK, jsonObject);
    }

    /**
     * 下载文件大小计入套餐中
     * @param map
     * @param user
     * @return
     */
    public Message downloadFileSize(Map<String, Object> map, User user) {
        Object projectId = map.get("projectId");
        Object fileSize = map.get("fileSize");
        Object fileName = map.get("fileName");//用于记录日志
        Object alias = map.get("alias");
        if (projectId == null || fileSize == null || fileName == null || alias == null) {
            return new Message(Message.Type.FAIL);
        }
        Package aPackage = packageService.findByUser(user);
        if (aPackage == null) {
            return new Message(Message.Type.FAIL);
        }
        try {
            aPackage.setCurTrafficNum(aPackage.getCurTrafficNum()+Long.valueOf(fileSize.toString()));
        } catch (Exception e) {
            return new Message(Message.Type.FAIL);
        }
        packageService.save(aPackage);
        storageLogService.saveStorageLog(aPackage,"download",fileSize);
        Map<String, String> aliases = new LinkedHashMap<>();
        aliases.put(alias.toString(),fileName.toString());
        projectLogService.saveLog(find(Long.valueOf(projectId.toString())),user,aliases,ProjectLog.Type.FILE_UPLOAD);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileSize", fileSize);
        return new Message(Message.Type.OK, jsonObject);
    }



    /**
     * 删除文件大小计入项目和套餐中
     * @param map
     * @param user
     * @return
     */
    public Message deleteFileSize(Map<String, Object> map, User user) {
        Object projectId = map.get("projectId");
        Object alias = map.get("alias");
        Object fileSize = map.get("fileSize");
        Object fileName = map.get("fileName");
        if (projectId == null || fileSize == null || alias == null || fileName == null) {
            return new Message(Message.Type.FAIL);
        }
        Project project = find(Long.valueOf(projectId.toString()));
        if (user == null) {
            user = project.getUser();
        }
        Package aPackage = packageService.findByUser(user);
        if (project == null || aPackage == null) {
            return new Message(Message.Type.FAIL);
        }
        try {
            project.setCurSpaceNum(project.getCurSpaceNum() - Long.valueOf(fileSize.toString()));
            aPackage.setCurSpaceNum(aPackage.getCurSpaceNum() - Long.valueOf(fileSize.toString()));
        } catch (Exception e) {
            return new Message(Message.Type.FAIL);
        }
        save(project);
        packageService.save(aPackage);
        storageLogService.saveStorageLog(aPackage,"delete",fileSize);
        Map<String, String> aliases = new LinkedHashMap<>();
        aliases.put(alias.toString(),fileName.toString());
        projectLogService.saveLog(project,user,aliases,ProjectLog.Type.FILE_DELETE);
        return new Message(Message.Type.OK);
    }

    /**
     * 判断计入次文件大小后是否满足限制条件
     * @param user
     * @return
     */
    public Message isAllowUpload(User user) {
        Package aPackage = packageService.findByUser(user);
        if (aPackage == null) {
            return new Message(Message.Type.NO_ALLOW);
        }
        PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
        for (PackageItem packageItem : packageModel.getPackageItems()) {
            if (packageItem.getServeItem().getType() == ServeItem.Type.SPACE && aPackage.getCurSpaceNum() <= packageItem.getLimitNum() && aPackage.getCurTrafficNum() <= packageItem.getLimitNum() * 10l) {
                return new Message(Message.Type.OK);
            }
        }
        return new Message(Message.Type.NO_ALLOW);
    }

    /**
     * 是否允许下载
     * @param user
     * @return
     */
    public Message isAllowDownload(User user) {
        Package aPackage = packageService.findByUser(user);
        if (aPackage == null) {
            return new Message(Message.Type.NO_ALLOW);
        }
        PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
        for (PackageItem packageItem : packageModel.getPackageItems()) {
            if (packageItem.getServeItem().getType() == ServeItem.Type.SPACE && aPackage.getCurTrafficNum() <= packageItem.getLimitNum() * 10) {
                return new Message(Message.Type.OK);
            }
        }
        return new Message(Message.Type.NO_ALLOW);
    }

    /**
     * 是否允许创建项目，如果允许，套餐内增加项目创建初始大小
     * @param user
     * @return
     */
    public Message isAllowCreateProject(User user) {
        Package aPackage = packageService.findByUser(user);
        if (aPackage == null) {
            return new Message(Message.Type.NO_ALLOW);
        }
        PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
        long l = findByUser(user).size();
        for (PackageItem packageItem : packageModel.getPackageItems()) {
            if (packageItem.getServeItem().getType() == ServeItem.Type.PROJECT && l < packageItem.getLimitNum()) {
                return new Message(Message.Type.OK);
            }
        }
        return new Message(Message.Type.NO_ALLOW);
    }

    public Message isAllowBim(User user) {
        Package aPackage = packageService.findByUser(user);
        if (aPackage == null) {
            return new Message(Message.Type.NO_ALLOW);
        }
        PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
        for (PackageItem packageItem : packageModel.getPackageItems()) {
            if (packageItem.getServeItem().getType() == ServeItem.Type.BIMSERVE) {
                return new Message(Message.Type.OK);
            }
        }
        return new Message(Message.Type.NO_ALLOW);
    }

    /**
     * 项目图标类型定制
     *
     * @param user
     * @param map
     * @return
     */
    public Message iconTypeUpdate(User user, Map<String, Object> map) {
        Object id = map.get("id");
        Object iconType = map.get("iconType");
        if (id == null || iconType == null) {
            return new Message(Message.Type.FAIL);
        }
        Project project;
        try {
            project = find(Long.valueOf(id.toString()));
        } catch (Exception e) {
            return new Message(Message.Type.FAIL);
        }
        if (project == null) {
            return new Message(Message.Type.EXIST);
        }
        if (!project.getUser().getId().equals(user.getId())) {
            return new Message(Message.Type.FAIL);
        }
        project.setIconType(Project.IconType.valueOf(iconType.toString()));
        save(project);
        return new Message(Message.Type.OK);
    }
}
