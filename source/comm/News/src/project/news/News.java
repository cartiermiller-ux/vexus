package project.news;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 系统公告
 */
@Table(name="T_NEWS")
public class News extends EntityObject implements Comparator<News> {
	private static final long serialVersionUID = -4670490376092518726L;
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
	
	@Column(name="LANGUAGE")
	private String language;
	
	/**
	 * 首页弹出新闻，如果为true弹出
	 */
	@Column(name="INDEX_TOP")
	private Boolean index = false;
	
	@Column(name="CREATE_TIME")
	private Date createTime;
	private String createTimeStr;
	
	/**
	 * 玩家ID/代理ID/空（空是全局配置）
	 */
	@Column(name="PARTY_ID")
	private Serializable partyId;
	
	/**
	 * 图片地址
	 */
	@Column(name="IMG_URL")
	private String imgUrl;
	
	/**
	 * 图片跳转链接
	 */
	@Column(name="IMG_JUMP_URL")
	private String imgJumpUrl;
	
	/**
	 * 可否点击
	 */
	@Column(name="CLICK")
	private int click;
	
	/**
	 * 是否弹出
	 */
	@Column(name="POP_UP")
	private int popUp;
	
	@Column(name="START_TIME")
	private Date startTime;
	
	@Column(name="END_TIME")
	private Date endTime;

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Serializable getPartyId() {
		return partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getImgJumpUrl() {
		return imgJumpUrl;
	}

	public void setImgJumpUrl(String imgJumpUrl) {
		this.imgJumpUrl = imgJumpUrl;
	}

	public int getClick() {
		return click;
	}

	public void setClick(int click) {
		this.click = click;
	}

	public int getPopUp() {
		return popUp;
	}

	public void setPopUp(int popUp) {
		this.popUp = popUp;
	}

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

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Boolean getIndex() {
		return index;
	}

	public void setIndex(Boolean index) {
		this.index = index;
	}

	public String getCreateTimeStr() {
		return createTimeStr;
	}

	public void setCreateTimeStr(String createTimeStr) {
		this.createTimeStr = createTimeStr;
	}

	@Override
	public int compare(News paramT1, News paramT2) {
		// TODO Auto-generated method stub
		return -paramT1.getCreateTime().compareTo(paramT2.getCreateTime());
	}

}
