package com.hysw.qqsl.cloud.nine.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.StationModel;
import com.hysw.qqsl.cloud.core.entity.data.Station;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.StationService;
import com.hysw.qqsl.cloud.core.service.UserService;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 水位站service测试用例
 *
 * @since 2017年9月30日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 */
public class StationServiceTest extends BaseTest {

    @Autowired
    private StationService stationService;
    @Autowired
    private UserService userService;
    // 宏电水位站
    private String huangshh01_instanceId = "0010000001";
    private String huangshh02_instanceId = "0010000002";
    private String huangshh03_instanceId = "0010000003";
    private String jiefq01_instanceId = "0020000001";
    private String jiefq02_instanceId = "0020000002";
    private String jiefq03_instanceId = "0020000003";
    // 互助三源
    private String huzhsy = "BBA5D6FACFD8C8FDD4B4B5E7";
    // 乌兰都兰
    private String wuldl = "CEDAC0BCCFD8000000000000";
    // 达日水电站
    private String darsdzh = "B9FBC2E5D6DDB8CAB5C2CFD8";
    // 甘德水电站
    private String gandsdzh = "B9FBC2E5D6DDB4EFC8D5CFD8";
    // 玛沁江壤
    private String maqsdzh = "B9FBC2E5D6DDC2EAC7DFCFD8";

    /**
     * 测站xml配置缓存测试
     */
    @Test
    public void testPutInCache(){
        stationService.putStationModelInCache();
        List<StationModel> stationModels = stationService.getStationModelFromCache();
        Assert.assertTrue(stationModels.size()==4);
    }

    /**
     * 恢复第八次迭代的水位站
     */
    @Test
    public void testRestoreStation() throws ParseException {
        Station station = null;
        // 湟水河1#
        station = stationService.findByInstanceId(huangshh01_instanceId);
        if (station==null) {
            build(huangshh01_instanceId);
            station = stationService.findByInstanceId(huangshh01_instanceId);
        }
        Assert.assertNotNull(station);
        // 湟水河2#
        station = stationService.findByInstanceId(huangshh02_instanceId);
        if (station==null) {
            build(huangshh02_instanceId);
            station = stationService.findByInstanceId(huangshh02_instanceId);
        }
        Assert.assertNotNull(station);
        // 湟水河3#
        station = stationService.findByInstanceId(huangshh03_instanceId);
        if (station==null) {
            build(huangshh03_instanceId);
            station = stationService.findByInstanceId(huangshh03_instanceId);
        }
        Assert.assertNotNull(station);
        // 解放渠1#
        station = stationService.findByInstanceId(jiefq01_instanceId);
        if (station==null) {
            build(jiefq01_instanceId);
            station = stationService.findByInstanceId(jiefq01_instanceId);
        }
        Assert.assertNotNull(station);
        // 解放渠2#
        station = stationService.findByInstanceId(jiefq02_instanceId);
        if (station==null) {
            build(jiefq02_instanceId);
            station = stationService.findByInstanceId(jiefq02_instanceId);
        }
        // 解放渠3#
        station = stationService.findByInstanceId(jiefq03_instanceId);
        if (station==null) {
            build(jiefq03_instanceId);
            station = stationService.findByInstanceId(jiefq03_instanceId);
        }
        // 互助三源
        station = stationService.findByInstanceId(huzhsy);
        if (station==null) {
            build(huzhsy);
            station = stationService.findByInstanceId(huzhsy);
        }
        // 乌兰都兰
        station = stationService.findByInstanceId(wuldl);
        if (station==null) {
            build(wuldl);
            station = stationService.findByInstanceId(wuldl);
        }
        // 达日
        station = stationService.findByInstanceId(darsdzh);
        if (station==null) {
            build(darsdzh);
            station = stationService.findByInstanceId(darsdzh);
        }
        // 甘德
        station = stationService.findByInstanceId(gandsdzh);
        if (station==null) {
            build(gandsdzh);
            station = stationService.findByInstanceId(gandsdzh);
        }
        // 玛沁
        station = stationService.findByInstanceId(maqsdzh);
        if (station==null) {
            build(maqsdzh);
            station = stationService.findByInstanceId(maqsdzh);
        }
        Assert.assertNotNull(station);
    }

    @Test
    public void testGetStations() {
        User user = userService.find(16l);
        List<JSONObject> stations = stationService.getStations(user);
        Assert.assertNotNull(stations);
    }

    /**
     * 建立水位站
     * @param instanceId 唯一编码
     */
    private void build(String instanceId) throws ParseException {
        User user;
        org.springframework.util.Assert.hasLength(instanceId, "唯一编码不能为空");
        Station station = new Station();
        station.setInstanceId(instanceId);
        // 到期时间无限
        String sDate = "2099/12/31";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        station.setExpireDate(sdf.parse(sDate));
        station.setType(CommonEnum.StationType.WATER_LEVEL_STATION);
        station.setTransform(false);
        station.setParameter("{}");
        station.setRiverModel(getRiverModel(instanceId));
        station.setFlowModel(getFlowModel(instanceId));
        // 湟水河1#
        if (instanceId.equals(huangshh01_instanceId)==true) {
            station.setName("湟水河一号测点");
            station.setDescription("湟水河一号测点");
            station.setAddress("西宁市");
            station.setCoor("{\"longitude\":\"101.67822819977684\",\"latitude\":\"36.65375645069668\",\"elevation\":\"0\"}");
        }
        // 湟水河2#
        if (instanceId.equals(huangshh02_instanceId)==true) {
            station.setName("湟水河二号测点");
            station.setDescription("湟水河二号测点");
            station.setAddress("西宁市");
            station.setCoor("{\"longitude\":\"101.70140996276658\",\"latitude\":\"36.65429995072397\",\"elevation\":\"0\"}");
        }
        // 湟水河3#
        if (instanceId.equals(huangshh03_instanceId)==true) {
            station.setName("湟水河三号测点");
            station.setDescription("湟水河三号测点");
            station.setAddress("西宁市");
            station.setCoor("{\"longitude\":\"101.75777646850432\",\"latitude\":\"36.64124067972177\",\"elevation\":\"0\"}");
        }
        // 解放渠1#
        if (instanceId.equals(jiefq01_instanceId)==true) {
            station.setName("解放渠一号测点");
            station.setDescription("解放渠一号测点");
            station.setAddress("西宁市");
            station.setCoor("{\"longitude\":\"101.66680146228761\",\"latitude\":\"36.6472833310488\",\"elevation\":\"0\"}");
        }
        // 解放渠2#
        if(instanceId.equals(jiefq02_instanceId)==true) {
            station.setName("解放渠二号测点");
            station.setDescription("解放渠二号测点");
            station.setAddress("西宁市");
            station.setCoor("{\"longitude\":\"101.70956709457784\",\"latitude\":\"36.63710166807447\",\"elevation\":\"0\"}");
        }
        // 解放渠3#
        if(instanceId.equals(jiefq03_instanceId)==true) {
            station.setName("解放渠三号测点");
            station.setDescription("解放渠三号测点");
            station.setAddress("西宁市");
            station.setCoor("{\"longitude\":\"101.74519193239226\",\"latitude\":\"36.62518183074351\",\"elevation\":\"0\"}");
        }
        // 互助三源
        if (instanceId.equals(huzhsy)) {
            station.setName("互助三源电站");
            station.setDescription("互助三源电站");
            station.setAddress("中国青海省海东市互助土族自治县242县道");
            station.setCoor("{\"longitude\":\"102.571235\",\"latitude\":\"36.869238\",\"elevation\":\"0\"}");
        }
        // 乌兰都兰
        if (instanceId.equals(wuldl)) {
            station.setName("乌兰县都兰河水电站");
            station.setDescription("乌兰县都兰河水电站");
            station.setAddress("中国青海省乌兰县");
            station.setCoor("{\"longitude\":\"98.515411\",\"latitude\":\"36.964837\",\"elevation\":\"0\"}");
        }
        // 达日
        if (instanceId.equals(darsdzh)) {
            station.setName("达日县1#水位站");
            station.setDescription("达日县1#水位站");
            station.setAddress("中国青海省果洛藏族自治州达日县市场路73号 邮政编码: 814200");
            station.setCoor("{\"longitude\":\"99.65446415287657\",\"latitude\":\"33.76272991528731\",\"elevation\":\"0\"}");
        }
        // 甘德
        if (instanceId.equals(gandsdzh)) {
            station.setName("甘德县1#水位站");
            station.setDescription("甘德县1#水位站");
            station.setAddress(" 甘德县果洛藏族自治州青海省中国");
            station.setCoor("{\"longitude\":\"100.46299111925686\",\"latitude\":\"34.226925891683166\",\"elevation\":\"0\"}");
        }
        // 玛沁
        if (instanceId.equals(maqsdzh)) {
            station.setName("玛沁县1#水位站");
            station.setDescription("玛沁县1#水位站");
            station.setAddress("中国青海省果洛藏族自治州玛沁县702县道");
            station.setCoor("{\"longitude\":\"100.24752321441764\",\"latitude\":\"34.70227363559634\",\"elevation\":\"0\"}");
        }
        user = userService.find(16l);
        station.setUser(user);
        stationService.save(station);
    }

    /**
     * 取得河道模型
     * @param instanceId 测站唯一编码
     * @return 河道模型
     */
    private String getRiverModel(String instanceId) {
        // 湟水河1#
        if (instanceId.equals(huangshh01_instanceId)==true) {
            return "[{x: 0, y: 2235.36},{x: 1, y: 2235.3},{x: 2, y: 2235.24}, {x: 3, y: 2235.18},{x: 3.4, y: 2235.16},{x: 4, y: 2235.11},{x: 5, y: 2235.03},{x: 5.7, y: 2234.97},{x: 7, y: 2234.67}, {x: 7.7, y: 2234.63},{x: 8, y: 2234.54},{x: 9, y: 2234.28},{x: 10, y: 2233.86},{x: 10.5, y: 2233.63},{x: 11, y: 2233.38},{x: 11.5, y: 2233.16},{x: 12, y: 2232.91},{x: 12.7, y: 2232.58},{x: 13.6, y: 2232.1},{x: 14, y: 2231.8},{x: 14.4, y: 2231.53},{x: 15, y: 2231.5},{x: 15.9, y: 2231.24},{x: 16.9, y: 2230.95},{x: 18, y: 2230.41},{x: 18.5, y: 2230.18},{x: 19, y: 2230.15},{x: 20, y: 2230.08},{x: 20.5, y: 2230.05},{x: 21, y: 2230.04},{x: 22, y: 2230.03},{x: 23, y: 2230.02},{x: 24, y: 2230.01},{x: 25, y: 2230},{x: 25.5, y: 2229.99},{x: 26, y: 2229.98},{x: 27, y: 2229.97},{x: 28, y: 2229.96},{x: 29, y: 2229.94},{x: 30, y: 2229.93},{x: 31, y: 2229.92},{x: 31.4, y: 2229.91},{x: 32, y: 2229.9},{x: 33, y: 2229.89},{x: 34, y: 2229.87},{x: 35, y: 2229.85},{x: 36, y: 2229.84},{x: 37, y: 2229.83}, {x: 38, y: 2229.81},{x: 39, y: 2229.8},{x: 39.6, y: 2229.79},{x: 40, y: 2229.79},{x: 41, y: 2229.79},{x: 42, y: 2229.8},{x: 43, y: 2229.8},{x: 43.4, y: 2229.8},{x: 44, y: 2229.8},{x: 45, y: 2229.81},{x: 46, y: 2229.82},{x: 47, y: 2229.82},{x: 48, y: 2229.83},{x: 49, y: 2229.84},{x: 50, y: 2229.85},{x: 51, y: 2229.86},{x: 52, y: 2229.87},{x: 53, y: 2229.88},{x: 54, y: 2229.89},{x: 55, y: 2229.9},{x: 56, y: 2229.91},{x: 57, y: 2229.93},{x: 58, y: 2229.94},{x: 59, y: 2230.15},{x: 59.9, y: 2230.33},{x: 61, y: 2230.67},{x: 62, y: 2231},{x: 63, y: 2231.25},{x: 63.4, y: 2231.49},{x: 64, y: 2231.78},{x: 64.5, y: 2232.04},{x: 65, y: 2232.1},{x: 66, y: 2232.24},{x: 66.7, y: 2232.34},{x: 67.7, y: 2232.89},{x: 68.7, y: 2233.16},{x: 69.8, y: 2233.53},{x: 71, y: 2233.98},{x: 71.8, y: 2234.31},{x: 73, y: 2234.69},{x: 74, y: 2235.03},{x: 75, y: 2235.27},{x: 75.7, y: 2235.43},{x: 77, y: 2235.44},{x: 78, y: 2235.44},{x: 79, y: 2235.45},{x: 80, y: 2235.46}]";
        }
        // 湟水河2#
        if (instanceId.equals(huangshh02_instanceId)==true) {
            return "[{x: 0, y: 2225.46},{x: 1, y: 2225.29},{x: 2, y: 2224.77},{x: 3, y: 2224.44},{x: 3.8, y: 2224.41},{x: 5, y: 2224.04},{x: 5.8, y: 2223.93},{x: 7, y: 2223.59},{x: 8, y: 2223.27},{x: 9, y: 2223.04},{x: 10, y: 2222.77},{x: 10.4, y: 2222.79},{x: 11, y: 2222.49},{x: 11.8, y: 2222.05},{x: 12.4, y: 2222.05},{x: 13, y: 2221.95},{x: 13.3, y: 2221.91},{x: 14, y: 2221.54},{x: 14.9, y: 2221.09},{x: 15.9, y: 2220.2},{x: 17, y: 2220.12},{x: 17.4, y: 2220.08},{x: 18, y: 2220.09},{x: 19, y: 2220.07},{x: 19.4, y: 2220.07},{x: 20, y: 2220.06},{x: 21, y: 2220.02},{x: 22, y: 2219.93},{x: 23, y: 2219.89},{x: 24, y: 2219.89},{x: 25, y: 2219.85},{x: 26, y: 2219.78},{x: 26.8, y: 2219.78},{x: 28, y: 2219.76},{x: 29, y: 2219.69},{x: 30, y: 2219.57},{x: 31, y: 2219.53},{x: 31.6, y: 2219.52},{x: 32, y: 2219.52},{x: 33, y: 2219.54},{x: 34, y: 2219.55},{x: 35, y: 2219.57},{x: 36, y: 2219.57},{x: 36.7, y: 2219.57},{x: 37, y: 2219.57},{x: 38, y: 2219.58},{x: 39, y: 2219.62},{x: 40, y: 2219.64},{x: 40.8, y: 2219.65},{x: 42, y: 2219.64},{x: 43, y: 2219.62},{x: 44, y: 2219.59},{x: 44.7, y: 2219.58},{x: 45, y: 2219.58},{x: 46, y: 2219.62},{x: 47, y: 2219.7},{x: 48, y: 2219.79},{x: 49, y: 2219.81},{x: 50, y: 2219.81},{x: 51, y: 2219.81},{x: 52, y: 2219.85},{x: 53, y: 2219.91},{x: 54, y: 2219.93},{x: 55, y: 2219.93},{x: 56, y: 2219.94},{x: 57, y: 2219.98},{x: 58, y: 2220.03},{x: 59, y: 2220.06},{x: 60, y: 2220.07},{x: 60.5, y: 2220.04},{x: 61, y: 2220.11},{x: 62, y: 2220.2},{x: 62.4, y: 2220.3},{x: 63, y: 2221.08},{x: 63.9, y: 2221.63},{x: 64.9, y: 2221.77},{x: 65.5, y: 2221.89},{x: 65.9, y: 2221.9},{x: 66.9, y: 2222.17},{x: 67.9, y: 2222.54},{x: 68.9, y: 2222.68},{x: 69.9, y: 2222.93},{x: 70.9, y: 2223.21},{x: 71.8, y: 2223.45},{x: 72.9, y: 2223.45},{x: 73.9, y: 2223.44},{x: 74.9, y: 2223.42},{x: 80, y: 2223.41}]";
        }
        // 湟水河3#
        if (instanceId.equals(huangshh03_instanceId)) {
            return "[{x: 0, y: 2191.28},{x: 2, y: 2191.28},{x: 4.2, y: 2187.01},{x: 6.2, y: 2187.13},{x: 7, y: 2187.07},{x: 7.9, y: 2186.99},{x: 10.2, y: 2187.01},{x: 12, y: 2187.75},{x: 12.5, y: 2190.37},{x: 13.7, y: 2190.36},{x: 15.4, y: 2186.97},{x: 17, y: 2186.94},{x: 18.1, y: 2186.93},{x: 22, y: 2187},{x: 22.7, y: 2187.01},{x: 27, y: 2187.24},{x: 28.6, y: 2187.28},{x: 32.3, y: 2187.2},{x: 34.1, y: 2187.18},{x: 37, y: 2187.17},{x: 41.9, y: 2187.13},{x: 47, y: 2186.99},{x: 52, y: 2186.96},{x: 53.9, y: 2186.95},{x: 57, y: 2187},{x: 59.6, y: 2187.05},{x: 62, y: 2190.35},{x: 62.6, y: 2190.36},{x: 64.6, y: 2190.36}]";
        }
        // 解放渠1#
        if(instanceId.equals(jiefq01_instanceId)==true) {
            return "[{x: 0, y: 2259.808},{x: 5, y: 2259.808},{x: 5, y: 2258.308},{x: 7.4, y: 2258.308},{x: 7.4, y: 2259.808},{x: 12.4, y: 2259.808}]";
        }
        // 解放渠2#
        if (instanceId.equals(jiefq02_instanceId)) {
            return "[{x: 0, y: 2257.769},{x: 5, y: 2257.769},{x: 5, y: 2255.469},{x: 8, y: 2255.469},{x: 8, y: 2257.769},{x: 13, y: 2257.769}]";
        }
        // 解放渠3#
        if (instanceId.equals(jiefq03_instanceId)) {
            return "[{x: 0, y: 2247.035},{x: 5, y: 2247.035},{x: 5, y: 2244.835},{x: 7, y: 2244.835},{x: 7, y: 2247.035},{x: 12, y: 2247.035}]";
        }
        // 互助三源
        if (instanceId.equals(huzhsy)) {
            return "[{x: 0, y: 2222.39},{x: 1.5, y: 2222.39},{x: 2.3, y: 2221.59},{x: 5.8, y: 2221.59},{x: 6.6, y: 2222.39},{x: 8, y: 2222.39}]";
        }
        // 乌兰都兰
        if (instanceId.equals(wuldl)) {
            return "[{x: 0, y: 2974.169},{x: 2, y: 2974.169},{x: 2, y: 2973.269},{x: 3, y: 2973.269},{x: 3, y: 2974.169},{x: 5, y: 2974.169}]";
        }
        // 达日
        if (instanceId.equals(darsdzh)) {
            return "[{x: 0, y: 4001.6},{x: 1., y: 4001.6},{x: 2, y: 4001.6},{x: 2, y: 3997.1},{x: 3, y: 3997.1},{x: 4, y: 3997.1},{x: 5, y: 3997.1},{x: 6, y: 3997.1},{x: 7, y: 3997.1},{x: 8, y: 3997.1},{x: 9, y: 3997.1},{x: 10, y: 3997.1},{x: 11, y: 3997.1},{x: 12, y: 3997.1},{x: 12, y: 4001.6},{x: 13, y: 4001.6},{x: 14, y: 4001.6}]";
        }
        // 甘德
        if (instanceId.equals(gandsdzh)) {
            return "[{x: 0, y: 4037.499},{x: 1, y: 4036.822},{x: 2, y: 4036.557},{x: 3, y: 4036.48},{x: 4, y: 4036.464},{x: 5, y: 4036.461},{x: 6, y: 4036.47},{x: 7, y: 4036.448},{x: 8, y: 4036.44},{x: 9, y: 4036.445},{x: 10, y: 4036.448},{x: 11, y: 4036.45},{x: 12, y: 4036.44},{x: 13, y: 4036.445},{x: 14, y: 4036.442},{x: 15, y: 4036.446},{x: 16, y: 4036.42},{x: 17, y: 4036.415},{x: 18, y: 4036.373},{x: 19, y: 4036.364},{x: 20, y: 4036.3},{x: 21, y: 4036.271},{x: 22, y: 4036.251},{x: 23, y: 4036.24},{x: 24, y: 4036.221},{x: 25, y: 4036.208},{x: 26, y: 4036.181},{x: 27, y: 4036.179},{x: 28, y: 4036.164},{x: 29, y: 4036.15},{x: 30, y: 4036.194},{x: 31, y: 4037.056},{x: 32, y: 4037.499}]";
        }
        // 玛沁
        if (instanceId.equals(maqsdzh)) {
            return "[{x: 0, y: 3536.825},{x: 0.5, y: 3536.785},{x: 1, y: 3536.644},{x: 1.5, y: 3536.504},{x: 2, y: 3536.364},{x: 2.333, y: 3536.27},{x: 2.833, y: 3536.181},{x: 3.333, y: 3536.122},{x: 3.833, y: 3536.063},{x: 4.333, y: 3536.004},{x: 4.833, y: 3535.946},{x: 5.333, y: 3535.891},{x: 5.833, y: 3535.837},{x: 6.333, y: 3535.783},{x: 6.833, y: 3535.728},{x: 7.02, y: 3535.708},{x: 7.52, y: 3535.726},{x: 8.02, y: 3535.756},{x: 8.52, y: 3535.778},{x: 9.02, y: 3535.761},{x: 9.52, y: 3535.753},{x: 9.994, y: 3535.741},{x: 10.494, y: 3535.738},{x: 10.994, y: 3535.725},{x: 11.494, y: 3535.711},{x: 11.994, y: 3535.703},{x: 12.494, y: 3535.695},{x: 12.994, y: 3535.686},{x: 13.494, y: 3535.678},{x: 13.994, y: 3535.662},{x: 14.202, y: 3535.659},{x: 14.702, y: 3535.641},{x: 15.202, y: 3535.631},{x: 15.702, y: 3535.622},{x: 16.202, y: 3535.613},{x: 16.702, y: 3535.604},{x: 17.167, y: 3535.596},{x: 17.667, y: 3535.584},{x: 18.167, y: 3535.573},{x: 18.667, y: 3535.562},{x: 19.167, y: 3535.551},{x: 19.667, y: 3535.548},{x: 20.167, y: 3535.558},{x: 20.501, y: 3535.568},{x: 21.001, y: 3535.581},{x: 21.501, y: 3535.603},{x: 22.001, y: 3535.635},{x: 22.166, y: 3535.654},{x: 22.666, y: 3535.688},{x: 23.166, y: 3535.715},{x: 23.666, y: 3535.753},{x: 24.166, y: 3535.813},{x: 24.666, y: 3535.862},{x: 25.166, y: 3535.912},{x: 25.666, y: 3535.941},{x: 26.313, y: 3535.981},{x: 26.813, y: 3536.047},{x: 27.313, y: 3536.128},{x: 27.813, y: 3536.31},{x: 28.313, y: 3536.592},{x: 28.852, y: 3536.842}]";
        }
        return "[]";
    }

    /**
     * 取得水位流量模型
     * @param instanceId 测站唯一编码
     * @return 水位流量模型
     */
    private String getFlowModel(String instanceId) {
        // 湟水河1#
        if (instanceId.equals(huangshh01_instanceId)==true) {
            return "[{x: 2230.603,y: 0},{x: 2231.103,y: 17.71}, {x: 2231.603,y: 70.59}, {x: 2232.103,y: 154.04}, {x: 2232.603,y: 261.37}, {x: 2233.103,y: 396.88}, {x: 2233.603,y: 553.62}, {x: 2234.103,y: 731.89}, {x: 2234.603,y: 937.70}, {x: 2235.103,y: 1152.48}]";
        }
        // 湟水河2#
        if (instanceId.equals(huangshh02_instanceId)==true) {
            return "[{ x: 2220.197,y: 0}, {x: 2220.697,y: 16.39}, {x: 2221.197,y: 70.75}, {x: 2221.697,y: 153.35}, {x: 2222.197,y: 256.93}, {x: 2222.697,y: 382.41}, {x: 2223.197,y: 530.05}, {x: 2223.697,y: 701.50}]";
        }
        // 湟水河3#
        if (instanceId.equals(huangshh03_instanceId)) {
            return "[{x: 2187.13,y: 0}, {x: 2187.63,y: 26.55}, {x: 2188.13,y: 83.11}, {x: 2188.63,y: 161.11}, {x: 2189.13,y: 256.75}, {x: 2189.63,y: 367.52}, {x: 2190.13,y: 491.60}, {x: 2190.63,y: 627.57}, {x: 2191.13,y: 774.28}, {x: 2191.63,y: 930.74}]";
        }
        // 解放渠1#
        if(instanceId.equals(jiefq01_instanceId)) {
            return "[{x: 2258.308, y: 0},{x: 2258.508, y: 0.32},{x: 2258.708, y: 0.94},{x: 2258.908, y: 1.71},{x: 2259.108, y: 2.58},{x: 2259.308, y: 3.5},{x: 2259.508, y: 4.49},{x: 2259.708, y: 5.51},{x: 2259.908, y: 6.55}]";
        }
        // 解放渠2#
        if(instanceId.equals(jiefq02_instanceId)) {
            return "[{x: 2255.469, y: 0},{x: 2255.669, y: 0.13},{x: 2255.869, y: 0.39},{x: 2256.069, y: 0.72},{x: 2256.269, y: 1.1},{x: 2256.469, y: 1.51},{x: 2256.669, y: 1.94},{x: 2256.869, y: 2.39},{x: 2257.069, y: 2.86},{x: 2257.269, y: 3.34},{x: 2257.469, y: 3.83},{x: 2257.669, y: 4.32},{x: 2257.869, y: 4.83}]";
        }
        // 解放渠3#
        if (instanceId.equals(jiefq03_instanceId)) {
            return "[{x: 2244.835, y: 0},{x: 2245.035, y: 0.24},{x: 2245.235, y: 0.7},{x: 2245.435, y: 1.25},{x: 2245.635, y: 1.86},{x: 2245.835, y: 2.51},{x: 2246.035, y: 3.18},{x: 2246.235, y: 3.88},{x: 2246.435, y: 4.59},{x: 2246.635, y: 5.31},{x: 2246.835, y: 6.04},{x: 2247.035, y: 6.78},{x: 2247.235, y: 7.52},{x: 2247.435, y: 8.27}    ]";
        }
        // 互助三源
        if (instanceId.equals(huzhsy)) {
            return "[{x: 0, y: 0},{x: 0.1, y: 0.37},{x: 0.2, y: 1.17},{x: 0.3, y: 2.30},{x: 0.4, y: 3.72},{x: 0.5, y: 5.42},{x: 0.6, y: 7.38},{x: 0.7, y: 9.60},{x: 0.8, y: 12.07},{x: 0.9, y: 14.77}]";
        }
        // 乌兰都兰
        if (instanceId.equals(wuldl)) {
            return "[{x: 0, y: 0},{x: 0.1, y: 0.06},{x: 0.2, y: 0.17},{x: 0.3, y: 0.30},{x: 0.4, y: 0.45},{x: 0.5, y: 0.61},{x: 0.6, y: 0.77},{x: 0.7, y: 0.94},{x: 0.8, y: 1.11},{x: 0.9, y: 1.29}]";
        }
        // 达日
        if (instanceId.equals(darsdzh)) {
            return "[{x: 0, y: 0},{x: 0.5, y: 5.27},{x: 1, y: 15.83},{x: 1.5, y: 29.53},{x: 2, y: 45.43},{x: 2.5, y: 62.97},{x: 3, y: 81.77},{x: 3.5, y: 101.57},{x: 4, y: 122.18},{x: 4.5, y: 143.44}];";
        }
        // 甘德
        if (instanceId.equals(gandsdzh)) {
            return "[{x: 0, y: 0},{x: 0.2, y: 2.94},{x: 0.4, y: 8.16},{x: 0.6, y: 18.39},{x: 0.8, y: 29.14},{x: 1, y: 47.09},{x: 1.2, y: 64.28},{x: 1.4, y: 87.27}]";
        }
        // 玛沁
        if (instanceId.equals(maqsdzh)) {
            return "[{x: 0, y: 0},{x: 0.1, y: 0.10},{x: 0.2, y: 0.45},{x: 0.3, y: 1.49},{x: 0.4, y: 2.97},{x: 0.5, y: 5.09},{x: 0.6, y: 7.80},{x: 0.7, y: 10.82},{x: 0.8, y: 14.56},{x: 0.9, y: 18.90}]";
        }
        return "[]";
    }

}
