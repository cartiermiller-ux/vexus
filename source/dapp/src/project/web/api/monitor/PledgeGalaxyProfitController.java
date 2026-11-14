package project.web.api.monitor;

import java.util.ArrayList;
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
import kernel.web.BaseAction;
import kernel.web.ResultObject;
import project.monitor.pledgegalaxy.PledgeGalaxyProfit;
import project.monitor.pledgegalaxy.PledgeGalaxyProfitService;
import project.monitor.pledgegalaxy.PledgeGalaxyStatusConstants;
import project.monitor.pledgegalaxy.PledgeLock;
import project.party.PartyService;
import project.party.model.Party;
import project.syspara.SysparaService;

/**
 * 质押2.0
 * 银河数码-AI量化交易投资收益
 *
 */
@RestController
@CrossOrigin
public class PledgeGalaxyProfitController extends BaseAction {
	
	private Logger logger = LoggerFactory.getLogger(PledgeGalaxyProfitController.class);
	
	@Autowired
	PledgeGalaxyProfitService pledgeGalaxyProfitService;
	
	@Autowired
	SysparaService sysparaService;
	
	@Autowired
	PartyService partyService;

	public final String action = "/api/pledgeGalaxyProfit!";
	
	/**
	 * 收益记录列表
	 */
	@RequestMapping(action + "queryList.action")
	public Object list(HttpServletRequest request) {
		
		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
		
		String page_no = request.getParameter("page_no");
		
		try {
			String partyId = getLoginPartyId();
			int pageNo = Integer.valueOf(page_no);
			datas = pledgeGalaxyProfitService.pagedQuery(pageNo, 40, partyId).getElements();
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", e.fillInStackTrace());
		}
		resultObject.setData(datas);
		return resultObject;
	}
//	
//	/**
//	 * 领取收益 审核到账
//	 */
//	@RequestMapping(action + "receive.action")
//	public Object receive(HttpServletRequest request) {
//		
//		ResultObject resultObject = new ResultObject();
//		resultObject = readSecurityContextFromSession(resultObject);
//		if (!"0".equals(resultObject.getCode())) {
//			return resultObject;
//		}
//		
//		try {
//			String id = request.getParameter("id");
//			pledgeGalaxyProfitService.updateReceive(id);
//			
//			resultObject.setMsg("领取成功");
//			resultObject.setCode("0");
//		} catch (Exception e) {
//			resultObject.setCode("1");
//			resultObject.setMsg("程序错误");
//			logger.error("error:", e.fillInStackTrace());
//		}
//		return resultObject;
//	}
	
	/**
	 * 领取收益 及时到账
	 */
	@RequestMapping(action + "receiveToWallet.action")
	public Object receiveToWallet(HttpServletRequest request) {
		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		String id = request.getParameter("id");

		boolean lock = false;
		PledgeGalaxyProfit profit = pledgeGalaxyProfitService.get(id);
		String partyId = profit.getPartyId();
		try {
			
			if (PledgeGalaxyStatusConstants.PLEDGE_APPLY != profit.getStatus()) {
				throw new BusinessException("Redeemed");
			}
			
			Party party = partyService.cachePartyBy(profit.getPartyId(), false);
			if (!party.getEnabled()) {
				throw new BusinessException(1, "您的账户已被冻结");
			}

			if (!PledgeLock.add(partyId)) {
				throw new BusinessException("Please try again later");
			}
			lock = true;
			
			pledgeGalaxyProfitService.updateReceiveToWallet(profit);
			resultObject.setMsg("领取成功");
			resultObject.setCode("0");
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", e.fillInStackTrace());
		} finally {
			if (lock) {
				PledgeLock.remove(partyId);
			}
		}
		return resultObject;
	}
}
