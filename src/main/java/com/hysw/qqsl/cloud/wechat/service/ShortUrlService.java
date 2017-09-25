package com.hysw.qqsl.cloud.wechat.service;

import com.hysw.qqsl.cloud.util.HttpRequestUtil;
import com.hysw.qqsl.cloud.wechat.util.WeChatHttpRequest;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 长连接转短连接
 */
public class ShortUrlService {
    private final String shortUrlHttp = "https://api.weixin.qq.com/cgi-bin/shorturl?access_token=ACCESS_TOKEN";
    @Autowired
    private GetAccessTokenService getAccessTokenService;

    public void longUrlToShortUrl(String longUrl) {
        String shortUrlHttp1 = shortUrlHttp.replace("ACCESS_TOKEN", "nA-6RPbs1GL4KMABXftCIXBFfiUdGZf8UUA9McUjSeAhgAxSSEBucJ-lwQi4xIZvZD61kVYPkoyE8va51W1WtmKAbnjMnRp-_uART7nZR-sWBThCCAOTL");
        try {
            String param = "{\"action\":\"long2short\","
                    + "\"long_url\":\""+longUrl+"\"}";
            JSONObject jsonObject = WeChatHttpRequest.jsonObjectHttpRequest(shortUrlHttp1, "POST", param);
            System.err.println("jsonObject:"+jsonObject);
            boolean containsValue = jsonObject.containsKey("errcode");
            if(containsValue){
                String errcode = jsonObject.getString("errcode");
                System.err.println("errcode"+errcode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
