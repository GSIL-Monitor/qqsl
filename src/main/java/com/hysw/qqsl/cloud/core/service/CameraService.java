package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.CameraDao;
import com.hysw.qqsl.cloud.core.entity.data.Camera;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 * @since 2018/11/14
 */
@Service("cameraService")
public class CameraService extends BaseService<Camera, Long> {
    @Autowired
    private CameraDao cameraDao;
    @Autowired
    public void setBaseDao(CameraDao cameraDao) {
        super.setBaseDao( cameraDao);
    }

    public void editCamera(Camera camera, Object name, Object description, Object factory, Object contact, Object phone, Object settingAddress, Object password) {
        if (name != null) {
            if (name.equals("")) {
                camera.setName(null);
            } else {
                camera.setName(name.toString());
            }
        }
        if (description != null) {
            if (description.equals("")) {
                camera.setDescription(null);
            } else {
                camera.setDescription(description.toString());
            }
        }
        if (factory != null) {
            if (factory.equals("")) {
                camera.setFactroy(null);
            } else {
                camera.setFactroy(factory.toString());
            }
        }
        if (contact != null) {
            if (contact.equals("")) {
                camera.setContact(null);
            } else {
                camera.setContact(contact.toString());
            }
        }
        if (phone != null) {
            if (phone.equals("")) {
                camera.setPhone(null);
            } else {
                camera.setPhone(phone.toString());
            }
        }
        if (settingAddress != null) {
            if (settingAddress.equals("")) {
                camera.setSettingAddress(null);
            } else {
                camera.setSettingAddress(settingAddress.toString());
            }
        }
        if (password != null) {
            if (password.equals("")) {
                camera.setPassword(null);
            } else {
                camera.setPassword(password.toString());
            }
        }
        save(camera);
    }

    public JSONObject makeCameraJson(Camera camera) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", camera.getName());
        jsonObject.put("settingAddress", camera.getSettingAddress());
        jsonObject.put("phone", camera.getPhone());
        jsonObject.put("password", camera.getPassword());
        jsonObject.put("factroy", camera.getFactroy());
        jsonObject.put("description", camera.getDescription());
        jsonObject.put("contact", camera.getContact());
        jsonObject.put("code", camera.getCode());
        jsonObject.put("id", camera.getId());
        return jsonObject;
    }
}
