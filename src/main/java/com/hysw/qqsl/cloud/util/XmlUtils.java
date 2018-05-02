package com.hysw.qqsl.cloud.util;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.SAXException;

import java.io.*;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by leinuo on 17-3-30.
 */
public class XmlUtils {

    /**
     * 读取xml文件
     *
     * @param path
     * @return
     * @throws DocumentException
     */
    public Document readXml(String path) throws DocumentException {

        File file = null;
        try {
            System.out.println(path);
            file = new ClassPathResource(path).getFile();
            System.out.print(file.getPath());
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
        return doc;
    }

    /**
     * 保存xml文件
     *
     * @param path
     * @param document
     * @throws Exception
     */
    public void saveXml(String path, Document document) throws Exception {
        XMLWriter xmlWriter = new XMLWriter(new FileWriter(
                path));
        xmlWriter.write(document);
        xmlWriter.close();

    }

    /**
     * 编辑xml节点
     */
    public void editXml(Document document) {
        Element root, element;
        List<Element> elements;
        try {
            // 获取根节点
            root = document.getRootElement();
            elements = SettingUtils.getInstance().getElementGroupList(root);
            // 遍历elements的子节点
            System.out.println(elements.size());
            for (int i = 0; i < elements.size(); i++) {
                element = elements.get(i);
                   /* if(element.attributeValue("type")!=null){
                        element.addAttribute("status",element.attributeValue("type"));
                        Attribute attribute = element.attribute("type");
                        element.remove(attribute);
                    }*/
                   /* if(element.attributeValue("name")==null){
                        element.addAttribute("name","结构属性");
                    }*/
                i = i + 1;
                element.addAttribute("alias", i + "");
                i = i - 1;
                readChilds(element);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readChilds(Element element) {
        List<Element> elements;
        Element elementChild;
        elements = element.elements();
        for (int i = 0; i < elements.size(); i++) {
            elementChild = elements.get(i);
            if (elementChild.elements().size() > 0) {
                i = i + 1;
                elementChild.addAttribute("alias", element.attributeValue("alias") + i);
                i = i - 1;
                /*if(elementChild.attributeValue("type")!=null){
                    elementChild.addAttribute("status",elementChild.attributeValue("type"));
                    Attribute attribute = elementChild.attribute("type");
                    elementChild.remove(attribute);
                }else{
                  //  elementChild.addAttribute("status","normal");
                }*/
                readChilds(elementChild);
            } else {
                i = i + 1;
                elementChild.addAttribute("alias", elementChild.getName().substring(0, 1).toUpperCase() + element.attributeValue("alias") + i);
                if (elementChild.attributeValue("type") != null) {
                    // if(elementChild.attributeValue("unit").toString().equals("m")||elementChild.attributeValue("unit").toString().equals("个")){
                    //   elementChild.attribute("type").setValue("number");
                    //  }
                }
                i = i - 1;

            }
        }

    }

    public void xmlEdit(String path) {
        try {
            Document document = readXml(path);
            editXml(document);
            saveXml("/home/leinuo/projects1/" + path, document);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }







    public void getTour(){
        VelocityEngine ve = new VelocityEngine();
        //设置vm模板的装载路径
        Properties prop = new Properties();
        //设置编码
        prop.setProperty(Velocity.ENCODING_DEFAULT, "UTF-8");
        prop.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
        prop.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
        ve.setProperty("resource.loader", "class");
        ve.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        ve.init(prop);
        //得到模板文件
        Template template = ve.getTemplate("velocityTemp/tour.vm", "UTF-8");
        VelocityContext context = new VelocityContext();

        context.put("cubeUrl", "panos/webwxgetmsgimg.tiles/pano_%s.jpg");
        try {
            //生成xml
            FileWriter fw = getFileWriter("");
            template.merge(context,fw);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static FileWriter getFileWriter(String fileName) throws IOException {
        String fullPath = MessageFormat.format("{1}{0}{2}",
                File.separator,
                "/home/leinuo/pic",
                fileName);
        System.out.println("fileName = " + fullPath);
        File outputFile = new File(fullPath);
        return new FileWriter(outputFile);
    }

















    public static void main(String[] agrs) {
        //   XmlUtils xmlUtils = new XmlUtils();
        //  xmlUtils.xmlEdit("buildsGeology.xml");
        //  xmlUtils.xmlEdit("buildsDimension.xml");
        // xmlUtils.xmlEdit("buildsHydraulics.xml");
        // xmlUtils.xmlEdit("buildsMater.xml");
        //     xmlUtils.xmlEdit("buildsStructure.xml");
       /* String str = "a,b,c";
        List<String> strs = Arrays.asList(str.split(","));
        System.out.println(strs);
        String str1 = "a";
        List<String> strs1 = Arrays.asList(str1.split(","));
        System.out.println(strs1);
        String str2 = "";
        List<String> strs2 = Arrays.asList(str2.split(","));
        System.out.println(strs2);*/

        String key = "panorama/26/15252256025839ge.jpg";
        String key2 = "http://qqslimage.oss-cn-hangzhou.aliyuncs.com/panorama/26/15252256025839ge.tiles/thumb.jpg";
        System.out.println(key2.substring(0,key2.lastIndexOf("/")));
        String key3 = key2.substring(0,key2.lastIndexOf("/"));
        System.out.println(key3.substring(key3.lastIndexOf(".")));
        String prefix = key.replace(key.substring(key.lastIndexOf(".")+1),key3.substring(key3.lastIndexOf(".")+1));

        System.out.println(prefix.substring(prefix.indexOf("/")+1)+"/");


   /*     DecimalFormat df1 = new DecimalFormat("#.00");
        DecimalFormat df2 = new DecimalFormat("######0.00");
        double d1 = 1.0;
        double d2 = 5;
        double d3 = 0.11;
        double d4 = 1.11;
        System.out.println("df1:" + df1.format(d1) + " pk " + df2.format(d1));
        System.out.println("df1:" + df1.format(d2) + " pk " + df2.format(d2));
        System.out.println("df1:" + df1.format(d3) + " pk " + df2.format(d3));
        System.out.println("df1:" + df1.format(d4) + " pk " + df2.format(d4));
        List<String> list = new ArrayList<>();
        list.add("11");
        list.add("55");
        list.add("sw");
        list.add("33");
        list.add("2sw");
        System.out.println(list.size() + ":" + list);
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String string = iterator.next();
            if (string.equals("11")) {
                iterator.remove();
            }
            if (string.equals("sw")) {
                iterator.remove();
            }
        }
        System.out.println(list.size() + ":" + list);

        ArrayList<Integer> list1 = new ArrayList<Integer>();
        list1.add(2);
        Iterator<Integer> iterator1 = list1.iterator();
        while (iterator1.hasNext()) {
            Integer integer = iterator1.next();
            if (integer == 2)
                iterator.remove();   //注意这个地方
        }*/
    }
}
