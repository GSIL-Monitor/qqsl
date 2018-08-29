package com.hysw.qqsl.cloud.core.entity.builds;


import com.hysw.qqsl.cloud.CommonEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Graph implements Serializable{

	private static final long serialVersionUID = -8094145981956727995L;
	/** 前台坐标*/
	private List<CoordinateBase> coordinates=new ArrayList<CoordinateBase>();
	/** 线面类型*/
//	private Type type;
	/** 类型*/
	private CommonEnum.CommonType baseType;
	/** 描述*/
	private String description;
	/** 别名 */
	private String alias;
	/** 点类型*/
//	public Type getType() {
//		return type;
//	}
//	public void setType(Type type) {
//		this.type = type;
//	}
	public CommonEnum.CommonType getBaseType() {
		return baseType;
	}
	public void setBaseType(CommonEnum.CommonType baseType) {
		this.baseType = baseType;
	}
	public List<CoordinateBase> getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(List<CoordinateBase> coordinates) {
		this.coordinates = coordinates;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
}
