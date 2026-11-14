package project.miner.job;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.DateUtils;
import kernel.util.ThreadUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.Constants;
import project.data.DataService;
import project.data.model.Realtime;
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

public class MinerOrderProfitServiceImpl implements MinerOrderProfitService {
	
	protected DataService dataService;
	
	protected MinerService minerService;
	
	protected PartyService partyService;
	
	protected RedisHandler redisHandler;
	
	protected WalletService walletService;
	
	protected SysparaService sysparaService;
	
	protected UserDataService userDataService;
	
	protected MoneyLogService moneyLogService;
	
	protected UserRecomService userRecomService;
	
	protected MinerOrderService minerOrderService;
	
	private static final Logger logger = LoggerFactory.getLogger(MinerOrderProfitServiceImpl.class);

	/**
	 * 计算推荐人收益
	 */
	protected Map<String, Double> cacheRecomProfit = new ConcurrentHashMap<String, Double>();

	/**
	 * 分页获取计息中的矿机订单
	 */
	public Page pagedQueryComputeOrder(int pageNo, int pageSize) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		List<MinerOrder> list=ApplicationUtil.executeSelect(MinerOrder.class,"WHERE STATE=1 LIMIT ?,?",new Object[] {page.getFirstElementNumber(),pageSize});
		page.setElements(list);
		return page;
	}

	/**
	 * 计算订单收益
	 * 
	 * @param orders                 订单列表
	 * @param miner_profit_symbol    指定币种
	 * @param realtime               币种行情
	 * @param miner_bonus_parameters 推荐人收益参数
	 */
	public void saveComputeOrderProfit(List<MinerOrder> orders, String miner_bonus_parameters, Date systemTime) {
		logger.info("start compute order size:{}", orders.size());
		List<MinerOrderMessage> saveMinerOrders = new ArrayList<MinerOrderMessage>();
		for (MinerOrder order : orders) {
			Miner miner = minerService.cacheById(order.getMinerId());
			if (null == miner) {
				logger.error("该矿机不存在，停止计息，minerId:" + order.getMinerId());
				continue;
			}
			if (!miner.getTest() && systemTime.before(order.getEarn_time())) {
				// 非体验矿机，今天<起息时间 不计息，表示满24小时才开始计息
				continue;
			}
				
			if (miner.getTest() && systemTime.before(DateUtils.getDayStart(order.getEarn_time()))) {
				// 体验矿机，今天<起息日 不计息
				continue;
			}

			// 当前时间 > 截止时间
			if (order.getStop_time() != null && systemTime.after(order.getStop_time())) {
				// 正常赎回，停止计息，退还本金
				order.setState("0");
				order.setCompute_day(systemTime);
				// 截止日 = 今天时就已经返还完毕
				minerOrderService.saveClose(order, miner);
			} else {
				Double miner_test_profit = sysparaService.find("miner_test_profit").getDouble();
				double day_profit = 0;
				if (miner.getTest()) {
					day_profit = miner_test_profit;
				} else {
					day_profit = Arith.mul(order.getAmount(), Arith.mul(miner.getDaily_rate(), 0.01));
				}
				
				// 当天计算过，则不再计算，例如 1号4点计算， 则2号0点以前进入都判定计算过
				if (order.getCompute_day() != null
						&& systemTime.before(DateUtils.getDayStart(DateUtils.addDate(order.getCompute_day(), 1)))) {
					// 已经计息过的直接进入缓存
					handleRecomProfit(order.getPartyId().toString(), day_profit, miner, miner_bonus_parameters);
					continue;
				}
				order.setCompute_day(systemTime);// 记息日期
				order.setProfit(Arith.add(order.getProfit(), day_profit));// 累计收益

				updateProfitToWallet(day_profit, order, miner, systemTime);

				saveMinerOrders.add(new MinerOrderMessage(order.getOrder_no(), order.getProfit(), order.getCompute_day()));
				// 更新矿机订单
				redisHandler.setSync(MinerRedisKeys.MINER_ORDER_ORDERNO + order.getOrder_no(), order);

				userDataService.saveMinerProfit(order.getPartyId().toString(), day_profit);

				handleRecomProfit(order.getPartyId().toString(), day_profit, miner, miner_bonus_parameters);
				//每单处理完后等待100ms，避免循环提交事务导致问题
				ThreadUtils.sleep(200);
			}
		}
		logger.info("start miner batch update size:{}", saveMinerOrders.size());
		updateBatchMinerOrdersProfit(saveMinerOrders);
	}
	
	/**
	 * 计算订单收益
	 * 
	 * @param orders                 订单列表
	 * @param miner_profit_symbol    指定币种
	 * @param realtime               币种行情
	 * @param miner_bonus_parameters 推荐人收益参数
	 */
	public void saveComputeOrderProfit(List<MinerOrder> orders, String miner_bonus_parameters) {
		logger.info("start compute order size:{}", orders.size());
		List<MinerOrderMessage> saveMinerOrders = new ArrayList<MinerOrderMessage>();
		for (MinerOrder order : orders) {
			Miner miner = minerService.cacheById(order.getMinerId());
			if (null == miner) {
				logger.error("该矿机不存在，停止计息，minerId:" + order.getMinerId());
				continue;
			}
			if (!miner.getTest() && new Date().before(order.getEarn_time())) {
				// 非体验矿机，今天<起息时间 不计息，表示满24小时才开始计息
				continue;
			}
				
			if (miner.getTest() && new Date().before(DateUtils.getDayStart(order.getEarn_time()))) {
				// 体验矿机，今天<起息日 不计息
				continue;
			}

			// 当前时间 > 截止时间
			if (order.getStop_time() != null && new Date().after(order.getStop_time())) {
				// 正常赎回，停止计息，退还本金
				order.setState("0");
				// 截止日 = 今天时就已经返还完毕
				order.setCompute_day(new Date());
				minerOrderService.saveClose(order, miner);
			} else {
				Double miner_test_profit = sysparaService.find("miner_test_profit").getDouble();
				// 当日获取的收益
				double day_profit = 0;
				if (miner.getTest()) {
					day_profit = miner_test_profit;
				} else {
					day_profit = Arith.mul(order.getAmount(), Arith.mul(miner.getDaily_rate(), 0.01));
				}
				
				// 当天计算过，则不再计算，例如 1号4点计算， 则2号0点以前进入都判定计算过
				if (order.getCompute_day() != null
						&& new Date().before(DateUtils.getDayStart(DateUtils.addDate(order.getCompute_day(), 1)))) {

					// 已经计息过的直接进入缓存
					handleRecomProfit(order.getPartyId().toString(), day_profit, miner, miner_bonus_parameters);
					continue;
				}
				// 记息日期
				order.setCompute_day(new Date());
				// 累计收益
				order.setProfit(Arith.add(order.getProfit(), day_profit));

				/**
				 * 矿机产出是否需要转化成某个币种,若为空则不转化，若写入的symbol代码不存在则也不转化
				 */
				updateProfitToWallet(day_profit, order, miner);

				saveMinerOrders
						.add(new MinerOrderMessage(order.getOrder_no(), order.getProfit(), order.getCompute_day()));
				// 更新矿机订单
				redisHandler.setSync(MinerRedisKeys.MINER_ORDER_ORDERNO + order.getOrder_no(), order);

				userDataService.saveMinerProfit(order.getPartyId().toString(), day_profit);

				handleRecomProfit(order.getPartyId().toString(), day_profit, miner, miner_bonus_parameters);
				//每单处理完后等待100ms，避免循环提交事务导致问题
				ThreadUtils.sleep(200);
			}
		}
		logger.info("start miner batch update size:{}", saveMinerOrders.size());
		updateBatchMinerOrdersProfit(saveMinerOrders);
	}

	/**
	 * 更新收益到钱包
	 * 
	 * @param miner_profit_symbol 指定币种
	 * @param day_profit          当日收益
	 * @param realtime            币种行情
	 * @param order               矿机订单
	 */
	protected void updateProfitToWallet(double dayProfit, MinerOrder order, Miner miner) {
		// 矿机购买时使用的币种，则产生对应的币种，转换成u
		String buyCurrency = miner.getBuyCurrency();
		String outputCurrency = miner.getOutputCurrency();
		
//		// U购买 产出U
//		if ("usdt".equals(buyCurrency) && "usdt".equals(outputCurrency)) {
//			symbolProfit = symbolProfit;
//		}
		
		// U购买 产出第三方币
		if ("usdt".equals(buyCurrency) && !"usdt".equals(outputCurrency)) {
			List<Realtime> realtimes = this.dataService.realtime(outputCurrency);
			if (null == realtimes || realtimes.size() <= 0) {
				throw new BusinessException("行情获取异常，稍后再试");
			}
			dayProfit = Arith.div(dayProfit, realtimes.get(0).getClose());
		}
		
		// 第三方币购买 产出U
		if (!"usdt".equals(buyCurrency) && "usdt".equals(outputCurrency)) {
			List<Realtime> realtimes = this.dataService.realtime(buyCurrency);
			if (null == realtimes || realtimes.size() <= 0) {
				throw new BusinessException("行情获取异常，稍后再试");
			}
			dayProfit = Arith.mul(dayProfit, realtimes.get(0).getClose());
		}
		
		// 第三方币购买 产出第三方币
		if (!"usdt".equals(buyCurrency) && !"usdt".equals(outputCurrency)) {
//			// A购买 产出A
//			if (buyCurrency.equals(outputCurrency)) {
//				symbolProfit = symbolProfit;
//			}
			// A购买 产出B
			if (!buyCurrency.equals(outputCurrency)) {
				List<Realtime> realtimes = this.dataService.realtime(buyCurrency);
				if (null == realtimes || realtimes.size() <= 0) {
					throw new BusinessException("行情获取异常，稍后再试");
				}
				dayProfit = Arith.mul(dayProfit, realtimes.get(0).getClose());
				
				List<Realtime> realtimeList = this.dataService.realtime(outputCurrency);
				if (null == realtimeList || realtimeList.size() <= 0) {
					throw new BusinessException("行情获取异常，稍后再试");
				}
				dayProfit = Arith.div(dayProfit, realtimeList.get(0).getClose());
			}
		}
		
		// 收益为USDT
		if ("usdt".equals(outputCurrency)) {
			Wallet wallet = this.walletService.saveWalletByPartyId(order.getPartyId());
			double amount_before = wallet.getMoney();
			this.walletService.update(wallet.getPartyId().toString(), dayProfit);

			MoneyLog moneylog = new MoneyLog();
			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
			moneylog.setAmount_before(amount_before);
			moneylog.setAmount(dayProfit);
			moneylog.setAmount_after(Arith.add(wallet.getMoney(), dayProfit));
			moneylog.setLog("矿机收益，订单号[" + order.getOrder_no() + "]");
			moneylog.setPartyId(order.getPartyId());
			moneylog.setWallettype(Constants.WALLET);
			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_MINER_PROFIT);
			moneylog.setCreateTime(new Date());
			moneyLogService.save(moneylog);
		} 
		// 非USDT币种收益
		else {
			WalletExtend walletExtend = walletService.saveExtendByPara(order.getPartyId(), outputCurrency);
			double amount_before = walletExtend.getAmount();
			this.walletService.updateExtend(order.getPartyId().toString(), outputCurrency, dayProfit);

			MoneyLog moneylog = new MoneyLog();
			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
			moneylog.setAmount_before(amount_before);
			moneylog.setAmount(dayProfit);
			moneylog.setAmount_after(Arith.add(walletExtend.getAmount(), dayProfit));
			moneylog.setLog("矿机收益，订单号[" + order.getOrder_no() + "]");
			moneylog.setPartyId(order.getPartyId());
			moneylog.setWallettype(outputCurrency);
			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_MINER_PROFIT);
			moneylog.setCreateTime(new Date());
			moneyLogService.save(moneylog);
		}
	}
	
	/**
	 * 更新收益到钱包
	 * 
	 * @param miner_profit_symbol 指定币种
	 * @param day_profit          当日收益
	 * @param realtime            币种行情
	 * @param order               矿机订单
	 */
	protected void updateProfitToWallet(double dayProfit, MinerOrder order, Miner miner, Date systemTime) {
		// 矿机购买时使用的币种，则产生对应的币种，转换成u
		String buyCurrency = miner.getBuyCurrency();
		String outputCurrency = miner.getOutputCurrency();
		
//		// U购买 产出U
//		if ("usdt".equals(buyCurrency) && "usdt".equals(outputCurrency)) {
//			symbolProfit = symbolProfit;
//		}
		
		// U购买 产出第三方币
		if ("usdt".equals(buyCurrency) && !"usdt".equals(outputCurrency)) {
			List<Realtime> realtimes = this.dataService.realtime(outputCurrency);
			if (null == realtimes || realtimes.size() <= 0) {
				throw new BusinessException("行情获取异常，稍后再试");
			}
			dayProfit = Arith.div(dayProfit, realtimes.get(0).getClose());
		}
		
		// 第三方币购买 产出U
		if (!"usdt".equals(buyCurrency) && "usdt".equals(outputCurrency)) {
			List<Realtime> realtimes = this.dataService.realtime(buyCurrency);
			if (null == realtimes || realtimes.size() <= 0) {
				throw new BusinessException("行情获取异常，稍后再试");
			}
			dayProfit = Arith.mul(dayProfit, realtimes.get(0).getClose());
		}
		
		// 第三方币购买 产出第三方币
		if (!"usdt".equals(buyCurrency) && !"usdt".equals(outputCurrency)) {
//			// A购买 产出A
//			if (buyCurrency.equals(outputCurrency)) {
//				symbolProfit = symbolProfit;
//			}
			// A购买 产出B
			if (!buyCurrency.equals(outputCurrency)) {
				List<Realtime> realtimes = this.dataService.realtime(buyCurrency);
				if (null == realtimes || realtimes.size() <= 0) {
					throw new BusinessException("行情获取异常，稍后再试");
				}
				dayProfit = Arith.mul(dayProfit, realtimes.get(0).getClose());
				
				List<Realtime> realtimeList = this.dataService.realtime(outputCurrency);
				if (null == realtimeList || realtimeList.size() <= 0) {
					throw new BusinessException("行情获取异常，稍后再试");
				}
				dayProfit = Arith.div(dayProfit, realtimeList.get(0).getClose());
			}
		}
		
		// 收益为USDT
		if ("usdt".equals(outputCurrency)) {
			Wallet wallet = this.walletService.saveWalletByPartyId(order.getPartyId());
			double amount_before = wallet.getMoney();
			this.walletService.update(wallet.getPartyId().toString(), dayProfit);

			MoneyLog moneylog = new MoneyLog();
			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
			moneylog.setAmount_before(amount_before);
			moneylog.setAmount(dayProfit);
			moneylog.setAmount_after(Arith.add(wallet.getMoney(), dayProfit));
			moneylog.setLog("矿机收益，订单号[" + order.getOrder_no() + "]");
			moneylog.setPartyId(order.getPartyId());
			moneylog.setWallettype(Constants.WALLET);
			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_MINER_PROFIT);
			moneylog.setCreateTime(systemTime);
			moneyLogService.save(moneylog);
		}
		// 非USDT币种收益
		else {
			WalletExtend walletExtend = walletService.saveExtendByPara(order.getPartyId(), outputCurrency);
			double amount_before = walletExtend.getAmount();
			this.walletService.updateExtend(order.getPartyId().toString(), outputCurrency, dayProfit);

			MoneyLog moneylog = new MoneyLog();
			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
			moneylog.setAmount_before(amount_before);
			moneylog.setAmount(dayProfit);
			moneylog.setAmount_after(Arith.add(walletExtend.getAmount(), dayProfit));
			moneylog.setLog("矿机收益，订单号[" + order.getOrder_no() + "]");
			moneylog.setPartyId(order.getPartyId());
			moneylog.setWallettype(outputCurrency);
			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_MINER_PROFIT);
			moneylog.setCreateTime(systemTime);
			moneyLogService.save(moneylog);
		}
	}
	
//	/**
//	 * 更新收益到钱包
//	 * 
//	 * @param miner_profit_symbol 指定币种
//	 * @param day_profit          当日收益
//	 * @param realtime            币种行情
//	 * @param order               矿机订单
//	 */
//	protected void updateProfitToWallet(String miner_profit_symbol, double day_profit, Realtime realtime,
//			MinerOrder order) {
//		// 矿机购买时使用的币种，则产生对应的币种，转换成u
//		String miner_buy_symbol = sysparaService.find("miner_buy_symbol").getValue();
//		if (StringUtils.isNotEmpty(miner_buy_symbol)) {
//			if (miner_buy_symbol.equals(miner_profit_symbol)) {// 两个相同，则行情直接可用，转化为U
//				day_profit = Arith.mul(day_profit, realtime.getClose());
//			} else {
//				List<Realtime> realtime_list = this.dataService.realtime(miner_buy_symbol);
//				Realtime realtimeBuySymbol = null;
//				if (realtime_list.size() > 0) {
//					realtimeBuySymbol = realtime_list.get(0);
//				}
//				day_profit = Arith.mul(day_profit, realtimeBuySymbol.getClose());
//			}
//		}
//		if (StringUtils.isNotEmpty(miner_profit_symbol)) {
//			/**
//			 * 非Usdt币种收益
//			 */
//
//			double get_symbol_volume = Arith.div(day_profit, realtime.getClose());
//
//			WalletExtend walletExtend = walletService.saveExtendByPara(order.getPartyId(), miner_profit_symbol);
//			double amount_before = walletExtend.getAmount();
////			walletExtend.setAmount(Arith.add(walletExtend.getAmount(), get_symbol_volume));
//			this.walletService.updateExtend(order.getPartyId().toString(), miner_profit_symbol, get_symbol_volume);
//			/*
//			 * 保存资金日志
//			 */
//			MoneyLog moneylog = new MoneyLog();
//			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
//			moneylog.setAmount_before(amount_before);
//			moneylog.setAmount(get_symbol_volume);
//			moneylog.setAmount_after(Arith.add(walletExtend.getAmount(), get_symbol_volume));
//			moneylog.setLog("矿机收益，订单号[" + order.getOrder_no() + "]");
//			moneylog.setPartyId(order.getPartyId());
//			moneylog.setWallettype(realtime.getSymbol());
//			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_MINER_PROFIT);
//			moneylog.setCreateTime(new Date());
//			moneyLogService.save(moneylog);
//		} else {
//			/**
//			 * 此为收益为usdt
//			 */
//			Wallet wallet = this.walletService.saveWalletByPartyId(order.getPartyId());
//			double amount_before = wallet.getMoney();
////			wallet.setMoney(Arith.add(wallet.getMoney(), day_profit));
//			this.walletService.update(wallet.getPartyId().toString(), day_profit);
//			/**
//			 * 保存资金日志
//			 */
//			MoneyLog moneylog = new MoneyLog();
//			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
//			moneylog.setAmount_before(amount_before);
//			moneylog.setAmount(day_profit);
//			moneylog.setAmount_after(Arith.add(wallet.getMoney(), day_profit));
//			moneylog.setLog("矿机收益，订单号[" + order.getOrder_no() + "]");
//			moneylog.setPartyId(order.getPartyId());
//			moneylog.setWallettype(Constants.WALLET);
//			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_MINER_PROFIT);
//			moneylog.setCreateTime(new Date());
//			moneyLogService.save(moneylog);
//		}
//	}

//	/**
//	 * 更新收益到钱包
//	 * 
//	 * @param miner_profit_symbol 指定币种
//	 * @param day_profit          当日收益
//	 * @param realtime            币种行情
//	 * @param order               矿机订单
//	 */
//	protected void updateProfitToWallet(String miner_profit_symbol, double day_profit, Realtime realtime,
//			MinerOrder order, Date systemTime) {
//		// 矿机购买时使用的币种，则产生对应的币种，转换成u
//		String miner_buy_symbol = sysparaService.find("miner_buy_symbol").getValue();
//		if (StringUtils.isNotEmpty(miner_buy_symbol)) {
//			if (miner_buy_symbol.equals(miner_profit_symbol)) {// 两个相同，则行情直接可用，转化为U
//				day_profit = Arith.mul(day_profit, realtime.getClose());
//			} else {
//				List<Realtime> realtime_list = this.dataService.realtime(miner_buy_symbol);
//				Realtime realtimeBuySymbol = null;
//				if (realtime_list.size() > 0) {
//					realtimeBuySymbol = realtime_list.get(0);
//				}
//				day_profit = Arith.mul(day_profit, realtimeBuySymbol.getClose());
//			}
//		}
//		if (StringUtils.isNotEmpty(miner_profit_symbol)) {
//			/**
//			 * 非Usdt币种收益
//			 */
//			double get_symbol_volume = Arith.div(day_profit, realtime.getClose());
//
//			WalletExtend walletExtend = walletService.saveExtendByPara(order.getPartyId(), miner_profit_symbol);
//			double amount_before = walletExtend.getAmount();
////			walletExtend.setAmount(Arith.add(walletExtend.getAmount(), get_symbol_volume));
//			this.walletService.updateExtend(order.getPartyId().toString(), miner_profit_symbol, get_symbol_volume);
//			/*
//			 * 保存资金日志
//			 */
//			MoneyLog moneylog = new MoneyLog();
//			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
//			moneylog.setAmount_before(amount_before);
//			moneylog.setAmount(get_symbol_volume);
//			moneylog.setAmount_after(Arith.add(walletExtend.getAmount(), get_symbol_volume));
//			moneylog.setLog("矿机收益，订单号[" + order.getOrder_no() + "]");
//			moneylog.setPartyId(order.getPartyId());
//			moneylog.setWallettype(realtime.getSymbol());
//			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_MINER_PROFIT);
//			moneylog.setCreateTime(systemTime);
//			moneyLogService.save(moneylog);
//		} else {
//			/**
//			 * 此为收益为usdt
//			 */
//			Wallet wallet = this.walletService.saveWalletByPartyId(order.getPartyId());
//			double amount_before = wallet.getMoney();
////			wallet.setMoney(Arith.add(wallet.getMoney(), day_profit));
//			this.walletService.update(wallet.getPartyId().toString(), day_profit);
//			/**
//			 * 保存资金日志
//			 */
//			MoneyLog moneylog = new MoneyLog();
//			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
//			moneylog.setAmount_before(amount_before);
//			moneylog.setAmount(day_profit);
//			moneylog.setAmount_after(Arith.add(wallet.getMoney(), day_profit));
//			moneylog.setLog("矿机收益，订单号[" + order.getOrder_no() + "]");
//			moneylog.setPartyId(order.getPartyId());
//			moneylog.setWallettype(Constants.WALLET);
//			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_MINER_PROFIT);
//			moneylog.setCreateTime(systemTime);
//			moneyLogService.save(moneylog);
//		}
//	}

	/**
	 * 批量更新订单收益
	 * 
	 * @param orderList
	 */
	protected void updateBatchMinerOrdersProfit(final List<MinerOrderMessage> orderList) {
		String sql = "UPDATE T_MINER_ORDER SET PROFIT=?,COMPUTE_DAY=? WHERE ORDER_NO=?";
		int[] batchUpdate = ApplicationUtil.getBean(JdbcTemplate.class).batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setDouble(1, orderList.get(i).getProfit());
				ps.setTimestamp(2, new Timestamp(orderList.get(i).getComputeDay().getTime()));
				ps.setString(3, orderList.get(i).getOrderNo());
			}

			@Override
			public int getBatchSize() {
				return orderList.size();
			}
		});

		logger.info("end miner batch update attr:{}", batchUpdate);
	}

	public void handleRecomProfit(String partyId, double profit, Miner miner, String miner_bonus_parameters) {
		if (miner.getTest()) {
			return;
		}
		List<UserRecom> list_parents = this.userRecomService.getParents(partyId);
		if (list_parents.size() == 0) {
			return;
		}
		String[] miner_bonus_array = miner_bonus_parameters.split(",");
		int loop = 0;
		int loopMax = miner_bonus_array.length;
		for (int i = 0; i < list_parents.size(); i++) {
			if (loop >= loopMax) {
				break;
			}
			Party party_parent = this.partyService.cachePartyBy(list_parents.get(i).getReco_id(), true);
			if (!Constants.SECURITY_ROLE_MEMBER.equals(party_parent.getRolename())) {
				continue;
			}
			loop++;
			Map<String, MinerOrder> map_party = (Map<String, MinerOrder>) redisHandler.get(MinerRedisKeys.MINER_ORDER_PARTY_ID + party_parent.getId().toString());
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
				 * 增加收益
				 */
				double pip_amount = Double.valueOf(miner_bonus_array[loop - 1]);
				double get_money = Arith.mul(profit, pip_amount);

				Double recom_profit = cacheRecomProfit.get(party_parent.getId().toString());
				cacheRecomProfit.put(party_parent.getId().toString(),
						Arith.add(recom_profit == null ? 0D : recom_profit, get_money));

			}

		}

	}

	/**
	 * 最终收益持久化数据库
	 */
	public void saveRecomProfit() {
		if (cacheRecomProfit.isEmpty())
			return;

		// 开始增加推荐人收益
		logger.info("start ------recom user miner profit,date:{},count:{}",
				new Object[] { new Date(), cacheRecomProfit.size() });
		for (Entry<String, Double> entry : cacheRecomProfit.entrySet()) {
			Wallet wallet = walletService.saveWalletByPartyId(entry.getKey());
			double amount_before = wallet.getMoney();
//			wallet.setMoney(Arith.add(wallet.getMoney(), entry.getValue()));
			walletService.update(wallet.getPartyId().toString(), entry.getValue());

			/**
			 * 保存资金日志
			 */
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
			moneyLog.setAmount_before(amount_before);
			moneyLog.setAmount(entry.getValue());
			moneyLog.setAmount_after(Arith.add(wallet.getMoney(), entry.getValue()));
			moneyLog.setLog("矿机推荐奖励金");
			moneyLog.setPartyId(entry.getKey());
			moneyLog.setWallettype(Constants.WALLET);
			moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_MINER_RECOM_PROFIT);
			moneyLogService.save(moneyLog);
			userDataService.saveMinerProfit(entry.getKey(), entry.getValue());
		}
		// 推荐人矿机收益计算完成，纪录日志
		logger.info("finish ------recom user miner profit,date:{},count:{}",
				new Object[] { new Date(), cacheRecomProfit.size() });
		// 处理完后收益清空

	}

	/**
	 * 最终收益持久化数据库
	 */
	public void saveRecomProfit(Date systemTime) {
		if (cacheRecomProfit.isEmpty())
			return;

		// 开始增加推荐人收益
		logger.info("start ------recom user miner profit,date:{},count:{}",
				new Object[] { new Date(), cacheRecomProfit.size() });
		for (Entry<String, Double> entry : cacheRecomProfit.entrySet()) {
			Wallet wallet = walletService.saveWalletByPartyId(entry.getKey());
			double amount_before = wallet.getMoney();
//			wallet.setMoney(Arith.add(wallet.getMoney(), entry.getValue()));
			walletService.update(wallet.getPartyId().toString(), entry.getValue());

			/**
			 * 保存资金日志
			 */
			MoneyLog moneyLog = new MoneyLog();
			moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_MINER);
			moneyLog.setAmount_before(amount_before);
			moneyLog.setAmount(entry.getValue());
			moneyLog.setAmount_after(Arith.add(wallet.getMoney(), entry.getValue()));
			moneyLog.setLog("矿机推荐奖励金");
			moneyLog.setPartyId(entry.getKey());
			moneyLog.setWallettype(Constants.WALLET);
			moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_MINER_RECOM_PROFIT);
			moneyLog.setCreateTime(systemTime);
			moneyLogService.save(moneyLog);
			userDataService.saveMinerProfit(entry.getKey(), entry.getValue());
		}
		// 推荐人矿机收益计算完成，纪录日志
		logger.info("finish ------recom user miner profit,date:{},count:{}",
				new Object[] { new Date(), cacheRecomProfit.size() });
		// 处理完后收益清空

	}

	public void cacheRecomProfitClear() {
		cacheRecomProfit.clear();
	}

	public void setMinerService(MinerService minerService) {
		this.minerService = minerService;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setUserDataService(UserDataService userDataService) {
		this.userDataService = userDataService;
	}

	public void setMoneyLogService(MoneyLogService moneyLogService) {
		this.moneyLogService = moneyLogService;
	}

	public void setMinerOrderService(MinerOrderService minerOrderService) {
		this.minerOrderService = minerOrderService;
	}

	public void setWalletService(WalletService walletService) {
		this.walletService = walletService;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}
}
