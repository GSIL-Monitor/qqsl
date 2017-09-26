package com.hysw.qqsl.cloud.wechat.data.resp;

import com.hysw.qqsl.cloud.wechat.util.XStreamCDATA;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 回复视频消息
 * Created by chenl on 17-6-28.
 */
@XStreamAlias("xml")
public class VideoMessage extends BaseMessage{
    // 接收方帐号（收到的OpenID）
    @XStreamAlias("ToUserName")
    @XStreamCDATA
    private String ToUserName;
    // 开发者微信号
    @XStreamAlias("FromUserName")
    @XStreamCDATA
    private String FromUserName;
    // 消息创建时间 （整型）
    @XStreamAlias("CreateTime")
    @XStreamCDATA
    private long CreateTime;
    // 消息类型
    @XStreamAlias("MsgType")
    @XStreamCDATA
    private String MsgType;
    // 视频
    private Video Video;

    public String getToUserName() {
        return ToUserName;
    }

    public void setToUserName(String toUserName) {
        ToUserName = toUserName;
    }

    public String getFromUserName() {
        return FromUserName;
    }

    public void setFromUserName(String fromUserName) {
        FromUserName = fromUserName;
    }

    public long getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(long createTime) {
        CreateTime = createTime;
    }

    public String getMsgType() {
        return MsgType;
    }

    public void setMsgType(String msgType) {
        MsgType = msgType;
    }

    public Video getVideo() {
        return Video;
    }

    public void setVideo(Video video) {
        Video = video;
    }
}
