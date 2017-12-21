package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.data.Interest;
import com.hysw.qqsl.cloud.core.entity.data.Panorama;
import com.hysw.qqsl.cloud.core.service.InterestService;
import com.hysw.qqsl.cloud.core.service.PanoramaService;
import net.sf.json.JSONArray;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by chenl on 17-2-18.
 */
public class InterestServiceTest extends BaseTest{
    @Autowired
    private InterestService interestService;
    @Autowired
    private PanoramaService panoramaService;

    @Test
    public void testFindAllPass(){
        List<Interest> list = interestService.findAllPass(null);
        List<Interest> list1 = interestService.findAllPass(25l);
        System.out.println(list.size());
    }

    @Test
    public void testPanorama(){
        List<Panorama> panoramas = panoramaService.findAllPass(null);
        JSONArray panoramasToJson = panoramaService.panoramasToJson(panoramas);
        System.out.println(panoramasToJson);
    }

}
