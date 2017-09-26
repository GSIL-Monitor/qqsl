package com.hysw.qqsl.cloud.util;

import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;

/**
 * 邮件缓存池
 *
 * @author chenl
 * @create 2017-09-22 上午9:26
 */
@Component("emailCache")
public class EmailCache {
    /** 邮件缓存 */
    private List<MimeMessage> list = new ArrayList<>();

    /**
     * 增加邮件实体
     * @param mimeMessage
     */
    public void add(MimeMessage mimeMessage) {
        if (mimeMessage == null) {
            return;
        }
        list.add(mimeMessage);
    }

    /**
     * 删除邮件实体
     * @param mimeMessage
     */
    public void remove(MimeMessage mimeMessage) {
        list.remove(mimeMessage);
    }

    /**
     * 获取一个邮件实体
     */
    public MimeMessage getMimeMessage(){
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }
}
