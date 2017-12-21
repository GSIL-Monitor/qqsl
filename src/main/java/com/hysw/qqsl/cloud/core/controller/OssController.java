package com.hysw.qqsl.cloud.core.controller;

import java.util.*;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.data.Oss;
import com.hysw.qqsl.cloud.core.service.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.hysw.qqsl.cloud.core.entity.ObjectFile;
import com.hysw.qqsl.cloud.core.entity.data.User;

/**
 * 阿里云ossController
 *
 * @author Administrator
 *
 */
@Controller
@RequestMapping("/oss")
public class OssController {

	@Autowired
	private OssService ossService;
	@Autowired
	private CoordinateService coordinateService;
	@Autowired
	private ApplicationTokenService applicationTokenService;
	@Autowired
	private AuthentService authentService;
	@Autowired
	private ProjectService projectService;
	/** office文件的所有后缀 */
	private List<String> extensiones = Arrays
			.asList(CommonAttributes.OFFICE_FILE_EXTENSION.split(","));

	/**
	 * 根据treePath(阿里云路径)得到文件列表
	 *
	 * @param id
	 * @return
	 */
	@RequiresAuthentication
	@RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
	@RequestMapping(value = "/objectFiles", method = RequestMethod.GET)
	public @ResponseBody Message getObjectFiles(
			@RequestParam("id") String id) {
		List<ObjectFile> objectFiles = ossService
				.getFiles("project" + "/" + id,"qqsl");
		return new Message(Message.Type.OK,objectFiles);
	}

	/**
	 * 根据treePath(阿里云路径)得到文件url
	 *
	 * @param key
	 * @return
	 */
	@RequiresAuthentication
	@RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
	@RequestMapping(value = "/getFileUrl", method = RequestMethod.GET)
	public @ResponseBody Message getFileUrl(
			@RequestParam("key") String key,@RequestParam("bucketName") String bucketName) {
		try {
			ossService.getObjectMetadata(key);
		} catch (Exception e) {
			return new Message(Message.Type.FAIL);
		}
		String url = ossService.getObjectUrl(key, bucketName);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("url", url);
		return new Message(Message.Type.OK,jsonObject);
	}




	/**
	 * 获取sts
	 *
	 * @return
	 */
	@RequiresAuthentication
	@RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
	@RequestMapping(value = "/sts", method = RequestMethod.GET)
	public @ResponseBody Message getSts() {
		String sts = ossService.getStsToken();
		return new Message(Message.Type.OK,sts);
	}

	/**
	 * office文件上传记录，子系统需转换为pdf
	 * @param object
	 * @return
	 */
	@RequiresAuthentication
	@RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/create",method = RequestMethod.POST)
	public @ResponseBody Message saveOss(@RequestBody Object object){
		Message message = Message.parameterCheck(object);
		if(message.getType()== Message.Type.FAIL){
			return message;
		}
		Long prpjectId = Long.valueOf(((HashMap<String,Object>)message.getData()).get("projectId").toString());
		String treePath = ((HashMap<String,Object>)message.getData()).get("treePath").toString();
		message = ossService.filePrefixCheck(treePath);
		if(!message.getType().equals(Message.Type.OK)){
			return message;
		}
		User user = authentService.getUserFromSubject();
		if(user==null){
			user = projectService.find(prpjectId).getUser();
		}
		Oss oss = new Oss(treePath,user.getId(),prpjectId);
		ossService.save(oss);
		return new Message(Message.Type.OK);
	}

	/**
	 * 子系统获取office文件路径
	 * @param token
	 * @return
	 * http://localhost:8080/qqsl/oss/list?token=9F590A681F4248F09CB2DD51E45CF5A9
	 */
	@RequestMapping(value = "/list",method = RequestMethod.GET,produces = "application/json")
	public @ResponseBody
	Message getOss(@RequestParam String token){
		if(!applicationTokenService.decrypt(token)){
			return new Message(Message.Type.FAIL);
		}
		List<Oss> ossList = ossService.getOssList();
		if(ossList==null) {
			return new Message(Message.Type.FAIL);
		}
		return new Message(Message.Type.OK, ossService.ossListToJsonArray(ossList));
	}

	/**
	 * 转换成功，从数据库清除记录
	 * @param data
	 */
	@RequestMapping(value = "/remove",method = RequestMethod.POST)
	public @ResponseBody Message deleteOss(@RequestBody String data){
		Message message = Message.parametersCheck(data);
		if(message.getType()== Message.Type.FAIL){
			return message;
		}
		JSONObject jsonObject = JSONObject.fromObject(data);
		Long id = jsonObject.getLong("id");
		String token = jsonObject.getString("token");
		if(applicationTokenService.decrypt(token)){
			ossService.deleteOss(id);
			return new Message(Message.Type.OK);
		}
		return new Message(Message.Type.FAIL);
	}

	/**
	 * 根据treePath(阿里云路径)得到多媒体文件(外业测量）
	 *
	 * @param id
	 * @return
	 */
	@RequiresAuthentication
	@RequestMapping(value = "/objectMediaFiles", method = RequestMethod.GET)
	@RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
	public @ResponseBody Message getObjectMediaFiles(
			@RequestParam("id") String id) {
		List<ObjectFile> objectFiles = ossService
				.getSubdirectoryFiles("project" + "/" + id,"qqsl1");
		return new Message(Message.Type.OK,objectFiles);
	}

	/**
	 * 删除单个文件
	 *
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequiresAuthentication
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	@RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
	public @ResponseBody Message delete(@RequestBody Object object) {
		Map<String, String> map = (Map<String, String>) object;
		Object obj =  map.get("objectFile");
		if (obj == null || obj.equals("")) {
			return new Message(Message.Type.FAIL);
		}
		String key = map.get("objectFile");
		// 删除project下的文件
		ossService.deleteObject(key,"qqsl");
		if (extensiones.contains(key.substring(key.lastIndexOf(".") + 1)
				.toLowerCase())) {
			key = key.replaceAll("project", "pdf");
			key = key.substring(0, key.lastIndexOf('.')) + ".pdf";
			// 删除pdf
			ossService.deleteObject(key,"qqsl");
		}
		return new Message(Message.Type.OK);
	}

	/**
	 * 批量删除文件
	 *
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequiresAuthentication
	@RequestMapping(value = "/deletes", method = RequestMethod.POST)
	@RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
	public @ResponseBody Message deleteAll(@RequestBody Object object) {
		Map<String, String> map = (Map<String, String>) object;
		String obj = map.get("deleteFilesIds");
		if (obj == null || obj.equals("")) {
			return new Message(Message.Type.FAIL);
		}
		String str =  map.get("deleteFilesIds");
		String[] key1 = str.split(",");
		String key;
		for (int i = 0; i < key1.length; i++) {
			key = key1[i];
			// 删除project下的文件
			ossService.deleteObject(key,"qqsl");
			if (extensiones.contains(key.substring(key.lastIndexOf(".") + 1)
					.toLowerCase())) {
				key = key.replaceAll("project", "pdf");
				key = key.substring(0, key.lastIndexOf('.')) + ".pdf";
				// 删除pdf
				ossService.deleteObject(key,"qqsl");
			}
		}
		return new Message(Message.Type.OK);
	}
}
