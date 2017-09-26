package com.hysw.qqsl.cloud.core.service;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
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
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.dao.OssDao;
import com.hysw.qqsl.cloud.core.entity.ObjectFile;
import com.hysw.qqsl.cloud.core.entity.data.Oss;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

		String policy ="{\n" +
				"    \"Version\": \"1\",\n" +
				"    \"Statement\": [\n" +
				"        {\n" +
				"            \"Effect\": \"Allow\",\n" +
				"            \"Action\": \"ecs:Describe*\",\n" +
				"            \"Resource\": \"acs:ecs:cn-hangzhou:*:*\"\n" +
				"        },\n" +
				"        {\n" +
				"            \"Effect\": \"Allow\",\n" +
				"            \"Action\": [\n" +
				"                \"oss:ListObjects\",\n" +
				"                \"oss:GetObject\"\n" +
				"            ],\n" +
				"            \"Resource\": [\n" +
				"                \"acs:oss:*:30150706:*\",\n" +
				"                \"acs:oss:*:30150706:*/*\"\n" +

				"            ]\n" +
				"        }\n" +
				"    ]\n" +
				"}";

		String policy1 = "{\n"
				+ "   \"Version\": \"1\", \n"
				+ "    \"Statement\": [\n"
				+ "        {\n"
				+ "            \"Action\": [\n"
				//	+ "                \"oss:GetBucket\", \n"
				+ "                \" oss:ListObjects\",\n"
				+ "                \"oss:*\" \n"
				+                   "  ], \n"
				+ "            \"Resource\": [\n"
				+ "                 \"acs:oss:*:30150706:*\", \n"
				+ "                 \"acs:oss:*:30150706:*/*\" \n"
				//	+ "                \"acs:oss:*:30150706:qqslimage\", \n"
				//	+ "                \"acs:oss:*:30150706:qqslimage/*\" \n"
				+ "            ], \n"
				+ "            \"Effect\": \"Allow\"\n"
				+ "        }\n"
				+ "    ]\n"
				+ "}";
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
		PutObjectRequest putObjectRequest = new PutObjectRequest(
				imageBucketName, dir, inputStream, meta);
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
		ObjectListing listing = null;
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
		if (bucket.equals("qqsl")) {
			bucketName=qqslBucketName;
		} else if (bucket.equals("image")) {
			bucketName = imageBucketName;
		}
		try {
			client.deleteObject(bucketName, key);
			 logger.info(key+"删除成功");
		} catch (Exception e) {
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
				lifecycles = new ArrayList<LifecycleRule>();
			}
		}else if(flag.equals("interest")){
			prefixes = new ArrayList<>();
			String interestPrefix = "interest/" + prefix;
			prefixes.add(interestPrefix);
			try {
				lifecycles = client.getBucketLifecycle(imageBucketName);
			} catch (OSSException e) {
				lifecycles = new ArrayList<LifecycleRule>();
			}
		}else if(flag.equals("panorama")){
			prefixes = new ArrayList<>();
			String panoramaPrefix = "panorama/" + prefix;
			prefixes.add(panoramaPrefix);
			try {
				lifecycles = client.getBucketLifecycle(imageBucketName);
			} catch (OSSException e) {
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
		String bucketName = null;
		if(bucket.equals("qqsl")){
			bucketName=qqslBucketName;
		}else if(bucket.equals("image")){
			bucketName=imageBucketName;
		}
		List<String> newDirs = Arrays.asList(CommonAttributes.NEWDIR);
		List<String> oldDirs = Arrays.asList(CommonAttributes.OLDDIR);
		List<ObjectFile> files = new ArrayList<ObjectFile>();
		// office文件夹
		List<OSSObjectSummary> ossObjectSummaries = getObjects(dir,bucketName);
		// 循环生成文件信息
		getFiles(files, ossObjectSummaries,bucketName);
		if(newDirs.contains(dir.substring(dir.lastIndexOf("/")+1))){
			ossObjectSummaries = getObjects(dir.replaceAll(dir.substring(dir.lastIndexOf("/")+1), oldDirs.get(newDirs.indexOf(dir.substring(dir.lastIndexOf("/")+1)))),bucketName);
			getFiles(files, ossObjectSummaries,bucketName);
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
				files.add(file);
				continue;
			}
			key = key.replaceAll("project", "pdf");
			key = key.substring(0, key.lastIndexOf('.')) + ".pdf";
			try {
				getObjectMetadata(key);
				file.setPreviewUrl(getObjectUrl(key,bucketName));
			} catch (Exception e) {
			}
			files.add(file);
		}

	}

	/**
	 * 检查文件是否为office文件
	 * @param treePath
	 * @return
	 */
	public Message filePrefixCheck(String treePath){
		String prefix = treePath.substring(treePath.lastIndexOf(".")+1).toLowerCase();
		if(extensiones.contains(prefix)==false){
			//文件类型未知
			return new Message(Message.Type.UNKNOWN);
		}
		return new Message(Message.Type.OK);
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
}
