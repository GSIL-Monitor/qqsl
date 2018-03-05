package com.hysw.qqsl.cloud.pay.service.aliPay;


import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create by leinuo on 17-9-13 下午6:06
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
public class AliPayServiceTest extends BaseTest{

    @Autowired
    private AliPayService aliPayService;
    @Test
    public void qureyTrade() throws Exception{
        Trade trade = new Trade();
        trade.setOutTradeNo("6823789339921212121");
        Message message = aliPayService.queryTrade(trade);
    }
}