package com.hysw.qqsl.cloud.pay.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import com.hysw.qqsl.cloud.pay.entity.data.Turnover;
import com.hysw.qqsl.cloud.util.TradeUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * 流水测试
 *
 * @author chenl
 * @create 2017-08-22 上午10:11
 */
public class TurnoverServiceTest extends BaseTest {
    @Autowired
    private TurnoverService turnoverService;

//    public void writeTheFirstItem(){
//        Turnover turnover = new Turnover();
//    }

    @Test
    @Ignore
    public void testFindByLastItem(){
        Turnover item = turnoverService.findByLastItem();
        Assert.assertTrue(item != null);
    }

    @Test
    public void test(){
        long l = System.currentTimeMillis();
        l = l - 7 * 3600*1000l;
        Message message = turnoverService.getTurnoverListBetweenDate(new Date(l), new Date());
        System.out.println(message.toString());
    }

    @Test
    public void testWriteTurnover(){
        Trade trade = new Trade();
        trade.setStatus(Trade.Status.PAY);
        trade.setPayDate(new Date());
        trade.setOutTradeNo(TradeUtil.buildOutTradeNo());
        trade.setPrice(200l);
        turnoverService.writeTurnover(trade);
        turnoverService.flush();
        Turnover turnover = turnoverService.findByLastItem();
        Assert.assertTrue(turnover!=null&&turnover.getBalance()==200);
    }
}
