package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.SceneDao;
import com.hysw.qqsl.cloud.core.entity.data.Panorama;
import com.hysw.qqsl.cloud.core.entity.data.Scene;
import com.hysw.qqsl.cloud.core.entity.data.User;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    /**
     * 保存场景
     * @param user
     * @param panorama
     * @param images
     */
    public String saveScene(User user, Panorama panorama, List<JSONObject> images) {
        Object name;
        Object fileName;
        boolean flag = true;
        String thumbUrl = null;
        for (JSONObject image : images) {
            name = image.get("name");
            fileName = image.get("fileName");
            Scene scene = new Scene();
            scene.setFileName(name.toString());
            scene.setInstanceId(fileName.toString().substring(0,fileName.toString().lastIndexOf(".")));
            scene.setThumbUrl("http://qqslimage.oss-cn-hangzhou.aliyuncs.com/panorama/" + user.getId() + "/" + scene.getInstanceId() + ".tiles/thumb.jpg");
            scene.setPanorama(panorama);
            save(scene);
            if (flag) {
                thumbUrl = scene.getThumbUrl();
                flag = false;
            }
        }
        return thumbUrl;
    }
}
