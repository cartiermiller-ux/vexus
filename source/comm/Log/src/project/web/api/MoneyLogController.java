package project.web.api;

import java.io.IOException;
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
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.BaseAction;
import kernel.web.Page;
import kernel.web.ResultObject;
import project.Constants;
import project.log.MoneyLog;
import project.log.MoneyLogService;
import project.party.PartyService;
import project.party.model.Party;

@RestController
@CrossOrigin
public class MoneyLogController extends BaseAction {
	
	@Autowired
	protected MoneyLogService moneyLogService;
	
	private static final Logger logger = LoggerFactory.getLogger(MoneyLogController.class);

	@RequestMapping("api/moneylog!list.action")
	public Object list(HttpServletRequest request) throws IOException {
		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);

		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		String partyId = this.getLoginPartyId();
		try {
			
			String page_no = request.getParameter("page_no");
			if (StringUtils.isNullOrEmpty(page_no) 
					|| !StringUtils.isInteger(page_no) || Integer.valueOf(page_no) <= 0) {
				page_no = "1";
			}
			double amount = 0;
			int pageNo = Integer.valueOf(page_no);
			String category = request.getParameter("category");
			String contentType = request.getParameter("contentType");
			PartyService partyService = ApplicationUtil.getBean(PartyService.class);
			Party party = partyService.cachePartyBy(partyId, false);
			if(StringUtils.isNotEmpty(party.getBalance_income_config())) {
				String[] balance_income_configArr = party.getBalance_income_config().split(",");
				if(balance_income_configArr.length >=2) {
					amount = Double.valueOf(balance_income_configArr[1]).doubleValue();
				}				
			}
			Page pagedQuery = moneyLogService.pagedQuery(pageNo, 20, category, contentType, partyId);
			for (MoneyLog log : (List<MoneyLog>) pagedQuery.getElements()) {
				if(Constants.MONEYLOG_CONTENT_FINANCE_PROFIT.equals(log.getContent_type())) {
					log.setAmount(log.getAmount()+amount);
					log.setCreateTimeStr(DateUtils.format(log.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
				}
			}
			resultObject.setData(pagedQuery.getElements());
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}
		return resultObject;
	}
	
	@RequestMapping("api/moneylog!sumBSalance.action")
	public Object sumBSalance(HttpServletRequest request) throws IOException {

		ResultObject resultObject = new ResultObject();
		resultObject = readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		String partyId = this.getLoginPartyId();
		try {			
 			String category = Constants.MONEYLOG_FINANCE;
			String contentType = Constants.MONEYLOG_CONTENT_FINANCE_PROFIT;	
			PartyService partyService = ApplicationUtil.getBean(PartyService.class);
 			Map<String,Object> map = moneyLogService.sumBSalance(category, contentType, partyId);
			Party party = partyService.cachePartyBy(partyId, false);
			if(StringUtils.isNotEmpty(party.getBalance_income_config())) {
				String[] balance_income_configArr = party.getBalance_income_config().split(",");
				
				double sumAmount=Double.parseDouble(map.get("sumAmount").toString())+Double.valueOf(balance_income_configArr[0]).doubleValue();
				if(balance_income_configArr.length >=2) {
					double dateAmount=Double.parseDouble(map.get("dateAmount").toString())+Double.valueOf(balance_income_configArr[1]).doubleValue();
					map.put("dateAmount", dateAmount);
				}			
				map.put("sumAmount", sumAmount);
				
			}		
			resultObject.setData(map);
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}
		return resultObject;
	}
	
}
