package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.data.Interest;
import com.hysw.qqsl.cloud.core.entity.data.Panorama;
import com.hysw.qqsl.cloud.core.service.InterestService;
import com.hysw.qqsl.cloud.core.service.PanoramaService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenl on 17-2-18.
 */
public class InterestServiceTest extends BaseTest{
    @Autowired
    private InterestService interestService;

    @Test
    public void testSaveInterest(){
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name","aaa");
        map.put("type",Interest.Type.BASE.ordinal());
        map.put("category",Interest.Category.DESIGN.ordinal());
        map.put("coordinate","102.35,35.6,0");
        map.put("region","xiningshi");
        map.put("contact","13519779005");
        map.put("content","bbb");
        map.put("evaluate","`1111");
        map.put("business","2222");
        map.put("level","444");
        map.put("pictures","image");
        map.put("status", CommonEnum.Review.PENDING.ordinal());
        map.put("userId",1l);
        Message message = interestService.saveInterest(map, new Interest());
        Assert.assertTrue(message.getType()== Message.Type.OK);
        interestService.flush();
        List<Interest> interests = interestService.findAll();
        Assert.assertTrue(interests.size() == 1);
        JSONArray jsonArray = interestService.interestsToJson(interests);
        Assert.assertTrue(!jsonArray.isEmpty());
        JSONObject jsonObject = interestService.interestToJson(interests.get(0));
        Assert.assertTrue(!jsonObject.isEmpty());
    }

}
