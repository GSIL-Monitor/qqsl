package com.hysw.qqsl.cloud.core.service;

import com.aliyun.oss.OSSException;
import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.dao.PanoramaDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.ObjectFile;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Panorama;
import com.hysw.qqsl.cloud.core.entity.data.Scene;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;

/**
 * Created by chenl on 17-1-9.
 */
@Service("panoramaService")
public class PanoramaService extends BaseService<Panorama, Long> {
    @Autowired
    private PanoramaDao panoramaDao;
    @Autowired
    private OssService ossService;
    @Autowired
    private UserService userService;
    @Autowired
    private SceneService sceneService;
    @Autowired
    private AccountService accountService;
    private String path;

    @Autowired
    public void setBaseDao(PanoramaDao panoramaDao) {
        super.setBaseDao(panoramaDao);
    }

    /**
     * 查询所有审核通过的全景和用户自己建立的全景
     * @return
     */
    public List<Panorama> findAllPass(Object object){
        List<Filter> filters1 = new ArrayList<>();
        filters1.add(Filter.eq("status", CommonEnum.Review.PASS));
        filters1.add(Filter.eq("share", true));
        List<Panorama> panoramas;
        if (object == null) {
            panoramas = panoramaDao.findList(0, null, filters1);
        }else{
            List<Filter> filters2 = new ArrayList<>();
            if (object instanceof User) {
                filters2.add(Filter.eq("userId", ((User) object).getId()));
            } else if (object instanceof Account) {
                filters2.add(Filter.eq("accountId", ((Account) object).getId()));
            }
            panoramas = panoramaDao.findList(0, null, filters1,filters2);
        }
        return panoramas;
    }


    public JSONArray panoramasToJson(List<Panorama> panoramas){
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < panoramas.size(); i++) {
            jsonObject = new JSONObject();
            jsonObject.put("id", panoramas.get(i).getId());
            jsonObject.put("createDate", panoramas.get(i).getCreateDate());
            jsonObject.put("modifyDate", panoramas.get(i).getModifyDate());
            jsonObject.put("name", panoramas.get(i).getName());
            jsonObject.put("coor", panoramas.get(i).getCoor());
            jsonObject.put("region", panoramas.get(i).getRegion());
            jsonObject.put("status", panoramas.get(i).getStatus());
            jsonObject.put("advice", panoramas.get(i).getAdvice());
            jsonObject.put("reviewDate", panoramas.get(i).getReviewDate());
            jsonObject.put("isShare", panoramas.get(i).getShare());
            List<ObjectFile> objectFiles= ossService
                    .getSubdirectoryFiles("panorama" + "/" +panoramas.get(i).getId(),"qqslimage");
            jsonObject.put("pictures", objectFiles);
            jsonObject.put("user", userJson(panoramas.get(i).getUserId()));
            jsonObject.put("instanceId", panoramas.get(i).getInstanceId());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    private JSONObject userJson(Long userId) {
        User user = userService.find(userId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("nickName", userService.nickName(userId));
        jsonObject.put("id", user.getId());
        jsonObject.put("phone", user.getPhone());
        return jsonObject;
    }


    /**
     * 查询所有待审核的全景
     * @return
     */
    public List<Panorama> findAllPending() {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("status", CommonEnum.Review.PENDING));
       // filters.add(Filter.eq("share", true));
        List<Panorama> panoramas = panoramaDao.findList(0, null, filters);
        return panoramas;
    }

    public JSONObject savePanorama(Map<String, Object> map, Panorama panorama) {
        Object name = map.get("name");
        Object coor = map.get("coor");
        Object region = map.get("region");
        Object status = map.get("status");
//        Object advice = map.get("advice");
//        Object reviewDate = map.get("reviewDate");
        Object isShare = map.get("isShare");
        Object picture = map.get("picture");
        Object userId = map.get("userId");
        Object shootDate = map.get("shootDate");
        if (name == null || coor == null || isShare == null) {
            return null;
        }
        JSONObject jsonObject1 = SettingUtils.checkCoordinateIsInvalid(coor.toString());
        if (jsonObject1 == null) {
            return null;
        }
        panorama.setName(name.toString());
        panorama.setCoor(jsonObject1.toString());
       // panorama.setRegion(region.toString());
        panorama.setStatus(CommonEnum.Review.valueOf(Integer.valueOf(status.toString())));
        panorama.setShare(Boolean.valueOf(isShare.toString()));
        panorama.setUserId(Long.valueOf(userId.toString()));
        save(panorama);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", panorama.getId());
        return jsonObject;
    }

    public List<Panorama> findByUser(User user) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("userId", user.getId()));
        List<Panorama> panoramas = panoramaDao.findList(0, null, filters);
        return panoramas;
    }

    public JSONObject panoramaToJson(Panorama panorama){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", panorama.getId());
        jsonObject.put("createDate", panorama.getCreateDate());
        jsonObject.put("modifyDate", panorama.getModifyDate());
        jsonObject.put("name", panorama.getName());
        jsonObject.put("coor", panorama.getCoor());
        jsonObject.put("region", panorama.getRegion());
        jsonObject.put("status", panorama.getStatus());
        jsonObject.put("advice", panorama.getAdvice());
        jsonObject.put("reviewDate", panorama.getReviewDate());
        jsonObject.put("isShare", panorama.getShare());
        List<ObjectFile> objectFiles;
        if (SettingUtils.getInstance().getSetting().getStatus().equals("run")) {
            objectFiles= ossService
                    .getSubdirectoryFiles("panorama" + "/" +panorama.getId(),"qqslimage");
        }else{
            objectFiles= ossService
                    .getSubdirectoryFiles("panorama_test" + "/" +panorama.getId(),"qqslimage");
        }
        jsonObject.put("pictures", objectFiles);
        jsonObject.put("user", userJson(panorama.getUserId()));
        return jsonObject;
    }


/**************************************************************************************************************************/
    /**
     * 添加全景
     * @param name
     * @param jsonObject
     * @param region
     * @param isShare
     * @param info
     * @param images
     * @param panorama
     * @param object
     */

    public String addPanorama(Object name, JSONObject jsonObject, Object region, Object isShare, Object info, Object images, Panorama panorama, Object object) {
        panorama.setStatus(CommonEnum.Review.PENDING);
        panorama.setCoor(jsonObject.toString());
        panorama.setName(name.toString());
        panorama.setShare(Boolean.valueOf(isShare.toString()));
        User user = null;
        if (object instanceof User) {
            user = (User) object;
        }else if(object instanceof Account){
//            子账户与主账户改成一对多后，查询user
            user = accountService.find(((Account) object).getId()).getUser();
            panorama.setAccountId(((Account) object).getId());
        }
        panorama.setUserId(user.getId());
        panorama.setRegion(region.toString());
        panorama.setInfo(info.toString());
        panorama.setInstanceId(DigestUtils.md5Hex(String.valueOf(System.currentTimeMillis())));
        panorama.setAngleOfView("{\"viewSettings\": []}");
        panorama.setSceneGroup("{\"sceneGroups\": []}");
        panorama.setHotspot("{}");
        String thumbUrl = null;
        if (images != null) {
            if (path == null || path.length() == 0) {
                getTargetFilePath();
            }
            File randomFile = createRandomDir();
            List<Map<String,String>> images1 = (List<Map<String,String>>) images;
            List<String> paths = downloadPicture(user, images1, randomFile);
            if (paths == null || paths.size() == 0) {
                return "PANORAMA_IMAGE_NOT_EXIST";//下载失败
            }
            boolean flag= cutPicture(paths);
            if (!flag) {
                return "PANORAMA_SLICE_ERROE";//图片切割失败
            }
            uploadCutPicture(randomFile.getName(),user);
            thumbUrl = sceneService.saveScene(user, panorama, images1);
            delAllFile(path);
            panorama.setThumbUrl(thumbUrl);
            save(panorama);
            return "OK";
        }
        return "PANORAMA_NO_SCENE";
    }

    /**
     * 下载图片
     *  @param images
     * @param randomFile
     */
    private List<String> downloadPicture(User user, List<Map<String,String>> images, File randomFile) {
        List<String> paths= new ArrayList<>();
        for (Map<String, String> image : images) {
            Object fileName = image.get("fileName");
            try {
                paths.add(ossService.downloadFileToLocal(user.getId() + "/" + fileName, randomFile.getAbsolutePath() + System.getProperty("file.separator") + fileName.toString()));
            } catch (OSSException e) {
                continue;
            }
        }
        return paths;
    }

    /**
     * 删除所有文件夹
     * @param path
     * @return
     */
    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    /**
     *删除文件夹
     */
    public static void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 上传切割图片
     * @param fileName
     */
    private boolean uploadCutPicture(String fileName,User user) {
        String str = path+System.getProperty("file.separator") + fileName + System.getProperty("file.separator")+"vtour"+System.getProperty("file.separator")+"panos";
        try {
            traverseFolder(str,str.length(),user);
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * 获取目标文件path
     */
    private void getTargetFilePath() {
        try {
            path = new ClassPathResource("qqsl.xml").getFile().getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        path = path.substring(0, path.lastIndexOf(System.getProperty("file.separator"))) + System.getProperty("file.separator")+"panorama";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 切割图片
     * @param paths
     */
    private boolean cutPicture(List<String> paths){
        String cmd =getOsName();
        for (String s : paths) {
            cmd += " " + s;
        }
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmd);
            readCommandInfo(p);
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    /**
     * 循环上传切片文件
     * @param path
     * @param l
     * @throws FileNotFoundException
     */
    public void traverseFolder(String path,int l,User user) throws FileNotFoundException {
        File file = new File(path);
        File[] files;
        if (file.isFile()) {
            String str=file.getAbsolutePath();
            String str1 = str.substring(l+1, str.length());
            str1 = "panorama/"+user.getId()+"/" + str1;
            str1 = str1.replace("\\", "/");
            ossService.uploadImage(str1, new FileInputStream(file), null);
        }else if (file.isDirectory()) {
            files = file.listFiles();
            for (File file1 : files) {
                traverseFolder(file1.getAbsolutePath(),l,user);
            }
        }
    }

    protected File createRandomDir(){
        UUID uuid = UUID.randomUUID();
        File file = new File(path+System.getProperty("file.separator")+DigestUtils.md5Hex(uuid.toString()));
        file.mkdir();
        return file;
    }

    /**
     * 接收命令行输入信息流与错误信息流
     * @param p
     * @throws InterruptedException
     */
    private void readCommandInfo(Process p) throws InterruptedException {
        InputStream is1 = p.getInputStream();
        InputStream is2 = p.getErrorStream();
        new Thread() {
            public void run() {
                BufferedReader br1 = new BufferedReader(new InputStreamReader(is1));
                try {
                    String line1 = null;
                    while ((line1 = br1.readLine()) != null) {
                        if (line1 != null){
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally{
                    try {
                        is1.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        new Thread() {
            public void  run() {
                BufferedReader br2 = new  BufferedReader(new  InputStreamReader(is2));
                try {
                    String line2 = null ;
                    while ((line2 = br2.readLine()) !=  null ) {
                        if (line2 != null){
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally{
                    try {
                        is2.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        p.waitFor();
        p.destroy();
    }

    public String getOsName(){
        String cmd = "";
        Properties props=System.getProperties(); //获得系统属性集
        String osName = props.getProperty("os.name"); //操作系统名称
        String osUserName = props.getProperty("user.name");
        if (osName.toLowerCase().contains("windows")) {
            cmd = "D:\\krpano\\make.bat";
        }  else if (osName.toLowerCase().contains("linux")) {
            if(osUserName.equals("leinuo")){
                cmd ="/home/leinuo/soft/krpano-1.19-pr14/krpanotools makepano -config=templates/vtour-normal.config";
            }else {
                cmd = "/home/qqsl/krpano/krpanotools makepano -config=templates/vtour-normal.config";
            }
        }
        return cmd;
    }

/////////////////////////////////////////////////////////////////////////////////

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
        Panorama panorama = findByInstanceId(instanceId);
        if(panorama==null){
            context.put("status", "4021");
            return getString(template,context);
        }
        List<Scene> scenes = panorama.getScenes();
        context.put("status", scenes==null?"4101":"200");
        context.put("scenes",scenes);
        if(scenes==null){
            context.put("prefixPath","");
            context.put("afterPath",".tiles");
        }else {
            String path = scenes.get(0).getThumbUrl().substring(0,scenes.get(0).getThumbUrl().lastIndexOf("/"));
            String prefixPath = path.substring(0,path.lastIndexOf("/"));
            context.put("prefixPath",prefixPath);
            context.put("afterPath",".tiles");
        }
        context.put("skinPath","skin.xml");
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
        Panorama panorama = findByInstanceId(instsnceId);
        JSONObject panoramaJson = new JSONObject();
        if(panorama == null){
            return panoramaJson;
        }
        JSONObject jsonObject = new JSONObject();
        panoramaJson.put("hotSpot",panorama.getHotspot()==null?jsonObject:JSONObject.fromObject(panorama.getHotspot()));
        panoramaJson.put("advice",panorama.getAdvice());
        panoramaJson.put("id",panorama.getId());
        //  panoramaJson.put("cdnHost",panoramaConfig.getCdnHost());
        panoramaJson.put("createDate",panorama.getCreateDate());
        panoramaJson.put("angleOfView",panorama.getAngleOfView()==null?jsonObject:JSONObject.fromObject(panorama.getAngleOfView()));
        panoramaJson.put("coor",panorama.getCoor()==null?"":JSONObject.fromObject(panorama.getCoor()));
        panoramaJson.put("instanceId",panorama.getInstanceId());
        panoramaJson.put("info",panorama.getInfo());
        panoramaJson.put("thumbUrl",StringUtils.hasText(panorama.getThumbUrl())?panorama.getThumbUrl():"");
        panoramaJson.put("status",panorama.getStatus());
        panoramaJson.put("name",panorama.getName());
        panoramaJson.put("reviewDate",panorama.getReviewDate());
        panoramaJson.put("region",panorama.getRegion());
        panoramaJson.put("sceneGroup",StringUtils.hasText(panorama.getSceneGroup())?JSONObject.fromObject(panorama.getSceneGroup()):jsonObject);
        panoramaJson.put("scenes",sceneService.getScenes(panorama.getScenes()));
        return panoramaJson;
    }

    private Panorama findByInstanceId(String instsnceId) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("instanceId",instsnceId));
        List<Panorama> panoramas = panoramaDao.findList(0,null,filters);
        if(panoramas.size()==1){
            return panoramas.get(0);
        }
        return null;
    }

    /**
     * 获取panoramas的json
     * @param panoramas
     * @return
     */
    public JSONArray panoramasToJsonNoScene(List<Panorama> panoramas){
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        for (Panorama panorama : panoramas) {
            jsonObject = new JSONObject();
            jsonObject.put("instanceId", panorama.getInstanceId());
            jsonObject.put("name", panorama.getName());
            jsonObject.put("advice", panorama.getAdvice());
            jsonObject.put("coor", panorama.getCoor());
            jsonObject.put("id",panorama.getId());
            jsonObject.put("region", panorama.getRegion());
            jsonObject.put("reviewDate", panorama.getReviewDate()==null?"":panorama.getReviewDate()==null);
            jsonObject.put("status", panorama.getStatus());
            jsonObject.put("isShare", panorama.getShare());
            jsonObject.put("info", panorama.getInfo());
            jsonObject.put("thumbUrl",StringUtils.hasText(panorama.getThumbUrl())?panorama.getThumbUrl():"");
            jsonObject.put("createDate", panorama.getCreateDate().getTime());
            jsonObject.put("modifyDate", panorama.getModifyDate().getTime());
            jsonObject.put("sceneGroup",panorama.getSceneGroup());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public List<Panorama> findByAccount(Account account) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("accountId", account.getId()));
        List<Panorama> panoramas = panoramaDao.findList(0, null, filters);
        return panoramas;
    }

    /**
     * 获取panoramas的json
     * @param panoramas
     * @return
     */
    public JSONArray panoramasToJsonHaveScene(List<Panorama> panoramas){
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        for (Panorama panorama : panoramas) {
            jsonObject = new JSONObject();
            jsonObject.put("name", panorama.getName());
            jsonObject.put("instanceId", panorama.getInstanceId());
            jsonObject.put("id", panorama.getId());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /**
     * 获取panoramas的json
     * @param panoramas
     * @return
     */
    public JSONArray panoramasToJsonAdmin(List<Panorama> panoramas){
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        for (Panorama panorama : panoramas) {
            jsonObject = new JSONObject();
            jsonObject.put("instanceId", panorama.getInstanceId());
            jsonObject.put("name", panorama.getName());
            jsonObject.put("coor", panorama.getCoor());
            jsonObject.put("region", panorama.getRegion());
            jsonObject.put("status", panorama.getStatus());
            jsonObject.put("info", panorama.getInfo());
            jsonObject.put("thumbUrl",StringUtils.hasText(panorama.getThumbUrl())?panorama.getThumbUrl():"");
            jsonObject.put("createDate", panorama.getCreateDate().getTime());
            jsonObject.put("modifyDate", panorama.getModifyDate().getTime());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    /**
     * 全景基本信息修改
     * @param panorama
     * @param objectMap
     * @return
     */
    public String update(Panorama panorama,Map<String, Object> objectMap) {
        //参数：id,全景id，name，info，isShare，coor，region,
        if(objectMap.get("instanceId")!=null&&StringUtils.hasText(objectMap.get("instanceId").toString())){
            panorama.setInstanceId(objectMap.get("instanceId").toString());
        }
        if(objectMap.get("name")!=null&&StringUtils.hasText(objectMap.get("name").toString())){
            panorama.setName(objectMap.get("name").toString());
        }
        if(objectMap.get("info")!=null&&StringUtils.hasText(objectMap.get("info").toString())){
            panorama.setInfo(objectMap.get("info").toString());
        }
        if(objectMap.get("isShare")!=null&&StringUtils.hasText(objectMap.get("isShare").toString())){
            panorama.setShare((Boolean)objectMap.get("isShare"));
        }
        if(objectMap.get("coor")!=null&&StringUtils.hasText(objectMap.get("coor").toString())){
            panorama.setCoor(objectMap.get("coor").toString());
        }
        if(objectMap.get("region")!=null&&StringUtils.hasText(objectMap.get("region").toString())){
            panorama.setRegion(objectMap.get("region").toString());
        }
        panorama.setStatus(CommonEnum.Review.PENDING);
        panoramaDao.save(panorama);
        return  Message.Type.OK.toString();
    }

    /**
     * 热点更新
     * @param panorama
     * @param objectMap
     * @return
     */
    public String updateHotspot(Panorama panorama,Map<String, Object> objectMap) {
        //参数: id,全景id，angleOfView,起始视角json, hotspot,热点json,sceneGroup,场景顺序
        if(objectMap.get("instanceId")!=null&&StringUtils.hasText(objectMap.get("instanceId").toString())){
            panorama.setInstanceId(objectMap.get("instanceId").toString());
        }
        if(objectMap.get("angleOfView")!=null&&StringUtils.hasText(objectMap.get("angleOfView").toString())){
            panorama.setAngleOfView(objectMap.get("angleOfView").toString());
        }
        if(objectMap.get("hotspot")!=null&&StringUtils.hasText(objectMap.get("hotspot").toString())){
            panorama.setInfo(objectMap.get("info").toString());
        }
        if(objectMap.get("sceneGroup")!=null&&StringUtils.hasText(objectMap.get("sceneGroup").toString())){
            panorama.setSceneGroup(objectMap.get("sceneGroup").toString());
        }
        panorama.setStatus(CommonEnum.Review.PENDING);
        panoramaDao.save(panorama);
        return  Message.Type.OK.toString();
    }

    public void delete(Panorama panorama) {
        panoramaDao.remove(panorama);
    }

    /**
     * 全景审核
     * @param map
     * @param flag
     * @return
     */
    public String review(Map<String, Object> map, boolean flag) {
        Panorama panorama = panoramaDao.find(Long.valueOf(map.get("id").toString()));
        if(panorama == null){
            return Message.Type.DATA_NOEXIST.toString();
        }
        //审核通过
        if(flag){
           panorama.setStatus(CommonEnum.Review.PASS);
           panorama.setReviewDate(new Date());
           panoramaDao.save(panorama);
        }else{
            panorama.setStatus(CommonEnum.Review.NOTPASS);
            panorama.setReviewDate(new Date());
            panorama.setAdvice(map.get("advice").toString());
            panoramaDao.save(panorama);
        }
        return Message.Type.OK.toString();
    }

    /**
     * 获取需要审核的全景列表
     * @return
     */
    public JSONArray getPanoramas() {
        //每个全景只传递，name，instanceId, info, thumbUrl, coor, region
        List<Panorama> panoramas = findAllPending();
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        Panorama panorama;
        if(panoramas.isEmpty()){
            return jsonArray;
        }
        for(int i = 0;i<panoramas.size();i++){
            panorama = panoramas.get(i);
            jsonObject = new JSONObject();
            jsonObject.put("name",panorama.getName());
            jsonObject.put("instanceId",panorama.getInstanceId());
            jsonObject.put("id", panorama.getId());
            jsonObject.put("thumbUrl",StringUtils.hasText(panorama.getThumbUrl())?panorama.getThumbUrl():"");
            jsonObject.put("coor",panorama.getCoor());
            jsonObject.put("region",panorama.getRegion());
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

}
