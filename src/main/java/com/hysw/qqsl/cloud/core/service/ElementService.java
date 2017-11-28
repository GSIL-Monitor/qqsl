package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.core.entity.element.Element;
import com.hysw.qqsl.cloud.core.entity.element.ElementGroup;
import com.hysw.qqsl.cloud.core.entity.element.Info;
import com.hysw.qqsl.cloud.core.entity.element.Unit;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

@Service("elementService")
public class ElementService {
	@Autowired
	private ElementDBService elementDBService;
	@Autowired
	private ContactService contactService;
	@Autowired
	private ElementDataGroupService elementDataGroupService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private IntroduceService introduceService;
	@Autowired
	private InfoService infoService;
	@Autowired
	private ProjectLogService projectLogService;
	private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 根据单元和项目从数据库构建带值的element,用于要素输出
	 *
	 * @param unit
	 * @return
	 */
	public List<Element> bulidExportElement(Unit unit) {
		Project project = unit.getProject();
		List<ElementGroup> elementGroups = unit.getElementGroups();
		List<Element> elements,elementList;
		List<Element> elementses = new ArrayList<Element>();
		Element element;
		for (int i = 0; i < elementGroups.size(); i++) {
			elements = new ArrayList<Element>();
			elementList = elementGroups.get(i).getElements();
			introduceService.makeElementChilds(elementList);
			for (int k = 0; k < elementList.size(); k++) {
				element = elementList.get(k);
				element.setProject(project);
				if(element.getElementParent()!=null){
					continue;
				}
				makeExportElementDataStr(element);
				elements.add(element);
			}
			elementses.addAll(elements);
		}
		return elementses;
	}

	/**
	 * 构建要素输出的要素值
	 * @param element
     */
	public void makeExportElementDataStr(Element element){
		String exportStr  = "";
		String str = "";
		Element elementChild=null;
		int value=0;
		List<ElementDataGroup> elementDataGroups;
           if(element.getChilds()!=null&&element.getChilds().size()>0){
			   if(element.getAlias().equals("23A6")&&element.getName().equals("控制灌溉面积(亩)")){
				   List<String> aliass = new ArrayList<>();
				   String alias="";
				   for(int i = 0;i<element.getChilds().size();i++){
					   aliass.add(element.getChilds().get(i).getAlias());
				   }
				   List<ElementDB>  elementDBs = elementDBService.findElementDBChilds(element.getProject().getId(),aliass);
				   List<Long> times = new ArrayList<>();
				   if(elementDBs.size()>0){
					   for(int j=0;j<elementDBs.size();j++){
						   times.add(elementDBs.get(j).getModifyDate().getTime());
					   }
					   Long maxTime = Collections.max(times);
					   for(int k=0;k<elementDBs.size();k++){
						   if(maxTime==elementDBs.get(k).getModifyDate().getTime()){
							   alias = elementDBs.get(k).getAlias();
						   }
					   }
					   for(int h=0;h<element.getChilds().size();h++){
						   if(element.getChilds().get(h).getAlias().equals(alias)){
							   elementChild = element.getChilds().get(h);
						   }
					   }
					   if(elementChild!=null){
						   List<Element> childs = new ArrayList<>();
						   childs.add(elementChild);
						   element.setChilds(childs);
					   }
				   }else{
					   element.setElementDataStr(exportStr);
					  return;
				   }
			   }
			   for(int i = 0;i<element.getChilds().size();i++){
				   elementChild = element.getChilds().get(i);
				   if (elementChild.getId() == null||!StringUtils.hasText(elementChild.getValue())) {
					   continue;
				   }
				   elementDataGroups = elementChild.getElementDataGroups();
				   if (elementDataGroups != null && elementDataGroups.size() > 0) {
					   exportStr = exportStr + introduceService.getElementDataByElementDataGroups(elementChild);
				   }
				   boolean flag = element.getAlias().equals("23A6")&&element.getName().equals("控制灌溉面积(亩)");
				   if(!flag){
					   str = elementChild.getUnit() != null || StringUtils.hasText(elementChild.getUnit()) ?
							   elementChild.getIntroduceDescription() + elementChild.getValue() + elementChild.getUnit()
							   : elementChild.getIntroduceDescription() + elementChild.getValue();
					   exportStr = exportStr + str + "，";
				   }
				   if(element.getId()!=null&&element.getValue() == null){
					   if(elementChild.getValue()!=null){
						   try{
							   value = value+Integer.valueOf(elementChild.getValue());
						   }catch(Exception e){
						   }
					   }
				   }
			   }
			   if(StringUtils.hasText(exportStr)){
				   exportStr = element.getUnit() != null || StringUtils.hasText(element.getUnit()) ?
						   element.getIntroduceDescription() + element.getValue()+element.getUnit()+"，其中：" + exportStr:
						   element.getIntroduceDescription() +"，其中：" + exportStr;
			   }
			   if(!String.valueOf(value).equals("0")){
				   element.setValue(String.valueOf(value));
			   }
		   }
		       elementDataGroups = element.getElementDataGroups();
		   if (elementDataGroups != null && elementDataGroups.size() > 0) {
				for(int i = 0;i<elementDataGroups.size();i++){
					exportStr = introduceService.getElementDataByElementDataGroups(element);
			     	}
			    }
		if(StringUtils.hasText(exportStr)){
			element.setElementDataStr(exportStr);
		}
	}

	/**
	 * 根据单元和项目从数据库构建带值的element,构建完整单元
	 *
	 * @param unit
	 * @return
	 */
	public Unit bulidElement(Unit unit) {
		List<ElementGroup> elementGroups = unit.getElementGroups();
		List<ElementDB> elementDBs = elementDBService.findByProject(unit
				.getProject().getId());
		if (elementDBs == null||elementDBs.size()==0) {
			return unit;
		}
		for (int i = 0; i < elementGroups.size(); i++) {
			for (int k = 0; k < elementGroups.get(i).getElements().size(); k++) {
				for (int j = 0; j < elementDBs.size(); j++) {
					if (elementDBs.get(j).getAlias().equals(elementGroups.get(i).getElements().get(k).getAlias())) {
						elementGroups.get(i).getElements().get(k).setValue(elementDBs.get(j).getValue());
						elementGroups.get(i).getElements()	.get(k)	.setElementDataStr(elementDBs.get(j).getElementDataStr());
						elementGroups.get(i).getElements().get(k).setProject(elementDBs.get(j).getProject());
						elementGroups.get(i).getElements().get(k).setId(elementDBs.get(j).getId());
						if (elementDataGroupService.findByElementDB(elementDBs.get(j)).size() != 0) {
							elementGroups.get(i).getElements().get(k).setElementDataGroups(elementDataGroupService.findByElementDB(elementDBs.get(j)));
						}
						unit.setElementGroups(elementGroups);
					}
				}
				if (elementGroups.get(i).getElements().get(k).getDescription() == null) {
					continue;
				}
				if (elementGroups.get(i).getElements().get(k).getDescription()
						.contains("CONTACTS")
						&& elementGroups.get(i).getElements().get(k)
								.getDescription().contains("name")) {
					contactService.contactNameSelect(elementGroups.get(i)
							.getElements().get(k), unit.getProject().getUser());
				}
				if (elementGroups.get(i).getElements().get(k).getDescription()
						.contains("CONTACTS")
						&& elementGroups.get(i).getElements().get(k)
								.getDescription().contains("company")) {
					contactService.contactCompanySelect(elementGroups.get(i)
							.getElements().get(k), unit.getProject().getUser());
				}
			}
		}
		return unit;
	}

	/**
	 * 保存项目进度
	 * @param unitAlias
	 * @param unitName
	 * @param project
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	public void doSaveProjectSchedule(String unitAlias,String unitName,Project project) {
		// 获取所有项目信息
		List<Info> infos = infoService.getInfos();
		List<String> selects = infos.get(13).getSelectValues();
		JSONArray jsonArray = new JSONArray();
		List<JSONObject> infoJsons = jsonArray.fromObject(project.getInfoStr());
		for(int i = 0;i<infoJsons.size();i++){
			if (selects.contains(unitName)){
				for (String select : selects) {
					if (unitName.equals(select)) {
						// 始终保存最后的进度，和不随编辑状态改变
						if(infoJsons.get(i).get("order").equals("13")){
							if (selects.indexOf(select) > selects.indexOf(infoJsons.get(i).get("value"))) {
								infoService.saveInfo(project, 13, select);
								break;
							}
						}
					}
				}
			}
			if (unitAlias.equals("3")) {
				if(infoJsons.get(i).get("order").equals("13")){
					infoService.saveInfo(project, 13,"施工阶段");
					break;
				}
			}
		}
	}
	/**
	 * 保存要素与要素数据
	 * @param object
	 */
	@SuppressWarnings("unchecked")
	public void doSaveElement(Map<String,Object> elementGroup, Unit unit, Object object) {
		Project project = projectService.find(unit.getProject().getId());
		int info;
		Element element;
		String description, elementAlias, value = null;
		Object objectValue, infoOrder, introduceDescription;
		List<Object> elements= (List<Object>) elementGroup.get("elements");
		List<String> aliases = new ArrayList<>();
		for (int k = 0; k < elements.size(); k++) {
			objectValue = ((Map<String, Object>) elements.get(k)).get("value");
			if (objectValue != null && objectValue.equals(",")) {
				objectValue = null;
			}
			elementAlias = ((Map<String, Object>) elements.get(k)).get("alias")
					.toString();
			infoOrder = ((Map<String, Object>) elements.get(k))
					.get("infoOrder");
			introduceDescription = ((Map<String, Object>) elements.get(k))
					.get("introduce");
			value = null;
			List<Object> elementDataGroups = (List<Object>) ((Map<String, Object>) elements
					.get(k)).get("elementDataGroups");
			if (objectValue != null && objectValue != "") {
				value = objectValue.toString();
				value = value.endsWith(",")?value.substring(0,value.length()-1):value;
				if (infoOrder != null) {
					info = Integer.valueOf(infoOrder.toString());
					infoService.saveInfo(project, info, value);
				}
				if (introduceDescription != null) {
					description = introduceDescription.toString();
					element = new Element();
					element.setIntroduceDescription(description);
					element.setValue(value);
				}
			}
			List<ElementDB> elementDBs = elementDBService.findByProject(
					project.getId(), elementAlias);
			ElementDB elementDB;
			if (elementDataGroups.size() == 0) {
				if (value == null) {
					if (elementDBs.size() == 0) {
						continue;
					}
				}
			}
			if (elementDBs.size() == 0) {
				elementDB = new ElementDB();
				elementDB.setValue(value);
				elementDB.setAlias(elementAlias);
				elementDB.setProject(project);
			} else {
				elementDB = elementDBs.get(0);
				elementDB.setValue(value);
				elementDB.setAlias(elementAlias);
				elementDB.setProject(project);
			}
			aliases.add(elementAlias);
			addLogTimeToProject(project,elementAlias,"element");
			// 保存要素数据和项目简介
			elementDataGroupService.doSaveElementDataGroup(elementDB, elementDataGroups);
			elementDBService.save(elementDB);
		}
		if(aliases.size()!=0){
			projectLogService.saveLog(project,object,aliases,value,null);
		}
		//保存项目进度
		doSaveProjectSchedule(unit.getAlias(),unit.getName(),project);
		//更新项目基本新信息
		projectService.save(project);
	}

	public void addLogTimeToProject(Project project,String elementAlias,String stage) {
		JSONObject object = JSONObject.fromObject(project.getLogStr());
		JSONObject jsonObject = new JSONObject();
		Object inviteElement = null;
		Object inviteFile = null;
		Object preparationElement = null;
		Object preparationFile = null;
		Object buildingElement = null;
		Object buildingFile = null;
		Object maintenanceElement = null;
		Object maintenanceFile = null;
		if(object.size()!=0){
			inviteElement = object.get("inviteElement");
			inviteFile =  object.get("inviteFile");
			preparationElement =  object.get("preparationElement");
			preparationFile =  object.get("preparationFile");
			buildingElement =  object.get("buildingElement");
			buildingFile = object.get("buildingFile");
			maintenanceElement = object.get("maintenanceElement");
			maintenanceFile =  object.get("maintenanceFile");
		}
		if(stage.equals("element")){
			if(elementAlias.substring(0,1).equals("1")){
				inviteElement=System.currentTimeMillis();
			}
			if(elementAlias.substring(0,1).equals("2")){
				preparationElement=System.currentTimeMillis();
			}
			if(elementAlias.substring(0,1).equals("3")){
				buildingElement=System.currentTimeMillis();
			}
			if(elementAlias.substring(0,1).equals("4")){
				maintenanceElement=System.currentTimeMillis();
			}
		}else{
			if(elementAlias.substring(0,1).equals("1")){
				inviteFile=System.currentTimeMillis();
			}
			if(elementAlias.substring(0,1).equals("2")){
				preparationFile=System.currentTimeMillis();
			}
			if(elementAlias.substring(0,1).equals("3")){
				buildingFile=System.currentTimeMillis();
			}
			if(elementAlias.substring(0,1).equals("4")){
				maintenanceFile=System.currentTimeMillis();
			}
		}
		if(inviteElement!=null){
			jsonObject.put("inviteElement",inviteElement);
		}
		if(inviteFile!=null){
			jsonObject.put("inviteFile",inviteFile);
		}
		if(preparationElement!=null){
			jsonObject.put("preparationElement",preparationElement);
		}
		if(preparationFile!=null){
			jsonObject.put("preparationFile",preparationFile);
		}
		if(buildingElement!=null){
			jsonObject.put("buildingElement",buildingElement );
		}
		if(buildingFile!=null){
			jsonObject.put("buildingFile", buildingFile);
		}
		if(maintenanceElement!=null){
			jsonObject.put("maintenanceElement",maintenanceElement );
		}
		if(maintenanceFile!=null){
			jsonObject.put("maintenanceFile",maintenanceFile );
		}
		if(jsonObject!=null&&!jsonObject.isEmpty()){
			project.setLogStr(jsonObject.toString());
		}
	}

	/**
	 * 判断用户权限
	 * @param unit
	 * @param user
     * @return
     */
	/*public boolean authorityJudge(Unit unit,User user){
		Project project = unit.getProject();
		ProjectVisit projectVisit=projectVisitService.makeByUser(project, user);
		if(unit.getAlias().trim().substring(0,1).equals("1")){
			if(projectVisit.getInviteElementUser()==null){
				return false;
			}
		}
		if(unit.getAlias().trim().substring(0,1).equals("2")){
			if(projectVisit.getPreparationElementUser()==null){
				return false;
			}
		}
		if(unit.getAlias().trim().substring(0,1).equals("3")){
			if(projectVisit.getBuildingElementUser()==null){
				return false;
			}
		}
		if(unit.getAlias().trim().substring(0,1).equals("4")){
			if(projectVisit.getMaintenanceElementUser()==null){
				return false;
			}
		}
		return true;
	}*/

//	/**
//	 * 保存文件编辑日志
//	 * @param simpleUser
//	 * @param map
//     * @return
//     */
//	public Message saveFileLog(User simpleUser, Map<String,Object> map){
//		Project project = projectService.find(Long.valueOf(map.get("id").toString()));
//		List<String> aliases = new ArrayList<>();
//		Object logType = map.get("type");
//		if (logType == null || map.get("alias") == null) {
//			return new Message(Message.Type.FAIL);
//		}
//		aliases.add(map.get("alias").toString());
//		ProjectLog.Type type = null;
//		if (logType.toString().equals("upload")) {
//			type=ProjectLog.Type.FILE_UPLOAD;
//		} else if (logType.toString().equals("download")) {
//			type = ProjectLog.Type.FILE_DOWNLOAD;
//		}
//		projectLogService.saveLog(project,simpleUser,aliases,map.get("fileNames").toString(),type);
//		List<ElementDB> elementDBs = elementDBService.findByProject(project.getId(), map.get("alias").toString());
//		ElementDB elementDB;
//		if(elementDBs.size()==0){
//			elementDB = new ElementDB();
//			elementDB.setAlias(map.get("alias").toString());
//			elementDB.setProject(project);
//		}else{
//			elementDB = elementDBs.get(0);
//		}
//		elementDBService.save(elementDB);
//		addLogTimeToProject(project,map.get("alias").toString(),"file");
//		return new Message(Message.Type.OK);
//	}
}
