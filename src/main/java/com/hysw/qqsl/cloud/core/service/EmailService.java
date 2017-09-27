package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.entity.Email;
import com.hysw.qqsl.cloud.core.entity.Verification;
import com.hysw.qqsl.cloud.core.entity.data.Certify;
import com.hysw.qqsl.cloud.util.SettingUtils;
import com.sun.mail.util.MailSSLSocketFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Properties;

/**
 * Create by leinuo on 17-9-27 上午10:59
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
@Service("emailService")
public class EmailService {

    private Session session;
    @Autowired
    private EmailCache emailCache;

    public EmailService() {
        this.session = createEmailSession();
    }

    public Session createEmailSession(){
        Properties props = new Properties();
        //协议
        props.setProperty("mail.transport.protocol", "smtp");
        //服务器
        props.setProperty("mail.smtp.host", "smtp.exmail.qq.com");
        //端口
        props.setProperty("mail.smtp.port", "465");
        //使用smtp身份验证
        props.setProperty("mail.smtp.auth", "true");
        //使用SSL，企业邮箱必需！
        //开启安全协议
        MailSSLSocketFactory sf = null;
        try {
            sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
        } catch (GeneralSecurityException e1) {
            e1.printStackTrace();
        }
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.socketFactory", sf);
        //
        //获取Session对象
        Session session = Session.getDefaultInstance(props,new Authenticator() {
            //此访求返回用户和密码的对象
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                PasswordAuthentication pa = new PasswordAuthentication(CommonAttributes.MYEMAILACCOUNT, CommonAttributes.MYEMAILPASSWORD);
                return pa;
            }
        });
        //设置session的调试模式，发布时取消
        session.setDebug(true);
        return session;
    }

    /**
     * 实名认证失败
     * @param certify
     * @return
     */
    public void personalCertifyFail(Certify certify){
        Email email = new Email(certify.getUser().getEmail(), "水利云实名认证失败", "尊敬的水利云用户您好，您的实名认证由于==>" + certify.getIdentityAdvice() + "<==原因，导致认证失败，请重新进行认证。");
        emailCache.add(createMimeMessage(email));
    }

    /**
     * 企业认证失败
     * @param certify
     * @return
     */
    public void companyCertifyFail(Certify certify){
        Email email = new Email(certify.getUser().getEmail(), "水利云企业认证失败", "尊敬的水利云用户您好，您的企业认证由于==>" + certify.getCompanyAdvice() + "<==原因，导致认证失败，请重新进行认证。");
        emailCache.add(createMimeMessage(email));
    }

    /**
     * 实名认证成功
     * @param certify
     * @return
     */
    public void personalCertifySuccess(Certify certify){
        Email email = new Email(certify.getUser().getEmail(), "水利云实名认证成功", "尊敬的水利云用户您好，您的实名认证已经通过认证，水利云将为您提供更多，更优质的服务。");
        emailCache.add(createMimeMessage(email));
    }

    /**
     * 企业认证成功
     * @param certify
     * @return
     */
    public void companyCertifySuccess(Certify certify){
        Email email = new Email(certify.getUser().getEmail(), "水利云企业认证成功", "尊敬的水利云用户您好，您的企业认证已经通过认证，水利云将为您提供更多企业级功能，更优质的企业级服务。");
        emailCache.add(createMimeMessage(email));
    }


    /**
     * 登录验证码
     *
     * @param email
     * @param session
     * @return
     */
    public Message getVerifyCodeLogin(String email, HttpSession session) {
        Verification verification = new Verification();
        String code = SettingUtils.createRandomVcode();
        verification.setEmail(email);
        verification.setCode(code);
        session.setAttribute("verification", verification);
        Email email1 =new Email(email, "水利云登录验证码", "尊敬的水利云用户您好，您的登录验证码为：" + code + ",5分钟内有效。");
        emailCache.add(createMimeMessage(email1));
        return new Message(Message.Type.OK);
    }

    /**
     * 重置密码验证码
     * @param email
     * @param session
     * @return
     */
    public Message getVerifyCoderesetPassword(String email, HttpSession session){
        Verification verification = new Verification();
        String code = SettingUtils.createRandomVcode();
        verification.setEmail(email);
        verification.setCode(code);
        session.setAttribute("verification", verification);
        Email email1=new Email(email, "水利云重置密码验证码", "尊敬的水利云用户您好，您的重置密码验证码为："+code+",5分钟内有效。");
        emailCache.add(createMimeMessage(email1));
        return new Message(Message.Type.OK);
    }

    /**
     * 绑定邮箱验证码
     * @param email
     * @param session
     * @return
     */
    public Message getVerifyCodeBinding(String email, HttpSession session){
        Verification verification = new Verification();
        String code = SettingUtils.createRandomVcode();
        verification.setEmail(email);
        verification.setCode(code);
        session.setAttribute("verification", verification);
        Email email1=new Email(email, "水利云邮箱绑定验证码", "尊敬的水利云用户您好，您的邮箱绑定验证码为："+code+",5分钟内有效。");
        emailCache.add(createMimeMessage(email1));
        return new Message(Message.Type.OK);
    }

    /**
     * 创建一封只包含文本的简单邮件
     *
     * @param email    email实体
     * @return
     * @throws Exception
     */
    private MimeMessage createMimeMessage(Email email){
        // 1. 创建一封邮件
        MimeMessage mimeMessage = new MimeMessage(session);
        try{
            // 2. From: 发件人
            mimeMessage.setFrom(new InternetAddress(CommonAttributes.MYEMAILACCOUNT, "qqsl", "UTF-8"));

            // 3. To: 收件人（可以增加多个收件人、抄送、密送）
            mimeMessage.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(email.getReceiveMail(), "水利云", "UTF-8"));

            // 4. Subject: 邮件主题
            mimeMessage.setSubject(email.getSubject(), "UTF-8");

            // 5. Content: 邮件正文（可以使用html标签）
            mimeMessage.setContent(email.getContext(), "text/html;charset=UTF-8");

            // 6. 设置发件时间
            mimeMessage.setSentDate(new Date());
            // 7. 保存设置
            mimeMessage.saveChanges();
        }catch (Exception e){
            return null;
        }
        return mimeMessage;
    }
}
