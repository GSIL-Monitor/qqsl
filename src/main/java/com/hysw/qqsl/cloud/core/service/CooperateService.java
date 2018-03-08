package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.entity.element.ElementGroup;
import com.hysw.qqsl.cloud.core.entity.project.Cooperate;
import com.hysw.qqsl.cloud.core.entity.project.CooperateVisit;
import com.hysw.qqsl.cloud.core.entity.project.Stage;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Create by leinuo on 17-5-17 下午2:43
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 * 子账号间项目协同工作service层
 */
@Service("cooperateService")
public class CooperateService {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private AccountMessageService accountMessageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;

    /**
     * 企业账号取消子账号查看权限
     *
     * @param project
     * @param accountIds
     */
    public void unViews(Project project, List<Integer> accountIds, User own) {
        Long accId;
        Account account;
        for (int i = 0; i < accountIds.size(); i++) {
            accId = Long.valueOf((long) (accountIds.get(i)));
            account = accountService.find(accId);
            if (account == null || !project.getUser().getId().equals(own.getId())) {
                continue;
            }
            unRegistView(project, account);
        }
    }

    /**
     * 注销查看权限
     *
     * @param project
     * @param account
     */
    private void unRegistView(Project project, Account account) {
        Cooperate cooperate = new Cooperate(project);
        makeCooperateView(cooperate);
        cooperate.unRegister(account);
        project.setViews(cooperate.toViewJson().isEmpty() ? null : cooperate.toViewJson().toString());
        projectService.save(project);
        //记录消息
        accountMessageService.viewMessage(project, account, false);
    }

    /**
     * 构建coorperate
     *
     * @param project
     * @return
     */
    public Cooperate makeCooperate(Project project) {
        Cooperate cooperate = new Cooperate(project);
        makeCooperateView(cooperate);
        makeCooperateEdit(cooperate);
        return cooperate;
    }

    /**
     * 构建子账号查看权限
     *
     * @param cooperate
     */
    private void makeCooperateView(Cooperate cooperate) {
        if (!StringUtils.hasText(cooperate.getProject().getViews())) {
            return;
        }
        JSONArray viewJsons = JSONArray.fromObject(cooperate.getProject().getViews());
        JSONObject viewJson;
        Account account;
        CooperateVisit cooperateVisit;
        List<CooperateVisit> cooperateVisits = new ArrayList<>();
        for (int i = 0; i < viewJsons.size(); i++) {
            viewJson = (JSONObject) viewJsons.get(i);
            account = new Account();
            account.setId(viewJson.getLong("id"));
            account.setName(viewJson.get("name") == null ? null : viewJson.get("name").toString());
            account.setPhone(viewJson.getString("phone"));
            cooperate.register(account);
            cooperateVisit = new CooperateVisit(account);
            cooperateVisit.setCreateTime(new Date(viewJson.getLong("createTime")));
            cooperateVisit.setModifyTime(viewJson.get("modifyTime") == null ? null : new Date(viewJson.getLong("modifyTime")));
            cooperateVisits.add(cooperateVisit);
        }
        cooperate.setVisits(cooperateVisits);
    }

    /**
     * 构建子账号编辑权限
     *
     * @param cooperate
     */
    private void makeCooperateEdit(Cooperate cooperate) {
        if (!StringUtils.hasText(cooperate.getProject().getCooperate())) {
            return;
        }
        if (cooperate.getProject().getCooperate().equals("[]")) {
            System.out.print(cooperate.getProject().getCooperate() + " : " + cooperate.getProject().getId());
            cooperate.getProject().setCooperate(null);
            projectService.save(cooperate.getProject());
            return;
        }
        JSONObject cooperateJson = JSONObject.fromObject(cooperate.getProject().getCooperate());
        JSONObject invite = cooperateJson.getJSONObject("invite");
        makeCooperateVisit(cooperate.getInvite(), invite);
        JSONObject preparation = cooperateJson.getJSONObject("preparation");
        makeCooperateVisit(cooperate.getPreparation(), preparation);
        JSONObject building = cooperateJson.getJSONObject("building");
        makeCooperateVisit(cooperate.getBuilding(), building);
        JSONObject maintenance = cooperateJson.getJSONObject("maintenance");
        makeCooperateVisit(cooperate.getMaintenance(), maintenance);
    }

    /**
     * 构建各个阶段的权限
     *
     * @param stage
     * @param stageJson
     */
    private void makeCooperateVisit(Stage stage, JSONObject stageJson) {
        if (stageJson.isEmpty()) {
            return;
        }
        JSONObject elementJson = stageJson.getJSONObject("element");
        JSONObject fileJson = stageJson.getJSONObject("file");
        Account account;
        Date modifyTime;
        if (!elementJson.isEmpty()) {
            account = new Account();
            account.setId(elementJson.getLong("id"));
            account.setName(elementJson.getString("name"));
            account.setPhone(elementJson.getString("phone"));
            stage.registerElement(account);
            stage.getElementVisit().setCreateTime(new Date(elementJson.getLong("createTime")));
            modifyTime = elementJson.get("modifyTime") == null ? null : new Date(elementJson.getLong("modifyTime"));
            stage.getElementVisit().setModifyTime(modifyTime);
        }
        if (!fileJson.isEmpty()) {
            account = new Account();
            account.setId(fileJson.getLong("id"));
            account.setName(fileJson.getString("name"));
            account.setPhone(fileJson.getString("phone"));
            stage.registerFile(account);
            stage.getFileVisit().setCreateTime(new Date(fileJson.getLong("createTime")));
            modifyTime = fileJson.get("modifyTime") == null ? null : new Date(fileJson.getLong("modifyTime"));
            stage.getFileVisit().setModifyTime(modifyTime);
        }
    }

    /**
     * 将多个项目的一个权限分享给一个子账号
     *
     * @param ids
     * @param type
     * @param account
     */
    public Message cooperate(List<Integer> ids, String type, Account account,User own) {
        Project project;
        Cooperate cooperate;
        boolean flag;
        Message message = isOwn(own,ids,account);
        if(message.getType()!=Message.Type.OK){
            return message;
        }
        CooperateVisit.Type type1 = CooperateVisit.Type.valueOf(type);
        for (int i = 0; i < ids.size(); i++) {
            project = projectService.find((long) (ids.get(i)));
            cooperate = makeCooperate(project);
            flag = regist(cooperate, account, type1);
            //权限注册成功，记录注册消息
            if (flag) {
                saveCooperate(cooperate);
                if (type1.equals(CooperateVisit.Type.VISIT_VIEW.toString())) {
                    accountMessageService.viewMessage(project, account, true);
                } else {
                    accountMessageService.cooperate(type1, project, account, true);
                }
            }
        }
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 判断项目及子账号归属
     * @param own
     * @param ids
     * @param account
     * @return
     */
    private Message isOwn(User own, List<Integer> ids, Account account) {
        Project project;
        Message message;
        for (int i = 0; i < ids.size(); i++) {
            project = projectService.find((long) (ids.get(i)));
            message = userService.isOwn(own,project,account);
            if (message.getType() != Message.Type.OK) {
                return MessageService.message(Message.Type.FAIL);
            }
        }
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 企业账号查看子账号协同情况
     *
     * @param account
     * @return
     */
    public Message viewCooperate(User user, Account account) {
        List<Project> projects = projectService.findByUser(user);
        Cooperate cooperate;
        List<String> types;
        JSONArray resultJson = new JSONArray();
        JSONObject jsonObject;
        Project project;
        for (int i = 0; i < projects.size(); i++) {
            project = projects.get(i);
            if (isCooperate(project, account)) {
                cooperate = makeCooperate(project);
                types = getEditTypes(cooperate, account);
                if (isView(cooperate, account)) {
                    types.add("VISIT_VIEW");
                }
                jsonObject = new JSONObject();
                jsonObject.put("projectId", project.getId());
                jsonObject.put("projectName", project.getName());
                jsonObject.put("projectCode", project.getCode());
                jsonObject.put("cooperate", types);
                resultJson.add(jsonObject);
            }
        }
        return MessageService.message(Message.Type.OK, resultJson);
    }

    /**
     * 权限注册
     *
     * @param cooperate
     * @param account
     * @param type
     * @return
     */
    private boolean regist(Cooperate cooperate, Account account, CooperateVisit.Type type) {
        boolean flag;
        switch (type) {
            case VISIT_INVITE_ELEMENT:
                flag = cooperate.registerInviteElement(account);
                break;
            case VISIT_INVITE_FILE:
                flag = cooperate.registerInviteFile(account);
                break;
            case VISIT_PREPARATION_ELEMENT:
                flag = cooperate.registerPreparationElement(account);
                break;
            case VISIT_PREPARATION_FILE:
                flag = cooperate.registerPreparationFile(account);
                break;
            case VISIT_BUILDING_ELEMENT:
                flag = cooperate.registerBuildingElement(account);
                break;
            case VISIT_BUILDING_FILE:
                flag = cooperate.registerBuildingFile(account);
                break;
            case VISIT_MAINTENANCE_ELEMENT:
                flag = cooperate.registerMaintenanceElement(account);
                break;
            case VISIT_MAINTENANCE_FILE:
                flag = cooperate.registerMaintenanceFile(account);
                break;
            case VISIT_VIEW:
                cooperate.register(account);
                flag = true;
                break;
            default:
                flag = false;
        }
        return flag;
    }

    /**
     * 将一个项目的多个权限分享给一个子账号
     *
     * @param project
     * @param typeStr
     * @param account
     */
    public Message cooperateMult(Project project, String typeStr, Account account, User own) {
        Message message = userService.isOwn(own,project,account);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Cooperate cooperate = makeCooperate(project);
        boolean flag;
        List<String> types = Arrays.asList(typeStr.split(","));
        for (int i = 0; i < types.size(); i++) {
            flag = regist(cooperate, account, CooperateVisit.Type.valueOf(types.get(i)));
            if (flag) {
                if (types.get(i).equals(CooperateVisit.Type.VISIT_VIEW.toString())) {
                    accountMessageService.viewMessage(project, account, true);
                } else {
                    accountMessageService.cooperate(CooperateVisit.Type.valueOf(types.get(i)), project, account, true);
                }
            }
        }
        saveCooperate(cooperate);
       return MessageService.message(Message.Type.OK);
    }


    /**
     * 注销子账号编辑权限
     *
     * @param project
     * @param typeList
     */
    public Message unCooperate(Project project, List<String> typeList,User own) {
        if(!project.getUser().getId().equals(own.getId())){
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        Cooperate cooperate = makeCooperate(project);
        List<String> commTypes = Arrays.asList(CommonAttributes.COOPERATES);
        CooperateVisit.Type type;
        Account account;
        for (int i = 0; i < typeList.size(); i++) {
            if (!commTypes.contains(typeList.get(i))) {
                continue;
            }
            type = CooperateVisit.Type.valueOf(typeList.get(i));
            account = unRegist(cooperate, type);
            if (account == null) {
                continue;
            }
            //记录子账号编辑权限注销的消息
            accountMessageService.cooperate(type, project, account, false);
        }
        saveCooperate(cooperate);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 注销子账号编辑权限
     *
     * @param cooperate
     * @param type
     */
    private Account unRegist(Cooperate cooperate, CooperateVisit.Type type) {
        Account account;
        switch (type) {
            case VISIT_INVITE_ELEMENT:
                account = cooperate.getInviteElementAccount();
                cooperate.unRegisterInviteElement();
                break;
            case VISIT_INVITE_FILE:
                account = cooperate.getInviteFileAccount();
                cooperate.unRegisterInviteFile();
                break;
            case VISIT_PREPARATION_ELEMENT:
                account = cooperate.getPreparationElementAccount();
                cooperate.unRegisterPreparationElement();
                break;
            case VISIT_PREPARATION_FILE:
                account = cooperate.getPreparationFileAccount();
                cooperate.unRegisterPreparationFile();
                break;
            case VISIT_BUILDING_ELEMENT:
                account = cooperate.getBuildingElementAccount();
                cooperate.unRegisterBuildingElement();
                break;
            case VISIT_BUILDING_FILE:
                account = cooperate.getBuildingFileAccount();
                cooperate.unRegisterBuildingFile();
                break;
            case VISIT_MAINTENANCE_ELEMENT:
                account = cooperate.getMaintenanceElementAccount();
                cooperate.unRegisterMaintenanceElement();
                break;
            case VISIT_MAINTENANCE_FILE:
                account = cooperate.getMaintenanceFileAccount();
                cooperate.unRegisterMaintenanceFile();
                break;
            default:
                account = null;
        }
        return account;
    }


    /**
     * 企业给子账号分享的项目
     *
     * @param account
     * @return
     */
    public List<Cooperate> getCooperates(Account account) {
        List<Project> projects = projectService.findProjectsByAccount(account);
        List<Cooperate> cooperates = new ArrayList<>();
        Cooperate cooperate;
        for (int i = 0; i < projects.size(); i++) {
            cooperate = makeCooperate(projects.get(i));
            cooperates.add(cooperate);
        }
        return cooperates;
    }

    /**
     * 保存项目的协同权限
     *
     * @param cooperate
     */
    private void saveCooperate(Cooperate cooperate) {
        Project project = cooperate.getProject();
        JSONObject cooperateJson = cooperate.toCooperateJson();
        JSONArray viewJsons = cooperate.toViewJson();
        project.setCooperate(cooperateJson.isEmpty() ? null : cooperateJson.toString());
        project.setViews(viewJsons.isEmpty() ? null : viewJsons.toString());
        projectService.save(project);
    }

    /**
     * 子账号是否可以编辑该项目
     *
     * @param cooperate
     * @param account
     * @return
     */
    public boolean isEdit(Cooperate cooperate, Account account) {
        if (cooperate.getInviteElementAccount() != null && cooperate.getInviteElementAccount().getId().equals(account.getId())) {
            return true;
        }
        if (cooperate.getInviteFileAccount() != null && cooperate.getInviteFileAccount().getId().equals(account.getId())) {
            return true;
        }
        if (cooperate.getBuildingElementAccount() != null && cooperate.getBuildingElementAccount().getId().equals(account.getId())) {
            return true;
        }
        if (cooperate.getBuildingFileAccount() != null && cooperate.getBuildingFileAccount().getId().equals(account.getId())) {
            return true;
        }
        if (cooperate.getPreparationElementAccount() != null && cooperate.getPreparationElementAccount().getId().equals(account.getId())) {
            return true;
        }
        if (cooperate.getPreparationFileAccount() != null && cooperate.getPreparationFileAccount().getId().equals(account.getId())) {
            return true;
        }
        if (cooperate.getMaintenanceElementAccount() != null && cooperate.getMaintenanceElementAccount().getId().equals(account.getId())) {
            return true;
        }
        if (cooperate.getMaintenanceFileAccount() != null && cooperate.getMaintenanceFileAccount().getId().equals(account.getId())) {
            return true;
        }
        return false;
    }

    /**
     * 子账号是否可以查看该项目
     *
     * @param cooperate
     * @param account
     * @return
     */
    private boolean isView(Cooperate cooperate, Account account) {
        List<Account> accounts = cooperate.getVisitAccounts();
        if (accounts.size() == 0) {
            return false;
        }
        for (int i = 0; i < accounts.size(); i++) {
            if (account.getId().equals(accounts.get(i).getId())) {
                return true;
            }
        }
        return false;
    }


    public boolean isCooperate(Project project, Account account) {
        if (project == null) {
            return false;
        }
        Cooperate cooperate = makeCooperate(project);
        if (isEdit(cooperate, account) || isView(cooperate, account)) {
            return true;
        }
        return false;
    }

    /**
     * 子账号是否可以编辑该项目
     *
     * @param project
     * @param account
     * @return
     */
    public boolean isEditElementAccount(Project project, Account account, String unitAilas) {
        if (account == null) {
            return false;
        }
        Cooperate cooperate = makeCooperate(project);
        String prefix = unitAilas.substring(0, 1);
        switch (prefix) {
            case "1":
                if (cooperate.getInviteElementAccount() != null && cooperate.getInviteElementAccount().getId().equals(account.getId())) {
                    return true;
                }
            case "2":
                if (cooperate.getBuildingElementAccount() != null && cooperate.getBuildingElementAccount().getId().equals(account.getId())) {
                    return true;
                }
            case "3":
                if (cooperate.getPreparationElementAccount() != null && cooperate.getPreparationElementAccount().getId().equals(account.getId())) {
                    return true;
                }
            case "4":
                if (cooperate.getMaintenanceElementAccount() != null && cooperate.getMaintenanceElementAccount().getId().equals(account.getId())) {
                    return true;
                }
            default:
                return false;
        }
    }

    /**
     * 企业是否可以编辑该项目
     *
     * @param project
     * @param user
     * @param unitAilas
     * @return
     */
    public boolean isEditElementUser(Project project, User user, String unitAilas) {
        if (user == null || !user.getId().equals(project.getUser().getId())) {
            return false;
        }
        Cooperate cooperate = makeCooperate(project);
        String prefix = unitAilas.substring(0, 1);
        switch (prefix) {
            case "1":
                if (cooperate.getInviteElementAccount() == null) {
                    return true;
                }
            case "2":
                if (cooperate.getBuildingElementAccount() == null) {
                    return true;
                }
            case "3":
                if (cooperate.getPreparationElementAccount() == null) {
                    return true;
                }
            case "4":
                if (cooperate.getMaintenanceElementAccount() == null) {
                    return true;
                }
            default:
                return false;
        }
    }

    /**
     * 取消协同授权
     *
     * @param user
     * @param account
     */
    public void cooperateRevoke(User user, Account account) {
        List<Project> projects = projectService.findByUser(user);
        if (projects == null || projects.size() == 0) {
            return;
        }
        Cooperate cooperate;
        List<String> types;
        for (int i = 0; i < projects.size(); i++) {
            cooperate = makeCooperate(projects.get(i));
            if (isView(cooperate, account)) {
                unRegistView(projects.get(i), account);
            }
            if (isEdit(cooperate, account)) {
                types = getEditTypes(cooperate, account);
                unCooperate(projects.get(i), types,user);
            }
        }
    }

    /**
     * 子账号是否可以编辑该项目
     *
     * @param cooperate
     * @param account
     * @return
     */
    private List<String> getEditTypes(Cooperate cooperate, Account account) {
        List<String> types = new ArrayList<>();
        if (cooperate.getInviteElementAccount() != null && cooperate.getInviteElementAccount().getId().equals(account.getId())) {
            types.add("VISIT_INVITE_ELEMENT");
        }
        if (cooperate.getInviteFileAccount() != null && cooperate.getInviteFileAccount().getId().equals(account.getId())) {
            types.add("VISIT_INVITE_FILE");
        }
        if (cooperate.getBuildingElementAccount() != null && cooperate.getBuildingElementAccount().getId().equals(account.getId())) {
            types.add("VISIT_BUILDING_ELEMENT");
        }
        if (cooperate.getBuildingFileAccount() != null && cooperate.getBuildingFileAccount().getId().equals(account.getId())) {
            types.add("VISIT_BUILDING_FILE");
        }
        if (cooperate.getPreparationElementAccount() != null && cooperate.getPreparationElementAccount().getId().equals(account.getId())) {
            types.add("VISIT_PREPARATION_ELEMENT");
        }
        if (cooperate.getPreparationFileAccount() != null && cooperate.getPreparationFileAccount().getId().equals(account.getId())) {
            types.add("VISIT_PREPARATION_FILE");
        }
        if (cooperate.getMaintenanceElementAccount() != null && cooperate.getMaintenanceElementAccount().getId().equals(account.getId())) {
            types.add("VISIT_MAINTENANCE_ELEMENT");
        }
        if (cooperate.getMaintenanceFileAccount() != null && cooperate.getMaintenanceFileAccount().getId().equals(account.getId())) {
            types.add("VISIT_MAINTENANCE_FILE");
        }
        return types;
    }

    /**
     * 构建对应阶段的权限
     *
     * @param project
     * @param object
     * @return
     */
    public JSONObject getStageJson(Project project, Object object) {
        JSONObject jsonObject = new JSONObject();
        Cooperate cooperate = makeCooperate(project);
        if (object instanceof User) {
            User user = (User) object;
            boolean flag = false;
            if (project.getUser().getId().equals(user.getId())) {
                flag = true;
            }
            buildStageJson(jsonObject, cooperate.getInvite(), "invite", flag);
            buildStageJson(jsonObject, cooperate.getPreparation(), "preparation", flag);
            buildStageJson(jsonObject, cooperate.getBuilding(), "building", flag);
            buildStageJson(jsonObject, cooperate.getMaintenance(), "maintenance", flag);
        }
        if (object instanceof Account) {
            Account account = (Account) object;
            if (!isCooperate(project, account)) {
                return jsonObject;
            }
            buildStageJson(jsonObject, cooperate.getInvite(), "invite", account);
            buildStageJson(jsonObject, cooperate.getPreparation(), "preparation", account);
            buildStageJson(jsonObject, cooperate.getBuilding(), "building", account);
            buildStageJson(jsonObject, cooperate.getMaintenance(), "maintenance", account);
        }
        return jsonObject;
    }

    /**
     * 构建企业用户对应阶段的权限(包含分享用户和自己)
     *
     * @param jsonObject
     * @param stage
     * @param stageName
     * @param flag
     */
    private void buildStageJson(JSONObject jsonObject, Stage stage, String stageName, boolean flag) {
        JSONObject stageJson = new JSONObject();
        boolean flag1 = stage.getElementVisit().getAccount() == null;
        //判断是否为分享用户
        if (!flag) {
            stageJson.put("action", ElementGroup.Action.VIEW);
        } else {
            if (flag1) {
                stageJson.put("action", ElementGroup.Action.EDIT);
            } else {
                stageJson.put("action", ElementGroup.Action.VIEW);
            }
        }
        jsonObject.put(stageName, stageJson);
    }

    /**
     * 构建子账号对应阶段的权限
     *
     * @param jsonObject
     * @param stage
     * @param stageName
     * @param account
     */
    private void buildStageJson(JSONObject jsonObject, Stage stage, String stageName, Account account) {
        JSONObject stageJson = new JSONObject();
        boolean flag = stage.getElementVisit().getAccount() != null && stage.getElementVisit().getAccount().getId().equals(account.getId());
        if (flag) {
            stageJson.put("action", ElementGroup.Action.EDIT);
        } else {
            stageJson.put("action", ElementGroup.Action.VIEW);
        }
        jsonObject.put(stageName, stageJson);
    }
}
