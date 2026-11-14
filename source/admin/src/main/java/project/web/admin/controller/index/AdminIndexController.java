package project.web.admin.controller.index;

import java.util.Date;
import java.util.HashMap;
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
import kernel.util.DateUtils;
import project.Constants;
import project.party.PartyService;
import project.party.model.Party;
import project.user.UserDataService;
import project.web.admin.service.report.AdminAllStatisticsService;
import project.web.admin.service.report.AdminUserMoneyStatisticsService;
import security.web.BaseSecurityAction;

/**
 * 综合查询
 */
@RestController
public class AdminIndexController extends BaseSecurityAction {

	private Logger logger = LoggerFactory.getLogger(AdminIndexController.class);

	@Autowired
	private AdminAllStatisticsService adminAllStatisticsService;
	@Autowired
	private AdminUserMoneyStatisticsService adminUserMoneyStatisticsService;
	@Autowired
	private PartyService partyService;
	@Autowired
	private UserDataService userDataService;

	private final String action = "normal/adminIndexAction!";

	/**
	 * 综合查询 页面
	 */
	@RequestMapping(value = action + "view.action")
	public ModelAndView view(HttpServletRequest request) {
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("index_admin");

		Map<String, Object> statistics = new HashMap<String, Object>();		
		statistics.put("recharge", 0.0);
		statistics.put("withdraw", 0.0);
		// 充提差额
		statistics.put("balance_amount", 0.0);
		statistics.put("totle_income", 0.0);
		statistics.put("today_user_count", 0);
		statistics.put("all_user_count", 0);
		statistics.put("today_recharge_user_count", 0);
		statistics.put("sum_usdt_amount", 0.0);	
		
		try {
			
			String today = DateUtils.format(new Date(), DateUtils.DF_yyyyMMdd);
			
			String loginPartyId = this.getLoginPartyId();
			
			// 当日用户增量
			int todayUserCount = 0;
			
			// 用户总量
			int allUserCount = 0;
			
			List<Party> cacheAll = this.partyService.getAll();
			for (Party party : cacheAll) {
				if (Constants.SECURITY_ROLE_MEMBER.equals(party.getRolename())) {
					if (today.equals(DateUtils.format(party.getCreateTime(), DateUtils.DF_yyyyMMdd))) {
						todayUserCount++;
					}
					allUserCount++;
				}
			}
			
			Map<String, Object> daySumData = this.adminAllStatisticsService.daySumData(loginPartyId, today);
			
			statistics.put("recharge", daySumData.get("recharge_usdt"));
			statistics.put("withdraw", daySumData.get("withdraw"));
			// 充提差额
			statistics.put("balance_amount", daySumData.get("balance_amount"));
			statistics.put("totle_income", daySumData.get("totle_income"));
			statistics.put("today_user_count", todayUserCount);
			statistics.put("all_user_count", allUserCount);
			statistics.put("today_recharge_user_count", this.userDataService.filterRechargeByDate(today));
			statistics.put("sum_usdt_amount", this.adminUserMoneyStatisticsService.getSumWalletByMember(loginPartyId));	

		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.addObject("statistics", statistics);
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.addObject("statistics", statistics);
			return modelAndView;
		}

		modelAndView.addObject("statistics", statistics);
		return modelAndView;
	}

}
