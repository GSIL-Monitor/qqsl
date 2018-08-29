package com.hysw.qqsl.cloud.core.entity.builds;

import com.hysw.qqsl.cloud.core.entity.data.Build;

import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @since 2018/8/16
 */
public class CoordinateMap {
    private Map<String, List<Build>> buildMap;
    private Map<String, List<CoordinateObject>> lineMap;
    private Map<String, List<CoordinateObject>> areaMap;
    private Map<String, List<Build>> simpleBuildMap;

    public Map<String, List<Build>> getBuildMap() {
        return buildMap;
    }

    public void setBuildMap(Map<String, List<Build>> buildMap) {
        this.buildMap = buildMap;
    }

    public Map<String, List<CoordinateObject>> getLineMap() {
        return lineMap;
    }

    public void setLineMap(Map<String, List<CoordinateObject>> lineMap) {
        this.lineMap = lineMap;
    }

    public Map<String, List<CoordinateObject>> getAreaMap() {
        return areaMap;
    }

    public void setAreaMap(Map<String, List<CoordinateObject>> areaMap) {
        this.areaMap = areaMap;
    }

    public Map<String, List<Build>> getSimpleBuildMap() {
        return simpleBuildMap;
    }

    public void setSimpleBuildMap(Map<String, List<Build>> simpleBuildMap) {
        this.simpleBuildMap = simpleBuildMap;
    }
}
