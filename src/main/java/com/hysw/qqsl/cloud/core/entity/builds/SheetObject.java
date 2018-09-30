package com.hysw.qqsl.cloud.core.entity.builds;

import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @since 2018/8/17
 */
public class SheetObject {
    private Map<String, List<Sheet>> scetionPlaneModelWBs = new HashMap<>();
    private Map<String, List<Sheet>> lineWBs = new HashMap<>();
    private Map<String, List<Sheet>> buildWBs = new HashMap<>();
    private Map<String, List<Sheet>> areaWBs = new HashMap<>();
    private Map<String, List<Sheet>> unknowWBs = new HashMap<>();

    public void setLineSheetList(String key,Sheet sheet){
        addSheet(lineWBs,key,sheet);
    }

    public void setAreaSheetList(String key,Sheet sheet){
        addSheet(areaWBs,key,sheet);
    }

    public void setUnknowSheetList(String key,Sheet sheet){
        addSheet(unknowWBs,key,sheet);
    }
    public void setBuildSheetList(String key,Sheet sheet){
        addSheet(buildWBs,key,sheet);
    }

    public Map<String, List<Sheet>> getBuildWBs() {
        return buildWBs;
    }

    public void setBuildWBs(Map<String, List<Sheet>> buildWBs) {
        this.buildWBs = buildWBs;
    }

    public void setScetionPlaneModelList(String key, Sheet sheet){
        addSheet(scetionPlaneModelWBs,key,sheet);
    }

    public Map<String, List<Sheet>> getScetionPlaneModelWBs() {
        return scetionPlaneModelWBs;
    }

    public void setScetionPlaneModelWBs(Map<String, List<Sheet>> scetionPlaneModelWBs) {
        this.scetionPlaneModelWBs = scetionPlaneModelWBs;
    }

    public Map<String, List<Sheet>> getLineWBs() {
        return lineWBs;
    }

    public void setLineWBs(Map<String, List<Sheet>> lineWBs) {
        this.lineWBs = lineWBs;
    }

    public Map<String, List<Sheet>> getAreaWBs() {
        return areaWBs;
    }

    public void setAreaWBs(Map<String, List<Sheet>> areaWBs) {
        this.areaWBs = areaWBs;
    }

    public Map<String, List<Sheet>> getUnknowWBs() {
        return unknowWBs;
    }

    public void setUnknowWBs(Map<String, List<Sheet>> unknowWBs) {
        this.unknowWBs = unknowWBs;
    }

    private void addSheet(Map<String, List<Sheet>> map, String key, Sheet sheet) {
        List<Sheet> sheets;
        if (map.get(key) == null) {
            sheets = new ArrayList<>();
            sheets.add(sheet);
            map.put(key, sheets);
        } else {
            sheets = map.get(key);
            sheets.add(sheet);
            map.put(key, sheets);
        }
    }
}
