package com.hysw.qqsl.cloud.entity;

/**
 * 程序运行时,参数错误
 * @author Administrator
 *
 */
public class QQSLException extends Exception {

	private static final long serialVersionUID = 1L;

	public QQSLException(){
		super();
	}
	public QQSLException(String message){
		super(message);
	}
}
