package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.entity.QQSLException;
import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.core.entity.XMLFileException;
import com.hysw.qqsl.cloud.core.entity.builds.AttributeGroup;
import com.hysw.qqsl.cloud.core.entity.data.Build;
import com.hysw.qqsl.cloud.core.entity.data.BuildAttribute;
import com.hysw.qqsl.cloud.util.SettingUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.stereotype.Service;


import java.util.*;

/**
 * Created by leinuo on 17-3-28.
 * 读取建筑物属性文件
 */
@Service("attributeGroupService")
public class AttributeGroupService {
    Log logger = LogFactory.getLog(getClass());


    /////////////////////////////////////////////newAttribeGroup//////////////////////////////////////////////////////////////
    public void initAttributeGroup(Map<String, Build> buildMap, String xml,List<String> stringAlias) throws QQSLException {
        Element root = null;
        try {
            root = SettingUtils.getInstance().getRootElement(xml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        List<Element> elements = SettingUtils.getInstance().getElementGroupList(root);
        AttributeGroup attributeGroup;
        Build build;
        for (Element element : elements) {
            if (element.attributeValue("buildAlias") != null&&!element.attributeValue("buildAlias").equals("")) {
                build = buildMap.get(element.attributeValue("buildAlias"));
                attributeGroup = new AttributeGroup();
                if (element.attributeValue("name") != null&&!element.attributeValue("name").equals("")) {
                    attributeGroup.setName(element.attributeValue("name"));
                }
                if (element.attributeValue("alias") != null&&!element.attributeValue("alias").equals("")) {
                    attributeGroup.setAlias(element.attributeValue("alias"));
                }
                if (element.elements().size() != 0) {
                    initAttribute(element.elements(),attributeGroup,stringAlias);
                }
                if (attributeGroup.getAlias().equals("coor")) {
                    build.setCoordinate(attributeGroup);
                }
                if (attributeGroup.getAlias().equals("wr")) {
                    build.setWaterResources(attributeGroup);
                }
                if (attributeGroup.getAlias().equals("cs")) {
                    build.setControlSize(attributeGroup);
                }
                if (attributeGroup.getAlias().equals("gs")) {
                    build.setGroundStress(attributeGroup);
                }
                if (attributeGroup.getAlias().startsWith("ct")) {
                    build.setComponent(attributeGroup);
                }
            }
        }
    }

    private void initAttribute(List<Element> elements,AttributeGroup attributeGroup,List<String> stringAlias) throws QQSLException {
        List<BuildAttribute> buildAttributes = new LinkedList<>();
        List<AttributeGroup> childs = new LinkedList<>();
        AttributeGroup child;
        for (Element element : elements) {
            if (element.getName().equals("attributeGroup")) {
                child = new AttributeGroup();
                if (element.attributeValue("name") != null&&!element.attributeValue("name").equals("")) {
                    child.setName(element.attributeValue("name"));
                }
                if (element.attributeValue("alias") != null&&!element.attributeValue("alias").equals("")) {
                    child.setAlias(element.attributeValue("alias"));
                    if (stringAlias.contains(element.attributeValue("alias"))) {
                        throw new QQSLException("alias:重复:"+element.attributeValue("alias"));
                    }else {
                        stringAlias.add(element.attributeValue("alias"));
                    }
                }
                initAttribute(element.elements(),child,stringAlias);
                childs.add(child);
            } else if (element.getName().equals("attribute")) {
                initAttributes(element,buildAttributes,stringAlias,attributeGroup);
            }
        }
        if (buildAttributes.size() != 0) {
            attributeGroup.setBuildAttributes(buildAttributes);
        }
        if (childs.size() != 0) {
            attributeGroup.setChilds(childs);
        }
    }

    private void initAttributes(Element element,List<BuildAttribute> buildAttributes,List<String> stringAlias,AttributeGroup attributeGroup) throws QQSLException {
        BuildAttribute buildAttribute = new BuildAttribute();
        if (element.attributeValue("alias") != null&&!element.attributeValue("alias").equals("")) {
            buildAttribute.setAlias(element.attributeValue("alias"));
            if (attributeGroup.getAlias() == null || !attributeGroup.getAlias().equals("coor")) {
                if (stringAlias.contains(element.attributeValue("alias"))) {
                    throw new QQSLException("alias:重复:" + element.attributeValue("alias"));
                } else {
                    stringAlias.add(element.attributeValue("alias"));
                }
            }
        }
        if (element.attributeValue("name") != null&&!element.attributeValue("name").equals("")) {
            buildAttribute.setName(element.attributeValue("name"));
        }
        if (element.attributeValue("type") != null&&!element.attributeValue("type").equals("")) {
            buildAttribute.setType(BuildAttribute.Type.valueOf(element.attributeValue("type").toUpperCase()));
        }
        if (element.attributeValue("unit") != null&&!element.attributeValue("unit").equals("")) {
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
        buildAttributes.add(buildAttribute);
    }
}
