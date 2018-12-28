package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.BuildDynAttributeDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.buildModel.AttributeGroup;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Administrator
 * @since 2018/12/25
 */
@Service("buildDynAttributeService")
public class BuildDynAttributeService extends BaseService<BuildDynAttribute, Long> {
    @Autowired
    private BuildDynAttributeDao buildDynAttributeDao;
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    public void setBaseDao(BuildDynAttributeDao buildDynAttributeDao) {
        super.setBaseDao(buildDynAttributeDao);
    }

    public List<AttributeGroup> getAttributeGroups(){
        Cache cache = cacheManager.getCache("dynCache");
        net.sf.ehcache.Element element = cache.get("dyn");
        if (element == null) {
            try {
                element = new net.sf.ehcache.Element("dyn", initDynModel(SettingUtils.getInstance().getSetting().getDyn()));
                cache.put(element);
                return (List<AttributeGroup>) element.getValue();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return (List<AttributeGroup>) element.getValue();
    }

    private List<AttributeGroup> initDynModel(String xml) {
        Element root = null;
        try {
            root = SettingUtils.getInstance().getRootElement(xml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        AttributeGroup attributeGroup;
        List<AttributeGroup> attributeGroups = new ArrayList<>();
        List<Element> elements = SettingUtils.getInstance().getElementGroupList(root);
        for (Element element : elements) {
            attributeGroup = new AttributeGroup();
            if (element.attributeValue("name") != null&&!element.attributeValue("name").equals("")) {
                attributeGroup.setName(element.attributeValue("name"));
            }
            if (element.attributeValue("dyn") != null&&!element.attributeValue("dyn").equals("")) {
                attributeGroup.setDyn(Boolean.valueOf(element.attributeValue("dyn")));
            }
            if (element.attributeValue("groupAlias") != null&&!element.attributeValue("groupAlias").equals("")) {
                attributeGroup.setGroupAlias(element.attributeValue("groupAlias"));
            }
            initAttributes(element.elements(),attributeGroup);
            attributeGroups.add(attributeGroup);
        }
        return attributeGroups;
    }

    public void initAttributes(List<Element> elements, AttributeGroup attributeGroup){
        List<BuildAttribute> buildAttributes = new ArrayList<>();
        BuildAttribute buildAttribute;
        for (Element element : elements) {
            buildAttribute = new BuildAttribute();
            if (element.attributeValue("alias") != null && !element.attributeValue("alias").equals("")) {
                buildAttribute.setAlias(element.attributeValue("alias"));
            }
            if (element.attributeValue("name") != null && !element.attributeValue("name").equals("")) {
                buildAttribute.setName(element.attributeValue("name"));
            }
            if (element.attributeValue("type") != null && !element.attributeValue("type").equals("")) {
                buildAttribute.setType(BuildAttribute.Type.valueOf(element.attributeValue("type").toUpperCase()));
            }
            if (element.attributeValue("unit") != null && !element.attributeValue("unit").equals("")) {
                buildAttribute.setUnit(element.attributeValue("unit"));
            }
            if (element.attributeValue("select") != null && !element.attributeValue("select").equals("")) {
                buildAttribute.setSelects(Arrays.asList(element.attributeValue("select").split(",")));
            }
            if (element.attributeValue("locked") != null && !element.attributeValue("locked").equals("")) {
                buildAttribute.setLocked(Boolean.valueOf(element.attributeValue("locked")));
            }
            if (element.attributeValue("formula") != null && !element.attributeValue("formula").equals("")) {
                buildAttribute.setFormula(element.attributeValue("formula"));
            }
            if (element.attributeValue("fieldName") != null && !element.attributeValue("fieldName").equals("")) {
                buildAttribute.setFieldName(element.attributeValue("fieldName"));
            }
            buildAttributes.add(buildAttribute);
        }
        attributeGroup.setBuildAttributes(buildAttributes);
    }

    public AttributeGroup getAttributeGroup(String alias) {
        List<AttributeGroup> attributeGroups = getAttributeGroups();
        for (AttributeGroup attributeGroup : attributeGroups) {
            if (attributeGroup.getGroupAlias().equals(alias)) {
                return attributeGroup;
            }
        }
        return null;
    }

    public JSONArray toJSON(AttributeGroup attributeGroup) {
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        for (BuildAttribute buildAttribute : attributeGroup.getBuildAttributes()) {
            jsonObject = new JSONObject();
            jsonObject.put("id", buildAttribute.getId());
            jsonObject.put("name", buildAttribute.getName());
            jsonObject.put("alias", buildAttribute.getAlias());
//            jsonObject.put("groupAlias", attributeGroup.getGroupAlias());
            jsonObject.put("type", buildAttribute.getType());
            jsonObject.put("value", buildAttribute.getValue());
            jsonObject.put("formula", buildAttribute.getFormula());
            if (buildAttribute.getSelects() != null && buildAttribute.getSelects().size() != 0) {
                jsonObject.put("selects", buildAttribute.getSelects());
            }
            if (buildAttribute.getUnit() != null && !buildAttribute.getUnit().equals("")) {
                jsonObject.put("unit", buildAttribute.getUnit());
            }
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public List<BuildDynAttribute> findByBuild(Build build) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("build", build));
        return  buildDynAttributeDao.findList(0, null, filters);
    }

    public JSONArray toJSON(List<BuildDynAttribute> dynAttributeList) {
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        Map<Integer, List<BuildDynAttribute>> map = new TreeMap<>(
                new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        for (BuildDynAttribute buildDynAttribute : dynAttributeList) {
            List<BuildDynAttribute> buildDynAttributes = map.get(buildDynAttribute.getCode());
            if (buildDynAttributes == null) {
                buildDynAttributes = new ArrayList<>();
                buildDynAttributes.add(buildDynAttribute);
                map.put(buildDynAttribute.getCode(), buildDynAttributes);
            } else {
                buildDynAttributes.add(buildDynAttribute);
                map.put(buildDynAttribute.getCode(), buildDynAttributes);
            }
        }
        JSONArray jsonArray1;
        for (Map.Entry<Integer, List<BuildDynAttribute>> entry : map.entrySet()) {
            jsonArray1 = new JSONArray();
            List<BuildDynAttribute> dynAttributes = entry.getValue();
            if (dynAttributes == null || dynAttributes.size() == 0) {
                continue;
            }
            AttributeGroup attributeGroup = getAttributeGroup(dynAttributes.get(0).getGroupAlias());
            for (BuildAttribute buildAttribute : attributeGroup.getBuildAttributes()) {
                for (BuildDynAttribute dynAttribute : dynAttributes) {
                    if (buildAttribute.getAlias().equals(dynAttribute.getAlias())) {
                        jsonObject = new JSONObject();
                        jsonObject.put("id", dynAttribute.getId());
                        jsonObject.put("name", buildAttribute.getName());
                        jsonObject.put("alias", buildAttribute.getAlias());
                        //            jsonObject.put("groupAlias", attributeGroup.getGroupAlias());
                        jsonObject.put("type", buildAttribute.getType());
                        jsonObject.put("value", buildAttribute.getValue());
                        jsonObject.put("formula", buildAttribute.getFormula());
                        jsonObject.put("code", dynAttribute.getCode());
                        jsonObject.put("value", dynAttribute.getValue());
                        if (buildAttribute.getSelects() != null && buildAttribute.getSelects().size() != 0) {
                            jsonObject.put("selects", buildAttribute.getSelects());
                        }
                        if (buildAttribute.getUnit() != null && !buildAttribute.getUnit().equals("")) {
                            jsonObject.put("unit", buildAttribute.getUnit());
                        }
                        jsonArray1.add(jsonObject);
                    }
                }
            }
            jsonArray.add(jsonArray1);
        }
        return jsonArray;
    }

    public List<BuildDynAttribute> findByBuildAndCodeAndGroupAlias(Build build, Object code, Object groupAlias) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("build", build));
        filters.add(Filter.eq("code", code));
        filters.add(Filter.eq("groupAlias", groupAlias));
        return buildDynAttributeDao.findList(0, null, filters);
    }

    public void changeCode(Build build, Object code, Object groupAlias) {
        List<BuildDynAttribute> buildDynAttributes = findByBuildAndCodeAndGroupAlias(build, Integer.valueOf(code.toString()) + 1, groupAlias);
        if (buildDynAttributes.size() == 0) {
            return;
        }
        for (BuildDynAttribute buildDynAttribute : buildDynAttributes) {
            buildDynAttribute.setCode(Integer.valueOf(code.toString()));
            save(buildDynAttribute);
        }
        changeCode(build,Integer.valueOf(code.toString()) + 1,groupAlias);
    }


    public JSONArray toJSONSimple(List<BuildDynAttribute> dynAttributeList) {
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        Map<Integer, List<BuildDynAttribute>> map = new TreeMap<>(
                new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return o1.compareTo(o2);
                    }
                });
        for (BuildDynAttribute buildDynAttribute : dynAttributeList) {
            List<BuildDynAttribute> buildDynAttributes = map.get(buildDynAttribute.getCode());
            if (buildDynAttributes == null) {
                buildDynAttributes = new ArrayList<>();
                buildDynAttributes.add(buildDynAttribute);
                map.put(buildDynAttribute.getCode(), buildDynAttributes);
            } else {
                buildDynAttributes.add(buildDynAttribute);
                map.put(buildDynAttribute.getCode(), buildDynAttributes);
            }
        }
        for (Map.Entry<Integer, List<BuildDynAttribute>> entry : map.entrySet()) {
            List<BuildDynAttribute> dynAttributes = entry.getValue();
            if (dynAttributes == null || dynAttributes.size() == 0) {
                continue;
            }
            AttributeGroup attributeGroup = getAttributeGroup(dynAttributes.get(0).getGroupAlias());
            jsonObject = new JSONObject();
            for (BuildAttribute buildAttribute : attributeGroup.getBuildAttributes()) {
                for (BuildDynAttribute dynAttribute : dynAttributes) {
                    if (buildAttribute.getAlias().equals(dynAttribute.getAlias())) {
                        jsonObject.put(dynAttribute.getAlias(), dynAttribute.getValue());
                    }
                }
            }
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }
}
