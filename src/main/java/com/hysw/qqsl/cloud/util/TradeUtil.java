package com.hysw.qqsl.cloud.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 生成订单号工具类
 */
//@Service("tradeUtil")
public class TradeUtil {
    /**
     * 生辰订单号
     * @return
     */
    public static String buildOutTradeNo() {
        long l = System.currentTimeMillis();
        return String.valueOf(l) + (int) ((Math.random() * 9 + 1) * ((int) (Math.pow(10, (18 - String.valueOf(l).length() - 1)))));
    }

    public static String buildInstanceId(){
        UUID uuid = UUID.randomUUID();
        return DigestUtils.md5Hex(uuid.toString()).substring(0,8);
    }

}
