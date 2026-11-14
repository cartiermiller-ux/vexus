package project.c2c.internal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.providers.encoding.PasswordEncoder;

import kernel.exception.BusinessException;
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.Constants;
import project.c2c.AdminC2cOrderService;
import project.c2c.C2cAdvert;
import project.c2c.C2cAdvertService;
import project.c2c.C2cOrder;
import project.c2c.C2cOrderService;
import project.c2c.C2cPaymentMethod;
import project.c2c.C2cPaymentMethodService;
import project.log.LogService;
import security.SecUser;
import security.internal.SecUserService;

public class AdminC2cOrderServiceImpl implements AdminC2cOrderService {

	private PagedQueryDao pagedDao;
	private C2cOrderService c2cOrderService;
	private C2cPaymentMethodService c2cPaymentMethodService;	
	private SecUserService secUserService;
	private PasswordEncoder passwordEncoder;
	private LogService logService;
	private C2cAdvertService c2cAdvertService;

	public Page pagedQuery(int pageNo, int pageSize, Integer status_int, String order_no_para, String user_code_para, String rolename_para, 
			String c2c_user_code_para, Integer c2c_user_type_int, String c2c_user_party_code_para, String direction_para, String loginPartyId) {

		StringBuffer queryString = new StringBuffer();
		
		queryString.append(" SELECT ");
		queryString.append(" c2c_order.UUID id, c2c_order.C2C_USER_ID c2c_user_id, c2c_order.C2C_ADVERT_ID c2c_advert_id, ");
		queryString.append(" c2c_order.PAYMENT_METHOD_ID payment_method_id, c2c_order.ORDER_TYPE order_type, c2c_order.ORDER_NO order_no, c2c_order.STATE state, c2c_order.C2C_USER_TYPE c2c_user_type, ");
		queryString.append(" c2c_order.C2C_USER_CODE c2c_user_code, c2c_order.C2C_USER_NICK_NAME c2c_user_nick_name, c2c_order.C2C_USER_PARTY_ID c2c_user_party_id, ");
		queryString.append(" c2c_order.C2C_USER_PARTY_CODE c2c_user_party_code, c2c_order.C2C_USER_PARTY_NAME c2c_user_party_name, c2c_order.DIRECTION direction, ");
		queryString.append(" c2c_order.CURRENCY currency, c2c_order.SYMBOL symbol, c2c_order.PAY_RATE pay_rate, c2c_order.SYMBOL_VALUE symbol_value, ");
		queryString.append(" c2c_order.COIN_AMOUNT coin_amount, c2c_order.EXPIRE_TIME expire_time, c2c_order.AMOUNT amount, c2c_order.METHOD_TYPE method_type, ");
		queryString.append(" c2c_order.METHOD_NAME method_name, c2c_order.METHOD_IMG method_img, c2c_order.REAL_NAME real_name, ");
		queryString.append(" c2c_order.PARAM_NAME1 param_name1, c2c_order.PARAM_VALUE1 param_value1, c2c_order.PARAM_NAME2 param_name2, c2c_order.PARAM_VALUE2 param_value2, ");
		queryString.append(" c2c_order.PARAM_NAME3 param_name3, c2c_order.PARAM_VALUE3 param_value3, c2c_order.PARAM_NAME4 param_name4, c2c_order.PARAM_VALUE4 param_value4, ");
		queryString.append(" c2c_order.PARAM_NAME5 param_name5, c2c_order.PARAM_VALUE5 param_value5, c2c_order.PARAM_NAME6 param_name6, c2c_order.PARAM_VALUE6 param_value6, ");
		queryString.append(" c2c_order.PARAM_NAME7 param_name7, c2c_order.PARAM_VALUE7 param_value7, c2c_order.PARAM_NAME8 param_name8, c2c_order.PARAM_VALUE8 param_value8, ");
		queryString.append(" c2c_order.PARAM_NAME9 param_name9, c2c_order.PARAM_VALUE9 param_value9, c2c_order.PARAM_NAME10 param_name10, c2c_order.PARAM_VALUE10 param_value10, ");
		queryString.append(" c2c_order.PARAM_NAME11 param_name11, c2c_order.PARAM_VALUE11 param_value11, c2c_order.PARAM_NAME12 param_name12, c2c_order.PARAM_VALUE12 param_value12, ");
		queryString.append(" c2c_order.PARAM_NAME13 param_name13, c2c_order.PARAM_VALUE13 param_value13, c2c_order.PARAM_NAME14 param_name14, c2c_order.PARAM_VALUE14 param_value14, ");
		queryString.append(" c2c_order.PARAM_NAME15 param_name15, c2c_order.PARAM_VALUE15 param_value15, ");
		queryString.append(" c2c_order.QRCODE qrcode, c2c_order.REMARK remark, c2c_order.CREATE_TIME create_time, c2c_order.HANDLE_TIME handle_time, ");
		queryString.append(" c2c_order.CLOSE_TIME close_time, c2c_order.PAY_TIME pay_time, c2c_order.CANCEL_TIME cancel_time, ");
		queryString.append(" party.UUID party_id, party.USERCODE usercode, party.USERNAME username, party.ROLENAME rolename ");

		queryString.append(" FROM T_C2C_ORDER c2c_order ");
		queryString.append(" LEFT JOIN PAT_PARTY party ON party.UUID = c2c_order.PARTY_ID ");
		queryString.append(" LEFT JOIN T_C2C_USER c2c_user ON c2c_user.UUID = c2c_order.C2C_USER_ID ");
		queryString.append(" WHERE 1=1 ");

		Map<String, Object> parameters = new HashMap<String, Object>();

		if (status_int != null) {
			queryString.append(" AND c2c_order.STATE = :status_int  ");
			parameters.put("status_int", status_int);
		}
		
		if (StringUtils.isNotEmpty(order_no_para)) {
			queryString.append(" AND c2c_order.ORDER_NO = :order_no_para ");
			parameters.put("order_no_para", order_no_para);
		}

		if (StringUtils.isNotEmpty(user_code_para)) {
			queryString.append(" AND (party.USERNAME like :user_code_para OR party.USERCODE like :user_code_para) ");
			parameters.put("user_code_para", "%" + user_code_para + "%");
		}
		
		if (StringUtils.isNotEmpty(rolename_para)) {
			queryString.append(" AND party.ROLENAME = :rolename_para ");
			parameters.put("rolename_para", rolename_para);
		}

		if (StringUtils.isNotEmpty(c2c_user_code_para)) {
			queryString.append(" AND (c2c_order.C2C_USER_NICK_NAME like :c2c_user_code_para OR c2c_order.C2C_USER_CODE like :c2c_user_code_para) ");
			parameters.put("c2c_user_code_para", "%" + c2c_user_code_para + "%");
		}

		if (c2c_user_type_int != null) {
			queryString.append(" and c2c_order.C2C_USER_TYPE = :c2c_user_type_int  ");
			parameters.put("c2c_user_type_int", c2c_user_type_int);
		}

		if (StringUtils.isNotEmpty(c2c_user_party_code_para)) {
			queryString.append(" AND (c2c_order.C2C_USER_PARTY_NAME like :c2c_user_party_code_para OR c2c_order.C2C_USER_PARTY_CODE like :c2c_user_party_code_para) ");
			parameters.put("c2c_user_party_code_para", "%" + c2c_user_party_code_para + "%");
		}
		
		if (StringUtils.isNotEmpty(direction_para)) {
			queryString.append(" AND c2c_order.DIRECTION = :direction_para ");
			parameters.put("direction_para", direction_para);
		}

		if (StringUtils.isNotEmpty(loginPartyId)) {
			queryString.append(" AND c2c_user.C2C_MANAGER_PARTY_ID = :loginPartyId ");
			parameters.put("loginPartyId", loginPartyId);
		}
		
		queryString.append(" order by FIELD(c2c_order.STATE, '0', '5','2','1') DESC, c2c_order.CREATE_TIME DESC ");

		Page page = this.pagedDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		
		for (Map<String, Object> data : (List<Map<String, Object>>) page.getElements()) {
			data.put("coin_amount", new BigDecimal(data.get("coin_amount").toString()).toPlainString());
		}
		
		return page;
	}
	
	public void savePass(C2cOrder c2cOrder, String safeword, String operator_username) {
		
		SecUser sec = this.secUserService.findUserByLoginName(operator_username);
		String sysSafeword = sec.getSafeword();

		String safeword_md5 = this.passwordEncoder.encodePassword(safeword, operator_username);
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("资金密码错误");
		}
		if ("3".equals(c2cOrder.getState())) {
			throw new BusinessException("订单已完成，无法放行");
		}
		if ("4".equals(c2cOrder.getState())) {
			throw new BusinessException("订单已取消，无法放行");
		}

		this.c2cOrderService.saveOrderPass(c2cOrder);
		
		SecUser order_user = this.secUserService.findUserByPartyId(c2cOrder.getPartyId());
		
		this.saveLog(order_user.getUsername(), operator_username, "订单放行", c2cOrder.getPartyId().toString());		
	}
	
	public void saveOrderPay(String order_no, String safeword, String operator_username, String payment_method_id_order_pay) {
		
		SecUser sec = this.secUserService.findUserByLoginName(operator_username);
		String sysSafeword = sec.getSafeword();
		
		String safeword_md5 = this.passwordEncoder.encodePassword(safeword, operator_username);
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("资金密码错误");
		}
		
		C2cOrder c2cOrder = this.c2cOrderService.get(order_no);
		if (null == c2cOrder) {
			throw new BusinessException("订单不存在");
		}
		if (!Arrays.asList("0", "2").contains(c2cOrder.getState())) {
			throw new BusinessException("订单不处于待支付或申诉中状态，无法转账");
		}
		
		C2cPaymentMethod method = this.c2cPaymentMethodService.get(payment_method_id_order_pay);
		
		// 更新最终的支付方式
		c2cOrder.setPaymentMethodId(method.getId().toString());
		c2cOrder.setMethodType(method.getMethodType());
		c2cOrder.setMethodName(method.getMethodName());
		c2cOrder.setMethodImg(method.getMethodImg());
		c2cOrder.setRealName(method.getRealName());
		c2cOrder.setParamName1(method.getParamName1());
		c2cOrder.setParamValue1(method.getParamValue1());
		c2cOrder.setParamName2(method.getParamName2());
		c2cOrder.setParamValue2(method.getParamValue2());
		c2cOrder.setParamName3(method.getParamName3());
		c2cOrder.setParamValue3(method.getParamValue3());
		c2cOrder.setParamName4(method.getParamName4());
		c2cOrder.setParamValue4(method.getParamValue4());
		c2cOrder.setParamName5(method.getParamName5());
		c2cOrder.setParamValue5(method.getParamValue5());
		c2cOrder.setParamName6(method.getParamName6());
		c2cOrder.setParamValue6(method.getParamValue6());
		c2cOrder.setParamName7(method.getParamName7());
		c2cOrder.setParamValue7(method.getParamValue7());
		c2cOrder.setParamName8(method.getParamName8());
		c2cOrder.setParamValue8(method.getParamValue8());
		c2cOrder.setParamName9(method.getParamName9());
		c2cOrder.setParamValue9(method.getParamValue9());
		c2cOrder.setParamName10(method.getParamName10());
		c2cOrder.setParamValue10(method.getParamValue10());
		c2cOrder.setParamName11(method.getParamName11());
		c2cOrder.setParamValue11(method.getParamValue11());
		c2cOrder.setParamName12(method.getParamName12());
		c2cOrder.setParamValue12(method.getParamValue12());
		c2cOrder.setParamName13(method.getParamName13());
		c2cOrder.setParamValue13(method.getParamValue13());
		c2cOrder.setParamName14(method.getParamName14());
		c2cOrder.setParamValue14(method.getParamValue14());
		c2cOrder.setParamName15(method.getParamName15());
		c2cOrder.setParamValue15(method.getParamValue15());
		c2cOrder.setQrcode(method.getQrcode());
		
		c2cOrder.setState("1");
		c2cOrder.setPayTime(new Date());
		this.c2cOrderService.update(c2cOrder);
		
		this.c2cOrderService.c2cSendMessageByState(c2cOrder, "1");
		
		SecUser orderUser = this.secUserService.findUserByPartyId(c2cOrder.getPartyId());
		
		this.saveLog(orderUser.getUsername(), operator_username, "订单转账完成", c2cOrder.getPartyId());
	}
	
	public List<C2cPaymentMethod> getOrderPayments(String order_no) {
		
		C2cOrder c2cOrder = this.c2cOrderService.get(order_no);
		if (null == c2cOrder) {
			throw new BusinessException("订单不存在");
		}
		
		C2cAdvert c2cAdvert = this.c2cAdvertService.get(c2cOrder.getC2cAdvertId());
		if (null == c2cAdvert) {
			throw new BusinessException("广告不存在");
		}
				
		String[] types = c2cAdvert.getPayType().split(",");
		List<String> typeList = Arrays.asList(types);
				
		List<C2cPaymentMethod> list = new ArrayList<C2cPaymentMethod>();
		Map<String, C2cPaymentMethod> methodMap = this.c2cPaymentMethodService.getByPartyId(c2cOrder.getPartyId());
		for (String key : methodMap.keySet()) {
			C2cPaymentMethod method = methodMap.get(key);
			if (null != method) {
				list.add(method);
			}
		}
		
		C2cPaymentMethod cpm = this.c2cPaymentMethodService.get(c2cOrder.getPaymentMethodId());
		
		List<C2cPaymentMethod> listRet = new ArrayList<C2cPaymentMethod>();
		// 先添加订单记录的支付方式
		for (C2cPaymentMethod pay : list) {			
			if (c2cOrder.getPaymentMethodId().equals(pay.getId().toString())) {
				listRet.add(pay);
			}
		}
		// 再添加与订单记录的支付方式类型相同的支付方式
		for (C2cPaymentMethod pay : list) {
			if (!c2cOrder.getPaymentMethodId().equals(pay.getId().toString())) {
				if (cpm.getMethodConfigId().equals(pay.getMethodConfigId())) {
					listRet.add(pay);
				}
			}
		}
		// 最后添加与广告匹配的支付方式
		for (C2cPaymentMethod pay : list) {			
			if (!c2cOrder.getPaymentMethodId().equals(pay.getId().toString()) && !cpm.getMethodConfigId().equals(pay.getMethodConfigId())) {
				if (typeList.contains(pay.getMethodConfigId())) {
					listRet.add(pay);
				}
			}
		}
		
		for (int i = 0; i < listRet.size(); i++) {
			C2cPaymentMethod method = listRet.get(i);
			String methodType = String.valueOf(method.getMethodType());
			Map<String, String> pmtMap = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");
			method.setMethodTypeName(pmtMap.containsKey(methodType) ? pmtMap.get(methodType) : methodType);
		}
		
		return listRet;
	}
	
	public Map<String, Object> detail(C2cOrder order) {	
		
		Map<String, String> pmtMap = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("id", order.getId());
		result.put("party_id", order.getPartyId());
		result.put("c2c_user_id", order.getC2cUserId());
		result.put("c2c_advert_id", order.getC2cAdvertId());
		result.put("payment_method_id", order.getPaymentMethodId());
		result.put("order_type", order.getOrderType());
		result.put("order_no", order.getOrderNo());
		result.put("state", order.getState());
		result.put("c2c_user_type", order.getC2cUserType());
		result.put("c2c_user_code", order.getC2cUserCode());
		result.put("c2c_user_nick_name", order.getC2cUserNickName());
		result.put("c2c_user_party_id", order.getC2cUserPartyId());
		result.put("c2c_user_party_code", order.getC2cUserPartyCode());
		result.put("c2c_user_party_name", order.getC2cUserPartyName());
		result.put("direction", order.getDirection());
		result.put("currency", order.getCurrency());
		result.put("symbol", order.getSymbol().toUpperCase());
		result.put("pay_rate", order.getPayRate());
		result.put("symbol_value", order.getSymbolValue());
		result.put("coin_amount", order.getCoinAmount());
		result.put("expire_time", order.getExpireTime());
		result.put("amount", order.getAmount());
		result.put("method_type", order.getMethodType());
		String methodType = String.valueOf(order.getMethodType());
		result.put("method_type_name", pmtMap.containsKey(methodType) ? pmtMap.get(methodType) : methodType);
		result.put("method_name", order.getMethodName());
		result.put("method_img", order.getMethodImg());
		result.put("real_name", order.getRealName());
		result.put("param_name1", order.getParamName1());
		result.put("param_value1", order.getParamValue1());
		result.put("param_name2", order.getParamName2());
		result.put("param_value2", order.getParamValue2());
		result.put("param_name3", order.getParamName3());
		result.put("param_value3", order.getParamValue3());
		result.put("param_name4", order.getParamName4());
		result.put("param_value4", order.getParamValue4());
		result.put("param_name5", order.getParamName5());
		result.put("param_value5", order.getParamValue5());
		result.put("param_name6", order.getParamName6());
		result.put("param_value6", order.getParamValue6());
		result.put("param_name7", order.getParamName7());
		result.put("param_value7", order.getParamValue7());
		result.put("param_name8", order.getParamName8());
		result.put("param_value8", order.getParamValue8());
		result.put("param_name9", order.getParamName9());
		result.put("param_value9", order.getParamValue9());
		result.put("param_name10", order.getParamName10());
		result.put("param_value10", order.getParamValue10());
		result.put("param_name11", order.getParamName11());
		result.put("param_value11", order.getParamValue11());
		result.put("param_name12", order.getParamName12());
		result.put("param_value12", order.getParamValue12());
		result.put("param_name13", order.getParamName13());
		result.put("param_value13", order.getParamValue13());
		result.put("param_name14", order.getParamName14());
		result.put("param_value14", order.getParamValue14());
		result.put("param_name15", order.getParamName15());
		result.put("param_value15", order.getParamValue15());
		result.put("qrcode", order.getQrcode());
		result.put("remark", order.getRemark());
		result.put("create_time", DateUtils.format(order.getCreateTime(), DateUtils.DF_yyyyMMddHHmmss));
		result.put("handle_time", DateUtils.format(order.getHandleTime(), DateUtils.DF_yyyyMMddHHmmss));
		result.put("close_time", DateUtils.format(order.getCloseTime(), DateUtils.DF_yyyyMMddHHmmss));
		result.put("pay_time", DateUtils.format(order.getPayTime(), DateUtils.DF_yyyyMMddHHmmss));
		result.put("cancel_time", DateUtils.format(order.getCancelTime(), DateUtils.DF_yyyyMMddHHmmss));		
		return result;
	}
	
	public void saveLog(String order_username, String operator, String context, String orderPartyId) {
		project.log.Log log = new project.log.Log();
		log.setCategory(Constants.LOG_CATEGORY_C2C);
		log.setUsername(order_username);
		log.setPartyId(orderPartyId);
		log.setOperator(operator);
		log.setLog(context);
		log.setCreateTime(new Date());
		logService.saveSync(log);
	}
		
//	public class PassDelayThread implements Runnable {
//		
//		private C2cOrderService c2cOrderService;
//		private C2cOrder order;
//
//		public void run() {
//			
//			try {
//				
//				while (true) {					
//					if (C2cOrderLock.add(order.getOrderNo())) {
//						this.c2cOrderService.saveOrderPass(order);
//						// 处理完退出
//						break;
//					}
//					ThreadUtils.sleep(200);
//				}
//
//			} catch (Throwable t) {
//				logger.error("error:", t);
//			} finally {
//				C2cOrderLock.remove(order.getOrderNo());
//			}
//
//		}
//
//		public PassDelayThread(C2cOrder order, C2cOrderService c2cOrderService) {
//			this.order = order;
//			this.c2cOrderService = c2cOrderService;
//		}
//		
//	}

	public void setPagedDao(PagedQueryDao pagedDao) {
		this.pagedDao = pagedDao;
	}

	public void setC2cOrderService(C2cOrderService c2cOrderService) {
		this.c2cOrderService = c2cOrderService;
	}

	public void setC2cPaymentMethodService(C2cPaymentMethodService c2cPaymentMethodService) {
		this.c2cPaymentMethodService = c2cPaymentMethodService;
	}

	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	public void setC2cAdvertService(C2cAdvertService c2cAdvertService) {
		this.c2cAdvertService = c2cAdvertService;
	}

}
