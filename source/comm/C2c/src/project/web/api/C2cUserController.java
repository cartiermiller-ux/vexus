package project.web.api;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
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
import kernel.util.Arith;
import kernel.util.StringUtils;
import kernel.web.BaseAction;
import kernel.web.ResultObject;
import project.Constants;
import project.c2c.C2cAdvert;
import project.c2c.C2cAdvertService;
import project.c2c.C2cPaymentMethodConfig;
import project.c2c.C2cPaymentMethodConfigService;
import project.c2c.C2cTranslate;
import project.c2c.C2cTranslateService;
import project.c2c.C2cUser;
import project.c2c.C2cUserParamBaseSet;
import project.c2c.C2cUserParamBaseSetService;
import project.c2c.C2cUserService;
import project.log.LogService;
import project.party.PartyService;
import project.party.model.Party;
import security.SecUser;
import security.internal.SecUserService;

/**
 * C2C承兑商
 */
@RestController
@CrossOrigin
public class C2cUserController extends BaseAction {

	private Logger logger = LoggerFactory.getLogger(C2cUserController.class);

	@Autowired
	private C2cUserService c2cUserService;
	@Autowired
	private C2cUserParamBaseSetService c2cUserParamBaseSetService;
	@Autowired
	private PartyService partyService;
	@Autowired
	private C2cAdvertService c2cAdvertService;
	@Autowired
	private LogService logService;
	@Autowired
	private C2cPaymentMethodConfigService c2cPaymentMethodConfigService;
	@Autowired
	private C2cTranslateService c2cTranslateService;
	
	private final String action = "/api/c2cUser!";
	
	/**
	 * 获取 承兑商 详情
	 */
	@RequestMapping(action + "get.action")
	public Object get(HttpServletRequest request) {
		String c2c_user_id = request.getParameter("c2c_user_id");
		String language = request.getParameter("language");
		
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			return resultObject;
		}
		
		try {
			
			Map<String, Object> result = new HashMap<String, Object>();
			
			C2cUser c2cUser = this.c2cUserService.get(c2c_user_id);
			if (null == c2cUser) {
				throw new BusinessException("承兑商不存在");				
			}
			
			C2cUserParamBaseSet paramBaseSet = this.c2cUserParamBaseSetService.getByPartyId(c2cUser.getC2cUserPartyId());
			if (null == paramBaseSet) {
				throw new BusinessException("承兑商参数基础设置不存在");				
			}
			
			if (StringUtils.isNotEmpty(c2cUser.getHeadImg())) {
				String path = Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + c2cUser.getHeadImg();
				c2cUser.setHeadImg(path);
			}
			
			Party party = this.partyService.cachePartyBy(c2cUser.getC2cUserPartyId(), false);
			if (null == party) {
				throw new BusinessException("承兑商的用户信息不存在");				
			}
			
			DecimalFormat df = new DecimalFormat("#.########");
			
			c2cUser.setThirtyDaysOrder(paramBaseSet.getThirtyDaysOrder() + c2cUser.getThirtyDaysOrder());
			c2cUser.setThirtyDaysOrderRatio(0 != paramBaseSet.getThirtyDaysOrderRatio() ? paramBaseSet.getThirtyDaysOrderRatio() : c2cUser.getThirtyDaysOrderRatio());
			c2cUser.setThirtyDaysPassAverageTime(0 != paramBaseSet.getThirtyDaysPassAverageTime() ? paramBaseSet.getThirtyDaysPassAverageTime() : c2cUser.getThirtyDaysPassAverageTime());
			c2cUser.setThirtyDaysPayAverageTime(0 != paramBaseSet.getThirtyDaysPayAverageTime() ? paramBaseSet.getThirtyDaysPayAverageTime() : c2cUser.getThirtyDaysPayAverageTime());
			c2cUser.setThirtyDaysAmount(Double.valueOf(df.format(Arith.add(paramBaseSet.getThirtyDaysAmount(), c2cUser.getThirtyDaysAmount()))).doubleValue());
			c2cUser.setBuyAmount(Double.valueOf(df.format(Arith.add(paramBaseSet.getBuyAmount(), c2cUser.getBuyAmount()))).doubleValue());
			c2cUser.setSellAmount(Double.valueOf(df.format(Arith.add(paramBaseSet.getSellAmount(), c2cUser.getSellAmount()))).doubleValue());
			c2cUser.setTotalAmount(Double.valueOf(df.format(Arith.add(paramBaseSet.getTotalAmount(), c2cUser.getTotalAmount()))).doubleValue());
			c2cUser.setAccountCreateDays(paramBaseSet.getAccountCreateDays() + c2cUser.getAccountCreateDays());
			c2cUser.setFirstExchangeDays(paramBaseSet.getFirstExchangeDays() + c2cUser.getFirstExchangeDays());
			c2cUser.setExchangeUsers(paramBaseSet.getExchangeUsers() + c2cUser.getExchangeUsers());
			c2cUser.setBuySuccessOrders(paramBaseSet.getBuySuccessOrders() + c2cUser.getBuySuccessOrders());
			c2cUser.setSellSuccessOrders(paramBaseSet.getSellSuccessOrders() + c2cUser.getSellSuccessOrders());
			c2cUser.setTotalSuccessOrders(paramBaseSet.getTotalSuccessOrders() + c2cUser.getTotalSuccessOrders());
			c2cUser.setAppraiseGood(paramBaseSet.getAppraiseGood() + c2cUser.getAppraiseGood());
			c2cUser.setAppraiseBad(paramBaseSet.getAppraiseBad() + c2cUser.getAppraiseBad());

			c2cUser.setOrderMailNoticeOpen(1 == paramBaseSet.getOrderMailNoticeOpen() || 1 == c2cUser.getOrderMailNoticeOpen() ? 1 : 0);
			c2cUser.setOrderSmsNoticeOpen(1 == paramBaseSet.getOrderSmsNoticeOpen() || 1 == c2cUser.getOrderSmsNoticeOpen() ? 1 : 0);
			c2cUser.setOrderAppNoticeOpen(1 == paramBaseSet.getOrderAppNoticeOpen() || 1 == c2cUser.getOrderAppNoticeOpen() ? 1 : 0);
			c2cUser.setAppealMailNoticeOpen(1 == paramBaseSet.getAppealMailNoticeOpen() || 1 == c2cUser.getAppealMailNoticeOpen() ? 1 : 0);
			c2cUser.setAppealSmsNoticeOpen(1 == paramBaseSet.getAppealSmsNoticeOpen() || 1 == c2cUser.getAppealSmsNoticeOpen() ? 1 : 0);
			c2cUser.setAppealAppNoticeOpen(1 == paramBaseSet.getAppealAppNoticeOpen() || 1 == c2cUser.getAppealAppNoticeOpen() ? 1 : 0);
			c2cUser.setChatAppNoticeOpen(1 == paramBaseSet.getChatAppNoticeOpen() || 1 == c2cUser.getChatAppNoticeOpen() ? 1 : 0);
			c2cUser.setSecurityMailNoticeOpen(1 == paramBaseSet.getSecurityMailNoticeOpen() || 1 == c2cUser.getSecurityMailNoticeOpen() ? 1 : 0);
			c2cUser.setSecuritySmsNoticeOpen(1 == paramBaseSet.getSecuritySmsNoticeOpen() || 1 == c2cUser.getSecuritySmsNoticeOpen() ? 1 : 0);
			c2cUser.setSecurityAppNoticeOpen(1 == paramBaseSet.getSecurityAppNoticeOpen() || 1 == c2cUser.getSecurityAppNoticeOpen() ? 1 : 0);
			
			Map<String, Object> partyMap = new HashMap<String, Object>();
			partyMap.put("phoneAuthority", "Y".equals(paramBaseSet.getPhoneAuthority()) || true == party.getPhone_authority() ? true : false);
			partyMap.put("emailAuthority", "Y".equals(paramBaseSet.getEmailAuthority()) || true == party.getEmail_authority() ? true : false);
			partyMap.put("kycAuthority", "Y".equals(paramBaseSet.getKycAuthority()) || true == party.getKyc_authority() ? true : false);
			partyMap.put("kycHighlevelAuthority", "Y".equals(paramBaseSet.getKycHighlevelAuthority()) || true == party.isKyc_highlevel_authority() ? true : false);
			
			List<C2cAdvert> adverts = this.c2cAdvertService.getByC2cUserId(c2c_user_id);			
			for (int i = 0; i < adverts.size(); i++) {
				C2cAdvert adv = adverts.get(i);
				if (null != adv) {
					
					if (StringUtils.isNotEmpty(adv.getPayType())) {							
						List<String> pay_type = new LinkedList<String>();							
						String[] types = adv.getPayType().split(",");
						for (String type : types) {								
							C2cPaymentMethodConfig method = this.c2cPaymentMethodConfigService.get(type.trim());
							if (null != method) {
								C2cTranslate trans = this.c2cTranslateService.get(method.getMethodName(), language);
								if (null != trans) {
									pay_type.add(trans.getTranslate());							
								} else {
									pay_type.add(method.getMethodName());		
								}					
							}
						}
						adv.setPayTypeName(String.join(",", pay_type));
					}
				}
			}
			
			result.put("c2c_user", c2cUser);
			result.put("party", partyMap);
			result.put("advert", adverts);
			
			resultObject.setData(result);
			
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
	
//	/**
//	 * 获取 C2C用户中心
//	 */
//	@RequestMapping(action + "getUserCenter.action")
//	public Object getUserCenter(HttpServletRequest request) {
//		String language = request.getParameter("language");
//		
//		ResultObject resultObject = new ResultObject();
//		resultObject = this.readSecurityContextFromSession(resultObject);
//		if (!"0".equals(resultObject.getCode())) {
//			return resultObject;
//		}
//		
//		try {
//			
//			Map<String, Object> result = new HashMap<String, Object>();
//			
//			String partyId = this.getLoginPartyId();
//			if (null == partyId) {
//				throw new BusinessException("请重新登录");
//			}
//			
//			C2cUser c2cUser = this.c2cUserService.getByPartyId(partyId);
//			if (null == c2cUser) {
//				throw new BusinessException("承兑商不存在");				
//			}
//			
//			C2cUserParamBaseSet paramBaseSet = this.c2cUserParamBaseSetService.getByPartyId(c2cUser.getC2cUserPartyId());
//			if (null == paramBaseSet) {
//				throw new BusinessException("承兑商参数基础设置不存在");				
//			}
//			
//			if (StringUtils.isNotEmpty(c2cUser.getHeadImg())) {
//				String path = Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + c2cUser.getHeadImg();
//				c2cUser.setHeadImg(path);
//			}
//			
//			Party party = this.partyService.cachePartyBy(c2cUser.getC2cUserPartyId(), false);
//			if (null == party) {
//				throw new BusinessException("承兑商的用户信息不存在");				
//			}
//			
//			DecimalFormat df = new DecimalFormat("#.########");
//			
//			c2cUser.setThirtyDaysOrder(paramBaseSet.getThirtyDaysOrder() + c2cUser.getThirtyDaysOrder());
//			c2cUser.setThirtyDaysOrderRatio(0 != paramBaseSet.getThirtyDaysOrderRatio() ? paramBaseSet.getThirtyDaysOrderRatio() : c2cUser.getThirtyDaysOrderRatio());
//			c2cUser.setThirtyDaysPassAverageTime(0 != paramBaseSet.getThirtyDaysPassAverageTime() ? paramBaseSet.getThirtyDaysPassAverageTime() : c2cUser.getThirtyDaysPassAverageTime());
//			c2cUser.setThirtyDaysPayAverageTime(0 != paramBaseSet.getThirtyDaysPayAverageTime() ? paramBaseSet.getThirtyDaysPayAverageTime() : c2cUser.getThirtyDaysPayAverageTime());
//			c2cUser.setThirtyDaysAmount(Double.valueOf(df.format(Arith.add(paramBaseSet.getThirtyDaysAmount(), c2cUser.getThirtyDaysAmount()))).doubleValue());
//			c2cUser.setBuyAmount(Double.valueOf(df.format(Arith.add(paramBaseSet.getBuyAmount(), c2cUser.getBuyAmount()))).doubleValue());
//			c2cUser.setSellAmount(Double.valueOf(df.format(Arith.add(paramBaseSet.getSellAmount(), c2cUser.getSellAmount()))).doubleValue());
//			c2cUser.setTotalAmount(Double.valueOf(df.format(Arith.add(paramBaseSet.getTotalAmount(), c2cUser.getTotalAmount()))).doubleValue());
//			c2cUser.setAccountCreateDays(paramBaseSet.getAccountCreateDays() + c2cUser.getAccountCreateDays());
//			c2cUser.setFirstExchangeDays(paramBaseSet.getFirstExchangeDays() + c2cUser.getFirstExchangeDays());
//			c2cUser.setExchangeUsers(paramBaseSet.getExchangeUsers() + c2cUser.getExchangeUsers());
//			c2cUser.setBuySuccessOrders(paramBaseSet.getBuySuccessOrders() + c2cUser.getBuySuccessOrders());
//			c2cUser.setSellSuccessOrders(paramBaseSet.getSellSuccessOrders() + c2cUser.getSellSuccessOrders());
//			c2cUser.setTotalSuccessOrders(paramBaseSet.getTotalSuccessOrders() + c2cUser.getTotalSuccessOrders());
//			c2cUser.setAppraiseGood(paramBaseSet.getAppraiseGood() + c2cUser.getAppraiseGood());
//			c2cUser.setAppraiseBad(paramBaseSet.getAppraiseBad() + c2cUser.getAppraiseBad());
//
//			c2cUser.setOrderMailNoticeOpen(1 == paramBaseSet.getOrderMailNoticeOpen() || 1 == c2cUser.getOrderMailNoticeOpen() ? 1 : 0);
//			c2cUser.setOrderSmsNoticeOpen(1 == paramBaseSet.getOrderSmsNoticeOpen() || 1 == c2cUser.getOrderSmsNoticeOpen() ? 1 : 0);
//			c2cUser.setOrderAppNoticeOpen(1 == paramBaseSet.getOrderAppNoticeOpen() || 1 == c2cUser.getOrderAppNoticeOpen() ? 1 : 0);
//			c2cUser.setAppealMailNoticeOpen(1 == paramBaseSet.getAppealMailNoticeOpen() || 1 == c2cUser.getAppealMailNoticeOpen() ? 1 : 0);
//			c2cUser.setAppealSmsNoticeOpen(1 == paramBaseSet.getAppealSmsNoticeOpen() || 1 == c2cUser.getAppealSmsNoticeOpen() ? 1 : 0);
//			c2cUser.setAppealAppNoticeOpen(1 == paramBaseSet.getAppealAppNoticeOpen() || 1 == c2cUser.getAppealAppNoticeOpen() ? 1 : 0);
//			c2cUser.setChatAppNoticeOpen(1 == paramBaseSet.getChatAppNoticeOpen() || 1 == c2cUser.getChatAppNoticeOpen() ? 1 : 0);
//			c2cUser.setSecurityMailNoticeOpen(1 == paramBaseSet.getSecurityMailNoticeOpen() || 1 == c2cUser.getSecurityMailNoticeOpen() ? 1 : 0);
//			c2cUser.setSecuritySmsNoticeOpen(1 == paramBaseSet.getSecuritySmsNoticeOpen() || 1 == c2cUser.getSecuritySmsNoticeOpen() ? 1 : 0);
//			c2cUser.setSecurityAppNoticeOpen(1 == paramBaseSet.getSecurityAppNoticeOpen() || 1 == c2cUser.getSecurityAppNoticeOpen() ? 1 : 0);
//			
//			Map<String, Object> partyMap = new HashMap<String, Object>();
//			partyMap.put("phoneAuthority", "Y".equals(paramBaseSet.getPhoneAuthority()) || true == party.getPhone_authority() ? true : false);
//			partyMap.put("emailAuthority", "Y".equals(paramBaseSet.getEmailAuthority()) || true == party.getEmail_authority() ? true : false);
//			partyMap.put("kycAuthority", "Y".equals(paramBaseSet.getKycAuthority()) || true == party.getKyc_authority() ? true : false);
//			partyMap.put("kycHighlevelAuthority", "Y".equals(paramBaseSet.getKycHighlevelAuthority()) || true == party.isKyc_highlevel_authority() ? true : false);
//						
//			List<C2cAdvert> adverts = this.c2cAdvertService.getByC2cUserId(c2cUser.getId().toString());
//			for (int i = 0; i < adverts.size(); i++) {
//				C2cAdvert adv = adverts.get(i);
//				if (null != adv) {
//					
//					if (StringUtils.isNotEmpty(adv.getPayType())) {							
//						List<String> pay_type = new LinkedList<String>();							
//						String[] types = adv.getPayType().split(",");
//						for (String type : types) {								
//							C2cPaymentMethodConfig method = this.c2cPaymentMethodConfigService.get(type.trim());
//							if (null != method) {
//								C2cTranslate trans = this.c2cTranslateService.get(method.getMethodName(), language);
//								if (null != trans) {
//									pay_type.add(trans.getTranslate());							
//								} else {
//									pay_type.add(method.getMethodName());		
//								}						
//							}
//						}
//						adv.setPayTypeName(String.join(",", pay_type));
//					}
//				}
//			}
//			
//			result.put("c2c_user", c2cUser);
//			result.put("party", partyMap);
//			result.put("advert", adverts);
//
//			resultObject.setData(result);
//
//		} catch (BusinessException e) {
//			resultObject.setCode("1");
//			resultObject.setMsg(e.getMessage());
//		} catch (Throwable t) {
//			resultObject.setCode("1");
//			resultObject.setMsg("程序错误");
//			logger.error("error:", t);
//		}
//
//		return resultObject;
//	}
	
//	/**
//	 * 设置 承兑商 信息
//	 */
//	@RequestMapping(action + "set.action")
//	public Object set(HttpServletRequest request) {
//		String order_mail_notice_open = request.getParameter("order_mail_notice_open");
//		String order_sms_notice_open = request.getParameter("order_sms_notice_open");
//		String order_app_notice_open = request.getParameter("order_app_notice_open");
//		String appeal_mail_notice_open = request.getParameter("appeal_mail_notice_open");
//		String appeal_sms_notice_open = request.getParameter("appeal_sms_notice_open");
//		String appeal_app_notice_open = request.getParameter("appeal_app_notice_open");
//		String chat_app_notice_open = request.getParameter("chat_app_notice_open");
//		String security_mail_notice_open = request.getParameter("security_mail_notice_open");
//		String security_sms_notice_open = request.getParameter("security_sms_notice_open");
//		String security_app_notice_open = request.getParameter("security_app_notice_open");
//		
//		ResultObject resultObject = new ResultObject();
//		resultObject = this.readSecurityContextFromSession(resultObject);
//		if (!"0".equals(resultObject.getCode())) {
//			return resultObject;
//		}
//		
//		try {
//			
//			String error = this.verifNotice(order_mail_notice_open, order_sms_notice_open, order_app_notice_open, appeal_mail_notice_open, appeal_sms_notice_open, 
//					appeal_app_notice_open, chat_app_notice_open, security_mail_notice_open, security_sms_notice_open, security_app_notice_open);
//			if (!StringUtils.isNullOrEmpty(error)) {
//				throw new BusinessException(error);
//			}
//			
//			String partyId = this.getLoginPartyId();
//			if (null == partyId) {
//				throw new BusinessException("请重新登录");
//			}
//			
//			C2cUser c2cUser = this.c2cUserService.getByPartyId(partyId);
//			if (null == c2cUser) {
//				throw new BusinessException("承兑商不存在");				
//			}
//			
//			String log = MessageFormat.format("ip:" + this.getIp() 
//				+ ",承兑商修改信息,id:{0},原承兑商类型:{1},原承兑商CODE:{2},原承兑商PARTY_ID:{3},原承兑商昵称:{4},"
//				+ "原订单邮件通知:{5},原订单短信通知:{6},原订单APP通知:{7},原申诉邮件通知:{8},原申诉短信通知:{9},原申诉APP通知:{10},原聊天APP通知:{11},"
//				+ "原安全邮件通知:{12},原安全短信通知:{13},原安全APP通知:{14},原备注:{15},原创建时间:{16},原更新时间:{17}", 
//				c2cUser.getId(), c2cUser.getC2cUserType(), c2cUser.getC2cUserCode(), c2cUser.getC2cUserPartyId(), c2cUser.getNickName(), 
//				c2cUser.getOrderMailNoticeOpen(), c2cUser.getOrderSmsNoticeOpen(), c2cUser.getOrderAppNoticeOpen(), c2cUser.getAppealMailNoticeOpen(), 
//				c2cUser.getAppealSmsNoticeOpen(), c2cUser.getAppealAppNoticeOpen(), c2cUser.getChatAppNoticeOpen(), c2cUser.getSecurityMailNoticeOpen(), 
//				c2cUser.getSecuritySmsNoticeOpen(), c2cUser.getSecurityAppNoticeOpen(), c2cUser.getRemark(), c2cUser.getCreateTime(), c2cUser.getUpdateTime());
//			
//			c2cUser.setOrderMailNoticeOpen(Integer.valueOf(order_mail_notice_open).intValue());
//			c2cUser.setOrderSmsNoticeOpen(Integer.valueOf(order_sms_notice_open).intValue());
//			c2cUser.setOrderAppNoticeOpen(Integer.valueOf(order_app_notice_open).intValue());
//			c2cUser.setAppealMailNoticeOpen(Integer.valueOf(appeal_mail_notice_open).intValue());
//			c2cUser.setAppealSmsNoticeOpen(Integer.valueOf(appeal_sms_notice_open).intValue());
//			c2cUser.setAppealAppNoticeOpen(Integer.valueOf(appeal_app_notice_open).intValue());
//			c2cUser.setChatAppNoticeOpen(Integer.valueOf(chat_app_notice_open).intValue());
//			c2cUser.setSecurityMailNoticeOpen(Integer.valueOf(security_mail_notice_open).intValue());
//			c2cUser.setSecuritySmsNoticeOpen(Integer.valueOf(security_sms_notice_open).intValue());
//			c2cUser.setSecurityAppNoticeOpen(Integer.valueOf(security_app_notice_open).intValue());
//			c2cUser.setUpdateTime(new Date());
//
//			this.c2cUserService.update(c2cUser);
//			
//			log += MessageFormat.format(",id:{0},新承兑商类型:{1},新承兑商CODE:{2},新承兑商PARTY_ID:{3},新承兑商昵称:{4},"
//				+ "新订单邮件通知:{5},新订单短信通知:{6},新订单APP通知:{7},新申诉邮件通知:{8},新申诉短信通知:{9},新申诉APP通知:{10},新聊天APP通知:{11},"
//				+ "新安全邮件通知:{12},新安全短信通知:{13},新安全APP通知:{14},新备注:{15},新创建时间:{16},新更新时间:{17}", 
//				c2cUser.getId(), c2cUser.getC2cUserType(), c2cUser.getC2cUserCode(), c2cUser.getC2cUserPartyId(), c2cUser.getNickName(), 
//				c2cUser.getOrderMailNoticeOpen(), c2cUser.getOrderSmsNoticeOpen(), c2cUser.getOrderAppNoticeOpen(), c2cUser.getAppealMailNoticeOpen(), 
//				c2cUser.getAppealSmsNoticeOpen(), c2cUser.getAppealAppNoticeOpen(), c2cUser.getChatAppNoticeOpen(), c2cUser.getSecurityMailNoticeOpen(), 
//				c2cUser.getSecuritySmsNoticeOpen(), c2cUser.getSecurityAppNoticeOpen(), c2cUser.getRemark(), c2cUser.getCreateTime(), c2cUser.getUpdateTime());
//
//			SecUser sec = this.secUserService.findUserByPartyId(partyId);
//			
//			this.saveLog(sec, sec.getUsername(), log, Constants.LOG_CATEGORY_C2C);
//
//		} catch (BusinessException e) {
//			resultObject.setCode("1");
//			resultObject.setMsg(e.getMessage());
//		} catch (Throwable t) {
//			resultObject.setCode("1");
//			resultObject.setMsg("程序错误");
//			logger.error("error:", t);
//		}
//
//		return resultObject;
//	}

	private String verifNotice(String order_mail_notice_open, String order_sms_notice_open, String order_app_notice_open, String appeal_mail_notice_open, String appeal_sms_notice_open, 
			String appeal_app_notice_open, String chat_app_notice_open, String security_mail_notice_open, String security_sms_notice_open, String security_app_notice_open) {
		if (StringUtils.isEmptyString(order_mail_notice_open) || !Arrays.asList("0", "1").contains(order_mail_notice_open)) {
			return "订单邮件通知未填或格式不正确";
		}
		if (StringUtils.isEmptyString(order_sms_notice_open) || !Arrays.asList("0", "1").contains(order_sms_notice_open)) {
			return "订单短信通知未填或格式不正确";
		}
		if (StringUtils.isEmptyString(order_app_notice_open) || !Arrays.asList("0", "1").contains(order_app_notice_open)) {
			return "订单APP通知未填或格式不正确";
		}
		if (StringUtils.isEmptyString(appeal_mail_notice_open) || !Arrays.asList("0", "1").contains(appeal_mail_notice_open)) {
			return "申诉邮件通知未填或格式不正确";
		}
		if (StringUtils.isEmptyString(appeal_sms_notice_open) || !Arrays.asList("0", "1").contains(appeal_sms_notice_open)) {
			return "申诉短信通知未填或格式不正确";
		}
		if (StringUtils.isEmptyString(appeal_app_notice_open) || !Arrays.asList("0", "1").contains(appeal_app_notice_open)) {
			return "申诉APP通知未填或格式不正确";
		}
		if (StringUtils.isEmptyString(chat_app_notice_open) || !Arrays.asList("0", "1").contains(chat_app_notice_open)) {
			return "聊天APP通知未填或格式不正确";
		}
		if (StringUtils.isEmptyString(security_mail_notice_open) || !Arrays.asList("0", "1").contains(security_mail_notice_open)) {
			return "安全邮件通知未填或格式不正确";
		}
		if (StringUtils.isEmptyString(security_sms_notice_open) || !Arrays.asList("0", "1").contains(security_sms_notice_open)) {
			return "安全短信通知未填或格式不正确";
		}
		if (StringUtils.isEmptyString(security_app_notice_open) || !Arrays.asList("0", "1").contains(security_app_notice_open)) {
			return "安全APP通知未填或格式不正确";
		}
		return null;
	}
	
	public void saveLog(SecUser secUser, String operator, String context, String category) {
		project.log.Log log = new project.log.Log();
		log.setCategory(category);
		log.setOperator(operator);
		log.setUsername(secUser.getUsername());
		log.setPartyId(secUser.getPartyId());
		log.setLog(context);
		log.setCreateTime(new Date());
		this.logService.saveSync(log);
	}
	
}
