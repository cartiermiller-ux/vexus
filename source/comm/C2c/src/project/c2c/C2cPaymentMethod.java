package project.c2c;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/*
 * C2C支付方式
 */
@Table(name="T_C2C_PAYMENT_METHOD")
public class C2cPaymentMethod extends EntityObject {

	private static final long serialVersionUID = -1883480204147696409L;

	/**
	 * 用户PARTY_ID
	 */
	@Column(name="PARTY_ID")
	private String partyId;

	/**
	 * 支付方式模板ID
	 */
	@Column(name="METHOD_CONFIG_ID")
	private String methodConfigId;
	
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
	 * 承兑商真实姓名
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
	 * 支付二维码 完整链接
	 */
	private String qrcodePath;
	
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
	 * 更新时间
	 */
	@Column(name="UPDATE_TIME")
	private Date updateTime;

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public String getMethodConfigId() {
		return methodConfigId;
	}

	public void setMethodConfigId(String methodConfigId) {
		this.methodConfigId = methodConfigId;
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

	public String getQrcodePath() {
		return qrcodePath;
	}

	public void setQrcodePath(String qrcodePath) {
		this.qrcodePath = qrcodePath;
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

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

}
