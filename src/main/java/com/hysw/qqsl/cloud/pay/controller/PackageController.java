package com.hysw.qqsl.cloud.pay.controller;

import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.AuthentService;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 水利云配置controller
 *
 * @author chenl
 * @create 2017-09-12 上午10:34
 */
@Controller
@RequestMapping("/package")
public class PackageController {
    @Autowired
    private AuthentService authentService;
    @Autowired
    private PackageService packageService;
    /**
     * 获取套餐详情(用户)
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/getPackage", method = RequestMethod.GET)
    public @ResponseBody
    Message getPackage() {
        User user = authentService.getUserFromSubject();
        Package aPackage = packageService.findByUser(user);
        return new Message(Message.Type.OK,packageService.toJson(aPackage));
    }
}
