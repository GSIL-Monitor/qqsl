package com.hysw.qqsl.cloud.krpano.service;

import com.hysw.qqsl.cloud.core.service.OssService;
import com.hysw.qqsl.cloud.krpano.entity.Scene;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 全景编辑
 * Create by leinuo on 18-3-13 下午4:09
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
@Service("krpanoService")
public class KrpanoService {

    @Autowired
    private OssService ossService;

   /* public static void main(String[] args) {
        String file = "353";
        String title = "yyyyyyyy";
        String temppath = "/home/leinuo/pic/";
        //   String music = "vshow/backgroundmusic/default.mp3";
        try {
            setKrpano(file, temppath);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }
    }*/

    public boolean slice(String path)
            throws InterruptedException {
        File targetFile = new File(path);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        String ex = "/home/leinuo/soft/krpano-1.19-pr14/krpanotools makepano -config=templates/vtour-normal.config "
                + path + "/*.jpg";
        Runtime runtime = Runtime.getRuntime();
        String[] cmd = new String[]{"/bin/sh", "-c", ex};
        boolean b = true;
        Process p = null;
        try {
            p = runtime.exec(cmd);
        } catch (Exception e) {
            b = false;
        }finally {
            p.waitFor();
            p.destroy();
        }
        return b;
    }

    /**
     * 从oss下载全景图片并保存到临时目录
     *
     * @param key
     */
    public boolean downloadPicture(String key) {
        InputStream inputStream = ossService.downloadFile("qqslimage", key);
        String fileName = key.substring(key.lastIndexOf("/")+1);
        File file = new File("/home/leinuo/pic/"+fileName);
        OutputStream outputStream = null;
        boolean flag = true;
        try {
            outputStream = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }finally {
            try {
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return flag;

    }

    /**
     * 制作全景
     * @param key
     */
    public void makeKrpano(String key){
       boolean flag = downloadPicture(key);
       if(flag){
           try {
               flag = slice("\"/home/leinuo/pic/\"");
               if (flag){
                   uploadPanos("/home/leinuo/pic/vtour/panos/");
               }
           } catch (InterruptedException e) {
               e.printStackTrace();
           }

       }
    }

    /**
     * 将临时目录pic下的生成的全景文件夹panos上传至oss
     */
    public void uploadPanos(String path){
        if(new File(path).exists()){
            
        }
    }

    public String getTour(){
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
        //传入参数
        List<Scene> scenes = getScenes();
        context.put("scenes", scenes);
        context.put("includeUrl","skin/vtourskin.xml1");
        try {
            //生成xml
            StringWriter sw = new StringWriter();
            template.merge(context,sw);
            System.out.println(sw.toString());
            sw.flush();
            sw.close();
            return sw.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }


    private List<Scene> getScenes(){
        List<Scene> scenes = new ArrayList<>();
        Scene scene;
        for(int i=0;i<4;i++){
            scene = new Scene();
            scene.setName("全景测试"+i);
            scene.setTitle("全景测试"+i);
            scene.setThumbUrl("panos/webwxgetmsgimg.tiles/thumb.jpg");
            scene.setLat("36.59055500");
            scene.setLng("101.45179444");
            scene.setHeading("0.0");
            scene.setHlookat("0.0");
            scene.setVlookat("0.0");
            scene.setFovtype("MFOV");
            scene.setFov("120");
            scene.setMaxpixelzoom("2.0");
            scene.setFovmin("70");
            scene.setFovmax("140");
            scene.setLimitview("auto");
            scene.setPreviewUrl("panos/webwxgetmsgimg.tiles/preview.jpg");
            scene.setPrealign("0|0.0|0");
            scene.setCubeUrl("panos/webwxgetmsgimg.tiles/pano_%s.jpg");
            scenes.add(scene);
        }
        return scenes;
    }
}
