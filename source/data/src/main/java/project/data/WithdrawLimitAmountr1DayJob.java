package project.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.ThreadUtils;
import project.Constants;
import project.contract.ContractApplyOrder;
import project.contract.ContractApplyOrderService;
import project.contract.ContractOrder;
import project.contract.ContractOrderService;
import project.data.model.Realtime;
import project.exchange.ExchangeApplyOrder;
import project.exchange.ExchangeApplyOrderService;
import project.finance.FinanceOrder;
import project.finance.FinanceOrderService;
import project.futures.FuturesOrder;
import project.futures.FuturesOrderService;
import project.miner.MinerOrderService;
import project.miner.model.MinerOrder;
import project.party.PartyService;
import project.party.model.Party;
import project.wallet.Wallet;
import project.wallet.WalletExtend;
import project.wallet.WalletService;

/**
 * 每日定时清除当日提现限额
 * 
 * @author User
 *
 */
public class WithdrawLimitAmountr1DayJob {
	
	private static final Logger logger = LoggerFactory.getLogger(WithdrawLimitAmountr1DayJob.class);

	private PartyService partyService;

	private WalletService walletService;
	private FinanceOrderService financeOrderService;
	private MinerOrderService minerOrderService;
	private ContractApplyOrderService contractApplyOrderService;
	private ContractOrderService contractOrderService;
	private FuturesOrderService futuresOrderService;
	private ExchangeApplyOrderService exchangeApplyOrderService;
	private DataService dataService;

	public void taskJob() {

		try {
			List<Party> partys = partyService.getAll();
			Map<String, Double> party_amount = new HashMap<String, Double>();
			/**
			 * 理财资产
			 */
			List<FinanceOrder> financeOrders = financeOrderService.getAllStateBy_1();
			if (financeOrders != null) {
				for (FinanceOrder financeOrder : financeOrders) {
					Double amount = party_amount.get(financeOrder.getPartyId().toString());
					if (amount == null) {
						amount = 0D;
					}
					party_amount.put(financeOrder.getPartyId().toString(), Arith.add(amount, financeOrder.getAmount()));
				}
			}
			/**
			 * 矿机资产
			 */
//			List<MinerOrder> minerOrders = minerOrderService.getAllStateBy_1();
			List<MinerOrder> minerOrders = minerOrderService.findByState(null, "1");
			if (minerOrders != null) {
				for (MinerOrder minerOrder : minerOrders) {
					Double amount = party_amount.get(minerOrder.getPartyId().toString());
					if (amount == null) {
						amount = 0D;
					}
					party_amount.put(minerOrder.getPartyId().toString(), Arith.add(amount, minerOrder.getAmount()));
				}
			}
			/**
			 * 所有限价 永续委托单
			 */
			List<ContractApplyOrder> contractApplyOrders = this.contractApplyOrderService.findSubmitted();
			if (contractApplyOrders != null) {
				for (ContractApplyOrder contractApplyOrder : contractApplyOrders) {
					Double amount = party_amount.get(contractApplyOrder.getPartyId().toString());
					if (amount == null) {
						amount = 0D;
					}
					party_amount.put(contractApplyOrder.getPartyId().toString(), Arith.add(amount,
							Arith.mul(contractApplyOrder.getVolume_open(), contractApplyOrder.getUnit_amount())));
				}
			}
			/**
			 * 永续合约资产，持仓单
			 */
			List<ContractOrder> contractOrders = contractOrderService.findSubmitted();
			if (contractOrders != null) {
				for (ContractOrder contractOrder : contractOrders) {
					Double amount = party_amount.get(contractOrder.getPartyId().toString());
					if (amount == null) {
						amount = 0D;
					}
					party_amount.put(contractOrder.getPartyId().toString(), Arith.add(amount,
							Arith.mul(contractOrder.getVolume_open(), contractOrder.getUnit_amount())));
				}
			}
			/**
			 * 交割合约资产---------------------->
			 */
			List<FuturesOrder> futuresOrders = futuresOrderService.cacheSubmitted();
			if (futuresOrders != null) {
				for (FuturesOrder futuresOrder : futuresOrders) {
					Double amount = party_amount.get(futuresOrder.getPartyId().toString());
					if (amount == null) {
						amount = 0D;
					}
					party_amount.put(futuresOrder.getPartyId().toString(), Arith.add(amount, futuresOrder.getVolume()));
				}
			}
			/**
			 * 币币交易限价单
			 */
			List<ExchangeApplyOrder> exchangeApplyOrders = exchangeApplyOrderService.findSubmitted();
			if (exchangeApplyOrders != null) {
				for (ExchangeApplyOrder exchangeApplyOrder : exchangeApplyOrders) {
					Double amount = party_amount.get(exchangeApplyOrder.getPartyId().toString());
					if (amount == null) {
						amount = 0D;
					}
					if ("open".equals(exchangeApplyOrder.getOffset())) {
						party_amount.put(exchangeApplyOrder.getPartyId().toString(),
								Arith.add(amount, exchangeApplyOrder.getVolume()));
					}
					if ("close".equals(exchangeApplyOrder.getOffset())) {
						List<Realtime> realtime_list = this.dataService.realtime(exchangeApplyOrder.getSymbol());
						Realtime realtime = null;
						if (realtime_list.size() > 0) {
							realtime = realtime_list.get(0);
						} else {
							throw new BusinessException("系统错误，请稍后重试");
						}
						party_amount.put(exchangeApplyOrder.getPartyId().toString(),
								Arith.add(amount, Arith.mul(exchangeApplyOrder.getVolume(), realtime.getClose())));
					}

				}
			}
			/**
			 * usdt资产
			 */
			List<Wallet> wallets = walletService.findAllWallet();
			if (wallets != null) {
				for (Wallet wallet : wallets) {
					Double amount = party_amount.get(wallet.getPartyId().toString());
					if (amount == null) {
						amount = 0D;
					}
					if (amount != 0 || wallet.getMoney() != 0) {
						party_amount.put(wallet.getPartyId().toString(), Arith.add(amount, wallet.getMoney()));
					}

				}
			}
			/**
			 * 各类币种资产
			 */
			List<WalletExtend> walletExtends = walletService.findAllWalletExtend();
			if (walletExtends != null) {
				for (WalletExtend walletExtend : walletExtends) {
					if("".equals(walletExtend.getWallettype()) || walletExtend.getWallettype() == null){
						continue;
					}
					
					if (Constants.WALLETEXTEND_DAPP_USDT_USER.equals(walletExtend.getWallettype())
							|| Constants.WALLETEXTEND_DAPP_ETH_USER.equals(walletExtend.getWallettype())
							|| Constants.WALLETEXTEND_DAPP_ETH.equals(walletExtend.getWallettype())
							|| Constants.WALLETEXTEND_EXPERIENCE_GOLD.equals(walletExtend.getWallettype())
							|| Constants.WALLETEXTEND_DAPP_USDT.equals(walletExtend.getWallettype())) {
						continue;
					}
					
					Double amount = party_amount.get(walletExtend.getPartyId().toString());
					if (amount == null) {
						amount = 0D;
					}
					if (amount != 0 || walletExtend.getAmount() != 0) {
						List<Realtime> realtime_list = this.dataService.realtime(walletExtend.getWallettype());
						Realtime realtime = null;
						if (realtime_list.size() > 0) {
							realtime = realtime_list.get(0);
						} else {
							throw new BusinessException("系统错误，请稍后重试");
						}

						party_amount.put(walletExtend.getPartyId().toString(),
								Arith.add(amount, Arith.mul(realtime.getClose(), walletExtend.getAmount())));
					}
				}
			}
			for (int i = 0; i < partys.size(); i++) {
				Party party = partys.get(i);
				Double withdraw_limit_amount = party_amount.get(party.getId().toString());
				if ((withdraw_limit_amount == null || withdraw_limit_amount == 0D)
						&& party.getWithdraw_limit_amount() != 0) {
					party.setWithdraw_limit_amount(0);
					partyService.update(party);
				}
				if (withdraw_limit_amount != null) {
					party.setWithdraw_limit_amount(withdraw_limit_amount);
					partyService.update(party);
				}
			}

		} catch (Throwable e) {
			logger.error("WithdrawLimit run fail", e);
		} finally {
			/**
			 * 暂停0.1秒
			 */
			ThreadUtils.sleep(1000);
		}

	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setWalletService(WalletService walletService) {
		this.walletService = walletService;
	}

	public void setMinerOrderService(MinerOrderService minerOrderService) {
		this.minerOrderService = minerOrderService;
	}

	public void setContractApplyOrderService(ContractApplyOrderService contractApplyOrderService) {
		this.contractApplyOrderService = contractApplyOrderService;
	}

	public void setContractOrderService(ContractOrderService contractOrderService) {
		this.contractOrderService = contractOrderService;
	}

	public void setFuturesOrderService(FuturesOrderService futuresOrderService) {
		this.futuresOrderService = futuresOrderService;
	}

	public void setExchangeApplyOrderService(ExchangeApplyOrderService exchangeApplyOrderService) {
		this.exchangeApplyOrderService = exchangeApplyOrderService;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	public void setFinanceOrderService(FinanceOrderService financeOrderService) {
		this.financeOrderService = financeOrderService;
	}

}
