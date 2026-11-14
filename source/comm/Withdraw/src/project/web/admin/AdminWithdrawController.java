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
import project.withdraw.AdminWithdrawService;
import util.LockFilter;

/**
 * 提现订单
 */
@RestController
public class AdminWithdrawController extends PageActionSupport {

	@Autowired
	private AdminWithdrawService adminWithdrawService;

	private static final String action = "normal/adminWithdrawAction!";
	
	private static final Logger logger = LoggerFactory.getLogger(AdminWithdrawController.class);

	/**
	 * 获取 提现订单 列表
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String pageNoStr = request.getParameter("pageNo");
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		String name_para = request.getParameter("name_para");
		String succeeded_para = request.getParameter("succeeded_para");
		String order_no_para = request.getParameter("order_no_para");
		String rolename_para = request.getParameter("rolename_para");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("withdraw_list");

		int pageNo=1;
		Page page=null;
		int pageSize=20;
		try {
			pageNo=checkAndSetPageNo(pageNoStr);
			
			Integer succeeded_para_int = null;
			if (!StringUtils.isEmptyString(succeeded_para)) {
				succeeded_para_int = Integer.valueOf(succeeded_para).intValue();
			}

			String loginPartyId = this.getLoginPartyId();

			page = this.adminWithdrawService.pagedQuery(pageNo, pageSize, name_para, succeeded_para_int,
					loginPartyId, order_no_para, rolename_para);

			List<Map> list = page.getElements();
			for (int i = 0; i < list.size(); i++) {
				Map map = list.get(i);
				map.put("volume", new BigDecimal(map.get("volume").toString()).toPlainString());
				map.put("amount", new BigDecimal(map.get("amount").toString()).toPlainString());
				
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
		modelAndView.addObject("succeeded_para", succeeded_para);
		modelAndView.addObject("order_no_para", order_no_para);
		modelAndView.addObject("rolename_para", rolename_para);
		modelAndView.addObject("method_map", Constants.WITHDRAW_METHOD);
		return modelAndView;
	}
	
	/**
	 * 修改用户提现备注描述
	 */
	@RequestMapping(action + "remark.action")
	public ModelAndView updateWithdraw(HttpServletRequest request) {
		String id = request.getParameter("id");
		String remark = request.getParameter("remark");
		ModelAndView modelAndView = new ModelAndView();
		this.adminWithdrawService.updateWithdraw(id,this.getLoginPartyId(), remark);
		modelAndView.setViewName("redirect:/" + action + "list.action");
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
	}

	/**
	 * 修改用户提现地址
	 */
	@RequestMapping(action + "changeAddress.action")
	public ModelAndView changeAddress(HttpServletRequest request) {
		String id = request.getParameter("id");
		String safeword = request.getParameter("safeword");
		String changeAfterAddress = request.getParameter("changeAfterAddress");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");

		boolean lock = false;
		try {
			if (!LockFilter.add(id)) {
				throw new BusinessException("系统繁忙，请稍后重试");
			}
			
			lock = true;
			
			// 统一处理成功接口
			this.adminWithdrawService.saveAddress(id, safeword, this.getUsername_login(), this.getLoginPartyId(), changeAfterAddress);
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
	 * 处理一个代付
	 */
	@RequestMapping(action + "success.action")
	public ModelAndView success(HttpServletRequest request) {
		String id = request.getParameter("id");
		String safeword = request.getParameter("safeword");
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");
		
		boolean lock = false;
		try {
			if (!LockFilter.add(id)) {
				throw new BusinessException("系统繁忙，请稍后重试");
			}
			lock = true;
			
			// 统一处理成功接口
			this.adminWithdrawService.saveSucceeded(id, safeword, this.getUsername_login(), this.getLoginPartyId());
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
	 * 驳回
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
			this.adminWithdrawService.saveReject(id, failure_msg, this.getUsername_login(), this.getLoginPartyId());
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
