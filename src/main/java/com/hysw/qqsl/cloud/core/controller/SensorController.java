package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Sensor;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.*;
import net.sf.json.JSONObject;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by chenl on 17-3-29.
 */
@Controller
@RequestMapping("/sensor")
public class SensorController {
    @Autowired
    private ApplicationTokenService applicationTokenService;

    /**
     * 获取token
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/token", method = RequestMethod.GET)
    public @ResponseBody Message getToken() {
        return new Message(Message.Type.OK, applicationTokenService.getToken());
    }

}

