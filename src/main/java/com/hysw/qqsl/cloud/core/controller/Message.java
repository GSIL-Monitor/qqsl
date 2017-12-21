package com.hysw.qqsl.cloud.core.controller;

/**
 * 后台发给前台的处理消息
 *
 * @since 2015年10月9日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 异常类型 
 * 
 * -2 上传出错
 * 
 * -1 session失效,文件过大无法上传
 * 
 * 0  登陆成功,注册成功,修改个人信息成功,从属用户设置成功,重置密码成功,保存项目成功,项目编号修改成功,项目名称修改成功,删除成功
 * 
 * 1  登陆失败, 邀请码错误
 * 
 * 2 用户已存在,用户不存在,项目编号已存在,业主联系人不存在
 * 
 */
public class Message {
	private Type type;
	private String total;
	private Object data;
	public enum Type{
		/** 成功*/
		OK,
		/** 失败*/
		FAIL,
		/** 是否存在*/
		EXIST, 
		/** 验证码失效*/
		INVALID,  
		/** 无权限*/
		NO_AUTHORIZE, 
		/** session失效*/
		NO_SESSION, 
		/** 其他*/
		OTHER, 
		/** 未知*/
		UNKNOWN,
		/** 未认证 */
		NO_CERTIFY,
		/** 过期 */
		EXPIRED,
		NO_ALLOW
	}
	
	public Message(Type type) {
		this.type = type;
	}
	
	public Message(Type type,Object data) {
		this.type = type;
		this.data = data;
	}	
	public Message(Type type,Object data,String total) {
		this.type = type;
		this.data = data;
		this.total=total;
	}	


	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Object getData() {
		return this.data;
	}

	public String getTotal() {
		return total;
	}

	/**
	 * 参数验证
	 * @param object
	 * @return
     */
	public static Message parameterCheck(Object object){
		if(object==null){
			return new Message(Message.Type.FAIL);
		}
		Map<String,Object> map = (Map<String,Object>)object;
		Map<String,Object> sonMap;
		List<Object> objectList;
		if(map==null||map.size()==0){
			return new Message(Message.Type.FAIL);
		}
		for(String key:map.keySet()) {
			if (map.get(key) == null || map.get(key).toString().equals("null") ||
					!StringUtils.hasText(map.get(key).toString())) {
				return new Message(Message.Type.FAIL);
			}
			if (map.get(key) instanceof Map) {
				sonMap = (Map<String, Object>) map.get(key);
				if( sonMap.size() == 0){
					return new Message(Message.Type.FAIL);
				}
				for (String sonKey : sonMap.keySet()) {
					if (sonMap.get(sonKey) == null) {
						return new Message(Message.Type.FAIL);
					}
					if (sonMap.get(sonKey).toString().equals("null") ||
							!StringUtils.hasText(sonMap.get(sonKey).toString())) {
						return new Message(Message.Type.FAIL);
					}
				}
			}
			if(map.get(key) instanceof Collection){
				objectList = (ArrayList<Object>) (map.get(key));
               if(objectList.size()==0){
				   return new Message(Message.Type.FAIL);
			   }
               for(int i=0;i<objectList.size();i++){
				   if(objectList.get(i)==null||objectList.get(i).equals("null")||
						   !StringUtils.hasText(objectList.get(i).toString())){
					   return new Message(Message.Type.FAIL);
				   }
			   }
			}
		}
			return new Message(Message.Type.OK,map);
	}

	/**
	 * 多个参数验证
	 * @param objects
	 * @return
     */
	public static Message parametersCheck(Object...objects){
		for(Object object:objects){
			if(object==null||object.toString().equals("null")||!StringUtils.hasText(object.toString())){
				return new Message(Message.Type.FAIL);
			}
		}
		return new Message(Message.Type.OK);
	}
}
