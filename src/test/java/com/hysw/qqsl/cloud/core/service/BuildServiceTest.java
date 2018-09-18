package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.QQSLException;
import com.hysw.qqsl.cloud.core.entity.builds.AttributeGroup;
import com.hysw.qqsl.cloud.core.entity.builds.SheetObject;
import com.hysw.qqsl.cloud.core.entity.data.Build;
import com.hysw.qqsl.cloud.core.entity.data.BuildAttribute;
import com.hysw.qqsl.cloud.core.entity.data.Project;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONObject;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by leinuo on 17-4-13.
 */
public class BuildServiceTest extends BaseTest {

    @Autowired
    private BuildService buildService;
    @Autowired
    private BuildGroupService buildGroupService;
    @Autowired
    private AttributeGroupService attributeGroupService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private CoordinateService coordinateService;
    @Autowired
    private BuildAttributeService buildAttributeService;
    @Test
    public void save(){
        Build build = new Build();
        build.setAlias("11");
        Project project = projectService.find(531l);
        build.setProject(project);
        build.setType(CommonEnum.CommonType.QS);
        build.setName("泉室");
        buildService.save(build);
    }



    private void add(List<BuildAttribute> attributes, List<BuildAttribute> attributeList){
        for(int i=0;i<attributeList.size();i++){
            attributes.add(attributeList.get(i));
        }
    }
  private void save(List<BuildAttribute> attributes,Build build){
        for (int i=0;i<attributes.size();i++){
            attributes.get(i).setBuild(build);
            if(attributes.get(i).getType().equals(BuildAttribute.Type.SELECT)){
                String value = attributes.get(i).getSelects().get(0);
                attributes.get(i).setValue(value);
            }else{
                attributes.get(i).setValue("test");
            }
            buildAttributeService.save(attributes.get(i));
        }
    }


    private void getAttributeGroup(List<BuildAttribute> attributes,BuildAttribute attribute){
        for(int j=0;j<attributes.size();j++){
            if(attributes.get(j).getAlias().equals(attribute.getAlias())){
                attributes.get(j).setValue(attribute.getValue());
                break;
            }
        }
    }


    @Test
    public void testBuildJson(){
        Build build = buildService.find(6106l);
        JSONObject jsonObject = buildService.buildJson(build);
        Assert.assertNotNull(jsonObject);
    }

    @Test
    public void getNewBuildModels() throws QQSLException, IOException {
        Workbook wb = new XSSFWorkbook();
        List<Build> builds = buildService.initBuildModel(SettingUtils.getInstance().getSetting().getBuild());
        buildService.outBuildModel(wb, builds);
        OutputStream outputStream = new FileOutputStream(new File("783.xlsx"));
        wb.write(outputStream);
        outputStream.flush();
    }

    @Test
    public void inputBuilds() throws Exception {
        Project project = projectService.find(868l);
        Map<String, Workbook> wbs = new HashMap<>();
        SheetObject sheetObject = new SheetObject();
        File file = new ClassPathResource("model.xls").getFile();
        InputStream is = new FileInputStream(file);
        coordinateService.readExcels(is,file.getName().substring(file.getName().lastIndexOf(".")+1),file.getName(),new JSONObject(),wbs);
        coordinateService.getAllSheet(wbs,sheetObject);
        Map<String, List<Build>> stringListMap = buildService.inputBuilds(sheetObject.getBuildWBs(), project);
        System.out.println();
    }

    @Test
    public void outputBuilds(){
        Build build = buildService.find(20168l);
        Build build1 = buildService.find(20167l);
        List<Build> builds = new ArrayList<>();
        builds.add(build);
        builds.add((Build) SettingUtils.objectCopy(build));
        builds.add(build1);
//        buildService.outputBuilds(builds);
    }

    @Test
    public void test0003() throws DocumentException, IOException {
        File file = null;
        try {
            file = new ClassPathResource(SettingUtils.getInstance().getSetting().getComponent()).getFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SAXReader reader = new SAXReader();
        try {
            reader.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                    false);
        } catch (SAXException e1) {
            e1.printStackTrace();
        }
        Document doc = reader.read(file);
        Element elem = doc.getRootElement();
        test0004(elem);
        saveDocument(doc,new File("D:\\qqsl\\src\\main\\resources\\5.xml"));
    }

    public void test0004(Element root){
        List<Element> elements = SettingUtils.getInstance().getElementList(root);
        int i = 1;
        for (Element element : elements) {
            i = addAttribute(element.elements(), i);
        }
    }

    private int addAttribute(List<Element> elements, int i) {
        String a = null;
        for (Element element : elements) {
            if (element.getName().equals("attributeGroup")) {
                i = addAttribute(element.elements(), i);
            }
            if (i < 10) {
                a = "ct000" + i;
            } else if (i < 100) {
                a = "ct00" + i;
            } else if (i < 1000) {
                a = "ct0" + i;
            } else if (i < 10000) {
                a = "ct" + i;
            }
            Attribute alias = element.attribute("alias");
            if (alias == null) {
                continue;
            }
            alias.setValue(a);
            i++;
        }
        return i;
    }

    public static void saveDocument(Document document, File xmlFile) throws IOException {
        Writer osWrite = new OutputStreamWriter(new FileOutputStream(xmlFile));// 创建输出流
        OutputFormat format = OutputFormat.createPrettyPrint(); // 获取输出的指定格式
        format.setEncoding("UTF-8");// 设置编码 ，确保解析的xml为UTF-8格式
        XMLWriter writer = new XMLWriter(osWrite, format);// XMLWriter
        // 指定输出文件以及格式
        writer.write(document);// 把document写入xmlFile指定的文件(可以为被解析的文件或者新创建的文件)
        writer.flush();
        writer.close();
    }
}