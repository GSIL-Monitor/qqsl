package com.hysw.qqsl.cloud;

import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.stereotype.Component;

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
    private List<MimeMailMessage> list = new ArrayList<>();

    /**
     * 增加邮件实体
     * @param mimeMailMessage
     */
    public void add(MimeMailMessage mimeMailMessage) {
        if (mimeMailMessage == null) {
            return;
        }
        list.add(mimeMailMessage);
    }

    /**
     * 删除邮件实体
     * @param mimeMailMessage
     */
    public void remove(MimeMailMessage mimeMailMessage) {
        list.remove(mimeMailMessage);
    }

    /**
     * 获取一个邮件实体
     */
    public MimeMailMessage getMimeMailMessage(){
        return list.get(0);
    }
}
