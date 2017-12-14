package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.BaseDao;
import com.hysw.qqsl.cloud.core.entity.data.BaseEntity;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;


@Transactional
public abstract class BaseService<T extends BaseEntity, Id extends Serializable> {
	/** entity类的dao层类型对象 */
	private BaseDao<T, Id> baseDao;
	@Autowired
	private CacheManager cacheManager;
	/** 实体类类型 */
	public Class<T> entityClass;

	@SuppressWarnings("unchecked")
	public BaseService() {
		Type type = getClass().getGenericSuperclass();
		Type[] parameterizedType = ((ParameterizedType) type)
				.getActualTypeArguments();
		entityClass = (Class<T>) parameterizedType[0];
	}

	public void setBaseDao(BaseDao< T,Id> baseDao) {
		this.baseDao = baseDao;
	}
	/**
	 * 增加方法
	 * @param entity
	 */
	@Transactional
	public void save(T entity) {
		baseDao.save(entity);
		entity.setModifyDate(new Date());
		String name = getClassName(entity.getClass().getName());
		Cache cache = cacheManager.getCache(name + "Cache");
		if (cache != null) {
			Element element = new Element(entity.getId(), entity);
			cache.put(element);
		}
		Cache cache1 = cacheManager.getCache(name + "AllCache")!=null?cacheManager.getCache(name + "AllCache"):cacheManager.getCache(name + "PartCache");
		if (cache1 != null) {
			Object key = cache1.getKeys().get(0);
			Element element = cache1.get(key);
			List<T> list = (List<T>) element.getValue();
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getId().equals(entity.getId())) {
					list.remove(i);
					break;
				}
			}
			list.add(entity);
			Element element1 = new Element(key, list);
			cache1.put(element1);
		}
	}

	/**
	 * 删除方法
	 * @param entity
	 */
	@Transactional
	public void remove(T entity) {
		baseDao.remove(entity);
		String name = getClassName(entity.getClass().getName());
		Cache cache = cacheManager.getCache(name + "Cache");
		if (cache != null) {
			cache.remove(entity.getId());
		}
		Cache cache1 = cacheManager.getCache(name + "AllCache")!=null?cacheManager.getCache(name + "AllCache"):cacheManager.getCache(name + "PartCache");
		if (cache1 != null) {
			Object key = cache1.getKeys().get(0);
			Element element = cache1.get(key);
			List<T> list = (List<T>) element.getValue();
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getId().equals(entity.getId())) {
					list.remove(i);
					break;
				}
			}
			Element element1 = new Element(key, list);
			cache1.put(element1);
		}
	}

	/**
	 * 立即更新数据库
	 */
	@Transactional
	public void flush(){
		baseDao.flush();
	}

	/**
	 * 根据id查询
	 * @param id
	 * @return
	 */
	@Transactional(readOnly = true)
	public T find(Id id) {
		T t = null;
		String name = getClassName(entityClass.getName());
		Cache cache = cacheManager.getCache(name + "Cache");
		if (cache != null) {
			Element element = cache.get(id);
			if (element != null) {
				t=(T) element.getValue();
			}else{
				t = baseDao.find(id);
				Element element1 = new Element(id, t);
				cache.put(element1);
			}
		}else{
			t = baseDao.find(id);
		}
		return t;
	}
	/**
	 * 查询15条泛型对应的数据记录
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<T> findAll(){
		List<T> t = null;
		String name = getClassName(entityClass.getName());
		Cache cache = cacheManager.getCache(name + "AllCache");
		if (cache != null) {
			if (cache.getKeys().size() == 0) {
				t=baseDao.findList(0, null, null);
				Element element1 = new Element(name, t);
				cache.put(element1);
			}else{
				Object key = cache.getKeys().get(0);
				Element element = cache.get(key);
				if (element != null) {
					t = (List<T>) element.getValue();
				}else{
					t=baseDao.findList(0, null, null);
					Element element1 = new Element(name, t);
					cache.put(element1);
				}
			}
		}else{
			t=baseDao.findList(0, null, null);
		}
		return t;
	}

	private String getClassName(String name){
		String s = name.substring(name.lastIndexOf(".") + 1);
		String s1 = s.substring(0, 1);
		String s2 = s.substring(1, s.length());
		String s3 = s1.toLowerCase() + s2;
		return s3;
	}

}
