package project.web.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.util.JsonUtils;
import kernel.util.StringUtils;
import kernel.util.ThreadUtils;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.Constants;
import project.c2c.AdminC2cOrderService;
import project.c2c.C2cAdvertService;
import project.c2c.C2cOrder;
import project.c2c.C2cOrderLock;
import project.c2c.C2cOrderService;
import project.c2c.C2cPaymentMethod;
import project.item.ItemService;
import project.onlinechat.otc.OtcOnlineChatMessageService;
import project.party.PartyService;
import project.party.model.Party;
import project.wallet.rate.ExchangeRateService;
import security.Role;
import security.SecUser;
import security.internal.SecUserService;

/**
 * C2C订单
 */
@RestController
public class AdminC2cOrderController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminC2cOrderController.class);
	
	@Autowired
	protected ExchangeRateService exchangeRateService;
	@Autowired
	protected ItemService itemService;
	@Autowired
	protected AdminC2cOrderService adminC2cOrderService;
	@Autowired
	protected OtcOnlineChatMessageService otcOnlineChatMessageService;
	@Autowired
	protected C2cOrderService c2cOrderService;
	@Autowired
	protected SecUserService secUserService;
	@Autowired
	protected PartyService partyService;
	@Autowired
	protected C2cAdvertService c2cAdvertService;
	
	private final String action = "normal/adminC2cOrderAction!";

	/**
	 * 获取 C2C订单 列表
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String pageNoStr = request.getParameter("pageNo");
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		String status_para = request.getParameter("status_para");
		String order_no_para = request.getParameter("order_no_para");
		String user_code_para = request.getParameter("user_code_para");		
		String rolename_para = request.getParameter("rolename_para");		
		String c2c_user_code_para = request.getParameter("c2c_user_code_para");		
		String c2c_user_type_para = request.getParameter("c2c_user_type_para");		
		String c2c_user_party_code_para = request.getParameter("c2c_user_party_code_para");		
		String direction_para = request.getParameter("direction_para");

		ModelAndView modelAndView = new ModelAndView();	
		modelAndView.setViewName("c2c_order_list");	

		int pageNo=1;
		Page page=null;
		int pageSize = 20;
		try {
			pageNo=checkAndSetPageNo(pageNoStr);
			
			Integer status_int = null;
			if (!StringUtils.isEmptyString(status_para)) {
				status_int = Integer.valueOf(status_para);
			}
			
			Integer c2c_user_type_int = null;
			if (!StringUtils.isEmptyString(c2c_user_type_para)) {
				c2c_user_type_int = Integer.valueOf(c2c_user_type_para);
			}
			
			String secUuid = "";
			String userNameLogin = this.getUsername_login();
			if (null != userNameLogin) {
				SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
				Set<Role> roles = sec.getRoles();
				Iterator<Role> it = roles.iterator();
				while (it.hasNext()) {
					Role role = (Role) it.next();
					if (role.getRoleName().equals("C2C")) {
						secUuid = sec.getId().toString();
						break;
					}
				}
			}

			page = this.adminC2cOrderService.pagedQuery(pageNo, pageSize, status_int, order_no_para, user_code_para,
					rolename_para, c2c_user_code_para, c2c_user_type_int, c2c_user_party_code_para, direction_para, secUuid);
			
			Map<String, String> pmtMap = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");
			
			List<String> nos = new ArrayList<String>();
			for (Map<String, Object> map : (List<Map<String, Object>>) page.getElements()) {
				nos.add(map.get("order_no").toString());
				String methodType = String.valueOf((Integer) map.get("method_type"));
				map.put("method_type_name", pmtMap.containsKey(methodType) ? pmtMap.get(methodType) : methodType);
			}
			String order_nos = String.join(",", nos);
			
			Map<String, Integer> unreadMsgs = this.otcOnlineChatMessageService.unreadMsgs(nos);
			
			for (Map<String, Object> map : (List<Map<String, Object>>) page.getElements()) {
				String orderNo = map.get("order_no").toString();
				if(unreadMsgs.containsKey(orderNo)) {
					map.put("unread_msg", unreadMsgs.get(orderNo));
				}

				if (null == map.get("rolename")) {
					map.put("roleNameDesc", "");
				} else {
					String roleName = map.get("rolename").toString();
					map.put("roleNameDesc", Constants.ROLE_MAP.containsKey(roleName) ? Constants.ROLE_MAP.get(roleName) : roleName);
				}
			}

			modelAndView.addObject("order_nos", order_nos);	
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			return modelAndView;
		}
		
		modelAndView.addObject("pageNo", pageNo);
		modelAndView.addObject("pageSize", pageSize);
		modelAndView.addObject("page", page);
		modelAndView.addObject("message", message);
		modelAndView.addObject("error", error);		
		modelAndView.addObject("status_para", status_para);
		modelAndView.addObject("order_no_para", order_no_para);
		modelAndView.addObject("user_code_para", user_code_para);
		modelAndView.addObject("rolename_para", rolename_para);
		modelAndView.addObject("c2c_user_code_para", c2c_user_code_para);
		modelAndView.addObject("c2c_user_type_para", c2c_user_type_para);
		modelAndView.addObject("c2c_user_party_code_para", c2c_user_party_code_para);
		modelAndView.addObject("direction_para", direction_para);		
		return modelAndView;
	}
	
	/**
	 * 手动放行
	 */
	@RequestMapping(action + "orderPass.action")
	public ModelAndView orderPass(HttpServletRequest request) {			
		String order_no = request.getParameter("order_no");
		String page_from = request.getParameter("page_from");
		String safeword = request.getParameter("safeword");
		
		ModelAndView modelAndView = new ModelAndView();
		if ("detail".equals(page_from)) {
			modelAndView.setViewName("redirect:/" + action + "detail.action");
		} else {
			modelAndView.setViewName("redirect:/" + action + "list.action");
		}
		modelAndView.addObject("order_no", order_no);
		modelAndView.addObject("page_from", page_from);
		
		boolean lock = false;
		try {
			
			if (!C2cOrderLock.add(order_no)) {
				throw new BusinessException("系统繁忙，请稍后重试");
			}
			
			lock = true;
				
			C2cOrder order = this.c2cOrderService.get(order_no);
			if (null == order) {
				throw new BusinessException("订单不存在");
			}
			
			this.adminC2cOrderService.savePass(order, safeword, this.getUsername_login());
			
			ThreadUtils.sleep(100);				
			
		} catch (BusinessException e) {	
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			return modelAndView;
		} finally {
			if (lock) {
				C2cOrderLock.remove(order_no);
			}
		}
		
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
	}
	
	/**
	 * 取消订单
	 * 
	 * remark 取消理由
	 */
	@RequestMapping(action + "orderCancel.action")
	public ModelAndView orderCancel(HttpServletRequest request) {
		String order_no = request.getParameter("order_no");
		String remark = request.getParameter("remark");
		String page_from = request.getParameter("page_from");
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("order_no", order_no);
		modelAndView.addObject("remark", remark);
		modelAndView.addObject("page_from", page_from);
		
		try {
			
			C2cOrder order = this.c2cOrderService.get(order_no);
			if (null == order) {
				throw new BusinessException("订单不存在");
			}
			
//			if (StringUtils.isEmptyString(remark)) {
//				throw new BusinessException("请输入取消理由");
//			}
			
			order.setRemark("订单号:"+order.getOrderNo()+"管理员取消订单:"+remark);
			
			this.c2cOrderService.saveOrderCancel(order, "manager");
			
//			CloseDelayThread closeDelayThread = new CloseDelayThread(order, this.c2cOrderService);
//			Thread t = new Thread(closeDelayThread);
//			t.start();
			
		} catch (BusinessException e) {	
			modelAndView.addObject("error", e.getMessage());
			if ("detail".equals(page_from)) {
				modelAndView.setViewName("redirect:/" + action + "detail.action");
			} else {
				modelAndView.setViewName("redirect:/" + action + "list.action");
			}
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			if ("detail".equals(page_from)) {
				modelAndView.setViewName("redirect:/" + action + "detail.action");
			} else {
				modelAndView.setViewName("redirect:/" + action + "list.action");
			}
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		if ("detail".equals(page_from)) {
			modelAndView.setViewName("redirect:/" + action + "detail.action");
		} else {
			modelAndView.setViewName("redirect:/" + action + "list.action");
		}
		return modelAndView;
	}
	
	/**
	 * 手动转账
	 */
	@RequestMapping(action + "orderPay.action")
	public ModelAndView orderPay(HttpServletRequest request) {
		String order_no = request.getParameter("order_no");
		String payment_method_id_order_pay = request.getParameter("payment_method_id_order_pay");
		String page_from = request.getParameter("page_from");
		String safeword = request.getParameter("safeword");
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("order_no", order_no);
		modelAndView.addObject("page_from", page_from);		
		
		boolean lock = false;
		try {
			
			if (!C2cOrderLock.add(order_no)) {
				throw new BusinessException("系统繁忙，请稍后重试");
			}
			
			lock = true;

			this.adminC2cOrderService.saveOrderPay(order_no, safeword, this.getUsername_login(), payment_method_id_order_pay);
			
			ThreadUtils.sleep(100);
			
			
		} catch (BusinessException e) {	
			modelAndView.addObject("error", e.getMessage());
			if ("detail".equals(page_from)) {
				modelAndView.setViewName("redirect:/" + action + "detail.action");
			} else {
				modelAndView.setViewName("redirect:/" + action + "list.action");
			}
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			if ("detail".equals(page_from)) {
				modelAndView.setViewName("redirect:/" + action + "detail.action");
			} else {
				modelAndView.setViewName("redirect:/" + action + "list.action");
			}
			return modelAndView;
		} finally {
			if (lock) {
				C2cOrderLock.remove(order_no);
			}
		}
		
		modelAndView.addObject("message", "操作成功");
		if ("detail".equals(page_from)) {
			modelAndView.setViewName("redirect:/" + action + "detail.action");
		} else {
			modelAndView.setViewName("redirect:/" + action + "list.action");
		}
		return modelAndView;
	}
	
	/**
	 * 获取 订单的支付方式
	 */
	@RequestMapping(action + "getOrderPayments.action")
	public String getOrderPayments(HttpServletRequest request) {
		String order_no = request.getParameter("order_no");
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			
			List<C2cPaymentMethod> methodList = this.adminC2cOrderService.getOrderPayments(order_no);
			
			resultMap.put("code", 200);
			resultMap.put("payments", methodList);
			
		} catch (BusinessException e) {			
			resultMap.put("code", 500);
			resultMap.put("message", e.getMessage());
		} catch (Throwable t) {			
			logger.error(" error ", t);
			resultMap.put("code", 500);
			resultMap.put("message", "程序错误");
		}
		
		return JsonUtils.getJsonString(resultMap);
	}
	
	/**
	 * 获取 C2C订单 详情
	 */	
	@RequestMapping(action + "detail.action")
	public ModelAndView detail(HttpServletRequest request) {
		String order_no = request.getParameter("order_no");
		
		ModelAndView modelAndView = new ModelAndView();
		
		try {
			
			C2cOrder order = this.c2cOrderService.get(order_no);
			if (null == order) {
				throw new BusinessException("订单不存在");
			}
			
			Map<String, Object> detail_map = this.adminC2cOrderService.detail(order);
			
			Party user = this.partyService.cachePartyBy(order.getPartyId(), false);
			detail_map.put("username", user.getUsername());
			
			modelAndView.addObject("detail_map", detail_map);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.setViewName("redirect:/" + action + "list.action");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.setViewName("redirect:/" + action + "list.action");
			return modelAndView;
		}
		
		modelAndView.setViewName("c2c_order_detail");
		return modelAndView;
	}
	
	/**
	 * 刷新 C2C订单 详情
	 */	
	@RequestMapping(action + "getAjaxOrderDetail.action")
	public String getAjaxOrderDetail(HttpServletRequest request) {
		String order_no = request.getParameter("order_no");
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			
			C2cOrder order = this.c2cOrderService.get(order_no);
			if (null == order) {
				throw new BusinessException("订单不存在");
			}
			
			Map<String, Object> map = this.adminC2cOrderService.detail(order);
			
			Party user = this.partyService.cachePartyBy(order.getPartyId(), false);
			map.put("username", user.getUsername());
			
			resultMap.put("code", 200);
			resultMap.put("data", map);
			
		} catch (BusinessException e) {
			resultMap.put("code", 500);
			resultMap.put("message", e.getMessage());
		} catch (Throwable t) {
			logger.error(" error ", t);
			resultMap.put("code", 500);
			resultMap.put("message", "程序错误");
		}
		
		return JsonUtils.getJsonString(resultMap);
	}
	
	/**
	 * 新线程处理，直接拿到订单锁处理完成后退出
	 */
//	public class CloseDelayThread implements Runnable {
//		
//		private C2cOrderService c2cOrderService;
//		private C2cOrder order;
//
//		public void run() {
//			
//			try {
//				
//				while (true) {
//					if (C2cOrderLock.add(order.getOrderNo())) {
//						this.c2cOrderService.saveOrderCancel(order, "manager");
//						// 处理完退出
//						break;
//					}
//					ThreadUtils.sleep(200);
//				}
//
//			} catch (Throwable t) {
//				logger.error("error:", t);
//			} finally {
//				C2cOrderLock.remove(order.getOrderNo());
//			}
//
//		}
//
//		public CloseDelayThread(C2cOrder order, C2cOrderService c2cOrderService) {
//			this.order = order;
//			this.c2cOrderService = c2cOrderService;
//		}
//
//	}
	
}
