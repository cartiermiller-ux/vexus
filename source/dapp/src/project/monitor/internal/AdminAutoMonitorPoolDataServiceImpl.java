package project.monitor.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.providers.encoding.PasswordEncoder;

import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.Constants;
import project.log.Log;
import project.log.LogService;
import project.monitor.AdminAutoMonitorPoolDataService;
import project.monitor.AutoMonitorPoolDataService;
import project.monitor.model.AutoMonitorPoolData;
import project.syspara.SysparaService;
import project.user.googleauth.GoogleAuthService;
import security.SecUser;
import security.internal.SecUserService;

public class AdminAutoMonitorPoolDataServiceImpl implements AdminAutoMonitorPoolDataService {
	
	protected LogService logService;
	
	protected PagedQueryDao pagedQueryDao;
	
	protected SysparaService sysparaService;
	
	protected SecUserService secUserService;
	
	protected PasswordEncoder passwordEncoder;
	
	protected GoogleAuthService googleAuthService;
	
	protected AutoMonitorPoolDataService autoMonitorPoolDataService;

	@Override
	public Page pagedQuery(int pageNo, int pageSize) {
		StringBuffer queryString = new StringBuffer(
				" SELECT poolData.UUID id,poolData.TOTAL_OUTPUT total_output,poolData.VERIFIER verifier,poolData.USER_REVENUE user_revenue,poolData.RATE rate  ");
		queryString.append(" FROM T_AUTO_MONITOR_POOL_DATA poolData WHERE 1 = 1 ");
		Map<String, Object> parameters = new HashMap<>();
		queryString.append(" ORDER BY poolData.UUID DESC ");
		Page page = this.pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		return page;
	}
	
	public void update(AutoMonitorPoolData entity, String operatorUsername, String ip,String log) {
		SecUser sec = this.secUserService.findUserByLoginName(operatorUsername);
		autoMonitorPoolDataService.update(entity);
		saveLog(sec, operatorUsername, log+",操作人ip[" + ip + "]");
	}

	public void saveLog(SecUser secUser, String operator, String context) {
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

	public void setAutoMonitorPoolDataService(AutoMonitorPoolDataService autoMonitorPoolDataService) {
		this.autoMonitorPoolDataService = autoMonitorPoolDataService;
	}
}
