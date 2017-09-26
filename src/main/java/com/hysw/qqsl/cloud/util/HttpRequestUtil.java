package com.hysw.qqsl.cloud.util;

import com.aliyun.oss.common.utils.IOUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 页面请求
 * Created by chenl on 17-6-26.
 */
@Service("httpRequestUtil")
public class HttpRequestUtil {

    /**
     *
     * @param requestUrl
     * @param requestMethod
     * @param outputStr
     * @return
     */
    public static JSONArray jsonArrayHttpRequest(String requestUrl, String requestMethod, String outputStr) {
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

            String str = null;
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
            System.out.println("Application server connection timed out");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("http request error:{}");
        }finally{
            IOUtils.safeClose(inputStream);
            IOUtils.safeClose(outputStream);
        }
        return jsonArray;
    }



}
