package com.hysw.qqsl.cloud.core.entity.station;

import com.hysw.qqsl.cloud.core.entity.data.User;
import net.sf.json.JSONObject;

import java.util.Date;

/**
 * Create by leinuo on 17-6-28 上午10:59
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
public class ShareVisit {
    private User user;

    private Date createTime;

    private ShareVisit(){}

    public ShareVisit(User user) {
        assert(user!=null);
        this.user = user;
        this.createTime = new Date();
    }

    public final User getUser() {
        return user;
    }

    public final Date getCreateTime() {
        return createTime;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", this.user.getId());
        jsonObject.put("name", this.user.getName());
        jsonObject.put("phone", this.user.getPhone());
        jsonObject.put("createTime", this.createTime);
        return jsonObject;
    }

}
