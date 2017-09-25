package com.hysw.qqsl.cloud.core.entity;

import java.io.Serializable;

/**
 * 短信实体类
 * 
 * @since 2015年11月23日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public class Note implements Serializable {

	private static final long serialVersionUID = 8018754754866533505L;
	/** 项目负责人 */
	private String name;
	/** 项目负责人联系电话 */
	private String phone;
	/**最终确定的短信内容*/
	private String sendMsg;

	public  Note(){

	}

	public Note(String phone, String sendMsg) {
		this.phone = phone;
		this.sendMsg = sendMsg;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getSendMsg() {
		return sendMsg;
	}

	public void setSendMsg(String sendMsg) {
		this.sendMsg = sendMsg;
	}

}

