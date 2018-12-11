package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.element.ElementGroup;
import com.hysw.qqsl.cloud.core.entity.XMLFileException;
import com.hysw.qqsl.cloud.core.service.ElementGroupService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ElementGroupServiceTest extends BaseTest {

	@Autowired
	private ElementGroupService elementGroupService;
	//路径前缀
	private final String[] pathPrefixs = {"agrXmlTest","conXmlTest","driXmlTest","floXmlTest","hydXmlTest","watXmlTest"};
	private final int[] exceptSizes = {75,65,72,71,65,65};
	private final String[]  agrXMLs = {"agrElementGroup.xml","agrElementGroupErrorOfAlias.xml","agrElementGroupErrorOfElementGroupAlias.xml","agrElementGroupErrorOfDataType.xml",
			"agrElementGroupErrorOfDescriptionIntroduce.xml","agrElementGroupErrorOfInfoOrder.xml","agrElementGroupErrorOfType.xml","agrElementGroupErrorOfDescription.xml",
			"agrElementGroupErrorOfReDescription.xml"};
	private final String[]  conXMLs = {"conElementGroup.xml","conElementGroupErrorOfAlias.xml","conElementGroupErrorOfElementGroupAlias.xml","conElementGroupErrorOfDataType.xml",
			"conElementGroupErrorOfDescriptionIntroduce.xml","conElementGroupErrorOfInfoOrder.xml","conElementGroupErrorOfType.xml","conElementGroupErrorOfDescription.xml",
			"conElementGroupErrorOfReDescription.xml"};
	private final String[]  driXMLs = {"driElementGroup.xml","driElementGroupErrorOfAlias.xml","driElementGroupErrorOfElementGroupAlias.xml","driElementGroupErrorOfDataType.xml",
			"driElementGroupErrorOfDescriptionIntroduce.xml","driElementGroupErrorOfInfoOrder.xml","driElementGroupErrorOfType.xml","driElementGroupErrorOfDescription.xml",
			"driElementGroupErrorOfReDescription.xml"};
	private final String[]  floXMLs ={"floElementGroup.xml","floElementGroupErrorOfAlias.xml","floElementGroupErrorOfElementGroupAlias.xml","floElementGroupErrorOfDataType.xml",
			"floElementGroupErrorOfDescriptionIntroduce.xml","floElementGroupErrorOfInfoOrder.xml","floElementGroupErrorOfType.xml","floElementGroupErrorOfDescription.xml",
			"floElementGroupErrorOfReDescription.xml"};
	private final String[]  hydXMLs = {"hydElementGroup.xml","hydElementGroupErrorOfAlias.xml","hydElementGroupErrorOfElementGroupAlias.xml","hydElementGroupErrorOfDataType.xml",
			"hydElementGroupErrorOfDescriptionIntroduce.xml","hydElementGroupErrorOfInfoOrder.xml","hydElementGroupErrorOfType.xml","hydElementGroupErrorOfDescription.xml",
			"hydElementGroupErrorOfReDescription.xml"};
	private final String[]  watXMLs = {"watElementGroup.xml","watElementGroupErrorOfAlias.xml","watElementGroupErrorOfElementGroupAlias.xml","watElementGroupErrorOfDataType.xml",
			"watElementGroupErrorOfDescriptionIntroduce.xml","watElementGroupErrorOfInfoOrder.xml","watElementGroupErrorOfType.xml","watElementGroupErrorOfDescription.xml",
			"watElementGroupErrorOfReDescription.xml"};

	public List<ElementGroup> getElementGroup(String XMLName) throws XMLFileException{
			return elementGroupService.makeElementGroup(XMLName);
	}
	/**
	 * 缓存读取与刷新测试
	 */
	@Test
	public void testRefreshElementGroupXML(){
		List<ElementGroup> agrElementGroups = elementGroupService.getAgrElementGroups();
		List<ElementGroup> watElementGroups = elementGroupService.getWatElementGroups();
		assertEquals(agrElementGroups.size(), 68);
		assertEquals(watElementGroups.size(), 68);
		List<ElementGroup> conElementGroups = elementGroupService.getConElementGroups();
		List<ElementGroup> hydElementGroups = elementGroupService.getHydElementGroups();
		assertEquals(conElementGroups.size(), hydElementGroups.size());
		List<ElementGroup> driElementGroups = elementGroupService.getDriElementGroups();
		List<ElementGroup> floElementGroups = elementGroupService.getFloElementGroups();
		assertEquals(driElementGroups.size(),68);
		assertEquals(floElementGroups.size(),68);
		ElementGroup elementGroup = new ElementGroup();
		elementGroup.setName("test");
		agrElementGroups.add(elementGroup);
//		elementGroupService.refreshElementGroupXML();
		assertEquals(agrElementGroups.size(),69);
	    List<ElementGroup> elementGroups = elementGroupService.getAgrElementGroups();
	    assertEquals(elementGroups.size(),69);
	}
	@Test
	public void testMakeElementGroup1() {
		List<ElementGroup> elementGroupModels = new ArrayList<ElementGroup>();
		try {
			elementGroupModels = getElementGroup(floXMLs[0]);
		} catch (XMLFileException e) {
			e.printStackTrace();
		}
		assertEquals(68, elementGroupModels.size());
	}

	@Test
	public void testAgrElementGroupXML(){
		String pathPrefix = pathPrefixs[0];
		int exceptSize = exceptSizes[0];
		testXML(exceptSize,pathPrefix,agrXMLs);
	}
	@Test
	public void testConElementGroupXML(){
		String pathPrefix = pathPrefixs[1];
		int exceptSize = exceptSizes[1];
		testXML(exceptSize,pathPrefix,conXMLs);
	}
	@Test
	public void testDriElementGroupXML(){
		String pathPrefix = pathPrefixs[2];
		int exceptSize = exceptSizes[2];
		testXML(exceptSize,pathPrefix,driXMLs);
	}
	@Test
	public void testFloElementGroupXML(){
		String pathPrefix = pathPrefixs[3];
		int exceptSize = exceptSizes[3];
		testXML(exceptSize,pathPrefix,floXMLs);
	}
	@Test
	public void testHydElementGroupXML(){
		String pathPrefix = pathPrefixs[4];
		int exceptSize = exceptSizes[4];
		testXML(exceptSize,pathPrefix,hydXMLs);
	}
	@Test
	public void testWatElementGroupXML(){
		String pathPrefix = pathPrefixs[5];
		int exceptSize = exceptSizes[5];
		testXML(exceptSize,pathPrefix,watXMLs);
	}

	public void testXML(int exceptSize,String pathPrefix,String[] XMLName){
		testMakeElementGroup(exceptSize,pathPrefix,XMLName);
		testXMLElementAlias(pathPrefix,XMLName);
		testXMLElementGroupAlias(pathPrefix,XMLName);
		testElementDataType(pathPrefix,XMLName);
		testXMLDescriptionIntroduce(pathPrefix,XMLName);
		testElementInfoOrder(pathPrefix,XMLName);
		testElementType(pathPrefix,XMLName);
		testElementDescription(pathPrefix,XMLName);
		testElementReDescription(pathPrefix,XMLName);
	}

	public void testMakeElementGroup(int exceptSize,String pathPrefix,String[] XMLName) {
		List<ElementGroup> elementGroupModels = new ArrayList<ElementGroup>();
		try {
			elementGroupModels = getElementGroup(pathPrefix+"/"+XMLName[0]);
		} catch (XMLFileException e) {
			e.printStackTrace();
		}
		assertEquals(exceptSize, elementGroupModels.size());
	}

	public void testXMLElementAlias(String pathPrefix,String[] XMLName){
		List<ElementGroup> elementGroupModels = new ArrayList<ElementGroup>();
		try {
			elementGroupModels = getElementGroup(pathPrefix+"/"+XMLName[1]);
		} catch (XMLFileException e) {
			e.printStackTrace();
			assertEquals(0, elementGroupModels.size());
			logger.info(e.getMessage()+":"+XMLName[1]);
			return;
		}
		fail("应该抛出异常，但是没有抛出");
	}
	
	public void testXMLElementGroupAlias(String pathPrefix,String[] XMLName){
		List<ElementGroup> elementGroupModels = new ArrayList<ElementGroup>();
		try {
			elementGroupModels = getElementGroup(pathPrefix+"/"+XMLName[2]);
		} catch (XMLFileException e) {
			e.printStackTrace();
			assertEquals(0, elementGroupModels.size());
			logger.info(e.getMessage()+":"+XMLName[2]);
			return;
		}
		fail("应该抛出异常，但是没有抛出");
	}

	public void testElementDataType(String pathPrefix,String[] XMLName){
		List<ElementGroup> elementGroupModels = new ArrayList<ElementGroup>();
		try {
			elementGroupModels = getElementGroup(pathPrefix+"/"+XMLName[3]);
		} catch (XMLFileException e) {
			e.printStackTrace();
			assertEquals(0, elementGroupModels.size());
			logger.info(e.getMessage()+":"+XMLName[3]);
			return;
		}
		fail("应该抛出异常，但是没有抛出");
	}

	public void testXMLDescriptionIntroduce(String pathPrefix,String[] XMLName){
		List<ElementGroup> elementGroupModels = new ArrayList<ElementGroup>();
		try {
			elementGroupModels = getElementGroup(pathPrefix+"/"+XMLName[4]);
		} catch (XMLFileException e) {
			e.printStackTrace();
			assertEquals(0, elementGroupModels.size());
			logger.info(e.getMessage()+":"+XMLName[4]);
			return;
		}
		fail("应该抛出异常，但是没有抛出");
	}

	public void testElementInfoOrder(String pathPrefix,String[] XMLName){
		List<ElementGroup> elementGroupModels = new ArrayList<ElementGroup>();
		try {
			elementGroupModels = getElementGroup(pathPrefix+"/"+XMLName[5]);
		} catch (XMLFileException e) {
			e.printStackTrace();
			assertEquals(0, elementGroupModels.size());
			logger.info(e.getMessage()+":"+XMLName[5]);
			return;
		}
		fail("应该抛出异常，但是没有抛出");
	}

	public void testElementType(String pathPrefix,String[] XMLName){
		List<ElementGroup> elementGroupModels = new ArrayList<ElementGroup>();
		try {
			elementGroupModels = getElementGroup(pathPrefix+"/"+XMLName[6]);
		} catch (XMLFileException e) {
			e.printStackTrace();
			assertEquals(0, elementGroupModels.size());
			logger.info(e.getMessage()+":"+XMLName[6]);
			return;
		}
		fail("应该抛出异常，但是没有抛出");
	}

	public void testElementDescription(String pathPrefix,String[] XMLName){
		List<ElementGroup> elementGroupModels = new ArrayList<ElementGroup>();
		try {
			elementGroupModels = getElementGroup(pathPrefix+"/"+XMLName[7]);
		} catch (XMLFileException e) {
			e.printStackTrace();
			assertEquals(0, elementGroupModels.size());
			logger.info(e.getMessage()+":"+XMLName[7]);
			return;
		}
		fail("应该抛出异常，但是没有抛出");
	}
	

		public void testElementReDescription(String pathPrefix,String[] XMLName){
			List<ElementGroup> elementGroupModels = new ArrayList<ElementGroup>();
			try {
				elementGroupModels = getElementGroup(pathPrefix+"/"+XMLName[8]);
			} catch (XMLFileException e) {
				e.printStackTrace();
				assertEquals(0, elementGroupModels.size());
				logger.info(e.getMessage()+":"+XMLName[8]);
				return;
			}
			fail("应该抛出异常，但是没有抛出");
		}

	public void testElementDescription1(){
		List<ElementGroup> elementGroupModels = new ArrayList<ElementGroup>();
		try {
			elementGroupModels = getElementGroup(watXMLs[8]);
		} catch (XMLFileException e) {
			e.printStackTrace();
			assertEquals(0, elementGroupModels.size());
			logger.info(e.getMessage()+":"+watXMLs[8]);
			return;
		}
		fail("应该抛出异常，但是没有抛出");
	}
	//@Test
	public void testAgrAliasMatch() throws XMLFileException {
		List<ElementGroup> elementGroup = elementGroupService
				.makeElementGroup("agrElementGroup.xml");
		int d=0;
		for (int i = 0; i < elementGroup.size(); i++) {
			for (int j = 0; j < elementGroup.get(i).getElements().size(); j++) {
				String b=elementGroup.get(i).getElements().get(j).getAlias();
				String a = elementGroup.get(i).getAlias();
				int c=a.length();
			
				if(!a.equals(b.substring(0, c))){
					logger.info(a+":"+b);
					d++;
				}
			}
		}
		logger.info(d);
		Assert.assertTrue(d==0);
	}
	
	//@Test
	public void testConAliasMatch() throws XMLFileException {
		List<ElementGroup> elementGroup = elementGroupService
				.makeElementGroup("conElementGroup.xml");
		int d=0;
		for (int i = 0; i < elementGroup.size(); i++) {
			for (int j = 0; j < elementGroup.get(i).getElements().size(); j++) {
				String b=elementGroup.get(i).getElements().get(j).getAlias();
				String a = elementGroup.get(i).getAlias();
				int c=a.length();
			
				if(!a.equals(b.substring(0, c))){
					logger.info(a+":"+b);
					d++;
				}
			}
		}
		logger.info(d);
		Assert.assertTrue(d==0);
	}
	
	//@Test
	public void testDriAliasMatch() throws XMLFileException {
		List<ElementGroup> elementGroup = elementGroupService
				.makeElementGroup("driElementGroup.xml");
		int d=0;
		for (int i = 0; i < elementGroup.size(); i++) {
			for (int j = 0; j < elementGroup.get(i).getElements().size(); j++) {
				String b=elementGroup.get(i).getElements().get(j).getAlias();
				String a = elementGroup.get(i).getAlias();
				int c=a.length();
			
				if(!a.equals(b.substring(0, c))){
					logger.info(a+":"+b);
					d++;
				}
			}
		}
		logger.info(d);
		Assert.assertTrue(d==0);
	}
	
	//@Test
	public void testFloAliasMatch() throws XMLFileException {
		List<ElementGroup> elementGroup = elementGroupService
				.makeElementGroup("floElementGroup.xml");
		int d=0;
		for (int i = 0; i < elementGroup.size(); i++) {
			for (int j = 0; j < elementGroup.get(i).getElements().size(); j++) {
				String b=elementGroup.get(i).getElements().get(j).getAlias();
				String a = elementGroup.get(i).getAlias();
				int c=a.length();
			
				if(!a.equals(b.substring(0, c))){
					logger.info(a+":"+b);
					d++;
				}
			}
		}
		logger.info(d);
		Assert.assertTrue(d==0);
	}
	
	//@Test
	public void testHydAliasMatch() throws XMLFileException {
		List<ElementGroup> elementGroup = elementGroupService
				.makeElementGroup("hydElementGroup.xml");
		int d=0;
		for (int i = 0; i < elementGroup.size(); i++) {
			for (int j = 0; j < elementGroup.get(i).getElements().size(); j++) {
				String b=elementGroup.get(i).getElements().get(j).getAlias();
				String a = elementGroup.get(i).getAlias();
				int c=a.length();
			
				if(!a.equals(b.substring(0, c))){
					logger.info(a+":"+b);
					d++;
				}
			}
		}
		logger.info(d);
		Assert.assertTrue(d==0);
	}

	//@Test
	public void testWatAliasMatch() throws XMLFileException {
		List<ElementGroup> elementGroup = elementGroupService
				.makeElementGroup("watElementGroup.xml");
		int d=0;
		for (int i = 0; i < elementGroup.size(); i++) {
			for (int j = 0; j < elementGroup.get(i).getElements().size(); j++) {
				String b=elementGroup.get(i).getElements().get(j).getAlias();
				String a = elementGroup.get(i).getAlias();
				int c=a.length();
			
				if(!a.equals(b.substring(0, c))){
					logger.info(a+":"+b);
					d++;
				}
			}
		}
		logger.info(d);
		Assert.assertTrue(d==0);
	}
}
