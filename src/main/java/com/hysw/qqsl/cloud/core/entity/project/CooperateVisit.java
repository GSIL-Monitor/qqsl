package com.hysw.qqsl.cloud.core.entity.project;

import com.hysw.qqsl.cloud.core.entity.data.Account;
import net.sf.json.JSONObject;

import java.util.Date;

/**
 * 项目协同访问
 * 在企业与子账号间协同
 *
 * @since 2017年5月16日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 */
public class CooperateVisit {

    private Account account;
    private Type type;
    private Date createTime;
    private Date modifyTime;

    public enum Type{
        VISIT_INVITE_ELEMENT("招投标要素"),
        VISIT_INVITE_FILE("招投标文件"),
        VISIT_PREPARATION_ELEMENT("项目前期要素"),
        VISIT_PREPARATION_FILE("项目前期文件"),
        VISIT_BUILDING_ELEMENT("建设期要素"),
        VISIT_BUILDING_FILE("建设期文件"),
        VISIT_MAINTENANCE_ELEMENT("运营期要素"),
        VISIT_MAINTENANCE_FILE("运营期文件"),
        VISIT_VIEW("查看");
        //必须增加一个构造函数,变量,得到该变量的值
        private String  typeC;

        Type(String typeC) {
            this.typeC = typeC;
        }

        public String getTypeC() {
            return typeC;
        }
    }

    private CooperateVisit() {}

    public CooperateVisit(Type type) {
        this.type = type;
        this.createTime = new Date();
    }

    public CooperateVisit(Account account) {
        this.account = account;
        this.type = Type.VISIT_VIEW;
        this.createTime = new Date();
    }

    public final Account getAccount() {
        return account;
    }

    public final Type getType() {
        return type;
    }

    public final Date getCreateTime() {
        return createTime;
    }

    public final Date getModifyTime() {
        return modifyTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    /**
     * 协同注册
     * @param account
     * @return
     */
    public boolean register(Account account) {
        assert(account!=null);
        if (this.account==null) {
            this.account = account;
            this.createTime = new Date();
            this.modifyTime = null;
            return true;
        }
        return false;
    }

    /**
     * 协同注销
     * @return
     */
    public void unRegister() {
        this.account = null;
        this.createTime = null;
        this.modifyTime = null;
    }

    /**
     * 转换为json
     * @return
     */
    public JSONObject toJson() {
        if(this.getAccount()==null){
           return null;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", this.account.getId());
        jsonObject.put("name", this.account.getName());
        jsonObject.put("phone", this.account.getPhone());
        jsonObject.put("createTime", this.createTime.getTime());
        jsonObject.put("modifyTime", this.modifyTime==null?null:this.modifyTime.getTime());
        return jsonObject;
    }

}
