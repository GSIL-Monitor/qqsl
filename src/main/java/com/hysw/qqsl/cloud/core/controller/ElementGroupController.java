package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.element.Unit;
import com.hysw.qqsl.cloud.core.entity.data.ElementDataGroup;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.*;

import com.hysw.qqsl.cloud.annotation.util.PackageIsExpire;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 复合要素Controller
 *
 * @author Administrator
 */
@Controller
@RequestMapping("/element")
public class ElementGroupController {
    Log logger = LogFactory.getLog(getClass());

    @Autowired
    private UnitService unitService;
    @Autowired
    private ContactService contactService;
    @Autowired
    private ElementService elementService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ElementDataGroupService elementDataGroupService;
    @Autowired
    private AuthentService authentService;
    @Autowired
    private CooperateService cooperateService;

    /**
     * 保存单元下的复合单元列表
     */
    @PackageIsExpire
    @SuppressWarnings("unchecked")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/elementGroup", method = RequestMethod.POST)
    public
    @ResponseBody
    Message saveElementGroup(@RequestBody Object object) {
        User user = authentService.getUserFromSubject();
        Map<String, Object> objectMap = (Map<String, Object>) object;
        // 取得单元id
        String alias = objectMap.get("alias").toString();
        Long projectId = Long.valueOf(objectMap.get("projectId").toString());
        Project project = projectService.find(projectId);
        // 取得单元
        Unit unit = unitService.findUnit(alias, false, project);
        Map<String,Object> elementGroup = (Map<String,Object>) objectMap
                .get("elementGroup");
        Account account = authentService.getAccountFromSubject();
        //判断是否有要素编辑权限
       /* boolean flagUser =elementService.authorityJudge(unit,simpleUser);
        if(!flagUser){
            return new Message(Message.Type.NO_AUTHORIZE);
        }*/
        boolean flagUser =cooperateService.isEditElementUser(project,user,unit.getAlias());
        boolean flagAccount =cooperateService.isEditElementAccount(project,account,unit.getAlias());
        if(!flagUser&&!flagAccount){
            return new Message(Message.Type.NO_AUTHORIZE);
        }
        // 保存要素，要素数据，项目简介，更新noteStr
        if(account!=null){
            elementService.doSaveElement(elementGroup, unit, account);
        }
        if(user!=null){
           elementService.doSaveElement(elementGroup, unit, user);
        }
        Map<String, Object> contactMap = (Map<String, Object>) objectMap
                .get("contact");
        // 保存通讯录
        if (contactMap != null && contactMap.size() > 0) {
            contactService.doSaveContact(contactMap,project.getUser());
        }
        return new Message(Message.Type.OK);
    }

    /**
     * 添加要素数据
     *
     * @param object
     * @return
     */
    @PackageIsExpire
    @SuppressWarnings("unchecked")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/makeElementDataGroup", method = RequestMethod.POST)
    public
    @ResponseBody
    Message makeElementDataGroup(@RequestBody Object object) {
        Message message = Message.parameterCheck(object);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        Map<String,Object> objectMap= (Map<String,Object>)message.getData();
        Long projectId = Long.valueOf(objectMap.get("projectId").toString());
        String elementAlias = objectMap.get("elementAlias").toString();
        String elementDataGroupName = objectMap.get("name").toString();
        String elementDataGroupType = objectMap.get("elementDataType").toString();
        ElementDataGroup elementDataGroup = elementDataGroupService.makeElementDataGroup(
            elementAlias, elementDataGroupName,elementDataGroupType, projectId);
        elementDataGroupService.saveElementDataGroup(elementDataGroup);
        elementDataGroup.setElementDB(null);
        return new Message(Message.Type.OK, elementDataGroup);
    }

    /**
     * 删除要素数据
     *
     * @param jsonObject
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteElementDataGroup", method = RequestMethod.POST)
    public
    @ResponseBody
    Message deleteElementData( @RequestBody JSONObject jsonObject) {
        Long elementDataGroupId = Long.valueOf(jsonObject.get("id").toString());
        Message message = Message.parametersCheck(elementDataGroupId);
        if(message.getType()== Message.Type.FAIL){
            return message;
        }
        Long id = Long.valueOf(elementDataGroupId);
        ElementDataGroup elementDataGroup = elementDataGroupService.find(id);
        if (elementDataGroup == null) {
            return new Message(Message.Type.EXIST);
        }
        elementDataGroupService.remove(id);
        return new Message(Message.Type.OK);
    }


}
