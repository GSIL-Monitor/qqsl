package com.hysw.qqsl.cloud.core.entity;

import java.io.InputStream;

/**
 * pdf转换对象
 *
 * @since 2015年7月30日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public class ConverterObject {

	private String key;
	private InputStream inputStream;
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}
	
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

}
