package project.contract.internal;

import project.contract.ContractLock;
import project.contract.ContractLockService;

public class ContractLockServiceImpl implements ContractLockService {

	/**
	 * 获取data服永续合约订单锁
	 */
	public boolean getContractLock(String orderNo) {
		return ContractLock.add(orderNo);
	}
	
	/**
	 * 删除锁
	 */
	public void removeContractLock(String orderNo) {
		ContractLock.remove(orderNo);
	}
}
