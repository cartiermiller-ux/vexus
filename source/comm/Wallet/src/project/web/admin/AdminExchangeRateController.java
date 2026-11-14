package project.web.admin;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.wallet.rate.ExchangeRate;
import project.wallet.rate.ExchangeRateService;

/**
 * 货币汇率配置
 *
 */
@RestController
public class AdminExchangeRateController extends PageActionSupport {

	private static final Logger logger = LoggerFactory.getLogger(AdminExchangeRateController.class);
	
	@Autowired
	private ExchangeRateService exchangeRateService;

	private final String action = "normal/adminExchangeRateAction!";
	
	/**
	 * 货币汇率配置-列表
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		
		int pageNo = this.checkAndSetPageNo(request.getParameter("pageNo"));
		int pageSize = 10;
		Page page = this.exchangeRateService.pagedQuery(pageNo, pageSize, null);
		ModelAndView model = new ModelAndView();
		model.addObject("pageNo", pageNo);
		model.addObject("pageSize", pageSize);
		model.addObject("page", page);
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("exchangerate_list");
		return model;
	}

	/**
	 * 货币汇率配置-更新
	 */
	@RequestMapping(action + "toUpdate.action")
	public ModelAndView toUpdate(HttpServletRequest request) {
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		String id = request.getParameter("id");
		String rata = request.getParameter("rata");
		
		ModelAndView model = new ModelAndView();
		model.addObject("message", message);
		model.addObject("error", error);
		model.addObject("id", id);
		model.addObject("rata", rata);
		model.setViewName("exchangerate_update");
		return model;
	}

	@RequestMapping(action + "update.action")
	public ModelAndView update(HttpServletRequest request) {
		String rate_temp = request.getParameter("rata");
		if (StringUtils.isNullOrEmpty(rate_temp) 
				|| !StringUtils.isDouble(rate_temp) || Double.valueOf(rate_temp) <= 0) {
			throw new BusinessException("汇率错误");
		}
		
		ModelAndView model = new ModelAndView();
		try {
			String id = request.getParameter("id");
			ExchangeRate exchangeRate = exchangeRateService.get(id);
			if (exchangeRate != null) {
				exchangeRate.setRata(Double.valueOf(rate_temp));
				exchangeRateService.update(exchangeRate);
			}
		} catch (BusinessException e) {
			model.addObject("error", e.getMessage());
			model.setViewName("exchangerate_update");
			return model;
		} catch (Throwable t) {
			logger.error("update error ", t);
			model.addObject("error", "程序错误");
			model.setViewName("exchangerate_update");
		}
		model.addObject("message", "操作成功");
		model.setViewName("redirect:/" + action + "list.action");
		return model;
	}

}
