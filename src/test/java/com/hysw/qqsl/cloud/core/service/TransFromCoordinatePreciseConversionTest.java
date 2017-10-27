package com.hysw.qqsl.cloud.core.service;

import Jama.Matrix;
import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.service.TransFromService;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.osgeo.proj4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2016/8/17.
 */
public class TransFromCoordinatePreciseConversionTest extends BaseTest {
    @Autowired
    private TransFromService transFromService;


    @Test
    public void testTransFromCoordinate(){
        double[][] param54 = new double[3][3];
        param54[0][0] = 3973264.495;//x
        param54[0][1] = 576999.863;//y
        param54[0][2] = 1805.985;//z
        param54[1][0] = 3973263.304;
        param54[1][1] = 576994.637;
        param54[1][2] = 1805.891;
        param54[2][0] = 3973260.166;
        param54[2][1] = 576987.273;
        param54[2][2] = 1805.861;

        double[][] param84 = new double[3][3];
        param84[0][0] = 35.8856820505556;//B
        param84[0][1] = 102.852747449722;//L
        param84[0][2] = 1805.979;//H
        param84[1][0] = 35.8856716627778;
        param84[1][1] = 102.852689645556;
        param84[1][2] = 1805.932;
        param84[2][0] = 35.8856440841667;
        param84[2][1] = 102.852607821111;
        param84[2][2] = 1805.851;

        double[][] param = new double[1][3];
        param[0][0] = 35.8856092102778;
        param[0][1] = 102.852845316111;
        param[0][2] = 1805.685;
        transfrom(param54,param84,param);
    }
    public void transfrom(double[][] param54,double[][] param84,double[][] param){
        Matrix R = transFromService.calculate7Param(param54, param84);

        ProjCoordinate projCoordinate = new ProjCoordinate();
        projCoordinate.x = param[0][0];
        projCoordinate.y = param[0][1];
        projCoordinate.z = param[0][2];
        double[] wgs84s = transFromService.transFromRectangularSpaceCoordinate(transFromService.selcetTransFromParam("WGS84"), projCoordinate);

        double X54 = R.get(0, 0) + (1 + R.get(3, 0)) * wgs84s[0] - R.get(5, 0) * wgs84s[2] + R.get(6, 0) * wgs84s[1];
        double Y54 = R.get(1, 0) + (1 + R.get(3, 0)) * wgs84s[1] + R.get(4, 0) * wgs84s[2] - R.get(6, 0) * wgs84s[0];
        double Z54 = R.get(2, 0) + (1 + R.get(3, 0)) * wgs84s[2] - R.get(4, 0) * wgs84s[1] + R.get(5, 0) * wgs84s[0];

        double[] doubles = transFromService.transFromgeodeticCoordinate(transFromService.selcetTransFromParam("Beijing54"),X54, Y54, Z54);
        ProjCoordinate projCoordinate1 = transFromService.transFrom54GroundTo54Plane("102", doubles[0], doubles[1], doubles[2]);
        System.out.println(projCoordinate1.x);
        System.out.println(projCoordinate1.y);
        System.out.println(projCoordinate1.z);
    }

    @Test
    //平面 plane  大地ground
    //54平面转84大地
    public void testUsedProj4jTransfrom(){
        ProjCoordinate projCoordinate = transFromService.usedProj4jTransfrom(transFromService.checkCode54("96"), 592259.8555349195, 3653843.879175393, "plane");
        System.out.println(projCoordinate.x);
}
    @Test
    //84大地转54平面
    public void testUsedProj4jTransfrom1(){
        ProjCoordinate projCoordinate = transFromService.usedProj4jTransfrom(transFromService.checkCode54("96"), 96.98725386712154, 33.005391984039214, "ground");
        System.out.println(projCoordinate.x);
    }

    @Test
    public void testaaaaa(){
        ProjCoordinate projCoordinate = transFromService.transFrom54PlaneTo54Ground("114",  274820.104, 4372952.906,1381.393);
        System.out.println(projCoordinate.x+"-->"+projCoordinate.y+"-->"+projCoordinate.z);
    }

    @Test
    public void readExcel() throws IOException {
        File file = new ClassPathResource("/coordinateDautm.xlsx").getFile();
        FileInputStream is = new FileInputStream(file);
        String str = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        Workbook wb = null;
        if (str.trim().toLowerCase().equals("xls")) {
            wb = new HSSFWorkbook(is);
        } else if (str.trim().toLowerCase().equals("xlsx")) {
            wb = new XSSFWorkbook(is);
        }
        List<double[]> blh = new ArrayList<>();
        List<double[]> xyh = new ArrayList<>();
        for (int numSheet = 0; numSheet < wb.getNumberOfSheets(); numSheet++) {
            Sheet sheet = wb.getSheetAt(numSheet);
            if (sheet == null) {
                continue;
            }
            for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row != null) {
                    String B = null, L = null, H = null, y = null, x = null, h = null;
                    if (row.getCell(0) != null) {
                        row.getCell(0).setCellType(Cell.CELL_TYPE_STRING);
                        B = row.getCell(0).getStringCellValue();
                    }
                    if (row.getCell(1) != null) {
                        row.getCell(1).setCellType(Cell.CELL_TYPE_STRING);
                        L = row.getCell(1).getStringCellValue();
                    }
                    if (row.getCell(2) != null) {
                        row.getCell(2).setCellType(Cell.CELL_TYPE_STRING);
                        H = row.getCell(2).getStringCellValue();
                    }
                    if (row.getCell(3) != null) {
                        row.getCell(3).setCellType(Cell.CELL_TYPE_STRING);
                        y = row.getCell(3).getStringCellValue();
                    }
                    if (row.getCell(4) != null) {
                        row.getCell(4).setCellType(Cell.CELL_TYPE_STRING);
                        x = row.getCell(4).getStringCellValue();
                    }
                    if (row.getCell(5) != null) {
                        row.getCell(5).setCellType(Cell.CELL_TYPE_STRING);
                        h = row.getCell(5).getStringCellValue();
                    }
                    if (B.equals("B")) {
                        continue;
                    }
                    double[] blh1 = {Double.valueOf(B), Double.valueOf(L), Double.valueOf(H)};
                    double[] xyh1 = {Double.valueOf(x), Double.valueOf(y), Double.valueOf(h)};
                    blh.add(blh1);
                    xyh.add(xyh1);
                }
            }

        }
        int[][] jiesuandian = {/*{1, 2, 3}, *//*{1,5,8},*/ /*{1,5,10},*/ /*{1,7,10},*//*{1,6,9},*/{3,6,9}};
        for (int[] ints : jiesuandian) {
            double[][] param54 = {{xyh.get(ints[0]-1)[0], xyh.get(ints[0]-1)[1], xyh.get(ints[0]-1)[2]}, {xyh.get(ints[1]-1)[0], xyh.get(ints[1]-1)[1], xyh.get(ints[1]-1)[2]}, {xyh.get(ints[2]-1)[0], xyh.get(ints[2]-1)[1], xyh.get(ints[2]-1)[2]}};
            double[][] param84 = {{blh.get(ints[0]-1)[0], blh.get(ints[0]-1)[1], blh.get(ints[0]-1)[2]}, {blh.get(ints[1]-1)[0], blh.get(ints[1]-1)[1], blh.get(ints[1]-1)[2]}, {blh.get(ints[2]-1)[0], blh.get(ints[2]-1)[1], blh.get(ints[2]-1)[2]}};
            Matrix R = transFromService.calculate7Param(param54, param84);

            for (int i = 0; i <blh.size() ; i++) {
                ProjCoordinate projCoordinate1 = new ProjCoordinate();
                projCoordinate1.x = blh.get(i)[0];
                projCoordinate1.y = blh.get(i)[1];
                projCoordinate1.z = blh.get(i)[2];

                double[] wgs84s = transFromService.transFromRectangularSpaceCoordinate(transFromService.selcetTransFromParam("WGS84"), projCoordinate1);
                double X54 = R.get(0, 0) + (1 + R.get(3, 0)) * wgs84s[0] - R.get(5, 0) * wgs84s[2] + R.get(6, 0) * wgs84s[1];
                double Y54 = R.get(1, 0) + (1 + R.get(3, 0)) * wgs84s[1] + R.get(4, 0) * wgs84s[2] - R.get(6, 0) * wgs84s[0];
                double Z54 = R.get(2, 0) + (1 + R.get(3, 0)) * wgs84s[2] - R.get(4, 0) * wgs84s[1] + R.get(5, 0) * wgs84s[0];

                double[] doubles = transFromService.transFromgeodeticCoordinate(transFromService.selcetTransFromParam("Beijing54"),X54, Y54, Z54);
                ProjCoordinate projCoordinate2 = transFromService.transFrom54GroundTo54Plane("102", doubles[0], doubles[1], doubles[2]);
                System.out.println(projCoordinate2.x);
                System.out.println(projCoordinate2.y);
                System.out.println(projCoordinate2.z);
            }
            break;
        }

    }

    @Test
    public void matrix0(){

    }

    @Test
    public void testTransFromCoordinate1(){
        double[][] param54 = new double[2][2];
        param54[0][0] = 3973264.495;//x
        param54[0][1] = 576999.863;//y
//        param54[0][2] = 1805.985;//z
        param54[1][0] = 3973263.304;
        param54[1][1] = 576994.637;
//        param54[1][2] = 1805.891;

        double[][] param84 = new double[2][2];
        param84[0][0] = 35.8856820505556;//B
        param84[0][1] = 102.852747449722;//L
//        param84[0][2] = 1805.979;//H
        param84[1][0] = 35.8856716627778;
        param84[1][1] = 102.852689645556;
//        param84[1][2] = 1805.932;

        double[][] param = new double[1][2];
        param[0][0] = 35.8856092102778;
        param[0][1] = 102.852845316111;
//        param[0][2] = 1805.685;
        transfrom1(param54,param84,param);
    }
    public void transfrom1(double[][] param54,double[][] param84,double[][] param){
        Matrix R = transFromService.calculate4Param(param54, param84);
        double[] fg84 = transFromService.transFromGroundToPlane("102", param[0][0], param[0][1]);
        double [][] C ={
                {1,0,-fg84[1],fg84[0]},
                {0,1,fg84[0],fg84[1]},
        };
        double [][] b={
                {fg84[0]},
                {fg84[1]},
        };
        Matrix A = new Matrix(C);
        Matrix B = new Matrix(b);
        Matrix plus = A.times(R).plus(B);
        System.out.println(plus.get(0,0)+":"+plus.get(1,0));
    }

}



