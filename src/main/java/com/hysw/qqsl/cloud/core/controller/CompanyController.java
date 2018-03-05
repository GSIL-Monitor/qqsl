package com.hysw.qqsl.cloud.core.controller;

import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.entity.data.Article;
import com.hysw.qqsl.cloud.core.service.ArticleService;
import com.hysw.qqsl.cloud.core.service.MessageService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Create by leinuo on 17-5-26 上午11:23
 *
 * qq:1321404703 https://github.com/leinuo2016
 *
 * 企业公共服务控制层，提供所有企业和子账号的公共资源访问
 */
@Controller
@RequestMapping("/company")
public class CompanyController {

    @Autowired
    private ArticleService articleService;

    /**
     * 查看具体文章
     * @return message消息体包含文章信息,FAIL:参数不正确,OK:查看成功
     */
    @RequestMapping(value = "/queryArticle/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    Message queryArticle(@PathVariable("id") Long id) {
        Message message = MessageService.parametersCheck(id);
        if (message.getType() != Message.Type.OK) {
            return message;
        }
        Article article = articleService.findById(id);
        return MessageService.message(Message.Type.OK, articleService.makeArticleJson(article));
    }
    /**
     * 查询所有的文章
     * @return message消息体包含文章信息,FAIL:参数不正确,OK:查看成功
     */
    @RequestMapping(value = "/queryArticles", method = RequestMethod.GET)
    public
    @ResponseBody
    Message queryArticles() {
        List<JSONObject> articles = articleService.findArticles();
        return MessageService.message(Message.Type.OK, articles);
    }

}
