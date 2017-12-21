package com.hysw.qqsl.cloud.core.entity.project;

import com.hysw.qqsl.cloud.core.entity.data.User;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * 项目分享访问
 *
 * @since 2017年5月16日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 */
public class ShareVisit implements Serializable{

    private static final long serialVersionUID = -95356524309465662L;

    // 分享的用户
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
