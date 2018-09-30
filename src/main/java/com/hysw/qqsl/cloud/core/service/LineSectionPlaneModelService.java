package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.entity.builds.LineAttributeGroup;
import com.hysw.qqsl.cloud.core.entity.builds.LineSectionPlaneModel;
import com.hysw.qqsl.cloud.core.entity.data.ShapeAttribute;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 线剖面模板service
 * @author Administrator
 * @since 2018/9/17
 */
@Service("lineBuildModelService")
public class LineSectionPlaneModelService {
    @Autowired
    private CacheManager cacheManager;

    public List<LineSectionPlaneModel> getLineSectionPlaneModel(){
        Cache cache = cacheManager.getCache("lineSectionPlaneModelCache");
        net.sf.ehcache.Element element = cache.get("lineSectionPlane");
        if (element == null) {
            try {
                element = new net.sf.ehcache.Element("lineSectionPlane", readLineBuildXML());
                cache.put(element);
                return (List<LineSectionPlaneModel>) element.getValue();
            } catch (DocumentException e) {
                e.printStackTrace();
                return null;
            }
        }
        return (List<LineSectionPlaneModel>) element.getValue();
    }

    public List<LineSectionPlaneModel> readLineBuildXML() throws DocumentException {
        LineSectionPlaneModel lineSectionPlaneModel;
        Map<String, LineSectionPlaneModel> lineSectionPlaneModelMap = new LinkedHashMap<>();
        List<LineSectionPlaneModel> lineSectionPlaneModels = new ArrayList<>();
        Element root = SettingUtils.getInstance().getRootElement(SettingUtils.getInstance().getSetting().getLineSectionPlaneModel());
        List<Element> elements = SettingUtils.getInstance().getElementList(root);
        for (Element element : elements) {
            lineSectionPlaneModel = new LineSectionPlaneModel();
            if (element.attributeValue("alias") != null && !element.attributeValue("alias").equals("")) {
                lineSectionPlaneModel.setAlias(element.attributeValue("alias"));
            }
            if (element.attributeValue("name") != null && !element.attributeValue("name").equals("")) {
                lineSectionPlaneModel.setName(element.attributeValue("name"));
            }
            if (element.attributeValue("type") != null && !element.attributeValue("type").equals("")) {
                lineSectionPlaneModel.setType(LineSectionPlaneModel.Type.valueOf(element.attributeValue("type")));
            }
            if (element.attributeValue("number") != null && !element.attributeValue("number").equals("")) {
                lineSectionPlaneModel.setNumber(Integer.valueOf(element.attributeValue("number")));
            }
            lineSectionPlaneModelMap.put(lineSectionPlaneModel.getAlias(), lineSectionPlaneModel);
        }
        initLineBuildAttributeGroup(lineSectionPlaneModelMap,SettingUtils.getInstance().getSetting().getRemark());
        initLineBuildAttributeGroup(lineSectionPlaneModelMap,SettingUtils.getInstance().getSetting().getLineWaterResources());
        initLineBuildAttributeGroup(lineSectionPlaneModelMap,SettingUtils.getInstance().getSetting().getLineControlSize());
        initLineBuildAttributeGroup(lineSectionPlaneModelMap,SettingUtils.getInstance().getSetting().getLineGroundStress());
        initLineBuildAttributeGroup(lineSectionPlaneModelMap,SettingUtils.getInstance().getSetting().getLineComponent());
        for (Map.Entry<String, LineSectionPlaneModel> entry : lineSectionPlaneModelMap.entrySet()) {
            lineSectionPlaneModels.add(entry.getValue());
        }
        return lineSectionPlaneModels;
    }

    private void initLineBuildAttributeGroup(Map<String, LineSectionPlaneModel> lineSectionPlaneModelMap, String xml) {
        Element root = null;
        try {
            root = SettingUtils.getInstance().getRootElement(xml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        List<Element> elements = SettingUtils.getInstance().getElementGroupList(root);
        LineAttributeGroup lineAttributeGroup;
        LineSectionPlaneModel lineSectionPlaneModel;
        for (Element element : elements) {
            if (element.attributeValue("lineAlias") != null&&!element.attributeValue("lineAlias").equals("")) {
                lineSectionPlaneModel = lineSectionPlaneModelMap.get(element.attributeValue("lineAlias"));
                lineAttributeGroup = new LineAttributeGroup();
                if (element.attributeValue("name") != null&&!element.attributeValue("name").equals("")) {
                    lineAttributeGroup.setName(element.attributeValue("name"));
                }
                if (element.attributeValue("alias") != null&&!element.attributeValue("alias").equals("")) {
                    lineAttributeGroup.setAlias(element.attributeValue("alias"));
                }
                if (element.elements().size() != 0) {
                    initShapeAttribute(element.elements(),lineAttributeGroup);
                }
                if (lineAttributeGroup.getAlias().equals("remark")) {
                    lineSectionPlaneModel.setRemark(lineAttributeGroup);
                }
                if (lineAttributeGroup.getAlias().equals("wr")) {
                    lineSectionPlaneModel.setLineWaterResources(lineAttributeGroup);
                }
                if (lineAttributeGroup.getAlias().equals("cs")) {
                    lineSectionPlaneModel.setLineControlSize(lineAttributeGroup);
                }
                if (lineAttributeGroup.getAlias().equals("gs")) {
                    lineSectionPlaneModel.setLineGroundStress(lineAttributeGroup);
                }
                if (lineAttributeGroup.getAlias().startsWith("ct")) {
                    lineSectionPlaneModel.setLineComponent(lineAttributeGroup);
                }
            }
        }
    }

    private void initShapeAttribute(List<Element> elements, LineAttributeGroup lineAttributeGroup) {
        List<ShapeAttribute> buildAttributes = new LinkedList<>();
        List<LineAttributeGroup> childs = new LinkedList<>();
        LineAttributeGroup child;
        for (Element element : elements) {
            if (element.getName().equals("attributeGroup")) {
                child = new LineAttributeGroup();
                if (element.attributeValue("name") != null&&!element.attributeValue("name").equals("")) {
                    child.setName(element.attributeValue("name"));
                }
                if (element.attributeValue("alias") != null&&!element.attributeValue("alias").equals("")) {
                    child.setAlias(element.attributeValue("alias"));
                }
                initShapeAttribute(element.elements(),child);
                childs.add(child);
            } else if (element.getName().equals("attribute")) {
                initShapeAttributes(element,buildAttributes,lineAttributeGroup);
            }
        }
        if (buildAttributes.size() != 0) {
            lineAttributeGroup.setShapeAttributes(buildAttributes);
        }
        if (childs.size() != 0) {
            lineAttributeGroup.setChilds(childs);
        }
    }

    private void initShapeAttributes(Element element, List<ShapeAttribute> shapeAttributes, LineAttributeGroup lineAttributeGroup) {
        ShapeAttribute shapeAttribute = new ShapeAttribute();
        if (element.attributeValue("alias") != null&&!element.attributeValue("alias").equals("")) {
            shapeAttribute.setAlias(element.attributeValue("alias"));
        }
        if (element.attributeValue("name") != null&&!element.attributeValue("name").equals("")) {
            shapeAttribute.setName(element.attributeValue("name"));
        }
        if (element.attributeValue("type") != null&&!element.attributeValue("type").equals("")) {
            shapeAttribute.setType(ShapeAttribute.Type.valueOf(element.attributeValue("type").toUpperCase()));
        }
        if (element.attributeValue("unit") != null&&!element.attributeValue("unit").equals("")) {
            shapeAttribute.setUnit(element.attributeValue("unit"));
        }
        if (element.attributeValue("select") != null && !element.attributeValue("select").equals("")) {
            shapeAttribute.setSelects(Arrays.asList(element.attributeValue("select").split(",")));
        }
        if (element.attributeValue("locked") != null && !element.attributeValue("locked").equals("")) {
            shapeAttribute.setLocked(Boolean.valueOf(element.attributeValue("locked")));
        }
        if (element.attributeValue("formula") != null && !element.attributeValue("formula").equals("")) {
            shapeAttribute.setFormula(element.attributeValue("formula"));
        }
        shapeAttributes.add(shapeAttribute);
    }
}
