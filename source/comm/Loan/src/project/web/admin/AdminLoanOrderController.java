package project.web.admin;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.util.JsonUtils;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.loan.LoanOrderService;
import project.loan.LoanRelationOrderService;

/**
 * 质押借币 后台页面
 *
 */
@RestController
public class AdminLoanOrderController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminLoanOrderController.class);
	
	@Autowired
	LoanOrderService loanOrderService;
	@Autowired
	LoanRelationOrderService loanRelationOrderService;
	
	private final String action = "/normal/adminLoanOrder!";
	
	/**
	 * 质押借币订单
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String pageNoStr = request.getParameter("pageNo");
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		String orderNo = request.getParameter("orderNo");
		String roleName = request.getParameter("roleName");
		String userParam = request.getParameter("userParam");
		String state = request.getParameter("state");
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("loan_list");

		int pageNo=1;
		Page page = null;
		int pageSize=300;
		try {
			pageNo=checkAndSetPageNo(pageNoStr);
			page = loanOrderService.pagedQueryAdmin(pageNo, pageSize, userParam, orderNo, roleName, state);
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
		modelAndView.addObject("userParam", userParam);
		return modelAndView;
	}
	
	@RequestMapping(action + "queryLoanRelation.action")
	public String queryLoanRelation(HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			String id = request.getParameter("id");
			resultMap.put("code", 200);
			resultMap.put("loanRelation", loanRelationOrderService.queryLoanRelation(id));
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
	
}
