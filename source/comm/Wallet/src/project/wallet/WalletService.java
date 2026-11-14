package project.wallet;

import java.io.Serializable;
import java.util.List;

/**
 * 钱包
 */
public interface WalletService {

	public Wallet saveWalletByPartyId(Serializable partyId);

	public void save(Wallet wallet);

	public void update(String partyId, double amount);

	public WalletExtend saveExtendByPara(Serializable partyId, String wallettype);

	public List<WalletExtend> findExtend(Serializable partyId);

	public List<WalletExtend> findExtend(Serializable partyId, List<String> list_symbol);

	public void save(WalletExtend walletExtend);

	/**
	 * 
	 * @param partyId
	 * @param walletType
	 * @param amount修改的金额
	 */
	public void updateExtend(String partyId, String walletType, double amount);
	
	/**
	 * 修改拓展钱包 余额 及冻结余额
	 */
	public void updateExtend(String partyId, String walletType, double amount, double frozenAmount);

//	public WalletExtend findExtendByPara(Serializable partyId, String wallettype);

	/**
	 * 转账给其他用户 出款方 byPartyId 收款方Uid toPartyId 手续费数量
	 */
	public void transfer_wallet(String byPartyId, String safeword, String toPartyId, String coin, double amount,
			double fee_amount);

	public List<WalletExtend> findAllWalletExtend();
	
	public List<WalletExtend> findWalletExtendForRealtime();
	
	public void updateExtendWithLockAndFreeze(String partyId, String walletType, double amount, double lockAmount, double freezeAmount);

	public void updateWithLockAndFreeze(String partyId, double amount,double lockAmount,double freezeAmount);
	
	public List<Wallet> findAllWallet();
}
