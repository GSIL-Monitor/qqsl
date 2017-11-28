package com.hysw.qqsl.cloud.core.entity;

import java.util.Date;

/**
 * StorageCountLog由StorageLog表中的数据后台开机时构建的虚拟数据，每次构建一个月的，每1小时1条记录，
 * 共有720条左右记录，定时任务生成1条记录，就从缓存中删除一条最旧的记录，保证日志有720条。
 *
 * Create by leinuo on 17-11-28 下午5:31
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
public class StorageCountLog {

    /** ID */
    private Long id;
    /** 创建日期 */
    private Date createDate;
    /** 修改日期 */
    private Date modifyDate;
    /** 用户id */
    private long userId;
    /** 完成后的空间数 */
    private long curSpaceNum;
    /** 上传大小 */
    private long uploadCount;
    /** 下载大小 */
    private long downloadCount;
    /** 完成后的流量数 */
    private long curTrafficNum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getCurSpaceNum() {
        return curSpaceNum;
    }

    public void setCurSpaceNum(long curSpaceNum) {
        this.curSpaceNum = curSpaceNum;
    }

    public long getUploadCount() {
        return uploadCount;
    }

    public void setUploadCount(long uploadCount) {
        this.uploadCount = uploadCount;
    }

    public long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public long getCurTrafficNum() {
        return curTrafficNum;
    }

    public void setCurTrafficNum(long curTrafficNum) {
        this.curTrafficNum = curTrafficNum;
    }
}
