package com.hysw.qqsl.cloud.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.element.ElementGroup;
import com.hysw.qqsl.cloud.core.entity.ObjectFile;
import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.core.entity.element.Unit;
import com.hysw.qqsl.cloud.core.entity.XMLFileException;
import com.hysw.qqsl.cloud.core.entity.data.Contact;
import com.hysw.qqsl.cloud.core.entity.data.ElementDB;
import com.hysw.qqsl.cloud.core.entity.data.ElementDataGroup;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.Project.Type;
import com.hysw.qqsl.cloud.util.SettingUtils;

/**
 * 建立单元模版缓存
 * 
 * @author leinuo
 *
 * @date 2016年1月12日
 */
@Service("unitService")
public class UnitService {
	Log logger = LogFactory.getLog(getClass());
	@Autowired
	private ElementGroupService elementGroupService;
	@Autowired
	private ElementService elementService;
	@Autowired
	private ElementDBService elementDBService;
	/** 层级 */
	int grade;
	@Autowired
	private CacheManager cacheManager;
	Setting setting = SettingUtils.getInstance().getSetting();


	/**
	 * 用于测试刷新是否成功
	 * 
	 * @return
	 */
	public List<Unit> getAgrModel() {
		return this.getAgrUnitModels();
	}
	public List<Unit> getAgr() {
		return this.getAgrUnits();
	}

	/**
	 * 连接字符串
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	private String concate(String str1, String str2) {
		return str1 + "/" + str2;
	}

	private List<Unit> getUnits(String XMLName) {
		List<Unit> units = null;
		try {
			units = readProjectModelXML(XMLName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return units;
	}

	/**
	 * 读取单元模版xml，所有项目都只有这一份模版
	 * 
	 * @param XMLName
	 * @return
	 * @throws XMLFileException
	 */
	public List<Unit> readProjectModelXML(String XMLName)
			throws XMLFileException, DocumentException {
		List<String> unitAliass = new ArrayList<String>();
		List<Unit> units = new ArrayList<Unit>();
		Element element;
		List<String> aliass = Arrays.asList(CommonAttributes.ALIAS.split(","));
		element = SettingUtils.getInstance().getRootElement(XMLName);
		Iterator<Element> it = element.elementIterator();
		Unit unit0;
		Element child;
		while (it.hasNext()) {
			this.grade = 0;
			unit0 = new Unit();
			child = it.next();
			unit0.setName(child.attributeValue("name"));
			unit0.setAlias(child.attributeValue("alias"));
			unit0.setAliases(child.attributeValue("aliases"));
			unit0.setGrade(grade);
			unit0.setUnitParent(null);
			unit0.setTreePath(unit0.getAlias());
			units.add(unit0);
			checkUnitsAlias(unitAliass, unit0, XMLName, aliass);
			makeUnit(child, grade + 1, unit0, units, unitAliass, XMLName,
					aliass);
		}
		for (int i = 0; i < units.size(); i++) {
			// 判断单元类型
			if (units.get(i).getUnitChildrens().size() > 0) {
				units.get(i).setType(Unit.Type.MENU);
			} else {
				units.get(i).setType(Unit.Type.DIRCTORY);
			}
		}
		checkAliasesAndName(units, XMLName);
		// logger.info("unitAliass:"+unitAliass);
		return units;

	}

	private void makeUnit(Element child, int grade, Unit unit0,
			List<Unit> units, List<String> unitAliass, String xmlPath,
			List<String> aliass) throws XMLFileException {
		Iterator<Element> it1 = child.elementIterator();
		this.grade = grade;
		Unit unit1;
		Element child1;
		while (it1.hasNext()) {
			unit1 = new Unit();
			child1 = it1.next();
			unit1.setName(child1.attributeValue("name"));
			unit1.setAlias(child1.attributeValue("alias"));
			unit1.setAliases(child1.attributeValue("aliases"));
			unit1.setGrade(grade);
			unit1.setUnitParent(unit0);
			unit1.setTreePath(concate(unit0.getTreePath(), unit1.getAlias()));
			unit0.getUnitChildrens().add(unit1);
			units.add(unit1);
			checkUnitsAlias(unitAliass, unit1, xmlPath, aliass);
			makeUnit(child1, grade + 1, unit1, units, unitAliass, xmlPath,
					aliass);
		}
	}

	/**
	 * 检查单元下复合要素的别名和单元名
	 * 
	 * @param units
	 * @param xmlPath
	 * @throws XMLFileException
	 */
	private void checkAliasesAndName(List<Unit> units, String xmlPath)
			throws XMLFileException {
		List<String> aliseseList = Arrays.asList(CommonAttributes.ALIASES
				.split(";"));
		List<String> unitNames = Arrays.asList(CommonAttributes.UNIT_NAME
				.split(";"));
		for (Unit unit : units) {
			if (unit.getAliases() != null
					&& !aliseseList.contains(unit.getAliases())) {
				logger.info(unit.getName() + ":" + unit.getAlias() + ":"
						+ unit.getAliases() + ":" + xmlPath);
				throw new XMLFileException("复合要素别名未知！");
			}
			if (!unitNames.contains(unit.getName())) {
				logger.info(unit.getName() + ":" + unit.getAlias() + ":"
						+ xmlPath);
				throw new XMLFileException("单元名称未知！");
			}
		}

	}

	/**
	 * 检查一个类型的项目中是否有重复的别名或者未知的别名
	 * 
	 * @param unitAliass
	 * @param unit
	 * @param xmlPath
	 * @param aliass
	 * @throws XMLFileException
	 */
	private void checkUnitsAlias(List<String> unitAliass, Unit unit,
			String xmlPath, List<String> aliass) throws XMLFileException {
		if (unitAliass.contains(unit.getAlias())) {
			logger.info(unit.getName() + ":" + unit.getAlias() + ":" + xmlPath);
			throw new XMLFileException("单元别名有重复！");
		}
		if (!aliass.contains(unit.getAlias())) {
			logger.info(unit.getName() + ":" + unit.getAlias() + ":" + xmlPath);
			throw new XMLFileException("单元别名未知！");
		}
		unitAliass.add(unit.getAlias());
	}

	/**
	 * 将对应的复合要素模版绑定到单元模版上，包含所有项目类型的复合要素模版
	 * 
	 * @return
	 */
	public List<Unit> makeElementGroupModel(List<Unit> units,
			List<ElementGroup> elementGroups) {
		for (int i = 0; i < units.size(); i++) {
			List<ElementGroup> unitElementGroups = new ArrayList<ElementGroup>();
			for (int j = 0; j < elementGroups.size(); j++) {
				// 无复合要素
				if (units.get(i).getAliases() == null) {
					continue;
				} else {
					if (units.get(i).getAliases().indexOf(",") != -1) {
						String[] str2 = units.get(i).getAliases().split(",");
						for (int k = 0; k < str2.length; k++) {
							if (str2[k].equals(elementGroups.get(j).getAlias())) {
								unitElementGroups.add(elementGroups.get(j));
							}
						}
						units.get(i).setElementGroups(unitElementGroups);
					} else {
						if (units.get(i).getAliases()
								.equals(elementGroups.get(j).getAlias())) {
							unitElementGroups.add(elementGroups.get(j));
							units.get(i).setElementGroups(unitElementGroups);
						}
					}
				}
			}
		}
		return units;
	}

	/**
	 * 为所有缓存去除多余的层级
	 */
	public List<Unit> unitRelieveOnModel(List<Unit> units) {
		for (int i = 0; i < units.size(); i++) {
			if (units.get(i).getUnitChildrens() != null) {
				for (int k = 0; k < units.get(i).getUnitChildrens().size(); k++) {
					if (units.get(i).getUnitChildrens().get(k)
							.getUnitChildrens() != null) {
						units.get(i).getUnitChildrens().get(k)
								.setUnitChildrens(null);
					}
					if (units.get(i).getUnitChildrens().get(k).getUnitParent() != null) {
						units.get(i).getUnitChildrens().get(k)
								.setUnitParent(null);
					}
				}
			}
		}
		return units;
	}

	/**
	 * 构建具体单元下的复合要素包含要素值
	 * 
	 * @param unit
	 * @return
	 */
	public Unit bulidElementDB(Unit unit) {
		List<ElementGroup> elementGroups = unit.getElementGroups();
		List<ElementDB> elementDBs = elementDBService.findByProject(unit
				.getProject().getId());
		com.hysw.qqsl.cloud.core.entity.element.Element element;
		// 判断单元下的复合要素
		for (int i = 0; i < elementGroups.size(); i++) {
			for (int k = 0; k < elementGroups.get(i).getElements().size(); k++) {
				elementGroups.get(i).setUnit(unit);
				elementGroups.get(i).setProject(unit.getProject());
				element = elementGroups.get(i).getElements().get(k);
				for (int t = 0; t < elementDBs.size(); t++) {
					if (element.getAlias().equals(elementDBs.get(t).getAlias())) {
						element.setValue(elementDBs.get(t).getValue());
						element.setElementDataStr(elementDBs.get(t)
								.getElementDataStr());
						element.setProject(unit.getProject());
						element.setId(elementDBs.get(t).getId());
						element.setElementGroup(elementGroups.get(i));
					}
				}
			}
		}
		unit.setElementGroups(elementGroups);
		return unit;
	}

	public List<Unit> getAgrUnitModels() {
		Cache cache = cacheManager.getCache("unitModelsCache");
		net.sf.ehcache.Element element = cache.get("agr");
		if(element==null){
			element = new net.sf.ehcache.Element("agr", getUnits(setting.getAgrProjectModel()));
			cache.put(element);
		}
		return (List<Unit>) element.getValue();
	}

	public List<Unit> getConUnitModels() {
		Cache cache = cacheManager.getCache("unitModelsCache");
		net.sf.ehcache.Element element = cache.get("con");
		if(element==null){
			element = new net.sf.ehcache.Element("con", getUnits(setting.getConProjectModel()));
			cache.put(element);
		}
		return (List<Unit>) element.getValue();
	}

	public List<Unit> getWatUnitModels() {
		Cache cache = cacheManager.getCache("unitModelsCache");
		net.sf.ehcache.Element element = cache.get("wat");
		if(element==null){
			element = new net.sf.ehcache.Element("wat", getUnits(setting.getWatProjectModel()));
			cache.put(element);
		}
		return (List<Unit>) element.getValue();
	}

	public List<Unit> getHydUnitModels() {
		Cache cache = cacheManager.getCache("unitModelsCache");
		net.sf.ehcache.Element element = cache.get("hyd");
		if(element==null){
			element = new net.sf.ehcache.Element("hyd", getUnits(setting.getHydProjectModel()));
			cache.put(element);
		}
		return (List<Unit>) element.getValue();
	}

	public List<Unit> getFloUnitModels() {
		Cache cache = cacheManager.getCache("unitModelsCache");
		net.sf.ehcache.Element element = cache.get("flo");
		if(element==null){
			element = new net.sf.ehcache.Element("flo", getUnits(setting.getFloProjectModel()));
			cache.put(element);
		}
		return (List<Unit>) element.getValue();
	}

	public List<Unit> getDriUnitModels() {
		Cache cache = cacheManager.getCache("unitModelsCache");
		net.sf.ehcache.Element element = cache.get("dri");
		if(element==null){
			element = new net.sf.ehcache.Element("dri", getUnits(setting.getDriProjectModel()));
			cache.put(element);
		}
		return (List<Unit>) element.getValue();
	}

	/**
	 * 公共的获取有复合要素关系的units的方法
	 * 
	 * @param unitModels
	 * @param elementGroupModels
	 * @return
	 */
	public List<Unit> getUnits(List<Unit> unitModels,
			List<ElementGroup> elementGroupModels) {
		@SuppressWarnings("unchecked")
		List<Unit> unitSimpleModels = (List<Unit>) SettingUtils
				.objectCopy(unitModels);
		List<Unit> units = makeElementGroupModel(
				unitRelieveOnModel(unitSimpleModels), elementGroupModels);
		return units;
	}

	public List<Unit> getAgrUnits() {
		Cache cache = cacheManager.getCache("unitsCache");
		net.sf.ehcache.Element element = cache.get("agr");
		if(element==null){
			List<Unit> unitModels = getAgrUnitModels();
			List<ElementGroup> elementGroupModels = elementGroupService
					.getAgrElementGroups();
			List<Unit> units = getUnits(unitModels, elementGroupModels);
			element = new net.sf.ehcache.Element("agr",units);
			cache.put(element);
		}
		return (List<Unit>) element.getValue();
	}

	public List<Unit> getConUnits() {
		Cache cache = cacheManager.getCache("unitsCache");
		net.sf.ehcache.Element element = cache.get("con");
		if(element==null){
			List<Unit> unitModels = getConUnitModels();
			List<ElementGroup> elementGroupModels = elementGroupService
					.getConElementGroups();
			List<Unit> units = getUnits(unitModels, elementGroupModels);
			element = new net.sf.ehcache.Element("con",units);
			cache.put(element);
		}
		return (List<Unit>) element.getValue();
	}

	public List<Unit> getDriUnits() {
		Cache cache = cacheManager.getCache("unitsCache");
		net.sf.ehcache.Element element = cache.get("dri");
		if(element==null){
			List<Unit> unitModels = getDriUnitModels();
			List<ElementGroup> elementGroupModels = elementGroupService
					.getDriElementGroups();
			List<Unit> units = getUnits(unitModels, elementGroupModels);
			element = new net.sf.ehcache.Element("dri",units);
			cache.put(element);
		}
		return (List<Unit>) element.getValue();
	}

	public List<Unit> getHydUnits() {
		Cache cache = cacheManager.getCache("unitsCache");
		net.sf.ehcache.Element element = cache.get("hyd");
		if(element==null){
			List<Unit> unitModels = getHydUnitModels();
			List<ElementGroup> elementGroupModels = elementGroupService
					.getHydElementGroups();
			List<Unit> units = getUnits(unitModels, elementGroupModels);
			element = new net.sf.ehcache.Element("hyd",units);
			cache.put(element);
		}
		return (List<Unit>) element.getValue();
	}

	public List<Unit> getFloUnits() {
		Cache cache = cacheManager.getCache("unitsCache");
		net.sf.ehcache.Element element = cache.get("flo");
		if(element==null){
		List<Unit> unitModels = getFloUnitModels();
		List<ElementGroup> elementGroupModels = elementGroupService
				.getFloElementGroups();
		List<Unit> units = getUnits(unitModels, elementGroupModels);
		element = new net.sf.ehcache.Element("flo",units);
		cache.put(element);
		}
		return (List<Unit>) element.getValue();
	}

	public List<Unit> getWatUnits() {
		Cache cache = cacheManager.getCache("unitsCache");
		net.sf.ehcache.Element element = cache.get("wat");
		if(element==null){
			List<Unit> unitModels = getWatUnitModels();
			List<ElementGroup> elementGroupModels = elementGroupService
					.getWatElementGroups();
			List<Unit> units = getUnits(unitModels, elementGroupModels);
			element = new net.sf.ehcache.Element("wat",units);
			cache.put(element);
		}
		return (List<Unit>) element.getValue();
	}

	/**
	 * 构建具体单元
	 * 
	 * @param unitAlias
	 * @return
	 */
	public Unit findUnit(String unitAlias, boolean value, Project project) {
		List<Unit> units = makeUnit(project.getType());
		Unit unit = null;
		for (int i = 0; i < units.size(); i++) {
			if (units.get(i).getAlias().equals(unitAlias)) {
				unit = (Unit) SettingUtils.objectCopy(units.get(i));
				break;
			}
		}
		if(unit == null){
			return unit;
		}
		unit.setProject(project);
		unit.setTreePath(project.getTreePath() + "/" + unit.getTreePath());
		if (value == false) {
			return unit;
		} else {
			return elementService.bulidElement(unit);
		}
	}

	/**
	 * 根据项目类型创建模板
	 * 
	 * @param type
	 * @return
	 */
	public List<Unit> makeUnit(Type type) {
		List<Unit> units = null;
		if (type.equals(Project.Type.AGRICULTURAL_IRRIGATION)) {
			units = getAgrUnits();
		}
		if (type.equals(Project.Type.DRINGING_WATER)) {
			units = getDriUnits();
		}
		if (type.equals(Project.Type.CONSERVATION)) {
			units = getConUnits();
		}
		if (type.equals(Project.Type.FLOOD_DEFENCES)) {
			units = getFloUnits();
		}
		if (type.equals(Project.Type.HYDROPOWER_ENGINEERING)) {
			units = getHydUnits();
		}
		if (type.equals(Project.Type.WATER_SUPPLY)) {
			units = getWatUnits();
		}
		return units;
	}

	/**
	 * 构建单元的json，包含elementGroup,element,elementDataGroup,elementData
	 * 
	 * @param unit
	 * @return
	 */
	public JSONObject makeUnitJson(Unit unit) {
		JSONObject unitJson = new JSONObject();
		unitJson.put("alias", unit.getAlias());
		unitJson.put("aliases", unit.getAliases());
		unitJson.put("grade", unit.getGrade());
		unitJson.put("name", unit.getName());
		List<ObjectFile> objectFiles = unit.getObjectFiles();
		if (objectFiles != null) {
			setObjectFileJsons(objectFiles, unitJson);
		}
		unitJson.put("treePath", unit.getTreePath());
		unitJson.put("type", unit.getType());
		unitJson.put("alias", unit.getAlias());
		if (unit.getContacts() != null) {
			setContactJsons(unit.getContacts(), unitJson);
		}
		setElementGroupJsons(unit.getElementGroups(), unitJson);
		return unitJson;
	}

	/**
	 * 构建elementGroup数组的elementGroupJson
	 * 
	 * @param elementGroups
	 * @param unitJson
	 */
	private void setElementGroupJsons(List<ElementGroup> elementGroups,
			JSONObject unitJson) {
		List<JSONObject> elementGroupJsons = new ArrayList<JSONObject>();
		ElementGroup elementGroup;
		JSONObject elementGroupJson;
		for (int i = 0; i < elementGroups.size(); i++) {
			elementGroupJson = new JSONObject();
			elementGroup = elementGroups.get(i);
			elementGroupJson.put("action", elementGroup.getAction());
			elementGroupJson.put("alias", elementGroup.getAlias());
			elementGroupJson.put("name", elementGroup.getName());
			setElementJsons(elementGroupJson, elementGroup);
			elementGroupJsons.add(elementGroupJson);
		}
		unitJson.put("elementGroups", elementGroupJsons);
	}

	/**
	 * 构建element数组的elementJsons
	 * 
	 * @param elementGroupJson
	 * @param elementGroup
	 */
	private void setElementJsons(JSONObject elementGroupJson,
			ElementGroup elementGroup) {
		List<JSONObject> elementDataGroupJsons;
		List<JSONObject> elementJsons;
		com.hysw.qqsl.cloud.core.entity.element.Element element;
		JSONObject elementJson;
		ElementDataGroup elementDataGroup;
		JSONObject elementDataGroupJson;
		elementJsons = new ArrayList<JSONObject>();
		for (int j = 0; j < elementGroup.getElements().size(); j++) {
			element = elementGroup.getElements().get(j);
			elementJson = new JSONObject();
			elementJson.put("alias", element.getAlias());
			elementJson.put("countType", element.getCountType());
			elementJson.put("description", element.getDescription());
			elementJson.put("elementDataAlias", element.getElementDataAlias());
			elementJson.put("elementDataGroupType",element.getElementDataGroupType());
			elementJson.put("elementDataSelect", element.getElementDataSelect());
			if(element.getSelects()!=null){
				elementJson.put("selects", element.getSelects());
			}
			if(element.getElementDataSelects()!=null){
				elementJson.put("elementDataSelects", element.getElementDataSelects());
			}
			elementJson.put("elementDataStr", element.getElementDataStr());
			if(element.getElementParent()!=null){
				setElementParentJson(elementJson,element.getElementParent());
			}
			elementJson.put("grade", element.getGrade());
			elementJson.put("id", element.getId());
			elementJson.put("infoOrder", element.getInfoOrder());
			elementJson.put("name", element.getName());
			elementJson.put("select", element.getSelect());
			elementJson.put("type", element.getType());
			elementJson.put("value", element.getValue());
			elementDataGroupJsons = new ArrayList<JSONObject>();
			for (int k = 0; k < element.getElementDataGroups().size(); k++) {
				elementDataGroup = element.getElementDataGroups().get(k);
				elementDataGroupJson = new JSONObject();
				elementDataGroupJson.put("id", elementDataGroup.getId());
				elementDataGroupJson.put("name", elementDataGroup.getName());
				elementDataGroupJson.put("dataType", elementDataGroup.getDataType());
				JSONArray dataStr = JSONArray.fromObject(elementDataGroup.getDataStr());
				if(!dataStr.get(0).equals("null")){
					elementDataGroupJson.put("elementDatas",dataStr);
				}
				elementDataGroupJsons.add(elementDataGroupJson);
			}
			elementJson.put("elementDataGroups", elementDataGroupJsons);
			elementJsons.add(elementJson);
		}
		elementGroupJson.put("elements", elementJsons);
	}

	/**
	 * 构建要素父节点json串
	 * @param elementParent
	 */
	private void setElementParentJson(JSONObject elementJson,
			com.hysw.qqsl.cloud.core.entity.element.Element elementParent) {
		List<JSONObject> elementDataGroupJsons;
		ElementDataGroup elementDataGroup;
		JSONObject elementDataGroupJson = null;
		JSONObject elementParentJson = new JSONObject();
		elementParentJson.put("alias", elementParent.getAlias());
		elementParentJson.put("countType", elementParent.getCountType());
		elementParentJson.put("description", elementParent.getDescription());
		elementParentJson.put("elementDataAlias", elementParent.getElementDataAlias());
		elementParentJson.put("elementDataGroupType",
				elementParent.getElementDataGroupType());
		elementParentJson
				.put("elementDataSelect", elementParent.getElementDataSelect());
		elementParentJson.put("elementDataStr", elementParent.getElementDataStr());
		if(elementParent.getElementParent()!=null){
			setElementParentJson(elementParentJson,elementParent.getElementParent());
			elementParentJson.put("alias",elementParent.getElementParent());	
		}
		elementParentJson.put("grade", elementParent.getGrade());
		elementParentJson.put("id", elementParent.getId());
		elementParentJson.put("infoOrder", elementParent.getInfoOrder());
		elementParentJson.put("name", elementParent.getName());
		elementParentJson.put("select", elementParent.getSelect());
		elementParentJson.put("type", elementParent.getType());
		elementParentJson.put("value", elementParent.getValue());
		elementDataGroupJsons = new ArrayList<JSONObject>();
		for (int k = 0; k < elementParent.getElementDataGroups().size(); k++) {
			elementDataGroup = elementParent.getElementDataGroups().get(k);
			elementDataGroupJson = new JSONObject();
			elementDataGroupJson.put("id", elementDataGroup.getId());
			elementDataGroupJson.put("dataStr",
					elementDataGroup.getDataStr());
			elementDataGroupJsons.add(elementDataGroupJson);
		}
		elementParentJson.put("elementDataGroups", elementDataGroupJsons);
		elementJson.put("elementParent",elementParentJson);	
	}

	/**
	 * 构建通讯录的json
	 * 
	 * @param contacts
	 * @param unitJson
	 */
	private void setContactJsons(List<Contact> contacts, JSONObject unitJson) {
		JSONObject contactJson;
		Contact contact;
		JSONObject userJson;
		List<JSONObject> contactJsons = new ArrayList<JSONObject>();
		for (int i = 0; i < contacts.size(); i++) {
			contactJson = new JSONObject();
			contact = contacts.get(i);
			contactJson.put("id", contact.getId());
			contactJson.put("company", contact.getCompany());
			contactJson.put("depart", contact.getDepart());
			contactJson.put("createDate", contact.getCreateDate());
			contactJson.put("email", contact.getEmail());
			contactJson.put("master", contact.getMaster());
			contactJson.put("masterEmail", contact.getMasterEmail());
			contactJson.put("masterPhone", contact.getMasterPhone());
			contactJson.put("modifyDate", contact.getModifyDate());
			contactJson.put("name", contact.getName());
			contactJson.put("phone", contact.getPhone());
			contactJson.put("qualify", contact.getQualify());
			contactJson.put("type", contact.getType());
			userJson = new JSONObject();
			userJson.put("id", contact.getUser().getId());
			userJson.put("name", contact.getUser().getName());
			userJson.put("userName", contact.getUser().getUserName());
			contactJson.put("user", userJson);
			contactJsons.add(contactJson);
		}
		unitJson.put("contacts", contactJsons);
	}

	/**
	 * 构建阿里云文件的json
	 * 
	 * @param objectFiles
	 * @param unitJson
	 */
	private void setObjectFileJsons(List<ObjectFile> objectFiles,
			JSONObject unitJson) {
		List<JSONObject> objectFileJsons = new ArrayList<JSONObject>();
		JSONObject objectFileJson;
		ObjectFile objectFile;
		for (int k = 0; k < objectFiles.size(); k++) {
			objectFile = objectFiles.get(k);
			objectFileJson = new JSONObject();
			objectFileJson.put("description", objectFile.getDescription());
			objectFileJson.put("downloadUrl", objectFile.getDownloadUrl());
			objectFileJson.put("key", objectFile.getKey());
			objectFileJson.put("name", objectFile.getName());
			objectFileJson.put("previewUrl", objectFile.getPreviewUrl());
			objectFileJson.put("type", objectFile.getType());
			objectFileJson.put("updateDate", objectFile.getUpdateDate());
			objectFileJson.put("size", objectFile.getSize());
			objectFileJsons.add(objectFileJson);
		}
		unitJson.put("objectFiles", objectFileJsons);
	}
}
