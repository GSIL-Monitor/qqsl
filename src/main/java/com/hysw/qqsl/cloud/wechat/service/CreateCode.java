package com.hysw.qqsl.cloud.wechat.service;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.wechat.util.WeChatHttpRequest;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

/**
 * 创建二维码
 * Created by chenl on 17-7-3.
 */
@Service("createCode")
public class CreateCode {
    @Autowired
    private GetAccessTokenService getAccessTokenService;

    private final String createCode = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=ACCESS_TOKEN";
    private final String downloadCode = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=TICKET";
    public void createCode() throws UnsupportedEncodingException {
        String createCode1 = createCode.replace("ACCESS_TOKEN", getAccessTokenService.getToken());
        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("expire_seconds", 1800);
        jsonObject.put("action_name", "QR_LIMIT_SCENE");
        JSONObject jsonObject1 = new JSONObject();
        Random random = new Random();
        jsonObject1.put("scene_id", random.nextInt(99999) + 1);
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("scene", jsonObject1);
        jsonObject.put("action_info", jsonObject2);
        System.out.println(jsonObject);
        JSONObject request = WeChatHttpRequest.jsonObjectHttpRequest(createCode1, "POST", jsonObject.toString());
        String ticket = request.get("ticket").toString();
        String downloadCode1 = downloadCode.replace("TICKET", URLEncoder.encode(ticket, "utf-8"));
        JSONObject request1 = jsonObjectHttpRequest(downloadCode1, "GET", null);
    }

    public static JSONObject jsonObjectHttpRequest(String requestUrl, String requestMethod, String outputStr) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        InputStream inputStream=null;
        OutputStream outputStream = null;
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();
            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            // 设置请求方式（GET/POST）
            httpUrlConn.setRequestMethod(requestMethod);
            if ("GET".equalsIgnoreCase(requestMethod))
                httpUrlConn.connect();
            if("POST".equalsIgnoreCase(requestMethod)){
                httpUrlConn.setInstanceFollowRedirects(true);
                httpUrlConn.setRequestProperty("Content-Type",
                        "application/json");
                httpUrlConn.setRequestMethod(requestMethod);
                httpUrlConn.connect();
            }
            // 当有数据需要提交时
            if (null != outputStr) {
                outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }
            //将返回的输入流转换成字符串
            inputStream = httpUrlConn.getInputStream();
            File file = new File("345.jpg");
            FileOutputStream output = new FileOutputStream(file);
            byte b[] = new byte[1024];
            while (true) {
                int length = inputStream.read(b);
                if (length == -1) {
                    break;
                }
                output.write(b, 0, length);
            }
        } catch (ConnectException ce) {
            ce.printStackTrace();
            System.out.println("WeChat server connection timed out");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("http request error:{}");
        }finally{
            IOUtils.safeClose(inputStream);
            IOUtils.safeClose(outputStream);
        }
        return jsonObject;
    }
}
