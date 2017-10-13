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
        param54[0][0] = 4380209.665;//x
        param54[0][1] = 174088.142;//y
        param54[0][2] = 1564.500;//z
        param54[1][0] = 4368684.639;
        param54[1][1] = 204362.488;
        param54[1][2] = 1473.300;
        param54[2][0] = 4413189.680;
        param54[2][1] = 294345.440;
        param54[2][2] = 1382.600;

        double[][] param84 = new double[3][3];
        param84[0][0] = 39.293729248;//纬度
        param84[0][1] = 107.124414629;//经度
        param84[0][2] = 1524.162;//高程
        param84[1][0] = 39.240359947;
        param84[1][1] = 107.340707641;
        param84[1][2] = 1433.161;
        param84[2][0] = 39.493944301;
        param84[2][1] = 108.355403200;
        param84[2][2] = 1346.888;

        double[][] param = new double[1][3];
        param[0][0] = 39.512664223;
        param[0][1] = 108.300870965;
        param[0][2] = 1243.493;
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



