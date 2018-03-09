package com.hysw.qqsl.cloud.core.controller;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hysw.qqsl.cloud.annotation.util.*;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.ProjectLog;
import com.hysw.qqsl.cloud.core.service.*;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.element.Info;
import com.hysw.qqsl.cloud.core.entity.element.JsonTree;
import com.hysw.qqsl.cloud.core.entity.element.JsonUnit;
import com.hysw.qqsl.cloud.core.entity.QQSLException;
import com.hysw.qqsl.cloud.core.entity.element.Unit;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.util.ObjectJsonConvertUtils;

@Controller
@RequestMapping("/project")
public class ProjectController {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UnitService unitService;
    @Autowired
    private InfoService infoService;
    @Autowired
    private UserService userService;
    @Autowired
    private NoteService noteService;
    @Autowired
    private ElementService elementService;
    @Autowired
    private ContactService contactService;
    @Autowired
    private ObjectJsonConvertUtils objectJsonConvertUtils;
    @Autowired
    private AuthentService authentService;
    @Autowired
    private ShareService shareService;
    @Autowired
    private CooperateService cooperateService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ProjectLogService projectLogService;
    @Autowired
    private PollingService pollingService;


    /**
     * 取得当前用户或子帐号对应的项目列表
     *
     * @param start 起始值,默认为0;当用户项目数大于200时,start为上次获取到的
     *              项目列表的size,循环获取,直到获取全部项目
     * @return message消息体, FAIL:参数不合理,OK:获取成功,包含项目列表,以及
     * 此次获取的起始值和终止值组成的字符串
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/lists", method = RequestMethod.GET)
    public @ResponseBody
    Message getProjects(@RequestParam int start) {
        Message message;
        User user = authentService.getUserFromSubject();
        if (user != null) {
            user = userService.getSimpleUser(user);
            message = projectService.getProjects(start, user);
            pollingService.changeShareStatus(user, false);
            return message;
        }
        Account account = authentService.getAccountFromSubject();
        if (account != null) {
            account = accountService.getSimpleAccount(account);
            message = projectService.getAccountProjects(start, account);
            pollingService.changeCooperateStatus(account, false);
            return message;
        }
        return MessageService.message(Message.Type.FAIL);
    }


    /**
     * 保存新建的项目
     *
     * @param objectMap 包含项目名称name,项目类型type,项目前缀prefix以及项目序号order,
     *                  规划planning
     * @return message消息体, FAIL:参数不全,或参数不合理;NO_ALLOW:套餐限制
     */
    @PackageIsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public
    @ResponseBody
    Message createProject(
            @RequestBody Map<String, String> objectMap) {
        Message message = MessageService.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        User user = authentService.getUserFromSubject();
        //是否可以创建项目
        message = projectService.isAllowCreateProject(user);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        try {
            Project project = projectService.convertMap(map, user, false);
            message = projectService.createProject(project);
        } catch (QQSLException e) {
            e.printStackTrace();
            return MessageService.message(Message.Type.FAIL, e.getMessage());
        } catch (Exception e1) {
            e1.printStackTrace();
            return MessageService.message(Message.Type.FAIL);
        }
        return message;
    }

    /**
     * 获取项目类型
     *
     * @return message OK:获取成功
     */
    @RequiresAuthentication
    @RequestMapping(value = "/typeSelects", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getSelects() {
        List<Info> infos = infoService.getInfos();
        return MessageService.message(Message.Type.OK, infos.get(7));
    }

    /**
     * 根据项目id得到单元树形结构json
     *
     * @param id 项目id
     * @return message消息体, FAIL:参数错误;EXIT:项目不存在;NO_AUTHORIZE:没有操作权限,OK:获取成功,附带树形结构JSON数据
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/tree/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getTreeJsons(
            @PathVariable("id") Long id) {
        Message message = MessageService.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Project project = projectService.find(id);
        if (project == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        User user = authentService.getUserFromSubject();
        Account account = authentService.getAccountFromSubject();
        if (!isOperate(user, account, project)) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        List<Unit> units = projectService.buildTemplate(project);
        return MessageService.message(Message.Type.OK, objectJsonConvertUtils.getJsons(project, units));
    }

    /**
     * 修改新建的项目
     *
     * @param objectMap 包含项目名称name,项目编号code,规划planning,以及项目id
     * @return message消息体, FAIL:参数不全,或参数不合法,EXIT:项目编号已存在,OK:编辑成功
     */
    @PackageIsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public @ResponseBody
    Message updateProject(@RequestBody Map<String, String> objectMap) {
        Message message = MessageService.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        User user = authentService.getUserFromSubject();
        try {
            Project newProject = projectService.convertMap(map, user, true);
            message = projectService.updateProject(newProject);
        } catch (QQSLException e) {
            e.printStackTrace();
            return MessageService.message(Message.Type.FAIL);
        }
        return message;
    }

    /**
     * 根据项目编号删除项目
     *
     * @param id 项目id
     * @return message消息体, FAIL:删除失败;EXIT:项目不存在;NO_AUTHORIZE:没有删除权限,OK:删除成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/removeProject/{id}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    Message removeProjectByCode(
            @PathVariable("id") Long id) {
        Message message = MessageService.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        return projectService.removeById(id, user);
    }

    /**
     * 根据项目编号查询项目
     *
     * @return
     */
  /*  @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/findByCode/{code}", method = RequestMethod.GET)
    public
    @ResponseBody
    Message findProject(
            @PathVariable("code") Object object) {
        Message message = Message.parametersCheck(object);
        if(message.getType()== Message.Type.FAIL){
            return message;
        }
        User user = authentService.getUserFromSubject();
        String code = (String) object;
        List<Project> projects = projectService.findByCode(code,user.getId());
        if (projects.size() == 0) {
            return MessageService.message(Message.Type.OK);
        }
        return MessageService.message(Message.Type.FAIL);
    }*/

    /**
     * 查询项目信息
     *
     * @return message消息体, OK:获取成功,附带info消息
     */
//    @RequiresAuthentication
    @RequestMapping(value = "/infos", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Message getInfos() {
        return MessageService.message(Message.Type.OK, infoService.getInfos());
    }

    /**
     * 根据项目type得到项目模版单元树形结构json,用于要素输出
     *
     * @param type 项目type
     * @return message消息体, FAIL:参数错误或不合法,OK:项目模版单元树形结构json获取成功,附带json数据
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/template/{type}", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getTemplateJsons(
            @PathVariable("type") String type) {
        Message message = MessageService.parametersCheck(type);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Project project = new Project();
        try {
            project.setType(Project.Type.valueOf(type));
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return MessageService.message(Message.Type.FAIL);
        }
        List<JsonTree> jsonTrees = objectJsonConvertUtils.getTemplateJsonTree(project);
        return MessageService.message(Message.Type.OK, jsonTrees);
    }

    /**
     * 根据项目id，单元别名，得到单元(附带要素值)
     *
     * @param id    项目id
     * @param alias 单元别名
     * @return message消息体, FAIL:参数错误,NO_AUTHORIZE:无权操作,OK:获取成功,附带单元json数据
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/unit", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getUnit(@RequestParam long id,
                    @RequestParam String alias) {
        Message message = MessageService.parametersCheck(id, alias);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        Account account = authentService.getAccountFromSubject();
        Project project = projectService.find(id);
        Unit unit = projectService.buildUnitByUnitAlias(project, alias);
        if (unit == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        if (!isOperate(user, account, project)) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        projectService.buildUnitAction(project, unit, user, account);
        if (user != null) {
            for (int i = 0; i < unit.getElementGroups().size(); i++) {
                contactService.findPhase(unit, unit.getElementGroups().get(i));
            }
        }
        //构建单元json串
        JSONObject unitJson = unitService.makeUnitJson(unit);
        return MessageService.message(Message.Type.OK, unitJson);
    }

    /**
     * 取得要素输出界面需要的要素值
     *
     * @param alias       单元别名
     * @param projectType 项目类型
     * @return message消息体, FAIL:参数不全,OK:获取成功,附带所有此类项目的单元json数据
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/values", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getValues(@RequestParam String alias,
                      @RequestParam String projectType) {
        Message message = MessageService.parametersCheck(alias, projectType);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        List<JsonUnit> jsonUnits = new ArrayList<JsonUnit>();
        User user = authentService.getUserFromSubject();
        Account account = authentService.getAccountFromSubject();
        // 取得项目id列表
        if (projectType.length() == 0 || alias.equals(CommonAttributes.TOP_TREE_ID)) {
            return MessageService.message(Message.Type.OK, jsonUnits);
        }
        Project.Type type = Project.Type.valueOf(projectType);
        if (user != null) {
            jsonUnits = projectService.getExportValues(user, type, jsonUnits, alias);
        }
        if (account != null) {
            jsonUnits = projectService.getExportValues(account, type, jsonUnits, alias);
        }
        return MessageService.message(Message.Type.OK, jsonUnits);
    }


    /**
     * 根据项目id得到项目包括项目信息和项目简介以及短信
     *
     * @param id 项目id
     * @return message消息体, FAIL:参数错误,NO_AUTHORIZE:没有操作权限,OK:获取成功,包含"introduce"简介 ,"note"短信,"stage"对于阶段的权限
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/project/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getProject(@PathVariable("id") Long id) {
        Message message = MessageService.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        Account account = authentService.getAccountFromSubject();
        Project project = projectService.find(id);
        if (!isOperate(user, account, project)) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        message = projectService.getProjectBySubject(id, user == null ? account : user);
        return message;
    }

    /**
     * 根据项目id得到项目及项目简介信息
     *
     * @param id 项目id
     * @return messasge消息体, FAIL:参数错误;EXIST:项目不存在,OK:获取成功,附带项目简介,中心坐标,设计和外业信息
     */
    @RequestMapping(value = "/projectIntroduce/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getProjectIntroduce(@PathVariable("id") Long id) {
        Message message = MessageService.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Project project = projectService.find(id);
        if (project == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        JSONObject projectJson = projectService.makeProjectJson(project, false);
        return MessageService.message(Message.Type.OK, projectJson);
    }

    /**
     * 向项目的业主和负责人发送短信
     *
     * @param objectMap 包含contact字符串,以及消息内容sendMsg
     * @return message消息体, FAIL:参数不合法,OK:发送成功
     */
    @PackageIsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
    public
    @ResponseBody
    Message sendMessage(
            @RequestBody Map<String, String> objectMap) {
        Message message = MessageService.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Map<String, String> realMap = (Map<String, String>) message.getData();
        String contact = realMap.get("contact");
        String sendMsg = realMap.get("sendMsg");
        List<String> contacts = new ArrayList<String>();
        if (contact.indexOf(",") != -1) {
            List<String> contacts1 = Arrays.asList(contact.split(","));
            for (String phone : contacts1) {
                if (!contacts.contains(phone)) {
                    contacts.add(phone);
                }
            }
        } else {
            contacts.add(contact);
        }
        return noteService.addToNoteCache(contacts, sendMsg);
    }


//    /**
//     * 保存文件日志
//     * @param
//     * @return
//     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
//    @RequestMapping(value = "/fileLog", method = RequestMethod.POST)
//    public
//    @ResponseBody
//    Message fileLog(@RequestBody  Object object) {
//        Message message = Message.parameterCheck(object);
//        if(message.getType()==Message.Type.FAIL){
//            return message;
//        }
//        Map<String,Object> map = (Map<String,Object>)message.getData();
//        User user = authentService.getUserFromSubject();
//        User simpleUser = userService.getSimpleUser(user);
//        message = elementService.saveFileLog(simpleUser,map);
//        return message;
//    }

    /**
     * 根据请求的项目返回相应类型的外业测量要素
     *
     * @param id    项目id
     * @param alias 单元别名
     * @return message消息体, FAIl:参数错误,OK:获取成功,附带单元数据
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/uploadModel", method = RequestMethod.GET)
    public @ResponseBody
    Message uploadModel(@RequestParam long id, @RequestParam String alias) {
        Message message = MessageService.parametersCheck(id, alias);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Project project = projectService.find(id);
        Unit unit = projectService.buildUnitByUnitAlias(project, alias);
        return MessageService.message(Message.Type.OK, unit);
    }

    /**
     * 添加全景地图连接
     *
     * @param object 包含项目id,全景url
     * @return message消息体, UNKNOWN:参数不合法,OK:编辑成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/editPanoramicUrl", method = RequestMethod.POST)
    public @ResponseBody
    Message addPanoramicUrl(@RequestBody Object object) {
        Map<String, Object> map = (Map<String, Object>) object;
        long id = Long.valueOf(map.get("id").toString());
        Project project = projectService.find(id);
        Object url = map.get("url");
        if (url != null && StringUtils.hasText(url.toString())) {
            if (!SettingUtils.httpUrlRegex(url.toString())) {
                return MessageService.message(Message.Type.FAIL);
            }
        }
        projectService.editPanoramicUrl(url, project);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 企业间分享项目
     *
     * @param map 包含projectIds,userIds
     * @return message消息体, FAIL:分享失败,OK:分享成功
     */
    @PackageIsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/share", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Message share(@RequestBody Map<String, Object> map) {
        Message message = MessageService.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        List<Integer> projectIds = (List<Integer>) map.get("projectIds");
        List<Integer> userIds = (List<Integer>) map.get("userIds");
        User own = authentService.getUserFromSubject();
        return shareService.shares(projectIds, userIds, own);
    }

    /**
     * 取消企业间分享项目
     *
     * @param map 项目标识projectId,企业标识:userIds
     * @return message消息体 FAIL:取消失败,UNKNOWN:项目不存在,OK:取消成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/unShare", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Message unShare(@RequestBody Map<String, Object> map) {
        Message message = MessageService.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Long projectId = Long.valueOf(map.get("projectId").toString());
        Project project = projectService.find(projectId);
        if (project == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        List<Integer> userIds = (List<Integer>) map.get("userIds");
        User own = authentService.getUserFromSubject();
        if (!project.getUser().getId().equals(own.getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        shareService.unShares(project, userIds, own);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 企业账号查看子账号协同情况
     *
     * @param accountId 子帐号标识accountId
     * @return message消息体, FAIL:参数错误,UNKNOWN:子帐号不存在,OK:获取成功,包含对应项目的协同权限
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/viewCooperate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Message viewCooperate(@RequestParam long accountId) {
        Message message = MessageService.parametersCheck(accountId);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Account account = accountService.find(accountId);
        User user = authentService.getUserFromSubject();
        if (account == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        return cooperateService.viewCooperate(user, account);
    }

    /**
     * 企业账号取消子账号查看权限
     *
     * @param map 包含项目标识projectId,以及子帐号标识accountIds
     * @return message消息体, FAIL:参数错误,UNKNOWN:项目不存在或没有操作权限,OK:取消成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/unView", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Message unViews(@RequestBody Map<String, Object> map) {
        Message message = MessageService.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Long projectId = Long.valueOf(map.get("projectId").toString());
        List<Integer> accountIds = (List<Integer>) map.get("accountIds");
        Project project = projectService.find(projectId);
        User own = authentService.getUserFromSubject();
        if (project == null || !project.getUser().getId().equals(own.getId())) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        cooperateService.unViews(project, accountIds, own);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 将一个项目的多个权限分享给一个子账号
     *
     * @param map 包含项目标识projectId,以及权限类型types,子帐号标识accountId
     * @return message消息体, FAIL:参数不合法,OK:分享成功
     */
    @PackageIsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/cooperateMul", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Message cooperateMult(@RequestBody Map<String, Object> map) {
        Message message = MessageService.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if (map.get("projectId") == null || !StringUtils.hasText(map.get("projectId").toString())
                || map.get("types") == null || !StringUtils.hasText(map.get("types").toString())
                || map.get("accountId") == null || !StringUtils.hasText(map.get("accountId").toString())) {
            return MessageService.message(Message.Type.FAIL);
        }
        Long projectId = Long.valueOf(map.get("projectId").toString());
        Long accountId = Long.valueOf(map.get("accountId").toString());
        String typeStr = map.get("types").toString();
        Account account = accountService.find(accountId);
        if (account == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (account.getName() == null) {
            return MessageService.message(Message.Type.ACCOUNT_NOINFO);
        }
        Project project = projectService.find(projectId);
        //判断子账号,项目归属
        User own = authentService.getUserFromSubject();
        return cooperateService.cooperateMult(project, typeStr, account, own);

    }

    /**
     * 将多个项目的一个权限分享给一个子账号
     *
     * @param map 包含项目标识projectIds,以及权限类型type,子帐号标识accountId
     * @return message消息体, FAIL:参数不合法,OTHER:子帐号信息不全,OK:分享成功
     */
    @PackageIsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/cooperateSim", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Message cooperateSim(@RequestBody Map<String, Object> map) {
        Message message = MessageService.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if (map.get("projectIds") == null || !StringUtils.hasText(map.get("projectIds").toString())
                || map.get("type") == null || !StringUtils.hasText(map.get("type").toString())
                || map.get("accountId") == null || !StringUtils.hasText(map.get("accountId").toString())) {
            return MessageService.message(Message.Type.FAIL);
        }
        String type = map.get("type").toString();
        Long accountId = Long.valueOf(map.get("accountId").toString());
        List<Integer> projectIds = (List<Integer>) map.get("projectIds");
        Account account = accountService.find(accountId);
        if (account == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (account.getName() == null) {
            return MessageService.message(Message.Type.ACCOUNT_NOINFO);
        }
        User own = authentService.getUserFromSubject();
        Project project;
        for (int i = 0; i < projectIds.size(); i++) {
            project = projectService.find((long) (projectIds.get(i)));
            if (project == null) {
                return MessageService.message(Message.Type.DATA_NOEXIST);
            }
            if (!project.getUser().getId().equals(own.getId())) {
                return MessageService.message(Message.Type.DATA_REFUSE);
            }
        }
        if (!cooperateService.isOwn(own, account)) {
            return MessageService.message(Message.Type.FAIL);
        }
        boolean cooperate = cooperateService.cooperate(projectIds, type, account);
        if (cooperate) {
            return MessageService.message(Message.Type.OK);
        }else{
            return MessageService.message(Message.Type.FAIL);
        }
    }

    /**
     * 注销子账号编辑权限
     *
     * @param map 包含项目标识projectId,以及权限类型type
     * @return FAIL:参数错误,OK:注销成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/unCooperate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Message unCooperate(@RequestBody Map<String, Object> map) {
        Message message = MessageService.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if (map.get("projectId") == null || !StringUtils.hasText(map.get("projectId").toString())
                || map.get("types") == null || !StringUtils.hasText(map.get("types").toString())) {
            return MessageService.message(Message.Type.FAIL);
        }
        Long projectId = Long.valueOf(map.get("projectId").toString());
        Project project = projectService.find(projectId);
        String typeStr = map.get("types").toString();
        List<String> types = Arrays.asList(typeStr.split(","));
        User own = authentService.getUserFromSubject();
        return cooperateService.unCooperate(project, types, own);
    }

    /**
     * 判断当前访问对象是否对项目有编辑或查看的权限
     *
     * @param user    企业或用户
     * @param account 子帐号
     * @param project 项目
     * @return false无权限, true有权限
     */
    private boolean isOperate(User user, Account account, Project project) {
        if (user != null) {
            if (shareService.isShare(project, user) == false) {
                return false;
            }
        }
        //看看是否有查看的权限
        if (account != null) {
            if (account != null) {
                //当前用户是否对此项目有查看权限
                if (!cooperateService.isCooperate(project, account)) {
                    return false;
                }
            }
        }
        return true;
    }

    @RequestMapping(value = "/infos1", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    public @ResponseBody
    Message getInfos1() {
        return MessageService.message(Message.Type.OK, infoService.getInfos());
    }

    /**
     * 记录上传文件大小
     *
     * @param map 包含项目标识projectId,fileSize:文件大小,fileNames:文件名,ailas:单元别名
     * @return message消息体, FAIL:记录失败;OK:记录成功
     */
    @PackageIsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/reportUploadFileInfo", method = RequestMethod.POST)
    public @ResponseBody
    Message uploadFileSize(@RequestBody Map<String, Object> map) {
        Message message = MessageService.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object o = authentService.getUserFromSubject();
        if (o == null) {
            o = authentService.getAccountFromSubject();
        }
        return projectService.uploadFileSize(map, o);
    }

    /**
     * 记录下载文件大小
     *
     * @param map 包含项目标识projectId,fileSize:文件大小,fileNames:文件名,ailas:单元别名
     * @return message消息体, FAIL:记录失败;OK:记录成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/reportDownloadFileInfo", method = RequestMethod.POST)
    public @ResponseBody
    Message downloadFileSize(@RequestBody Map<String, Object> map) {
        Message message = MessageService.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object o = authentService.getUserFromSubject();
        if (o == null) {
            o = authentService.getAccountFromSubject();
        }
        return projectService.downloadFileSize(map, o);
    }

    /**
     * 记录删除文件大小
     *
     * @param map 包含项目标识projectId,fileSize:文件大小,fileNames:文件名,ailas:单元别名
     * @return message消息体, FAIL:记录失败;OK:记录成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteFileSize", method = RequestMethod.POST)
    public @ResponseBody
    Message deleteFileSize(@RequestBody Map<String, Object> map) {
        Message message = MessageService.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        return projectService.deleteFileSize(map, user);
    }

    /**
     * 是否允许上传
     *
     * @param projectId 项目标识
     * @return message消息体, NO_ALLOW:不允许上传;OK:允许上传
     */
    @PackageIsExpire(value = "property")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/isAllowUpload", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Message isAllowUpload(@RequestParam long projectId) {
        Message message = MessageService.parametersCheck(projectId);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (user == null) {
            Project project = projectService.find(projectId);
            user = project.getUser();
        }
        return projectService.isAllowUpload(user);
    }

    /**
     * 是否允许下载
     *
     * @param projectId 项目标识
     * @return message消息体, NO_ALLOW:不允许下载;OK:允许下载
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/isAllowDownload", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Message isAllowDownload(@RequestParam long projectId) {
        Message message = MessageService.parametersCheck(projectId);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (user == null) {
            Project project = projectService.find(projectId);
            user = project.getUser();
        }
        return projectService.isAllowDownload(user);
    }

    /**
     * 是否允许BIM
     *
     * @param map 包含projectId
     * @return message消息体, NO_ALLOW:不允许BIM;OK:允许BIM
     */
    @PackageIsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple", "account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/isAllowBim", method = RequestMethod.GET)
    public @ResponseBody
    Message isAllowBim(@RequestBody Map<String, Object> map) {
        Message message = MessageService.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (user == null) {
            Object projectId = map.get("projectId");
            if (projectId == null) {
                return MessageService.message(Message.Type.DATA_NOEXIST);
            }
            Project project = projectService.find(Long.valueOf(projectId.toString()));
            user = project.getUser();
        }
        return projectService.isAllowBim(user);
    }

    /**
     * 项目图标类型定制
     *
     * @param map <br/>
     *            <ol>
     *            <li>id:项目id</li>
     *            <li>iconType:图标类型</li>
     *            </ol>
     * @return <br/>
     * <ol>
     * <li>FAIL:参数验证失败</li>
     * <li>EXIST:实体对象不存在</li>
     * <li>OK:设置成功</li>
     * </ol>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/iconType/update", method = RequestMethod.POST)
    public @ResponseBody
    Message iconTypeUpdate(@RequestBody Map<String, Object> map) {
        Message message = MessageService.parameterCheck(map);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        return projectService.iconTypeUpdate(user, map);
    }

    /**
     * 日志-->最近一周内
     *
     * @return <br/>
     * <ol>
     * <li>FAIL:参数验证失败</li>
     * <li>OK:拉取成功</li>
     * </ol>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/elementLog/week/{projectId}", method = RequestMethod.GET)
    public @ResponseBody
    Message elementLogWeek(@PathVariable Long projectId) {
        List<ProjectLog> projectLogs;
        try {
            projectLogs = projectLogService.findByProjectIdAndWeek(projectId);
        } catch (Exception e) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        JSONArray jsonArray = projectLogService.projectLogsToJson(projectLogs);
        return MessageService.message(Message.Type.OK, jsonArray);
    }

    /**
     * 日志-->最近一月内
     *
     * @return <br/>
     * <ol>
     * <li>FAIL:参数验证失败</li>
     * <li>OK:拉取成功</li>
     * </ol>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/elementLog/month/{projectId}", method = RequestMethod.GET)
    public @ResponseBody
    Message elementLogMonth(@PathVariable Long projectId) {
        List<ProjectLog> projectLogs;
        try {
            projectLogs = projectLogService.findByProjectIdAndMonth(projectId);
        } catch (Exception e) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        JSONArray jsonArray = projectLogService.projectLogsToJson(projectLogs);
        return MessageService.message(Message.Type.OK, jsonArray);
    }

    /**
     * 日志-->最近三月内
     *
     * @param projectId 项目id
     * @return <br/>
     * <ol>
     * <li>FAIL:参数验证失败</li>
     * <li>OK:拉取成功</li>
     * </ol>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/elementLog/threeMonth/{projectId}", method = RequestMethod.GET)
    public @ResponseBody
    Message elementLogThreeMonth(@PathVariable Long projectId) {
        List<ProjectLog> projectLogs;
        try {
            projectLogs = projectLogService.findByProjectIdAndThreeMonth(projectId);
        } catch (Exception e) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        JSONArray jsonArray = projectLogService.projectLogsToJson(projectLogs);
        return MessageService.message(Message.Type.OK, jsonArray);
    }

    /**
     * 日志-->最近一年内
     *
     * @param projectId 项目id
     * @return <br/>
     * <ol>
     * <li>FAIL:参数验证失败</li>
     * <li>OK:拉取成功</li>
     * </ol>
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/elementLog/year/{projectId}", method = RequestMethod.GET)
    public @ResponseBody
    Message elementLogYear(@PathVariable Long projectId) {
        List<ProjectLog> projectLogs;
        try {
            projectLogs = projectLogService.findByProjectIdAndYear(projectId);
        } catch (Exception e) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        JSONArray jsonArray = projectLogService.projectLogsToJson(projectLogs);
        return MessageService.message(Message.Type.OK, jsonArray);
    }

}
