package com.hysw.qqsl.cloud.entity.data;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * 与微信相关实体类
 * Created by chenl on 17-7-4.
 */
@Entity
@Table(name = "weChat")
@SequenceGenerator(name = "sequenceGenerator",sequenceName = "weChat_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class WeChat extends BaseEntity {
    private Long userId;
    private String openId;
    private String nickName;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
