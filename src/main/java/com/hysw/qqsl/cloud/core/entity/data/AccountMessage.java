package com.hysw.qqsl.cloud.core.entity.data;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

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
    private Status status;
    /** 企业id */
    private long userId;
    /** 项目id */
    private long projectId;

    public enum Status {
        UNREAD,
        READED
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getUserId() {
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
    }
}
