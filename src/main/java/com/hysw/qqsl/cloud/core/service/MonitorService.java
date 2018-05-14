package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.core.entity.data.Note;
import com.hysw.qqsl.cloud.core.entity.data.Sensor;
import com.hysw.qqsl.cloud.core.entity.data.Station;
import com.hysw.qqsl.cloud.util.HttpRequestUtil;
import com.hysw.qqsl.cloud.util.RSACoderUtil;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/5.
 */
@Service("monitorService")
public class MonitorService {
    //    唯一标识缓存
    private List<String> codes = new ArrayList<>();
    @Autowired
    private ApplicationTokenService applicationTokenService;
    @Autowired
    private SensorService sensorService;
    @Autowired
    private HttpRequestUtil httpRequestUtil;
    @Autowired
    private NoteCache noteCache;

    Setting setting = SettingUtils.getInstance().getSetting();

    Log logger = LogFactory.getLog(getClass());

    public boolean verify(String code, String ciphertext) {
        String s = null;
        try {
            s = RSACoderUtil.encryptAES(code, CommonAttributes.appliactionKey, CommonAttributes.appliactionIv);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuffer simple = new StringBuffer("");
        for (int i = 0; i < s.length(); i += 3) {
            simple.append(s.substring(i, i + 1));
        }
        if (simple.toString().equals(ciphertext)) {
            return true;
        }
        return false;
    }

    /**
     * 获取所有仪器列表信息
     *
     * @return
     */
    public JSONArray getApplicationList() {
        String url = "http://" + setting.getWaterIP() + ":8080/";
        String method = "sensors";
        String token = applicationTokenService.getToken();
        try {
            return httpRequestUtil.jsonArrayHttpRequest(url + method + "?token=" + token, "GET", null);
        } catch (Exception e) {
            Note note = new Note(SettingUtils.getInstance().getSetting().getNotice(), "异常：监测子系统");
            noteCache.add(SettingUtils.getInstance().getSetting().getNotice(),note);
        }
        return null;
    }


    //仪器与列表的绑定
    //添加仪器唯一标识进入缓存
    public void add(String s) {
        codes.add(s);
    }

    /**
     * 激活仪器
     */
    public void isActivation() {
        if (codes.size() == 0) {
            return;
        }
        Station station;
        JSONArray applicationList = getApplicationList();
        if(applicationList==null){
            return;
        }
        for (int i = 0; i < applicationList.size(); i++) {
            Map<String, Object> map = (Map<String, Object>) applicationList.get(i);
            for (int j = 0; j < codes.size(); j++) {
                if (map.get("code").toString().equals(codes.get(j))) {
                    //将数据库中激活状态改为true
                    Sensor sensor = sensorService.findByCode(codes.get(j));
                    station = sensor.getStation();
                    if (sensor == null) {
                        return;
                    }
                    Object type = map.get("type");
                    if (type == null || type.toString().trim().equals("0")) {
                        return;
                    }
                    sensor.setType(Sensor.Type.valueOf(Integer.valueOf(String.valueOf(type))));
                    if (CommonEnum.StationType.HYDROLOGIC_STATION.equals(station.getType()) && station.getSensors().size() != 0) {
                        //水位站已有仪表绑定;
                        logger.info("测站Id:" + station.getId() + ",水位站已有仪表绑定");
                        return;
                    }
                    sensor.setActivate(true);
                    sensorService.save(sensor);
                    logger.info("唯一编码:" + sensor.getCode() + "--激活并保存完毕");
                    //激活成功删除缓存中数据
                    codes.remove(j);
                }
            }
        }
    }

    /**
     * 格式化未激活仪器列表缓存
     */
    public void format() {
        codes = new ArrayList<>();
    }
}
