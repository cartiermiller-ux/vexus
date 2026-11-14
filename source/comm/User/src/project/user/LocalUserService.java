package project.user;

import project.party.model.Party;

public interface LocalUserService {
	
	/**
	 * 注册用户
	 */
	public void saveRegister(LocalNormalReg reg, String type);
	
	/**
	 * 手机/邮箱注册（无验证码）
	 */
	public void saveRegisterNoVerifcode(LocalNormalReg reg, String type);
	
	/**
	 * 体验用户注册
	 */
	public void saveRegisterTest(LocalNormalReg reg, String type);
	
	/**
	 * 承兑商注册
	 */
	public Party registerC2cUser(String username, String password, String re_password, String type, String usercode, String ip);

	/**
	 * 注册用户无验证码
	 */
	public void saveRegisterUsername(LocalNormalReg reg);

	/**
	 * 无验证码注册试用用户
	 */
	public void saveRegisterUsernameTest(LocalNormalReg reg);

	/**
	 * 获取手机号
	 */
	public String getPhone(String partyId);

	/**
	 * 保存更新手机号
	 */
	public void savePhone(String phone, String partyId);

	/**
	 * 获取邮箱号
	 */
	public String getEmail(String partyId);

	/**
	 * 保存更新邮箱
	 */
	public void saveEmail(String email, String partyId);

	/**
	 * 获取图片
	 */
//	public List<ImageQr> findImageByUsercode(String usercode, String image_language, String image_type);
//
//	public List<ImageQr> findAndSaveImageByUsercode(String usercode, String image_language, String image_type);

}
