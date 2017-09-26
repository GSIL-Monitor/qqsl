package com.hysw.qqsl.cloud.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;


/**
 * 邮件管理
 *
 * @author chenl
 * @create 2017-09-22 上午9:38
 */
@Component("emailManager")
public class EmailManager implements Runnable{
    @Autowired
    private EmailCache emailCache;

//    private MimeMessage getMimeMessage(){
//        return emailCache.getMimeMessage();
//    }

    @Override
    public void run() {
        while (true) {
            try {
                MimeMessage mimeMessage = emailCache.getMimeMessage();
                if (mimeMessage == null) {
                    continue;
                }
                Transport.send(mimeMessage);
                emailCache.remove(mimeMessage);
            } catch (MessagingException e) {
                continue;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
