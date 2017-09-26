package com.hysw.qqsl.cloud.service;


import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.entity.QQSLException;
import com.hysw.qqsl.cloud.entity.Setting;
import com.hysw.qqsl.cloud.entity.XMLFileException;
import com.hysw.qqsl.cloud.entity.build.AttribeGroup;
import com.hysw.qqsl.cloud.entity.build.Config;
import com.hysw.qqsl.cloud.entity.data.Attribe;
import com.hysw.qqsl.cloud.entity.data.Build;
import com.hysw.qqsl.cloud.entity.build.BuildGroup;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Cache;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.util.StringUtil;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by leinuo on 17-3-29.
 * 建筑物配制文件读取
 */
@Service("buildGroupService")
public class BuildGroupService {

    Log logger = LogFactory.getLog(getClass());
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private AttribeGroupService attribeGroupService;

    Setting setting = SettingUtils.getInstance().getSetting();

    List<String> names = Arrays.asList(CommonAttributes.BASETYPEC);
    List<String> types = Arrays.asList(CommonAttributes.BASETYPEE);

    /**
     * xml文件中读取到的建筑物结构
     */
    public List<BuildGroup> getBuildGroupsXml(){
        Cache cache = cacheManager.getCache("buildGroupsCache");
        net.sf.ehcache.Element element = cache.get("buildGroups");
        if(element==null){
            element = new net.sf.ehcache.Element("buildGroups", getBuildGroups(setting.getBuildsModel()));
            if(((List<BuildGroup>) element.getValue()).size()!=9){
                System.err.println("建筑物size错误！");
            }
            cache.put(element);
        }
        return (List<BuildGroup>) element.getValue();
    }

    /**
     * 构建完整建筑物结构
     */
    public List<BuildGroup> getCompleteBuildGroups(){
        Cache cache = cacheManager.getCache("buildGroupsCache");
        net.sf.ehcache.Element element = cache.get("completeBuildGroups");
        if(element==null){
            element = new net.sf.ehcache.Element("completeBuildGroups", BuildCompleteBuildGroups());
            cache.put(element);
        }
        return (List<BuildGroup>) element.getValue();
    }

    /**
     * 获取不含组的建筑物列表
     * @return
     */
    public List<Build> getBuilds(){
        Cache cache = cacheManager.getCache("buildsCache");
        net.sf.ehcache.Element element = cache.get("builds");
        if(element==null){
            List<Build> builds = new ArrayList<>();
            List<BuildGroup> completeBuildGroups = getCompleteBuildGroups();
            for (BuildGroup completeBuildGroup : completeBuildGroups) {
                builds.addAll(completeBuildGroup.getBuilds());
            }
            element = new net.sf.ehcache.Element("builds", builds);
            cache.put(element);
        }
        return (List<Build>) element.getValue();
    }

    /**
     * 获取动态地质属性带有code的建筑物列表
     * @return
     */
    public List<Build> getBuildsDynamic(){
        Cache cache = cacheManager.getCache("buildsCache");
        net.sf.ehcache.Element element = cache.get("buildsDynamic");
        if(element==null){
            List<Build> builds = new ArrayList<>();
            List<BuildGroup> completeBuildGroups = getCompleteBuildGroups();
            for (BuildGroup completeBuildGroup : completeBuildGroups) {
                builds.addAll(completeBuildGroup.getBuilds());
            }
            AttribeGroup geologyDynamicGroup = attribeGroupService.getGeologyDynamicGroups("1");
            for(int i=0;i<builds.size();i++){
                builds.get(i).setGeologyAttribeGroup(geologyDynamicGroup);
            }
            element = new net.sf.ehcache.Element("buildsDynamic", builds);
            cache.put(element);
        }
        return (List<Build>) element.getValue();
    }

    /**
     * 构建完全属性的建筑物结构
     * @return
     */
    private List<BuildGroup> BuildCompleteBuildGroups(){
        List<BuildGroup> buildGroups = getBuildGroupsXml();
        Build build;
        AttribeGroup attribeGroup;
        List<Build> builds;
        BuildGroup buildGroup;
        for(int i=0;i<buildGroups.size();i++){
            buildGroup = buildGroups.get(i);
            builds = buildGroup.getBuilds();
            for(int j=0;j<builds.size();j++){
                build = builds.get(j);
                if(StringUtils.hasText(build.getMater())){
                    attribeGroup = attribeGroupService.getMaterGroup(build.getMater());
                    build.setMaterAttribeGroup(attribeGroup);
                }
                if(StringUtils.hasText(build.getDimensions())){
                    attribeGroup = attribeGroupService.getDimensionsGroup(build.getDimensions());
                    build.setDimensionsAttribeGroup(attribeGroup);
                }
                if(StringUtils.hasText(build.getHydraulics())){
                    attribeGroup = attribeGroupService.getHydraulicsGroup(build.getHydraulics());
                    build.setHydraulicsAttribeGroup(attribeGroup);
                }
                if(StringUtils.hasText(build.getStructure())){
                    attribeGroup = attribeGroupService.getStructureGroup(build.getStructure());
                    build.setStructureAttribeGroup(attribeGroup);
                }
                build.setGeologyAttribeGroup(attribeGroupService.getGeologyGroup("1"));
            }
            buildGroup.setBuilds(builds);
        }
        return buildGroups;
    }

    /**
     * 将设计和外业的建筑物列表分开
     * @param flag
     * @return
     */
    public List<BuildGroup> getBuildGroups(boolean flag){
        List<BuildGroup> buildGroups = (List<BuildGroup>)SettingUtils.objectCopy(getCompleteBuildGroups());
        if(flag){ //设计
            buildGroups = buildGroups.subList(0,6);
        }else{
            buildGroups.remove(4);
        }
        return buildGroups;
    }

    private List<BuildGroup> getBuildGroups(String XMLName) {
        List<BuildGroup> buildGroups = null;
        try {
            buildGroups = readBuildGroups(XMLName);
        } catch (Exception e) {
            logger.error(XMLName+","+e.getMessage());
            e.printStackTrace();
        }
        return buildGroups;
    }

    /**
     * 读取建筑物xml文件
     *
     * @return
     */
    private List<BuildGroup> readBuildGroups(String XMLName)
            throws XMLFileException, DocumentException {
        List<BuildGroup> buildGroups = new ArrayList<>();
        Element root, element;
        List<Element> elements, elementList;
        BuildGroup buildGroup;
        Build build;
        List<Build> builds;
        root = SettingUtils.getInstance().getRootElement(XMLName);
        elements = root.elements();
        List<String> buildAilass = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {
            element = elements.get(i);
            buildGroup = new BuildGroup();
            buildGroup.setName(element.attributeValue("name"));
            buildGroup.setAlias(element.attributeValue("alias"));
            builds = new ArrayList<>();
            elementList = element.elements();
            for (int j = 0; j < elementList.size(); j++) {
                element = elementList.get(j);
                build = new Build();
                build.setAlias(element.attributeValue("alias"));
                build.setName(element.attributeValue("name"));
                build.setMater(element.attributeValue("mater"));
                build.setDimensions(element.attributeValue("dimension"));
                build.setHydraulics(element.attributeValue("hydraulics"));
                build.setGeology(element.attributeValue("geology"));
                build.setStructure(element.attributeValue("structure"));
                buildAliasCheck(build.getAlias(),buildAilass);
                setType(build);
                builds.add(build);
            }
            buildGroup.setBuilds(builds);
            buildGroups.add(buildGroup);
        }
        return buildGroups;
    }

    private void setType(Build build) throws XMLFileException{
        String name = build.getName();
        if(names.contains(name)){
            String type = types.get(names.indexOf(name));
            build.setType(Config.CommonType.valueOf(type));
            build.setPy(type.substring(0,1));
        }else throw new XMLFileException("建筑物出现未知类型！" + name);

    }

    private void buildAliasCheck(String buildAlias,List<String> buildAliass) throws XMLFileException{
        if(!buildAliass.contains(buildAlias)){
            buildAliass.add(buildAlias);
        }else throw new XMLFileException("建筑物别名重复:"+buildAlias);
    }

    /**
     * 构建建筑物json数据
     * @param isSimple
     * @return
     */
    public JSONArray getBuildJson(boolean isSimple){
        List<BuildGroup> buildGroups = getBuildGroups(false);
        JSONArray jsonArray = new JSONArray();
        JSONArray buildArray;
        JSONObject buildGroupJson,buildJson;
        Build build;
        for(int i=0;i<buildGroups.size();i++){
            buildGroupJson = new JSONObject();
            buildGroupJson.put("alias",buildGroups.get(i).getAlias());
            buildGroupJson.put("name",buildGroups.get(i).getName());
            buildArray = new JSONArray();
            for(int j=0;j<buildGroups.get(i).getBuilds().size();j++){
                buildJson = new JSONObject();
                build = buildGroups.get(i).getBuilds().get(j);
                buildJson.put("alias",build.getAlias());
                buildJson.put("name",build.getName());
                buildJson.put("py",build.getPy());
                buildJson.put("type",build.getType());
                if(!isSimple){
                    //构建完整的建筑物属性数据
                    getAttribeGroupJson(build,buildJson);
                }
                buildArray.add(buildJson);
            }
            buildGroupJson.put("builds",buildArray);
            jsonArray.add(buildGroupJson);
        }
        return jsonArray;
    }

    /**
     * 构建建筑物下的各个属性的json数据
     * @param build
     * @param buildJson
     */
    private void getAttribeGroupJson(Build build,JSONObject buildJson){
        getAttribeGroupJson(build.getMaterAttribeGroup(),buildJson,"materAttribeGroup");
        getAttribeGroupJson(build.getHydraulicsAttribeGroup(),buildJson,"hydraulicsAttribeGroup");
        getAttribeGroupJson(build.getDimensionsAttribeGroup(),buildJson,"dimensionsAttribeGroup");
        getAttribeGroupJson(build.getStructureAttribeGroup(),buildJson,"structureAttribeGroup");
    //  getAttribeGroupJson(build.getGeologyAttribeGroup(),buildJson,"geologyAttribeGroup");
    }

    /**
     * 构建AttribeGroup的json数据的通用方法
     * @param attribeGroup
     * @param buildJson
     * @param name
     */
    public void getAttribeGroupJson(AttribeGroup attribeGroup,JSONObject buildJson,String name){
        if(attribeGroup!=null){
            JSONObject attribeGroupJson = new JSONObject();
            attribeGroupJson.put("alias",attribeGroup.getAlias());
          //  logger.info(attribeGroup.getAlias());
            attribeGroupJson.put("name",attribeGroup.getName());
            if(attribeGroup.getStatus()!=null){
                /*if(attribeGroup.getStatus().equals(Config.Status.SELECT)){
                    attribeGroupJson.put("selects",attribeGroup.getSelects());
                }*/
                attribeGroupJson.put("status",attribeGroup.getStatus());
            }
            if(attribeGroup.getAttribes()!=null&&attribeGroup.getAttribes().size()>0){
                attribeGroupJson.put("attribes",getAttribeJsons(attribeGroup.getAttribes()));
            }
            if(attribeGroup.getChilds()!=null&&attribeGroup.getChilds().size()>0){
                attribeGroupJson.put("childs",getchildJsons(attribeGroup.getChilds()));
            }
            buildJson.put(name,attribeGroupJson);
        }
    }

    /**
     * 获取地质属性的json数据
     * @return
     */
    public JSONObject getGeologyJson(){
     JSONObject jsonObject = new JSONObject();
     AttribeGroup geologyGroup = attribeGroupService.getGeologyGroup("1");
     getAttribeGroupJson(geologyGroup,jsonObject,"geologyAttribeGroup");
     return  (JSONObject) jsonObject.get("geologyAttribeGroup");
    }

    /**
     * 构建属性组下所有属性的json数据
     * @param attribes
     * @return
     */
    private JSONArray getAttribeJsons(List<Attribe> attribes){
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        Attribe attribe;
        for(int i=0;i<attribes.size();i++){
           jsonObject = new JSONObject();
           attribe = attribes.get(i);
            jsonObject.put("name",attribe.getName());
            jsonObject.put("alias",attribe.getAlias());
            jsonObject.put("status",attribe.getStatus());
            jsonObject.put("select",attribe.getSelect());
            jsonObject.put("selects",attribe.getSelects());
            jsonObject.put("type",attribe.getType());
            jsonObject.put("unit",attribe.getUnit());
            jsonArray.add(jsonObject);

        }
        return jsonArray;
    }

    /**
     * 递归构建子级属性组json数据
     * @param childs
     * @return
     */
    private JSONArray getchildJsons(List<AttribeGroup> childs){
        JSONArray jsonArray = new JSONArray();
        AttribeGroup attribeGroup;
        JSONObject childJson;
        for(int i=0;i<childs.size();i++){
            attribeGroup = childs.get(i);
            childJson = new JSONObject();
            childJson.put("name",attribeGroup.getName());
            childJson.put("alias",attribeGroup.getAlias());
            childJson.put("status",attribeGroup.getStatus());
            //childJson.put("selects",attribeGroup.getSelects());
            if(attribeGroup.getAttribes()!=null&&attribeGroup.getAttribes().size()>0){
                childJson.put("attribes",getAttribeJsons(attribeGroup.getAttribes()));
            }
            if(attribeGroup.getChilds()!=null&&attribeGroup.getChilds().size()>0){
                JSONArray childJsons = getchildJsons(attribeGroup.getChilds());
                childJson.put("childs",childJsons);
            }
            jsonArray.add(childJson);
        }
        return jsonArray;
    }
}
