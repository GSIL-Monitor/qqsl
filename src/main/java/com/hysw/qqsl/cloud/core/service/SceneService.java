package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.SceneDao;
import com.hysw.qqsl.cloud.core.entity.data.Scene;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
