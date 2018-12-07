package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.core.entity.data.Panorama;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
//import org.springframework.test.annotation.Rollback;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * @anthor Administrator
 * @since 9:36 2018/4/4
 */
public class PanoramaServiceTest extends BaseTest {
    @Autowired
    private PanoramaService panoramaService;
    @Autowired
    private OssService ossService;
    @Test
    public void testCreatePanorama() throws IOException, InterruptedException {
        String str = "{\"name\":\"全景名称1111111111\",\"info\":\"全景描述2222222222222222\",\"coor\":\"103.77645101765913,36.05377593481913,0\",\"isShare\":\"true\",\"region\":\"中国甘肃省兰州市七里河区兰工坪南街190号 邮政编码: 730050\",\"images\":[{\"name\":\"001-西宁\", \"fileName\":\"1522811870947bik.jpg\"},{\"name\":\"333-西安\",\"fileName\":\"152281187095756l.jpg\"}]}";
        Map<String, Object> map =JSONObject.fromObject(str);
        User user = new User();
        user.setId(26l);
        JSONObject jsonObject1 = SettingUtils.checkCoordinateIsInvalid(map.get("coor").toString());
        panoramaService.addPanorama(map.get("name"),jsonObject1,map.get("region"),map.get("isShare"),map.get("info"),map.get("images"), new Panorama(), user);
    }

//    @Test
//    public void testDownloadFileToLocal(){
//        ossService.downloadFileToLocal("26/1522811870947bik.jpg", "1522811870947bik.jpg");
//    }

//    @Test
//    public void testEXEC() throws IOException {
//        Runtime.getRuntime().exec("C:\\Users\\Administrator\\Desktop\\krpano-1.19-pr10-win\\MAKE VTOUR (MULTIRES) droplet.bat D:\\qqsl\\out\\production\\resources\\panorama\\1522811870947bik.jpg");
//    }

    @Test
    public void createRandomDir(){
        panoramaService.createRandomDir();
    }

    @Test
    public void bianliwenjiajia() throws FileNotFoundException {
        String fileName = "adc7f83b537755272ce0837318f08135";
        String str = "D:\\qqsl\\out\\production\\resources\\panorama\\" + fileName + "\\vtour\\panos";
        User user = new User();
        user.setId(17l);
        panoramaService.traverseFolder(str,str.length(),user);

    }

    //    @Test
//    public void createFile() throws IOException {
//        File file = new File("/resources/1231.txt");
//        FileWriter fw = new FileWriter(file, false);
//        BufferedWriter bw = new BufferedWriter(fw);
//        bw.write("0000000000000000000");
//        bw.flush();
//        bw.close();
//        fw.close();
//    }
    String path;

    @Test
    public void getPath() {
        getTargetFilePath();
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
        path = path.substring(0, path.lastIndexOf("\\"));
        File file = new File(path+"\\panorama");
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @Test
    public void test(){
        Properties props=System.getProperties(); //获得系统属性集
        String osName = props.getProperty("os.name"); //操作系统名称
        if (osName.toLowerCase().contains("windows")) {
            System.out.println("windows");
        }  else if (osName.toLowerCase().contains("linux")) {
            System.out.println("linux");
        }
        String path = this.getClass().getClassLoader().getResource("/").getPath();
        System.out.println();
    }

    //@Test
    public void dataClear(){
        List<Panorama> panoramas = panoramaService.findAll();
        for(int i = 0;i<panoramas.size();i++){
            logger.info(panoramas.get(i).getInstanceId());
            panoramaService.delete(panoramas.get(i));
        }
    }
}
