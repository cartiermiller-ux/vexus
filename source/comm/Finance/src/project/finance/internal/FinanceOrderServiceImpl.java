package project.finance.internal;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.bo.RecordObjectMapper;
import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.Constants;
import project.finance.Finance;
import project.finance.FinanceOrder;
import project.finance.FinanceOrderService;
import project.finance.FinanceService;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.party.PartyService;
import project.party.model.Party;
import project.party.model.UserRecom;
import project.party.recom.UserRecomService;
import project.syspara.SysparaService;
import project.user.UserDataService;
import project.wallet.Wallet;
import project.wallet.WalletService;

public class FinanceOrderServiceImpl implements FinanceOrderService {
	
	protected JdbcTemplate jdbcTemplate;
	
	protected PartyService partyService;
	
	protected WalletService walletService;
	
	protected FinanceService financeService;
	
	protected SysparaService sysparaService;
	
	protected MoneyLogService moneyLogService;
	
	protected UserDataService userDataService;
	
	protected UserRecomService userRecomService;
	
	public void saveCreate(FinanceOrder entity) {

		entity.setCreate_time(new Date());

		// 加入周期
		Finance finance = financeService.findById(entity.getFinanceId());
		entity.setCycle(finance.getCycle());

		// 买入金额需要在区间内
		if (entity.getAmount() > finance.getInvestment_max() || entity.getAmount() < finance.getInvestment_min()) {
			throw new BusinessException("金额错误");

		}

		// 查看余额
		Wallet wallet = this.walletService.saveWalletByPartyId(entity.getPartyId());
		double amount_before = wallet.getMoney();
		if (wallet.getMoney() < entity.getAmount()) {
			throw new BusinessException("余额不足");
		}

		this.walletService.update(wallet.getPartyId().toString(), Arith.sub(0, entity.getAmount()));

		// 起息时间=确认时间加1天
		Date date = new Date();// 取时间
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		// 把日期往后增加一天.整数往后推,负数往前移动
		calendar.add(Calendar.DATE, 1);
		// 这个时间就是日期往后推一天的结果
		date = calendar.getTime(); 
		entity.setEarn_time(date);

		// 截止时间=起息时间+周期+1
		int days = (int) Arith.sub(entity.getCycle(), 1);
		calendar.add(Calendar.DATE, days);
		date = calendar.getTime();
		entity.setStop_time(date);

		// 默认的赎回时间=截止时间+1
//		calendar.add(calendar.DATE, 1);
//		date = calendar.getTime();
		int dateHour = DateUtils.getHour(date);
		if (dateHour < 4) {
			entity.setClose_time(DateUtils.addHour(date, 4));
		} else {
			entity.setClose_time(date);
		}

		// 保存资金日志
		MoneyLog moneylog = new MoneyLog();
		moneylog.setCategory(Constants.MONEYLOG_CATEGORY_FINANCE);
		moneylog.setAmount_before(amount_before);
		moneylog.setAmount(Arith.sub(0, entity.getAmount()));
		moneylog.setAmount_after(Arith.sub(wallet.getMoney(), entity.getAmount()));
		moneylog.setLog("购买理财产品，订单号[" + entity.getOrder_no() + "]");
		moneylog.setPartyId(entity.getPartyId());
		moneylog.setWallettype(Constants.WALLET);
		moneylog.setContent_type(Constants.MONEYLOG_CONTENT_FINANCE_BUY);
		moneyLogService.save(moneylog);

		entity.setId(ApplicationUtil.getCurrentTimeUUID());
        Object[] jdbcParams = ApplicationUtil.getInsertStatement(entity);
        String insertUserSql = (String)jdbcParams[0];
        Object[] sqlParameters = (Object[])jdbcParams[1];
        jdbcTemplate.update(insertUserSql,sqlParameters);
        
		// 购买理财产品后是否需要增加用户提现流水，1不增加，2要增加(coinspace,cqpro,amex,emex)
		String finance_buy_add_userdata = this.sysparaService.find("finance_buy_add_userdata").getValue();

		// 理财购买后给他增加提现流水限制金额
		if ("2".equals(finance_buy_add_userdata)) {
			Party party = this.partyService.cachePartyBy(entity.getPartyId(), false);
			party.setWithdraw_limit_amount(Arith.add(party.getWithdraw_limit_amount(), entity.getAmount()));
			partyService.update(party);
		}
	}

	public void saveClose(FinanceOrder entity, Date systemTime) {

		double get_amount = Arith.add(entity.getAmount(), entity.getProfit());
		if (get_amount < 0) {
			entity.setProfit(Arith.sub(0, entity.getAmount()));
			get_amount = Arith.add(entity.getAmount(), entity.getProfit());
		}

		if (get_amount > 0) {
			Wallet wallet = this.walletService.saveWalletByPartyId(entity.getPartyId());
			double amount_before = wallet.getMoney();
			this.walletService.update(wallet.getPartyId().toString(), get_amount);
			// 保存资金日志
			MoneyLog moneylog = new MoneyLog();
			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_FINANCE);
			moneylog.setAmount_before(amount_before);
			moneylog.setAmount(Arith.add(0, get_amount));
			moneylog.setAmount_after(Arith.add(wallet.getMoney(), get_amount));
			moneylog.setLog("赎回理财产品，订单号[" + entity.getOrder_no() + "]");
			moneylog.setPartyId(entity.getPartyId());
			moneylog.setWallettype(Constants.WALLET);
			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_FINANCE_BACK);
			moneylog.setCreateTime(systemTime != null ? systemTime : new Date());
			moneyLogService.save(moneylog);
		}
		updateFinanceOrder(entity);
		this.userDataService.saveSellFinance(entity);
	}

	public void saveClose(FinanceOrder entity) {
		double get_amount = Arith.add(entity.getAmount(), entity.getProfit());
		if (get_amount < 0) {
			entity.setProfit(Arith.sub(0, entity.getAmount()));
			get_amount = Arith.add(entity.getAmount(), entity.getProfit());
		}

		if (get_amount > 0) {
			Wallet wallet = this.walletService.saveWalletByPartyId(entity.getPartyId());
			double amount_before = wallet.getMoney();
			this.walletService.update(wallet.getPartyId().toString(), get_amount);
			// 保存资金日志
			MoneyLog moneylog = new MoneyLog();
			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_FINANCE);
			moneylog.setAmount_before(amount_before);
			moneylog.setAmount(Arith.add(0, get_amount));
			moneylog.setAmount_after(Arith.add(wallet.getMoney(), get_amount));
			moneylog.setLog("赎回理财产品，订单号[" + entity.getOrder_no() + "]");
			moneylog.setPartyId(entity.getPartyId());
			moneylog.setWallettype(Constants.WALLET);
			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_FINANCE_BACK);
			moneylog.setCreateTime(new Date());
			moneyLogService.save(moneylog);
		}
		updateFinanceOrder(entity);
		this.userDataService.saveSellFinance(entity);
	}

	@Override
	public FinanceOrder findByOrder_no(String order_no) {
		List<FinanceOrder> list = jdbcTemplate.query("SELECT * FROM T_FINANCE_ORDER WHERE ORDER_NO = ?", 
				RecordObjectMapper.newInstance(FinanceOrder.class), order_no);
		if (list.size() > 0)
			return list.get(0);
		return null;
	}

	public List<FinanceOrder> findByState(String partyId, String state) {
		/**
		 * 如果是查询已赎回的，则将提前赎回和正常赎回的都查出来
		 */
		List<FinanceOrder> list;
		if ("2".equals(state)) {
			list = jdbcTemplate.query("SELECT * FROM T_FINANCE_ORDER WHERE PARTY_ID=? AND STATE=? OR STATE=?", 
					RecordObjectMapper.newInstance(FinanceOrder.class), partyId, "0", "2");
		}
		if ("0".equals(state) || "1".equals(state)) {
			list = jdbcTemplate.query("SELECT * FROM T_FINANCE_ORDER WHERE STATE=? AND PARTY_ID=?", 
					RecordObjectMapper.newInstance(FinanceOrder.class), state, partyId);
		} else {
			list = jdbcTemplate.query("SELECT * FROM T_FINANCE_ORDER WHERE PARTY_ID=?", 
					RecordObjectMapper.newInstance(FinanceOrder.class), partyId);
		}

		if (list.size() > 0)
			return list;
		return null;
	}

	public Page pagedQuery(int pageNo, int pageSize, String partyId, String state) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		StringBuilder whereBuilder=new StringBuilder("WHERE PARTY_ID=? ");
		ArrayList<Object> params=new ArrayList<Object>();
		params.add(partyId);
		
		if (StringUtils.isNotEmpty(state)) {
			if ("2".equals(state)) {
				whereBuilder.append("AND (STATE='0' OR STATE='2') ");
			} else if ("02".equals(state)) {
				whereBuilder.append("AND STATE='2' ");
			} else {
				whereBuilder.append("AND STATE=? ");
				params.add(state);
			}
		}
		
		whereBuilder.append("ORDER BY CREATE_TIME DESC LIMIT ?,?");
		params.add(page.getFirstElementNumber());
		params.add(pageSize);
		
		List<FinanceOrder> list=ApplicationUtil.executeSelect(FinanceOrder.class,whereBuilder.toString(),params.toArray(new Object[params.size()]));
		page.setElements(list);
		return page;
	}

	/**
	 * 根据日期获取到当日的购买订单
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @param date
	 * @return
	 */
	public Page pagedQueryByDate(int pageNo, int pageSize, String date) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		List<FinanceOrder> list=ApplicationUtil.executeSelect(FinanceOrder.class,"WHERE DATE(CREATE_TIME)=DATE(?) ORDER BY CREATE_TIME ASC LIMIT ?,?",new Object[] {date,page.getFirstElementNumber(),pageSize});
		page.setElements(list);
		return page;
	}

	@Override
	public List<FinanceOrder> getAllStateBy_1() {
		List<FinanceOrder> list = jdbcTemplate.query("SELECT * FROM T_FINANCE_ORDER WHERE STATE = '1' ", RecordObjectMapper.newInstance(FinanceOrder.class));
		if (list.size() > 0) {
			return list;
		}
		return null;
	}

	public void addListProfit(FinanceOrder order) {
		/**
		 * 截止时间要大于现在这个时间则计算收益 赎回时间如果小于现在时间则不计算收益
		 */
		List<Finance> finances = financeService.findAll();
		Finance finance = new Finance();
		Date now_date = new Date();
		String date_string = DateUtils.format(now_date, DateUtils.DF_yyyyMMdd);

		/**
		 * 现在时间是否大于赎回时间，如果大于等于，则赎回，不计算收益了,并将状态改变为已赎回 只计算年月日
		 */
		String order_earn = DateUtils.format(order.getEarn_time(), DateUtils.DF_yyyyMMdd);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date_now = null;
		Date earn_date = null;
		try {
			date_now = dateFormat.parse(date_string);
			earn_date = dateFormat.parse(order_earn);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		/**
		 * 理财收益赎回时统一下发1还是每日下发2
		 */
		String finance_profit_days = sysparaService.find("finance_profit_days").getValue();

		if ("1".equals(finance_profit_days)) {

			if (now_date.after(order.getClose_time())) {
				order.setState("0");
				saveClose(order);
				return;
			}
			// 如果起息时间earn_date < 现在时间date_now 则 arrive_close > 0
			// 起息时间要大于等于今天
			int arrive_earn = date_now.compareTo(earn_date);
			if (arrive_earn < 0) {
				return;
			}

			for (int j = 0; j < finances.size(); j++) {
				finance = finances.get(j);
				if (finance.getId().equals(order.getFinanceId())) {
					break;
				}
			}
			// 当日获取的收益
			double today_rate = Arith.mul(finance.getToday_rate(), 0.01);
			double get_amount = Arith.mul(order.getAmount(), today_rate);
			order.setProfit(Arith.add(order.getProfit(), get_amount));
			updateFinanceOrder(order);
			
			if (DateUtils.addDay(now_date, 1).after(order.getClose_time())) {
		        order.setState("0");
		        saveClose(order);
		    }
		}

		if ("2".equals(finance_profit_days)) {

			if (now_date.after(order.getClose_time())) {
				order.setState("0");
				// 自动赎回调用,只返回本金
				saveClosePrincipal(order);
				return;
			}
			// 如果起息时间earn_date < 现在时间date_now 则 arrive_close > 0
			/**
			 * 起息时间要大于等于今天
			 */
			int arrive_earn = date_now.compareTo(earn_date);
			if (arrive_earn < 0) {
				return;
			}

			for (int j = 0; j < finances.size(); j++) {
				finance = finances.get(j);
				if (finance.getId().equals(order.getFinanceId())) {
					break;
				}
			}
			/**
			 * 当日获取的收益
			 */
			double today_rate = Arith.mul(finance.getToday_rate(), 0.01);
			double get_amount = Arith.mul(order.getAmount(), today_rate);

			// 5,0.5,3,0.3,2,0.2
			String finance_level_profit = sysparaService.find("finance_level_profit").getValue();
			boolean finance_level_profit_open = StringUtils.isNotEmpty(finance_level_profit);
			// 判断是否有理财推荐奖励，如果有，需要扣除30%给推荐人
			String finance_bonus_parameters = "";
			finance_bonus_parameters = this.sysparaService.find("finance_bonus_parameters").getValue();
			if ((finance_bonus_parameters != null && !"".equals(finance_bonus_parameters))
					|| finance_level_profit_open) {
				String[] finance_bonus_array = StringUtils.isNotEmpty(finance_bonus_parameters)
						? finance_bonus_parameters.split(",")
						: new String[] {};
				List<UserRecom> list_parents = this.userRecomService.getParents(order.getPartyId());

				if (CollectionUtils.isNotEmpty(list_parents)) {

					int loop = 0;
					for (int i = 0; i < list_parents.size(); i++) {
						if (finance_level_profit_open) {// 等级奖励只给推荐一人
							if (loop >= 1) {
								break;
							}
						} else {
							if (loop >= 3) {
								break;
							}
						}
						Party party_parent = this.partyService.cachePartyBy(list_parents.get(i).getReco_id(), true);
						if (!Constants.SECURITY_ROLE_MEMBER.equals(party_parent.getRolename())) {
							continue;
						}
						get_amount = Arith.mul(order.getAmount(), today_rate);
						/**
						 * 增加推荐人理财收益
						 */
						double parent_get_money = 0d;
						if (finance_level_profit_open) {
							parent_get_money = levelProfit(party_parent.getId().toString(), get_amount,
									finance_level_profit);
						} else {
							double finance_pip_amount = Double.valueOf(finance_bonus_array[loop]);
							parent_get_money = Arith.mul(get_amount, finance_pip_amount);
						}
						if (parent_get_money == 0d) {
							break;
						}

						Wallet wallet_parent = walletService.saveWalletByPartyId(party_parent.getId().toString());
						double amount_before_parent = wallet_parent.getMoney();
						walletService.update(wallet_parent.getPartyId().toString(), parent_get_money);
						/**
						 * 保存资金日志
						 */
						MoneyLog moneyLog = new MoneyLog();
						moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_FINANCE);
						moneyLog.setAmount_before(amount_before_parent);
						moneyLog.setAmount(parent_get_money);
						moneyLog.setAmount_after(Arith.add(wallet_parent.getMoney(), parent_get_money));
						moneyLog.setLog("第" + (i + 1) + "代下级用户，每日理财收益奖励金");
						moneyLog.setPartyId(party_parent.getId().toString());
						moneyLog.setWallettype(Constants.WALLET);
						moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_FINANCE_RECOM_PROFIT);
						moneyLogService.save(moneyLog);

						loop++;
					}
				}
				/**
				 * 理财收益减少百分之30给上级
				 */
				if (!finance_level_profit_open) {// 等级奖励则无需扣除本人收益
					get_amount = Arith.sub(get_amount, Arith.mul(order.getAmount(), 0.3));
				}
			}

			Wallet wallet = this.walletService.saveWalletByPartyId(order.getPartyId());
			double amount_before = wallet.getMoney();
			this.walletService.update(wallet.getPartyId().toString(), get_amount);
			// 保存资金日志
			MoneyLog moneylog = new MoneyLog();
			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_FINANCE);
			moneylog.setAmount_before(amount_before);
			moneylog.setAmount(Arith.add(0, get_amount));
			moneylog.setAmount_after(Arith.add(wallet.getMoney(), get_amount));
			moneylog.setLog("理财产品每日收益，订单号[" + order.getOrder_no() + "]");
			moneylog.setPartyId(order.getPartyId());
			moneylog.setWallettype(Constants.WALLET);
			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_FINANCE_PROFIT);
			moneyLogService.save(moneylog);

			order.setProfit(Arith.add(order.getProfit(), get_amount));
			updateFinanceOrder(order);
			
			if (DateUtils.addDay(now_date, 1).after(order.getClose_time())) {
		        order.setState("0");
		        saveClosePrincipal(order);
		    }
		}
	}

	public void addListProfit(FinanceOrder order, Date systemTime) {

		/**
		 * 截止时间要大于现在这个时间则计算收益 赎回时间如果小于现在时间则不计算收益
		 */
		List<Finance> finances = financeService.findAll();
		Finance finance = new Finance();
		Date now_date = systemTime != null ? systemTime : new Date();
		String date_string = DateUtils.format(now_date, DateUtils.DF_yyyyMMdd);

		/**
		 * 现在时间是否大于赎回时间，如果大于等于，则赎回，不计算收益了,并将状态改变为已赎回 只计算年月日
		 */
		String order_earn = DateUtils.format(order.getEarn_time(), DateUtils.DF_yyyyMMdd);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date_now = null;
		Date earn_date = null;
		try {
			date_now = dateFormat.parse(date_string);
			earn_date = dateFormat.parse(order_earn);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		/**
		 * 理财收益赎回时统一下发1还是每日下发2
		 */
		String finance_profit_days = sysparaService.find("finance_profit_days").getValue();

		/**
		 * 1计算收益，赎回统一发放
		 */
		if ("1".equals(finance_profit_days)) {
			// 如果赎回时间close_date < 现在时间date_now 则 arrive_close > 0
			if (now_date.after(order.getClose_time())) {
				order.setState("0");
				saveClose(order, systemTime);
				return;
			}
			// 如果起息时间earn_date < 现在时间date_now 则 arrive_close > 0
			/**
			 * 起息时间要大于等于今天
			 */
			int arrive_earn = date_now.compareTo(earn_date);
			if (arrive_earn < 0) {
				return;
			}

			for (int j = 0; j < finances.size(); j++) {
				finance = finances.get(j);
				if (finance.getId().equals(order.getFinanceId())) {
					break;
				}
			}
			// 当日获取的收益
			double today_rate = Arith.mul(finance.getToday_rate(), 0.01);
			double get_amount = Arith.mul(order.getAmount(), today_rate);
			order.setProfit(Arith.add(order.getProfit(), get_amount));
			updateFinanceOrder(order);

			if (DateUtils.addDay(now_date, 1).after(order.getClose_time())) {
		        order.setState("0");
		        saveClose(order);
		    }
		}

		/**
		 * 2每日下发收益
		 */
		if ("2".equals(finance_profit_days)) {

			// 如果赎回时间close_date < 现在时间date_now 则 arrive_close > 0
			if (now_date.after(order.getClose_time())) {
				order.setState("0");
				/**
				 * 自动赎回调用,只返回本金
				 */
				saveClosePrincipal(order);
				return;
			}
			// 如果起息时间earn_date < 现在时间date_now 则 arrive_close > 0
			/**
			 * 起息时间要大于等于今天
			 */
			int arrive_earn = date_now.compareTo(earn_date);
			if (arrive_earn < 0) {
				return;
			}

			for (int j = 0; j < finances.size(); j++) {
				finance = finances.get(j);
				if (finance.getId().equals(order.getFinanceId())) {
					break;
				}
			}

			/**
			 * 当日获取的收益
			 */

			double today_rate = Arith.mul(finance.getToday_rate(), 0.01);
			double get_amount = Arith.mul(order.getAmount(), today_rate);

			// 5,0.5,3,0.3,2,0.2
			String finance_level_profit = sysparaService.find("finance_level_profit").getValue();
			boolean finance_level_profit_open = StringUtils.isNotEmpty(finance_level_profit);
			// 判断是否有理财推荐奖励，如果有，需要扣除30%给推荐人
			String finance_bonus_parameters = "";
			finance_bonus_parameters = this.sysparaService.find("finance_bonus_parameters").getValue();
			if ((finance_bonus_parameters != null && !"".equals(finance_bonus_parameters))
					|| finance_level_profit_open) {
				String[] finance_bonus_array = StringUtils.isNotEmpty(finance_bonus_parameters)
						? finance_bonus_parameters.split(",")
						: new String[] {};
				List<UserRecom> list_parents = this.userRecomService.getParents(order.getPartyId());

				if (CollectionUtils.isNotEmpty(list_parents)) {

					int loop = 0;
					for (int i = 0; i < list_parents.size(); i++) {
						if (finance_level_profit_open) {// 等级奖励只给推荐一人
							if (loop >= 1) {
								break;
							}
						} else {
							if (loop >= 3) {
								break;
							}
						}
						Party party_parent = this.partyService.cachePartyBy(list_parents.get(i).getReco_id(), true);
						if (!Constants.SECURITY_ROLE_MEMBER.equals(party_parent.getRolename())) {
							continue;
						}
						get_amount = Arith.mul(order.getAmount(), today_rate);
						/**
						 * 增加推荐人理财收益
						 */
						double parent_get_money = 0d;
						if (finance_level_profit_open) {
							parent_get_money = levelProfit(party_parent.getId().toString(), get_amount,
									finance_level_profit);
						} else {
							double finance_pip_amount = Double.valueOf(finance_bonus_array[loop]);
							parent_get_money = Arith.mul(get_amount, finance_pip_amount);
						}
						if (parent_get_money == 0d) {
							break;
						}
						Wallet wallet_parent = walletService.saveWalletByPartyId(party_parent.getId().toString());
						double amount_before_parent = wallet_parent.getMoney();
						walletService.update(wallet_parent.getPartyId().toString(), parent_get_money);
						/**
						 * 保存资金日志
						 */
						MoneyLog moneyLog = new MoneyLog();
						moneyLog.setCategory(Constants.MONEYLOG_CATEGORY_FINANCE);
						moneyLog.setAmount_before(amount_before_parent);
						moneyLog.setAmount(parent_get_money);
						moneyLog.setAmount_after(Arith.add(wallet_parent.getMoney(), parent_get_money));
						moneyLog.setLog("第" + (i + 1) + "代下级用户，每日理财收益奖励金");
						moneyLog.setPartyId(party_parent.getId().toString());
						moneyLog.setWallettype(Constants.WALLET);
						moneyLog.setContent_type(Constants.MONEYLOG_CONTENT_FINANCE_RECOM_PROFIT);
						moneyLog.setCreateTime(systemTime);
						moneyLogService.save(moneyLog);

						loop++;
					}
				}
				/**
				 * 理财收益减少百分之30给上级
				 */
				if (finance_level_profit_open) {// 等级奖励则无需扣除本人收益
					get_amount = Arith.sub(get_amount, Arith.mul(get_amount, 0.3));
				}
			}

			Wallet wallet = this.walletService.saveWalletByPartyId(order.getPartyId());
			double amount_before = wallet.getMoney();
			this.walletService.update(wallet.getPartyId().toString(), get_amount);
			// 保存资金日志
			MoneyLog moneylog = new MoneyLog();
			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_FINANCE);
			moneylog.setAmount_before(amount_before);
			moneylog.setAmount(Arith.add(0, get_amount));
			moneylog.setAmount_after(Arith.add(wallet.getMoney(), get_amount));
			moneylog.setLog("理财产品每日收益，订单号[" + order.getOrder_no() + "]");
			moneylog.setPartyId(order.getPartyId());
			moneylog.setWallettype(Constants.WALLET);
			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_FINANCE_PROFIT);
			moneylog.setCreateTime(systemTime);
			moneyLogService.save(moneylog);
			order.setProfit(Arith.add(order.getProfit(), get_amount));
			updateFinanceOrder(order);

			if (DateUtils.addDay(now_date, 1).after(order.getClose_time())) {
		        order.setState("0");
		        saveClosePrincipal(order);
		    }
			
		}

	}

	/**
	 * 每日下发收益,自动赎回调用,只返回本金
	 */
	public void saveClosePrincipal(FinanceOrder entity) {
		double get_amount = Arith.add(entity.getAmount(), entity.getProfit_before());
		if (get_amount < 0) {
			entity.setProfit(Arith.sub(0, entity.getAmount()));
			get_amount = Arith.add(entity.getAmount(), entity.getProfit());
		}

		if (get_amount > 0) {
			Wallet wallet = this.walletService.saveWalletByPartyId(entity.getPartyId());
			double amount_before = wallet.getMoney();
			this.walletService.update(wallet.getPartyId().toString(), get_amount);
			// 保存资金日志
			MoneyLog moneylog = new MoneyLog();
			moneylog.setCategory(Constants.MONEYLOG_CATEGORY_FINANCE);
			moneylog.setAmount_before(amount_before);
			moneylog.setAmount(Arith.add(0, get_amount));
			moneylog.setAmount_after(Arith.add(wallet.getMoney(), get_amount));
			moneylog.setLog("赎回理财产品，订单号[" + entity.getOrder_no() + "]");
			moneylog.setPartyId(entity.getPartyId());
			moneylog.setWallettype(Constants.WALLET);
			moneylog.setContent_type(Constants.MONEYLOG_CONTENT_FINANCE_BACK);
			moneyLogService.save(moneylog);
		}
		updateFinanceOrder(entity);
		this.userDataService.saveSellFinance(entity);
	}

	/**
	 * 等级奖励
	 * 
	 * @param partyId
	 * @param profit
	 * @return 返回奖励
	 */
	public double levelProfit(String partyId, double profit, String finance_level_profit) {
		// 5,0.5,3,0.3,2,0.2
		double levelProfitMoney = 0d;
		if (StringUtils.isEmptyString(finance_level_profit)) {
			return levelProfitMoney;
		}
		List<UserRecom> listRecoms = this.userRecomService.findRecoms(partyId);
		int recomsNum = listRecoms.size();
		String[] finance_level_profit_array = finance_level_profit.split(",");

		for (int i = 0; i < finance_level_profit_array.length; i++) {
			double levelNeed = Double.valueOf(finance_level_profit_array[i]);

			if (recomsNum >= levelNeed) {// 等级满足要求
				double pipAmount = Double.valueOf(finance_level_profit_array[i + 1]);
				levelProfitMoney = Arith.mul(profit, pipAmount);
				break;
			}
			i++;
		}
		return levelProfitMoney;
	}

	public FinanceOrder findById(String id) {
		List<FinanceOrder> list = jdbcTemplate.query("SELECT * FROM T_FINANCE_ORDER WHERE UUID=?", RecordObjectMapper.newInstance(FinanceOrder.class), id);
        if (null != list && list.size() > 0) {
        	return list.get(0);
        }
        return null;
	}

    public void updateFinanceOrder(FinanceOrder order) {
        Object[] jdbcParams = ApplicationUtil.getUpdateStatement(order, "WHERE UUID=?", new Object[] {order.getId()});
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

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}
	
	public void setWalletService(WalletService walletService) {
		this.walletService = walletService;
	}

	public void setMoneyLogService(MoneyLogService moneyLogService) {
		this.moneyLogService = moneyLogService;
	}

	public void setFinanceService(FinanceService financeService) {
		this.financeService = financeService;
	}

}
