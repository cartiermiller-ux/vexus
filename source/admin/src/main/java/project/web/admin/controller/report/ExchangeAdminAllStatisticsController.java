package project.web.admin.controller.report;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.web.admin.service.report.AdminAllStatisticsService;

/**
 * 交易所_总充提报表
 */
@RestController
public class ExchangeAdminAllStatisticsController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(ExchangeAdminAllStatisticsController.class);

	@Autowired
	private AdminAllStatisticsService adminAllStatisticsService;
	
	private final String action = "normal/exchangeAdminAllStatisticsAction!";

	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String start_time = request.getParameter("start_time");
		String end_time = request.getParameter("end_time");
		String para_time = request.getParameter("para_time");

		ModelAndView modelAndView = new ModelAndView();
		
		int pageNo = 1;
		int pageSize = 30;
		Page page = null;
		try {
			
			pageNo = this.checkAndSetPageNo(request.getParameter("pageNo"));

			if (null == start_time && null == end_time && null == para_time) {
				end_time = DateUtils.format(new Date(), DateUtils.DF_yyyyMMdd);
				start_time = DateUtils.format(DateUtils.getMonthStart(new Date()), DateUtils.DF_yyyyMMdd);
			}
			if ("day".equals(para_time)) {
				// 当天
				end_time = DateUtils.format(new Date(), DateUtils.DF_yyyyMMdd);
				start_time = end_time;
			} else if ("week".equals(para_time)) {
				// 往前推7天
				end_time = DateUtils.format(new Date(), DateUtils.DF_yyyyMMdd);
				start_time = DateUtils.format(DateUtils.addDate(new Date(), -7), DateUtils.DF_yyyyMMdd);
			} else if ("month".equals(para_time)) {
				// 往前推一月
				end_time = DateUtils.format(new Date(), DateUtils.DF_yyyyMMdd);
				start_time = DateUtils.format(DateUtils.addMonth(new Date(), -1), DateUtils.DF_yyyyMMdd);
			} else if ("all".equals(para_time)) {
				// 所有数据
				end_time = null;
				start_time = null;
			}

			page = this.adminAllStatisticsService.pagedQuery(pageNo, pageSize, start_time, end_time,
					this.getLoginPartyId());
			
			Map<String, Object> sumdata = this.adminAllStatisticsService.sumDatas(start_time, end_time, this.getLoginPartyId());
			if(ObjectUtils.isNotEmpty(sumdata)) {
				sumdata.put("recharge", new BigDecimal(sumdata.get("recharge").toString()));
				sumdata.put("recharge_usdt", new BigDecimal(sumdata.get("recharge_usdt").toString()));
			}
			modelAndView.addObject("sumdata", sumdata);
			
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
		modelAndView.addObject("start_time", start_time);
		modelAndView.addObject("end_time", end_time);
		modelAndView.addObject("para_time", para_time);
		modelAndView.setViewName("statistics_all_list");
		return modelAndView;
	}

	/**
	 * 导出订单数据到文件
	 */
	@RequestMapping(action + "exportData.action")
	public ModelAndView exportData(HttpServletRequest request) {
		String pageNo = request.getParameter("pageNo");
		String start_time = request.getParameter("start_time");
		String end_time = request.getParameter("end_time");
		String para_time = request.getParameter("para_time");

		ModelAndView modelAndView = new ModelAndView();

		try {
			
			this.checkAndSetPageNo(pageNo);

			if (null == start_time && null == end_time && null == para_time) {
				end_time = DateUtils.format(new Date(), DateUtils.DF_yyyyMMdd);
				start_time = DateUtils.format(DateUtils.getMonthStart(new Date()), DateUtils.DF_yyyyMMdd);
			}
			if ("day".equals(para_time)) {
				// 当天
				end_time = DateUtils.format(new Date(), DateUtils.DF_yyyyMMdd);
				start_time = end_time;
			} else if ("week".equals(para_time)) {
				// 往前推7天
				end_time = DateUtils.format(new Date(), DateUtils.DF_yyyyMMdd);
				start_time = DateUtils.format(DateUtils.addDate(new Date(), -7), DateUtils.DF_yyyyMMdd);
			} else if ("month".equals(para_time)) {
				// 往前推一月
				end_time = DateUtils.format(new Date(), DateUtils.DF_yyyyMMdd);
				start_time = DateUtils.format(DateUtils.addMonth(new Date(), -1), DateUtils.DF_yyyyMMdd);
			} else if ("all".equals(para_time)) {
				// 所有数据
				end_time = null;
				start_time = null;
			}
			
			String error = this.adminAllStatisticsService.loadExportData(this.getResponse(), 30, start_time,
					end_time, this.getLoginPartyId());
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}

		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (IOException e) {
			logger.error("export fail:{}", e);
			modelAndView.addObject("error", "程序错误,导出异常");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			return modelAndView;
		}

		modelAndView.addObject("start_time", start_time);
		modelAndView.addObject("end_time", end_time);
		modelAndView.addObject("para_time", para_time);
		modelAndView.setViewName("statistics_all_list");
		return modelAndView;
	}

}
