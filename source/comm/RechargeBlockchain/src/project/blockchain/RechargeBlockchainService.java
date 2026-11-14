package project.blockchain;

import java.io.Serializable;
import java.util.List;

/**
 * 区块链充值订单接口
 * 
 * @author User
 *
 */
public interface RechargeBlockchainService {

	public void save(RechargeBlockchain recharge);

	public void save_api(RechargeBlockchain recharge);

	public RechargeBlockchain findByOrderNo(String order_no);

	/**
	 * 查找当日内用户的所有充值订单
	 * 
	 * @param order_no
	 * @return
	 */
	public List<RechargeBlockchain> findByPartyIdAndToday(Serializable partyId);

	/**
	 * 查找所有用户几天前的指定一天的的所有充值订单
	 * 
	 * @param order_no days 几天前
	 * @return
	 */
	public List<RechargeBlockchain> findBySucceededAndDay(int succeeded, Integer days);

	/**
	 * 
	 * @param recharge
	 */
	public boolean saveSucceeded(String order_no, String operator, double success_amount);

	/**
	 * 
	 * @param 驳回申请
	 */
	public boolean saveReject(RechargeBlockchain recharge);

}
