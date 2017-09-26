package com.hysw.qqsl.cloud.wechat.data.req;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

/**
 * Created by chenl on 17-6-29.
 */
@XStreamAlias("xml")
public class InputMessage implements Serializable {

    @XStreamAlias("ToUserName")
    private String ToUserName;
    @XStreamAlias("FromUserName")
    private String FromUserName;
    @XStreamAlias("CreateTime")
    private Long CreateTime;
    @XStreamAlias("MsgType")
    private String MsgType = "text";
    @XStreamAlias("MsgId")
    private Long MsgId;
    // 文本消息
    @XStreamAlias("Content")
    private String Content;
    // 图片消息
    @XStreamAlias("PicUrl")
    private String PicUrl;
    // 位置消息
    @XStreamAlias("LocationX")
    private String LocationX;
    @XStreamAlias("LocationY")
    private String LocationY;
    @XStreamAlias("Scale")
    private Long Scale;
    @XStreamAlias("Label")
    private String Label;
    // 链接消息
    @XStreamAlias("Title")
    private String Title;
    @XStreamAlias("Description")
    private String Description;
    @XStreamAlias("Url")
    private String URL;
    // 语音信息
    @XStreamAlias("MediaId")
    private String MediaId;
    @XStreamAlias("Format")
    private String Format;
    @XStreamAlias("Recognition")
    private String Recognition;
    //    视频信息
    @XStreamAlias("ThumbMediaId")
    private String ThumbMediaId;
    // 事件
    @XStreamAlias("Event")
    private String Event;
    @XStreamAlias("EventKey")
    private String EventKey;
    @XStreamAlias("Ticket")
    private String Ticket;
    @XStreamAlias("ScanCodeInfo")
    private String ScanCodeInfo;
    @XStreamAlias("MenuId")
    private String MenuId;

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

    public Long getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(Long createTime) {
        CreateTime = createTime;
    }

    public String getMsgType() {
        return MsgType;
    }

    public void setMsgType(String msgType) {
        MsgType = msgType;
    }

    public Long getMsgId() {
        return MsgId;
    }

    public void setMsgId(Long msgId) {
        MsgId = msgId;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getPicUrl() {
        return PicUrl;
    }

    public void setPicUrl(String picUrl) {
        PicUrl = picUrl;
    }

    public String getLocationX() {
        return LocationX;
    }

    public void setLocationX(String locationX) {
        LocationX = locationX;
    }

    public String getLocationY() {
        return LocationY;
    }

    public void setLocationY(String locationY) {
        LocationY = locationY;
    }

    public Long getScale() {
        return Scale;
    }

    public void setScale(Long scale) {
        Scale = scale;
    }

    public String getLabel() {
        return Label;
    }

    public void setLabel(String label) {
        Label = label;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String uRL) {
        URL = uRL;
    }

    public String getEvent() {
        return Event;
    }

    public void setEvent(String event) {
        Event = event;
    }

    public String getEventKey() {
        return EventKey;
    }

    public void setEventKey(String eventKey) {
        EventKey = eventKey;
    }

    public String getMediaId() {
        return MediaId;
    }

    public void setMediaId(String mediaId) {
        MediaId = mediaId;
    }

    public String getFormat() {
        return Format;
    }

    public void setFormat(String format) {
        Format = format;
    }

    public String getRecognition() {
        return Recognition;
    }

    public void setRecognition(String recognition) {
        Recognition = recognition;
    }

    public String getThumbMediaId() {
        return ThumbMediaId;
    }

    public void setThumbMediaId(String thumbMediaId) {
        ThumbMediaId = thumbMediaId;
    }

    public String getTicket() {
        return Ticket;
    }

    public void setTicket(String ticket) {
        Ticket = ticket;
    }

    public String getScanCodeInfo() {
        return ScanCodeInfo;
    }

    public void setScanCodeInfo(String scanCodeInfo) {
        ScanCodeInfo = scanCodeInfo;
    }

    public String getMenuId() {
        return MenuId;
    }

    public void setMenuId(String menuId) {
        MenuId = menuId;
    }
}
