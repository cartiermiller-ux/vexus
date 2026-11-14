package project.log;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 系统日志
 */
@Table(name="T_SYSLOG")
public class SysLog extends EntityObject implements AbstractLog {

	private static final long serialVersionUID = -2169262629412176665L;

	public static String level_error = "error";
	public static String level_warn = "warn";
	public static String level_info = "info";

	// 日志分类
	@Column(name="CATEGORY")
	private String category;

	@Column(name="LOG")
	private String log;

	/**
	 * 特定渠道发起交易时需要的额外参数，以及部分渠道支付成功返回的额外参数。 存储json格式
	 */
	@Column(name="EXTRA")
	private String extra;

	/**
	 * 日志级别 错误 error 信息 info 警告 warn
	 */
	@Column(name="LEVEL")
	private String level;

	// 创建时间
	@Column(name="CREATE_TIME")
	private Date createTime;

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

}
