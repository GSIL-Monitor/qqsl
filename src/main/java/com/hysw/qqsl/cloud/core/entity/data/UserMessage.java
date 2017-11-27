package com.hysw.qqsl.cloud.core.entity.data;

import com.hysw.qqsl.cloud.CommonEnum;
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
	/** 内容 */
	private String content;
	/** 状态 */
	private CommonEnum.MessageStatus status;
	/** 类型 */
	private Type type;

	/**
	 * 类型
	 */
	public enum Type {
		// 购买套餐，包含购买，续费，升级
		BUY_PACKAGE,
		// 购买测站，包含购买，续费
		BUY_STATION,
		// 分享项目
		SHARE_PROJECT,
		// 分享测站
		SHARE_STATION,
		// 子账号解绑企业
		INVITE__ACCOUNT,
		// 认证
		CERTIFY
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

	public CommonEnum.MessageStatus getStatus() {
		return status;
	}

	public void setStatus(CommonEnum.MessageStatus status) {
		this.status = status;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
