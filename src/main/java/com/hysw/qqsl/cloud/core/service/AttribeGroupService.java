package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.entity.QQSLException;
import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.core.entity.XMLFileException;
import com.hysw.qqsl.cloud.core.entity.data.Build;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hysw.qqsl.cloud.core.entity.builds.AttribeGroup;
import com.hysw.qqsl.cloud.core.entity.data.Attribe;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by leinuo on 17-3-28.
 * 读取建筑物属性文件
 */
@Service("attribeGroupService")
public class AttribeGroupService {
    Log logger = LogFactory.getLog(getClass());


    /////////////////////////////////////////////newAttribeGroup//////////////////////////////////////////////////////////////
    public void initAttribeGroup(Map<String, Build> buildMap, String xml,List<String> stringAlias) throws QQSLException {
        Element root = null;
        try {
            root = SettingUtils.getInstance().getRootElement(xml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        List<Element> elements = SettingUtils.getInstance().getElementGroupList(root);
        AttribeGroup attribeGroup;
        Build build;
        for (Element element : elements) {
            if (element.attributeValue("buildAlias") != null&&!element.attributeValue("buildAlias").equals("")) {
                build = buildMap.get(element.attributeValue("buildAlias"));
                attribeGroup = new AttribeGroup();
                if (element.attributeValue("name") != null&&!element.attributeValue("name").equals("")) {
                    attribeGroup.setName(element.attributeValue("name"));
                }
                if (element.attributeValue("alias") != null&&!element.attributeValue("alias").equals("")) {
                    attribeGroup.setAlias(element.attributeValue("alias"));
                }
                if (element.elements().size() != 0) {
                    initAttribe(element.elements(),attribeGroup,stringAlias);
                }
                if (attribeGroup.getAlias().equals("coor")) {
                    build.setCoordinate(attribeGroup);
                }
                if (attribeGroup.getAlias().equals("wr")) {
                    build.setWaterResources(attribeGroup);
                }
                if (attribeGroup.getAlias().equals("cs")) {
                    build.setControlSize(attribeGroup);
                }
                if (attribeGroup.getAlias().equals("gs")) {
                    build.setGroundStress(attribeGroup);
                }
                if (attribeGroup.getAlias().startsWith("ct")) {
                    build.setComponent(attribeGroup);
                }
            }
        }
    }

    private void initAttribe(List<Element> elements,AttribeGroup attribeGroup,List<String> stringAlias) throws QQSLException {
        List<Attribe> attribes = new LinkedList<>();
        List<AttribeGroup> childs = new LinkedList<>();
        AttribeGroup child;
        for (Element element : elements) {
            if (element.getName().equals("attribeGroup")) {
                child = new AttribeGroup();
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
                initAttribe(element.elements(),child,stringAlias);
                childs.add(child);
            } else if (element.getName().equals("attribe")) {
                initAttribes(element,attribes,stringAlias,attribeGroup);
            }
        }
        if (attribes.size() != 0) {
            attribeGroup.setAttribes(attribes);
        }
        if (childs.size() != 0) {
            attribeGroup.setChilds(childs);
        }
    }

    private void initAttribes(Element element,List<Attribe> attribes,List<String> stringAlias,AttribeGroup attribeGroup) throws QQSLException {
        Attribe attribe = new Attribe();
        if (element.attributeValue("alias") != null&&!element.attributeValue("alias").equals("")) {
            attribe.setAlias(element.attributeValue("alias"));
            if (attribeGroup.getAlias() == null || !attribeGroup.getAlias().equals("coor")) {
                if (stringAlias.contains(element.attributeValue("alias"))) {
                    throw new QQSLException("alias:重复:" + element.attributeValue("alias"));
                } else {
                    stringAlias.add(element.attributeValue("alias"));
                }
            }
        }
        if (element.attributeValue("name") != null&&!element.attributeValue("name").equals("")) {
            attribe.setName(element.attributeValue("name"));
        }
        if (element.attributeValue("type") != null&&!element.attributeValue("type").equals("")) {
            attribe.setType(Attribe.Type.valueOf(element.attributeValue("type").toUpperCase()));
        }
        if (element.attributeValue("unit") != null&&!element.attributeValue("unit").equals("")) {
            attribe.setUnit(element.attributeValue("unit"));
        }
        if (element.attributeValue("select") != null && !element.attributeValue("select").equals("")) {
            attribe.setSelects(Arrays.asList(element.attributeValue("select").split(",")));
        }
        if (element.attributeValue("locked") != null && !element.attributeValue("locked").equals("")) {
            attribe.setLocked(Boolean.valueOf(element.attributeValue("locked")));
        }
        if (element.attributeValue("formula") != null && !element.attributeValue("formula").equals("")) {
            attribe.setFormula(element.attributeValue("formula"));
        }
        attribes.add(attribe);
    }
}
