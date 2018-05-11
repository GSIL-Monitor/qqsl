package com.hysw.qqsl.cloud.core.service;

import com.aliyun.oss.model.AccessControlList;
import com.aliyun.oss.model.CannedAccessControlList;
import com.hysw.qqsl.cloud.BaseTest;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.data.Oss;
import com.hysw.qqsl.cloud.core.entity.data.Panorama;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.OssService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class OssServiceTest extends BaseTest {

	Log logger = LogFactory.getLog(getClass());
	@Autowired
	private OssService ossService;
	@Autowired
	private PanoramaService panoramaService;
	@Autowired
	private UserService userService;
	@Test
	public void testSave() throws Exception{
	 Long userId = 1l;
	 Long projectId = 1l;
     Oss oss;
     String treePath = "project/15bd99f835f96cfaa50b3c985b6e494d/"+"2/23/231/231A/";
     for(int i=0;i<100;i++){
     	oss = new Oss(treePath+(int)(Math.random()*100),userId,projectId);
     	ossService.save(oss);
	 }
	 List<Oss> ossList = ossService.findAll();
	 Assert.assertNotNull(ossList);
	}

	@Test
	public void test1(){
		AccessControlList acl = ossService.getClient().getBucketAcl(CommonAttributes.BUCKET_NAME);
		System.out.println(acl.toString());
		ossService.getClient().setBucketAcl(CommonAttributes.BUCKET_NAME, CannedAccessControlList.PublicReadWrite);
		acl = ossService.getClient().getBucketAcl(CommonAttributes.BUCKET_NAME);
		System.out.println(acl.toString());
	}


	/////////////////////////
	@Test
	public void testGetPanoramas(){
		//keys = ossService.getAllPanoramaFromOss(id);
		//String keyStr = "panorama/100/湟水河S.jpg, panorama/101/湟水河T.jpg, panorama/102/湟水河U.jpg, panorama/103/湟水河V.jpg, panorama/104/湟水河W.jpg, panorama/105/甘河A.jpg, panorama/106/甘河B.jpg, panorama/107/甘河C.jpg, panorama/108/甘河D.jpg, panorama/109/甘河F.jpg, panorama/110/甘河G.jpg, panorama/111/甘河H.jpg, panorama/112/甘河E.jpg, panorama/113/海子沟A.jpg, panorama/114/海子沟B.jpg, panorama/115/海子沟C.jpg, panorama/116/巴塘草原站.jpg, panorama/117/多巴卡拉A.jpg, panorama/118/多巴卡拉B.jpg, panorama/119/多巴卡拉C.jpg, panorama/120/多巴卡拉D.jpg, panorama/121/多巴卡拉E.jpg, panorama/122/门源泉口A.jpg, panorama/123/门源泉口B.jpg, panorama/124/门源泉口C.jpg, panorama/125/门源泉口D.jpg, panorama/126/巴米灌区.jpg, panorama/127/巴塘灌区.jpg, panorama/128/冷日灌区上游.jpg, panorama/129/青土灌区上游.jpg, panorama/130/如巴塘灌区上游.jpg, panorama/131/如巴塘灌区下游.jpg, panorama/132/青土灌区下游.jpg, panorama/133/巴塘灌区水源.jpg, panorama/134/冷日灌区上游.jpg, panorama/135/冷日灌区下游.jpg, panorama/136/青土灌区上游.jpg, panorama/138/窑洞村设施农业项目航拍-水源.jpg, panorama/142/窑洞村设施农业项目航拍-中游左岸.jpg, panorama/143/窑洞村设施农业项目航拍-中游右岸.jpg, panorama/144/窑洞村设施农业项目航拍-下游左岸.jpg, panorama/148/德令哈尕海国土东南.jpg, panorama/149/德令哈尕海国土西北.jpg, panorama/150/德令哈尕海国土西南.jpg, panorama/151/北山乡.jpg, panorama/152/青清水利.jpg, panorama/153/选区_019.png, panorama/154/湟水河G.jpg, panorama/155/湟水河D.jpg, panorama/156/湟水河L.jpg, panorama/157/湟水河U.jpg, panorama/158/湟水河V.jpg, panorama/159/湟水河Q.jpg, panorama/160/湟水河E.jpg, panorama/161/湟水河H.jpg, panorama/162/湟水河C.jpg, panorama/163/湟水河R.jpg, panorama/164/湟水河M.jpg, panorama/165/湟水河O.jpg, panorama/166/湟水河S.jpg, panorama/167/湟水河N.jpg, panorama/168/湟水河B.jpg, panorama/169/湟水河A.jpg, panorama/170/湟水河I.jpg, panorama/171/湟水河W.jpg, panorama/172/湟水河P.jpg, panorama/173/湟水河J.jpg, panorama/174/湟水河T.jpg, panorama/175/湟水河K.jpg, panorama/176/湟水河F.jpg, panorama/183/窑洞村设施农业项目航拍-水源.jpg, panorama/184/窑洞村设施农业项目航拍-下游左岸.jpg, panorama/185/大南川A.jpg, panorama/186/大南川B.jpg, panorama/187/大南川C.jpg";

		String keyStr = "panorama/100/湟水河S.jpg";
		List<String> list = Arrays.asList(keyStr.split(","));
		/*System.out.println(list.size());
		ossService.copyToDev(list);	*/
		String useridsStr = "1,16,37,55,6";
		User user;
		for(String str:list){
			String pidstr = str.substring(0,str.lastIndexOf("/"));
			String pid = pidstr.substring(pidstr.lastIndexOf("/")+1);
			Panorama panorama  = panoramaService.find(Long.valueOf(pid));
			user = userService.find(panorama.getUserId());
			String fileName = str.substring(str.lastIndexOf("/")+1,str.lastIndexOf("."));
			String oirginName = System.currentTimeMillis()+".jpg";
			panoramaService.testHistoryData(panorama,user,fileName,oirginName);
		}


	}


	/**
	 * 从qqslimage复制全景原图到qqsl-dev，并将key中的全景id替换为用户id
	 *
	 * 新建全景，切片上传
	 *
	 *
	 * 列出的所有全景key值
	 * panorama/100/湟水河S.jpg, panorama/101/湟水河T.jpg, panorama/102/湟水河U.jpg, panorama/103/湟水河V.jpg, panorama/104/湟水河W.jpg, panorama/105/甘河A.jpg, panorama/106/甘河B.jpg, panorama/107/甘河C.jpg, panorama/108/甘河D.jpg, panorama/109/甘河F.jpg, panorama/110/甘河G.jpg, panorama/111/甘河H.jpg, panorama/112/甘河E.jpg, panorama/113/海子沟A.jpg, panorama/114/海子沟B.jpg, panorama/115/海子沟C.jpg, panorama/116/巴塘草原站.jpg, panorama/117/多巴卡拉A.jpg, panorama/118/多巴卡拉B.jpg, panorama/119/多巴卡拉C.jpg, panorama/120/多巴卡拉D.jpg, panorama/121/多巴卡拉E.jpg, panorama/122/门源泉口A.jpg, panorama/123/门源泉口B.jpg, panorama/124/门源泉口C.jpg, panorama/125/门源泉口D.jpg, panorama/126/巴米灌区.jpg, panorama/127/巴塘灌区.jpg, panorama/128/冷日灌区上游.jpg, panorama/129/青土灌区上游.jpg, panorama/130/如巴塘灌区上游.jpg, panorama/131/如巴塘灌区下游.jpg, panorama/132/青土灌区下游.jpg, panorama/133/巴塘灌区水源.jpg, panorama/134/冷日灌区上游.jpg, panorama/135/冷日灌区下游.jpg, panorama/136/青土灌区上游.jpg, panorama/138/窑洞村设施农业项目航拍-水源.jpg, panorama/142/窑洞村设施农业项目航拍-中游左岸.jpg, panorama/143/窑洞村设施农业项目航拍-中游右岸.jpg, panorama/144/窑洞村设施农业项目航拍-下游左岸.jpg, panorama/148/德令哈尕海国土东南.jpg, panorama/149/德令哈尕海国土西北.jpg, panorama/150/德令哈尕海国土西南.jpg, panorama/151/北山乡.jpg, panorama/152/青清水利.jpg, panorama/153/选区_019.png, panorama/154/湟水河G.jpg, panorama/155/湟水河D.jpg, panorama/156/湟水河L.jpg, panorama/157/湟水河U.jpg, panorama/158/湟水河V.jpg, panorama/159/湟水河Q.jpg, panorama/160/湟水河E.jpg, panorama/161/湟水河H.jpg, panorama/162/湟水河C.jpg, panorama/163/湟水河R.jpg, panorama/164/湟水河M.jpg, panorama/165/湟水河O.jpg, panorama/166/湟水河S.jpg, panorama/167/湟水河N.jpg, panorama/168/湟水河B.jpg, panorama/169/湟水河A.jpg, panorama/170/湟水河I.jpg, panorama/171/湟水河W.jpg, panorama/172/湟水河P.jpg, panorama/173/湟水河J.jpg, panorama/174/湟水河T.jpg, panorama/175/湟水河K.jpg, panorama/176/湟水河F.jpg, panorama/183/窑洞村设施农业项目航拍-水源.jpg, panorama/184/窑洞村设施农业项目航拍-下游左岸.jpg, panorama/185/大南川A.jpg, panorama/186/大南川B.jpg, panorama/187/大南川C.jpg
	 */
}
