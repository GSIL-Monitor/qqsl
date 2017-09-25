package com.hysw.qqsl.cloud.wechat.service;

import com.hysw.qqsl.cloud.wechat.util.WeChatHttpRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 素材管理
 * Created by chenl on 17-6-30.
 */
@Service("uploadFodderService")
public class UploadFodderService {
    @Autowired
    private GetAccessTokenService getAccessTokenService;
    private final String transientFodder = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE";

    private final String newsFodder = "https://api.weixin.qq.com/cgi-bin/material/add_news?access_token=ACCESS_TOKEN";

    private final String inNewsFodder = "https://api.weixin.qq.com/cgi-bin/media/uploadimg?access_token=ACCESS_TOKEN";
//    媒体文件类型，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb）
    private final String otherFodder = "https://api.weixin.qq.com/cgi-bin/material/add_material?access_token=ACCESS_TOKEN&type=TYPE";

    private final String getFodder = "https://api.weixin.qq.com/cgi-bin/material/get_material?access_token=ACCESS_TOKEN";

    private final String getFodderList = "https://api.weixin.qq.com/cgi-bin/material/batchget_material?access_token=ACCESS_TOKEN";

    private final String getFodderCount = "https://api.weixin.qq.com/cgi-bin/material/get_materialcount?access_token=ACCESS_TOKEN";
    /**
     * 上传临时素材
     * @param type 上传媒体文件类型，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb）
     * @param filePath
     * @return
     * @throws IOException
     */
//    public String uploadTransientFodder(String type, String filePath) throws IOException {
//        String transientFodder1 = transientFodder.replace("ACCESS_TOKEN", getAccessTokenService.getToken()).replace("TYPE",type);
//        String send = WeChatHttpRequest.send(transientFodder1, filePath);
//        return send;
//    }

    /**
     * 上传永久素材
     * @param type 上传媒体文件类型，分别有图片（image）、语音（voice）、视频（video）和缩略图（thumb）
     * @param filePath
     * @return
     * @throws IOException
     */
    public String uploadFodder(String type, String filePath) throws IOException {
        String otherFodder1 = otherFodder.replace("ACCESS_TOKEN", getAccessTokenService.getToken()).replace("TYPE",type);
        String send = WeChatHttpRequest.send(otherFodder1, filePath);
        return send;
    }

    /**
     * 获取永久素材
     */
    public void getFodder(){
        String getFodder1 = getFodder.replace("ACCESS_TOKEN","xofrZqlHc8L2sKRllkLA2t9ldxuDF_45nL-mn2A8FCKl8vM9LdVimqOQZl78MQ89W5cRYDfYrmJzXtafLmgDPn4E3y6MqWn1lg4WpBxs7m4l7oAcBYXx_mdR0zeN4eMGKKNjCBAEEH");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("media_id", "WP5b4q5KGmn4bRJN5HRPVUdKZJLrra_ZU_0Y3VBWgXQ");
        JSONObject request = WeChatHttpRequest.jsonObjectHttpRequest(getFodder1, "POST", jsonObject.toString());
        System.out.println(request);
    }
    /**
     * 获取永久素材列表
     */
    public JSONObject getFodderList(String type,int offset,int count){
        String getFodderList1=getFodderList.replace("ACCESS_TOKEN", getAccessTokenService.getToken());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("offset",offset);
        jsonObject.put("count",count);
        JSONObject request = WeChatHttpRequest.jsonObjectHttpRequest(getFodderList1, "POST", jsonObject.toString());
        return request;
    }

    public void getFodderCount(){
        String getFodderCount1=getFodderCount.replace("ACCESS_TOKEN", "rmXDIsDLAyL7LO8ZmwS1AUXks5_mvuyGIcughSGNhEQCv3tf9O5Jdo3oIq5CzDi3c99Q7WksIiF5enndgkbtOgqaAqNUwQcDIZX1-JlJlCmeEda6UQ2In25ulZPtJgelPCZlCAAHUR");
        JSONObject request = WeChatHttpRequest.jsonObjectHttpRequest(getFodderCount1, "GET", null);
        System.out.println(request);
    }
}
