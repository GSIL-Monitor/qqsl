package com.hysw.qqsl.cloud.core.entity.data;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.station.Camera;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

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
    /** 参数 */
    private String parameter;
    /** 测站唯一标识 */
    private String instanceId;
    /** 测站分享 */
    private String shares;
    /** 是否修改过 */
    private boolean transform;
    /** 到期时间 */
    private Date expireDate;
    /** 测站图片(阿里云图片路径) */
    private String  picture;

    private User user;

    /** 河底高程*/
    private Double bottomElevation;

    /** 摄像头 非数据库对应*/
    private Camera camera;


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
    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

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

    public boolean isTransform() {
        return transform;
    }

    public void setTransform(boolean transform) {
        this.transform = transform;
    }

    @Column(length = 10000)
    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    @ManyToOne(fetch=FetchType.EAGER)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @OneToMany(mappedBy="station", fetch=FetchType.EAGER, cascade={CascadeType.PERSIST})
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

    @Transient
    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

}
