package com.hysw.qqsl.cloud.pay.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.data.UserMessage;
import com.hysw.qqsl.cloud.core.service.UserMessageService;
import com.hysw.qqsl.cloud.core.service.UserService;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import com.hysw.qqsl.cloud.util.TradeUtil;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * @anthor Administrator
 * @since 16:11 2018/1/15
 */
public class CommonServiceTest extends BaseTest {
    @Autowired
    private CommonService commonService;
    @Autowired
    private UserService userService;
    @Autowired
    private TradeService tradeService;
    @Autowired
    private UserMessageService userMessageService;

    @Test
    public void testSendMessage(){
        Trade trade = new Trade();
        trade.setBuyType(Trade.BuyType.BUY);
        trade.setType(Trade.Type.PACKAGE);
        trade.setBaseType(Trade.BaseType.TEST);
        trade.setPayDate(new Date());
//        trade.setValidTime(1);
        trade.setInstanceId(TradeUtil.buildInstanceId());
        trade.setUser(userService.find(1l));
        JSONObject remarkJson = new JSONObject();
        Date expireDate = tradeService.getExpireDate(trade.getCreateDate(),"package");
        remarkJson.put("expireDate",expireDate.getTime());
        trade.setRemark(remarkJson.toString());
        commonService.sendMessage(trade);
        List<UserMessage> userMessages = userMessageService.findByUser(userService.find(1l));
        Assert.assertTrue(userMessages.size()!=0);
    }
}
