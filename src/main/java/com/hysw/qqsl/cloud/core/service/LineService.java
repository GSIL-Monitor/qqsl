package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.buildModel.Line;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 线模板service
 * @author Administrator
 * @since 2018/9/19
 */
@Service("lineService")
public class LineService {

    @Autowired
    private CacheManager cacheManager;

    public List<Line> getLines(){
        Cache cache = cacheManager.getCache("linesCache");
        net.sf.ehcache.Element element = cache.get("lines");
        if (element == null) {
            try {
                element = new net.sf.ehcache.Element("lines", readLineXML());
                cache.put(element);
                return (List<Line>) element.getValue();
            } catch (DocumentException e) {
                e.printStackTrace();
                return null;
            }
        }
        return (List<Line>) element.getValue();
    }

    public List<Line> readLineXML() throws DocumentException {
        Line line;
        List<Line> lines = new ArrayList<>();
        Element root = SettingUtils.getInstance().getRootElement(SettingUtils.getInstance().getSetting().getLine());
        List<Element> elements = SettingUtils.getInstance().getElementList(root);
        for (Element element : elements) {
            line = new Line();
            if (element.attributeValue("name") != null && !element.attributeValue("name").equals("")) {
                line.setName(element.attributeValue("name"));
            }
            if (element.attributeValue("type") != null && !element.attributeValue("type").equals("")) {
                line.setCommonType(CommonEnum.CommonType.valueOf(element.attributeValue("type")));
            }
//            if (element.attributeValue("childType") != null && !element.attributeValue("childType").equals("")) {
//                line.setChildType(LineSectionPlaneModel.Type.valueOf(element.attributeValue("childType")));
//            }
            if (element.attributeValue("cellProperty") != null && !element.attributeValue("cellProperty").equals("")) {
                line.setCellProperty(element.attributeValue("cellProperty"));
            }
            lines.add(line);
        }
        return lines;
    }
}
