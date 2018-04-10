package com.hysw.qqsl.cloud.core.entity.data;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.core.entity.project.CooperateVisit;

/**
 * @anthor Administrator
 * @since 15:48 2018/4/8
 */
@Entity
@Table(name = "scene")
@SequenceGenerator(name = "sequenceGenerator", sequenceName = "project_sequence")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer"})
public class Scene extends BaseEntity{
    /** 文件名 */
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
