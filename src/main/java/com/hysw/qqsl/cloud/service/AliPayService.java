package com.hysw.qqsl.cloud.service;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.hysw.qqsl.cloud.CommonAttributes;
import org.springframework.stereotype.Service;

/**
 * Create by leinuo on 17-7-17 下午5:01
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 *
 * 支付宝业务层
 */
@Service("aliPayService")
public class AliPayService {

    private AlipayClient alipayClient;
    public AliPayService(){
        alipayClient = new DefaultAlipayClient(CommonAttributes.OPEN_API, CommonAttributes.APP_ID, CommonAttributes.APP_PRIVATE_KEY,
                CommonAttributes.RESULT_TYPE, CommonAttributes.CHARSET, CommonAttributes.ALIPAY_PUBLIC_KEY, CommonAttributes.SIGN_TYPE);
    }



}
