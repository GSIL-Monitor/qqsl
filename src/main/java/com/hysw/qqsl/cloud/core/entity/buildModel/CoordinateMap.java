package com.hysw.qqsl.cloud.core.entity.buildModel;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @since 2018/8/16
 */
public class CoordinateMap {
    private Map<String, List<ShapeCache>> lineMap;
    private Map<String, List<ShapeCache>> areaMap;
    private Map<String,List<ShapeCache>> sectionPlaneModelMap;

    public Map<String, List<ShapeCache>> getLineMap() {
        return lineMap;
    }

    public void setLineMap(Map<String, List<ShapeCache>> lineMap) {
        this.lineMap = lineMap;
    }

    public Map<String, List<ShapeCache>> getAreaMap() {
        return areaMap;
    }

    public void setAreaMap(Map<String, List<ShapeCache>> areaMap) {
        this.areaMap = areaMap;
    }

    public Map<String, List<ShapeCache>> getSectionPlaneModelMap() {
        return sectionPlaneModelMap;
    }

    public void setSectionPlaneModelMap(Map<String, List<ShapeCache>> sectionPlaneModelMap) {
        this.sectionPlaneModelMap = sectionPlaneModelMap;
    }
}
