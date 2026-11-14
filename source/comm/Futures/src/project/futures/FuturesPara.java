package project.futures;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_FUTURES_PARA")
public class FuturesPara extends EntityObject {

	private static final long serialVersionUID = 8373415549401215805L;
	public final static String TIMENUM_SECOND = "second";
	public final static String TIMENUM_MINUTE = "minute";
	public final static String TIMENUM_HOUR = "hour";
	public final static String TIMENUM_DAY = "day";

	public enum TIMENUM {
		second(TIMENUM_SECOND, "秒"), minute(TIMENUM_MINUTE, "分"), hour(TIMENUM_HOUR, "时"), day(TIMENUM_DAY, "天");

		private String timenum;
		private String cn;

		TIMENUM(String timenum, String cn) {
			this.timenum = timenum;
			this.cn = cn;
		}

		public String getCn() {
			return this.cn;
		}

		public String getTimenum() {
			return timenum;
		}

	}

	/**
	 * 币种
	 */
	@Column(name="SYMBOL")
	private String symbol;
	
	/**
	 * 时间长度
	 */
	@Column(name="TIMENUM")
	private int timeNum;
	
	/**
	 * 时间单位
	 */
	@Column(name="TIMEUNIT")
	private String timeUnit;

	/**
	 * 金额 ，字段命令错误，这里是最低购买金额
	 */
	@Column(name="UNIT_AMOUNT")
	private double unit_amount;

	/**
	 * 手续费(%)，字段命令错误
	 */
	@Column(name="UNIT_FEE")
	private double unit_fee;

	/**
	 * 浮动最小收益率
	 */
	@Column(name="PROFIT_RATIO")
	private double profit_ratio;
	/**
	 * 浮动最大收益率
	 */
	@Column(name="PROFIT_RATIO_MAX")
	private double profit_ratio_max;

	/**
	 * 时间单位cn
	 */
	private String timeUnitCn;

	/**
	 * 每手最高价格
	 */
	@Column(name="UNIT_MAX_AMOUNT")
	private double unit_max_amount;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public int getTimeNum() {
		return timeNum;
	}

	public void setTimeNum(int timeNum) {
		this.timeNum = timeNum;
	}

	public String getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(String timeUnit) {
		this.timeUnit = timeUnit;
	}

	public double getUnit_amount() {
		return unit_amount;
	}

	public void setUnit_amount(double unit_amount) {
		this.unit_amount = unit_amount;
	}

	public double getProfit_ratio() {
		return profit_ratio;
	}

	public void setProfit_ratio(double profit_ratio) {
		this.profit_ratio = profit_ratio;
	}

	public double getUnit_fee() {
		return unit_fee;
	}

	public void setUnit_fee(double unit_fee) {
		this.unit_fee = unit_fee;
	}

	public String getTimeUnitCn() {
		return timeUnitCn;
	}

	public void setTimeUnitCn(String timeUnitCn) {
		this.timeUnitCn = timeUnitCn;
	}

	public double getProfit_ratio_max() {
		return profit_ratio_max;
	}

	public void setProfit_ratio_max(double profit_ratio_max) {
		this.profit_ratio_max = profit_ratio_max;
	}

	public double getUnit_max_amount() {
		return unit_max_amount;
	}

	public void setUnit_max_amount(double unit_max_amount) {
		this.unit_max_amount = unit_max_amount;
	}

}
