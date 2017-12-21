package com.hysw.qqsl.cloud.core.entity.element;

/**
 * 用于前台要素输出的要素对象  
 *
 * @since 2015年10月19日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public class JsonElement {
	private String name;
	private String value;
	
	public JsonElement(String name) {
		this.name = name;
	}
	
	public JsonElement(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}