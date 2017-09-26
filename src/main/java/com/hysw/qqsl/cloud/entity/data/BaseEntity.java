package com.hysw.qqsl.cloud.entity.data;

import com.hysw.qqsl.cloud.listener.EntityListener;
import org.hibernate.search.annotations.*;
import org.hibernate.search.annotations.Index;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity - 基类
 *
 * @since 2015年8月11日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
@EntityListeners(EntityListener.class)
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

	private static final long serialVersionUID = -9100968677794664568L;

	/** ID */
	private Long id;

	/** 创建日期 */
	private Date createDate;

	/** 修改日期 */
	private Date modifyDate;
	
	/**
	 * 获取ID
	 * 
	 * @return ID
	 */
	@DocumentId
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "sequenceGenerator")
	public Long getId() {
		return id;
	}

	/**
	 * 设置ID
	 * 
	 * @param id
	 *            ID
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * 获取创建日期
	 * 
	 * @return 创建日期
	 */
	@org.codehaus.jackson.annotate.JsonProperty
	@Field(store = Store.YES, index = Index.YES)
	@DateBridge(resolution = Resolution.SECOND)
	@Column(nullable = false, updatable = false)
	public Date getCreateDate() {
		return createDate;
	}

	/**
	 * 设置创建日期
	 * 
	 * @param createDate
	 *            创建日期
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	/**
	 * 获取修改日期
	 * 
	 * @return 修改日期
	 */
	@Field(store = Store.YES, index = Index.YES)
	@DateBridge(resolution = Resolution.SECOND)
	@Column(nullable = false)
	public Date getModifyDate() {
		return modifyDate;
	}

	/**
	 * 设置修改日期
	 * 
	 * @param modifyDate
	 *            修改日期
	 */
	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

}