package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.BaseTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.osgeo.proj4j.ProjCoordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TransactionConfiguration;

@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
public class TransFromServiceTest extends BaseTest {
	Log logger = LogFactory.getLog(getClass());
	@Autowired
	private TransFromService transFromService;

	/**
	 * 测试平面坐标转大地坐标84
	 */
	@Test
	public void testXYZToBLH84() {
		double x = 504329.9315;
		double y = 3945101.323215;
		for (int i = 75; i < 136; i+=3) {
			String code = transFromService.checkCode84(String.valueOf(i));
			ProjCoordinate projCoordinate = transFromService.XYZToBLH(code, x, y);
			Assert.assertTrue(projCoordinate!=null);
		}
	}
	
	/**
	 * 测试大地坐标转平面坐标84
	 */
	@Test
	public void testBLHToXYZ84(){
		double lon=102.5;
		double lat=35.3;
		for (int j = 75; j < 136; j+=3) {
			String code= transFromService.checkCode84(String.valueOf(j));
			ProjCoordinate projCoordinate=transFromService.BLHToXYZ(code, lon, lat);
			Assert.assertTrue(projCoordinate!=null);
		}
	}
	/**
	 * 测试平面坐标转大地坐标54
	 */
	@Test
	public void testXYZToBLH54() {
		double x = 504329.9315;
		double y = 3945101.323215;
		for (int i = 75; i < 136; i+=3) {
			String code = transFromService.checkCode54(String.valueOf(i));
			ProjCoordinate projCoordinate = transFromService.XYZToBLH(code, x, y);
			Assert.assertTrue(projCoordinate!=null);
		}
	}
	
	/**
	 * 测试大地坐标转平面坐标54
	 */
	@Test
	public void testBLHToXYZ54(){
		double lon=102.5;
		double lat=35.3;
		for (int j = 75; j < 136; j+=3) {
			String code= transFromService.checkCode54(String.valueOf(j));
			ProjCoordinate projCoordinate=transFromService.BLHToXYZ(code, lon, lat);
			Assert.assertTrue(projCoordinate!=null);
		}
	}
	/**
	 * 测试平面坐标转大地坐标80
	 */
	@Test
	public void testXYZToBLH80() {
		double x = 504329.9315;
		double y = 3945101.323215;
		for (int i = 75; i < 136; i+=3) {
			String code = transFromService.checkCode80(String.valueOf(i));
			ProjCoordinate projCoordinate = transFromService.XYZToBLH(code, x, y);
			Assert.assertTrue(projCoordinate!=null);
		}
	}
	
	/**
	 * 测试大地坐标转平面坐标80
	 */
	@Test
	public void testBLHToXYZ80(){
		double lon=102.5;
		double lat=35.3;
		for (int j = 75; j < 136; j+=3) {
			String code= transFromService.checkCode80(String.valueOf(j));
			ProjCoordinate projCoordinate=transFromService.BLHToXYZ(code, lon, lat);
			Assert.assertTrue(projCoordinate!=null);
		}
	}
}
