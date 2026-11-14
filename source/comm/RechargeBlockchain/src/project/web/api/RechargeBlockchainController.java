package project.web.api;

import java.io.IOException;
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
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.BaseAction;
import kernel.web.ResultObject;
import project.Constants;
import project.blockchain.RechargeBlockchain;
import project.blockchain.RechargeBlockchainService;
import project.log.Log;
import project.log.LogService;
import project.party.PartyService;
import project.party.model.Party;
import project.syspara.SysparaService;
import project.wallet.WalletLogService;

/**
 * 充值
 */
@CrossOrigin
@RestController
@SuppressWarnings("rawtypes")
public class RechargeBlockchainController extends BaseAction {

	@Autowired
	protected PartyService partyService;
	
	@Autowired
	private SysparaService sysparaService;
	
	@Autowired
	protected WalletLogService walletLogService;
	
	@Autowired
	private SessionTokenService sessionTokenService;

	private final String action = "/api/rechargeBlockchain!";
	
	@Autowired
	private RechargeBlockchainService rechargeBlockchainService;
	
	@Autowired
	private LogService logService;
	
	private static final Logger logger = LoggerFactory.getLogger(RechargeBlockchainController.class);

	/**
	 * 首次进入页面,传递session_token
	 */
	@RequestMapping(action + "recharge_open.action")
	public Object recharge_open() throws IOException {
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		try {
			String partyId = this.getLoginPartyId();
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
	 * 充值申请
	 * 
	 * from 客户自己的区块链地址
	 * blockchain_name 充值链名称
	 * amount 充值数量
	 * img 已充值的上传图片
	 * coin 充值币种
	 * channel_address 通道充值地址
	 * tx 转账hash
	 */
	@RequestMapping(action + "recharge.action")
	public Object recharge(HttpServletRequest request) throws IOException {
		//如果未登录则直接返回
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		//获取请求参数
		String session_token = request.getParameter("session_token");
		String amount = request.getParameter("amount");
		String from = request.getParameter("from");
		String blockchain_name = request.getParameter("blockchain_name");
		String img = request.getParameter("img");
		String coin = request.getParameter("coin");
		String channel_address = request.getParameter("channel_address");
		String tx = request.getParameter("tx");
		
		//重复提交抛出异常
		Object object = this.sessionTokenService.cacheGet(session_token);
		this.sessionTokenService.del(session_token);
		String partyId=this.getLoginPartyId();
		if (null == object || !partyId.equals((String) object)) throw new BusinessException("请稍后再试");				
		try {
			//用户无权限抛出异常
			Party party = this.partyService.cachePartyBy(partyId, false);
			if (Constants.SECURITY_ROLE_TEST.equals(party.getRolename())) throw new BusinessException("无权限");
			
			//参数校验不合法抛出异常
			String error = this.verif(amount, coin, blockchain_name, channel_address);
			if (!StringUtils.isNullOrEmpty(error)) throw new BusinessException(error);
			// 区块链充值方式选择 0:联系客服充值，1：可以线上充值
			if (1 == this.sysparaService.find("can_recharge").getInteger()) {
				// 币充值是否强制需要上传图片，需要true,不需要false
				boolean recharge_must_need_qr = this.sysparaService.find("recharge_must_need_qr").getBoolean();
				if (recharge_must_need_qr) {
			        if (StringUtils.isEmptyString(img)) {
			        	throw new BusinessException("请上传图片");
			        }
				}
			}
			
			//存储充值申请订单记录
			RechargeBlockchain recharge = new RechargeBlockchain();
			recharge.setAddress(from);
			recharge.setBlockchain_name(blockchain_name);
			recharge.setVolume(Double.valueOf(amount).doubleValue());
			recharge.setImg(img);
			recharge.setSymbol(coin.toLowerCase());
			recharge.setPartyId(this.getLoginPartyId());
			recharge.setSucceeded(0);
			recharge.setChannel_address(channel_address);
			recharge.setTx(StringUtils.isEmptyString(tx) ? "" : tx);
			
			this.rechargeBlockchainService.save(recharge);
			Log log = new Log();
			log.setCategory(Constants.LOG_CATEGORY_SECURITY);
			log.setLog("用户充值申请,订单号["+recharge.getOrder_no()+"],ip[" + this.getIp(getRequest()) + "]");
			log.setPartyId(partyId);
			log.setUsername(party.getUsername());
			logService.saveAsyn(log);
			//响应数据回客户端
			Map<String, Object> data = new HashMap<String, Object>();
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
	 * 充值订单详情
	 * 
	 * order_no 订单号
	 */
	@RequestMapping(action + "get.action")
	public Object get(HttpServletRequest request) throws IOException {
		String order_no = request.getParameter("order_no");

		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}

		try {
			Map<String, Object> map = new HashMap<String, Object>();
			RechargeBlockchain order = this.rechargeBlockchainService.findByOrderNo(order_no);

			map.put("order_no", order.getOrder_no());
			map.put("volume", order.getVolume());
			map.put("amount", order.getVolume());
			map.put("create_time", DateUtils.format(order.getCreated(), DateUtils.DF_yyyyMMddHHmmss));
			map.put("from", order.getAddress());
			map.put("coin", order.getSymbol().toUpperCase());
			map.put("coin_blockchain",
					order.getSymbol().toUpperCase().indexOf("BTC") != -1
							|| order.getSymbol().toUpperCase().indexOf("ETH") != -1 ? order.getSymbol().toUpperCase()
									: "USDT_" + order.getBlockchain_name().toUpperCase());
			map.put("fee", 0);
			map.put("state", order.getSucceeded());
			map.put("tx", order.getTx());
			map.put("failure_msg", order.getDescription());
			
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
	 * 充值记录
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

			List<Map<String, Object>> data = this.walletLogService.pagedQueryRecharge(page_no_int, 10, this.getLoginPartyId(), "1").getElements();
			for (Map<String, Object> log : data) {

				if (null == log.get("coin") || !StringUtils.isNotEmpty(log.get("coin").toString()))
					log.put("coin", Constants.WALLET);
				else {
					log.put("coin", log.get("coin").toString().toUpperCase());
				}
				
				log.put("coin_blockchain", log.get("symbol").toString().toUpperCase().indexOf("BTC") != -1
						|| log.get("symbol").toString().toUpperCase().indexOf("ETH") != -1 ? log.get("symbol").toString().toUpperCase()
										: "USDT_" + log.get("blockchain_name").toString().toUpperCase());
				log.put("fee", 0);
				log.put("from", log.get("address").toString());
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

	private String verif(String amount, String coin, String blockchain_name, String channel_address) {
		if (StringUtils.isNullOrEmpty(amount)) {
			return "充值数量必填";
		}
		if (!StringUtils.isDouble(amount)) {
			return "充值数量输入错误，请输入浮点数";
		}
		if (Double.valueOf(amount).doubleValue() <= 0) {
			return "充值数量不能小于等于0";
		}

		if (StringUtils.isEmptyString(coin)) {
			return "请输入充值币种";			
		}

		if (StringUtils.isEmptyString(blockchain_name)) {
			return "参数错误";			
		}
		
		if (StringUtils.isEmptyString(channel_address)) {
			return "请输入地址";
		}
		
		return null;
	}
	
	/**
	 * 充值自动到账
	 */
	@RequestMapping(action + "autoRecharge.action")
	public Object autoRechange(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		return resultObject;
	}
}
