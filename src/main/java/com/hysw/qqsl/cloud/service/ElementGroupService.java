package com.hysw.qqsl.cloud.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.hysw.qqsl.cloud.entity.data.Project;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import nu.xom.XMLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.entity.element.Element;
import com.hysw.qqsl.cloud.entity.element.ElementGroup;
import com.hysw.qqsl.cloud.entity.Setting;
import com.hysw.qqsl.cloud.entity.XMLFileException;
import com.hysw.qqsl.cloud.entity.data.ElementDataGroup;
import com.hysw.qqsl.cloud.util.SettingUtils;

/**
 * 复合要素模版缓存
 * 
 * @author leinuo
 *
 * @date 2016年1月13日
 */
@Service("elementGroupService")
public class ElementGroupService  {
	Log logger = LogFactory.getLog(getClass());
	@Autowired
	private CacheManager cacheManager;
	int grade;
	private Setting setting = SettingUtils.getInstance().getSetting();

	/**
	 * 读取复合要素
	 * @param elementGroupXMLName
	 * @return
	 */
	public List<ElementGroup> getElementGroups(String elementGroupXMLName){
		List<ElementGroup> elementGroups = null;
				try {
					elementGroups = makeElementGroup(elementGroupXMLName);
				} catch (XMLFileException e) {
					logger.error(elementGroupXMLName+"----");
					e.printStackTrace();
				}
		return elementGroups;	
	}
	/**
	 * 读取xml创建各个elementGroup对象的方法
	 * 
	 * @param elementGroupXMLName
	 * @return
	 * @throws XMLException 
	 */
	public List<ElementGroup> makeElementGroup(String elementGroupXMLName) throws XMLFileException  {
		List<ElementGroup> elementGroupModels = new ArrayList<ElementGroup>();
		org.dom4j.Element root;
		List<String> elementAliass = new ArrayList<String>();
		List<String> elementGroupAliass = new ArrayList<String>();
		ElementGroup elementGroup;
		List<Element> elements;
		try {
			// 获取ElementGroupModels根节点
			root = SettingUtils.getInstance().getRootElement(
					elementGroupXMLName);
			// 获取子节点ElementGroup的List
			List<org.dom4j.Element> elementGroups = SettingUtils.getInstance()
					.getElementGroupList(root);
			// 遍历ElementGroup的子节点
			for (int i = 0; i < elementGroups.size(); i++) {
				 elementGroup = new ElementGroup();
				 elements = new ArrayList<Element>();
				if (elementGroups.get(i).attributeValue("name") != null) {
					elementGroup.setName(elementGroups.get(i).attributeValue(
							"name"));
				}
				if (elementGroups.get(i).attributeValue("alias") != null) {
					elementGroup.setAlias(elementGroups.get(i).attributeValue(
							"alias"));
				}
				checkElementGroupAlias(elementGroupAliass,elementGroup.getAlias());
				// 获取elementGroup下的element集合
				@SuppressWarnings("unchecked")
				Iterator<Element> it = elementGroups.get(i).elementIterator();
				org.dom4j.Element child;
				while (it.hasNext()) {
					this.grade = 0;
					Element element = new Element();
					child = (org.dom4j.Element) it.next();
					makeElement(child, element, elementGroup, elements,elementAliass);
					element.setElementParent(null);
					makeElementGroupModel(child, grade + 1, element, elements,
								elementGroup,elementAliass);	
				}
				elementGroupModels.add(elementGroup);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		//logger.info(elementAliass);
		//logger.info(elementGroupAliass);
		return elementGroupModels;
	}

	/**
	 * 验证要素别名是否重复
	 * @param aliass
	 * @param alias
	 */
	public void checkAlias(List<String> aliass,String alias) throws XMLFileException{
		if(aliass.contains(alias)){
			throw new XMLFileException("要素别名有重复");
		}
		aliass.add(alias);
	}
	
	/**
	 * 验证复合要素名重复
	 * @param elementGroupAliass
	 * @param alias
	 */
	public void checkElementGroupAlias(List<String> elementGroupAliass,String alias) throws XMLFileException{
		if(elementGroupAliass.contains(alias)){
			logger.info(alias);
			throw new XMLFileException("复合要素别名有重复");
		}
		elementGroupAliass.add(alias);
	}
	/**
	 * 读取xml要素下所有子要素
	 * @param child
	 * 				读取的要素
	 * @param grade
	 *    			层级
	 * @param element
	 * 				写入的要素
	 * @param elements
	 * 				要素集
	 * @param elementGroup
	 * 				复合要素
	 * @throws XMLException 
	 */
	private void makeElementGroupModel(org.dom4j.Element child, int grade,
			Element element, List<Element> elements, ElementGroup elementGroup,List<String> aliass) throws XMLFileException {
		@SuppressWarnings("unchecked")
		Iterator<Element> it1 = child.elementIterator();
		this.grade = grade;
		Element element1;
		org.dom4j.Element child1;
		while (it1.hasNext()) {
			element1 = new Element();
			child1 = (org.dom4j.Element) it1.next();
			makeElement(child1, element1, elementGroup, elements,aliass);
			element1.setElementParent(element);
			makeElementGroupModel(child1, grade + 1, element1, elements,
					elementGroup,aliass);
		}
	}
	
	/**
	 * 读取xml中的所有要素属性
	 * @param child 读取的要素
	 * @param element 写入的要素
	 * @param elementGroup 复合要素
	 * @param elements 要素集
	 * @throws XMLException 
	 */
	private void makeElement(org.dom4j.Element child,Element element,ElementGroup elementGroup,List<Element> elements,List<String> aliass) throws XMLFileException{
		String countType,elementDataType,elementType;
		if (child.attributeValue("name") != null) {
			element.setName(child.attributeValue("name"));
		}
		if (child.attributeValue("alias") != null) {
			element.setAlias(child.attributeValue("alias"));
		}
		//检查是否有要素别名重复
		    checkAlias(aliass,element.getAlias());
		if (child.attributeValue("elementDataSelect") != null) {
			List<String> elementDataSelects = Arrays.asList(child.attributeValue("elementDataSelect").split(","));
			element.setElementDataSelects(elementDataSelects);
		}
		if (child.attributeValue("description") != null) {
			element.setDescription(child
					.attributeValue("description"));
			checkDescription(element,child);
		}
		if (child.attributeValue("introduce") != null) {
			element.setIntroduceDescription(child.attributeValue("introduce"));
		}
	
		if(child.attributeValue("elementDataAlias") != null){
			element.setElementDataAlias(child.attributeValue("elementDataAlias"));
		}
		if (child.attributeValue("infoOrder") != null) {
			element.setInfoOrder(child.attributeValue("infoOrder"));
			checkInfoOrder(element);
		}
		if (child.attributeValue("unit") != null) {
			element.setUnit(child.attributeValue("unit"));
		}
		if (child.attributeValue("alive") != null) {
			element.setAlive(child.attributeValue("alive"));
		}
		element.setGrade(grade);
		 countType = child.attributeValue("countType");
		if (countType != null) {
			if (countType.equals("add")) {
				element.setCountType(Element.CountType.add);
			} else if (countType.equals("average")) {
				element.setCountType(Element.CountType.average);
			}
		}
		elementDataType = child
				.attributeValue("elementDataType");
		if (elementDataType != null) {
			makeElementDataType(elementDataType, element);
		}
		elementType = child.attributeValue("type");
		if (elementType != null) {
			makeElementType(elementType, element,child);
		}
		elements.add(element);
		elementGroup.setElements(elements);
	}

	/**
	 * 检验要素描述
	 * @param element
	 * @param child
	 * @throws XMLException
	 */
	private void checkDescription(Element element,org.dom4j.Element child) throws XMLFileException {
		List<String> descriptions = Arrays.asList(CommonAttributes.DESCRIPTION.split(":"));
		List<String> redescription1;
		List<String> redescription2;
		if(element.getDescription().indexOf(",")!=-1){
		 redescription1 = Arrays.asList(element.getDescription().split(","));
		 redescription2 = new ArrayList<String>();
		 for(int i = 0;i<redescription1.size();i++){
			 if(redescription2.contains(redescription1.get(i))){
				logger.info(element.getAlias()+":"+element.getDescription());
				 throw new XMLFileException("要素描述自身有重复");
			 }
			 redescription2.add(redescription1.get(i));
		 }
		}
		if(!descriptions.contains(element.getDescription())){
			logger.info(element.getAlias()+":"+element.getDescription());
			throw new XMLFileException("要素描述未知！");
		}

		
		
	}
	/**
	 * 验证infoOrder
	 * @param element
	 * @throws XMLException
	 */
	private void checkInfoOrder(Element element) throws XMLFileException {
		String infoOrder = element.getInfoOrder();
		List<String> orders = Arrays.asList(CommonAttributes.INFO_ORDER);
		if(!orders.contains(infoOrder)){
			logger.info(element.getAlias()+":"+infoOrder);
			throw new XMLFileException("infOrder未知！");
		}
		
	}

	/**
	 * 判断要素数据的类型
	 *
	 * @param elementDataType
	 * @param element
	 * @throws XMLException
	 */
	public void makeElementDataType(String elementDataType, Element element) throws XMLFileException {
		List<String> dataTypes = Arrays.asList(CommonAttributes.DATA_TYPE);
		if(!dataTypes.contains(elementDataType)){
			logger.info(element.getAlias()+":"+elementDataType);
			throw new XMLFileException("要素数据类型未知！");
		}
		element.setElementDataGroupType(ElementDataGroup.DataType.valueOf(elementDataType.toUpperCase()));
	}


	/**
	 * 判断要素类型
	 * 
	 * @param elementType
	 * @param elem
	 * @param element
	 * @throws XMLException 
	 */
	public void makeElementType(String elementType, Element elem,
			org.dom4j.Element element) throws XMLFileException {
		List<String> types = Arrays.asList(CommonAttributes.ELEMENT_TYPE);
		if(!types.contains(elementType)){
			logger.info(elem.getAlias()+":"+elementType);
			throw new XMLFileException("要素类型未知");
		}
		String selectValues;
		elem.setType(Element.Type.valueOf(elementType.toUpperCase()));
		if(elementType.equals("select")||elementType.equals("checkBox")){
			selectValues = element.attributeValue("select");
			elem.setSelects(Arrays.asList(selectValues.split(",")));
		}
		if(elementType.equals("select_text")){
			elem.setType(Element.Type.SELECT_TEXT);
			selectValues = element.attributeValue("select");
			if(selectValues!=null){
				elem.setSelects(Arrays.asList(selectValues.split(",")));
			}
		}
	}

	public List<ElementGroup> getAgrElementGroups() {
		Cache cache = cacheManager.getCache("elementGroupCache");
		net.sf.ehcache.Element element = cache.get("agr");
		if(element==null){
			element = new net.sf.ehcache.Element("agr", getElementGroups(setting
					.getAgrElementGroupModel()));
			cache.put(element);
		}
		return (List<ElementGroup>) element.getValue();
	}


	public List<ElementGroup> getConElementGroups() {
		Cache cache = cacheManager.getCache("elementGroupCache");
		net.sf.ehcache.Element element = cache.get("con");
		if(element==null){
			element = new net.sf.ehcache.Element("con", getElementGroups(setting
					.getConElementGroupModel()));
			cache.put(element);
		}
		return (List<ElementGroup>) element.getValue();
	}

	public List<ElementGroup> getWatElementGroups() {
		Cache cache = cacheManager.getCache("elementGroupCache");
		net.sf.ehcache.Element element = cache.get("wat");
		if(element==null){
			element = new net.sf.ehcache.Element("wat", getElementGroups(setting
					.getWatElementGroupModel()));
			cache.put(element);
		}
		return (List<ElementGroup>) element.getValue();
	}

	public List<ElementGroup> getFloElementGroups() {
		Cache cache = cacheManager.getCache("elementGroupCache");
		net.sf.ehcache.Element element = cache.get("flo");
		if(element==null){
			element = new net.sf.ehcache.Element("flo", getElementGroups(setting
					.getFloElementGroupModel()));
			cache.put(element);
		}
		return (List<ElementGroup>) element.getValue();
	}

	public List<ElementGroup> getHydElementGroups() {
		Cache cache = cacheManager.getCache("elementGroupCache");
		net.sf.ehcache.Element element = cache.get("hyd");
		if(element==null){
			element = new net.sf.ehcache.Element("hyd", getElementGroups(setting
					.getHydElementGroupModel()));
			cache.put(element);
		}
		return (List<ElementGroup>) element.getValue();
	}

	public List<ElementGroup> getDriElementGroups() {
		Cache cache = cacheManager.getCache("elementGroupCache");
		net.sf.ehcache.Element element = cache.get("dri");
		if(element==null){
			element = new net.sf.ehcache.Element("dri", getElementGroups(setting
					.getDriElementGroupModel()));
			cache.put(element);
		}
		return (List<ElementGroup>) element.getValue();
	}

	/**
	 * 根据项目类型获取复合要素集合
	 * @param type
	 * @return
     */
	public List<ElementGroup> buildByType(Project.Type type) {
		List<ElementGroup> elementGroups = null;
		if (type == Project.Type.AGRICULTURAL_IRRIGATION) {
			elementGroups = getAgrElementGroups();
		} else if (type == Project.Type.CONSERVATION) {
			elementGroups = getConElementGroups();
		} else if (type == Project.Type.DRINGING_WATER) {
			elementGroups = getDriElementGroups();
		} else if (type == Project.Type.FLOOD_DEFENCES) {
			elementGroups = getFloElementGroups();
		} else if (type == Project.Type.HYDROPOWER_ENGINEERING) {
			elementGroups = getHydElementGroups();
		} else if (type == Project.Type.WATER_SUPPLY) {
			elementGroups =getWatElementGroups();
		}
		return elementGroups;
	}
}
