package com.hysw.qqsl.cloud.wechat.data.resp;

import com.hysw.qqsl.cloud.wechat.util.XStreamCDATA;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.List;

/**
 * 回复图文消息
 * Created by chenl on 17-6-28.
 */
@XStreamAlias("xml")
public class NewsMessage extends BaseMessage{
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
    // 图文消息个数，限制为10条以内
    @XStreamAlias("ArticleCount")
    @XStreamCDATA
    private int ArticleCount;
    // 多条图文消息信息，默认第一个item为大图
    private List<Article> Articles;

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

    public int getArticleCount() {
        return ArticleCount;
    }

    public void setArticleCount(int articleCount) {
        ArticleCount = articleCount;
    }

    public List<Article> getArticles() {
        return Articles;
    }

    public void setArticles(List<Article> articles) {
        Articles = articles;
    }
}
