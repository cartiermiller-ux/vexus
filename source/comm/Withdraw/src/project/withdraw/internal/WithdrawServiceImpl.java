package project.withdraw.internal;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;


import kernel.bo.RecordObjectMapper;
import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import project.Constants;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.party.PartyService;
import project.party.model.Party;
import project.syspara.Syspara;
import project.syspara.SysparaService;
import project.tip.TipConstants;
import project.tip.TipService;
import project.user.QRGenerateService;
import project.user.UserData;
import project.user.UserDataService;
import project.user.googleauth.GoogleAuthService;
import project.user.kyc.Kyc;
import project.user.kyc.KycHighLevel;
import project.user.kyc.KycHighLevelService;
import project.user.kyc.KycService;
import project.wallet.Wallet;
import project.wallet.WalletExtend;
import project.wallet.WalletLog;
import project.wallet.WalletLogService;
import project.wallet.WalletService;
import project.wallet.rate.ExchangeRateService;
import project.withdraw.Withdraw;
import project.withdraw.WithdrawService;
import security.SecUser;
import security.internal.SecUserService;
import util.DateUtil;
import util.RandomUtil;

public class WithdrawServiceImpl implements WithdrawService {
	
	private Logger logger = LoggerFactory.getLogger(WithdrawServiceImpl.class);
	protected JdbcTemplate jdbcTemplate;
	protected SysparaService sysparaService;
	protected WalletService walletService;
	protected MoneyLogService moneyLogService;
	protected ExchangeRateService exchangeRateService;
	protected WalletLogService walletLogService;
	protected QRGenerateService qRGenerateService;
	protected UserDataService userDataService;
	protected PartyService partyService;
	protected KycService kycService;
	protected KycHighLevelService kycHighLevelService;
	protected TipService tipService;

	@Override
	public void saveApply(Withdraw withdraw, String channel, String method_id, String googleCode) {
		Party party = this.partyService.cachePartyBy(withdraw.getPartyId(), false);
		if (Constants.SECURITY_ROLE_TEST.equals(party.getRolename())) {
			throw new BusinessException(1, "无权限");
		}
		
		//验证谷歌密钥
		Syspara google_auth_secret_open = sysparaService.find("google_auth_secret_open");
		if(ObjectUtils.isNotEmpty(google_auth_secret_open)) {	
			if("1".equals(google_auth_secret_open.getValue())) {
				GoogleAuthService googleAuthService =ApplicationUtil.getBean(GoogleAuthService.class);
				SecUserService secUserService = ApplicationUtil.getBean(SecUserService.class);
				SecUser secUser = secUserService.findUserByLoginName(party.getUsername());
				if(!secUser.isGoogle_auth_bind()) {
					throw new BusinessException(1, "需要绑定谷歌验证器才可以提现");
				}
				boolean bool = googleAuthService.checkCode(secUser.getGoogle_auth_secret(), googleCode);
				if(!bool) {
					throw new BusinessException(1, "谷歌验证码错误");
				}
			}
		}
		
		Syspara bind_phone_email_withdraw = sysparaService.find("bind_phone_email_withdraw");
		if(ObjectUtils.isNotEmpty(bind_phone_email_withdraw)) {
			if("1".equals(bind_phone_email_withdraw.getValue())) {
				if(party.getEmail_authority() == false || party.getPhone_authority()== false) {
					throw new BusinessException(1, "需要绑定邮箱和手机才可以提现");
				}
			}
		}
		
		List<Withdraw> pendingWithdrawList = findAllByStateAndPartyId(0, withdraw.getPartyId().toString());
		if (null != pendingWithdrawList && pendingWithdrawList.size() > 1) {
			throw new BusinessException(1, "当前有待处理提现订单，请稍后提现！");
		}
		
		List<Withdraw> withdrawList = findAllByStateAndPartyId(null, withdraw.getPartyId().toString());
		if (withdrawList.size() > 0 && !withdrawList.get(0).getAddress().equals(withdraw.getAddress())) {
			withdraw.setRemarks("系统提示: 本次提现地址[" + withdraw.getAddress() + "] 和上次提现地址["
		+ withdrawList.get(0).getAddress() +"]不同, 请注意核对!!!");
		}
		
		if (withdrawList.size() > 0 && null != withdrawList.get(0).getDeviceIp() && 
				!withdrawList.get(0).getDeviceIp().equals(withdraw.getDeviceIp())) {
			if (null != withdraw.getRemarks()) {
				withdraw.setRemarks("系统提示: 本次提现地址["+withdraw.getAddress()+"]及本次设备IP["+withdraw.getDeviceIp()
				+"] 和上次提现地址["+withdrawList.get(0).getAddress()+"]及上次设备IP["+withdrawList.get(0).getDeviceIp()+"]不同, 请注意核对!!!");
			} else {
				withdraw.setRemarks("系统提示: 本次提现用户设备IP["+withdraw.getDeviceIp()+"] 和上次提现用户设备IP["
			+withdrawList.get(0).getDeviceIp()+"]不同, 请注意核对!!!");
			}
		}
		
		withdraw.setMethod(channel);
		if (channel.indexOf("BTC") != -1) {
			saveApplyOtherChannel(withdraw, "btc");
			return;
		} else if (channel.indexOf("ETH") != -1) {
			saveApplyOtherChannel(withdraw, "eth");
			return;
		}
		Kyc party_kyc = this.kycService.get(withdraw.getPartyId().toString());

		KycHighLevel party_kycHighLevel = this.kycHighLevelService.get(withdraw.getPartyId().toString());

		if (!(party_kyc.getStatus() == 2) && "true".equals(sysparaService.find("withdraw_by_kyc").getValue())) {
			throw new BusinessException(401, "无权限");
		}
		
		double withdraw_by_high_kyc = Double.valueOf(sysparaService.find("withdraw_by_high_kyc").getValue());

		if (withdraw_by_high_kyc > 0 && withdraw.getVolume() > withdraw_by_high_kyc
				&& !(party_kycHighLevel.getStatus() == 2)) {
			throw new BusinessException(1, "请先通过高级认证");
		}

		if (!party.getWithdraw_authority()) {
			throw new BusinessException(1, "无权限");
		}
		if (!party.getEnabled()) {
			throw new BusinessException(506, "Your account has been frozen");
		}

		Wallet wallet = walletService.saveWalletByPartyId(withdraw.getPartyId());
		if (wallet.getMoney() < withdraw.getVolume()) {
			throw new BusinessException(1, "余额不足");
		}

		// 手续费(USDT)
		/**
		 * 提现手续费类型,fixed是单笔固定金额，rate是百分比，part是分段
		 */
		String withdraw_fee_type = sysparaService.find("withdraw_fee_type").getValue();

		/**
		 * fixed单笔固定金额 和 rate百分比 的手续费数值
		 */
		double withdraw_fee = Double.valueOf(sysparaService.find("withdraw_fee").getValue());

		double fee = 0;
		if ("fixed".equals(withdraw_fee_type)) {
			fee = withdraw_fee;
		}
		if ("rate".equals(withdraw_fee_type)) {
			withdraw_fee = Arith.div(withdraw_fee, 100);
			fee = Arith.mul(withdraw.getVolume(), withdraw_fee);
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
				if (withdraw.getVolume() <= part_amount) {
					fee = part_fee;
					break;
				}
				i++;
			}

		}

		String withdraw_limit = sysparaService.find("withdraw_limit").getValue();
		if (withdraw.getVolume() < Double.valueOf(withdraw_limit)) {
			throw new BusinessException(1, "提现不得小于限额");
		}
		String withdraw_limit_max = sysparaService.find("withdraw_limit_max").getValue();
		if (withdraw.getVolume() > Double.valueOf(withdraw_limit_max)) {
			throw new BusinessException(1, "提现不得大于限额");
		}

		/**
		 * 当日提现次数是否超过
		 */
		double withdraw_limit_num = Double.valueOf(sysparaService.find("withdraw_limit_num").getValue());
		List<Withdraw> withdraw_days = findAllByDate(withdraw.getPartyId().toString());
		if (withdraw_limit_num > 0 && withdraw_days != null) {
			if (withdraw_days.size() >= withdraw_limit_num) {
				throw new BusinessException(1, "当日可提现次数不足");
			}
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
			//
			String dateString = sdf.format(date);
			if (dateString.compareTo(withdraw_time[0]) < 0 || dateString.compareTo(withdraw_time[1]) > 0) {
				throw new BusinessException(1, "不在可提现时间内");
			}

		}
		/**
		 * 周提现额度限制开关
		 */
		boolean withdraw_week_limit_button = sysparaService.find("withdraw_week_limit_button").getBoolean();
		if (withdraw_week_limit_button) {
//			this.checkWithdrawLimit(party, party_kyc, party_kycHighLevel, withdraw.getVolume());
			this.checkWithdrawLimit(party, party_kyc, withdraw.getVolume());
		}
		/**
		 * 可提现差额开启 取party Withdraw_limit_amount 的可提现金 和剩余金额与流水中的最小值相加
		 * 流水为Userdate里的交割，合约，理财，矿池的交易量
		 */
		String withdraw_limit_open = sysparaService.find("withdraw_limit_open").getValue();

		if ("true".equals(withdraw_limit_open)) {

			// 提现限制流水开启后，提现判断用的用户当前流水是使用UserData表的当日流水1还是使用Party表里的用户当前流水2
			String withdraw_limit_open_use_type = sysparaService.find("withdraw_limit_open_use_type").getValue();
			// 当使用userdata流水提现时，提现限制流水是否加入永续合约流水1增加，2不增加
			String withdraw_limit_contract_or = sysparaService.find("withdraw_limit_contract_or").getValue();

			if ("1".equals(withdraw_limit_open_use_type)) {
				/**
				 * 还差多少可提现金额
				 */
				double fact_withdraw_amount = 0;
				/**
				 * 用户Party表里可提现金额参数 -----可为负数
				 */
				double party_withdraw = party.getWithdraw_limit_amount();
				/**
				 * usdt剩余余额
				 */
				// double last_usdt_amount = wallet.getMoney();
				/**
				 * userdata交易流水
				 */
				double userdata_turnover = 0;
//				Map<String, UserData> data_all = this.userDataService.getCache().get(withdraw.getPartyId());
				Map<String, UserData> data_all = this.userDataService.cacheByPartyId(withdraw.getPartyId().toString());
				if (data_all != null) {
					SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
					Date date_now = new Date();
					for (Entry<String, UserData> valueEntry : data_all.entrySet()) {
						UserData userdata = valueEntry.getValue();
						// 如果日期等于当天就赋值
						if (fmt.format(date_now).equals(fmt.format(userdata.getCreateTime()))) {
							/**
							 * 永续合约下单金额amount 理财买入金额finance_amount 币币exchange_amount 矿机下单金额miner_amount
							 * 交割合约下单金额furtures_amount
							 */

							// 当使用userdata流水提现时，提现限制流水是否加入永续合约流水1增加，2不增加
							double contract_amount = userdata.getAmount();
							if ("2".equals(withdraw_limit_contract_or)) {
								contract_amount = 0;
							}

							double amount_finance_amount = Arith.add(contract_amount, userdata.getFinance_amount());
//							币币交易流水不加入
							double exchange_amount_miner_amount = Arith.add(0, userdata.getMiner_amount());

							userdata_turnover = Arith.add(userdata.getFurtures_amount(),
									Arith.add(amount_finance_amount, exchange_amount_miner_amount));
						}
					}
				}

				double withdraw_limit_turnover_percent = Double
						.valueOf(sysparaService.find("withdraw_limit_turnover_percent").getValue());
				party_withdraw = Arith.mul(party_withdraw, withdraw_limit_turnover_percent);
				// 流水小于限额
				if (userdata_turnover < party_withdraw) {
					fact_withdraw_amount = Arith.sub(party_withdraw, userdata_turnover);
					throw new BusinessException(105, fact_withdraw_amount + "");
//					throw new BusinessException(1, "当前还需交易" + fact_withdraw_amount + ",才可提币");
				}
			}

			if ("2".equals(withdraw_limit_open_use_type)) {
				/**
				 * 还差多少可提现金额
				 */
				double fact_withdraw_amount = 0;
				/**
				 * 用户Party表里可提现金额参数 -----可为负数
				 */
				double party_withdraw = party.getWithdraw_limit_amount();
				/**
				 * userdata交易流水
				 */
				double userdata_turnover = party.getWithdraw_limit_now_amount();

				double withdraw_limit_turnover_percent = Double
						.valueOf(sysparaService.find("withdraw_limit_turnover_percent").getValue());
				party_withdraw = Arith.mul(party_withdraw, withdraw_limit_turnover_percent);
				// 流水小于限额
				if (userdata_turnover < party_withdraw) {
					fact_withdraw_amount = Arith.sub(party_withdraw, userdata_turnover);
					throw new BusinessException(105, fact_withdraw_amount + "");
				}
			}

		}

//		String withdraw_fee_type = sysparaService.find("withdraw_fee_type").getValue();
//		double withdraw_fee = Double.valueOf(((Syspara) sysparaService.find("withdraw_fee")).getValue());
//		DecimalFormat df = new DecimalFormat("#.##");
//		if ("fixed".equals(withdraw_fee_type)) {
//			fee = withdraw_fee;
//		} else {
//			fee = Double.valueOf(df.format(Arith.mul(withdraw.getVolume(), withdraw_fee)));
//
//		}

		withdraw.setAmount_fee(fee);

//		ExchangeRate exchangeRate = exchangeRateService.findBy(ExchangeRate.OUT, withdraw.getCurrency());
//
//		if (exchangeRate == null) {
//			throw new BusinessException("参数错误");
//		}
//
//		withdraw.setAmount(Double.valueOf(df.format(Arith.mul(withdraw.getVolume(), exchangeRate.getRata()))));
		withdraw.setAmount(Arith.sub(withdraw.getVolume(), fee));
		if (channel.indexOf("USDT") != -1) {
			withdraw.setMethod(channel);
		}
//		if ("USDT".equals(channel)) {
//			withdraw.setMethod("USDT");
//		} 
		else if ("OTC".equals(channel)) {
			throw new BusinessException(1, "渠道未开通");
//			if (StringUtils.isNullOrEmpty(method_id)) {
//				throw new BusinessException("请选择付款账号");
//			}
//			PaymentMethod paymentMethod = paymentMethodService.get(method_id);
//			if (paymentMethod == null) {
//				throw new BusinessException("请选择付款账号");
//			}
//			withdraw.setMethod(paymentMethod.getMethod());
//			withdraw.setAccount(paymentMethod.getAccount());
//			withdraw.setBank(paymentMethod.getBank());
//			withdraw.setDeposit_bank(paymentMethod.getDeposit_bank());
//			withdraw.setQdcode(paymentMethod.getQdcode());
//			withdraw.setUsername(withdraw.getUsername());
		} else {
			throw new BusinessException(1, "渠道未开通");
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

		double amount_before = wallet.getMoney();

		wallet.setMoney(Arith.sub(wallet.getMoney(), withdraw.getVolume()));
		walletService.update(wallet.getPartyId().toString(), Arith.sub(0, withdraw.getVolume()));

		withdraw.setId(ApplicationUtil.getCurrentTimeUUID());
		insertWithdraw(withdraw);

		/*
		 * 保存资金日志
		 */
		MoneyLog moneyLog = new MoneyLog();
		moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
		moneyLog.setAmount_before(amount_before);
		moneyLog.setAmount(Arith.sub(0, withdraw.getVolume()));
		moneyLog.setAmount_after(wallet.getMoney());

		moneyLog.setLog("提现订单[" + withdraw.getOrder_no() + "]");
		// moneyLog.setExtra(withdraw.getOrder_no());
		moneyLog.setPartyId(withdraw.getPartyId());
		moneyLog.setWallettype(Constants.WALLET);
		moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_WITHDRAW);

		moneyLogService.save(moneyLog);

		/*
		 * 保存资金日志
		 */
		WalletLog walletLog = new WalletLog();
		walletLog.setCategory("withdraw");
		walletLog.setPartyId(withdraw.getPartyId());
		walletLog.setOrder_no(withdraw.getOrder_no());
		walletLog.setStatus(withdraw.getSucceeded());
		walletLog.setAmount(withdraw.getVolume());
		walletLog.setWallettype(Constants.WALLET);
		walletLogService.save(walletLog);

//		double last_withdraw_amount = Arith.sub(party.getWithdraw_limit_amount(), withdraw.getVolume());
////		if (last_withdraw_amount < 0) {
////			last_withdraw_amount = 0;
////		}
//		party.setWithdraw_limit_amount(last_withdraw_amount);
//		partyService.update(party);
		tipService.saveTip(withdraw.getId().toString(), TipConstants.WITHDRAW);
	}

//	private void checkWithdrawLimit(Party party, Kyc kyc, KycHighLevel kycHighLevel, double withdrawVolumn) {
	private void checkWithdrawLimit(Party party, Kyc kyc, double withdrawVolumn) {
		
		double limit = 0d;
		// 特殊人员不受限制(只有在周提现限制开启后有效)
		String unLimitUid = sysparaService.find("withdraw_week_unlimit_uid").getValue();
		if (StringUtils.isNotEmpty(unLimitUid)) {
			String[] unLimitUisArr = unLimitUid.split(",");
			if (Arrays.asList(unLimitUisArr).contains(party.getUsercode())) {
				return;
			}
		}
//		if (kycHighLevel.getStatus() == 2) {
//			// 基础认证可提现额度
//			limit = sysparaService.find("withdraw_week_limit_kyc_high").getDouble();
//		} else 
		if (kyc.getStatus() == 2) {
			// 高级基础认证每周可提现额度
			limit = sysparaService.find("withdraw_week_limit_kyc").getDouble();
		}
		if (limit > 0) {
			/**
			 * 已用额度
			 */
			double weekWithdraw = weekWithdraw(party.getId().toString());
			if (Arith.add(weekWithdraw, withdrawVolumn) > limit) {
				throw new BusinessException(1, "提现不得大于限额");
			}
		}
	}

	/**
	 * 当周已使用额度
	 * 
	 * @param partyId
	 * @param withdrawVolumn
	 * @return
	 */
	public double weekWithdraw(String partyId) {
		Map<String, UserData> map = userDataService.cacheByPartyId(partyId);
		Date now = new Date();
		String endTime = DateUtils.getDateStr(new Date());
		String startTime = DateUtils.getDateStr(DateUtils.addDay(now, -6));
		// 一周内已用额度
		double withdrawMoney = withdrawMoney(map, startTime, endTime);
		return withdrawMoney;
//		double remain = Arith.sub(maxLimit, withdrawMoney);
//		if (Arith.add(withdrawMoney, withdrawVolumn) > maxLimit) {
//			throw new BusinessException("提现不得大于限额");
//		}
	}

	/**
	 * 时间范围内的充值总额
	 * 
	 * @param datas
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	private double withdrawMoney(Map<String, UserData> datas, String startTime, String endTime) {
		if (datas == null || datas.isEmpty())
			return 0;
		double userWithdraw = 0;
		for (Entry<String, UserData> valueEntry : datas.entrySet()) {
			UserData userdata = valueEntry.getValue();
			Date time = userdata.getCreateTime();
			if (!StringUtils.isNullOrEmpty(startTime)) {
				Date startDate = DateUtils.toDate(startTime, DateUtils.DF_yyyyMMdd);
				int intervalDays = DateUtils.getIntervalDaysByTwoDate(startDate, time);// 开始-数据时间
				if (intervalDays > 0) // 开始>数据时间 ，则过滤
					continue;
			}
			if (!StringUtils.isNullOrEmpty(endTime)) {
				Date endDate = DateUtils.toDate(endTime, DateUtils.DF_yyyyMMdd);
				int intervalDays = DateUtils.getIntervalDaysByTwoDate(endDate, time);// 结束-数据时间
				if (intervalDays < 0) // 结束<数据时间
					continue;
			}
			userWithdraw = Arith.add(userdata.getWithdraw(), userWithdraw);
		}

		return userWithdraw;
	}

	public void saveApplyOtherChannel(Withdraw withdraw, String symbol) {

		Party party = this.partyService.cachePartyBy(withdraw.getPartyId(), false);
		if (Constants.SECURITY_ROLE_TEST.equals(party.getRolename())) {
			throw new BusinessException("无权限");
		}
		Kyc party_kyc = this.kycService.get(withdraw.getPartyId().toString());

		KycHighLevel party_kycHighLevel = this.kycHighLevelService.get(withdraw.getPartyId().toString());

		if (!(party_kyc.getStatus() == 2) && "true".equals(sysparaService.find("withdraw_by_kyc").getValue())) {
			throw new BusinessException(401, "无权限");
		}
		
		double withdraw_by_high_kyc = Double.valueOf(sysparaService.find("withdraw_by_high_kyc").getValue());

		if (withdraw_by_high_kyc > 0 && withdraw.getVolume() > withdraw_by_high_kyc
				&& !(party_kycHighLevel.getStatus() == 2)) {
			throw new BusinessException(1, "请先通过高级认证");
		}

		if (!party.getWithdraw_authority()) {
			throw new BusinessException(1, "无权限");
		}
		if (!party.getEnabled()) {
			throw new BusinessException(506, "Your account has been frozen");
		}

		WalletExtend walletExtend = walletService.saveExtendByPara(party.getId(), symbol);
		if (walletExtend.getAmount() < withdraw.getVolume()) {
			throw new BusinessException(1, "余额不足");
		}

		String withdraw_limit = sysparaService.find("withdraw_limit_" + symbol).getValue();
		if (withdraw.getVolume() < Double.valueOf(withdraw_limit)) {
			throw new BusinessException(1, "提现不得小于限额");
		}
		String withdraw_limit_max = sysparaService.find("withdraw_limit_max").getValue();
		if (withdraw.getVolume() > Double.valueOf(withdraw_limit_max)) {
			throw new BusinessException(1, "提现不得大于限额");
		}

		/**
		 * 当日提现次数是否超过
		 */
		double withdraw_limit_num = Double.valueOf(sysparaService.find("withdraw_limit_num").getValue());
		List<Withdraw> withdraw_days = findAllByDate(withdraw.getPartyId().toString());
		if (withdraw_limit_num > 0 && withdraw_days != null) {
			if (withdraw_days.size() >= withdraw_limit_num) {
				throw new BusinessException(1, "当日可提现次数不足");
			}

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
			//
			String dateString = sdf.format(date);
			if (dateString.compareTo(withdraw_time[0]) < 0 || dateString.compareTo(withdraw_time[1]) > 0) {
				throw new BusinessException(1, "不在可提现时间内");
			}

		}

		/**
		 * 可提现差额开启 取party Withdraw_limit_amount 的可提现金 和剩余金额与流水中的最小值相加
		 * 流水为Userdate里的交割，合约，理财，矿池的交易量
		 */
		String withdraw_limit_open = sysparaService.find("withdraw_limit_open").getValue();
		if ("true".equals(withdraw_limit_open)) {

			// 提现限制流水开启后，提现判断用的用户当前流水是使用UserData表的当日流水1还是使用Party表里的用户当前流水2
			String withdraw_limit_open_use_type = sysparaService.find("withdraw_limit_open_use_type").getValue();
			// 当使用userdata流水提现时，提现限制流水是否加入永续合约流水1增加，2不增加
			String withdraw_limit_contract_or = sysparaService.find("withdraw_limit_contract_or").getValue();

			if ("1".equals(withdraw_limit_open_use_type)) {
				/**
				 * 还差多少可提现金额
				 */
				double fact_withdraw_amount = 0;
				/**
				 * 用户Party表里可提现金额参数 -----可为负数
				 */
				double party_withdraw = party.getWithdraw_limit_amount();
				/**
				 * usdt剩余余额
				 */
				// double last_usdt_amount = wallet.getMoney();
				/**
				 * userdata交易流水
				 */
				double userdata_turnover = 0;
//				Map<String, UserData> data_all = this.userDataService.getCache().get(withdraw.getPartyId());
				Map<String, UserData> data_all = this.userDataService.cacheByPartyId(withdraw.getPartyId().toString());
				if (data_all != null) {
					SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
					Date date_now = new Date();
					for (Entry<String, UserData> valueEntry : data_all.entrySet()) {
						UserData userdata = valueEntry.getValue();
						// 如果日期等于当天就赋值
						if (fmt.format(date_now).equals(fmt.format(userdata.getCreateTime()))) {
							/**
							 * 永续合约下单金额amount 理财买入金额finance_amount 币币exchange_amount 矿机下单金额miner_amount
							 * 交割合约下单金额furtures_amount
							 */
							// 当使用userdata流水提现时，提现限制流水是否加入永续合约流水1增加，2不增加
							double contract_amount = userdata.getAmount();
							if ("2".equals(withdraw_limit_contract_or)) {
								contract_amount = 0;
							}
							double amount_finance_amount = Arith.add(contract_amount, userdata.getFinance_amount());
//							币币交易流水不加入
							double exchange_amount_miner_amount = Arith.add(0, userdata.getMiner_amount());

							userdata_turnover = Arith.add(userdata.getFurtures_amount(),
									Arith.add(amount_finance_amount, exchange_amount_miner_amount));
						}
					}
				}
				double withdraw_limit_turnover_percent = Double
						.valueOf(sysparaService.find("withdraw_limit_turnover_percent").getValue());
				party_withdraw = Arith.mul(party_withdraw, withdraw_limit_turnover_percent);
				// 流水小于限额
				if (userdata_turnover < party_withdraw) {
					fact_withdraw_amount = Arith.sub(party_withdraw, userdata_turnover);
					throw new BusinessException(105, fact_withdraw_amount + "");
				}
			}

			if ("2".equals(withdraw_limit_open_use_type)) {
				/**
				 * 还差多少可提现金额
				 */
				double fact_withdraw_amount = 0;
				/**
				 * 用户Party表里可提现金额参数 -----可为负数
				 */
				double party_withdraw = party.getWithdraw_limit_amount();
				/**
				 * usdt剩余余额
				 */
				// double last_usdt_amount = wallet.getMoney();
				/**
				 * userdata交易流水
				 */
				double userdata_turnover = party.getWithdraw_limit_now_amount();

//				
				double withdraw_limit_turnover_percent = Double
						.valueOf(sysparaService.find("withdraw_limit_turnover_percent").getValue());
				party_withdraw = Arith.mul(party_withdraw, withdraw_limit_turnover_percent);
				// 流水小于限额
				if (userdata_turnover < party_withdraw) {
					fact_withdraw_amount = Arith.sub(party_withdraw, userdata_turnover);
					throw new BusinessException(105, fact_withdraw_amount + "");
				}
			}

		}

		double fee = getOtherChannelWithdrawFee(withdraw.getVolume());
		withdraw.setAmount_fee(fee);
		withdraw.setAmount(Arith.sub(withdraw.getVolume(), fee));

		if ("".equals(withdraw.getOrder_no()) || withdraw.getOrder_no() == null) {
			withdraw.setOrder_no(DateUtil.getToday("yyMMddHHmmss") + RandomUtil.getRandomNum(8));
		}

		withdraw.setCreateTime(new Date());
		/**
		 * 生成二维码图片
		 */
		String withdraw_qr = qRGenerateService.generateWithdraw(withdraw.getOrder_no(), withdraw.getAddress());

		withdraw.setQdcode(withdraw_qr);

		double amount_before = walletExtend.getAmount();

		walletService.updateExtend(walletExtend.getPartyId().toString(), symbol, Arith.sub(0, withdraw.getVolume()));
		withdraw.setId(ApplicationUtil.getCurrentTimeUUID());
		insertWithdraw(withdraw);

		/*
		 * 保存资金日志
		 */
		MoneyLog moneyLog = new MoneyLog();
		moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
		moneyLog.setAmount_before(amount_before);
		moneyLog.setAmount(Arith.sub(0, withdraw.getVolume()));
		moneyLog.setAmount_after(Arith.sub(amount_before, withdraw.getVolume()));

		moneyLog.setLog("提现订单[" + withdraw.getOrder_no() + "]");
		// moneyLog.setExtra(withdraw.getOrder_no());
		moneyLog.setPartyId(withdraw.getPartyId());
		moneyLog.setWallettype(symbol.toUpperCase());
		moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_WITHDRAW);

		moneyLogService.save(moneyLog);

		/*
		 * 保存资金日志
		 */
		WalletLog walletLog = new WalletLog();
		walletLog.setCategory("withdraw");
		walletLog.setPartyId(withdraw.getPartyId());
		walletLog.setOrder_no(withdraw.getOrder_no());
		walletLog.setStatus(withdraw.getSucceeded());
		walletLog.setAmount(withdraw.getVolume());
		walletLog.setWallettype(symbol.toUpperCase());
		walletLogService.save(walletLog);

		tipService.saveTip(withdraw.getId().toString(), TipConstants.WITHDRAW);

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
	public Withdraw findByOrderNo(String order_no) {
        List<Withdraw> list = jdbcTemplate.query("SELECT * FROM T_WITHDRAW_ORDER WHERE ORDER_NO=?", 
        		RecordObjectMapper.newInstance(Withdraw.class), order_no);
        if (null != list && list.size() > 0) {
            return list.get(0);
        }
        return null;
	}

	public boolean saveReject(Withdraw withdraw) {
		if (withdraw.getSucceeded() == 2 || withdraw.getSucceeded() == 1) {// 通过后不可驳回
			return false;
		}
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
			walletService.update(wallet.getPartyId().toString(),
					Arith.add(withdraw.getAmount(), withdraw.getAmount_fee()));

			/*
			 * 保存资金日志
			 */
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
			moneyLog.setAmount_before(amount_before);
			moneyLog.setAmount(Arith.add(withdraw.getAmount(), withdraw.getAmount_fee()));
			moneyLog.setAmount_after(
					Arith.add(amount_before, Arith.add(withdraw.getAmount(), withdraw.getAmount_fee())));

			moneyLog.setLog("驳回提现[" + withdraw.getOrder_no() + "]");
			// moneyLog.setExtra(withdraw.getOrder_no());
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
		this.updateWithdraw(withdraw);
		this.walletLogService.updateStatus(withdraw.getOrder_no(), withdraw.getSucceeded());

		return true;

	}

	public List<Withdraw> findAllByDate(String partyId) {
        List<Withdraw> list = jdbcTemplate.query("SELECT * FROM T_WITHDRAW_ORDER WHERE PARTY_ID = ? AND DateDiff(CREATE_TIME,NOW())=0 ", 
                RecordObjectMapper.newInstance(Withdraw.class), partyId);
        return list;
	}

	public List<Withdraw> findAllByStateAndPartyId(Integer state, String partyId) {
		// 查询所有的
		if (null == state) {
	        List<Withdraw> list = jdbcTemplate.query("SELECT * FROM T_WITHDRAW_ORDER WHERE PARTY_ID = ? ORDER BY CREATE_TIME DESC LIMIT 0, 10", 
	                RecordObjectMapper.newInstance(Withdraw.class), partyId);
	        return list;
		} else {
	        List<Withdraw> list = jdbcTemplate.query("SELECT * FROM T_WITHDRAW_ORDER WHERE PARTY_ID = ? AND SUCCEEDED=? ", 
	                RecordObjectMapper.newInstance(Withdraw.class), partyId, state);
	        return list;
		}
	}

	@Override
	public boolean saveSucceeded(Withdraw withdraw) {
		if (withdraw.getSucceeded() == 1) {
			return false;
		}
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
		this.userDataService.saveWithdrawHandle(withdraw.getPartyId(), withdraw.getAmount(), withdraw.getAmount_fee(),
				symbol);

		this.updateWithdraw(withdraw);

		return true;
	}
	
    public void insertWithdraw(Withdraw withdraw) {
        Object[] jdbcParams = ApplicationUtil.getInsertStatement(withdraw);
        String insertUserSql = (String)jdbcParams[0];
        Object[] sqlParameters = (Object[])jdbcParams[1];
        jdbcTemplate.update(insertUserSql, sqlParameters);
    }
	
    public void updateWithdraw(Withdraw withdraw) {
        Object[] jdbcParams = ApplicationUtil.getUpdateStatement(withdraw, "WHERE UUID=?", new Object[] {withdraw.getId()});
        String updateUserSql = (String)jdbcParams[0];
        Object[] sqlParameters = (Object[])jdbcParams[1];
        jdbcTemplate.update(updateUserSql, sqlParameters);
    }

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
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
	
	public void setUserDataService(UserDataService userDataService) {
		this.userDataService = userDataService;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setKycService(KycService kycService) {
		this.kycService = kycService;
	}

	public void setKycHighLevelService(KycHighLevelService kycHighLevelService) {
		this.kycHighLevelService = kycHighLevelService;
	}

	public void setTipService(TipService tipService) {
		this.tipService = tipService;
	}

}
