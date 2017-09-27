package com.hysw.qqsl.cloud.core.entity;

import java.io.Serializable;

/**
 * 筛选
 *
 * @since 2015年8月13日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public class Filter implements Serializable {

	private static final long serialVersionUID = 2906732159238967230L;

	/**
	 * 运算符
	 */
	public enum Operator {
		/** 等于 */
		eq,
		/** 不等于 */
		ne,
		/** 大于 */
		gt,
		/** 小于 */
		lt,
		/** 大于等于 */
		ge,
		/** 小于等于 */
		le,
		/** 相似 */
		like,
		/** 包含 */
		in,
		/** 为空 */
		isNull,
		/** 不为空 */
		isNotNull,
		/** 两者之间*/
		between,
		/** 倒序 */
		desc;

		/**
		 * 根据String得到Operator
		 * 
		 * @param value
		 *            值
		 * @return String对应的Operator
		 */
		public static Operator fromString(String value) {
			return Operator.valueOf(value.toLowerCase());
		}
	}

	/** 属性 */
	private String property;
	/** 运算符 */
	private Operator operator;
	/** 值 */
	private Object value;
	/** 值 */
	private Object value1;

	/**
	 * 新建Filter
	 * 
	 * @param property
	 *            属性
	 * @param operator
	 *            运算符
	 * @param value
	 *            值
	 */
	public Filter(String property, Operator operator, Object value,Object value1) {
		this.property = property;
		this.operator = operator;
		this.value = value;
		this.value1=value1;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getValue1() {
		return value1;
	}

	public void setValue1(Object value1) {
		this.value1 = value1;
	}

	/**
	 * 建立等于筛选
	 * 
	 * @param property
	 *            属性
	 * @param value
	 *            值
	 * @return 等于筛选
	 */
	public static Filter eq(String property, Object value) {
		return new Filter(property, Operator.eq, value,null);
	}

	/**
	 * 建立不等于筛选
	 * 
	 * @param property
	 *            属性
	 * @param value
	 *            值
	 * @return 不等于筛选
	 */
	public static Filter ne(String property, Object value) {
		return new Filter(property, Operator.ne, value,null);
	}
	
	/**
	 * 建立大于筛选
	 * 
	 * @param property
	 *            属性
	 * @param value
	 *            值
	 * @return 大于筛选
	 */
	public static Filter gt(String property, Object value) {
		return new Filter(property, Operator.gt, value,null);
	}	
	
	/**
	 * 建立小于筛选
	 * 
	 * @param property
	 *            属性
	 * @param value
	 *            值
	 * @return 小于筛选
	 */
	public static Filter lt(String property, Object value) {
		return new Filter(property, Operator.lt, value,null);
	}	
	
	/**
	 * 建立大于等于筛选
	 * 
	 * @param property
	 *            属性
	 * @param value
	 *            值
	 * @return 大于等于筛选
	 */
	public static Filter ge(String property, Object value) {
		return new Filter(property, Operator.ge, value,null);
	}
	
	/**
	 * 建立小于等于筛选
	 * 
	 * @param property
	 *            属性
	 * @param value
	 *            值
	 * @return 小于等于筛选
	 */
	public static Filter le(String property, Object value) {
		return new Filter(property, Operator.le, value,null);
	}		
	
	/**
	 * 建立相似筛选
	 * 
	 * @param property
	 *            属性
	 * @param value
	 *            值
	 * @return 相似筛选
	 */
	public static Filter like(String property, Object value) {
		return new Filter(property, Operator.like, value,null);
	}	
	
	/**
	 * 建立包含筛选
	 * 
	 * @param property
	 *            属性
	 * @param value
	 *            值
	 * @return 包含筛选
	 */
	public static Filter in(String property, Object value) {
		return new Filter(property, Operator.in, value,null);
	}
	
	/**
	 * 建立为Null筛选
	 * 
	 * @param property
	 *            属性
	 * @return 为Null筛选
	 */
	public static Filter isNull(String property) {
		return new Filter(property, Operator.isNull, null,null);
	}	
	
	/**
	 * 返回不为Null筛选
	 * 
	 * @param property
	 *            属性
	 * @return 不为Null筛选
	 */
	public static Filter isNotNull(String property) {
		return new Filter(property, Operator.isNotNull, null,null);
	}	
	
	/**
	 * 建立两者之间筛选
	 * 
	 * @param property
	 * 				属性
	 * @param value
	 * 				值1
	 * @param value1
	 * 				值2
	 * @return  值1与值2之间筛选
	 */
	public static Filter between(String property,Object value,Object value1) {
		return new Filter(property, Operator.between, value,value1);
	}

	/**
	 * 倒序查询第一条数据
	 * @param property
	 * @return
	 */
	public static Filter desc(String property) {
		return new Filter(property, Operator.desc, null, null);
	}

}
