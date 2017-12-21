package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.data.Certify;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.EmailService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;

/**
 * 邮件服务
 *
 * @author chenl
 * @create 2017-09-25 上午9:07
 */
public class EmailCacheTest extends BaseTest {
    @Autowired
    private EmailService emailService;
    @Autowired
    private EmailCache emailCache;

//    @Test
//    public void testSendEmail() throws MessagingException, IOException {
//        User user=new User();
//        user.setEmail("84781320@qq.com");
//        Certify certify = new Certify();
//        certify.setUser(user);
//        emailService.companyCertifyFail(certify);
//        MimeMessage mimeMessage = emailCache.getMimeMessage();
//        System.out.println(mimeMessage.getContent());
//        System.out.println(mimeMessage.getAllRecipients()[0].toString().substring(mimeMessage.getAllRecipients()[0].toString().indexOf("<")+1, mimeMessage.getAllRecipients()[0].toString().indexOf(">")));
//        System.out.println();
//    }
}
