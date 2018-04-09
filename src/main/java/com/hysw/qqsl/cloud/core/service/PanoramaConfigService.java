package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.PanoramaConfigDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.PanoramaConfig;
import com.hysw.qqsl.cloud.core.entity.data.Scene;
import net.sf.json.JSONObject;
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
 * Create by leinuo on 18-3-28 上午9:42
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
@Service("panoramaConfigService")
public class PanoramaConfigService extends BaseService<PanoramaConfig,Long> {

    @Autowired
    private PanoramaConfigDao panoramaConfigDao;
    @Autowired
    private SceneService sceneService;
    @Autowired
    private OssService ossService;

    @Autowired
    public void setBaseDao(PanoramaConfigDao panoramaConfigDao) {
        super.setBaseDao(panoramaConfigDao);
    }


    /**
     * 获取全景tour.xml文件
     * @return
     */
    public String getTour(String instanceId){
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
        PanoramaConfig panoramaConfig = findByInstanceId(instanceId);
        if(panoramaConfig==null){
            context.put("status", "4021");
            return getString(template,context);
        }
        List<Scene> scenes = panoramaConfig.getScenes();
        context.put("status", scenes==null?"4101":"200");
        context.put("scenes",scenes);
        context.put("prefixPath","https://qqslimage.oss-cn-hangzhou.aliyuncs.com/1/works/");
        context.put("skinPath","/skin.xml");
        return getString(template,context);
    }

    /**
     * 获取全景皮肤vtouskin.xml文件
     * @return
     */
    public String getSkin(){
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
        Template template = ve.getTemplate("velocityTemp/vtourskin.vm", "UTF-8");
        VelocityContext context = new VelocityContext();
        return getString(template,context);
    }

    public String getString(Template template ,VelocityContext context){
        try {
            //生成xml
            StringWriter sw = new StringWriter();
            template.merge(context,sw);
            //  System.out.println(sw.toString());
            sw.flush();
            sw.close();
            return sw.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public JSONObject get(String instsnceId){
        PanoramaConfig panoramaConfig = findByInstanceId(instsnceId);
        JSONObject panoramaJson = new JSONObject();
        panoramaJson.put("hotSpot",JSONObject.fromObject(panoramaConfig.getHotspot()));
        panoramaJson.put("advice",panoramaConfig.getAdvice());
        panoramaJson.put("id",panoramaConfig.getId());
      //  panoramaJson.put("cdnHost",panoramaConfig.getCdnHost());
        panoramaJson.put("createDate",panoramaConfig.getCreateDate());
        panoramaJson.put("angleOfView",JSONObject.fromObject(panoramaConfig.getAngleOfView()));
        panoramaJson.put("coor",JSONObject.fromObject(panoramaConfig.getCoor()));
        panoramaJson.put("instanceId",panoramaConfig.getInstanceId());
        panoramaJson.put("info",panoramaConfig.getInfo());
        panoramaJson.put("thumbUrl",panoramaConfig.getThumbUrl());
        panoramaJson.put("status",panoramaConfig.getStatus());
        panoramaJson.put("name",panoramaConfig.getName());
        panoramaJson.put("reviewDate",panoramaConfig.getReviewDate());
        panoramaJson.put("name",panoramaConfig.getRegion());
        panoramaJson.put("region",panoramaConfig.getName());
       // panoramaJson.put("scenes",panoramaConfig.getScenes());
        return panoramaJson;
    }

    private PanoramaConfig findByInstanceId(String instsnceId) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("instanceId",instsnceId));
        List<PanoramaConfig> panoramaConfigs = panoramaConfigDao.findList(0,null,filters);
        if(panoramaConfigs.size()==1){
            return panoramaConfigs.get(0);
        }
        return null;
    }

    /**
     * 新建全景
     */
    public void create(){

    }


    /**
     * 使用krpano插件生成切片
     * @param path
     * @return
     * @throws InterruptedException
     */
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


    /**
     * 递归上传文件
     */


}
