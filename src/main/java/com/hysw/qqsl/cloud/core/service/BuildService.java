package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.dao.BuildDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.QQSLException;
import com.hysw.qqsl.cloud.core.entity.builds.AttribeGroup;
import com.hysw.qqsl.cloud.core.entity.data.Attribe;
import com.hysw.qqsl.cloud.core.entity.data.Build;
import com.hysw.qqsl.cloud.core.entity.data.Coordinate;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

/**
 * Created by leinuo on 17-4-13.
 */
@Service("buildService")
public class BuildService extends BaseService<Build,Long> {

    @Autowired
    private BuildDao buildDao;
    @Autowired
    private BuildGroupService buildGroupService;
    @Autowired
    private FieldWorkService fieldWorkService;
    @Autowired
    private AttribeGroupService attribeGroupService;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    public void setBaseDao(BuildDao buildDao) {
        super.setBaseDao( buildDao);
    }

    public List<Build> findByProject(Project project) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("project", project));
        List<Build> list = buildDao.findList(0, null, filters);
        for (Build build : list) {
            build.getId();
            if (build.getAttribeList() == null) {
                continue;
            }
            for (Attribe attribe : build.getAttribeList()) {
                attribe.getId();
            }
        }
        return list;
    }

    @Override
    public Build find(Long id){
        Build build = super.find(id);
        if (build == null) {
            return null;
        }
        build.getId();
        for (Attribe attribe : build.getAttribeList()) {
            attribe.getId();
        }
        return build;
    }
    //      public void findByProject() {
//        List<Filter> filters = new ArrayList<>();
//        filters.add(Filter.eq(""))
//    }

    public List<Build> findByProjectAndAlias(Project project) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("project", project));
//        filters.add(Filter.eq("alias", alias));
        List<Build> list = buildDao.findList(0, null, filters);
        return list;
    }


    public List<Build> findByProjectAndSource(Project project, Build.Source source) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("project", project));
        filters.add(Filter.eq("source", source));
        List<Build> list = buildDao.findList(0, null, filters);
        for (Build build : list) {
            build.getId();
            if (build.getAttribeList() == null) {
                continue;
            }
            for (Attribe attribe : build.getAttribeList()) {
                attribe.getId();
            }
        }
        return list;
    }

    public List<Build> findByProjectAndSourceCoordinateId(Project project, Build.Source source,Long coordinateId) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("project", project));
        filters.add(Filter.eq("source", source));
        filters.add(Filter.eq("coordinateId", coordinateId));
        List<Filter> filters1 = new ArrayList<>();
        filters1.add(Filter.eq("project", project));
        filters1.add(Filter.eq("source", source));
        filters1.add(Filter.isNull("coordinateId"));
        List<Build> list = buildDao.findList(0, null, filters,filters1);
        for (Build build : list) {
            build.getId();
            if (build.getAttribeList() == null) {
                continue;
            }
            for (Attribe attribe : build.getAttribeList()) {
                attribe.getId();
            }
        }
        return list;
    }

    public JSONObject buildJson(Build build2) {
        Build build = null;
        List<Build> builds1 = getBuilds();
        for (Build build1 : builds1) {
            if (build2.getType().equals(build1.getType())) {
                build = (Build) SettingUtils.objectCopy(build1);
                fieldWorkService.setProperty(build,build2,true);
                break;
            }
        }
        JSONObject jsonObject;
        JSONObject jsonObject1;
        jsonObject = new JSONObject();
        jsonObject.put("id", build.getId());
        jsonObject.put("name", build.getName());
        jsonObject.put("alias", build.getAlias());
        jsonObject.put("type", build.getType());
        jsonObject.put("centerCoor", build.getCenterCoor());
        jsonObject.put("positionCoor", build.getPositionCoor());
        jsonObject.put("remark", build.getRemark());
        jsonObject1 = new JSONObject();
        writeAttribeGroup(build.getCoordinate(),jsonObject1);
        if (!jsonObject1.isEmpty()) {
            jsonObject.put("coordinate", jsonObject1);
        }
        jsonObject1 = new JSONObject();
        writeAttribeGroup(build.getWaterResources(),jsonObject1);
        if (!jsonObject1.isEmpty()) {
            jsonObject.put("waterResources", jsonObject1);
        }
        jsonObject1 = new JSONObject();
        writeAttribeGroup(build.getControlSize(),jsonObject1);
        if (!jsonObject1.isEmpty()) {
            jsonObject.put("controlSize", jsonObject1);
        }
        jsonObject1 = new JSONObject();
        writeAttribeGroup(build.getGroundStress(),jsonObject1);
        if (!jsonObject1.isEmpty()) {
            jsonObject.put("groundStress", jsonObject1);
        }
        jsonObject1 = new JSONObject();
        writeAttribeGroup(build.getComponent(),jsonObject1);
        if (!jsonObject1.isEmpty()) {
            jsonObject.put("component", jsonObject1);
        }
        return jsonObject;
    }

    private void writeAttribeGroup(AttribeGroup attribeGroup, JSONObject jsonObject) {
        if (attribeGroup == null) {
            return;
        }
        JSONArray jsonArray;
        jsonArray = new JSONArray();
        writeAttribe(attribeGroup.getAttribes(),jsonArray);
        if (!jsonArray.isEmpty()) {
            jsonObject.put("name", attribeGroup.getName());
            jsonObject.put("alias", attribeGroup.getAlias());
            jsonObject.put("attribes", jsonArray);
        }
        jsonArray = new JSONArray();
        writeChild(attribeGroup.getChilds(), jsonArray);
        if (!jsonArray.isEmpty()) {
            jsonObject.put("name", attribeGroup.getName());
            jsonObject.put("alias", attribeGroup.getAlias());
            jsonObject.put("child", jsonArray);
        }
    }

    private void writeChild(List<AttribeGroup> attribeGroups, JSONArray jsonArray) {
        JSONObject jsonObject;
        if (attribeGroups == null) {
            return;
        }
        for (AttribeGroup attribeGroup : attribeGroups) {
            jsonObject = new JSONObject();
            writeAttribeGroup(attribeGroup, jsonObject);
            if (!jsonObject.isEmpty()) {
                jsonArray.add(jsonObject);
            }
        }
    }

    private void writeAttribe(List<Attribe> attribes, JSONArray jsonArray) {
        if (attribes == null) {
            return;
        }
        JSONObject jsonObject;
        for (Attribe attribe : attribes) {
            if (attribe.getValue() == null) {
                continue;
            }
            jsonObject = new JSONObject();
            jsonObject.put("id", attribe.getId());
            jsonObject.put("name", attribe.getName());
            jsonObject.put("alias", attribe.getAlias());
            jsonObject.put("type", attribe.getType());
            jsonObject.put("value", attribe.getValue());
            if (attribe.getSelects() != null && attribe.getSelects().size() != 0) {
                jsonObject.put("selects", attribe.getSelects());
            }
            if (attribe.getUnit() != null && !attribe.getUnit().equals("")) {
                jsonObject.put("unit", attribe.getUnit());
            }
            jsonArray.add(jsonObject);
        }
    }

    public List<Build> findByCoordinateId(long id) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("coordinateId", id));
        return buildDao.findList(0, null, filters);
     }

    public void removes(List<Build> builds) {
        for (Build build : builds) {
            remove(build);
        }
    }


    ///////////////////////////////////////newBuild新建筑物模板构建////////////////////////////////////////////////////////////
    public List<Build> initBuildModel(String xml) throws QQSLException {
        Element root = null;
        try {
            root = SettingUtils.getInstance().getRootElement(xml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        List<String> stringAlias = new ArrayList<>();
        List<Element> elements = SettingUtils.getInstance().getElementGroupList(root);
        Map<String, Build> buildMap = new HashMap<>();
        Build build;
        for (Element element : elements) {
            build = new Build();
            if (element.attributeValue("name") != null&&!element.attributeValue("name").equals("")) {
                build.setName(element.attributeValue("name"));
            }
            if (element.attributeValue("childType") != null&&!element.attributeValue("childType").equals("")) {
                build.setChildType(Build.ChildType.valueOf(element.attributeValue("childType")));
                build.setType(build.getChildType().getCommonType());
            }else{
                if (element.attributeValue("type") != null&&!element.attributeValue("type").equals("")) {
                    build.setType(CommonEnum.CommonType.valueOf(element.attributeValue("type")));
                }
            }
            if (element.attributeValue("alias") != null&&!element.attributeValue("alias").equals("")) {
                build.setAlias(element.attributeValue("alias"));
                if (stringAlias.contains(element.attributeValue("alias"))) {
                    throw new QQSLException("alias:重复:"+element.attributeValue("alias"));
                }else {
                    stringAlias.add(element.attributeValue("alias"));
                }
            }
            if (element.attributeValue("number") != null && !element.attributeValue("number").equals("")) {
                build.setNumber(Integer.valueOf(element.attributeValue("number")));
            }
            buildMap.put(build.getAlias(),build);
        }
        attribeGroupService.initAttribeGroup(buildMap,SettingUtils.getInstance().getSetting().getCoordinate(),stringAlias);
        attribeGroupService.initAttribeGroup(buildMap,SettingUtils.getInstance().getSetting().getWaterResources(),stringAlias);
        attribeGroupService.initAttribeGroup(buildMap,SettingUtils.getInstance().getSetting().getControlSize(),stringAlias);
        attribeGroupService.initAttribeGroup(buildMap,SettingUtils.getInstance().getSetting().getGroundStress(),stringAlias);
        attribeGroupService.initAttribeGroup(buildMap, SettingUtils.getInstance().getSetting().getComponent(), stringAlias);
        List<Build> builds = new ArrayList<>();
        for (Map.Entry<String, Build> entry : buildMap.entrySet()) {
            builds.add(entry.getValue());
        }
        return builds;
    }

    public List<Build> getBuilds(){
        Cache cache = cacheManager.getCache("buildModelCache");
        net.sf.ehcache.Element element = cache.get("builds");
        if (element == null) {
            try {
                element = new net.sf.ehcache.Element("builds", initBuildModel(SettingUtils.getInstance().getSetting().getBuild()));
                cache.put(element);
                return (List<Build>) element.getValue();
            } catch (QQSLException e) {
                e.printStackTrace();
                return null;
            }
        }
        return (List<Build>) element.getValue();
    }

    public void outBuildModel(Workbook wb, List<Build> builds) {
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;
        for (Build build : builds) {
            if (build.getCoordinate() == null) {
                continue;
            }
            int i = 0/*, n = 0*/;
            sheet = wb.createSheet(build.getName());
            for (int l = 0; l < build.getNumber(); l++) {
                int j = 0, k = 0;
                preBuildModel(build, i);
//                n = i;
                writeToCell(i, sheet, row, cell, bold(wb), "编号", "名称", "单位", "值", null, null, null, wb, true, null);
                i = writeAttribeGroup1(i, sheet, row, cell, wb, build.getCoordinate(), "一", j, k);
                i = writeAttribeGroup1(i, sheet, row, cell, wb, build.getWaterResources(), "二", j, k);
                i = writeAttribeGroup1(i, sheet, row, cell, wb, build.getControlSize(), "三", j, k);
                i = writeAttribeGroup1(i, sheet, row, cell, wb, build.getGroundStress(), "四", j, k);
                i = writeAttribeGroup1(i, sheet, row, cell, wb, build.getComponent(), "五", j, k);
                i++;
//                sheet.groupRow(n+1,i-1);
            }
            sheet.protectSheet("hyswqqsl");
        }
    }

    /**
     * 预构建excel结构
     * @param build
     */
    private void preBuildModel(Build build,int i) {
        writeToCell3(i, null);
        i = writeAttribeGroup3(i, build.getCoordinate());
        i = writeAttribeGroup3(i, build.getWaterResources());
        i = writeAttribeGroup3(i, build.getControlSize());
        i = writeAttribeGroup3(i, build.getGroundStress());
        i = writeAttribeGroup3(i, build.getComponent());
        i++;
        List<Attribe> attribes = new ArrayList<>();
        pickedAttribe(build.getCoordinate(),attribes);
        pickedAttribe(build.getWaterResources(),attribes);
        pickedAttribe(build.getControlSize(),attribes);
        pickedAttribe(build.getGroundStress(),attribes);
        pickedAttribe(build.getComponent(),attribes);
        replaceFx(build.getCoordinate(),attribes);
        replaceFx(build.getWaterResources(),attribes);
        replaceFx(build.getControlSize(),attribes);
        replaceFx(build.getGroundStress(),attribes);
        replaceFx(build.getComponent(),attribes);
    }

    private void pickedAttribe(AttribeGroup attribeGroup, List<Attribe> attribes) {
        if (attribeGroup == null) {
            return;
        }
        if (attribeGroup.getChilds() == null) {
            attribes.addAll(attribeGroup.getAttribes());
        } else {
            for (AttribeGroup child : attribeGroup.getChilds()) {
                pickedAttribe(child, attribes);
            }
        }
    }

    private void replaceFx(AttribeGroup attribeGroup,List<Attribe> attribes) {
        if (attribeGroup == null) {
            return;
        }
        if (attribeGroup.getChilds() == null) {
            for (Attribe attribe : attribeGroup.getAttribes()) {
                if (attribe.getFormula() == null) {
                    continue;
                }
                attribe.setFx(attribe.getFormula());
                for (Attribe attribe1 : attribes) {
                    attribe.setFx(attribe.getFx().replaceAll(attribe1.getAlias(), "D" + attribe1.getRow()));
                }
            }
        } else {
            for (AttribeGroup child : attribeGroup.getChilds()) {
                replaceFx(child, attribes);
            }
        }
    }

    private int writeAttribeGroup3(int i, AttribeGroup attribeGroup) {
        if (attribeGroup == null) {
            return i;
        }
        writeToCell3(++i, null);
        if (attribeGroup.getChilds() == null) {
            for (Attribe attribe : attribeGroup.getAttribes()) {
                writeToCell3(++i, attribe);
            }
        }else{
            for (AttribeGroup child : attribeGroup.getChilds()) {
                i = writeAttribeGroup3(i, child);
            }
        }
        return i;
    }

    private void writeToCell3(int i, Attribe attribe) {
        if (attribe == null) {
            return;
        }
        attribe.setRow(i + 1);
    }

    private int writeAttribeGroup1(int i, Sheet sheet, Row row, Cell cell, Workbook wb, AttribeGroup attribeGroup, String numC,int j,int k) {
        if (attribeGroup == null) {
            return i;
        }
        if (k == 0) {
            writeToCell(++i, sheet, row, cell, noBoldHaveBackgroundYellow(wb), numC, attribeGroup.getName(), null, null, null, null, null, wb, true, null);
        }else {
            writeToCell(++i, sheet, row, cell, noBoldHaveBackgroundSkyBlue(wb), numC, attribeGroup.getName(), null, null, null, null, null, wb, true, null);
        }
        if (attribeGroup.getChilds() == null) {
            if (k == 0) {
                j = 0;
                for (Attribe attribe : attribeGroup.getAttribes()) {
                    if (attribe.getFx() != null) {
                        writeToCell(++i, sheet, row, cell, noBoldAndUnlocked(wb), String.valueOf(++j), attribe.getName(), attribe.getUnit(), null, attribe.getAlias(), attribe.getType(), attribe.getSelects(), wb, attribe.getLocked(), attribe.getFx());
                        continue;
                    }
                    writeToCell(++i, sheet, row, cell, noBold(wb), String.valueOf(++j), attribe.getName(), attribe.getUnit(), null, attribe.getAlias(), attribe.getType(), attribe.getSelects(), wb, attribe.getLocked(), attribe.getFx());
                }
            }else{
                j = 0;
                for (Attribe attribe : attribeGroup.getAttribes()) {
                    if (attribe.getFx() != null) {
                        writeToCell(++i, sheet, row, cell, noBoldAndUnlocked(wb), "" + k + "." + (++j), attribe.getName(), attribe.getUnit(), null, attribe.getAlias(), attribe.getType(), attribe.getSelects(), wb, attribe.getLocked(), attribe.getFx());
                        continue;
                    }
                    writeToCell(++i, sheet, row, cell, noBold(wb), "" + k + "." + (++j), attribe.getName(), attribe.getUnit(), null, attribe.getAlias(), attribe.getType(), attribe.getSelects(), wb, attribe.getLocked(), attribe.getFx());
                }
            }
        }else{
            for (AttribeGroup child : attribeGroup.getChilds()) {
                i = writeAttribeGroup1(i, sheet, row, cell, wb, child, String.valueOf(++k),j,k);
            }
        }
        return i;
    }

    private void writeToCell(int i, Sheet sheet, Row row, Cell cell, CellStyle style, String a, String b, String c, String d, String e, Attribe.Type type, List<String> selects, Workbook wb, boolean locked, String fx) {
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
        if (type == Attribe.Type.SELECT) {
            setXSSFValidation1((XSSFSheet) sheet, selects.toArray(new String[selects.size()]), i, i, 3, 3);
        }
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


    /**
     * 设置某些列的值只能输入预制的数据,显示下拉框.
     *
     * @param sheet
     *            要设置的sheet.
     * @param textlist
     *            下拉框显示的内容
     * @param firstRow
     *            开始行
     * @param endRow
     *            结束行
     * @param firstCol
     *            开始列
     * @param endCol
     *            结束列
     * @return 设置好的sheet.
     */
    public static Sheet setHSSFValidation(Sheet sheet,
                                          String[] textlist, int firstRow, int endRow, int firstCol,
                                          int endCol) {
        // 加载下拉列表内容
        DVConstraint constraint = DVConstraint
                .createExplicitListConstraint(textlist);
        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow,
                endRow, firstCol, endCol);
        // 数据有效性对象
        HSSFDataValidation data_validation_list = new HSSFDataValidation(
                regions, constraint);
        sheet.addValidationData(data_validation_list);
        return sheet;
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

    ///////////////////////////////////////////////建筑物导入////////////////////////////////////////////////////////////
    Map<String, List<Build>> inputBuilds(Map<String, List<Sheet>> buildWBs,Project project){
        Row row;
        String a = null,b = null,c,d,e,comment = null;
        Build build = null;
        Map<String, List<Build>> buildMap = new HashMap<>();
        List<Build> builds;
        Attribe attribe;
        List<Attribe> attribes = null;
        for (Map.Entry<String, List<Sheet>> entry : buildWBs.entrySet()) {
            for (Sheet sheet : entry.getValue()) {
                if (sheet == null) {
                    continue;
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
                            comment = cellComment.getString().getString();
                        }
                    }
                    if (row.getCell(3) != null) {
                        row.getCell(3).setCellType(Cell.CELL_TYPE_STRING);
                        b = row.getCell(3).getStringCellValue();
                    }
                    if (a.trim().equals("编号")) {
                        if (j == 0) {
                            build = pickedBuild(sheet.getSheetName());
                            attribes = new ArrayList<>();
                            comment = null;
                            continue;
                        }else{
                            build.setProject(project);
                            build.setAttribeList(attribes);
                            if (buildMap.get(entry.getKey()) == null) {
                                builds = new ArrayList<>();
                                builds.add(build);
                                buildMap.put(entry.getKey(), builds);
                            } else {
                                builds = buildMap.get(entry.getKey());
                                builds.add(build);
                                buildMap.put(entry.getKey(), builds);
                            }
                            build = pickedBuild(sheet.getSheetName());
                            attribes = new ArrayList<>();
                            continue;
                        }
                    }
                    if (comment == null) {
                        continue;
                    }
                    if (comment.trim().equals("center")) {
                        build.setCenterCoor(b);
                        build.setCenterCoorNum(j+1);
                        comment = null;
                        continue;
                    }
                    if (comment.trim().equals("position")) {
                        build.setPositionCoor(b);
                        build.setPositionCoorNum(j+1);
                        comment = null;
                        continue;
                    }
                    if (comment.trim().equals("designElevation")) {
                        build.setDesignElevation(b);
                        build.setDesignElevationNum(j+1);
                        comment = null;
                        continue;
                    }
                    if (comment.trim().equals("remark")) {
                        build.setRemark(b);
                        build.setRemarkNum(j+1);
                        comment = null;
                        continue;
                    }
                    attribe = new Attribe();
                    attribe.setAlias(comment);
                    comment = null;
                    attribe.setValue(b);
                    attribe.setBuild(build);
                    attribes.add(attribe);
                    if (j == sheet.getLastRowNum()) {
                        build.setProject(project);
                        build.setAttribeList(attribes);
                        if (buildMap.get(entry.getKey()) == null) {
                            builds = new ArrayList<>();
                            builds.add(build);
                            buildMap.put(entry.getKey(), builds);
                        } else {
                            builds = buildMap.get(entry.getKey());
                            builds.add(build);
                            buildMap.put(entry.getKey(), builds);
                        }
                    }
                }
            }
        }
        return buildMap;
    }

    private JSONObject coordinateToJson(String[] split) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("lon", split[0]);
        jsonObject.put("lat", split[1]);
        jsonObject.put("ele", split[2]);
        return jsonObject;
    }

    public Build pickedBuild(String sheetName) {
        List<Build> builds = getBuilds();
        for (Build build : builds) {
            if (build.getChildType() != null) {
                if (build.getChildType().getTypeC().equals(sheetName.trim())) {
                    return (Build) SettingUtils.objectCopy(build);
                }
            }else{
                if (build.getType().getTypeC().equals(sheetName.trim())) {
                    return (Build) SettingUtils.objectCopy(build);
                }
            }
        }
        return null;
    }

    ////////////////////////////////////////////////////新真实建筑物导出/////////////////////////////////////////////////////////////
    public void outputBuilds(List<Build> builds, Workbook wb, String code, Coordinate.WGS84Type wgs84Type) {
//        将建筑物中心坐标，定位坐标，标高，描述手动构建到attribeList中
        makeAttribeToAttribeList(builds, code, wgs84Type);
//        将模板内容写入真实建筑物
        writeModelToRealBuild(builds);
//        将属性构建进attribeGroup
        makeAttribeToAttribeGroup(builds);
//        对建筑物分类
        Map<String, List<Build>> map = pickedTheSameBuild(builds);
//        输出excel
        outBuild(map,wb);
        System.out.println();
    }

    private Map<String, List<Build>> pickedTheSameBuild(List<Build> builds) {
        Map<String, List<Build>> map = new HashMap<>();
        List<Build> builds1;
        for (Build build : builds) {
            if (build.getChildType() != null) {
                if (map.get(build.getChildType().getTypeC()) == null) {
                    builds1 = new ArrayList<>();
                    builds1.add(build);
                    map.put(build.getChildType().getTypeC(), builds1);
                } else {
                    builds1 = map.get(build.getChildType().getTypeC());
                    builds1.add(build);
                    map.put(build.getChildType().getTypeC(), builds1);
                }
            }else{
                if (map.get(build.getType().getTypeC()) == null) {
                    builds1 = new ArrayList<>();
                    builds1.add(build);
                    map.put(build.getType().getTypeC(), builds1);
                } else {
                    builds1 = map.get(build.getType().getTypeC());
                    builds1.add(build);
                    map.put(build.getType().getTypeC(), builds1);
                }
            }
        }
        return map;
    }

    private void outBuild(Map<String, List<Build>> map, Workbook wb) {
        Sheet sheet = null;
        Row row = null;
        Cell cell = null;
        for (Map.Entry<String, List<Build>> entry : map.entrySet()) {
            int i = 0, j = 0, k = 0;
            for (Build build : entry.getValue()) {
                if (build.getAttribeList().size() == 3) {
                    continue;
                }
                if (wb.getSheet(entry.getKey()) == null) {
                    sheet = wb.createSheet(entry.getKey());
                }
                writeToCell(i, sheet, row, cell, bold(wb), "编号", "名称", "单位", "值", null, null, null, wb, true, null);
                i = writeAttribeGroup2(i, sheet, row, cell, wb, build.getCoordinate(), "一", j, k);
                i = writeAttribeGroup2(i, sheet, row, cell, wb, build.getWaterResources(), "二", j, k);
                i = writeAttribeGroup2(i, sheet, row, cell, wb, build.getControlSize(), "三", j, k);
                i = writeAttribeGroup2(i, sheet, row, cell, wb, build.getGroundStress(), "四", j, k);
                i = writeAttribeGroup2(i, sheet, row, cell, wb, build.getComponent(), "五", j, k);
                i++;
            }
            if (wb.getSheet(entry.getKey()) != null) {
                sheet.protectSheet("hyswqqsl");
            }
        }
    }

    private int writeAttribeGroup2(int i, Sheet sheet, Row row, Cell cell, Workbook wb, AttribeGroup attribeGroup, String numC,int j,int k) {
        if (attribeGroup == null) {
            return i;
        }
        if (k == 0) {
            writeToCell(++i, sheet, row, cell, noBoldHaveBackgroundYellow(wb), numC, attribeGroup.getName(), null, null, null, null, null, wb, true, null);
        }else {
            writeToCell(++i, sheet, row, cell, noBoldHaveBackgroundSkyBlue(wb), numC, attribeGroup.getName(), null, null, null, null, null, wb, true, null);
        }
        if (attribeGroup.getChilds() == null) {
            if (k == 0) {
                j = 0;
                for (Attribe attribe : attribeGroup.getAttribes()) {
                    writeToCell(++i, sheet, row, cell, noBold(wb), String.valueOf(++j), attribe.getName(), attribe.getUnit(), attribe.getValue(), attribe.getAlias(), attribe.getType(), attribe.getSelects(), wb, attribe.getLocked(), attribe.getFx());
                }
            }else{
                j = 0;
                for (Attribe attribe : attribeGroup.getAttribes()) {
                    writeToCell(++i, sheet, row, cell, noBold(wb), "" + k + "." + (++j), attribe.getName(), attribe.getUnit(), attribe.getValue(), attribe.getAlias(), attribe.getType(), attribe.getSelects(), wb, attribe.getLocked(), attribe.getFx());
                }
            }
        }else{
            for (AttribeGroup child : attribeGroup.getChilds()) {
                i = writeAttribeGroup2(i, sheet, row, cell, wb, child, String.valueOf(++k),j,k);
            }
        }
        return i;
    }

    private void makeAttribeToAttribeGroup(List<Build> builds) {
        for (Build build : builds) {
//            将属性写入attribeGroup
            attribeToAttribeGroup(build.getAttribeList(), build.getCoordinate());
            attribeToAttribeGroup(build.getAttribeList(), build.getWaterResources());
            attribeToAttribeGroup(build.getAttribeList(), build.getControlSize());
            attribeToAttribeGroup(build.getAttribeList(), build.getGroundStress());
            attribeToAttribeGroup(build.getAttribeList(), build.getComponent());
        }
    }

    private void attribeToAttribeGroup(List<Attribe> attribeList, AttribeGroup attribeGroup) {
        if (attribeGroup == null) {
            return;
        }
        if (attribeGroup.getChilds() == null) {
            buildAttribeGroup(attribeList,attribeGroup);
        }else{
            for (AttribeGroup child : attribeGroup.getChilds()) {
                attribeToAttribeGroup(attribeList, child);
            }
        }
    }

    private void buildAttribeGroup(List<Attribe> attribeList, AttribeGroup attribeGroup) {
        for (Attribe attribe : attribeList) {
            for (Attribe groupAttribe : attribeGroup.getAttribes()) {
                if (groupAttribe.getAlias().equals(attribe.getAlias())) {
                    groupAttribe.setValue(attribe.getValue());
                    break;
                }
            }
        }
    }

    private void writeModelToRealBuild(List<Build> builds) {
        Build build1;
        for (Build build : builds) {
            if (build.getChildType() != null) {
                build1 = pickedBuild(build.getChildType().getTypeC());
            }else{
                build1 = pickedBuild(build.getType().getTypeC());
            }
            build.setName(build1.getName());
            build.setAlias(build1.getAlias());
            build.setCoordinate(build1.getCoordinate());
            build.setWaterResources(build1.getWaterResources());
            build.setControlSize(build1.getControlSize());
            build.setGroundStress(build1.getGroundStress());
            build.setComponent(build1.getComponent());
        }
    }

    private void makeAttribeToAttribeList(List<Build> builds, String code, Coordinate.WGS84Type wgs84Type) {
        Attribe attribe;
        List<Attribe> attribes;
        for (Build build : builds) {
            attribes = build.getAttribeList();
            attribe = new Attribe();
            attribe.setValue(jsonToCoordinate(build.getCenterCoor(), code, wgs84Type));
            attribe.setAlias("center");
            attribes.add(attribe);
            if (build.getPositionCoor() != null) {
                attribe = new Attribe();
                attribe.setValue(jsonToCoordinate(build.getPositionCoor(), code, wgs84Type));
                attribe.setAlias("position");
                attribes.add(attribe);
            }
            attribe = new Attribe();
            attribe.setValue(build.getDesignElevation());
            attribe.setAlias("designElevation");
            attribes.add(attribe);
            attribe = new Attribe();
            attribe.setValue(build.getRemark());
            attribe.setAlias("remark");
            attribes.add(attribe);
            build.setAttribeList(attribes);
        }
    }

    private String jsonToCoordinate(String coor, String code, Coordinate.WGS84Type wgs84Type) {
        JSONObject jsonObject = JSONObject.fromObject(coor);
        JSONObject jsonObject1 = fieldWorkService.coordinateBLHToXYZ(jsonObject.get("lon").toString(), jsonObject.get("lat").toString(), code, wgs84Type);
        return jsonObject1.get("lon") + "," + jsonObject1.get("lat") + "," + jsonObject.get("ele");
    }

    public void saveBuild(Build build, List<Build> builds, List<Coordinate> coordinates,List<Build> builds1) {
        Build build1;
        if (build.isErrorMsg()) {
            return;
        }
        Coordinate coordinate = buildBelongToCoordinate(build, coordinates);
        if (builds.size() == 0) {
            build.setSource(Build.Source.DESIGN);
            if (coordinate != null) {
                build.setCoordinateId(coordinate.getId());
            } else {
                build.setCoordinateId(null);
            }
            save(build);
            builds1.add(build);
            return;
        }
        boolean flag = true;
        Iterator<Build> iterator = builds.iterator();
        while (iterator.hasNext()) {
            build1 = iterator.next();
            if (checkCenterCoordinateIsSame(build1.getCenterCoor(), build.getCenterCoor())) {
                if (build1.getAttribeList().size() == 0 && (build.getAttribeList() == null || build.getAttribeList().size() == 0)) {
                    build1.setPositionCoor(build.getPositionCoor());
                    build1.setDesignElevation(build.getDesignElevation());
                    build1.setRemark(build.getRemark());
                    build1.setChildType(build.getChildType());
                    build1.setType(build.getType());
                    if (coordinate != null) {
                        build1.setCoordinateId(coordinate.getId());
                    } else {
                        build1.setCoordinateId(null);
                    }
                    build1.setSource(Build.Source.DESIGN);
                    save(build1);
                } else if (build1.getAttribeList().size() == 0) {
                    remove(build1);
                    builds.remove(build1);
                    build.setSource(Build.Source.DESIGN);
                    if (coordinate != null) {
                        build.setCoordinateId(coordinate.getId());
                    } else {
                        build.setCoordinateId(null);
                    }
                    save(build);
                    builds1.add(build);
                } else if (build.getAttribeList().size() == 0) {

                } else {
                    remove(build1);
                    builds.remove(build1);
                    build.setSource(Build.Source.DESIGN);
                    if (coordinate != null) {
                        build.setCoordinateId(coordinate.getId());
                    } else {
                        build.setCoordinateId(null);
                    }
                    save(build);
                    builds1.add(build);
                }
                flag = false;
                break;
            }
        }
        if (flag) {
            build.setSource(Build.Source.DESIGN);
            if (coordinate != null) {
                build.setCoordinateId(coordinate.getId());
            } else {
                build.setCoordinateId(null);
            }
            save(build);
            builds1.add(build);
        }
    }

    private Coordinate buildBelongToCoordinate(Build build, List<Coordinate> coordinates) {
        JSONArray jsonArray;
        JSONObject jsonObject;
        for (Coordinate coordinate : coordinates) {
            JSONObject jsonObject1 = JSONObject.fromObject(coordinate.getCoordinateStr());
            jsonArray = (JSONArray) jsonObject1.get("coordinate");
            for (Object o : jsonArray) {
                jsonObject = JSONObject.fromObject(o);
                if (checkCenterCoordinateIsSame(jsonObject.toString(), build.getCenterCoor())) {
                    return coordinate;
                }
            }
        }
        return null;
    }

    private boolean checkCenterCoordinateIsSame(String var1, String var2) {
        JSONObject jsonObject1 = JSONObject.fromObject(var1);
        JSONObject jsonObject2 = JSONObject.fromObject(var2);
        if (jsonObject1.get("lon").toString().equals(jsonObject2.get("lon").toString())&&
                jsonObject1.get("lat").toString().equals(jsonObject2.get("lat").toString())&&
                jsonObject1.get("ele").toString().equals(jsonObject2.get("ele").toString())) {
            return true;
        }
        return false;
    }

    public JSONArray getBuildJson() {
        List<Build> builds = getBuilds();
        JSONObject jsonObject,jsonObject1;
        JSONArray jsonArray = new JSONArray(), jsonArray1 = null;
        for (Build build : builds) {
            jsonObject1 = new JSONObject();
            jsonObject1.put("alias", build.getAlias());
            jsonObject1.put("name", build.getName());
            jsonObject1.put("py", build.getType().getAbbreviate());
            jsonObject1.put("type", build.getType().name());
            jsonObject1.put("childType", build.getChildType() == null ? null : build.getChildType().name());
            boolean flag = true;
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject2 = (JSONObject) jsonArray.get(i);
                if (jsonObject2.get("name").toString().equals(build.getType().getBuildType())) {
                    jsonArray1 = (JSONArray) jsonObject2.get("builds");
                    flag = false;
                    break;
                }
            }
            if (flag) {
                jsonObject = new JSONObject();
                jsonObject.put("name", build.getType().getBuildType());
                jsonArray1 = new JSONArray();
                jsonArray1.add(jsonObject1);
                jsonObject.put("builds", jsonArray1);
                jsonArray.add(jsonObject);
            } else {
                jsonArray1.add(jsonObject1);
            }
        }
        return jsonArray;
    }
}
