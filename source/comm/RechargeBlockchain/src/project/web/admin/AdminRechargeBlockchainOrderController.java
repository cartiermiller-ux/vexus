package project.web.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.util.ThreadUtils;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.Constants;
import project.blockchain.AdminRechargeBlockchainOrderService;
import util.LockFilter;

/**
 * 区块链充值订单
 */
@RestController
public class AdminRechargeBlockchainOrderController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminRechargeBlockchainOrderController.class);

	@Autowired
	private AdminRechargeBlockchainOrderService adminRechargeBlockchainOrderService;

	private final String action = "normal/adminRechargeBlockchainOrderAction!";

	/**
	 * 获取 区块链充值订单 列表
	 * name_para 链名称 
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String pageNoStr = request.getParameter("pageNo");
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		String name_para = request.getParameter("name_para");
		String state_para = request.getParameter("state_para");
		String order_no_para = request.getParameter("order_no_para");
		String rolename_para = request.getParameter("rolename_para");
		String start_time = request.getParameter("start_time");
		String end_time = request.getParameter("end_time");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("recharge_blockchain_list");

		int pageNo=1;
		Page page=null;
		int pageSize=20;
		try {
			pageNo=checkAndSetPageNo(pageNoStr);
			Integer state_para_int = null;
			if (!StringUtils.isEmptyString(state_para)) {
				state_para_int = Integer.valueOf(state_para);
			}

			String loginPartyId = getLoginPartyId();

			page = this.adminRechargeBlockchainOrderService.pagedQuery(pageNo, pageSize, name_para,
					state_para_int, loginPartyId, order_no_para, rolename_para, start_time, end_time);

			List<Map> list = page.getElements();
			for (int i = 0; i < list.size(); i++) {
				Map map = list.get(i);
				map.put("channel_amount", new BigDecimal(map.get("channel_amount") + "").toString());
				
				if (null == map.get("rolename")) {
					map.put("roleNameDesc", "");
				} else {
					String roleName = map.get("rolename").toString();
					map.put("roleNameDesc", Constants.ROLE_MAP.containsKey(roleName) ? Constants.ROLE_MAP.get(roleName) : roleName);
				}
			}
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
		modelAndView.addObject("name_para", name_para);
		modelAndView.addObject("state_para", state_para);
		modelAndView.addObject("order_no_para", order_no_para);
		modelAndView.addObject("rolename_para", rolename_para);
		modelAndView.addObject("start_time", start_time);
		modelAndView.addObject("end_time", end_time);
		return modelAndView;
	}

	/**
	 * 手动到账
	 */
	@RequestMapping(action + "onsucceeded.action")
	public ModelAndView onsucceeded(HttpServletRequest request) {
		String orderId = request.getParameter("order_id");
		String success_amount = request.getParameter("success_amount");
		String safeword = request.getParameter("safeword");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");

		boolean lock = false;
		try {
			if (!LockFilter.add(orderId)) {
				throw new BusinessException("系统繁忙，请稍后重试");
			}
			
			lock = true;
			
			if (StringUtils.isNullOrEmpty(orderId)) {
				throw new BusinessException("充值订单ID必填");
			}
			if (StringUtils.isNullOrEmpty(success_amount)) {
				throw new BusinessException("到账金额必填");
			}
			if (!StringUtils.isDouble(success_amount)) {
				throw new BusinessException("到账金额不是浮点数");
			}
			if (Double.valueOf(success_amount).doubleValue() < 0) {
				throw new BusinessException("到账金额不能小于0");
			}
			
			this.adminRechargeBlockchainOrderService.saveSucceeded(orderId, safeword, this.getUsername_login(), Double.valueOf(success_amount));
			ThreadUtils.sleep(300);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error("update error ", t);
			modelAndView.addObject("error", "程序错误");
			return modelAndView;
		} finally {
			if (lock) {
				LockFilter.remove(orderId);
			}
		}
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
	}

	/**
	 * 驳回充值申请
	 */
	@RequestMapping(action + "reject.action")
	public ModelAndView reject(HttpServletRequest request) {
		String id = request.getParameter("id");
		String failure_msg = request.getParameter("failure_msg");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");

		boolean lock = false;
		
		try {
			
			if (!LockFilter.add(id)) {
				throw new BusinessException("系统繁忙，请稍后重试");
			}
			
			lock = true;
			
			// 统一处理失败接口
			this.adminRechargeBlockchainOrderService.saveReject(id, failure_msg, this.getUsername_login(), this.getLoginPartyId());
			
			ThreadUtils.sleep(300);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error("update error ", t);
			modelAndView.addObject("error", "程序错误");
			return modelAndView;
		} finally {
			if (lock) {
				LockFilter.remove(id);
			}
		}
		
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
	}

	/**
	 * 修改备注信息
	 */
	@RequestMapping(action + "reject_remark.action")
	public ModelAndView reject_remark(HttpServletRequest request) {
		String id = request.getParameter("id");
		String failure_msg = request.getParameter("remark");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");

		boolean lock = false;
		
		try {
			
			if (!LockFilter.add(id)) {
				throw new BusinessException("系统繁忙，请稍后重试");
			}
			
			lock = true;
			
			// 统一处理失败接口
			this.adminRechargeBlockchainOrderService.saveRejectRemark(id, failure_msg, this.getUsername_login(), this.getLoginPartyId());
			
			ThreadUtils.sleep(300);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error("update error ", t);
			modelAndView.addObject("error", "程序错误");
			return modelAndView;
		} finally {
			if (lock) {
				LockFilter.remove(id);
			}
		}
		
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
	}

}
