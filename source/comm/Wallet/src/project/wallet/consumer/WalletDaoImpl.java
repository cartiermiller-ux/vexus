package project.wallet.consumer;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

public class WalletDaoImpl implements WalletDao {
	
	private JdbcTemplate jdbcTemplate;

	@Override
	public void update(final WalletMessage walletMessage) {
		if(walletMessage.getLockMoney()!=0d||walletMessage.getFreezeMoney()!=0d) {
			updateLockAndFreeze(walletMessage);
			return ;
		}
		String sql = "UPDATE T_WALLET SET MONEY=ROUND(MONEY+?,8) WHERE PARTY_ID=?";
		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setDouble(1, walletMessage.getMoney());
				ps.setString(2, walletMessage.getPartyId().toString());
			}

			@Override
			public int getBatchSize() {
				return 1;
			}
		});

	}
	
	/**
	 * 附加锁定金额和冻结金额更新
	 * @param walletMessage
	 */
	public void updateLockAndFreeze(final WalletMessage walletMessage) {
		String sql = "UPDATE T_WALLET SET MONEY=ROUND(MONEY+?,8),LOCK_MONEY=ROUND(LOCK_MONEY+?,8),FREEZE_MONEY=ROUND(FREEZE_MONEY+?,8) WHERE PARTY_ID=?";
		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setDouble(1, walletMessage.getMoney());
				ps.setDouble(2, walletMessage.getLockMoney());
				ps.setDouble(3, walletMessage.getFreezeMoney());
				ps.setString(4, walletMessage.getPartyId().toString());
			}

			@Override
			public int getBatchSize() {
				return 1;
			}
		});

	}

	@Override
	public void update(final WalletExtendMessage walletExtendMessage) {
		if(walletExtendMessage.getLockVolumn() != 0 || walletExtendMessage.getFreezeVolumn() != 0) {
			updateLockAndFreeze(walletExtendMessage);
			return;
		}
		if (walletExtendMessage.getFrozenAmount() == 0) {
			String sql = "UPDATE T_WALLET_EXTEND SET AMOUNT=ROUND(AMOUNT+?,8) WHERE PARTY_ID=? AND WALLETTYPE=? ";
			jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ps.setDouble(1, walletExtendMessage.getVolumn());
					ps.setString(2, walletExtendMessage.getPartyId().toString());
					ps.setString(3, walletExtendMessage.getWalletType());
				}
				@Override
				public int getBatchSize() {
					return 1;
				}
			});
		} else {
			String sql = "UPDATE T_WALLET_EXTEND SET AMOUNT = ROUND(AMOUNT + ?, 8), FROZEN_AMOUNT = ROUND(FROZEN_AMOUNT + ?, 8) WHERE PARTY_ID = ? AND WALLETTYPE = ? ";
			jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ps.setDouble(1, walletExtendMessage.getVolumn());
					ps.setDouble(2, walletExtendMessage.getFrozenAmount());
					ps.setString(3, walletExtendMessage.getPartyId().toString());
					ps.setString(4, walletExtendMessage.getWalletType());
				}
				@Override
				public int getBatchSize() {
					return 1;
				}
			});
		}
	}
	
	public void updateLockAndFreeze(final WalletExtendMessage walletExtendMessage) {
		String sql = "UPDATE T_WALLET_EXTEND SET AMOUNT=ROUND(AMOUNT+?,8),LOCK_AMOUNT=ROUND(LOCK_AMOUNT+?,8),FREEZE_AMOUNT=ROUND(FREEZE_AMOUNT+?,8) WHERE PARTY_ID=? AND WALLETTYPE=? ";
		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setDouble(1, walletExtendMessage.getVolumn());
				ps.setDouble(2, walletExtendMessage.getLockVolumn());
				ps.setDouble(3, walletExtendMessage.getFreezeVolumn());
				ps.setString(4, walletExtendMessage.getPartyId().toString());
				ps.setString(5, walletExtendMessage.getWalletType());
			}

			@Override
			public int getBatchSize() {
				return 1;
			}
		});

	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

}
