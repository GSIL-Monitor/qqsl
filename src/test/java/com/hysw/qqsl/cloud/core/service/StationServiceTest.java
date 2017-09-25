package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.data.Station;
import com.hysw.qqsl.cloud.core.entity.data.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.hysw.qqsl.cloud.core.entity.StationModel;
import net.sf.json.JSONArray;
import org.junit.Assert;

import java.util.List;

public class StationServiceTest extends BaseTest {
    @Autowired
    private StationService stationService;
    @Autowired
    private UserService userService;
    @Test
    public void testGetAllType(){
        JSONArray jsonArray = stationService.getStationList();
        System.out.println(jsonArray);
    }

    @Test
    public void testPutInCache(){
        stationService.putStationModelInCache();
        List<StationModel> stationModels = stationService.getStationModelFromCache();
        Assert.assertTrue(stationModels.size()==4);
    }
    @Test
    public void testCreate(){
        Station station = new Station();
        station.setName("水文测试");
        station.setAddress("西安");
      //  station.setCoor();
        station.setDescription("青清水利");
        station.setInstanceId("1214312fgnjgaud7stvasvbous");
     //   station.setExprieDate(new Date());
     //   station.setParameter("测试");
        User user = userService.findByPhone("18661925010");
        station.setUser(user);
        station.setTransform(false);
        station.setType(CommonEnum.StationType.HYDROLOGIC_STATION);
        stationService.save(station);
    }

}
