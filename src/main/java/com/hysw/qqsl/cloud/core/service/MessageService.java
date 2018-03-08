package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.util.SettingUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    /**
     * 参数验证
     * @param object
     * @return
     */
    public static Message parameterCheck(Object object){
        if(object==null){
            return MessageService.message(Message.Type.PARAMETER_ERROR);
        }
        Map<String,Object> map = (Map<String,Object>)object;
        Map<String,Object> sonMap;
        List<Object> objectList;
        if(map==null||map.size()==0){
            return MessageService.message(Message.Type.PARAMETER_ERROR);
        }
        for(String key:map.keySet()) {
            if (map.get(key) == null || map.get(key).toString().equals("null") ||
                    !StringUtils.hasText(map.get(key).toString())) {
                return MessageService.message(Message.Type.PARAMETER_ERROR);
            }
            if (map.get(key) instanceof Map) {
                sonMap = (Map<String, Object>) map.get(key);
                if( sonMap.size() == 0){
                    return MessageService.message(Message.Type.PARAMETER_ERROR);
                }
                for (String sonKey : sonMap.keySet()) {
                    if (sonMap.get(sonKey) == null) {
                        return MessageService.message(Message.Type.PARAMETER_ERROR);
                    }
                    if (sonMap.get(sonKey).toString().equals("null") ||
                            !StringUtils.hasText(sonMap.get(sonKey).toString())) {
                        return MessageService.message(Message.Type.PARAMETER_ERROR);
                    }
                }
            }
            if(map.get(key) instanceof Collection){
                objectList = (ArrayList<Object>) (map.get(key));
                if(objectList.size()==0){
                    return MessageService.message(Message.Type.PARAMETER_ERROR);
                }
                for(int i=0;i<objectList.size();i++){
                    if(objectList.get(i)==null||objectList.get(i).equals("null")||
                            !StringUtils.hasText(objectList.get(i).toString())){
                        return MessageService.message(Message.Type.PARAMETER_ERROR);
                    }
                }
            }
        }
        return MessageService.message(Message.Type.OK,map);
    }

    /**
     * 多个参数验证
     * @param objects
     * @return
     */
    public static Message parametersCheck(Object...objects){
        for(Object object:objects){
            if(object==null||object.toString().equals("null")||!StringUtils.hasText(object.toString())){
                return MessageService.message(Message.Type.PARAMETER_ERROR);
            }
        }
        return MessageService.message(Message.Type.OK);
    }

}
