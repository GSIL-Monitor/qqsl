package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.util.HttpRequestUtil;
import com.hysw.qqsl.cloud.util.SettingUtils;
import com.hysw.qqsl.cloud.wechat.service.CreateCode;
import com.hysw.qqsl.cloud.wechat.service.GetAccessTokenService;
import com.hysw.qqsl.cloud.wechat.service.UploadFodderService;
import com.hysw.qqsl.cloud.wechat.service.WeChatService;
import com.hysw.qqsl.cloud.wechat.util.WeChatHttpRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by chenl on 17-6-26.
 */
public class WeiXinRequest {
    private String httpsGetUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
    private String httpsPostUrl = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";

    private final String appID = "wx0d8493d76fa4b58d";
    private final String appsecret = "0a638eccdd73d77f317c900afd01ea55";

    @Test
    public void testGetToken() {

        httpsGetUrl = httpsGetUrl.replaceAll("APPID", appID).replaceAll("APPSECRET", appsecret);
        JSONObject jsonObject = WeChatHttpRequest.jsonObjectHttpRequest(httpsGetUrl, "GET", null);
        System.out.println(jsonObject);
    }

    @Test
    public void weixinMenu(){
        httpsPostUrl = httpsPostUrl.replaceAll("ACCESS_TOKEN", "qqsl");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "click");
        jsonObject.put("name", "今日歌曲");
        jsonObject.put("key", "ertqwetqw");
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("button", jsonArray);
        JSONObject jsonObject2 = WeChatHttpRequest.jsonObjectHttpRequest(httpsPostUrl, "POST",jsonObject1.toString() );
        System.out.println(jsonObject2);
    }

//    signature=dsgas&timestamp=sdgas&nonce=sga7&echostr=sga

//        @Test
//    public void testToXml(){
//        TextMessage textMessage = new TextMessage();
//        textMessage.setContent("11111");
//        textMessage.setCreateTime(123456);
//        textMessage.setFromUserName("22222");
//        textMessage.setMsgType("3333");
//        textMessage.setToUserName("444444");
//        Class<?> clazz = TextMessage.class;
//        WeChatService weChatService = new WeChatService();
//        weChatService.outputXML(clazz, textMessage);
//    }
//    @Test
//    public void testUploadTransientFodder() throws IOException {
//        String filePath = "/home/chenl/qqsl/webwxgetmsgimg.jpg";
//        UploadFodderService uploadFodderService1 = new UploadFodderService();
//        String mediaId = uploadFodderService1.uploadTransientFodder("image", filePath);
//        System.out.println("mediaId--------\n"+mediaId);
//    }

    @Test
    public void getFodderCount(){
        UploadFodderService uploadFodderService = new UploadFodderService();
        uploadFodderService.getFodderCount();
    }

    @Test
    public void getFodderList(){
        String a = "?__biz=MzA5ODM3NzEwNQ==&mid=100000006&idx=1&sn=1d388aeaaf4a8c3458618fa8b081bd3c&chksm=1093cdf827e444ee5638b185811ec25fe610c646cda492bdbfb8d6138e78a2fa79823f56cbf8#rd";
        System.out.println(a.length());
        UploadFodderService uploadFodderService = new UploadFodderService();
        uploadFodderService.getFodderList("news",0,19);
    }

    @Test
    public void getFodder(){
        UploadFodderService uploadFodderService = new UploadFodderService();
        uploadFodderService.getFodder();
    }

    @Test
    public void testCreateCode() throws UnsupportedEncodingException {
        CreateCode createCode = new CreateCode();
        createCode.createCode();
    }

    @Test
    public void testGetCodeToken(){
        GetAccessTokenService getAccessTokenService = new GetAccessTokenService();
        JSONObject accessTokenByCode = getAccessTokenService.getAccessTokenByCode("081S6UzZ0zb5t22KEuyZ09fWzZ0S6Uzt");
        System.out.println(accessTokenByCode);
    }

    /**
     * 测试登录
     */
    @Test
    public void testWeChatLogin() throws UnsupportedEncodingException {
        System.out.println("https://open.weixin.qq.com/connect/oauth2/authorize?appid="+ CommonAttributes.APPID+"&redirect_uri="+ URLEncoder.encode("http://www.qingqingshuili.com/hot-update/weChat/auth.html","utf-8")+"&response_type=code&scope="+"snsapi_base"+"&state=STATE#wechat_redirect");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("phone", "13519779005");
        jsonObject.put("password", DigestUtils.md5Hex("77460038"));
        jsonObject.put("loginType", "wechat");
        JSONObject jsonObject1 = WeChatHttpRequest.jsonObjectHttpRequest("localhost:8080/qqsl/user/login", "POST", jsonObject.toString());
        System.out.println(jsonObject1);
    }
}
