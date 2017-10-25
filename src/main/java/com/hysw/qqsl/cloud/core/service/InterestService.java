package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.dao.InterestDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.ObjectFile;
import com.hysw.qqsl.cloud.core.entity.data.Interest;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.core.entity.Review;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chenl on 17-1-9.
 */
@Service("interestService")
public class InterestService extends BaseService<Interest, Long> {
    @Autowired
    private InterestDao interestDao;
    @Autowired
    private UserService userService;
    @Autowired
    private OssService ossService;

    @Autowired
    public void setBaseDao(InterestDao interestDao) {
        super.setBaseDao(interestDao);
    }


//    public void reflectSaveProprety(Map<String,Object> map,Object obj){
//        Class<?> clazz = null;
//        try {
//            clazz = Class.forName(super.entityClass.getName());
//        } catch (ClassNotFoundException e) {
//            return;
//        }
//        for (Map.Entry<String, Object> entry : map.entrySet()) {
//            setPropretyValue(entry.getKey(),entry.getValue().toString(),obj,clazz);
//        }
//        save((Interest) obj);
//    }
//    private void setPropretyValue(String parm, String value, Object obj, Class<?> clazz){
//        // 可以直接对 private 的属性赋值
//        Field field = null;
//        try {
//            field = clazz.getDeclaredField(parm);
//        } catch (NoSuchFieldException e) {
//            return;
//        }
//        Type type = field.getGenericType();
//        String s;
//        String s1 = type.toString().substring(type.toString().lastIndexOf(".")+1);
//        if (s1.contains("$")) {
//            s = s1.substring(s1.lastIndexOf("$") + 1);
//        }else{
//            s = s1;
//        }
//        field.setAccessible(true);
//        try {
//            if (s.toLowerCase().equals("string")) {
//                field.set(obj, value);
//            }
//            if (s.toLowerCase().equals("long")) {
//                field.set(obj, Long.valueOf(value));
//            }
//            if (s.toLowerCase().equals("int")) {
//                field.set(obj, Integer.valueOf(value));
//            }
//            if (s.toLowerCase().equals("date")) {
//                field.set(obj, Date.valueOf(value));
//            }
//            if (s.toLowerCase().equals("review")) {
//                field.set(obj, Review.valueOf(value));
//            }
//            if (s.toLowerCase().equals("category")) {
//                field.set(obj, Interest.Category.valueOf(value));
//            }
//            if (s.toLowerCase().equals("type")) {
//                field.set(obj, Interest.Type.valueOf(value));
//            }
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 查询所有审核通过的兴趣点和用户自己建立的兴趣点
     * @return
     */
    public List<Interest> findAllPass(Long userId){
        List<Filter> filters1 = new ArrayList<>();
        filters1.add(Filter.eq("status", Review.PASS));
        List<Interest> interests;
        if (userId == null) {
            interests = interestDao.findList(0, null, filters1);
        }else{
            List<Filter> filters2 = new ArrayList<>();
            filters2.add(Filter.eq("userId", userId));
            interests = interestDao.findList(0, null, filters1,filters2);
        }
        return interests;
    }

    public JSONArray interestsToJson(List<Interest> interests){
        JSONObject jsonObject;
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < interests.size(); i++) {
            jsonObject = new JSONObject();
            jsonObject.put("id", interests.get(i).getId());
            jsonObject.put("createDate", interests.get(i).getCreateDate());
            jsonObject.put("modifyDate", interests.get(i).getModifyDate());
            jsonObject.put("name", interests.get(i).getName());
            jsonObject.put("type", interests.get(i).getType());
            jsonObject.put("category", interests.get(i).getCategory().ordinal());
            jsonObject.put("coordinate", interests.get(i).getCoordinate());
            jsonObject.put("region", interests.get(i).getRegion());
            jsonObject.put("contact", interests.get(i).getContact());
            jsonObject.put("content", interests.get(i).getContent());
            jsonObject.put("evaluate", interests.get(i).getEvaluate());
            jsonObject.put("business", interests.get(i).getBusiness());
            jsonObject.put("level", interests.get(i).getLevel());
            List<ObjectFile> objectFiles;
            if (SettingUtils.getInstance().getSetting().getStatus().equals("run")) {
                objectFiles= ossService
                        .getSubdirectoryFiles("interest" + "/" +interests.get(i).getId(),"qqslimage");
            }else{
                objectFiles= ossService
                        .getSubdirectoryFiles("interest_test" + "/" +interests.get(i).getId(),"qqslimage");
            }
            jsonObject.put("pictures", objectFiles);
            jsonObject.put("status", interests.get(i).getStatus());
            jsonObject.put("advice", interests.get(i).getAdvice());
            jsonObject.put("reviewDate", interests.get(i).getReviewDate());
            if (interests.get(i).getUserId() != null) {
                jsonObject.put("user", userJson(interests.get(i).getUserId()));
            }
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    private JSONObject userJson(Long userId) {
        User user = userService.find(userId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userName", user.getName());
        jsonObject.put("id", user.getId());
        jsonObject.put("phone", user.getPhone());
        return jsonObject;
    }


    /**
     * 查询所有待审核的兴趣点
     * @return
     */
    public List<Interest> findAllPending() {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("status", Review.PENDING));
        List<Interest> interests = interestDao.findList(0, null, filters);
        return interests;
    }

    public Message saveInterest(Map<String, Object> map,Interest interest) {
        Object name=map.get("name");
        Object type=map.get("type");
        Object category=map.get("category");
        Object coordinate=map.get("coordinate");
        Object region=map.get("region");
        Object contact=map.get("contact");
        Object content=map.get("content");
        Object evaluate=map.get("evaluate");
        Object business=map.get("business");
        Object level=map.get("level");
        Object pictures=map.get("pictures");
        Object status=map.get("status");
//        Object advice=map.get("advice");
//        Object reviewDate=map.get("reviewDate");
        if (name == null || category == null || coordinate == null || region == null || business == null) {
            return new Message(Message.Type.FAIL);
        }
        Message message = SettingUtils.checkCoordinateIsInvalid(coordinate.toString());
        if (!Message.Type.OK.equals(message.getType())) {
            return message;
        }
        interest.setCoordinate(message.getData().toString());
        interest.setName(name.toString());
        interest.setType(Interest.Type.valueOf(Integer.valueOf(type.toString())));
        interest.setCategory(Interest.Category.valueOf(Integer.valueOf(category.toString())));
        interest.setRegion(region.toString());
        if (contact != null) {
            interest.setContact(contact.toString());
        }
        if (content != null) {
            interest.setContent(content.toString());
        }
        if (evaluate != null) {
            interest.setEvaluate(evaluate.toString());
        }
        interest.setBusiness(business.toString());
        if (level != null) {
            interest.setLevel(level.toString());
        }
        if (pictures != null) {
            interest.setPictures(pictures.toString());
        }
        interest.setStatus(Review.valueOf(Integer.valueOf(status.toString())));
        save(interest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", interest.getId());
        return new Message(Message.Type.OK,jsonObject);
    }

    public List<Interest> findAllBase() {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("type", Interest.Type.BASE));
        List<Interest> interests = interestDao.findList(0, null, filters);
        return interests;
    }

    public List<Interest> findAllPersonal(User user) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("type", Interest.Type.PERSONAL));
        filters.add(Filter.eq("userId", user.getId()));
        List<Interest> interests = interestDao.findList(0, null, filters);
        return interests;
    }

    public JSONObject interestToJson(Interest interest) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", interest.getId());
        jsonObject.put("createDate", interest.getCreateDate());
        jsonObject.put("modifyDate", interest.getModifyDate());
        jsonObject.put("name", interest.getName());
        jsonObject.put("type", interest.getType());
        jsonObject.put("category", interest.getCategory().ordinal());
        jsonObject.put("coordinate", interest.getCoordinate());
        jsonObject.put("region", interest.getRegion());
        jsonObject.put("contact", interest.getContact());
        jsonObject.put("content", interest.getContent());
        jsonObject.put("evaluate", interest.getEvaluate());
        jsonObject.put("business", interest.getBusiness());
        jsonObject.put("level", interest.getLevel());
        List<ObjectFile> objectFiles;
        if (SettingUtils.getInstance().getSetting().getStatus().equals("run")) {
            objectFiles= ossService
                    .getSubdirectoryFiles("interest" + "/" +interest.getId(),"qqslimage");
        }else{
            objectFiles= ossService
                    .getSubdirectoryFiles("interest_test" + "/" +interest.getId(),"qqslimage");
        }
        jsonObject.put("pictures", objectFiles);
        jsonObject.put("status", interest.getStatus());
        jsonObject.put("advice", interest.getAdvice());
        jsonObject.put("reviewDate", interest.getReviewDate());
        jsonObject.put("user", userJson(interest.getUserId()));
        return jsonObject;
    }
}
