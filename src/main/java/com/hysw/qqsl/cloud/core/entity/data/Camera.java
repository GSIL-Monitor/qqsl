package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.core.entity.data.Sensor;
import com.hysw.qqsl.cloud.core.entity.data.Station;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Create by leinuo on 17-9-12 上午10:51
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 *
 *
 */
@Entity
@Table(name="camera")
@SequenceGenerator(name="sequenceGenerator", sequenceName="camera_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Camera extends BaseEntity{

    private static final long serialVersionUID = -3097083860247123817L;
    /** 摄像头名字 */
    private String name;
    /** 描述 */
    private String description;
    /** 厂家 */
    private String factroy;
    /** 联系人 */
    private String contact;
    /** 电话 */
    private String phone;
    /** 编码 */
    private String code;
    /** 密码 */
    private String password;
    /** 安装位置 */
    private String settingAddress;


    private Station station;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFactroy() {
        return factroy;
    }

    public void setFactroy(String factroy) {
        this.factroy = factroy;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @ManyToOne(fetch= FetchType.EAGER)
    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public String getSettingAddress() {
        return settingAddress;
    }

    public void setSettingAddress(String settingAddress) {
        this.settingAddress = settingAddress;
    }
}
