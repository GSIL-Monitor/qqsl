package com.hysw.qqsl.cloud.wechat.controller;

import com.hysw.qqsl.cloud.wechat.sdk.WXPayConfigImpl;
import com.hysw.qqsl.cloud.wechat.sdk.WXPayUtil;
import com.hysw.qqsl.cloud.wechat.service.WeChatMenu;
import com.hysw.qqsl.cloud.wechat.service.WeChatService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * 微信控制类
 * Created by chenl on 17-6-26.
 */
@Controller
@RequestMapping("/weChat")
public class WeChatController {
    @Autowired
    private WeChatMenu weChatMenu;
    @Autowired
    private WeChatService weChatService;
    @Autowired
    private WXPayConfigImpl wxPayConfig;

    @RequestMapping(value = "/action", method = {RequestMethod.GET,RequestMethod.POST})
    public @ResponseBody  String getToken(HttpServletRequest request) {
        String respXml="";
        if (request.getMethod().toLowerCase().equals("get")) {
            String signature = request.getParameter("signature");
            String timestamp = request.getParameter("timestamp");
            String nonce = request.getParameter("nonce");
            String echostr = request.getParameter("echostr");
            return weChatService.access(signature,timestamp,nonce,echostr);
        }else{
            try {
                // 处理接收消息
                ServletInputStream in = request.getInputStream();
                respXml = weChatService.acceptMessage(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return respXml;
    }

    @RequestMapping(value = "/createMenu", method = RequestMethod.GET)
    public @ResponseBody String creatMenu(){
        JSONObject jsonObject = null;
        try {
            jsonObject = weChatMenu.weixinMenu();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }


    @RequestMapping(value = "/notice", method = RequestMethod.POST)
    public @ResponseBody String notice(HttpServletRequest request) throws Exception {
        System.out.println(request);
        InputStream inStream = request.getInputStream();
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        outSteam.close();
        inStream.close();
        String result = new String(outSteam.toByteArray(), "utf-8");
        Map<String, String> map = WXPayUtil.xmlToMap(result);
        map.put("total_fee", "1");
        String s = WXPayUtil.mapToXml(map);
        boolean b = WXPayUtil.isSignatureValid(s, wxPayConfig.getKey());
        System.out.println(b);
        return "success";
    }
}
