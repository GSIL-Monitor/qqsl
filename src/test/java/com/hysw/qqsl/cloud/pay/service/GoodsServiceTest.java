package com.hysw.qqsl.cloud.pay.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.pay.entity.GoodsModel;
import net.sf.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class GoodsServiceTest extends BaseTest {
    @Autowired
    private GoodsService goodsService;

    @Test
    public void testPutInCache(){
        goodsService.putGoodsModelInCache();
        List<GoodsModel> goodsModels = goodsService.getGoodsModelFromCache();
        Assert.assertTrue(goodsModels.size()==2);
    }

    @Test
    public void testGetGoodsList(){
        JSONArray goodsList = goodsService.getGoodsList();
        Assert.assertTrue(goodsList.size()==2);
    }
}
