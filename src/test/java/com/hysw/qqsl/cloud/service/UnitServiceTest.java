package com.hysw.qqsl.cloud.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.entity.element.ElementGroup;
import com.hysw.qqsl.cloud.entity.element.Unit;
import com.hysw.qqsl.cloud.entity.XMLFileException;
import com.hysw.qqsl.cloud.entity.data.ElementDB;
import com.hysw.qqsl.cloud.entity.data.Project;
import com.hysw.qqsl.cloud.entity.element.Element;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * 
 * @author leinuo  
 *
 * 测试别名是否重复，aliases是否与ElementGroup别名对应，name是否输入错误
 * @date  2016年3月24日
 */
public class UnitServiceTest extends BaseTest {

	Log logger = LogFactory.getLog(getClass());
	@Autowired
	private UnitService unitService;
	@Autowired
	private ElementGroupService elementGroupService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ElementDBService elementDBService;
	//@Autowired
	//private JpaTransactionManager transactionManager;
	
	private final String[] agrXMLs = {"agrProjectModel.xml","agrProjectModelErrorOfAlias.xml","agrProjectModelErrorOfAliases.xml","agrProjectModelErrorOfName.xml","agrProjectModelErrorOfReAlias.xml"};
	private final String[] conXMLs = {"conProjectModel.xml","conProjectModelErrorOfAlias.xml","conProjectModelErrorOfAliases.xml","conProjectModelErrorOfName.xml"};
	private final String[] driXMLs = {"driProjectModel.xml","driProjectModelErrorOfAlias.xml","driProjectModelErrorOfAliases.xml","driProjectModelErrorOfName.xml"}; 
	private final String[] floXMLs = {"floProjectModel.xml","floProjectModelErrorOfAlias.xml","floProjectModelErrorOfAliases.xml","floProjectModelErrorOfName.xml"}; 
	private final String[] hydXMLs = {"hydProjectModel.xml","hydProjectModelErrorOfAlias.xml","hydProjectModelErrorOfAliases.xml","hydProjectModelErrorOfName.xml"}; 
	private final String[] watXMLs = {"watProjectModel.xml","watProjectModelErrorOfAlias.xml","watProjectModelErrorOfAliases.xml","watProjectModelErrorOfName.xml"}; 
	private final String[] elementGroupXMLs = {"agrElementGroup.xml","conElementGroup.xml","driElementGroup.xml","floElementGroup.xml","hydElementGroup.xml","watElementGroup.xml"};
	private final String[] pathPrefixs = {"agrXmlTest","conXmlTest","driXmlTest","floXmlTest","hydXmlTest","watXmlTest"};
	/**每种类型的项目所拥有的单元个数*/
	private final int size[] = {33,31,33,33,31,31};
	/** 设计  */
	private final String unitAlias = "11";
	private final String unitAlias1 = "22";
	/*private final String unitAlias2 = "23";
	private final String unitAlias3 = "231";
	private final String unitAlias4 = "2311";*/
	/**
	 * 单元缓存刷新测试
	 */
	@Test
	public void testRefreshUnitModelXML(){
		List<Unit> agrUnitModels=unitService.getAgrUnitModels();
		assertEquals(agrUnitModels.size(), size[0]);
		List<Unit> conUnitModels=unitService.getConUnitModels();
		assertEquals(conUnitModels.size(), size[1]);
		List<Unit> driUnitModels=unitService.getDriUnitModels();
		assertEquals(driUnitModels.size(), size[2]);
		List<Unit> floUnitModels=unitService.getFloUnitModels();
		assertEquals(floUnitModels.size(), size[3]);
		List<Unit> hydUnitModels=unitService.getHydUnitModels();
		assertEquals(hydUnitModels.size(), size[4]);
		List<Unit> watUnitModels=unitService.getWatUnitModels();
		assertEquals(watUnitModels.size(), size[5]);
		List<Unit> agrUnits=unitService.getAgrUnits();
		assertEquals(agrUnits.size(), size[0]);
		List<Unit> conUnits=unitService.getConUnits();
		assertEquals(conUnits.size(), size[1]);
		List<Unit> driUnits=unitService.getDriUnits();
		assertEquals(driUnits.size(), size[2]);
		List<Unit> floUnits=unitService.getFloUnits();
		assertEquals(floUnits.size(), size[3]);
		List<Unit> hydUnits=unitService.getHydUnits();
		assertEquals(hydUnits.size(), size[4]);
		List<Unit> watUnits=unitService.getWatUnits();
		assertEquals(watUnits.size(), size[5]);
//		unitService.refreshUnitModelXML();
		List<Unit> units = unitService.getAgrModel();
		assertEquals(units, null);

	}
	@Test
	public void testBulidElementDB(){
		List<Unit> agrUnits=unitService.getAgrUnits();
		List<Project> projects = projectService.findAll();
		Project project = projects.get(0); 
		Unit unit = agrUnits.get(1);
		unit.setProject(project);
		unit = unitService.bulidElementDB(unit);
		Element element;
		List<ElementGroup> elementGroups = unit.getElementGroups();
		List<ElementDB> elementDBs = elementDBService.findByProject(unit.getProject().getId());
		if(elementDBs.size()==0){
			return;
		}
		Assert.assertTrue(elementDBs.size()>0);
		for(int i= 0;i<elementGroups.size();i++){
			for(int k = 0;k<elementGroups.get(i).getElements().size();k++){
				elementGroups.get(i).setUnit(unit);
				elementGroups.get(i).setProject(unit.getProject());
				 element = elementGroups.get(i).getElements().get(k);
				for(int t = 0;t<elementDBs.size();t++){
					if(element.getAlias().equals(elementDBs.get(t).getAlias())){
						assertEquals(element.getValue(), elementDBs.get(t).getValue());
						assertEquals(element.getId(), elementDBs.get(t).getId());
					}
				}
			}
		}
	}
	@Test
	public void testUnit(){
		testUnitReAlias();
	}
	@Test
	public void testAgrUnitsModel(){
		testUnitAndElementGroup(agrXMLs,elementGroupXMLs[0],pathPrefixs[0]);
		testUnits(agrXMLs,pathPrefixs[0],size[0]);
	}
	@Test
	public void testconUnitsModel(){
		testUnitAndElementGroup(conXMLs,elementGroupXMLs[1],pathPrefixs[1]);
		testUnits(conXMLs,pathPrefixs[1],size[1]);
	}
	@Test
	public void testDriUnitsModel(){
		testUnitAndElementGroup(driXMLs,elementGroupXMLs[2],pathPrefixs[2]);
		testUnits(driXMLs,pathPrefixs[2],size[2]);
	}
	@Test
	public void testFloUnitsModel(){
		testUnitAndElementGroup(floXMLs,elementGroupXMLs[3],pathPrefixs[3]);
		testUnits(floXMLs,pathPrefixs[3],size[3]);
	}
	
	@Test
	public void testHydUnitsModel(){
		testUnitAndElementGroup(hydXMLs,elementGroupXMLs[4],pathPrefixs[4]);
		testUnits(hydXMLs,pathPrefixs[4],size[4]);
	}
	@Test
	public void testWatUnitsModel(){
		testUnitAndElementGroup(watXMLs,elementGroupXMLs[5],pathPrefixs[5]);
		testUnits(watXMLs,pathPrefixs[5],size[5]);
	}
	/**
	 * 测试单元是否与复合要素一一对应
	 * @param XMLs
	 * @param elementGroupXML
	 */
	private void testUnitAndElementGroup(String[] XMLs,
			String elementGroupXML,String path) {
		List<Unit> units = null;
		try {
			 units = unitService.readProjectModelXML(path+"/"+XMLs[0]);
		} catch (Exception e) {
	        logger.info(e.getMessage());
		}
		List<ElementGroup> elementGroups = elementGroupService.getElementGroups(path+"/"+elementGroupXML);
		try {
			unitAndElementGroup(units, elementGroups);
		} catch (XMLFileException e) {
			logger.info(e.getMessage());
			return;
		}
		fail("应该抛出异常，但是没有抛出");
	}
	public void unitAndElementGroup(List<Unit> units,List<ElementGroup> elementGroups) throws XMLFileException{
		List<String> unitAliases = new ArrayList<String>();
		List<String> elementGroupAliass = new ArrayList<String>();
		String aliases;
		if(units == null){
			System.out.print("1121212");
		}
		for(Unit unit:units){
			aliases = unit.getAliases();
		    if(aliases!=null){
		    	if(aliases.indexOf(",")!=-1){
		    		unitAliases.addAll(Arrays.asList(aliases.split(",")));
		    	}else{
		    		unitAliases.add(aliases);
		    	}
		    }
		}
		for(ElementGroup elementGroup:elementGroups){
			elementGroupAliass.add(elementGroup.getAlias());
		}
		logger.info(unitAliases);
		unitAliases.add("24D");
		logger.info(elementGroupAliass);
		assertEquals(unitAliases.size()-1, elementGroupAliass.size());
		for(int i = 0;i<unitAliases.size();i++){
			if(!elementGroupAliass.contains(unitAliases.get(i))){
				logger.info(unitAliases.get(i));
				throw new XMLFileException("单元与复合要素不对应！");
			}
		}
		for(int i = 0;i<elementGroupAliass.size();i++){
			if(!unitAliases.contains(elementGroupAliass.get(i))){
				logger.info(unitAliases.get(i));
				throw new XMLFileException("复合要素与单元不对应！");
			}
		}
		
	}
	public void testUnits(String[] XMLs,String path,int except){
		testRightUnits(XMLs,path,except);
		testUnitAlias(XMLs,path,except);
		testUnitAliases(XMLs,path,except);
		testUnitName(XMLs,path,except);
		
	}
	public void testRightUnits(String[] XMLs,String path,int except){
		List<Unit> units = null;
		try {
			units = unitService.readProjectModelXML(path+"/"+XMLs[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(except,units.size());
	}
	
	public void testUnitAlias(String[] XMLs,String path,int except){
		List<Unit> units ;
		try {
			units = unitService.readProjectModelXML(path+"/"+XMLs[1]);
			assertEquals(except, units.size());
		} catch (Exception e) {
			logger.info(e.getMessage());
			return;
		}
		fail("应该抛出异常，但是没有抛出");
	}
	
	
	
	public void testUnitAliases(String[] XMLs,String path,int except){
		List<Unit> units;
		try {
			units = unitService.readProjectModelXML(path+"/"+XMLs[2]);
			assertEquals(except, units.size());
		} catch (Exception e) {
			logger.info(e.getMessage());
			return;
		}
		fail("应该抛出异常，但是没有抛出");
	}
	
	
	public void testUnitName(String[] XMLs,String path,int except){
		List<Unit> units;
		try {
			units = unitService.readProjectModelXML(path+"/"+XMLs[3]);
			assertEquals(except, units.size());
		} catch (Exception e) {
			logger.info(e.getMessage());
			return;
		}
		fail("应该抛出异常，但是没有抛出");
	}

	@Test
	public void testUnitReAlias(){
		List<Unit> units;
		try {
			units = unitService.readProjectModelXML("agrXmlTest/" +agrXMLs[4]);
			assertEquals(size[0], units.size());
		} catch (Exception e) {
			logger.info(e.getMessage());
			return;
		}
		fail("应该抛出异常，但是没有抛出");
	}

	@Test
	public void testFindUnit(){
		List<Project> projects = projectService.findAll();
		Project project = projects.get(0);
		Unit unit = unitService.findUnit(unitAlias1, false, project);
		assertEquals(unit.getAlias(), unitAlias1);
		Unit unit2 = unitService.findUnit(unitAlias, true, project);
		assertEquals(unit2.getAlias(), unitAlias);
		assertEquals(1, unit2.getElementGroups().size());
		//assertEquals(unit2.getElementGroups().get(0).getElements().get(1).getValue(), "甲级");
	}

	/**
	 *
	 */
	@Test
	public void testMakeUnitJson(){
		Unit unit = new Unit();
		JSONObject unitJson = unitService.makeUnitJson(unit);
	}
	
}
