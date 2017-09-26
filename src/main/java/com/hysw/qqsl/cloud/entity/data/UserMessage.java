package com.hysw.qqsl.cloud.entity.data;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.*;

@Entity
@Table(name = "userMessage")
@SequenceGenerator(name = "sequenceGenerator", sequenceName = "userMessage_sequence")
@JsonIgnoreProperties(value = { "hibernateLazyInitializer" })
public class UserMessage extends BaseEntity {

	private static final long serialVersionUID = -3452664373859421059L;

	/** 用户 */
	private User user;
	/** 项目id */
	private String projectId;
	/** 内容 */
	private String content;
	/** 状态 */
	private Status status;
	/** 权限标记*/
	private Sign sign;
	/** 子账号id */
	private Long accountId;
	/** 仪表id */
	private String sensorId;
	public enum Sign{
		/** 失去权限*/
		MISS,
		/** 有权限*/
		GIVEN
	}
	
	public enum Status {
		UNREAD,
		READED
	}

	@ManyToOne(fetch=FetchType.EAGER)
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	//@Lob
	@Basic(fetch = FetchType.EAGER)
	@Column(columnDefinition = "text")
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/*public VisitType getVisitType() {
		return visitType;
	}

	public void setVisitType(VisitType visitType) {
		this.visitType = visitType;
	}*/

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public String getSensorId() {
		return sensorId;
	}

	public void setSensorId(String sensorId) {
		this.sensorId = sensorId;
	}

	@Transient
	public Sign getSign() {
		return sign;
	}

	public void setSign(Sign sign) {
		this.sign = sign;
	}
		
}
