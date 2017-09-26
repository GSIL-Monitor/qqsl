package com.hysw.qqsl.cloud.wechat.data.resp;

import com.hysw.qqsl.cloud.wechat.util.XStreamCDATA;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 音乐
 * Created by chenl on 17-6-28.
 */
@XStreamAlias("Music")
public class Music {
    // 音乐标题
    @XStreamAlias("Title")
    @XStreamCDATA
    private String Title;
    // 音乐描述
    @XStreamAlias("Description")
    @XStreamCDATA
    private String Description;
    // 音乐链接
    @XStreamAlias("MusicUrl")
    @XStreamCDATA
    private String MusicUrl;
    // 高质量音乐链接，WIFI环境优先使用该链接播放音乐
    @XStreamAlias("HQMusicUrl")
    @XStreamCDATA
    private String HQMusicUrl;
    // 缩略图的媒体id，通过上传多媒体文件得到的id
    @XStreamAlias("ThumbMediaId")
    @XStreamCDATA
    private String ThumbMediaId;

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

    public String getMusicUrl() {
        return MusicUrl;
    }

    public void setMusicUrl(String musicUrl) {
        MusicUrl = musicUrl;
    }

    public String getHQMusicUrl() {
        return HQMusicUrl;
    }

    public void setHQMusicUrl(String HQMusicUrl) {
        this.HQMusicUrl = HQMusicUrl;
    }

    public String getThumbMediaId() {
        return ThumbMediaId;
    }

    public void setThumbMediaId(String thumbMediaId) {
        ThumbMediaId = thumbMediaId;
    }
}
