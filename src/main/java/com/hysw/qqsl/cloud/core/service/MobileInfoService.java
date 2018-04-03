package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.core.dao.MobileInfoDao;
import com.hysw.qqsl.cloud.core.entity.data.MobileInfo;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by leinuo on 17-6-29 上午10:49
 *
 * qq:1321404703 https://github.com/leinuo2016
 *
 * 移动端版本号业务层
 */
@Service("mobileInfoService")
public class MobileInfoService extends BaseService<MobileInfo,Long> {
    @Autowired
    private MobileInfoDao mobileInfoDao;
    public void setBaseDao(MobileInfoDao mobileInfoDao){
        super.setBaseDao(mobileInfoDao);
    }


    /**
     * 更新版本号
     * @param version
     * @return
     */
    public void update(Long version) {
        MobileInfo mobileInfo = mobileInfoDao.find(1L);
        mobileInfo.setVersion(version);
        mobileInfoDao.save(mobileInfo);
    }

    /**
     * 获取版本号
     * @return
     */
    public JSONObject findVersion() {
        MobileInfo mobileInfo = mobileInfoDao.find(1L);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("version",mobileInfo.getVersion());
        return jsonObject;
    }
}
