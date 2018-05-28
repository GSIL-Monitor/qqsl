package com.hysw.qqsl.cloud.pay.controller;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.annotation.util.*;
import com.hysw.qqsl.cloud.core.controller.CommonController;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.StationModel;
import com.hysw.qqsl.cloud.core.entity.data.Station;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.AuthentService;
import com.hysw.qqsl.cloud.core.service.MessageService;
import com.hysw.qqsl.cloud.core.service.StationService;
import com.hysw.qqsl.cloud.pay.entity.GoodsModel;
import com.hysw.qqsl.cloud.pay.entity.PackageModel;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import com.hysw.qqsl.cloud.pay.service.TradeService;
import com.hysw.qqsl.cloud.pay.service.wxPay.WXPayService;
import net.sf.json.JSONObject;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单controller
 */
@Controller
@RequestMapping("/trade")
public class TradeController {
    @Autowired
    private AuthentService authentService;
    @Autowired
    private TradeService tradeService;
    @Autowired
    private WXPayService wxPayService;
    @Autowired
    private PackageService packageService;
    @Autowired
    private StationService stationService;

    /**
     * 生成订单--首购--套餐
     *
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @IsHaveTradeNoPay
    @RequestMapping(value = "/createPackage", method = RequestMethod.POST)
    public @ResponseBody
    Message createPackage(@RequestBody Map<String, Object> objectMap) {
        User user = authentService.getUserFromSubject();
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object packageType = objectMap.get("packageType");
        if (packageType == null||packageType.toString().equals("TEST")) {
            return MessageService.message(Message.Type.FAIL);
        }
        PackageModel packageModel = tradeService.getPackageModel(packageType.toString());
        if (packageModel == null) {
            return MessageService.message(Message.Type.FAIL);
        }
//        空间大小以及子账户数是否满足套餐限制,套餐是否已过期
        if (packageService.isRequirementPackage(user.getId(), packageModel)) {
            return MessageService.message(Message.Type.PACKAGE_LIMIT);
        }
//        未通过企业认证的用户不能购买套餐等级大于10的套餐
        if (packageModel.getLevel() > CommonAttributes.PROJECTLIMIT && !(user.getCompanyStatus() == CommonEnum.CertifyStatus.PASS || user.getCompanyStatus() == CommonEnum.CertifyStatus.EXPIRING)) {
            return MessageService.message(Message.Type.CERTIFY_NO_COMPANY);
        }
        return MessageService.message(Message.Type.OK, tradeService.createPackageTrade(packageModel, packageType, user));
    }

    /**
     * 生成订单--首购--测站
     *
     * @param objectMap
     * @return
     */
    @IsHaveTradeNoPay
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/createStation", method = RequestMethod.POST)
    public @ResponseBody
    Message createStation(@RequestBody Map<String, Object> objectMap) {
        User user = authentService.getUserFromSubject();
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object stationType = objectMap.get("stationType");
        if (stationType == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        StationModel stationModel = tradeService.getStationModel(stationType.toString());
        if (stationModel == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        return MessageService.message(Message.Type.OK, tradeService.createStationTrade(stationModel, stationType, user));
    }

    /**
     * 生成订单--首购--数据服务
     *
     * @param objectMap
     * @return
     */
    @IsHaveTradeNoPay
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/createGoods", method = RequestMethod.POST)
    public @ResponseBody
    Message createGoods(@RequestBody Map<String, Object> objectMap) {
        User user = authentService.getUserFromSubject();
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object goodsType = objectMap.get("goodsType");
        Object remark = objectMap.get("remark");
        Object goodsNum = objectMap.get("goodsNum");
        if (goodsType == null || remark == null || goodsNum == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        GoodsModel goodsModel = tradeService.getGoodsModel(goodsType.toString());
        if (goodsModel == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        try {
            Integer.valueOf(goodsNum.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return MessageService.message(Message.Type.FAIL);
        }
        return MessageService.message(Message.Type.OK, tradeService.createGoodsTrade(goodsModel, goodsNum, goodsType,remark, user));
    }

    /**
     * 套餐续费
     *
     * @param objectMap
     * @return
     */
    @PackageIsExpire
    @IsHaveTradeNoPay
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/renewPackage", method = RequestMethod.POST)
    public @ResponseBody
    Message renewPackage(@RequestBody Map<String, Object> objectMap) {
        User user = authentService.getUserFromSubject();
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object instanceId = objectMap.get("instanceId");
        if (instanceId == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        com.hysw.qqsl.cloud.pay.entity.data.Package aPackage = packageService.findByInstanceId(instanceId.toString());
        if (aPackage == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (!aPackage.getUser().getId().equals(user.getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
//        套餐为测试版不可续费
        if (aPackage.getType() == CommonEnum.PackageType.TEST) {
            return MessageService.message(Message.Type.PACKAGE_NOALLOW_RENEW);
        }
        PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
        if (packageModel == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        return MessageService.message(Message.Type.OK, tradeService.renewPackageTrade(aPackage, packageModel, user));
    }

    /**
     * 测站续费
     *
     * @param objectMap
     * @return
     */
    @StationIsExpire("instanceId")
    @IsHaveTradeNoPay
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/renewStation", method = RequestMethod.POST)
    public @ResponseBody Message renewStation(@RequestBody Map<String, Object> objectMap) {
        User user = authentService.getUserFromSubject();
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object instanceId = objectMap.get("instanceId");
        if (instanceId == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Station station=stationService.findByInstanceId(instanceId.toString());
        if (station == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (!station.getUser().getId().equals(user.getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        StationModel stationModel = tradeService.getStationModel(station.getType().toString());
        if (stationModel == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        return MessageService.message(Message.Type.OK, tradeService.renewStationTrade(station, stationModel, user));
    }

    /**
     * 套餐升级
     *
     * @param objectMap
     * @return
     */
    @PackageIsExpire
    @IsHaveTradeNoPay
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/updatePackage", method = RequestMethod.POST)
    public @ResponseBody
    Message updatePackage(@RequestBody Map<String, Object> objectMap) {
        User user = authentService.getUserFromSubject();
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object instanceId = objectMap.get("instanceId");
        Object packageType = objectMap.get("packageType");
        message = getIndividualTemplates(instanceId, packageType);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        com.hysw.qqsl.cloud.pay.entity.data.Package aPackage=(com.hysw.qqsl.cloud.pay.entity.data.Package) (map.get("aPackage"));
        if (!aPackage.getUser().getId().equals(user.getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        return MessageService.message(Message.Type.OK, tradeService.updatePackageTrade(map, aPackage, packageType, user));
    }

    /**
     * 获取订单列表(不包含已删除订单)
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/lists", method = RequestMethod.GET)
    public @ResponseBody Message getTradeList() {
        User user = authentService.getUserFromSubject();
        List<Trade> trades = tradeService.findByUser(user);
        return MessageService.message(Message.Type.OK, tradeService.tradesToJson(trades));
    }

    /**
     * 获取订单详情(用户)
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/trade/{outTradeNo}", method = RequestMethod.GET)
    public @ResponseBody Message getTrade(@PathVariable("outTradeNo") String outTradeNo) {
        if (outTradeNo == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Trade trade = tradeService.findByOutTradeNo(outTradeNo);
        if (trade == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        User user = authentService.getUserFromSubject();
        if (!trade.getUser().getId().equals(user.getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        return MessageService.message(Message.Type.OK,tradeService.tradeToJson(trade));
    }

    /**
     * 获取订单详情(管理员)
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/trade/{outTradeNo}", method = RequestMethod.GET)
    public @ResponseBody Message getTradeAdmin(@PathVariable("outTradeNo") String outTradeNo) {
        if (outTradeNo == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Trade trade = tradeService.findByOutTradeNo(outTradeNo);
        if (trade == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        return MessageService.message(Message.Type.OK,tradeService.tradeToJson(trade));
    }

    /**
     * 删除订单
     * @param outTradeNo
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/remove/{outTradeNo}", method = RequestMethod.POST)
    public @ResponseBody Message detele(@PathVariable("outTradeNo") String outTradeNo) {
        if (outTradeNo == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Trade trade = tradeService.findByOutTradeNo(outTradeNo);
        if (trade == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        User user = authentService.getUserFromSubject();
        if (!trade.getUser().getId().equals(user.getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        if (trade.getStatus() == Trade.Status.NOPAY) {
            return MessageService.message(Message.Type.TRADE_NOPAY);
        }
        trade.setDeleteStatus(true);
        tradeService.save(trade);
        return MessageService.message(Message.Type.OK);
    }

//    /**
//     * 计算套餐差价
//     * @param instanceId
//     * @param packageType
//     * @return
//     */
//    @RequestMapping(value = "/getPackageDiff", method = RequestMethod.GET)
//    public @ResponseBody Message getPackageDiff(@RequestParam("instanceId") Object instanceId,@RequestParam("packageType") Object packageType) {
//        return tradeService.getPackageDiff(instanceId,packageType);
//    }

    /**
     * 关闭订单
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:identify","user:company"}, logical = Logical.OR)
    @RequestMapping(value = "/close", method = RequestMethod.POST)
    public @ResponseBody Message close(@RequestBody Map<String, Object> objectMap) {
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Object outTradeNo = objectMap.get("outTradeNo");
        if (outTradeNo == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Trade trade = tradeService.findByOutTradeNo(outTradeNo.toString());
        if (trade == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        User user = authentService.getUserFromSubject();
        if (!trade.getUser().getId().equals(user.getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        if (trade.getStatus() != Trade.Status.NOPAY) {
            return MessageService.message(Message.Type.TRADE_PAYED);
        }
        trade.setStatus(Trade.Status.CLOSE);
        tradeService.save(trade);
        wxPayService.orderClose(trade);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 获取各个类模板
     * @param instanceId
     * @param packageType
     * @return
     */
    private Message getIndividualTemplates(Object instanceId, Object packageType) {
        if (instanceId == null || packageType == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        com.hysw.qqsl.cloud.pay.entity.data.Package aPackage = packageService.findByInstanceId(instanceId.toString());
        if (aPackage == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
//        获取原始套餐模板
        PackageModel oldPackageModel = tradeService.getPackageModel(aPackage.getType().toString());
        if (oldPackageModel == null) {
            return MessageService.message(Message.Type.FAIL);
        }
//        获取新的套餐模板
        PackageModel newPackageModel = tradeService.getPackageModel(packageType.toString());
        if (newPackageModel == null) {
            return MessageService.message(Message.Type.FAIL);
        }
//        新套餐等级不能小于原套餐等级
        if (newPackageModel.getLevel() <= oldPackageModel.getLevel()) {
            return MessageService.message(Message.Type.PACKAGE_NOALLOW_UPDATE);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("aPackage", aPackage);
        map.put("oldPackageModel", oldPackageModel);
        map.put("newPackageModel", newPackageModel);
        return MessageService.message(Message.Type.OK, map);
    }

    /**
     * 获取套餐差价
     * @param instanceId
     * @param packageType
     * @return
     */
    public Message getPackageDiff(Object instanceId, Object packageType) {
        Message message = getIndividualTemplates(instanceId, packageType);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        double diff=tradeService.diffPrice((com.hysw.qqsl.cloud.pay.entity.data.Package) (map.get("aPackage")),(PackageModel)(map.get("newPackageModel")),(PackageModel)(map.get("oldPackageModel")));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("diff", diff);
        return MessageService.message(Message.Type.OK, jsonObject);
    }

//    ?套餐设定等级，升级套餐时验证套餐等级，降级购买时，验证是否符合降级要求


}
