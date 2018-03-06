package com.hysw.qqsl.cloud.util;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.util.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Create by leinuo on 17-12-27 下午4:47
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
public class RiverMaster {
    private JSONArray readExle(String filePath){
        Workbook wb = null;
        try {
           wb = SettingUtils.readExcel(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Sheet sheet;
        JSONArray jsonArray = null;
        int number = wb.getNumberOfSheets();
        for(int i=0;i<number;i++){
           sheet = wb.getSheetAt(i);
          jsonArray = readSheet(sheet);
        }
       return jsonArray;
    }

    private JSONArray readSheet(Sheet sheet) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        Row row;
        int rows = sheet.getLastRowNum() + 1;
        for (int i = 0; i < rows; i++) {
            jsonObject = new JSONObject();
            row = sheet.getRow(i);
            if (i == 0||i==1) {
                continue;
            }
            jsonObject.put("riverName", row.getCell(0).getStringCellValue());
            jsonObject.put("country", row.getCell(1).getStringCellValue());
            jsonObject.put("name", row.getCell(2).getStringCellValue());
            jsonObject.put("town", row.getCell(3).getStringCellValue());
            jsonObject.put("village", row.getCell(4).getStringCellValue());
            jsonObject.put("villageMaster", row.getCell(5).getStringCellValue());
            jsonObject.put("range", row.getCell(6).getStringCellValue());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public Map<String,Object> getInfo(String filePath){
        Map<String,Object> map = new HashedMap();
        JSONArray jsonArray = readExle(filePath);
        JSONArray towns = null;
        JSONArray townInfos = new JSONArray();
        JSONObject jsonObject,village;
        JSONObject town =null;
        JSONArray villages = null;
        String controyName = "";
        String  riverName = "";
        List<String> list = new ArrayList<>();
        for(int i =0;i<jsonArray.size();i++){
            jsonObject = (JSONObject) jsonArray.get(i);
            if(StringUtils.hasText(jsonObject.getString("riverName"))){
                if(towns!=null){
                    tail(villages,towns,town,riverName,townInfos);
                }
                towns = new JSONArray();
                riverName = jsonObject.getString("riverName").trim();
                list.add(riverName);
                town = null;
            }
            if(StringUtils.hasText(jsonObject.getString("town"))){
                if(town!=null){
                    town.put("villages",villages);
                    towns.add(town);
                }
                town = new JSONObject();
                controyName = StringUtils.hasText(jsonObject.getString("country").trim())?jsonObject.getString("country").trim():controyName;
                town.put("name",jsonObject.getString("name").trim());
                town.put("country",controyName);
                town.put("town",jsonObject.getString("town").trim());
                villages = new JSONArray();
            }
            village = new JSONObject();
            village.put("name",jsonObject.getString("village").trim());
            village.put("master",jsonObject.getString("villageMaster").trim());
            village.put("range",jsonObject.getString("range").trim());
            villages.add(village);
            if(i==jsonArray.size()-1){
                tail(villages,towns,town,riverName,townInfos);
            }
        }
        map.put("river",list);
        map.put("townInfo",townInfos);
        System.out.println(map);
        return map;
    }

    private void tail(JSONArray villages,JSONArray towns,JSONObject town,String riverName,JSONArray townInfos){
        JSONObject  object = new JSONObject();
        town.put("villages",villages);
        towns.add(town);
        object.put("riverName",riverName);
        object.put("towns",towns);
        townInfos.add(object);
    }

    public void wirteToExcle(String filePath){
        Map<String,Object> map = getInfo(filePath);
        Workbook wb = null;
        try {
            wb = SettingUtils.readExcel(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Sheet sheet;
        int number = wb.getNumberOfSheets();
        for(int i=0;i<number;i++){
            sheet = wb.getSheetAt(i);
            wirteToSheet(sheet,map);
        }
        FileOutputStream fileOut = null;
        try {
            String fileName = filePath.substring(0,filePath.indexOf(".")+1)+"xls";
            fileOut = new FileOutputStream(fileName);
            wb.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(fileOut!=null){
                    fileOut.close();
                }
                if(wb!=null){
                    wb.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void wirteToSheet(Sheet sheet, Map<String, Object> map) {
        Row row;
        int rows = sheet.getLastRowNum() + 1;
        for (int i = 0; i < rows; i++) {
            row = sheet.getRow(i);
            if (i == 0||i==1) {
                continue;
            }
            if(StringUtils.hasText(row.getCell(0).getStringCellValue())){
                System.out.println("riverName: "+row.getCell(0).getStringCellValue());
                JSONArray jsonArray = (JSONArray) map.get("townInfo");
                    Cell cell = row.createCell(7);
                    for(int j =0;j<jsonArray.size();j++){
                        JSONObject jsonObject = (JSONObject) jsonArray.get(j);
                        if(jsonObject.get("riverName").equals(row.getCell(0).getStringCellValue().trim())){
                            String value = jsonObject.get("towns").toString();
                            cell.setCellValue(value);
                            break;
                        }
                    }
            }
        }
    }

    public static void main(String[] agrs) {
       RiverMaster riverMaster =new RiverMaster();
       riverMaster.wirteToExcle("riverMaster(huangyuan).xls");
       riverMaster.wirteToExcle("riverMaster(huangzhong).xlsx");
    }
}
