package com.hysw.qqsl.cloud.listener;

import com.hysw.qqsl.cloud.core.entity.data.BaseEntity;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

/**
 * Listener - 创建日期、修改日期处理
 *
 * @since 2015年8月11日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public class EntityListener {

	/**
	 * 保存前处理
	 * 
	 * @param entity
	 *            基类
	 */
	@PrePersist
	public void perPersist(BaseEntity entity) {
		entity.setCreateDate(new Date());
		entity.setModifyDate(new Date());
	}
	
	/**
	 * 更新前处理
	 * 
	 * @param entity
	 *            基类
	 */
	@PreUpdate
	public void preUpdate(BaseEntity entity) {
		entity.setModifyDate(new Date());
	}	
}
