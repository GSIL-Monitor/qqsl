package com.hysw.qqsl.cloud.util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
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
import java.net.URL;
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


    /**
     * 根据坐标获取具体地址
     * @param coor 坐标字符串
     * @return
     */
    public static String getAdd(String coor){
        //lat 小  log  大
        //参数解释: 纬度,经度 type 001 (100代表道路，010代表POI，001代表门址，111可以同时显示前三项)
       // String urlString = "http://gc.ditu.aliyun.com/regeocoding?l="+lat+","+log+"&type=111";
        String urlString = "http://restapi.amap.com/v3/geocode/regeo?key=8325164e247e15eea68b59e89200988b&s=rsv3&location="+coor+"&radius=2800&callback=jsonp_452865_&platform=JS&logversion=2.0&sdkversion=1.3&appname=http%3A%2F%2Flbs.amap.com%2Fconsole%2Fshow%2Fpicker&csid=49851531-2AE3-4A3B-A8C8-675A69BCA316";
        //String urlString = "http://restapi.amap.com/v3/place/text?s=rsv3&children=&key=8325164e247e15eea68b59e89200988b&page=1&offset=10&city=610100&language=zh_cn&callback=jsonp_25126_&platform=JS&logversion=2.0&sdkversion=1.3&appname=http%3A%2F%2Flbs.amap.com%2Fconsole%2Fshow%2Fpicker&csid=19FA0D45-180F-4D45-BCB4-C6C265F55FF6&keywords="+address;
        String res = "";
        try {
            //http://restapi.amap.com/v3/geocode/regeo?key=8325164e247e15eea68b59e89200988b&s=rsv3&location=101.539737903028,36.79828256329313&radius=2800&callback=jsonp_452865_&platform=JS&logversion=2.0&sdkversion=1.3&appname=http%3A%2F%2Flbs.amap.com%2Fconsole%2Fshow%2Fpicker&csid=49851531-2AE3-4A3B-A8C8-675A69BCA316
            URL url = new URL(urlString);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(),"UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                res += line+"\n";
            }
            in.close();
        } catch (Exception e) {
            System.out.println("error in wapaction,and e is " + e.getMessage());
        }
        return res;
    }




    public static void main(String[] agrs) {
        // lat 31.2990170   纬度
        //log 121.3466440    经度
        String add = getAdd("101.539737903028,36.79828256329313");
        String json = add.substring(add.indexOf("(")+1,add.lastIndexOf(")"));
        JSONObject jsonObject = JSONObject.fromObject(json);
        jsonObject = JSONObject.fromObject(jsonObject.get("regeocode"));
        System.out.println(jsonObject.getString("formatted_address"));
      /*  JSONArray jsonArray = JSONArray.fromObject(jsonObject.getString("addrList"));
        JSONObject j_2 = JSONObject.fromObject(jsonArray.get(0));
        String allAdd = j_2.getString("admName");
        String str= allAdd.replaceAll(",","");
        System.out.println(str);*/
        //{"longitude":"","latitude":"32.88323333321792","elevation":"4405.429021279564"}
       // {"longitude":"101.50464694444672","latitude":"36.7285541666189","elevation":"2705.48780033033"}




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
