package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.entity.Message;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 消息服务层+参数验证
 * @anthor yutisn
 * @since 10:41 2018/2/28
 */
@Service("messageService")
public class MessageService {
    private static Map<String, Message> messages;

    /**
     * 初始化静态类型message
     */
    public void init(){
        messages = new LinkedHashMap<>();
        for (Message.Type type : Message.Type.values()) {
            messages.put(type.toString(), new Message(type));
        }
    }

    public static Message message(Message.Type type){
        return messages.get(type.toString());
    }

    public static Message message(Message.Type type, Object data){
        return new Message(type, data);
    }

    public static Message message(Message.Type type, Object data, String total){
        return new Message(type,data,total);
    }


}
