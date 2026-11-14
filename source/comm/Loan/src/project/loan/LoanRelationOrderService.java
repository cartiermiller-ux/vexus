package project.loan;

import java.util.List;
import java.util.Map;

public interface LoanRelationOrderService {

	/**
	 * 新增质押借币关联订单
	 */
	public void saveLoanRelationOrder(LoanRelationOrder order);
	
	/**
	 * 质押记录
	 */
	public List<Map<String, Object>> pagedQueryPledge(int pageNo, int pageSize, String loanOrderId);
	
	/**
	 * 补增质押
	 */
	public void saveReplenishOrder(String loanOrderId, double pledgeAmount, String partyId);
	
	/**
	 * 续借
	 */
	public void saveRefurbishOrder(String loanOrderId, double loanAmount, String partyId);
	
	/**
	 * 还款
	 */
	public void saveRepayOrder(String loanOrderId, double repayAmount, String partyId);
	
	/**
	 * 根据订单关联ID获取订单列表
	 */
	public List<LoanRelationOrder> queryOrders(String loanRelationOrderId, int orderType);
	
	/**
	 * 后台质押记录
	 */
	public List<Map<String,Object>> queryLoanRelation(String loanRelationOrderId);
}
