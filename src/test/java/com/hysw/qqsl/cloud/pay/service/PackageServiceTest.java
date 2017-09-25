package com.hysw.qqsl.cloud.pay.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.service.UserService;
import com.hysw.qqsl.cloud.pay.entity.PackageModel;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import com.hysw.qqsl.cloud.util.TradeUtil;
import net.sf.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PackageServiceTest extends BaseTest{
    @Autowired
    private PackageService packageService;
    @Autowired
    private UserService userService;

    @Test
    public void testCache(){
        packageService.putPackageModelInCache();
        List<PackageModel> packageModels = packageService.getPackageModelFromCache();
        Assert.assertTrue(packageModels != null);
    }

    @Test
    public void testGetPackageList(){
        JSONArray jsonArray = packageService.getPackageList();
        Assert.assertTrue(jsonArray.size()==1);
    }

    @Test
    public void testActivatePackage(){
        Trade trade = new Trade();
        trade.setBuyType(Trade.BuyType.FIRST);
        trade.setType(Trade.Type.PACKAGE);
        trade.setBaseType(Trade.BaseType.TEST);
        trade.setPayDate(new Date());
//        trade.setValidTime(1);
        trade.setInstanceId(TradeUtil.buildInstanceId());
        trade.setUser(userService.find(17l));
        packageService.activatePackage(trade);
        packageService.flush();
        Assert.assertTrue(packageService.findByInstanceId(trade.getInstanceId()) != null);
    }

    @Test
    public void testRenewPackage(){
        Trade trade = new Trade();
        trade.setBuyType(Trade.BuyType.FIRST);
        trade.setType(Trade.Type.PACKAGE);
        trade.setBaseType(Trade.BaseType.TEST);
        trade.setPayDate(new Date());
//        trade.setValidTime(1);
        trade.setInstanceId("799cec8b");
        trade.setUser(userService.find(17l));
        packageService.renewPackage(trade);
        packageService.flush();
        Package aPackage = packageService.findByInstanceId(trade.getInstanceId());
        long l=(aPackage.getExpireDate().getTime() - aPackage.getModifyDate().getTime())/3600/24/1000;
        Assert.assertTrue(l>500);
    }

    @Test
    public void testInitCurTrafficNum(){
        packageService.initCurTrafficNum();
        packageService.flush();
        List<Package> packages = packageService.findAll();
        Assert.assertTrue(packages!=null&&packages.get(0).getCurTrafficNum()==0);
    }

}
