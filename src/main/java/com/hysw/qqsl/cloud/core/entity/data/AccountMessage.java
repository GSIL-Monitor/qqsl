package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.CommonEnum;

import javax.persistence.*;

/**
 * Create by leinuo on 17-5-16 下午3:28
 *
 * qq:1321404703 https://github.com/leinuo2016
 *
 * 与子账号相关的通知
 */
@Entity
@Table(name = "accountMessage")
@SequenceGenerator(name = "sequenceGenerator", sequenceName = "accountMessage_sequence")
@JsonIgnoreProperties(value = { "hibernateLazyInitializer" })
public class AccountMessage extends BaseEntity{

    private static final long serialVersionUID = -3452664373859421059L;

    private Account account;
    /** 内容 */
    private String content;
    /** 消息状态 */
    private CommonEnum.MessageStatus status;
    /** 类型 */
    private Type type;

    /**
     * 类型
     */
    public enum Type {
        /** 项目协同 */
        COOPERATE_PROJECT,
        /** 反馈回复 */
        FEEDBACK,
        /** 测站查看 */
        COOPERATE_STATION
    }

    @ManyToOne(fetch=FetchType.EAGER)
    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public CommonEnum.MessageStatus getStatus() {
        return status;
    }

    public void setStatus(CommonEnum.MessageStatus status) {
        this.status = status;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
/* public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }*/
}
