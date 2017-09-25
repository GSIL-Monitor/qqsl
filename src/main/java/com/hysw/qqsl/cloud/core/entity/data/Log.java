package com.hysw.qqsl.cloud.core.entity.data;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.*;

@Entity
@Table(name = "log")
@SequenceGenerator(name = "sequenceGenerator", sequenceName = "log_sequence")
@JsonIgnoreProperties(value = { "hibernateLazyInitializer" })
public class Log extends BaseEntity {

	private static final long serialVersionUID = 6991675772429724989L;

	/** 内容 */
	private String content;
	/** 用户id */
	private Long userId;
	/** 子账号id */
	private Long accountId;
	/** 项目id */
	private Long projectId;
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	
	
}
