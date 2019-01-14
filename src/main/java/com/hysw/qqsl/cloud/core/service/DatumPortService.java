package com.hysw.qqsl.cloud.core.service;

import com.aliyun.oss.common.utils.IOUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

/**
 * @author Administrator
 * @since 2019/1/14
 */
@Service("datumPortService")
public class DatumPortService implements Serializable {

    public void datumPort(){
        String requestUrl = "http://202.100.92.240:8088/flow";
        String requestMethod = "POST";
        String outputStr = "";
        Double a, b, c, d;
        a = new BigDecimal(0.7 + random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        b = new BigDecimal(2.8 + random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        c = new BigDecimal(0.5 + random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        d = new BigDecimal(2.4 + random()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", 0);
        jsonObject.put("station_code", "621224000009");
        jsonObject.put("value", a);
        jsonObject.put("time", String.valueOf(System.currentTimeMillis()));
        jsonArray.add(jsonObject);
        jsonObject = new JSONObject();
        jsonObject.put("type", 1);
        jsonObject.put("station_code", "621224000009");
        jsonObject.put("value", b);
        jsonObject.put("time", String.valueOf(System.currentTimeMillis()));
        jsonArray.add(jsonObject);
        jsonObject = new JSONObject();
        jsonObject.put("type", 0);
        jsonObject.put("station_code", "621224000014");
        jsonObject.put("value", c);
        jsonObject.put("time", String.valueOf(System.currentTimeMillis()));
        jsonArray.add(jsonObject);
        jsonObject = new JSONObject();
        jsonObject.put("type", 1);
        jsonObject.put("station_code", "621224000014");
        jsonObject.put("value", d);
        jsonObject.put("time", String.valueOf(System.currentTimeMillis()));
        jsonArray.add(jsonObject);
        outputStr = jsonArray.toString();
        System.out.println(outputStr);
        JSONObject jsonObject1 = jsonObjectHttpRequest(requestUrl, requestMethod, outputStr);
        System.out.println(jsonObject1);
    }

    public double random(){
        Random rand = new Random();
        int i = rand.nextInt(10);
        if (i % 2 == 0) {
            return new BigDecimal(rand.nextDouble() / 10).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        } else {
            return Double.parseDouble("-" + new BigDecimal(rand.nextDouble() / 10).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        }
    }

    public static void main(String[] args) {
        DatumPortService datumPortService = new DatumPortService();
        datumPortService.datumPort();
    }

    public static JSONObject jsonObjectHttpRequest(String requestUrl, String requestMethod, String outputStr) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        InputStream inputStream = null;
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
            if ("POST".equalsIgnoreCase(requestMethod)) {
                httpUrlConn.setInstanceFollowRedirects(true);
                httpUrlConn.setRequestProperty("Content-Type",
                        "application/json;charset=utf-8");
                httpUrlConn.setRequestProperty("Token","8096cde9-a845-48fa-9a0a-a577e77e9e69");
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
            if (httpUrlConn.getResponseCode() == 500) {
                inputStream = httpUrlConn.getErrorStream();
            } else {
                inputStream = httpUrlConn.getInputStream();
            }
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
            jsonObject = JSONObject.fromObject(buffer.toString());
        } catch (ConnectException ce) {
            ce.printStackTrace();
            System.out.println("WeChat server connection timed out");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("http request error:{}");
        } finally {
            IOUtils.safeClose(inputStream);
            IOUtils.safeClose(outputStream);
        }
        return jsonObject;
    }


}
