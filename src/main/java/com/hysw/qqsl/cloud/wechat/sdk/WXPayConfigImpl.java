package com.hysw.qqsl.cloud.wechat.sdk;

import com.hysw.qqsl.cloud.CommonAttributes;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Service("wXPayConfigImpl")
public class WXPayConfigImpl extends WXPayConfig {

    private byte[] certData;
    private static WXPayConfigImpl INSTANCE;

    private WXPayConfigImpl() throws Exception{
        String certPath = "/apiclient_cert.p12";
        File file = new ClassPathResource(certPath).getFile();
        InputStream certStream = new FileInputStream(file);
        this.certData = new byte[(int) file.length()];
        certStream.read(this.certData);
        certStream.close();
    }

    public static WXPayConfigImpl getInstance() throws Exception{
        if (INSTANCE == null) {
            synchronized (WXPayConfigImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WXPayConfigImpl();
                }
            }
        }
        return INSTANCE;
    }

    public String getAppID() {
//        return "wxab8acb865bb1637e";
        return CommonAttributes.APPID;
    }

    public String getMchID() {
//        return "11473623";
        return CommonAttributes.MCHID;
    }

    public String getKey() {
//        return "2ab9071b06b9f739b950ddb41db2690d";
        return CommonAttributes.APIKEY;
    }

    public InputStream getCertStream() {
        ByteArrayInputStream certBis;
        certBis = new ByteArrayInputStream(this.certData);
        return certBis;
    }


    public int getHttpConnectTimeoutMs() {
        return 2000;
    }

    public int getHttpReadTimeoutMs() {
        return 10000;
    }

    IWXPayDomain getWXPayDomain() {
        return WXPayDomainSimpleImpl.instance();
    }

    public String getPrimaryDomain() {
        return "api.mch.weixin.qq.com";
    }

    public String getAlternateDomain() {
        return "api2.mch.weixin.qq.com";
    }

    @Override
    public int getReportWorkerNum() {
        return 1;
    }

    @Override
    public int getReportBatchSize() {
        return 2;
    }
}
