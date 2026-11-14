package project.monitor.withdraw.internal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.web.ApplicationUtil;
import project.Constants;
import project.data.DataService;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.monitor.AutoMonitorDAppLogService;
import project.monitor.model.AutoMonitorDAppLog;
import project.monitor.telegram.business.TelegramBusinessMessageService;
import project.monitor.withdraw.AutoMonitorWithdrawCollection;
import project.monitor.withdraw.AutoMonitorWithdrawCollectionService;
import project.party.PartyService;
import project.party.model.Party;
import project.syspara.SysparaService;
import project.tip.TipConstants;
import project.tip.TipService;
import project.user.QRGenerateService;
import project.user.UserDataService;
//import project.user.kyc.KycHighLevelService;
//import project.user.kyc.KycService;
//import project.user.payment.PaymentMethodService;
import project.wallet.Wallet;
import project.wallet.WalletExtend;
import project.wallet.WalletLogService;
import project.wallet.WalletService;
import project.wallet.rate.ExchangeRateService;
import util.DateUtil;
import util.RandomUtil;

public class AutoMonitorWithdrawCollectionServiceImpl implements AutoMonitorWithdrawCollectionService {

	protected TipService tipService;
	
	protected DataService dataService;
	
	protected PartyService partyService;
	
	protected WalletService walletService;
	
	protected SysparaService sysparaService;
	
	protected MoneyLogService moneyLogService;
	
	protected UserDataService userDataService;

	protected WalletLogService walletLogService;
	
	protected QRGenerateService qRGenerateService;
	
	protected ExchangeRateService exchangeRateService;
	
	protected AutoMonitorDAppLogService autoMonitorDAppLogService;

	protected TelegramBusinessMessageService telegramBusinessMessageService;

	public void saveExchangeApply(AutoMonitorWithdrawCollection withdraw) {
		String symbol = withdraw.getMethod();
		Party party = this.partyService.cachePartyBy(withdraw.getPartyId(), false);
		if (Constants.SECURITY_ROLE_TEST.equals(party.getRolename())) {
			throw new BusinessException("No permission");
		}

		if (!party.getWithdraw_authority()) {
			throw new BusinessException(1, "No permission");
		}
		if (!party.getEnabled()) {
			throw new BusinessException(506, "No permission");
		}

		// 转换金额USDT,手续费计算
		double fee = feeOfExchange(withdraw.getPartyId().toString(), withdraw.getVolume());
		withdraw.setAmount_fee(fee);
		withdraw.setAmount(Arith.sub(withdraw.getVolume(), fee));

		if (withdraw.getAmount() < 0) {
			// 提现的金额都不足缴纳手续费
			throw new BusinessException(1, "Conversion must not be less than the fee ");
		}

		WalletExtend walletExtend = walletService.saveExtendByPara(party.getId(), symbol);
		if (walletExtend.getAmount() < withdraw.getVolume()) { // 余额不足
			throw new BusinessException(1, "Insufficient balance");
		}
		
		String withdraw_limit = sysparaService.find("withdraw_limit_dapp").getValue();
		if (withdraw.getVolume() < Double.valueOf(withdraw_limit)) { //转换不得小于限额
			throw new BusinessException(1, "Conversion must not be less than the limit (" + withdraw_limit + " USDT)");
		}
		
		/**
		 * 是否在当日提现时间内
		 */
		SimpleDateFormat sdf = new SimpleDateFormat();// 格式化时间
		sdf.applyPattern("HH:mm:ss");// a为am/pm的标记
		Date date = new Date();// 获取当前时间
		String withdraw_limit_time = sysparaService.find("withdraw_limit_time").getValue();
		if (!"".equals(withdraw_limit_time) && withdraw_limit_time != null) {
			String[] withdraw_time = withdraw_limit_time.split("-");
			String dateString = sdf.format(date);
			if (dateString.compareTo(withdraw_time[0]) < 0 || dateString.compareTo(withdraw_time[1]) > 0) {
				//不在可转换时间内
				throw new BusinessException(1, "Not within the convertible time");
			}
		}

		if ("".equals(withdraw.getOrder_no()) || withdraw.getOrder_no() == null) {
			withdraw.setOrder_no(DateUtil.getToday("yyMMddHHmmss") + RandomUtil.getRandomNum(8));
		}

		withdraw.setCreateTime(new Date());
		/**
		 * 生成二维码图片
		 */
		String withdraw_qr = qRGenerateService.generateWithdraw(withdraw.getOrder_no(), withdraw.getAddress());

		withdraw.setQdcode(withdraw_qr);

		walletService.updateExtend(walletExtend.getPartyId().toString(), symbol, Arith.sub(0, withdraw.getVolume()));
		ApplicationUtil.executeInsert(withdraw);

		/*
		 * 保存资金日志
		 */

		AutoMonitorDAppLog autoMonitorDAppLog = new AutoMonitorDAppLog();
		autoMonitorDAppLog.setPartyId(withdraw.getPartyId());
		autoMonitorDAppLog.setOrder_no(withdraw.getOrder_no());
		autoMonitorDAppLog.setStatus(withdraw.getSucceeded());

		autoMonitorDAppLog.setAmount(withdraw.getAmount());
		autoMonitorDAppLog.setCreateTime(new Date());
		// 转换的金额是负数
		autoMonitorDAppLog.setExchange_volume(-withdraw.getVolume());
		autoMonitorDAppLog.setAction(AutoMonitorDAppLog.ACTION_REDEEM);

		autoMonitorDAppLogService.save(autoMonitorDAppLog);
		tipService.saveTip(withdraw.getId().toString(), TipConstants.AUTO_MONITOR_REDEEM);
	}

	public double feeOfExchange(String partyId, double volume) {
		/**
		 * 提现手续费类型,fixed是单笔固定金额，rate是百分比，part是分段
		 */
		String withdraw_fee_type = sysparaService.find("withdraw_fee_type").getValue();

		/**
		 * fixed单笔固定金额 和 rate百分比 的手续费数值
		 */
		double withdraw_fee = Double.valueOf(sysparaService.find("withdraw_fee").getValue());

		double fee = 0;
		double usdtVolume = volume;
		if ("fixed".equals(withdraw_fee_type)) {
			fee = withdraw_fee;
		}
		if ("rate".equals(withdraw_fee_type)) {
			withdraw_fee = Arith.div(withdraw_fee, 100);
			fee = Arith.mul(usdtVolume, withdraw_fee);
		}
		if ("part".equals(withdraw_fee_type)) {
			/**
			 * 提现手续费part分段的值
			 */
			String withdraw_fee_part = sysparaService.find("withdraw_fee_part").getValue();

			String[] withdraw_fee_parts = withdraw_fee_part.split(",");
			for (int i = 0; i < withdraw_fee_parts.length; i++) {
				double part_amount = Double.valueOf(withdraw_fee_parts[i]);
				double part_fee = Double.valueOf(withdraw_fee_parts[i + 1]);
				if (usdtVolume <= part_amount) {
					fee = part_fee;
					break;
				}
				i++;
			}
		}
		return fee;
	}

	/**
	 * 获取其他通道的手续费
	 * 
	 * @param volume 提现数量
	 * @return
	 */
	public double getOtherChannelWithdrawFee(double volume) {
		/**
		 * 提现手续费part分段的值
		 */
		String withdraw_fee_part = sysparaService.find("withdraw_other_channel_fee_part").getValue();
		double fee = 0;
		String[] withdraw_fee_parts = withdraw_fee_part.split(",");
		for (int i = 0; i < withdraw_fee_parts.length; i++) {
			double part_amount = Double.valueOf(withdraw_fee_parts[i]);
			double part_fee = Double.valueOf(withdraw_fee_parts[i + 1]);
			if (volume <= part_amount) {
				fee = Arith.mul(part_fee, volume);
				break;
			}
			i++;
		}
		return fee;
	}

	@Override
	public AutoMonitorWithdrawCollection findByOrderNo(String order_no) {
		List<AutoMonitorWithdrawCollection> list = ApplicationUtil.executeSelect(AutoMonitorWithdrawCollection.class,"WHERE ORDER_NO=?",new Object[] {order_no});
		return list.size()<=0?null:list.get(0);
	}

	public boolean saveReject(AutoMonitorWithdrawCollection withdraw) {
		// 通过后不可驳回
		if (withdraw.getSucceeded() == 2 || withdraw.getSucceeded() == 1) return false;
		
		withdraw.setSucceeded(2);
		String symbol = "";
		if (withdraw.getMethod().indexOf("BTC") != -1) {
			symbol = "btc";
		} else if (withdraw.getMethod().indexOf("ETH") != -1) {
			symbol = "eth";
		} else {
			symbol = "usdt";
		}

		if ("usdt".equals(symbol)) {
			Wallet wallet = walletService.saveWalletByPartyId(withdraw.getPartyId());
			double amount_before = wallet.getMoney();
			walletService.update(wallet.getPartyId().toString(),Arith.add(withdraw.getAmount(), withdraw.getAmount_fee()));

			/*
			 * 保存资金日志
			 */
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
			moneyLog.setAmount_before(amount_before);
			moneyLog.setAmount(Arith.add(withdraw.getAmount(), withdraw.getAmount_fee()));
			moneyLog.setAmount_after(Arith.add(amount_before, Arith.add(withdraw.getAmount(), withdraw.getAmount_fee())));

			moneyLog.setLog("驳回提现[" + withdraw.getOrder_no() + "]");
			moneyLog.setPartyId(withdraw.getPartyId());
			moneyLog.setWallettype(Constants.WALLET);
			moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_WITHDRAW);

			moneyLogService.save(moneyLog);

		} else {
			WalletExtend walletExtend = walletService.saveExtendByPara(withdraw.getPartyId(), symbol);
			double amount_before = walletExtend.getAmount();
			walletService.updateExtend(withdraw.getPartyId().toString(), symbol, withdraw.getVolume());

			/*
			 * 保存资金日志
			 */
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
			moneyLog.setAmount_before(amount_before);
			moneyLog.setAmount(withdraw.getVolume());
			moneyLog.setAmount_after(Arith.add(amount_before, withdraw.getVolume()));

			moneyLog.setLog("驳回提现[" + withdraw.getOrder_no() + "]");
			// moneyLog.setExtra(withdraw.getOrder_no());
			moneyLog.setPartyId(withdraw.getPartyId());
			moneyLog.setWallettype(symbol.toUpperCase());
			moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_WITHDRAW);

			moneyLogService.save(moneyLog);
		}
		this.update(withdraw);
		this.walletLogService.updateStatus(withdraw.getOrder_no(), withdraw.getSucceeded());

		return true;

	}

	public List<AutoMonitorWithdrawCollection> findAllByDate(String partyId) {
		return ApplicationUtil.executeSelect(AutoMonitorWithdrawCollection.class,"WHERE PARTY_ID=? AND DATEDIFF(CREATE_TIME,NOW())=0",new Object[] {partyId});
	}

	public List<AutoMonitorWithdrawCollection> findAllByStateAndPartyId(int state, String partyId) {
		return ApplicationUtil.executeSelect(AutoMonitorWithdrawCollection.class,"WHERE PARTY_ID=? AND SUCCEEDED=?",new Object[] {partyId,state});
	}
	
	/**
	 * 是否存在提现订单
	 * @param partyId
	 * @return true:存在，false：不存在
	 */
	public boolean hasWithdraw(String partyId) {
		List<String> list=ApplicationUtil.executeDQL("SELECT UUID FROM T_AUTO_MONITOR_WITHDRAW_COLLECTION_ORDER WHERE PARTY_ID=? LIMIT 0,1", new Object[] {partyId},String.class);
		return !CollectionUtils.isEmpty(list);
	}

	public void update(AutoMonitorWithdrawCollection withdraw) {
		ApplicationUtil.executeUpdate(withdraw);
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setWalletService(WalletService walletService) {
		this.walletService = walletService;
	}

	public void setMoneyLogService(MoneyLogService moneyLogService) {
		this.moneyLogService = moneyLogService;
	}

	public void setExchangeRateService(ExchangeRateService exchangeRateService) {
		this.exchangeRateService = exchangeRateService;
	}

	public void setWalletLogService(WalletLogService walletLogService) {
		this.walletLogService = walletLogService;
	}

	public void setqRGenerateService(QRGenerateService qRGenerateService) {
		this.qRGenerateService = qRGenerateService;
	}

	@Override
	public boolean saveSucceeded(AutoMonitorWithdrawCollection withdraw) {
		if (withdraw.getSucceeded() == 1) return false;
		
		withdraw.setSucceeded(1);
		
		String symbol = "";
		if (withdraw.getMethod().indexOf("BTC") != -1) {
			symbol = "btc";
		} else if (withdraw.getMethod().indexOf("ETH") != -1) {
			symbol = "eth";
		} else {
			symbol = "usdt";
		}

		this.walletLogService.updateStatus(withdraw.getOrder_no(), withdraw.getSucceeded());
		
		/**
		 * 提现订单加入userdate
		 */
		this.userDataService.saveWithdrawHandleDapp(withdraw.getPartyId(), withdraw.getAmount(), withdraw.getAmount_fee(),symbol);

		this.update(withdraw);

		return true;
	}

	public void setUserDataService(UserDataService userDataService) {
		this.userDataService = userDataService;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setTipService(TipService tipService) {
		this.tipService = tipService;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	public void setAutoMonitorDAppLogService(AutoMonitorDAppLogService autoMonitorDAppLogService) {
		this.autoMonitorDAppLogService = autoMonitorDAppLogService;
	}

	public void setTelegramBusinessMessageService(TelegramBusinessMessageService telegramBusinessMessageService) {
		this.telegramBusinessMessageService = telegramBusinessMessageService;
	}
}
