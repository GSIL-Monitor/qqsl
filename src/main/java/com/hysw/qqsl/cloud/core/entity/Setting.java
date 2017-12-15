package com.hysw.qqsl.cloud.core.entity;

import java.io.Serializable;

/**
 * 系统参数
 *
 * @since 2015年8月10日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public class Setting implements Serializable {
	private static final long serialVersionUID = -1259772664435701923L;
	
	/**qqsl运行状态控制 */
	private String status;
	/**工程模版*/ 
 	private String agrProjectModel;
 	private String conProjectModel;
 	private String hydProjectModel;
 	private String floProjectModel;
 	private String watProjectModel;
 	private String driProjectModel;
 	
 	/**工程复合要素模版*/ 
 	private String agrElementGroupModel;
 	private String conElementGroupModel;
 	private String hydElementGroupModel;
 	private String floElementGroupModel;
 	private String watElementGroupModel;
 	private String driElementGroupModel;
 	
 	/** info模板*/
 	private String info;
 	
 	/**elementData模版*/
	private String elementDataSimpleModel;

	/**
	 * 实时数据采集端IP
	 */
	private String waterIP;

	/**
	 * 支付成功之后,前台指定的跳转页面的地址
	 */
	private String aliPayReturnUrl;

	/**
	 * 支付成功之后,向后台异步通知的接口地址
	 */
	private String aliPayNotifyUrl;


	/** 建筑物 */
	private String buildsModel;
	private String buildsMater;
	private String buildsDimension;
	private String buildsHydraulics;
	private String buildsGeology;
	private String buildsStructure;

	/** 服务 */
	private String serveItem;
	private String packageModel;
	private String goods;
	private String station;

	private String nat123;
 	
 	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
 	public String getAgrProjectModel() {
		return agrProjectModel;
	}
	public void setAgrProjectModel(String agrProjectModel) {
		this.agrProjectModel = agrProjectModel;
	}
	public String getConProjectModel() {
		return conProjectModel;
	}
	public void setConProjectModel(String conProjectModel) {
		this.conProjectModel = conProjectModel;
	}
	public String getHydProjectModel() {
		return hydProjectModel;
	}
	public void setHydProjectModel(String hydProjectModel) {
		this.hydProjectModel = hydProjectModel;
	}
	public String getFloProjectModel() {
		return floProjectModel;
	}
	public void setFloProjectModel(String floProjectModel) {
		this.floProjectModel = floProjectModel;
	}
	public String getWatProjectModel() {
		return watProjectModel;
	}
	public void setWatProjectModel(String watProjectModel) {
		this.watProjectModel = watProjectModel;
	}
	public String getDriProjectModel() {
		return driProjectModel;
	}
	public void setDriProjectModel(String driProjectModel) {
		this.driProjectModel = driProjectModel;
	}
	public String getAgrElementGroupModel() {
		return agrElementGroupModel;
	}
	public void setAgrElementGroupModel(String agrElementGroupModel) {
		this.agrElementGroupModel = agrElementGroupModel;
	}
	public String getConElementGroupModel() {
		return conElementGroupModel;
	}
	public void setConElementGroupModel(String conElementGroupModel) {
		this.conElementGroupModel = conElementGroupModel;
	}
	public String getHydElementGroupModel() {
		return hydElementGroupModel;
	}
	public void setHydElementGroupModel(String hydElementGroupModel) {
		this.hydElementGroupModel = hydElementGroupModel;
	}
	public String getFloElementGroupModel() {
		return floElementGroupModel;
	}
	public void setFloElementGroupModel(String floElementGroupModel) {
		this.floElementGroupModel = floElementGroupModel;
	}
	public String getWatElementGroupModel() {
		return watElementGroupModel;
	}
	public void setWatElementGroupModel(String watElementGroupModel) {
		this.watElementGroupModel = watElementGroupModel;
	}
	public String getDriElementGroupModel() {
		return driElementGroupModel;
	}
	public void setDriElementGroupModel(String driElementGroupModel) {
		this.driElementGroupModel = driElementGroupModel;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}

	public String getElementDataSimpleModel() {
		return elementDataSimpleModel;
	}

	public void setElementDataSimpleModel(String elementDataSimpleModel) {
		this.elementDataSimpleModel = elementDataSimpleModel;
	}

	public String getWaterIP() {
		return waterIP;
	}

	public void setWaterIP(String waterIP) {
		this.waterIP = waterIP;
	}

	public String getAliPayReturnUrl() {
		return aliPayReturnUrl;
	}

	public void setAliPayReturnUrl(String aliPayReturnUrl) {
		this.aliPayReturnUrl = aliPayReturnUrl;
	}

	public String getAliPayNotifyUrl() {
		return aliPayNotifyUrl;
	}

	public void setAliPayNotifyUrl(String aliPayNotifyUrl) {
		this.aliPayNotifyUrl = aliPayNotifyUrl;
	}

	public String getBuildsModel() {
		return buildsModel;
	}

	public void setBuildsModel(String buildsModel) {
		this.buildsModel = buildsModel;
	}

	public String getBuildsMater() {
		return buildsMater;
	}

	public void setBuildsMater(String buildsMater) {
		this.buildsMater = buildsMater;
	}

	public String getBuildsDimension() {
		return buildsDimension;
	}

	public void setBuildsDimension(String buildsDimension) {
		this.buildsDimension = buildsDimension;
	}

	public String getBuildsHydraulics() {
		return buildsHydraulics;
	}

	public void setBuildsHydraulics(String buildsHydraulics) {
		this.buildsHydraulics = buildsHydraulics;
	}

	public String getBuildsGeology() {
		return buildsGeology;
	}

	public void setBuildsGeology(String buildsGeology) {
		this.buildsGeology = buildsGeology;
	}

	public String getBuildsStructure() {
		return buildsStructure;
	}

	public void setBuildsStructure(String buildsStructure) {
		this.buildsStructure = buildsStructure;
	}

	public String getServeItem() {
		return serveItem;
	}

	public void setServeItem(String serveItem) {
		this.serveItem = serveItem;
	}

	public String getPackageModel() {
		return packageModel;
	}

	public void setPackageModel(String packageModel) {
		this.packageModel = packageModel;
	}

	public String getGoods() {
		return goods;
	}

	public void setGoods(String goods) {
		this.goods = goods;
	}

	public String getStation() {
		return station;
	}

	public void setStation(String station) {
		this.station = station;
	}

	public String getNat123() {
		return nat123;
	}

	public void setNat123(String nat123) {
		this.nat123 = nat123;
	}
}
