package com.hysw.qqsl.cloud.core.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.Account;
import com.hysw.qqsl.cloud.core.entity.data.Oss;
import com.hysw.qqsl.cloud.core.service.*;
import net.sf.json.JSONObject;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.hysw.qqsl.cloud.core.entity.ObjectFile;
import com.hysw.qqsl.cloud.core.entity.data.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	 * @param id 项目id
	 * @return message消息体,OK:获取成功
	 */
	@RequiresAuthentication
	@RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
	@RequestMapping(value = "/objectFiles", method = RequestMethod.GET)
	public @ResponseBody
    Message getObjectFiles(
			@RequestParam("id") String id) {
		List<ObjectFile> objectFiles = ossService
				.getFiles("project" + "/" + id,"qqsl");
		return MessageService.message(Message.Type.OK,objectFiles);
	}

	/**
	 * 根据treePath(阿里云路径)得到文件url,一般获取图片的url
	 * @param key 文件对应的oss地址
	 * @return message消息体,FAIL:文件不存在,url获取失败,OK:url获取成功
	 */
	@RequiresAuthentication
	@RequiresRoles(value = {"user:simple","account:simple","admin:simple"}, logical = Logical.OR)
	@RequestMapping(value = "/getFileUrl", method = RequestMethod.GET)
	public @ResponseBody Message getFileUrl(
			@RequestParam("key") String key,@RequestParam("bucketName") String bucketName) {
		String url = ossService.getObjectUrl(key, bucketName);
		if (!StringUtils.hasText(url)){
			return MessageService.message(Message.Type.FAIL);
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("url", url);
		return MessageService.message(Message.Type.OK,jsonObject);
	}

	/**
	 * 获取sts安全令牌,用于上传或获取oss存储的文件
	 * @return message消息体,OK:获取成功
	 */
	@RequiresAuthentication
	@RequiresRoles(value = {"user:simple","account:simple","admin:simple"}, logical = Logical.OR)
	@RequestMapping(value = "/sts", method = RequestMethod.GET)
	public @ResponseBody Message getSts() {
		String sts = ossService.getStsToken();
		return MessageService.message(Message.Type.OK,sts);
	}

	/**
	 * office文件上传记录，子系统需转换为pdf
	 * @param object 包含文件存储路径treePath,以及项目标识projectId
	 * @return message消息体,UNKNOWN:未知文件类型,FAIL:参数错误,OK:记录成功
	 */
	@SuppressWarnings("unchecked")
	@RequiresAuthentication
	@RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/create",method = RequestMethod.POST)
	public @ResponseBody Message saveOss(@RequestBody Object object){
		Message message = CommonController.parameterCheck(object);
		if (message.getType() != Message.Type.OK) {
			return message;
		}
		Long prpjectId = Long.valueOf(((HashMap<String,Object>)message.getData()).get("projectId").toString());
		String treePath = ((HashMap<String,Object>)message.getData()).get("treePath").toString();
		if (!ossService.filePrefixCheck(treePath)) {
			//文件类型未知
			return MessageService.message(Message.Type.FILE_TYPE_ERROR);
		}
		User user = authentService.getUserFromSubject();
		if(user==null){
			user = projectService.find(prpjectId).getUser();
		}
		Oss oss = new Oss(treePath,user.getId(),prpjectId);
		ossService.save(oss);
		return MessageService.message(Message.Type.OK);
	}

	/**
	 * 子系统获取office文件路径
	 * @param token 与各自系统交互的自定义token安全令牌
	 * @return message消息体,FAIL:获取失败,OK:获取成功,包含所有要转换的记录
	 * http://localhost:8080/qqsl/oss/list?token=9F590A681F4248F09CB2DD51E45CF5A9
	 */
	@RequestMapping(value = "/list",method = RequestMethod.GET,produces = "application/json")
	public @ResponseBody
	Message getOss(@RequestParam String token){
		if(!applicationTokenService.decrypt(token)){
			return MessageService.message(Message.Type.FAIL);
		}
		List<Oss> ossList = ossService.getOssList();
		if(ossList==null) {
			return MessageService.message(Message.Type.FAIL);
		}
		return MessageService.message(Message.Type.OK, ossService.ossListToJsonArray(ossList));
	}

	/**
	 * 转换成功，从数据库清除记录
	 * @param data 为JSONObject字符串,包含oss文件路径实体id,以及token令牌
	 * @return message消息体,FAIL:参数不全,OK:记录删除成功
	 */
	@RequestMapping(value = "/remove",method = RequestMethod.POST)
	public @ResponseBody Message deleteOss(@RequestBody String data){
		Message message = CommonController.parametersCheck(data);
		if (message.getType() != Message.Type.OK) {
			return message;
		}
		JSONObject jsonObject = JSONObject.fromObject(data);
		Long id = jsonObject.getLong("id");
		String token = jsonObject.getString("token");
		if(applicationTokenService.decrypt(token)){
			ossService.deleteOss(id);
			return MessageService.message(Message.Type.OK);
		}
		return MessageService.message(Message.Type.FAIL);
	}

	/**
	 * 根据treePath(阿里云路径)得到多媒体文件(外业测量）
	 * @param id 项目id
	 * @return message消息体,OK:获取成功,包含文件列表
	 */
	@RequiresAuthentication
	@RequestMapping(value = "/objectMediaFiles", method = RequestMethod.GET)
	@RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
	public @ResponseBody Message getObjectMediaFiles(
			@RequestParam("id") String id) {
		List<ObjectFile> objectFiles = ossService
				.getSubdirectoryFiles("project" + "/" + id,"qqsl1");
		return MessageService.message(Message.Type.OK,objectFiles);
	}

	/**
	 * 删除单个文件
	 * @param object 包含对应删除文件的key
	 * @return message消息体,FAIL:参数错误,OK:删除成功
	 */
	@SuppressWarnings("unchecked")
	@RequiresAuthentication
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	@RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
	public @ResponseBody Message delete(@RequestBody Object object) {
		Map<String, String> map = (Map<String, String>) object;
		Object obj =  map.get("key");
		if (obj == null || !StringUtils.hasText(obj.toString())) {
			return MessageService.message(Message.Type.FAIL);
		}
		String key = map.get("key");
		// 删除project下的文件
		ossService.deleteObject(key,"qqsl");
		if (extensiones.contains(key.substring(key.lastIndexOf(".") + 1)
				.toLowerCase())) {
			key = key.replaceAll("project", "pdf");
			key = key.substring(0, key.lastIndexOf('.')) + ".pdf";
			// 删除pdf
			ossService.deleteObject(key,"qqsl");
		}
		return MessageService.message(Message.Type.OK);
	}

	/**
	 * 批量删除文件
	 * @param object 包含所有要删除文件的key链接的字符串,用逗号间隔的keys
	 * @return message消息体,FAIL:参数错误,OK:删除成功
	 */
	@SuppressWarnings("unchecked")
	@RequiresAuthentication
	@RequestMapping(value = "/deletes", method = RequestMethod.POST)
	@RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
	public @ResponseBody Message deleteAll(@RequestBody Object object) {
		Map<String, String> map = (Map<String, String>) object;
		if (!StringUtils.hasText(map.get("keys"))) {
			return MessageService.message(Message.Type.FAIL);
		}
		String str =  map.get("keys");
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
		return MessageService.message(Message.Type.OK);
	}

	/**
	 * 直传token
	 * @return
	 * <ul>
	 *     <li>OK 获取成功</li>
	 *     <li>FAIL 失败</li>
	 * </ul>
	 */
	@RequiresAuthentication
	@RequestMapping(value = "/directToken", method = RequestMethod.GET)
	@RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
	public @ResponseBody Message directToken(){
		User user = authentService.getUserFromSubject();
		if (user == null) {
			Account account = authentService.getAccountFromSubject();
		}
		try {
			return MessageService.message(Message.Type.OK, ossService.directToken(user));
		} catch (UnsupportedEncodingException e) {
			return MessageService.message(Message.Type.FAIL);
		}
	}

	@RequestMapping(value = "/getPng", method = RequestMethod.GET)
	public @ResponseBody Message directToken11(){
		//ossService.getThumbUrl();
		return new Message(Message.Type.OK,null);
	}


}
