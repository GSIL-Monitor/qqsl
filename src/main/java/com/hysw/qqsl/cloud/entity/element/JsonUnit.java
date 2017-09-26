package com.hysw.qqsl.cloud.entity.element;

import java.util.ArrayList;
import java.util.List;

/*
 * 用于前台要素输出的要素对象 
 *
 * @since 2015年10月19日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public class JsonUnit {

	private Long projectId;
	private List<JsonElement> jsonElements = new ArrayList<JsonElement>();

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}
	
	public List<JsonElement> getJsonElements() {
		return jsonElements;
	}


}

