package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.Verification;
import com.hysw.qqsl.cloud.core.service.MessageService;
import org.apache.shiro.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @anthor Administrator
 * @since 15:46 2018/3/12
 */
@Component("commonController")
public class CommonController {
    /**
     * 验证验证码
     *
     * @param code
     * @param verification
     * @return
     */
    public Message checkCode(String code, Verification verification) {
        if (verification == null) {
            return MessageService.message(Message.Type.CODE_NOEXIST);
        }
        if (verification.isInvalied()) {
            // 验证码过期
            return MessageService.message(Message.Type.CODE_INVALID);
        }
        boolean result = checkCodeIsSame(code, verification);
        if (result) {
            return MessageService.message(Message.Type.CODE_ERROR);
        }
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 验证验证码
     * @return false ==   ture !=
     */
    public boolean checkCodeIsSame(String code, Verification verification){
        if (verification.getCode().equals(code)) {
            return false;
        }
        return true;
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
