package com.hysw.qqsl.cloud.core.service;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.common.utils.IOUtils;
import com.aliyun.oss.model.*;
import com.aliyun.oss.model.LifecycleRule.RuleStatus;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.dao.OssDao;
import com.hysw.qqsl.cloud.core.entity.ObjectFile;
import com.hysw.qqsl.cloud.core.entity.data.Oss;
import com.hysw.qqsl.cloud.core.entity.data.Panorama;
import com.hysw.qqsl.cloud.core.entity.data.Scene;
import com.hysw.qqsl.cloud.core.entity.data.User;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.*;

/**
 * 阿里云OSS服务
 *
 * @since 2015年7月27日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
@Service("ossService")
public class OssService extends BaseService<Oss,Long>{

	@Autowired
	private OssDao ossDao;
	@Autowired
	private PanoramaService panoramaService;
	private Log logger = LogFactory.getLog(getClass());
	private String qqslBucketName;
	private String imageBucketName;
	private OSSClient client;
	private AssumeRoleResponse response = null;
	private String stsToken = null;
	private List<String> unitNames = Arrays.asList(CommonAttributes.GET_FILE
			.split(","));
	private List<String> extensiones = Arrays
			.asList(CommonAttributes.OFFICE_FILE_EXTENSION.split(","));
	private List<String> picturePrefixs = Arrays.asList(CommonAttributes.PICTURE_FILE_EXTENSION.split(","));
	private static OssService ossService = null;

	@Autowired
	public void setBaseDao(OssDao ossDao){
		super.setBaseDao(ossDao);
	}

	public OssService() {
		this.client = new OSSClient(CommonAttributes.END_POINT,
				CommonAttributes.ACCESSKEY_ID,
				CommonAttributes.SECRET_ACCESSKEY);
		this.qqslBucketName = CommonAttributes.BUCKET_NAME;
		this.imageBucketName = CommonAttributes.BUCKET_IMAGE;
		try {
			response = assumeRole(CommonAttributes.ROLE_ACCESSKEY_ID,
					CommonAttributes.ROLE_ACCESSKEY_SECRET,
					CommonAttributes.ROLE_ARN,
					CommonAttributes.ROLE_SESSION_NAME);
		} catch (ClientException e) {
			logger.info("sts响应失败");
			e.printStackTrace();
		}
	}

	/**
	 * uediter上传图片需要的ossService实体
	 * @return
	 */
	public static OssService getInstance(){
		if(ossService == null){
			ossService = new OssService();
		}
		return ossService;
	}

	/**
	 * 获取client
	 * 
	 * @return
	 */
	public OSSClient getClient() {
		return client;
	}

	/**
	 *
	 *            授权角色的id
	 *            授权角色的秘钥
	 * @param roleArn
	 * @param roleSessionName

	 * @return 返回AssumeRoleResponse接口的对象用于获取临时访问权限的相关数据
	 * @throws ClientException
	 */
	// 目前只有"cn-hangzhou"这个region可用, 不要使用填写其他region的值
	private AssumeRoleResponse assumeRole(String roleAccessKeyId,
			String roleAccessKeySecret, String roleArn, String roleSessionName)
			throws ClientException {
		// 如何定制你的policy?
		// 参考:getBucketAcl CreateBucket、deleteBucket
		// https://docs.aliyun.com/#/pub/ram/ram-user-guide/policy_reference&struct_def
		// OSS policy 例子:
		// https://docs.aliyun.com/#/pub/oss/product-documentation/acl&policy-configure
		// OSS 授权相关问题的FAQ: https://docs.aliyun.com/#/pub/ram/faq/oss&basic
		String policy = "{ \"Statement\": [ {\"Action\": \"oss:*\", \"Effect\": \"Allow\", \"Resource\": \"*\" }],\"Version\": \"1\"}";
		// 此处必须为 HTTPS
		ProtocolType protocolType = ProtocolType.HTTPS;
		try {
			// 创建一个 Aliyun Acs Client, 用于发起 OpenAPI 请求
			IClientProfile profile = DefaultProfile.getProfile(
					CommonAttributes.REGION_CN_HANGZHOU, roleAccessKeyId,
					roleAccessKeySecret);
			DefaultAcsClient client = new DefaultAcsClient(profile);
			// 创建一个 AssumeRoleRequest 并设置请求参数
			final AssumeRoleRequest request = new AssumeRoleRequest();
			request.setVersion(CommonAttributes.STS_API_VERSION);
			request.setMethod(MethodType.POST);
			request.setProtocol(protocolType);
			request.setRoleArn(roleArn);
			request.setRoleSessionName(roleSessionName);
			request.setPolicy(policy);
			// 发起请求，并得到response
			final AssumeRoleResponse response = client.getAcsResponse(request);
			return response;
		} catch (ClientException e) {
			throw e;
		}
	}

	/**
	 * 获取阿里云的qqsl bucket的sts凭证
	 *
	 * @return
	 */
	private String getSts() {
		try {
			response = assumeRole(CommonAttributes.ROLE_ACCESSKEY_ID,
					CommonAttributes.ROLE_ACCESSKEY_SECRET,
					CommonAttributes.ROLE_ARN,
					CommonAttributes.ROLE_SESSION_NAME);
		} catch (ClientException e) {
			logger.info("sts响应失败");
			e.printStackTrace();
		}
		String Expiration = response.getCredentials().getExpiration();
		String AccessKey_Id = response.getCredentials().getAccessKeyId();
		String AccessKey_Secret = response.getCredentials()
				.getAccessKeySecret();
		String Security_Token = response.getCredentials().getSecurityToken();
		String RequestId = response.getRequestId();
		String AssumedRoleId = response.getAssumedRoleUser().getAssumedRoleId();
		stsToken = "{\n" + "                   \"requestId\":" + "\""
				+ RequestId + "\"" + ",\n"
				+ "                   \"AssumedRoleUser\":{\n"
				+ "                     \"AssumedRoleId\":" + "\""
				+ AssumedRoleId + "\"" + ",\n"
				+ "                     \"Arn\":" + "\""
				+ CommonAttributes.ROLE_ARN + "\"" + "\n"
				+ "                                        },\n"
				+ "                     \"Credentials\":{\n"
				+ "                     \"AccessKeyId\":" + "\"" + AccessKey_Id
				+ "\"" + ",\n" + "                     \"AccessKeySecret\":"
				+ "\"" + AccessKey_Secret + "\"" + ",\n"
				+ "                     \"Expiration\":" + "\"" + Expiration
				+ "\"" + ",\n" + "                     \"SecurityToken\":"
				+ "\"" + Security_Token + "\"" + "\n"
				+ "                                      }\n" + "}\n";
		return stsToken;
	}

	/**
	 * 提供外界获取sts的方法
	 *
	 * @return
	 */
	public String getStsToken() {
		if (stsToken == null) {
			stsToken = getSts();
		}
		return stsToken;
	}

	public void setStsToken() {
		this.stsToken = getSts();
	}



	/**
	 * 上传转换过的用于预览的文件
	 * 
	 * @param dir
	 *            上传bucket的文件夹
	 * @param fileName
	 *            文件名x
	 * @param inputStream
	 *            数据流
	 * @param meta
	 *            属性
	 * @param isPreview
	 *            是否预览
	 */
	public void upload(String dir, String fileName, InputStream inputStream,
			ObjectMetadata meta, boolean isPreview) {
		if (dir != null) {
			fileName = dir + "/" + fileName;
		}
		if (meta == null) {
			meta = new ObjectMetadata();
		}
		if (fileName.endsWith("pdf")) {
			meta.setContentType("application/pdf");
		}
		PutObjectRequest putObjectRequest = new PutObjectRequest(
				qqslBucketName, fileName, inputStream, meta);
		@SuppressWarnings("unused")
		PutObjectResult result = client.putObject(putObjectRequest);
		logger.debug("上传文件成功：" + fileName);
		// 关闭数据流
		IOUtils.safeClose(inputStream);
	}

	/**
	 * 向imageBucket上传图片
	 * 
	 * @param dir
	 *            图片在阿里云bucket下的文件路径
	 * @param inputStream
	 *            图片流
	 * @param meta
	 *            设置图片消息头
	 */
	public void uploadImage(String dir, InputStream inputStream,
			ObjectMetadata meta) {
		if (dir == null) {
			return;
		}
		if (meta == null) {
			meta = new ObjectMetadata();
		}
		if (dir.endsWith("jpeg") || dir.endsWith("jpg")) {
			meta.setContentType("image/jpeg");
		}
		if (dir.endsWith("png")) {
			meta.setContentType("image/png");
		}
		/*PutObjectRequest putObjectRequest = new PutObjectRequest(
				imageBucketName, dir, inputStream, meta);*/

		PutObjectRequest putObjectRequest = new PutObjectRequest(
				CommonAttributes.BUCKET_IMAGE, dir, inputStream, meta);
		@SuppressWarnings("unused")
		PutObjectResult result = client.putObject(putObjectRequest);
		logger.debug("上传文件成功：" + dir);
		// 关闭数据流
		IOUtils.safeClose(inputStream);
	}

	/**
	 * 取得指定文件夹下的文件列表
	 *
	 *
	 * @return
	 */
	public List<OSSObjectSummary> getObjects(String dir,String bucketName) {
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest(
				bucketName);
		listObjectsRequest.setDelimiter("/");
		if (dir != null) {
			listObjectsRequest.setPrefix(dir + "/");
		}
		ObjectListing listing;
		listing = client.listObjects(listObjectsRequest);
		return listing.getObjectSummaries();
	}

	/**
	 * 取得指定文件夹下的文件列表
	 *
	 *
	 * @return
	 */
	public List<String> getFolder(String dir,String bucketName) {
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest(
				bucketName);
		listObjectsRequest.setDelimiter("/");
		if (dir != null) {
			listObjectsRequest.setPrefix(dir + "/");
		}
		ObjectListing listing = null;
		listing = client.listObjects(listObjectsRequest);
		return listing.getCommonPrefixes();
	}

	/**
	 * 判断object是否存在
	 * @param bucketName
	 * @param key
	 * @return
	 */
	private boolean isFound(String bucketName,String key){
		boolean found = client.doesObjectExist(bucketName, key);
		return found;
	}

	/**
	 * 获取文件的属性
	 * 
	 * @param key
	 * @return
	 */
	public ObjectMetadata getObjectMetadata(String key) {
		return client.getObjectMetadata(qqslBucketName, key);
	}

	/**
	 * 删除文件
	 * 
	 * @param key
	 */
	public void deleteObject(String key,String bucket) {
		String bucketName = null;
		if (bucket.equals(CommonAttributes.BUCKET_NAME)) {
			bucketName=qqslBucketName;
		} else if (bucket.equals("image")) {
			bucketName = imageBucketName;
		}
		try {
			client.deleteObject(bucketName, key);
			 logger.info(key+"删除成功");
		} catch (Exception e) {
			e.printStackTrace();
            logger.info(key+"删除失败");
		}
	}

	/**
	 * bucket生命周期管理(项目)
	 * 
	 * @param prefix
	 */
	public void setBucketLife(String prefix,String flag) {
		logger.info(prefix);
		List<LifecycleRule> lifecycles = null;
		List<String> prefixes = null;
		if (flag.equals("project")) {
			prefixes = new ArrayList<>();
			String pdfPrefix = "pdf/" + prefix;
			prefixes.add(pdfPrefix);
			String projectPrefix = "project/" + prefix;
			prefixes.add(projectPrefix);
			String coordinatePrefix = "coordinate/" + prefix;
			prefixes.add(coordinatePrefix);
			try {
				lifecycles = client.getBucketLifecycle(qqslBucketName);
			} catch (OSSException e) {
				e.printStackTrace();
				lifecycles = new ArrayList<LifecycleRule>();
			}
		}else if(flag.equals("interest")){
			prefixes = new ArrayList<>();
			String interestPrefix = "interest/" + prefix;
			prefixes.add(interestPrefix);
			try {
				lifecycles = client.getBucketLifecycle(imageBucketName);
			} catch (OSSException e) {
				e.printStackTrace();
				lifecycles = new ArrayList<LifecycleRule>();
			}
		}else if(flag.equals("panorama")){
			prefixes = new ArrayList<>();
			String panoramaPrefix = "panorama/" + prefix;
			prefixes.add(panoramaPrefix);
			try {
				lifecycles = client.getBucketLifecycle(imageBucketName);
			} catch (OSSException e) {
				e.printStackTrace();
				lifecycles = new ArrayList<LifecycleRule>();
			}
		}
		logger.info("未添加删除策略时:" + lifecycles.size());
		// 添加Lifecycle规则
		for (String s : prefixes) {
			lifecycles
					.add(new LifecycleRule(s + ","
							+ System.currentTimeMillis(), s,
							RuleStatus.Enabled, 1));
		}
		logger.info("删除过期生命周期之前:" + lifecycles.size());
		expiredLifecycle(lifecycles,flag);

	}

	/**
	 * 去除过期和重复的生命周期
	 */
	private void expiredLifecycle(List<LifecycleRule> lifecycle,String flag) {
		long times = System.currentTimeMillis();
		String id = null;
		String[] strs = null;
		// 去除过期生命周期
		for (int i = 0; i < lifecycle.size(); i++) {
			id = lifecycle.get(i).getId();
			strs = id.split(",");
			if (times - Long.valueOf(strs[1]) > 87400000) {
				lifecycle.remove(i);
				i--;
			}
		}
		logger.info("删除过期生命周期之后:" + lifecycle.size());
		// 去除重复生命周期
		for (int i = 0; i < lifecycle.size(); i++) {
			for (int j = i + 1; j < lifecycle.size();) {
				if (lifecycle.get(j).getPrefix()
						.equals(lifecycle.get(i).getPrefix())) {
					lifecycle.remove(j);
				} else {
					j++;
				}
				// 不同，则指针移动
			}
		}
		logger.info("删除重复生命周期之后:" + lifecycle.size());
		if (lifecycle.size() == 0) {
			logger.info("生命周期列表为空！");
			return;
		}
		SetBucketLifecycleRequest req = null;
		if (flag.equals("project")) {
			req = new SetBucketLifecycleRequest(qqslBucketName);
		}else if(flag.equals("interest")||flag.equals("panorama")){
			req = new SetBucketLifecycleRequest(imageBucketName);
		}
		for (int i = 0; i < lifecycle.size(); i++) {
			req.AddLifecycleRule(lifecycle.get(i));
		}
		client.setBucketLifecycle(req);
	}



	/**
	 * 取得文件url，默认有效时间30分钟·
	 * 
	 * @param key
	 * @return
	 */
	public String getObjectUrl(String key,String bucketName) {
		Calendar now = Calendar.getInstance();
		now.add(Calendar.MINUTE, 30);
		if(!isFound(bucketName,key)){
			return null;
		}
		return client.generatePresignedUrl(bucketName, key, now.getTime())
				.toString();
	}

	public List<ObjectFile> getSubdirectoryFiles(String dir,String bucket){
		String bucketName = null;
		List<ObjectFile> files;
		List<ObjectFile> objectFiles = new ArrayList<>();
		if(bucket.equals("qqsl")){
			bucketName=qqslBucketName;
			List<String> folders= getFolder(dir,bucketName);
			// office文件夹
			for (String folder : folders) {
				files = new ArrayList<ObjectFile>();
				List<OSSObjectSummary> ossObjectSummaries = getObjects(folder.substring(0,folder.length()-1),bucketName);
				getFiles(files, ossObjectSummaries,bucketName);
				objectFiles.addAll(files);
			}
		}else if(bucket.equals("qqsl1")){
			bucketName=qqslBucketName;
			files = new ArrayList<>();
			List<OSSObjectSummary> ossObjectSummaries = getObjects(dir,bucketName);
			getFiles(files, ossObjectSummaries,bucketName);
			objectFiles.addAll(files);
		}
		else if(bucket.equals("qqslimage")){
			bucketName=imageBucketName;
			files = new ArrayList<>();
			List<OSSObjectSummary> ossObjectSummaries = getObjects(dir,bucketName);
			getFiles(files, ossObjectSummaries,bucketName);
			objectFiles.addAll(files);
		}
		// 循环生成文件信息
		return objectFiles;
	}

	/**
	 * 取得目录下的文件
	 * 
	 * @param dir
	 * @return
	 */
	public List<ObjectFile> getFiles(String dir,String bucket) {
		// 在单元招投标,设计,施工,监理,质检,其他,可研,初设(实施),施工图,建设期,施工阶段,运营维护期下没有文件
		// 直接跳过
		if (unitNames.contains(dir.substring(dir.lastIndexOf("/") + 1))) {
			return null;
		}
		List<String> newDirs = Arrays.asList(CommonAttributes.NEWDIR);
		List<String> oldDirs = Arrays.asList(CommonAttributes.OLDDIR);
		List<ObjectFile> files = new ArrayList<ObjectFile>();
		// office文件夹
		List<OSSObjectSummary> ossObjectSummaries = getObjects(dir,bucket);
		// 循环生成文件信息
		getFiles(files, ossObjectSummaries,bucket);
		if(newDirs.contains(dir.substring(dir.lastIndexOf("/")+1))){
			ossObjectSummaries = getObjects(dir.replaceAll(dir.substring(dir.lastIndexOf("/")+1), oldDirs.get(newDirs.indexOf(dir.substring(dir.lastIndexOf("/")+1)))),bucket);
			getFiles(files, ossObjectSummaries,bucket);
			return files;
		}else{
			return files;	
		}
	}

	private void getFiles(List<ObjectFile> files,List<OSSObjectSummary> ossObjectSummaries,String bucketName) {
		ObjectFile file = null;
		String key = null;
		for (OSSObjectSummary ossObjectSummary : ossObjectSummaries) {
			key = ossObjectSummary.getKey();
			StringUtils.hasText(key);
			if (key.endsWith("/")) {
				continue;
			}
			file = new ObjectFile(ossObjectSummary);
			file.setDownloadUrl(getObjectUrl(key,bucketName));
			if (extensiones.contains(key.substring(key.lastIndexOf(".") + 1).toLowerCase()) == false) {
				if(picturePrefixs.contains(key.substring(key.lastIndexOf(".") + 1).toLowerCase())){
					String thumbUrl = getThumbUrl(bucketName,key);
					file.setThumbUrl(thumbUrl);
				}
				files.add(file);
				continue;
			}
			key = key.replaceAll("project", "pdf");
			key = key.substring(0, key.lastIndexOf('.')) + ".pdf";
			try {
				getObjectMetadata(key);
				file.setPreviewUrl(getObjectUrl(key,bucketName));
			} catch (Exception e) {
				e.printStackTrace();
			}
			files.add(file);
		}

	}

	/**
	 * 检查文件是否为office文件
	 * @param treePath
	 * @return
	 */
	public boolean filePrefixCheck(String treePath){
		String prefix = treePath.substring(treePath.lastIndexOf(".")+1).toLowerCase();
		if(extensiones.contains(prefix)==false){
			//文件类型未知
			return false;
		}
		return true;
	}

	public List<Oss> getOssList() {
		List<Oss> ossList = findAll();
		return ossList;
	}

	/**
	 * 转为jsonArray
	 * @param ossList
	 * @return
	 */
	public JSONArray ossListToJsonArray(List<Oss> ossList){
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject;
		for(int i=0;i<ossList.size();i++){
			jsonObject = new JSONObject();
			jsonObject.put("id",ossList.get(i).getId());
			jsonObject.put("treePath",ossList.get(i).getTreePath());
			jsonArray.add(jsonObject);
		}
		return jsonArray;
	}

	/**
	 * 转换成功，从数据库清除记录
	 * @param id
	 */
	public void deleteOss(Long id) {
	  Oss oss = ossDao.find(id);
	  if(oss!=null){
		  ossDao.remove(oss);
	  }
	}

	/**
	 * 从指定地址下载文件到本地
	 * @param bucketName
	 * @param key
	 * @return
	 */
	public InputStream downloadFile(String bucketName, String key)throws OSSException{
		OSSObject ossObject = client.getObject(bucketName, key);
		return ossObject.getObjectContent();
	}

	public String downloadFileToLocal(String key, String path) throws OSSException {
// 下载object到文件
		File file = new File(path);
		client.getObject(new GetObjectRequest(CommonAttributes.BUCKET_NAME, "panorama/" + key), file);
// 关闭client
//		ossClient.shutdown();
		return file.getAbsolutePath();
	}

	public String downloadFileToLocal1(String key, String path) throws OSSException {
// 下载object到文件
		File file = new File(path);
		client.getObject(new GetObjectRequest("qqsl-dev", "panorama/" + key), file);
// 关闭client
//		ossClient.shutdown();
		return file.getAbsolutePath();
	}

	/**
	 * 生成直传token
	 * @param user
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public JSONObject directToken(User user,String bucketName) throws UnsupportedEncodingException {
		String endpoint = "oss-cn-hangzhou.aliyuncs.com";
		String dir = "panorama/"+user.getId()+"/";
		String host = "http://" + bucketName + "." + endpoint;
		OSSClient client = new OSSClient(endpoint, CommonAttributes.ACCESSKEY_ID, CommonAttributes.SECRET_ACCESSKEY);
		long expireTime = 8*3600+1800;
		long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
		Date expiration = new Date(expireEndTime);
		PolicyConditions policyConds = new PolicyConditions();
		policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
		policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

		String postPolicy = client.generatePostPolicy(expiration, policyConds);
		byte[] binaryData = postPolicy.getBytes("utf-8");
		String encodedPolicy = BinaryUtil.toBase64String(binaryData);
		String postSignature = client.calculatePostSignature(postPolicy);
//		byte[] bytes = BinaryUtil.fromBase64String(encodedPolicy);
//		String s = new String(bytes);
		Map<String, String> respMap = new LinkedHashMap<>();
		respMap.put("OSSAccessKeyId", CommonAttributes.ACCESSKEY_ID);
		respMap.put("policy", encodedPolicy);
		respMap.put("signature", postSignature);
		//respMap.put("expire", formatISO8601Date(expiration));
		respMap.put("prefix", dir);
		respMap.put("host", host);
		respMap.put("expire", String.valueOf(expireEndTime / 1000));
		return JSONObject.fromObject(respMap);
	}

	/**
	 * 获取缩略图
	 * @param bucketName
	 * @param key
	 * @return
	 */
	private String getThumbUrl(String bucketName,String key){
		// 图片处理样式
		String style = "image/resize,m_fixed,w_150,h_100";
		// 过期时间10分钟
		Date expiration = new Date(new Date().getTime() + 1000 * 60 * 10 );
		GeneratePresignedUrlRequest request = new
				GeneratePresignedUrlRequest(bucketName,key, HttpMethod.GET);
		request.setExpiration(expiration);
		request.setProcess(style);
		try {
			request.setProcess(style);
			URL signedUrl = client.generatePresignedUrl(request);
			return signedUrl.toString();
		}catch (Exception e){
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 删除oss的全景图片
	 * @param panorama
	 */
	public void deletePanorama(Panorama panorama) {
		Panorama panorama1 = panoramaService.findByInstanceId(panorama.getInstanceId());
		List<Scene> scenes = panorama1.getScenes();
		if (scenes==null||scenes.size()==0){
			return;
		}
		String key,prefix;
		for(int i = 0;i<scenes.size();i++){
			key = scenes.get(i).getOriginUrl();
			if(key==null){
				continue;
			}
			deleteObject(key,CommonAttributes.BUCKET_NAME);
			String key2 = scenes.get(i).getThumbUrl();
			String key3 = key2.substring(0,key2.lastIndexOf("/"));
			prefix = key.replace(key.substring(key.lastIndexOf(".")+1),key3.substring(key3.lastIndexOf(".")+1));
			setBucketLife(prefix.substring(prefix.indexOf("/")+1)+"/","panorama");
		}
	}


	/////////////////////////////////////////////
	//用于全景兼容


	//列出所有原qqslimage下的全景key值
	public List<String> getAllPanoramaFromOss(long id){
		List<String> panoramaKeys = new ArrayList<>();
		// 构造ListObjectsRequest请求
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest("qqslimage");
		listObjectsRequest.setPrefix("panorama/"+id+"/");
// 递归列出fun目录下的所有文件
		ObjectListing listing = client.listObjects(listObjectsRequest);
// 遍历所有Object
		//System.out.println("Objects:");
		for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
			if(objectSummary.getKey().equals("panorama/")||objectSummary.getKey().contains(".tiles")){
				continue;
			}
			panoramaKeys.add(objectSummary.getKey());
		}
		/*System.out.println(panoramaKeys.size());
		System.out.println(panoramaKeys);*/
// 遍历所有CommonPrefix
	//	System.out.println("\nCommonPrefixs:");
		for (String commonPrefix : listing.getCommonPrefixes()) {
		//	System.out.println(commonPrefix);
		}
		return panoramaKeys;
	}

	//复制所有全景到qqslimage-dev下
	public Map<String,Object> copyToDev(List<String> list,Panorama panorama){
		Map<String,Object> map = new HashedMap();
		String name;
		for(String key:list){
			String originName = System.currentTimeMillis()+"";
			key = key.replaceAll("\"","").trim();
			name = key.substring(key.lastIndexOf("/")+1);
			if(panorama.getUserId()==null){
				System.out.println(panorama.getId()+":用户id为空，可忽略-----------------------------");
				continue;
			}
			System.out.println(panorama.getUserId()+"::"+name);
			// 拷贝Object
			CopyObjectResult result = client.copyObject(
			//		CommonAttributes.BUCKET_IMAGE,key.trim(), "qqsl-dev", "panorama/"+panorama.getUserId()+"/"+name);
					"qqslimage",key.trim(), "qqsl-dev", "panorama/"+panorama.getUserId()+"/"+originName+".jpg");
			System.out.println("ETag: " + result.getETag() + " LastModified: " + result.getLastModified());
			map.put(name,originName);
		}
		map.put("list",list);
		return map;
		// 关闭client
		//client.shutdown();

	}
///panorama/6/1526694390164.tiles
	//panorama/6/1526694390164.tiles
	public void copyCuts(String object) {
	/*	List<ObjectFile> files = getFiles(object,bucketName);
		if(files==null||files.size()==0){
			logger.info(object+"无文件");
		}*/
		CopyObjectResult result = null;
		//List<OSSObjectSummary> summaries = getObjects(object,bucketName);
		List<OSSObjectSummary> summaries = getObjects(object,"qqslimage-dev");
		for (OSSObjectSummary ossObjectSummary:summaries){
			result = client.copyObject("qqslimage-dev",ossObjectSummary.getKey(), "qqslimage",ossObjectSummary.getKey());
			System.out.println("ETag: " + result.getETag() + " LastModified: " + result.getLastModified());
		}
// 关闭OSSClient。
	//
	}

	public void fin(){
		client.shutdown();
	}
//panorama/55/1526691245934.tiles/pano_b.jpg
	public void getObjectAndCopy() {
		CopyObjectResult result;
		String str1 = "panorama/16, panorama/25, panorama/26, panorama/37, panorama/50, panorama/55, panorama/6";
		List<String> strings = Arrays.asList(str1.split(","));
		List<String> list;
		List<OSSObjectSummary> summaries;
			list = getFolder(strings.get(1),"qqslimage-dev");
			for(String str:list){
				summaries = getObjects(str.substring(0,str.lastIndexOf("/")),"qqslimage-dev");
				for (OSSObjectSummary ossObjectSummary:summaries){
					result = client.copyObject("qqslimage-dev",ossObjectSummary.getKey(),"qqslimage",ossObjectSummary.getKey());
					System.out.println(ossObjectSummary.getKey()+"-------ETag: " + result.getETag() + " LastModified: " + result.getLastModified());
				}
			}
			System.out.println("----------------------"+strings.get(0)+"-------------------------------");

		//ossService.getObjectAndCopy("panorama/37");
// 关闭OSSClient。
		client.shutdown();
	}

	public void copyOrigin(String originUrl) {
		CopyObjectResult result = client.copyObject("qqsl-dev",originUrl, "qqsl",originUrl);
		System.out.println("-------ETag: " + result.getETag() + " LastModified: " + result.getLastModified());
	}
}
