package com.hysw.qqsl.cloud.pay.service;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.AccountService;
import com.hysw.qqsl.cloud.core.service.BaseService;
import com.hysw.qqsl.cloud.core.service.ProjectService;
import com.hysw.qqsl.cloud.core.service.UserService;
import com.hysw.qqsl.cloud.pay.dao.PackageDao;
import com.hysw.qqsl.cloud.pay.entity.PackageItem;
import com.hysw.qqsl.cloud.pay.entity.PackageModel;
import com.hysw.qqsl.cloud.pay.entity.ServeItem;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import com.hysw.qqsl.cloud.util.SettingUtils;
import com.hysw.qqsl.cloud.util.TradeUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 套餐服务类
 */
@Service("packageService")
public class PackageService extends BaseService<Package,Long>{
    @Autowired
    private PackageDao packageDao;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;
    Setting setting = SettingUtils.getInstance().getSetting();

    @Autowired
    public void setBaseDao(PackageDao packageDao) {
        super.setBaseDao(packageDao);
    }

    /**
     * 读取xml--serveItem
     */

    private List<ServeItem> readServeItemContent(String xml) {
        Element root = null;
        try {
            root = SettingUtils.getInstance().getRootElement(xml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        List<Element> elements = root.elements();
        Element element;
        List<ServeItem> serveItems = new ArrayList<>();
        ServeItem serveItem;
        for (int i = 0; i < elements.size(); i++) {
            element = elements.get(i);
            serveItem = new ServeItem();
            serveItem.setName(element.attributeValue("name"));
            serveItem.setType(ServeItem.Type.valueOf(element.attributeValue("type").toUpperCase()));
            serveItem.setDescription(element.attributeValue("description"));
            serveItems.add(serveItem);
        }
        return serveItems;
    }

    /**
     * 读取xml--packageModel
     * @param xml
     * @param serveItems
     * @throws DocumentException
     */
    private List<PackageModel> readPackageModel(String xml,List<ServeItem> serveItems){
        Element root = null;
        try {
            root = SettingUtils.getInstance().getRootElement(xml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        List<Element> elements = root.elements();
        Element element;
        List<PackageModel> packageModels = new ArrayList<>();
        PackageModel packageModel;
        List<PackageItem> packageItems;
        PackageItem packageItem;
        for (int i = 0; i < elements.size(); i++) {
            packageModel = new PackageModel();
            element = elements.get(i);
            packageModel.setName(element.attributeValue("name"));
            packageModel.setType(CommonEnum.PackageType.valueOf(element.attributeValue("type").toUpperCase()));
            packageModel.setDescription(element.attributeValue("description"));
            packageModel.setPrice(Double.valueOf(element.attributeValue("price")));
            packageModel.setLevel(Integer.valueOf(element.attributeValue("level")));
            List elements1 = element.elements();
            packageItems = new ArrayList<>();
            for (int j = 0; j < elements1.size(); j++) {
                packageItem = new PackageItem();
                element = (Element) elements1.get(j);
                for (int k = 0; k < serveItems.size(); k++) {
                    if (serveItems.get(k).getType().toString().equals(element.attributeValue("type").toUpperCase())) {
                        packageItem.setServeItem(serveItems.get(k));
                        break;
                    }
                }
                packageItem.setLimitNum(Long.valueOf(element.attributeValue("limitNum")));
                packageItems.add(packageItem);
            }
            packageModel.setPackageItems(packageItems);
            packageModels.add(packageModel);
        }
        return packageModels;
    }

    /**
     * 将套餐模板放入缓存
     */
    public void putPackageModelInCache(){
        Cache cache = cacheManager.getCache("packageModelCache");
        net.sf.ehcache.Element element = new net.sf.ehcache.Element("packages", readPackageModel(setting.getPackageModel(), readServeItemContent(setting.getServeItem())));
        cache.put(element);
    }

    /**
     * 从缓存取出套餐模板
     * @return
     */
    public List<PackageModel> getPackageModelFromCache(){
        Cache cache = cacheManager.getCache("packageModelCache");
        net.sf.ehcache.Element element = cache.get("packages");
        return (List<PackageModel>) element.getValue();
    }

    /**
     * 获取套餐列表
     * @return
     */
    public JSONArray getPackageList(){
        List<PackageModel> packageModels = (List<PackageModel>) SettingUtils.objectCopy(getPackageModelFromCache());
        JSONArray jsonArray1 = new JSONArray();
        for (PackageModel packageModel : packageModels) {
            JSONObject jsonObject = packageModelToJson(packageModel);
            jsonArray1.add(jsonObject);
        }
        return jsonArray1;
    }

    public JSONObject packageModelToJson(PackageModel packageModel){
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObject1;
        jsonObject.put("name", packageModel.getName());
        jsonObject.put("type", packageModel.getType());
        jsonObject.put("price", packageModel.getPrice());
        jsonObject.put("description", packageModel.getDescription());
        jsonObject.put("level", packageModel.getLevel());
        List<PackageItem> packageItems = packageModel.getPackageItems();
        JSONArray jsonArray = new JSONArray();
        for (PackageItem packageItem : packageItems) {
            jsonObject1 = new JSONObject();
            jsonObject1.put("name", packageItem.getServeItem().getName());
            jsonObject1.put("type", packageItem.getServeItem().getType());
            jsonObject1.put("description", packageItem.getServeItem().getDescription());
            jsonObject1.put("limitNum", packageItem.getLimitNum());
            jsonArray.add(jsonObject1);
        }
        jsonObject.put("packageItems", jsonArray);
        return jsonObject;
    }

    /**
     * 激活套餐
     * @param trade
     */
    public void activatePackage(Trade trade) {
        Package aPackage = findByUser(trade.getUser());
        if (aPackage == null) {
            aPackage = new Package();
        }
        aPackage.setType(CommonEnum.PackageType.valueOf(trade.getBaseType().toString()));
        JSONObject remarkJson = JSONObject.fromObject(trade.getRemark());
        Long times = remarkJson.getLong("expireDate");
        aPackage.setExpireDate(new Date(times));
        aPackage.setInstanceId(trade.getInstanceId());
        aPackage.setUser(trade.getUser());
        aPackage.setCurSpaceNum(0);
        aPackage.setCurTrafficNum(0);
        save(aPackage);
    }

    public Package findByInstanceId(String instanceId) {
        List<Package> packages = findAll();
        for (Package aPackage : packages) {
            if (aPackage.getInstanceId().equals(instanceId)) {
                return (Package) SettingUtils.objectCopy(aPackage);
            }
        }
        return null;
    }

    /**
     * 套餐续费
     * @param trade
     */
    public void renewPackage(Trade trade) {
        Package aPackage = findByInstanceId(trade.getInstanceId());
        JSONObject remarkJson = JSONObject.fromObject(trade.getRemark());
        Long times = remarkJson.getLong("expireDate");
        aPackage.setExpireDate(new Date(times));
        save(aPackage);
    }

    /**
     * 套餐升级
     * @param trade
     */
    public void updatePackage(Trade trade) {
        Package aPackage = findByInstanceId(trade.getInstanceId());
        aPackage.setType(CommonEnum.PackageType.valueOf(trade.getBaseType().toString()));
        save(aPackage);
    }

    /**
     * 是否满足套餐购买要求
     * @param userId
     * @param packageModel
     * @return false 满足要求  true 不满足要求
     */
    public Boolean isRequirementPackage(Long userId,PackageModel packageModel){
        User user = userService.findByDao(userId);
        Package aPackage = findByUser(user);
        if (aPackage == null) {
            return false;
        }
        if (packageModel.getType() == CommonEnum.PackageType.TEST) {
            return false;
        }
        if (aPackage.getExpireDate().getTime() <= System.currentTimeMillis()) {
            return true;
        }
        boolean flag1=false;
        boolean flag2=false;
        boolean flag3=false;
        for (PackageItem packageItem : packageModel.getPackageItems()) {
            if (packageItem.getServeItem().getType() == ServeItem.Type.ACCOUNT && packageItem.getLimitNum() >= user.getAccounts().size()) {
                flag1 = true;
            }
            if (packageItem.getServeItem().getType() == ServeItem.Type.SPACE && packageItem.getLimitNum() >= aPackage.getCurSpaceNum()) {
                flag2 = true;
            }
            if (packageItem.getServeItem().getType() == ServeItem.Type.PROJECT && packageItem.getLimitNum() >= projectService.findByUser(user).size()) {
                flag3 = true;
            }
//            if (packageItem.getServeItem().getType() == ServeItem.Type.PANO && packageItem.getLimitNum() >= aPackage.getCurSpaceNum()) {
//                flag2 = true;
//            }
//            if (packageItem.getServeItem().getType() == ServeItem.Type.FINDCM && packageItem.getLimitNum() >= aPackage.getCurSpaceNum()) {
//                flag2 = true;
//            }
//            if (packageItem.getServeItem().getType() == ServeItem.Type.BIMSERVE && packageItem.getLimitNum() >= aPackage.getCurSpaceNum()) {
//                flag2 = true;
//            }
        }
        if (flag1 && flag2 && flag3) {
            return false;
        }
        return true;
    }


    /**
     * 根据用户查询套餐
     * @param user
     * @return
     */
    public Package findByUser(User user){
        List<Package> packages = findAll();
        for (Package aPackage : packages) {
            if (aPackage.getUser().getId().equals(user.getId())) {
                return (Package) SettingUtils.objectCopy(aPackage);
            }
        }
        return null;
    }

    /**
     * 初始化套餐流量
     */
    public void initCurTrafficNum() {
        List<Package> packages = (List<Package>) SettingUtils.objectCopy(findAll());
        for (Package aPackage : packages) {
            aPackage.setCurTrafficNum(0);
            save(aPackage);
        }
    }

    /**
     * 手动初始化套餐流量
     * @param user
     */
    public void initCurTrafficNum(User user) {
        List<Package> packages = (List<Package>) SettingUtils.objectCopy(findAll());
        for (Package aPackage : packages) {
            if (aPackage.getUser().getId().equals(user.getId())) {
                aPackage.setCurTrafficNum(0);
                save(aPackage);
            }
        }
    }

    /**
     * 用户注册就激活测试版套餐
     * @param user
     */
    public void activateTestPackage(User user) {
        Package aPackage = new Package();
        aPackage.setType(CommonEnum.PackageType.TEST);
        aPackage.setUser(user);
        aPackage.setInstanceId(TradeUtil.buildInstanceId());
        Calendar c = Calendar.getInstance();
        c.set(2099, Calendar.DECEMBER, 31, 23, 59, 59);
        aPackage.setExpireDate(new Date(c.getTimeInMillis()));
        save(aPackage);
    }

    /**
     * 添加package组缓存
     */
    public void packageCache() {
        List<Package> packages = packageDao.findList(0, null, null);
        Cache cache = cacheManager.getCache("packageAllCache");
        net.sf.ehcache.Element element = new net.sf.ehcache.Element("package", packages);
        cache.put(element);
    }

    @Override
    public List<Package> findAll() {
        Cache cache = cacheManager.getCache("packageAllCache");
        net.sf.ehcache.Element element = cache.get("package");
        List<Package> packages=(List<Package>) element.getValue();
        return packages;
    }

    /**
     * packageTojson
     * @param aPackage
     * @return
     */
    public JSONObject toJson(Package aPackage) {
        if (aPackage == null) {
            return new JSONObject();
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", aPackage.getId());
        jsonObject.put("instanceId", aPackage.getInstanceId());
        jsonObject.put("type", aPackage.getType());
        jsonObject.put("expireDate", aPackage.getExpireDate().getTime());
        jsonObject.put("curSpaceNum", aPackage.getCurSpaceNum());
        jsonObject.put("curTrafficNum", aPackage.getCurTrafficNum());
        jsonObject.put("curAccountNum", userService.getAccountsByUserId(aPackage.getUser().getId()).size());
        jsonObject.put("curProjectNum", projectService.findByUser(aPackage.getUser()).size());
        return jsonObject;
    }

    /**
     * 本公司套餐取最大
     */
    public void packageMax(){
        User user = userService.find(16l);
        Package aPackage = findByUser(user);
        List<PackageModel> packageModels = getPackageModelFromCache();
        PackageModel packageModel = null;
        for (PackageModel model : packageModels) {
            if (packageModel == null) {
                packageModel = model;
            }else{
                if (packageModel.getLevel() < model.getLevel()) {
                    packageModel = model;
                }
            }
        }
        if (packageModel == null) {
            return;
        }
        aPackage.setType(packageModel.getType());
        Calendar c = Calendar.getInstance();
        c.set(2099, Calendar.DECEMBER, 31, 23, 59, 59);
        aPackage.setExpireDate(new Date(c.getTimeInMillis()));
        save(aPackage);
    }
}
