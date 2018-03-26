package com.hysw.qqsl.cloud.krpano.controller;

import com.hysw.qqsl.cloud.krpano.service.KrpanoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全景控制层
 * Create by leinuo on 18-3-22 下午4:33
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
@Controller
@RequestMapping("/krpano")
public class KrpanoController {
    @Autowired
    private KrpanoService krpanoService;
    @RequestMapping(value = "/tour", method = RequestMethod.GET,produces="text/html; charset=UTF-8")
    public
    @ResponseBody
    String getTour() {
        String tourStr = krpanoService.getTour();
        return tourStr;
      //  return "轻轻";
    }
}
