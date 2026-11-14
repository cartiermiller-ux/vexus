package project.user.internal;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.web.ApplicationUtil;
import project.user.UserWalletAddress;
import project.user.UserWalletAddressService;

public class UserWalletAddressServiceImpl implements UserWalletAddressService {
	/**
	 * 查询所有未生成地址的记录
	 */
	public List<UserWalletAddress> queryByState(String state){
		return ApplicationUtil.executeSelect(UserWalletAddress.class,"WHERE STATE=?",new Object[] {state});
	}
	
	/**
	 * 批量用户钱包地址信息
	 */
	public void updateBatch(List<UserWalletAddress> addressList) {
		String sql = "UPDATE T_USER_WALLET_ADDRESS SET ERC_ADDRESS=?,STATE=? WHERE PARTY_ID=?";
		ApplicationUtil.getBean(JdbcTemplate.class).batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setString(1, addressList.get(i).getErcAddress());
				ps.setString(2, "1");
				ps.setString(3, addressList.get(i).getPartyId());
			}
			@Override
			public int getBatchSize() {
				return addressList.size();
			}
		});
	}
}
