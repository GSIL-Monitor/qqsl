package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.data.Panorama;
import com.hysw.qqsl.cloud.core.entity.data.Scene;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.Date;

import static org.junit.Assert.*;

/**
 * Create by leinuo on 18-3-27 下午5:44
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
public class SceneServiceTest extends BaseTest {

    @Autowired
    private SceneService sceneService;
    @Autowired
    private PanoramaService panoramaService;

    @Test
    public void testSave(){
        Panorama panorama = read();
        if(panorama.getScenes()==null||panorama.getScenes().size()==0){
            Scene scene = new Scene();
            scene.setFileName("666(1)");
            scene.setThumbUrl("http://qqslimage.oss-cn-hangzhou.aliyuncs.com/1/works/75c116470f1a1e3b/thumb.jpg");
             scene.setInstanceId("75c116470f1a1e3b");
            scene.setOriginUrl("http://qqslimage.oss-cn-hangzhou.aliyuncs.com/1/sourceimg/1522044969009cco.jpg");
            scene.setPanorama(panorama);
            sceneService.save(scene);
            scene = new Scene();
            scene.setFileName("湟中维新渠化效果(1)");
            scene.setThumbUrl("http://qqslimage.oss-cn-hangzhou.aliyuncs.com/1/works/590a999cd358fb6e/thumb.jpg");
            scene.setInstanceId("590a999cd358fb6e");
            scene.setOriginUrl("http://qqslimage.oss-cn-hangzhou.aliyuncs.com/1/sourceimg/1522044969010jmg.jpg");
            scene.setPanorama(panorama);
            sceneService.save(scene);
        }
        panorama = panoramaService.find(1l);
        assertNotNull(panorama.getId());
        assertTrue(panorama.getScenes().size()==2);
        panorama.setHotspot("{\"scene_75c116470f1a1e3b\":{\"scene\":[{\"iconType\":\"custom\",\"imgPath\":\"http://qqslimage.oss-cn-hangzhou.aliyuncs.com/1/media/img/a2b3e82dded8e3fd.png\",\"thumbPath\":\"http://qqslimage.oss-cn-hangzhou.aliyuncs.com/1/media/img/a2b3e82dded8e3fd.png\",\"isDynamic\":\"0\",\"ath\":\"7.2739317319591\",\"atv\":\"37.587015248987\",\"name\":\"schp_fjcex3bwkd\",\"linkedscene\":\"scene_590a999cd358fb6e\",\"sceneTitle\":\"湟中维新渠化效果(1)\",\"sceneImg\":\"http://qqslimage.oss-cn-hangzhou.aliyuncs.com/1/works/590a999cd358fb6e/thumb.jpg\"}],\"link\":[],\"image\":[],\"text\":[{\"iconType\":\"custom\",\"imgPath\":\"http://qqslimage.oss-cn-hangzhou.aliyuncs.com/1/media/img/95b84432a515fec1.png\",\"thumbPath\":\"http://qqslimage.oss-cn-hangzhou.aliyuncs.com/1/media/img/95b84432a515fec1.png\",\"isDynamic\":\"0\",\"ath\":\"21.609106152338\",\"atv\":\"48.473176347164\",\"name\":\"schp_jk2fwtstbi\",\"hotspotTitle\":\"你好\",\"wordContent\":\"你的应用加了身份认证，有人（或者你自己，呵呵）试图用manager用户登陆你的应用，密码输入错误5次或者5次以上（缺省是5次），就会在日志中记录警告信息，并锁定并禁止该用户的进一步登陆。以提醒你可能有人恶意猜测你的管理员密码。是tomcat为了阻止brute-force攻击（基于密码加密的暴力破解法）的安全策略。\",\"isShowSpotName\":\"\"}],\"voice\":[],\"imgtext\":[],\"obj\":[]},\"scene_590a999cd358fb6e\":{\"scene\":[],\"link\":[],\"image\":[],\"text\":[],\"voice\":[],\"imgtext\":[],\"obj\":[]}}");
        panoramaService.save(panorama);

    }

    private Panorama read(){
        Panorama panorama = panoramaService.find(1l);
        if(panorama == null){
            panorama = new Panorama();
            panorama.setThumbUrl("http://qqslimage.oss-cn-hangzhou.aliyuncs.com/1/works/75c116470f1a1e3b/thumb.jpg");
            //panoramaConfig.setCdnHost("http://qqslimage.oss-cn-hangzhou.aliyuncs.com/");
            panorama.setInfo("青海西宁全景");
            panorama.setAngleOfView("{\"viewSettings\":[{\"sceneName\":\"scene_75c116470f1a1e3b\",\"hlookat\":\"24.088790438777\",\"vlookat\":\"56.670667874245\",\"fov\":\"90\",\"fovmin\":\"5\",\"fovmax\":\"120\",\"vlookatmin\":\"-90\",\"vlookatmax\":\"90\",\"keepView\":\"0\"},{\"sceneName\":\"scene_590a999cd358fb6e\",\"hlookat\":\"14.186482857673\",\"vlookat\":\"23.921350220188\",\"fov\":\"90\",\"fovmin\":\"5\",\"fovmax\":\"120\",\"vlookatmin\":\"-90\",\"vlookatmax\":\"90\",\"keepView\":\"0\"}]}");
            panorama.setInstanceId("4202f21fd3dbc3e6");
            panorama.setHotspot("{\"scene_75c116470f1a1e3b\":{\"scene\":[{\"iconType\":\"custom\",\"imgPath\":\"http://qqslimage.oss-cn-hangzhou.aliyuncs.com/1/media/img/a2b3e82dded8e3fd.png\",\"thumbPath\":\"http://qqslimage.oss-cn-hangzhou.aliyuncs.com/1/media/img/a2b3e82dded8e3fd.png\",\"isDynamic\":\"0\",\"ath\":\"7.2739317319591\",\"atv\":\"37.587015248987\",\"name\":\"schp_fjcex3bwkd\",\"linkedscene\":\"scene_590a999cd358fb6e\",\"sceneTitle\":\"湟中维新渠化效果(1)\",\"sceneImg\":\"http://qqslimage.oss-cn-hangzhou.aliyuncs.com/1/works/590a999cd358fb6e/thumb.jpg\"}],\"link\":[],\"image\":[],\"text\":[{\"iconType\":\"custom\",\"imgPath\":\"http://qqslimage.oss-cn-hangzhou.aliyuncs.com/1/media/img/95b84432a515fec1.png\",\"thumbPath\":\"http://qqslimage.oss-cn-hangzhou.aliyuncs.com/1/media/img/95b84432a515fec1.png\",\"isDynamic\":\"0\",\"ath\":\"21.609106152338\",\"atv\":\"48.473176347164\",\"name\":\"schp_jk2fwtstbi\",\"hotspotTitle\":\"你好\",\"wordContent\":\"你的应用加了身份认证，有人（或者你自己，呵呵）试图用manager用户登陆你的应用，密码输入错误5次或者5次以上（缺省是5次），就会在日志中记录警告信息，并锁定并禁止该用户的进一步登陆。以提醒你可能有人恶意猜测你的管理员密码。是tomcat为了阻止brute-force攻击（基于密码加密的暴力破解法）的安全策略。\",\"isShowSpotName\":\"\"}],\"voice\":[],\"imgtext\":[],\"obj\":[]},\"scene_590a999cd358fb6e\":{\"scene\":[],\"link\":[],\"image\":[],\"text\":[],\"voice\":[],\"imgtext\":[],\"obj\":[]}}");
            panorama.setRegion("西宁");
            panorama.setShare(true);
            panorama.setAdvice("success");
            panorama.setCoor("{\"longitude\":\"101.50464694444672\",\"latitude\":\"36.7285541666189\",\"elevation\":\"2705.48780033033\"}");
            panorama.setStatus(CommonEnum.Review.PASS);
            panorama.setReviewDate(new Date());
            panorama.setName("333333");
            panoramaService.save(panorama);
        }
        return panorama;
    }
}