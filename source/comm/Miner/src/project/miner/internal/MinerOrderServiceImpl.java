package project.miner.internal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.Constants;
import project.data.DataService;
import project.data.model.Realtime;
import project.log.Log;
import project.log.LogService;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.miner.MinerOrderService;
import project.miner.MinerRedisKeys;
import project.miner.MinerService;
import project.miner.model.Miner;
import project.miner.model.MinerOrder;
import project.party.PartyService;
import project.party.model.Party;
import project.party.model.UserRecom;
import project.party.recom.UserRecomService;
import project.redis.RedisHandler;
import project.syspara.SysparaService;
import project.user.UserDataService;
import project.wallet.Wallet;
import project.wallet.WalletExtend;
import project.wallet.WalletService;
import security.SecUser;
import security.internal.SecUserService;

public class MinerOrderServiceImpl implements MinerOrderService {
	
	protected WalletService walletService;
	
	protected MoneyLogService moneyLogService;
	
	protected MinerService minerService;
	
	protected UserDataService userDataService;
	
	protected UserRecomService userRecomService;
	
	protected PartyService partyService;
	
	protected SysparaService sysparaService;
	
	protected LogService logService;
	
	protected SecUserService secUserService;
	
	protected RedisHandler redisHandler;
	
	protected DataService dataService;

	/**
	 * 管理员新增订单
	 */
	public void saveCreateByManage(MinerOrder entity, String operator) {
		this.saveCreate(entity, true);
		SecUser secUser = secUserService.findUserByPartyId(entity.getPartyId());
		Log log = new Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		log.setExtra(entity.getOrder_no());
		log.setOperator(operator);
		log.setUsername(secUser.getUsername());
		log.setPartyId(entity.getPartyId());
		log.setLog("手动新增矿机订单。订单号[" + entity.getOrder_no() + "],订单金额[" + entity.getAmount() + "]。");
		
		logService.saveSync(log);
	}
	
	/**
	 * 矿机下单
	 * @param entity
	 * @param isManage 是否后台购买，后台则可以直接解锁所有矿机
	 */
	public void saveCreateNew(MinerOrder entity, boolean isManage) {
		entity.setCreate_time(new Date());
		String partyId = entity.getPartyId().toString();
		Miner miner = minerService.findById(entity.getMinerId());
		if (null == miner) {
			throw new BusinessException("矿机不存在");
		}
		// 管理员解锁所有，用户正常流程
		if (!isManage && "0".equals(miner.getOn_sale())) {
			throw new BusinessException("矿机未解锁，无法购买");
		}

		if (miner.getTest() && this.findByTest(partyId)) {
			throw new BusinessException("您已购买过体验矿机,不得重复购买");
		}

		// 买入金额需要在区间内(非体验矿机)
		if (!miner.getTest()
				&& (entity.getAmount() < miner.getInvestment_min() || entity.getAmount() > miner.getInvestment_max())) {
			if (miner.getInvestment_max() != 0) {
				throw new BusinessException("买入金额需要在区间内");
			} 
			// 无限制的矿机不得小于最小购买金额
			else if (entity.getAmount() < miner.getInvestment_min()) {
				throw new BusinessException("买入金额需要在区间内");
			}
		}
		
		// 起息时间 = 确认时间加1天
		Date date = new Date();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		// 把日期往后增加一天.整数往后推,负数往前移动
		calendar.add(calendar.DATE, 1);
		// 这个时间就是日期往后推一天的结果
		date = calendar.getTime(); 
		entity.setEarn_time(date);

		if (miner.getTest()) {
			int days = (int) Arith.sub(miner.getCycle(), 1);
			calendar.add(calendar.DATE, days);
			date = calendar.getTime();
			entity.setStop_time(DateUtils.getDayEnd(date));
			// 体验矿机不管输入多少都为0
			entity.setAmount(0d);
		}
		
		if (findByFist(partyId)) {
			// 标识首次购买
			entity.setFirst_buy("1");
		} else {
			// 标识首次购买
			entity.setFirst_buy("0");
		}
		
		double amount=entity.getAmount();
		if(amount<0) entity.setAmount(-amount);
		
		// 扣钱
		String buyCurrency = miner.getBuyCurrency();
		double close = 0;
		if ("usdt".equals(buyCurrency)) {
			saveMinerBuyUsdt(entity);
		} else {
			List<Realtime> realtimes = this.dataService.realtime(buyCurrency);
			if (null == realtimes || realtimes.size() <= 0) {
				throw new BusinessException("行情获取异常，稍后再试");
			}
			close = realtimes.get(0).getClose();
			saveMinerBuyOtherCoin(entity, buyCurrency);
		}
		
		this.insertMinerOrder(entity);
		
		redisHandler.setSync(MinerRedisKeys.MINER_ORDER_ORDERNO + entity.getOrder_no(), entity);

		if (!miner.getTest()) {
			
			Map<String, MinerOrder> map_partyid = (Map<String, MinerOrder>) redisHandler.get(MinerRedisKeys.MINER_ORDER_PARTY_ID + partyId);

			if (map_partyid == null) {
				map_partyid = new ConcurrentHashMap<String, MinerOrder>();
			}
			map_partyid.put(entity.getOrder_no(), entity);
			redisHandler.setSync(MinerRedisKeys.MINER_ORDER_PARTY_ID + partyId, map_partyid);
			
			// 状态：0/正常赎回； 1/ 托管中 ；2/提前赎回 (违约)；3/取消；
			if ("1".equals(entity.getState())) {
				
				// 获取 单个订单 矿机总资产
				Double minerAssetsOrder = entity.getAmount();

				Double minerAssets = (Double) this.redisHandler.get(MinerRedisKeys.MINER_ASSETS_PARTY_ID + partyId);

				this.redisHandler.setSync(MinerRedisKeys.MINER_ASSETS_PARTY_ID + partyId, 
						Arith.add(null == minerAssets ? 0.000D : minerAssets, minerAssetsOrder));
			}			
		}
		
		// 首次购买则给予上级额外本金百分比奖励
		if ("1".equals(entity.getFirst_buy()) && !miner.getTest()) {
			List<UserRecom> list_parents = this.userRecomService.getParents(partyId);

			if (CollectionUtils.isNotEmpty(list_parents)) {
				String miner_bonus_parameters = "";
				miner_bonus_parameters = sysparaService.find("miner_first_bonus_parameters").getValue();
				String[] miner_bonus_array = miner_bonus_parameters.split(",");
				int loop = 0;
				for (int i = 0; i < list_parents.size(); i++) {
					if (loop >= 3) {
						break;
					}
					Party party_parent = this.partyService.cachePartyBy(list_parents.get(i).getReco_id(), true);
					if (!Constants.SECURITY_ROLE_MEMBER.equals(party_parent.getRolename())) {
						continue;
					}
					loop++;
					String parentId = party_parent.getId().toString();
					Map<String, MinerOrder> map_party = (Map<String, MinerOrder>) redisHandler
							.get(MinerRedisKeys.MINER_ORDER_PARTY_ID + parentId);
					if (map_party == null || map_party.size() == 0) {
						continue;
					}

					// 判断是否
					int cycle = 0;
					Iterator<Entry<String, MinerOrder>> it = map_party.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, MinerOrder> entry = it.next();
						MinerOrder minerOrder = entry.getValue();
						if (!"1".equals(minerOrder.getState())) {
							continue;
						}
						Miner miner_party = this.minerService.cacheById(minerOrder.getMinerId());
						if (cycle < miner_party.getCycle()) {
							cycle = miner_party.getCycle();
						}
					}

					if (cycle >= miner.getCycle()) {
						// 增加首次推荐人收益
						double pip_amount = Double.valueOf(miner_bonus_array[loop - 1]);
						double get_money = Arith.mul(entity.getAmount(), pip_amount);
						if ("usdt".equals(buyCurrency)) {
							firstBuyProfitUsdt(party_parent, get_money, i);
						} else {
							firstBuyProfitOtherCoin(buyCurrency, party_parent, get_money, i);
							// 转化成usdt，统计计算
							userDataService.saveMinerProfit(parentId, Arith.div(get_money, close));
						}
					}
				}
			}
		}

		// userdata 数据
		if ("usdt".equals(buyCurrency)) {
			userDataService.saveMinerBuy(entity);
		} else {
			userDataService.saveMinerBuy(minerUserDataOtherCoin(entity, buyCurrency, close));
		}
	}

	/**
	 * 矿机下单
	 * 
	 * @param entity
	 * @param isManage 是否后台购买，后台则可以直接解锁所有矿机
	 */
	public void saveCreate(MinerOrder entity, boolean isManage) {

		entity.setCreate_time(new Date());

//		Party party = this.partyService.findPartyById(entity.getPartyId());
//		if (!party.getEnabled()) {
//			throw new BusinessException(1, "No permission");
//		}
		/**
		 * 加入周期
		 */
		Miner miner = minerService.findById(entity.getMinerId());
		if (null == miner) {
			throw new BusinessException("矿机不存在");
		}
		if (!isManage && "0".equals(miner.getOn_sale())) {// 管理员解锁所有，用户正常流程
//			if (!this.getUnLockMiner(entity.getPartyId().toString(), miner.getId().toString())) {
			throw new BusinessException("矿机未解锁，无法购买");
//			}
		}
//		entity.setAmount(Arith.mul(miner.getInvestment_min(), entity.getVolume()));
//		entity.setCycle(miner.getCycle());

		if (miner.getTest() && this.findByTest(entity.getPartyId().toString())) {// 买过体验机则
			throw new BusinessException("您已购买过体验矿机,不得重复购买");
		}
		/**
		 * 买入金额需要在区间内
		 */
//		if (entity.getAmount() < miner.getInvestment_min()) {
//			throw new BusinessException("不得低于该矿机的金额");
//
//		}
		/**
		 * 买入金额需要在区间内(非体验矿机)
		 */
		if (!miner.getTest()
				&& (entity.getAmount() < miner.getInvestment_min() || entity.getAmount() > miner.getInvestment_max())) {
			if (miner.getInvestment_max() != 0) {
				throw new BusinessException("买入金额需要在区间内");
			} else if (entity.getAmount() < miner.getInvestment_min()) {// 无限制的矿机不得小于最小购买金额
				throw new BusinessException("买入金额需要在区间内");
			}
		}

		String minerBuySymbol = sysparaService.find("miner_buy_symbol").getValue();
		// 是否是其他币种购买
		boolean isOtherCoin = !StringUtils.isEmptyString(minerBuySymbol);
		double close = 0d;
		if (isOtherCoin) {
			List<Realtime> realtimes = this.dataService.realtime(minerBuySymbol);
			if (CollectionUtils.isEmpty(realtimes) || null == realtimes.get(0)) {
				throw new BusinessException("系统错误，请稍后重试");
			}
			close = realtimes.get(0).getClose();

			saveMinerBuyOtherCoin(entity, minerBuySymbol);
		} else {
			/**
			 * 查看余额
			 */
			Wallet wallet = this.walletService.saveWalletByPartyId(entity.getPartyId());
			double amount_before = wallet.getMoney();
			if (wallet.getMoney() < entity.getAmount()) {
				throw new BusinessException("余额不足");
			}

//		wallet.setMoney(Arith.sub(wallet.getMoney(), entity.getAmount()));
			this.walletService.update(wallet.getPartyId().toString(), Arith.sub(0, entity.getAmount()));
			/**
			 * 保存资金日志
			 */

			MoneyLog moneylog = new MoneyLog();
			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
			moneylog.setAmount_before(amount_before);
			moneylog.setAmount(Arith.sub(0, entity.getAmount()));
			moneylog.setAmount_after(Arith.sub(wallet.getMoney(), entity.getAmount()));
			moneylog.setLog("购买矿机产品，订单号[" + entity.getOrder_no() + "]");
			moneylog.setPartyId(entity.getPartyId());
			moneylog.setWallettype(Constants.WALLET);
			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_MINER_BUY);

			moneyLogService.save(moneylog);
		}
		/**
		 * 起息时间=确认时间加1天
		 */
		Date date = new Date();// 取时间
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(calendar.DATE, 1);// 把日期往后增加一天.整数往后推,负数往前移动
		date = calendar.getTime(); // 这个时间就是日期往后推一天的结果
		entity.setEarn_time(date);

		if (miner.getTest()) {
			/**
			 * 截止时间=起息时间+周期+1
			 */
//			Date date = new Date();
//			Calendar calendar1 = new GregorianCalendar();
			int days = (int) Arith.sub(miner.getCycle(), 1);
			calendar.add(calendar.DATE, days);
			date = calendar.getTime();

			entity.setStop_time(DateUtils.getDayEnd(date));
			entity.setAmount(0d);// 体验矿机不管输入多少都为0
		}
		if (findByFist(entity.getPartyId().toString())) {
			entity.setFirst_buy("1");// 标识首次购买
		} else {
			entity.setFirst_buy("0");// 标识首次购买
		}
		
		this.insertMinerOrder(entity);

		redisHandler.setSync(MinerRedisKeys.MINER_ORDER_ORDERNO + entity.getOrder_no(), entity);

		if (!miner.getTest()) {
			
			Map<String, MinerOrder> map_partyid = (Map<String, MinerOrder>) redisHandler
					.get(MinerRedisKeys.MINER_ORDER_PARTY_ID + entity.getPartyId().toString());

			if (map_partyid == null) {
				map_partyid = new ConcurrentHashMap<String, MinerOrder>();
			}
			map_partyid.put(entity.getOrder_no(), entity);
			redisHandler.setSync(MinerRedisKeys.MINER_ORDER_PARTY_ID + entity.getPartyId().toString(), map_partyid);
			
			// 状态：0/正常赎回； 1/ 托管中 ；2/提前赎回 (违约)；3/取消；
			if ("1".equals(entity.getState())) {
				
				// 获取 单个订单 矿机总资产
				Double minerAssetsOrder = entity.getAmount();

				Double minerAssets = (Double) this.redisHandler.get(MinerRedisKeys.MINER_ASSETS_PARTY_ID + entity.getPartyId().toString());

				this.redisHandler.setSync(MinerRedisKeys.MINER_ASSETS_PARTY_ID + entity.getPartyId().toString(), 
						Arith.add(null == minerAssets ? 0.000D : minerAssets, minerAssetsOrder));
			}			
		}
		
		/**
		 * 首次购买则给予上级额外本金百分比奖励
		 */
		if ("1".equals(entity.getFirst_buy()) && !miner.getTest()) {
			List<UserRecom> list_parents = this.userRecomService.getParents(entity.getPartyId());

			if (CollectionUtils.isNotEmpty(list_parents)) {
				String miner_bonus_parameters = "";
				miner_bonus_parameters = sysparaService.find("miner_first_bonus_parameters").getValue();
				String[] miner_bonus_array = miner_bonus_parameters.split(",");
				int loop = 0;
				for (int i = 0; i < list_parents.size(); i++) {
					if (loop >= 3) {
						break;
					}
					Party party_parent = this.partyService.cachePartyBy(list_parents.get(i).getReco_id(), true);
					if (!Constants.SECURITY_ROLE_MEMBER.equals(party_parent.getRolename())) {
						continue;
					}
					loop++;
					Map<String, MinerOrder> map_party = (Map<String, MinerOrder>) redisHandler
							.get(MinerRedisKeys.MINER_ORDER_PARTY_ID + party_parent.getId().toString());
					if (map_party == null || map_party.size() == 0) {
						continue;
					}
					/*
					 * 判断是否
					 */
					int cycle = 0;
					Iterator<Entry<String, MinerOrder>> it = map_party.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, MinerOrder> entry = it.next();
						MinerOrder minerOrder = entry.getValue();
						if (!"1".equals(minerOrder.getState())) {
							continue;
						}
						Miner miner_party = this.minerService.cacheById(minerOrder.getMinerId());
						if (cycle < miner_party.getCycle()) {
							cycle = miner_party.getCycle();
						}

					}

					if (cycle >= miner.getCycle()) {
						/**
						 * 增加首次推荐人收益
						 */
						double pip_amount = Double.valueOf(miner_bonus_array[loop - 1]);
						double get_money = Arith.mul(entity.getAmount(), pip_amount);

						if (isOtherCoin) {
							firstBuyProfitOtherCoin(minerBuySymbol, party_parent, get_money, i);
							// 转化成usdt，统计计算
							userDataService.saveMinerProfit(party_parent.getId().toString(),
									Arith.div(get_money, close));
						} else {


						}

					}
				}

			}
		}

		/**
		 * userdata 数据
		 */
		if (isOtherCoin) {
			userDataService.saveMinerBuy(minerUserDataOtherCoin(entity, minerBuySymbol, close));
		} else {
			userDataService.saveMinerBuy(entity);
		}

	}

	/**
	 * 增加首次推荐人收益
	 */
	protected void firstBuyProfitOtherCoin(String symbol, Party partyParent, double getMoney, int i) {
		WalletExtend walletExtend = walletService.saveExtendByPara(partyParent.getId().toString(), symbol);
		double amount_before_parent = walletExtend.getAmount();
		this.walletService.updateExtend(walletExtend.getPartyId().toString(), symbol, getMoney);

		MoneyLog moneyLog = new MoneyLog();
		moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
		moneyLog.setAmount_before(amount_before_parent);
		moneyLog.setAmount(getMoney);
		moneyLog.setAmount_after(Arith.add(walletExtend.getAmount(), getMoney));
		moneyLog.setLog("第" + (i + 1) + "代下级用户，首次购买矿机推荐奖励金");
		moneyLog.setPartyId(partyParent.getId().toString());
		moneyLog.setWallettype(symbol);
		moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_MINER_RECOM_PROFIT);
		moneyLogService.save(moneyLog);
		userDataService.saveMinerProfit(walletExtend.getPartyId().toString(), getMoney);
	}
	
	/**
	 * 增加首次推荐人收益
	 */
	protected void firstBuyProfitUsdt(Party partyParent, double getMoney, int i) {
		Wallet wallet_parent = walletService.saveWalletByPartyId(partyParent.getId().toString());
		double amount_before_parent = wallet_parent.getMoney();
		walletService.update(wallet_parent.getPartyId().toString(), getMoney);

		MoneyLog moneyLog = new MoneyLog();
		moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
		moneyLog.setAmount_before(amount_before_parent);
		moneyLog.setAmount(getMoney);
		moneyLog.setAmount_after(Arith.add(wallet_parent.getMoney(), getMoney));
		moneyLog.setLog("第" + (i + 1) + "代下级用户，首次购买矿机推荐奖励金");
		moneyLog.setPartyId(partyParent.getId().toString());
		moneyLog.setWallettype(Constants.WALLET);
		moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_MINER_RECOM_PROFIT);
		moneyLogService.save(moneyLog);
		userDataService.saveMinerProfit(partyParent.getId().toString(), getMoney);
	}

	protected MinerOrder minerUserDataOtherCoin(MinerOrder entity, String symbol, double close) {
		MinerOrder order = new MinerOrder();
		// 不改变原有的
		BeanUtils.copyProperties(entity, order);
		// 转化成usdt，统计计算
		order.setAmount(Arith.div(order.getAmount(), close));
		return order;
	}
	
	protected void saveMinerBuyUsdt(MinerOrder entity) {
		Wallet wallet = this.walletService.saveWalletByPartyId(entity.getPartyId());
		double amount_before = wallet.getMoney();
		if (wallet.getMoney() < entity.getAmount()) {
			throw new BusinessException("余额不足");
		}

		this.walletService.update(wallet.getPartyId().toString(), Arith.sub(0, entity.getAmount()));

		MoneyLog moneylog = new MoneyLog();
		moneylog.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
		moneylog.setAmount_before(amount_before);
		moneylog.setAmount(Arith.sub(0, entity.getAmount()));
		moneylog.setAmount_after(Arith.sub(wallet.getMoney(), entity.getAmount()));
		moneylog.setLog("购买矿机产品，订单号[" + entity.getOrder_no() + "]");
		moneylog.setPartyId(entity.getPartyId());
		moneylog.setWallettype(Constants.WALLET);
		moneylog.setContent_type(Constants.MONEYLOG_CONTENT_MINER_BUY);
		moneyLogService.save(moneylog);
	}

	protected void saveMinerBuyOtherCoin(MinerOrder entity, String symbol) {
		WalletExtend walletExtend = walletService.saveExtendByPara(entity.getPartyId(), symbol);
		if (entity.getAmount() > walletExtend.getAmount()) {
			throw new BusinessException("持有币种不足");
		}
		double amount_before = walletExtend.getAmount();
		walletService.updateExtend(walletExtend.getPartyId().toString(), walletExtend.getWallettype(),
				Arith.sub(0, entity.getAmount()));
		
		MoneyLog moneylog = new MoneyLog();
		moneylog.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
		moneylog.setAmount_before(amount_before);
		moneylog.setAmount(Arith.sub(0, entity.getAmount()));
		moneylog.setAmount_after(Arith.sub(walletExtend.getAmount(), entity.getAmount()));
		moneylog.setLog("购买矿机产品，订单号[" + entity.getOrder_no() + "]");
		moneylog.setPartyId(entity.getPartyId());
		moneylog.setWallettype(symbol);
		moneylog.setContent_type(Constants.MONEYLOG_CONTENT_MINER_BUY);
		moneyLogService.save(moneylog);
	}
	
	protected void saveMinerCloseUsdt(MinerOrder entity) {
		Wallet wallet = this.walletService.saveWalletByPartyId(entity.getPartyId());
		double amount_before = wallet.getMoney();
		double back_money = entity.getAmount();
		walletService.update(wallet.getPartyId().toString(), back_money);

		MoneyLog moneylog = new MoneyLog();
		moneylog.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
		moneylog.setAmount_before(amount_before);
		moneylog.setAmount(Arith.add(0, back_money));
		moneylog.setAmount_after(Arith.add(wallet.getMoney(), back_money));
		moneylog.setPartyId(entity.getPartyId());
		moneylog.setWallettype(Constants.WALLET);
		moneylog.setLog("矿机本金退回，订单号[" + entity.getOrder_no() + "]");
		moneylog.setContent_type(Constants.MONEYLOG_CONTENT_MINER_BACK);
		moneyLogService.save(moneylog);
	}

	protected void saveMinerCloseOtherCoin(MinerOrder entity, String symbol) {
		WalletExtend walletExtend = walletService.saveExtendByPara(entity.getPartyId(), symbol);
		double amount_before = walletExtend.getAmount();
		double back_money = entity.getAmount();
		this.walletService.updateExtend(walletExtend.getPartyId().toString(), walletExtend.getWallettype(), back_money);

		MoneyLog moneylog_deposit = new MoneyLog();
		moneylog_deposit.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
		moneylog_deposit.setAmount_before(amount_before);
		moneylog_deposit.setAmount(back_money);
		moneylog_deposit.setAmount_after(Arith.add(walletExtend.getAmount(), back_money));
		moneylog_deposit.setLog("矿机本金退回，订单号[" + entity.getOrder_no() + "]");
		moneylog_deposit.setPartyId(entity.getPartyId());
		moneylog_deposit.setWallettype(symbol);
		moneylog_deposit.setContent_type(Constants.MONEYLOG_CONTENT_MINER_BACK);
		moneyLogService.save(moneylog_deposit);
	}
	
	/**
	 * 赎回
	 */
	public void saveClose(MinerOrder entity, Miner miner) {
		String buyCurrency = miner.getBuyCurrency();
		double close = 0;
		if ("usdt".equals(buyCurrency)) {
			if (entity.getAmount() != 0) {
				saveMinerCloseUsdt(entity);
			}
		} else {
			List<Realtime> realtimes = this.dataService.realtime(buyCurrency);
			if (null == realtimes || realtimes.size() <= 0) {
				throw new BusinessException("行情获取异常，稍后再试");
			}
			close = realtimes.get(0).getClose();
			saveMinerCloseOtherCoin(entity, buyCurrency);
		}

		entity.setClose_time(new Date());// 赎回时间
		
		this.updateMinerOrder(entity);

		// userdata 数据
		if ("usdt".equals(buyCurrency)) {
			userDataService.saveMinerClose(entity);
		} else {
			userDataService.saveMinerClose(minerUserDataOtherCoin(entity, buyCurrency, close));
		}

		// 更新矿机订单
		redisHandler.setSync(MinerRedisKeys.MINER_ORDER_ORDERNO + entity.getOrder_no(), entity);
		String partyId = entity.getPartyId().toString();
		if (!miner.getTest()) {
			
			Map<String, MinerOrder> map_partyid = (Map<String, MinerOrder>) redisHandler.get(MinerRedisKeys.MINER_ORDER_PARTY_ID + partyId);
			if (map_partyid == null) {
				map_partyid = new ConcurrentHashMap<String, MinerOrder>();
			}
			
			map_partyid.put(entity.getOrder_no(), entity);
			redisHandler.setSync(MinerRedisKeys.MINER_ORDER_PARTY_ID + partyId, map_partyid);
			
			Double minerAssets = (Double) this.redisHandler.get(MinerRedisKeys.MINER_ASSETS_PARTY_ID + partyId);
								
			MinerOrder minerOld = map_partyid.get(entity.getOrder_no());
			// 状态：0/正常赎回； 1/ 托管中 ；2/提前赎回 (违约)；3/取消；
			if ("1".equals(minerOld.getState())) {

				// 获取 单个订单 矿机总资产
				Double minerAssetsOld = minerOld.getAmount();
				
				minerAssets = null == minerAssets ? 0.000D - minerAssetsOld : minerAssets - minerAssetsOld;
			}

			// 状态：0/正常赎回； 1/ 托管中 ；2/提前赎回 (违约)；3/取消；
			if ("1".equals(entity.getState())) {

				// 获取 单个订单 矿机总资产
				Double minerAssetsOrder = entity.getAmount();
								
				minerAssets = null == minerAssets ? minerAssetsOrder : minerAssets + minerAssetsOrder;
			}

			this.redisHandler.setSync(MinerRedisKeys.MINER_ASSETS_PARTY_ID + partyId, null == minerAssets ? 0.000D : minerAssets);
		}
	}

	public void saveClose(MinerOrder entity) {
		String minerBuySymbol = sysparaService.find("miner_buy_symbol").getValue();
		// 是否是其他币种购买
		boolean isOtherCoin = !StringUtils.isEmptyString(minerBuySymbol);
		double close = 0;
		if (isOtherCoin) {
			List<Realtime> realtimes = this.dataService.realtime(minerBuySymbol);
			if (CollectionUtils.isEmpty(realtimes) || null == realtimes.get(0)) {
				throw new BusinessException("系统错误，请稍后重试");
			}
			close = realtimes.get(0).getClose();

			saveMinerCloseOtherCoin(entity, minerBuySymbol);
		} else if (entity.getAmount() != 0) {// 体验矿机，购买价为0
			Wallet wallet = this.walletService.saveWalletByPartyId(entity.getPartyId());
			double amount_before = wallet.getMoney();
			// 购买金额-违约金=退还金额
//			double back_money = Arith.sub(entity.getAmount(), entity.getDefault_money());
			double back_money = entity.getAmount();
//			if ("0".equals(entity.getState())) {// 正常状态下 到期后一天 奖励5%
//				double profit = entity.getCompute_profit();
//				back_money = profit;
//			}
//			wallet.setMoney(Arith.add(wallet.getMoney(), back_money));
			this.walletService.update(wallet.getPartyId().toString(), back_money);
			/**
			 * 保存资金日志
			 */
			MoneyLog moneylog = new MoneyLog();
			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
			moneylog.setAmount_before(amount_before);
			moneylog.setAmount(Arith.add(0, back_money));
			moneylog.setAmount_after(Arith.add(wallet.getMoney(), back_money));
			moneylog.setPartyId(entity.getPartyId());
			moneylog.setWallettype(Constants.WALLET);
			moneylog.setLog("矿机本金退回，订单号[" + entity.getOrder_no() + "]");
			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_MINER_BACK);
			moneyLogService.save(moneylog);
		}

		entity.setClose_time(new Date());// 赎回时间
		this.updateMinerOrder(entity);
		/**
		 * userdata 数据
		 */
		if (isOtherCoin) {
			userDataService.saveMinerClose(minerUserDataOtherCoin(entity, minerBuySymbol, close));
		} else {
			userDataService.saveMinerClose(entity);
		}

		Miner miner = this.minerService.cacheById(entity.getMinerId());

		// 更新矿机订单
		redisHandler.setSync(MinerRedisKeys.MINER_ORDER_ORDERNO + entity.getOrder_no(), entity);
		if (!miner.getTest()) {
			
			Map<String, MinerOrder> map_partyid = (Map<String, MinerOrder>) redisHandler
					.get(MinerRedisKeys.MINER_ORDER_PARTY_ID + entity.getPartyId().toString());
			if (map_partyid == null) {
				map_partyid = new ConcurrentHashMap<String, MinerOrder>();
			}
			
			MinerOrder minerOld = map_partyid.get(entity.getOrder_no());
			
			map_partyid.put(entity.getOrder_no(), entity);
			redisHandler.setSync(MinerRedisKeys.MINER_ORDER_PARTY_ID + entity.getPartyId().toString(), map_partyid);
			
			Double minerAssets = (Double) this.redisHandler.get(MinerRedisKeys.MINER_ASSETS_PARTY_ID + entity.getPartyId().toString());
									
			// 状态：0/正常赎回； 1/ 托管中 ；2/提前赎回 (违约)；3/取消；
			if ("1".equals(minerOld.getState())) {

				// 获取 单个订单 矿机总资产
				Double minerAssetsOld = minerOld.getAmount();
				
				minerAssets = null == minerAssets ? 0.000D - minerAssetsOld : minerAssets - minerAssetsOld;
			}

			// 状态：0/正常赎回； 1/ 托管中 ；2/提前赎回 (违约)；3/取消；
			if ("1".equals(entity.getState())) {

				// 获取 单个订单 矿机总资产
				Double minerAssetsOrder = entity.getAmount();
								
				minerAssets = null == minerAssets ? 0.000D + minerAssetsOrder : minerAssets + minerAssetsOrder;
			}

			this.redisHandler.setSync(MinerRedisKeys.MINER_ASSETS_PARTY_ID + entity.getPartyId().toString(), null == minerAssets ? 0.000D : minerAssets);
		}
	}

	public List<MinerOrder> findAllByState(String state) {
		return ApplicationUtil.executeSelect(MinerOrder.class,"WHERE STATE=?",new Object[] {state});
	}

	@Override
	public MinerOrder findByOrder_no(String order_no) {
		return (MinerOrder) redisHandler.get(MinerRedisKeys.MINER_ORDER_ORDERNO + order_no);
	}

	public List<MinerOrder> findByState(String partyId, String state) {
		if (StringUtils.isEmptyString(partyId)) return findAllByState(state);
		
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		
		/**
		 * 如果是查询已赎回的，则将提前赎回和正常赎回的都查出来
		 */
		List<MinerOrder> list;
		if (StringUtils.isEmptyString(state)) {
			list = jdbcTemplate.query("SELECT * FROM T_MINER_ORDER WHERE PARTY_ID=?",RecordObjectMapper.newInstance(MinerOrder.class), partyId);
		} else {
			if ("2".equals(state)) {
				list = jdbcTemplate.query("SELECT * FROM T_MINER_ORDER WHERE PARTY_ID=? AND (STATE=? OR STATE=?)",RecordObjectMapper.newInstance(MinerOrder.class), partyId, "0", "2");
			} else {
				list = jdbcTemplate.query("SELECT * FROM T_MINER_ORDER WHERE STATE=? AND PARTY_ID=?",RecordObjectMapper.newInstance(MinerOrder.class), state, partyId);
			}
		}

		return list.size()<=0?null:list;
	}

	public Page pagedQuery(int pageNo, int pageSize, String partyId, String state) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		StringBuilder sqlBuilder=new StringBuilder("SELECT minerOrder.UUID id,minerOrder.ORDER_NO order_no ,minerOrder.MINER_ID minerId,minerOrder.AMOUNT amount,DATE_FORMAT(minerOrder.CREATE_TIME,'%Y-%m-%d %H:%i:%S') create_time,");
		sqlBuilder.append("DATE(minerOrder.EARN_TIME) earn_time,DATE(minerOrder.STOP_TIME) stop_time,minerOrder.PROFIT profit,minerOrder.STATE state,minerOrder.BASE_COMPUTE_AMOUNT base_compute_amount,");
		sqlBuilder.append("miner.CYCLE cycle, miner.CYCLE_CLOSE cycle_close, miner.DAILY_RATE daily_rate, miner.TEST test, miner.BUY_CURRENCY buyCurrency, miner.OUTPUT_CURRENCY outputCurrency,");
		sqlBuilder.append("miner.NAME miner_name,miner.NAME_EN miner_name_en,miner.NAME_CN miner_name_cn FROM T_MINER_ORDER minerOrder ");
		sqlBuilder.append("LEFT JOIN T_MINER miner ON miner.UUID= minerOrder.MINER_ID WHERE minerOrder.PARTY_ID=? ");
		
		ArrayList<Object> params=new ArrayList<Object>();
		params.add(partyId);
		
		if ("2".equals(state)) {
			sqlBuilder.append("AND minerOrder.STATE in('0','2') ");
		} else if(StringUtils.isNotEmpty(state)){
			sqlBuilder.append("AND minerOrder.STATE=? ");
			params.add(state);
		}
		
		sqlBuilder.append("ORDER BY minerOrder.CREATE_TIME DESC LIMIT ?,?");
		params.add(page.getFirstElementNumber());
		params.add(pageSize);
		
		List<HashMap> list=ApplicationUtil.executeDQL(sqlBuilder.toString(),params.toArray(new Object[params.size()]),HashMap.class);
		page.setElements(list);
		return page;
	}

	public void deleteAllByPartyId(String partyId) {
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		List<MinerOrder> list = jdbcTemplate.query("SELECT * FROM T_MINER_ORDER WHERE PARTY_ID=?",RecordObjectMapper.newInstance(MinerOrder.class), partyId);
		
		if (!CollectionUtils.isEmpty(list)) {
			for (MinerOrder order : list) {
				jdbcTemplate.update("DELETE FROM T_MINER_ORDER WHERE UUID=?", order.getId());
				redisHandler.remove(MinerRedisKeys.MINER_ORDER_ORDERNO + order.getOrder_no());
			}
			redisHandler.remove(MinerRedisKeys.MINER_ORDER_PARTY_ID + partyId);
			
			this.redisHandler.remove(MinerRedisKeys.MINER_ASSETS_PARTY_ID + partyId);
		}
	}

	public void setWalletService(WalletService walletService) {
		this.walletService = walletService;
	}

	public void setMoneyLogService(MoneyLogService moneyLogService) {
		this.moneyLogService = moneyLogService;
	}

	public void setMinerService(MinerService minerService) {
		this.minerService = minerService;
	}

	/**
	 * true:买过体验矿机，false:没买过
	 */
	public boolean findByTest(String partyId) {
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		List<String> list = jdbcTemplate.queryForList("SELECT mo.UUID FROM T_MINER_ORDER mo LEFT JOIN T_MINER m ON m.UUID = mo.MINER_ID WHERE PARTY_ID=? AND m.TEST='Y' ", String.class, partyId);
		return list != null && CollectionUtils.isNotEmpty(list) && list.get(0) != null;// 存在返回值，且不为空
	}

	/**
	 * true：首次购买，false:非首次购买
	 * 
	 * @param partyId
	 * @return
	 */
	public boolean findByFist(String partyId) {
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		List<String> list = jdbcTemplate.queryForList("SELECT mo.UUID FROM T_MINER_ORDER mo LEFT JOIN T_MINER m ON m.UUID = mo.MINER_ID WHERE PARTY_ID=? AND m.TEST='N' AND FIRST_BUY='1' ", String.class, partyId);
		// 存在返回值，且不为空
		return !(list != null && CollectionUtils.isNotEmpty(list) && list.get(0) != null);
	}

	public boolean getUnLockMiner(String partyId, String minerId) {
		Miner miner = this.minerService.cacheById(minerId);

		List<UserRecom> list_userRecoms = this.userRecomService.findRecoms(partyId);
		int cycle = 0;
		for (int i = 0; i < list_userRecoms.size(); i++) {
			Map<String, MinerOrder> map = (Map<String, MinerOrder>) redisHandler.get(MinerRedisKeys.MINER_ORDER_PARTY_ID + list_userRecoms.get(i).getPartyId().toString());
			if (map != null) cycle = cycle + map.size();
		}

		return cycle >= miner.getCycle();// 如果周期比该矿机的大，则解锁
	}
	
	/**
	 * 新增矿机订单
	 */
	public void insertMinerOrder(MinerOrder order) {
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		Object[] jdbcParams = ApplicationUtil.getInsertStatement(order);
		String insertUserSql = (String)jdbcParams[0];
		Object[] sqlParameters = (Object[])jdbcParams[1];
		jdbcTemplate.update(insertUserSql,sqlParameters);
	}
	
	/**
	 * 修改矿机订单
	 */
	public void updateMinerOrder(MinerOrder order) {
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		Object[] jdbcParams=ApplicationUtil.getUpdateStatement(order,"WHERE UUID=?",new Object[] {order.getId()});
		String updateUserSql=(String)jdbcParams[0];
		Object[] sqlParameters=(Object[])jdbcParams[1];
		jdbcTemplate.update(updateUserSql,sqlParameters);
	}

	public void setUserDataService(UserDataService userDataService) {
		this.userDataService = userDataService;
	}

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}
}
