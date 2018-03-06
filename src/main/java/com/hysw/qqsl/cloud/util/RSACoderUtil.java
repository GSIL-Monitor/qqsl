package com.hysw.qqsl.cloud.util;

import com.hysw.qqsl.cloud.CommonAttributes;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.hysw.qqsl.cloud.util.Coder.decryptBASE64;
import static com.hysw.qqsl.cloud.util.Coder.encryptBASE64;
import static com.hysw.qqsl.cloud.util.Coder.initMacKey;

/**
 * Created by chenl on 17-3-28.
 */
public class RSACoderUtil {

    /**
     * 用私钥对密文解密(对应C#)
     * @param ciphertext 密文
     */
    public String decryptByPrivateKey(String ciphertext,String privateKey){
        byte[] decodedData = new byte[0];
        try {
            byte[] bytes = decryptBASE64(ciphertext);
            decodedData = RSACoder.decryptByPrivateKey(bytes,
                    privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //与C#对应，除去byte中多余的0移位
        byte[] newDec = new byte[decodedData.length / 2];
        for (int i = 0; i < decodedData.length; i+=2) {
            newDec[i/2] = decodedData[i];
        }
        String original = new String(newDec);
        return original;
    }

    /**
     * 用公钥对明文进行加密(对应C#)
     * @param original 明文
     * @return
     */
    public String encryptByPublicKey(String original,String publicKey){
        byte[] data = original.getBytes();
        //与C#对应，添加相应的0移位
        byte[] data1 = new byte[data.length * 2];
        for (int i = 0; i < data1.length; i+=2) {
            data1[i]=data[i/2];
        }
        String ciphertext = null;
        try {
            byte[] encodedData = RSACoder.encryptByPublicKey(data1, publicKey);
            ciphertext = encryptBASE64(encodedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ciphertext;
    }


    /**
     * @author miracle.qu
     * @param data 明文
     * @param key 密钥，长度16
     * @param iv 偏移量，长度16
     * @return 密文
     */
    public static String encryptAES(String data,String key,String iv) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();
            byte[] dataBytes = data.getBytes();
            int plaintextLength = dataBytes.length;

            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }

            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);

            return Base64Helper.bytesToHexString(encrypted);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @author miracle.qu
     * @param data 密文
     * @param key 密钥，长度16
     * @param iv 偏移量，长度16
     * @return 明文
     */
    public static String decryptAES(String data,String key,String iv) throws Exception {
        try
        {
            byte[] encrypted1 = Base64Helper.hexStringToByte(data);

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original);
            return originalString.trim();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private static String key = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDKjq/X6VCH17HyTf/DxVSVLAJ0\n" +
            "A+Fb9r82BMHA/kQiRvr15m8IKTAOS8+YlI3PzCfwTCixZw+VncxTodHgOk/iBPDrGV79Acbm+lcA\n" +
            "w3SSwxJ1Lr0BN4t6ticuCiYb+B59XPApVQmdLRpkJhKrYehlk1ygt8wqTlFJvjWmAtWsl+YAkV5i\n" +
            "SdkSZDtrlfPwKBE8bJb6RICOSHaCoHadijs6KfK/x2+iK8KImOVq29SzzZ1jP8op+DUz8F54OHRH\n" +
            "qtsfnqLLQ35Wlzah79anOpU+06kV/dDxcGgMYv2FSNvazmaiSyX2/LC0bIXxC8rAyieYX62clwVZ\n" +
            "pAEnP8gWawYpAgMBAAECggEAKppIMclIvsmHC8eL+8kNOTLAcVY/l/AV1264sbbI7cywC/gjcjv3\n" +
            "d4pKZ3UXPpVZ8RiTlnxusrE1wIFQezDIjGktosT7TYuKIhqVIfv6EMQmpa7m4dKk63JUW0KXMVrG\n" +
            "Tue1O3w/QWezHU1Er/cvwe10lfkSX/OI9VGkRv5hoqMQVemQjvD9hCc6GiXaUxIIYVP3WdsFROqo\n" +
            "wdEgnmPYwDrsPifoJXVh2D3J10ykpgbRPJuVYqD0HpP6i/Ov3vX0fEJJxJdMTbxIkRiEHzCBe9+K\n" +
            "aNF+7z5avkjoIoQCG8EY5AlhBCleV1AEbxVCCLoDedGcY19XjAQDIup/mHc5gQKBgQDoXhG8jJc8\n" +
            "0h1csWezIgRUfe9NwQZtv2qZRcFgR/JF4fMAxS+jWjWwziNxj0jOGWaBN7kFDgZQ6ggRzOcJFiBs\n" +
            "UoiwgzPn+oagr2TujHmQzy5iwQpGQDfdGPdZH3WHwsXK3IAoGkUr2cNIdQc37v4t+3PV7/Si0SXH\n" +
            "j9k4Zs0kTQKBgQDfKHsN/XzkETzynHdFPtRVH24eUzMYTn8dVyJ/F0GmhcKGiXKvW3iIootnSFyS\n" +
            "qaRWf2IuyAd1dUpXEiq3qXvO49dIekKue/+H4ZWn7JvcPRwrSLIF/5JlRkJd6H9LxyXKxspzuvID\n" +
            "IgJRQxS/biyfDslB0Ktk2RrXjL83Oh0HTQKBgD55LCOZWQANLb/CIrNSrf9ZbIFnKCdRk4Np8bfF\n" +
            "ICoRuZe3bDtrAYxrn7PkZhqjUZ/kL2Zgguo2Qb0nbeZPgKAfAIkUfVwdSxzgiy5nrjd0vG/onq6M\n" +
            "jXhwQfBAOQUrJKqJ5hriFT7Y6VwNuscbBlOui1I1lXxA5vtkfW5glT7xAoGANZiHVWjYIf5x4OLr\n" +
            "UlowrPELhYT99IyFwwo2Yse3IMNnYOCKBsu1Ozuut3ONqDvOGGgnsIyiHfe5jwfx8oJmFd5qyiFy\n" +
            "+m0VhhEL3HTbSh3zMgIXn5EuG9yv+9XnHNALp5Pu87Smg1IbtGCkDwXq+ZiXhZMUVvg9lh9bnWv7\n" +
            "pNECgYAnrorJhlAXeC2wGU2AttS4K/zZssLq41OSGPpTXlZGizKy6UoXnXRDvFh2T1iuaQfbhtqX\n" +
            "QhbC9a+jEoZZtVwQ0SiPaJ74w4hgXAZb5kkuzvuf2TZh9Vrt3dWSK260hfLTnTZWseaXe7Kc5Lt3\n" +
            "c8YPtUWJX051zP6OJOQ8mmbf9g==";

    public static void main(String[] args){
        RSACoderUtil rsaCoderUtil = new RSACoderUtil();
        //sensorMap.put("code", "9930023");
        // sensorMap.put("ciphertext", "02D7145AFB1");
        String data = "9930023";
        String result = rsaCoderUtil.decryptByPrivateKey(data, key);
        System.out.print(result);
    }

}
