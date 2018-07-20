package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * 短信实体类
 * 
 * @since 2015年11月23日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
@Entity
@Table(name="note")
@SequenceGenerator(name="sequenceGenerator", sequenceName="account_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Note extends BaseEntity {

	private static final long serialVersionUID = 8018754754866533505L;
	/** 项目负责人 */
	private String name;
	/** 项目负责人联系电话 */
	private String phone;
	/** 短信号 */
	private String code;
	/** 短信内容 */
	private String sendMsg;
	/** 回复内容 */
	private String reply;


	public  Note(){

	}

	public Note(String phone, String sendMsg) {
		this.phone = phone;
		this.sendMsg = sendMsg;
	}

	@Transient
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

	@Transient
	public String getSendMsg() {
		return sendMsg;
	}

	public void setSendMsg(String sendMsg) {
		this.sendMsg = sendMsg;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
	}

}

