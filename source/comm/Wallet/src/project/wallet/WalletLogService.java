package project.wallet;

import kernel.web.Page;

/**
 * 充提记录
 *
 */
public interface WalletLogService {

	public void save(WalletLog entity);

	public WalletLog find(String category, String order_no);

	public void update(WalletLog entity);

	public Page pagedQuery(int pageNo, int pageSize, String partyId, String category, String order_no_null);
	
	public Page pagedQueryWithdraw(int pageNo, int pageSize, String partyId, String order_no_null);
	
	public Page pagedQueryRecharge(int pageNo, int pageSize, String partyId, String order_no_null);

	public Page pagedQueryRecords(int pageNo, int pageSize, String partyId, String category, String start_time, String end_time, String symbol, Integer status);

	public WalletLog find(String order_no);

	/**
	 * 根据订单去更新日志状态
	 * 
	 * @param orderNo
	 * @param status
	 */
	public void updateStatus(String orderNo, int status);

}
