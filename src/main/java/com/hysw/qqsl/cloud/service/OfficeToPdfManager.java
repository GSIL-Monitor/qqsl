package com.hysw.qqsl.cloud.service;

import com.aliyun.oss.common.utils.IOUtils;
import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.DocumentFormat;
import com.hysw.qqsl.cloud.entity.ConverterObject;
import com.hysw.qqsl.cloud.entity.Note;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;

/**
 * office文档转换为pdf管理类
 * 
 * @since 2015年7月30日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
@Service("officeToPdfManager")
public class OfficeToPdfManager implements Runnable {
	
    Log logger = LogFactory.getLog(getClass());
	@Autowired
	private OssService ossService;
	@Autowired
	private UploadCache uploadCache;
	@Autowired
	private NoteCache noteCache;
	/** openoffice是否正常运行，文件是否转换成功的标识 */
	public boolean flag = false;
	/** 转换对象 */
	private DocumentConverter converter;
	

	
	public OfficeToPdfManager() {
		/*String ip = CommonAttributes.OPENOFFICE_IP;
		int port = CommonAttributes.OPENOFFICE_PORT;
		OpenOfficeConnection connection = new SocketOpenOfficeConnection(ip, port);
		// 获取openoffice连接
		try {
			connection.connect();
			flag = true;
		} catch (ConnectException e) {
			e.printStackTrace();
		}
		logger.info("openOffice状态:"+flag);
		//converter = new OpenOfficeDocumentConverter(connection);
		converter = new StreamOpenOfficeDocumentConverter(connection);*/
	}

	/**
	 * 转换office文件到pdf转换对象
	 * 
	 * @param key
	 * @param inputStream
	 * @param converterObject
	 */
	void converter(String key, InputStream inputStream,
			ConverterObject converterObject) {
		String codeKey = key.substring(0, key.lastIndexOf('.'))+".pdf";
		OutputStream outputStream = new ByteArrayOutputStream();
		DefaultDocumentFormatRegistry formatReg = new DefaultDocumentFormatRegistry();
		DocumentFormat doc = formatReg.getFormatByFileExtension("doc");
		DocumentFormat pdf = formatReg.getFormatByFileExtension("pdf");
		converter.convert(inputStream, doc, outputStream, pdf);
		converterObject.setKey(codeKey);
		byte[] bytes = ((ByteArrayOutputStream) outputStream).toByteArray();
		converterObject.setInputStream(new ByteArrayInputStream(bytes));
		// 关闭流
		IOUtils.safeClose(inputStream);
	}

	/**
	 * 产生预览文件
	 */
	public void run() {
		String key;
		InputStream inputStream;
		ConverterObject converterObject = new ConverterObject();
		while (true) {
			// 取出一个缓存
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			key = uploadCache.getCacheKey();
			if (key == null) continue;
			inputStream = uploadCache.getCacheInputStream(key);
			try {
				// 转换
				if(!key.contains(",")&&flag){
					logger.info(key+"--开始转换pdf");
					//转换成功更新备份
					converter(key, inputStream, converterObject);
					logger.info(converterObject.getKey()+"--转换pdf成功");
				    // 上传
					logger.info(converterObject.getKey()+"--开始上传pdf");
					ossService.upload("pdf", converterObject.getKey(),
							converterObject.getInputStream(), null, true);
					logger.info(converterObject.getKey()+"--上传pdf成功");
				}
				if(key.contains(",")){
					logger.info(key.substring(0, key.indexOf(","))+"--开始上传坐标文件");
					ossService.upload("coordinate", key.substring(0, key.indexOf(",")),
							inputStream, null, true);
					logger.info(key.substring(0, key.indexOf(","))+"--上传坐标文件成功");
				}
				if(inputStream!=null){
					IOUtils.safeClose(inputStream);
				}
			} catch(Exception e) {
				flag = false;
				//发送openOffice服务发生异常的短信
				Note note = new Note("13028710937","openOffice服务发生异常,需处理");
				noteCache.add("13028710937",note);
				e.printStackTrace();
			}
			// 清除缓存
			uploadCache.remove(key);
		}
	}

}
