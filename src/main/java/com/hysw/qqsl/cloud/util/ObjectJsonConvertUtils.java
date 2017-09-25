package com.hysw.qqsl.cloud.util;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.element.*;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.Project.Type;
import com.hysw.qqsl.cloud.core.entity.element.Element;
import com.hysw.qqsl.cloud.core.service.ElementGroupService;
import com.hysw.qqsl.cloud.core.service.UnitService;
import net.sf.ehcache.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 单元转换为json串工具
 *
 * @since 2015年9月7日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
@Component("objectJsonConvertUtils")
public class ObjectJsonConvertUtils {
	@Autowired
	private CacheManager cacheManager;
	@Autowired
	private UnitService unitService;
	@Autowired
	private ElementGroupService elementGroupService;

	/**
	 * 单元转换为json
	 * 
	 * @param units
	 *            单元
	 * @return json列表
	 */
	public List<JsonTree> getJsons(Project project, List<Unit> units) {
		List<JsonTree> jsonList = new ArrayList<JsonTree>();
		String[] isOpenStr = {"23","24","25"};
		List<String> isOpens = Arrays.asList(isOpenStr);
		JsonTree jsonTree ;
		// 构建顶层节点
		jsonTree = new JsonTree();
		// 节点id(顶层id)
		jsonTree.setId(CommonAttributes.TOP_TREE_ID);
		// 父节点id
		jsonTree.setpId("0");
		// 节点名称
		jsonTree.setName(project.getCode() + "(" + project.getName() + ")");
		// 是否展开
		jsonTree.setOpen("true");
		// 节点类型
		jsonTree.setType("top");
		// 项目id
		jsonTree.setTopId(project.getId().toString());
		jsonList.add(jsonTree);
		for (Unit unit : units) {
			jsonTree = new JsonTree();
			// 节点id
			jsonTree.setId(unit.getAlias());
			// 父节点id
			if (null != unit.getUnitParent()) {
				jsonTree.setpId(unit.getUnitParent().getAlias());
				if(isOpens.contains(unit.getAlias())){
					// 展开
					jsonTree.setOpen("true");
				}
			} else {
				// 次级层
				jsonTree.setpId(CommonAttributes.TOP_TREE_ID);
				// 展开
				jsonTree.setOpen("true");
			}
			// 节点名称
			jsonTree.setName(unit.getName());
			// 节点类型
			jsonTree.setType("child");
			jsonList.add(jsonTree);
		}
		return jsonList;
	}

	/**
	 * 单元转换为json，用于要素输出模版
	 * 
	 * @param
	 *
	 * @return json列表
	 */
	public List<JsonTree> getTemplateJsons(Project project,List<Unit> units) {
		List<JsonTree> jsonList = new ArrayList<JsonTree>();
		JsonTree jsonTree;
		// 构建顶层节点
		jsonTree = new JsonTree();
		// 节点id(顶层id)
		jsonTree.setId(CommonAttributes.TOP_TREE_ID);
		// 父节点id
		jsonTree.setpId("0");
		// 节点名称
		jsonTree.setName(getName(project.getType()));
		// 是否展开
		jsonTree.setOpen("true");
		// 节点类型
		jsonTree.setType("top");
		// 项目id
//		jsonTree.setTopId(project.getId().toString());
		jsonList.add(jsonTree);
		List<JsonElement> elements;
		for (Unit unit : units) {
			jsonTree = new JsonTree();
			// 节点id
			jsonTree.setId(unit.getAlias().toString());
			// 父节点id
			if (null != unit.getUnitParent()) {
				jsonTree.setpId(unit.getUnitParent().getAlias().toString());
			} else {
				// 次级层
				jsonTree.setpId(CommonAttributes.TOP_TREE_ID);
				// 展开
				jsonTree.setOpen("true");
			}
			elements = new ArrayList<JsonElement>();
			if (unit.getElementGroups().size() > 0) {
				for (ElementGroup elementGroup : unit.getElementGroups()) {
					for (Element element : elementGroup.getElements()) {
						if(!element.getType().equals(Element.Type.FILE_UPLOAD)&&!element.getType().equals(Element.Type.COORDINATE_UPLOAD)){
							if(element.getElementParent()==null){
								elements.add(new JsonElement(element.getName()));				
							}
						}
					}
				}
			}
			jsonTree.setElements(elements);
			// 节点名称
			jsonTree.setName(unit.getName());
			// 节点类型
			jsonTree.setType("child");
			jsonList.add(jsonTree);
		}
		return jsonList;
	}

	/**
	 * 设置项目类型
	 * 
	 * @param
	 * @param
	 */
	public String getName(Type type) {
		if (type.equals(Project.Type.DRINGING_WATER)) {
			return "人畜饮水工程";
		} else if (type.equals(Project.Type.AGRICULTURAL_IRRIGATION))  {
			return "灌溉工程";
		} else if (type.equals(Project.Type.FLOOD_DEFENCES))  {
			return "防洪减灾工程";
		} else if (type.equals(Project.Type.CONSERVATION))  {
			return "水土保持工程";
		} else if (type.equals(Project.Type.HYDROPOWER_ENGINEERING))  {
			return "农村小水电工程";
		} else if (type.equals(Project.Type.WATER_SUPPLY))  {
			return "供水保障工程";
		} else {
			return "人畜饮水工程";
		}
	}
	/**
	 * 获取各个项目类型的要素输出模版json
	 * @param project
	 * @return
	 */
	public List<JsonTree> getTemplateJsonTree(Project project){
		List<JsonTree> jsonTree = null;
		if(project.getType().equals(Project.Type.AGRICULTURAL_IRRIGATION)){
			jsonTree = getAgrJsonTree();
		}
		if(project.getType().equals(Project.Type.CONSERVATION)){
			jsonTree = getConJsonTree();
		}
		if(project.getType().equals(Project.Type.DRINGING_WATER)){
			jsonTree = getDriJsonTree();
		}
		if(project.getType().equals(Project.Type.FLOOD_DEFENCES)){
			jsonTree = getFloJsonTree();
		}
		if(project.getType().equals(Project.Type.HYDROPOWER_ENGINEERING)){
			jsonTree = getHydJsonTree();
		}
		if(project.getType().equals(Project.Type.WATER_SUPPLY)){
			jsonTree = getWatJsonTree();
		}
		return jsonTree;
	}

	/**
	 * @return the conJsonTree
	 */
	public List<JsonTree> getConJsonTree() {
		Cache cache = cacheManager.getCache("jsonTreeCache");
		net.sf.ehcache.Element element = cache.get("con");
		if(element==null){
			List<Unit> units = unitService.makeElementGroupModel(unitService.getConUnitModels(), elementGroupService
					.getConElementGroups());
			Project project = new Project();
			project.setType(Type.CONSERVATION);
			element = new net.sf.ehcache.Element("con",getTemplateJsons(project,units));
			cache.put(element);
		}
		return (List<JsonTree>) element.getValue();
	}

	public List<JsonTree> getAgrJsonTree() {
		Cache cache = cacheManager.getCache("jsonTreeCache");
		net.sf.ehcache.Element element = cache.get("agr");
		if(element==null){
			List<Unit> units = unitService.makeElementGroupModel(unitService.getAgrUnitModels(), elementGroupService
					.getAgrElementGroups());
			Project project = new Project();
			project.setType(Type.AGRICULTURAL_IRRIGATION);
			element = new net.sf.ehcache.Element("agr",getTemplateJsons(project,units));
			cache.put(element);
		}
		return (List<JsonTree>) element.getValue();
	}

	public List<JsonTree> getDriJsonTree() {
		Cache cache = cacheManager.getCache("jsonTreeCache");
		net.sf.ehcache.Element element = cache.get("dri");
		if(element==null){
			List<Unit> units = unitService.makeElementGroupModel(unitService.getDriUnitModels(), elementGroupService
					.getDriElementGroups());
			Project project = new Project();
			project.setType(Type.DRINGING_WATER);
			element = new net.sf.ehcache.Element("dri",getTemplateJsons(project,units));
			cache.put(element);
		}
		return (List<JsonTree>) element.getValue();
	}

	public List<JsonTree> getHydJsonTree() {
		Cache cache = cacheManager.getCache("jsonTreeCache");
		net.sf.ehcache.Element element = cache.get("hyd");
		if(element==null){
			List<Unit> units = unitService.makeElementGroupModel(unitService.getHydUnitModels(), elementGroupService
					.getHydElementGroups());
			Project project = new Project();
			project.setType(Type.HYDROPOWER_ENGINEERING);
			element = new net.sf.ehcache.Element("hyd",getTemplateJsons(project,units));
			cache.put(element);
		}
		return (List<JsonTree>) element.getValue();
	}

	public List<JsonTree> getFloJsonTree() {
		Cache cache = cacheManager.getCache("jsonTreeCache");
		net.sf.ehcache.Element element = cache.get("flo");
		if(element==null){
			List<Unit> units = unitService.makeElementGroupModel(unitService.getFloUnitModels(), elementGroupService
					.getFloElementGroups());
			Project project = new Project();
			project.setType(Type.FLOOD_DEFENCES);
			element = new net.sf.ehcache.Element("flo",getTemplateJsons(project,units));
			cache.put(element);
		}
		return (List<JsonTree>) element.getValue();
	}

	public List<JsonTree> getWatJsonTree() {
		Cache cache = cacheManager.getCache("jsonTreeCache");
		net.sf.ehcache.Element element = cache.get("wat");
		if(element==null){
			List<Unit> units = unitService.makeElementGroupModel(unitService.getWatUnitModels(), elementGroupService
					.getWatElementGroups());
			Project project = new Project();
			project.setType(Type.WATER_SUPPLY);
			element = new net.sf.ehcache.Element("wat",getTemplateJsons(project,units));
			cache.put(element);
		}
		return (List<JsonTree>) element.getValue();
	}

}
