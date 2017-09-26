package com.hysw.qqsl.cloud.entity.data;

import javax.persistence.*;

import com.hysw.qqsl.cloud.entity.build.Config;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目坐标实体类
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

	public enum Type {
		POINT,LINE, AREA, FIELD;
		public static Type valueOf(int ordinal) {
			if (ordinal < 0 || ordinal >= values().length) {
				throw new IndexOutOfBoundsException("Invalid ordinal");
			}
			return values()[ordinal];
		}
	}

	public Coordinate(){
		this.source = Build.Source.DESIGN;
	}

	/** 点线面字符串 */
	private String coordinateStr;
	private Project project;
	private Build.Source source;

	/** 坐标类型 */
//	private Type type;
//	private Config.CommonType baseType;
	private String TreePath;
	private String description;
	/** caijiyonghu*/
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

	//	@Transient
//	public Type getType() {
//		return type;
//	}
//
//	public void setType(Type type) {
//		this.type = type;
//	}
//
//	@Transient
//	public Config.CommonType getBaseType() {
//		return baseType;
//	}
//
//	public void setBaseType(Config.CommonType baseType) {
//		this.baseType = baseType;
//	}
//
	@JsonIgnore
	public String getTreePath() {
		return TreePath;
	}

	public void setTreePath(String treePath) {
		TreePath = treePath;
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
}
