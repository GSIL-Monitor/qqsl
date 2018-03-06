package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonEnum;
import com.hysw.qqsl.cloud.core.controller.Message;
import com.hysw.qqsl.cloud.core.dao.PanoramaDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.ObjectFile;
import com.hysw.qqsl.cloud.core.entity.data.Panorama;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.pay.entity.PackageItem;
import com.hysw.qqsl.cloud.pay.entity.PackageModel;
import com.hysw.qqsl.cloud.pay.entity.ServeItem;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import com.hysw.qqsl.cloud.pay.service.PackageService;
import com.hysw.qqsl.cloud.pay.service.TradeService;
import com.hysw.qqsl.cloud.util.SettingUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private PackageService packageService;
    @Autowired
    private TradeService tradeService;

    @Autowired
    public void setBaseDao(PanoramaDao panoramaDao) {
        super.setBaseDao(panoramaDao);
    }

    /**
     * 查询所有审核通过的全景和用户自己建立的全景
     * @return
     */
    public List<Panorama> findAllPass(Long userId){
        List<Filter> filters1 = new ArrayList<>();
        filters1.add(Filter.eq("status", CommonEnum.Review.PASS));
        filters1.add(Filter.eq("share", true));
        List<Panorama> panoramas;
        if (userId == null) {
            panoramas = panoramaDao.findList(0, null, filters1);
        }else{
            List<Filter> filters2 = new ArrayList<>();
            filters2.add(Filter.eq("userId", userId));
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
        filters.add(Filter.eq("share", true));
        List<Panorama> panoramas = panoramaDao.findList(0, null, filters);
        return panoramas;
    }

    public Message savePanorama(Map<String, Object> map, Panorama panorama) {
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
        panorama.setShootDate((Date)shootDate);
        if (name == null || coor == null || isShare == null) {
            return new Message(Message.Type.FAIL);
        }
        Message message = SettingUtils.checkCoordinateIsInvalid(coor.toString());
        if (!Message.Type.OK.equals(message.getType())) {
            return message;
        }
        panorama.setName(name.toString());
        panorama.setCoor(message.getData().toString());
       // panorama.setRegion(region.toString());
        panorama.setStatus(CommonEnum.Review.valueOf(Integer.valueOf(status.toString())));
        panorama.setShare(Boolean.valueOf(isShare.toString()));
        if (picture != null) {
            panorama.setPicture(picture.toString());
        }
        panorama.setUserId(Long.valueOf(userId.toString()));
        save(panorama);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", panorama.getId());
        return new Message(Message.Type.OK,jsonObject);
    }

    public List<Panorama> findByuser(User user) {
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

    /**
     * 是否允许保存全景
     * @param user
     * @Return Message
     */
    public Message isAllowSavePanorma(User user) {
        Package aPackage = packageService.findByUser(user);
        if (aPackage == null) {
            return new Message(Message.Type.EXIST);
        }
        int size = findByuser(user).size();
        PackageModel packageModel = tradeService.getPackageModel(aPackage.getType().toString());
        for (PackageItem packageItem : packageModel.getPackageItems()) {
            if (packageItem.getServeItem().getType() == ServeItem.Type.PANO && size < packageItem.getLimitNum()) {
                return new Message(Message.Type.OK);
            }
        }
        return new Message(Message.Type.NO_ALLOW);
    }
}
