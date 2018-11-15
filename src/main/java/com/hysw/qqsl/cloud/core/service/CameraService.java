package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.CameraDao;
import com.hysw.qqsl.cloud.core.entity.data.Camera;
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
            camera.setName(name.toString());
        }
        if (description != null) {
            camera.setDescription(description.toString());
        }
        if (factory != null) {
            camera.setFactroy(factory.toString());
        }
        if (contact != null) {
            camera.setContact(contact.toString());
        }
        if (phone != null) {
            camera.setPhone(phone.toString());
        }
        if (settingAddress != null) {
            camera.setSettingAddress(settingAddress.toString());
        }
        if (password != null) {
            camera.setPassword(password.toString());
        }
        save(camera);
    }
}
