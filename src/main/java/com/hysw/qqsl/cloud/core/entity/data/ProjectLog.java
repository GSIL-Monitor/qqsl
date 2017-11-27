package com.hysw.qqsl.cloud.core.entity.data;

import com.hysw.qqsl.cloud.core.entity.project.CooperateVisit;

public class ProjectLog extends BaseEntity {
    /** 内容 */
    private String content;
    /** 项目id */
    private long projectId;
    /** 子账号id */
    private long accountId;
    /** 类型 */
    private Type type;
    /** 阶段
     * 8个阶段，用于在取消且协同时构建是否编辑操作
     */
    private CooperateVisit.Type cooperateType;

    /**
     * 类型
     */
    public enum Type {
        // 项目要素修改
        ELEMENT,
        // 项目文件上传
        FILE_UPLOAD,
        // 项目文件下载
        FILE_DOWNLOAD
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public CooperateVisit.Type getCooperateType() {
        return cooperateType;
    }

    public void setCooperateType(CooperateVisit.Type cooperateType) {
        this.cooperateType = cooperateType;
    }
}
