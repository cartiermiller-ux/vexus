package project.contract.internal;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import kernel.util.Arith;
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.Constants;
import project.contract.ContractApplyOrder;
import project.contract.ContractApplyOrderService;
import project.contract.ContractLock;
import project.contract.ContractOrder;
import project.contract.ContractOrderService;
import project.contract.ContractRedisKeys;
import project.data.model.Realtime;
import project.item.ItemService;
import project.item.model.Item;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.party.PartyService;
import project.party.model.Party;
import project.redis.RedisHandler;
import project.syspara.Syspara;
import project.syspara.SysparaService;
import project.tip.TipConstants;
import project.tip.TipService;
import project.user.UserDataService;
import project.wallet.AssetService;
import project.wallet.Wallet;
import project.wallet.WalletService;
import util.DateUtil;
import util.RandomUtil;
public class ContractOrderServiceImpl implements ContractOrderService {
	protected WalletService walletService;
	protected UserDataService userDataService;
	protected ItemService itemService;
	protected MoneyLogService moneyLogService;
	protected ContractApplyOrderService contractApplyOrderService;
	protected RedisHandler redisHandler;
	protected PartyService partyService;
	protected TipService tipService;
	protected AssetService assetService;

	public void saveOpen(ContractApplyOrder applyOrder, Realtime realtime) {
		Item item = this.itemService.cacheBySymbol(applyOrder.getSymbol(), false);

		ContractOrder order = new ContractOrder();
		order.setId(ApplicationUtil.getCurrentTimeUUID());
		order.setPartyId(applyOrder.getPartyId());
		order.setSymbol(applyOrder.getSymbol());
		order.setOrder_no(DateUtil.getToday("yyMMddHHmmss") + RandomUtil.getRandomNum(8));
		order.setDirection(applyOrder.getDirection());
		order.setLever_rate(applyOrder.getLever_rate());
		order.setVolume(applyOrder.getVolume());
		order.setVolume_open(applyOrder.getVolume_open());
		order.setUnit_amount(applyOrder.getUnit_amount());
		order.setFee(applyOrder.getFee());
		order.setDeposit(applyOrder.getDeposit());
		order.setDeposit_open(applyOrder.getDeposit());
		order.setTrade_avg_price(realtime.getClose());
		order.setStop_price_profit(applyOrder.getStop_price_profit());
		order.setStop_price_loss(applyOrder.getStop_price_loss());
		order.setPips(item.getPips());
		order.setPips_amount(item.getPips_amount());
		order.setCreate_time(new Date());

        ApplicationUtil.executeInsert(order);
		redisHandler.setSync(ContractRedisKeys.CONTRACT_ORDERNO + order.getOrder_no(), order);
		
		Map<String, ContractOrder> map = (Map<String, ContractOrder>) redisHandler.get(ContractRedisKeys.CONTRACT_SUBMITTED_ORDER_PARTY_ID + order.getPartyId().toString());
		if (map == null) map = new ConcurrentHashMap<String, ContractOrder>();
		map.put(order.getOrder_no(), order);
		redisHandler.setSync(ContractRedisKeys.CONTRACT_SUBMITTED_ORDER_PARTY_ID + order.getPartyId().toString(), map);
		// 获取单个订单的合约总资产、总保证金、总未实现盈利
		Map<String, Double> contractAssetsOrder = this.assetService.getMoneyContractByOrder(order);
		
		Double contractAssets = (Double) this.redisHandler.get(ContractRedisKeys.CONTRACT_ASSETS_PARTY_ID + order.getPartyId().toString());
		Double contractAssetsDeposit = (Double) this.redisHandler.get(ContractRedisKeys.CONTRACT_ASSETS_DEPOSIT_PARTY_ID + order.getPartyId().toString());
		Double contractAssetsProfit = (Double) this.redisHandler.get(ContractRedisKeys.CONTRACT_ASSETS_PROFIT_PARTY_ID + order.getPartyId().toString());
		
		this.redisHandler.setSync(ContractRedisKeys.CONTRACT_ASSETS_PARTY_ID + order.getPartyId().toString(), 
				Arith.add(null == contractAssets ? 0.000D : contractAssets, contractAssetsOrder.get("money_contract")));
		this.redisHandler.setSync(ContractRedisKeys.CONTRACT_ASSETS_DEPOSIT_PARTY_ID + order.getPartyId().toString(), 
				Arith.add(null == contractAssetsDeposit ? 0.000D : contractAssetsDeposit, contractAssetsOrder.get("money_contract_deposit")));		
		this.redisHandler.setSync(ContractRedisKeys.CONTRACT_ASSETS_PROFIT_PARTY_ID + order.getPartyId().toString(),  
				Arith.add(null == contractAssetsProfit ? 0.000D : contractAssetsProfit, contractAssetsOrder.get("money_contract_profit")));
		
		/**
		 * 进入市场
		 */
		applyOrder.setVolume(0D);
		applyOrder.setState(ContractApplyOrder.STATE_CREATED);

		this.contractApplyOrderService.update(applyOrder);
		
		Party party = this.partyService.cachePartyBy(order.getPartyId(), false);
		if (Constants.SECURITY_ROLE_MEMBER.equals(party.getRolename())) {
			tipService.saveTip(order.getId().toString(), TipConstants.CONTRACT_ORDER);
		}
	}

	public ContractApplyOrder saveClose(ContractApplyOrder applyOrder, Realtime realtime, String order_no) {
		ContractOrder order = this.findByOrderNo(order_no);
		if (order == null || !ContractOrder.STATE_SUBMITTED.equals(order.getState()) || order.getVolume() <= 0) {
			/**
			 * 状态已改变，退出处理
			 */
			return applyOrder;
		}
		double volume;
		if (applyOrder.getVolume() > order.getVolume()) {
			volume = order.getVolume();
		} else {
			volume = applyOrder.getVolume();
		}
		/**
		 * 平仓退回的金额
		 */
		double profit = this.settle(order, volume);
		update(order);
//		if (profit > 0) {
		Wallet wallet = this.walletService.saveWalletByPartyId(order.getPartyId());
		double amount_before = wallet.getMoney();

//		wallet.setMoney(Arith.add(wallet.getMoney(), profit));/
		if (Arith.add(wallet.getMoney(), profit) < 0) {
			profit = Arith.sub(0, wallet.getMoney());
		}

		this.walletService.update(wallet.getPartyId().toString(), profit);

		MoneyLog moneylog = new MoneyLog();
		moneylog.setCategory(Constants.MONEYLOG_CATEGORY_CONTRACT);
		moneylog.setAmount_before(amount_before);
		moneylog.setAmount(profit);
		moneylog.setAmount_after(Arith.add(wallet.getMoney(), profit));
		moneylog.setLog("平仓，平仓合约数[" + volume + "],订单号[" + order.getOrder_no() + "]");
		moneylog.setPartyId(order.getPartyId());
		moneylog.setWallettype(Constants.WALLET);
		moneylog.setContent_type(Constants.MONEYLOG_CONTENT_CONTRACT_CLOSE);

		moneyLogService.save(moneylog);

//		}
		applyOrder.setVolume(Arith.sub(applyOrder.getVolume(), volume));
		if (applyOrder.getVolume() <= 0) {
			applyOrder.setState(ContractApplyOrder.STATE_CREATED);
		}
		contractApplyOrderService.update(applyOrder);

		return applyOrder;

	}

	/**
	 * 根据用户批量赎回订单
	 * 
	 * @param partyId
	 */
	public void saveCloseRemoveAllByPartyId(String partyId) {
		List<ContractOrder> orders = ApplicationUtil.executeSelect(ContractOrder.class,"WHERE PARTY_ID=?",new Object[] {partyId});
		List<ContractOrder> findSubmittedContractOrders = findSubmitted(partyId, null, null);
		if (!CollectionUtils.isEmpty(findSubmittedContractOrders)) {
			for (ContractOrder order : orders) {
				if (ContractOrder.STATE_SUBMITTED.equals(order.getState())) {
					saveClose(order.getPartyId().toString(), order.getOrder_no());
				}
				redisHandler.remove(ContractRedisKeys.CONTRACT_ORDERNO + order.getOrder_no());
			}			
			redisHandler.remove(ContractRedisKeys.CONTRACT_SUBMITTED_ORDER_PARTY_ID + partyId);
			
			this.redisHandler.remove(ContractRedisKeys.CONTRACT_ASSETS_PARTY_ID + partyId);
			this.redisHandler.remove(ContractRedisKeys.CONTRACT_ASSETS_DEPOSIT_PARTY_ID + partyId);
			this.redisHandler.remove(ContractRedisKeys.CONTRACT_ASSETS_PROFIT_PARTY_ID + partyId);
		}
	}

	public ContractOrder saveClose(String partyId, String order_no) {
		/*
		 * 平仓
		 */
		ContractOrder order = this.findByOrderNo(order_no);
		if (order == null || !ContractOrder.STATE_SUBMITTED.equals(order.getState()) || !partyId.equals(order.getPartyId().toString()) || order.getVolume() <= 0) {
			return null; //状态已改变，退出处理
		}
		
		/**
		 * 收益
		 */
		double volume = order.getVolume();
		double profit = this.settle(order, order.getVolume());

		Wallet wallet = this.walletService.saveWalletByPartyId(order.getPartyId());
		double amount_before = wallet.getMoney();

		if (Arith.add(wallet.getMoney(), profit) < 0) {
			profit = Arith.sub(0, wallet.getMoney());
		}

		if (Arith.add(wallet.getMoney(), profit) < 0) {
			profit = Arith.sub(0, wallet.getMoney());
		}
		this.walletService.update(wallet.getPartyId().toString(), profit);

		MoneyLog moneylog = new MoneyLog();
		moneylog.setCategory(Constants.MONEYLOG_CATEGORY_CONTRACT);
		moneylog.setAmount_before(amount_before);
		moneylog.setAmount(profit);
		moneylog.setAmount_after(Arith.add(wallet.getMoney(), profit));
		moneylog.setLog("平仓，平仓合约数[" + volume + "],订单号[" + order.getOrder_no() + "]");
		moneylog.setPartyId(order.getPartyId());
		moneylog.setWallettype(Constants.WALLET);
		moneylog.setContent_type(Constants.MONEYLOG_CONTENT_CONTRACT_CLOSE);

		moneyLogService.save(moneylog);

		order.setState(ContractOrder.STATE_CREATED);
		order.setVolume(0D);
		order.setDeposit(0);
		order.setClose_time(new Date());
		update(order);

		/**
		 * 合约产品平仓后添加当前流水
		 */
		Party party = this.partyService.cachePartyBy(order.getPartyId(), false);
		party.setWithdraw_limit_now_amount(Arith.add(party.getWithdraw_limit_now_amount(), order.getDeposit_open()));
		partyService.update(party);

		return order;

	}

	public void update(ContractOrder order) {
		String id = null == order.getId() ? ApplicationUtil.getCurrentTimeUUID() : order.getId().toString();
		ApplicationUtil.executeInsertOrUpdate(order, "WHERE UUID=?", new Object[] {id});
		redisHandler.setSync(ContractRedisKeys.CONTRACT_ORDERNO + order.getOrder_no(), order);
		
		if (ContractOrder.STATE_SUBMITTED.equals(order.getState())) {
			Map<String, ContractOrder> map = (Map<String, ContractOrder>) redisHandler.get(ContractRedisKeys.CONTRACT_SUBMITTED_ORDER_PARTY_ID + order.getPartyId().toString());
			if (null == map) map = new ConcurrentHashMap<String, ContractOrder>();
			ContractOrder orderOld = map.get(order.getOrder_no());
			
			map.put(order.getOrder_no(), order);
			redisHandler.setSync(ContractRedisKeys.CONTRACT_SUBMITTED_ORDER_PARTY_ID + order.getPartyId().toString(), map);
			
			// 获取单个订单的合约总资产、总保证金、总未实现盈利
			Map<String, Double> contractAssetsOrder = this.assetService.getMoneyContractByOrder(order);
			Map<String, Double> contractAssetsOrderOld = this.assetService.getMoneyContractByOrder(orderOld);
			
			Double contractAssets = (Double) this.redisHandler.get(ContractRedisKeys.CONTRACT_ASSETS_PARTY_ID + order.getPartyId().toString());
			Double contractAssetsDeposit = (Double) this.redisHandler.get(ContractRedisKeys.CONTRACT_ASSETS_DEPOSIT_PARTY_ID + order.getPartyId().toString());
			Double contractAssetsProfit = (Double) this.redisHandler.get(ContractRedisKeys.CONTRACT_ASSETS_PROFIT_PARTY_ID + order.getPartyId().toString());
			
			this.redisHandler.setSync(ContractRedisKeys.CONTRACT_ASSETS_PARTY_ID + order.getPartyId().toString(), 
					Arith.add(null == contractAssets ? 0.000D : contractAssets, contractAssetsOrder.get("money_contract") - contractAssetsOrderOld.get("money_contract")));
			this.redisHandler.setSync(ContractRedisKeys.CONTRACT_ASSETS_DEPOSIT_PARTY_ID + order.getPartyId().toString(), 
					Arith.add(null == contractAssetsDeposit ? 0.000D : contractAssetsDeposit, contractAssetsOrder.get("money_contract_deposit") - contractAssetsOrderOld.get("money_contract_deposit")));		
			this.redisHandler.setSync(ContractRedisKeys.CONTRACT_ASSETS_PROFIT_PARTY_ID + order.getPartyId().toString(),  
					Arith.add(null == contractAssetsProfit ? 0.000D : contractAssetsProfit, contractAssetsOrder.get("money_contract_profit") - contractAssetsOrderOld.get("money_contract_profit")));
			
		} else if (ContractOrder.STATE_CREATED.equals(order.getState())) { // 平仓后，移除持仓列表
			Map<String, ContractOrder> map = (Map<String, ContractOrder>) redisHandler.get(ContractRedisKeys.CONTRACT_SUBMITTED_ORDER_PARTY_ID + order.getPartyId().toString());
			ContractOrder orderOld = null;
			if (map != null && !map.isEmpty()) {
				orderOld = map.get(order.getOrder_no());
				map.remove(order.getOrder_no());
			}
			redisHandler.setSync(ContractRedisKeys.CONTRACT_SUBMITTED_ORDER_PARTY_ID + order.getPartyId().toString(), map);
			
			// 获取单个订单的合约总资产、总保证金、总未实现盈利
			Map<String, Double> contractAssetsOrderOld = this.assetService.getMoneyContractByOrder(orderOld);
			
			Double contractAssets = (Double) this.redisHandler.get(ContractRedisKeys.CONTRACT_ASSETS_PARTY_ID + order.getPartyId().toString());
			Double contractAssetsDeposit = (Double) this.redisHandler.get(ContractRedisKeys.CONTRACT_ASSETS_DEPOSIT_PARTY_ID + order.getPartyId().toString());
			Double contractAssetsProfit = (Double) this.redisHandler.get(ContractRedisKeys.CONTRACT_ASSETS_PROFIT_PARTY_ID + order.getPartyId().toString());
			
			this.redisHandler.setSync(ContractRedisKeys.CONTRACT_ASSETS_PARTY_ID + order.getPartyId().toString(), 
					Arith.add(null == contractAssets ? 0.000D : contractAssets, 0.000D - contractAssetsOrderOld.get("money_contract")));
			this.redisHandler.setSync(ContractRedisKeys.CONTRACT_ASSETS_DEPOSIT_PARTY_ID + order.getPartyId().toString(), 
					Arith.add(null == contractAssetsDeposit ? 0.000D : contractAssetsDeposit, 0.000D - contractAssetsOrderOld.get("money_contract_deposit")));		
			this.redisHandler.setSync(ContractRedisKeys.CONTRACT_ASSETS_PROFIT_PARTY_ID + order.getPartyId().toString(),  
					Arith.add(null == contractAssetsProfit ? 0.000D : contractAssetsProfit, 0.000D - contractAssetsOrderOld.get("money_contract_profit")));
			
			// 平仓则纪录数据（委托平仓，订单直接平仓）
			this.userDataService.saveClose(order);
			Party party = this.partyService.cachePartyBy(order.getPartyId(), false);
			if (Constants.SECURITY_ROLE_MEMBER.equals(party.getRolename())) {
				tipService.deleteTip(order.getId().toString());
			}
		}
	}

	/**
	 * 收益结算，平仓时计算
	 * 
	 * @param closevolume 平仓的张数
	 */
	public double settle(ContractOrder order, double volume) {
		double profit = 0;
		/**
		 * 平仓比率
		 */
		double rate = Arith.div(volume, order.getVolume_open());

		//profit = Arith.mul(Arith.add(order.getDeposit(), order.getProfit()), rate);
		profit = Arith.add(Arith.mul(order.getDeposit_open(),rate),Arith.mul(order.getProfit(),rate));
		
		order.setAmount_close(Arith.add(order.getAmount_close(), profit));

		order.setVolume(Arith.sub(order.getVolume(), volume));
		order.setDeposit(Arith.sub(order.getDeposit(), Arith.mul(order.getDeposit_open(), rate)));

		if (order.getVolume() <= 0) {
			order.setState(ContractOrder.STATE_CREATED);
			order.setClose_time(new Date());
		}

		return profit;

	}

	public ContractOrder findByOrderNo(String order_no) {
		ContractOrder order = (ContractOrder) this.redisHandler.get(ContractRedisKeys.CONTRACT_ORDERNO + order_no);
		if (null != order) return order;
		
		List<ContractOrder> list = ApplicationUtil.executeSelect(ContractOrder.class,"WHERE ORDER_NO=?",new Object[] {order_no});
		return list.size()<=0?null:list.get(0);
	}

	public List<ContractOrder> findSubmitted(String partyId, String symbol, String direction) {
		if (!StringUtils.isNullOrEmpty(partyId)) {// 如果有partyId，走缓存查询
			Map<String, ContractOrder> map = (Map<String, ContractOrder>) redisHandler.get(ContractRedisKeys.CONTRACT_SUBMITTED_ORDER_PARTY_ID + partyId);
			List<ContractOrder> list = new ArrayList<ContractOrder>();
			if (map != null && !map.isEmpty()) {
				for (ContractOrder order : map.values()) {
					boolean valify = true;// 验证
					if (valify && !StringUtils.isNullOrEmpty(symbol)) {// 币种是否相同
						valify = symbol.equals(order.getSymbol());
					}
					if (valify && !StringUtils.isNullOrEmpty(direction)) {
						valify = direction.equals(order.getDirection());
					}
					if (valify) {// 条件全部满足，添加
						list.add(order);
					}
				}
			}
			return list;
		}
		
		Page page = new Page(1,Integer.MAX_VALUE,Integer.MAX_VALUE);
		StringBuilder whereSql=new StringBuilder("WHERE 1=1 ");
		ArrayList<Object> params=new ArrayList<Object>();
		
		if (!StringUtils.isNullOrEmpty(partyId)) {
			whereSql.append("AND PARTY_ID=? ");
			params.add(partyId);
		}

		if (!StringUtils.isNullOrEmpty(symbol)) {
			whereSql.append("AND SYMBOL=? ");
			params.add(symbol);
		}
		
		if (!StringUtils.isNullOrEmpty(direction)) {
			whereSql.append("AND DIRECTION=? ");
			params.add(direction);
		}

		whereSql.append("AND STATE=? LIMIT ?,?");
		params.add("submitted");
		params.add(page.getFirstElementNumber());
		params.add(page.getPageSize());
		
		return ApplicationUtil.executeSelect(ContractOrder.class,whereSql.toString(),params.toArray(new Object[params.size()]));
	}

	public List<Map<String, Object>> getPaged(int pageNo, int pageSize, String partyId, String symbol, String type,String startTime, String endTime) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		StringBuilder whereSql=new StringBuilder("WHERE PARTY_ID=? ");
		ArrayList<Object> params=new ArrayList<Object>();
		params.add(partyId);
		
		if (!StringUtils.isNullOrEmpty(symbol)) {
			whereSql.append("AND SYMBOL=? ");
			params.add(symbol);
		}

		if ("orders".equals(type)) {
			whereSql.append("AND STATE=? ");
			params.add("submitted");
		} else if ("hisorders".equals(type)) {
			whereSql.append("AND STATE=? ");
			params.add("created");
		}
		
		if (!StringUtils.isNullOrEmpty(startTime)) {
			whereSql.append("AND DATE(CREATE_TIME)>=DATE('"+startTime+" 00:00:00') ");
		}
		
		if (!StringUtils.isNullOrEmpty(endTime)) {
			whereSql.append("AND DATE(CREATE_TIME)<=DATE('"+endTime+" 23:59:59') ");
		}
		
		whereSql.append("ORDER BY CREATE_TIME DESC LIMIT ?,?");
		params.add(page.getFirstElementNumber());
		params.add(pageSize);
		
		return bulidData(ApplicationUtil.executeSelect(ContractOrder.class,whereSql.toString(),params.toArray(new Object[params.size()])));
	}

	public List<Map<String, Object>> findSubmittedRedis(String partyId, String symbol) {
		List<ContractOrder> ordersList = new ArrayList<ContractOrder>();
		Map<String, ContractOrder> ordersMap = (Map<String, ContractOrder>) this.redisHandler.get(ContractRedisKeys.CONTRACT_SUBMITTED_ORDER_PARTY_ID + partyId);
		if (null != ordersMap && 0 != ordersMap.size()) {
			for (Entry<String, ContractOrder> entry : ordersMap.entrySet()) {
				if (StringUtils.isNotEmpty(symbol)) {
					if (symbol.equals(entry.getValue().getSymbol())) {
						ordersList.add(entry.getValue());								
					}
				} else {
					ordersList.add(entry.getValue());
				}
			}
			
			// 创建时间 降序排列			
			if (ordersList.size() > 0) {
				Collections.sort(ordersList, new Comparator<ContractOrder>() {
					@Override
					public int compare(ContractOrder o1, ContractOrder o2) {
						Date date1 = o1.getCreate_time();
						Date date2 = o2.getCreate_time();
						return -date1.compareTo(date2);
					}
				});
			}
		}

		return this.bulidData(ordersList);
	}

	private List<Map<String, Object>> bulidData(List<ContractOrder> list) {
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < list.size(); i++) {
			ContractOrder order = list.get(i);
			Map<String, Object> map = bulidOne(order);
			data.add(map);
		}
		return data;
	}

	public Map<String, Object> bulidOne(ContractOrder order) {
		DecimalFormat df = new DecimalFormat("#.##");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("order_no", order.getOrder_no());
		map.put("name", itemService.cacheBySymbol(order.getSymbol(), false).getName());
		map.put("symbol", order.getSymbol());
		map.put("create_time", DateUtils.format(order.getCreate_time(), DateUtils.DF_yyyyMMddHHmmss));
		if (order.getClose_time() != null) {
			map.put("close_time", DateUtils.format(order.getClose_time(), DateUtils.DF_yyyyMMddHHmmss));
		} else {
			map.put("close_time", "");
		}

		map.put("direction", order.getDirection());
		map.put("lever_rate", order.getLever_rate());
		map.put("trade_avg_price", order.getTrade_avg_price());
		map.put("close_avg_price", order.getClose_avg_price());
		map.put("stop_price_profit", order.getStop_price_profit());
		map.put("stop_price_loss", order.getStop_price_loss());
		map.put("state", order.getState());
		map.put("amount", Arith.mul(order.getVolume(), order.getUnit_amount()));
		map.put("amount_open", Arith.mul(order.getVolume_open(), order.getUnit_amount()));
		map.put("fee", order.getFee());
		map.put("deposit", order.getDeposit());
		map.put("deposit_open", order.getDeposit_open());
		map.put("change_ratio", order.getChange_ratio());
		if(order.getAmount_close() < 0) {
			order.setAmount_close(0);
		}
		/**
		 * 收益
		 */
		if (ContractOrder.STATE_SUBMITTED.equals(order.getState())) {
			double rate = Arith.div(order.getVolume(), order.getVolume_open());
			map.put("profit",df.format(Arith.sub(
							Arith.add(Arith.add(order.getAmount_close(),Arith.mul(order.getProfit(),rate)), order.getDeposit()),
							order.getDeposit_open())));
		} else {
			map.put("profit", df.format(Arith.sub(Arith.add(order.getAmount_close(), order.getDeposit()), order.getDeposit_open())));
		}

		map.put("volume", order.getVolume());
		map.put("volume_open", order.getVolume_open());

		return map;
	}

	@Override
	public List<ContractOrder> findSubmitted() {
		return ApplicationUtil.executeSelect(ContractOrder.class,"WHERE STATE=?",new Object[] {ContractOrder.STATE_SUBMITTED});
	}

	public List<ContractOrder> findByPartyIdAndToday(String partyId) {
		return ApplicationUtil.executeSelect(ContractOrder.class,"WHERE PARTY_ID=? AND DATEDIFF(CREATE_TIME,NOW())=0",new Object[] {partyId});
	}

	public void setWalletService(WalletService walletService) {
		this.walletService = walletService;
	}

	public void setUserDataService(UserDataService userDataService) {
		this.userDataService = userDataService;
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}

	public void setMoneyLogService(MoneyLogService moneyLogService) {
		this.moneyLogService = moneyLogService;
	}

	public void setContractApplyOrderService(ContractApplyOrderService contractApplyOrderService) {
		this.contractApplyOrderService = contractApplyOrderService;
	}

	@Override
	public boolean lock(String order_no) {
		return ContractLock.add(order_no);
	}

	@Override
	public void unlock(String order_no) {
		ContractLock.remove(order_no);
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}
	
	public void setTipService(TipService tipService) {
		this.tipService = tipService;
	}
	
	public void setAssetService(AssetService assetService) {
		this.assetService = assetService;
	}
}
