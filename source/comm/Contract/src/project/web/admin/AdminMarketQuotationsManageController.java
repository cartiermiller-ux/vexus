package project.web.admin;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
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
import kernel.util.Arith;
import kernel.util.JsonUtils;
import kernel.util.StringUtils;
import kernel.util.ThreadUtils;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.Constants;
import project.contract.AdminMarketQuotationsService;
import project.data.AdjustmentValue;
import project.data.AdjustmentValueService;
import project.data.DataCache;
import project.data.DataService;
import project.data.model.Realtime;
import project.item.ItemService;
import project.item.model.Item;
import project.log.LogService;
import security.SecUser;
import security.internal.SecUserService;

/**
 * 行情数据
 *
 */
@RestController
public class AdminMarketQuotationsManageController extends PageActionSupport {
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private ItemService itemService;
	
	@Autowired
	private DataService dataService;
	
	@Autowired
	private SecUserService secUserService;
	
	@Autowired
	private AdjustmentValueService adjustmentValueService;
	
	@Autowired
	private AdminMarketQuotationsService adminMarketQuotationsService;
	
	private final String action = "normal/adminMarketQuotationsManageAction!";
	
	private static final Logger logger = LoggerFactory.getLogger(AdminMarketQuotationsManageController.class);

	/**
	 * 列表
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		
		int pageSize = 30;
		int pageNo=checkAndSetPageNo(request.getParameter("pageNo"));
		Page page = this.adminMarketQuotationsService.pageQuery(pageNo, pageSize);
		
		ModelAndView model = new ModelAndView();
		model.addObject("pageNo", pageNo);
		model.addObject("pageSize", pageSize);
		model.addObject("page", page);
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("market_quotations_list");
		return model;
	}

	/**
	 * 行情管理-调整弹框显示
	 */
	@RequestMapping(action + "showModal.action")
	public String showModal(HttpServletRequest request) {
		if (!StringUtils.isNullOrEmpty(getLoginPartyId())) {
			return "";
		}
		
		String symbol = request.getParameter("symbol");
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Realtime realtime = DataCache.getRealtime(symbol);
		if (realtime == null) {
			realtime = this.dataService.realtime(symbol).get(0);
		}
		Item item = this.itemService.cacheBySymbol(symbol, false);
		Double currentValues = this.adjustmentValueService.getCurrentValue(symbol);
		String currentValue = new BigDecimal(String.valueOf(currentValues)).toPlainString();
		if (currentValue == null) {
			resultMap.put("adjust_value", 0D);
		} else {
			resultMap.put("adjust_value", currentValue);
		}
		if (currentValue == null) {
			resultMap.put("new_price", realtime.getClose());
		} else {
			resultMap.put("new_price", Arith.sub(realtime.getClose(), currentValues));
		}

		resultMap.put("pips",new BigDecimal(String.valueOf(item.getPips())).toPlainString());
		resultMap.put("totalPrice",new BigDecimal(String.valueOf(realtime.getClose()+currentValues)).toPlainString());
		AdjustmentValue delayValue = this.adjustmentValueService.getDelayValue(symbol);

		if (delayValue != null) {
			resultMap.put("delay_value", delayValue.getValue());
			resultMap.put("delay_second", delayValue.getSecond());
		}
		return JsonUtils.getJsonString(resultMap);
	}

	/**
	 * 页面计算
	 * 
	 */
	@RequestMapping(action + "getValue.action")
	public String getValue(HttpServletRequest request) {
		if (!StringUtils.isNullOrEmpty(getLoginPartyId())) {
			return "";
		}
		
		String symbol = request.getParameter("symbol");
		// 0增加一个pips 1减少一个pips 2直接修改调整值
		String type_temp = request.getParameter("type");
		String value_temp = request.getParameter("value");
		
		Double type = Double.valueOf(type_temp);
		Double value = Double.valueOf(value_temp);

		Map<String, Object> resultMap = new HashMap<String, Object>();

		Realtime realtime = null;

		if (realtime == null) {
			realtime = this.dataService.realtime(symbol).get(0);
		}
		Item item = this.itemService.cacheBySymbol(symbol, false);
		Double currentValue = this.adjustmentValueService.getCurrentValue(symbol);
		if (currentValue == null) {
			resultMap.put("new_price", realtime.getClose());
		} else {
			resultMap.put("new_price", Arith.sub(realtime.getClose(), currentValue));
		}

		double temp;
		if (type == 0) {
			temp = Arith.add(value, item.getPips());
			// 调整量
			resultMap.put("adjust_current_value",new BigDecimal(String.valueOf(temp)).toPlainString());
			// 调整后的值
			resultMap.put("adjust_value_after",new BigDecimal(String.valueOf(Arith.add(realtime.getClose(), temp))).toPlainString());
		} else if (type == 1) {
			temp = Arith.sub(value, item.getPips());

			resultMap.put("adjust_current_value", new BigDecimal(String.valueOf(temp)).toPlainString());
			resultMap.put("adjust_value_after",new BigDecimal(String.valueOf(Arith.add(realtime.getClose(), temp))).toPlainString());
		} else {
			temp = value;
			resultMap.put("adjust_current_value", new BigDecimal(String.valueOf(temp)).toPlainString());
			resultMap.put("adjust_value_after",new BigDecimal(String.valueOf(Arith.add(realtime.getClose(), temp))).toPlainString());
		}

		if (currentValue == null) {
			resultMap.put("adjust_value",new BigDecimal(String.valueOf(item.getPips())).toPlainString());
		} else {
			resultMap.put("adjust_value",new BigDecimal(String.valueOf(Arith.add(temp, currentValue))).toPlainString());
		}
		AdjustmentValue delayValue = this.adjustmentValueService.getDelayValue(symbol);

		if (delayValue != null) {
			resultMap.put("delay_value", delayValue.getValue());
			resultMap.put("delay_second", delayValue.getSecond());
		}

		return JsonUtils.getJsonString(resultMap);
	}

	/**
	 * 调整
	 */
	@RequestMapping(action + "adjust.action")
	public ModelAndView adjust(HttpServletRequest request) {
		ModelAndView model = new ModelAndView();
		String message = "";
		String error = "";
		
		// 调整值
		String value = request.getParameter("value");
		String symbol = request.getParameter("symbol");
		// 延迟秒
		String second = request.getParameter("second");
		
		if (StringUtils.isNullOrEmpty(value)) {
			throw new BusinessException("调整值不能为空!");
		}
		try {
			
			Double currentValue = this.adjustmentValueService.getCurrentValue(symbol);
			if (currentValue == null) {
				Realtime realtime =  this.dataService.realtime(symbol).get(0);;
				currentValue=realtime.getClose();
			} 
			SecUser sec = this.secUserService.findUserByLoginName(this.getUsername_login());
			String log = MessageFormat.format("ip:"+this.getIp()+",管理员调整行情,币种:{0},原值:{1},调整值:{2},调整时间:{3}", 
					symbol,new BigDecimal(currentValue).toPlainString(),new BigDecimal(value).toPlainString(),second);
			
			this.adjustmentValueService.adjust(symbol, Double.valueOf(value), Double.valueOf(second));
			saveLog(sec, this.getUsername_login(), log);
			
			ThreadUtils.sleep(1000);
			message = "操作成功";
		} catch (BusinessException e) {
		    error = e.getMessage();
		} catch (Exception e) {
			logger.error("error ", e);
			error = "程序错误";
		}
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("redirect:/" + action + "list.action");
	    return model;
	}

	public void saveLog(SecUser secUser, String operator,String context) {
		project.log.Log log = new project.log.Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		log.setOperator(operator);
		log.setUsername(secUser.getUsername());
		log.setPartyId(secUser.getPartyId());
		log.setLog(context);
		log.setCreateTime(new Date());
		logService.saveSync(log);
	}	
}
