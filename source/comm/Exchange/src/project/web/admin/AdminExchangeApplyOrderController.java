package project.web.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.providers.encoding.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.Constants;
import project.data.model.Realtime;
import project.exchange.AdminExchangeApplyOrderService;
import project.exchange.ExchangeApplyOrder;
import project.exchange.ExchangeApplyOrderService;
import project.exchange.job.ExchangeApplyOrderHandleJobService;
import project.log.LogService;
import project.monitor.job.transferfrom.LockFilter;
import project.party.PartyService;
import project.party.model.Party;
import security.SecUser;
import security.internal.SecUserService;

/**
 * 管理后台 
 * 币币交易订单
 *
 */
@RestController
public class AdminExchangeApplyOrderController extends PageActionSupport {
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private PartyService partyService;
	
	@Autowired
	private SecUserService secUserService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	protected ExchangeApplyOrderService exchangeApplyOrderService;
	
    private Map<String, Object> session = new HashMap<>();
	
	private final String action = "normal/adminExchangeApplyOrderAction!";
	
	@Autowired
	protected AdminExchangeApplyOrderService adminExchangeApplyOrderService;
	
	@Autowired
	private ExchangeApplyOrderHandleJobService exchangeApplyOrderHandleJobService;
	
	private static final Logger logger = LoggerFactory.getLogger(AdminExchangeApplyOrderController.class);

	/**
	 * 币币交易订单 列表查询
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		
        String session_token = UUID.randomUUID().toString();
        this.session.put("session_token", session_token);
        
		int pageSize = 30;
		int pageNo=checkAndSetPageNo(request.getParameter("pageNo"));
		
		String loginPartyId = getLoginPartyId();
		String status_para = request.getParameter("status_para");
		String rolename_para = request.getParameter("rolename_para");
		String name_para = request.getParameter("name_para");
		String order_no_para = request.getParameter("order_no_para");
		
		Page page = this.adminExchangeApplyOrderService.pagedQuery(pageNo, pageSize, status_para,rolename_para, loginPartyId, name_para, order_no_para);

		List<Map> list = page.getElements();
		for (int i = 0; i < list.size(); i++) {
			Map map = list.get(i);
			if (null == map.get("rolename")) {
				map.put("roleNameDesc", "");
			} else {
				String roleName = map.get("rolename").toString();
				map.put("roleNameDesc", Constants.ROLE_MAP.containsKey(roleName) ? Constants.ROLE_MAP.get(roleName) : roleName);
			}
		}

		ModelAndView model = new ModelAndView();
		model.addObject("pageNo", pageNo);
		model.addObject("pageSize", pageSize);
		model.addObject("page", page);
		model.addObject("status_para", status_para);
		model.addObject("rolename_para", rolename_para);
		model.addObject("name_para", name_para);
		model.addObject("order_no_para", order_no_para);
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("exchange_order_list");
		return model;
	}

	/**
	 * 限价成交
	 */
	@RequestMapping(action + "success.action")
	public ModelAndView success(HttpServletRequest request) {
		String message = "";
		String error = "";
		ModelAndView model = new ModelAndView();
		String safeword = request.getParameter("login_safeword");
		String orderNo = request.getParameter("orderNo");
		boolean lock = false;
		try {
			if (!LockFilter.add(orderNo)) {
				model.setViewName("exchange_order_list");
			}
			lock = true;

			SecUser sec = this.secUserService.findUserByLoginName(this.getUsername_login());
			String sysSafeword = sec.getSafeword();

			String safeword_md5 = passwordEncoder.encodePassword(safeword, getUsername_login());
			if (!safeword_md5.equals(sysSafeword)) {
				throw new BusinessException("资金密码错误");
			}

			ExchangeApplyOrder order = exchangeApplyOrderService.findByOrderNo(orderNo);
			if (order == null) {
				throw new BusinessException("委托单不存在或请稍后再试");
			}
			if (ExchangeApplyOrder.STATE_CREATED.equals(order.getState()))
				throw new BusinessException("委托已完成无法操作");
			if (!"limit".equals(order.getOrder_price_type())) {
				throw new BusinessException("委托并非限价单，无法限价成交");
			}

			Realtime realtime = new Realtime();
			realtime.setClose(order.getPrice());
			exchangeApplyOrderHandleJobService.handle(order, realtime);

			Party party = partyService.cachePartyBy(order.getPartyId().toString(), true);
			project.log.Log log = new project.log.Log();
			log.setCategory(Constants.LOG_CATEGORY_OPERATION);
			log.setExtra(order.getOrder_no());
			log.setOperator(getUsername_login());
			log.setUsername(party.getUsername());
			log.setPartyId(order.getPartyId());
			log.setLog("币币限价单，操作限价成交。订单号[" + order.getOrder_no() + "]。");
			logService.saveSync(log);
			message = "操作成功";
		} catch (BusinessException e) {
			error = e.getMessage();
		} catch (Exception e) {
			logger.error("error ", e);
			error = "程序错误";
		} finally {
			if (lock) {
				LockFilter.remove(orderNo);
			}
		}
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("redirect:/" + action + "list.action");
		return model;
	}

	/**
	 * 平仓或撤单
	 */
	@RequestMapping(action + "close.action")
	public ModelAndView close(HttpServletRequest request) {
		String message = "";
		String error = "";
		try {
			String order_no = request.getParameter("order_no");
			ExchangeApplyOrder order = exchangeApplyOrderService.findByOrderNo(order_no);
			if (ExchangeApplyOrder.STATE_CREATED.equals(order.getState())) {
				throw new BusinessException("委托已完成无法撤销");
			}
			
			exchangeApplyOrderService.saveCancel(order.getPartyId().toString(), order_no);

			message = "操作成功";
		} catch (BusinessException e) {
			error = e.getMessage();
		} catch (Exception e) {
			logger.error("error ", e);
			error = "程序错误";
		}
		ModelAndView model = new ModelAndView();
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("redirect:/" + action + "list.action");
		return model;
	}
	
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

}
