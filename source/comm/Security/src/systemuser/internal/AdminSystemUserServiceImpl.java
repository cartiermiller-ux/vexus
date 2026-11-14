package systemuser.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.providers.encoding.PasswordEncoder;
import org.springframework.util.ObjectUtils;

import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.Constants;
import project.log.Log;
import project.log.LogService;
import project.user.googleauth.GoogleAuthService;
import security.Role;
import security.RoleService;
import security.SecUser;
import security.internal.SecUserService;
import security.internal.SecUserServiceImpl;
import systemuser.AdminSystemUserService;

public class AdminSystemUserServiceImpl implements AdminSystemUserService {
	
	private RoleService roleService;
	
	protected LogService logService;
	
	private SecUserService secUserService;
	
	private PasswordEncoder passwordEncoder;
	
	private GoogleAuthService googleAuthService;
	
	public void save(SecUser user,String operatorUsername,String loginSafeword,String code,String ip,String superGoogleAuthCode) {
		googleAuthService.checkSuperGoogleAuthCode(superGoogleAuthCode);
		String enable = user.getEnabled()?"开启":"未开启";
		
		checkLoginSafeword(operatorUsername,loginSafeword);
		
		saveLog(user,operatorUsername,"ip:"+ip+"管理员新增系统用户,角色为["+user.getId()+"],登录权限为["+enable+"],邮箱为["+user.getEmail()+"],验证码:["+code+"]");
		user.setSafeword(passwordEncoder.encodePassword(user.getSafeword(), user.getUsername()));
		
		secUserService.saveUser(user);
	}
	
	public SecUser get(Serializable id) {
		return secUserService.findUserById(id);
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
	
	/**
	 * 更新系统用户，有密码则更新密码,否则更新用户
	 * @param user
	 * @param newPassword
	 * @param type 密码类型,password:登录密码,safe_password:资金密码
	 */
	public void update(SecUser user,String newPassword,String type,String operatorUsername,String loginSafeword,String code,String ip,String superGoogleAuthCode) {
		if(ObjectUtils.isEmpty(newPassword)) {
			checkLoginSafeword(operatorUsername,loginSafeword);
			SecUser userDB = get(user.getId());
			String enableDB = userDB.getEnabled()?"开启":"未开启";
			
			String emailDB = user.getEmail();
			this.secUserService.update(user);
			String enable = user.getEnabled()?"开启":"未开启";
			
			saveLog(user,operatorUsername,"ip:"+ip+"管理员修改系统用户,修改前角色为["+userDB.getRoles().toArray(new Role[0])[0].getRoleName()+"],登录权限["+enableDB+"],邮箱为["+emailDB+"],"
					+ "修改后角色为["+user.getRoles().toArray(new Role[0])[0].getRoleName()+"],登录权限["+enable+"],邮箱为["+user.getEmail()+"]");
		}else {
			googleAuthService.checkSuperGoogleAuthCode(superGoogleAuthCode);
			checkLoginSafeword(operatorUsername,loginSafeword);
			switch(type) {
				case "password":
					secUserService.updatePassword(user.getUsername(), newPassword);
					saveLog(user,operatorUsername,"ip:"+ip+"管理员修改系统用户登录密码,验证码:["+code+"]");
					break;
				case "safe_password":
					secUserService.updateSafeword(user.getUsername(), newPassword);
					saveLog(user,operatorUsername,"ip:"+ip+"管理员修改系统用户资金密码,验证码:["+code+"]");
					break;
				default:
					break;
			}
		}
	}
	
	/**
	 * 删除系统用户
	 */
	public void delete(SecUser user, String operatorUsername, String loginSafeword, String ip, String superGoogleAuthCode) {
		googleAuthService.checkSuperGoogleAuthCode(superGoogleAuthCode);
		
		this.checkLoginSafeword(operatorUsername, loginSafeword);
		
		SecUser userDB = get(user.getId());
		String enableDB = userDB.getEnabled()?"开启":"未开启";
		String emailDB = user.getEmail();
		
		secUserService.deleteUser(user);
		saveLog(user, operatorUsername, "ip:"+ip+"管理员删除系统用户,系统用户角色为["+userDB.getRoles().toArray(new Role[0])[0].getRoleName()+"],登录权限["+enableDB+"],邮箱为["+emailDB+"]");
	}
	
	public Page pagedQuery(int pageNo,int pageSize ,String usernamePara) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo,pageSize,Integer.MAX_VALUE);
		
		ArrayList<Object> whereParams=new ArrayList<Object>();
		StringBuilder whereStatement=new StringBuilder("WHERE (PARTY_UUID IS NULL OR PARTY_UUID='') AND UUID NOT IN('SROOT') ");
		if(StringUtils.isNotEmpty(usernamePara)) {
			whereStatement.append("AND USERNAME LIKE ? ");
			whereParams.add("%"+usernamePara+"%");
		}
		
		whereStatement.append("ORDER BY CREATETIME ASC LIMIT ?,?");
		whereParams.add(page.getFirstElementNumber());
		whereParams.add(pageSize);
		
		List<SecUser> secuserList=ApplicationUtil.executeSelect(SecUser.class, whereStatement.toString(), whereParams.toArray(new Object[whereParams.size()]));
		secuserList.forEach(secuser->SecUserServiceImpl.bindRoleResource(secuser));
		page.setElements(secuserList);
		
		return page;
	}
	
	public Map<String,String> findRoleMap() {
		Map<String,String> map = new HashMap<>();
		List<Role> all = roleService.getAll();
		for(Role role:all) {
			String roleName=role.getRoleName();
			if(Constants.ROLE_MAP.containsKey(roleName)) continue;
			map.put(roleName,roleName);
		}
		
		return map;
	}
	
	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}
	public SecUserService getSecUserService() {
		return secUserService;
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
}
