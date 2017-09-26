package com.hysw.qqsl.cloud.entity;

import com.aliyun.oss.model.OSSObjectSummary;

import java.io.Serializable;
import java.util.Date;


/**
 * 文件，传递给表现层
 *
 * @since 2015年7月27日
 * @author 雪庭(flysic) qq: 119238122 github: https://github.com/flysic
 *
 */
public class ObjectFile implements Serializable{

	private static final long serialVersionUID = -5265174311803965440L;
	
	/** 文件名 */
	private String name;
	/** 大小 */
	private String size;
	/** key值 */
	private String key;
	/** 上传时间 */
	private Date updateDate;
	/** 属性 */
	//private String meta;
	/** 下载地址 */
	private String downloadUrl;
	/** 预览地址 */
	private String previewUrl;
	/** 描述*/
	private String description;
	/** 线面类型*/
	private String type;
	
	@SuppressWarnings("unused")
	private ObjectFile() {
	}
	
	public ObjectFile(OSSObjectSummary ossObjectSummary) {
		setKey(ossObjectSummary.getKey());
		String key = ossObjectSummary.getKey(); 
		String name = key.substring(key.lastIndexOf("/")+1);
		setName(name);
		String size = String.valueOf(ossObjectSummary.getSize());
		setSize(size);
		setUpdateDate(ossObjectSummary.getLastModified());
	}
	
	/*public String getMeta() {
		return meta;
	}	*/
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	
	public String getPreviewUrl() {
		return previewUrl;
	}

	public void setPreviewUrl(String previewUrl) {
		this.previewUrl = previewUrl;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	
	
}
