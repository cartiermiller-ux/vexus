package project.cms;

import java.util.Comparator;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 用户端内容管理
 */
@Table(name="T_CMS")
public class Cms extends EntityObject implements Comparator<Cms>  {
	private static final long serialVersionUID = -5246000727456595774L;
	/**
	 * 标题
	 */
	@Column(name="TITLE")
	private String title;
	/**
	 * 内容
	 */
	@Column(name="CONTENT")
	private String content;
	
	@Column(name="MODEL")
	private String model;
	
	@Column(name="CREATE_TIME")
	private Date createTime;
	private String createTimeStr;
	
	@Column(name="LANGUAGE")
	private String language;
	
	/**
	 * 业务代码， 同种内容 不同语言下的code相同
	 */
	@Column(name="CONTENT_CODE")
	private String content_code;

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getCreateTimeStr() {
		return createTimeStr;
	}

	public void setCreateTimeStr(String createTimeStr) {
		this.createTimeStr = createTimeStr;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getContent_code() {
		return content_code;
	}

	public void setContent_code(String content_code) {
		this.content_code = content_code;
	}

	@Override
	public int compare(Cms paramT1, Cms paramT2) {
		return -paramT1.getCreateTime().compareTo(paramT2.getCreateTime());
	}

}
