package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.entity.build.AttribeGroup;
import com.hysw.qqsl.cloud.entity.build.Config;
import com.hysw.qqsl.cloud.entity.data.Build;
import com.hysw.qqsl.cloud.entity.data.Project;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenl on 17-4-21.
 */
public class FieldServiceTest extends BaseTest {
    @Autowired
    private FieldService fieldService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private BuildBelongService buildBelongService;
    @Autowired
    private BuildGroupService buildGroupService;

    @Test
    public void testfield(){
        fieldService.field(projectService.find(531l), Build.Source.DESIGN);
    }

    @Test
    public void testWriteExcel() throws IOException {
        Project project = projectService.find(818l);
//        fieldService.writeExcel(project, Build.Source.DESIGN);
    }


    /**
     * 输出模板
     */
    @Test
    public void testWriteToModel() throws IOException {
        List<Build> buildList;
        Map<Config.CommonType, List<Build>> map = new LinkedHashMap<>();
        Workbook wb = new HSSFWorkbook();
        List<Build> builds = buildGroupService.getBuilds();
        for (Build build : builds) {
            List<Build> builds1 = map.get(build.getType());
            if (builds1 == null || builds1.size() == 0) {
                buildList = new ArrayList<>();
                buildList.add(build);
                map.put(build.getType(), buildList);
            }else{
                builds1.add(build);
                map.put(build.getType(), builds1);
            }
        }
        writeBuildToExcel(map,wb);
        OutputStream os = new FileOutputStream(new File("111123.xls"));
        wb.write(os);
        os.close();
    }

    class WriteExecl{
        private int index;
        private int max;

        WriteExecl(){
            this.index = 0;
        }

        public int getIndexAdd() {
            if (max < index) {
                max = index;
            }
            return index++;
        }

        public int getIndex() {
            return index;
        }

        public int getIndexMinus(){
            return index--;
        }

        public int getMax() {
            return max;
        }
    }


    void writeBuildToExcel(Map<Config.CommonType, List<Build>> map, Workbook wb) {
        Row row = null;
        Cell cell = null;
        final String[] num = {"一","二","三","四","五","六","七"};
        for (Map.Entry<Config.CommonType, List<Build>> entry : map.entrySet()) {
            Sheet sheet = null;
            WriteExecl we = new WriteExecl();
            for (int i = 0; i < CommonAttributes.BASETYPEE.length; i++) {
                if (CommonAttributes.BASETYPEE[i].equals(entry.getKey().toString())) {
                    sheet = wb.createSheet(CommonAttributes.BASETYPEC[i]);
                    break;
                }
            }
            List<Build>  builds= entry.getValue();
            for (int i = 0; i < builds.size(); i++) {
                CellStyle style1 = wb.createCellStyle();
                style1.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                Font font1 = wb.createFont();
                font1.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                font1.setFontName("宋体");//设置字体名称
                style1.setFont(font1);
                style1.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
                style1.setBorderBottom(HSSFCellStyle.BORDER_THIN);//下边框
                style1.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
                style1.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
                writeToCell(sheet,row,cell,style1,we,"编号","名称","单位","值",null,true);

                CellStyle style = wb.createCellStyle();
                style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                Font font = wb.createFont();
                font.setFontName("宋体");//设置字体名称
                style.setBorderTop(HSSFCellStyle.BORDER_THIN);//上边框
                style.setBorderBottom(HSSFCellStyle.BORDER_THIN);//下边框
                style.setBorderLeft(HSSFCellStyle.BORDER_THIN);//左边框
                style.setBorderRight(HSSFCellStyle.BORDER_THIN);//右边框
                style.setFont(font);
                //坐标头
                writeToCell(sheet,row,cell,style,we,num[0],"坐标",null,null,null,true);

                writeToCell(sheet,row,cell,style,we,"1","中心点","经度,纬度,高程",null,"coor1",true);

                writeToCell(sheet,row,cell,style,we,"2","定位点","经度,纬度,高程",null,"coor2",true);

                int n = 1;

                writeToCell(sheet,row,cell,style,we,num[n++],"描述",null,null,"remark",true);

                int aa = we.getIndex();
                writeToExcel(style, sheet, row, cell, we, builds.get(i).getMaterAttribeGroup(),num[n],false,true);
                if (aa != we.getIndex()) {
                    n++;
                    aa = we.getIndex();
                }
                writeToExcel(style, sheet, row, cell, we, builds.get(i).getHydraulicsAttribeGroup(),num[n],false,true);
                if (aa != we.getIndex()) {
                    n++;
                    aa = we.getIndex();
                }
                writeToExcel(style, sheet, row, cell, we, builds.get(i).getDimensionsAttribeGroup(),num[n],false,true);
                if (aa != we.getIndex()) {
                    n++;
                    aa = we.getIndex();
                }
                writeToExcel(style, sheet, row, cell, we, builds.get(i).getStructureAttribeGroup(),num[n],false,true);
//                if (aa != we.getIndex()) {
//                    n++;
//                    aa = we.getIndex();
//                }
//                writeToExcel(style, sheet, row, cell, we, builds.get(i).getGeologyAttribeGroup(),num[n]);

//                a++;
            }
            int max = we.getMax();
            while (we.getIndex() <= max) {
                sheet.removeRow(sheet.createRow(we.getIndexAdd()));
            }
        }

    }

    private void writeToCell(Sheet sheet, Row row, Cell cell, CellStyle style, WriteExecl we, String a, String b, String c, String d, String e, boolean isAdd) {
        if (isAdd) {
            row = sheet.createRow(we.getIndexAdd());
        }else{
            row = sheet.createRow(we.getIndex());
        }
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
        if (c != null) {
            cell.setCellValue(c);
        }
        cell.setCellStyle(style);
        cell = row.createCell(3);
        if (e != null) {
            Drawing patriarch = sheet.createDrawingPatriarch();
            Comment comment1 = patriarch.createCellComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 4, 2, (short) 6, 5));
            comment1.setString(new HSSFRichTextString(e));
            row.getCell(3).setCellComment(comment1);
        }
        if (d != null) {
            cell.setCellValue(d);
        }
        cell.setCellStyle(style);
    }

    boolean writeToExcel(CellStyle style, Sheet sheet, Row row, Cell cell, WriteExecl we, AttribeGroup attribeGroup, String sign, boolean flag, boolean isComment) {
        if (attribeGroup == null) {
            return false;
        }
        writeToCell(sheet, row, cell,style, we, sign, attribeGroup.getName(), null, null, null, true);
        Drawing patriarch = sheet.createDrawingPatriarch();
        boolean flag1 = false;
        boolean flag2 = false;
        int s=1;
        String ss = "";
        String sss = "";
        int a = 0;
        if (attribeGroup.getAttribes() != null) {
            for (int j = 0; j < attribeGroup.getAttribes().size(); j++) {
//                if (attribeGroup.getAttribes().get(j).getValue() != null && !attribeGroup.getAttribes().get(j).getValue().equals("")) {
                    if (attribeGroup.getName().equals(attribeGroup.getAttribes().get(j).getName())) {
                        flag = true;
                        continue;
                    }else{
                        row = sheet.createRow(we.getIndexAdd());
                        cell = row.createCell(0);
                        int e = a + 1;
                        a++;
                        if (sign.equals("一") || sign.equals("二") || sign.equals("三") || sign.equals("四") || sign.equals("五") || sign.equals("六") || sign.equals("七")) {
                            cell.setCellValue(e);
                        } else {
                            cell.setCellValue(sign + "." + e);
                        }
                        if (j == attribeGroup.getAttribes().size() - 1) {
                            s = s + e;
                        }
                        cell.setCellStyle(style);
                        cell = row.createCell(1);
                        cell.setCellValue(attribeGroup.getAttribes().get(j).getName());
                        cell.setCellStyle(style);
                        cell = row.createCell(2);
                        cell.setCellValue(attribeGroup.getAttribes().get(j).getUnit());
                        cell.setCellStyle(style);
                        cell = row.createCell(3);
                        if (isComment) {
                            Comment comment = patriarch.createCellComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 4, 2, (short) 6, 5));
                            comment.setString(new HSSFRichTextString(attribeGroup.getAttribes().get(j).getAlias()));
                            row.getCell(3).setCellComment(comment);
                        }
                        cell.setCellValue(attribeGroup.getAttribes().get(j).getValue());
                        cell.setCellStyle(style);
                    }
                    flag = true;
                    if (flag) {
                        flag2 = true;
                    }
//                }
            }
        }
        if (attribeGroup.getChilds() != null) {
            for (int i = 0; i < attribeGroup.getChilds().size(); i++) {
                if (i == 0) {
                    ss = ss + s;
                    if (sign.equals("一") || sign.equals("二") || sign.equals("三") || sign.equals("四") || sign.equals("五") || sign.equals("六") || sign.equals("七")) {
                        sss = ss;
                    } else {
                        sss = sign + "." + ss;

                    }
                }else{
                    s++;
                    ss = "";
                    ss = ss + s;
                    if (sign.equals("一") || sign.equals("二") || sign.equals("三") || sign.equals("四") || sign.equals("五") || sign.equals("六") || sign.equals("七")) {
                        sss = ss;
                    } else {
                        sss = sign + "." + ss;

                    }
                }
                flag1=writeToExcel(style, sheet, row, cell, we, attribeGroup.getChilds().get(i),sss,false,isComment);
//                flag = true;
                if (flag1) {
                    flag2 = true;
                }else{
                    s--;
                }
            }
        }
        if (!(flag||flag2)) {
            we.getIndexMinus();
        }
        return flag2;
    }

    @Test
    public void testSaveField(){
        String s = "";
    }

}
