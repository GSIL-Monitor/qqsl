package com.hysw.qqsl.cloud.core.entity.buildModel;

import net.sf.json.JSONObject;

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

	/** 经度 */
	private String lon;
	/** 纬度 */
	private String lat;
	/** 海拔高程 */
	private String ele;

	public CoordinateBase() {
	}

	public CoordinateBase(String lon, String lat, String ele) {
		this.lon = lon;
		this.lat = lat;
		this.ele = ele;
	}

	public CoordinateBase(String center) {
		JSONObject jsonObject = JSONObject.fromObject(center);
		this.lon = jsonObject.get("lon").toString();
		this.lat = jsonObject.get("lat").toString();
		this.ele = jsonObject.get("ele").toString();
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getEle() {
		return ele;
	}

	public void setEle(String ele) {
		this.ele = ele;
	}

	public String toJSON(){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("lon", this.lon);
		jsonObject.put("lat", this.lat);
		jsonObject.put("ele", this.ele);
		return jsonObject.toString();
	}
}
