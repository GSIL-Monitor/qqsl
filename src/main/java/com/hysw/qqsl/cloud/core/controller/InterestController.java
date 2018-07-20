package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.Interest;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.service.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by chenl on 17-1-10.
 */
@Controller
@RequestMapping("/interest")
public class InterestController {
    @Autowired
    private InterestService interestService;
    @Autowired
    private OssService ossService;
    @Autowired
    private AuthentService authentService;

    /**
     * 保存基础兴趣点
     * @param objectMap <ol>
     *                  <li>interest</li>
     *                  <li>
     *                      <ol>
     *                  <li>name名称</li>
     *                  <li>type类别</li>
     *                  <li>category分类</li>
     *                  <li>coordinate坐标</li>
     *                  <li>region行政区</li>
     *                  <li>contact联系方式</li>
     *                  <li>content描述</li>
     *                  <li>evaluate客户评价</li>
     *                  <li>business特色业务</li>
     *                  <li>level等级</li>
     *                  <li>pictures图片</li>
     *                  <li>userId用户id</li>
     *                  </ol>
     *                  </li>
     * </ol>
     * @return FAIL参数验证失败，OK保存成功
     */
    @SuppressWarnings("unchecked")
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/saveBaseInterest", method = RequestMethod.POST)
    public @ResponseBody
    Message saveBaseInterest(@RequestBody Map<String, Object> objectMap){
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Map<String,Object> map = (Map<String, Object>) objectMap.get("interest");
        Object type = map.get("type");
        if (type == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        if (type.toString().equals(String.valueOf(Interest.Type.BASE.ordinal()))) {
            map.put("status", CommonEnum.Review.PASS.ordinal());
        } else {
            return MessageService.message(Message.Type.FAIL);
        }
        JSONObject jsonObject = interestService.saveInterest(map, new Interest());
        if (jsonObject == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        return MessageService.message(Message.Type.OK, jsonObject);
//        interestService.reflectSaveProprety(map, new Interest());
//        return MessageService.message(Message.Type.OK);
    }

    /**
     * 保存个性兴趣点
     * @param objectMap <br/>
     *                  <ol>
     *                  <li>interest</li>
     *                  <li>
     *                      <ol>
     *                      <li>name:名称</li>
     *                      <li>type:类别</li>
     *                      <li>category:分类</li>
     *                      <li>coordinate:坐标</li>
     *                      <li>region:行政区</li>
     *                      <li>contact:联系方式</li>
     *                      <li>content:描述</li>
     *                      <li>evaluate:客户评价</li>
     *                      <li>business:特色业务</li>
     *                      <li>level:等级</li>
     *                      <li>pictures:图片</li>
     *                      </ol>
     *                  </li>
     *                  </ol>
     * @return <br/>
     * <ol>
     *     <li>FAIL参数验证失败</li>
     *     <li>OK保存成功</li>
     *     <li>EXIST用户不存在</li>
     * </ol>
     */
    @SuppressWarnings("unchecked")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/savePersonalInterest", method = RequestMethod.POST)
    public @ResponseBody Message savePersonalInterest(@RequestBody Map<String, Object> objectMap){
        Message message = CommonController.parameterCheck(objectMap);
        if(message.getType()!=Message.Type.OK){
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        Map<String,Object> map = (Map<String, Object>) objectMap.get("interest");
        Object type = map.get("type");
        if (type == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        if (type.toString().equals(String.valueOf(Interest.Type.PERSONAL.ordinal()))) {
            map.put("status", CommonEnum.Review.PENDING.ordinal());
        } else {
            return MessageService.message(Message.Type.FAIL);
        }
        map.put("userId", user.getId());
        JSONObject jsonObject = interestService.saveInterest(map, new Interest());
        if (jsonObject == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        return MessageService.message(Message.Type.OK, jsonObject);
//        interestService.reflectSaveProprety(map, new Interest());
//        return MessageService.message(Message.Type.OK);
    }

    /**
     * 编辑基础兴趣点
     * @param objectMap <ol>
     *                  <li>interest</li>
     *                  <li>
     *                      <ol>
     *                  <li>name名称</li>
     *                  <li>type类别</li>
     *                  <li>category分类</li>
     *                  <li>coordinate坐标</li>
     *                  <li>region行政区</li>
     *                  <li>contact联系方式</li>
     *                  <li>content描述</li>
     *                  <li>evaluate客户评价</li>
     *                  <li>business特色业务</li>
     *                  <li>level等级</li>
     *                  <li>pictures图片</li>
     *                  <li>userId用户id</li>
     *                  </ol>
     *                  </li>
     * </ol>
     * @return FAIL参数验证失败，OK保存成功
     */
    @SuppressWarnings("unchecked")
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/updateBaseInterest", method = RequestMethod.POST)
    public @ResponseBody Message updateBaseInterest(@RequestBody Map<String, Object> objectMap){
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Map<String,Object> map = (Map<String, Object>) objectMap.get("interest");
        Object type = map.get("type");
        if (type == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        if (type.toString().equals(String.valueOf(Interest.Type.BASE.ordinal()))) {
            map.put("status", CommonEnum.Review.PASS.ordinal());
        } else {
            return MessageService.message(Message.Type.FAIL);
        }
        Interest interest = interestService.find(Long.valueOf(map.get("id").toString()));
        if(interest==null){
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        JSONObject jsonObject = interestService.saveInterest(map, interest);
        if (jsonObject == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        return MessageService.message(Message.Type.OK, jsonObject);
//        interestService.reflectSaveProprety(map,interest);
//        return MessageService.message(Message.Type.OK);
    }

    /**
     * 编辑兴趣点
     * @param objectMap <br/>
     *                  <ol>
     *                  <li>interest</li>
     *                  <li>
     *                      <ol>
     *                      <li>name:名称</li>
     *                      <li>type:类别</li>
     *                      <li>category:分类</li>
     *                      <li>coordinate:坐标</li>
     *                      <li>region:行政区</li>
     *                      <li>contact:联系方式</li>
     *                      <li>content:描述</li>
     *                      <li>evaluate:客户评价</li>
     *                      <li>business:特色业务</li>
     *                      <li>level:等级</li>
     *                      <li>pictures:图片</li>
     *                      </ol>
     *                  </li>
     *                  </ol>
     * @return <br/>
     * <ol>
     *     <li>FAIL参数验证失败</li>
     *     <li>OK保存成功</li>
     *     <li>EXIST用户不存在</li>
     * </ol>
     */
    @SuppressWarnings("unchecked")
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/updatePersonalInterest", method = RequestMethod.POST)
    public @ResponseBody Message updatePersonalInterest(@RequestBody Map<String, Object> objectMap){
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        Map<String,Object> map = (Map<String, Object>) objectMap.get("interest");
        Object type = map.get("type");
        if (type == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        if (type.toString().equals(String.valueOf(Interest.Type.PERSONAL.ordinal()))) {
            map.put("status", CommonEnum.Review.PENDING.ordinal());
        } else {
            return MessageService.message(Message.Type.FAIL);
        }
        map.put("userId", user.getId());
        Interest interest = interestService.find(Long.valueOf(map.get("id").toString()));
        if (interest == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        JSONObject jsonObject = interestService.saveInterest(map, interest);
        if (jsonObject == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        return MessageService.message(Message.Type.OK, jsonObject);
//        interestService.reflectSaveProprety(map,interest);
//        return MessageService.message(Message.Type.OK);
    }

    /**
     * 删除基础兴趣点
     * @param id 兴趣点id
     * @return FAil参数验证失败，OK删除成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/deleteBaseInterest/{id}", method = RequestMethod.DELETE)
    public @ResponseBody Message deteleBaseInterest(@PathVariable("id") Long id){
        Message message = CommonController.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Interest interest = interestService.find(id);
        if (interest == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (!interest.getType().toString().equals(Interest.Type.BASE.toString())) {
            return MessageService.message(Message.Type.FAIL);
        }
        if (interest.getReviewDate() != null) {
            return MessageService.message(Message.Type.FAIL);
        }
        ossService.setBucketLife(interest.getId() + "/","interest");
        interestService.remove(interest);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 删除兴趣点
     * @param id 兴趣点id
     * @return FAil参数验证失败，OK删除成功
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deletePersonalInterest/{id}", method = RequestMethod.DELETE)
    public @ResponseBody Message detelePersonalInterest(@PathVariable("id") Long id){
        Message message = CommonController.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        Interest interest = interestService.find(id);
        if (interest == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        if (!interest.getType().toString().equals(Interest.Type.PERSONAL.toString())) {
            return MessageService.message(Message.Type.FAIL);
        }
        if (interest.getReviewDate() != null) {
            return MessageService.message(Message.Type.FAIL);
        }
        if (!interest.getUserId().equals(user.getId())) {
            return MessageService.message(Message.Type.DATA_REFUSE);
        }
        ossService.setBucketLife(interest.getId() + "/","interest");
        interestService.remove(interest);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 获取兴趣点列表(包括用户自己的)
     * @return 兴趣点列表
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/interestList", method = RequestMethod.GET)
    public @ResponseBody Message interestList(){
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        List<Interest> interests = interestService.findAllPass(user.getId());
        JSONArray interestsJson = interestService.interestsToJson(interests);
        return MessageService.message(Message.Type.OK,interestsJson);
    }

    /**
     * 获取兴趣点列表(基础)
     * @return 基础兴趣点列表
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/baseInterestList", method = RequestMethod.GET)
    public @ResponseBody Message baseInterestList(){
        List<Interest> interests = interestService.findAllBase();
        JSONArray interestsJson = interestService.interestsToJson(interests);
        return MessageService.message(Message.Type.OK,interestsJson);
    }

    /**
     * 获取兴趣点列表(个性)
     * @return 个性兴趣点列表
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/personalInterestList", method = RequestMethod.GET)
    public @ResponseBody Message personalInterestList(){
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return MessageService.message(Message.Type.DATA_NOEXIST);
        }
        List<Interest> interests = interestService.findAllPersonal(user);
        JSONArray interestsJson = interestService.interestsToJson(interests);
        return MessageService.message(Message.Type.OK,interestsJson);
    }

    /**
     * 获取兴趣点列表(所有通过的)
     * @return 审核通过的兴趣点列表
     */
    @RequestMapping(value = "/interestPassList", method = RequestMethod.GET)
    public @ResponseBody Message interestAllPassList(){
        List<Interest> interests = interestService.findAllPass(null);
        JSONArray interestsJson = interestService.interestsToJson(interests);
        return MessageService.message(Message.Type.OK, interestsJson);
    }

//    审核

    /**
     * 兴趣点审核列表
     * @return 兴趣点审核列表
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/interestReview", method = RequestMethod.GET)
    public @ResponseBody Message interestReview(){
        List<Interest> interests = interestService.findAllPending();
        JSONArray interestsJson = interestService.interestsToJson(interests);
        return MessageService.message(Message.Type.OK,interestsJson);
    }

//    审核通过

    /**
     * 个人兴趣点审核通过
     * @param objectMap <ol>
     *                  <li>id:兴趣点id</li>
     * </ol>
     * @return FAIL参数验证失败，OK审核通过
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/interestReviewSuccess", method = RequestMethod.POST)
    public @ResponseBody Message interestReviewSuccess(@RequestBody Map<String, Object> objectMap){
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Interest interest = interestService.find(Long.valueOf(objectMap.get("id").toString()));
        interest.setStatus(CommonEnum.Review.PASS);
        interest.setReviewDate(new Date());
        interestService.save(interest);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 个人兴趣点审核未通过
     * @param objectMap <ol>
     *                  <li>id:兴趣点id</li>
     *                  <li>advice:审核意见（可不传）</li>
     * </ol>
     * @return FAIL参数验证失败，OK审核未通过
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/interestReviewFail", method = RequestMethod.POST)
    public @ResponseBody Message interestReviewFail(@RequestBody Map<String, Object> objectMap){
        Message message = CommonController.parameterCheck(objectMap);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        if (objectMap.get("id") == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        Interest interest = interestService.find(Long.valueOf(objectMap.get("id").toString()));
        interest.setStatus(CommonEnum.Review.NOTPASS);
        if (objectMap.get("advice") == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        interest.setAdvice(objectMap.get("advice").toString());
        interest.setReviewDate(new Date());
        interestService.save(interest);
        return MessageService.message(Message.Type.OK);
    }

    /**
     * 根据id获取兴趣点详细信息
     * @param id 兴趣点id
     * @return 兴趣点详情
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/interest/{id}", method = RequestMethod.GET)
    public @ResponseBody Message getInterest(@PathVariable("id") Long id) {
        Message message = CommonController.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Interest interest = interestService.find(id);
        if (interest == null) {
            return MessageService.message(Message.Type.FAIL);
        }
        JSONObject interestToJson = interestService.interestToJson(interest);
        return MessageService.message(Message.Type.OK, interestToJson);
    }

}
