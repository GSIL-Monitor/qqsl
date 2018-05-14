package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.dao.SceneDao;
import com.hysw.qqsl.cloud.core.entity.data.Panorama;
import com.hysw.qqsl.cloud.core.entity.data.Scene;
import com.hysw.qqsl.cloud.core.entity.data.User;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Create by leinuo on 18-3-27 下午5:17
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
@Service("sceneService")
public class SceneService  extends BaseService<Scene, Long> {

    @Autowired
    private OssService ossService;
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
    public String saveScene(User user, Panorama panorama, List<Map<String,String>> images) {
        Object name;
        Object fileName;
        boolean flag = true;
        String thumbUrl = null;
        for (Map<String,String> image : images) {
            name = image.get("name");
            fileName = image.get("fileName");
            Scene scene = new Scene();
            scene.setFileName(name.toString());
            scene.setInstanceId(fileName.toString().substring(0,fileName.toString().lastIndexOf(".")));
            scene.setThumbUrl("http://qqslimage.oss-cn-hangzhou.aliyuncs.com/panorama/" + user.getId() + "/" + scene.getInstanceId() + ".tiles/thumb.jpg");
            scene.setOriginUrl("panorama/"+user.getId() + "/" + fileName);
            scene.setPanorama(panorama);
            save(scene);
            if (flag) {
                thumbUrl = scene.getThumbUrl();
                flag = false;
            }
        }
        return thumbUrl;
    }

    /**
     * 保存场景
     * @param user
     * @param panorama
     */
    public String saveScene(User user, Panorama panorama,String fileName,String oirginName) {

        boolean flag = true;
        String thumbUrl = null;
            Scene scene = new Scene();
            scene.setFileName(fileName);
            scene.setInstanceId(oirginName);
            scene.setThumbUrl("http://qqslimage.oss-cn-hangzhou.aliyuncs.com/panorama/" + user.getId() + "/" + scene.getInstanceId() + ".tiles/thumb.jpg");
            scene.setOriginUrl("panorama/"+user.getId() + "/" + oirginName+".jpg");
            scene.setPanorama(panorama);
            save(scene);
            if (flag) {
                thumbUrl = scene.getThumbUrl();

            }

        return thumbUrl;
    }


    public JSONArray getScenes(List<Scene> scenes,boolean isEdit) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        Scene scene;
        if(scenes.size()==0){
            return jsonArray;
        }
        String url = "";
        for(int i=0;i<scenes.size();i++){
            scene = scenes.get(i);
            jsonObject = new JSONObject();
            jsonObject.put("thumbUrl",scene.getThumbUrl());
            jsonObject.put("createDate",scene.getCreateDate());
            jsonObject.put("modifyDate",scene.getModifyDate());
            jsonObject.put("id",scene.getId());
            jsonObject.put("instanceId",scene.getInstanceId());
            jsonObject.put("fileName",scene.getFileName());
            jsonObject.put("panorama",scene.getPanorama().getId());
            if(isEdit){
                url = ossService.getObjectUrl(scene.getOriginUrl(), CommonAttributes.BUCKET_NAME);
                jsonObject.put("downloadUrl", StringUtils.hasText(url)?url:"");
            }
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }
}
