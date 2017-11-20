package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.LogDao;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.element.Element;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.element.Unit;
import com.hysw.qqsl.cloud.core.entity.data.Log;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
@Service("logService")
public class LogService extends BaseService<Log, Long> {

	@Autowired
	private LogDao logDao;
	@Autowired
	private UnitService unitService;
	@Autowired
	private UserService userService;
	@Autowired
	private AccountService accountService;
	private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	@Autowired
	public void setBaseDao(LogDao logDao){
		super.setBaseDao(logDao);
	}
	public void removeAll() {
		List<Log> logs = super.findAll();
		for(int i=0;i<logs.size();i++){
			super.remove(logs.get(i));
		}
	}

	/**
	 * 保存日志
	 * @param logs
	 */
	public void save(List<Log> logs) {
		if(logs.size()>0){
			for(int i = 0;i<logs.size();i++){
				super.save(logs.get(i));
			}
		}
	}

	/**
	 * 保存项目编辑日志
	 * @return
	 */
	public boolean saveLog(Project project, Object object, List<String>  aliases, String name, String type) {
		Log log = new Log();
		List<Unit> units=null;
		if(project.getType()==Project.Type.AGRICULTURAL_IRRIGATION){
			units=unitService.getAgrUnits();
		}
		if(project.getType()==Project.Type.CONSERVATION){
			units=unitService.getConUnits();
		}
		if(project.getType()==Project.Type.DRINGING_WATER){
			units=unitService.getDriUnits();
		}
		if(project.getType()==Project.Type.FLOOD_DEFENCES){
			units=unitService.getFloUnits();
		}
		if(project.getType()==Project.Type.HYDROPOWER_ENGINEERING){
			units=unitService.getHydUnits();
		}
		if(project.getType()==Project.Type.WATER_SUPPLY){
			units=unitService.getWatUnits();
		}
		String content ="于" +sdf.format(System.currentTimeMillis())+"修改：";
		List<String> contents=new ArrayList<String>();
		for (int i = 0; i < units.size(); i++) {
			for (int j = 0; j < units.get(i).getElementGroups().size(); j++) {
				for (int j2 = 0; j2 < units.get(i).getElementGroups().get(j).getElements().size(); j2++) {
					for (String alias : aliases) {
						if(units.get(i).getElementGroups().get(j).getElements().get(j2).getAlias().equals(alias)){
							if(units.get(i).getUnitParent()!=null){
								contents.add(units.get(i).getUnitParent().getName());
							}
							contents.add(units.get(i).getName());
							contents.add(units.get(i).getElementGroups().get(j).getName());
							getElementParent(units.get(i).getElementGroups().get(j).getElements().get(j2),contents);
							contents.add(units.get(i).getElementGroups().get(j).getElements().get(j2).getName());
							contents.add("，");
						}
					}
				}
			}
		}
		for (int i = 0; i < contents.size(); i++) {
			if(contents.get(i).equals("，")){
				content=content.substring(0, content.lastIndexOf("--"));
				content+=contents.get(i);
			}else{
				content+=contents.get(i)+"--";
			}
		}
		if(content.endsWith("--")){
			content=content.substring(0, content.lastIndexOf("--"));
		}
		if(name!=null&&type!=null){
			if(type.toLowerCase().equals("upload")){
				content+="上传："+name+"。";
			}
			if(type.toLowerCase().equals("delete")){
				content+="删除："+name+"。";
			}
			if(type.toLowerCase().equals("download")){
				content+="下载："+name+"。";
			}
			if(type.toLowerCase().equals("new")){
				content+="新建："+name+"。";
			}
			if(type.toLowerCase().equals("edit")){
				content+="编辑："+name+"。";
			}
		}
		String str="";
		for (String alias : aliases) {
			str += alias + ",";
		}
		log.setContent(content);
		log.setProjectId(project.getId());
		if(object instanceof User){
			log.setUserId(((User)object).getId());
		}else{
			log.setAccountId(((Account)object).getId());
		}

		log.setDescription(str);
		save(log);
		return true;
	}
	private void getElementParent(Element element, List<String> contents) {
		if(element.getElementParent()!=null){
			getElementParent(element.getElementParent(), contents);
			contents.add(element.getElementParent().getName());
		}else{
			return;
		}

	}

	/**
	 * 管理员获取当前项目日志并构建logJson
	 * @param id
	 * @return
	 */
	public List<JSONObject> getLogJsonsByProject(Long id){
		List<JSONObject> logJsons = new ArrayList<>();
		List<Log> logs = getLogsByProjectId(id);
		if(logs.size()==0){
			return logJsons;
		}
		List<User> users = userService.findAll();
		List<Account> accounts = accountService.findAll();
		List<JSONObject> userJsons = userService.makeUserJsons(users);
		List<JSONObject> accountJsons = accountService.makeAccountJsons(accounts);
		//构建该日志用户信息
		JSONObject logJson;
		Log log;
		for(int i=0;i<logs.size();i++){
			log = logs.get(i);
			logJson = new JSONObject();
			logJson.put("id",log.getId());
			logJson.put("projectId",log.getProjectId());
			logJson.put("content",log.getContent());
			logJson.put("description",log.getDescription());
			logJson.put("createDate",log.getCreateDate());
			logJson.put("modifyDate",log.getModifyDate());
			logJson.put("id",log.getId());
			if(log.getUserId()!=null){
				for(int k = 0;k<userJsons.size();k++){
					if(log.getUserId().toString().equals(userJsons.get(k).get("id").toString())){
						logJson.put("user",userJsons.get(k));
						break;
					}
			    }
			}
			if(log.getAccountId()!=null){
				for(int k = 0;k<accountJsons.size();k++){
						if(log.getAccountId().toString().equals(accountJsons.get(k).get("id").toString())){
							logJson.put("account",accountJsons.get(k));
							break;
					  }
				  }
		  	}
			logJsons.add(logJson);
		}
		return logJsons;
	}



	/**
	 * 查找对应项目下所有日志
	 * @return
	 */
	private List<Log> getLogsByProjectId(Long id){
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(Filter.eq("projectId", id));
		List<Log> logs = logDao.findList(0,null,filters);
		return logs;
	}
}
