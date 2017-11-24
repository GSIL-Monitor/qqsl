package com.hysw.qqsl.cloud.pay.service;

import com.hysw.qqsl.cloud.core.entity.Note;
import com.hysw.qqsl.cloud.core.service.EmailService;
import com.hysw.qqsl.cloud.core.service.NoteCache;
import com.hysw.qqsl.cloud.core.service.UserMessageService;
import com.hysw.qqsl.cloud.pay.entity.data.Trade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("commonService")
public class CommonService {
    @Autowired
    private NoteCache noteCache;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserMessageService userMessageService;
    @Autowired
    private TradeService tradeService;

    public void sendMessage(Trade trade){
        String message = "尊敬的水利云用户您好，您已进行了"+tradeService.convertType(trade)+"-->"+tradeService.convertBaseType(trade)+"-->"+tradeService.convertBuyType(trade)+",编号为:"+trade.getInstanceId();
//                            //                    短信通知
        Note note = new Note(trade.getUser().getPhone(), message);
        noteCache.add(trade.getUser().getPhone(),note);
//                            //                    邮件通知
        emailService.emailNotice(trade.getUser().getEmail(), tradeService.convertType(trade)+tradeService.convertBuyType(trade), message);
//                                                  站内通知
        userMessageService.buyPackage(trade.getUser(),message);
    }
}
