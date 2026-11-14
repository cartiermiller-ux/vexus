package project.monitor.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.providers.encoding.PasswordEncoder;

import kernel.exception.BusinessException;
import kernel.util.Endecrypt;
import kernel.util.StringUtils;
import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.Constants;
import project.log.Log;
import project.log.LogService;
import project.monitor.AdminAutoMonitorAddressConfigService;
import project.monitor.AutoMonitorAddressConfigService;
import project.monitor.model.AutoMonitorAddressConfig;
import project.syspara.SysparaService;
import project.user.googleauth.GoogleAuthService;
import security.SecUser;
import security.internal.SecUserService;

public class AdminAutoMonitorAddressConfigServiceImpl implements AdminAutoMonitorAddressConfigService {
	protected PagedQueryDao pagedQueryDao;
	protected SysparaService sysparaService;
	protected SecUserService secUserService;
	protected PasswordEncoder passwordEncoder;
	protected LogService logService;
	protected GoogleAuthService googleAuthService;
	protected AutoMonitorAddressConfigService autoMonitorAddressConfigService;
	
	@Override
	public Page pagedQuery(int pageNo, int pageSize,String status,String address) {
		StringBuffer queryString = new StringBuffer(
				" SELECT addressConfig.UUID id,addressConfig.ADDRESS address,addressConfig.STATUS status,addressConfig.CREATE_TIME create_time,addressConfig.SORT_INDEX sort_index,addressConfig.APPROVE_NUM approve_num  ");
		queryString.append(" FROM T_AUTO_MONITOR_ADDRESS_CONFIG addressConfig WHERE 1 = 1 ");
		Map<String, Object> parameters = new HashMap<>();
		if (!StringUtils.isNullOrEmpty(status)) {
			queryString.append(" and  addressConfig.STATUS =:status ");
			parameters.put("status", status);
		}
		if (!StringUtils.isNullOrEmpty(address)) {
			queryString.append(" and  addressConfig.ADDRESS LIKE:address ");
			parameters.put("address","%"+address+"%");
		}
		queryString.append(" ORDER BY addressConfig.SORT_INDEX DESC,addressConfig.CREATE_TIME ASC ");
		Page page = this.pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		return page;
	}
	
	public void save(AutoMonitorAddressConfig addressConfig,String operatorUsername,String loginSafeword,String superGoogleAuthCode,String ip,String googleAuthCode,String key) {
		SecUser sec = this.secUserService.findUserByLoginName(operatorUsername);
		checkLoginSafeword(operatorUsername,loginSafeword);
		
		String private_key=addressConfig.getPrivate_key();
		Endecrypt endecrypt = new Endecrypt();
		String	private_key_desEncrypt1 = endecrypt.get3DESEncrypt(private_key,key.split("&")[0]);
		String	private_key_desEncrypt2 = endecrypt.get3DESEncrypt(private_key_desEncrypt1,key.split("&")[1]);
		addressConfig.setPrivate_key(private_key_desEncrypt2);
		
		addressConfig.setAddress(addressConfig.getAddress().toLowerCase());
		addressConfig = autoMonitorAddressConfigService.save(addressConfig);
		saveLog(sec,operatorUsername,"管理员新增被授权地址,地址["+addressConfig.getAddress()+"],ip["+ip+"]");;
		autoMonitorAddressConfigService.updateEnabledAddress(addressConfig);
	}
	public void updatePrivateKey(AutoMonitorAddressConfig addressConfig,String operatorUsername,String loginSafeword,String superGoogleAuthCode,String ip,String googleAuthCode,String key) {
		SecUser sec = this.secUserService.findUserByLoginName(operatorUsername);
		googleAuthService.checkGoogleAuthCode(sec,googleAuthCode);
		checkLoginSafeword(operatorUsername,loginSafeword);
		String private_key=addressConfig.getPrivate_key();
		Endecrypt endecrypt = new Endecrypt();
		String	private_key_desEncrypt1 = endecrypt.get3DESEncrypt(private_key,key.split("&")[0]);
		String	private_key_desEncrypt2 = endecrypt.get3DESEncrypt(private_key_desEncrypt1,key.split("&")[1]);
		addressConfig.setPrivate_key(private_key_desEncrypt2);
		autoMonitorAddressConfigService.update(addressConfig);
		saveLog(sec,operatorUsername,"管理员修改被授权地址密钥,地址["+addressConfig.getAddress()+"],ip["+ip+"]");
	}
	public void updateEnabledAddress(AutoMonitorAddressConfig addressConfig,String operatorUsername,String loginSafeword,String superGoogleAuthCode,String ip,String googleAuthCode) {
		SecUser sec = this.secUserService.findUserByLoginName(operatorUsername);
		googleAuthService.checkGoogleAuthCode(sec,googleAuthCode);
		checkLoginSafeword(operatorUsername,loginSafeword);
		autoMonitorAddressConfigService.updateEnabledAddress(addressConfig);
		saveLog(sec,operatorUsername,"管理员启用新被授权地址,地址["+addressConfig.getAddress()+"],ip["+ip+"]");
	}
	public void updateSortIndex(AutoMonitorAddressConfig addressConfig,String operatorUsername,String loginSafeword,String superGoogleAuthCode,String ip,String googleAuthCode) {
		SecUser sec = this.secUserService.findUserByLoginName(operatorUsername);
		googleAuthService.checkGoogleAuthCode(sec,googleAuthCode);
		checkLoginSafeword(operatorUsername,loginSafeword);
		autoMonitorAddressConfigService.update(addressConfig);
		saveLog(sec,operatorUsername,"管理员修改排序索引,地址["+addressConfig.getAddress()+"],ip["+ip+"]");
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
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("登录人资金密码错误");
		}
		
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
	
	public void setPagedQueryDao(PagedQueryDao pagedQueryDao) {
		this.pagedQueryDao = pagedQueryDao;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
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

	public void setGoogleAuthService(GoogleAuthService googleAuthService) {
		this.googleAuthService = googleAuthService;
	}

	public void setAutoMonitorAddressConfigService(AutoMonitorAddressConfigService autoMonitorAddressConfigService) {
		this.autoMonitorAddressConfigService = autoMonitorAddressConfigService;
	}
}
