package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.CommonEnum;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 测站实体类
 */
@Entity
@Table(name="station")
@SequenceGenerator(name="sequenceGenerator", sequenceName="station_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Station extends BaseEntity {

    private static final long serialVersionUID = 1753324356353389598L;
    /** 站名 */
    private String name;
    /** 描述 */
    private String description;
    /** 站类型 */
    private CommonEnum.StationType type;
    /** 坐标 */
    private String coor;
    /** 位置 */
    private String address;
    /** 河道模型 */
    private String riverModel;
    /** 流量曲线 */
    private String flowModel;
//    /** 参数 */
//    private String parameter;
    /** 测站唯一标识 */
    private String instanceId;
    /** 测站分享 */
    private String shares;
//    /** 是否修改过 */
//    private boolean transform;
    /** 到期时间 */
    private Date expireDate;
    /** 测站图片(阿里云图片路径) */
    private String  pictureUrl;

    private User user;

    private String cooperate;

    /** 河底高程*/
    private Double bottomElevation;

    /** 摄像头 非数据库对应*/
    private List<Camera> cameras;


    private List<Sensor> sensors = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CommonEnum.StationType getType() {
        return type;
    }

    public void setType(CommonEnum.StationType type) {
        this.type = type;
    }

    public String getCoor() {
        return coor;
    }

    public void setCoor(String coor) {
        this.coor = coor;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
//    @Basic(fetch = FetchType.EAGER)
//    @Column(columnDefinition = "text")
//    public String getParameter() {
//        return parameter;
//    }
//
//    public void setParameter(String parameter) {
//        this.parameter = parameter;
//    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getRiverModel() {
        return riverModel;
    }

    public void setRiverModel(String riverModel) {
        this.riverModel = riverModel;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getFlowModel() {
        return flowModel;
    }

    public void setFlowModel(String flowModel) {
        this.flowModel = flowModel;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getShares() {
        return shares;
    }

    public void setShares(String shares) {
        this.shares = shares;
    }

    @ManyToOne(fetch=FetchType.EAGER)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @OneToMany(mappedBy="station", fetch=FetchType.LAZY, cascade={CascadeType.PERSIST})
    @JsonIgnore
    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public Double getBottomElevation() {
        return bottomElevation;
    }

    public void setBottomElevation(Double bottomElevation) {
        this.bottomElevation = bottomElevation;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    @OneToMany(mappedBy="station", fetch=FetchType.LAZY, cascade={CascadeType.PERSIST})
    @JsonIgnore
    public List<Camera> getCameras() {
        return cameras;
    }

    public void setCameras(List<Camera> cameras) {
        this.cameras = cameras;
    }

    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getCooperate() {
        return cooperate;
    }

    public void setCooperate(String cooperate) {
        this.cooperate = cooperate;
    }
}
