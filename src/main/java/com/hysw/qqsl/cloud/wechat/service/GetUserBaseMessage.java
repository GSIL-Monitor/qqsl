package com.hysw.qqsl.cloud.wechat.service;

import com.hysw.qqsl.cloud.wechat.util.WeChatHttpRequest;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 获取用户信息service类
 * Created by chenl on 17-7-4.
 */
@Service("getUserBaseMessage")
public class GetUserBaseMessage {
    @Autowired
    private GetAccessTokenService getAccessTokenService;

    private final String getUserBaseMessage = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";

    /**
     * 获取用户基本信息
     * @param openid 普通用户的标识，对当前公众号唯一
     * @return
     */
    public JSONObject getUserBaseMessage(String openid){
        String getUserBaseMessage1 = getUserBaseMessage.replace("ACCESS_TOKEN", getAccessTokenService.getToken()).replace("OPENID", openid);
        JSONObject jsonObject = WeChatHttpRequest.jsonObjectHttpRequest(getUserBaseMessage1, "GET", null);
        return jsonObject;
    }

    /**
     * 获取用户信息中的昵称
     * @param openid 普通用户的标识，对当前公众号唯一
     * @return
     */
    public String getNickname(String openid){
        JSONObject jsonObject = getUserBaseMessage(openid);
        Object nickname = jsonObject.get("nickname");
        if (nickname == null) {
            return null;
        }
        return nickname.toString();
    }
}
