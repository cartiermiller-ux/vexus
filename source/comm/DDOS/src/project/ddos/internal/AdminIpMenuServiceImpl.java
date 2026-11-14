package project.ddos.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.security.providers.encoding.PasswordEncoder;

import kernel.exception.BusinessException;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.Constants;
import project.ddos.AdminIpMenuService;
import project.ddos.CheckIpRequestCountService;
import project.ddos.IpMenuService;
import project.ddos.model.IpMenu;
import project.log.Log;
import project.log.LogService;
import security.SecUser;
import security.internal.SecUserService;

@SuppressWarnings({"rawtypes","unchecked"})
public class AdminIpMenuServiceImpl implements AdminIpMenuService {
	
	private LogService logService;
	
	private IpMenuService ipMenuService;
	
	private SecUserService secUserService;
	
	private PasswordEncoder passwordEncoder;
	
	private CheckIpRequestCountService checkIpRequestCountService;

	@Override
	public Page pagedQuery(int pageNo, int pageSize, String ip_para, String type_para) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		
		StringBuilder sqlBuilder=new StringBuilder("SELECT UUID id,TYPE type,IP ip,TYPE type,LAST_OPERA_TIME last_opera_time,CREATE_TIME create_time FROM T_IP_MENU WHERE DELETE_STATUS=0 ");
		ArrayList<Object> params=new ArrayList<Object>();
		if (null!=ip_para && !(ip_para=ip_para.trim()).isEmpty()) {
			sqlBuilder.append("AND IP LIKE ? ");
			params.add("%"+ip_para+"%");
		}
		
		if (null!=type_para && !(type_para=type_para.trim()).isEmpty()) {
			sqlBuilder.append("AND TYPE=? ");
			params.add(type_para);
		}
		
		sqlBuilder.append("LIMIT ?,?");
		params.add(page.getFirstElementNumber());
		params.add(pageSize);
		
		List<Map> list=ApplicationUtil.executeDQL(sqlBuilder.toString(),params.toArray(new Object[params.size()]),Map.class);
		page.setElements(list);
		handleDatas(list);
		return page;
	}

	public void handleDatas(List<Map> datas) {
		List<String> ips = new ArrayList<String>();
		for (Map data : datas) {
			ips.add(data.get("ip").toString());
		}
		
		Map<String, Long> ipCountByIps = checkIpRequestCountService.ipCountByIps(ips);
		for (Map data : datas) {
			data.put("count", ipCountByIps.get(data.get("ip").toString()));
		}
	}

	public void save(IpMenu entity, String operatorUsername, String loginSafeword, String ip) {
		IpMenu ipMenu = ipMenuService.cacheByIp(entity.getIp());
		if (ipMenu != null && ipMenu.getDelete_status() == 0) throw new BusinessException("ip名单已经存在");
		
		checkLoginSafeword(operatorUsername, loginSafeword);
		
		SecUser user = this.secUserService.findUserByLoginName(operatorUsername);
		saveLog(user, operatorUsername,"ip:" + ip + "管理员新增IP名单,ip名单为[" + entity.getIp() + "],类型为[" + entity.getType() + "]");
		
		if (ipMenu != null) {
			ipMenu.setDelete_status(0);
			ipMenu.setType(entity.getType());
			ipMenu.setLast_opera_time(entity.getLast_opera_time());
			this.ipMenuService.update(ipMenu);
		} else {
			this.ipMenuService.save(entity);
		}
	}

	public void update(IpMenu entity, String operatorUsername, String loginSafeword, String ip) {
		IpMenu ipMenu = ipMenuService.cacheByIp(entity.getIp());
		if (ipMenu == null || ipMenu.getDelete_status() == -1) throw new BusinessException("ip名单不存在");
		
		checkLoginSafeword(operatorUsername, loginSafeword);
		
		SecUser user = this.secUserService.findUserByLoginName(operatorUsername);
		saveLog(user, operatorUsername, "ip:" + ip + "管理员更新IP名单,ip名单为[" + entity.getIp() + "],原类型为[" + ipMenu.getType()+ "],新类型为[" + entity.getType() + "]");

		ipMenu.setDelete_status(entity.getDelete_status());
		ipMenu.setType(entity.getType());
		ipMenu.setLast_opera_time(entity.getLast_opera_time());
		this.ipMenuService.update(ipMenu);
	}

	public void delete(String menu_ip, String operatorUsername, String loginSafeword, String ip) {
		IpMenu ipMenu = ipMenuService.cacheByIp(menu_ip);
		if (ipMenu == null || ipMenu.getDelete_status() == -1) throw new BusinessException("ip名单不存在");
		
		checkLoginSafeword(operatorUsername, loginSafeword);
		
		SecUser user = this.secUserService.findUserByLoginName(operatorUsername);
		saveLog(user, operatorUsername, "ip:" + ip + "管理员删除IP名单,ip名单为[" + menu_ip + "],类型为[" + ipMenu.getType() + "]");

		ipMenu.setDelete_status(-1);
		ipMenu.setLast_opera_time(new Date());
		this.ipMenuService.update(ipMenu);
		checkIpRequestCountService.cacheRemoveIp(ipMenu.getIp());
	}

	/**
	 * 验证登录人资金密码
	 * @param operatorUsername
	 * @param loginSafeword
	 */
	private void checkLoginSafeword(String operatorUsername, String loginSafeword) {
		SecUser sec = this.secUserService.findUserByLoginName(operatorUsername);
		String sysSafeword = sec.getSafeword();
		String safeword_md5 = passwordEncoder.encodePassword(loginSafeword, operatorUsername);
		if (!safeword_md5.equals(sysSafeword)) throw new BusinessException("登录人资金密码错误");
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

	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public void setIpMenuService(IpMenuService ipMenuService) {
		this.ipMenuService = ipMenuService;
	}

	public void setCheckIpRequestCountService(CheckIpRequestCountService checkIpRequestCountService) {
		this.checkIpRequestCountService = checkIpRequestCountService;
	}
}
