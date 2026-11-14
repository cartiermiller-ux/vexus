package project.c2c;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/*
 * C2C支付方式模板
 */
@Table(name="T_C2C_PAYMENT_METHOD_CONFIG")
public class C2cPaymentMethodConfig extends EntityObject {

	private static final long serialVersionUID = -5097468546071632477L;
	
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
	 * 参数名1（配关键数据：如微信账号、支付宝账号、银行卡号、虚拟货币地址等等）
	 */
    @Column(name="PARAM_NAME1")
	private String paramName1;
	
	/**
	 * 参数名2
	 */
    @Column(name="PARAM_NAME2")
	private String paramName2;
	
	/**
	 * 参数名3
	 */
    @Column(name="PARAM_NAME3")
	private String paramName3;
	
	/**
	 * 参数名4
	 */
    @Column(name="PARAM_NAME4")
	private String paramName4;
	
	/**
	 * 参数名5
	 */
    @Column(name="PARAM_NAME5")
	private String paramName5;
	
	/**
	 * 参数名6
	 */
    @Column(name="PARAM_NAME6")
	private String paramName6;
	
	/**
	 * 参数名7
	 */
    @Column(name="PARAM_NAME7")
	private String paramName7;
	
	/**
	 * 参数名8
	 */
    @Column(name="PARAM_NAME8")
	private String paramName8;
	
	/**
	 * 参数名9
	 */
    @Column(name="PARAM_NAME9")
	private String paramName9;
	
	/**
	 * 参数名10
	 */
    @Column(name="PARAM_NAME10")
	private String paramName10;
	
	/**
	 * 参数名11
	 */
    @Column(name="PARAM_NAME11")
	private String paramName11;
	
	/**
	 * 参数名12
	 */
    @Column(name="PARAM_NAME12")
	private String paramName12;
	
	/**
	 * 参数名13
	 */
    @Column(name="PARAM_NAME13")
	private String paramName13;
	
	/**
	 * 参数名14
	 */
    @Column(name="PARAM_NAME14")
	private String paramName14;
	
	/**
	 * 参数名15
	 */
    @Column(name="PARAM_NAME15")
	private String paramName15;
	
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

	public String getParamName1() {
		return paramName1;
	}

	public void setParamName1(String paramName1) {
		this.paramName1 = paramName1;
	}

	public String getParamName2() {
		return paramName2;
	}

	public void setParamName2(String paramName2) {
		this.paramName2 = paramName2;
	}

	public String getParamName3() {
		return paramName3;
	}

	public void setParamName3(String paramName3) {
		this.paramName3 = paramName3;
	}

	public String getParamName4() {
		return paramName4;
	}

	public void setParamName4(String paramName4) {
		this.paramName4 = paramName4;
	}

	public String getParamName5() {
		return paramName5;
	}

	public void setParamName5(String paramName5) {
		this.paramName5 = paramName5;
	}

	public String getParamName6() {
		return paramName6;
	}

	public void setParamName6(String paramName6) {
		this.paramName6 = paramName6;
	}

	public String getParamName7() {
		return paramName7;
	}

	public void setParamName7(String paramName7) {
		this.paramName7 = paramName7;
	}

	public String getParamName8() {
		return paramName8;
	}

	public void setParamName8(String paramName8) {
		this.paramName8 = paramName8;
	}

	public String getParamName9() {
		return paramName9;
	}

	public void setParamName9(String paramName9) {
		this.paramName9 = paramName9;
	}

	public String getParamName10() {
		return paramName10;
	}

	public void setParamName10(String paramName10) {
		this.paramName10 = paramName10;
	}

	public String getParamName11() {
		return paramName11;
	}

	public void setParamName11(String paramName11) {
		this.paramName11 = paramName11;
	}

	public String getParamName12() {
		return paramName12;
	}

	public void setParamName12(String paramName12) {
		this.paramName12 = paramName12;
	}

	public String getParamName13() {
		return paramName13;
	}

	public void setParamName13(String paramName13) {
		this.paramName13 = paramName13;
	}

	public String getParamName14() {
		return paramName14;
	}

	public void setParamName14(String paramName14) {
		this.paramName14 = paramName14;
	}

	public String getParamName15() {
		return paramName15;
	}

	public void setParamName15(String paramName15) {
		this.paramName15 = paramName15;
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
