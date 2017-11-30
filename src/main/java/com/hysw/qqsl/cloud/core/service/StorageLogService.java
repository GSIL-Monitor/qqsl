package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.StorageLogDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.StorageCountLog;
import com.hysw.qqsl.cloud.core.entity.data.StorageLog;
import com.hysw.qqsl.cloud.core.entity.data.User;
import com.hysw.qqsl.cloud.pay.entity.data.Package;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
    /**
     * 获取到的用户列表
     */
    private List<Long> userIds = new ArrayList<>();
    /**
     * 一个月内所有用户的存储日志
     */
    private Map<Long,List<StorageLog>> storageLogMap= new HashMap<>();
    /**
     * 一个月内所有用户的存储日志缓存
     */
    private Map<Long,List<StorageCountLog>> storageCountLogMap= new HashMap<>();
    /**
     * 一个月的时间毫秒数
     */
    private static final  long MONTH_TIMES = 30*24*60*60*1000L;
    /**
     * 两个小时的时间毫秒数
     */
    private static final  long TWO_HOUR_TIMES = 2*60*60*1000L;
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
        List<StorageCountLog> storageCountLogs = storageCountLogMap.get(user.getId());
        if(storageCountLogs==null){
            return null;
        }
        JSONArray storageCountLogJsons = new JSONArray();
        JSONObject jsonObject;
        StorageCountLog storageCountLog;
        for(int i = 0;i < storageCountLogs.size();i++){
            storageCountLog = storageCountLogs.get(i);
            jsonObject = makeStorageCountLogJson(storageCountLog);
            storageCountLogJsons.add(jsonObject);
        }
        return storageCountLogJsons;
    }

    /**
     * 定时每两个小时构建一次日志
     * @return
     */
    public void buildStorageCountLogByHour(){
        List<StorageLog> storageLogs = findByTwoHour();
        StorageLog storageLog;
        for (int i = 0;i < storageLogs.size();i++){
           storageLog = storageLogs.get(i);
           if (userIds.contains(storageLog.getUserId())){

           }else {
               userIds.add(storageLog.getUserId());
           }
        }
    }

    /**
     * 启动构建一次日志
     * @return
     */
    public void buildStorageLog(){
        List<StorageLog> storageLogs = findByMonth();
        StorageLog storageLog;
        for (int i = 0;i < storageLogs.size();i++){
            storageLog = storageLogs.get(i);
          if(!userIds.contains(storageLog.getUserId())){
              userIds.add(storageLog.getUserId());
          }
        }
        List<StorageLog> storageLogs1;
        for(int j = 0;j < userIds.size();j++){
            storageLogs1 = new ArrayList<>();
            for (int i = 0;i < storageLogs.size();i++) {
                storageLog = storageLogs.get(i);
                if (userIds.get(j).equals(storageLog.getUserId())) {
                    storageLogs1.add(storageLog);
                }
            }
            storageLogMap.put(userIds.get(j),storageLogs1);
        }
    }

    /**
     * 构建所有用户一个月的存储日志缓存
     */
    private void buildStorageCountLogs(){
        long userId;
        List<StorageCountLog> storageCountLogs;
       for(int i = 0;i < userIds.size();i++){
           userId = userIds.get(i);
           storageCountLogs = buildStorageCountLogsByUser(userId);
           storageCountLogMap.put(userId,storageCountLogs);
       }
    }

    /**
     * 构建一个用户一个月内的存储日志
     * @param userId
     * @return
     */
    private List<StorageCountLog>  buildStorageCountLogsByUser(Long userId){
        List<StorageLog> storageLogs = storageLogMap.get(userId);
        long now = System.currentTimeMillis();
        long month = System.currentTimeMillis()-MONTH_TIMES;
        long cut;
        List<StorageCountLog> storageCountLogs = new ArrayList<>();
        StorageCountLog storageCountLog;
        for(int i = 0;i < 360;i++){
            cut = month+TWO_HOUR_TIMES;
            if(cut>now){
                return storageCountLogs;
            }
            storageCountLog = getStorageCountLogByTwoHour(cut,storageLogs);
            storageCountLogs.add(storageCountLog);
        }
        return storageCountLogs;
    }

    /**
     * 每两个小时的存储记录
     * @param cut
     * @param storageLogs
     */
    private StorageCountLog getStorageCountLogByTwoHour(long cut,List<StorageLog> storageLogs){
        StorageCountLog storageCountLog = new StorageCountLog();
        StorageLog storageLog;
        /** 完成后的空间数 */
        long curSpaceNum = 0, uploadCount = 0, downloadCount = 0,curTrafficNum = 0;
        long createDateTime;
        for(int i = 0;i < storageLogs.size();i++){
            storageLog = storageLogs.get(i);
            createDateTime = storageLog.getCreateDate().getTime();
            if(cut-TWO_HOUR_TIMES<=createDateTime&&createDateTime<cut){
                curSpaceNum = curSpaceNum + storageLog.getCurSpaceNum();
                uploadCount = uploadCount + storageLog.getUploadSize();
                downloadCount = downloadCount + storageLog.getDownloadSize();
                curTrafficNum = curTrafficNum + storageLog.getCurTrafficNum();
            }
        }
        storageCountLog.setCurSpaceNum(curSpaceNum);
        storageCountLog.setCurTrafficNum(curTrafficNum);
        storageCountLog.setUserId(storageLogs.get(0).getUserId());
        storageCountLog.setUploadCount(uploadCount);
        storageCountLog.setDownloadCount(downloadCount);
        storageCountLog.setCreateDate(new Date(cut));
        return storageCountLog;
    }


    /**
     * 构建存储日志json数据
     * @param storageCountLog
     * @return
     */
    private JSONObject makeStorageCountLogJson(StorageCountLog storageCountLog) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId",storageCountLog.getUserId());
        jsonObject.put("createDate",storageCountLog.getCreateDate().getTime());
        jsonObject.put("curSpaceNum",storageCountLog.getCurSpaceNum());
        jsonObject.put("downloadCount",storageCountLog.getDownloadCount());
        jsonObject.put("curTrafficNum",storageCountLog.getCurTrafficNum());
        jsonObject.put("uploadCount",storageCountLog.getUploadCount());
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

    /**
     * 查询数据库所有用户两个小时以内的存储日志数据
     * @return
     */
    private List<StorageLog> findByTwoHour() {
        List<Filter> filters = new ArrayList<>();
        Long times = System.currentTimeMillis();
        filters.add(Filter.between("createDate", new Date(times-TWO_HOUR_TIMES), new Date(times)));
        List<StorageLog> storageLogs = storageLogDao.findList(0,null,filters);
        return storageLogs;
    }


}
