package com.hysw.qqsl.cloud.entity.data;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.persistence.*;



/**
 * 文章实体类，用于信息发布
 * @author leinuo  
 *
 * @date  2016年1月12日
 */
@Entity
@Table(name = "article")
@SequenceGenerator(name="sequenceGenerator", sequenceName="article_sequence")
@JsonIgnoreProperties(value={"hibernateLazyInitializer"})  
public class Article extends BaseEntity{

	private static final long serialVersionUID = 2047368680026095150L;
	/**文章标题*/
	private String title;
	/**文章内容*/
	private String content;

	/**文章类型*/
	private Type type;
	public enum Type{
		/**水利云平台新闻*/
		WATERCLOUDNEW,
		/**水利政策*/
		WATERPOLICY,
		/**水利新闻*/
		WATERNEW;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	//@Lob
	@Basic(fetch = FetchType.EAGER)
	@Column(columnDefinition = "text")
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	
	
	
	
	
}
