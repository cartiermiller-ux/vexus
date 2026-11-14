package project.web.admin;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.providers.encoding.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.JsonUtils;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.Constants;
import project.c2c.AdminC2cUserService;
import project.c2c.C2cUser;
import project.c2c.C2cUserParamBaseSet;
import project.c2c.C2cUserParamBaseSetService;
import project.log.LogService;
import project.party.PartyService;
import project.party.model.Party;
import project.user.LocalUserService;
import security.Role;
import security.SecUser;
import security.internal.SecUserService;

/**
 * C2C承兑商管理
 */
@RestController
public class AdminC2cUserController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminC2cUserController.class);

	@Autowired
	private AdminC2cUserService adminC2cUserService;
	@Autowired
	private C2cUserParamBaseSetService c2cUserParamBaseSetService;
	@Autowired
	private LogService logService;
	@Autowired
	private PartyService partyService;
	@Autowired
	private LocalUserService localUserService;
	@Autowired
	private SecUserService secUserService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	private final String action = "normal/adminC2cUserAction!";

	/**
	 * 获取 承兑商 列表
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		long startTime=System.nanoTime();
		
		String pageNoStr = request.getParameter("pageNo");
		String error = request.getParameter("error");
		String message = request.getParameter("message");
		String c2c_user_id_para = request.getParameter("c2c_user_id_para");
		String c2c_user_type_para = request.getParameter("c2c_user_type_para");
		String c2c_user_party_id_para = request.getParameter("c2c_user_party_id_para");
		String c2c_manager_name_para = request.getParameter("c2c_manager_name_para");

		ModelAndView modelAndView = new ModelAndView();

		int pageNo=1;
		Page page=null;
		int pageSize=20;
		try {
			pageNo=checkAndSetPageNo(pageNoStr);
			Integer c2c_user_type_int = null;
			if (!StringUtils.isEmptyString(c2c_user_type_para)) {
				c2c_user_type_int = Integer.valueOf(c2c_user_type_para);
			}			
			
			String secUuid = "";
			String userNameLogin = this.getUsername_login();
			if (null != userNameLogin) {
				SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
				Set<Role> roles = sec.getRoles();
				Iterator<Role> it = roles.iterator();
				while (it.hasNext()) {
					Role role = (Role) it.next();
					if (role.getRoleName().equals("C2C")) {
						secUuid = sec.getId().toString();
						break;
					}
				}
			}
			
			logger.error("after secUserService.findUserByLoginName:"+(System.nanoTime()-startTime));
			page = this.adminC2cUserService.pagedQuery(pageNo, pageSize, c2c_user_id_para, c2c_user_type_int, 
					c2c_user_party_id_para, c2c_manager_name_para, secUuid);
			
			logger.error("after adminC2cUserService.pagedQuery:"+(System.nanoTime()-startTime));
		} catch (BusinessException e) {			
			modelAndView.addObject("error", e.getMessage());
			modelAndView.addObject("message", message);
			return modelAndView;			
		} catch (Throwable t) {			
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.addObject("message", message);
			return modelAndView;
		}
		
		modelAndView.addObject("pageNo", pageNo);
		modelAndView.addObject("pageSize", pageSize);
		modelAndView.addObject("page", page);
		modelAndView.addObject("message", message);
		modelAndView.addObject("error", error);
		modelAndView.addObject("c2c_user_id_para", c2c_user_id_para);
		modelAndView.addObject("c2c_user_type_para", c2c_user_type_para);
		modelAndView.addObject("c2c_user_party_id_para", c2c_user_party_id_para);
		modelAndView.addObject("c2c_manager_name_para", c2c_manager_name_para);
		modelAndView.setViewName("c2c_user_list");
		
		logger.error("after modelAndView:"+(System.nanoTime()-startTime));
		
		return modelAndView;
	}

	/**
	 * 新增 承兑商 页面
	 */
	@RequestMapping(action + "toAdd.action")
	public ModelAndView toAdd(HttpServletRequest request) {	
		ModelAndView modelAndView = new ModelAndView();	
		
		modelAndView.addObject("deposit", 0);
		modelAndView.addObject("deposit_open", 0);
		modelAndView.addObject("deposit_gift_rate", 0);
		
		modelAndView.addObject("thirty_days_order", 0);
		modelAndView.addObject("thirty_days_order_ratio", 0);
		modelAndView.addObject("thirty_days_pass_average_time", 0);
		modelAndView.addObject("thirty_days_pay_average_time", 0);
		modelAndView.addObject("thirty_days_amount", 0);
		modelAndView.addObject("buy_amount", 0);
		modelAndView.addObject("sell_amount", 0);
		modelAndView.addObject("total_amount", 0);
		modelAndView.addObject("account_create_days", 0);
		modelAndView.addObject("first_exchange_days", 0);
		modelAndView.addObject("exchange_users", 0);
		modelAndView.addObject("buy_success_orders", 0);
		modelAndView.addObject("sell_success_orders", 0);
		modelAndView.addObject("total_success_orders", 0);
		modelAndView.addObject("appraise_good", 0);
		modelAndView.addObject("appraise_bad", 0);
		
		modelAndView.addObject("phone_authority", "N");
		modelAndView.addObject("email_authority", "N");
		modelAndView.addObject("kyc_authority", "N");
		modelAndView.addObject("kyc_highlevel_authority", "N");
		
		modelAndView.addObject("thirty_days_order_base", 0);
		modelAndView.addObject("thirty_days_order_ratio_base", 0);
		modelAndView.addObject("thirty_days_pass_average_time_base", 0);
		modelAndView.addObject("thirty_days_pay_average_time_base", 0);
		modelAndView.addObject("thirty_days_amount_base", 0);
		modelAndView.addObject("buy_amount_base", 0);
		modelAndView.addObject("sell_amount_base", 0);
		modelAndView.addObject("total_amount_base", 0);
		modelAndView.addObject("account_create_days_base", 0);
		modelAndView.addObject("first_exchange_days_base", 0);
		modelAndView.addObject("exchange_users_base", 0);
		modelAndView.addObject("buy_success_orders_base", 0);
		modelAndView.addObject("sell_success_orders_base", 0);
		modelAndView.addObject("total_success_orders_base", 0);
		modelAndView.addObject("appraise_good_base", 0);
		modelAndView.addObject("appraise_bad_base", 0);
		
		modelAndView.addObject("phone_authority_base", "N");
		modelAndView.addObject("email_authority_base", "N");
		modelAndView.addObject("kyc_authority_base", "N");
		modelAndView.addObject("kyc_highlevel_authority_base", "N");

		modelAndView.addObject("c2cManagerMap", this.adminC2cUserService.getAllC2cManager());
		
		modelAndView.setViewName("c2c_user_add");
		return modelAndView;		
	}
	
	private String usingUUID() {
		UUID randomUUID = UUID.randomUUID();
		return randomUUID.toString().replaceAll("-", "");
	}
	
	/**
	 * 新增 承兑商
	 */
	@RequestMapping(action + "add.action")
	public ModelAndView add(HttpServletRequest request) {
		String c2c_manager_party_id = request.getParameter("c2c_manager_party_id");
		
		String type_front = request.getParameter("type_front");
		String username_front = request.getParameter("username_front");
		String password_front = request.getParameter("password_front");
		String re_password_front = request.getParameter("re_password_front");
		String usercode_front = request.getParameter("usercode_front");
		String c2c_user_type = request.getParameter("c2c_user_type");	
		String c2c_user_party_code = request.getParameter("c2c_user_party_code");		
		String nick_name = request.getParameter("nick_name");
		String head_img = request.getParameter("head_img");
		String deposit = request.getParameter("deposit");
		String deposit_open = request.getParameter("deposit_open");
		String deposit_gift_rate = request.getParameter("deposit_gift_rate");
		
		String thirty_days_order = request.getParameter("thirty_days_order");
		String thirty_days_order_ratio = request.getParameter("thirty_days_order_ratio");
		String thirty_days_pass_average_time = request.getParameter("thirty_days_pass_average_time");
		String thirty_days_pay_average_time = request.getParameter("thirty_days_pay_average_time");
		String thirty_days_amount = request.getParameter("thirty_days_amount");
		String buy_amount = request.getParameter("buy_amount");
		String sell_amount = request.getParameter("sell_amount");
		String total_amount = request.getParameter("total_amount");
		String account_create_days = request.getParameter("account_create_days");
		String first_exchange_days = request.getParameter("first_exchange_days");
		String exchange_users = request.getParameter("exchange_users");
		String buy_success_orders = request.getParameter("buy_success_orders");
		String sell_success_orders = request.getParameter("sell_success_orders");
		String total_success_orders = request.getParameter("total_success_orders");
		String appraise_good = request.getParameter("appraise_good");
		String appraise_bad = request.getParameter("appraise_bad");
		
		String order_mail_notice_open = request.getParameter("order_mail_notice_open");
		String order_sms_notice_open = request.getParameter("order_sms_notice_open");
		String order_app_notice_open = request.getParameter("order_app_notice_open");
		String appeal_mail_notice_open = request.getParameter("appeal_mail_notice_open");
		String appeal_sms_notice_open = request.getParameter("appeal_sms_notice_open");
		String appeal_app_notice_open = request.getParameter("appeal_app_notice_open");
		String chat_app_notice_open = request.getParameter("chat_app_notice_open");
		String security_mail_notice_open = request.getParameter("security_mail_notice_open");
		String security_sms_notice_open = request.getParameter("security_sms_notice_open");
		String security_app_notice_open = request.getParameter("security_app_notice_open");
		
		String phone_authority = request.getParameter("phone_authority");
		String email_authority = request.getParameter("email_authority");
		String kyc_authority = request.getParameter("kyc_authority");
		String kyc_highlevel_authority = request.getParameter("kyc_highlevel_authority");
		
		String thirty_days_order_base = request.getParameter("thirty_days_order_base");
		String thirty_days_order_ratio_base = request.getParameter("thirty_days_order_ratio_base");
		String thirty_days_pass_average_time_base = request.getParameter("thirty_days_pass_average_time_base");
		String thirty_days_pay_average_time_base = request.getParameter("thirty_days_pay_average_time_base");
		String thirty_days_amount_base = request.getParameter("thirty_days_amount_base");
		String buy_amount_base = request.getParameter("buy_amount_base");
		String sell_amount_base = request.getParameter("sell_amount_base");
		String total_amount_base = request.getParameter("total_amount_base");
		String account_create_days_base = request.getParameter("account_create_days_base");
		String first_exchange_days_base = request.getParameter("first_exchange_days_base");
		String exchange_users_base = request.getParameter("exchange_users_base");
		String buy_success_orders_base = request.getParameter("buy_success_orders_base");
		String sell_success_orders_base = request.getParameter("sell_success_orders_base");
		String total_success_orders_base = request.getParameter("total_success_orders_base");
		String appraise_good_base = request.getParameter("appraise_good_base");
		String appraise_bad_base = request.getParameter("appraise_bad_base");
		
		String order_mail_notice_open_base = request.getParameter("order_mail_notice_open_base");
		String order_sms_notice_open_base = request.getParameter("order_sms_notice_open_base");
		String order_app_notice_open_base = request.getParameter("order_app_notice_open_base");
		String appeal_mail_notice_open_base = request.getParameter("appeal_mail_notice_open_base");
		String appeal_sms_notice_open_base = request.getParameter("appeal_sms_notice_open_base");
		String appeal_app_notice_open_base = request.getParameter("appeal_app_notice_open_base");
		String chat_app_notice_open_base = request.getParameter("chat_app_notice_open_base");
		String security_mail_notice_open_base = request.getParameter("security_mail_notice_open_base");
		String security_sms_notice_open_base = request.getParameter("security_sms_notice_open_base");
		String security_app_notice_open_base = request.getParameter("security_app_notice_open_base");
		
		String phone_authority_base = request.getParameter("phone_authority_base");
		String email_authority_base = request.getParameter("email_authority_base");
		String kyc_authority_base = request.getParameter("kyc_authority_base");
		String kyc_highlevel_authority_base = request.getParameter("kyc_highlevel_authority_base");
		
		String remark = request.getParameter("remark");
		
		String login_safeword = request.getParameter("login_safeword");
				
		if (StringUtils.isEmptyString(deposit)) {deposit = "0";}
		if (StringUtils.isEmptyString(deposit_open)) {deposit_open = "0";}
		if (StringUtils.isEmptyString(deposit_gift_rate)) {deposit_gift_rate = "0";}
		
		if (StringUtils.isEmptyString(thirty_days_order)) {thirty_days_order = "0";}
		if (StringUtils.isEmptyString(thirty_days_order_ratio)) {thirty_days_order_ratio = "0";}
		if (StringUtils.isEmptyString(thirty_days_pass_average_time)) {thirty_days_pass_average_time = "0";}
		if (StringUtils.isEmptyString(thirty_days_pay_average_time)) {thirty_days_pay_average_time = "0";}
		if (StringUtils.isEmptyString(thirty_days_amount)) {thirty_days_amount = "0";}
		if (StringUtils.isEmptyString(buy_amount)) {buy_amount = "0";}
		if (StringUtils.isEmptyString(sell_amount)) {sell_amount = "0";}
		if (StringUtils.isEmptyString(total_amount)) {total_amount = "0";}
		if (StringUtils.isEmptyString(account_create_days)) {account_create_days = "0";}
		if (StringUtils.isEmptyString(first_exchange_days)) {first_exchange_days = "0";}
		if (StringUtils.isEmptyString(exchange_users)) {exchange_users = "0";}
		if (StringUtils.isEmptyString(buy_success_orders)) {buy_success_orders = "0";}
		if (StringUtils.isEmptyString(sell_success_orders)) {sell_success_orders = "0";}
		if (StringUtils.isEmptyString(total_success_orders)) {total_success_orders = "0";}
		if (StringUtils.isEmptyString(appraise_good)) {appraise_good = "0";}
		if (StringUtils.isEmptyString(appraise_bad)) {appraise_bad = "0";}

		if (StringUtils.isEmptyString(phone_authority)) {phone_authority = "N";}
		if (StringUtils.isEmptyString(email_authority)) {email_authority = "N";}
		if (StringUtils.isEmptyString(kyc_authority)) {kyc_authority = "N";}
		if (StringUtils.isEmptyString(kyc_highlevel_authority)) {kyc_highlevel_authority = "N";}
		
		if (StringUtils.isEmptyString(thirty_days_order_base)) {thirty_days_order_base = "0";}
		if (StringUtils.isEmptyString(thirty_days_order_ratio_base)) {thirty_days_order_ratio_base = "0";}
		if (StringUtils.isEmptyString(thirty_days_pass_average_time_base)) {thirty_days_pass_average_time_base = "0";}
		if (StringUtils.isEmptyString(thirty_days_pay_average_time_base)) {thirty_days_pay_average_time_base = "0";}
		if (StringUtils.isEmptyString(thirty_days_amount_base)) {thirty_days_amount_base = "0";}
		if (StringUtils.isEmptyString(buy_amount_base)) {buy_amount_base = "0";}
		if (StringUtils.isEmptyString(sell_amount_base)) {sell_amount_base = "0";}
		if (StringUtils.isEmptyString(total_amount_base)) {total_amount_base = "0";}
		if (StringUtils.isEmptyString(account_create_days_base)) {account_create_days_base = "0";}
		if (StringUtils.isEmptyString(first_exchange_days_base)) {first_exchange_days_base = "0";}
		if (StringUtils.isEmptyString(exchange_users_base)) {exchange_users_base = "0";}
		if (StringUtils.isEmptyString(buy_success_orders_base)) {buy_success_orders_base = "0";}
		if (StringUtils.isEmptyString(sell_success_orders_base)) {sell_success_orders_base = "0";}
		if (StringUtils.isEmptyString(total_success_orders_base)) {total_success_orders_base = "0";}
		if (StringUtils.isEmptyString(appraise_good_base)) {appraise_good_base = "0";}
		if (StringUtils.isEmptyString(appraise_bad_base)) {appraise_bad_base = "0";}

		if (StringUtils.isEmptyString(phone_authority_base)) {phone_authority_base = "N";}
		if (StringUtils.isEmptyString(email_authority_base)) {email_authority_base = "N";}
		if (StringUtils.isEmptyString(kyc_authority_base)) {kyc_authority_base = "N";}
		if (StringUtils.isEmptyString(kyc_highlevel_authority_base)) {kyc_highlevel_authority_base = "N";}

		if(StringUtils.isEmptyString(username_front)) {
			username_front = usingUUID().substring(0, 10);		
			password_front = "000000";
			re_password_front = "000000";
			c2c_user_type = "1";
			type_front = "1";
			order_mail_notice_open_base = "0";
			order_sms_notice_open_base = "0";
			order_app_notice_open_base = "0";
			appeal_mail_notice_open_base = "0";
			appeal_sms_notice_open_base = "0";
			appeal_app_notice_open_base = "0";
			chat_app_notice_open_base = "0";
			security_mail_notice_open_base = "0";
			security_sms_notice_open_base = "0";
			security_app_notice_open_base = "0";
		}	
		
		ModelAndView modelAndView = new ModelAndView();
		
		modelAndView.addObject("c2c_manager_party_id", c2c_manager_party_id);
		modelAndView.addObject("type_front", type_front);
		modelAndView.addObject("username_front", username_front);
		modelAndView.addObject("password_front", password_front);
		modelAndView.addObject("re_password_front", re_password_front);
		modelAndView.addObject("usercode_front", usercode_front);
		
		modelAndView.addObject("c2c_user_type", c2c_user_type);
		modelAndView.addObject("c2c_user_party_code", c2c_user_party_code);
		modelAndView.addObject("nick_name", nick_name);
		modelAndView.addObject("head_img", head_img);
		modelAndView.addObject("deposit", deposit);
		modelAndView.addObject("deposit_open", deposit_open);
		modelAndView.addObject("deposit_gift_rate", deposit_gift_rate);
		
		modelAndView.addObject("thirty_days_order", thirty_days_order);
		modelAndView.addObject("thirty_days_order_ratio", thirty_days_order_ratio);
		modelAndView.addObject("thirty_days_pass_average_time", thirty_days_pass_average_time);
		modelAndView.addObject("thirty_days_pay_average_time", thirty_days_pay_average_time);
		modelAndView.addObject("thirty_days_amount", thirty_days_amount);
		modelAndView.addObject("buy_amount", buy_amount);
		modelAndView.addObject("sell_amount", sell_amount);
		modelAndView.addObject("total_amount", total_amount);
		modelAndView.addObject("account_create_days", account_create_days);
		modelAndView.addObject("first_exchange_days", first_exchange_days);
		modelAndView.addObject("exchange_users", exchange_users);
		modelAndView.addObject("buy_success_orders", buy_success_orders);
		modelAndView.addObject("sell_success_orders", sell_success_orders);
		modelAndView.addObject("total_success_orders", total_success_orders);
		modelAndView.addObject("appraise_good", appraise_good);
		modelAndView.addObject("appraise_bad", appraise_bad);
		
		modelAndView.addObject("order_mail_notice_open", order_mail_notice_open);
		modelAndView.addObject("order_sms_notice_open", order_sms_notice_open);
		modelAndView.addObject("order_app_notice_open", order_app_notice_open);
		modelAndView.addObject("appeal_mail_notice_open", appeal_mail_notice_open);
		modelAndView.addObject("appeal_sms_notice_open", appeal_sms_notice_open);
		modelAndView.addObject("appeal_app_notice_open", appeal_app_notice_open);
		modelAndView.addObject("chat_app_notice_open", chat_app_notice_open);
		modelAndView.addObject("security_mail_notice_open", security_mail_notice_open);
		modelAndView.addObject("security_sms_notice_open", security_sms_notice_open);
		modelAndView.addObject("security_app_notice_open", security_app_notice_open);

		modelAndView.addObject("phone_authority", phone_authority);
		modelAndView.addObject("email_authority", email_authority);
		modelAndView.addObject("kyc_authority", kyc_authority);
		modelAndView.addObject("kyc_highlevel_authority", kyc_highlevel_authority);
		
		modelAndView.addObject("thirty_days_order_base", thirty_days_order_base);
		modelAndView.addObject("thirty_days_order_ratio_base", thirty_days_order_ratio_base);
		modelAndView.addObject("thirty_days_pass_average_time_base", thirty_days_pass_average_time_base);
		modelAndView.addObject("thirty_days_pay_average_time_base", thirty_days_pay_average_time_base);
		modelAndView.addObject("thirty_days_amount_base", thirty_days_amount_base);
		modelAndView.addObject("buy_amount_base", buy_amount_base);
		modelAndView.addObject("sell_amount_base", sell_amount_base);
		modelAndView.addObject("total_amount_base", total_amount_base);
		modelAndView.addObject("account_create_days_base", account_create_days_base);
		modelAndView.addObject("first_exchange_days_base", first_exchange_days_base);
		modelAndView.addObject("exchange_users_base", exchange_users_base);
		modelAndView.addObject("buy_success_orders_base", buy_success_orders_base);
		modelAndView.addObject("sell_success_orders_base", sell_success_orders_base);
		modelAndView.addObject("total_success_orders_base", total_success_orders_base);
		modelAndView.addObject("appraise_good_base", appraise_good_base);
		modelAndView.addObject("appraise_bad_base", appraise_bad_base);
		
		modelAndView.addObject("order_mail_notice_open_base", order_mail_notice_open_base);
		modelAndView.addObject("order_sms_notice_open_base", order_sms_notice_open_base);
		modelAndView.addObject("order_app_notice_open_base", order_app_notice_open_base);
		modelAndView.addObject("appeal_mail_notice_open_base", appeal_mail_notice_open_base);
		modelAndView.addObject("appeal_sms_notice_open_base", appeal_sms_notice_open_base);
		modelAndView.addObject("appeal_app_notice_open_base", appeal_app_notice_open_base);
		modelAndView.addObject("chat_app_notice_open_base", chat_app_notice_open_base);
		modelAndView.addObject("security_mail_notice_open_base", security_mail_notice_open_base);
		modelAndView.addObject("security_sms_notice_open_base", security_sms_notice_open_base);
		modelAndView.addObject("security_app_notice_open_base", security_app_notice_open_base);

		modelAndView.addObject("phone_authority_base", phone_authority_base);
		modelAndView.addObject("email_authority_base", email_authority_base);
		modelAndView.addObject("kyc_authority_base", kyc_authority_base);
		modelAndView.addObject("kyc_highlevel_authority_base", kyc_highlevel_authority_base);
		
		modelAndView.addObject("remark", remark);
		
		try {

			modelAndView.addObject("c2cManagerMap", this.adminC2cUserService.getAllC2cManager());
			
			String error = this.verifBase(c2c_user_type, c2c_user_party_code, nick_name, head_img, deposit, deposit_open, deposit_gift_rate);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			error = this.verifFront(type_front, username_front, password_front, re_password_front, usercode_front);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			error = this.verifStat(" 基础设置 ", thirty_days_order_base, thirty_days_order_ratio_base, thirty_days_pass_average_time_base, thirty_days_pay_average_time_base, 
					thirty_days_amount_base, buy_amount_base, sell_amount_base, total_amount_base, account_create_days_base, first_exchange_days_base);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			error = this.verifNotice(" 基础设置 ", order_mail_notice_open_base, order_sms_notice_open_base, order_app_notice_open_base, appeal_mail_notice_open_base, appeal_sms_notice_open_base, 
					appeal_app_notice_open_base, chat_app_notice_open_base, security_mail_notice_open_base, security_sms_notice_open_base, security_app_notice_open_base);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			error = this.verifVerif(" 基础设置 ", phone_authority_base, email_authority_base, kyc_authority_base, kyc_highlevel_authority_base);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			error = this.verifOthers(" 基础设置 ", exchange_users_base, buy_success_orders_base, sell_success_orders_base, total_success_orders_base, appraise_good_base, appraise_bad_base);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			String userNameLogin = this.getUsername_login();
			
			SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
			this.checkLoginSafeword(sec, userNameLogin, login_safeword);
				
			Party party = null;
			if ("2".equals(c2c_user_type)) {
				// 用户承兑商
				party = this.partyService.findPartyByUsercode(c2c_user_party_code);
				if (null == party) {
					throw new BusinessException("用户承兑商：用户UID不存在");
				}
			} else if ("1".equals(c2c_user_type)) {
				// 后台承兑商
				party = this.localUserService.registerC2cUser(username_front, password_front, re_password_front, type_front, usercode_front, this.getIp());	
				if (null == party) {
					throw new BusinessException("后台承兑商：注册失败");
				}
				this.saveLog(sec, userNameLogin, "管理员操作：后台承兑商用户注册,ip[" + this.getIp() + "]", Constants.LOG_CATEGORY_SECURITY);
			} else {
				throw new BusinessException("承兑商类型不正确");
			}

			party.setC2cUserType(Integer.valueOf(c2c_user_type).intValue());
			this.partyService.update(party);
			
			C2cUser c2cUser = new C2cUser();
			c2cUser.setId(ApplicationUtil.getCurrentTimeUUID());
			c2cUser.setC2cManagerPartyId(c2c_manager_party_id);
			c2cUser.setC2cUserType(Integer.valueOf(c2c_user_type).intValue());
//				c2cUser.setC2cUserCode();
			c2cUser.setC2cUserPartyId(party.getId().toString());
			c2cUser.setNickName(nick_name);
			c2cUser.setHeadImg(head_img);
			c2cUser.setDeposit(0);
			c2cUser.setDepositOpen(0);
			c2cUser.setDepositGiftRate(Double.valueOf(deposit_gift_rate).doubleValue());
			
			c2cUser.setThirtyDaysOrder(0);
			c2cUser.setThirtyDaysOrderRatio(0);
			c2cUser.setThirtyDaysPassAverageTime(0);
			c2cUser.setThirtyDaysPayAverageTime(0);
			c2cUser.setThirtyDaysAmount(0);
			c2cUser.setBuyAmount(0);
			c2cUser.setSellAmount(0);
			c2cUser.setTotalAmount(0);
			c2cUser.setAccountCreateDays(0);
			c2cUser.setFirstExchangeDays(0);
			c2cUser.setExchangeUsers(0);
			c2cUser.setBuySuccessOrders(0);
			c2cUser.setSellSuccessOrders(0);
			c2cUser.setTotalSuccessOrders(0);
			c2cUser.setAppraiseGood(0);
			c2cUser.setAppraiseBad(0);
			c2cUser.setOrderMailNoticeOpen(0);
			c2cUser.setOrderSmsNoticeOpen(0);
			c2cUser.setOrderAppNoticeOpen(0);
			c2cUser.setAppealMailNoticeOpen(0);
			c2cUser.setAppealSmsNoticeOpen(0);
			c2cUser.setAppealAppNoticeOpen(0);
			c2cUser.setChatAppNoticeOpen(0);
			c2cUser.setSecurityMailNoticeOpen(0);
			c2cUser.setSecuritySmsNoticeOpen(0);
			c2cUser.setSecurityAppNoticeOpen(0);	
							
			c2cUser.setRemark(remark);
			c2cUser.setCreateTime(new Date());
			c2cUser.setUpdateTime(new Date());

			this.adminC2cUserService.save(c2cUser);
			
			String log = MessageFormat.format("ip:" + this.getIp() 
				+ ",管理员新增承兑商,id:{0},承兑商类型:{1},承兑商CODE:{2},承兑商PARTY_ID:{3},承兑商昵称:{4},承兑商头像:{5},剩余保证金:{6},保证金:{7},"
				+ "备注:{8},创建时间:{9},更新时间:{10},C2C管理员PARTY_ID:{11},保证金赠送比率:{12}", 
				c2cUser.getId(), c2cUser.getC2cUserType(), c2cUser.getC2cUserCode(), c2cUser.getC2cUserPartyId(), c2cUser.getNickName(), c2cUser.getHeadImg(), c2cUser.getDeposit(), c2cUser.getDepositOpen(), 
				c2cUser.getRemark(), c2cUser.getCreateTime(), c2cUser.getUpdateTime(), c2cUser.getC2cManagerPartyId(), c2cUser.getDepositGiftRate());
			
			this.saveLog(sec, userNameLogin, log, Constants.LOG_CATEGORY_C2C);

			C2cUserParamBaseSet paramBaseSet = new C2cUserParamBaseSet();
			paramBaseSet.setId(ApplicationUtil.getCurrentTimeUUID());
			paramBaseSet.setC2cUserPartyId(party.getId().toString());
			paramBaseSet.setThirtyDaysOrder(Integer.valueOf(thirty_days_order_base).intValue());
			paramBaseSet.setThirtyDaysOrderRatio(Double.valueOf(thirty_days_order_ratio_base).doubleValue());
			paramBaseSet.setThirtyDaysPassAverageTime(Integer.valueOf(thirty_days_pass_average_time_base).intValue());
			paramBaseSet.setThirtyDaysPayAverageTime(Integer.valueOf(thirty_days_pay_average_time_base).intValue());
			paramBaseSet.setThirtyDaysAmount(Double.valueOf(thirty_days_amount_base).doubleValue());
			paramBaseSet.setBuyAmount(Double.valueOf(buy_amount_base).doubleValue());
			paramBaseSet.setSellAmount(Double.valueOf(sell_amount_base).doubleValue());
			paramBaseSet.setTotalAmount(Double.valueOf(total_amount_base).doubleValue());
			paramBaseSet.setAccountCreateDays(Integer.valueOf(account_create_days_base).intValue());
			paramBaseSet.setFirstExchangeDays(Integer.valueOf(first_exchange_days_base).intValue());
			paramBaseSet.setExchangeUsers(Integer.valueOf(exchange_users_base).intValue());
			paramBaseSet.setBuySuccessOrders(Integer.valueOf(buy_success_orders_base).intValue());
			paramBaseSet.setSellSuccessOrders(Integer.valueOf(sell_success_orders_base).intValue());
			paramBaseSet.setTotalSuccessOrders(Integer.valueOf(total_success_orders_base).intValue());
			paramBaseSet.setAppraiseGood(Integer.valueOf(appraise_good_base).intValue());
			paramBaseSet.setAppraiseBad(Integer.valueOf(appraise_bad_base).intValue());
			paramBaseSet.setOrderMailNoticeOpen(Integer.valueOf(order_mail_notice_open_base).intValue());
			paramBaseSet.setOrderSmsNoticeOpen(Integer.valueOf(order_sms_notice_open_base).intValue());
			paramBaseSet.setOrderAppNoticeOpen(Integer.valueOf(order_app_notice_open_base).intValue());
			paramBaseSet.setAppealMailNoticeOpen(Integer.valueOf(appeal_mail_notice_open_base).intValue());
			paramBaseSet.setAppealSmsNoticeOpen(Integer.valueOf(appeal_sms_notice_open_base).intValue());
			paramBaseSet.setAppealAppNoticeOpen(Integer.valueOf(appeal_app_notice_open_base).intValue());
			paramBaseSet.setChatAppNoticeOpen(Integer.valueOf(chat_app_notice_open_base).intValue());
			paramBaseSet.setSecurityMailNoticeOpen(Integer.valueOf(security_mail_notice_open_base).intValue());
			paramBaseSet.setSecuritySmsNoticeOpen(Integer.valueOf(security_sms_notice_open_base).intValue());
			paramBaseSet.setSecurityAppNoticeOpen(Integer.valueOf(security_app_notice_open_base).intValue());
			paramBaseSet.setPhoneAuthority(phone_authority_base);
			paramBaseSet.setEmailAuthority(email_authority_base);
			paramBaseSet.setKycAuthority(kyc_authority_base);
			paramBaseSet.setKycHighlevelAuthority(kyc_highlevel_authority_base);
			
			paramBaseSet.setCreateTime(new Date());
			paramBaseSet.setUpdateTime(new Date());

			this.c2cUserParamBaseSetService.save(paramBaseSet);
			
			String logBase = MessageFormat.format("ip:" + this.getIp() 
				+ ",管理员新增承兑商参数基础设置,id:{0},承兑商PARTY_ID:{1},"
				+ "30日成单数:{2},30日成单率:{3},30日平均放行时间:{4},30日平均付款时间:{5},30日交易量:{6},买交易量:{7},卖交易量:{8},总交易量:{9},"
				+ "账号创建天数:{10},首次交易至今天数:{11},交易人数:{12},买成单数:{13},卖成单数:{14},总成单数:{15},"
				+ "好评数:{16},差评数:{17},订单邮件通知:{18},订单短信通知:{19},订单APP通知:{20},申诉邮件通知:{21},"
				+ "申诉短信通知:{22},申诉APP通知:{23},聊天APP通知:{24},安全邮件通知:{25},安全短信通知:{26},安全APP通知:{27},"
				+ "手机验证:{28},邮箱验证:{29},身份认证:{30},高级认证:{31},创建时间:{32},更新时间:{33}", 
				paramBaseSet.getId(), paramBaseSet.getC2cUserPartyId(), 
				paramBaseSet.getThirtyDaysOrder(), paramBaseSet.getThirtyDaysOrderRatio(), paramBaseSet.getThirtyDaysPassAverageTime(), paramBaseSet.getThirtyDaysPayAverageTime(), paramBaseSet.getThirtyDaysAmount(), paramBaseSet.getBuyAmount(), paramBaseSet.getSellAmount(), paramBaseSet.getTotalAmount(), 
				paramBaseSet.getAccountCreateDays(), paramBaseSet.getFirstExchangeDays(), paramBaseSet.getExchangeUsers(), paramBaseSet.getBuySuccessOrders(), paramBaseSet.getSellSuccessOrders(), paramBaseSet.getTotalSuccessOrders(), 
				paramBaseSet.getAppraiseGood(), paramBaseSet.getAppraiseBad(), paramBaseSet.getOrderMailNoticeOpen(), paramBaseSet.getOrderSmsNoticeOpen(), paramBaseSet.getOrderAppNoticeOpen(), paramBaseSet.getAppealMailNoticeOpen(), 
				paramBaseSet.getAppealSmsNoticeOpen(), paramBaseSet.getAppealAppNoticeOpen(), paramBaseSet.getChatAppNoticeOpen(), paramBaseSet.getSecurityMailNoticeOpen(), paramBaseSet.getSecuritySmsNoticeOpen(), paramBaseSet.getSecurityAppNoticeOpen(), 
				paramBaseSet.getPhoneAuthority(), paramBaseSet.getEmailAuthority(), paramBaseSet.getKycAuthority(), paramBaseSet.getKycHighlevelAuthority(), paramBaseSet.getCreateTime(), paramBaseSet.getUpdateTime());
			
			this.saveLog(sec, userNameLogin, logBase, Constants.LOG_CATEGORY_C2C);
			
		} catch (BusinessException e) {	
			modelAndView.addObject("error", e.getMessage());
			modelAndView.setViewName("c2c_user_add");
			return modelAndView;			
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.setViewName("c2c_user_add");
			return modelAndView;			
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	/**
	 * 修改 承兑商 页面
	 */
	@RequestMapping(action + "toUpdate.action")
	public ModelAndView toUpdate(HttpServletRequest request) {		
		String id = request.getParameter("id");
		ModelAndView modelAndView = new ModelAndView();

		try {
			C2cUser c2cUser = this.adminC2cUserService.findById(id);
			if (null == c2cUser) {
				throw new BusinessException("承兑商不存在");				
			}
			
			C2cUserParamBaseSet paramBaseSet = this.c2cUserParamBaseSetService.getByPartyId(c2cUser.getC2cUserPartyId());
			if (null == paramBaseSet) {
				throw new BusinessException("承兑商参数基础设置不存在");				
			}
			
			Party party = this.partyService.cachePartyBy(c2cUser.getC2cUserPartyId(), false);
			if (null == party) {
				throw new BusinessException("承兑商的用户信息不存在");				
			}
			
			modelAndView.addObject("id", id);	
			modelAndView.addObject("c2c_manager_party_id", c2cUser.getC2cManagerPartyId());
			
			modelAndView.addObject("c2c_user_type", c2cUser.getC2cUserType());
			modelAndView.addObject("c2c_user_type_name", 1 == c2cUser.getC2cUserType() ? "后台承兑商" : "用户承兑商");
			modelAndView.addObject("c2c_user_code", c2cUser.getC2cUserCode());
			modelAndView.addObject("nick_name", c2cUser.getNickName());
			modelAndView.addObject("head_img", c2cUser.getHeadImg());
			modelAndView.addObject("user_code", party.getUsercode());
			modelAndView.addObject("user_name", party.getUsername());
			modelAndView.addObject("deposit", c2cUser.getDeposit());
			modelAndView.addObject("deposit_open", c2cUser.getDepositOpen());
			modelAndView.addObject("deposit_gift_rate", c2cUser.getDepositGiftRate());
			
			modelAndView.addObject("thirty_days_order", c2cUser.getThirtyDaysOrder());
			modelAndView.addObject("thirty_days_order_ratio", c2cUser.getThirtyDaysOrderRatio());
			modelAndView.addObject("thirty_days_pass_average_time", c2cUser.getThirtyDaysPassAverageTime());
			modelAndView.addObject("thirty_days_pay_average_time", c2cUser.getThirtyDaysPayAverageTime());
			modelAndView.addObject("thirty_days_amount", c2cUser.getThirtyDaysAmount());
			modelAndView.addObject("buy_amount", c2cUser.getBuyAmount());
			modelAndView.addObject("sell_amount", c2cUser.getSellAmount());
			modelAndView.addObject("total_amount", c2cUser.getTotalAmount());
			modelAndView.addObject("account_create_days", c2cUser.getAccountCreateDays());
			modelAndView.addObject("first_exchange_days", c2cUser.getFirstExchangeDays());
			modelAndView.addObject("exchange_users", c2cUser.getExchangeUsers());
			modelAndView.addObject("buy_success_orders", c2cUser.getBuySuccessOrders());
			modelAndView.addObject("sell_success_orders", c2cUser.getSellSuccessOrders());
			modelAndView.addObject("total_success_orders", c2cUser.getTotalSuccessOrders());
			modelAndView.addObject("appraise_good", c2cUser.getAppraiseGood());
			modelAndView.addObject("appraise_bad", c2cUser.getAppraiseBad());
			
			modelAndView.addObject("order_mail_notice_open", c2cUser.getOrderMailNoticeOpen());
			modelAndView.addObject("order_sms_notice_open", c2cUser.getOrderSmsNoticeOpen());
			modelAndView.addObject("order_app_notice_open", c2cUser.getOrderAppNoticeOpen());
			modelAndView.addObject("appeal_mail_notice_open", c2cUser.getAppealMailNoticeOpen());
			modelAndView.addObject("appeal_sms_notice_open", c2cUser.getAppealSmsNoticeOpen());
			modelAndView.addObject("appeal_app_notice_open", c2cUser.getAppealAppNoticeOpen());
			modelAndView.addObject("chat_app_notice_open", c2cUser.getChatAppNoticeOpen());
			modelAndView.addObject("security_mail_notice_open", c2cUser.getSecurityMailNoticeOpen());
			modelAndView.addObject("security_sms_notice_open", c2cUser.getSecuritySmsNoticeOpen());
			modelAndView.addObject("security_app_notice_open", c2cUser.getSecurityAppNoticeOpen());
			
			modelAndView.addObject("phone_authority", party.getPhone_authority());
			modelAndView.addObject("email_authority", party.getEmail_authority());
			modelAndView.addObject("kyc_authority", party.getKyc_authority());
			modelAndView.addObject("kyc_highlevel_authority", party.isKyc_highlevel_authority());
						
			modelAndView.addObject("thirty_days_order_base", paramBaseSet.getThirtyDaysOrder());
			modelAndView.addObject("thirty_days_order_ratio_base", paramBaseSet.getThirtyDaysOrderRatio());
			modelAndView.addObject("thirty_days_pass_average_time_base", paramBaseSet.getThirtyDaysPassAverageTime());
			modelAndView.addObject("thirty_days_pay_average_time_base", paramBaseSet.getThirtyDaysPayAverageTime());
			modelAndView.addObject("thirty_days_amount_base", paramBaseSet.getThirtyDaysAmount());
			modelAndView.addObject("buy_amount_base", paramBaseSet.getBuyAmount());
			modelAndView.addObject("sell_amount_base", paramBaseSet.getSellAmount());
			modelAndView.addObject("total_amount_base", paramBaseSet.getTotalAmount());
			modelAndView.addObject("account_create_days_base", paramBaseSet.getAccountCreateDays());
			modelAndView.addObject("first_exchange_days_base", paramBaseSet.getFirstExchangeDays());
			modelAndView.addObject("exchange_users_base", paramBaseSet.getExchangeUsers());
			modelAndView.addObject("buy_success_orders_base", paramBaseSet.getBuySuccessOrders());
			modelAndView.addObject("sell_success_orders_base", paramBaseSet.getSellSuccessOrders());
			modelAndView.addObject("total_success_orders_base", paramBaseSet.getTotalSuccessOrders());
			modelAndView.addObject("appraise_good_base", paramBaseSet.getAppraiseGood());
			modelAndView.addObject("appraise_bad_base", paramBaseSet.getAppraiseBad());
			
			modelAndView.addObject("order_mail_notice_open_base", paramBaseSet.getOrderMailNoticeOpen());
			modelAndView.addObject("order_sms_notice_open_base", paramBaseSet.getOrderSmsNoticeOpen());
			modelAndView.addObject("order_app_notice_open_base", paramBaseSet.getOrderAppNoticeOpen());
			modelAndView.addObject("appeal_mail_notice_open_base", paramBaseSet.getAppealMailNoticeOpen());
			modelAndView.addObject("appeal_sms_notice_open_base", paramBaseSet.getAppealSmsNoticeOpen());
			modelAndView.addObject("appeal_app_notice_open_base", paramBaseSet.getAppealAppNoticeOpen());
			modelAndView.addObject("chat_app_notice_open_base", paramBaseSet.getChatAppNoticeOpen());
			modelAndView.addObject("security_mail_notice_open_base", paramBaseSet.getSecurityMailNoticeOpen());
			modelAndView.addObject("security_sms_notice_open_base", paramBaseSet.getSecuritySmsNoticeOpen());
			modelAndView.addObject("security_app_notice_open_base", paramBaseSet.getSecurityAppNoticeOpen());
			
			modelAndView.addObject("security_app_notice_open_base", paramBaseSet.getSecurityAppNoticeOpen());
			modelAndView.addObject("security_app_notice_open_base", paramBaseSet.getSecurityAppNoticeOpen());
			modelAndView.addObject("security_app_notice_open_base", paramBaseSet.getSecurityAppNoticeOpen());
			modelAndView.addObject("security_app_notice_open_base", paramBaseSet.getSecurityAppNoticeOpen());
			
			modelAndView.addObject("phone_authority_base", paramBaseSet.getPhoneAuthority());
			modelAndView.addObject("email_authority_base", paramBaseSet.getEmailAuthority());
			modelAndView.addObject("kyc_authority_base", paramBaseSet.getKycAuthority());
			modelAndView.addObject("kyc_highlevel_authority_base", paramBaseSet.getKycHighlevelAuthority());
			
			modelAndView.addObject("remark", c2cUser.getRemark());

			modelAndView.addObject("c2cManagerMap", this.adminC2cUserService.getAllC2cManager());
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.setViewName("redirect:/" + action + "list.action");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.setViewName("redirect:/" + action + "list.action");
			return modelAndView;
		}
		
		modelAndView.setViewName("c2c_user_update");
		return modelAndView;
	}
	
	/**
	 * 修改 承兑商
	 */
	@RequestMapping(action + "update.action")
	public ModelAndView update(HttpServletRequest request) {
		String id = request.getParameter("id");
		
		String c2c_manager_party_id = request.getParameter("c2c_manager_party_id");
		
		String c2c_user_type = request.getParameter("c2c_user_type");
		String c2c_user_type_name = request.getParameter("c2c_user_type_name");
		String c2c_user_code = request.getParameter("c2c_user_code");		
		String nick_name = request.getParameter("nick_name");
		String head_img = request.getParameter("head_img");
		String user_code = request.getParameter("user_code");
		String user_name = request.getParameter("user_name");
		String deposit = request.getParameter("deposit");
		String deposit_open = request.getParameter("deposit_open");
		String deposit_gift_rate = request.getParameter("deposit_gift_rate");
		
		String thirty_days_order = request.getParameter("thirty_days_order");
		String thirty_days_order_ratio = request.getParameter("thirty_days_order_ratio");
		String thirty_days_pass_average_time = request.getParameter("thirty_days_pass_average_time");
		String thirty_days_pay_average_time = request.getParameter("thirty_days_pay_average_time");
		String thirty_days_amount = request.getParameter("thirty_days_amount");
		String buy_amount = request.getParameter("buy_amount");
		String sell_amount = request.getParameter("sell_amount");
		String total_amount = request.getParameter("total_amount");
		String account_create_days = request.getParameter("account_create_days");
		String first_exchange_days = request.getParameter("first_exchange_days");
		String exchange_users = request.getParameter("exchange_users");
		String buy_success_orders = request.getParameter("buy_success_orders");
		String sell_success_orders = request.getParameter("sell_success_orders");
		String total_success_orders = request.getParameter("total_success_orders");
		String appraise_good = request.getParameter("appraise_good");
		String appraise_bad = request.getParameter("appraise_bad");
		
		String order_mail_notice_open = request.getParameter("order_mail_notice_open");
		String order_sms_notice_open = request.getParameter("order_sms_notice_open");
		String order_app_notice_open = request.getParameter("order_app_notice_open");
		String appeal_mail_notice_open = request.getParameter("appeal_mail_notice_open");
		String appeal_sms_notice_open = request.getParameter("appeal_sms_notice_open");
		String appeal_app_notice_open = request.getParameter("appeal_app_notice_open");
		String chat_app_notice_open = request.getParameter("chat_app_notice_open");
		String security_mail_notice_open = request.getParameter("security_mail_notice_open");
		String security_sms_notice_open = request.getParameter("security_sms_notice_open");
		String security_app_notice_open = request.getParameter("security_app_notice_open");

		String phone_authority = request.getParameter("phone_authority");
		String email_authority = request.getParameter("email_authority");
		String kyc_authority = request.getParameter("kyc_authority");
		String kyc_highlevel_authority = request.getParameter("kyc_highlevel_authority");
				
		String thirty_days_order_base = request.getParameter("thirty_days_order_base");
		String thirty_days_order_ratio_base = request.getParameter("thirty_days_order_ratio_base");
		String thirty_days_pass_average_time_base = request.getParameter("thirty_days_pass_average_time_base");
		String thirty_days_pay_average_time_base = request.getParameter("thirty_days_pay_average_time_base");
		String thirty_days_amount_base = request.getParameter("thirty_days_amount_base");
		String buy_amount_base = request.getParameter("buy_amount_base");
		String sell_amount_base = request.getParameter("sell_amount_base");
		String total_amount_base = request.getParameter("total_amount_base");
		String account_create_days_base = request.getParameter("account_create_days_base");
		String first_exchange_days_base = request.getParameter("first_exchange_days_base");
		String exchange_users_base = request.getParameter("exchange_users_base");
		String buy_success_orders_base = request.getParameter("buy_success_orders_base");
		String sell_success_orders_base = request.getParameter("sell_success_orders_base");
		String total_success_orders_base = request.getParameter("total_success_orders_base");
		String appraise_good_base = request.getParameter("appraise_good_base");
		String appraise_bad_base = request.getParameter("appraise_bad_base");
		
		String order_mail_notice_open_base = request.getParameter("order_mail_notice_open_base");
		String order_sms_notice_open_base = request.getParameter("order_sms_notice_open_base");
		String order_app_notice_open_base = request.getParameter("order_app_notice_open_base");
		String appeal_mail_notice_open_base = request.getParameter("appeal_mail_notice_open_base");
		String appeal_sms_notice_open_base = request.getParameter("appeal_sms_notice_open_base");
		String appeal_app_notice_open_base = request.getParameter("appeal_app_notice_open_base");
		String chat_app_notice_open_base = request.getParameter("chat_app_notice_open_base");
		String security_mail_notice_open_base = request.getParameter("security_mail_notice_open_base");
		String security_sms_notice_open_base = request.getParameter("security_sms_notice_open_base");
		String security_app_notice_open_base = request.getParameter("security_app_notice_open_base");

		String phone_authority_base = request.getParameter("phone_authority_base");
		String email_authority_base = request.getParameter("email_authority_base");
		String kyc_authority_base = request.getParameter("kyc_authority_base");
		String kyc_highlevel_authority_base = request.getParameter("kyc_highlevel_authority_base");
		
		String remark = request.getParameter("remark");
		
		String login_safeword = request.getParameter("login_safeword");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("id", id);

		modelAndView.addObject("c2c_manager_party_id", c2c_manager_party_id);
		
		modelAndView.addObject("c2c_user_type", c2c_user_type);
		modelAndView.addObject("c2c_user_type_name", c2c_user_type_name);
		modelAndView.addObject("c2c_user_code", c2c_user_code);
		modelAndView.addObject("nick_name", nick_name);
		modelAndView.addObject("head_img", head_img);
		modelAndView.addObject("user_code", user_code);
		modelAndView.addObject("user_name", user_name);
		modelAndView.addObject("deposit", deposit);
		modelAndView.addObject("deposit_open", deposit_open);
		modelAndView.addObject("deposit_gift_rate", deposit_gift_rate);
		
		modelAndView.addObject("thirty_days_order", thirty_days_order);
		modelAndView.addObject("thirty_days_order_ratio", thirty_days_order_ratio);
		modelAndView.addObject("thirty_days_pass_average_time", thirty_days_pass_average_time);
		modelAndView.addObject("thirty_days_pay_average_time", thirty_days_pay_average_time);
		modelAndView.addObject("thirty_days_amount", thirty_days_amount);
		modelAndView.addObject("buy_amount", buy_amount);
		modelAndView.addObject("sell_amount", sell_amount);
		modelAndView.addObject("total_amount", total_amount);
		modelAndView.addObject("account_create_days", account_create_days);
		modelAndView.addObject("first_exchange_days", first_exchange_days);
		modelAndView.addObject("exchange_users", exchange_users);
		modelAndView.addObject("buy_success_orders", buy_success_orders);
		modelAndView.addObject("sell_success_orders", sell_success_orders);
		modelAndView.addObject("total_success_orders", total_success_orders);
		modelAndView.addObject("appraise_good", appraise_good);
		modelAndView.addObject("appraise_bad", appraise_bad);
		
		modelAndView.addObject("order_mail_notice_open", order_mail_notice_open);
		modelAndView.addObject("order_sms_notice_open", order_sms_notice_open);
		modelAndView.addObject("order_app_notice_open", order_app_notice_open);
		modelAndView.addObject("appeal_mail_notice_open", appeal_mail_notice_open);
		modelAndView.addObject("appeal_sms_notice_open", appeal_sms_notice_open);
		modelAndView.addObject("appeal_app_notice_open", appeal_app_notice_open);
		modelAndView.addObject("chat_app_notice_open", chat_app_notice_open);
		modelAndView.addObject("security_mail_notice_open", security_mail_notice_open);
		modelAndView.addObject("security_sms_notice_open", security_sms_notice_open);
		modelAndView.addObject("security_app_notice_open", security_app_notice_open);
		
		modelAndView.addObject("phone_authority", phone_authority);
		modelAndView.addObject("email_authority", email_authority);
		modelAndView.addObject("kyc_authority", kyc_authority);
		modelAndView.addObject("kyc_highlevel_authority", kyc_highlevel_authority);
		
		modelAndView.addObject("thirty_days_order_base", thirty_days_order_base);
		modelAndView.addObject("thirty_days_order_ratio_base", thirty_days_order_ratio_base);
		modelAndView.addObject("thirty_days_pass_average_time_base", thirty_days_pass_average_time_base);
		modelAndView.addObject("thirty_days_pay_average_time_base", thirty_days_pay_average_time_base);
		modelAndView.addObject("thirty_days_amount_base", thirty_days_amount_base);
		modelAndView.addObject("buy_amount_base", buy_amount_base);
		modelAndView.addObject("sell_amount_base", sell_amount_base);
		modelAndView.addObject("total_amount_base", total_amount_base);
		modelAndView.addObject("account_create_days_base", account_create_days_base);
		modelAndView.addObject("first_exchange_days_base", first_exchange_days_base);
		modelAndView.addObject("exchange_users_base", exchange_users_base);
		modelAndView.addObject("buy_success_orders_base", buy_success_orders_base);
		modelAndView.addObject("sell_success_orders_base", sell_success_orders_base);
		modelAndView.addObject("total_success_orders_base", total_success_orders_base);
		modelAndView.addObject("appraise_good_base", appraise_good_base);
		modelAndView.addObject("appraise_bad_base", appraise_bad_base);
		
		modelAndView.addObject("order_mail_notice_open_base", order_mail_notice_open_base);
		modelAndView.addObject("order_sms_notice_open_base", order_sms_notice_open_base);
		modelAndView.addObject("order_app_notice_open_base", order_app_notice_open_base);
		modelAndView.addObject("appeal_mail_notice_open_base", appeal_mail_notice_open_base);
		modelAndView.addObject("appeal_sms_notice_open_base", appeal_sms_notice_open_base);
		modelAndView.addObject("appeal_app_notice_open_base", appeal_app_notice_open_base);
		modelAndView.addObject("chat_app_notice_open_base", chat_app_notice_open_base);
		modelAndView.addObject("security_mail_notice_open_base", security_mail_notice_open_base);
		modelAndView.addObject("security_sms_notice_open_base", security_sms_notice_open_base);
		modelAndView.addObject("security_app_notice_open_base", security_app_notice_open_base);
		
		modelAndView.addObject("phone_authority_base", phone_authority_base);
		modelAndView.addObject("email_authority_base", email_authority_base);
		modelAndView.addObject("kyc_authority_base", kyc_authority_base);
		modelAndView.addObject("kyc_highlevel_authority_base", kyc_highlevel_authority_base);
		
		modelAndView.addObject("remark", remark);

		try {

			modelAndView.addObject("c2cManagerMap", this.adminC2cUserService.getAllC2cManager());
			
			String error = this.verifBase(c2c_user_type, user_code, nick_name, head_img, deposit, deposit_open, deposit_gift_rate);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			error = this.verifStat(" 基础设置 ", thirty_days_order_base, thirty_days_order_ratio_base, thirty_days_pass_average_time_base, thirty_days_pay_average_time_base, 
					thirty_days_amount_base, buy_amount_base, sell_amount_base, total_amount_base, account_create_days_base, first_exchange_days_base);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			error = this.verifNotice(" 基础设置 ", order_mail_notice_open_base, order_sms_notice_open_base, order_app_notice_open_base, appeal_mail_notice_open_base, appeal_sms_notice_open_base, 
					appeal_app_notice_open_base, chat_app_notice_open_base, security_mail_notice_open_base, security_sms_notice_open_base, security_app_notice_open_base);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			error = this.verifVerif(" 基础设置 ", phone_authority_base, email_authority_base, kyc_authority_base, kyc_highlevel_authority_base);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			error = this.verifOthers(" 基础设置 ", exchange_users_base, buy_success_orders_base, sell_success_orders_base, total_success_orders_base, appraise_good_base, appraise_bad_base);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			String userNameLogin = this.getUsername_login();
			
			SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
			this.checkLoginSafeword(sec, userNameLogin, login_safeword);
				
			C2cUser c2cUser = this.adminC2cUserService.findById(id);
			if (null == c2cUser) {
				throw new BusinessException("承兑商不存在");				
			}
			
			C2cUserParamBaseSet paramBaseSet = this.c2cUserParamBaseSetService.getByPartyId(c2cUser.getC2cUserPartyId());
			if (null == paramBaseSet) {
				throw new BusinessException("承兑商参数基础设置不存在");				
			}
			
			Party party = this.partyService.cachePartyBy(c2cUser.getC2cUserPartyId(), false);
			if (null == party) {
				throw new BusinessException("承兑商的用户信息不存在");				
			}
			
			String log = MessageFormat.format("ip:" + this.getIp() 
				+ ",管理员修改承兑商信息,id:{0},原承兑商类型:{1},原承兑商CODE:{2},原承兑商PARTY_ID:{3},原承兑商昵称:{4},原承兑商头像:{5},原剩余保证金:{6},原保证金:{7},"
				+ "原备注:{8},原创建时间:{9},原更新时间:{10},原C2C管理员PARTY_ID:{11},原保证金赠送比率:{12}", 
				c2cUser.getId(), c2cUser.getC2cUserType(), c2cUser.getC2cUserCode(), c2cUser.getC2cUserPartyId(), c2cUser.getNickName(), c2cUser.getHeadImg(), c2cUser.getDeposit(), c2cUser.getDepositOpen(), 
				c2cUser.getRemark(), c2cUser.getCreateTime(), c2cUser.getUpdateTime(), c2cUser.getC2cManagerPartyId(), c2cUser.getDepositGiftRate());

			c2cUser.setC2cManagerPartyId(c2c_manager_party_id);
			c2cUser.setNickName(nick_name);
			c2cUser.setHeadImg(head_img);
//				c2cUser.setDeposit(Double.valueOf(deposit).doubleValue());
//				c2cUser.setDepositOpen(Double.valueOf(deposit_open).doubleValue());
			c2cUser.setDepositGiftRate(Double.valueOf(deposit_gift_rate).doubleValue());	
			c2cUser.setRemark(remark);
			c2cUser.setUpdateTime(new Date());

			this.adminC2cUserService.update(c2cUser);
			
			log += MessageFormat.format(",id:{0},新承兑商类型:{1},新承兑商CODE:{2},新承兑商PARTY_ID:{3},新承兑商昵称:{4},新承兑商头像:{5},新剩余保证金:{6},新保证金:{7},"
				+ "新备注:{8},新创建时间:{9},新更新时间:{10},新C2C管理员PARTY_ID:{11},新保证金赠送比率:{12}", 
				c2cUser.getId(), c2cUser.getC2cUserType(), c2cUser.getC2cUserCode(), c2cUser.getC2cUserPartyId(), c2cUser.getNickName(), c2cUser.getHeadImg(), c2cUser.getDeposit(), c2cUser.getDepositOpen(), 
				c2cUser.getRemark(), c2cUser.getCreateTime(), c2cUser.getUpdateTime(), c2cUser.getC2cManagerPartyId(), c2cUser.getDepositGiftRate());
			
			this.saveLog(sec, userNameLogin, log, Constants.LOG_CATEGORY_C2C);
			
			String logBase = MessageFormat.format("ip:" + this.getIp() 
				+ ",管理员修改承兑商参数基础设置信息,id:{0},原承兑商PARTY_ID:{1},"
				+ "原30日成单数:{2},原30日成单率:{3},原30日平均放行时间:{4},原30日平均付款时间:{5},原30日交易量:{6},原买交易量:{7},原卖交易量:{8},原总交易量:{9},"
				+ "原账号创建天数:{10},原首次交易至今天数:{11},原交易人数:{12},原买成单数:{13},原卖成单数:{14},原总成单数:{15},"
				+ "原好评数:{16},原差评数:{17},原订单邮件通知:{18},原订单短信通知:{19},原订单APP通知:{20},原申诉邮件通知:{21},"
				+ "原申诉短信通知:{22},原申诉APP通知:{23},原聊天APP通知:{24},原安全邮件通知:{25},原安全短信通知:{26},原安全APP通知:{27},"
				+ "原手机验证:{28},原邮箱验证:{29},原身份认证:{30},原高级认证:{31},原创建时间:{32},原更新时间:{33}", 
				paramBaseSet.getId(), paramBaseSet.getC2cUserPartyId(), 
				paramBaseSet.getThirtyDaysOrder(), paramBaseSet.getThirtyDaysOrderRatio(), paramBaseSet.getThirtyDaysPassAverageTime(), paramBaseSet.getThirtyDaysPayAverageTime(), paramBaseSet.getThirtyDaysAmount(), paramBaseSet.getBuyAmount(), paramBaseSet.getSellAmount(), paramBaseSet.getTotalAmount(), 
				paramBaseSet.getAccountCreateDays(), paramBaseSet.getFirstExchangeDays(), paramBaseSet.getExchangeUsers(), paramBaseSet.getBuySuccessOrders(), paramBaseSet.getSellSuccessOrders(), paramBaseSet.getTotalSuccessOrders(), 
				paramBaseSet.getAppraiseGood(), paramBaseSet.getAppraiseBad(), paramBaseSet.getOrderMailNoticeOpen(), paramBaseSet.getOrderSmsNoticeOpen(), paramBaseSet.getOrderAppNoticeOpen(), paramBaseSet.getAppealMailNoticeOpen(), 
				paramBaseSet.getAppealSmsNoticeOpen(), paramBaseSet.getAppealAppNoticeOpen(), paramBaseSet.getChatAppNoticeOpen(), paramBaseSet.getSecurityMailNoticeOpen(), paramBaseSet.getSecuritySmsNoticeOpen(), paramBaseSet.getSecurityAppNoticeOpen(), 
				paramBaseSet.getPhoneAuthority(), paramBaseSet.getEmailAuthority(), paramBaseSet.getKycAuthority(), paramBaseSet.getKycHighlevelAuthority(), paramBaseSet.getCreateTime(), paramBaseSet.getUpdateTime());

			paramBaseSet.setThirtyDaysOrder(Integer.valueOf(thirty_days_order_base).intValue());
			paramBaseSet.setThirtyDaysOrderRatio(Double.valueOf(thirty_days_order_ratio_base).doubleValue());
			paramBaseSet.setThirtyDaysPassAverageTime(Integer.valueOf(thirty_days_pass_average_time_base).intValue());
			paramBaseSet.setThirtyDaysPayAverageTime(Integer.valueOf(thirty_days_pay_average_time_base).intValue());
			paramBaseSet.setThirtyDaysAmount(Double.valueOf(thirty_days_amount_base).doubleValue());
			paramBaseSet.setBuyAmount(Double.valueOf(buy_amount_base).doubleValue());
			paramBaseSet.setSellAmount(Double.valueOf(sell_amount_base).doubleValue());
			paramBaseSet.setTotalAmount(Double.valueOf(total_amount_base).doubleValue());
			paramBaseSet.setAccountCreateDays(Integer.valueOf(account_create_days_base).intValue());
			paramBaseSet.setFirstExchangeDays(Integer.valueOf(first_exchange_days_base).intValue());
			paramBaseSet.setExchangeUsers(Integer.valueOf(exchange_users_base).intValue());
			paramBaseSet.setBuySuccessOrders(Integer.valueOf(buy_success_orders_base).intValue());
			paramBaseSet.setSellSuccessOrders(Integer.valueOf(sell_success_orders_base).intValue());
			paramBaseSet.setTotalSuccessOrders(Integer.valueOf(total_success_orders_base).intValue());
			paramBaseSet.setAppraiseGood(Integer.valueOf(appraise_good_base).intValue());
			paramBaseSet.setAppraiseBad(Integer.valueOf(appraise_bad_base).intValue());
			paramBaseSet.setOrderMailNoticeOpen(Integer.valueOf(order_mail_notice_open_base).intValue());
			paramBaseSet.setOrderSmsNoticeOpen(Integer.valueOf(order_sms_notice_open_base).intValue());
			paramBaseSet.setOrderAppNoticeOpen(Integer.valueOf(order_app_notice_open_base).intValue());
			paramBaseSet.setAppealMailNoticeOpen(Integer.valueOf(appeal_mail_notice_open_base).intValue());
			paramBaseSet.setAppealSmsNoticeOpen(Integer.valueOf(appeal_sms_notice_open_base).intValue());
			paramBaseSet.setAppealAppNoticeOpen(Integer.valueOf(appeal_app_notice_open_base).intValue());
			paramBaseSet.setChatAppNoticeOpen(Integer.valueOf(chat_app_notice_open_base).intValue());
			paramBaseSet.setSecurityMailNoticeOpen(Integer.valueOf(security_mail_notice_open_base).intValue());
			paramBaseSet.setSecuritySmsNoticeOpen(Integer.valueOf(security_sms_notice_open_base).intValue());
			paramBaseSet.setSecurityAppNoticeOpen(Integer.valueOf(security_app_notice_open_base).intValue());
			paramBaseSet.setPhoneAuthority(phone_authority_base);
			paramBaseSet.setEmailAuthority(email_authority_base);
			paramBaseSet.setKycAuthority(kyc_authority_base);
			paramBaseSet.setKycHighlevelAuthority(kyc_highlevel_authority_base);
			paramBaseSet.setUpdateTime(new Date());
			
			this.c2cUserParamBaseSetService.update(paramBaseSet);
			
			logBase += MessageFormat.format(",id:{0},新承兑商PARTY_ID:{1},"
				+ "新30日成单数:{2},新30日成单率:{3},新30日平均放行时间:{4},新30日平均付款时间:{5},新30日交易量:{6},新买交易量:{7},新卖交易量:{8},新总交易量:{9},"
				+ "新账号创建天数:{10},新首次交易至今天数:{11},新交易人数:{12},新买成单数:{13},新卖成单数:{14},新总成单数:{15},"
				+ "新好评数:{16},新差评数:{17},新订单邮件通知:{18},新订单短信通知:{19},新订单APP通知:{20},新申诉邮件通知:{21},"
				+ "新申诉短信通知:{22},新申诉APP通知:{23},新聊天APP通知:{24},新安全邮件通知:{25},新安全短信通知:{26},新安全APP通知:{27},"
				+ "新手机验证:{28},新邮箱验证:{29},新身份认证:{30},新高级认证:{31},新创建时间:{32},新更新时间:{33}", 
				paramBaseSet.getId(), paramBaseSet.getC2cUserPartyId(), 
				paramBaseSet.getThirtyDaysOrder(), paramBaseSet.getThirtyDaysOrderRatio(), paramBaseSet.getThirtyDaysPassAverageTime(), paramBaseSet.getThirtyDaysPayAverageTime(), paramBaseSet.getThirtyDaysAmount(), paramBaseSet.getBuyAmount(), paramBaseSet.getSellAmount(), paramBaseSet.getTotalAmount(), 
				paramBaseSet.getAccountCreateDays(), paramBaseSet.getFirstExchangeDays(), paramBaseSet.getExchangeUsers(), paramBaseSet.getBuySuccessOrders(), paramBaseSet.getSellSuccessOrders(), paramBaseSet.getTotalSuccessOrders(), 
				paramBaseSet.getAppraiseGood(), paramBaseSet.getAppraiseBad(), paramBaseSet.getOrderMailNoticeOpen(), paramBaseSet.getOrderSmsNoticeOpen(), paramBaseSet.getOrderAppNoticeOpen(), paramBaseSet.getAppealMailNoticeOpen(), 
				paramBaseSet.getAppealSmsNoticeOpen(), paramBaseSet.getAppealAppNoticeOpen(), paramBaseSet.getChatAppNoticeOpen(), paramBaseSet.getSecurityMailNoticeOpen(), paramBaseSet.getSecuritySmsNoticeOpen(), paramBaseSet.getSecurityAppNoticeOpen(), 
				paramBaseSet.getPhoneAuthority(), paramBaseSet.getEmailAuthority(), paramBaseSet.getKycAuthority(), paramBaseSet.getKycHighlevelAuthority(), paramBaseSet.getCreateTime(), paramBaseSet.getUpdateTime());
			
			this.saveLog(sec, userNameLogin, logBase, Constants.LOG_CATEGORY_C2C);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.setViewName("c2c_user_update");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.setViewName("c2c_user_update");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}
	
	/**
	 * 修改 承兑商保证金
	 */
	@RequestMapping(value = action + "reset.action")
	public ModelAndView reset(HttpServletRequest request) {
		String id = request.getParameter("id");
		String recharge_withdraw = request.getParameter("recharge_withdraw");
		String money_change = request.getParameter("money_change");
		String safe_password = request.getParameter("safe_password");
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");
		modelAndView.addObject("id", id);
		modelAndView.addObject("recharge_withdraw", recharge_withdraw);
		modelAndView.addObject("money_change", money_change);
		
		try {
			
			if (StringUtils.isEmptyString(recharge_withdraw)) {
				throw new BusinessException("修改方式不正确");
			}
			
			if (StringUtils.isEmptyString(money_change) || !StringUtils.isDouble(money_change) || Double.valueOf(money_change).doubleValue() <= 0) {
				throw new BusinessException("修改额度未填或格式不正确");
			}
			
			DecimalFormat df = new DecimalFormat("#.########");
			double money_change_double = Double.valueOf(money_change).doubleValue();
						
			C2cUser c2cUser = this.adminC2cUserService.findById(id);
			if (null == c2cUser) {
				throw new BusinessException("承兑商不存在");
			}
			
			String userNameLogin = this.getUsername_login();
			
			SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
			this.checkLoginSafeword(sec, userNameLogin, safe_password);	
			
			double newDepositOpen = 0;
			double newDeposit = 0;
			if ("recharge".equals(recharge_withdraw)) {
//					if (money_change_double > 1000000) {
//						throw new BusinessException("单次充值额度不能超过 1000000 USDT");
//					}
				newDepositOpen = Double.valueOf(df.format(Arith.add(money_change_double, c2cUser.getDepositOpen()))).doubleValue();
				newDeposit = Double.valueOf(df.format(Arith.add(money_change_double, c2cUser.getDeposit()))).doubleValue();
			} else if ("withdraw".equals(recharge_withdraw)) {
				money_change_double = 0 - money_change_double;
				newDepositOpen = Double.valueOf(df.format(Arith.add(money_change_double, c2cUser.getDepositOpen()))).doubleValue();
				newDeposit = Double.valueOf(df.format(Arith.add(money_change_double, c2cUser.getDeposit()))).doubleValue();
				if (newDepositOpen <= 0 || newDeposit <= 0) {
					throw new BusinessException("提现额度不能超过总保证金和剩余保证金");
				}
			} else {
				throw new BusinessException("修改方式不正确");
			}

			c2cUser.setDepositOpen(newDepositOpen);
			c2cUser.setDeposit(newDeposit);
			
			this.adminC2cUserService.update(c2cUser);
			
			String log = MessageFormat.format("承兑商CODE:{0},承兑商昵称:{1},剩余保证金:{2},保证金:{3},修改金额:{4}", 
					c2cUser.getC2cUserCode(), c2cUser.getNickName(), c2cUser.getDeposit(), c2cUser.getDepositOpen(), money_change_double);

			this.saveLog(sec, userNameLogin, log, Constants.LOG_CATEGORY_C2C);
			
		} catch (BusinessException e) {	
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		return modelAndView;	
	}
	
	/**
	 * 获取 C2C管理员信息
	 */
	@RequestMapping(value = action + "getC2cManagerInfo.action")
	public String getC2cManagerInfo(HttpServletRequest request) {
		String manager_id = request.getParameter("manager_id");
		String c2c_user_id = request.getParameter("c2c_user_id");
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			if (StringUtils.isEmptyString(manager_id)) {
				throw new BusinessException("C2C管理员PARTY_ID为空");
			}
			
			if (StringUtils.isEmptyString(c2c_user_id)) {
				throw new BusinessException("承兑商ID为空");
			}
			
			List<Map<String, Object>> ret = new ArrayList<Map<String,Object>>();
			
			List<Map<String, Object>> result = this.adminC2cUserService.getC2cManagerC2cUser(manager_id);
			if (null == result) {
				resultMap.put("manager_c2c_user", ret);
			} else {
				for (int i = 0; i < result.size(); i++) {
					Map<String, Object> res = result.get(i);
					if (c2c_user_id.equals(res.get("id"))) {
						ret.add(res);
						result.remove(i);
						break;
					}
				}
				ret.addAll(result);
				resultMap.put("manager_c2c_user", ret);
			}
			
			resultMap.put("code", 200);
			
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

	private String verifBase(String c2c_user_type, String c2c_user_party_code, String nick_name, String head_img, String deposit, String deposit_open, String deposit_gift_rate) {
		if (StringUtils.isEmptyString(c2c_user_type) || !Arrays.asList("1", "2").contains(c2c_user_type)) {
			return "承兑商类型未填或格式不正确";
		}		
		if ("2".equals(c2c_user_type)) {
			// 用户承兑商
			if (StringUtils.isEmptyString(c2c_user_party_code)) {
				return "用户承兑商：必须填写用户UID";
			}
		}		
		if (StringUtils.isEmptyString(nick_name)) {
			return "承兑商昵称必填";
		}		
		if (StringUtils.isEmptyString(head_img)) {
			return "请上传承兑商头像";
		}
		if (StringUtils.isEmptyString(deposit_gift_rate) || !StringUtils.isDouble(deposit_gift_rate) || Double.valueOf(deposit_gift_rate).doubleValue() < 0) {
			return "保证金赠送比率未填或格式不正确";
		}
		return null;
	}
	
	private String verifFront(String type_front, String username_front, String password_front, String re_password_front, String usercode_front) {
		if (StringUtils.isEmptyString(type_front) || !Arrays.asList("1", "2").contains(type_front)) {
			return "注册类型（手机或邮箱）未填或格式不正确";
		}	
		if (StringUtils.isEmptyString(username_front)) {
			return "前端登录用户名必填";
		}		
		if (StringUtils.isEmptyString(password_front)) {
			return "前端登录密码必填";
		}
		if (!password_front.equals(re_password_front)) {
			return "前端登录密码确认不一致";
		}
//		if (StringUtils.isEmptyString(usercode_front)) {
//			return "推荐码必填";
//		}
		return null;
	}
	
	private String verifStat(String baseSetTip, String thirty_days_order, String thirty_days_order_ratio, String thirty_days_pass_average_time, String thirty_days_pay_average_time, 
			String thirty_days_amount, String buy_amount, String sell_amount, String total_amount, String account_create_days, String first_exchange_days) {
		if (StringUtils.isEmptyString(thirty_days_order) || !StringUtils.isInteger(thirty_days_order) || Integer.valueOf(thirty_days_order).intValue() < 0) {
			return "30日成单数" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(thirty_days_order_ratio) || !StringUtils.isDouble(thirty_days_order_ratio) || Double.valueOf(thirty_days_order_ratio).doubleValue() < 0) {
			return "30日成单率" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(thirty_days_pass_average_time) || !StringUtils.isInteger(thirty_days_pass_average_time) || Integer.valueOf(thirty_days_pass_average_time).intValue() < 0) {
			return "30日平均放行时间" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(thirty_days_pay_average_time) || !StringUtils.isInteger(thirty_days_pay_average_time) || Integer.valueOf(thirty_days_pay_average_time).intValue() < 0) {
			return "30日平均付款时间" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(thirty_days_amount) || !StringUtils.isDouble(thirty_days_amount) || Double.valueOf(thirty_days_amount).doubleValue() < 0) {
			return "30日交易量" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(buy_amount) || !StringUtils.isDouble(buy_amount) || Double.valueOf(buy_amount).doubleValue() < 0) {
			return "买交易量" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(sell_amount) || !StringUtils.isDouble(sell_amount) || Double.valueOf(sell_amount).doubleValue() < 0) {
			return "卖交易量" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(total_amount) || !StringUtils.isDouble(total_amount) || Double.valueOf(total_amount).doubleValue() < 0) {
			return "总交易量" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(account_create_days) || !StringUtils.isInteger(account_create_days) || Integer.valueOf(account_create_days).intValue() < 0) {
			return "账号创建天数" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(first_exchange_days) || !StringUtils.isInteger(first_exchange_days) || Integer.valueOf(first_exchange_days).intValue() < 0) {
			return "首次交易至今天数" + baseSetTip + "未填或格式不正确";
		}
		return null;
	}

	private String verifNotice(String baseSetTip, String order_mail_notice_open, String order_sms_notice_open, String order_app_notice_open, String appeal_mail_notice_open, String appeal_sms_notice_open, 
			String appeal_app_notice_open, String chat_app_notice_open, String security_mail_notice_open, String security_sms_notice_open, String security_app_notice_open) {
		if (StringUtils.isEmptyString(order_mail_notice_open) || !Arrays.asList("0", "1").contains(order_mail_notice_open)) {
			return "订单邮件通知" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(order_sms_notice_open) || !Arrays.asList("0", "1").contains(order_sms_notice_open)) {
			return "订单短信通知" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(order_app_notice_open) || !Arrays.asList("0", "1").contains(order_app_notice_open)) {
			return "订单APP通知" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(appeal_mail_notice_open) || !Arrays.asList("0", "1").contains(appeal_mail_notice_open)) {
			return "申诉邮件通知" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(appeal_sms_notice_open) || !Arrays.asList("0", "1").contains(appeal_sms_notice_open)) {
			return "申诉短信通知" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(appeal_app_notice_open) || !Arrays.asList("0", "1").contains(appeal_app_notice_open)) {
			return "申诉APP通知" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(chat_app_notice_open) || !Arrays.asList("0", "1").contains(chat_app_notice_open)) {
			return "聊天APP通知" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(security_mail_notice_open) || !Arrays.asList("0", "1").contains(security_mail_notice_open)) {
			return "安全邮件通知" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(security_sms_notice_open) || !Arrays.asList("0", "1").contains(security_sms_notice_open)) {
			return "安全短信通知" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(security_app_notice_open) || !Arrays.asList("0", "1").contains(security_app_notice_open)) {
			return "安全APP通知" + baseSetTip + "未填或格式不正确";
		}
		return null;
	}
	
	private String verifVerif(String baseSetTip, String phone_authority, String email_authority, String kyc_authority, String kyc_highlevel_authority) {
		if (StringUtils.isEmptyString(phone_authority) || !Arrays.asList("N", "Y").contains(phone_authority)) {
			return "手机验证" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(email_authority) || !Arrays.asList("N", "Y").contains(email_authority)) {
			return "邮箱验证" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(kyc_authority) || !Arrays.asList("N", "Y").contains(kyc_authority)) {
			return "身份认证" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(kyc_highlevel_authority) || !Arrays.asList("N", "Y").contains(kyc_highlevel_authority)) {
			return "高级认证" + baseSetTip + "未填或格式不正确";
		}
		return null;
	}
	
	private String verifOthers(String baseSetTip, String exchange_users, String buy_success_orders, String sell_success_orders, String total_success_orders, String appraise_good, String appraise_bad) {
		if (StringUtils.isEmptyString(exchange_users) || !StringUtils.isInteger(exchange_users) || Integer.valueOf(exchange_users).intValue() < 0) {
			return "交易人数" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(buy_success_orders) || !StringUtils.isInteger(buy_success_orders) || Integer.valueOf(buy_success_orders).intValue() < 0) {
			return "买成单数" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(sell_success_orders) || !StringUtils.isInteger(sell_success_orders) || Integer.valueOf(sell_success_orders).intValue() < 0) {
			return "卖成单数" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(total_success_orders) || !StringUtils.isInteger(total_success_orders) || Integer.valueOf(total_success_orders).intValue() < 0) {
			return "总成单数" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(appraise_good) || !StringUtils.isInteger(appraise_good) || Integer.valueOf(appraise_good).intValue() < 0) {
			return "好评数" + baseSetTip + "未填或格式不正确";
		}
		if (StringUtils.isEmptyString(appraise_bad) || !StringUtils.isInteger(appraise_bad) || Integer.valueOf(appraise_bad).intValue() < 0) {
			return "差评数" + baseSetTip + "未填或格式不正确";
		}
		return null;
	}

	/**
	 * 验证登录人资金密码
	 */
	protected void checkLoginSafeword(SecUser secUser, String operatorUsername, String loginSafeword) {
//		SecUser sec = this.secUserService.findUserByLoginName(operatorUsername);
		String sysSafeword = secUser.getSafeword();
		String safeword_md5 = this.passwordEncoder.encodePassword(loginSafeword, operatorUsername);
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("登录人资金密码错误");
		}		
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
