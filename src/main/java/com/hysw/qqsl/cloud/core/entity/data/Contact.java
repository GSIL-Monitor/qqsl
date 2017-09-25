package com.hysw.qqsl.cloud.core.entity.data;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.*;


/**
 * 通讯录实体类
 * 
 * @since 2015年11月23日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
@Entity
@Table(name = "contact")
@SequenceGenerator(name = "sequenceGenerator", sequenceName = "contact_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class Contact extends BaseEntity{

	private static final long serialVersionUID = -8289526663925027521L;

	/** 中标单位 */
	private String company;
	/** 资质等级 */
	private String qualify;
	/** 承担部门 */
	private String depart;
	/** 部门负责人 */
	private String master;
	/** 部门负责人联系电话 */
	private String masterPhone;
	/** 部门负责人邮箱*/
	private String masterEmail;
	/** 项目负责 */
	private String name;
	/** 项目负责联系电话 */
	private String phone;
	/** 项目负责人邮箱*/
	private String email;
	/** 通讯录类型*/
	private Type type;
	/** 用户*/
	private User user;
	
	/**
	 * 项目类型 
	 */
	public enum Type {
		/** 业主法人*/
		OWN_MASTER,
		/** 设计 */
		DESIGN,
		/** 施工 */
		CON,
		/** 监理 */
		SUP,
		/** 质检 */
		QC,
		/** 其他 */
		OTHER,
		/** 业主负责*/
		OWN_NAME;
	}

	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getQualify() {
		return qualify;
	}
	public void setQualify(String qualify) {
		this.qualify = qualify;
	}
	public String getDepart() {
		return depart;
	}
	public void setDepart(String depart) {
		this.depart = depart;
	}
	public String getMaster() {
		return master;
	}
	public void setMaster(String master) {
		this.master = master;
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
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	@ManyToOne(fetch=FetchType.EAGER)
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}

	public String getMasterPhone() {
		return masterPhone;
	}
	public void setMasterPhone(String masterPhone) {
		this.masterPhone = masterPhone;
	}
	public String getMasterEmail() {
		return masterEmail;
	}
	public void setMasterEmail(String masterEmail) {
		this.masterEmail = masterEmail;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	
}
