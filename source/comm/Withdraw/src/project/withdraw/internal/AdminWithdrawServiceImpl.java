package project.withdraw.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.providers.encoding.PasswordEncoder;

import kernel.bo.RecordObjectMapper;
import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.Constants;
import project.log.Log;
import project.log.LogService;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.party.recom.UserRecomService;
import project.tip.TipService;
import project.user.UserDataService;
import project.wallet.Wallet;
import project.wallet.WalletExtend;
import project.wallet.WalletLogService;
import project.wallet.WalletService;
import project.withdraw.AdminWithdrawService;
import project.withdraw.Withdraw;
import security.SecUser;
import security.internal.SecUserService;

public class AdminWithdrawServiceImpl implements AdminWithdrawService {
	
	private JdbcTemplate jdbcTemplate;
	private WalletService walletService;
	private MoneyLogService moneyLogService;
	private PagedQueryDao pagedQueryDao;
	private UserRecomService userRecomService;
	private WalletLogService walletLogService;
	private PasswordEncoder passwordEncoder;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private UserDataService userDataService;
	private LogService logService;
	private SecUserService secUserService;
	private TipService tipService;
	
	@Override
	public void saveReject(String id, String failure_msg, String userName, String partyId) {
		Withdraw withdraw = this.get(id);

		if (withdraw.getSucceeded() == 2 ) {// 通过后不可驳回
			return;
		}
		Date date = new Date();
		withdraw.setReviewTime(date);

		withdraw.setFailure_msg(failure_msg);
		withdraw.setSucceeded(2);

		this.updateWithdraw(withdraw);

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
		this.walletLogService.updateStatus(withdraw.getOrder_no(), withdraw.getSucceeded());

		SecUser SecUser = secUserService.findUserByPartyId(withdraw.getPartyId());
		Log log = new Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		log.setExtra(withdraw.getOrder_no());
		log.setOperator(userName);
		log.setPartyId(withdraw.getPartyId());
		log.setUsername(SecUser.getUsername());
		log.setLog("驳回提现申请。原因[" + withdraw.getFailure_msg() + "],订单号[" + withdraw.getOrder_no() + "]");

		logService.saveSync(log);
		tipService.deleteTip(withdraw.getId().toString());
	}

	@Override
	public void saveSucceeded(String id, String safeword, String userName, String partyId) {
		SecUser sec = this.secUserService.findUserByLoginName(userName);
		String sysSafeword = sec.getSafeword();

		String safeword_md5 = passwordEncoder.encodePassword(safeword, userName);
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("资金密码错误");
		}

		Withdraw withdraw = this.get(id);
		Date date = new Date();
		withdraw.setReviewTime(date);

		/**
		 * 
		 */

		if (withdraw != null && withdraw.getSucceeded() == 0) {
			String symbol = "";
			if (withdraw.getMethod().indexOf("BTC") != -1) {
				symbol = "btc";
			} else if (withdraw.getMethod().indexOf("ETH") != -1) {
				symbol = "eth";
			} else {
				symbol = "usdt";
			}

			withdraw.setSucceeded(1);
			this.updateWithdraw(withdraw);

			this.walletLogService.updateStatus(withdraw.getOrder_no(), withdraw.getSucceeded());
			/**
			 * 提现订单加入userdate
			 */
			this.userDataService.saveWithdrawHandle(withdraw.getPartyId(), withdraw.getAmount(),
					withdraw.getAmount_fee(), symbol);

			SecUser SecUser = secUserService.findUserByPartyId(withdraw.getPartyId());
			Log log = new Log();
			log.setCategory(Constants.LOG_CATEGORY_OPERATION);
			log.setExtra(withdraw.getOrder_no());
			log.setOperator(userName);
			log.setUsername(SecUser.getUsername());
			log.setPartyId(SecUser.getPartyId());
			log.setLog("通过提现申请。订单号[" + withdraw.getOrder_no() + "]。");

			logService.saveSync(log);
			tipService.deleteTip(withdraw.getId().toString());
		}
	}
	
	/**
	 * 修改提现描述
	 */
	public void updateWithdraw(String id,String partyId,String remark) {
		Withdraw withdraw = this.get(id);
		if (withdraw != null ) {
			withdraw.setFailure_msg(remark);
			this.updateWithdraw(withdraw);
		}
	}
	
	/**
	 * 修改用户提现地址
	 */
	public void saveAddress(String id,String safeword,String userName,String partyId,String newAddress) {
		SecUser sec = this.secUserService.findUserByLoginName(userName);
		String sysSafeword = sec.getSafeword();

		String safeword_md5 = passwordEncoder.encodePassword(safeword, userName);
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("资金密码错误");
		}

		Withdraw withdraw = this.get(id);

		if (withdraw != null ) {
			String oldaddres = withdraw.getAddress();
			withdraw.setAddress(newAddress);

			this.updateWithdraw(withdraw);

			SecUser SecUser = secUserService.findUserByPartyId(withdraw.getPartyId());
			Log log = new Log();
			log.setCategory(Constants.LOG_CATEGORY_OPERATION);
			log.setExtra(withdraw.getOrder_no());
			log.setOperator(userName);
			log.setUsername(SecUser.getUsername());
			log.setPartyId(SecUser.getPartyId());
			log.setLog("后台手动修改用户提现订单提现地址。提现订单号[" + withdraw.getOrder_no() + "]，旧提现地址["+oldaddres+"]，修改后提现订单新提现地址["+newAddress+"]");

			logService.saveSync(log);
		}

	}
	
	
//	public void saveSucceededThird(String id, String safeword, String userName, String partyId) {
//		SecUser sec = this.secUserService.findUserByLoginName(userName);
//		String sysSafeword = sec.getSafeword();
//
//		String safeword_md5 = passwordEncoder.encodePassword(safeword, userName);
//		if (!safeword_md5.equals(sysSafeword)) {
//			throw new BusinessException("资金密码错误");
//		}
//
//		Withdraw withdraw = this.get(id);
//		Date date = new Date();
//		withdraw.setReviewTime(date);
//
//		/**
//		 * 
//		 */
//
//		if (withdraw != null && withdraw.getSucceeded() == 0) {
//			//三方提现处理
//			thirdHandle(withdraw);
//			String symbol = "";
//			if (withdraw.getMethod().indexOf("BTC") != -1) {
//				symbol = "btc";
//			} else if (withdraw.getMethod().indexOf("ETH") != -1) {
//				symbol = "eth";
//			} else {
//				symbol = "usdt";
//			}
//
//			withdraw.setSucceeded(1);
//			this.getHibernateTemplate().update(withdraw);
//
//			this.walletLogService.updateStatus(withdraw.getOrder_no(), withdraw.getSucceeded());
//			/**
//			 * 提现订单加入userdate
//			 */
//			this.userDataService.saveWithdrawHandle(withdraw.getPartyId(), withdraw.getAmount(),
//					withdraw.getAmount_fee(), symbol);
//
//			SecUser SecUser = secUserService.findUserByPartyId(withdraw.getPartyId());
//			Log log = new Log();
//			log.setCategory(Constants.LOG_CATEGORY_OPERATION);
//			log.setExtra(withdraw.getOrder_no());
//			log.setOperator(userName);
//			log.setUsername(SecUser.getUsername());
//			log.setPartyId(SecUser.getPartyId());
//			log.setLog("通过提现申请。订单号[" + withdraw.getOrder_no() + "]。");
//
//			logService.saveSync(log);
//			tipService.deleteTip(withdraw.getId().toString());
//		}
//
//	}
//	/**
//	 * 三方区块链提现处理
//	 * @param withdraw
//	 */
//	public void thirdHandle(Withdraw withdraw) {
//		String method = withdraw.getMethod();
//		String[] methodArr = method.split("_");
//		String symbol = methodArr[0];
//		String blockchain = methodArr.length==2?methodArr[1]:"";
//		Map<String,Object> param = new HashMap<String, Object>();
//		param.put("address", withdraw.getAddress());
//		param.put("order_no", withdraw.getOrder_no());
//		param.put("symbol", symbol);
//		param.put("blockchain", blockchain);
//		param.put("amount", withdraw.getAmount());
//		thirdBlockChainService.initThirdBlockChainBusinessService();
//		thirdBlockChainService.withdraw(param);
//	}
	@Override
	public Page pagedQuery(int pageNo, int pageSize, String name_para, Integer succeeded, String loginPartyId,
			String orderNo, String rolename_para) {

		StringBuffer queryString = new StringBuffer();
		queryString.append("SELECT");
		queryString.append(
				" party.USERNAME username,party.ROLENAME rolename,party.USERCODE usercode, party.REMARKS userRemarks,");
		queryString.append(
				" withdraw.UUID id,withdraw.ORDER_NO order_no,withdraw.CREATE_TIME createTime,withdraw.FAILURE_MSG failure_msg,");
		queryString.append(" withdraw.AMOUNT amount,withdraw.SUCCEEDED succeeded,withdraw.REVIEWTIME reviewTime, withdraw.REMARKS remarks, ");
		queryString.append(
				" withdraw.CURRENCY currency,withdraw.AMOUNT_FEE amount_fee,withdraw.DEVICE_IP deviceIp, ");
		queryString.append(
				" withdraw.METHOD method, withdraw.QDCODE qdcode, withdraw.CHAIN_ADDRESS address,withdraw.TIME_SETTLE time_settle,"
						+ " withdraw.VOLUME volume, party_parent.USERNAME username_parent ");
		queryString.append(" FROM");
		queryString
				.append(" T_WITHDRAW_ORDER withdraw  " + " LEFT JOIN PAT_PARTY party ON withdraw.PARTY_ID = party.UUID "
						+ " LEFT JOIN PAT_USER_RECOM user ON user.PARTY_ID = party.UUID  "
						+ "  LEFT JOIN PAT_PARTY party_parent ON user.RECO_ID = party_parent.UUID   " + "");
		queryString.append(" WHERE 1=1 ");

		Map<String, Object> parameters = new HashMap<String, Object>();

		if (!StringUtils.isNullOrEmpty(loginPartyId)) {
			List children = this.userRecomService.findChildren(loginPartyId);
			if (children.size() == 0) {
//				return Page.EMPTY_PAGE;
				return new Page();
			}
			queryString.append(" and withdraw.PARTY_ID in (:children) ");
			parameters.put("children", children);
		}

//		if (!StringUtils.isNullOrEmpty(name_para)) {
//			queryString.append(" and (party.USERNAME like :name_para or party.USERCODE =:usercode) ");
//			parameters.put("name_para", "%" + name_para + "%");
//			parameters.put("usercode", name_para);
//
//		}

		if (succeeded != null) {
			queryString.append(" and withdraw.SUCCEEDED = :succeeded  ");
			parameters.put("succeeded", succeeded);

		}
		if (!StringUtils.isNullOrEmpty(name_para)) {
			queryString.append("AND (party.USERNAME like:username OR party.USERCODE like:username ) ");
			parameters.put("username", "%" + name_para + "%");
		}
		if (!StringUtils.isNullOrEmpty(rolename_para)) {
			queryString.append(" and   party.ROLENAME =:rolename");
			parameters.put("rolename", rolename_para);
		}
		if (!StringUtils.isNullOrEmpty(orderNo)) {
			queryString.append(" and withdraw.ORDER_NO = :orderNo  ");
			parameters.put("orderNo", orderNo);

		}

		queryString.append(" order by withdraw.CREATE_TIME desc ");

		Page page = pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);

		return page;
	}

	public int getCount(Integer state_para, String loginPartyId) {
		StringBuffer queryString = new StringBuffer();
		queryString.append("SELECT ");
		queryString.append(" count(withdraw.UUID) id_count");

		queryString.append(" FROM");
		queryString.append(" T_WITHDRAW_ORDER withdraw   ");
		queryString.append(" WHERE 1=1");

		Map parameters = new HashMap();
		if (state_para != null) {
			queryString.append(" and withdraw.SUCCEEDED = :state_para");
			parameters.put("state_para", state_para);
		}
		if (!StringUtils.isNullOrEmpty(loginPartyId)) {
			queryString.append(" and withdraw.PARTY_ID  = :loginPartyId");
			parameters.put("loginPartyId", loginPartyId);
		}
		List list = this.namedParameterJdbcTemplate.queryForList(queryString.toString(), parameters);

		Map map = new HashMap();
		if (list.size() > 0) {
			Object obj = ((Map) list.get(0)).get("id_count");
			if (obj != null) {
				return Integer.valueOf(String.valueOf(obj));
			} else {
				return 0;
			}
		} else {
			return 0;
		}

	}
	
	public Withdraw get(String id) {
        List<Withdraw> list = jdbcTemplate.query("SELECT * FROM T_WITHDRAW_ORDER WHERE UUID=?", RecordObjectMapper.newInstance(Withdraw.class), id);
        if (null != list && list.size() > 0) {
            return list.get(0);
        }
        return null;
	}
	
    public void insertWithdraw(Withdraw withdraw) {
        Object[] jdbcParams = ApplicationUtil.getInsertStatement(withdraw);
        String insertUserSql = (String)jdbcParams[0];
        Object[] sqlParameters = (Object[])jdbcParams[1];
        jdbcTemplate.update(insertUserSql, sqlParameters);
    }


    public void updateWithdraw(Withdraw withdraw) {
        Object[] jdbcParams = ApplicationUtil.getUpdateStatement(withdraw,"WHERE UUID=?",new Object[] {withdraw.getId()});
        String updateUserSql = (String)jdbcParams[0];
        Object[] sqlParameters = (Object[])jdbcParams[1];
        jdbcTemplate.update(updateUserSql, sqlParameters);
    }
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public void setWalletService(WalletService walletService) {
		this.walletService = walletService;
	}

	public void setMoneyLogService(MoneyLogService moneyLogService) {
		this.moneyLogService = moneyLogService;
	}

	public void setPagedQueryDao(PagedQueryDao pagedQueryDao) {
		this.pagedQueryDao = pagedQueryDao;
	}

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}

	public void setWalletLogService(WalletLogService walletLogService) {
		this.walletLogService = walletLogService;
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return namedParameterJdbcTemplate;
	}

	public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public void setUserDataService(UserDataService userDataService) {
		this.userDataService = userDataService;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}

	public void setTipService(TipService tipService) {
		this.tipService = tipService;
	}
	
}
