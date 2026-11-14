package project.blockchain.internal;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.web.ApplicationUtil;
import project.Constants;
import project.blockchain.ChannelBlockchain;
import project.blockchain.ChannelBlockchainService;
import project.blockchain.RechargeBlockchain;
import project.blockchain.RechargeBlockchainService;
import project.bonus.RechargeBonusService;
import project.data.DataService;
import project.data.model.Realtime;
import project.log.Log;
import project.log.LogService;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.party.PartyService;
import project.party.model.Party;
import project.syspara.Syspara;
import project.syspara.SysparaService;
import project.tip.TipConstants;
import project.tip.TipService;
import project.user.UserDataService;
import project.wallet.Wallet;
import project.wallet.WalletExtend;
import project.wallet.WalletLog;
import project.wallet.WalletLogService;
import project.wallet.WalletService;
import project.wallet.rate.ExchangeRateService;
import security.SecUser;
import security.internal.SecUserService;
import util.DateUtil;
import util.RandomUtil;

public class RechargeBlockchainServiceImpl implements RechargeBlockchainService {

	protected JdbcTemplate jdbcTemplate;
	protected UserDataService userDataService;
	protected WalletService walletService;
	protected MoneyLogService moneyLogService;
	protected ExchangeRateService exchangeRateService;
	protected WalletLogService walletLogService;
	protected RechargeBonusService rechargeBonusService;
	protected DataService dataService;
	protected LogService logService;
	protected SecUserService secUserService;
	protected PartyService partyService;
	protected SysparaService sysparaService;
	protected ChannelBlockchainService channelBlockchainService;
	protected TipService tipService;

	@Override
	public void save(RechargeBlockchain recharge) {

		List<RechargeBlockchain> oreders = this.findByPartyIdAndSucceeded(recharge.getPartyId(), 0);
		double recharge_only_one = Double.valueOf(sysparaService.find("recharge_only_one").getValue());
		if (oreders != null && recharge_only_one==1) {
			throw new BusinessException("提交失败，当前有未处理订单");
		}
		
		if (!"ETH".equals(recharge.getSymbol().toUpperCase())) {
			JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
			List<HashMap> list=jdbcTemplate.query("SELECT * FROM T_PARTY_BLOCKCHAIN WHERE ADDRESS =?",RecordObjectMapper.newInstance(HashMap.class),recharge.getChannel_address());
			if(ObjectUtils.isEmpty(list)) {
				ChannelBlockchain channel = channelBlockchainService.findByNameAndCoinAndAdd(recharge.getBlockchain_name(),
						recharge.getSymbol(), recharge.getChannel_address());

				if (channel == null || !recharge.getSymbol().toUpperCase().equals(channel.getCoin())) {
					throw new BusinessException("充值链错误");
				}
			}
		}

		DecimalFormat df = new DecimalFormat("#.##");
//		amount = Double.valueOf(df.format(amount));
		double recharge_limit_min = Double.valueOf(sysparaService.find("recharge_limit_min").getValue());
		double recharge_limit_max = Double.valueOf(sysparaService.find("recharge_limit_max").getValue());
		if ("usdt".equals(recharge.getSymbol())||"usdc".equals(recharge.getSymbol())) {
			if (recharge.getVolume() < recharge_limit_min) {
				throw new BusinessException("充值价值不得小于最小限额");
			}

			if (recharge.getVolume() > recharge_limit_max) {
				throw new BusinessException("充值价值不得大于最大限额");
			}

		} else {

			List<Realtime> realtime_list = this.dataService.realtime(recharge.getSymbol());
			Realtime realtime = null;
			if (realtime_list.size() > 0) {
				realtime = realtime_list.get(0);
			} else {
				throw new BusinessException("系统错误，请稍后重试");
			}
			double transfer_usdt = Arith.mul(realtime.getClose(), recharge.getVolume());// 对应usdt价格
			if (transfer_usdt < recharge_limit_min) {

				throw new BusinessException("充值价值不得小于最小限额");
			}

			if (transfer_usdt > recharge_limit_max) {
				throw new BusinessException("充值价值不得大于最大限额");
			}

		}

		recharge.setCreated(new Date());
		if ("".equals(recharge.getOrder_no()) || recharge.getOrder_no() == null) {
			recharge.setOrder_no(DateUtil.getToday("yyMMddHHmmss") + RandomUtil.getRandomNum(8));
		}

		recharge.setId(ApplicationUtil.getCurrentTimeUUID());
		this.insertRechargeBlockchain(recharge);

		/*
		 * 保存资金日志
		 */
		WalletLog walletLog = new WalletLog();
		walletLog.setCategory(Constants.MONEYLOG_CATEGORY_RECHARGE);
		walletLog.setPartyId(recharge.getPartyId());
		walletLog.setOrder_no(recharge.getOrder_no());
		walletLog.setStatus(recharge.getSucceeded());

		walletLog.setAmount(recharge.getVolume());
		walletLog.setWallettype(recharge.getSymbol());
		walletLog.setCreateTime(new Date());
		walletLogService.save(walletLog);

		tipService.saveTip(recharge.getId().toString(), TipConstants.RECHARGE_BLOCKCHAIN);
	}

	@Override
	public void save_api(RechargeBlockchain recharge) {

		if (!"ETH".equals(recharge.getSymbol().toUpperCase())) {
			ChannelBlockchain channel = channelBlockchainService.findByNameAndCoinAndAdd(recharge.getBlockchain_name(),
					recharge.getSymbol(), recharge.getChannel_address());

			if (channel == null || !recharge.getSymbol().toUpperCase().equals(channel.getCoin())) {
				throw new BusinessException("充值链错误");
			}
		}

		double recharge_limit_min = Double.valueOf(sysparaService.find("recharge_limit_min").getValue());
		double recharge_limit_max = Double.valueOf(sysparaService.find("recharge_limit_max").getValue());
		if ("usdt".equals(recharge.getSymbol())) {
			if (recharge.getVolume() < recharge_limit_min) {
				throw new BusinessException("充值价值不得小于最小限额");
			}

			if (recharge.getVolume() > recharge_limit_max) {
				throw new BusinessException("充值价值不得大于最大限额");
			}

		} else {

			List<Realtime> realtime_list = this.dataService.realtime(recharge.getSymbol());
			Realtime realtime = null;
			if (realtime_list.size() > 0) {
				realtime = realtime_list.get(0);
			} else {
				throw new BusinessException("系统错误，请稍后重试");
			}
			double transfer_usdt = Arith.mul(realtime.getClose(), recharge.getVolume());// 对应usdt价格
			if (transfer_usdt < recharge_limit_min) {
				throw new BusinessException("充值价值不得小于最小限额");
			}

			if (transfer_usdt > recharge_limit_max) {
				throw new BusinessException("充值价值不得大于最大限额");
			}

		}
		recharge.setCreated(new Date());
		if ("".equals(recharge.getOrder_no()) || recharge.getOrder_no() == null) {
			recharge.setOrder_no(DateUtil.getToday("yyMMddHHmmss") + RandomUtil.getRandomNum(8));
		}

		/*
		 * 保存资金日志
		 */
		WalletLog walletLog = new WalletLog();
		walletLog.setCategory(Constants.MONEYLOG_CATEGORY_RECHARGE);
		walletLog.setPartyId(recharge.getPartyId());
		walletLog.setOrder_no(recharge.getOrder_no());

		walletLog.setAmount(recharge.getVolume());
		walletLog.setWallettype(recharge.getSymbol());
		walletLog.setCreateTime(new Date());

		Date date = new Date();
		recharge.setReviewTime(date);
		recharge.setSucceeded(1);

		/**
		 * 如果是usdt则加入wallet，否则寻找walletExtend里相同币种
		 */

		Party party = this.partyService.cachePartyBy(recharge.getPartyId(), false);

		Syspara user_recom_bonus_open = sysparaService.find("user_recom_bonus_open");

		if ("usdt".equals(recharge.getSymbol())) {
			double amount = recharge.getVolume();
			Wallet wallet = new Wallet();
			wallet = walletService.saveWalletByPartyId(recharge.getPartyId());

			double amount_before = wallet.getMoney();

			walletService.update(wallet.getPartyId().toString(), amount);

			/*
			 * 保存资金日志
			 */
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
			moneyLog.setAmount_before(amount_before);
			moneyLog.setAmount(amount);
			moneyLog.setAmount_after(Arith.add(wallet.getMoney(), amount));

			moneyLog.setLog("使用区块链三方接口充值订单[" + recharge.getOrder_no() + "]");
			moneyLog.setPartyId(recharge.getPartyId());
			moneyLog.setWallettype(Constants.WALLET);
			moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_RECHARGE);
			moneyLog.setCreateTime(new Date());
			moneyLogService.save(moneyLog);

			walletLog.setStatus(recharge.getSucceeded());
			walletLogService.save(walletLog);

			/**
			 * 创建订单
			 */
			recharge.setId(ApplicationUtil.getCurrentTimeUUID());
			this.insertRechargeBlockchain(recharge);
			/**
			 * 给他的代理添加充值记录
			 */
			userDataService.saveRechargeHandle(recharge.getPartyId(), recharge.getVolume(), recharge.getSymbol());

			/**
			 * 若已开启充值奖励 ，则充值到账后给他的代理用户添加奖金
			 */
			if ("true".equals(user_recom_bonus_open.getValue())) {
				rechargeBonusService.saveBounsHandle(recharge, 1);
			}

			/**
			 * 充值到账后给他增加提现流水限制金额 充值到账后，当前流水大于提现限制流水时是否重置提现限制流水并将Party表里的当前流水设置清零，1不重置，2重置
			 */

			String recharge_sucess_reset_withdraw = this.sysparaService.find("recharge_sucess_reset_withdraw")
					.getValue();
			if ("1".equals(recharge_sucess_reset_withdraw)) {
				party.setWithdraw_limit_amount(Arith.add(party.getWithdraw_limit_amount(), amount));
				if (party.getWithdraw_limit_now_amount() > party.getWithdraw_limit_amount()) {
					party.setWithdraw_limit_now_amount(0);
				}
			}
			if ("2".equals(recharge_sucess_reset_withdraw)) {
				double withdraw_limit_turnover_percent = Double
						.valueOf(sysparaService.find("withdraw_limit_turnover_percent").getValue());
				double party_withdraw = Arith.mul(party.getWithdraw_limit_amount(), withdraw_limit_turnover_percent);

				if (party.getWithdraw_limit_now_amount() >= party_withdraw) {
					party.setWithdraw_limit_amount(amount);
					party.setWithdraw_limit_now_amount(0);
				} else {
					party.setWithdraw_limit_amount(Arith.add(party.getWithdraw_limit_amount(), amount));
				}
			}

			partyService.update(party);

		} else {

			List<Realtime> realtime_list = this.dataService.realtime(recharge.getSymbol());
			Realtime realtime = null;
			if (realtime_list.size() > 0) {
				realtime = realtime_list.get(0);
			} else {
				throw new BusinessException("系统错误，请稍后重试");
			}
			double transfer_usdt = realtime.getClose();// 对应usdt价格

			WalletExtend walletExtend = new WalletExtend();
			walletExtend = walletService.saveExtendByPara(recharge.getPartyId(), recharge.getSymbol());

			double volume = recharge.getVolume();
			double amount_before = walletExtend.getAmount();

			walletService.updateExtend(walletExtend.getPartyId().toString(), walletExtend.getWallettype(), volume);

			/**
			 * 创建订单
			 */
			recharge.setId(ApplicationUtil.getCurrentTimeUUID());
			this.insertRechargeBlockchain(recharge);
			/**
			 * 币种usdt价格= 币种价格×充值数量
			 */
			double usdt_amount = Arith.mul(volume, transfer_usdt);
			/*
			 * 保存资金日志
			 */
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
			moneyLog.setAmount_before(amount_before);
			moneyLog.setAmount(volume);
			moneyLog.setAmount_after(Arith.add(walletExtend.getAmount(), volume));

			moneyLog.setLog("使用区块链三方接口充值订单[" + recharge.getOrder_no() + "]");
			moneyLog.setPartyId(recharge.getPartyId());
			moneyLog.setWallettype(recharge.getSymbol());
			moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_RECHARGE);
			moneyLog.setCreateTime(new Date());
			moneyLogService.save(moneyLog);

			walletLog.setStatus(recharge.getSucceeded());
			walletLogService.save(walletLog);
			/**
			 * 给他的代理添加充值记录
			 */
			userDataService.saveRechargeHandle(recharge.getPartyId(), recharge.getVolume(), recharge.getSymbol());
			/**
			 * 买币-币冲充值其他非usdt币时使用
			 */
//			userDataService.saveBuy(recharge.getPartyId(), recharge.getSymbol(), recharge.getVolume());

			/**
			 * 充值到账后给他的代理用户添加奖金
			 */
			if ("true".equals(user_recom_bonus_open.getValue())) {
				rechargeBonusService.saveBounsHandle(recharge, transfer_usdt);
			}

			/**
			 * 充值到账后给他增加提现流水限制金额
			 */

			/**
			 * 充值到账后给他增加提现流水限制金额 充值到账后，当前流水大于提现限制流水时是否重置提现限制流水并将Party表里的当前流水设置清零，1不重置，2重置
			 */
			String recharge_sucess_reset_withdraw = this.sysparaService.find("recharge_sucess_reset_withdraw")
					.getValue();
			if ("1".equals(recharge_sucess_reset_withdraw)) {
				party.setWithdraw_limit_amount(Arith.add(party.getWithdraw_limit_amount(), usdt_amount));
				if (party.getWithdraw_limit_now_amount() > party.getWithdraw_limit_amount()) {
					party.setWithdraw_limit_now_amount(0);
				}
			}
			if ("2".equals(recharge_sucess_reset_withdraw)) {
				double withdraw_limit_turnover_percent = Double
						.valueOf(sysparaService.find("withdraw_limit_turnover_percent").getValue());
				double party_withdraw = Arith.mul(party.getWithdraw_limit_amount(), withdraw_limit_turnover_percent);

				if (party.getWithdraw_limit_now_amount() >= party_withdraw) {
					party.setWithdraw_limit_amount(usdt_amount);
					party.setWithdraw_limit_now_amount(0);
				} else {
					party.setWithdraw_limit_amount(Arith.add(party.getWithdraw_limit_amount(), usdt_amount));
				}
			}

			partyService.update(party);

		}

		Log log = new Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		log.setExtra(recharge.getOrder_no());
		log.setUsername(party.getUsername());
		log.setPartyId(recharge.getPartyId());
		log.setCreateTime(new Date());
		log.setLog("使用区块链三方接口到账一笔充值订单。订单号[" + recharge.getOrder_no() + "]。");

		logService.saveSync(log);
		tipService.deleteTip(recharge.getId().toString());

	}
	
	/**
	 * 根据ID获取充值订单
	 */
	public RechargeBlockchain get(String id) {
        List<RechargeBlockchain> list = jdbcTemplate.query("SELECT * FROM T_RECHARGE_BLOCKCHAIN_ORDER WHERE UUID=?", RecordObjectMapper.newInstance(RechargeBlockchain.class), id);
        if (null != list && list.size() > 0) {
            return list.get(0);
        }
        return null;
	}
	
	@Override
	public RechargeBlockchain findByOrderNo(String order_no) {
        List<RechargeBlockchain> list = jdbcTemplate.query("SELECT * FROM T_RECHARGE_BLOCKCHAIN_ORDER WHERE ORDER_NO=?", 
        		RecordObjectMapper.newInstance(RechargeBlockchain.class), order_no);
        if (null != list && list.size() > 0) {
            return list.get(0);
        }
        return null;
	}

	public List<RechargeBlockchain> findByPartyIdAndToday(Serializable partyId) {
        List<RechargeBlockchain> list = jdbcTemplate.query("SELECT * FROM T_RECHARGE_BLOCKCHAIN_ORDER WHERE PARTY_ID=? AND DateDiff(CREATED,NOW())=0 ", 
        		RecordObjectMapper.newInstance(RechargeBlockchain.class), partyId);
        if (list.size() > 0) {
            return list;
        }
        return null;
	}

	public List<RechargeBlockchain> findBySucceededAndDay(int succeeded, Integer days) {
        List<RechargeBlockchain> list = jdbcTemplate.query("SELECT * FROM T_RECHARGE_BLOCKCHAIN_ORDER WHERE SUCCEEDED=? AND DateDiff(CREATED,NOW())=-1 ", 
        		RecordObjectMapper.newInstance(RechargeBlockchain.class), succeeded);
        if (list.size() > 0) {
            return list;
        }
        return null;
	}

	public List<RechargeBlockchain> findByPartyIdAndSucceeded(Serializable partyId, int succeeded) {
        List<RechargeBlockchain> list = jdbcTemplate.query("SELECT * FROM T_RECHARGE_BLOCKCHAIN_ORDER WHERE PARTY_ID=? AND SUCCEEDED=? ", 
        		RecordObjectMapper.newInstance(RechargeBlockchain.class), partyId, succeeded);
        if (list.size() > 0) {
            return list;
        }
        return null;
	}

	@Override
	public boolean saveSucceeded(String id, String operator, double success_amount) {
		// RechargeBlockchain recharge = this.findByOrderNo(order_no);
		RechargeBlockchain recharge = this.get(id);
		String order_no = recharge.getOrder_no();
		if (recharge.getSucceeded() == 1) {
			return false;
		}
		
		recharge.setReviewTime(new Date());
		recharge.setSucceeded(1);

		WalletLog walletLog = walletLogService.find(Constants.MONEYLOG_CATEGORY_RECHARGE, recharge.getOrder_no());
		SecUser secUser = secUserService.findUserByPartyId(recharge.getPartyId());
		if (success_amount != recharge.getVolume()) {
			Log log = new Log();
			log.setCategory(Constants.LOG_CATEGORY_OPERATION);
			log.setExtra(order_no);
			log.setOperator(operator);
			log.setUsername(secUser.getUsername());
			log.setPartyId(recharge.getPartyId());
			log.setCreateTime(new Date());
			log.setLog("管理员修改充值数量，原充值数量[" 
			+ recharge.getVolume() + "],修改后充值数量[" + success_amount + "]。订单号[" + order_no+ "]。");

			logService.saveSync(log);
			walletLog.setAmount(success_amount);
			recharge.setVolume(success_amount);
		}

		/**
		 * 如果是usdt则加入wallet，否则寻找walletExtend里相同币种
		 */

		Party party = this.partyService.cachePartyBy(recharge.getPartyId(), false);

		Syspara user_recom_bonus_open = sysparaService.find("user_recom_bonus_open");

		if ("usdt".equals(recharge.getSymbol())) {
			double amount = recharge.getVolume();

			Wallet wallet = new Wallet();
			wallet = walletService.saveWalletByPartyId(recharge.getPartyId());
			
			double amount_before = wallet.getMoney();
			
			walletService.update(wallet.getPartyId().toString(), amount);

			// 保存资金日志
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
			moneyLog.setAmount_before(amount_before);
			moneyLog.setAmount(amount);
			moneyLog.setAmount_after(Arith.add(wallet.getMoney(), amount));
			moneyLog.setLog("充值订单[" + recharge.getOrder_no() + "]");
			moneyLog.setPartyId(recharge.getPartyId());
			moneyLog.setWallettype(Constants.WALLET);
			moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_RECHARGE);
			moneyLog.setCreateTime(new Date());
			moneyLogService.save(moneyLog);

			walletLog.setStatus(recharge.getSucceeded());
			walletLogService.update(walletLog);

			this.updateRechargeBlockchain(recharge);
			/**
			 * 给他的代理添加充值记录
			 */
			userDataService.saveRechargeHandle(recharge.getPartyId(), recharge.getVolume(), recharge.getSymbol());

			/**
			 * 若已开启充值奖励 ，则充值到账后给他的代理用户添加奖金
			 */
			if ("true".equals(user_recom_bonus_open.getValue())) {
				rechargeBonusService.saveBounsHandle(recharge, 1);
			}

			// 充值到账后给他增加提现流水限制金额 充值到账后，当前流水大于提现限制流水时是否重置提现限制流水并将Party表里的当前流水设置清零，
			// 1不重置，2重置
			String recharge_sucess_reset_withdraw = this.sysparaService.find("recharge_sucess_reset_withdraw").getValue();
			if ("1".equals(recharge_sucess_reset_withdraw)) {
				party.setWithdraw_limit_amount(Arith.add(party.getWithdraw_limit_amount(), amount));
				if (party.getWithdraw_limit_now_amount() > party.getWithdraw_limit_amount()) {
					party.setWithdraw_limit_now_amount(0);
				}
			}
			if ("2".equals(recharge_sucess_reset_withdraw)) {
				double withdraw_limit_turnover_percent = Double
						.valueOf(sysparaService.find("withdraw_limit_turnover_percent").getValue());
				double party_withdraw = Arith.mul(party.getWithdraw_limit_amount(), withdraw_limit_turnover_percent);

				if (party.getWithdraw_limit_now_amount() >= party_withdraw) {
					party.setWithdraw_limit_amount(amount);
					party.setWithdraw_limit_now_amount(0);
				} else {
					party.setWithdraw_limit_amount(Arith.add(party.getWithdraw_limit_amount(), amount));
				}
			}

			partyService.update(party);

		} else {

			List<Realtime> realtime_list = this.dataService.realtime(recharge.getSymbol());
			Realtime realtime = null;
			if (realtime_list.size() > 0) {
				realtime = realtime_list.get(0);
			} else {
				throw new BusinessException("系统错误，请稍后重试");
			}
			// 对应usdt价格
			double transfer_usdt = realtime.getClose();

			WalletExtend walletExtend = new WalletExtend();
			walletExtend = walletService.saveExtendByPara(recharge.getPartyId(), recharge.getSymbol());

			double volume = recharge.getVolume();

			double amount_before = walletExtend.getAmount();

		    // walletExtend = walletService.saveWalletExtendByParaAndUpdate(String.valueOf(recharge.getPartyId()), recharge.getSymbol(), volume);
			walletService.updateExtend(walletExtend.getPartyId().toString(), walletExtend.getWallettype(), volume);
			this.updateRechargeBlockchain(recharge);
			
			// 币种usdt价格= 币种价格×充值数量
			double usdt_amount = Arith.mul(volume, transfer_usdt);
			
			// 保存资金日志
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
			moneyLog.setAmount_before(amount_before);
			moneyLog.setAmount(volume);
			moneyLog.setAmount_after(Arith.add(walletExtend.getAmount(), volume));

			moneyLog.setLog("充值订单[" + recharge.getOrder_no() + "]");
			moneyLog.setPartyId(recharge.getPartyId());
			moneyLog.setWallettype(recharge.getSymbol());
			moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_RECHARGE);
			moneyLog.setCreateTime(new Date());
			moneyLogService.save(moneyLog);

			walletLog.setStatus(recharge.getSucceeded());
			walletLogService.update(walletLog);
			/**
			 * 给他的代理添加充值记录
			 */
			userDataService.saveRechargeHandle(recharge.getPartyId(), recharge.getVolume(), recharge.getSymbol());
			/**
			 * 买币-币冲充值其他非usdt币时使用
			 */
//			userDataService.saveBuy(recharge.getPartyId(), recharge.getSymbol(), recharge.getVolume());

			/**
			 * 充值到账后给他的代理用户添加奖金
			 */
			if ("true".equals(user_recom_bonus_open.getValue())) {
				rechargeBonusService.saveBounsHandle(recharge, transfer_usdt);
			}

			/**
			 * 充值到账后给他增加提现流水限制金额
			 */

			/**
			 * 充值到账后给他增加提现流水限制金额 充值到账后，当前流水大于提现限制流水时是否重置提现限制流水并将Party表里的当前流水设置清零，1不重置，2重置
			 */
			String recharge_sucess_reset_withdraw = this.sysparaService.find("recharge_sucess_reset_withdraw")
					.getValue();
			if ("1".equals(recharge_sucess_reset_withdraw)) {
				party.setWithdraw_limit_amount(Arith.add(party.getWithdraw_limit_amount(), usdt_amount));
				if (party.getWithdraw_limit_now_amount() > party.getWithdraw_limit_amount()) {
					party.setWithdraw_limit_now_amount(0);
				}
			}
			if ("2".equals(recharge_sucess_reset_withdraw)) {
				double withdraw_limit_turnover_percent = Double
						.valueOf(sysparaService.find("withdraw_limit_turnover_percent").getValue());
				double party_withdraw = Arith.mul(party.getWithdraw_limit_amount(), withdraw_limit_turnover_percent);

				if (party.getWithdraw_limit_now_amount() >= party_withdraw) {
					party.setWithdraw_limit_amount(usdt_amount);
					party.setWithdraw_limit_now_amount(0);
				} else {
					party.setWithdraw_limit_amount(Arith.add(party.getWithdraw_limit_amount(), usdt_amount));
				}
			}

			partyService.update(party);

		}

		Log log = new Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		log.setExtra(order_no);
		log.setOperator(operator);
		log.setUsername(secUser.getUsername());
		log.setPartyId(recharge.getPartyId());
		log.setCreateTime(new Date());
		log.setLog("手动到账一笔充值订单。订单号[" + order_no + "]。");

		logService.saveSync(log);
		tipService.deleteTip(recharge.getId().toString());
		return true;

	}

	public boolean saveReject(RechargeBlockchain recharge) {
		// 通过后不可驳回
		if (recharge.getSucceeded() == 2 || recharge.getSucceeded() == 1) {
			return false;
		}

		recharge.setSucceeded(2);

		this.updateRechargeBlockchain(recharge);
		tipService.deleteTip(recharge.getId().toString());

		WalletLog walletLog = walletLogService.find(Constants.MONEYLOG_CATEGORY_RECHARGE, recharge.getOrder_no());
		walletLog.setStatus(recharge.getSucceeded());
		walletLogService.update(walletLog);

		return true;

	}
	
    public void insertRechargeBlockchain(RechargeBlockchain rechargeBlockchain) {
        Object[] jdbcParams = ApplicationUtil.getInsertStatement(rechargeBlockchain);
        String insertUserSql = (String)jdbcParams[0];
        Object[] sqlParameters = (Object[])jdbcParams[1];
        jdbcTemplate.update(insertUserSql, sqlParameters);
    }

    public void updateRechargeBlockchain(RechargeBlockchain rechargeBlockchain) {
        Object[] jdbcParams = ApplicationUtil.getUpdateStatement(rechargeBlockchain, "WHERE UUID=?", new Object[] {rechargeBlockchain.getId()});
        String updateUserSql = (String)jdbcParams[0];
        Object[] sqlParameters = (Object[])jdbcParams[1];
        jdbcTemplate.update(updateUserSql, sqlParameters);
    }
    
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setUserDataService(UserDataService userDataService) {
		this.userDataService = userDataService;
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

	public void setRechargeBonusService(RechargeBonusService rechargeBonusService) {
		this.rechargeBonusService = rechargeBonusService;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setChannelBlockchainService(ChannelBlockchainService channelBlockchainService) {
		this.channelBlockchainService = channelBlockchainService;
	}

	public void setTipService(TipService tipService) {
		this.tipService = tipService;
	}

}
