package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.Panorama;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @anthor Administrator
 * @since 17:28 2018/1/17
 */
public class PanoramaServiceTest extends BaseTest {
    @Autowired
    private PanoramaService panoramaService;
    @Autowired
    private UserService userService;

    @Test
    public void testSavePanorama(){
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name","aaa");
        map.put("coor","102.36,35.5,0");
        map.put("region","xiningshi");
        map.put("status", CommonEnum.Review.PENDING.ordinal());
        map.put("isShare",true);
        map.put("picture","image");
        map.put("userId",1l);
        map.put("shootDate", new Date());
        JSONObject jsonObject = panoramaService.savePanorama(map, new Panorama());
        Assert.assertTrue(!jsonObject.isEmpty());
        map = new LinkedHashMap<>();
//        map.put("name","aaa");
//        map.put("coor","102.36,35.5,0");
        map.put("region","xiningshi");
        map.put("status", CommonEnum.Review.PENDING.ordinal());
        map.put("isShare",true);
        map.put("picture","image");
        map.put("userId",1l);
        map.put("shootDate", new Date());
        jsonObject = panoramaService.savePanorama(map, new Panorama());
        Assert.assertTrue(jsonObject == null);
        map = new LinkedHashMap<>();
        map.put("name","aaa");
        map.put("coor","102");
        map.put("region","xiningshi");
        map.put("status", CommonEnum.Review.PENDING.ordinal());
        map.put("isShare",true);
        map.put("picture","image");
        map.put("userId",1l);
        map.put("shootDate", new Date());
        jsonObject = panoramaService.savePanorama(map, new Panorama());
        Assert.assertTrue(jsonObject == null);
    }

//    @Test
//    public void testIsAllowSavePanorma(){
//        Message message = panoramaService.isAllowSavePanorma(userService.find(1l));
//        Assert.assertTrue(message.getType() == Message.Type.OK);
//    }
}
