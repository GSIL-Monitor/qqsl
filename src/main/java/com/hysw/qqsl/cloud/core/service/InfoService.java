package com.hysw.qqsl.cloud.core.service;

import java.util.*;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hysw.qqsl.cloud.core.entity.element.Info;
import com.hysw.qqsl.cloud.core.entity.Setting;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.util.SettingUtils;

import org.springframework.util.StringUtils;

@Service("infoService")
public class InfoService {

	Log logger = LogFactory.getLog(getClass());
	@Autowired
	private CacheManager cacheManager;
	Setting setting = SettingUtils.getInstance().getSetting();

	/**
	 * 创建项目信息 项目信息分为工程投资信息和普通信息(有无selects)
	 *
	 * @return
	 */
	public List<Info> makeInfos(String infoXML) {
		List<Info> infos = new ArrayList<>();
		Info info;
		try {
			// 读取info.xml
			Element element = (Element) SettingUtils.getInstance()
					.getRootElement(infoXML);
			// 获取项目信息list
			List<Element> elements = SettingUtils.getInstance().getElementList(
					element);
			for (int i = 0; i < elements.size(); i++) {
				// 获取项目信息名称
				String infoName = elements.get(i).attributeValue("name");
				// 获取项目信息选择值
				String infoSelect = elements.get(i).attributeValue("select");
				if (infoSelect != null && infoSelect.length() > 0) {
					List<String> selects = Arrays.asList(infoSelect.split(","));
					info = new Info(infoName, selects);
					info.setOrder(i);
					infos.add(info);
				} else {
					info = new Info(infoName, null);
					info.setOrder(i);
					infos.add(info);
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return infos;
	}

	public String getPlanning(int index) {
		return getInfos().get(11).getSelectValues().get(index);
	}

	public void infosCache(){
		Cache cache = cacheManager.getCache("infosCache");
		net.sf.ehcache.Element element = new net.sf.ehcache.Element("infos", makeInfos(setting.getInfo()));
		cache.put(element);
	}

	/**
	 * 获取项目信息
	 *
	 * @return
	 */
	public List<Info> getInfos() {
		Cache cache = cacheManager.getCache("infosCache");
		net.sf.ehcache.Element element = cache.get("infos");
		return (List<Info>) element.getValue();
	}

	/**
	 * 用于测试
	 * @return
     */
	public List<Info> getTestInfos() {
		return getInfos();
	}
	/**
	 * 将info信息写入project
	 *
	 * @param project
	 * @param order
	 * @param value
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	public void saveInfo(Project project, int order, String value) {
		String infoOrder = String.valueOf(order); 
		JSONArray jsonArray = new JSONArray();
		List<JSONObject> infoJsons;
		if(StringUtils.hasText(project.getInfoStr())==true){
			infoJsons = jsonArray.fromObject(project.getInfoStr());
		}else{
			infoJsons = new ArrayList<JSONObject>();
		}
		if(infoOrder.equals("9")){
			value = getInvestmentInfo(value); 	
		}
		JSONObject infoJson;
		if(infoJsons.isEmpty()){
			infoJson = new JSONObject();
			infoJson.put("order", infoOrder);
			infoJson.put("value", value);
			infoJsons.add(infoJson);
		}else{
			List<String> infoOrders = new ArrayList<String>();
			 for(int i = 0;i<infoJsons.size();i++){
				 infoJson = infoJsons.get(i);
				 if(infoJson.get("order").equals(infoOrder)){
                   infoJson.put("value", value);                      
             	}  
				 infoOrders.add(infoJson.getString("order"));
			    }
			 if(infoOrders.size()>0&&!infoOrders.contains(infoOrder)){
				 infoJson = new JSONObject();
				 infoJson.put("order", infoOrder);
				 infoJson.put("value", value);
				 infoJsons.add(infoJson);
			 }
		}
		project.setInfoStr(infoJsons.toString());
	}

	/**
	 * 工程投资信息的处理
	 * @param value
	 * @return
	 */
	public String getInvestmentInfo(String value) {
		List<String> investments = getInfos().get(9).getSelectValues();
		Double investment;
		try {
			investment = Double.valueOf(value);
		} catch (Exception e) {
			investment = 0.0d;
		}
		if (investment < 100.0f) {
			return investments.get(0);
		} else if (investment < 500.0f) {
			return investments.get(1);
		} else if (investment < 1000.0f) {
			return investments.get(2);
		} else if (investment < 5000.0f) {
			return investments.get(3);
		} else {
			return investments.get(4);
		}
	}
}