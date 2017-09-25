package com.hysw.qqsl.cloud.core.entity.element;

import java.io.Serializable;
import java.util.List;

/**
 * 项目信息抽象类
 *
 * @since 2015年8月14日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public  class  Info implements Serializable {

	private static final long serialVersionUID = 2620250480957795707L;

	/** 项目信息名称  */
	private String name;
	/** 序号 */
	private int order;
	/** 项目信息选择值列表  */
	private List<String> selectValues;
	
	public Info(String name, List<String>selectValues ) {
		this.name = name;
		this.selectValues = selectValues;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public List<String> getSelectValues() {
		return selectValues;
	}

	public void setSelectValues(List<String> selectValues) {
		this.selectValues = selectValues;
	}
	
		
}
