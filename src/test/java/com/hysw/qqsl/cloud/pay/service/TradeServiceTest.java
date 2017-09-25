package com.hysw.qqsl.cloud.pay.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.StationService;
import com.hysw.qqsl.cloud.core.service.UserService;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import com.hysw.qqsl.cloud.pay.entity.data.Turnover;
import com.hysw.qqsl.cloud.pay.service.TradeService;
import com.hysw.qqsl.cloud.util.SettingUtils;
import com.hysw.qqsl.cloud.util.TradeUtil;
import net.sf.json.JSONObject;
import org.dom4j.DocumentException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class TradeServiceTest extends BaseTest {
    @Autowired
    private TradeService tradeService;
    @Autowired
    private UserService userService;
    @Autowired
    private PackageService packageService;
    @Autowired
    private TurnoverService turnoverService;
    @Autowired
    private StationService stationService;

    /**
     * 测试激活套餐服务
     */
    @Test
    public void testActivateServePackage(){
        Trade trade = new Trade();
        trade.setBuyType(Trade.BuyType.FIRST);
        trade.setType(Trade.Type.PACKAGE);
        trade.setBaseType(Trade.BaseType.TEST);
        trade.setPayDate(new Date());
//        trade.setValidTime(1);
        trade.setInstanceId(TradeUtil.buildInstanceId());
        trade.setUser(userService.find(17l));
        tradeService.activateServe(trade);
        tradeService.flush();
        Assert.assertTrue(packageService.findByInstanceId(trade.getInstanceId()) != null);
        Assert.assertTrue(packageService.findByInstanceId(trade.getInstanceId()).getType().toString().equalsIgnoreCase("test"));
        trade.setBuyType(Trade.BuyType.RENEW);
        trade.setType(Trade.Type.PACKAGE);
        trade.setBaseType(Trade.BaseType.TEST);
        trade.setPayDate(new Date());
//        trade.setValidTime(1);
        trade.setInstanceId(trade.getInstanceId());
        trade.setUser(userService.find(17l));
        tradeService.activateServe(trade);
        tradeService.flush();
        packageService.flush();
        Assert.assertTrue(packageService.findByInstanceId(trade.getInstanceId()) != null);
        Assert.assertTrue(packageService.findByInstanceId(trade.getInstanceId()).getType().toString().equalsIgnoreCase("test"));
        trade.setBuyType(Trade.BuyType.UPDATE);
        trade.setType(Trade.Type.PACKAGE);
        trade.setBaseType(Trade.BaseType.SUN);
        trade.setPayDate(new Date());
//        trade.setValidTime(1);
        trade.setInstanceId(trade.getInstanceId());
        trade.setUser(userService.find(17l));
        tradeService.activateServe(trade);
        tradeService.flush();
        packageService.flush();
        Assert.assertTrue(packageService.findByInstanceId(trade.getInstanceId()) != null);
        Assert.assertTrue(packageService.findByInstanceId(trade.getInstanceId()).getType().toString().equalsIgnoreCase("sun"));
    }

    /**
     * 测试激活测站服务
     */
    @Test
    public void testActivateServeStation(){
        Trade trade = new Trade();
        trade.setBuyType(Trade.BuyType.FIRST);
        trade.setType(Trade.Type.STATION);
        trade.setBaseType(Trade.BaseType.HYDROLOGIC_STATION);
        trade.setPayDate(new Date());
//        trade.setValidTime(1);
        trade.setInstanceId(TradeUtil.buildInstanceId());
        trade.setUser(userService.find(17l));
        tradeService.activateServe(trade);
        tradeService.flush();
        Assert.assertTrue(stationService.findByInstanceId(trade.getInstanceId()) != null);
        Assert.assertTrue(stationService.findByInstanceId(trade.getInstanceId()).getType().toString().equalsIgnoreCase("HYDROLOGIC_STATION"));
        trade.setBuyType(Trade.BuyType.RENEW);
        trade.setType(Trade.Type.STATION);
        trade.setBaseType(Trade.BaseType.HYDROLOGIC_STATION);
        trade.setPayDate(new Date());
//        trade.setValidTime(1);
        trade.setInstanceId(trade.getInstanceId());
        trade.setUser(userService.find(17l));
        tradeService.activateServe(trade);
        tradeService.flush();
        stationService.flush();
        Assert.assertTrue(stationService.findByInstanceId(trade.getInstanceId()) != null);
        Assert.assertTrue(stationService.findByInstanceId(trade.getInstanceId()).getType().toString().equalsIgnoreCase("HYDROLOGIC_STATION"));
    }

    /**
     * 测试套餐等级低于10级未通过企业认证
     */
    @Test
    public void testCreatePackageTrade() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "PACKAGE");
        jsonObject.put("packageType", "YOUTH");
        User user = userService.findByPhoneOrUserName("13028710937");
//        user.setCompanyStatus(CommonEnum.CertifyStatus.PASS);
        Message message = tradeService.createPackageTrade(jsonObject, user);
        Assert.assertTrue(message.getType()==Message.Type.OK);
    }

    /**
     * 测试套餐等级大于10级 未通过企业认证
     */
    @Test
    public void testCreatePackageTrade2() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "PACKAGE");
        jsonObject.put("packageType", "SUN");
        User user = userService.findByPhoneOrUserName("13028710937");
//        user.setCompanyStatus(CommonEnum.CertifyStatus.PASS);
        Message message = tradeService.createPackageTrade(jsonObject, user);
        Assert.assertTrue(message.getType()==Message.Type.UNKNOWN);
    }

    /**
     * 测试套餐等级大于10级通过企业认证
     */
    @Test
    public void testCreatePackageTrade3() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "PACKAGE");
        jsonObject.put("packageType", "SUN");
        User user = userService.findByPhoneOrUserName("13028710937");
        user.setCompanyStatus(CommonEnum.CertifyStatus.PASS);
        Message message = tradeService.createPackageTrade(jsonObject, user);
        Assert.assertTrue(message.getType()==Message.Type.OK);
    }

    /**
     * 测试购买test套餐
     */
    @Test
    public void testCreatePackageTrade4() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "PACKAGE");
        jsonObject.put("packageType", "TEST");
        User user = userService.findByPhoneOrUserName("13028710937");
//        user.setCompanyStatus(CommonEnum.CertifyStatus.PASS);
        Message message = tradeService.createPackageTrade(jsonObject, user);
        Assert.assertTrue(message.getType()==Message.Type.NO_ALLOW);
    }

    /**
     * 测试购买不存在类型套餐
     */
    @Test
    public void testCreatePackageTrade5() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "PACKAGE");
        jsonObject.put("packageType", "TEST1");
        User user = userService.findByPhoneOrUserName("13028710937");
        Message message = tradeService.createPackageTrade(jsonObject, user);
        Assert.assertTrue(message.getType()==Message.Type.FAIL);
    }

    /**
     * 测试激活套餐逻辑（由于线程改变数据库不能被回滚，
     * 故该方法只用于手动测试，测试完毕后由测试人员将数据还原）
     */
//    @Test
    public void testCreatePackageTrade6() throws InterruptedException {
        User user = userService.findByPhoneOrUserName("13028710937");
        Package aPackage = packageService.findByUser(user);
        CommonEnum.PackageType type = aPackage.getType();
        List<Trade> trades = tradeService.findByUser(user);
        for (Trade trade : trades) {
            tradeService.remove(trade);
        }
        tradeService.flush();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "PACKAGE");
        jsonObject.put("packageType", "SUN");
        user.setCompanyStatus(CommonEnum.CertifyStatus.PASS);
        Message message = tradeService.createPackageTrade(jsonObject, user);
        Assert.assertTrue(message.getType()==Message.Type.OK);
        Trade trade = (Trade) message.getData();
        trade.setPayDate(new Date());
        trade.setPayType(Trade.PayType.WX);
        trade.setStatus(Trade.Status.PAY);
        tradeService.save(trade);
        CountDownLatch countDownLatch = new CountDownLatch(1);
//                激活业务
        new Thread(){
            public void run(){
                turnoverService.writeTurnover(trade);
                tradeService.activateServe(trade);
                countDownLatch.countDown();
            }
        }.start();
        countDownLatch.await();
        packageService.flush();
        tradeService.flush();
        turnoverService.flush();
        aPackage = packageService.findByUser(user);
        Assert.assertTrue(aPackage.getType().toString().equals("SUN"));
    }

    /**
     * 测试测站创建订单
     */
    @Test
    public void testCreateStationTrade() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("stationType", "HYDROLOGIC_STATION");
        User user = userService.findByPhoneOrUserName("13028710937");
        Message message = tradeService.createStationTrade(jsonObject, user);
        Assert.assertTrue(message.getType()==Message.Type.OK);
    }

    /**
     * 测试购买不存在类型测站
     */
    @Test
    public void testCreateStationTrade1() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("stationType", "HYDROLOGIC_STATION1");
        User user = userService.findByPhoneOrUserName("13028710937");
        Message message = tradeService.createStationTrade(jsonObject, user);
        Assert.assertTrue(message.getType()==Message.Type.FAIL);
    }

    /**
     * 测试数据服务创建订单
     */
    @Test
    public void testCreateGoodsTrade() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("goodsType", "PANORAMA");
        jsonObject.put("remark", "test");
        jsonObject.put("goodsNum", "2");
        User user = userService.findByPhoneOrUserName("13028710937");
        Message message = tradeService.createGoodsTrade(jsonObject, user);
        Assert.assertTrue(message.getType()==Message.Type.OK);
    }

    /**
     * 测试购买不存在类型数据服务
     */
    @Test
    public void testCreateGoodsTrade2() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("goodsType", "PANORAMA1");
        jsonObject.put("remark", "test");
        jsonObject.put("goodsNum", "2");
        User user = userService.findByPhoneOrUserName("13028710937");
        Message message = tradeService.createGoodsTrade(jsonObject, user);
        Assert.assertTrue(message.getType()==Message.Type.FAIL);
    }

    /**
     * 测试购买数量不能解析
     */
    @Test
    public void testCreateGoodsTrade3() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("goodsType", "PANORAMA1");
        jsonObject.put("remark", "test");
        jsonObject.put("goodsNum", "");
        User user = userService.findByPhoneOrUserName("13028710937");
        Message message = tradeService.createGoodsTrade(jsonObject, user);
        Assert.assertTrue(message.getType()==Message.Type.FAIL);
        jsonObject = new JSONObject();
        jsonObject.put("goodsType", "PANORAMA1");
        jsonObject.put("remark", "test");
        jsonObject.put("goodsNum", "a");
        message = tradeService.createGoodsTrade(jsonObject, user);
        Assert.assertTrue(message.getType()==Message.Type.FAIL);
    }
}
