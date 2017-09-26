package com.hysw.qqsl.cloud.entity.data;

import com.hysw.qqsl.cloud.entity.element.ElementData;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 要素数据组
 *
 * @since 2016年6月21日 夏至
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 */
@Entity
@Table(name = "elementDataGroup")
@SequenceGenerator(name = "sequenceGenerator", sequenceName = "elementDataGroup_sequence")
@JsonIgnoreProperties(value = { "hibernateLazyInitializer" })
public class ElementDataGroup extends BaseEntity {

	private static final long serialVersionUID = -730233709137043622L;
	/** 名城 */
    private String name;
    /** 要素数据类型(简易) */
    private DataType DataType;
    /** 数据 */
    private String dataStr;
    /** 简介数据 */
    private String introDataStr;
    /** 要素数据 */
    private List<ElementData> elementDatas=new ArrayList<>();
//    private List<CoordinateBase> coordinateBases = new ArrayList<CoordinateBase>();
//    private List<CoordinateBase> marscoordinateBases = new ArrayList<CoordinateBase>();

    /** 要素外键 */
    private ElementDB elementDB;

    public enum DataType {
        /** 普通 */
        NORMAL,
        /** 总灌溉面积 */
        AGRICULTURAL,
        /** 浮标法 */
        BUOYMETHOD,
        /** 固定容积法 */
        FIXEDVVOLUME,
        /** 土井/机井 */
        MOTOR,
        /** 取水建筑物类型 */
        GETBULIDING,
        /** 阀门井 */
        CHAMBER,
        /** 蓄水池 */
        RESERVAIR,
        /** 管道 */
        PIPELINE,
        /** 其他 */
        OTHER,
        /** 总投资 */
        INVESTMENT,
        /** 灌区换分 */
        AGRDISTRICTSELECT,
        /** 灌溉方式划分 */
        AGRSTYLESELECT,
        /** 灌溉面积划分 */
        AGRAREASELECT,
        /** 渠道 */
        CHANNEL,
        /** 渠系建筑物 */
        CHANNELBUILD,
        /** 引水流量 */
        DIVERSION,
        /** 保护对象 */
        PROTECTION,
        /** 防洪堤/护岸及其长度  */
        FENCE,
        /** 排洪渠/箱涵及其长度*/
        BOX,
        /** 其他 */
        BRIDGE,
        /** 防治目标 */
        TREATMENT,
        /** 治坡工程 */
        SLOPE,
        /** 支沟工程 */
        BRANCH,
        /** 小型水利工程 */
        HYD,
        /** 植物措施 */
        VEGETATION,
        /** 水电站类型 */
        SHYD,
        /** 工程任务 */
        TASK,
        /** 坝型 */
        DAM,
        /** 节水设施*/
        WATERSAVING;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public ElementDataGroup.DataType getDataType() {
        return DataType;
    }

    public void setDataType(ElementDataGroup.DataType dataType) {
        DataType = dataType;
    }
    //@Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    @JsonIgnore
    public String getDataStr() {
        return dataStr;
    }

    public void setDataStr(String dataStr) {
        this.dataStr = dataStr;
    }
    //@Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(columnDefinition = "text")
    @JsonIgnore
    public String getIntroDataStr() {
        return introDataStr;
    }

    public void setIntroDataStr(String introDataStr) {
        this.introDataStr = introDataStr;
    }
    @Transient
    public List<ElementData> getElementDatas() {
        return elementDatas;
    }

    public void setElementDatas(List<ElementData> elementDatas) {
        this.elementDatas = elementDatas;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    public ElementDB getElementDB() {
        return elementDB;
    }

    public void setElementDB(ElementDB elementDB) {
        this.elementDB = elementDB;
    }

}
