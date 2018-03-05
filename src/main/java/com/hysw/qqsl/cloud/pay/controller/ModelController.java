package com.hysw.qqsl.cloud.pay.controller;

import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.service.MessageService;
import com.hysw.qqsl.cloud.core.service.StationService;
import com.hysw.qqsl.cloud.pay.service.GoodsService;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 商品列表
 */
@Controller
@RequestMapping("/model")
public class ModelController {
    @Autowired
    private StationService stationService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private PackageService packageService;


    /**
     * 套餐列表
     * @return
     */
    @RequestMapping(value = "/packages", method = RequestMethod.GET)
    public @ResponseBody Message getPackages() {
        return MessageService.message(Message.Type.OK, packageService.getPackageList());
    }

    /**
     * 数据服务列表
     * @return
     */
    @RequestMapping(value = "/goods", method = RequestMethod.GET)
    public @ResponseBody Message getGoods() {
        return MessageService.message(Message.Type.OK, goodsService.getGoodsList());
    }

    /**
     * 测站列表
     * @return
     */
    @RequestMapping(value = "/stations", method = RequestMethod.GET)
    public @ResponseBody Message getStations() {
        return MessageService.message(Message.Type.OK,stationService.getStationList());
    }
}
