package com.hysw.qqsl.cloud.wechat.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.wechat.util.WeChatHttpRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 微信菜单控制
 * Created by chenl on 17-6-27.
 */
@Service("weChatMenu")
public class WeChatMenu {
    @Autowired
    private GetAccessTokenService getAccessTokenService;
    private final String httpsPostUrl = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";

    public JSONObject weixinMenu() throws UnsupportedEncodingException {
        String httpsPostUrl1 = httpsPostUrl.replaceAll("ACCESS_TOKEN", getAccessTokenService.getToken());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "view");
        jsonObject.put("name", "淘宝小店");
        jsonObject.put("url", "https://shop373912999.m.taobao.com/");
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);
        jsonObject = new JSONObject();
        jsonObject.put("type", "view");
        jsonObject.put("name", "微信小店");
        jsonObject.put("url", "http://mp.weixin.qq.com/bizmall/mallshelf?id=&t=mall/list&biz=MzA5ODM3NzEwNQ==&shelf_id=2&showwxpaytitle=1#wechat_redirect");
        jsonArray.add(jsonObject);
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("name", "青清小店");
        jsonObject1.put("sub_button", jsonArray);
        jsonObject = new JSONObject();
        jsonObject.put("type", "view");
        jsonObject.put("name", "青清公益");
        jsonObject.put("url", "https://buluo.qq.com/p/barindex.html?bid=118496&from=");
        JSONArray jsonArray1 = new JSONArray();
        jsonArray1.add(jsonObject);
        jsonArray1.add(jsonObject1);
        jsonObject = new JSONObject();
        jsonObject.put("type", "view");
        jsonObject.put("name", "水利云");
        jsonObject.put("url", "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+CommonAttributes.APPID+"&redirect_uri="+URLEncoder.encode("http://www.qingqingshuili.com/hot-update/weChat/www/auth.html","utf-8")+"&response_type=code&scope="+"snsapi_base"+"&state=STATE#wechat_redirect");
        jsonArray = new JSONArray();
        jsonArray.add(jsonObject);
        jsonObject = new JSONObject();
        jsonObject.put("type", "click");
        jsonObject.put("name", "联系我们");
        jsonObject.put("key", "lianxiwomen");
        jsonArray.add(jsonObject);
        jsonObject1 = new JSONObject();
        jsonObject1.put("name", "其他");
        jsonObject1.put("sub_button", jsonArray);
        jsonArray1.add(jsonObject1);
        jsonObject = new JSONObject();
        jsonObject.put("button", jsonArray1);
//        System.out.println(jsonObject);
        JSONObject jsonObject2 = WeChatHttpRequest.jsonObjectHttpRequest(httpsPostUrl1, "POST", jsonObject.toString());
        return jsonObject2;
    }
}
