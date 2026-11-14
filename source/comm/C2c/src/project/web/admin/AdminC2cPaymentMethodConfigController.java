package project.web.admin;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.providers.encoding.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.util.JsonUtils;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.Constants;
import project.RedisKeys;
import project.c2c.AdminC2cPaymentMethodConfigService;
import project.c2c.C2cAdvertService;
import project.c2c.C2cPaymentMethodConfig;
import project.c2c.C2cPaymentMethodConfigService;
import project.c2c.C2cTranslateService;
import project.log.LogService;
import project.party.PartyService;
import project.redis.RedisHandler;
import security.SecUser;
import security.internal.SecUserService;

/**
 * C2C支付方式模板
 */
@RestController
public class AdminC2cPaymentMethodConfigController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminC2cPaymentMethodConfigController.class);

	@Autowired
	private AdminC2cPaymentMethodConfigService adminC2cPaymentMethodConfigService;
	@Autowired
	private SecUserService secUserService;
	@Autowired
	private LogService logService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	protected PartyService partyService;
	@Autowired
	private C2cPaymentMethodConfigService c2cPaymentMethodConfigService;
	@Autowired
	private C2cAdvertService c2cAdvertService;
	@Autowired
	private C2cTranslateService c2cTranslateService;
	@Autowired
	private RedisHandler redisHandler;

	private final String action = "normal/adminC2cPaymentMethodConfigAction!";
	
	/**
	 * 获取 C2C支付方式模板 列表
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String pageNoStr = request.getParameter("pageNo");
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		String method_type_para = request.getParameter("method_type_para");
		String method_name_para = request.getParameter("method_name_para");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("c2c_payment_method_config_list");

		int pageNo=1;
		Page page=null;
		int pageSize=20;
		try {
			pageNo=checkAndSetPageNo(pageNoStr);
			page = this.adminC2cPaymentMethodConfigService.pagedQuery(pageNo, pageSize, method_type_para, method_name_para);

			List<Map<String, Object>> list = (List<Map<String, Object>>) page.getElements();
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> map = list.get(i);

				if (null == map.get("method_type")) {
					map.put("method_type_name", "");
				} else {
					String methodType = map.get("method_type").toString();
					Map<String, String> pmtMap = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");
					map.put("method_type_name", pmtMap.containsKey(methodType) ? pmtMap.get(methodType) : methodType);
				}
			}

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
		modelAndView.addObject("method_type_para", method_type_para);
		modelAndView.addObject("method_name_para", method_name_para);
		modelAndView.addObject("methodTypeMap", this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type"));
		return modelAndView;
	}
	
	/**
	 * 新增 C2C支付方式模板 页面
	 */
	@RequestMapping(action + "toAdd.action")
	public ModelAndView toAdd(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("methodTypeMap", this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type"));
		modelAndView.setViewName("c2c_payment_method_config_add");
		return modelAndView;
	}
	
	/**
	 * 新增 C2C支付方式模板
	 */
	@RequestMapping(action + "add.action")
	public ModelAndView add(HttpServletRequest request) {	
		String method_type = request.getParameter("method_type");			
		String method_name = request.getParameter("method_name");	
		String method_img = request.getParameter("method_img");
		String param_name1 = request.getParameter("param_name1").trim();		
		String param_name2 = request.getParameter("param_name2").trim();		
		String param_name3 = request.getParameter("param_name3").trim();		
		String param_name4 = request.getParameter("param_name4").trim();		
		String param_name5 = request.getParameter("param_name5").trim();		
		String param_name6 = request.getParameter("param_name6").trim();		
		String param_name7 = request.getParameter("param_name7").trim();		
		String param_name8 = request.getParameter("param_name8").trim();		
		String param_name9 = request.getParameter("param_name9").trim();		
		String param_name10 = request.getParameter("param_name10").trim();		
		String param_name11 = request.getParameter("param_name11").trim();		
		String param_name12 = request.getParameter("param_name12").trim();		
		String param_name13 = request.getParameter("param_name13").trim();		
		String param_name14 = request.getParameter("param_name14").trim();		
		String param_name15 = request.getParameter("param_name15").trim();		
		String login_safeword = request.getParameter("login_safeword");
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("method_type", method_type);
		modelAndView.addObject("method_name", method_name);
		modelAndView.addObject("method_img", method_img);
		modelAndView.addObject("param_name1", param_name1);
		modelAndView.addObject("param_name2", param_name2);
		modelAndView.addObject("param_name3", param_name3);
		modelAndView.addObject("param_name4", param_name4);
		modelAndView.addObject("param_name5", param_name5);
		modelAndView.addObject("param_name6", param_name6);
		modelAndView.addObject("param_name7", param_name7);
		modelAndView.addObject("param_name8", param_name8);
		modelAndView.addObject("param_name9", param_name9);
		modelAndView.addObject("param_name10", param_name10);
		modelAndView.addObject("param_name11", param_name11);
		modelAndView.addObject("param_name12", param_name12);
		modelAndView.addObject("param_name13", param_name13);
		modelAndView.addObject("param_name14", param_name14);
		modelAndView.addObject("param_name15", param_name15);
		modelAndView.addObject("methodTypeMap", this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type"));
		
		try {
			
			String error = this.verif(method_type, method_name, method_img, param_name1, login_safeword);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			String userNameLogin = this.getUsername_login();
			
			SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
			this.checkLoginSafeword(sec, userNameLogin, login_safeword);
			
			C2cPaymentMethodConfig cfg = new C2cPaymentMethodConfig();
			cfg.setId(ApplicationUtil.getCurrentTimeUUID());
			cfg.setMethodType(Integer.valueOf(method_type).intValue());
			cfg.setMethodName(method_name);
			cfg.setMethodImg(method_img);
			cfg.setParamName1(param_name1);
			cfg.setParamName2(param_name2);
			cfg.setParamName3(param_name3);
			cfg.setParamName4(param_name4);
			cfg.setParamName5(param_name5);
			cfg.setParamName6(param_name6);
			cfg.setParamName7(param_name7);
			cfg.setParamName8(param_name8);
			cfg.setParamName9(param_name9);
			cfg.setParamName10(param_name10);
			cfg.setParamName11(param_name11);
			cfg.setParamName12(param_name12);
			cfg.setParamName13(param_name13);
			cfg.setParamName14(param_name14);
			cfg.setParamName15(param_name15);
			cfg.setCreateTime(new Date());
			cfg.setUpdateTime(new Date());
			this.c2cPaymentMethodConfigService.save(cfg);
			C2cPaymentMethodConfig cfgSaved = c2cPaymentMethodConfigService.getC2cPaymentMethodConfig(cfg.getId().toString());
			if (null != cfgSaved) {
				this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_CONFIG_ID + cfgSaved.getId().toString(), cfgSaved);
				
				Map<String, String> map1 = (Map<String, String>) this.redisHandler.get(RedisKeys.C2C_PAYMENT_METHOD_CONFIG_ID_TYPE);
				if (null == map1) {
					map1 = new ConcurrentHashMap<String, String>();
				}
				map1.put(cfgSaved.getId().toString(), String.valueOf(cfgSaved.getMethodType()));
				this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_CONFIG_ID_TYPE, map1);
				
				String log = MessageFormat.format("管理员新增支付方式模板,id:{0},支付方式类型:{1},支付方式名称:{2},支付方式图片:{3},参数名1:{4},参数名2:{5},参数名3:{6}", 
				cfgSaved.getId(), cfg.getMethodType(), cfg.getMethodName(), cfg.getMethodImg(), 
				cfg.getParamName1(), cfg.getParamName2(), cfg.getParamName3());
				
				this.saveLog(sec, userNameLogin, log, Constants.LOG_CATEGORY_C2C);
			}
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.setViewName("c2c_payment_method_config_add");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.setViewName("c2c_payment_method_config_add");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	/**
	 * 修改 C2C支付方式模板 页面
	 */
	@RequestMapping(action + "toUpdate.action")
	public ModelAndView toUpdate(HttpServletRequest request) {
		String id = request.getParameter("id");
		ModelAndView modelAndView = new ModelAndView();

		try {
			C2cPaymentMethodConfig cfg = this.c2cPaymentMethodConfigService.get(id);
			if (null == cfg) {
				throw new BusinessException("支付方式模板不存在");
			}

			modelAndView.addObject("id", id);
			modelAndView.addObject("method_type", cfg.getMethodType());
			modelAndView.addObject("method_name", cfg.getMethodName());
			modelAndView.addObject("method_img", cfg.getMethodImg());
			modelAndView.addObject("param_name1", cfg.getParamName1());
			modelAndView.addObject("param_name2", cfg.getParamName2());
			modelAndView.addObject("param_name3", cfg.getParamName3());
			modelAndView.addObject("param_name4", cfg.getParamName4());
			modelAndView.addObject("param_name5", cfg.getParamName5());
			modelAndView.addObject("param_name6", cfg.getParamName6());
			modelAndView.addObject("param_name7", cfg.getParamName7());
			modelAndView.addObject("param_name8", cfg.getParamName8());
			modelAndView.addObject("param_name9", cfg.getParamName9());
			modelAndView.addObject("param_name10", cfg.getParamName10());
			modelAndView.addObject("param_name11", cfg.getParamName11());
			modelAndView.addObject("param_name12", cfg.getParamName12());
			modelAndView.addObject("param_name13", cfg.getParamName13());
			modelAndView.addObject("param_name14", cfg.getParamName14());
			modelAndView.addObject("param_name15", cfg.getParamName15());
			
			modelAndView.addObject("method_name_lang_trans", StringUtils.isNotEmpty(cfg.getMethodName()) ? this.c2cTranslateService.getTranslate(cfg.getMethodName()) : "");
			modelAndView.addObject("param_name1_lang_trans", StringUtils.isNotEmpty(cfg.getParamName1()) ? this.c2cTranslateService.getTranslate(cfg.getParamName1()) : "");
			modelAndView.addObject("param_name2_lang_trans", StringUtils.isNotEmpty(cfg.getParamName2()) ? this.c2cTranslateService.getTranslate(cfg.getParamName2()) : "");
			modelAndView.addObject("param_name3_lang_trans", StringUtils.isNotEmpty(cfg.getParamName3()) ? this.c2cTranslateService.getTranslate(cfg.getParamName3()) : "");
			modelAndView.addObject("param_name4_lang_trans", StringUtils.isNotEmpty(cfg.getParamName4()) ? this.c2cTranslateService.getTranslate(cfg.getParamName4()) : "");
			modelAndView.addObject("param_name5_lang_trans", StringUtils.isNotEmpty(cfg.getParamName5()) ? this.c2cTranslateService.getTranslate(cfg.getParamName5()) : "");
			modelAndView.addObject("param_name6_lang_trans", StringUtils.isNotEmpty(cfg.getParamName6()) ? this.c2cTranslateService.getTranslate(cfg.getParamName6()) : "");
			modelAndView.addObject("param_name7_lang_trans", StringUtils.isNotEmpty(cfg.getParamName7()) ? this.c2cTranslateService.getTranslate(cfg.getParamName7()) : "");
			modelAndView.addObject("param_name8_lang_trans", StringUtils.isNotEmpty(cfg.getParamName8()) ? this.c2cTranslateService.getTranslate(cfg.getParamName8()) : "");
			modelAndView.addObject("param_name9_lang_trans", StringUtils.isNotEmpty(cfg.getParamName9()) ? this.c2cTranslateService.getTranslate(cfg.getParamName9()) : "");
			modelAndView.addObject("param_name10_lang_trans", StringUtils.isNotEmpty(cfg.getParamName10()) ? this.c2cTranslateService.getTranslate(cfg.getParamName10()) : "");
			modelAndView.addObject("param_name11_lang_trans", StringUtils.isNotEmpty(cfg.getParamName11()) ? this.c2cTranslateService.getTranslate(cfg.getParamName11()) : "");
			modelAndView.addObject("param_name12_lang_trans", StringUtils.isNotEmpty(cfg.getParamName12()) ? this.c2cTranslateService.getTranslate(cfg.getParamName12()) : "");
			modelAndView.addObject("param_name13_lang_trans", StringUtils.isNotEmpty(cfg.getParamName13()) ? this.c2cTranslateService.getTranslate(cfg.getParamName13()) : "");
			modelAndView.addObject("param_name14_lang_trans", StringUtils.isNotEmpty(cfg.getParamName14()) ? this.c2cTranslateService.getTranslate(cfg.getParamName14()) : "");
			modelAndView.addObject("param_name15_lang_trans", StringUtils.isNotEmpty(cfg.getParamName15()) ? this.c2cTranslateService.getTranslate(cfg.getParamName15()) : "");
			
			modelAndView.addObject("methodTypeMap", this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type"));
			modelAndView.addObject("languageList", this.c2cAdvertService.getLanguageIntro());

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
		
		modelAndView.setViewName("c2c_payment_method_config_update");
		return modelAndView;
	}
	
	/**
	 * 修改 C2C支付方式模板
	 */
	@RequestMapping(action + "update.action")
	public ModelAndView update(HttpServletRequest request) {
		String id = request.getParameter("id");
		String method_type = request.getParameter("method_type");			
		String method_name = request.getParameter("method_name");		
		String method_img = request.getParameter("method_img");	
		String param_name1 = request.getParameter("param_name1").trim();		
		String param_name2 = request.getParameter("param_name2").trim();		
		String param_name3 = request.getParameter("param_name3").trim();		
		String param_name4 = request.getParameter("param_name4").trim();		
		String param_name5 = request.getParameter("param_name5").trim();		
		String param_name6 = request.getParameter("param_name6").trim();		
		String param_name7 = request.getParameter("param_name7").trim();		
		String param_name8 = request.getParameter("param_name8").trim();		
		String param_name9 = request.getParameter("param_name9").trim();		
		String param_name10 = request.getParameter("param_name10").trim();	
		String param_name11 = request.getParameter("param_name11").trim();
		String param_name12 = request.getParameter("param_name12").trim();
		String param_name13 = request.getParameter("param_name13").trim();
		String param_name14 = request.getParameter("param_name14").trim();
		String param_name15 = request.getParameter("param_name15").trim();
		
		String method_name_lang_trans = request.getParameter("method_name_lang_trans");
		String param_name1_lang_trans = request.getParameter("param_name1_lang_trans");		
		String param_name2_lang_trans = request.getParameter("param_name2_lang_trans");		
		String param_name3_lang_trans = request.getParameter("param_name3_lang_trans");		
		String param_name4_lang_trans = request.getParameter("param_name4_lang_trans");		
		String param_name5_lang_trans = request.getParameter("param_name5_lang_trans");		
		String param_name6_lang_trans = request.getParameter("param_name6_lang_trans");		
		String param_name7_lang_trans = request.getParameter("param_name7_lang_trans");		
		String param_name8_lang_trans = request.getParameter("param_name8_lang_trans");		
		String param_name9_lang_trans = request.getParameter("param_name9_lang_trans");		
		String param_name10_lang_trans = request.getParameter("param_name10_lang_trans");		
		String param_name11_lang_trans = request.getParameter("param_name11_lang_trans");		
		String param_name12_lang_trans = request.getParameter("param_name12_lang_trans");		
		String param_name13_lang_trans = request.getParameter("param_name13_lang_trans");		
		String param_name14_lang_trans = request.getParameter("param_name14_lang_trans");		
		String param_name15_lang_trans = request.getParameter("param_name15_lang_trans");
		
		String login_safeword = request.getParameter("login_safeword");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("id", id);
		modelAndView.addObject("method_type", method_type);
		modelAndView.addObject("method_name", method_name);
		modelAndView.addObject("method_img", method_img);
		modelAndView.addObject("param_name1", param_name1);
		modelAndView.addObject("param_name2", param_name2);
		modelAndView.addObject("param_name3", param_name3);
		modelAndView.addObject("param_name4", param_name4);
		modelAndView.addObject("param_name5", param_name5);
		modelAndView.addObject("param_name6", param_name6);
		modelAndView.addObject("param_name7", param_name7);
		modelAndView.addObject("param_name8", param_name8);
		modelAndView.addObject("param_name9", param_name9);
		modelAndView.addObject("param_name10", param_name10);
		modelAndView.addObject("param_name11", param_name11);
		modelAndView.addObject("param_name12", param_name12);
		modelAndView.addObject("param_name13", param_name13);
		modelAndView.addObject("param_name14", param_name14);
		modelAndView.addObject("param_name15", param_name15);

		modelAndView.addObject("method_name_lang_trans", method_name_lang_trans);
		modelAndView.addObject("param_name1_lang_trans", param_name1_lang_trans);
		modelAndView.addObject("param_name2_lang_trans", param_name2_lang_trans);
		modelAndView.addObject("param_name3_lang_trans", param_name3_lang_trans);
		modelAndView.addObject("param_name4_lang_trans", param_name4_lang_trans);
		modelAndView.addObject("param_name5_lang_trans", param_name5_lang_trans);
		modelAndView.addObject("param_name6_lang_trans", param_name6_lang_trans);
		modelAndView.addObject("param_name7_lang_trans", param_name7_lang_trans);
		modelAndView.addObject("param_name8_lang_trans", param_name8_lang_trans);
		modelAndView.addObject("param_name9_lang_trans", param_name9_lang_trans);
		modelAndView.addObject("param_name10_lang_trans", param_name10_lang_trans);
		modelAndView.addObject("param_name11_lang_trans", param_name11_lang_trans);
		modelAndView.addObject("param_name12_lang_trans", param_name12_lang_trans);
		modelAndView.addObject("param_name13_lang_trans", param_name13_lang_trans);
		modelAndView.addObject("param_name14_lang_trans", param_name14_lang_trans);
		modelAndView.addObject("param_name15_lang_trans", param_name15_lang_trans);
		
		modelAndView.addObject("methodTypeMap", this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type"));
		modelAndView.addObject("languageList", this.c2cAdvertService.getLanguageIntro());

		try {
			
			String error = this.verif(method_type, method_name, method_img, param_name1, login_safeword);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			String userNameLogin = this.getUsername_login();
			
			SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
			this.checkLoginSafeword(sec, userNameLogin, login_safeword);
			
			C2cPaymentMethodConfig cfg = this.c2cPaymentMethodConfigService.get(id);
			if (null == cfg) {
				throw new BusinessException("支付方式模板不存在");
			}
			
			String log = MessageFormat.format("ip:" + this.getIp() 
				+ ",管理员更新支付方式模板,id:{0},原支付方式类型:{1},原支付方式名称:{2},原支付方式图片:{3},原参数名1:{4},原参数名2:{5},原参数名3:{6},原参数名4:{7},原参数名5:{8},"
				+ "原参数名6:{9},原参数名7:{10},原参数名8:{11},原参数名9:{12},原参数名10:{13},原参数名11:{14},原参数名12:{15},原参数名13:{16},原参数名14:{17},原参数名15:{18},原创建时间:{19},原更新时间:{20}", 
				cfg.getId(), cfg.getMethodType(), cfg.getMethodName(), cfg.getMethodImg(), cfg.getParamName1(), cfg.getParamName2(), cfg.getParamName3(), cfg.getParamName4(), 
				cfg.getParamName5(), cfg.getParamName6(), cfg.getParamName7(), cfg.getParamName8(), cfg.getParamName9(), cfg.getParamName10(), cfg.getParamName11(), cfg.getParamName12(), 
				cfg.getParamName13(), cfg.getParamName14(), cfg.getParamName15(), cfg.getCreateTime(), cfg.getUpdateTime());

			cfg.setMethodType(Integer.valueOf(method_type).intValue());
			cfg.setMethodName(method_name);
			cfg.setMethodImg(method_img);
			cfg.setParamName1(param_name1);
			cfg.setParamName2(param_name2);
			cfg.setParamName3(param_name3);
			cfg.setParamName4(param_name4);
			cfg.setParamName5(param_name5);
			cfg.setParamName6(param_name6);
			cfg.setParamName7(param_name7);
			cfg.setParamName8(param_name8);
			cfg.setParamName9(param_name9);
			cfg.setParamName10(param_name10);
			cfg.setParamName11(param_name11);
			cfg.setParamName12(param_name12);
			cfg.setParamName13(param_name13);
			cfg.setParamName14(param_name14);
			cfg.setParamName15(param_name15);
			cfg.setUpdateTime(new Date());
			boolean state = this.c2cPaymentMethodConfigService.update(cfg);
			if (state) {
				this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_CONFIG_ID + cfg.getId().toString(), cfg);
				
				Map<String, String> map1 = (Map<String, String>) this.redisHandler.get(RedisKeys.C2C_PAYMENT_METHOD_CONFIG_ID_TYPE);
				if (null == map1) {
					map1 = new ConcurrentHashMap<String, String>();
				} else {
					map1.remove(cfg.getId().toString());
				}
				map1.put(cfg.getId().toString(), String.valueOf(cfg.getMethodType()));
				this.redisHandler.setSync(RedisKeys.C2C_PAYMENT_METHOD_CONFIG_ID_TYPE, map1);
			}
			
			log += MessageFormat.format(",新支付方式类型:{0},新支付方式名称:{1},新支付方式图片:{2},新参数名1:{3},新参数名2:{4},新参数名3:{5},新参数名4:{6},新参数名5:{7},"
					+ "新参数名6:{8},新参数名7:{9},新参数名8:{10},新参数名9:{11},新参数名10:{12},新参数名11:{13},新参数名12:{14},新参数名13:{15},新参数名14:{16},新参数名15:{17},新创建时间:{18},新更新时间:{29}", 
					cfg.getMethodType(), cfg.getMethodName(), cfg.getMethodImg(), cfg.getParamName1(), cfg.getParamName2(), cfg.getParamName3(), cfg.getParamName4(), 
					cfg.getParamName5(), cfg.getParamName6(), cfg.getParamName7(), cfg.getParamName8(), cfg.getParamName9(), cfg.getParamName10(), cfg.getParamName11(), cfg.getParamName12(), 
					cfg.getParamName13(), cfg.getParamName14(), cfg.getParamName15(), cfg.getCreateTime(), cfg.getUpdateTime());
						
			this.saveLog(sec, userNameLogin, log, Constants.LOG_CATEGORY_C2C);

		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.setViewName("c2c_payment_method_config_update");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.setViewName("c2c_payment_method_config_update");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	/**
	 * C2C支付方式模板 多语言配置 页面
	 */
	@RequestMapping(action + "toUpdateLanguage.action")
	public ModelAndView toUpdateLanguage(HttpServletRequest request) {
		String id = request.getParameter("id");
		ModelAndView modelAndView = new ModelAndView();

		try {
			C2cPaymentMethodConfig cfg = this.c2cPaymentMethodConfigService.get(id);
			if (null == cfg) {
				throw new BusinessException("支付方式模板不存在");
			}
			
			Map<String, String> pmtMap = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");
			
			modelAndView.addObject("methodTypeMap", pmtMap);
			modelAndView.addObject("methodTypeIntro", this.c2cAdvertService.getMethodTypeIntro());
			modelAndView.addObject("languageIntro", this.c2cAdvertService.getLanguageIntro());
			
			modelAndView.addObject("id", id);
			modelAndView.addObject("method_type", cfg.getMethodType());
			modelAndView.addObject("method_name", cfg.getMethodName());
			modelAndView.addObject("method_img", cfg.getMethodImg());
			modelAndView.addObject("param_name1", cfg.getParamName1());
			modelAndView.addObject("param_name2", cfg.getParamName2());
			modelAndView.addObject("param_name3", cfg.getParamName3());
			modelAndView.addObject("param_name4", cfg.getParamName4());
			modelAndView.addObject("param_name5", cfg.getParamName5());
			modelAndView.addObject("param_name6", cfg.getParamName6());
			modelAndView.addObject("param_name7", cfg.getParamName7());
			modelAndView.addObject("param_name8", cfg.getParamName8());
			modelAndView.addObject("param_name9", cfg.getParamName9());
			modelAndView.addObject("param_name10", cfg.getParamName10());
			modelAndView.addObject("param_name11", cfg.getParamName11());
			modelAndView.addObject("param_name12", cfg.getParamName12());
			modelAndView.addObject("param_name13", cfg.getParamName13());
			modelAndView.addObject("param_name14", cfg.getParamName14());
			modelAndView.addObject("param_name15", cfg.getParamName15());
			
			String methodTypeName = pmtMap.get(String.valueOf(cfg.getMethodType()));
			modelAndView.addObject("method_type_name", methodTypeName);
			modelAndView.addObject("method_type_lang_trans", StringUtils.isNotEmpty(methodTypeName) ? this.c2cTranslateService.getTranslate(methodTypeName) : "");
			modelAndView.addObject("method_name_lang_trans", StringUtils.isNotEmpty(cfg.getMethodName()) ? this.c2cTranslateService.getTranslate(cfg.getMethodName()) : "");
			modelAndView.addObject("param_name1_lang_trans", StringUtils.isNotEmpty(cfg.getParamName1()) ? this.c2cTranslateService.getTranslate(cfg.getParamName1()) : "");
			modelAndView.addObject("param_name2_lang_trans", StringUtils.isNotEmpty(cfg.getParamName2()) ? this.c2cTranslateService.getTranslate(cfg.getParamName2()) : "");
			modelAndView.addObject("param_name3_lang_trans", StringUtils.isNotEmpty(cfg.getParamName3()) ? this.c2cTranslateService.getTranslate(cfg.getParamName3()) : "");
			modelAndView.addObject("param_name4_lang_trans", StringUtils.isNotEmpty(cfg.getParamName4()) ? this.c2cTranslateService.getTranslate(cfg.getParamName4()) : "");
			modelAndView.addObject("param_name5_lang_trans", StringUtils.isNotEmpty(cfg.getParamName5()) ? this.c2cTranslateService.getTranslate(cfg.getParamName5()) : "");
			modelAndView.addObject("param_name6_lang_trans", StringUtils.isNotEmpty(cfg.getParamName6()) ? this.c2cTranslateService.getTranslate(cfg.getParamName6()) : "");
			modelAndView.addObject("param_name7_lang_trans", StringUtils.isNotEmpty(cfg.getParamName7()) ? this.c2cTranslateService.getTranslate(cfg.getParamName7()) : "");
			modelAndView.addObject("param_name8_lang_trans", StringUtils.isNotEmpty(cfg.getParamName8()) ? this.c2cTranslateService.getTranslate(cfg.getParamName8()) : "");
			modelAndView.addObject("param_name9_lang_trans", StringUtils.isNotEmpty(cfg.getParamName9()) ? this.c2cTranslateService.getTranslate(cfg.getParamName9()) : "");
			modelAndView.addObject("param_name10_lang_trans", StringUtils.isNotEmpty(cfg.getParamName10()) ? this.c2cTranslateService.getTranslate(cfg.getParamName10()) : "");
			modelAndView.addObject("param_name11_lang_trans", StringUtils.isNotEmpty(cfg.getParamName11()) ? this.c2cTranslateService.getTranslate(cfg.getParamName11()) : "");
			modelAndView.addObject("param_name12_lang_trans", StringUtils.isNotEmpty(cfg.getParamName12()) ? this.c2cTranslateService.getTranslate(cfg.getParamName12()) : "");
			modelAndView.addObject("param_name13_lang_trans", StringUtils.isNotEmpty(cfg.getParamName13()) ? this.c2cTranslateService.getTranslate(cfg.getParamName13()) : "");
			modelAndView.addObject("param_name14_lang_trans", StringUtils.isNotEmpty(cfg.getParamName14()) ? this.c2cTranslateService.getTranslate(cfg.getParamName14()) : "");
			modelAndView.addObject("param_name15_lang_trans", StringUtils.isNotEmpty(cfg.getParamName15()) ? this.c2cTranslateService.getTranslate(cfg.getParamName15()) : "");
			
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
		
		modelAndView.setViewName("c2c_payment_method_config_lang");
		return modelAndView;
	}

	/**
	 * C2C支付方式类型 多语言配置 页面
	 */
	@RequestMapping(action + "pmtLang.action")
	public ModelAndView pmtLang(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();

		try {
			List<Map<String, String>> retList = new ArrayList<Map<String,String>>();
			Map<String, String> pmtMap = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");
			for (String typeId : pmtMap.keySet()) {
				Map<String, String> map = new HashMap<String, String>();
				String name = pmtMap.get(typeId);
				if (null != name) {
					map.put("typeId", typeId);
					map.put("trans_typeId", "trans_" + typeId);
					map.put("name", name);					
					map.put("trans", StringUtils.isNotEmpty(name) ? this.c2cTranslateService.getTranslate(name) : "");
				} else {
					map.put("typeId", typeId);
					map.put("trans_typeId", "trans_" + typeId);
					map.put("name", "");					
					map.put("trans", "");
				}
				
				retList.add(map);
			}

			modelAndView.addObject("methodTypeList", retList);
			modelAndView.addObject("methodTypeIntro", this.c2cAdvertService.getMethodTypeIntro());
			modelAndView.addObject("languageIntro", this.c2cAdvertService.getLanguageIntro());
			
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
		
		modelAndView.setViewName("c2c_payment_method_config_pmt");
		return modelAndView;
	}

	/**
	 * 删除 C2C支付方式模板
	 */
	@RequestMapping(action + "delete.action")
	public ModelAndView delete(HttpServletRequest request) {
		String id = request.getParameter("id");
		String login_safeword = request.getParameter("login_safeword");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");
		
		try {
						
			String userNameLogin = this.getUsername_login();
			
			SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
			this.checkLoginSafeword(sec, userNameLogin, login_safeword);
			
			C2cPaymentMethodConfig cfg = this.c2cPaymentMethodConfigService.get(id);
			
			String log = MessageFormat.format("ip:" + this.getIp() 
				+ ",管理员删除支付方式模板,id:{0},支付方式类型:{1},支付方式名称:{2},支付方式图片:{3},参数名1:{4},参数名2:{5},参数名3:{6},参数名4:{7},参数名5:{8},"
				+ "参数名6:{9},参数名7:{10},参数名8:{11},参数名9:{12},参数名10:{13},参数名11:{14},参数名12:{15},参数名13:{16},参数名14:{17},参数名15:{18},创建时间:{19},更新时间:{20}",
				cfg.getId(), cfg.getMethodType(), cfg.getMethodName(), cfg.getMethodImg(), cfg.getParamName1(), cfg.getParamName2(), cfg.getParamName3(), cfg.getParamName4(), 
				cfg.getParamName5(), cfg.getParamName6(), cfg.getParamName7(), cfg.getParamName8(), cfg.getParamName9(), cfg.getParamName10(), cfg.getParamName11(), cfg.getParamName12(), 
				cfg.getParamName13(), cfg.getParamName14(), cfg.getParamName15(), cfg.getCreateTime(), cfg.getUpdateTime());
			
			this.c2cPaymentMethodConfigService.delete(id);
			
			this.saveLog(sec, userNameLogin, log, Constants.LOG_CATEGORY_C2C);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "程序错误");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
	}
	
	/**
	 * 添加或修改 支付方式模板 多语言
	 */
	@RequestMapping(action + "saveTranslate.action")
	public String saveTranslate(HttpServletRequest request) {
		String content_name = request.getParameter("content_name");
		String content = request.getParameter("content");			
		String lang_trans = request.getParameter("lang_trans");
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			if (StringUtils.isEmptyString(content_name)) {
				throw new BusinessException("内容名称不能为空");
			}			
			if (StringUtils.isEmptyString(content)) {
				throw new BusinessException("要翻译的内容不能为空");
			}			
			if (StringUtils.isEmptyString(lang_trans)) {
				throw new BusinessException("翻译不能为空");
			}
			
			String userNameLogin = this.getUsername_login();			
			SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
			
			this.c2cTranslateService.saveTranslate(content, lang_trans);
			
			String log = MessageFormat.format("ip:" + this.getIp() + ",管理员添加或修改支付方式模板多语言,内容名称{0},要翻译的内容:{1},翻译:{2}", content_name, content, lang_trans);
			this.saveLog(sec, userNameLogin, log, Constants.LOG_CATEGORY_C2C);
			
			resultMap.put("content_name", content_name);
			resultMap.put("content", content);
			resultMap.put("lang_trans", lang_trans);
			
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
	
	/**
	 * 添加或修改 支付方式类型多语言
	 */
	@RequestMapping(action + "savePmtTranslate.action")
	public String savePmtTranslate(HttpServletRequest request) {
		String name = request.getParameter("name");
		String trans = request.getParameter("trans");
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			if (StringUtils.isEmptyString(name)) {
				throw new BusinessException("要翻译的内容不能为空");
			}			
			if (StringUtils.isEmptyString(trans)) {
				throw new BusinessException("翻译不能为空");
			}
			
			String userNameLogin = this.getUsername_login();			
			SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
			
			this.c2cTranslateService.saveTranslate(name, trans);
			
			String log = MessageFormat.format("ip:" + this.getIp() + ",管理员添加或修改支付方式类型多语言,要翻译的内容:{0},翻译:{1}", name, trans);
			this.saveLog(sec, userNameLogin, log, Constants.LOG_CATEGORY_C2C);
			
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

	public String verif(String method_type, String method_name, String method_img, String param_name1, String login_safeword) {
		if (StringUtils.isNullOrEmpty(method_type)) {
			return "支付方式类型必填";
		}
		Map<String, String> pmtMap = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");
		if (!pmtMap.keySet().contains(method_type)) {
			return "支付方式类型错误";
		}
		
		if (StringUtils.isNullOrEmpty(method_name)) {
			return "支付方式名必填";
		}
		
		if (StringUtils.isNullOrEmpty(method_img)) {
			return "支付方式图片必填";
		}
		
		if (StringUtils.isNullOrEmpty(param_name1)) {
			return "参数名1必填";
		}
		
		if (StringUtils.isNullOrEmpty(login_safeword)) {
			return "请填写资金密码";
		}
		
		return "";
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
