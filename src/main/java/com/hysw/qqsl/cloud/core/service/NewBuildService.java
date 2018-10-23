package com.hysw.qqsl.cloud.core.service;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.dao.NewBuildDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.QQSLException;
import com.hysw.qqsl.cloud.core.entity.buildModel.*;
import com.hysw.qqsl.cloud.core.entity.data.*;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Administrator
 * @since 2018/10/23
 */
@Service("newBuildService")
public class NewBuildService extends BaseService<NewBuild, Long> {
    @Autowired
    private NewBuildDao newBuildDao;
    @Autowired
    private BuildService buildService;
    @Autowired
    private CoordinateService coordinateService;
    @Autowired
    private TransFromService transFromService;
    @Autowired
    private FieldWorkService fieldWorkService;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private NewBuildAttributeService newBuildAttributeService;

    @Autowired
    public void setBaseDao(NewBuildDao newBuildDao) {
        super.setBaseDao(newBuildDao);
    }
    Log logger = LogFactory.getLog(getClass());

    public JSONArray getModelType() {
        JSONObject jsonObject,jsonObject1;
        JSONArray jsonArray = new JSONArray(),jsonArray1;
        for (CommonEnum.CommonType commonType : CommonEnum.CommonType.values()) {
            if (SettingUtils.changeDeprecatedEnum(commonType, commonType.name())) {
                continue;
            }
            if (!commonType.getType().equals("buildModel")) {
                continue;
            }
            jsonObject = new JSONObject();
            jsonObject.put("typeC", commonType.getTypeC());
            jsonObject.put("commonType", commonType.name());
            jsonObject.put("type", commonType.getType());
            jsonObject.put("buildType", commonType.getBuildType());
            jsonObject.put("abbreviate", commonType.getAbbreviate());
            jsonArray1 = new JSONArray();
            for (NewBuild.ChildType childType : NewBuild.ChildType.values()) {
                if (childType.getCommonType() == commonType) {
                    jsonObject1 = new JSONObject();
                    jsonObject1.put("typeC", childType.getTypeC());
                    jsonObject1.put("childType", childType.name());
                    jsonObject1.put("type", childType.getType());
                    jsonObject1.put("abbreviate", childType.getAbbreviate());
                    jsonArray1.add(jsonObject1);
                }
            }
            if (!jsonArray1.isEmpty()) {
                jsonObject.put("child", jsonArray1);
            }
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public Workbook downloadBuild(CommonEnum.CommonType commonType, NewBuild.ChildType childType1) {
        Workbook wb = new XSSFWorkbook();
        NewBuild newBuild = findbyTypeAndChildType(childType1, commonType);
        outputBuilds(newBuild, wb);
        return wb;
    }

    public void outputBuilds(NewBuild newBuild, Workbook wb) {
//        将建筑物中心坐标，定位坐标，标高，描述手动构建到attribeList中
        makeAttributeToAttributeList(newBuild);
//        将模板内容写入真实建筑物
        writeModelToRealBuild(newBuild);
//        将属性构建进attribeGroup
        makeAttributeToAttributeGroup(newBuild);
//        对建筑物分类
        Map<String, List<NewBuild>> map = pickedTheSameBuild(newBuild);
//        输出excel
        outBuild(map,wb);
    }

    private void outBuild(Map<String, List<NewBuild>> map, Workbook wb) {
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;
        for (Map.Entry<String, List<NewBuild>> entry : map.entrySet()) {
            int i = 0, j = 0, k = 0;
            for (NewBuild newBuild : entry.getValue()) {
                if (newBuild.getCenterCoor() == null) {
                    continue;
                }
                if (newBuild.getNewBuildAttributes().size() == 3) {
                    continue;
                }
                if (wb.getSheet(entry.getKey()) == null) {
                    sheet = wb.createSheet(entry.getKey());
                }
                writeToCell(i, sheet, row, cell, bold(wb), "编号", "名称", "单位", "值", null, null, null, wb, true, null);
                i = writeAttributeGroup2(i, sheet, row, cell, wb, newBuild.getCoordinate(), "一", j, k);
                i = writeAttributeGroup2(i, sheet, row, cell, wb, newBuild.getWaterResources(), "二", j, k);
                i = writeAttributeGroup2(i, sheet, row, cell, wb, newBuild.getControlSize(), "三", j, k);
                i = writeAttributeGroup2(i, sheet, row, cell, wb, newBuild.getGroundStress(), "四", j, k);
                i = writeAttributeGroup2(i, sheet, row, cell, wb, newBuild.getComponent(), "五", j, k);
                i++;
            }
            if (wb.getSheet(entry.getKey()) != null) {
                sheet.protectSheet("hyswqqsl");
            }
        }
    }

    private int writeAttributeGroup2(int i, Sheet sheet, Row row, Cell cell, Workbook wb, NewAttributeGroup newAttributeGroup, String numC,int j,int k) {
        if (newAttributeGroup == null) {
            return i;
        }
        if (k == 0) {
            writeToCell(++i, sheet, row, cell, noBoldHaveBackgroundYellow(wb), numC, newAttributeGroup.getName(), null, null, null, null, null, wb, true, null);
        }else {
            writeToCell(++i, sheet, row, cell, noBoldHaveBackgroundSkyBlue(wb), numC, newAttributeGroup.getName(), null, null, null, null, null, wb, true, null);
        }
        if (newAttributeGroup.getChilds() == null) {
            if (k == 0) {
                j = 0;
                for (NewBuildAttribute newBuildAttribute : newAttributeGroup.getNewBuildAttributes()) {
                    writeToCell(++i, sheet, row, cell, noBold(wb), String.valueOf(++j), newBuildAttribute.getName(), newBuildAttribute.getUnit(), newBuildAttribute.getValue(), newBuildAttribute.getAlias(), newBuildAttribute.getType(), newBuildAttribute.getSelects(), wb, newBuildAttribute.getLocked(), newBuildAttribute.getFx());
                }
            }else{
                j = 0;
                for (NewBuildAttribute newBuildAttribute : newAttributeGroup.getNewBuildAttributes()) {
                    writeToCell(++i, sheet, row, cell, noBold(wb), "" + k + "." + (++j), newBuildAttribute.getName(), newBuildAttribute.getUnit(), newBuildAttribute.getValue(), newBuildAttribute.getAlias(), newBuildAttribute.getType(), newBuildAttribute.getSelects(), wb, newBuildAttribute.getLocked(), newBuildAttribute.getFx());
                }
            }
        }else{
            for (NewAttributeGroup child : newAttributeGroup.getChilds()) {
                i = writeAttributeGroup2(i, sheet, row, cell, wb, child, String.valueOf(++k),j,k);
            }
        }
        return i;
    }


    public CellStyle bold(Workbook wb){
        CellStyle style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        Font font = wb.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontName("宋体");//设置字体名称
        style.setFont(font);
        style.setBorderTop(CellStyle.BORDER_THIN);//上边框
        style.setBorderBottom(CellStyle.BORDER_THIN);//下边框
        style.setBorderLeft(CellStyle.BORDER_THIN);//左边框
        style.setBorderRight(CellStyle.BORDER_THIN);//右边框
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFillPattern((short) 9);
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        return style;
    }

    public CellStyle noBold(Workbook wb){
        CellStyle style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        Font font = wb.createFont();
//        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontName("宋体");//设置字体名称
        style.setFont(font);
        style.setBorderTop(CellStyle.BORDER_THIN);//上边框
        style.setBorderBottom(CellStyle.BORDER_THIN);//下边框
        style.setBorderLeft(CellStyle.BORDER_THIN);//左边框
        style.setBorderRight(CellStyle.BORDER_THIN);//右边框
        return style;
    }

    public CellStyle noBoldAndLocked(Workbook wb){
        CellStyle style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        Font font = wb.createFont();
//        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontName("宋体");//设置字体名称
        style.setFont(font);
        style.setBorderTop(CellStyle.BORDER_THIN);//上边框
        style.setBorderBottom(CellStyle.BORDER_THIN);//下边框
        style.setBorderLeft(CellStyle.BORDER_THIN);//左边框
        style.setBorderRight(CellStyle.BORDER_THIN);//右边框
//        style.setFillPattern((short) 1);
//        style.setFillForegroundColor(IndexedColors.PINK.getIndex());
        style.setLocked(false);
        return style;
    }

    public CellStyle noBoldAndUnlocked(Workbook wb){
        CellStyle style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        Font font = wb.createFont();
//        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontName("宋体");//设置字体名称
        style.setFont(font);
        style.setBorderTop(CellStyle.BORDER_THIN);//上边框
        style.setBorderBottom(CellStyle.BORDER_THIN);//下边框
        style.setBorderLeft(CellStyle.BORDER_THIN);//左边框
        style.setBorderRight(CellStyle.BORDER_THIN);//右边框
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFillPattern((short) 9);
        style.setFillForegroundColor(IndexedColors.PINK.getIndex());
        return style;
    }

    public CellStyle noBoldHaveBackgroundYellow(Workbook wb){
        CellStyle style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        Font font = wb.createFont();
//        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontName("宋体");//设置字体名称
        style.setFont(font);
        style.setBorderTop(CellStyle.BORDER_THIN);//上边框
        style.setBorderBottom(CellStyle.BORDER_THIN);//下边框
        style.setBorderLeft(CellStyle.BORDER_THIN);//左边框
        style.setBorderRight(CellStyle.BORDER_THIN);//右边框
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFillPattern((short) 9);
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        return style;
    }

    public CellStyle noBoldHaveBackgroundSkyBlue(Workbook wb){
        CellStyle style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        Font font = wb.createFont();
//        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontName("宋体");//设置字体名称
        style.setFont(font);
        style.setBorderTop(CellStyle.BORDER_THIN);//上边框
        style.setBorderBottom(CellStyle.BORDER_THIN);//下边框
        style.setBorderLeft(CellStyle.BORDER_THIN);//左边框
        style.setBorderRight(CellStyle.BORDER_THIN);//右边框
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFillPattern((short) 9);
        style.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        return style;
    }


    private void writeToCell(int i, Sheet sheet, Row row, Cell cell, CellStyle style, String a, String b, String c, String d, String e, NewBuildAttribute.Type type, List<String> selects, Workbook wb, boolean locked, String fx) {
        row = sheet.createRow(i);
        cell = row.createCell(0);
        if (a != null) {
            cell.setCellValue(a);
        }
        cell.setCellStyle(style);
        cell = row.createCell(1);
        if (b != null) {
            cell.setCellValue(b);
        }
        cell.setCellStyle(style);
        cell = row.createCell(2);
        if (e != null) {
            Drawing patriarch = sheet.createDrawingPatriarch();
            Comment comment1 = patriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 4, 2, (short) 6, 5));
            comment1.setString(new XSSFRichTextString(e));
            row.getCell(2).setCellComment(comment1);
        }
        if (c != null) {
            cell.setCellValue(c);
        }
        cell.setCellStyle(style);
        cell = row.createCell(3);
        if (fx != null) {
            cell.setCellType(Cell.CELL_TYPE_FORMULA);
            cell.setCellFormula(fx);
        } else {
            if (d != null) {
                cell.setCellValue(d);
            }
        }
        if (locked) {
            cell.setCellStyle(style);
        } else {
            cell.setCellStyle(noBoldAndLocked(wb));
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
//        sheet.autoSizeColumn(3);
        if (type == NewBuildAttribute.Type.SELECT) {
            setXSSFValidation1((XSSFSheet) sheet, selects.toArray(new String[selects.size()]), i, i, 3, 3);
        }
    }
    public static Sheet setXSSFValidation1(XSSFSheet sheet,
                                           String[] textlist, int firstRow, int endRow, int firstCol,
                                           int endCol) {
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(textlist);
        CellRangeAddressList regions = new CellRangeAddressList(firstRow,
                endRow, firstCol, endCol);
        XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(
                dvConstraint, regions);
        sheet.addValidationData(validation);
        return sheet;
    }

    private void makeAttributeToAttributeGroup(NewBuild newBuild) {
//            将属性写入attribeGroup
        attributeToAttributeGroup(newBuild.getNewBuildAttributes(), newBuild.getCoordinate());
        attributeToAttributeGroup(newBuild.getNewBuildAttributes(), newBuild.getWaterResources());
        attributeToAttributeGroup(newBuild.getNewBuildAttributes(), newBuild.getControlSize());
        attributeToAttributeGroup(newBuild.getNewBuildAttributes(), newBuild.getGroundStress());
        attributeToAttributeGroup(newBuild.getNewBuildAttributes(), newBuild.getComponent());

    }

    private void attributeToAttributeGroup(List<NewBuildAttribute> newBuildAttributes, NewAttributeGroup newAttributeGroup) {
        if (newAttributeGroup == null) {
            return;
        }
        if (newAttributeGroup.getChilds() == null) {
            buildAttributeGroup(newBuildAttributes,newAttributeGroup);
        }else{
            for (NewAttributeGroup child : newAttributeGroup.getChilds()) {
                attributeToAttributeGroup(newBuildAttributes, child);
            }
        }
    }

    private void buildAttributeGroup(List<NewBuildAttribute> newBuildAttributes, NewAttributeGroup newAttributeGroup) {
        for (NewBuildAttribute newBuildAttribute : newBuildAttributes) {
            for (NewBuildAttribute groupAttribute : newAttributeGroup.getNewBuildAttributes()) {
                if (groupAttribute.getAlias().equals(newBuildAttribute.getAlias())) {
                    groupAttribute.setValue(newBuildAttribute.getValue());
                    break;
                }
            }
        }
    }

    private Map<String, List<NewBuild>> pickedTheSameBuild(NewBuild newBuild) {
        Map<String, List<NewBuild>> map = new HashMap<>();
        List<NewBuild> newBuilds1;
        if (newBuild.getChildType() != null) {
            if (map.get(newBuild.getChildType().getTypeC()) == null) {
                newBuilds1 = new ArrayList<>();
                newBuilds1.add(newBuild);
                map.put(newBuild.getChildType().getTypeC(), newBuilds1);
            } else {
                newBuilds1 = map.get(newBuild.getChildType().getTypeC());
                newBuilds1.add(newBuild);
                map.put(newBuild.getChildType().getTypeC(), newBuilds1);
            }
        }else{
            if (map.get(newBuild.getType().getTypeC()) == null) {
                newBuilds1 = new ArrayList<>();
                newBuilds1.add(newBuild);
                map.put(newBuild.getType().getTypeC(), newBuilds1);
            } else {
                newBuilds1 = map.get(newBuild.getType().getTypeC());
                newBuilds1.add(newBuild);
                map.put(newBuild.getType().getTypeC(), newBuilds1);
            }
        }
        return map;
    }

    private void makeAttributeToAttributeList(NewBuild newBuild) {
        NewBuildAttribute newBuildAttribute;
        List<NewBuildAttribute> newBuildAttributes;
        if (newBuild.getCenterCoor() == null) {
            return;
        }
        newBuildAttributes = newBuild.getNewBuildAttributes();
        newBuildAttribute = new NewBuildAttribute();
        newBuildAttribute.setValue(jsonToCoordinate(newBuild.getCenterCoor(),Coordinate.WGS84Type.DEGREE));
        newBuildAttribute.setAlias("center");
        newBuildAttributes.add(newBuildAttribute);
        if (newBuild.getPositionCoor() != null) {
            newBuildAttribute = new NewBuildAttribute();
            newBuildAttribute.setValue(jsonToCoordinate(newBuild.getPositionCoor(),Coordinate.WGS84Type.DEGREE));
            newBuildAttribute.setAlias("position");
            newBuildAttributes.add(newBuildAttribute);
        }
        newBuildAttribute = new NewBuildAttribute();
        newBuildAttribute.setValue(newBuild.getDesignElevation());
        newBuildAttribute.setAlias("designElevation");
        newBuildAttributes.add(newBuildAttribute);
        newBuildAttribute = new NewBuildAttribute();
        newBuildAttribute.setValue(newBuild.getRemark());
        newBuildAttribute.setAlias("remark");
        newBuildAttributes.add(newBuildAttribute);
        newBuild.setNewBuildAttributes(newBuildAttributes);

    }

    private String jsonToCoordinate(String coor, Coordinate.WGS84Type wgs84Type) {
        JSONObject jsonObject = JSONObject.fromObject(coor);
        JSONObject jsonObject1 = coordinateBLHToXYZ(jsonObject.get("lon").toString(), jsonObject.get("lat").toString(), wgs84Type);
        return jsonObject1.get("lon") + "," + jsonObject1.get("lat");
    }

    /**
     * 将大地坐标转换为各类坐标
     *
     * @param longitude
     * @param latitude
     * @return
     */
    public JSONObject coordinateBLHToXYZ(String longitude, String latitude, Coordinate.WGS84Type wgs84Type) {
        if (wgs84Type == null) {
            return null;
        }
        switch (wgs84Type) {
            case DEGREE:
                return degree(longitude, latitude);
        }
        return null;
    }

    private JSONObject degree(String longitude, String latitude) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lon", longitude);
        jsonObject.put("lat", latitude);
        return jsonObject;
    }

    private void writeModelToRealBuild(NewBuild newBuild) {
        NewBuild newBuild1;
        if (newBuild.getChildType() != null) {
            newBuild1 = pickedBuild(newBuild.getChildType().getTypeC());
        }else{
            newBuild1 = pickedBuild(newBuild.getType().getTypeC());
        }
        newBuild.setName(newBuild1.getName());
        newBuild.setAlias(newBuild1.getAlias());
        newBuild.setCoordinate(newBuild1.getCoordinate());
        newBuild.setWaterResources(newBuild1.getWaterResources());
        newBuild.setControlSize(newBuild1.getControlSize());
        newBuild.setGroundStress(newBuild1.getGroundStress());
        newBuild.setComponent(newBuild1.getComponent());
    }


    public NewBuild pickedBuild(String sheetName) {
        List<NewBuild> builds = getBuilds();
        for (NewBuild build : builds) {
            if (build.getChildType() != null) {
                if (build.getChildType().getTypeC().equals(sheetName.trim())) {
                    return (NewBuild) SettingUtils.objectCopy(build);
                }
            }else{
                if (build.getType().getTypeC().equals(sheetName.trim())) {
                    return (NewBuild) SettingUtils.objectCopy(build);
                }
            }
        }
        return null;
    }

    public List<NewBuild> findByProject(Project project) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("project", project));
        List<NewBuild> list = newBuildDao.findList(0, null, filters);
        return list;
    }


    public JSONObject buildJson(NewBuild newBuild2) {
        NewBuild newBuild = null;
        List<NewBuild> builds1 = getBuilds();
        for (NewBuild build1 : builds1) {
            if (newBuild2.getType().equals(build1.getType())) {
                newBuild = (NewBuild) SettingUtils.objectCopy(build1);
                setProperty(newBuild,newBuild2,true);
                break;
            }
        }
        JSONObject jsonObject, jsonObject1;
        jsonObject = new JSONObject();
        jsonObject.put("id", newBuild.getId());
        jsonObject.put("name", newBuild.getName());
        jsonObject.put("alias", newBuild.getAlias());
        jsonObject.put("type", newBuild.getType());
        jsonObject.put("centerCoor", newBuild.getCenterCoor());
        jsonObject.put("positionCoor", newBuild.getPositionCoor());
        jsonObject.put("remark", newBuild.getRemark());
        jsonObject.put("childType", newBuild.getChildType()==null?null:newBuild.getChildType().name());
        jsonObject1 = new JSONObject();
        writeAttributeGroup(newBuild.getCoordinate(),jsonObject1);
        if (!jsonObject1.isEmpty()) {
            jsonObject.put("coordinate", jsonObject1);
        }
        jsonObject1 = new JSONObject();
        writeAttributeGroup(newBuild.getWaterResources(),jsonObject1);
        if (!jsonObject1.isEmpty()) {
            jsonObject.put("waterResources", jsonObject1);
        }
        jsonObject1 = new JSONObject();
        writeAttributeGroup(newBuild.getControlSize(),jsonObject1);
        if (!jsonObject1.isEmpty()) {
            jsonObject.put("controlSize", jsonObject1);
        }
        jsonObject1 = new JSONObject();
        writeAttributeGroup(newBuild.getGroundStress(),jsonObject1);
        if (!jsonObject1.isEmpty()) {
            jsonObject.put("groundStress", jsonObject1);
        }
        jsonObject1 = new JSONObject();
        writeAttributeGroup(newBuild.getComponent(),jsonObject1);
        if (!jsonObject1.isEmpty()) {
            jsonObject.put("component", jsonObject1);
        }
        return jsonObject;
    }

    private void writeAttributeGroup(NewAttributeGroup newAttributeGroup, JSONObject jsonObject) {
        if (newAttributeGroup == null) {
            return;
        }
        JSONArray jsonArray;
        jsonArray = new JSONArray();
        writeAttribute(newAttributeGroup.getNewBuildAttributes(),jsonArray);
        if (!jsonArray.isEmpty()) {
            jsonObject.put("name", newAttributeGroup.getName());
            jsonObject.put("alias", newAttributeGroup.getAlias());
            jsonObject.put("attributes", jsonArray);
        }
        jsonArray = new JSONArray();
        writeChild(newAttributeGroup.getChilds(), jsonArray);
        if (!jsonArray.isEmpty()) {
            jsonObject.put("name", newAttributeGroup.getName());
            jsonObject.put("alias", newAttributeGroup.getAlias());
            jsonObject.put("child", jsonArray);
        }
    }

    private void writeChild(List<NewAttributeGroup> newAttributeGroups, JSONArray jsonArray) {
        JSONObject jsonObject;
        if (newAttributeGroups == null) {
            return;
        }
        for (NewAttributeGroup newAttributeGroup : newAttributeGroups) {
            jsonObject = new JSONObject();
            writeAttributeGroup(newAttributeGroup, jsonObject);
            if (!jsonObject.isEmpty()) {
                jsonArray.add(jsonObject);
            }
        }
    }

    private void writeAttribute(List<NewBuildAttribute> attributes, JSONArray jsonArray) {
        if (attributes == null) {
            return;
        }
        JSONObject jsonObject;
        for (NewBuildAttribute attribute : attributes) {
            if (attribute.getValue() == null) {
                continue;
            }
            jsonObject = new JSONObject();
            jsonObject.put("id", attribute.getId());
            jsonObject.put("name", attribute.getName());
            jsonObject.put("alias", attribute.getAlias());
            jsonObject.put("type", attribute.getType());
            jsonObject.put("value", attribute.getValue());
            jsonObject.put("formula", attribute.getFormula());
            if (attribute.getSelects() != null && attribute.getSelects().size() != 0) {
                jsonObject.put("selects", attribute.getSelects());
            }
            if (attribute.getUnit() != null && !attribute.getUnit().equals("")) {
                jsonObject.put("unit", attribute.getUnit());
            }
            jsonArray.add(jsonObject);
        }
    }

    /**
     * 匹配建筑物数据
     * sign=true 匹配属性
     *
     * @param newBuild
     * @param newBuild1
     * @param sign
     */
    public void setProperty(NewBuild newBuild, NewBuild newBuild1, boolean sign) {
        newBuild.setCenterCoor(newBuild1.getCenterCoor());
        newBuild.setPositionCoor(newBuild1.getPositionCoor());
        newBuild.setDesignElevation(newBuild1.getDesignElevation());
        newBuild.setId(newBuild1.getId());
        newBuild.setRemark(newBuild1.getRemark());
        if (!sign) {
            return;
        }
        newBuild1.setNewBuildAttributes(newBuildAttributeService.findByNewBuild(newBuild1));
        newBuild.setNewBuildAttributes((List<NewBuildAttribute>) SettingUtils.objectCopy(newBuild1.getNewBuildAttributes()));
//        attributeGroupNotNuLL(build.getCoordinate(), build1.getAttribeList());

        attributeGroupNotNuLL(newBuild.getWaterResources(), newBuild1.getNewBuildAttributes());

        attributeGroupNotNuLL(newBuild.getControlSize(), newBuild1.getNewBuildAttributes());

        attributeGroupNotNuLL(newBuild.getGroundStress(), newBuild1.getNewBuildAttributes());

        attributeGroupNotNuLL(newBuild.getComponent(), newBuild1.getNewBuildAttributes());
    }

    private void attributeGroupNotNuLL(NewAttributeGroup attributeGroup, List<NewBuildAttribute> attributeList) {
        if (attributeGroup == null) {
            return;
        }
        setAttribute(attributeGroup.getNewBuildAttributes(), attributeList);
        setChild(attributeGroup.getChilds(), attributeList);
    }

    /**
     * 匹配属性
     *
     * @param buildAttributes    输出的
     * @param attributeList 数据库中的
     */
    private void setAttribute(List<NewBuildAttribute> buildAttributes, List<NewBuildAttribute> attributeList) {
        if (buildAttributes == null) {
            return;
        }
        for (NewBuildAttribute buildAttribute : buildAttributes) {
            for (NewBuildAttribute buildAttribute1 : attributeList) {
                if (buildAttribute.getAlias().equals(buildAttribute1.getAlias())) {
                    buildAttribute.setValue(buildAttribute1.getValue());
                    buildAttribute.setId(buildAttribute1.getId());
                    buildAttribute.setCreateDate(buildAttribute1.getCreateDate());
                    attributeList.remove(buildAttribute1);
                    break;
                }
            }
        }
    }

    /**
     * 子节点匹配属性
     *
     * @param attributeGroups
     * @param attributeList
     */
    private void setChild(List<NewAttributeGroup> attributeGroups, List<NewBuildAttribute> attributeList) {
        if (attributeGroups == null) {
            return;
        }
        for (NewAttributeGroup attributeGroup : attributeGroups) {
            setAttribute(attributeGroup.getNewBuildAttributes(), attributeList);
            setChild(attributeGroup.getChilds(), attributeList);
        }
    }

    public JSONObject toJSON(NewBuild newBuild2) {
        NewBuild newBuild = null;
        List<NewBuild> builds1 = getBuilds();
        for (NewBuild build1 : builds1) {
            if (newBuild2.getType().equals(build1.getType())) {
                newBuild = (NewBuild) SettingUtils.objectCopy(build1);
                setProperty(newBuild,newBuild2,true);
                break;
            }
        }
        JSONObject jsonObject,jsonObject1;
        JSONArray jsonArray = new JSONArray();
        jsonObject = new JSONObject();
        jsonObject.put("id", newBuild.getId());
        jsonObject.put("name", newBuild.getName());
        jsonObject.put("type", newBuild.getType());
        jsonObject.put("center", newBuild.getCenterCoor());
        jsonObject.put("position", newBuild.getPositionCoor());
        jsonObject.put("designElevation", newBuild.getDesignElevation());
        jsonObject.put("remark", newBuild.getRemark());
        jsonObject.put("childType", newBuild.getChildType() == null ? null : newBuild.getChildType());
        for (NewBuildAttribute newBuildAttribute : newBuild.getNewBuildAttributes()) {
            jsonObject1 = new JSONObject();
            jsonObject1.put("alias", newBuildAttribute.getAlias());
            jsonObject1.put("value", newBuildAttribute.getValue());
            jsonArray.add(jsonObject1);
        }
        jsonObject.put("buildAttribute", jsonArray);
        return jsonObject;
    }

    /**
     * 分析上传坐标文件是否符合要求
     *
     * @param entry
     * @param jsonObject
     * @param wbs
     */
    public void uploadCoordinate(Map.Entry<String, MultipartFile> entry, JSONObject jsonObject, Map<String, Workbook> wbs) {
        MultipartFile mFile = entry.getValue();
        String fileName = mFile.getOriginalFilename();
        // 限制上传文件的大小
        if (mFile.getSize() > CommonAttributes.CONVERT_MAX_SZIE) {
            // return "文件过大无法上传";
            logger.debug("文件过大");
            jsonObject.put(entry.getKey(), "文件过大");
            return;
        }
        InputStream is;
        try {
            is = mFile.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("坐标文件或格式异常");
            jsonObject.put(entry.getKey(), "坐标文件或格式异常");
            return;
        }
        String s = fileName.substring(fileName.lastIndexOf(".") + 1);
        try {
            readExcels(is, s, fileName, jsonObject, wbs);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("坐标文件或格式异常");
            jsonObject.put(entry.getKey(), "坐标文件或格式异常");
            return;
        } finally {
            IOUtils.safeClose(is);
        }
    }

    /**
     * 尝试使用workbook解析
     *
     * @param is
     * @param s
     * @param fileName
     * @param jsonObject
     * @param wbs
     * @throws Exception
     */
    public void readExcels(InputStream is, String s, String fileName, JSONObject jsonObject, Map<String, Workbook> wbs) throws Exception {
        Workbook wb = SettingUtils.readExcel(is, s);
        if (wb == null) {
            jsonObject.put(fileName, "坐标文件或格式异常");
            return;
        }
        wbs.put(fileName,wb);
    }

    /**
     * 分析出所有的sheet，并分类
     *
     * @param wbs
     * @param sheetObject
     */
    public void getAllSheet(Map<String, Workbook> wbs, SheetObject sheetObject) {
        for (Map.Entry<String, Workbook> entry : wbs.entrySet()) {
            for (int i = 0; i < entry.getValue().getNumberOfSheets(); i++) {
                boolean flag = true;
                Sheet sheet = entry.getValue().getSheetAt(i);
                for (CommonEnum.CommonType commonType : CommonEnum.CommonType.values()) {
                    if (SettingUtils.changeDeprecatedEnum(commonType, commonType.name())) {
                        continue;
                    }
                    if (sheet.getSheetName().trim().equals(commonType.getTypeC())) {
                        if (commonType.getType().equals("line")) {
                            sheetObject.setLineSheetList(entry.getKey(), sheet);
                            flag = false;
                        }
                        if (commonType.getType().equals("area")) {
                            sheetObject.setAreaSheetList(entry.getKey(), sheet);
                            flag = false;
                        }
                        if (commonType.getType().equals("buildModel")) {
                            sheetObject.setBuildSheetList(entry.getKey(), sheet);
                            flag = false;
                        }
                    }
                }
                for (LineSectionPlaneModel.Type type : LineSectionPlaneModel.Type.values()) {
                    if (sheet.getSheetName().trim().equals(type.getTypeC())) {
                        if (type.getType().equals("sectionPlane")) {
                            sheetObject.setScetionPlaneModelList(entry.getKey(), sheet);
                            flag = false;
                        }
                    }
                }
                for (NewBuild.ChildType childType : NewBuild.ChildType.values()) {
                    if (sheet.getSheetName().trim().equals(childType.getTypeC())) {
                        if (childType.getType().equals("buildModel")) {
                            sheetObject.setBuildSheetList(entry.getKey(), sheet);
                            flag = false;
                        }
                    }
                }
                if (flag) {
                    sheetObject.setUnknowSheetList(entry.getKey(), sheet);
                }
            }
        }
    }



    /**
     * 错误处理
     * @param unknowWBs
     */
    public JSONArray errorMsg(Map<String, List<Sheet>> unknowWBs) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        StringBuffer sb = null;
        for (Map.Entry<String, List<Sheet>> entry : unknowWBs.entrySet()) {
            List<Sheet> sheets = entry.getValue();
            sb = new StringBuffer("sheet表：");
            for (Sheet sheet : sheets) {
                sb.append(sheet.getSheetName());
                sb.append("，");
            }
            sb.replace(sb.length() - 1, sb.length(), "：");
            sb.append("未知类型");
            jsonObject = new JSONObject();
            jsonObject.put(entry.getKey(), sb.toString());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public int readSheet(SheetObject sheetObject, CommonEnum.CommonType commonType, NewBuild.ChildType childType1) {
        Row row;
        NewBuild newBuild = null;
        String a = null,b = null,c = null;
        NewBuildAttribute newBuildAttribute;
        JSONObject jsonObject;
        List<NewBuildAttribute> newBuildAttributes = null;
        for (Map.Entry<String, List<Sheet>> entry : sheetObject.getBuildWBs().entrySet()) {
            for (Sheet sheet : entry.getValue()) {
                if (sheet == null) {
                    continue;
                }
                if (childType1 != null) {
                    for (NewBuild.ChildType value : NewBuild.ChildType.values()) {
                        if (value.getTypeC().equals(sheet.getSheetName())) {
                            if (value != childType1) {
                                return -1;
                            }
                        }
                    }
                } else if (commonType != null) {
                    for (CommonEnum.CommonType value : CommonEnum.CommonType.values()) {
                        if (value.getTypeC().equals(sheet.getSheetName())) {
                            if (value != commonType) {
                                return -1;
                            }
                        }
                    }
                } else {
                    return -1;
                }
                for (NewBuild newBuild1 : getBuilds()) {
                    if (childType1 != null) {
                        if (newBuild1.getChildType() != null && newBuild1.getChildType().toString().equals(childType1.toString())) {
                            newBuild = (NewBuild) SettingUtils.objectCopy(newBuild1);
                            break;
                        }
                    } else if (commonType != null) {
                        if (newBuild1.getType() == commonType) {
                            newBuild = (NewBuild) SettingUtils.objectCopy(newBuild1);
                            break;
                        }
                    }
                }
                if (newBuild == null) {
                    return -2;
                }
                for (int j = 0; j <= sheet.getLastRowNum(); j++) {
                    row = sheet.getRow(j);
                    if (row == null) {
                        continue;
                    }
                    if (row.getCell(0) != null) {
                        row.getCell(0).setCellType(Cell.CELL_TYPE_STRING);
                        a = row.getCell(0).getStringCellValue();
                    }
                    if (row.getCell(2) != null) {
                        row.getCell(2).setCellType(Cell.CELL_TYPE_STRING);
                        Comment cellComment = row.getCell(2).getCellComment();
                        if (cellComment != null) {
                            b = cellComment.getString().getString();
                        } else {
                            b = "";
                        }
                    }
                    if (row.getCell(3) != null) {
                        row.getCell(3).setCellType(Cell.CELL_TYPE_STRING);
                        c = row.getCell(3).getStringCellValue();
                    }
                    if (a.trim().equals("编号")) {
                        if (j == 0) {
                            newBuildAttributes = new ArrayList<>();
                            continue;
                        }else{
                            newBuild.setNewBuildAttributes(newBuildAttributes);
                            NewBuild newBuild1 = findbyTypeAndChildType(newBuild.getChildType(), newBuild.getType());
                            if (newBuild1 != null) {
                                remove(newBuild1);
                            }
                            save(newBuild);
                            newBuild = new NewBuild();
                            newBuildAttributes = new ArrayList<>();
                        }
                    }
                    if (b.trim().equals("")) {
                        continue;
                    }
                    if (b.trim().equals("center")) {
                        String[] split = c.split(",");
                        if (split.length != 2) {
                            return -3;
                        }
                        jsonObject = new JSONObject();
                        jsonObject.put("lon", split[0]);
                        jsonObject.put("lat", split[1]);
                        newBuild.setCenterCoor(jsonObject.toString());
                        continue;
                    }
                    if (b.trim().equals("position")) {
                        String[] split = c.split(",");
                        if (split.length != 2) {
                            continue;
                        }
                        jsonObject = new JSONObject();
                        jsonObject.put("lon", split[0]);
                        jsonObject.put("lat", split[1]);
                        newBuild.setCenterCoor(jsonObject.toString());
                        continue;
                    }
                    if (b.trim().equals("designElevation")) {
                        newBuild.setDesignElevation(c);
                        continue;
                    }
                    if (b.trim().equals("remark")) {
                        newBuild.setRemark(c);
                        continue;
                    }
                    newBuildAttribute = new NewBuildAttribute();
                    newBuildAttribute.setAlias(b);
                    newBuildAttribute.setValue(c);
                    newBuildAttribute.setBuild(newBuild);
                    newBuildAttributes.add(newBuildAttribute);
                    if (j == sheet.getLastRowNum()) {
                        newBuild.setNewBuildAttributes(newBuildAttributes);
                        NewBuild newBuild1 = findbyTypeAndChildType(newBuild.getChildType(), newBuild.getType());
                        if (newBuild1 != null) {
                            remove(newBuild1);
                        }
                        save(newBuild);
                    }
                }
            }
        }
        return 0;
    }

    private NewBuild findbyTypeAndChildType(NewBuild.ChildType childType1, CommonEnum.CommonType commonType) {
        List<NewBuild> list;
        if (childType1 != null && commonType != null) {
            List<Filter> filters = new ArrayList<>();
            filters.add(Filter.eq("childType", childType1));
            filters.add(Filter.eq("type", commonType));
            list = newBuildDao.findList(0, null, filters);
        } else if (childType1 != null) {
            List<Filter> filters = new ArrayList<>();
            filters.add(Filter.eq("childType", childType1));
//            filters.add(Filter.eq("type", commonType));
            list = newBuildDao.findList(0, null, filters);
        } else if (commonType != null) {
            List<Filter> filters = new ArrayList<>();
//            filters.add(Filter.eq("childType", childType1));
            filters.add(Filter.eq("type", commonType));
            list = newBuildDao.findList(0, null, filters);
        }else {
            return null;
        }
        if (list.size() == 1) {
            return list.get(0);
        }

        return null;
    }

    public List<NewBuild> getBuilds(){
        Cache cache = cacheManager.getCache("buildModelCache");
        net.sf.ehcache.Element element = cache.get("buildModel");
        if (element == null) {
            try {
                element = new net.sf.ehcache.Element("buildModel", initBuildModel(SettingUtils.getInstance().getSetting().getBuild()));
                cache.put(element);
                return (List<NewBuild>) element.getValue();
            } catch (QQSLException e) {
                e.printStackTrace();
                return null;
            }
        }
        return (List<NewBuild>) element.getValue();
    }

    public List<NewBuild> initBuildModel(String xml) throws QQSLException {
        Element root = null;
        try {
            root = SettingUtils.getInstance().getRootElement(xml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        List<String> stringAlias = new ArrayList<>();
        List<Element> elements = SettingUtils.getInstance().getElementGroupList(root);
        Map<String, NewBuild> buildMap = new HashMap<>();
        NewBuild newBuild;
        for (Element element : elements) {
            newBuild = new NewBuild();
            if (element.attributeValue("name") != null&&!element.attributeValue("name").equals("")) {
                newBuild.setName(element.attributeValue("name"));
            }
            if (element.attributeValue("childType") != null&&!element.attributeValue("childType").equals("")) {
                newBuild.setChildType(NewBuild.ChildType.valueOf(element.attributeValue("childType")));
                newBuild.setType(newBuild.getChildType().getCommonType());
            }else{
                if (element.attributeValue("type") != null&&!element.attributeValue("type").equals("")) {
                    newBuild.setType(CommonEnum.CommonType.valueOf(element.attributeValue("type")));
                }
            }
            if (element.attributeValue("alias") != null&&!element.attributeValue("alias").equals("")) {
                newBuild.setAlias(element.attributeValue("alias"));
                if (stringAlias.contains(element.attributeValue("alias"))) {
                    throw new QQSLException("alias:重复:"+element.attributeValue("alias"));
                }else {
                    stringAlias.add(element.attributeValue("alias"));
                }
            }
            if (element.attributeValue("number") != null && !element.attributeValue("number").equals("")) {
                newBuild.setNumber(Integer.valueOf(element.attributeValue("number")));
            }
            buildMap.put(newBuild.getAlias(),newBuild);
        }
        initAttributeGroup(buildMap,SettingUtils.getInstance().getSetting().getCoordinate(),stringAlias);
        initAttributeGroup(buildMap,SettingUtils.getInstance().getSetting().getWaterResources(),stringAlias);
        initAttributeGroup(buildMap,SettingUtils.getInstance().getSetting().getControlSize(),stringAlias);
        initAttributeGroup(buildMap,SettingUtils.getInstance().getSetting().getGroundStress(),stringAlias);
        initAttributeGroup(buildMap, SettingUtils.getInstance().getSetting().getComponent(), stringAlias);
        List<NewBuild> builds = new ArrayList<>();
        for (Map.Entry<String, NewBuild> entry : buildMap.entrySet()) {
            builds.add(entry.getValue());
        }
        return builds;
    }

    public void initAttributeGroup(Map<String, NewBuild> buildMap, String xml,List<String> stringAlias) throws QQSLException {
        Element root = null;
        try {
            root = SettingUtils.getInstance().getRootElement(xml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        List<Element> elements = SettingUtils.getInstance().getElementGroupList(root);
        NewAttributeGroup newAttributeGroup;
        NewBuild newBuild;
        for (Element element : elements) {
            if (element.attributeValue("buildAlias") != null&&!element.attributeValue("buildAlias").equals("")) {
                newBuild = buildMap.get(element.attributeValue("buildAlias"));
                newAttributeGroup = new NewAttributeGroup();
                if (element.attributeValue("name") != null&&!element.attributeValue("name").equals("")) {
                    newAttributeGroup.setName(element.attributeValue("name"));
                }
                if (element.attributeValue("alias") != null&&!element.attributeValue("alias").equals("")) {
                    newAttributeGroup.setAlias(element.attributeValue("alias"));
                }
                if (element.elements().size() != 0) {
                    initAttribute(element.elements(),newAttributeGroup,stringAlias);
                }
                if (newAttributeGroup.getAlias().equals("coor")) {
                    newBuild.setCoordinate(newAttributeGroup);
                }
                if (newAttributeGroup.getAlias().equals("wr")) {
                    newBuild.setWaterResources(newAttributeGroup);
                }
                if (newAttributeGroup.getAlias().equals("cs")) {
                    newBuild.setControlSize(newAttributeGroup);
                }
                if (newAttributeGroup.getAlias().equals("gs")) {
                    newBuild.setGroundStress(newAttributeGroup);
                }
                if (newAttributeGroup.getAlias().startsWith("ct")) {
                    newBuild.setComponent(newAttributeGroup);
                }
            }
        }
    }

    private void initAttributes(Element element,List<NewBuildAttribute> newBuildAttributes,List<String> stringAlias,NewAttributeGroup newAttributeGroup) throws QQSLException {
        NewBuildAttribute newBuildAttribute = new NewBuildAttribute();
        if (element.attributeValue("alias") != null&&!element.attributeValue("alias").equals("")) {
            newBuildAttribute.setAlias(element.attributeValue("alias"));
//            if (newBuildAttribute.getAlias() == null || !newBuildAttribute.getAlias().equals("coor")) {
//                if (stringAlias.contains(element.attributeValue("alias"))) {
//                    throw new QQSLException("alias:重复:" + element.attributeValue("alias"));
//                } else {
//                    stringAlias.add(element.attributeValue("alias"));
//                }
//            }
        }
        if (element.attributeValue("name") != null&&!element.attributeValue("name").equals("")) {
            newBuildAttribute.setName(element.attributeValue("name"));
        }
        if (element.attributeValue("type") != null&&!element.attributeValue("type").equals("")) {
            newBuildAttribute.setType(NewBuildAttribute.Type.valueOf(element.attributeValue("type").toUpperCase()));
        }
        if (element.attributeValue("unit") != null&&!element.attributeValue("unit").equals("")) {
            newBuildAttribute.setUnit(element.attributeValue("unit"));
        }
        if (element.attributeValue("select") != null && !element.attributeValue("select").equals("")) {
            newBuildAttribute.setSelects(Arrays.asList(element.attributeValue("select").split(",")));
        }
        if (element.attributeValue("locked") != null && !element.attributeValue("locked").equals("")) {
            newBuildAttribute.setLocked(Boolean.valueOf(element.attributeValue("locked")));
        }
        if (element.attributeValue("formula") != null && !element.attributeValue("formula").equals("")) {
            newBuildAttribute.setFormula(element.attributeValue("formula"));
        }
        newBuildAttributes.add(newBuildAttribute);
    }

    private void initAttribute(List<Element> elements,NewAttributeGroup newAttributeGroup,List<String> stringAlias) throws QQSLException {
        List<NewBuildAttribute> newBuildAttributes = new LinkedList<>();
        List<NewAttributeGroup> childs = new LinkedList<>();
        NewAttributeGroup child;
        for (Element element : elements) {
            if (element.getName().equals("attributeGroup")) {
                child = new NewAttributeGroup();
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
                initAttributes(element,newBuildAttributes,stringAlias,newAttributeGroup);
            }
        }
        if (newBuildAttributes.size() != 0) {
            newAttributeGroup.setNewBuildAttributes(newBuildAttributes);
        }
        if (childs.size() != 0) {
            newAttributeGroup.setChilds(childs);
        }
    }

    public Workbook downloadBuildModel(List<String> list) {
        Workbook wb = new XSSFWorkbook();
        List<NewBuild> builds = new ArrayList<>();
        for (NewBuild build : getBuilds()) {
            for (String s : list) {
                if (build.getType().name().equals(s)) {
                    if (build.getCoordinate() != null) {
                        builds.add((NewBuild) SettingUtils.objectCopy(build));
                    }
                }
                if (build.getChildType() == null) {
                    continue;
                }
                if (build.getChildType().name().equals(s)) {
                    if (build.getCoordinate() != null) {
                        builds.add((NewBuild) SettingUtils.objectCopy(build));
                    }
                }
            }
        }
        if (builds.size() != 0) {
            outBuildModel(wb, builds);
        } else {
            wb = null;
        }
        return wb;
    }

    public void outBuildModel(Workbook wb, List<NewBuild> builds) {
        String[] s={"一","二","三","四","五"};
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;
        for (NewBuild build : builds) {
            if (build.getCoordinate() == null) {
                continue;
            }
            int i = 0/*, n = 0*/;
            sheet = wb.createSheet(build.getName());
            for (int l = 0; l < build.getNumber(); l++) {
                int j = 0, k = 0,m = 0;
                preBuildModel(build, i);
//                n = i;
                writeToCell(i, sheet, row, cell, bold(wb), "编号", "名称", "单位", "值", null, null, null, wb, true, null);
                if (build.getCoordinate() != null) {
                    i = writeAttributeGroup1(i, sheet, row, cell, wb, build.getCoordinate(), s[m], j, k);
                    m++;
                }
                if (build.getWaterResources() != null) {
                    i = writeAttributeGroup1(i, sheet, row, cell, wb, build.getWaterResources(), s[m], j, k);
                    m++;
                }
                if (build.getControlSize() != null) {
                    i = writeAttributeGroup1(i, sheet, row, cell, wb, build.getControlSize(), s[m], j, k);
                    m++;
                }
                if (build.getGroundStress() != null) {
                    i = writeAttributeGroup1(i, sheet, row, cell, wb, build.getGroundStress(), s[m], j, k);
                    m++;
                }
                if (build.getComponent() != null) {
                    i = writeAttributeGroup1(i, sheet, row, cell, wb, build.getComponent(), s[m], j, k);
                }
                i++;
//                sheet.groupRow(n+1,i-1);
            }
            sheet.protectSheet("hyswqqsl");
        }
    }

    private int writeAttributeGroup1(int i, Sheet sheet, Row row, Cell cell, Workbook wb, NewAttributeGroup attributeGroup, String numC,int j,int k) {
        if (k == 0) {
            writeToCell(++i, sheet, row, cell, noBoldHaveBackgroundYellow(wb), numC, attributeGroup.getName(), null, null, null, null, null, wb, true, null);
        }else {
            writeToCell(++i, sheet, row, cell, noBoldHaveBackgroundSkyBlue(wb), numC, attributeGroup.getName(), null, null, null, null, null, wb, true, null);
        }
        if (attributeGroup.getChilds() == null) {
            if (k == 0) {
                j = 0;
                for (NewBuildAttribute buildAttribute : attributeGroup.getNewBuildAttributes()) {
                    if (buildAttribute.getFx() != null) {
                        writeToCell(++i, sheet, row, cell, noBoldAndUnlocked(wb), String.valueOf(++j), buildAttribute.getName(), buildAttribute.getUnit(), null, buildAttribute.getAlias(), buildAttribute.getType(), buildAttribute.getSelects(), wb, buildAttribute.getLocked(), buildAttribute.getFx());
                        continue;
                    }
                    writeToCell(++i, sheet, row, cell, noBold(wb), String.valueOf(++j), buildAttribute.getName(), buildAttribute.getUnit(), null, buildAttribute.getAlias(), buildAttribute.getType(), buildAttribute.getSelects(), wb, buildAttribute.getLocked(), buildAttribute.getFx());
                }
            }else{
                j = 0;
                for (NewBuildAttribute buildAttribute : attributeGroup.getNewBuildAttributes()) {
                    if (buildAttribute.getFx() != null) {
                        writeToCell(++i, sheet, row, cell, noBoldAndUnlocked(wb), "" + k + "." + (++j), buildAttribute.getName(), buildAttribute.getUnit(), null, buildAttribute.getAlias(), buildAttribute.getType(), buildAttribute.getSelects(), wb, buildAttribute.getLocked(), buildAttribute.getFx());
                        continue;
                    }
                    writeToCell(++i, sheet, row, cell, noBold(wb), "" + k + "." + (++j), buildAttribute.getName(), buildAttribute.getUnit(), null, buildAttribute.getAlias(), buildAttribute.getType(), buildAttribute.getSelects(), wb, buildAttribute.getLocked(), buildAttribute.getFx());
                }
            }
        }else{
            for (NewAttributeGroup child : attributeGroup.getChilds()) {
                i = writeAttributeGroup1(i, sheet, row, cell, wb, child, String.valueOf(++k),j,k);
            }
        }
        return i;
    }

    /**
     * 预构建excel结构
     * @param build
     */
    private void preBuildModel(NewBuild build,int i) {
        writeToCell3(i, null);
        i = writeAttributeGroup3(i, build.getCoordinate());
        i = writeAttributeGroup3(i, build.getWaterResources());
        i = writeAttributeGroup3(i, build.getControlSize());
        i = writeAttributeGroup3(i, build.getGroundStress());
        i = writeAttributeGroup3(i, build.getComponent());
        i++;
        List<NewBuildAttribute> buildAttributes = new ArrayList<>();
        pickedAttribute(build.getCoordinate(),buildAttributes);
        pickedAttribute(build.getWaterResources(),buildAttributes);
        pickedAttribute(build.getControlSize(),buildAttributes);
        pickedAttribute(build.getGroundStress(),buildAttributes);
        pickedAttribute(build.getComponent(),buildAttributes);
        replaceFx(build.getCoordinate(),buildAttributes);
        replaceFx(build.getWaterResources(),buildAttributes);
        replaceFx(build.getControlSize(),buildAttributes);
        replaceFx(build.getGroundStress(),buildAttributes);
        replaceFx(build.getComponent(),buildAttributes);
    }

    private void writeToCell3(int i, NewBuildAttribute buildAttribute) {
        if (buildAttribute == null) {
            return;
        }
        buildAttribute.setRow(i + 1);
    }

    private int writeAttributeGroup3(int i, NewAttributeGroup newAttributeGroup) {
        if (newAttributeGroup == null) {
            return i;
        }
        writeToCell3(++i, null);
        if (newAttributeGroup.getChilds() == null) {
            for (NewBuildAttribute buildAttribute : newAttributeGroup.getNewBuildAttributes()) {
                writeToCell3(++i, buildAttribute);
            }
        }else{
            for (NewAttributeGroup child : newAttributeGroup.getChilds()) {
                i = writeAttributeGroup3(i, child);
            }
        }
        return i;
    }

    private void pickedAttribute(NewAttributeGroup attributeGroup, List<NewBuildAttribute> buildAttributes) {
        if (attributeGroup == null) {
            return;
        }
        if (attributeGroup.getChilds() == null) {
            buildAttributes.addAll(attributeGroup.getNewBuildAttributes());
        } else {
            for (NewAttributeGroup child : attributeGroup.getChilds()) {
                pickedAttribute(child, buildAttributes);
            }
        }
    }

    private void replaceFx(NewAttributeGroup attributeGroup,List<NewBuildAttribute> buildAttributes) {
        if (attributeGroup == null) {
            return;
        }
        if (attributeGroup.getChilds() == null) {
            for (NewBuildAttribute buildAttribute : attributeGroup.getNewBuildAttributes()) {
                if (buildAttribute.getFormula() == null) {
                    continue;
                }
                buildAttribute.setFx(buildAttribute.getFormula());
                for (NewBuildAttribute attribute1 : buildAttributes) {
                    buildAttribute.setFx(buildAttribute.getFx().replaceAll(attribute1.getAlias(), "D" + attribute1.getRow()));
                }
            }
        } else {
            for (NewAttributeGroup child : attributeGroup.getChilds()) {
                replaceFx(child, buildAttributes);
            }
        }
    }
}
