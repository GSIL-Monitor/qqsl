package com.hysw.qqsl.cloud.core.service;

import com.hysw.qqsl.cloud.CommonAttributes;
import com.hysw.qqsl.cloud.core.entity.Message;
import com.hysw.qqsl.cloud.core.dao.ArticleDao;
import com.hysw.qqsl.cloud.core.entity.Filter;
import com.hysw.qqsl.cloud.core.entity.data.Article;
import com.hysw.qqsl.cloud.core.entity.data.Article.Type;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 信息发布业务层
 * @author leinuo  
 *
 * @date  2016年2月23日
 */
@Service("articleService")
public class ArticleService extends BaseService<Article, Long> {
	@Autowired
	private ArticleDao articleDao;

	private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	@Autowired
	public void setBaseDao(ArticleDao articleDao) {
		super.setBaseDao(articleDao);
	}

	/**
	 * 根据idStr是否有值发布或更新文章
	 * @param idStr
	 * @param title
	 * @param content
	 * @param index
	 * @param
	 * @return
	 */
	public Message save(String idStr, String title, String content, int index) {
		Article article;
		if(!StringUtils.hasText(idStr)){
		    article = new Article();
			article.setContent(content);
			article.setTitle(title);
			save(article,index);
			return MessageService.message(Message.Type.OK);
		}else{
			long id = Long.valueOf(idStr);
			article = findById(id);
			if(article == null){
				return MessageService.message(Message.Type.DATA_NOEXIST);
			}
			article.setContent(content);
			article.setTitle(title);
			save(article,index);
			return MessageService.message(Message.Type.OK);
		}		
		
	}
	/**
	 * 根据标作者找文章
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unused")
	private List<Article> findByUser(Long userId) {
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(Filter.eq("user", userId));
		List<Article> articles = articleDao.findList(0, null, filters);
		if(articles.size()!=1){
			return null;
		}
		return articles;
	}

	/**
	 * 保存信息文章
	 * @param index
	 */
	public void save(Article article, int index) {
		article.setType(getType(index));
		super.save(article);
	} 
	/**
	 * 类型转换
	 * @param index
	 * @return
	 */
	public Type getType(int index){
		Type type;
		if(index == Article.Type.WATERCLOUDNEW.ordinal()){
			type = Article.Type.WATERCLOUDNEW;
        }else if(index == Article.Type.WATERNEW.ordinal()){
        	type = Article.Type.WATERNEW;
        }else{
        	type = Article.Type.WATERPOLICY;
        }
		return type;	
	}

	public Article findById(long id) {
		Article article = find(id);
		return article;
	}

	/**
	 * 删除文章
	 * @param id
	 */
	public void removeById(Long id) {
		Article article = findById(id);
		super.remove(article);
		
	}

	public List<JSONObject> findArticles(){
	   List<Article> articles = findAll();
       List<JSONObject> articleJsons = new ArrayList<>();
		if(articles.size()==0){
			return articleJsons;
		}
		JSONObject articleJson;
		for(Article article:articles){
			articleJson = makeArticleJson(article);
			articleJsons.add(articleJson);
		}
		return articleJsons;
	}

	/**
	 * 获取ArticleJson字符
	 * @param article
	 * @return
     */
	public JSONObject makeArticleJson(Article article){
		    JSONObject articleJson = new JSONObject();
			articleJson.put("content",article.getContent());
			articleJson.put("type",article.getType());
			articleJson.put("title",article.getTitle());
			articleJson.put("id",article.getId());
			articleJson.put("modifyDate",sdf.format(article.getModifyDate()));
			articleJson.put("createDate",sdf.format(article.getCreateDate()));
		return articleJson;
	}
	/**
	 * 替换图片地址
	 * @param content
	 * @return
	 */
	public String replacePath(String content){
		Pattern p = Pattern.compile(CommonAttributes.OSSIMAGE[0]);
		Matcher m = p.matcher(content);
		if(m.find()){
			content = content.replaceAll(CommonAttributes.OSSIMAGE[0],CommonAttributes.OSSIMAGE[2]);
		}
		p = Pattern.compile(CommonAttributes.OSSIMAGE[1]);
		 m = p.matcher(content);
		 if(m.find()){
			content = content.replaceAll(CommonAttributes.OSSIMAGE[1],CommonAttributes.OSSIMAGE[2]);
		 }
		return content;
	}

}

