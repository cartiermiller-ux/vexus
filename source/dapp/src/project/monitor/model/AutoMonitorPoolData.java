package project.monitor.model;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 转换记录
 */
@Table(name="T_AUTO_MONITOR_POOL_DATA")
public class AutoMonitorPoolData extends EntityObject {
	
	private static final long serialVersionUID = 5914244062518608589L;

	/**
	 * 总产量
	 */
	@Column(name="TOTAL_OUTPUT")
	private double total_output;
	
	/**
	 * 参与者
	 */
	@Column(name="VERIFIER")
	private double verifier;
	
	/**
	 * 用户收益
	 */
	@Column(name="USER_REVENUE")
	private double user_revenue;
	
	/**
	 * 比率倍数
	 */
	@Column(name="RATE")
	private double rate;
	
	/**
	 * 前端展示轮播数据
	 */
	@Column(name="NOTICE_LOGS")
	private String notice_logs;
	
	/**
	 * 日收益率下限
	 */
	@Column(name="DAY_RATE_MIN")
	private double dayRateMin;
	
	/**
	 * 日收益率上限
	 */
	@Column(name="DAY_RATE_MAX")
	private double dayRateMax;
	
	/**
	 * 节点数量
	 */
	@Column(name="NODE_NUM")
	private double node_num;
	
	/**
	 * 总流动性挖矿合约资金
	 */
	@Column(name="MINING_TOTAL")
	private double mining_total;
	
	/**
	 * 挖矿金额下限
	 */
	@Column(name="MINING_AMOUNT_MIN")
	private double miningAmountMin;
	
	/**
	 * 挖矿金额上限
	 */
	@Column(name="MINING_AMOUNT_MAX")
	private double miningAmountMax;
	
	/**
	 * 总交易量
	 */
	@Column(name="TRADING_SUM")
	private double tradingSum;
	
	/**
	 * 挖矿项目名称
	 */
	@Column(name="MINING_NAME")
	private String miningName;

	public String getMiningName() {
		return miningName;
	}

	public void setMiningName(String miningName) {
		this.miningName = miningName;
	}

	public double getTradingSum() {
		return tradingSum;
	}

	public void setTradingSum(double tradingSum) {
		this.tradingSum = tradingSum;
	}

	public double getDayRateMin() {
		return dayRateMin;
	}

	public void setDayRateMin(double dayRateMin) {
		this.dayRateMin = dayRateMin;
	}

	public double getDayRateMax() {
		return dayRateMax;
	}

	public void setDayRateMax(double dayRateMax) {
		this.dayRateMax = dayRateMax;
	}

	public double getMiningAmountMin() {
		return miningAmountMin;
	}
	
	public double getNode_num() {
		return node_num;
	}
	
	public double getMining_total() {
		return mining_total;
	}

	public void setMiningAmountMin(double miningAmountMin) {
		this.miningAmountMin = miningAmountMin;
	}

	public double getMiningAmountMax() {
		return miningAmountMax;
	}

	public void setMiningAmountMax(double miningAmountMax) {
		this.miningAmountMax = miningAmountMax;
	}

	public double getTotal_output() {
		return total_output;
	}

	public double getVerifier() {
		return verifier;
	}

	public double getUser_revenue() {
		return user_revenue;
	}

	public double getRate() {
		return rate;
	}

	public void setTotal_output(double total_output) {
		this.total_output = total_output;
	}

	public void setVerifier(double verifier) {
		this.verifier = verifier;
	}

	public void setUser_revenue(double user_revenue) {
		this.user_revenue = user_revenue;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public String getNotice_logs() {
		return notice_logs;
	}

	public void setNotice_logs(String notice_logs) {
		this.notice_logs = notice_logs;
	}
	
	public void setNode_num(double node_num) {
		this.node_num = node_num;
	}

	public void setMining_total(double mining_total) {
		this.mining_total = mining_total;
	}
	


}
