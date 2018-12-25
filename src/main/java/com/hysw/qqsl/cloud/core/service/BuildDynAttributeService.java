package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.BuildDynAttributeDao;
import com.hysw.qqsl.cloud.core.entity.buildModel.AttributeGroup;
import com.hysw.qqsl.cloud.core.entity.data.BuildAttribute;
import com.hysw.qqsl.cloud.core.entity.data.BuildDynAttribute;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            jsonObject.put("groupAlias", attributeGroup.getGroupAlias());
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
}
