package com.hysw.qqsl.cloud.wechat.service;

import org.junit.Test;

public class testShortUrlService {

    @Test
    public void testShortUrlService(){
        ShortUrlService shortUrlService = new ShortUrlService();
        shortUrlService.longUrlToShortUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx3b65c94d27a017aa&redirect_uri=http%3A%2F%2Fwww.qingqingshuili.com%2Fhot-update%2FweChat%2Fwww%2Fauth.html&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect");
    }

}
