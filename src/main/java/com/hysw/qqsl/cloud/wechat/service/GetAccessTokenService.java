package com.hysw.qqsl.cloud.wechat.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.wechat.util.WeChatHttpRequest;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 * 获取token
 * Created by chenl on 17-6-27.
 */
@Service("getAccessTokenService")
public class GetAccessTokenService {
    private String token;
//    获取wechat的token
    private final String httpsGetUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
    private final String httpsPostUrl = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";
//    获取网页授权token
    private final String codeHttpUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
//    刷新网页授权token
    private final String refreshCodeHttpUrl = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=APPID&grant_type=refresh_token&refresh_token=REFRESH_TOKEN";
    //    获取用户详细信息
    private final String getUserMessage = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
    //    网页授权有效性检验
    private final String checkUrl = "https://api.weixin.qq.com/sns/auth?access_token=ACCESS_TOKEN&openid=OPENID";
    //公众号用于调用微信JS接口的临时票据
    private final String jsapiTicketUrl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi";

    /**
     * 获取token
     */
    public void getAccessToken(){
        Object accessToken;
        do {
            String httpsGetUrl1 = httpsGetUrl.replaceAll("APPID", CommonAttributes.APPID).replaceAll("APPSECRET",CommonAttributes.APPSECRET);
            JSONObject jsonObject = WeChatHttpRequest.jsonObjectHttpRequest(httpsGetUrl1, "GET", null);
            accessToken = jsonObject.get("access_token");
        } while (accessToken==null);
        token = accessToken.toString();
//        System.out.println(token);
    }

    public String getToken(){
        return this.token;
    }

    /**
     * 根据网页授权获取信息
     * @param code 授权的code
     * @return
     */
    public JSONObject getAccessTokenByCode(String code){
        String codeHttpUrl1 = codeHttpUrl.replace("APPID", CommonAttributes.APPID).replace("SECRET", CommonAttributes.APPSECRET).replace("CODE", code);
        JSONObject request = WeChatHttpRequest.jsonObjectHttpRequest(codeHttpUrl1, "GET", null);
        return request;
    }

    /**
     * 刷新网页授权token
     * @param refreshToken
     * @return
     */
    public JSONObject refreshCodeToken(String refreshToken){
        String refreshCodeHttpUrl1 = refreshCodeHttpUrl.replace("APPID", CommonAttributes.APPID).replace("REFRESH_TOKEN", refreshToken);
        JSONObject request = WeChatHttpRequest.jsonObjectHttpRequest(refreshCodeHttpUrl1, "GET", null);
        return request;
    }

    /**
     * 获取网页授权token
     * @param code
     * @return
     */
    public Object getCodeToken(String code){
        return getAccessTokenByCode(code).get("access_token");
    }

    /**
     * 获取授权用户的openid
     * @param code
     * @return
     */
    public Object getCodeOpenId(String code){
        return getAccessTokenByCode(code).get("openid");
    }

    /**
     * 获取网页授权用用户的详细信息
     * @param code
     * @return
     */
    public JSONObject getUserMessage(String code){
        JSONObject jsonObject = getAccessTokenByCode(code);
        String getUserMessage1 = getUserMessage.replace("ACCESS_TOKEN", jsonObject.get("access_token").toString()).replace("OPENID", jsonObject.get("openid").toString());
        JSONObject request = WeChatHttpRequest.jsonObjectHttpRequest(getUserMessage1, "GET", null);
        return request;
    }

    /**
     * 验证网页授权token和openid有效性
     * @param access_token
     * @param openid
     * @return
     */
    public boolean checkCodeToken(String access_token, String openid) {
        String chekUrl1 = checkUrl.replace("ACCESS_TOKEN", access_token).replace("OPENID", openid);
        JSONObject jsonObject = WeChatHttpRequest.jsonObjectHttpRequest(chekUrl1, "GET", null);
        if (jsonObject.get("errcode").toString().equals("0") && jsonObject.get("errmsg").toString().equals("ok")) {
            return true;
        }
        return false;
    }

    /**
     * 获取ticket
     * @return
     */
    public String getJsapiTicket() {
        String chekUrl1 = jsapiTicketUrl.replace("ACCESS_TOKEN", getToken());
        JSONObject jsonObject = WeChatHttpRequest.jsonObjectHttpRequest(chekUrl1, "GET", null);
        return jsonObject.get("ticket").toString();
    }

    /**
     * 加密ticket
     */
    public void getShi1JsapiTicket() {

    }

}
