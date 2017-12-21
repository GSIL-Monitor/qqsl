package com.hysw.qqsl.cloud.pay.service.wxPay;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import com.hysw.qqsl.cloud.pay.service.TradeService;
import com.hysw.qqsl.cloud.util.TradeUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static com.hysw.qqsl.cloud.util.Coder.decryptBASE64;

public class WXPayServiceTest extends BaseTest{
    @Autowired
    private WXPayService wxPayService;
    @Autowired
    private TradeService tradeService;

    @Test
    public void testUnifiedOrderPay(){
        Trade trade = new Trade();
        trade.setBuyType(Trade.BuyType.BUY);
//        trade.setContent("...");
//        trade.setName("测试支付");
        trade.setOutTradeNo(TradeUtil.buildOutTradeNo());
//        trade.setPayDate();
//        trade.setPayType();
        trade.setPrice(160000);
//        trade.setRemark("测试支付");
//        trade.setServeId();
        trade.setBaseType(Trade.BaseType.TEST);
        trade.setStatus(Trade.Status.NOPAY);
//        trade.setUser();
//        tradeService.save(trade);
        System.out.println(trade.getOutTradeNo());
        System.out.println(wxPayService.unifiedOrderPay(trade));
    }

    @Test
    public void testorderClose(){
        Trade trade = tradeService.findAll().get(0);
        wxPayService.orderClose(trade);
    }

    @Test
    public void testorderQuery(){
        Trade trade = new Trade();
        trade.setOutTradeNo("15045745989766212");
        wxPayService.orderQuery(trade);
    }

    @Test
    public void testrefund(){
        Trade trade = new Trade();
        trade.setOutTradeNo("150457459897662126");
        trade.setType(Trade.Type.GOODS);
        trade.setPrice(160000);
        wxPayService.refund(trade);
    }

    @Test
    public void testOutTradeNo(){
        String s = TradeUtil.buildOutTradeNo();
        System.out.println(s);
        System.out.println(s.length());
    }


    @Test
    public void testtuikuanjiemi() throws Exception {
        String jiamimessage = "OYAkpijaBh4EaJCzS/VAKh/LtALNTUPxQuXRZAmkPYjkdRQvEnsmar36CZYudX8TGItdQXU3mX4ZxN6OoIUjinuoXjCyo2s7DHE7VFRB6G7j3poy5q2YpA5bE9LfzxhnqJpmJxtzPGcntOKAi+RbFuqMlPML+GoZQpQaNtOefxIEPxDGNq0QuxuWkaw10hux6p6vduRJbHl8OBF2tj0M53UqC4nAyQjBAT7F2r4Q2q1VnkXSm52UhRN5EHw5PZNEsbS1/sfU0dncBRi7PBlwi0r32qvFss/78TeDu0PaIN7u8N07RMyrHg2VHaz9ymVf4FgQo+UFjySR9YZLuWfE9A997rz5F3LAd7/UXT4eFFMwllkn7KTLosANp7nSCRhhwvakyF/pHm5cyuGp0Jsdp+lcT/y/bRgaofLEDcjZFWx20afuWiJoMi7X+53i5LLtlt3xoMLQv6k5Xglkc4hoEipNeddWg/X2KWwhQLB1wr1WZ4n5OQkO2o+sZm2tllkL2HRjUn2JXRMPVdp5b4QIi890ZlclDseXTBDrt6vFhZmGfOL9LdDYvpFvbQSWRi+bdSG0PzW1BeDiB8hD2swacjlvz5VKFvreOL+GbrxB0hQIrgx8IBQy+0RLmPneCafrieUV6C7OXmAJ7LGnidot5dtwnM74pPi9NYr1noy+tecOdTaj/1n4I8GJCT6e85SquXCKEYm+ctm1FHpL+naudMt+vaDIPY74q4RHuoTziOwFLFn35QEhEyalbyh+JXlfTUoQfCxzHveSGfD7M1iwTSDcmNFXNZSC30R9110oZ1XhI5UTkdkhob0KnfMPXVHzFMPoJnngNeRqvqft2I1rQL7vEFo1jvV9wQ+oH0Fu1rgyBbHYs13TjbpfOrYkNagkDKnPkEjtzB5hzzV/xZd5EfFZD+2gmmqt6gnT5b4rjO8WdxeupcA1WE2VLSQSlc6h9FtpmgOzDaVhCymJb2yBhQ/5mkRBVokJSFNNAQu8K2C41QO+iJYi/FbLPDYm6Z32Wxy8/sAec2Bp3AepgwWhNoqxzni0/9grmlLFHDhHJm0=";
        Map<String, String> decryptRefundTrade = tradeService.decryptRefundTrade(jiamimessage);
        System.out.println(decryptRefundTrade.toString());
    }
}
