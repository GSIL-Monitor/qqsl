package com.hysw.qqsl.cloud.wechat.data.resp;

import com.hysw.qqsl.cloud.wechat.util.XStreamCDATA;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 视频
 * Created by chenl on 17-6-28.
 */
@XStreamAlias("Video")
public class Video {
    // 媒体文件id
    @XStreamAlias("MediaId")
    @XStreamCDATA
    private String MediaId;
    // 缩略图的媒体id
    @XStreamAlias("ThumbMediaId")
    @XStreamCDATA
    private String ThumbMediaId;

    public String getMediaId() {
        return MediaId;
    }

    public void setMediaId(String mediaId) {
        MediaId = mediaId;
    }

    public String getThumbMediaId() {
        return ThumbMediaId;
    }

    public void setThumbMediaId(String thumbMediaId) {
        ThumbMediaId = thumbMediaId;
    }
}
