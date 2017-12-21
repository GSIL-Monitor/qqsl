package com.hysw.qqsl.cloud.pay.service;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.pay.entity.GoodsModel;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据服务service
 */
@Service("goodsService")
public class GoodsService implements Serializable{
    @Autowired
    private CacheManager cacheManager;
    Setting setting = SettingUtils.getInstance().getSetting();

    /**
     * 读取xml--goods
     * @param xml
     */
    private List<GoodsModel> readGoodsItem(String xml) {
        Element root = null;
        try {
            root = SettingUtils.getInstance().getRootElement(xml);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        List<Element> elements = root.elements();
        Element element;
        List<GoodsModel> goodsModels = new ArrayList<>();
        GoodsModel goodsModel;
        for (int i = 0; i < elements.size(); i++) {
            element = elements.get(i);
            goodsModel=new GoodsModel();
            goodsModel.setName(element.attributeValue("name"));
            goodsModel.setType(CommonEnum.GoodsType.valueOf(element.attributeValue("type").toUpperCase()));
            goodsModel.setDescription(element.attributeValue("description"));
            goodsModel.setPrice(Double.valueOf(element.attributeValue("price")));
            goodsModels.add(goodsModel);
        }
        return goodsModels;
    }

    /**
     * 将数据服务模板放入缓存
     */
    public void putGoodsModelInCache(){
        Cache cache = cacheManager.getCache("goodsModelCache");
        net.sf.ehcache.Element element = new net.sf.ehcache.Element("goods",readGoodsItem(setting.getGoods()) );
        cache.put(element);
    }

    /**
     * 从缓存取出数据服务模板
     * @return
     */
    public List<GoodsModel> getGoodsModelFromCache(){
        Cache cache = cacheManager.getCache("goodsModelCache");
        net.sf.ehcache.Element element = cache.get("goods");
        return (List<GoodsModel>) element.getValue();
    }

    /**
     * 向前台返回数据服务列表
     * @return
     */
    public JSONArray getGoodsList(){
        List<GoodsModel> goodsModels = (List<GoodsModel>) SettingUtils.objectCopy(getGoodsModelFromCache());
        JSONArray jsonArray = new JSONArray();
        for (GoodsModel goodsModel : goodsModels) {
            JSONObject jsonObject = goodModelToJson(goodsModel);
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public JSONObject goodModelToJson(GoodsModel goodsModel){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", goodsModel.getName());
        jsonObject.put("type", goodsModel.getType());
        jsonObject.put("price", goodsModel.getPrice());
        jsonObject.put("description", goodsModel.getDescription());
        return jsonObject;
    }
}
