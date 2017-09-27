package com.hysw.qqsl.cloud;

import com.hysw.qqsl.cloud.core.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 邮件服务
 *
 * @author chenl
 * @create 2017-09-25 上午9:07
 */
public class EmailCacheTest extends BaseTest{
    @Autowired
    private EmailService emailService;

//    @Test
//    public void testSendEmail() throws MessagingException {
//        User user=new User();
//        user.setEmail("84781320@qq.com");
////        Email email = new Email();
//       /* MimeMessage mimeMessage = Email.getVerifyCodeBinding(user,"123456");
//        emailCache.add(mimeMessage);*/
//        MimeMessage mimeMessage2 = emailService.getVerifyCoderesetPassword(user,"123456");
//        emailCache.add(mimeMessage2);
//    }
}
