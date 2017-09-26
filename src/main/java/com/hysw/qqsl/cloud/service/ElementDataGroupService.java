package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.controller.Message;
import com.hysw.qqsl.cloud.dao.ElementDataGroupDao;
import com.hysw.qqsl.cloud.entity.*;
import com.hysw.qqsl.cloud.entity.build.CoordinateBase;
import com.hysw.qqsl.cloud.entity.build.Gps;
import com.hysw.qqsl.cloud.entity.build.Graph;
import com.hysw.qqsl.cloud.entity.data.*;
import com.hysw.qqsl.cloud.entity.element.ElementData;
import com.hysw.qqsl.cloud.util.PositionUtil;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 *  要素数据组
 *  @author yutisn
 *  @since 2016年6月23日
 */
@Service("elementDataGroupService")
public class ElementDataGroupService extends BaseService<ElementDataGroup, Long> {
    @Autowired
    private ElementDataGroupDao elementDataGroupDao;
    @Autowired
    private ElementDBService elementDBService;
    @Autowired
    private CoordinateService coordinateService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private FieldService fieldService;
    @Autowired
    public void setBaseDao(ElementDataGroupDao elementDataGroupDao) {
        super.setBaseDao(elementDataGroupDao);
    }

    Setting setting=SettingUtils.getInstance().getSetting();
    final String DEFAULTELEMENTDATATYPE="NUMBER";

    public List<ElementDataGroup> getElementDataSimpleGroups() {
        Cache cache = cacheManager.getCache("elementDataGroupsCache");
        net.sf.ehcache.Element element = cache.get("simpleGroups");
        if(element==null){
            try {
                element = new net.sf.ehcache.Element("simpleGroups", makeElementDataGroups(setting.getElementDataSimpleModel()));
                cache.put(element);
            } catch (XMLFileException e) {
                e.printStackTrace();
            }
        }
        return (List<ElementDataGroup>) element.getValue();
    }

    /**
     * 测试要素数据缓存的刷新
     * @return
     */
    public List<ElementDataGroup> testGetSimpleElementDataGroups(){
        return this.getElementDataSimpleGroups();
    }

    /**
     * 读出要素数据xml
     * @param elementData1XMLName
     * @throws XMLFileException
     */
    @SuppressWarnings("unchecked")
    public List<ElementDataGroup> makeElementDataGroups(String elementData1XMLName) throws XMLFileException{
        List<ElementDataGroup> elementDataGroups = new ArrayList<>();
        List<ElementData> elementDatas;
        ElementData elementData;
        ElementDataGroup elementDataGroup;
        Element root;
        try {
            root = SettingUtils.getInstance().getRootElement(
                    elementData1XMLName);
        } catch (DocumentException e) {
            return elementDataGroups;
        }
        List<Element> xmlElementData1s = root.elements();
       for(int i = 0;i<xmlElementData1s.size();i++){
            elementDataGroup = new ElementDataGroup();
           if(xmlElementData1s.get(i).attributeValue("name")!=null){
                elementDataGroup.setName(xmlElementData1s.get(i).attributeValue("name"));
            }
           if(xmlElementData1s.get(i).attributeValue("type")!=null){
               elementDataGroup.setDataType(ElementDataGroup.DataType.valueOf(xmlElementData1s.get(i).attributeValue("type").toUpperCase()));
           }
           List<Element> elements = xmlElementData1s.get(i).elements();
           elementDatas = new ArrayList<>();
           for (int i1 = 0; i1 < elements.size(); i1++) {
               elementData = new ElementData();
               if(elements.get(i1).attributeValue("name")!=null){
                   elementData.setName(elements.get(i1).attributeValue("name"));
               }
               if(elements.get(i1).attributeValue("unit")!=null){
                   elementData.setUnit(elements.get(i1).attributeValue("unit"));
               }
               if(elements.get(i1).attributeValue("description")!=null){
                   elementData.setDescription(elements.get(i1).attributeValue("description"));
               }
               if(elements.get(i1).attributeValue("type")!=null){
                   elementData.setType(ElementData.Type.valueOf(elements.get(i1).attributeValue("type").toUpperCase()));
               }else{
                   elementData.setType(ElementData.Type.valueOf(DEFAULTELEMENTDATATYPE));
               }
               elementDatas.add(elementData);
               if(elements.get(i1).elements().size()!=0){
                   readRoot(elements.get(i1),elementDatas,elementData);
               }
           }
           elementDataGroup.setElementDatas(elementDatas);
           elementDataGroups.add(elementDataGroup);
       }
        return elementDataGroups;
    }

    /**
     * 递归读取子集xml
     * @param elements1
     * @param elementDatas
     * @param elementData
     */
    private void readRoot(Element elements1,List<ElementData> elementDatas,ElementData elementData) {
        ElementData elementData1;
        List<Element> elements = elements1.elements();
        for (int i = 0; i< elements.size(); i++) {
            elementData1 = new ElementData();
            if(elements.get(i).attributeValue("name")!=null){
                elementData1.setName(elements.get(i).attributeValue("name"));
            }
            if(elements.get(i).attributeValue("unit")!=null){
                elementData1.setUnit(elements.get(i).attributeValue("unit"));
            }
            if(elements.get(i).attributeValue("description")!=null){
                elementData1.setDescription(elements.get(i).attributeValue("description"));
            }
            if(elements.get(i).attributeValue("type")!=null){
                elementData1.setType(ElementData.Type.valueOf(elements.get(i).attributeValue("type").toUpperCase()));
            }else{
                elementData1.setType(ElementData.Type.valueOf(DEFAULTELEMENTDATATYPE));
            }
            elementData1.setParent(elementData);
            elementDatas.add(elementData1);
            if(elements.get(i).elements().size()!=0){
                readRoot(elements.get(i),elementDatas,elementData1);
            }
        }
    }

    /**
     * 创建要素数据
     * @param elementAlias
     * @param elementDataGroupName
     * @param projectId
     * @return
     */
    public ElementDataGroup makeElementDataGroup(String elementAlias, String elementDataGroupName, String elementDataGroupType, Long projectId){
        List<ElementDB> elementDBs = elementDBService.findByProject(projectId, elementAlias);
        ElementDB elementDB ;
        if(elementDBs.size()==1){
            elementDB = elementDBs.get(0);
        }else{
            elementDB = new ElementDB();
            elementDB.setAlias(elementAlias);
            elementDB.setProject(projectService.find(projectId));
            elementDBService.save(elementDB);
        }
        List<ElementDataGroup> elementDataGroups2= getElementDataSimpleGroups();
        List<ElementDataGroup> elementDataGroups1=new ArrayList<>();
        ElementDataGroup elementDataGroup = null;
        for (int i = 0; i < elementDataGroups2.size(); i++) {
            if(elementDataGroups2.get(i).getDataType().toString().equals(elementDataGroupType)){
                elementDataGroups1.add(elementDataGroups2.get(i));
            }
        }
        if(elementDataGroups1.size()==1&&elementDataGroups1.get(0).getName()==null){
            elementDataGroup = (ElementDataGroup) SettingUtils.objectCopy(elementDataGroups1.get(0));
            elementDataGroup.setName(elementDataGroupName);
            elementDataGroup.getElementDatas().get(0).setName(elementDataGroupName);
        }else{
            for (int k = 0; k < elementDataGroups1.size(); k++) {
                if(elementDataGroups1.get(k).getName().equals(elementDataGroupName)){
                    elementDataGroup = (ElementDataGroup) SettingUtils.objectCopy(elementDataGroups1.get(k));
                    break;
                }
            }
        }
        elementDataGroup.setElementDB(elementDB);
        return elementDataGroup;
    }

    /**
     * 删除要素数据
     * @param id
     */
    public void remove(Long id) {
        ElementDataGroup elementDataGroup = find(id);
        remove(elementDataGroup);
    }

    /**
     * 保存要素数据
     * @param elementDB
     * @param elementDataGroups
     */
    public void doSaveElementDataGroup(ElementDB elementDB, List<Object> elementDataGroups) {
        for (Object elementDataGroup : elementDataGroups) {
            JSONObject jsonObject = JSONObject.fromObject(elementDataGroup);
            Object id = jsonObject.get("id");
            Object name = jsonObject.get("name");
            Object dataType = jsonObject.get("dataType");
            List<Object> elementDatas = (List<Object>) jsonObject.get("elementDatas");
            if (id == null || name == null || dataType == null) {
                continue;
            }
            ElementDataGroup elementDataGroup1 = find(Long.valueOf(id.toString()));
            elementDataGroup1.setName(name.toString());
            elementDataGroup1.setElementDB(elementDB);
            elementDataGroup1.setDataType(com.hysw.qqsl.cloud.entity.data.ElementDataGroup.DataType.valueOf(dataType.toString()));
            saveElementDatas(elementDataGroup1, elementDatas);
        }
    }

    /**
     * 保存要素数据
     * @param elementDataGroup
     * @param objectElementData
     * @return
     */
    private void saveElementDatas(ElementDataGroup elementDataGroup, List<Object> objectElementData) {
        if(objectElementData!=null&&objectElementData.size()!=0){
            List<JSONObject> elementDatas=new ArrayList<>();
            JSONObject elementData;
            String introDataStr="";
            for (int j = 0; j < objectElementData.size(); j++) {
                Object dataName=((Map<String,Object>) objectElementData.get(j)).get("name");
                Object dataValue=((Map<String,Object>) objectElementData.get(j)).get("value");
                Object dataUnit=((Map<String,Object>) objectElementData.get(j)).get("unit");
                Object dataDescription=((Map<String,Object>) objectElementData.get(j)).get("description");
                Object dataType=((Map<String,Object>) objectElementData.get(j)).get("type");
                Object dataParent= ((Map<String,Object>) objectElementData.get(j)).get("parent");
                if(dataName==null||dataName.toString().trim().equals("")||dataName.toString().trim().equals("null")||dataType==null||dataType.toString().trim().equals("")||dataType.toString().trim().equals("null") ){
                    continue;
                }
                elementData=new JSONObject();
                elementData.put("name",dataName.toString());
                if(dataValue!=null&&!dataValue.toString().trim().equals("")&&!dataValue.toString().trim().equals("null")){
                    elementData.put("value",dataValue.toString());
                }
                if(dataUnit!=null&&!dataUnit.toString().trim().equals("")&&!dataUnit.toString().trim().equals("null")){
                    elementData.put("unit",dataUnit.toString());
                }
                if(dataDescription!=null&&!dataDescription.toString().trim().equals("")&&!dataDescription.toString().trim().equals("null")){
                    elementData.put("description",dataDescription);
                }
                elementData.put("type",dataType.toString().toUpperCase());
                if(dataParent!=null&&!dataParent.toString().trim().equals("")&&!dataParent.toString().trim().equals("null")){
                    JSONObject parent;
                    Object parentName=((Map<String,Object>)dataParent).get("name");
                    Object parentValue=((Map<String,Object>) dataParent).get("value");
                    Object parentAlias=((Map<String,Object>) dataParent).get("alias");
                    Object parentUnit=((Map<String,Object>) dataParent).get("unit");
                    Object parentDescription=((Map<String,Object>) dataParent).get("description");
                    Object parentType=((Map<String,Object>) dataParent).get("type");
                    parent=new JSONObject();
                    parent.put("name",parentName.toString());
                    parent.put("value",parentValue.toString());
                    parent.put("alias",parentAlias.toString());
                    parent.put("unit",parentUnit.toString());
                    parent.put("description",parentDescription.toString());
                    parent.put("type",parentType.toString().toUpperCase());
                    elementData.put("parent", parent);
                }
                introDataStr=makeDataIntroStr(introDataStr,dataName,dataValue,dataUnit,dataDescription);
                if(!elementData.isEmpty()){
                    elementDatas.add(elementData);
                }
            }
            elementDataGroup.setIntroDataStr(introDataStr);
            if(elementDatas.size()!=0){
                elementDataGroup.setDataStr(elementDatas.toString());
            }else{
                elementDataGroup.setDataStr(null);
            }
        }
    }

    /**
     * 保存要素数据简介
     * @param introDataStr
     * @param dataValue
     * @param dataUnit
     * @param dataDescription
     * @return
     */
    private String  makeDataIntroStr(String introDataStr,Object dataName,Object dataValue, Object dataUnit, Object dataDescription) {
        if (dataValue!=null&&!dataValue.toString().trim().equals("")&&!dataValue.toString().trim().equals("0")&&!dataValue.toString().trim().equals("null")){
            if(introDataStr.length()!=0){
                introDataStr=introDataStr.substring(0,introDataStr.length()-1)+"，";
            }
            if(dataDescription!=null&&!dataDescription.toString().equals("null")){
                introDataStr+=dataDescription.toString()+dataValue.toString()+"；";
            }else{
                introDataStr+=dataName.toString()+dataValue.toString()+"；";
            }
        }else{
            return introDataStr;
        }
        if(dataUnit!=null&&!dataUnit.toString().trim().equals("")&&!dataUnit.toString().trim().equals("null")){
            introDataStr=introDataStr.substring(0,introDataStr.length()-1);
            introDataStr+=dataUnit+"；";
        }
        return introDataStr;
    }

    /**
     * 根据elementDB查询要素数据
     * @param elementDB
     * @return
     */
    public List<ElementDataGroup> findByElementDB(ElementDB elementDB) {
        if (elementDB == null) {
            return null;
        }
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(Filter.eq("elementDB", elementDB.getId()));
        List<ElementDataGroup> objectElementDataGroups = elementDataGroupDao.findList(0, null,
                filters);
        return objectElementDataGroups;
    }

    public  void saveElementDataGroup(ElementDataGroup elementDataGroup){
        List<ElementData> elementDatas=elementDataGroup.getElementDatas();
        List<JSONObject> objects=new ArrayList<>();
        for (int i = 0; i < elementDatas.size(); i++) {
            JSONObject obj=new JSONObject();
            if(elementDatas.get(i).getName()!=null){
                obj.put("name",elementDatas.get(i).getName());
            }
            if(elementDatas.get(i).getValue()!=null){
                obj.put("value",elementDatas.get(i).getValue());
            }
            if(elementDatas.get(i).getUnit()!=null){
                obj.put("unit",elementDatas.get(i).getUnit());
            }
            if(elementDatas.get(i).getDescription()!=null){
                obj.put("description",elementDatas.get(i).getDescription());
            }
            if(elementDatas.get(i).getType()!=null){
                obj.put("type",elementDatas.get(i).getType());
            }
            if(elementDatas.get(i).getParent()!=null){
                obj.put("parent",elementDatas.get(i).getParent());
            }
            objects.add(obj);
        }
        elementDataGroup.setDataStr(objects.toString());
        super.save(elementDataGroup);
    }



}
