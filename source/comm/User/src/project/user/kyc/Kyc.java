package project.user.kyc;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

/**
 * 
 * 实名认证
 */
@Table(name="T_KYC")
public class Kyc extends EntityObject {

	private static final long serialVersionUID = 7664064141060377449L;
	
	@Column(name="PARTY_ID")
	private Serializable partyId;
	/**
	 * ID名称，如身份证等
	 */
	@Column(name="IDNAME")
	private String idname;
	/**
	 * 证件号码
	 */
	@Column(name="IDNUMBER")
	private String idnumber;
	/**
	 * 实名姓名
	 */
	@Column(name="NAME")
	private String name;
	/**
	 * 证件正面照
	 */
	@Column(name="IDIMG_1")
	private String idimg_1;
	/**
	 * 证件背面照
	 */
	@Column(name="IDIMG_2")
	private String idimg_2;
	/**
	 * 正面手持证件照
	 */
	@Column(name="IDIMG_3")
	private String idimg_3;
	/**
	 * 证件正面照文件名
	 */
	private String idimg_1_path;
	/**
	 * 证件背面照文件名
	 */
	private String idimg_2_path;
	/**
	 * 手持正面证件照文件名
	 */
	private String idimg_3_path;
	
	/**
	 * 0已申请未审核 ，1审核中 ，2 审核通过,3审核未通过
	 */
	@Column(name="STATUS")
	private int status;
	/**
	 * 审核消息，未通过原因
	 * 
	 */
	@Column(name="MSG")
	private String msg;

	/**
	 * 国籍
	 */
	@Column(name="NATIONALITY")
	private String nationality;
	/**
	 * 提交时间
	 */
	@Column(name="APPLY_TIME")
	private Date apply_time;
	/**
	 * 审核时间
	 */
	@Column(name="OPERATION_TIME")
	private Date operation_time;

	/**
	 * 性别，man:男，woman:女
	 */
	@Column(name="SEX")
	private String sex;
	/**
	 * 出生日期
	 */
	@Column(name="BORTH_DATE")
	private String borth_date;

	public Serializable getPartyId() {
		return partyId;
	}

	public void setPartyId(Serializable partyId) {
		this.partyId = partyId;
	}

	public String getIdname() {
		return idname;
	}

	public void setIdname(String idname) {
		this.idname = idname;
	}

	public String getIdnumber() {
		return idnumber;
	}

	public void setIdnumber(String idnumber) {
		this.idnumber = idnumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdimg_1() {
		return idimg_1;
	}

	public void setIdimg_1(String idimg_1) {
		this.idimg_1 = idimg_1;
	}

	public String getIdimg_2() {
		return idimg_2;
	}

	public void setIdimg_2(String idimg_2) {
		this.idimg_2 = idimg_2;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public Date getApply_time() {
		return apply_time;
	}

	public void setApply_time(Date apply_time) {
		this.apply_time = apply_time;
	}

	public String getIdimg_1_path() {
		return idimg_1_path;
	}

	public void setIdimg_1_path(String idimg_1_path) {
		this.idimg_1_path = idimg_1_path;
	}

	public String getIdimg_2_path() {
		return idimg_2_path;
	}

	public void setIdimg_2_path(String idimg_2_path) {
		this.idimg_2_path = idimg_2_path;
	}

	public String getIdimg_3() {
		return idimg_3;
	}

	public void setIdimg_3(String idimg_3) {
		this.idimg_3 = idimg_3;
	}

	public String getIdimg_3_path() {
		return idimg_3_path;
	}

	public void setIdimg_3_path(String idimg_3_path) {
		this.idimg_3_path = idimg_3_path;
	}

	public String getSex() {
		return sex;
	}

	public String getBorth_date() {
		return borth_date;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public void setBorth_date(String borth_date) {
		this.borth_date = borth_date;
	}

	public Date getOperation_time() {
		return operation_time;
	}

	public void setOperation_time(Date operation_time) {
		this.operation_time = operation_time;
	}


}
