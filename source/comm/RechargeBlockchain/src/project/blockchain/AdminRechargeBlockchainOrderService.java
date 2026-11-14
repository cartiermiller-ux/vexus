package project.blockchain;

import kernel.web.Page;

/**
 * 后台区块链充值订单查询与到账接口
 */
public interface AdminRechargeBlockchainOrderService {

	public Page pagedQuery(int pageNo, int pageSize, String name_para, Integer state_para, String loginPartyId,String orderNo,String rolename_para,String startTime,String endTime);

	/**
	 * 管理员手工到账
	 */

	public void saveSucceeded(String order_no, String safeword,String operator_username,double success_amount);
	
	/**
	 * 驳回充值
	 * 
	 * @param id
	 * @param failure_msg 驳回原因
	 */
	public void saveReject(String id, String failure_msg,String userName,String partyId);
	/**
	 * 修改备注信息
	 * 
	 * @param id
	 * @param failure_msg 备注信息
	 */
	public void saveRejectRemark(String id, String failure_msg,String userName,String partyId);
	
	/**
	 * 修改图片信息
	 */
	public void saveRechargeImg(String id, String img,String safeword,String userName,String partyId);
}
