package com.hysw.qqsl.cloud.service;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.hysw.qqsl.cloud.entity.element.Element;
import com.hysw.qqsl.cloud.entity.element.ElementGroup;
import com.hysw.qqsl.cloud.entity.element.Unit;
import com.hysw.qqsl.cloud.entity.data.ElementDB;
import com.hysw.qqsl.cloud.entity.data.ElementDataGroup;
import com.hysw.qqsl.cloud.entity.data.Project;

/**
 * 简介service
 * 
 * @since2015年12月8日
 * @author Administrator
 *
 */
@Service("introduceService")
public class IntroduceService{
	Log logger = LogFactory.getLog(getClass());
	@Autowired
	private UnitService unitService;
	@Autowired
	private ElementDBService elementDBService;

	/**
	 * 拼接要素简介成项目简介
	 * 
	 * @param elements
	 * @return
	 */
	private String covertIntroduce(List<Element> elements) {
		StringBuffer introduceStr = new StringBuffer("");
		String elementDatastr;
		Element element;
		if (elements.size() == 0) {
			return introduceStr.toString();
		}
		//构建节点关系
		makeElementChilds(elements);
		for (int i = 0; i < elements.size(); i++) {
			element = elements.get(i);
			if (element.getId()==null||element.getDescription()==null||!element.getDescription().equals("introduce")) {
				continue;
			}
			if(element.getElementParent()!=null){
				continue;
			}
			getElementDataStr(element);
			elementDatastr =elements.get(i).getElementDataStr();
			if(elementDatastr==null){
				continue;
			}
			if(elementDatastr.endsWith("，")||elementDatastr.endsWith("；")||introduceStr.equals(",")){
				introduceStr.append(elementDatastr);
			}else{
				introduceStr.append(elementDatastr+"，");
			}
			if (introduceStr.equals("，")||introduceStr.equals(",")) {
				continue;
			}
			introduceStr = introduceStr.substring(introduceStr.length() - 1).equals("，")||
					introduceStr.substring(introduceStr.length() - 1).equals("；")==true?
					introduceStr:introduceStr.append("，");
		}
		if (introduceStr.equals("，")||!StringUtils.hasText(introduceStr)) {
			return null;
		}
		if(introduceStr.lastIndexOf("，")!=-1||introduceStr.lastIndexOf("；")!=-1){
			introduceStr = introduceStr.replace(introduceStr.length() - 1,introduceStr.length(),"。");
		}
		return introduceStr.toString();
	}

	/**
	 * 构建要素的简介字段
	 * @param element
     */
    public void getElementDataStr(Element element) {
		String introduceStr = "";
		Element elementChild;
		List<ElementDataGroup> elementDataGroups;
		if (element.getChilds() != null && element.getChilds().size() > 0) {
			for (int i = 0; i < element.getChilds().size(); i++) {
				elementChild = element.getChilds().get(i);
				if (elementChild.getId() == null ||
						elementChild.getIntroduceDescription() == null||!StringUtils.hasText(elementChild.getValue())) {
					continue;
				}
				elementDataGroups = elementChild.getElementDataGroups();
				if (elementDataGroups != null || elementDataGroups.size() > 0) {
					introduceStr = introduceStr + getElementDataByElementDataGroups(elementChild);
				} else {
					introduceStr = elementChild.getUnit() != null || StringUtils.hasText(elementChild.getUnit()) ?
							elementChild.getIntroduceDescription() + elementChild.getValue() + elementChild.getUnit()
							: elementChild.getIntroduceDescription() + elementChild.getValue();
				}
			}
			introduceStr = (element.getUnit() != null || StringUtils.hasText(element.getUnit()))&&StringUtils.hasText(element.getValue()) ?
					element.getIntroduceDescription() + element.getValue() + element.getUnit() +  "，其中：" + introduceStr : element.getIntroduceDescription() + element.getValue();
		}else{
			elementDataGroups = element.getElementDataGroups();
			if (elementDataGroups != null && elementDataGroups.size() > 0) {
				introduceStr = getElementDataByElementDataGroups(element);
			} else {
				introduceStr = element.getUnit() != null || StringUtils.hasText(element.getUnit()) ?
						element.getIntroduceDescription() + element.getValue() + element.getUnit() : element.getIntroduceDescription() + element.getValue();
			}
		}
		if (StringUtils.hasText(introduceStr)) {
			element.setElementDataStr(introduceStr);
		}
	}


	/**
	 * 构建要素下要素数据组的简介字段
	 * @param element
	 * @return
     */
    public String getElementDataByElementDataGroups(Element element){
		String introduceStr = "";
		List<ElementDataGroup> elementDataGroups = element.getElementDataGroups();
		for(int i=0;i<elementDataGroups.size();i++){
			if(StringUtils.hasText(elementDataGroups.get(i).getIntroDataStr())){
				introduceStr = introduceStr+elementDataGroups.get(i).getIntroDataStr();
			}
		}
		if(introduceStr.equals("null")||!StringUtils.hasText(introduceStr)){
			return "";
		}
		if((element.getValue()!=null||!StringUtils.hasText(element.getValue()))&&element.getUnit()!=null){
			introduceStr = element.getIntroduceDescription()+element.getValue()+element.getUnit()+"，其中："+introduceStr;
		}else{
			introduceStr = element.getIntroduceDescription()+"："+introduceStr;
		}
		return introduceStr;
	}


	/**
	 * 构建节点子父级关系
	 * @param elements
     */
	public void makeElementChilds(List<Element> elements){
		List<Element> elementChilds = null;
		Element element,elementParent,elementChild;
		for(int i=0;i<elements.size();i++){
			element = elements.get(i);
			if(element.getElementParent()==null){
				continue;
			}
			elementParent = element.getElementParent();
			elementChilds = new ArrayList<>();
				for(int k=0;k<elements.size();k++){
					elementChild = elements.get(k);
					if(elementChild.getElementParent()!=null&&
							elementChild.getElementParent().getAlias().equals(elementParent.getAlias())){
						elementChilds.add(elementChild);
					}
				}
			elementParent.setChilds(elementChilds);
		}
	}
	/**
	 * 动态构建简介
	 * @param project
     */
	public JSONObject buildIntroduceJson(Project project){
		JSONObject introduceJson = new JSONObject();
	    List<ElementDB> elementDBs = elementDBService.findByProject(project.getId());
		if(elementDBs==null||elementDBs.size()==0) {
			return introduceJson;
		}
		Unit studyUnit = unitService.findUnit("23",true,project);
		Unit earlyUnit = unitService.findUnit("24",true,project);
		Unit geologyUnit = unitService.findUnit("21",true,project);
	    introduceJson = buildStudyIntroduce(studyUnit,earlyUnit,geologyUnit);
		return introduceJson;
	}


	/**
	 * 构建可研，初设的简介
	 * @param studyUnit
	 * @param earlyUnit
     * @return
     */
	private JSONObject buildStudyIntroduce(Unit studyUnit,Unit earlyUnit,Unit geologyUnit){
		JSONObject introduceJson = new JSONObject();
		JSONObject studyJson = getIntroduceJsons(studyUnit);
		JSONObject earlyJson = getIntroduceJsons(earlyUnit);
		//构建地理信息
		JSONObject geologyJson = getIntroduceJsons(geologyUnit);
		String studyBase,studyMatter,studyInvestment,earlyBase,earlyMatter,earlyInvestment,geology;
		geology = StringUtils.hasText(geologyJson.getString("geology")) ? geologyJson.getString("geology") : "";
		studyBase = StringUtils.hasText(studyJson.getString("base")) ? geology+studyJson.getString("base"):geology;
		studyMatter = StringUtils.hasText(studyJson.getString("matter")) ? studyJson.getString("matter"):"";
		studyInvestment = StringUtils.hasText(studyJson.getString("investment")) ? studyJson.getString("investment"):"";
		introduceJson.put("studyBase",studyBase);
		introduceJson.put("studyMatter",studyMatter);
		introduceJson.put("studyInvestment",studyInvestment);
		earlyBase = StringUtils.hasText(earlyJson.getString("base")) ? geology+earlyJson.getString("base"):"";
		earlyMatter = StringUtils.hasText(earlyJson.getString("matter")) ? earlyJson.getString("matter"):"";
		earlyInvestment = StringUtils.hasText(earlyJson.getString("investment")) ? earlyJson.getString("investment"):"";
		introduceJson.put("earlyBase",earlyBase);
		introduceJson.put("earlyMatter",earlyMatter);
		introduceJson.put("earlyInvestment",earlyInvestment);
		return introduceJson;
	}

	/**
	 * 构建部分简介
	 * @param unit
	 * @return
     */
	private JSONObject getIntroduceJsons(Unit unit){
		ElementGroup elementGroup;
		Element element;
		List<Element> bases = new ArrayList<Element>();
		List<Element> matters = new ArrayList<Element>();
		List<Element> investments = new ArrayList<Element>();
		List<Element> geologys = new ArrayList<Element>();
		for (int i = 0; i < unit.getElementGroups().size(); i++) {
			elementGroup = unit.getElementGroups().get(i);
			for (int j = 0; j < elementGroup.getElements().size(); j++) {
				element = elementGroup.getElements().get(j);
				if (elementGroup.getAlias().equals("23A") ||
						elementGroup.getAlias().equals("24A")) {
					if (element.getIntroduceDescription() != null && element.getValue() != null) {
						bases.add(element);
						continue;
					}
				}
				if (elementGroup.getAlias().equals("23B") ||
						elementGroup.getAlias().equals("24B")) {
					if (element.getIntroduceDescription() != null && element.getValue() != null) {
						matters.add(element);
						continue;
					}
				}
				if (elementGroup.getAlias().equals("23C") ||
						elementGroup.getAlias().equals("24C")) {
					if (element.getIntroduceDescription() != null && element.getValue() != null) {
						investments.add(element);
						continue;
					}
				}
				if(elementGroup.getAlias().equals("21A")){
					if (element.getIntroduceDescription() != null && element.getValue() != null) {
						geologys.add(element);
					}
				}
			}
		}
		//获取json
		String base = covertIntroduce(bases);
		String matter = covertIntroduce(matters);
		String investment = covertIntroduce(investments);
		String geology = covertIntroduce(geologys);
		JSONObject introduceJson = new JSONObject();
		base = StringUtils.hasText(base) ? base:"";
		matter = StringUtils.hasText(matter) ? matter:"";
		investment = StringUtils.hasText(investment) ? investment:"";
		geology = StringUtils.hasText(geology) ? geology:"";
		introduceJson.put("base",base);
		introduceJson.put("matter",matter);
		introduceJson.put("investment",investment);
		introduceJson.put("geology",geology);
		return introduceJson;
	}
}
