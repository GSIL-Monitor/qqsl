package com.hysw.qqsl.cloud.wechat.data.resp;

import com.hysw.qqsl.cloud.wechat.util.XStreamCDATA;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 语音
 * Created by chenl on 17-6-28.
 */
@XStreamAlias("Voice")
public class Voice {
    // 媒体文件id
    @XStreamAlias("MediaId")
    @XStreamCDATA
    private String MediaId;

    public String getMediaId() {
        return MediaId;
    }

    public void setMediaId(String mediaId) {
        MediaId = mediaId;
    }
}
