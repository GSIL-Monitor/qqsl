package com.hysw.qqsl.cloud.core.entity.data;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;

/**
 * Create by leinuo on 18-3-27 下午4:53
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
@Entity
@Table(name = "scene")
@SequenceGenerator(name = "sequenceGenerator",sequenceName = "scene_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Scene extends BaseEntity{

    private static final long serialVersionUID = 4455107521461776780L;

    /**文件名（场景名称）*/
    private String fileName;

    /** 缩略图 */
    private String thumbUrl;

    /** 唯一编码 */
    private String instanceId;

    /** 全景 */
    private Panorama panorama;


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    @NotEmpty
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @ManyToOne(fetch=FetchType.EAGER)
    public Panorama getPanorama() {
        return panorama;
    }

    public void setPanorama(Panorama panorama) {
        this.panorama = panorama;
    }
}
