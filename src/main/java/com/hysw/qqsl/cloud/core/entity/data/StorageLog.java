package com.hysw.qqsl.cloud.core.entity.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**存储日志
 * 保存用户当前存储空间的剩余量,以及上传下载的流量统计;
 * Create by leinuo on 17-11-28 下午5:12
 * <p>
 * qq:1321404703 https://github.com/leinuo2016
 */
@Entity
@Table(name="storageLog")
@SequenceGenerator(name="sequenceGenerator", sequenceName="storageLog_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})
public class StorageLog extends BaseEntity {

    private static final long serialVersionUID = 5684132177393981007L;

    /** 用户id */
    private long userId;
    /** 完成后的空间数 */
    private long curSpaceNum;
    /** 上传大小 */
    private long uploadSize;
    /** 下载大小 */
    private long downloadSize;
    /** 完成后的流量数 */
    private long curTrafficNum;

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

    public long getUploadSize() {
        return uploadSize;
    }

    public void setUploadSize(long uploadSize) {
        this.uploadSize = uploadSize;
    }

    public long getDownloadSize() {
        return downloadSize;
    }

    public void setDownloadSize(long downloadSize) {
        this.downloadSize = downloadSize;
    }

    public long getCurTrafficNum() {
        return curTrafficNum;
    }

    public void setCurTrafficNum(long curTrafficNum) {
        this.curTrafficNum = curTrafficNum;
    }
}
