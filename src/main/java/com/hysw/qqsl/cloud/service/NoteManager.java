package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.entity.Note;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;

@Service("noteManager")
public class NoteManager implements Runnable {

	Log logger = LogFactory.getLog(getClass());
	@Autowired
	private NoteCache noteCache;
	private static String url = CommonAttributes.URL;
	private static String account = CommonAttributes.ACCOUNT;
	private static String pswd = CommonAttributes.PSWD;
	private static String product = CommonAttributes.PRODUCT;
	private static String extno = CommonAttributes.EXTNO;
	private static Boolean needstatus = CommonAttributes.NEEDSTATUS;
	private HttpClient client;
	private GetMethod method;
	private URI base;
	public NoteManager() {
		this.client = new HttpClient();
		this.method = new GetMethod();
		try {
			this.base = new URI(url, false);
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 *
	 * @param mobile
	 *            手机号码，多个号码使用","分割
	 * @param msg
	 *            短信内容
	 *            是否需要状态报告，需要true，不需要false
	 * @return 返回值定义参见HTTP协议文档
	 * @throws Exception 
	 * @throws Exception
	 *
	 * 
	 */
	private String batchSend(String mobile, String msg) throws Exception {
		String returnStr;
		try {
			method.setURI(new URI(base, "HttpBatchSendSM", false));
			method.setQueryString(new NameValuePair[] {
					new NameValuePair("account", account),
					new NameValuePair("pswd", pswd),
					new NameValuePair("mobile", mobile),
					new NameValuePair("needstatus", String.valueOf(needstatus)),
					new NameValuePair("msg", msg),
					new NameValuePair("product", product),
					new NameValuePair("extno", extno), });
			int result = client.executeMethod(method);
			if (result == HttpStatus.SC_OK) {
				InputStream in = method.getResponseBodyAsStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = in.read(buffer)) != -1) {
					baos.write(buffer, 0, len);
				}
				returnStr = URLDecoder.decode(baos.toString(), "UTF-8");
				return returnStr;
			} else {
				throw new Exception("HTTP ERROR Status: "
						+ method.getStatusCode() + ":" + method.getStatusText());
			}
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * 线程调度此方法发短信
	 * 
	 * @param note
	 */
	public void sendNote(Note note) {
		String returnString = null;
		try {
			returnString = batchSend(note.getPhone(), note.getSendMsg());
		} catch (Exception e) {
			logger.info("短信服务异常");
			e.printStackTrace();
		}
		logger.info("  手机号码：" + note.getPhone());
		logger.info("  内容：" + note.getSendMsg());
		logger.info(returnString);
	}

	@Override
	// 发送短信的线程
	public void run() {
		Note note ;
		String key;
		while (true) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 取出一个缓存
			key = noteCache.getCacheKey();
			if (key == null) {
				continue;
			}
			note = noteCache.getCacheNote(key);
			sendNote(note);
			//移除缓存
			noteCache.remove(key);
		}
	}
}
