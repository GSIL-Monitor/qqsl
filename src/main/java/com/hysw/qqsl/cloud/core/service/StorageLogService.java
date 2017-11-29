package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.StorageLogDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.StorageLog;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 存储日志
 * Create by leinuo on 17-11-28 下午6:01
 *
 * qq:1321404703 https://github.com/leinuo2016
 */
@Service("storageLogService")
public class StorageLogService extends BaseService<StorageLog, Long> {
    @Autowired
    private StorageLogDao storageLogDao;
    private JSONArray jsonArray = new JSONArray();
    @Autowired
    public void setBaseDao(StorageLogDao storageLogDao) {
        super.setBaseDao(storageLogDao);
    }



    /**
     * 用户下所有相关文件上传,下载,删除时需要保存的存储日志
     * @param aPackage
     * @param type
     * @param fileSize
     */
    public void saveStorageLog(Package aPackage, String type, Object fileSize){
        StorageLog storageLog = new StorageLog();
        storageLog.setUserId(aPackage.getUser().getId());
        switch (type){
            case "upload":
                storageLog.setUploadSize(Long.valueOf(fileSize.toString()));
                return;
            case "download":
                storageLog.setDownloadSize(Long.valueOf(fileSize.toString()));
                return;
            case "delete":
                storageLog.setUploadSize(-Long.valueOf(fileSize.toString()));
                return;
        }
        storageLog.setCurTrafficNum(aPackage.getCurTrafficNum());
        storageLog.setCurSpaceNum(aPackage.getCurSpaceNum());
        storageLogDao.save(storageLog);
    }

    /**
     * 从缓存查找当前用户的该月所有存储日志
     * @param user
     * @return
     */
    public JSONArray getStorageCountLog(User user){
        JSONArray storageCountLogJsons = new JSONArray();
        JSONObject jsonObject;
        for(int i = 0;i < jsonArray.size();i++){
            jsonObject = (JSONObject) jsonArray.get(i);
            if(user.getId().equals(jsonObject.getLong("userId"))){
               storageCountLogJsons.add(jsonObject);
            }
        }
        return storageCountLogJsons;
    }

    /**
     * 定时每两个小时构建一次日志
     * @return
     */
    public void buildStorageCountLog(){
        jsonArray.clear();
        List<StorageLog> storageLogs = findByMonth();
        JSONObject jsonObject;
        for (int i = 0;i < storageLogs.size();i++){
            jsonObject = makeStorageCountLog(storageLogs.get(i));
            jsonArray.add(jsonObject);
        }
    }

    /**
     * 构建存储日志json数据
     * @param storageLog
     * @return
     */
    private JSONObject makeStorageCountLog(StorageLog storageLog) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",storageLog.getId());
        jsonObject.put("userId",storageLog.getUserId());
        jsonObject.put("createDate",storageLog.getCreateDate().getTime());
        jsonObject.put("modifyDate",storageLog.getModifyDate().getTime());
        jsonObject.put("curSpaceNum",storageLog.getCurSpaceNum());
        jsonObject.put("downloadSize",storageLog.getDownloadSize());
        jsonObject.put("curTrafficNum",storageLog.getCurTrafficNum());
        jsonObject.put("uploadSize",storageLog.getUploadSize());
        return jsonObject;
    }

    /**
     * 查询数据库所有用户一个月以内的存储日志数据
     * @return
     */
    private List<StorageLog> findByMonth() {
        List<Filter> filters = new ArrayList<>();
        Long times = System.currentTimeMillis();
        filters.add(Filter.between("createDate", new Date(times-30*24*60*60*1000L), new Date(times)));
        List<StorageLog> storageLogs = storageLogDao.findList(0,null,filters);
        return storageLogs;
    }

}
