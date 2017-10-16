package com.hysw.qqsl.cloud.core.service;

import Jama.Matrix;
import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.service.TransFromService;
import org.junit.Test;
import org.osgeo.proj4j.*;
import org.springframework.beans.factory.annotation.Autowired;

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
        ProjCoordinate projCoordinate1 = transFromService.transFrom54GroundTo54Plane("108", doubles[0], doubles[1], doubles[2]);
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

}



