package com.hysw.qqsl.cloud.pay.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.StationModel;
import com.hysw.qqsl.cloud.core.entity.data.Station;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.BaseService;
import com.hysw.qqsl.cloud.core.service.StationService;
import com.hysw.qqsl.cloud.pay.dao.TradeDao;
import com.hysw.qqsl.cloud.pay.entity.GoodsModel;
import com.hysw.qqsl.cloud.pay.entity.PackageModel;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import com.hysw.qqsl.cloud.pay.service.wxPay.WXPayConfigImpl;
import com.hysw.qqsl.cloud.pay.service.wxPay.WXPayUtil;
import com.hysw.qqsl.cloud.util.SettingUtils;
import com.hysw.qqsl.cloud.util.TradeUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.util.*;

@Service("tradeService")
public class TradeService extends BaseService<Trade, Long> {
    @Autowired
    private TradeDao tradeDao;
    @Autowired
    private PackageService packageService;
    @Autowired
    private StationService stationService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private WXPayConfigImpl wxPayConfig;

    @Autowired
    public void setBaseDao(TradeDao tradeDao) {
        super.setBaseDao(tradeDao);
    }

    public Trade findByOutTradeNo(String outTradeNo){
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("outTradeNo", outTradeNo));
        List<Trade> trades = tradeDao.findList(0, null, filters);
        if (trades != null && trades.size() == 1) {
            return trades.get(0);
        }
        return null;
    }


    /**
     * 激活服务
     * @param trade
     */
    public void activateServe(Trade trade) {
//        首次购买业务处理
        if (trade.getBuyType() == Trade.BuyType.BUY) {
            if (trade.getType() == Trade.Type.PACKAGE) {
                packageService.activatePackage(trade);
            } else if (trade.getType() == Trade.Type.STATION) {
                stationService.activateStation(trade);
            }
//        续费业务处理
        } else if (trade.getBuyType() == Trade.BuyType.RENEW) {
            if (trade.getType() == Trade.Type.PACKAGE) {
                packageService.renewPackage(trade);
            } else if (trade.getType() == Trade.Type.STATION) {
                stationService.renewStation(trade);
            }
        } else if (trade.getBuyType() == Trade.BuyType.UPGRADE) {
            if (trade.getType() == Trade.Type.PACKAGE) {
                packageService.updatePackage(trade);
            }
        }
    }

    /**
     * 对退款通知进行解密
     * @param info
     * @return
     */
    public Map<String, String> decryptRefundTrade(String info) {
        Base64 base64 = new Base64();
        byte[] decode = base64.decode(info);
        String hex = DigestUtils.md5Hex(wxPayConfig.getKey());
        SecretKey secretKey = new SecretKeySpec(hex.getBytes(), "AES");//恢复密钥
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] cipherByte = cipher.doFinal(decode);
            return WXPayUtil.xmlToMap(new String(cipherByte));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建套餐订单
     * @param objectMap
     * @param user
     * @return
     */
    public Message createPackageTrade(Map<String, Object> objectMap, User user) {
        Object packageType = objectMap.get("packageType");
        if (packageType == null||packageType.toString().equals("TEST")) {
            return new Message(Message.Type.NO_ALLOW);
        }
        PackageModel packageModel = getPackageModel(packageType.toString());
        if (packageModel == null) {
            return new Message(Message.Type.FAIL);
        }
//        空间大小以及子账户数是否满足套餐限制,套餐是否已过期
        if (packageService.isRequirementPackage(user.getId(), packageModel)) {
            return new Message(Message.Type.UNKNOWN);
        }
//        未通过企业认证的用户不能购买套餐等级大于10的套餐
        if (packageModel.getLevel() > CommonAttributes.PROJECTLIMIT && !(user.getCompanyStatus() == CommonEnum.CertifyStatus.PASS || user.getCompanyStatus() == CommonEnum.CertifyStatus.EXPIRING)) {
            return new Message(Message.Type.NO_CERTIFY);
        }
        Trade trade = new Trade();
        trade.setOutTradeNo(TradeUtil.buildOutTradeNo());
        trade.setPrice(packageModel.getPrice());
        trade.setUser(user);
        trade.setStatus(Trade.Status.NOPAY);
        trade.setBuyType(Trade.BuyType.BUY);
        trade.setBaseType(Trade.BaseType.valueOf(packageType.toString()));
        trade.setType(Trade.Type.PACKAGE);
        trade.setInstanceId(TradeUtil.buildInstanceId());
        JSONObject remarkJson = new JSONObject();
        Date expireDate = getExpireDate(trade.getCreateDate(),"package");
        remarkJson.put("expireDate",expireDate.getTime());
        trade.setRemark(remarkJson.toString());
//        trade.setValidTime(1);
        save(trade);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("trade", tradeToJson(trade));
        return new Message(Message.Type.OK,jsonObject);
    }

    /**
     * 获取套餐模板
     * @return
     */
    public PackageModel getPackageModel(String packageType){
        List<PackageModel> packageModels = (List<PackageModel>) SettingUtils.objectCopy(packageService.getPackageModelFromCache());
        for (PackageModel packageModel1 : packageModels) {
            if (packageModel1.getType().toString().equals(packageType)) {
                return (PackageModel) SettingUtils.objectCopy(packageModel1);
            }
        }
        return null;
    }

    /**
     * 创建测站订单
     * @param objectMap
     * @param user
     * @return
     */
    public Message createStationTrade(Map<String, Object> objectMap, User user) {
        Object stationType = objectMap.get("stationType");
        if (stationType == null) {
            return new Message(Message.Type.FAIL);
        }
        StationModel stationModel = getStationModel(stationType.toString());
        if (stationModel == null) {
            return new Message(Message.Type.FAIL);
        }
        Trade trade = new Trade();
        trade.setOutTradeNo(TradeUtil.buildOutTradeNo());
        trade.setPrice(stationModel.getPrice());
        trade.setUser(user);
        trade.setStatus(Trade.Status.NOPAY);
        trade.setBuyType(Trade.BuyType.BUY);
        trade.setInstanceId(TradeUtil.buildInstanceId());
        trade.setBaseType(Trade.BaseType.valueOf(stationType.toString()));
        trade.setType(Trade.Type.STATION);
        JSONObject remarkJson = new JSONObject();
        Date expireDate = getExpireDate(trade.getCreateDate(),"station");
        remarkJson.put("expireDate",expireDate.getTime());
        trade.setRemark(remarkJson.toString());
//        trade.setValidTime(1);
        save(trade);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("trade", tradeToJson(trade));
        return new Message(Message.Type.OK,jsonObject);
    }

    /**
     * 获取测站模板
     * @param stationType
     * @return
     */
    private StationModel getStationModel(String stationType) {
        List<StationModel> stationModels = (List<StationModel>) SettingUtils.objectCopy(stationService.getStationModelFromCache());
        for (StationModel stationModel1 : stationModels) {
            if (stationModel1.getType().toString().equals(stationType)) {
                return (StationModel) SettingUtils.objectCopy(stationModel1);
            }
        }
        return null;
    }

    /**
     * 创建数据服务订单
     * @param objectMap
     * @param user
     * @return
     */
    public Message createGoodsTrade(Map<String, Object> objectMap, User user) {
        Object goodsType = objectMap.get("goodsType");
        Object remark = objectMap.get("remark");
        Object goodsNum = objectMap.get("goodsNum");
        if (goodsType == null || remark == null || goodsNum == null) {
            return new Message(Message.Type.FAIL);
        }
        GoodsModel goodsModel = getGoodsModel(goodsType.toString());
        if (goodsModel == null) {
            return new Message(Message.Type.FAIL);
        }
        Trade trade = new Trade();
        trade.setOutTradeNo(TradeUtil.buildOutTradeNo());
        trade.setType(Trade.Type.GOODS);
        try {
            trade.setGoodsNum(Integer.valueOf(goodsNum.toString()));
        } catch (Exception e) {
            return new Message(Message.Type.FAIL);
        }
        trade.setPrice(goodsModel.getPrice()*trade.getGoodsNum());
        trade.setUser(user);
        trade.setStatus(Trade.Status.NOPAY);
        trade.setBuyType(Trade.BuyType.BUY);
        trade.setBaseType(Trade.BaseType.valueOf(goodsType.toString()));
        trade.setInstanceId(TradeUtil.buildInstanceId());
        trade.setRemark(remark.toString());
        save(trade);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("trade", tradeToJson(trade));
        return new Message(Message.Type.OK,jsonObject);
    }

    /**
     * 获取数据服务模板
     * @param goodsType
     * @return
     */
    private GoodsModel getGoodsModel(String goodsType) {
        List<GoodsModel> goodsModels = (List<GoodsModel>) SettingUtils.objectCopy(goodsService.getGoodsModelFromCache());
        for (GoodsModel goodsModel1 : goodsModels) {
            if (goodsModel1.getType().toString().equals(goodsType)) {
                return (GoodsModel) SettingUtils.objectCopy(goodsModel1);
            }
        }
        return null;
    }

    /**
     * 套餐续费
     * @param instanceId
     * @param user
     * @return
     */
    public Message renewPackageTrade(String instanceId, User user) {
        Package aPackage = packageService.findByInstanceId(instanceId);
        if (aPackage == null) {
            return new Message(Message.Type.FAIL);
        }
        if (!aPackage.getUser().getId().equals(user.getId())) {
            return new Message(Message.Type.FAIL);
        }
//        套餐为测试版不可续费
        if (aPackage.getType() == CommonEnum.PackageType.TEST) {
            return new Message(Message.Type.NO_ALLOW);
        }
        PackageModel packageModel = getPackageModel(aPackage.getType().toString());
        if (packageModel == null) {
            return new Message(Message.Type.FAIL);
        }
        Trade trade = new Trade();
        trade.setStatus(Trade.Status.NOPAY);
        trade.setBaseType(Trade.BaseType.valueOf(aPackage.getType().toString()));
        trade.setType(Trade.Type.PACKAGE);
        trade.setInstanceId(aPackage.getInstanceId());
        trade.setBuyType(Trade.BuyType.RENEW);
        trade.setUser(user);
        trade.setPrice(packageModel.getPrice());
        trade.setOutTradeNo(TradeUtil.buildOutTradeNo());
        Date expireDate = getExpireDate(trade.getCreateDate(),"package");
        JSONObject remarkJson = new JSONObject();
        remarkJson.put("expireDate",expireDate.getTime());
        trade.setRemark(remarkJson.toString());
//        trade.setValidTime(1);
        save(trade);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("trade", tradeToJson(trade));
        return new Message(Message.Type.OK,jsonObject);
    }

    /**
     * 测站续费
     * @param instanceId
     * @param user
     * @return
     */
    public Message renewStationTrade(String instanceId, User user) {
        Station station=stationService.findByInstanceId(instanceId);
        if (station == null) {
            return new Message(Message.Type.FAIL);
        }
        if (!station.getUser().getId().equals(user.getId())) {
            return new Message(Message.Type.FAIL);
        }
        StationModel stationModel = getStationModel(station.getType().toString());
        if (stationModel == null) {
            return new Message(Message.Type.FAIL);
        }
        Trade trade = new Trade();
        trade.setStatus(Trade.Status.NOPAY);
        trade.setBaseType(Trade.BaseType.valueOf(station.getType().toString()));
        trade.setType(Trade.Type.STATION);
        trade.setInstanceId(station.getInstanceId());
        trade.setBuyType(Trade.BuyType.RENEW);
        trade.setUser(user);
        trade.setPrice(stationModel.getPrice());
        trade.setOutTradeNo(TradeUtil.buildOutTradeNo());
        Date expireDate = getExpireDate(trade.getCreateDate(),"renewStation");
        JSONObject remarkJson = new JSONObject();
        remarkJson.put("expireDate",expireDate.getTime());
        trade.setRemark(remarkJson.toString());
//        trade.setValidTime(1);
        save(trade);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("trade", tradeToJson(trade));
        return new Message(Message.Type.OK,jsonObject);
    }

    /**
     * 套餐升级
     * @param objectMap
     * @param user
     * @return
     */
    public Message updatePackageTrade(Map<String, Object> objectMap, User user) {
        Object instanceId = objectMap.get("instanceId");
        Object packageType = objectMap.get("packageType");
        Message message = getIndividualTemplates(instanceId, packageType);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        Package aPackage=(Package) (map.get("aPackage"));
        if (!aPackage.getUser().getId().equals(user.getId())) {
            return new Message(Message.Type.FAIL);
        }
        PackageModel newPackageModel=(PackageModel)(map.get("newPackageModel"));
        PackageModel oldPackageModel=(PackageModel)(map.get("oldPackageModel"));
        Trade trade = new Trade();
        trade.setStatus(Trade.Status.NOPAY);
        trade.setBaseType(Trade.BaseType.valueOf(packageType.toString()));
        trade.setType(Trade.Type.PACKAGE);
        trade.setInstanceId(aPackage.getInstanceId());
        trade.setBuyType(Trade.BuyType.UPGRADE);
        trade.setUser(user);
        trade.setPrice(diffPrice(aPackage,newPackageModel,oldPackageModel));
        trade.setOutTradeNo(TradeUtil.buildOutTradeNo());
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("type", Trade.BuyType.UPGRADE);
        for (int i = 0; i < CommonAttributes.TRADEBASETYPEE.length; i++) {
            if (CommonAttributes.TRADEBASETYPEE[i].equals(oldPackageModel.getType().toString())) {
                jsonObject2.put("oldType", CommonAttributes.TRADEBASETYPEC[i]);
                break;
            }
        }
        for (int i = 0; i < CommonAttributes.TRADEBASETYPEE.length; i++) {
            if (CommonAttributes.TRADEBASETYPEE[i].equals(newPackageModel.getType().toString())) {
                jsonObject2.put("newType", CommonAttributes.TRADEBASETYPEC[i]);
                break;
            }
        }
        jsonObject2.put("expireDate",aPackage.getExpireDate().getTime());
        trade.setRemark(jsonObject2.toString());
        save(trade);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("trade", tradeToJson(trade));
        return new Message(Message.Type.OK,jsonObject);
    }

    /**
     * 根据用户查询订单列表
     * @param user
     * @return
     */
    public List<Trade> findByUser(User user) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("user", user));
        filters.add(Filter.eq("deleteStatus", false));
        return tradeDao.findList(0, null, filters);
    }

    /**
     * tradesListToJson
     * @param trades
     * @return
     */
    public JSONArray tradesToJson(List<Trade> trades) {
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        for (Trade trade : trades) {
            jsonObject = tradeToJson(trade);
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public JSONObject tradeToJson(Trade trade) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", trade.getId());
        jsonObject.put("outTradeNo", trade.getOutTradeNo());
        jsonObject.put("price", trade.getPrice());
        jsonObject.put("baseType", trade.getBaseType());
//        if (trade.getValidTime() != 0) {
//            jsonObject.put("validTime", trade.getValidTime());
//        }
        if (trade.getRefundDate() != null) {
            jsonObject.put("refundDate", trade.getRefundDate().getTime());
        }
        if (trade.getPayType() != null) {
            jsonObject.put("payType", trade.getPayType());
        }
        if (trade.getDownloadUrl() != null) {
            jsonObject.put("downloadUrl", trade.getDownloadUrl());
        }
        jsonObject.put("status", trade.getStatus());
        if (trade.getGoodsNum() != 0) {
            jsonObject.put("goodsNum", trade.getGoodsNum());
        }
        if (trade.getPayDate() != null) {
            jsonObject.put("payDate", trade.getPayDate().getTime());
        }
        if (trade.getBuyType() != null) {
            jsonObject.put("buyType", trade.getBuyType());
        }
        jsonObject.put("instanceId", trade.getInstanceId());
        jsonObject.put("type", trade.getType());
        if (trade.getRemark() != null) {
            jsonObject.put("remark", trade.getRemark());
        }
        jsonObject.put("createDate", trade.getCreateDate().getTime());
        return jsonObject;
    }

    /**
     * 获取套餐差价
     * @param instanceId
     * @param packageType
     * @return
     */
    public Message getPackageDiff(Object instanceId, Object packageType) {
        Message message = getIndividualTemplates(instanceId, packageType);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Map<String, Object> map = (Map<String, Object>) message.getData();
        double diff=diffPrice((Package) (map.get("aPackage")),(PackageModel)(map.get("newPackageModel")),(PackageModel)(map.get("oldPackageModel")));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("diff", diff);
        return new Message(Message.Type.OK, jsonObject);
    }

    /**
     * 获取各个类模板
     * @param instanceId
     * @param packageType
     * @return
     */
    private Message getIndividualTemplates(Object instanceId, Object packageType) {
        if (instanceId == null || packageType == null) {
            return new Message(Message.Type.FAIL);
        }
        Package aPackage = packageService.findByInstanceId(instanceId.toString());
        if (aPackage == null) {
            return new Message(Message.Type.FAIL);
        }
//        获取原始套餐模板
        PackageModel oldPackageModel = getPackageModel(aPackage.getType().toString());
        if (oldPackageModel == null) {
            return new Message(Message.Type.FAIL);
        }
//        获取新的套餐模板
        PackageModel newPackageModel = getPackageModel(packageType.toString());
        if (newPackageModel == null) {
            return new Message(Message.Type.FAIL);
        }
//        新套餐等级不能小于原套餐等级
        if (newPackageModel.getLevel() <= oldPackageModel.getLevel()) {
            return new Message(Message.Type.UNKNOWN);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("aPackage", aPackage);
        map.put("oldPackageModel", oldPackageModel);
        map.put("newPackageModel", newPackageModel);
        return new Message(Message.Type.OK, map);
    }


    /**
     * 计算差价
     * @param aPackage
     * @param newPackageModel
     * @param oldPackageModel
     * @return
     */
    private double diffPrice(Package aPackage, PackageModel newPackageModel, PackageModel oldPackageModel) {
        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
        Calendar c1 = Calendar.getInstance();
        c1.setTime(aPackage.getExpireDate());
        long expireDate = c1.getTimeInMillis();
//        未使用天数
        long day=(expireDate-now)/1000/60/60/24;
        double money = oldPackageModel.getPrice()/365;
        double money1 = newPackageModel.getPrice()/365;
        double m=money1-money;
        double m1=m*day;
        BigDecimal bg = new BigDecimal(m1);
        double f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return f1;
    }

    /**
     * 检查是否有未支付订单
     * @param user
     * @return true 不存在 false 有未支付订单
     */
    public boolean checkTradeHaveNopay(User user) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("user", user));
        filters.add(Filter.eq("status", Trade.Status.NOPAY));
        List<Trade> trades = tradeDao.findList(0, null, filters);
        if (trades == null || trades.size() == 0) {
            return true;
        }
        return false;
    }

    /**
     * BaseType转汉字
     * @param trade
     * @return
     */
    public String convertBaseType(Trade trade){
        String type="";
        for (int i = 0; i < CommonAttributes.TRADEBASETYPEE.length; i++) {
            if (CommonAttributes.TRADEBASETYPEE[i].equals(trade.getBaseType().toString())) {
                type = CommonAttributes.TRADEBASETYPEC[i];
                break;
            }
        }
        return type;
    }

    /**
     * type转汉字
     * @param trade
     * @return
     */
    public String convertType(Trade trade){
        String type="";
        for (int i = 0; i < CommonAttributes.TRADETYPEE.length; i++) {
            if (CommonAttributes.TRADETYPEE[i].equals(trade.getType().toString())) {
                type = CommonAttributes.TRADETYPEC[i];
                break;
            }
        }
        return type;
    }

    /**
     * buyType转汉字
     * @param trade
     * @return
     */
    public String convertBuyType(Trade trade){
        String type="";
        for (int i = 0; i < CommonAttributes.TRADEBUYTYPEE.length; i++) {
            if (CommonAttributes.TRADEBUYTYPEE[i].equals(trade.getType().toString())) {
                type = CommonAttributes.TRADEBUYTYPEC[i];
                break;
            }
        }
        return type;
    }

    /**
     * 设置产品到期时间
     * @param date
     */
    private Date getExpireDate(Date date,String type){
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        if("station".equals(type)){
            c.add(Calendar.MONTH,1);
        }else{
            c.add(Calendar.YEAR,1);
        }
        Date expireDate = new Date(c.getTimeInMillis());
        return expireDate;
    }

    /**
     * 订单过期定时检测(订单应在两小时之内支付,超过两小时视为过期)
     */
    public void expireCheck() {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("status", Trade.Status.NOPAY));
        List<Trade> trades = tradeDao.findList(0, null, filters);
        Long now = System.currentTimeMillis();
        Trade trade;
        for(int i = 0;i<trades.size();i++){
            trade = trades.get(i);
            if(now-trade.getCreateDate().getTime()>2*60*60*100){
                trade.setStatus(Trade.Status.EXPIRE);
                save(trade);
            }
        }
    }
}
