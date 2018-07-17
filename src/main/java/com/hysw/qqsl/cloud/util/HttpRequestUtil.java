package com.hysw.qqsl.cloud.util;

import com.aliyun.oss.common.utils.IOUtils;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.data.Note;
import com.hysw.qqsl.cloud.core.service.NoteCache;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 页面请求
 * Created by chenl on 17-6-26.
 */
@Service("httpRequestUtil")
public class HttpRequestUtil {
    @Autowired
    private NoteCache noteCache;
    private long sendTime=0;

    /**
     *
     * @param requestUrl
     * @param requestMethod
     * @param outputStr
     * @return
     */
    public JSONArray jsonArrayHttpRequest(String requestUrl, String requestMethod, String outputStr) {
        JSONArray jsonArray = null;
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
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            jsonArray = JSONArray.fromObject(buffer.toString());
        } catch (ConnectException ce) {
            ce.printStackTrace();
            if (sendTime+3600*1000l < System.currentTimeMillis()) {
                Note note = new Note(SettingUtils.getInstance().getSetting().getNotice(), "异常：监测子系统");
                noteCache.add(SettingUtils.getInstance().getSetting().getNotice(),note);
                sendTime = System.currentTimeMillis();
            }
            System.out.println("链接超时");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("http request error:{}");
        }finally{
            IOUtils.safeClose(inputStream);
            IOUtils.safeClose(outputStream);
        }
        return jsonArray;
    }

    /**
     * 发送请求获取身份证图片上的信息
     * @param host
     * @param path
     * @param bodys
     */
    public JSONObject getIdMessage(String host, String path, String bodys) {
        if (bodys.equals("")) {
            return null;
        }
        String method = "POST";
        String appcode = CommonAttributes.APPCODE;
        Map<String, String> headers = new HashMap<>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/json; charset=UTF-8");
        Map<String, String> querys = new HashMap<>();
        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
//            获取response的body
            String resp=EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = JSONObject.fromObject(resp);
            return jsonObject;
        } catch (Exception e) {
            Note note = new Note("18661925010","图形识别监测");
            noteCache.add("18661925010",note);
            return null;
        }
    }

    /**
     * 发送请求获取名字与身份证号是否一致
     * @return false 一致  true 不一致，notPass
     * @param path
     */
    public JSONObject nameAndIdIsSame(String host, String path, Map<String,String> querys){
        String method = "GET";
        String appcode = CommonAttributes.APPCODE;
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        try {
            HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
            //获取response的body
            String resp=EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = JSONObject.fromObject(resp);
            return jsonObject;
        } catch (Exception e) {
            Note note = new Note("18661925010","认证监测");
            noteCache.add("18661925010",note);
            return null;
        }
    }

}
