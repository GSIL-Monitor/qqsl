package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.entity.Note;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 短信对象缓存池
 * @author Administrator
 *
 */
@Component("noteCache")
public class NoteCache {

	private Map<String,Note> noteMap = new HashMap<String,Note>(); 

	/**
	 * 取得一个缓存key值
	 * @return
	 */
	public String getCacheKey() {
		if (this.noteMap.size()==0) {
			return null;
		}
		Iterator<String> it = this.noteMap.keySet().iterator();
		return it.next();
	}
	
	/**
	 * 根据key值获得note对象
	 * @param key
	 * @return
	 */
	public Note getCacheNote(String key) {
		return this.noteMap.get(key);
	}
	
	/**
	 * 添加缓存
	 * @param key
	 * @param note
	 */
	public void add(String key, Note note) {
			noteMap.put(key+System.currentTimeMillis(), note);	
	}
	
	/**
	 * 删除缓存
	 * @param key
	 */
	public void remove(String key)  {
		noteMap.remove(key);
	}

	
}
