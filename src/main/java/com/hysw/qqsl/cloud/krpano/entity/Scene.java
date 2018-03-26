package com.hysw.qqsl.cloud.krpano.entity;

import java.io.Serializable;

/**
 * 全景实体类
 * Create by leinuo on 18-3-26 上午10:39
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
public class Scene implements Serializable{

    private static final long serialVersionUID = 5684132177393441035L;

    private String  includeUrl;
    /**
     * 场景名称
     */
    private String  name;
    /**
     * 场景标题
     */
    private String  title;
    /**
     * 场景缩略图
     */
    private String  thumbUrl;
    /**
     * 维度
     */
    private String  lat;
    /**
     * 经度
     */
    private String  lng;

    private String  heading;
    private String  hlookat;
    private String  vlookat;
    private String  fovtype;
    private String  fov;
    private String  maxpixelzoom;
    private String  fovmin;
    private String  fovmax;
    private String  limitview;
    private String  previewUrl;
    private String  prealign;
    private String  cubeUrl;


    public String getIncludeUrl() {
        return includeUrl;
    }

    public void setIncludeUrl(String includeUrl) {
        this.includeUrl = includeUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getHlookat() {
        return hlookat;
    }

    public void setHlookat(String hlookat) {
        this.hlookat = hlookat;
    }

    public String getVlookat() {
        return vlookat;
    }

    public void setVlookat(String vlookat) {
        this.vlookat = vlookat;
    }

    public String getFovtype() {
        return fovtype;
    }

    public void setFovtype(String fovtype) {
        this.fovtype = fovtype;
    }

    public String getFov() {
        return fov;
    }

    public void setFov(String fov) {
        this.fov = fov;
    }

    public String getMaxpixelzoom() {
        return maxpixelzoom;
    }

    public void setMaxpixelzoom(String maxpixelzoom) {
        this.maxpixelzoom = maxpixelzoom;
    }

    public String getFovmin() {
        return fovmin;
    }

    public void setFovmin(String fovmin) {
        this.fovmin = fovmin;
    }

    public String getFovmax() {
        return fovmax;
    }

    public void setFovmax(String fovmax) {
        this.fovmax = fovmax;
    }

    public String getLimitview() {
        return limitview;
    }

    public void setLimitview(String limitview) {
        this.limitview = limitview;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getPrealign() {
        return prealign;
    }

    public void setPrealign(String prealign) {
        this.prealign = prealign;
    }

    public String getCubeUrl() {
        return cubeUrl;
    }

    public void setCubeUrl(String cubeUrl) {
        this.cubeUrl = cubeUrl;
    }

}
