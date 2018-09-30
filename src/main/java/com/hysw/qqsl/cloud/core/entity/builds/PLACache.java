package com.hysw.qqsl.cloud.core.entity.builds;

import com.hysw.qqsl.cloud.core.entity.data.Build;
import com.hysw.qqsl.cloud.core.entity.data.Shape;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @since 2018/8/20
 */
public class PLACache {
    private Map<String, List<Shape>> lineShape;
    private Map<String, List<Shape>> areaShape;
    private Map<String, List<Build>> buildsMap;

    public Map<String, List<Shape>> getLineShape() {
        return lineShape;
    }

    public void setLineShape(Map<String, List<Shape>> lineShape) {
        this.lineShape = lineShape;
    }

    public Map<String, List<Shape>> getAreaShape() {
        return areaShape;
    }

    public void setAreaShape(Map<String, List<Shape>> areaShape) {
        this.areaShape = areaShape;
    }

    public Map<String, List<Build>> getBuildsMap() {
        return buildsMap;
    }

    public void setBuildsMap(Map<String, List<Build>> buildsMap) {
        this.buildsMap = buildsMap;
    }
}
