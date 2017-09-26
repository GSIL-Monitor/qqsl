package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.entity.Setting;
import com.hysw.qqsl.cloud.entity.data.Sensor;
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

    Log logger = LogFactory.getLog(getClass());

    Setting setting = SettingUtils.getInstance().getSetting();

    public boolean verify(String code, String ciphertext) {
        String s = null;
        try {
            s = RSACoderUtil.encryptAES(code, CommonAttributes.appliactionKey, CommonAttributes.appliactionIv);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuffer simple = new StringBuffer("");
        for (int i = 0; i < s.length(); i+=3) {
            simple.append(s.substring(i,i+1));
        }
        if (simple.toString().equals(ciphertext)) {
            return true;
        }
        return false;
    }

    /**
     * 获取所有仪器列表信息
     * @return
     */
    public JSONArray getApplicationList(){
        String url="http://"+setting.getWaterIP()+":8080/";
        String method = "sensor";
        String token = applicationTokenService.getToken();
        JSONArray applicationList = HttpRequestUtil.jsonArrayHttpRequest(url+method+"?token="+token, "GET", null);
        return applicationList;
    }


    //仪器与列表的绑定
    //添加仪器唯一标识进入缓存
    public void add(String s){
        codes.add(s);
    }

    /**
     * 激活仪器
     */
    public void isActivation(){
        if (codes.size() == 0) {
            return;
        }
        JSONArray applicationList = getApplicationList();
        for (int i = 0; i < applicationList.size(); i++) {
            Map<String, Object> map = (Map<String, Object>) applicationList.get(i);
            for (int j = 0; j < codes.size(); j++) {
                if (map.get("unique").toString().equals(codes.get(j))) {
                    //将数据库中激活状态改为true
                    Sensor sensor = sensorService.findByCode(codes.get(j));
                    if (sensor == null) {
                        return;
                    }
                    Object type=map.get("type");
                    Object setupAddress = map.get("setup_address");
                    if (type == null || type.toString().trim().equals("0")) {
                        return;
                    }
                    sensor.setType(Sensor.Type.valueOf(Integer.valueOf(String.valueOf(type))));
                    if (setupAddress != null) {
                        sensor.setAddress(setupAddress.toString());
                    }
                    sensor.setActivate(true);
                    sensorService.save(sensor);
                    logger.info("唯一编码:"+sensor.getCode()+"--激活并保存完毕");
                    //激活成功删除缓存中数据
                    codes.remove(j);
                }
            }
        }
    }

    /**
     * 格式化未激活仪器列表缓存
     */
    public void format(){
        codes = new ArrayList<>();
    }
}
