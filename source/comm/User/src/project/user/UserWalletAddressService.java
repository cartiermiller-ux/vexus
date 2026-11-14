package project.user;

import java.util.List;

public interface UserWalletAddressService {

	/**
	 * 查询所有未生成地址的记录
	 */
	List<UserWalletAddress> queryByState(String state);
	
	void updateBatch(List<UserWalletAddress> addressList);
}
