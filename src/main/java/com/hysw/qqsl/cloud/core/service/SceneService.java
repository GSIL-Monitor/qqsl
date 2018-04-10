package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.SceneDao;
import com.hysw.qqsl.cloud.core.entity.data.Scene;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @anthor Administrator
 * @since 15:51 2018/4/8
 */
@Service("sceneService")
public class SceneService extends BaseService<Scene, Long> {
    @Autowired
    private SceneDao sceneDao;
    @Autowired
    public void setBaseDao(SceneDao sceneDao) {
        super.setBaseDao(sceneDao);
    }
}
