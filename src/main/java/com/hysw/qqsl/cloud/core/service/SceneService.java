package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.SceneDao;
import com.hysw.qqsl.cloud.core.entity.data.Scene;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Create by leinuo on 18-3-27 下午5:17
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
@Service("sceneService")
public class SceneService  extends BaseService<Scene, Long> {

    @Autowired
    private SceneDao sceneDao;

    @Autowired
    public void setBaseDao(SceneDao sceneDao) {
        super.setBaseDao(sceneDao);
    }

}
