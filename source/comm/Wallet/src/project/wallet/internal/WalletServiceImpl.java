package project.wallet.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.StringUtils;
import kernel.util.ThreadUtils;
import kernel.util.UUIDGenerator;
import kernel.web.ApplicationUtil;
import project.Constants;
import project.data.DataService;
import project.data.model.Realtime;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.party.PartyService;
import project.party.model.Party;
import project.redis.RedisHandler;
import project.syspara.SysparaService;
import project.user.UserDataService;
import project.wallet.Wallet;
import project.wallet.WalletExtend;
import project.wallet.WalletRedisKeys;
import project.wallet.WalletService;
import project.wallet.consumer.WalletExtendMessage;
import project.wallet.consumer.WalletMessage;

public class WalletServiceImpl implements WalletService {
	
	private DataService dataService;
	
	private RedisHandler redisHandler;
	
	private PartyService partyService;
	
	private JdbcTemplate jdbcTemplate;
	
	private SysparaService sysparaService;
	
	private MoneyLogService moneyLogService;
	
	private UserDataService userDataService;
	
	private static final Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);

	@Override
	public Wallet saveWalletByPartyId(Serializable partyId) {
		Wallet wallet = (Wallet) redisHandler.get(WalletRedisKeys.WALLET_PARTY_ID + partyId.toString());
		if (wallet != null) {
			return wallet;
		} else {
			wallet = new Wallet();
			wallet.setPartyId(partyId);
			save(wallet);
			return wallet;
		}
	}

	@Override
	public void save(Wallet entity) {
		entity.setTimestamp(new Date());
        Object[] jdbcParams = ApplicationUtil.getInsertStatement(entity);
        String insertUserSql = (String)jdbcParams[0];
        Object[] sqlParameters = (Object[])jdbcParams[1];
        jdbcTemplate.update(insertUserSql, sqlParameters);
		redisHandler.setSync(WalletRedisKeys.WALLET_PARTY_ID + entity.getPartyId().toString(), entity);
	}

	@Override
	public void update(String partyId, double amount) {
		Wallet wallet = (Wallet) redisHandler.get(WalletRedisKeys.WALLET_PARTY_ID + partyId.toString());
		wallet.setMoney(Arith.add(wallet.getMoney(), amount));
		redisHandler.setSync(WalletRedisKeys.WALLET_PARTY_ID + wallet.getPartyId().toString(), wallet);
		redisHandler.pushAsyn(WalletRedisKeys.WALLET_QUEUE_UPDATE, new WalletMessage(partyId, amount));
	}

	@Override
	public WalletExtend saveExtendByPara(Serializable partyId, String wallettype) {
		if (StringUtils.isEmptyString(wallettype) || partyId == null || StringUtils.isEmptyString(partyId.toString())) {
			logger.error("saveExtendByPara fail,partyId:{},wallettype:{}", new Object[] { partyId, wallettype });
			throw new RuntimeException("saveExtendByPara fail");
		}
		
		WalletExtend walletExtend = (WalletExtend) redisHandler.get(WalletRedisKeys.WALLET_EXTEND_PARTY_ID + partyId.toString() + wallettype);
		if (null!=walletExtend) return walletExtend;
		
		List<WalletExtend> list=jdbcTemplate.query("SELECT "
				+ "UUID id,"
				+ "PARTY_ID partyId,"
				+ "WALLETTYPE wallettype,"
				+ "AMOUNT amount,"
				+ "LOCK_AMOUNT lock_amount,"
				+ "FREEZE_AMOUNT freeze_amount,"
				+ "FROZEN_AMOUNT frozenAmount "
				+ "FROM T_WALLET_EXTEND WHERE PARTY_ID=? AND WALLETTYPE=?", 
				BeanPropertyRowMapper.newInstance(WalletExtend.class), partyId,wallettype);
		
		if(null!=list && !list.isEmpty()){
			if(list.size()>1) {
				logger.error("duplicate user:{} walletType:{} exists in the table T_WALLET_EXTEND", new Object[] { partyId, wallettype });
				throw new RuntimeException("duplicate user data exists in the wallet table T_WALLET_EXTEND");
			}
			WalletExtend we=list.get(0);
			redisHandler.setSync(WalletRedisKeys.WALLET_EXTEND_PARTY_ID + partyId.toString() + wallettype,we);
			return we;
		}
		
		walletExtend = new WalletExtend();
		walletExtend.setPartyId(partyId);
		walletExtend.setWallettype(wallettype);
		save(walletExtend);
		
		ThreadUtils.sleep(10);
		
		return walletExtend;
	}

	@Override
	public void save(WalletExtend entity) {
		entity.setId(UUIDGenerator.getUUID());
		final String sql = "INSERT INTO T_WALLET_EXTEND(UUID,PARTY_ID,WALLETTYPE,AMOUNT) VALUES('"+entity.getId().toString()+"','"+entity.getPartyId().toString()+"','"+entity.getWallettype()+"','0')";
		FutureTask<Object> future = new FutureTask<Object>(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				int update = jdbcTemplate.update(sql);
				return update;
			}
		});
		Thread thread = new Thread(future);
		thread.start();
		try {
			future.get();
		}catch (Exception e) {
			throw new BusinessException("create WalletExtend fail,partyId:{"+entity.getPartyId().toString()+"},symbol:{"+entity.getWallettype()+"}")  ;
		}
		redisHandler.setSync(
				WalletRedisKeys.WALLET_EXTEND_PARTY_ID + entity.getPartyId().toString() + entity.getWallettype(),
				entity);
	}

	@Override
	public void updateExtend(String partyId, String walletType, double amount) {
		WalletExtend walletExtend = (WalletExtend) redisHandler
				.get(WalletRedisKeys.WALLET_EXTEND_PARTY_ID + partyId.toString() + walletType);
		if (walletExtend == null) {
			walletExtend = this.saveExtendByPara(partyId, walletType);
		}
		walletExtend.setAmount(Arith.add(walletExtend.getAmount(), amount));
		redisHandler.setSync(WalletRedisKeys.WALLET_EXTEND_PARTY_ID + partyId.toString() + walletType, walletExtend);
		redisHandler.pushAsyn(WalletRedisKeys.WALLET_EXTEND_QUEUE_UPDATE,
				new WalletExtendMessage(partyId, walletType, amount));
	}
	
	/**
	 * 修改拓展钱包 余额 及冻结余额
	 */
	public void updateExtend(String partyId, String walletType, double amount, double frozenAmount) {
		WalletExtend walletExtend = (WalletExtend) redisHandler
				.get(WalletRedisKeys.WALLET_EXTEND_PARTY_ID + partyId.toString() + walletType);
		if (walletExtend == null) {
			walletExtend = this.saveExtendByPara(partyId, walletType);
		}
		walletExtend.setAmount(Arith.add(walletExtend.getAmount(), amount));
		walletExtend.setFrozenAmount(Arith.add(walletExtend.getFrozenAmount(), frozenAmount));
		redisHandler.setSync(WalletRedisKeys.WALLET_EXTEND_PARTY_ID + partyId.toString() + walletType, walletExtend);
		redisHandler.pushAsyn(WalletRedisKeys.WALLET_EXTEND_QUEUE_UPDATE, new WalletExtendMessage(partyId, walletType, amount, frozenAmount));
	}

	@Override
	public List<WalletExtend> findExtend(Serializable partyId) {
		
		List<String> list = ApplicationUtil.executeDQL("SELECT WALLETTYPE FROM T_WALLET_EXTEND WHERE PARTY_ID = ?",new Object[] {partyId}, String.class);
		
		List<String> keys = new LinkedList<String>();
		for (String key : list) {
			keys.add(WalletRedisKeys.WALLET_EXTEND_PARTY_ID + partyId.toString() + key);
		}
		Object[] objects = redisHandler.getList(keys.toArray(new String[0]));
		if (objects != null && objects.length > 0) {
			List<WalletExtend> result = new ArrayList<WalletExtend>();
			for (Object obj : objects) {
				result.add((WalletExtend) obj);
			}
			return result;
		}
		return new ArrayList<WalletExtend>();
	}

	@Override
	public List<WalletExtend> findExtend(Serializable partyId, List<String> list_symbol) {
		List<String> keys = new LinkedList<String>();
		for (String key : list_symbol) {
			keys.add(WalletRedisKeys.WALLET_EXTEND_PARTY_ID + partyId.toString() + key);
		}
		Object[] objects = redisHandler.getList(keys.toArray(new String[0]));
		if (objects != null && objects.length > 0) {
			List<WalletExtend> result = new ArrayList<WalletExtend>();
			for (Object obj : objects) {
				if (obj != null)
					result.add((WalletExtend) obj);
			}
			return result;
		}
		return new ArrayList<WalletExtend>();
	}

	public void transfer_wallet(String byPartyId, String safeword, String toPartyId, String coin, double amount,double fee_amount) {

		if ("false".equals(this.sysparaService.find("transfer_wallet_open").getValue())) {
			throw new BusinessException(1, "无权限");
		}

		/**
		 * 实际到账
		 */

		double get_amount = Arith.sub(amount, fee_amount);
		/**
		 * 币种改小写
		 */
		coin = coin.toLowerCase();

		/**
		 * 转账方
		 */
		Party byParty = this.partyService.cachePartyBy(byPartyId, false);
		String giftMoneyLog = "";
		if (byParty.getGift_money_flag()) {
			giftMoneyLog = "赠送金额";
		}
		/**
		 * 正式用户才有转账权限
		 */
		if (!Constants.SECURITY_ROLE_MEMBER.equals(byParty.getRolename())) {
			throw new BusinessException(1, "无权限");
		}
		if (!byParty.getEnabled()) {
			throw new BusinessException(506, "无权限");
		}

		/**
		 * 收款方
		 */
		Party toParty = this.partyService.cachePartyBy(toPartyId, false);
		if (toParty == null || toParty.getId().toString().equals(byParty.getId().toString())) {
			throw new BusinessException(1, "收款方输入错误");
		}
		/*
		 * 转出金额，usdt计价
		 */
		double outAmountToUsdt = amount;
		/*
		 * 转入金额，usdt计价
		 */
		double inAmountToUsdt = get_amount;
		if ("usdt".equals(coin) || "USDT".equals(coin)) {
			/**
			 * 转账方
			 */
			Wallet walletBy = saveWalletByPartyId(byPartyId);

			if (walletBy.getMoney() < amount) {
				throw new BusinessException(1, "余额不足");
			}

			double walletBy_before_amount = walletBy.getMoney();
			
			/*
			 * 保存资金日志
			 */
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
			moneyLog.setAmount_before(walletBy_before_amount);
			moneyLog.setAmount(Arith.sub(0, amount));
			moneyLog.setAmount_after(Arith.sub(walletBy.getMoney(), amount));

			moneyLog.setLog("用户手动转账" + giftMoneyLog + "给" + toParty.getUsername());
			moneyLog.setPartyId(byPartyId);
			moneyLog.setWallettype(Constants.WALLET);
			moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_WITHDRAW);
			moneyLogService.save(moneyLog);

			update(walletBy.getPartyId().toString(), Arith.sub(0, amount));

			/**
			 * 收款方 获得金额
			 */

			Wallet walletTo = saveWalletByPartyId(toPartyId);

			double walletTo_before_amount = walletTo.getMoney();
			
			/*
			 * 保存资金日志
			 */
			MoneyLog moneyLogto = new MoneyLog();
			moneyLogto.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
			moneyLogto.setAmount_before(walletTo_before_amount);
			moneyLogto.setAmount(get_amount);
			moneyLogto.setAmount_after(Arith.add(walletTo.getMoney(), get_amount));

			moneyLogto.setLog("收到" + byParty.getUsername() + giftMoneyLog + "的转账");
			moneyLogto.setPartyId(toPartyId);
			moneyLogto.setWallettype(Constants.WALLET);
			moneyLogto.setContent_type(Constants.MONEYLOG_CONTENT_RECHARGE);
			moneyLogService.save(moneyLogto);

			update(walletTo.getPartyId().toString(), get_amount);

			/**
			 * 充值到账后给他增加提现流水限制金额
			 */
			toParty.setWithdraw_limit_amount(Arith.add(toParty.getWithdraw_limit_amount(), get_amount));
			partyService.update(toParty);
		} else {

			/**
			 * 转账方
			 */
			WalletExtend walletBy = saveExtendByPara(byPartyId, coin);

			if (walletBy.getAmount() < amount) {
				throw new BusinessException(1, "余额不足");
			}

			double walletBy_before_amount = walletBy.getAmount();
			
			/*
			 * 保存资金日志
			 */
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
			moneyLog.setAmount_before(walletBy_before_amount);
			moneyLog.setAmount(Arith.sub(0, amount));
			moneyLog.setAmount_after(Arith.sub(walletBy.getAmount(), amount));

			moneyLog.setLog("用户手动转账" + giftMoneyLog + "给" + toParty.getUsername());
			moneyLog.setPartyId(byPartyId);
			moneyLog.setWallettype(coin);
			moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_WITHDRAW);
			moneyLogService.save(moneyLog);

			updateExtend(walletBy.getPartyId().toString(), coin, Arith.sub(0, amount));
			/**
			 * 收款方 获得金额
			 */
			WalletExtend walletTo = saveExtendByPara(toPartyId, coin);

			double walletTo_before_amount = walletTo.getAmount();
			
			/*
			 * 保存资金日志
			 */
			MoneyLog moneyLogto = new MoneyLog();
			moneyLogto.setCategory(Constants.MONEYLOG_CATEGORY_COIN);
			moneyLogto.setAmount_before(walletTo_before_amount);
			moneyLogto.setAmount(get_amount);
			moneyLogto.setAmount_after(Arith.add(walletTo.getAmount(), get_amount));

			moneyLogto.setLog("收到" + byParty.getUsername() + giftMoneyLog + "的转账");
			moneyLogto.setPartyId(toPartyId);
			moneyLogto.setWallettype(coin);
			moneyLogto.setContent_type(Constants.MONEYLOG_CONTENT_RECHARGE);
			moneyLogService.save(moneyLogto);

			List<Realtime> realtime_list = this.dataService.realtime(coin);
			Realtime realtime = null;
			if (realtime_list.size() > 0) {
				realtime = realtime_list.get(0);
			} else {
				throw new BusinessException("系统错误，请稍后重试");
			}
			outAmountToUsdt = Arith.mul(amount, realtime.getClose());
			inAmountToUsdt = Arith.mul(get_amount, realtime.getClose());
			/**
			 * 充值到账后给他增加提现流水限制金额
			 */
			toParty.setWithdraw_limit_amount(
					Arith.add(toParty.getWithdraw_limit_amount(), Arith.mul(get_amount, realtime.getClose())));
			partyService.update(toParty);

			updateExtend(walletTo.getPartyId().toString(), coin, get_amount);

		}
		userDataService.saveTransferMoneyHandle(byPartyId, toPartyId, outAmountToUsdt, inAmountToUsdt);
	}

	@Override
	public List<WalletExtend> findAllWalletExtend() {
		List<WalletExtend> list = jdbcTemplate.query("SELECT * FROM T_WALLET_EXTEND", RecordObjectMapper.newInstance(WalletExtend.class));
		return list;
	}
	
	@Override
	public List<WalletExtend> findWalletExtendForRealtime() {
		List<WalletExtend> list = jdbcTemplate.query("SELECT * FROM T_WALLET_EXTEND", RecordObjectMapper.newInstance(WalletExtend.class));
		List<WalletExtend> rsList = new ArrayList<>();
		for(WalletExtend ex : list) {
			if (!Constants.WALLETEXTEND_DAPP_USDT_USER.equals(ex.getWallettype())
					|| !Constants.WALLETEXTEND_DAPP_ETH_USER.equals(ex.getWallettype())
					|| !Constants.WALLETEXTEND_DAPP_ETH.equals(ex.getWallettype())
					|| !Constants.WALLETEXTEND_EXPERIENCE_GOLD.equals(ex.getWallettype())
					|| !Constants.WALLETEXTEND_DAPP_USDT.equals(ex.getWallettype())) {
				rsList.add(ex);
			}
		}
		return rsList;
	}
	
	@Override
	public void updateExtendWithLockAndFreeze(String partyId, String walletType, double amount, double lockAmount, double freezeAmount) {
		WalletExtend walletExtend = (WalletExtend) redisHandler
				.get(WalletRedisKeys.WALLET_EXTEND_PARTY_ID + partyId.toString() + walletType);
 
		walletExtend.setAmount(Arith.add(walletExtend.getAmount(), amount));
		walletExtend.setLock_amount(Arith.add(walletExtend.getLock_amount(), lockAmount));
		walletExtend.setFreeze_amount(Arith.add(walletExtend.getFreeze_amount(), freezeAmount));
		redisHandler.setSync(WalletRedisKeys.WALLET_EXTEND_PARTY_ID + partyId.toString() + walletType, walletExtend);
		redisHandler.pushAsyn(WalletRedisKeys.WALLET_EXTEND_QUEUE_UPDATE,
				new WalletExtendMessage(partyId, walletType, amount,lockAmount, freezeAmount));
	}
	
	@Override
	public void updateWithLockAndFreeze(String partyId, double amount,double lockAmount,double freezeAmount) {

		Wallet wallet = (Wallet) redisHandler.get(WalletRedisKeys.WALLET_PARTY_ID + partyId.toString());
		wallet.setMoney(Arith.add(wallet.getMoney(), amount));
		wallet.setLock_money(Arith.add(wallet.getLock_money(), lockAmount));
		wallet.setFreeze_money(Arith.add(wallet.getFreeze_money(), freezeAmount));
		redisHandler.setSync(WalletRedisKeys.WALLET_PARTY_ID + wallet.getPartyId().toString(), wallet);
		redisHandler.pushAsyn(WalletRedisKeys.WALLET_QUEUE_UPDATE, new WalletMessage(partyId, amount,lockAmount,freezeAmount));
	}

	@Override
	public List<Wallet> findAllWallet() {
		List<Wallet> list = jdbcTemplate.query("SELECT * FROM T_WALLET", RecordObjectMapper.newInstance(Wallet.class));
		return list;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setMoneyLogService(MoneyLogService moneyLogService) {
		this.moneyLogService = moneyLogService;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setUserDataService(UserDataService userDataService) {
		this.userDataService = userDataService;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
}
