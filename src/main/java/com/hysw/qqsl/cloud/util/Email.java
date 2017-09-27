package com.hysw.qqsl.cloud.util;

import com.hysw.qqsl.cloud.core.controller.*;
import com.hysw.qqsl.cloud.core.entity.Verification;
import com.hysw.qqsl.cloud.core.entity.data.Certify;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.NoteService;
import com.hysw.qqsl.cloud.core.service.UserService;
import com.sun.mail.util.MailSSLSocketFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.*;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Properties;

/**
 * Create by leinuo on 17-9-19 下午3:26
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
public class Email {
    @Autowired
    private NoteService noteService;

    private String receiveMail;
    private String subject;
    private String context;
    private static MimeMessage mimeMessage;

    // 发件人的 邮箱 和 密码（替换为自己的邮箱和密码）
    // PS: 某些邮箱服务器为了增加邮箱本身密码的安全性，给 SMTP 客户端设置了独立密码（有的邮箱称为“授权码”）,
    //     对于开启了独立密码的邮箱, 这里的邮箱密码必需使用这个独立密码（授权码）。
    //leinuo@qingqingshuili.cn
    //public static String myEmailAccount = "shy-wangyi@163.com";
    //public static String myEmailPassword = "qqsl12345678";
//    企业邮箱用户名与密码
    public static String myEmailAccount = "leinuo@qingqingshuili.cn";
    public static String myEmailPassword = "Ljb608403";
    // 发件人邮箱的 SMTP 服务器地址, 必须准确, 不同邮件服务器地址不同, 一般(只是一般, 绝非绝对)格式为: smtp.xxx.com
    // 网易163邮箱的 SMTP 服务器地址为: smtp.163.com
    //public static String myEmailSMTPHost = "smtp.163.com";
    public static String myEmailSMTPHost = "smtp.qq.com";
    // 收件人邮箱（替换为自己知道的有效邮箱）
    //public static String receiveMailAccount = "1321404703@qq.com";
//    public static String receiveMailAccount = "1321404703@qq.com";

    private Email(String receiveMail, String subject, String context) {
        this.receiveMail = receiveMail;
        this.subject = subject;
        this.context = context;
        createMimeMessage();
    }

    /**
     * 实名认证失败
     * @param certify
     * @return
     */
    public static MimeMessage personalCertifyFail(Certify certify){
        new Email(certify.getUser().getEmail(), "水利云实名认证失败", "尊敬的水利云用户您好，您的实名认证由于==>"+certify.getIdentityAdvice()+"<==原因，导致认证失败，请重新进行认证。");
        return mimeMessage;
    }

    /**
     * 企业认证失败
     * @param certify
     * @return
     */
    public static MimeMessage companyCertifyFail(Certify certify){
        new Email(certify.getUser().getEmail(), "水利云企业认证失败", "尊敬的水利云用户您好，您的企业认证由于==>"+certify.getCompanyAdvice()+"<==原因，导致认证失败，请重新进行认证。");
        return mimeMessage;
    }

    /**
     * 实名认证成功
     * @param certify
     * @return
     */
    public static MimeMessage personalCertifySuccess(Certify certify){
        new Email(certify.getUser().getEmail(), "水利云实名认证成功", "尊敬的水利云用户您好，您的实名认证已经通过认证，水利云将为您提供更多，更优质的服务。");
        return mimeMessage;
    }

    /**
     * 企业认证成功
     * @param certify
     * @return
     */
    public static MimeMessage companyCertifySuccess(Certify certify){
        new Email(certify.getUser().getEmail(), "水利云企业认证成功", "尊敬的水利云用户您好，您的企业认证已经通过认证，水利云将为您提供更多企业级功能，更优质的企业级服务。");
        return mimeMessage;
    }

    /**
     * 手机获取的验证码
     *
     * @return
     */
    public static String createRandomVcode() {
        // 验证码
        String vcode = "";
        for (int i = 0; i < 6; i++) {
            vcode = vcode + (int) (Math.random() * 10);
        }
        return vcode;
    }

    /**
     * 登录验证码
     *
     * @param email
     * @param session
     * @return
     */
    public static MimeMessage getVerifyCodeLogin(String email, HttpSession session) {
        Verification verification = new Verification();
        String code = createRandomVcode();
        verification.setEmail(email);
        verification.setCode(code);
        session.setAttribute("verification", verification);
        new Email(email, "水利云登录验证码", "尊敬的水利云用户您好，您的登录验证码为：" + code + ",5分钟内有效。");
        return mimeMessage;
    }

    /**
     * 重置密码验证码
     * @param user
     * @param verifyCode
     * @return
     */
    public static MimeMessage getVerifyCoderesetPassword(User user, String verifyCode){
        new Email(user.getEmail(), "水利云重置密码验证码", "尊敬的水利云用户您好，您的重置密码验证码为："+verifyCode+",5分钟内有效。");
        return mimeMessage;
    }

    /**
     * 绑定邮箱验证码
     * @param email
     * @param verifyCode
     * @return
     */
    public static MimeMessage getVerifyCodeBinding(String email, String verifyCode){
        new Email(email, "水利云邮箱绑定验证码", "尊敬的水利云用户您好，您的邮箱绑定验证码为："+verifyCode+",5分钟内有效。");
        return mimeMessage;
    }

    /**
     * 创建邮件服务
     * @return
     */
    private void createMimeMessage(){
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
                PasswordAuthentication pa = new PasswordAuthentication(myEmailAccount, myEmailPassword);
                return pa;
            }
        });
        //设置session的调试模式，发布时取消
        session.setDebug(true);
        createMimeMessage(session,myEmailAccount);

    }

    /**
     * 创建一封只包含文本的简单邮件
     *
     * @param session     和服务器交互的会话
     * @param sendMail    发件人邮箱
     * @return
     * @throws Exception
     */
    private void createMimeMessage(Session session, String sendMail){
        // 1. 创建一封邮件
        mimeMessage = new MimeMessage(session);
        try{
            // 2. From: 发件人
            mimeMessage.setFrom(new InternetAddress(sendMail, "qqsl", "UTF-8"));

            // 3. To: 收件人（可以增加多个收件人、抄送、密送）
            mimeMessage.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(this.receiveMail, "水利云", "UTF-8"));

            // 4. Subject: 邮件主题
            mimeMessage.setSubject(this.subject, "UTF-8");

            // 5. Content: 邮件正文（可以使用html标签）
            mimeMessage.setContent(this.context, "text/html;charset=UTF-8");

            // 6. 设置发件时间
            mimeMessage.setSentDate(new Date());
            // 7. 保存设置
            mimeMessage.saveChanges();
        }catch (Exception e){
            return;
        }
    }

//    public static void main(String[] args) throws Exception {
//        companySend();
//    }
//
//    public static void commSend() throws Exception{
//        // 1. 创建参数配置, 用于连接邮件服务器的参数配置
//        Properties props = new Properties();                    // 参数配置
//        props.setProperty("mail.transport.protocol", "smtp");   // 使用的协议（JavaMail规范要求）
//        props.setProperty("mail.smtp.host", myEmailSMTPHost);   // 发件人的邮箱的 SMTP 服务器地址
//        props.setProperty("mail.smtp.auth", "true");            // 需要请求认证
//
//        // PS: 某些邮箱服务器要求 SMTP 连接需要使用 SSL 安全认证 (为了提高安全性, 邮箱支持SSL连接, 也可以自己开启),
//        //     如果无法连接邮件服务器, 仔细查看控制台打印的 log, 如果有有类似 “连接失败, 要求 SSL 安全连接” 等错误,
//        //     打开下面 /* ... */ 之间的注释代码, 开启 SSL 安全连接。
//          /*
//          // SMTP 服务器的端口 (非 SSL 连接的端口一般默认为 25, 可以不添加, 如果开启了 SSL 连接,
//          //                  需要改为对应邮箱的 SMTP 服务器的端口, 具体可查看对应邮箱服务的帮助,
//          //                  QQ邮箱的SMTP(SLL)端口为465或587, 其他邮箱自行去查看)
//          final String smtpPort = "465";
//          props.setProperty("mail.smtp.port", smtpPort);
//          props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//          props.setProperty("mail.smtp.socketFactory.fallback", "false");
//          props.setProperty("mail.smtp.socketFactory.port", smtpPort);
//          */
//
//        // 2. 根据配置创建会话对象, 用于和邮件服务器交互
//        Session session = Session.getDefaultInstance(props);
//        session.setDebug(true);                                 // 设置为debug模式, 可以查看详细的发送 log
//
//        // 3. 创建一封邮件
//        MimeMessage message = createMimeMessage(session, myEmailAccount, receiveMailAccount);
//
//        // 4. 根据 Session 获取邮件传输对象
//        Transport transport = session.getTransport();
//
//        // 5. 使用 邮箱账号 和 密码 连接邮件服务器, 这里认证的邮箱必须与 message 中的发件人邮箱一致, 否则报错
//        //
//        //    PS_01: 成败的判断关键在此一句, 如果连接服务器失败, 都会在控制台输出相应失败原因的 log,
//        //           仔细查看失败原因, 有些邮箱服务器会返回错误码或查看错误类型的链接, 根据给出的错误
//        //           类型到对应邮件服务器的帮助网站上查看具体失败原因。
//        //
//        //    PS_02: 连接失败的原因通常为以下几点, 仔细检查代码:
//        //           (1) 邮箱没有开启 SMTP 服务;
//        //           (2) 邮箱密码错误, 例如某些邮箱开启了独立密码;
//        //           (3) 邮箱服务器要求必须要使用 SSL 安全连接;
//        //           (4) 请求过于频繁或其他原因, 被邮件服务器拒绝服务;
//        //           (5) 如果以上几点都确定无误, 到邮件服务器网站查找帮助。
//        //
//        //    PS_03: 仔细看log, 认真看log, 看懂log, 错误原因都在log已说明。
//        transport.connect(myEmailAccount, myEmailPassword);
//
//        // 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
//        transport.sendMessage(message, message.getAllRecipients());
//
//        // 7. 关闭连接
//        transport.close();
//    }

//    public static void companySend() throws Exception{
//        Properties props = new Properties();
//        //协议
//        props.setProperty("mail.transport.protocol", "smtp");
//        //服务器
//        props.setProperty("mail.smtp.host", "smtp.exmail.qq.com");
//        //端口
//        props.setProperty("mail.smtp.port", "465");
//        //使用smtp身份验证
//        props.setProperty("mail.smtp.auth", "true");
//        //使用SSL，企业邮箱必需！
//        //开启安全协议
//        MailSSLSocketFactory sf = null;
//        try {
//            sf = new MailSSLSocketFactory();
//            sf.setTrustAllHosts(true);
//        } catch (GeneralSecurityException e1) {
//            e1.printStackTrace();
//        }
//        props.put("mail.smtp.ssl.enable", "true");
//        props.put("mail.smtp.ssl.socketFactory", sf);
//        //
//        //获取Session对象
//        Session session = Session.getDefaultInstance(props,new Authenticator() {
//            //此访求返回用户和密码的对象
//            @Override
//            protected PasswordAuthentication getPasswordAuthentication() {
//                PasswordAuthentication pa = new PasswordAuthentication("leinuo@qingqingshuili.cn", "Ljb608403");
//                return pa;
//            }
//        });
//        //设置session的调试模式，发布时取消
//        session.setDebug(true);
//       // MimeMessage mimeMessage = new MimeMessage(session);
//        MimeMessage mimeMessage1 = createMimeMessage(session,myEmailAccount, receiveMailAccount);
//          /*  mimeMessage.setFrom(new InternetAddress(myEmailAccount,myEmailAccount));
//            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(receiveMailAccount));
//            //设置主题
//            mimeMessage.setSubject("测试");
//            mimeMessage.setSentDate(new Date());
//            //设置内容
//            mimeMessage.setText("你好!");
//            mimeMessage.saveChanges();
//            //发送*/
////            Transport.send(mimeMessage1);
//    }
//    /**
//     * 创建一封只包含文本的简单邮件
//     *
//     * @param session     和服务器交互的会话
//     * @param sendMail    发件人邮箱
//     * @param receiveMail 收件人邮箱
//     * @return
//     * @throws Exception
//     */
//    public static MimeMessage createMimeMessage(Session session, String sendMail, String receiveMail) throws Exception {
//        // 1. 创建一封邮件
//        MimeMessage message = new MimeMessage(session);
//
//        // 2. From: 发件人
//        message.setFrom(new InternetAddress(sendMail, "qqsl", "UTF-8"));
//
//        // 3. To: 收件人（可以增加多个收件人、抄送、密送）
//        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMail, "雷诺用户", "UTF-8"));
//
//        // 4. Subject: 邮件主题
//        message.setSubject("测试", "UTF-8");
//
//        // 5. Content: 邮件正文（可以使用html标签）
//        message.setContent("雷诺用户你好!", "text/html;charset=UTF-8");
//
//        // 6. 设置发件时间
//        message.setSentDate(new Date());
//        // 7. 保存设置
//        message.saveChanges();
//
//        return message;
//    }
}
