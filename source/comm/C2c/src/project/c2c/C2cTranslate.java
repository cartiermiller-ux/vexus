package project.c2c;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_C2C_TRANSLATE")
public class C2cTranslate extends EntityObject {

	private static final long serialVersionUID = -3956153479501477647L;

	/**
	 * 语言
	 */
	@Column(name="LANGUAGE")
	private String language;

	/**
	 * 内容
	 */
	@Column(name="CONTENT")
	private String content;

	/**
	 * 翻译
	 */
	@Column(name="TRANSLATE")
	private String translate;
	
	/**
	 * 创建时间
	 */
	@Column(name="CREATE_TIME")
	private Date createTime;

	/**
	 * 更新时间
	 */
	@Column(name="UPDATE_TIME")
	private Date updateTime;

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTranslate() {
		return translate;
	}

	public void setTranslate(String translate) {
		this.translate = translate;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

}
