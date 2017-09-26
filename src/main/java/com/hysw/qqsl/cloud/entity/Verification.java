package com.hysw.qqsl.cloud.entity;

import java.io.Serializable;



/**
 * 手机验证码
 * @author Administrator
 *
 */
public class Verification implements Serializable  {

	private static final long serialVersionUID = 6448745876233464757L;
	/**验证码*/
	private String code;
	/**过期时间 */
	private Long codeLife;
	/**手机号码 */
	private String phone;
	
	public Verification() {
		this.codeLife = System.currentTimeMillis();
		//this.code = noteService.createRandomVcode();
	}
	public boolean isInvalied() {
		Long currentTime = System.currentTimeMillis();
		if (currentTime-this.codeLife>500000) {
			return true;
		}
		return false;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code){
		this.code = code;
	}
	
	public String getPhone() {
		return phone;
	}
	
	public void setPhone(String phone) {
		this.phone = phone;
	}
}
