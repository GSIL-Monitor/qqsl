package com.hysw.qqsl.cloud.core.service;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 上传文件缓存
 *
 * @since 2015年7月30日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
@Component("uploadCache")
public class UploadCache {

	/** 缓存 */
	private Map<String, InputStream> fileMap = new HashMap<String, InputStream>();

	/**
	 * 取得一个缓存key值
	 * @return
	 */
	public String getCacheKey() {
		if (this.fileMap.size()==0) {
			return null;
		}
		Iterator<String> it = this.fileMap.keySet().iterator();
		return it.next();
	}
	
	/**
	 * 根据key值获得数据流
	 * @param key
	 * @return
	 */
	public InputStream getCacheInputStream(String key) {
		return this.fileMap.get(key);
	}
	
	/**
	 * 添加缓存
	 * @param key
	 * @param inputStream
	 */
	public void add(String key, InputStream inputStream) {
			fileMap.put(key, inputStream);	
	}
	
	/**
	 * 删除缓存
	 * @param key
	 */
	public void remove(String key)  {
		fileMap.remove(key);
	}

}
