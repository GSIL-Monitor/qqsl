package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.entity.data.Interest;
import com.hysw.qqsl.cloud.core.entity.data.Panorama;
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
    private UserService userService;
    @Autowired
    private InterestService interestService;
    @Autowired
    private PanoramaService panoramaService;
    @Autowired
    private OssService ossService;
    @Autowired
    private AuthentService authentService;
    /**
     * 保存基础兴趣点
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/saveBaseInterest", method = RequestMethod.POST)
    public @ResponseBody Message saveBaseInterest(@RequestBody Map<String, Object> objectMap){
        Message message = Message.parameterCheck(objectMap);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        Map<String,Object> map = (Map<String, Object>) objectMap.get("interest");
        Object type = map.get("type");
        if (type == null) {
            return new Message(Message.Type.FAIL);
        }
        if (type.toString().equals(String.valueOf(Interest.Type.BASE.ordinal()))) {
            map.put("status", CommonEnum.Review.PASS.ordinal());
        } else {
            return new Message(Message.Type.FAIL);
        }
        return interestService.saveInterest(map,new Interest());
//        interestService.reflectSaveProprety(map, new Interest());
//        return new Message(Message.Type.OK);
    }

    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/savePersonalInterest", method = RequestMethod.POST)
    public @ResponseBody Message savePersonalInterest(@RequestBody Map<String, Object> objectMap){
        Message message = Message.parameterCheck(objectMap);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        Map<String,Object> map = (Map<String, Object>) objectMap.get("interest");
        Object type = map.get("type");
        if (type == null) {
            return new Message(Message.Type.FAIL);
        }
        if (type.toString().equals(String.valueOf(Interest.Type.PERSONAL.ordinal()))) {
            map.put("status", CommonEnum.Review.PENDING.ordinal());
        } else {
            return new Message(Message.Type.FAIL);
        }
        map.put("userId", user.getId());
        return interestService.saveInterest(map,new Interest());
//        interestService.reflectSaveProprety(map, new Interest());
//        return new Message(Message.Type.OK);
    }

    /**
     * 保存全景
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/savePanorama", method = RequestMethod.POST)
    public @ResponseBody Message savePanorama(@RequestBody Map<String, Object> map){
        Message message = Message.parameterCheck(map);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        User user = authentService.getUserFromSubject();
        //是否可以保存全景
        message=panoramaService.isAllowSavePanorma(user);
        if(message.getType()==Message.Type.NO_ALLOW){
            return message;
        }
        map.put("status", CommonEnum.Review.PENDING.ordinal());
        map.put("userId", user.getId());
        return panoramaService.savePanorama(map,new Panorama());
//        panoramaService.reflectSaveProprety(map, new Panorama());
//        return new Message(Message.Type.OK);
    }

    /**
     * 批量上传全景
     * @param object
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/savePanoramas", method = RequestMethod.POST)
    public @ResponseBody Message savePanoramas(@RequestBody Object object){
        User user = authentService.getUserFromSubject();
        //是否可以保存全景
        Message message;
        message=panoramaService.isAllowSavePanorma(user);
        if(message.getType()==Message.Type.NO_ALLOW){
            return message;
        }
        List<Map<String,Object>> panoramas = (List<Map<String,Object>>) object;
        Map<String,Object> panoramaMap,objectMap;
        List<JSONObject> jsonObjects = new ArrayList<>();
        JSONObject jsonObject;
        for(int i=0;i<panoramas.size();i++){
            objectMap = panoramas.get(i);
            panoramaMap = (Map<String, Object>) objectMap.get("panorama");
            panoramaMap.put("status", CommonEnum.Review.PENDING.ordinal());
            panoramaMap.put("userId", user.getId());
            message = panoramaService.savePanorama(panoramaMap,new Panorama());
            if(message.getType()== Message.Type.OK){
                jsonObject = (JSONObject) message.getData();
                jsonObject.put("name",panoramaMap.get("name"));
                jsonObjects.add(jsonObject);
            }else{
                continue;
            }
        }
        return new Message(Message.Type.OK,jsonObjects);
    }

    /**
     * 编辑基础兴趣点
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/updateBaseInterest", method = RequestMethod.POST)
    public @ResponseBody Message updateBaseInterest(@RequestBody Map<String, Object> objectMap){
        Message message = Message.parameterCheck(objectMap);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        Map<String,Object> map = (Map<String, Object>) objectMap.get("interest");
        Object type = map.get("type");
        if (type == null) {
            return new Message(Message.Type.FAIL);
        }
        if (type.toString().equals(String.valueOf(Interest.Type.BASE.ordinal()))) {
            map.put("status", CommonEnum.Review.PASS.ordinal());
        } else {
            return new Message(Message.Type.FAIL);
        }
        Interest interest = interestService.find(Long.valueOf(map.get("id").toString()));
        if(interest==null){
            return new Message(Message.Type.FAIL);
        }
        return interestService.saveInterest(map,interest);
//        interestService.reflectSaveProprety(map,interest);
//        return new Message(Message.Type.OK);
    }

    /**
     * 编辑兴趣点
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/updatePersonalInterest", method = RequestMethod.POST)
    public @ResponseBody Message updatePersonalInterest(@RequestBody Map<String, Object> objectMap){
        Message message = Message.parameterCheck(objectMap);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        Map<String,Object> map = (Map<String, Object>) objectMap.get("interest");
        Object type = map.get("type");
        if (type == null) {
            return new Message(Message.Type.FAIL);
        }
        if (type.toString().equals(String.valueOf(Interest.Type.PERSONAL.ordinal()))) {
            map.put("status", CommonEnum.Review.PENDING.ordinal());
        } else {
            return new Message(Message.Type.FAIL);
        }
        map.put("userId", user.getId());
        Interest interest = interestService.find(Long.valueOf(map.get("id").toString()));
        if(interest==null){
            return new Message(Message.Type.FAIL);
        }
        return interestService.saveInterest(map,interest);
//        interestService.reflectSaveProprety(map,interest);
//        return new Message(Message.Type.OK);
    }

    /**
     * 编辑全景
     * @param map
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/updatePanorama", method = RequestMethod.POST)
    public @ResponseBody Message updatePanorama(@RequestBody Map<String, Object> map){
        Message message = Message.parameterCheck(map);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        map.put("status", CommonEnum.Review.PENDING.ordinal());
        map.put("userId", user.getId());
        Panorama panorama = panoramaService.find(Long.valueOf(map.get("id").toString()));
        if(panorama==null){
            return new Message(Message.Type.FAIL);
        }
        return panoramaService.savePanorama(map, panorama);
//        panoramaService.reflectSaveProprety(map,panorama);
//        return new Message(Message.Type.OK);
    }


    /**
     * 删除基础兴趣点
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/deleteBaseInterest/{id}", method = RequestMethod.DELETE)
    public @ResponseBody Message deteleBaseInterest(@PathVariable("id") Long id){
        Message message = Message.parametersCheck(id);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        Interest interest = interestService.find(id);
        if (interest == null) {
            return new Message(Message.Type.FAIL);
        }
        if (!interest.getType().toString().equals(Interest.Type.BASE.toString())) {
            return new Message(Message.Type.FAIL);
        }
        if (interest.getReviewDate() != null) {
            return new Message(Message.Type.FAIL);
        }
        ossService.setBucketLife(interest.getId() + "/","interest");
        interestService.remove(interest);
        return new Message(Message.Type.OK);
    }

    /**
     * 删除兴趣点
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deletePersonalInterest/{id}", method = RequestMethod.DELETE)
    public @ResponseBody Message detelePersonalInterest(@PathVariable("id") Long id){
        Message message = Message.parametersCheck(id);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        Interest interest = interestService.find(id);
        if (interest == null) {
            return new Message(Message.Type.FAIL);
        }
        if (!interest.getType().toString().equals(Interest.Type.PERSONAL.toString())) {
            return new Message(Message.Type.FAIL);
        }
        if (interest.getReviewDate() != null) {
            return new Message(Message.Type.FAIL);
        }
        if (!interest.getUserId().equals(user.getId())) {
            return new Message(Message.Type.FAIL);
        }
        ossService.setBucketLife(interest.getId() + "/","interest");
        interestService.remove(interest);
        return new Message(Message.Type.OK);
    }

    /**
     * 删除全景
     * @param id
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/deletePanorama/{id}", method = RequestMethod.DELETE)
    public @ResponseBody Message detelePanorama(@PathVariable("id") Long id){
        Message message = Message.parametersCheck(id);
        if(message.getType()==Message.Type.FAIL){
            return message;
        }
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        Panorama panorama = panoramaService.find(id);
        if (panorama == null) {
            return new Message(Message.Type.FAIL);
        }
        /*if (panorama.getReviewDate() != null) {
            return new Message(Message.Type.FAIL);
        }*/
        if (!panorama.getUserId().equals(user.getId())) {
            return new Message(Message.Type.FAIL);
        }
        ossService.setBucketLife(panorama.getId() + "/","panorama");
        panoramaService.remove(panorama);
        return new Message(Message.Type.OK);
    }

    /**
     * 获取兴趣点列表(包括用户自己的)
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/interestList", method = RequestMethod.GET)
    public @ResponseBody Message interestList(){
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        List<Interest> interests = interestService.findAllPass(user.getId());
        List<JSONObject> interestsJson = interestService.interestsToJson(interests);
        return new Message(Message.Type.OK,interestsJson);
    }

    /**
     * 获取兴趣点列表(基础)
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/baseInterestList", method = RequestMethod.GET)
    public @ResponseBody Message baseInterestList(){
        List<Interest> interests = interestService.findAllBase();
        List<JSONObject> interestsJson = interestService.interestsToJson(interests);
        return new Message(Message.Type.OK,interestsJson);
    }

    /**
     * 获取兴趣点列表(个性)
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/personalInterestList", method = RequestMethod.GET)
    public @ResponseBody Message personalInterestList(){
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        List<Interest> interests = interestService.findAllPersonal(user);
        List<JSONObject> interestsJson = interestService.interestsToJson(interests);
        return new Message(Message.Type.OK,interestsJson);
    }

    /**
     * 获取兴趣点列表(所有通过的)
     * @return
     */
    @RequestMapping(value = "/interestPassList", method = RequestMethod.GET)
    public @ResponseBody Message interestAllPassList(){
        List<Interest> interests = interestService.findAllPass(null);
        List<JSONObject> interestsJson = interestService.interestsToJson(interests);
        return new Message(Message.Type.OK, interestsJson);
    }

    /**
     * 获取全景列表
     * @return
     */
    @RequestMapping(value = "/panoramaList", method = RequestMethod.GET)
    public @ResponseBody Message panoramaList(){
        User user = authentService.getUserFromSubject();
        List<Panorama> panoramas;
        if(user!=null){
            panoramas = panoramaService.findAllPass(user.getId());
        }else{
            panoramas = panoramaService.findAllPass(null);
        }
        JSONArray panoramasJson = panoramaService.panoramasToJson(panoramas);
        return new Message(Message.Type.OK,panoramasJson);
    }

    /**
     * 获取全景列表
     * @return
     */
   /* @RequestMapping(value = "/panoramaPassList", method = RequestMethod.GET)
    public @ResponseBody Message panoramaPassList(){
        List<Panorama> panoramas = panoramaService.findAllPass(null);
        JSONArray panoramaJson = panoramaService.panoramaToJson(panoramas);
        return new Message(Message.Type.OK,panoramaJson);
    }
*/
    /**
     * 获取全景列表(个人)
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"user:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/personalPanoramaList", method = RequestMethod.GET)
    public @ResponseBody Message personalPanoramaList(){
        User user = authentService.getUserFromSubject();
        if (user == null) {
            return new Message(Message.Type.EXIST);
        }
        List<Panorama> panoramas = panoramaService.findByuser(user);
        JSONArray panoramasJson = panoramaService.panoramasToJson(panoramas);
        return new Message(Message.Type.OK,panoramasJson);
    }

    /**
     * 审核
     */

    /**
     * 兴趣点审核列表
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/interestReview", method = RequestMethod.GET)
    public @ResponseBody Message interestReview(){
        List<Interest> interests = interestService.findAllPending();
        JSONArray interestsJson = interestService.interestsToJson(interests);
        return new Message(Message.Type.OK,interestsJson);
    }

    /**
     * 全景审核列表
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/panoramaReview", method = RequestMethod.GET)
    public @ResponseBody Message panoramaReview(){
        List<Panorama> panoramas = panoramaService.findAllPending();
        JSONArray panoramasJson = panoramaService.panoramasToJson(panoramas);
        return new Message(Message.Type.OK,panoramasJson);
    }

    /**
     * 审核通过
     */

    /**
     * 个人兴趣点审核通过
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/interestReviewSuccess", method = RequestMethod.POST)
    public @ResponseBody Message interestReviewSuccess(@RequestBody Map<String, Object> objectMap){
        Message message = Message.parameterCheck(objectMap);
        if(message.getType()== Message.Type.FAIL){
            return message;
        }
        Interest interest = interestService.find(Long.valueOf(objectMap.get("id").toString()));
        interest.setStatus(CommonEnum.Review.PASS);
        interest.setReviewDate(new Date());
        interestService.save(interest);
        return new Message(Message.Type.OK);
    }

    /**
     * 全景审核通过
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/panoramaReviewSuccess", method = RequestMethod.POST)
    public @ResponseBody Message panoramaReviewSuccess(@RequestBody Map<String, Object> objectMap){
        Message message = Message.parameterCheck(objectMap);
        if(message.getType()== Message.Type.FAIL){
            return message;
        }
        Panorama panorama = panoramaService.find(Long.valueOf(objectMap.get("id").toString()));
        panorama.setStatus(CommonEnum.Review.PASS);
        panorama.setReviewDate(new Date());
        panoramaService.save(panorama);
        return new Message(Message.Type.OK);
    }

    /**
     * 个人兴趣点审核未通过
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/interestReviewFail", method = RequestMethod.POST)
    public @ResponseBody Message interestReviewFail(@RequestBody Map<String, Object> objectMap){
        Message message = Message.parameterCheck(objectMap);
        if(message.getType()== Message.Type.FAIL){
            return message;
        }
        if (objectMap.get("id") == null) {
            return new Message(Message.Type.FAIL);
        }
        Interest interest = interestService.find(Long.valueOf(objectMap.get("id").toString()));
        interest.setStatus(CommonEnum.Review.NOTPASS);
        if (objectMap.get("advice") == null) {
            return new Message(Message.Type.FAIL);
        }
        interest.setAdvice(objectMap.get("advice").toString());
        interest.setReviewDate(new Date());
        interestService.save(interest);
        return new Message(Message.Type.OK);
    }

    /**
     * 全景审核未通过
     * @param objectMap
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"admin:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/admin/panoramaReviewFail", method = RequestMethod.POST)
    public @ResponseBody Message panoramaReviewFail(@RequestBody Map<String, Object> objectMap){
        Message message = Message.parameterCheck(objectMap);
        if(message.getType()== Message.Type.FAIL){
            return message;
        }
        if (objectMap.get("id") == null) {
            return new Message(Message.Type.FAIL);
        }
        Panorama panorama = panoramaService.find(Long.valueOf(objectMap.get("id").toString()));
        panorama.setStatus(CommonEnum.Review.NOTPASS);
        if (objectMap.get("advice") == null) {
            return new Message(Message.Type.FAIL);
        }
        panorama.setAdvice(objectMap.get("advice").toString());
        panorama.setReviewDate(new Date());
        panoramaService.save(panorama);
        return new Message(Message.Type.OK);
    }

    /**
     * 根据id获取兴趣点详细信息
     * @param id
     * @return
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/interest/{id}", method = RequestMethod.GET)
    public @ResponseBody Message getInterest(@PathVariable("id") Long id) {
        Message message = Message.parametersCheck(id);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Interest interest = interestService.find(id);
        if (interest == null) {
            return new Message(Message.Type.FAIL);
        }
        JSONObject interestToJson = interestService.interestToJson(interest);
        return new Message(Message.Type.OK, interestToJson);
    }

    /**
     * 根据id获取全景详细信息
     * @param id
     * @return
     */
//    @RequiresAuthentication
//    @RequiresRoles(value = {"user:simple","account:simple"}, logical = Logical.OR)
    @RequestMapping(value = "/panorama/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    Message getProject(@PathVariable("id") Long id) {
        Message message = Message.parametersCheck(id);
        if (message.getType() == Message.Type.FAIL) {
            return message;
        }
        Panorama panorama = panoramaService.find(id);
        if (panorama == null) {
            return new Message(Message.Type.FAIL);
        }
        JSONObject panoramaToJson = panoramaService.panoramaToJson(panorama);
        return new Message(Message.Type.OK, panoramaToJson);
    }
}
