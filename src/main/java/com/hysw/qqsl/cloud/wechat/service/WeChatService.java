package com.hysw.qqsl.cloud.wechat.service;

import com.hysw.qqsl.cloud.wechat.dao.WeChatDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.wechat.entity.data.WeChat;
import com.hysw.qqsl.cloud.core.service.BaseService;
import com.hysw.qqsl.cloud.wechat.entity.resp.Article;
import com.hysw.qqsl.cloud.wechat.entity.resp.BaseMessage;
import com.hysw.qqsl.cloud.wechat.entity.resp.NewsMessage;
import com.hysw.qqsl.cloud.wechat.entity.resp.TextMessage;
import com.hysw.qqsl.cloud.wechat.util.EncryptAndVerifiy;
import com.hysw.qqsl.cloud.wechat.entity.req.InputMessage;
import com.hysw.qqsl.cloud.wechat.util.MessageUtil;
import com.thoughtworks.xstream.XStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.security.DigestException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 微信公众号业务逻辑处理
 * Created by chenl on 17-6-28.
 */
@Service("weChatService")
public class WeChatService extends BaseService<WeChat,Long>{
    @Autowired
    private EncryptAndVerifiy encryptAndVerifiy;
    @Autowired
    private WeChatDao weChatDao;
    @Autowired
    public void setBaseDao(WeChatDao weChatDao) {
        super.setBaseDao(weChatDao);
    }

    /**
     * 链接认证
     * @param signature
     * @return
     */
    public String access(String signature,String timestamp,String nonce,String echostr) {
        boolean b = false;
        try {
            b = encryptAndVerifiy.isWeiXin(signature, timestamp, nonce,true);
        } catch (DigestException e) {
            e.printStackTrace();
        }
        if (b) {
            return echostr;
        }
        return "验证失败";

    }

    /**
     * 处理请求消息
     * @param in
     * @return
     * @throws IOException
     */
    public String acceptMessage(ServletInputStream in) throws IOException {
        // 将POST流转换为XStream对象
        XStream xs = MessageUtil.createXstream();
        xs.processAnnotations(InputMessage.class);
        xs.alias("xml", InputMessage.class);
        StringBuilder xmlMsg = new StringBuilder();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            xmlMsg.append(new String(b, 0, n, "UTF-8"));
        }
        // 将xml内容转换为InputMessage对象
        InputMessage inputMsg = (InputMessage) xs.fromXML(xmlMsg.toString());

        String toUserName = inputMsg.getToUserName();// 服务端
        String fromUserName = inputMsg.getFromUserName();// 客户端
        long createTime = inputMsg.getCreateTime();// 接收时间
        Long returnTime = Calendar.getInstance().getTimeInMillis() / 1000;// 返回时间

        // 取得消息类型
        String msgType = inputMsg.getMsgType();
//        System.out.println(inputMsg.getPicUrl());
        Class<?> clazz;
        // 文本消息
        if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_TEXT)) {
//            // 回复文本消息
//            TextMessage textMessage = new TextMessage();
//            textMessage.setToUserName(fromUserName);
//            textMessage.setFromUserName(toUserName);
//            textMessage.setCreateTime(returnTime);
//            textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
//            String respContent = "您发送的是文本消息！";
//            clazz = TextMessage.class;
//            // 设置文本消息的内容
//            textMessage.setContent(respContent);
//            return outputXML(clazz,textMessage);
        }
        // 图片消息
        else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_IMAGE)) {
//            ImageMessage imageMessage = new ImageMessage();
//            imageMessage.setToUserName(fromUserName);
//            imageMessage.setFromUserName(toUserName);
//            imageMessage.setCreateTime(returnTime);
//            imageMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_IMAGE);
//            Image image = new Image();
//            image.setMediaId(inputMsg.getMediaId());
//            imageMessage.setImage(image);
//            clazz = ImageMessage.class;
//            // 设置文本消息的内容
//            return outputXML(clazz,imageMessage);
//            respContent = "您发送的是图片消息！";
        }
        // 语音消息
        else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VOICE)) {
//            VoiceMessage voiceMessage = new VoiceMessage();
//            voiceMessage.setToUserName(fromUserName);
//            voiceMessage.setFromUserName(toUserName);
//            voiceMessage.setCreateTime(returnTime);
//            voiceMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_VOICE);
//            Voice voice = new Voice();
//            voice.setMediaId(inputMsg.getMediaId());
//            voiceMessage.setVoice(voice);
//            clazz = VoiceMessage.class;
//            // 设置文本消息的内容
//            return outputXML(clazz,voiceMessage);
//            respContent = "您发送的是语音消息！";
        }
        // 视频消息
//        else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_VIDEO)) {
//            VideoMessage videoMessage = new VideoMessage();
//            videoMessage.setToUserName(fromUserName);
//            videoMessage.setFromUserName(toUserName);
//            videoMessage.setCreateTime(returnTime);
//            videoMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_VIDEO);
//            Video video = new Video();
//            video.setMediaId(inputMsg.getMediaId());
//            video.setThumbMediaId(inputMsg.getThumbMediaId());
//            videoMessage.setVideo(video);
//            clazz = VideoMessage.class;
            // 设置文本消息的内容
//            return outputXML(clazz,videoMessage);
//            respContent = "您发送的是视频消息！";
//        }
        // 视频消息
//        else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_SHORTVIDEO)) {
//            VideoMessage videoMessage = new VideoMessage();
//            videoMessage.setToUserName(fromUserName);
//            videoMessage.setFromUserName(toUserName);
//            videoMessage.setCreateTime(returnTime);
//            videoMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_SHORTVIDEO);
//            Video video = new Video();
//            video.setMediaId(inputMsg.getMediaId());
//            video.setThumbMediaId(inputMsg.getThumbMediaId());
//            videoMessage.setVideo(video);
//            clazz = VideoMessage.class;
//             设置文本消息的内容
//            return outputXML(clazz,videoMessage);
//            respContent = "您发送的是小视频消息！";
//        }
        // 地理位置消息
        else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LOCATION)) {
//            respContent = "您发送的是地理位置消息！";
        }
        // 链接消息
        else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_LINK)) {
//            respContent = "您发送的是链接消息！";
        }
        // 事件推送
        else if (msgType.equals(MessageUtil.REQ_MESSAGE_TYPE_EVENT)) {
            // 事件类型
            String eventType = inputMsg.getEvent();
            String eventKey = inputMsg.getEventKey();
            // 关注
            if (eventType.equals(MessageUtil.EVENT_TYPE_SUBSCRIBE)) {
                TextMessage textMessage = new TextMessage();
                textMessage.setToUserName(fromUserName);
                textMessage.setFromUserName(toUserName);
                textMessage.setCreateTime(returnTime);
                textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
                textMessage.setContent("感谢您的关注，我们致力于打造最优秀的水利协同设计平台，让我们一起轻松构建和谐水利！鸿源理念：\n" +
                        "以技术革命推动水利的市场化、工业化；\n" +
                        "做比等好，快比慢好；\n" +
                        "水利的民主随着互联网的推动是行的通的；\n" +
                        "学习是一种生活方式，信息的获取并不只限于工作；\n" +
                        "努力让人渐渐成熟、变的坚信，不昧良心照样可以创造财富；\n" +
                        "惟有不断学习才能让你快乐成长；\n" +
                        "不穿西装，照样可以认真工作；\n" +
                        "没有最好，只有更好！");
                clazz = TextMessage.class;
                return outputXML(clazz,textMessage);
            }
            // 取消关注
            else if (eventType.equals(MessageUtil.EVENT_TYPE_UNSUBSCRIBE)) {
                WeChat weChat=findByOpenId(fromUserName);
                if (weChat != null) {
                    remove(weChat);
                }
            }
            // 扫描带参数二维码
            else if (eventType.equals(MessageUtil.EVENT_TYPE_SCAN)) {
                // TODO 处理扫描带参数二维码事件
            }
            // 上报地理位置
            else if (eventType.equals(MessageUtil.EVENT_TYPE_LOCATION)) {
                // TODO 处理上报地理位置事件
            }
            // 自定义菜单
            else if (eventType.equals(MessageUtil.EVENT_TYPE_CLICK)) {
                // TODO 处理菜单点击事件
                if (eventKey.equals("lianxiwomen")) {
                    NewsMessage newsMessage = new NewsMessage();
                    newsMessage.setToUserName(fromUserName);
                    newsMessage.setFromUserName(toUserName);
                    newsMessage.setCreateTime(returnTime);
                    newsMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_NEWS);
                    newsMessage.setArticleCount(1);
                    Article article = new Article();
                    article.setTitle("轻松构建和谐水利");
                    article.setDescription("青清西宁小屋：西宁经济技术开发区金汇路10#时代大厦二单元31、32层青清西安小屋：西安经济技术开发区文景路");
                    article.setPicUrl("http://mmbiz.qpic.cn/mmbiz/TI3wUZic5T1HFV0NIj8oUPlEH3kT0lZxhdFJkTYo5LOIBy3F33PnribRecicod1Vho5icibnhPV8dAOTN9XMUaiaTqKw/0?wx_fmt=jpeg");
                    article.setUrl("http://mp.weixin.qq.com/s/EnLwicM2Md_djkCqE9c_1g");
                    List<Article> articles = new ArrayList<>();
                    articles.add(article);
                    newsMessage.setArticles(articles);
                    clazz = NewsMessage.class;
                    return outputXML(clazz,newsMessage);
                }
//                if (eventKey.equals("jiance")) {
//                    TextMessage textMessage = new TextMessage();
//                    textMessage.setToUserName(fromUserName);
//                    textMessage.setFromUserName(toUserName);
//                    textMessage.setCreateTime(returnTime);
//                    textMessage.setMsgType(MessageUtil.RESP_MESSAGE_TYPE_TEXT);
//                    WeChat byOpenId = findByOpenId(fromUserName);
//                    if (byOpenId.size() == 1) {
//                        查询绑定账户,并完成自动登录
//                        textMessage.setContent("");
//                    }else{
//                        String nickname = getUserBaseMessage.getNickname(fromUserName);
//                        textMessage.setContent("您还么有绑定青清水利账户哦!/::)绑定后即可:\n" +
//                                "*快速查看仪器仪表监控\n" +
//                                "<a href=\"http://www.baidu.com?fromUserName="+fromUserName+"&nickname="+nickname+"\">点击这里,立即绑定</a>\n");
//                        textMessage.setContent("<a href=\"http://218.244.134.139/hot-update/ios/www?fromUserName="+fromUserName+"&nickname="+nickname+"\">点击这里,立即查看相关内容</a>");
//                    textMessage.setContent(getUserBaseMessage.getUserBaseMessage(fromUserName).toString());
//                    }
//                    clazz = TextMessage.class;
//                    return outputXML(clazz,textMessage);
//                }
            }
        }

        // 设置文本消息的内容
//        textMessage.setContent(respContent);
//        return outputXML(clazz,textMessage);
        return "";
    }

    private String outputXML(Class<?> clazz, BaseMessage message) {
        // 将文本消息对象转换成xml
        XStream xs1 = MessageUtil.createXstream();
        xs1.processAnnotations(clazz);
        // 将指定节点下的xml节点数据映射为对象
        xs1.alias("xml", clazz);
        xs1.alias("item", Article.class);
//        System.out.println(xs1.toXML(message));
        return xs1.toXML(message);
    }

    public WeChat findByOpenId(String openId){
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("openId", openId));
        List<WeChat> list = weChatDao.findList(0, null, filters);
        if (list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    public WeChat findByUserId(Long userId) {
        List<Filter> filters = new ArrayList<>();
        filters.add(Filter.eq("userId", userId));
        List<WeChat> list = weChatDao.findList(0, null, filters);
        if (list.size() == 1) {
            return list.get(0);
        }
        return null;
    }
}
