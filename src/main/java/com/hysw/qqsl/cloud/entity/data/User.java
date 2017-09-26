package com.hysw.qqsl.cloud.entity.data;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 用户实体类
 *
 * @since 2015年8月11日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
@Entity
@Table(name="user")
@SequenceGenerator(name="sequenceGenerator", sequenceName="user_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class User extends BaseEntity {

	private static final long serialVersionUID = 148187703824551916L;
	
	/** 真实姓名 */
	private String name;
	/** 登陆名 */
	private String userName;
	/** 密码 */
	private String password;
	/** 联系电话 */
	private String phone;
	/** 邮箱 */
	private String email;
	/** 所属单位 */
	private String department;
	/** 是否锁定 */
	private Boolean isLocked;
	/** 锁定日期 */
	private Date lockedDate;
	/** 最后登录日期 */
	private Date loginDate;
	/** 最后登录IP */
	private String loginIp;
	/** 连续登录失败次数 */
	private Integer loginFailureCount;
	/** 用户头像字符串*/
	private String avatar;
	/** 用户类型 */
	private Type type;
	/** 前缀和编号json*/
	private String prefixOrderStr;
	/** 联系人*/
	private List<Contact> contacts=new ArrayList<Contact>();
	/** 用户消息  */
	private List<UserMessage> userMessages = new ArrayList<UserMessage>();
	/** 用户消息（仅对应前台）*/
	private List<UserMessage> userMessageses=new ArrayList<UserMessage>();
	/** 用户角色 */
	private String roles;
	/** 登录类型　*/
	private String loginType;
	/** 项目列表 */
	private List<Project> projects = new ArrayList<Project>();
	/** 仪表 */
	private  List<Sensor> sensors = new ArrayList<Sensor>();
	/** 子账号 */
	private List<Account> accounts = new ArrayList<>();

	public enum Type {
		USER, ADMIN
	}


	public User(){

	}
	public User(String userName,String name,Type type,String password,String phone,String email){
		this.userName = userName;
		this.name = name;
		this.type = type;
		this.password = password;
		this.email = email;
		this.phone = phone;
	}
	@NotEmpty
	@Length(max=255)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@NotEmpty
	@Length(max=255)
	@JsonIgnore
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	@NotEmpty
	@Length(max=255)
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Boolean getLocked() {
		return isLocked;
	}

	public void setLocked(Boolean locked) {
		isLocked = locked;
	}

	public Date getLockedDate() {
		return lockedDate;
	}

	public void setLockedDate(Date lockedDate) {
		this.lockedDate = lockedDate;
	}

	public Date getLoginDate() {
		return loginDate;
	}

	public void setLoginDate(Date loginDate) {
		this.loginDate = loginDate;
	}

	public String getLoginIp() {
		return loginIp;
	}

	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}
	public Integer getLoginFailureCount() {
		return loginFailureCount;
	}

	public void setLoginFailureCount(Integer loginFailureCount) {
		this.loginFailureCount = loginFailureCount;
	}
	//@Lob
	//@Basic(fetch = FetchType.LAZY)
	//@Column(columnDefinition = "text")
	@Column(length = 10000)
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	@OneToMany(mappedBy="user", fetch=FetchType.LAZY , cascade={CascadeType.PERSIST})
	@JsonIgnore
	public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getPrefixOrderStr() {
		return prefixOrderStr;
	}

	public void setPrefixOrderStr(String prefixOrderStr) {
		this.prefixOrderStr = prefixOrderStr;
	}

	@Transient
	public String getLoginType() {
		return loginType;
	}
	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}
	@OneToMany(cascade={CascadeType.PERSIST,CascadeType.REMOVE},  fetch=FetchType.LAZY, mappedBy="user")
	@JsonIgnore
	public List<Contact> getContacts() {
		return contacts;
	}

	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}

	@OneToMany(cascade={CascadeType.PERSIST,CascadeType.REMOVE},  fetch=FetchType.LAZY, mappedBy="user")
    @JsonIgnore
	public List<UserMessage> getUserMessages() {
		return userMessages;
	}
	public void setUserMessages(List<UserMessage> userMessages) {
		this.userMessages = userMessages;
	}
	@Transient
	public List<UserMessage> getUserMessageses() {
		return userMessageses;
	}
	public void setUserMessageses(List<UserMessage> userMessageses) {
		this.userMessageses = userMessageses;
	}
	public String getRoles() {
		return roles;
	}
	public void setRoles(String roles) {
		this.roles = roles;
	}

	@Transient
	public List<Sensor> getSensors() {
		return sensors;
	}

	public void setSensors(List<Sensor> sensors) {
		this.sensors = sensors;
	}

	@ManyToMany(mappedBy="users")
	@JsonIgnore
	public List<Account> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<Account> accounts) {
		this.accounts = accounts;
	}
}
