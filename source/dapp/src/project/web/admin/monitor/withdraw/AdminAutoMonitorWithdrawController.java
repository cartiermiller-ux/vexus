package project.web.admin.monitor.withdraw;

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
import project.monitor.job.transferfrom.LockFilter;
import project.monitor.withdraw.AdminAutoMonitorWithdrawService;

/**
 * DAPP_提现订单
 *
 */
@RestController
public class AdminAutoMonitorWithdrawController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminAutoMonitorWithdrawController.class);
	
	@Autowired
	private AdminAutoMonitorWithdrawService adminAutoMonitorWithdrawService;
	
	private final String action = "normal/adminAutoMonitorWithdrawAction!";

	@RequestMapping(value = action + "list.action") 
	public ModelAndView list(HttpServletRequest request) {
		int pageNo = this.checkAndSetPageNo(request.getParameter("pageNo"));
		int pageSize = 20;
		String loginPartyId = getLoginPartyId();
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		
		String name_para = request.getParameter("name_para");
		
		Integer succeeded_para = null;
		if (!StringUtils.isNullOrEmpty(request.getParameter("succeeded_para"))) {
			succeeded_para = Integer.valueOf(request.getParameter("succeeded_para"));
		}
		 
		String order_no_para = request.getParameter("order_no_para");
		String rolename_para = request.getParameter("rolename_para");
		
		Page page = this.adminAutoMonitorWithdrawService.pagedQuery(pageNo, pageSize, 
				name_para, succeeded_para,
				loginPartyId, order_no_para, rolename_para);

		List<Map> list = page.getElements();
		for (int i = 0; i < list.size(); i++) {
			Map map = list.get(i);
			map.put("volume", new BigDecimal(map.get("volume").toString()).toPlainString());
			map.put("amount", new BigDecimal(map.get("amount").toString()).toPlainString());
			map.put("createTime", map.get("createTime") == null ? null : map.get("createTime").toString().substring(0, 10));
			map.put("reviewTime", map.get("reviewTime") == null ? null : map.get("reviewTime").toString().substring(0, 10));
			
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
		model.addObject("message", message);
		model.addObject("error", error);
		
		model.addObject("name_para", name_para);
		model.addObject("rolename_para", rolename_para);
		
		model.setViewName("auto_monitor_withdraw_list");
	    return model;
	}

	/**
	 * 通过申请（手动打款）
	 */
	@RequestMapping(value = action + "success.action") 
	public ModelAndView success(HttpServletRequest request) {
		
		String message = "";
		String error = "";
		String id = request.getParameter("id");
		String safeword = request.getParameter("safeword");
		boolean lock = false;
		try {
			if (!LockFilter.add(id)) {
				return list(request);
			}
			lock = true;
			adminAutoMonitorWithdrawService.saveSucceeded(id, safeword, this.getUsername_login(),
					this.getLoginPartyId());
			ThreadUtils.sleep(300);
			message = "操作成功";
		} catch (BusinessException e) {
			error = e.getMessage();
		} catch (Throwable t) {
			logger.error(" error ", t);
			error = ("[ERROR] 服务器错误");
		} finally {
			if (lock) {
				LockFilter.remove(id);
			}
		}
		
		ModelAndView model = new ModelAndView();
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("redirect:/" + action + "list.action");
	    return model;
	}

	/**
	 * 通过申请（加入质押总资产）
	 */
	@RequestMapping(value = action + "success_collection.action") 
	public ModelAndView success_collection(HttpServletRequest request) {
		
		String message = "";
		String error = "";
		String id = request.getParameter("id");
		String safeword = request.getParameter("safeword");
		boolean lock = false;
		try {
			if (!LockFilter.add(id)) {
				return list(request);
			}
			lock = true;
			adminAutoMonitorWithdrawService.saveSucceeded_Collection(id, safeword, this.getUsername_login(),
					this.getLoginPartyId());
			ThreadUtils.sleep(300);
			message = "操作成功";
		} catch (BusinessException e) {
			error = e.getMessage();
		} catch (Throwable t) {
			logger.error(" error ", t);
			error = ("[ERROR] 服务器错误");
		} finally {
			if (lock) {
				LockFilter.remove(id);
			}
		}
		ModelAndView model = new ModelAndView();
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("redirect:/" + action + "list.action");
	    return model;
	}


	/**
	 * 处理一个代付
	 */
	@RequestMapping(value = action + "successThird.action") 
	public ModelAndView successThird(HttpServletRequest request) {
		
		String message = "";
		String error = "";
		String id = request.getParameter("id");
		String safeword = request.getParameter("safeword");
		boolean lock = false;
		try {
			if (!LockFilter.add(id)) {
				return list(request);
			}
			lock = true;
			adminAutoMonitorWithdrawService.saveSucceededThird(id, safeword, this.getUsername_login(),
					this.getLoginPartyId());
			ThreadUtils.sleep(300);
			message = "操作成功";
		} catch (BusinessException e) {
			error = e.getMessage();
		} catch (Throwable t) {
			logger.error(" error ", t);
			error = ("[ERROR] 服务器错误");
		} finally {
			if (lock) {
				LockFilter.remove(id);
			}
		}
		ModelAndView model = new ModelAndView();
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("redirect:/" + action + "list.action");
	    return model;
	}

	/**
	 * 驳回申请
	 */
	@RequestMapping(value = action + "reject.action") 
	public ModelAndView reject(HttpServletRequest request) {
		
		String message = "";
		String error = "";
		String id = request.getParameter("id");
		String failure_msg = request.getParameter("failure_msg");
		boolean lock = false;
		try {
			if (!LockFilter.add(id)) {
				return list(request);
			}
			lock = true;
			this.adminAutoMonitorWithdrawService.saveReject(id, failure_msg, this.getUsername_login(),
					this.getLoginPartyId());
			ThreadUtils.sleep(300);
			message = "操作成功";
		} catch (BusinessException e) {
			this.error = e.getMessage();
		} catch (Throwable t) {
			logger.error(" error ", t);
			error = ("[ERROR] 服务器错误");
		}  finally {
			if (lock) {
				LockFilter.remove(id);
			}
		}
		ModelAndView model = new ModelAndView();
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("redirect:/" + action + "list.action");
	    return model;
	}
}
