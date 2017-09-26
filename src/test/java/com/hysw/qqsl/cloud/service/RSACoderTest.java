package com.hysw.qqsl.cloud.service;


import static com.hysw.qqsl.cloud.util.Coder.decryptBASE64;
import static com.hysw.qqsl.cloud.util.Coder.encryptBASE64;
import static org.junit.Assert.*;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.util.HttpRequestUtil;
import com.hysw.qqsl.cloud.util.RSACoder;
import com.hysw.qqsl.cloud.util.RSACoderUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.hssf.usermodel.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by chenl on 17-3-23.
 */
public class RSACoderTest {
    private String publicKey;
    private String privateKey;
    @Autowired
    private MonitorService monitorService;

    @Before
    public void setUp() throws Exception {
        Map<String, Object> keyMap = RSACoder.initKey();

        publicKey = CommonAttributes.publicKeyApplication;
        privateKey = CommonAttributes.privateKeyApplication;
//        System.err.println("公钥: \n\r" + publicKey);
//        System.err.println("私钥： \n\r" + privateKey);
    }

    @Test
    public void test() throws Exception {
        System.err.println("公钥加密——私钥解密");
        String inputStr = "123";
        byte[] data = inputStr.getBytes();
        //与C#对应，添加相应的0移位
        byte[] data1 = new byte[data.length * 2];
        for (int i = 0; i < data1.length; i+=2) {
            data1[i]=data[i/2];
        }
//        List<Byte> list1 = new ArrayList<>();
//        for (int i = 0; i < data.length; i++) {
//            list1.add(data[i]);
//            list1.add((byte) 0);
//        }
//        for (int i = 0; i < data1.length; i++) {
//            data1[i] = list1.get(i);
//        }
        byte[] encodedData = RSACoder.encryptByPublicKey(data1, publicKey);
        String s = encryptBASE64(encodedData);
        System.err.println(s);
        byte[] bytes = decryptBASE64(s);
        byte[] decodedData = RSACoder.decryptByPrivateKey(bytes,
                privateKey);
        //与C#对应，除去byte中多余的0移位
        byte[] newDec = new byte[decodedData.length / 2];
        for (int i = 0; i < decodedData.length; i+=2) {
            newDec[i/2] = decodedData[i];
        }
//        List<Byte> list = new ArrayList<>();
//        for (int i = 0; i < decodedData.length; i+=2) {
//            list.add(decodedData[i]);
//        }
//        byte[] newDec = new byte[list.size()];
//        for (int i = 0; i <newDec.length ; i++) {
//            newDec[i] = list.get(i);
//        }
        String outputStr = new String(newDec);
        System.err.println("加密前: " + inputStr + "\n\r" + "解密后: " + outputStr);
//        assertEquals(inputStr, outputStr);

    }

    @Test
    public void testSign() throws Exception {
        System.err.println("私钥加密——公钥解密");
        String inputStr = "123";
        byte[] data = inputStr.getBytes();

        byte[] encodedData = RSACoder.encryptByPrivateKey(data, privateKey);

        String s = encryptBASE64(encodedData);
        System.err.println(s);
        byte[] bytes = decryptBASE64(s);
        byte[] decodedData = RSACoder
                .decryptByPublicKey(bytes, publicKey);

        String outputStr = new String(decodedData);
        System.err.println("加密前: " + inputStr + "\n\r" + "解密后: " + outputStr);
        assertEquals(inputStr, outputStr);

        System.err.println("私钥签名——公钥验证签名");
        // 产生签名
        String sign = RSACoder.sign(encodedData, privateKey);
        System.err.println("签名:\r" + sign);

        // 验证签名
        boolean status = RSACoder.verify(encodedData, publicKey, sign);
        System.err.println("状态:\r" + status);
        assertTrue(status);

    }

    /**
     * 测试获取仪器详细数据
     */
    @Test
    public void testHttprRequrest(){
        String url="http://localhost:8080/qqsl//monitor/token";
        String token = "1";
        String method = "1";
        String beginDate = "2017-3-19";
        String endDate = "2017-3-20";
        JSONArray jsonArray = HttpRequestUtil.jsonArrayHttpRequest(url,"GET", null);
        System.out.println(jsonArray);
    }

    /**
     * 测试获取仪器列表
     */
    @Test
    public void testHttpRequrestList(){
        String url="http://localhost:8080/qqsl/field/heartBeat";
        String token = "1";
        JSONObject obj = new JSONObject();
        obj.element("app_name", "asdf");
        obj.element("app_ip", "10.21.243.234");
        obj.element("app_port", 8080);
        obj.element("app_type", "001");
        obj.element("app_area", "asd");
        JSONArray jsonArray = HttpRequestUtil.jsonArrayHttpRequest(url, "POST", "{\"app_name\":\"asdf\",\"app_ip\":\"10.21.243.234\"}");
        System.out.println(jsonArray);
    }


    @Test
    public void testAES() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");//密钥生成器
        keyGen.init(128); //默认128，获得无政策权限后可为192或256
        SecretKey secretKey = keyGen.generateKey();//生成密钥
        byte[] key = secretKey.getEncoded();
        System.out.println(encryptBASE64(key)+":"+encryptBASE64(key).length());//密钥字节数组
        System.out.println(encryptBASE64(key).substring(0,16));
    }


    public String jiami(String key,String data) throws Exception {
        byte[] key1 = decryptBASE64(key);
        byte[] data1 = data.getBytes();
        SecretKey secretKey = new SecretKeySpec(key1, "AES");//恢复密钥
        Cipher cipher = Cipher.getInstance("AES");//Cipher完成加密或解密工作类
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);//对Cipher初始化，解密模式
        byte[] cipherByte = cipher.doFinal(data1);//加密data
        return encryptBASE64(cipherByte);

    }

    public String jiemi(String key,String data) throws Exception {
        byte[] key1 = decryptBASE64(key);
        byte[] data1 = decryptBASE64(data);
        SecretKey secretKey = new SecretKeySpec(key1, "AES");//恢复密钥
        Cipher cipher = Cipher.getInstance("AES");//Cipher完成加密或解密工作类
        cipher.init(Cipher.DECRYPT_MODE, secretKey);//对Cipher初始化，解密模式
        byte[] cipherByte = cipher.doFinal(data1);//解密data
        return new String(cipherByte);
    }

    @Test
    public void testJiaMiJieMi() throws Exception {
        String key = "KWyTISNRNvdfcsFsYb8hIA==";
        String data = "FFFFFFFFFFFFFFF";
        String jiami = jiami(key, data);
        System.out.println(jiami+":"+jiami.length());
        String jiemi = jiemi(key, jiami);
        System.out.println(jiemi);
    }

    @Test
    public void testAES111() throws Exception {
        String data = String.valueOf(System.currentTimeMillis());
        String appliactionKey=CommonAttributes.tokenKey;
        String appliactioniv = CommonAttributes.tokenIv;
        System.out.println(data);
        String s = RSACoderUtil.encryptAES(data, appliactionKey, appliactioniv);
        System.out.println(s+":"+s.length());
        StringBuffer bbb = new StringBuffer("");
        for (int i = 0; i < s.length(); i+=3) {
            bbb.append(s.substring(i,i+1));
        }
        System.out.println(bbb.toString());
        String ccc = "fXJEWbx=";
        if (assertjiami(bbb, ccc)) {
            System.out.println("激活成功");
        }else{
            System.out.println("激活失败");
        }
        String s1 = RSACoderUtil.decryptAES(s, appliactionKey, appliactioniv);
        System.out.println(s1);
        System.out.println(new Date(1492252202234l));
    }

    private boolean assertjiami(StringBuffer bbb,String ccc){
        if (bbb.toString().equals(ccc)) {
            return true;
        }else{
            return false;
        }
    }



}
