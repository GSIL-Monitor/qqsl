package com.hysw.qqsl.cloud.entity.element;

import com.hysw.qqsl.cloud.entity.data.Project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 复合要素模版实体类
 * @author Administrator
 *
 */
public class ElementGroup implements Serializable {

	private static final long serialVersionUID = -8696230708067222085L;

	/**单元模版*/
	private Unit unit;
	/**复合要素名称*/
    private String name;
	/** 别名 */
	private String alias;
	/** 要素模版列表 */
	private List<Element> elements = new ArrayList<Element>();
	/** 项目 */
	private Project project;
	/** 操作类型*/
	private Action action;
	
	/**
	 *  操作类型
	 */
	public enum Action{
		/** 编辑*/
		EDIT,
		/** 查看*/
	    VIEW;
	}
	public Action getAction() {
		return action;
	}
	public void setAction(Action action) {
		this.action = action;
	}
	public Unit getUnit() {
		return unit;
	}
	public void setUnit(Unit unit) {
		this.unit = unit;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias= alias;
	}
	public List<Element> getElements() {
		return elements;
	}
	public void setElements(List<Element> elements) {
		this.elements = elements;
	}
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
}
