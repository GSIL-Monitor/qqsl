package com.hysw.qqsl.cloud.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import java.io.IOException;


/**
 * 邮件管理
 *
 * @author chenl
 * @create 2017-09-22 上午9:38
 */
@Component("emailManager")
public class EmailManager implements Runnable{
    Log logger = LogFactory.getLog(getClass());
    @Autowired
    private EmailCache emailCache;

//    private MimeMessage getMimeMessage(){
//        return emailCache.getMimeMessage();
//    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MimeMessage mimeMessage = emailCache.getMimeMessage();
            if (mimeMessage == null) {
                continue;
            }
            sendEmail(mimeMessage);
            emailCache.remove(mimeMessage);
        }
    }

    private void sendEmail(MimeMessage mimeMessage) {
        try {
            Transport.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        try {
            logger.info("邮箱：" + mimeMessage.getAllRecipients()[0].toString().substring(mimeMessage.getAllRecipients()[0].toString().indexOf("<") + 1, mimeMessage.getAllRecipients()[0].toString().indexOf(">")));
            logger.info("内容"+mimeMessage.getContent());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
