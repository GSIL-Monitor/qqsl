package com.hysw.qqsl.cloud.entity.element;

import com.hysw.qqsl.cloud.entity.ObjectFile;
import com.hysw.qqsl.cloud.entity.data.Contact;
import com.hysw.qqsl.cloud.entity.data.Project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 单元模版实体类
 * @author Administrator
 *
 */
public class Unit implements Serializable {

	private static final long serialVersionUID = 1930204178536994053L;
	/**名称*/
	private String name;
	/** 树路径*/
	private String treePath;
	/**层级*/
	private int grade;
	/**别名*/
	private String alias;
	/**含有多个复合要素模版*/
	private String aliases; 
	/** 下级分类 */
	private List<Unit> unitChildrens = new ArrayList<Unit>();
	/** 上级分类 */
	private Unit unitParent;
	/** 类型 */
	private Type type;
	/** 复合要素列表 */
	private List<ElementGroup> elementGroups = new ArrayList<ElementGroup>();
	/** 文件列表 */
	private List<ObjectFile> objectFiles = new ArrayList<ObjectFile>();
	/** 通讯录 */
	private List<Contact> Contacts=new ArrayList<Contact>();
	/** 项目 */
	private Project project;
	/**
	 * 单元类型
	 */
	public enum Type {
		/** 菜单 */
		MENU,
		/** 文件夹 */
		DIRCTORY;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTreePath() {
		return treePath;
	}
	public void setTreePath(String treePath) {
		this.treePath = treePath;
	}
	public int getGrade() {
		return grade;
	}
	public void setGrade(int grade) {
		this.grade = grade;
	}

	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getAliases() {
		return aliases;
	}
	public void setAliases(String aliases) {
		this.aliases = aliases;
	}
	public List<Unit> getUnitChildrens() {
		return unitChildrens;
	}
	public void setUnitChildrens(List<Unit> unitChildrens) {
		this.unitChildrens = unitChildrens;
	}
	public Unit getUnitParent() {
		return unitParent;
	}
	public void setUnitParent(Unit unitParent) {
		this.unitParent = unitParent;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public List<ElementGroup> getElementGroups() {
		return elementGroups;
	}
	public void setElementGroups(List<ElementGroup> elementGroups) {
		this.elementGroups = elementGroups;
	}
	public List<ObjectFile> getObjectFiles() {
		return objectFiles;
	}
	public void setObjectFiles(List<ObjectFile> objectFiles) {
		this.objectFiles = objectFiles;
	}
	public List<Contact> getContacts() {
		return Contacts;
	}
	public void setContacts(List<Contact> contacts) {
		Contacts = contacts;
	}
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	

}
