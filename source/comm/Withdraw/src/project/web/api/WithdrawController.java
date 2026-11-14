package project.web.api;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kernel.exception.BusinessException;
import kernel.sessiontoken.SessionTokenService;
import kernel.util.Arith;
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.util.ThreadUtils;
import kernel.web.BaseAction;
import kernel.web.ResultObject;
import project.Constants;
import project.log.Log;
import project.log.LogService;
import project.party.PartyService;
import project.party.model.Party;
import project.syspara.SysparaService;
import project.wallet.WalletLogService;
import project.withdraw.Withdraw;
import project.withdraw.WithdrawService;
import util.LockFilter;

/**
 * 提现
 */
@RestController
@CrossOrigin
public class WithdrawController extends BaseAction {
	
	@Autowired
	private PartyService partyService;
	
	@Autowired
	private SysparaService sysparaService;

	@Autowired
	private WithdrawService withdrawService;
	
	@Autowired
	protected WalletLogService walletLogService;
	
	@Autowired
	private LogService logService;

	private final String action = "/api/withdraw!";
	
	@Autowired
	private SessionTokenService sessionTokenService;
	
	private static final Logger logger = LoggerFactory.getLogger(WithdrawController.class);

	/**
	 * 首次进入页面，传递session_token
	 */
	@RequestMapping(action + "withdraw_open.action")
	public Object withdraw_open() throws IOException {
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		try {
			String partyId = getLoginPartyId();
			String session_token = this.sessionTokenService.savePut(partyId);
			
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("session_token", session_token);

			resultObject.setData(data);
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}
		
		return resultObject;
	}

	/**
	 * 提现申请
	 * 
	 * safeword 资金密码
	 * amount 提现金额
	 * from 客户转出地址
	 * currency 货币 CNY USD
	 * channel 渠道 USDT,BTC,ETH
	 */
	@RequestMapping(action + "apply.action")
	public Object apply(HttpServletRequest request) throws IOException {
		String session_token = request.getParameter("session_token");
		String safeword = request.getParameter("safeword");
		String amount = request.getParameter("amount");
		String from = request.getParameter("from");
		String currency = request.getParameter("currency");
		String channel = request.getParameter("channel");
		String googleCode = request.getParameter("googleCode");
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		String partyId = this.getLoginPartyId();
		boolean lock = false;
		try {
			String error = this.verif(amount);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			double amount_double = Double.valueOf(amount).doubleValue();
			
			// 交易所提现是否需要资金密码
			String exchange_withdraw_need_safeword = this.sysparaService.find("exchange_withdraw_need_safeword").getValue();
			if(StringUtils.isEmptyString(exchange_withdraw_need_safeword)) {
				throw new BusinessException("系统参数错误");
			}
			
			if ("true".equals(exchange_withdraw_need_safeword)) {
				
				if (StringUtils.isEmptyString(safeword)) {
					throw new BusinessException("资金密码不能为空");
				}

				if (safeword.length() < 6 || safeword.length() > 12) {
					throw new BusinessException("资金密码必须6-12位");
				}
								
				if (!this.partyService.checkSafeword(safeword, partyId)) {
					throw new BusinessException("资金密码错误");
				}				
			}

			if (!LockFilter.add(partyId)) {
				throw new BusinessException(error);
			}
			
			lock = true;
			
			Object object = this.sessionTokenService.cacheGet(session_token);
			this.sessionTokenService.del(session_token);
			if (null == object || !this.getLoginPartyId().equals((String) object)) {
				throw new BusinessException("请稍后再试");
			}

			Withdraw withdraw = new Withdraw();
			withdraw.setPartyId(partyId);
			withdraw.setVolume(amount_double);
			withdraw.setAddress(from);
			withdraw.setCurrency(currency);
			withdraw.setDeviceIp(this.getIp());
			
			// 保存
			this.withdrawService.saveApply(withdraw, channel, null, googleCode);
			Log log = new Log();
			Party party = this.partyService.cachePartyBy(partyId, false);
			log.setCategory(Constants.LOG_CATEGORY_SECURITY);
			log.setLog("用户提现申请,订单号["+withdraw.getOrder_no()+"],ip[" + this.getIp(getRequest()) + "]");
			log.setPartyId(partyId);
			log.setUsername(party.getUsername());
			logService.saveAsyn(log);
			// 挂住线程保证事务提交
			ThreadUtils.sleep(300);

		} catch (BusinessException e) {
			if (105 == e.getSign()) {
				resultObject.setCode("105");				
			} else if (401 == e.getSign()) {
				resultObject.setCode("401");				
			} else if (506 == e.getSign()) {
				resultObject.setCode("506");				
			} else {
				resultObject.setCode("1");				
			}
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		} finally {
			if (lock) {
				LockFilter.remove(partyId);
			}
		}
		
		return resultObject;
	}

	/**
	 * 提现订单详情
	 * 
	 * order_no 订单号
	 */
	@RequestMapping(action + "get.action")
	public Object get(HttpServletRequest request) throws IOException {
		String order_no = request.getParameter("order_no");

		ResultObject resultObject = new ResultObject();

		try {

			Withdraw withdraw = this.withdrawService.findByOrderNo(order_no);

			Map<String, Object> map = new HashMap<String, Object>();
			
			map.put("order_no", withdraw.getOrder_no());
			map.put("volume", withdraw.getVolume());
			map.put("amount", withdraw.getAmount());
			map.put("create_time", DateUtils.format(withdraw.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
			map.put("review_time", DateUtils.format(withdraw.getReviewTime(), DateUtils.DF_yyyyMMddHHmmss));
			map.put("to", withdraw.getAddress());
			map.put("fee", withdraw.getAmount_fee());
			map.put("coin_blockchain", withdraw.getMethod());
			map.put("coin",
					withdraw.getMethod().indexOf("BTC") != -1 || withdraw.getMethod().indexOf("ETH") != -1
							? withdraw.getMethod()
							: "USDT");
			map.put("state", withdraw.getSucceeded());
			map.put("failure_msg", withdraw.getFailure_msg());
			
			resultObject.setData(map);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}
		
		return resultObject;
	}

	/**
	 * 提现记录
	 */
	@RequestMapping(action + "list.action")
	public Object list(HttpServletRequest request) throws IOException {
		String page_no = request.getParameter("page_no");

		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}

		try {

			if (StringUtils.isNullOrEmpty(page_no)) {
				page_no = "1";
			}
			if (!StringUtils.isInteger(page_no)) {
				throw new BusinessException("页码不是整数");
			}
			if (Integer.valueOf(page_no).intValue() <= 0) {
				throw new BusinessException("页码不能小于等于0");
			}

			int page_no_int = Integer.valueOf(page_no).intValue();

			List<Map<String, Object>> data = this.walletLogService.pagedQueryWithdraw(page_no_int, 10, this.getLoginPartyId(), "1").getElements();
			for (Map<String, Object> log : data) {

				if (null == log.get("coin") || !StringUtils.isNotEmpty(log.get("coin").toString()))
					log.put("coin", Constants.WALLET);
				else {
					log.put("coin", log.get("coin").toString().toUpperCase());
				}
			}
			
			resultObject.setData(data);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}
		
		return resultObject;
	}

	/**
	 * 提现手续费
	 * 
	 * channel 渠道 USDT,OTC
	 * amount 提币数量
	 */
	@RequestMapping(action + "fee.action")
	public Object fee(HttpServletRequest request) throws IOException {
		String channel = request.getParameter("channel");
		String amount = request.getParameter("amount");

		ResultObject resultObject = new ResultObject();

		try {
			String error = this.verif(amount);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			double amount_double = Double.valueOf(amount).doubleValue();

			Map<String, Object> map = new HashMap<String, Object>();
			
			DecimalFormat df = new DecimalFormat("#.########");
			
			double fee = 0;
			
			channel = StringUtils.isEmptyString(channel) ? "USDT" : channel;
			if (channel.indexOf("BTC") != -1 || channel.indexOf("ETH") != -1) {
				map.put("withdraw_fee_type", "rate");
				fee = this.withdrawService.getOtherChannelWithdrawFee(amount_double);
			} else {
				// 手续费(USDT)
				
				// 提现手续费类型,fixed是单笔固定金额，rate是百分比，part是分段
				String withdraw_fee_type = this.sysparaService.find("withdraw_fee_type").getValue();

				// fixed单笔固定金额 和 rate百分比 的手续费数值
				double withdraw_fee = Double.valueOf(this.sysparaService.find("withdraw_fee").getValue());

				if ("fixed".equals(withdraw_fee_type)) {
					fee = withdraw_fee;
				}
				
				if ("rate".equals(withdraw_fee_type)) {
					withdraw_fee = Arith.div(withdraw_fee, 100);
					fee = Arith.mul(amount_double, withdraw_fee);
				}
				
				if ("part".equals(withdraw_fee_type)) {
					
					// 提现手续费part分段的值
					String withdraw_fee_part = this.sysparaService.find("withdraw_fee_part").getValue();

					String[] withdraw_fee_parts = withdraw_fee_part.split(",");
					for (int i = 0; i < withdraw_fee_parts.length; i++) {
						double part_amount = Double.valueOf(withdraw_fee_parts[i]);
						double part_fee = Double.valueOf(withdraw_fee_parts[i + 1]);
						if (amount_double <= part_amount) {
							fee = part_fee;
							break;
						}
						i++;
					}
				}
			map.put("withdraw_fee_type", withdraw_fee_type);
			}
			
			double volume_last = Arith.sub(amount_double, fee);
			if (volume_last < 0) {
				volume_last = 0;
			}
			
			map.put("fee", fee);
			map.put("volume_last", df.format(volume_last));
			
			resultObject.setData(map);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}
		
		return resultObject;
	}

	/**
	 * 提现限额
	 * 
	 * channel 渠道 USDT,OTC
	 */
	@RequestMapping(action + "limit.action")
	public Object limit(HttpServletRequest request) throws IOException {
		String channel = request.getParameter("channel");
		ResultObject resultObject = new ResultObject();

		try {

			Map<String, Object> map = new HashMap<String, Object>();

			channel = StringUtils.isEmptyString(channel) ? "USDT" : channel;
			if (channel.indexOf("BTC") != -1) {				
				map.put("limit", this.sysparaService.find("withdraw_limit_btc").getValue());
			} else if (channel.indexOf("ETH") != -1) {			
				map.put("limit", this.sysparaService.find("withdraw_limit_eth").getValue());
			} else {
				map.put("limit", this.sysparaService.find("withdraw_limit").getValue());
			}
			
			resultObject.setData(map);

		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}
		
		return resultObject;
	}

	private String verif(String amount) {
		
		if (StringUtils.isNullOrEmpty(amount)) {
			return "提币数量必填";
		}
		if (!StringUtils.isDouble(amount)) {
			return "提币数量输入错误，请输入浮点数";
		}
		if (Double.valueOf(amount).doubleValue() <= 0) {
			return "提币数量不能小于等于0";
		}

		return null;
	}

}
