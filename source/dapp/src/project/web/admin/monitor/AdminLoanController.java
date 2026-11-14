package project.web.admin.monitor;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.monitor.loan.LoanService;
import project.monitor.loan.SimpleLoanOrder;
import project.party.PartyService;
import project.party.model.Party;

/**
 * @author JORGE
 * @description 借贷管理
 */
@RestController
@SuppressWarnings("unchecked")
public class AdminLoanController extends PageActionSupport {
	/**
	 * 借贷服务
	 */
	@Autowired
	private LoanService loanService;
	
	/**
	 * SLF4J日志组件
	 */
	private static final Logger logger = LoggerFactory.getLogger(AdminLoanController.class);
	
	/**
	 * 时间格式
	 */
	private static final DateFormat DATA_FORMATTER=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * 借贷额度正则式
	 */
	private static final Pattern QUOTA_REGEX=Pattern.compile("([A-Z]{2,8}):([1-9]{1}[0-9]{0,7})");
	
	/**
	 * 获取借贷申请单列表
	 * @return 申请单列表
	 */
	@RequestMapping("normal/loanadmin!list.action")
	public ModelAndView listLoanOrder(HttpServletRequest request) {
		String status=request.getParameter("status");
		String orderNo=request.getParameter("orderNo");
		String userName=request.getParameter("userName");
		int pageNo=checkAndSetPageNo(request.getParameter("pageNo"));
		if(null==status || (status=status.trim()).isEmpty()) status="0";
		
		HashMap<String,Object> paramMap=new HashMap<String,Object>();
		paramMap.put("status", new Integer(status));
		paramMap.put("userName", userName);
		paramMap.put("orderNo", orderNo);
		paramMap.put("pageNo", pageNo);
		paramMap.put("pageSize", 30);
		
		Page page=null;
		try {
			page=loanService.pagedQuery(paramMap);
		}catch(Throwable e) {
			logger.error("list loan order occur error:", e);
		}
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("pageNo", pageNo);
		modelAndView.addObject("status", status);
		modelAndView.addObject("pageSize", pageSize);
		if(null!=page) modelAndView.addObject("page",page);
		modelAndView.setViewName("simple_loan_order_list");
		
		return modelAndView;
	}
	
	/**
	 * 改变借贷申请单状态
	 * @return 申请单列表
	 */
	@RequestMapping("normal/loanadmin!change.action")
	public ModelAndView changeLoanOrderState(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		
		String orderId=getParamValue(request,modelAndView,"orderId");
		if(null==orderId) return modelAndView;
		
		String status=getParamValue(request,modelAndView,"statusStr");
		if(null==status) return modelAndView;
		
		String reason=getParamValue(request,modelAndView,"reason");
		try {
			loanService.updateLoanOrderState(orderId, status,reason);
			modelAndView.addObject("message", "操作成功");
		}catch(BusinessException e) {
			modelAndView.addObject("message", e.getMessage());
			logger.error("change loan order state error:", e);
		}catch(Throwable e) {
			modelAndView.addObject("message", "操作失败");
			logger.error("change loan order state error:", e);
		}
		
		modelAndView.setViewName("redirect:/normal/loanadmin!list.action");
		
		return modelAndView;
	}
	
	/**
	 * 返回借贷申请单添加页面
	 * @return 添加申请单表单页面
	 */
	@RequestMapping("normal/loanadmin!toAdd.action")
	public ModelAndView addBefore(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("simple_loan_order_add");
		
		try {
			HashMap<String,Object> paramMap=loanService.getLoanParams(null);
			if(null!=paramMap) {
				modelAndView.addObject("paramMap",paramMap);
				ApplicationUtil.getHttpSession().setAttribute("paramMap", paramMap);
			}else {
				modelAndView.addObject("code", "0");
				modelAndView.addObject("message", "获取借贷参数失败!");
			}
		}catch(Throwable e) {
			modelAndView.addObject("code", "1");
			modelAndView.addObject("message","程序错误");
			logger.error("error:", e);
		}
		
		return modelAndView;
	}
	
	/**
	 * 添加借贷申请单
	 * @return 添加状态
	 */
	@RequestMapping("normal/loanadmin!add.action")
	public ModelAndView add(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("simple_loan_order_add");
		
		String partyId=getParamValue(request,modelAndView,"partyId");
		if(null==partyId) return modelAndView;
		
		String term=getParamValue(request,modelAndView,"term");
		if(null==term) return modelAndView;
		
		String state=getParamValue(request,modelAndView,"state");
		if(null==state) return modelAndView;
		
		String quota=getParamValue(request,modelAndView,"quota");
		if(null==quota) return modelAndView;
		
		String symbol=getParamValue(request,modelAndView,"symbol");
		if(null==symbol) return modelAndView;
		
		String dailyRate=getParamValue(request,modelAndView,"dailyRate");
		if(null==dailyRate) return modelAndView;
		
		String repayment=getParamValue(request,modelAndView,"repayment");
		if(null==repayment) return modelAndView;
		
		String repayCycle=getParamValue(request,modelAndView,"repayCycle");
		if(null==repayCycle) return modelAndView;
		
		String lendingInstitution=getParamValue(request,modelAndView,"lendingInstitution");
		if(null==lendingInstitution) return modelAndView;
		
		/*String incomeImg=request.getParameter("incomeImg");
		if(null==incomeImg || (incomeImg=incomeImg.trim()).isEmpty()) {
			incomeImg=null;
		}*/
		
		Integer quotaInt=new Integer(quota);
		if(checkInvalidQuota(modelAndView,symbol,quotaInt)) return modelAndView;
		
		String img_idimg_1=getParamValue(request,modelAndView,"img_idimg_1");
		if(null==img_idimg_1) return modelAndView;
		String img_idimg_2=getParamValue(request,modelAndView,"img_idimg_2");
		if(null==img_idimg_2) return modelAndView;
		String img_idimg_3=getParamValue(request,modelAndView,"img_idimg_3");
		if(null==img_idimg_3) return modelAndView;
		String houseImgs=new StringBuilder(img_idimg_1).append(",").append(img_idimg_2).append(",").append(img_idimg_3).toString();
		
		SimpleLoanOrder simpleLoanOrder=new SimpleLoanOrder(partyId,new BigDecimal(quota),symbol);
		simpleLoanOrder.setLendingInstitution(new Integer(lendingInstitution));
		simpleLoanOrder.setRepayCycle(new Integer(repayCycle));
		simpleLoanOrder.setRepayment(new Integer(repayment));
		simpleLoanOrder.setDailyRate(new BigDecimal(dailyRate));
		simpleLoanOrder.setState(new Integer(state));
		simpleLoanOrder.setTerm(new Integer(term));
		simpleLoanOrder.setHouseImgs(houseImgs);
		
		try {
			boolean flag=loanService.addLoanOrder(simpleLoanOrder);
			if(flag) {
				modelAndView.addObject("message","借贷申请提交成功!");
			}else {
				modelAndView.addObject("code", "1");
				modelAndView.addObject("message","借贷申请提交失败!");
			}
		}catch(Throwable e) {
			modelAndView.addObject("code", "1");
			modelAndView.addObject("message","程序错误");
			logger.error("error:", e);
		}
		
		return modelAndView;
	}
	
	/**
	 * 返回借贷申请单修改页面
	 * @return 修改申请单页面
	 */
	@RequestMapping("normal/loanadmin!toModify.action")
	public ModelAndView modifyBefore(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("simple_loan_order_edit");
		
		String orderNo=getParamValue(request,modelAndView,"orderNo");
		if(null==orderNo) return modelAndView;
		
		try {
			HashMap<String,Object> paramMap=loanService.getLoanParams(null);
			if(null!=paramMap) {
				modelAndView.addObject("paramMap",paramMap);
				ApplicationUtil.getHttpSession().setAttribute("paramMap", paramMap);
			}else {
				modelAndView.addObject("code", "0");
				modelAndView.addObject("message", "获取借贷参数失败!");
				return modelAndView;
			}
			
			SimpleLoanOrder loanOrder=loanService.getLoanOrder(null,orderNo);
			
			PartyService partyService =ApplicationUtil.getBean(PartyService.class);
			
			Party party = partyService.cachePartyBy(loanOrder.getPartyId(), false);
			if(ObjectUtils.isNotEmpty(party)) {
				loanOrder.setUsername(party.getUsername());
			}		
			if(null!=loanOrder) {
				modelAndView.addObject("order",loanOrder);
			}
		}catch(Throwable e) {
			modelAndView.addObject("code", "1");
			modelAndView.addObject("message","程序错误");
			logger.error("error:", e);
		}
		
		return modelAndView;
	}
	
	/**
	 * 修改借贷申请单
	 * @return 修改申请单状态
	 */
	@RequestMapping("normal/loanadmin!modify.action")
	public ModelAndView modify(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/normal/loanadmin!list.action");
		
		String id=getParamValue(request,modelAndView,"orderNo");
		if(null==id) return modelAndView;
		
		String term=getParamValue(request,modelAndView,"term");
		if(null==term) return modelAndView;
		
		String quota=getParamValue(request,modelAndView,"quota");
		if(null==quota) return modelAndView;
		
		String symbol=getParamValue(request,modelAndView,"symbol");
		if(null==symbol) return modelAndView;
		
		String dailyRate=getParamValue(request,modelAndView,"dailyRate");
		if(null==dailyRate) return modelAndView;
		
		String repayment=getParamValue(request,modelAndView,"repayment");
		if(null==repayment) return modelAndView;
		
		String repayCycle=getParamValue(request,modelAndView,"repayCycle");
		if(null==repayCycle) return modelAndView;
		
		String createTime=getParamValue(request,modelAndView,"createTime");
		if(null==createTime) return modelAndView;
		
		//String lendingInstitution=getParamValue(request,modelAndView,"lendingInstitution");
		//if(null==lendingInstitution) return modelAndView;
		
		Integer quotaInt=new Integer(quota);
		if(checkInvalidQuota(modelAndView,symbol,quotaInt)) return modelAndView;
		
		String img_idimg_1=getParamValue(request,modelAndView,"img_idimg_1");
		//if(null==img_idimg_1) return modelAndView;
		String img_idimg_2=getParamValue(request,modelAndView,"img_idimg_2");
		//if(null==img_idimg_2) return modelAndView;
		String img_idimg_3=getParamValue(request,modelAndView,"img_idimg_3");
		//if(null==img_idimg_3) return modelAndView;
		
		SimpleLoanOrder simpleLoanOrder=new SimpleLoanOrder(new BigDecimal(quota),symbol,id);
		simpleLoanOrder.setRepayCycle(new Integer(repayCycle));
		simpleLoanOrder.setRepayment(new Integer(repayment));
		simpleLoanOrder.setDailyRate(new BigDecimal(dailyRate));
		simpleLoanOrder.setTerm(new Integer(term));
		if(StringUtils.isNotBlank(img_idimg_3)) {
			String houseImgs=new StringBuilder(img_idimg_1).append(",").append(img_idimg_2).append(",").append(img_idimg_3).toString();
			simpleLoanOrder.setHouseImgs(houseImgs);
		}
			
		try {
			simpleLoanOrder.setCreateTime(DATA_FORMATTER.parse(createTime));
		} catch (ParseException e) {
			modelAndView.addObject("code", "1");
			modelAndView.addObject("message",e.getMessage());
			return modelAndView;
		}
		
		try {
			boolean flag=loanService.modLoanOrder(simpleLoanOrder);
			if(flag) {
				modelAndView.addObject("message","借贷申请单修改成功!");
			}else {
				modelAndView.addObject("code", "1");
				modelAndView.addObject("message","借贷申请单修改失败!");
			}
		}catch(Throwable e) {
			modelAndView.addObject("code", "1");
			modelAndView.addObject("message","程序错误");
			logger.error("error:", e);
		}
		
		return modelAndView;
	}
	
	/**
	 * 删除借贷申请单
	 * @return 申请单列表
	 */
	@RequestMapping("normal/loanadmin!delete.action")
	public ModelAndView deleteLoanOrder(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		
		String orderId=getParamValue(request,modelAndView,"orderId");
		if(null==orderId) return modelAndView;
		
		try {
			loanService.deleteLoanOrder(orderId);
			modelAndView.addObject("message", "操作成功");
		}catch(BusinessException e) {
			modelAndView.addObject("message", e.getMessage());
			logger.error("delete loan order state error:", e);
		}catch(Throwable e) {
			modelAndView.addObject("message", "操作失败");
			logger.error("delete loan order occur error:", e);
		}
		
		modelAndView.setViewName("redirect:/normal/loanadmin!list.action");
		
		return modelAndView;
	}
	
	/**
	 * 获取参数值
	 * @param request 请求对象
	 * @param resultObject 结果对象
	 * @param paramName 参数名
	 * @return 参数值
	 */
	private static final String getParamValue(HttpServletRequest request,ModelAndView modelAndView,String paramName) {
		String paramValue=request.getParameter(paramName);
		if(null==paramValue || (paramValue=paramValue.trim()).isEmpty()) {
			modelAndView.addObject("code", "1");
			modelAndView.addObject("message",String.format("获取参数[%s]为空!",paramName));
			return null;
		}else {
			return paramValue;
		}
	}
	
	/**
	 * 校验借贷额度是否有效
	 * @param resultObject 结果对象
	 * @param quota 借贷额度
	 * @return 是否无效
	 */
	private static final boolean checkInvalidQuota(ModelAndView modelAndView,String symbol,Integer quota) {
		HashMap<String,Object> paramMap=(HashMap<String,Object>)ApplicationUtil.getHttpSession().getAttribute("paramMap");
		if(null==paramMap) {
			modelAndView.addObject("code", "1");
			modelAndView.addObject("message","从会话中获取借贷参数[paramMap]为空!");
			return true;
		}
		
		Matcher matcher=QUOTA_REGEX.matcher((String)paramMap.get("maxQuota"));
		Integer maxQuota=null;
		while(matcher.find()) {
			if(symbol.equalsIgnoreCase(matcher.group(1))) {
				maxQuota=new Integer(matcher.group(2));
				break;
			}
		}
		
		if(null==maxQuota) {
			modelAndView.addObject("code", "1");
			modelAndView.addObject("message","未找到额度最大值!");
			return true;
		}
		
		matcher=QUOTA_REGEX.matcher((String)paramMap.get("minQuota"));
		Integer minQuota=null;
		while(matcher.find()) {
			if(symbol.equalsIgnoreCase(matcher.group(1))) {
				minQuota=new Integer(matcher.group(2));
				break;
			}
		}
		
		if(null==minQuota) {
			modelAndView.addObject("code", "1");
			modelAndView.addObject("message","未找到额度最小值!");
			return true;
		}
		
		if(quota<minQuota || quota>maxQuota) {
			modelAndView.addObject("code", "1");
			modelAndView.addObject("message","借贷额度不在允许范围内!");
			return true;
		}
		
		return false;
	}
}
