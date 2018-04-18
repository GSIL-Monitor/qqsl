package com.hysw.qqsl.cloud.pay.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.StationModel;
import com.hysw.qqsl.cloud.core.entity.data.Station;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.BaseService;
import com.hysw.qqsl.cloud.core.service.MessageService;
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
     * @param packageModel
     * @param packageType
     * @param user
     * @return
     */
    public JSONObject createPackageTrade(PackageModel packageModel, Object packageType, User user) {
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
        return jsonObject;
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
     * @param stationModel
     * @param stationType
     * @param user
     * @return
     */
    public JSONObject createStationTrade(StationModel stationModel, Object stationType, User user) {
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
        return jsonObject;
    }

    /**
     * 获取测站模板
     * @param stationType
     * @return
     */
    public StationModel getStationModel(String stationType) {
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
     * @param goodsModel
     * @param goodsNum
     * @param goodsType
     * @param remark
     * @param user
     * @return
     */
    public JSONObject createGoodsTrade(GoodsModel goodsModel, Object goodsNum, Object goodsType, Object remark, User user) {
        Trade trade = new Trade();
        trade.setOutTradeNo(TradeUtil.buildOutTradeNo());
        trade.setType(Trade.Type.GOODS);
        trade.setGoodsNum(Integer.valueOf(goodsNum.toString()));
        trade.setPrice(goodsModel.getPrice() * trade.getGoodsNum());
        trade.setUser(user);
        trade.setStatus(Trade.Status.NOPAY);
        trade.setBuyType(Trade.BuyType.BUY);
        trade.setBaseType(Trade.BaseType.valueOf(goodsType.toString()));
        trade.setInstanceId(TradeUtil.buildInstanceId());
        trade.setRemark(remark.toString());
        save(trade);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("trade", tradeToJson(trade));
        return jsonObject;
    }

    /**
     * 获取数据服务模板
     * @param goodsType
     * @return
     */
    public GoodsModel getGoodsModel(String goodsType) {
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
     * @param aPackage
     * @param packageModel
     * @param user
     * @return
     */
    public JSONObject renewPackageTrade(Package aPackage, PackageModel packageModel, User user) {
        Trade trade = new Trade();
        trade.setStatus(Trade.Status.NOPAY);
        trade.setBaseType(Trade.BaseType.valueOf(aPackage.getType().toString()));
        trade.setType(Trade.Type.PACKAGE);
        trade.setInstanceId(aPackage.getInstanceId());
        trade.setBuyType(Trade.BuyType.RENEW);
        trade.setUser(user);
        trade.setPrice(packageModel.getPrice());
        trade.setOutTradeNo(TradeUtil.buildOutTradeNo());
        Date expireDate = getExpireDate(trade.getCreateDate(), "package");
        JSONObject remarkJson = new JSONObject();
        remarkJson.put("expireDate", expireDate.getTime());
        trade.setRemark(remarkJson.toString());
//        trade.setValidTime(1);
        save(trade);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("trade", tradeToJson(trade));
        return jsonObject;
    }

    /**
     * 测站续费
     * @param station
     * @param stationModel
     * @param user
     * @return
     */
    public JSONObject renewStationTrade(Station station, StationModel stationModel, User user) {
        Trade trade = new Trade();
        trade.setStatus(Trade.Status.NOPAY);
        trade.setBaseType(Trade.BaseType.valueOf(station.getType().toString()));
        trade.setType(Trade.Type.STATION);
        trade.setInstanceId(station.getInstanceId());
        trade.setBuyType(Trade.BuyType.RENEW);
        trade.setUser(user);
        trade.setPrice(stationModel.getServicePrice());
        trade.setOutTradeNo(TradeUtil.buildOutTradeNo());
        Date expireDate = getExpireDate(trade.getCreateDate(), "renewStation");
        JSONObject remarkJson = new JSONObject();
        remarkJson.put("expireDate", expireDate.getTime());
        trade.setRemark(remarkJson.toString());
//        trade.setValidTime(1);
        save(trade);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("trade", tradeToJson(trade));
        return jsonObject;
    }

    /**
     * 套餐升级
     * @param map
     * @param aPackage
     * @param packageType
     * @param user
     * @return
     */
    public JSONObject updatePackageTrade(Map<String, Object> map, Package aPackage, Object packageType, User user) {
        PackageModel newPackageModel = (PackageModel) (map.get("newPackageModel"));
        PackageModel oldPackageModel = (PackageModel) (map.get("oldPackageModel"));
        Trade trade = new Trade();
        trade.setStatus(Trade.Status.NOPAY);
        trade.setBaseType(Trade.BaseType.valueOf(packageType.toString()));
        trade.setType(Trade.Type.PACKAGE);
        trade.setInstanceId(aPackage.getInstanceId());
        trade.setBuyType(Trade.BuyType.UPGRADE);
        trade.setUser(user);
        trade.setPrice(diffPrice(aPackage, newPackageModel, oldPackageModel));
        trade.setOutTradeNo(TradeUtil.buildOutTradeNo());
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("type", Trade.BuyType.UPGRADE);
        for (Trade.BaseType baseType : Trade.BaseType.values()) {
            if (baseType.name().equals(oldPackageModel.getType().toString())) {
                jsonObject2.put("oldType", baseType.getTypeC());
                break;
            }
        }
        for (Trade.BaseType baseType : Trade.BaseType.values()) {
            if (baseType.name().equals(newPackageModel.getType().toString())) {
                jsonObject2.put("newType", baseType.getTypeC());
                break;
            }
        }
        jsonObject2.put("expireDate", aPackage.getExpireDate().getTime());
        trade.setRemark(jsonObject2.toString());
        save(trade);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("trade", tradeToJson(trade));
        return jsonObject;
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
     * 计算差价
     * @param aPackage
     * @param newPackageModel
     * @param oldPackageModel
     * @return
     */
    public double diffPrice(Package aPackage, PackageModel newPackageModel, PackageModel oldPackageModel) {
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
        for (Trade.BaseType baseType : Trade.BaseType.values()) {
            if (baseType.name().equals(trade.getBaseType().toString())) {
                type = baseType.getTypeC();
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
        for (Trade.Type type1 : Trade.Type.values()) {
            if (type1.name().equals(trade.getType().toString())) {
                type = type1.getTypeC();
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
        for (Trade.BuyType buyType : Trade.BuyType.values()) {
            if (buyType.name().equals(trade.getBuyType().toString())) {
                type = buyType.getTypeC();
                break;
            }
        }
        return type;
    }

    /**
     * 设置产品到期时间
     * @param date
     */
    protected Date getExpireDate(Date date, String type){
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
