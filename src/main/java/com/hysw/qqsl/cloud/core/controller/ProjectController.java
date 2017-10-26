package com.hysw.qqsl.cloud.core.controller;

import java.util.*;

import com.hysw.qqsl.cloud.annotation.util.*;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.service.*;
import com.hysw.qqsl.cloud.util.SettingUtils;
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

    /**
     * 取得用户对应的项目列表
     *
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/lists", method = RequestMethod.GET)
    public @ResponseBody Message getProjects(@RequestParam int start) {
        Message message;
        User user = authentService.getUserFromSubject();
        if(user !=null){
            user = userService.getSimpleUser(user);
            message = projectService.getProjects(start,user);
            return message;
        }
        Account account = authentService.getAccountFromSubject();
        if(account!=null){
            account = accountService.getSimpleAccount(account);
            message = projectService.getAccountProjects(start,account);
            return message;
        }
        return new Message(Message.Type.UNKNOWN);
    }


    /**
     * 保存新建的项目
     *
     * @param objectMap
     * @return
     */
    @IsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public
    @ResponseBody
    Message createProject(
            @RequestBody Map<String, String> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        Map<String,Object> map= (Map<String,Object>)message.getData();
        User user = authentService.getUserFromSubject();
        //是否可以创建项目
        message=projectService.isAllowCreateProject(user);
        if(message.getType()==Message.Type.NO_ALLOW){
            return message;
        }
        try {
            Project project = projectService.convertMap(map,user,false);
            message = projectService.createProject(project);
        } catch (QQSLException e) {
            e.printStackTrace();
            return new Message(Message.Type.FAIL,e.getMessage());
        } catch (Exception e1){
            e1.printStackTrace();
            return new Message(Message.Type.FAIL);
        }
        return message;
    }

    /**
     * 获取项目类型
     *
     * @return
     */
    @RequiresAuthentication
    @RequestMapping(value = "/typeSelects", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getSelects() {
        List<Info> infos = infoService.getInfos();
        return new Message(Message.Type.OK, infos.get(7));
    }

    /**
     * 根据项目id得到单元树形结构json
     *
     * @param id 项目id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/tree/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getTreeJsons(
            @PathVariable("id") Long id) {
        Message message = Message.parametersCheck(id);
        if(message.getType()== Message.Type.FAIL){
            return message;
        }
        Project project = projectService.find(id);
        if (project == null) {
            return new Message(Message.Type.EXIST);
        }
        User user = authentService.getUserFromSubject();
        Account account = authentService.getAccountFromSubject();
        if(!isOperate(user,account,project)){
            return new Message(Message.Type.NO_AUTHORIZE);
        }
        List<Unit> units = projectService.buildTemplate(project);
        return new Message(Message.Type.OK, objectJsonConvertUtils.getJsons(project,units));
    }

    /**
     * 修改新建的项目
     *
     * @return
     */
    @IsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public @ResponseBody Message updateProject(@RequestBody Map<String, String> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        Map<String,Object> map = (Map<String,Object>)message.getData();
        User user = authentService.getUserFromSubject();
        try {
            Project newProject = projectService.convertMap(map,user,true);
            message = projectService.updateProject(newProject);
        } catch (QQSLException e) {
            e.printStackTrace();
            return new Message(Message.Type.FAIL);
        }
        return message;
    }

    /**
     * 根据项目编号删除项目
     *
     * @param id
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/removeProject/{id}", method = RequestMethod.DELETE)
    public
    @ResponseBody
    Message removeProjectByCode(
            @PathVariable("id") Long id) {
        Message message = Message.parametersCheck(id);
        if(message.getType()== Message.Type.FAIL){
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
    @RequiresAuthentication
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
            return new Message(Message.Type.OK);
        }
        return new Message(Message.Type.FAIL);
    }

    /**
     * 查询项目信息
     *
     * @return
     */
//    @RequiresAuthentication
    @RequestMapping(value = "/infos", method = RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Message getInfos() {
        return new Message(Message.Type.OK, infoService.getInfos());
    }

    /**
     * 根据项目type得到项目模版单元树形结构json
     *
     * @param type 项目type
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/template/{type}", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getTemplateJsons(
            @PathVariable("type") String type) {
        Message message = Message.parametersCheck(type);
        if(message.getType()== Message.Type.FAIL){
            return message;
        }
        Project project = new Project();
        project.setType(Project.Type.valueOf(type));
        List<JsonTree> jsonTrees = objectJsonConvertUtils.getTemplateJsonTree(project);
        return new Message(Message.Type.OK, jsonTrees);
    }

    /**
     * 根据项目id，单元别名，得到单元
     * @param id
     * @param alias
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/unit", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getUnit(@RequestParam long id,
                    @RequestParam String alias) {
        Message message = Message.parametersCheck(id,alias);
        if(message.getType()== Message.Type.FAIL){
            return message;
        }
        User user = authentService.getUserFromSubject();
        Account account= authentService.getAccountFromSubject();
        Project project = projectService.find(id);
        Unit unit = projectService.buildUnitByUnitAlias(project, alias);
        if (unit == null) {
            return new Message(Message.Type.FAIL);
        }
        if(!isOperate(user,account,project)){
            return new Message(Message.Type.NO_AUTHORIZE);
        }
        projectService.buildUnitAction(project, unit,user,account);
        if(user!=null){
            for (int i = 0; i < unit.getElementGroups().size(); i++) {
                contactService.findPhase(unit, unit.getElementGroups().get(i));
            }
        }
        //构建单元json串
        JSONObject unitJson = unitService.makeUnitJson(unit);
        return new Message(Message.Type.OK, unitJson);
    }

    /**
     * 取得要素输出界面需要的要素值
     *
     * @return Message
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/values", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getValues(@RequestParam String alias,
                      @RequestParam String projectType) {
        Message message = Message.parametersCheck(alias,projectType);
        if(message.getType()== Message.Type.FAIL){
            return message;
        }
        List<JsonUnit> jsonUnits = new ArrayList<JsonUnit>();
        User user = authentService.getUserFromSubject();
        Account account = authentService.getAccountFromSubject();
        // 取得项目id列表
        if (projectType.length() == 0 || alias.equals(CommonAttributes.TOP_TREE_ID)) {
            return new Message(Message.Type.OK, jsonUnits);
        }
        Project.Type type = Project.Type.valueOf(projectType);
        if(user!=null){
            jsonUnits =  projectService.getExportValues(user,type,jsonUnits,alias);
        }
        if(account!=null){
            jsonUnits =  projectService.getExportValues(account,type,jsonUnits,alias);
        }
        return new Message(Message.Type.OK, jsonUnits);
    }


    /**
     * 根据项目id得到项目包括项目信息和项目简介以及短信
     *
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/project/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getProject(@PathVariable("id") Long id) {
        Message message = Message.parametersCheck(id);
        if(message.getType()== Message.Type.FAIL){
            return message;
        }
        User user = authentService.getUserFromSubject();
        Account account = authentService.getAccountFromSubject();
        Project project = projectService.find(id);
        if(!isOperate(user,account,project)){
            return new Message(Message.Type.NO_AUTHORIZE);
        }
        message = projectService.getProjectBySubject(id,user==null?account:user);
        return message;
    }

    /**
     * 根据项目id得到项目及项目简介信息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/projectIntroduce/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getProjectIntroduce(@PathVariable("id") Long id) {
        Message message = Message.parametersCheck(id);
        if(message.getType()== Message.Type.FAIL){
            return message;
        }
        Project project = projectService.find(id);
        if (project == null) {
            return new Message(Message.Type.EXIST);
        }
        JSONObject projectJson = projectService.makeProjectJson(project);
        return new Message(Message.Type.OK, projectJson);
    }

    /**
     * 向项目的业主和负责人发送短信
     *
     * @return
     */
    @IsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
    public
    @ResponseBody
    Message sendMessage(
            @RequestBody Map<String, String> objectMap) {
        Message message = Message.parameterCheck(objectMap);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        Map<String,String> realMap = (Map<String,String>)message.getData();
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


    /**
     * 保存文件日志
     * @param
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/fileLog", method = RequestMethod.POST)
    public
    @ResponseBody
    Message fileLog(@RequestBody  Object object) {
        Message message = Message.parameterCheck(object);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        Map<String,Object> map = (Map<String,Object>)message.getData();
        User user = authentService.getUserFromSubject();
        User simpleUser = userService.getSimpleUser(user);
        message = elementService.saveFileLog(simpleUser,map);
        return message;
    }
    /**
     * 根据请求的项目返回相应类型的外业测量要素
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/uploadModel", method = RequestMethod.GET)
    public @ResponseBody Message uploadModel(@RequestParam long id,@RequestParam String alias) {
        Message message = Message.parametersCheck(id,alias);
        if(message.getType()== Message.Type.FAIL){
            return message;
        }
        Project project=projectService.find(id);
        Unit unit = projectService.buildUnitByUnitAlias(project, alias);
        return new Message(Message.Type.OK,unit);
    }

    /**
     * 添加全景地图连接
     * @param object
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/editPanoramicUrl", method = RequestMethod.POST)
    public @ResponseBody Message addPanoramicUrl(@RequestBody  Object object ){
        Map<String,Object> map = (Map<String,Object>)object;
        long id = Long.valueOf(map.get("id").toString());
        Project project = projectService.find(id);
        Object url = map.get("url");
        if(url!=null&&StringUtils.hasText(url.toString())){
            if(!SettingUtils.httpUrlRegex(url.toString())){
                return new Message(Message.Type.UNKNOWN);
            }
        }
        projectService.editPanoramicUrl(url,project);
        return new Message(Message.Type.OK);
    }


    /**
     * 企业间分享项目
     * @param map
     * @return
     */
    @IsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/share", method = RequestMethod.POST, produces= MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Message share(@RequestBody Map<String,Object> map) {
        Message message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        List<Integer> projectIds = (List<Integer>) map.get("projectIds");
        List<Integer> userIds = (List<Integer>)map.get("userIds");
        User own = authentService.getUserFromSubject();
        return shareService.shares(projectIds,userIds,own);
    }
    /**
     * 取消企业间分享项目
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/unShare", method = RequestMethod.POST, produces= MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Message unShare(@RequestBody Map<String,Object> map) {
        Message message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        Long projectId = Long.valueOf(map.get("projectId").toString());
        Project project = projectService.find(projectId);
        if(project==null){
            return new Message(Message.Type.UNKNOWN);
        }
        List<Integer> userIds = (List<Integer>) map.get("userIds");
        User own = authentService.getUserFromSubject();
        if(!project.getUser().getId().equals(own.getId())){
            new Message(Message.Type.FAIL);
        }
        shareService.unShares(project,userIds,own);
        return new Message(Message.Type.OK);
    }

    /**
     * 企业账号查看子账号协同情况
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/viewCooperate", method = RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Message viewCooperate(@RequestParam long accountId) {
        Message  message = Message.parametersCheck(accountId);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        Account account = accountService.find(accountId);
        User user = authentService.getUserFromSubject();
        if(account==null){
            return new Message(Message.Type.UNKNOWN);
        }
        return cooperateService.viewCooperate(user,account);
    }
    /**
     * 企业账号取消子账号查看权限
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/unView", method = RequestMethod.POST, produces= MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Message unViews(@RequestBody Map<String,Object> map) {
        Message  message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        Long projectId = Long.valueOf(map.get("projectId").toString());
        List<Integer> accountIds = (List<Integer>) map.get("accountIds");
        Project project = projectService.find(projectId);
        User own = authentService.getUserFromSubject();
        if(project==null||!project.getUser().getId().equals(own.getId())){
            return new Message(Message.Type.UNKNOWN);
        }
        cooperateService.unViews(project,accountIds,own);
        return new Message(Message.Type.OK);
    }

    /**
     * 将一个项目的多个权限分享给一个子账号
     * @param map
     * @return
     */
    @IsExpire

    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/cooperateMul", method = RequestMethod.POST, produces= MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Message cooperateMult(@RequestBody Map<String,Object> map) {
        Message  message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        if(map.get("projectId")==null||!StringUtils.hasText(map.get("projectId").toString())){
            new Message(Message.Type.FAIL);
        }
        if(map.get("types")==null||!StringUtils.hasText(map.get("types").toString())){
            new Message(Message.Type.FAIL);
        }
        if(map.get("accountId")==null||!StringUtils.hasText(map.get("accountId").toString())){
            new Message(Message.Type.FAIL);
        }
        Long projectId = Long.valueOf(map.get("projectId").toString());
        Long accountId = Long.valueOf(map.get("accountId").toString());
        String typeStr = map.get("types").toString();
        Account account = accountService.find(accountId);
        Project project = projectService.find(projectId);
        //判断子账号,项目归属
        User own = authentService.getUserFromSubject();
        return  cooperateService.cooperateMult(project,typeStr,account,own);

    }

    /**
     * 将多个项目的一个权限分享给一个子账号
     * @param map
     * @return
     */
    @IsExpire
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/cooperateSim", method = RequestMethod.POST, produces= MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Message cooperateSim(@RequestBody Map<String,Object> map) {
        Message  message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        if(map.get("projectIds")==null||!StringUtils.hasText(map.get("projectIds").toString())){
            new Message(Message.Type.FAIL);
        }
        if(map.get("type")==null||!StringUtils.hasText(map.get("type").toString())){
            new Message(Message.Type.FAIL);
        }
        if(map.get("accountId")==null||!StringUtils.hasText(map.get("accountId").toString())){
            new Message(Message.Type.FAIL);
        }
        String type = map.get("type").toString();
        Long accountId = Long.valueOf(map.get("accountId").toString());
        List<Integer> projectIds = (List<Integer>) map.get("projectIds");
        Account account = accountService.find(accountId);
        if(account==null){
            new Message(Message.Type.FAIL);
        }
        if(account.getName()==null){
            new Message(Message.Type.OTHER);
        }
        User own = authentService.getUserFromSubject();
        return  cooperateService.cooperate(projectIds,type,account,own);
    }

    /**
     * 注销子账号编辑权限
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/unCooperate", method = RequestMethod.POST, produces= MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Message unCooperate(@RequestBody Map<String,Object> map) {
        Message  message = Message.parameterCheck(map);
        if(message.getType().equals(Message.Type.FAIL)){
            return message;
        }
        if(map.get("projectId")==null||!StringUtils.hasText(map.get("projectId").toString())){
            new Message(Message.Type.FAIL);
        }
        if(map.get("types")==null||!StringUtils.hasText(map.get("types").toString())){
            new Message(Message.Type.FAIL);
        }
        Long projectId = Long.valueOf(map.get("projectId").toString());
        Project project = projectService.find(projectId);
        String typeStr = map.get("types").toString();
        List<String> types = Arrays.asList(typeStr.split(","));
        User own = authentService.getUserFromSubject();
        return cooperateService.unCooperate(project,types,own);
    }

    /**
     * 判断当前访问对象是否对项目有编辑或查看的权限
     * @param user
     * @param account
     * @param project
     * @return
     */
    private boolean isOperate(User user,Account account,Project project){
        if(user!=null){
            if(shareService.isShare(project,user)==false){
                return false;
            }
        }
        //看看是否有查看的权限
        if(account!=null){
            if(account!=null){
                //当前用户是否对此项目有查看权限
                if(!cooperateService.isCooperate(project,account)){
                    return false;
                }
            }
        }
        return true;
    }

    @RequestMapping(value = "/infos1", method = RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    public @ResponseBody Message getInfos1() {
        return new Message(Message.Type.OK, infoService.getInfos());
    }

    /**
     * 上传文件大小
     * @param map
     * @return
     */
    @IsExpire
    @RequestMapping(value = "/reportUploadFileInfo", method = RequestMethod.POST)
    public @ResponseBody Message uploadFileSize(@RequestBody Map<String, Object> map) {
        Message message = Message.parameterCheck(map);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        return projectService.uploadFileSize(map,user);
    }

    /**
     * 下载文件大小
     * @param map
     * @return
     */
    @RequestMapping(value = "/reportDownloadFileInfo", method = RequestMethod.POST)
    public @ResponseBody Message downloadFileSize(@RequestBody Map<String, Object> map) {
        Message message = Message.parameterCheck(map);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        return projectService.downloadFileSize(map,user);
    }

    /**
     * 删除文件大小
     * @param map
     * @return
     */
    @RequestMapping(value = "/deleteFileSize", method = RequestMethod.POST)
    public @ResponseBody Message deleteFileSize(@RequestBody Map<String, Object> map) {
        Message message = Message.parameterCheck(map);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        return projectService.deleteFileSize(map,user);
    }

    /**
     * 是否允许上传
     * @return
     */
    @IsExpire
    @RequestMapping(value = "/isAllowUpload", method = RequestMethod.GET)
    public @ResponseBody Message isAllowUpload() {
        User user = authentService.getUserFromSubject();
        return projectService.isAllowUpload(user);
    }

    /**
     * 是否允许下载
     * @return
     */
    @RequestMapping(value = "/isAllowDownload", method = RequestMethod.GET)
    public @ResponseBody Message isAllowDownload() {
        User user = authentService.getUserFromSubject();
        return projectService.isAllowDownload(user);
    }
    /**
     * 是否允许下载
     * @return
     */
    @IsExpire
    @RequestMapping(value = "/isAllowBim", method = RequestMethod.GET)
    public @ResponseBody Message isAllowBim() {
        User user = authentService.getUserFromSubject();
        return projectService.isAllowBim(user);
    }

    /**
     * 项目图标类型定制
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/iconType/update", method = RequestMethod.POST)
    public @ResponseBody Message iconTypeUpdate(@RequestBody Map<String, Object> map) {
        Message message = Message.parameterCheck(map);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        return projectService.iconTypeUpdate(user,map);
    }

//    ?创建子账户限制条件(是否允许创建)
//    ?每年最后一天套餐流量统计归0  管理员手动初始化套餐流量
//    ?用户一旦创建就永久拥有测试版套餐
//    /用户创建项目需要查看空间是否满足要求
//    ?用户登录以后的各个操作首先判断套餐或测站是否过期aop
//    /订单状态检查线程
//    ?退款-先修改订单状态，再写入流水，最后发送退款请求
//    /如果发生支付或者退款成功，但是服务或者退款不到账情况，需联系客服
//    ?空间大小是否允许创建项目   删除项目 释放空间大小
//    /坐标文件大小应记录在套餐使用空间中（外业与内业）
//    ?master 增加仪表输出 方便寻找bug
//    ?测试版套餐不可购买，套餐购买，需检查过期时间，过期后才能再次购买  测试版不可续费
//    ?测试用例为所有用户增加测试版套餐
//    千寻账号限制账户获取数量
   }
