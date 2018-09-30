package com.hysw.qqsl.cloud.core.entity.buildModel;

import java.io.Serializable;

/**
 * 坐标基类
 * 
 * @author leinuo
 *
 * @date 2016年1月12日
 */
public class CoordinateBase implements Serializable{


	private static final long serialVersionUID = 5008629272379811359L;

		private String name;
		private String alias;
		/** 经度 */
		private String longitude;
		/** 纬度 */
		private String latitude;
		/** 海拔高程 */
		private String elevation;
		private Type type;
	    private String accessibility;
		public enum Type{
			/** 水面点*/
			WATERTABLE,
			/** 地形点*/
			TERRAIN,
			/** 空间点*/
			SPACE,
			/** 洪痕点*/
			FLOODMARKS,
			/** 建筑物*/
			OTHER,
		}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}


	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getElevation() {
		return elevation;
	}

	public void setElevation(String elevation) {
		this.elevation = elevation;
	}

	public String getAccessibility() {
		return accessibility;
	}

	public void setAccessibility(String accessibility) {
		this.accessibility = accessibility;
	}


}
