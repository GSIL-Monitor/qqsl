package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Create by leinuo on 17-4-28 下午5:50
 *
 * qq:1321404703 https://github.com/leinuo2016
 *
 * oss文件路径实体类
 */
@Entity
@Table(name="oss")
@SequenceGenerator(name="sequenceGenerator", sequenceName="oss_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Oss extends BaseEntity {

    private static final long serialVersionUID = -8562655528543230723L;

    private String treePath;

    private Long userId;

    private Long projectId;

    public Oss(){

    }

    public Oss(String treePath, Long userId, Long projectId) {
        this.treePath = treePath;
        this.userId = userId;
        this.projectId = projectId;
    }

    @NotNull
    public String getTreePath() {
        return treePath;
    }

    public void setTreePath(String treePath) {
        this.treePath = treePath;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
