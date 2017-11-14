package com.hysw.qqsl.cloud.core.entity.data;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
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
    /** 公司名称 */
    private String companyName;
	/** 登陆名(昵称) */
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
	/** 认证状态 */
	private CommonEnum.CertifyStatus personalStatus;
	private CommonEnum.CertifyStatus companyStatus;
	/** 项目列表 */
	private List<Project> projects = new ArrayList<Project>();
	/** 子账号 */
	private List<Account> accounts = new ArrayList<>();
	/** 订单 */
	private List<Trade> trades=new ArrayList<>();
	/** 服务 */
	private List<Package> packages=new ArrayList<>();
	/** 服务认证 */
	private List<Certify> certifies=new ArrayList<>();
	/** 站点 */
	private List<Station> stations=new ArrayList<>();

	public User(){
		this.personalStatus = CommonEnum.CertifyStatus.UNAUTHEN;
		this.companyStatus = CommonEnum.CertifyStatus.UNAUTHEN;
	}

    @Transient
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
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
//	@Lob
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

	@ManyToMany(mappedBy="users")
	@JsonIgnore
	public List<Account> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<Account> accounts) {
		this.accounts = accounts;
	}

	@OneToMany(mappedBy="user", fetch=FetchType.LAZY , cascade={CascadeType.PERSIST})
	@JsonIgnore
	public List<Trade> getTrades() {
		return trades;
	}

	public void setTrades(List<Trade> trades) {
		this.trades = trades;
	}

	@OneToMany(mappedBy="user", fetch=FetchType.LAZY , cascade={CascadeType.PERSIST})
	@JsonIgnore
	public List<Package> getPackages() {
		return packages;
	}

	public void setPackages(List<Package> packages) {
		this.packages = packages;
	}

	@OneToMany(mappedBy="user", fetch=FetchType.LAZY , cascade={CascadeType.PERSIST})
	@JsonIgnore
	public List<Certify> getCertifies() {
		return certifies;
	}

	public void setCertifies(List<Certify> certifies) {
		this.certifies = certifies;
	}

	@OneToMany(mappedBy="user", fetch=FetchType.LAZY , cascade={CascadeType.PERSIST})
	@JsonIgnore
	public List<Station> getStations() {
		return stations;
	}

	public void setStations(List<Station> stations) {
		this.stations = stations;
	}

	@Transient
	public CommonEnum.CertifyStatus getPersonalStatus() {
		return personalStatus;
	}

	public void setPersonalStatus(CommonEnum.CertifyStatus personalStatus) {
		this.personalStatus = personalStatus;
	}

	@Transient
	public CommonEnum.CertifyStatus getCompanyStatus() {
		return companyStatus;
	}

	public void setCompanyStatus(CommonEnum.CertifyStatus companyStatus) {
		this.companyStatus = companyStatus;
	}

}
