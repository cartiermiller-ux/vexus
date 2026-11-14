package project.web.admin;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import project.c2c.AdminC2cPaymentMethodService;
import project.c2c.C2cAdvertService;
import project.c2c.C2cPaymentMethod;
import project.c2c.C2cPaymentMethodConfig;
import project.c2c.C2cPaymentMethodConfigService;
import project.c2c.C2cPaymentMethodService;
import project.log.LogService;
import project.party.PartyService;
import project.party.model.Party;
import project.syspara.SysparaService;
import security.Role;
import security.SecUser;
import security.internal.SecUserService;

/**
 * C2C支付方式
 */
@RestController
public class AdminC2cPaymentMethodController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminC2cPaymentMethodController.class);

	@Autowired
	private AdminC2cPaymentMethodService adminC2cPaymentMethodService;
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
	private SysparaService sysparaService;
	@Autowired
	private C2cPaymentMethodService c2cPaymentMethodService;
	@Autowired
	private C2cAdvertService c2cAdvertService;

	private final String action = "normal/adminC2cPaymentMethodAction!";
	
	/**
	 * 获取 支付方式 列表
	 */
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String pageNoStr = request.getParameter("pageNo");
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		String user_code_para = request.getParameter("user_code_para");
		String method_type_para = request.getParameter("method_type_para");
		String method_name_para = request.getParameter("method_name_para");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("c2c_payment_method_list");
		
		Map<String, String> pmtMap = this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type");

		int pageNo=1;
		Page page=null;
		int pageSize=20;
		try {
			pageNo=checkAndSetPageNo(pageNoStr);
			Integer method_type_int = null;
			if (!StringUtils.isEmptyString(method_type_para)) {
				method_type_int = Integer.valueOf(method_type_para);
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
			
			page = this.adminC2cPaymentMethodService.pagedQuery(pageNo, pageSize, user_code_para, method_type_int, method_name_para, secUuid);

			List<Map<String, Object>> list = (List<Map<String, Object>>) page.getElements();
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> map = list.get(i);
				if (null == map.get("method_type")) {
					map.put("method_type_name", "");
				} else {
					String methodType = map.get("method_type").toString();
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
		modelAndView.addObject("user_code_para", user_code_para);
		modelAndView.addObject("method_type_para", method_type_para);
		modelAndView.addObject("method_name_para", method_name_para);
		modelAndView.addObject("methodTypeMap", pmtMap);
		return modelAndView;
	}
	
	/**
	 * 新增 支付方式 页面
	 */
	@RequestMapping(action + "toAdd.action")
	public ModelAndView toAdd(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		
		try {
			modelAndView.addObject("methodConfigMap", this.c2cPaymentMethodConfigService.getMethodConfigMap());
			modelAndView.addObject("methodTypeMap", this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type"));

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

		modelAndView.setViewName("c2c_payment_method_add");
		return modelAndView;
	}

	/**
	 * 新增 支付方式
	 */
	@RequestMapping(action + "add.action")
	public ModelAndView add(HttpServletRequest request) {
		String user_code = request.getParameter("user_code");			
		String method_config_id = request.getParameter("method_config_id");		
		String real_name = request.getParameter("real_name");		
		String param_value1 = request.getParameter("param_value1");		
		String param_value2 = request.getParameter("param_value2");		
		String param_value3 = request.getParameter("param_value3");		
		String param_value4 = request.getParameter("param_value4");		
		String param_value5 = request.getParameter("param_value5");		
		String param_value6 = request.getParameter("param_value6");		
		String param_value7 = request.getParameter("param_value7");		
		String param_value8 = request.getParameter("param_value8");		
		String param_value9 = request.getParameter("param_value9");		
		String param_value10 = request.getParameter("param_value10");
		String param_value11 = request.getParameter("param_value11");
		String param_value12 = request.getParameter("param_value12");
		String param_value13 = request.getParameter("param_value13");
		String param_value14 = request.getParameter("param_value14");
		String param_value15 = request.getParameter("param_value15");
		String qrcode_img = request.getParameter("qrcode_img");	
		String remark = request.getParameter("remark");
		String login_safeword = request.getParameter("login_safeword");
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("user_code", user_code);
		modelAndView.addObject("method_config_id", method_config_id);
		modelAndView.addObject("real_name", real_name);
		modelAndView.addObject("param_value1", param_value1);
		modelAndView.addObject("param_value2", param_value2);
		modelAndView.addObject("param_value3", param_value3);
		modelAndView.addObject("param_value4", param_value4);
		modelAndView.addObject("param_value5", param_value5);
		modelAndView.addObject("param_value6", param_value6);
		modelAndView.addObject("param_value7", param_value7);
		modelAndView.addObject("param_value8", param_value8);
		modelAndView.addObject("param_value9", param_value9);
		modelAndView.addObject("param_value10", param_value10);
		modelAndView.addObject("param_value11", param_value11);
		modelAndView.addObject("param_value12", param_value12);
		modelAndView.addObject("param_value13", param_value13);
		modelAndView.addObject("param_value14", param_value14);
		modelAndView.addObject("param_value15", param_value15);
		modelAndView.addObject("qrcode_img", qrcode_img);
		modelAndView.addObject("remark", remark);
		
		try {
			
			modelAndView.addObject("methodConfigMap", this.c2cPaymentMethodConfigService.getMethodConfigMap());
			modelAndView.addObject("methodTypeMap", this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type"));
			
			String error = this.verifAdd(user_code, method_config_id, real_name, param_value1, login_safeword);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			String userNameLogin = this.getUsername_login();
			
			SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
			this.checkLoginSafeword(sec, userNameLogin, login_safeword);
			
			Party party = this.partyService.findPartyByUsercode(user_code);
			if (null == party) {
				throw new BusinessException("用户不存在");
			}
			
			C2cPaymentMethodConfig methodConfig = this.c2cPaymentMethodConfigService.get(method_config_id);
			if (null == methodConfig) {
				throw new BusinessException("支付方式模板不存在");
			}
			
			// C2C用户和承兑商添加支付方式最大数量			
			Map<String, C2cPaymentMethod> methodMap = this.c2cPaymentMethodService.getByPartyId(party.getId().toString());
			Object obj = this.sysparaService.find("c2c_payment_method_count_max");
			if (null != obj) {
				if (methodMap.size() >= Integer.valueOf(this.sysparaService.find("c2c_payment_method_count_max").getValue()).intValue()) {
					throw new BusinessException("支付方式数量已达上限");
				}
			}
			
			C2cPaymentMethod method = new C2cPaymentMethod();
			method.setId(ApplicationUtil.getCurrentTimeUUID());
			method.setPartyId(party.getId().toString());
			method.setMethodConfigId(methodConfig.getId().toString());
			method.setMethodType(methodConfig.getMethodType());
			method.setMethodName(methodConfig.getMethodName());
			method.setMethodImg(methodConfig.getMethodImg());
			method.setRealName(real_name);
			method.setParamName1(methodConfig.getParamName1());
			method.setParamValue1(param_value1);
			method.setParamName2(methodConfig.getParamName2());
			method.setParamValue2(param_value2);
			method.setParamName3(methodConfig.getParamName3());
			method.setParamValue3(param_value3);
			method.setParamName4(methodConfig.getParamName4());
			method.setParamValue4(param_value4);
			method.setParamName5(methodConfig.getParamName5());
			method.setParamValue5(param_value5);
			method.setParamName6(methodConfig.getParamName6());
			method.setParamValue6(param_value6);
			method.setParamName7(methodConfig.getParamName7());
			method.setParamValue7(param_value7);
			method.setParamName8(methodConfig.getParamName8());
			method.setParamValue8(param_value8);
			method.setParamName9(methodConfig.getParamName9());
			method.setParamValue9(param_value9);
			method.setParamName10(methodConfig.getParamName10());
			method.setParamValue10(param_value10);
			method.setParamName10(methodConfig.getParamName11());
			method.setParamValue10(param_value11);
			method.setParamName10(methodConfig.getParamName12());
			method.setParamValue10(param_value12);
			method.setParamName10(methodConfig.getParamName13());
			method.setParamValue10(param_value13);
			method.setParamName10(methodConfig.getParamName14());
			method.setParamValue10(param_value14);
			method.setParamName10(methodConfig.getParamName15());
			method.setParamValue10(param_value15);
			method.setQrcode(qrcode_img);
			method.setRemark(remark);
			method.setCreateTime(new Date());
			method.setUpdateTime(new Date());
			
			this.adminC2cPaymentMethodService.save(method);
			
			String log = MessageFormat.format("ip:" + this.getIp() 
				+ ",管理员新增支付方式,id:{0},用户PARTY_ID:{1},支付方式模板id:{2},支付方式类型:{3},支付方式名称:{4},支付方式图片:{5},真实姓名:{6},"
				+ "参数名1:{7},参数值1:{8},参数名2:{9},参数值2:{10},参数名3:{11},参数值3:{12},参数名4:{13},参数值4:{14},参数名5:{15},参数值5:{16},"
				+ "参数名6:{17},参数值6:{18},参数名7:{19},参数值7:{20},参数名8:{21},参数值8:{22},参数名9:{23},参数值9:{24},参数名10:{25},参数值10:{26},"
				+ "参数名11:{27},参数值11:{28},参数名12:{29},参数值12:{30},参数名13:{31},参数值13:{32},参数名14:{33},参数值14:{34},参数名15:{35},参数值15:{36},"
				+ "支付二维码:{37},备注:{38},创建时间:{39},更新时间:{40}", 
				method.getId(), method.getPartyId(), method.getMethodConfigId(), method.getMethodType(), method.getMethodName(), method.getMethodImg(), method.getRealName(), 
				method.getParamName1(), method.getParamValue1(), method.getParamName2(), method.getParamValue2(), method.getParamName3(), method.getParamValue3(), method.getParamName4(), method.getParamValue4(), method.getParamName5(), method.getParamValue5(), 
				method.getParamName6(), method.getParamValue6(), method.getParamName7(), method.getParamValue7(), method.getParamName8(), method.getParamValue8(), method.getParamName9(), method.getParamValue9(), method.getParamName10(), method.getParamValue10(), 
				method.getParamName11(), method.getParamValue11(), method.getParamName12(), method.getParamValue12(), method.getParamName13(), method.getParamValue13(), method.getParamName14(), method.getParamValue14(), method.getParamName15(), method.getParamValue15(), 
				method.getQrcode(), method.getRemark(), method.getCreateTime(), method.getUpdateTime());
			
			this.saveLog(sec, userNameLogin, log, Constants.LOG_CATEGORY_C2C);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.setViewName("c2c_payment_method_add");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.setViewName("c2c_payment_method_add");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	/**
	 * 修改 支付方式 页面
	 */
	@RequestMapping(action + "toUpdate.action")
	public ModelAndView toUpdate(HttpServletRequest request) {
		String id = request.getParameter("id");
		ModelAndView modelAndView = new ModelAndView();

		try {
			C2cPaymentMethod method = this.adminC2cPaymentMethodService.findById(id);
			if (null == method) {
				throw new BusinessException("支付方式不存在");
			}
			
			Party party = this.partyService.cachePartyBy(method.getPartyId(), false);
			if (null == party) {
				throw new BusinessException("用户不存在");
			}

			modelAndView.addObject("id", id);
			modelAndView.addObject("user_code", party.getUsercode());
			modelAndView.addObject("user_name", party.getUsername());
			modelAndView.addObject("method_name", method.getMethodName());
			modelAndView.addObject("method_img", method.getMethodImg());
			modelAndView.addObject("method_type", method.getMethodType());
			modelAndView.addObject("real_name", method.getRealName());
			modelAndView.addObject("param_name1", method.getParamName1());
			modelAndView.addObject("param_value1", method.getParamValue1());
			modelAndView.addObject("param_name2", method.getParamName2());
			modelAndView.addObject("param_value2", method.getParamValue2());
			modelAndView.addObject("param_name3", method.getParamName3());
			modelAndView.addObject("param_value3", method.getParamValue3());
			modelAndView.addObject("param_name4", method.getParamName4());
			modelAndView.addObject("param_value4", method.getParamValue4());
			modelAndView.addObject("param_name5", method.getParamName5());
			modelAndView.addObject("param_value5", method.getParamValue5());
			modelAndView.addObject("param_name6", method.getParamName6());
			modelAndView.addObject("param_value6", method.getParamValue6());
			modelAndView.addObject("param_name7", method.getParamName7());
			modelAndView.addObject("param_value7", method.getParamValue7());
			modelAndView.addObject("param_name8", method.getParamName8());
			modelAndView.addObject("param_value8", method.getParamValue8());
			modelAndView.addObject("param_name9", method.getParamName9());
			modelAndView.addObject("param_value9", method.getParamValue9());
			modelAndView.addObject("param_name10", method.getParamName10());
			modelAndView.addObject("param_value10", method.getParamValue10());
			modelAndView.addObject("param_name11", method.getParamName11());
			modelAndView.addObject("param_value11", method.getParamValue11());
			modelAndView.addObject("param_name12", method.getParamName12());
			modelAndView.addObject("param_value12", method.getParamValue12());
			modelAndView.addObject("param_name13", method.getParamName13());
			modelAndView.addObject("param_value13", method.getParamValue13());
			modelAndView.addObject("param_name14", method.getParamName14());
			modelAndView.addObject("param_value14", method.getParamValue14());
			modelAndView.addObject("param_name15", method.getParamName15());
			modelAndView.addObject("param_value15", method.getParamValue15());
			modelAndView.addObject("qrcode_img", method.getQrcode());
			modelAndView.addObject("remark", method.getRemark());
			modelAndView.addObject("methodTypeMap", this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type"));

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
		
		modelAndView.setViewName("c2c_payment_method_update");
		return modelAndView;
	}
	
	/**
	 * 修改 支付方式
	 */
	@RequestMapping(action + "update.action")
	public ModelAndView update(HttpServletRequest request) {
		String id = request.getParameter("id");
		String user_code = request.getParameter("user_code");
		String user_name = request.getParameter("user_name");
		String method_name = request.getParameter("method_name");		
		String method_img = request.getParameter("method_img");			
		String method_type = request.getParameter("method_type");			
		String real_name = request.getParameter("real_name");		
		String param_name1 = request.getParameter("param_name1");		
		String param_value1 = request.getParameter("param_value1");
		String param_name2 = request.getParameter("param_name2");		
		String param_value2 = request.getParameter("param_value2");
		String param_name3 = request.getParameter("param_name3");		
		String param_value3 = request.getParameter("param_value3");
		String param_name4 = request.getParameter("param_name4");		
		String param_value4 = request.getParameter("param_value4");
		String param_name5 = request.getParameter("param_name5");		
		String param_value5 = request.getParameter("param_value5");
		String param_name6 = request.getParameter("param_name6");		
		String param_value6 = request.getParameter("param_value6");
		String param_name7 = request.getParameter("param_name7");		
		String param_value7 = request.getParameter("param_value7");
		String param_name8 = request.getParameter("param_name8");		
		String param_value8 = request.getParameter("param_value8");
		String param_name9 = request.getParameter("param_name9");		
		String param_value9 = request.getParameter("param_value9");
		String param_name10 = request.getParameter("param_name10");		
		String param_value10 = request.getParameter("param_value10");
		String param_name11 = request.getParameter("param_name11");		
		String param_value11 = request.getParameter("param_value11");
		String param_name12 = request.getParameter("param_name12");		
		String param_value12 = request.getParameter("param_value12");
		String param_name13 = request.getParameter("param_name13");		
		String param_value13 = request.getParameter("param_value13");
		String param_name14 = request.getParameter("param_name14");		
		String param_value14 = request.getParameter("param_value14");
		String param_name15 = request.getParameter("param_name15");		
		String param_value15 = request.getParameter("param_value15");	
		String qrcode_img = request.getParameter("qrcode_img");	
		String remark = request.getParameter("remark");
		String login_safeword = request.getParameter("login_safeword");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("id", id);
		modelAndView.addObject("user_code", user_code);
		modelAndView.addObject("user_name", user_name);
		modelAndView.addObject("method_name", method_name);
		modelAndView.addObject("method_img", method_img);
		modelAndView.addObject("method_type", method_type);
		modelAndView.addObject("real_name", real_name);
		modelAndView.addObject("param_name1", param_name1);
		modelAndView.addObject("param_value1", param_value1);
		modelAndView.addObject("param_name2", param_name2);
		modelAndView.addObject("param_value2", param_value2);
		modelAndView.addObject("param_name3", param_name3);
		modelAndView.addObject("param_value3", param_value3);
		modelAndView.addObject("param_name4", param_name4);
		modelAndView.addObject("param_value4", param_value4);
		modelAndView.addObject("param_name5", param_name5);
		modelAndView.addObject("param_value5", param_value5);
		modelAndView.addObject("param_name6", param_name6);
		modelAndView.addObject("param_value6", param_value6);
		modelAndView.addObject("param_name7", param_name7);
		modelAndView.addObject("param_value7", param_value7);
		modelAndView.addObject("param_name8", param_name8);
		modelAndView.addObject("param_value8", param_value8);
		modelAndView.addObject("param_name9", param_name9);
		modelAndView.addObject("param_value9", param_value9);
		modelAndView.addObject("param_name10", param_name10);
		modelAndView.addObject("param_value10", param_value10);
		modelAndView.addObject("param_name11", param_name11);
		modelAndView.addObject("param_value11", param_value11);
		modelAndView.addObject("param_name12", param_name12);
		modelAndView.addObject("param_value12", param_value12);
		modelAndView.addObject("param_name13", param_name13);
		modelAndView.addObject("param_value13", param_value13);
		modelAndView.addObject("param_name14", param_name14);
		modelAndView.addObject("param_value14", param_value14);
		modelAndView.addObject("param_name15", param_name15);
		modelAndView.addObject("param_value15", param_value15);
		modelAndView.addObject("qrcode_img", qrcode_img);
		modelAndView.addObject("remark", remark);

		try {
			modelAndView.addObject("methodTypeMap", this.c2cAdvertService.getC2cSyspara("c2c_payment_method_type"));

			String error = this.verifUpdate(real_name, param_value1, login_safeword);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}
			
			String userNameLogin = this.getUsername_login();
			SecUser sec = this.secUserService.findUserByLoginName(userNameLogin);
			this.checkLoginSafeword(sec, userNameLogin, login_safeword);
			
			C2cPaymentMethod method = this.adminC2cPaymentMethodService.findById(id);
			if (null == method) {
				throw new BusinessException("支付方式不存在");
			}
			
			String log = MessageFormat.format("ip:" + this.getIp() 
				+ ",管理员修改支付方式,id:{0},原用户PARTY_ID:{1},原支付方式模板:{2},原支付方式类型:{3},原支付方式名称:{4},原支付方式图片:{5},原真实姓名:{6},"
				+ "原参数名1:{7},原参数值1:{8},原参数名2:{9},原参数值2:{10},原参数名3:{11},原参数值3:{12},原参数名4:{13},原参数值4:{14},原参数名5:{15},原参数值5:{16},"
				+ "原参数名6:{17},原参数值6:{18},原参数名7:{19},原参数值7:{20},原参数名8:{21},原参数值8:{22},原参数名9:{23},原参数值9:{24},原参数名10:{25},原参数值10:{26},"
				+ "原参数名11:{27},原参数值11:{28},原参数名12:{29},原参数值12:{30},原参数名13:{31},原参数值13:{32},原参数名14:{33},原参数值14:{34},原参数名15:{35},原参数值15:{36},"
				+ "原支付二维码:{37},原备注:{38},原创建时间:{39},原更新时间:{40}", 
				method.getId(), method.getPartyId(), method.getMethodConfigId(), method.getMethodType(), method.getMethodName(), method.getMethodImg(), method.getRealName(), 
				method.getParamName1(), method.getParamValue1(), method.getParamName2(), method.getParamValue2(), method.getParamName3(), method.getParamValue3(), method.getParamName4(), method.getParamValue4(), method.getParamName5(), method.getParamValue5(), 
				method.getParamName6(), method.getParamValue6(), method.getParamName7(), method.getParamValue7(), method.getParamName8(), method.getParamValue8(), method.getParamName9(), method.getParamValue9(), method.getParamName10(), method.getParamValue10(),  
				method.getParamName11(), method.getParamValue11(), method.getParamName12(), method.getParamValue12(), method.getParamName13(), method.getParamValue13(), method.getParamName14(), method.getParamValue14(), method.getParamName15(), method.getParamValue15(),
				method.getQrcode(), method.getRemark(), method.getCreateTime(), method.getUpdateTime());
			
			method.setRealName(real_name);
			method.setParamValue1(param_value1);
			method.setParamValue2(param_value2);
			method.setParamValue3(param_value3);
			method.setParamValue4(param_value4);
			method.setParamValue5(param_value5);
			method.setParamValue6(param_value6);
			method.setParamValue7(param_value7);
			method.setParamValue8(param_value8);
			method.setParamValue9(param_value9);
			method.setParamValue10(param_value10);
			method.setParamValue10(param_value11);
			method.setParamValue10(param_value12);
			method.setParamValue10(param_value13);
			method.setParamValue10(param_value14);
			method.setParamValue10(param_value15);
			method.setQrcode(qrcode_img);
			method.setRemark(remark);
			method.setUpdateTime(new Date());

			this.adminC2cPaymentMethodService.update(method);
			
			log += MessageFormat.format(",id:{0},新用户PARTY_ID:{1},新支付方式模板:{2},新支付方式类型:{3},新支付方式名称:{4},新支付方式图片:{5},新真实姓名:{6},"
				+ "新参数名1:{7},新参数值1:{8},新参数名2:{9},新参数值2:{10},新参数名3:{11},新参数值3:{12},新参数名4:{13},新参数值4:{14},新参数名5:{15},新参数值5:{16},"
				+ "新参数名6:{17},新参数值6:{18},新参数名7:{19},新参数值7:{20},新参数名8:{21},新参数值8:{22},新参数名9:{23},新参数值9:{24},新参数名10:{25},新参数值10:{26},"
				+ "新参数名11:{27},新参数值11:{28},新参数名12:{29},新参数值12:{30},新参数名13:{31},新参数值13:{32},新参数名14:{33},新参数值14:{34},新参数名15:{35},新参数值15:{36},"
				+ "新支付二维码:{37},新备注:{38},新创建时间:{39},新更新时间:{40}", 
				method.getId(), method.getPartyId(), method.getMethodConfigId(), method.getMethodType(), method.getMethodName(), method.getMethodImg(), method.getRealName(), 
				method.getParamName1(), method.getParamValue1(), method.getParamName2(), method.getParamValue2(), method.getParamName3(), method.getParamValue3(), method.getParamName4(), method.getParamValue4(), method.getParamName5(), method.getParamValue5(), 
				method.getParamName6(), method.getParamValue6(), method.getParamName7(), method.getParamValue7(), method.getParamName8(), method.getParamValue8(), method.getParamName9(), method.getParamValue9(), method.getParamName10(), method.getParamValue10(), 
				method.getParamName11(), method.getParamValue11(), method.getParamName12(), method.getParamValue12(), method.getParamName13(), method.getParamValue13(), method.getParamName14(), method.getParamValue14(), method.getParamName15(), method.getParamValue15(), 
				method.getQrcode(), method.getRemark(), method.getCreateTime(), method.getUpdateTime());
			
			this.saveLog(sec, userNameLogin, log, Constants.LOG_CATEGORY_C2C);

		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.setViewName("c2c_payment_method_update");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.setViewName("c2c_payment_method_update");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	/**
	 * 删除 支付方式
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
			
			C2cPaymentMethod method = this.adminC2cPaymentMethodService.findById(id);
			if (null == method) {
				throw new BusinessException("支付方式不存在");
			}
			
			String log = MessageFormat.format("ip:" + this.getIp() 
				+ ",管理员删除支付方式,id:{0},用户PARTY_ID:{1},支付方式模板:{2},支付方式类型:{3},支付方式名称:{4},支付方式图片:{5},真实姓名:{6},"
				+ "参数名1:{7},参数值1:{8},参数名2:{9},参数值2:{10},参数名3:{11},参数值3:{12},参数名4:{13},参数值4:{14},参数名5:{15},参数值5:{16},"
				+ "参数名6:{17},参数值6:{18},参数名7:{19},参数值7:{20},参数名8:{21},参数值8:{22},参数名9:{23},参数值9:{24},参数名10:{25},参数值10:{26},"
				+ "参数名6:{27},参数值6:{28},参数名7:{29},参数值7:{30},参数名8:{31},参数值8:{32},参数名9:{33},参数值9:{34},参数名10:{35},参数值10:{36},"
				+ "支付二维码:{37},备注:{38},创建时间:{39},更新时间:{40}", 
				method.getId(), method.getPartyId(), method.getMethodConfigId(), method.getMethodType(), method.getMethodName(), method.getMethodImg(), method.getRealName(), 
				method.getParamName1(), method.getParamValue1(), method.getParamName2(), method.getParamValue2(), method.getParamName3(), method.getParamValue3(), method.getParamName4(), method.getParamValue4(), method.getParamName5(), method.getParamValue5(), 
				method.getParamName6(), method.getParamValue6(), method.getParamName7(), method.getParamValue7(), method.getParamName8(), method.getParamValue8(), method.getParamName9(), method.getParamValue9(), method.getParamName10(), method.getParamValue10(), 
				method.getParamName11(), method.getParamValue11(), method.getParamName12(), method.getParamValue12(), method.getParamName13(), method.getParamValue13(), method.getParamName14(), method.getParamValue14(), method.getParamName15(), method.getParamValue15(), 
				method.getQrcode(), method.getRemark(), method.getCreateTime(), method.getUpdateTime());

			this.adminC2cPaymentMethodService.delete(id);
			
			this.saveLog(sec, userNameLogin, log, Constants.LOG_CATEGORY_C2C);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error("delete error ", t);
			modelAndView.addObject("error", "程序错误");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
	}

	/**
	 * 获取 支付方式模板
	 */
	@RequestMapping(action + "getPaymentMethodConfig.action")
	public String getPaymentMethodConfig(HttpServletRequest request) {
		String id = request.getParameter("method_config_id");
		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			resultMap.put("code", 200);
			C2cPaymentMethodConfig config = this.c2cPaymentMethodConfigService.get(id);
			if (null == config) {
				throw new BusinessException("支付方式模板不存在");
			}
			
			Map<String, Object> methodConfigMap = new HashMap<String, Object>();
			methodConfigMap.put("method_img", config.getMethodImg());
			methodConfigMap.put("method_type", config.getMethodType());
			methodConfigMap.put("param_name1", config.getParamName1());
			methodConfigMap.put("param_name2", config.getParamName2());
			methodConfigMap.put("param_name3", config.getParamName3());
			methodConfigMap.put("param_name4", config.getParamName4());
			methodConfigMap.put("param_name5", config.getParamName5());
			methodConfigMap.put("param_name6", config.getParamName6());
			methodConfigMap.put("param_name7", config.getParamName7());
			methodConfigMap.put("param_name8", config.getParamName8());
			methodConfigMap.put("param_name9", config.getParamName9());
			methodConfigMap.put("param_name10", config.getParamName10());
			methodConfigMap.put("param_name11", config.getParamName11());
			methodConfigMap.put("param_name12", config.getParamName12());
			methodConfigMap.put("param_name13", config.getParamName13());
			methodConfigMap.put("param_name14", config.getParamName14());
			methodConfigMap.put("param_name15", config.getParamName15());
			
			resultMap.put("methodConfigMap", methodConfigMap);
			
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

	public String verifAdd(String user_code, String method_config_id, String real_name, String param_value1, String login_safeword) {
		
		if (StringUtils.isNullOrEmpty(user_code)) {
			return "用户UID必填";
		}
		
		if (StringUtils.isNullOrEmpty(method_config_id)) {
			return "支付方式模板不正确";
		}
		
		if (StringUtils.isNullOrEmpty(real_name)) {
			return "真实姓名必填";
		}
		
		if (StringUtils.isNullOrEmpty(param_value1)) {
			return "参数值1必填";
		}
		
		if (StringUtils.isNullOrEmpty(login_safeword)) {
			return "资金密码错误";
		}
		
		return "";
	}

	public String verifUpdate(String real_name, String param_value1, String login_safeword) {
		
		if (StringUtils.isNullOrEmpty(real_name)) {
			return "真实姓名必填";
		}
		
		if (StringUtils.isNullOrEmpty(param_value1)) {
			return "参数值1必填";
		}
		
		if (StringUtils.isNullOrEmpty(login_safeword)) {
			return "资金密码错误";
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
