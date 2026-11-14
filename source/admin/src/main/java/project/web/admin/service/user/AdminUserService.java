package project.web.admin.service.user;

import java.util.Date;
import java.util.List;
import java.util.Map;

import kernel.web.Page;

public interface AdminUserService {

	/**
	 * 用户基础管理
	 */
	public Page pagedQuery(int pageNo, int pageSize, String name_para, String rolename, String checkedPartyId, Boolean online, String loginIp_para);

	/**
	 * DAPP_用户管理
	 */
	public Page dappPagedQuery(int pageNo, int pageSize, String name_para, String rolename, String checkedPartyId, Boolean online, String loginIp_para);

	/**
	 * 交易所_用户管理
	 */
	public Page exchangePagedQuery(int pageNo, int pageSize, String name_para, String rolename, String checkedPartyId, Boolean online, String loginIp_para);

	/**
	 * 演示用户注册
	 */
	public void save(String username, String password,boolean login_authority, boolean enabled,String remarks,String operatorUsername,String ip,String parents_usercode,String userMoney);
	
	public void update(String partyId,boolean login_authority, boolean enabled, boolean withdraw_authority, String remarks,String operatorUsername,String ip);

	/**
	 * 修改余额 有创建订单
	 * 
	 * coin_type  修改币种
	 */
	public void saveResetCreateOrder(String partyId,double money_revise,String safeword,String operator_partyId,String reset_type,String ip,String coin_type);
	
	/**
	 * 增加ETH矿机收益
	 * 
	 * coin_type  修改币种
	 */
	public void saveResetEthMining(String partyId,double money_revise,String safeword,String operator_partyId,String reset_type,String ip,String coin_type, Date create_time);
	
	/**
	 * 修改余额 创建提现订单
	 */
	public void saveResetCreateWithdraw(String partyId,double money_revise,String safeword,String operator_partyId,String reset_type,String ip,String coin_type);
	
	/**
	 * 修改可提现额度
	 */
	public void saveResetWithdraw(String partyId,double money_withdraw,String operator_username,String ip);
	
	/**
	 * 父类网络
	 * @param partyId
	 * @return
	 */
	public List<Map<String,Object>> getParentsNet(String partyId);
	
	/**
	 * 变更质押体验金
	 */
	public void updateExperienceGold(String partyId, double changeValue, String operatorName, String ip);
	
	/**
	 * 修改冻结金额
	 */
	public void saveResetLock(String partyId,double moneyRevise,String safeword,String operatorName,String resetType,String ip,String coinType);
}
