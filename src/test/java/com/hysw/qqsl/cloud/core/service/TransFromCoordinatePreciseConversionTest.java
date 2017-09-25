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
        param54[0][0] = 3653829.756;
        param54[0][1] = 592291.313;
        param54[0][2] = 3651.383;
        param54[1][0] = 3653828.581;
        param54[1][1] = 592306.114;
        param54[1][2] = 3651.207;
        param54[2][0] = 3653847.820;
        param54[2][1] = 592291.096;
        param54[2][2] = 3651.499;

        double[][] param84 = new double[3][3];
        param84[0][0] = 33+0.0/60+18.943204/3600;
        param84[0][1] = 96+59.0/60+15.320476/3600;
        param84[0][2] = 3651.383;
        param84[1][0] = 33+0.0/60+18.900561/3600;
        param84[1][1] = 96+59.0/60+15.890161/3600;
        param84[1][2] = 3651.207;
        param84[2][0] = 33+0.0/60+19.529528/3600;
        param84[2][1] = 96+59.0/60+15.318675/3600;
        param84[2][2] = 3651.499;

        double[][] param = new double[1][3];
        param[0][0] = 33+0.0/60+19.411164/3600;
        param[0][1] = 96+59.0/60+14.113589/3600;
        param[0][2] = 3652.076;
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
        ProjCoordinate projCoordinate1 = transFromService.transFrom54GroundTo54Plane("96", doubles[0], doubles[1], doubles[2]);
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
        ProjCoordinate projCoordinate = transFromService.transFrom54PlaneTo54Ground("96",  659078.705,3993800.145, 2991.776);
        System.out.println(projCoordinate.x);
    }

}



