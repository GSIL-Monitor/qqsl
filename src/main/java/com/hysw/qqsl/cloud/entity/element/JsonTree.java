package com.hysw.qqsl.cloud.entity.element;

import java.util.List;

/**
 * json树型格式对象
 * 
 * @since 2015年9月7日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public class JsonTree {

	/** 节点id */
	private String id;
	/** 父节点id */
	private String pId;
	/** 节点名称 */
	private String name;
	/** 是否展开 */
	private String open;
	/** 节点类型(top/child) */
	private String type;
	/** top id (projectId/planningId) */
	private String topId;
	/** 要素列表 */
	private List<JsonElement> elements;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getpId() {
		return pId;
	}

	public void setpId(String pId) {
		this.pId = pId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOpen() {
		return open;
	}

	public void setOpen(String open) {
		this.open = open;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTopId() {
		return topId;
	}

	public void setTopId(String topId) {
		this.topId = topId;
	}

	public List<JsonElement> getElements() {
		return elements;
	}

	public void setElements(List<JsonElement> elements) {
		this.elements = elements;
	}
}
