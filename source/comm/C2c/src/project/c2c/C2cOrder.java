package project.c2c;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_C2C_ORDER")
public class C2cOrder extends EntityObject {

	private static final long serialVersionUID = -3219486331695837115L;

	/**
	 * 订单类型：按支付金额
	 */
	public final static String ORDER_TYPE_BY_AMOUNT = "by_amount";
	
	/**
	 * 订单类型：按币种数量
	 */
	public final static String ORDER_TYPE_BY_NUM = "by_num";

	/**
	 * 买入
	 */
	public final static String DIRECTION_BUY = "buy";
	
	/**
	 * 卖出
	 */
	public final static String DIRECTION_SELL = "sell";
	
	/**
	 * 按金额支付
	 */
	public final static String PAY_METHOD_CURRENCY = "currency";
	
	/**
	 * 按币种数量支付
	 */
	public final static String PAY_METHOD_COIN = "coin";
	
	/**
	 * 用户PARTY_ID
	 */
	@Column(name="PARTY_ID")
	private String partyId;

	/**
	 * 承兑商ID
	 */
	@Column(name="C2C_USER_ID")
	private String c2cUserId;

	/**
	 * 广告ID
	 */
	@Column(name="C2C_ADVERT_ID")
	private String c2cAdvertId;

	/**
	 * 支付方式ID：购买为承兑商收款方式ID，出售为用户收款方式ID
	 */
	@Column(name="PAYMENT_METHOD_ID")
	private String paymentMethodId;

	/**
	 * 订单类型：by_amount按支付金额/by_num按币种数量
	 */
	@Column(name="ORDER_TYPE")
	private String orderType;

	/**
	 * 订单号
	 */
	@Column(name="ORDER_NO")
	private String orderNo;

	/**
	 * 订单状态：0未付款/1已付款/2申诉中/3已完成/4已取消/5已超时
	 */
	@Column(name="STATE")
	private String state;
	
	/**
	 * 承兑商类型：1后台承兑商/2用户承兑商
	 */
	@Column(name="C2C_USER_TYPE")
	private int c2cUserType;

	/**
	 * 承兑商CODE
	 */
	@Column(name="C2C_USER_CODE")
	private String c2cUserCode;

	/**
	 * 承兑商昵称
	 */
	@Column(name="C2C_USER_NICK_NAME")
	private String c2cUserNickName;

	/**
	 * 承兑商头像
	 */
	@Column(name="C2C_USER_HEAD_IMG")
	private String c2cUserHeadImg;

	/**
	 * 承兑商用户PARTY_ID
	 */
	@Column(name="C2C_USER_PARTY_ID")
	private String c2cUserPartyId;

	/**
	 * 承兑商用户CODE
	 */
	@Column(name="C2C_USER_PARTY_CODE")
	private String c2cUserPartyCode;

	/**
	 * 承兑商用户名
	 */
	@Column(name="C2C_USER_PARTY_NAME")
	private String c2cUserPartyName;

	/**
	 * 买卖方式：buy买/sell卖
	 */
	@Column(name="DIRECTION")
	private String direction;
	
	/**
	 * 支付币种
	 */
	@Column(name="CURRENCY")
	private String currency;
	
	/**
	 * 上架币种
	 */
	@Column(name="SYMBOL")
	private String symbol;
	
	/**
	 * 支付比率
	 */
	@Column(name="PAY_RATE")
	private double payRate;

	/**
	 * 币种单价
	 */
	@Column(name="SYMBOL_VALUE")
	private double symbolValue;
	
	/**
	 * 币种数量
	 */
	@Column(name="COIN_AMOUNT")
	private double coinAmount;
	
	/**
	 * 支付时效（单位：分钟）
	 */
	@Column(name="EXPIRE_TIME")
	private int expireTime;
	
	/**
	 * 确认收款超时时间秒（单位：秒）
	 */
	private int expireTimeRemain;
	
	/**
	 * 超时自动取消时间秒（单位：秒）
	 */
	private int autoCancelTimeRemain;

	/**
	 * 支付金额
	 */
	@Column(name="AMOUNT")
	private double amount;

	/**
	 * 换算成USDT金额
	 */
	@Column(name="AMOUNT_USDT")
	private double amountUsdt;
	
	/**
	 * 支付方式类型：0其它/1银行卡/2虚拟货币/3微信/4支付宝/5PayPal/6西联汇款/7SWIFT国际汇款
	 */
	@Column(name="METHOD_TYPE")
	private int methodType;
	
	/**
	 * 支付方式类型名称
	 */
	private String methodTypeName;
	
	/**
	 * 支付方式名称
	 */
	@Column(name="METHOD_NAME")
	private String methodName;
	
	/**
	 * 支付方式图片
	 */
	@Column(name="METHOD_IMG")
	private String methodImg;
	
	/**
	 * 真实姓名
	 */
	@Column(name="REAL_NAME")
	private String realName;
	
	/**
	 * 参数名1
	 */
	@Column(name="PARAM_NAME1")
	private String paramName1;
	
	/**
	 * 参数值1
	 */
	@Column(name="PARAM_VALUE1")
	private String paramValue1;
	
	/**
	 * 参数名2
	 */
	@Column(name="PARAM_NAME2")
	private String paramName2;
	
	/**
	 * 参数值2
	 */
	@Column(name="PARAM_VALUE2")
	private String paramValue2;
	
	/**
	 * 参数名3
	 */
	@Column(name="PARAM_NAME3")
	private String paramName3;
	
	/**
	 * 参数值3
	 */
	@Column(name="PARAM_VALUE3")
	private String paramValue3;
	
	/**
	 * 参数名4
	 */
	@Column(name="PARAM_NAME4")
	private String paramName4;
	
	/**
	 * 参数值4
	 */
	@Column(name="PARAM_VALUE4")
	private String paramValue4;
	
	/**
	 * 参数名5
	 */
	@Column(name="PARAM_NAME5")
	private String paramName5;
	
	/**
	 * 参数值5
	 */
	@Column(name="PARAM_VALUE5")
	private String paramValue5;
	
	/**
	 * 参数名6
	 */
	@Column(name="PARAM_NAME6")
	private String paramName6;
	
	/**
	 * 参数值6
	 */
	@Column(name="PARAM_VALUE6")
	private String paramValue6;
	
	/**
	 * 参数名7
	 */
	@Column(name="PARAM_NAME7")
	private String paramName7;
	
	/**
	 * 参数值7
	 */
	@Column(name="PARAM_VALUE7")
	private String paramValue7;
	
	/**
	 * 参数名8
	 */
	@Column(name="PARAM_NAME8")
	private String paramName8;
	
	/**
	 * 参数值8
	 */
	@Column(name="PARAM_VALUE8")
	private String paramValue8;
	
	/**
	 * 参数名9
	 */
	@Column(name="PARAM_NAME9")
	private String paramName9;
	
	/**
	 * 参数值9
	 */
	@Column(name="PARAM_VALUE9")
	private String paramValue9;
	
	/**
	 * 参数名10
	 */
	@Column(name="PARAM_NAME10")
	private String paramName10;
	
	/**
	 * 参数值10
	 */
	@Column(name="PARAM_VALUE10")
	private String paramValue10;
	
	/**
	 * 参数名11
	 */
	@Column(name="PARAM_NAME11")
	private String paramName11;
	
	/**
	 * 参数值11
	 */
	@Column(name="PARAM_VALUE11")
	private String paramValue11;
	
	/**
	 * 参数名12
	 */
	@Column(name="PARAM_NAME12")
	private String paramName12;
	
	/**
	 * 参数值12
	 */
	@Column(name="PARAM_VALUE12")
	private String paramValue12;
	
	/**
	 * 参数名13
	 */
	@Column(name="PARAM_NAME13")
	private String paramName13;
	
	/**
	 * 参数值13
	 */
	@Column(name="PARAM_VALUE13")
	private String paramValue13;
	
	/**
	 * 参数名14
	 */
	@Column(name="PARAM_NAME14")
	private String paramName14;
	
	/**
	 * 参数值14
	 */
	@Column(name="PARAM_VALUE14")
	private String paramValue14;
	
	/**
	 * 参数名15
	 */
	@Column(name="PARAM_NAME15")
	private String paramName15;
	
	/**
	 * 参数值15
	 */
	@Column(name="PARAM_VALUE15")
	private String paramValue15;
	
	/**
	 * 支付二维码
	 */
	@Column(name="QRCODE")
	private String qrcode;
	
	/**
	 * 备注
	 */
	@Column(name="REMARK")
	private String remark;
	
	/**
	 * 创建时间
	 */
	@Column(name="CREATE_TIME")
	private Date createTime;
	
	/**
	 * 处理时间
	 */
	@Column(name="HANDLE_TIME")
	private Date handleTime;
	
	/**
	 * 关闭时间
	 */
	@Column(name="CLOSE_TIME")
	private Date closeTime;
	
	/**
	 * 支付时间
	 */
	@Column(name="PAY_TIME")
	private Date payTime;
	
	/**
	 * 取消时间
	 */
	@Column(name="CANCEL_TIME")
	private Date cancelTime;
	
	/**
	 * 未读消息数量
	 */
	private int unreadMsg;

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public String getC2cUserId() {
		return c2cUserId;
	}

	public void setC2cUserId(String c2cUserId) {
		this.c2cUserId = c2cUserId;
	}

	public String getC2cAdvertId() {
		return c2cAdvertId;
	}

	public void setC2cAdvertId(String c2cAdvertId) {
		this.c2cAdvertId = c2cAdvertId;
	}

	public String getPaymentMethodId() {
		return paymentMethodId;
	}

	public void setPaymentMethodId(String paymentMethodId) {
		this.paymentMethodId = paymentMethodId;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public int getC2cUserType() {
		return c2cUserType;
	}

	public void setC2cUserType(int c2cUserType) {
		this.c2cUserType = c2cUserType;
	}

	public String getC2cUserCode() {
		return c2cUserCode;
	}

	public void setC2cUserCode(String c2cUserCode) {
		this.c2cUserCode = c2cUserCode;
	}

	public String getC2cUserNickName() {
		return c2cUserNickName;
	}

	public void setC2cUserNickName(String c2cUserNickName) {
		this.c2cUserNickName = c2cUserNickName;
	}

	public String getC2cUserHeadImg() {
		return c2cUserHeadImg;
	}

	public void setC2cUserHeadImg(String c2cUserHeadImg) {
		this.c2cUserHeadImg = c2cUserHeadImg;
	}

	public String getC2cUserPartyId() {
		return c2cUserPartyId;
	}

	public void setC2cUserPartyId(String c2cUserPartyId) {
		this.c2cUserPartyId = c2cUserPartyId;
	}

	public String getC2cUserPartyCode() {
		return c2cUserPartyCode;
	}

	public void setC2cUserPartyCode(String c2cUserPartyCode) {
		this.c2cUserPartyCode = c2cUserPartyCode;
	}

	public String getC2cUserPartyName() {
		return c2cUserPartyName;
	}

	public void setC2cUserPartyName(String c2cUserPartyName) {
		this.c2cUserPartyName = c2cUserPartyName;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getPayRate() {
		return payRate;
	}

	public void setPayRate(double payRate) {
		this.payRate = payRate;
	}

	public double getSymbolValue() {
		return symbolValue;
	}

	public void setSymbolValue(double symbolValue) {
		this.symbolValue = symbolValue;
	}

	public double getCoinAmount() {
		return coinAmount;
	}

	public void setCoinAmount(double coinAmount) {
		this.coinAmount = coinAmount;
	}

	public int getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(int expireTime) {
		this.expireTime = expireTime;
	}

	public int getExpireTimeRemain() {
		return expireTimeRemain;
	}

	public void setExpireTimeRemain(int expireTimeRemain) {
		this.expireTimeRemain = expireTimeRemain;
	}

	public int getAutoCancelTimeRemain() {
		return autoCancelTimeRemain;
	}

	public void setAutoCancelTimeRemain(int autoCancelTimeRemain) {
		this.autoCancelTimeRemain = autoCancelTimeRemain;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getAmountUsdt() {
		return amountUsdt;
	}

	public void setAmountUsdt(double amountUsdt) {
		this.amountUsdt = amountUsdt;
	}

	public int getMethodType() {
		return methodType;
	}

	public void setMethodType(int methodType) {
		this.methodType = methodType;
	}

	public String getMethodTypeName() {
		return methodTypeName;
	}

	public void setMethodTypeName(String methodTypeName) {
		this.methodTypeName = methodTypeName;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getMethodImg() {
		return methodImg;
	}

	public void setMethodImg(String methodImg) {
		this.methodImg = methodImg;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getParamName1() {
		return paramName1;
	}

	public void setParamName1(String paramName1) {
		this.paramName1 = paramName1;
	}

	public String getParamValue1() {
		return paramValue1;
	}

	public void setParamValue1(String paramValue1) {
		this.paramValue1 = paramValue1;
	}

	public String getParamName2() {
		return paramName2;
	}

	public void setParamName2(String paramName2) {
		this.paramName2 = paramName2;
	}

	public String getParamValue2() {
		return paramValue2;
	}

	public void setParamValue2(String paramValue2) {
		this.paramValue2 = paramValue2;
	}

	public String getParamName3() {
		return paramName3;
	}

	public void setParamName3(String paramName3) {
		this.paramName3 = paramName3;
	}

	public String getParamValue3() {
		return paramValue3;
	}

	public void setParamValue3(String paramValue3) {
		this.paramValue3 = paramValue3;
	}

	public String getParamName4() {
		return paramName4;
	}

	public void setParamName4(String paramName4) {
		this.paramName4 = paramName4;
	}

	public String getParamValue4() {
		return paramValue4;
	}

	public void setParamValue4(String paramValue4) {
		this.paramValue4 = paramValue4;
	}

	public String getParamName5() {
		return paramName5;
	}

	public void setParamName5(String paramName5) {
		this.paramName5 = paramName5;
	}

	public String getParamValue5() {
		return paramValue5;
	}

	public void setParamValue5(String paramValue5) {
		this.paramValue5 = paramValue5;
	}

	public String getParamName6() {
		return paramName6;
	}

	public void setParamName6(String paramName6) {
		this.paramName6 = paramName6;
	}

	public String getParamValue6() {
		return paramValue6;
	}

	public void setParamValue6(String paramValue6) {
		this.paramValue6 = paramValue6;
	}

	public String getParamName7() {
		return paramName7;
	}

	public void setParamName7(String paramName7) {
		this.paramName7 = paramName7;
	}

	public String getParamValue7() {
		return paramValue7;
	}

	public void setParamValue7(String paramValue7) {
		this.paramValue7 = paramValue7;
	}

	public String getParamName8() {
		return paramName8;
	}

	public void setParamName8(String paramName8) {
		this.paramName8 = paramName8;
	}

	public String getParamValue8() {
		return paramValue8;
	}

	public void setParamValue8(String paramValue8) {
		this.paramValue8 = paramValue8;
	}

	public String getParamName9() {
		return paramName9;
	}

	public void setParamName9(String paramName9) {
		this.paramName9 = paramName9;
	}

	public String getParamValue9() {
		return paramValue9;
	}

	public void setParamValue9(String paramValue9) {
		this.paramValue9 = paramValue9;
	}

	public String getParamName10() {
		return paramName10;
	}

	public void setParamName10(String paramName10) {
		this.paramName10 = paramName10;
	}

	public String getParamValue10() {
		return paramValue10;
	}

	public void setParamValue10(String paramValue10) {
		this.paramValue10 = paramValue10;
	}

	public String getParamName11() {
		return paramName11;
	}

	public void setParamName11(String paramName11) {
		this.paramName11 = paramName11;
	}

	public String getParamValue11() {
		return paramValue11;
	}

	public void setParamValue11(String paramValue11) {
		this.paramValue11 = paramValue11;
	}

	public String getParamName12() {
		return paramName12;
	}

	public void setParamName12(String paramName12) {
		this.paramName12 = paramName12;
	}

	public String getParamValue12() {
		return paramValue12;
	}

	public void setParamValue12(String paramValue12) {
		this.paramValue12 = paramValue12;
	}

	public String getParamName13() {
		return paramName13;
	}

	public void setParamName13(String paramName13) {
		this.paramName13 = paramName13;
	}

	public String getParamValue13() {
		return paramValue13;
	}

	public void setParamValue13(String paramValue13) {
		this.paramValue13 = paramValue13;
	}

	public String getParamName14() {
		return paramName14;
	}

	public void setParamName14(String paramName14) {
		this.paramName14 = paramName14;
	}

	public String getParamValue14() {
		return paramValue14;
	}

	public void setParamValue14(String paramValue14) {
		this.paramValue14 = paramValue14;
	}

	public String getParamName15() {
		return paramName15;
	}

	public void setParamName15(String paramName15) {
		this.paramName15 = paramName15;
	}

	public String getParamValue15() {
		return paramValue15;
	}

	public void setParamValue15(String paramValue15) {
		this.paramValue15 = paramValue15;
	}

	public String getQrcode() {
		return qrcode;
	}

	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getHandleTime() {
		return handleTime;
	}

	public void setHandleTime(Date handleTime) {
		this.handleTime = handleTime;
	}

	public Date getCloseTime() {
		return closeTime;
	}

	public void setCloseTime(Date closeTime) {
		this.closeTime = closeTime;
	}

	public Date getPayTime() {
		return payTime;
	}

	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}

	public Date getCancelTime() {
		return cancelTime;
	}

	public void setCancelTime(Date cancelTime) {
		this.cancelTime = cancelTime;
	}

	public int getUnreadMsg() {
		return unreadMsg;
	}

	public void setUnreadMsg(int unreadMsg) {
		this.unreadMsg = unreadMsg;
	}

}
