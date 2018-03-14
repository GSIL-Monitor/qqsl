package com.hysw.qqsl.cloud.core.service;


import com.hysw.qqsl.cloud.core.dao.ElementDBDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.ElementDB;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * 单元Service
 *
 * @since 2015年9月9日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
@Service("elemenDBService")
public class ElementDBService extends BaseService<ElementDB, Long> {

	@Autowired
	private ElementDBDao elementDBDao;
	@Autowired
	private NoteService noteService;
//	private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	@Autowired
	public void setBaseDao(ElementDBDao elementDao) {
		super.setBaseDao(elementDao);
	}
	
	@Override
	public ElementDB find(Long id) {
		ElementDB elementDB = super.find(id);
		return elementDB; 
	}
	
	/**
	 * 确定项目下唯一的要素
	 * @param projectId
	 * @param alias
	 * @return
	 */
	public List<ElementDB> findByProject(Long projectId,String alias){
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(Filter.eq("project", projectId));
		filters.add(Filter.eq("alias", alias));
		List<ElementDB> ElementDBs = elementDBDao.findList(0, null, filters);
		if(ElementDBs.size()!=1){
		return new ArrayList<ElementDB>();	
		}
		return ElementDBs;
	}
	
	/**
	 * 获取项目中心点坐标
	 * @param projectId
	 * @param aliass
	 * @return
	 */
	public List<ElementDB> findProjectCenter(Long projectId,List<String> aliass){
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(Filter.eq("project", projectId));
		filters.add(Filter.in("alias", aliass));
		List<ElementDB> elementDBs = elementDBDao.findList(0, null, filters);
		if(elementDBs.size()!=1&&elementDBs.size()!=2){
			return new ArrayList<ElementDB>();
		}
		return elementDBs;
	}
	/**
	 * 获取项目下所有要素
	 * @param projectId
	 * @return
	 */
	public List<ElementDB> findByProject(Long projectId){
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(Filter.eq("project", projectId));
		List<ElementDB> ElementDBs = elementDBDao.findList(0, null, filters);
		return ElementDBs;
	}
	/** 
	 * 保存elementDB
	 */
	public List<ElementDB> findNoteElementDB(Long projectId) {
		List<String> aliases = noteService.getNoteAlias();
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(Filter.eq("project", projectId));
		filters.add(Filter.in("alias",aliases));
		List<ElementDB> ElementDBs = elementDBDao.findList(0, null, filters);
		return ElementDBs;
	}

	/**
	 * 通过父节点找到子节点要素
	 * @param projectId
	 * @param childAliass
     * @return
     */
	public List<ElementDB> findElementDBChilds(Long projectId,List<String> childAliass){
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(Filter.eq("project", projectId));
		filters.add(Filter.in("alias",childAliass));
		List<ElementDB> ElementDBs = elementDBDao.findList(0, null, filters);
		return ElementDBs;
	}

	/**
	 * test
	 * @param childAliass
	 * @return
	 */
	public List<ElementDB> findElementDBs(List<String> childAliass){
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(Filter.in("alias",childAliass));
		List<ElementDB> ElementDBs = elementDBDao.findList(0, null, filters);
		return ElementDBs;
	}

	public List<ElementDB> findByProjectAndAlias(Project project) {
		List<Filter> filters = new ArrayList<>();
		filters.add(Filter.eq("project", project));
		filters.add(Filter.eq("alias", "21A1"));
		List<ElementDB> list = elementDBDao.findList(0, null, filters);
		return list;
	}
}
