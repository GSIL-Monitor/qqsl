package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.entity.build.Gps;
import com.hysw.qqsl.cloud.entity.data.*;
import com.hysw.qqsl.cloud.util.PositionUtil;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by chenl on 17-5-24.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UpdateTest extends BaseTest {

    @Autowired
    private CoordinateService coordinateService;
    @Autowired
    private ElementDBService elementDBService;
    @Autowired
    private ElementDataGroupService elementDataGroupService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private BuildService buildService;
    @Autowired
    private BuildGroupService buildGroupService;
    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private SensorService sensorService;


//    数据库表elementDataGroup表删除属性alias,complexType,coorStr,marsCoorStr,projectId,treePath


}
