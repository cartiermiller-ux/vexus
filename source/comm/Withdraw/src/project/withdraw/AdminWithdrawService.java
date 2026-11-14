package project.withdraw;

import java.util.Date;

import kernel.web.Page;

public interface AdminWithdrawService {
	/**
	 * 驳回代付
	 * 
	 * @param id
	 * @param failure_msg 驳回原因
	 */
	public void saveReject(String id, String failure_msg,String userName,String partyId);

	/**
	 * 通过
	 */
	public void saveSucceeded(String id,String safeword,String userName,String partyId);
	
//	/**
//	 * 三方提现通过
//	 * @param id
//	 * @param safeword
//	 * @param userName
//	 * @param partyId
//	 */
//	public void saveSucceededThird(String id, String safeword, String userName, String partyId);

	public Page pagedQuery(int pageNo, int pageSize, String name_para, Integer succeeded, String loginPartyId,String orderNo,String rolename_para);

	public int getCount(Integer state_para, String loginPartyId);
	
	/**
	 * 修改用户提现地址
	 */
	public void saveAddress(String id,String safeword,String userName,String partyId,String newAddress);
	
	/**
	 * 修改备注描述
	 */
	public void updateWithdraw(String id,String partyId,String remark);
}
