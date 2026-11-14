package systemuser.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.security.providers.encoding.PasswordEncoder;

import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.Constants;
import project.log.Log;
import project.log.LogService;
import project.onlinechat.OnlineChatMessageService;
import security.SecUser;
import security.internal.SecUserService;
import systemuser.AdminCustomerService;
import systemuser.AdminSystemUserService;
import systemuser.CustomerService;
import systemuser.model.Customer;

@SuppressWarnings("rawtypes")
public class AdminCustomerServiceImpl implements AdminCustomerService{
	
	protected LogService logService;
	
	private SecUserService secUserService;
	
	private PasswordEncoder passwordEncoder;
	
	private CustomerService customerService;
	
	private AdminSystemUserService adminSystemUserService;
	
	private OnlineChatMessageService onlineChatMessageService;
	
	@Override
	public Page pagedQuery(int pageNo,int pageSize ,String usernamePara) {
		if (pageNo <= 0) pageNo=1;
		Page page=new Page(pageNo,pageSize,Integer.MAX_VALUE);
		
		StringBuilder sqlBuilder = new StringBuilder("SELECT customer.UUID id,customer.USERNAME username,customer.ONLINE_STATE online_state,customer.LAST_CUSTOMER_TIME last_customer_time,");
		sqlBuilder.append("customer.LAST_ONLINE_TIME last_online_time,user.GOOGLE_AUTH_BIND google_auth_bind,user.REMARKS remarks,user.ENABLED enabled ");
		sqlBuilder.append("FROM T_CUSTOMER customer LEFT JOIN SCT_USER user ON user.USERNAME=customer.USERNAME WHERE 1=1 ");
		ArrayList<Object> whereParams=new ArrayList<Object>();
		
		if(StringUtils.isNotEmpty(usernamePara)) {
			sqlBuilder.append("AND customer.USERNAME LIKE ? ");
			whereParams.add("%"+usernamePara+"%");
		}
		
		sqlBuilder.append("ORDER BY customer.CREATE_TIME ASC LIMIT ?,?");
		whereParams.add(page.getFirstElementNumber());
		whereParams.add(pageSize);
		
		List<Map> list=ApplicationUtil.executeDQL(sqlBuilder.toString(),whereParams.toArray(new Object[whereParams.size()]),Map.class);
		page.setElements(list);
		return page;
	}

	@Override
	public void save(SecUser user,String operatorUsername,String loginSafeword,String code,String ip,String superGoogleAuthCode,String autoAnswer) {
		adminSystemUserService.save(user, operatorUsername, loginSafeword, code, ip, superGoogleAuthCode);
		
		Customer entity = new Customer();
		entity.setUsername(user.getUsername());
		entity.setOnline_state(0);
		entity.setCreate_time(new Date());
		entity.setAuto_answer(autoAnswer);
		
		customerService.save(entity);
	}
	
	public void updatePersonalAutoAnswer(String username,String loginSafeword,String ip,String autoAnswer) {
		checkLoginSafeword(username,loginSafeword);
		SecUser user = this.secUserService.findUserByLoginName(username);
		updateAutoAnswer(user,username,ip,autoAnswer);
	}
	
	public void updateAutoAnswer(SecUser user,String operatorUsername,String ip,String autoAnswer) {
		Customer customer = this.customerService.cacheByUsername(user.getUsername());
		String sourceAutoAnswer = customer.getAuto_answer();
		customer.setAuto_answer(autoAnswer);
		customerService.update(customer, false);
		saveLog(user,operatorUsername,"ip:"+ip+"修改了客服["+user.getUsername()+"]自动回复,原自动回复["+sourceAutoAnswer+"],新自动回复["+autoAnswer+"]");
	}
	
	/**
	 * 管理员强制下线
	 * @param username
	 * @param operatorUsername
	 * @param loginSafeword
	 * @param ip
	 */
	public void forceOffline(String username,String operatorUsername,String loginSafeword,String ip) {
		checkLoginSafeword(operatorUsername,loginSafeword);
		offline(username); 
		SecUser user = this.secUserService.findUserByLoginName(username);
		saveLog(user,operatorUsername,"ip:"+ip+"管理员强制下线客服["+username+"]");
	}

	public void offline(String username) {
		Customer customer = customerService.cacheByUsername(username);
		if(customer==null) throw new BusinessException("客服不存在");
		
		customer.setOnline_state(0);
		customer.setLast_offline_time(new Date());
		
		customerService.update(customer,false);
	}
	
	public void online(String username) {
		Customer customer = customerService.cacheByUsername(username);
		if(customer==null) throw new BusinessException("客服不存在");
		
		customer.setOnline_state(1);
		customer.setLast_online_time(new Date());
		customerService.update(customer,false);
		
		onlineChatMessageService.updateNoAnwserUser(username);
	}

	/**
	 * 验证登录人资金密码
	 * @param operatorUsername
	 * @param loginSafeword
	 */
	private void checkLoginSafeword(String operatorUsername,String loginSafeword) {
		SecUser sec = this.secUserService.findUserByLoginName(operatorUsername);
		String sysSafeword = sec.getSafeword();
		String safeword_md5 = passwordEncoder.encodePassword(loginSafeword, operatorUsername);
		if (!safeword_md5.equals(sysSafeword)) throw new BusinessException("登录人资金密码错误");
	}
	
	public void saveLog(SecUser secUser, String operator,String context) {
		Log log = new Log();
		log.setCategory(Constants.LOG_CATEGORY_OPERATION);
		log.setOperator(operator);
		log.setUsername(secUser.getUsername());
		log.setPartyId(secUser.getPartyId());
		log.setLog(context);
		log.setCreateTime(new Date());
		
		logService.saveSync(log);
	}
	
	public void setAdminSystemUserService(AdminSystemUserService adminSystemUserService) {
		this.adminSystemUserService = adminSystemUserService;
	}

	public void setCustomerService(CustomerService customerService) {
		this.customerService = customerService;
	}

	public void setOnlineChatMessageService(OnlineChatMessageService onlineChatMessageService) {
		this.onlineChatMessageService = onlineChatMessageService;
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}
}
