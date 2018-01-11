package com.hysw.qqsl.cloud.util;

import com.hysw.qqsl.cloud.CommonAttributes;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

/**
 * RSA安全编码组件
 * Created by chenl on 17-3-23.
 */
public abstract class RSACoder extends Coder {
    public static final String KEY_ALGORITHM = "RSA";
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    private static final String PUBLIC_KEY = "RSAPublicKey";
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * 用私钥对信息生成数字签名
     *
     * @param data       加密数据
     * @param privateKey 私钥
     * @return
     * @throws Exception
     */
    public static String sign(byte[] data, String privateKey) throws Exception {
        // 解密由base64编码的私钥
        byte[] keyBytes = decryptBASE64(privateKey);

        // 构造PKCS8EncodedKeySpec对象
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);

        // KEY_ALGORITHM 指定的加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

        // 取私钥匙对象
        PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);

        // 用私钥对信息生成数字签名
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(priKey);
        signature.update(data);

        return encryptBASE64(signature.sign());
    }

    /**
     * 校验数字签名
     *
     * @param data      加密数据
     * @param publicKey 公钥
     * @param sign      数字签名
     * @return 校验成功返回true 失败返回false
     * @throws Exception
     */
    public static boolean verify(byte[] data, String publicKey, String sign)
            throws Exception {

        // 解密由base64编码的公钥
        byte[] keyBytes = decryptBASE64(publicKey);

        // 构造X509EncodedKeySpec对象
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);

        // KEY_ALGORITHM 指定的加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);

        // 取公钥匙对象
        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(pubKey);
        signature.update(data);

        // 验证签名是否正常
        return signature.verify(decryptBASE64(sign));
    }

    /**
     * 解密<br>
     * 用私钥解密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] data, String key)
            throws Exception {
        // 对密钥解密
        byte[] keyBytes = decryptBASE64(key);

        // 取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

        // 对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(data);
    }

    /**
     * 解密<br>
     * 用公钥解密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPublicKey(byte[] data, String key)
            throws Exception {
        // 对密钥解密
        byte[] keyBytes = decryptBASE64(key);

        // 取得公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicKey = keyFactory.generatePublic(x509KeySpec);

        // 对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        return cipher.doFinal(data);
    }

    /**
     * 加密<br>
     * 用公钥加密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] data, String key)
            throws Exception {
        // 对公钥解密
        byte[] keyBytes = decryptBASE64(key);

        // 取得公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicKey = keyFactory.generatePublic(x509KeySpec);

        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(data);
    }

    /**
     * 加密<br>
     * 用私钥加密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPrivateKey(byte[] data, String key)
            throws Exception {
        // 对密钥解密
        byte[] keyBytes = decryptBASE64(key);

        // 取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        return cipher.doFinal(data);
    }

    /**
     * 取得私钥
     *
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static String getPrivateKey(Map<String, Object> keyMap)
            throws Exception {
        Key key = (Key) keyMap.get(PRIVATE_KEY);

        return encryptBASE64(key.getEncoded());
    }

    /**
     * 取得公钥
     *
     * @param keyMap
     * @return
     * @throws Exception
     */
    public static String getPublicKey(Map<String, Object> keyMap)
            throws Exception {
        Key key = (Key) keyMap.get(PUBLIC_KEY);

        return encryptBASE64(key.getEncoded());
    }

    /**
     * 初始化密钥
     *
     * @return
     * @throws Exception
     */
    public static Map<String, Object> initKey() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator
                .getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(2048);

        KeyPair keyPair = keyPairGen.generateKeyPair();

        // 公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        // 私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        Map<String, Object> keyMap = new HashMap<String, Object>(2);
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }

    private static String privateKey = "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDYsDx1BHce216z9Xrj2QxtJdASP4V+8LnfZR7B8S2H/un+pstspajQy7cNtZCvvGBaPsAoPmu/raXBjVt4kN+IqsrZxxw3Ew+D92pBXeCgA+phFJpPbpkqVPnqb4bUclXb5Chk2GEpMoFNvQ59LmxglKB04WtXr2RjZa7HcMzJR64pgZufXhtQC3vgK7Y0MEvbeZan0E0qX2A4xQQNOd3twivcD80Z6uP223Wq/ezxUHFsewPGcO+gMZcLvKPTOIbxwnqBJHWEj7yA4DDHXgfoBLtoJsTpTo989WBxikZa0HPZsR1QdW5Wc/vhdmTq+/VZbo4XLIxB5DEysBoKlgpjAgMBAAECggEBALhjf46z9gmz8qqAQcqRcBmRC7X7h8W1YvXOPpYafPw01zlzBbe2YCkMsQBheHWSfwtMSof0VgwN3/cMKsI1xYGulF8tJ71V4uYRK8Hj+Lkxm8hwl77wBjYXiXMM5Fbz1EwBor3twwAjkhRsxF+RmXe2AbRz/bvn7C0I4emk+x5q52znjuFKYG043sRoveFl0cU6QY+uQZoMNxCxG2RfrnRTIZdOCMZa7TMyIR5TBfLmo4d9dZtJseJMcPzOf7l1v2eVqkd5pfXbgt3mH0H9+taw6qcIIHTHZSLdT3EbZ7pQX2ftMWqC2rabkOl13NubhzV6cXNwwBrFwq8foh0764ECgYEA9qAbqGoaCWuZehF4arOdXu+j6WU9/ntPMDYVyiuEAhEWaAYjPVQ4R5ima8z5r2AqIiNFZp2mJYcf7wdaTAgSMFdjsOMPema58WU0r5MVOjb4uYJE33ehpL81oOoQZJHbrarhPHbEsJ93ryXoGZR1fa3scLgvC2RVPAyiM95LDIkCgYEA4OzQSLu96Sry2gxcixj/GTuLBfmtEq0XFiJzF0NOud/SaGH9TqQm70xmOnNQyh4cQTFVpnJtFXkb2bLJZ7tVYJE9UudBBjWWqYlJBm/TcxK24EJpvTWPRbHzE75dYXJTJVUOjy2xvs9N8D1Xsa4JJ1nT1F/TJRO8SUKKiIFtXIsCgYEAr+mAEN6td66yKGmxtJotU/wmtGGsmIon78GERVJItJivnL9T/3jFM/xkKFxqdHdpVjRGWm10hywc0QgzlivjnWuYBiHPdoUzeEWyENIewAlZ3ChZK/RO7g4dGIwb6UBIq1VJlzE4FquIWPv209ga/exCzBv0InIMgeqY/9o9OdECgYEAv2JBR+IXkdLtmQNzqxe11+6GOKtdzMpjHn5pl5252wbLre3S75tVVcCYK5v7Xj+IoyArVNmAYAkQ3Yf3P8BywRxUJbqH5ZSrgc5CyKtWLE/8M43PEyQDqLXlsyMDqerxPI7HOKUU3Nrf4k8P0yvWthkE2Ww/6QiF6YApCEW4nEkCgYEAwPkhhYR3HL0ekzOz5IzlWgUxWf5iHftjw3lSdr5MDdasq+N/U6zYZGHcUBNCsqpCYAX0UYUJ4rNR+psj2CEuLRyqgBiBBXJexCA/kvjqNCfrEUbNjAN0UPN5HP6Fs5PCk3CgX6nfVivcJ822PPHEBcktsRioMuVbnuddtGw3pyw=";

    private static String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2LA8dQR3Httes/V649kMbSXQEj+FfvC532UewfEth/7p/qbLbKWo0Mu3DbWQr7xgWj7AKD5rv62lwY1beJDfiKrK2cccNxMPg/dqQV3goAPqYRSaT26ZKlT56m+G1HJV2+QoZNhhKTKBTb0OfS5sYJSgdOFrV69kY2Wux3DMyUeuKYGbn14bUAt74Cu2NDBL23mWp9BNKl9gOMUEDTnd7cIr3A/NGerj9tt1qv3s8VBxbHsDxnDvoDGXC7yj0ziG8cJ6gSR1hI+8gOAwx14H6AS7aCbE6U6PfPVgcYpGWtBz2bEdUHVuVnP74XZk6vv1WW6OFyyMQeQxMrAaCpYKYwIDAQAB";
    public static void main(String[] args) throws Exception{
       /* Map<String, Object> keyMap = initKey();
        System.out.print("公钥:"+getPublicKey(keyMap));
        System.out.print("私钥:"+getPrivateKey(keyMap));*/
        RSACoderUtil rsaCoderUtil = new RSACoderUtil();
        String data = "12313123122";
        byte[] bytes = data.getBytes();
        String result = sign(bytes,privateKey);
        System.out.print(result);
    }

}

