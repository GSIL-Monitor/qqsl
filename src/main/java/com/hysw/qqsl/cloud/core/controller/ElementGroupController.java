package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.core.entity.Message;
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
     * @param object 包含单元别名alias,项目标识projectId,以及所要保存的单元下的复合要素elementGroup,
     *               该单元有通讯录是还包含contact信息
     * @return message消息体,OK:保存成功 NO_AUTHORIZE:当前对象没有对当前单元下的要素编辑权限
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
        boolean flagUser =cooperateService.isEditElementUser(project,user,unit.getAlias());
        boolean flagAccount =cooperateService.isEditElementAccount(project,account,unit.getAlias());
        if(!flagUser&&!flagAccount){
            return MessageService.message(Message.Type.NO_AUTHORIZE);
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
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 添加要素数据
     * @param object 包含要素别名elementAlias,项目标识projectId,要添加的要素数据名称name,要素数据类型elementDataType
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
        Message message = MessageService.parameterCheck(object);
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
        return MessageService.message(Message.Type.OK, elementDataGroup);
    }

    /**
     * 删除要素数据
     * @param jsonObject 包含要删除的要素数据标识id
     * @return message消息体,FAIL:删除失败,EXIST:要素数据不存在,OK:删除成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deleteElementDataGroup", method = RequestMethod.POST)
    public
    @ResponseBody
    Message deleteElementData( @RequestBody JSONObject jsonObject) {
        Long elementDataGroupId = Long.valueOf(jsonObject.get("id").toString());
        Message message = MessageService.parametersCheck(elementDataGroupId);
        if(message.getType()== Message.Type.FAIL){
            return message;
        }
        Long id = Long.valueOf(elementDataGroupId);
        ElementDataGroup elementDataGroup = elementDataGroupService.find(id);
        if (elementDataGroup == null) {
            return MessageService.message(Message.Type.EXIST);
        }
        elementDataGroupService.remove(id);
        return MessageService.message(Message.Type.OK);
    }


}
