package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hysw.qqsl.cloud.CommonEnum;

import javax.persistence.*;

/**
 * 项目工程布置坐标实体类
 *
 * @author leinuo
 *
 * @date 2016年1月12日
 */
@Entity
@Table(name = "coordinate")
@SequenceGenerator(name = "sequenceGenerator", sequenceName = "coordinate_sequence")
@JsonIgnoreProperties(value = { "hibernateLazyInitializer" })
public class Coordinate extends BaseEntity {

	private static final long serialVersionUID = -5197136434842823108L;


	/**
	 * 坐标转换基准面类型
	 */
	public enum BaseLevelType {
		WGS84, CGCS2000;
		public static BaseLevelType valueOf(int ordinal) {
			if (ordinal < 0 || ordinal >= values().length) {
				throw new IndexOutOfBoundsException("Invalid ordinal");
			}
			return values()[ordinal];
		}
	}

	/**
	 * WGS84坐标格式
	 */
	public enum WGS84Type {
		// 35.429898
		DEGREE,
		// 35:23
		DEGREE_MINUTE_1,
		// 35^o23'
		DEGREE_MINUTE_2,
		// 35:23:45
		DEGREE_MINUTE_SECOND_1,
		// 35^o23'45''
		DEGREE_MINUTE_SECOND_2,
		// 平面坐标
		PLANE_COORDINATE;
		public static WGS84Type valueOf(int ordinal) {
			if (ordinal < 0 || ordinal >= values().length) {
				throw new IndexOutOfBoundsException("Invalid ordinal");
			}
			return values()[ordinal];
		}
	}

	public Coordinate(){
		this.source = Build.Source.DESIGN;
	}

	// 点线面json
	private String coordinateStr;
	private Project project;
	private Build.Source source= Build.Source.DESIGN;
	private String description;
	private CommonEnum.CommonType commonType;
	private Boolean errorMsg = false;
	// 采集用户
	private long userId;
	private String name;
	private String deviceMac;

	@JsonIgnore
	//@Lob
	@Basic(fetch = FetchType.EAGER)
	@Column(columnDefinition = "text")
	public String getCoordinateStr() {
		return coordinateStr;
	}

	public void setCoordinateStr(String coordinateStr) {
		this.coordinateStr = coordinateStr;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JsonIgnore
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@JsonIgnore
	public Build.Source getSource() {
		return source;
	}

	public void setSource(Build.Source source) {
		this.source = source;
	}

	@JsonIgnore
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDeviceMac() {
		return deviceMac;
	}

	public void setDeviceMac(String deviceMac) {
		this.deviceMac = deviceMac;
	}

	public CommonEnum.CommonType getCommonType() {
		return commonType;
	}

	public void setCommonType(CommonEnum.CommonType commonType) {
		this.commonType = commonType;
	}

	@Transient
	public Boolean getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(Boolean errorMsg) {
		this.errorMsg = errorMsg;
	}
}

