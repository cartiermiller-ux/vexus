package project.user.internal;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.providers.encoding.PasswordEncoder;

import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import project.Constants;
import project.party.PartyService;
import project.party.model.Party;
import project.syspara.Syspara;
import project.syspara.SysparaService;
import project.user.UserService;
import project.user.idcode.IdentifyingCodeTimeWindowService;
import security.Role;
import security.SecUser;
import security.internal.SecUserService;

public class UserServiceImpl implements UserService {
	
	private PartyService partyService;
	
	private SecUserService secUserService;
	
	private PasswordEncoder passwordEncoder;
	
	private OnlineUserService onlineUserService;
	
	private IdentifyingCodeTimeWindowService identifyingCodeTimeWindowService;
	
	/**
	 * 图片验证key，保证前后一致性
	 */
	private Map<String, String> imageCodeCache = new ConcurrentHashMap<String, String>();

	@Override
	public SecUser login(String username, String password) {
		SecUser user = secUserService.findUserByLoginName(username);
		SysparaService sysparaService = ApplicationUtil.getBean(SysparaService.class);
		Syspara syspara = sysparaService.find("bind_email_phone_login");
		if(ObjectUtils.isNotEmpty(syspara)&&"1".equals(syspara.getValue())) {
			user = secUserService.findUserByLoginName2(username);
		}
		if (user == null) {

			throw new BusinessException("用户不存在");
		}
		Party party = partyService.cachePartyBy(user.getPartyId(), false);
		String[] rolesArrty = new String[] { Constants.SECURITY_ROLE_GUEST, Constants.SECURITY_ROLE_MEMBER, Constants.SECURITY_ROLE_TEST };
		if (party == null || !party.getLogin_authority()) {
			throw new BusinessException("登录失败");
		}
		Set<Role> roles = user.getRoles();
		boolean find = false;
		for (Iterator<Role> iterator = roles.iterator(); iterator.hasNext();) {
			Role role = (Role) iterator.next();
			for (int i = 0; i < rolesArrty.length; i++) {
				if (role.getRoleName().equals(rolesArrty[i])) {
					find = true;
				}
			}
		}

		if (!find) {
			throw new BusinessException("登录失败");
		}
		String password_encoder = passwordEncoder.encodePassword(password, user.getUsername());

		if (!password_encoder.equals(user.getPassword())) {
			throw new BusinessException("密码不正确");
		}

		party.setLast_loginTime(new Date());
		partyService.update(party);

		return user;

	}
	
	/**
	 * 验证码登录
	 */
	@Override
	public SecUser login_idcode(String username, String verifcode) {
		SecUser user = secUserService.findUserByLoginName(username);

		if (user == null) {
			throw new BusinessException("用户名不存在");
		}
		Party party = partyService.cachePartyBy(user.getPartyId(), false);
		String[] rolesArrty = new String[] {};
		if (party == null || !party.getLogin_authority()) {
			throw new BusinessException("登录失败");
		}
		Set<Role> roles = user.getRoles();
		boolean find = false;
		for (Iterator<Role> iterator = roles.iterator(); iterator.hasNext();) {
			Role role = (Role) iterator.next();
			for (int i = 0; i < rolesArrty.length; i++) {
				if (role.getRoleName().equals(rolesArrty[i])) {
					find = true;
				}
			}
		}

		if (!find) {
			throw new BusinessException("登录失败");
		}

		String authcode = this.identifyingCodeTimeWindowService.getAuthCode(username);

		if ((authcode == null) || (!authcode.equals(verifcode))) {
			throw new BusinessException("登录失败");
		}
		this.identifyingCodeTimeWindowService.delAuthCode(username);
		return user;
	}
	
	@Override
	public void online(String partyId) {
		if (StringUtils.isNullOrEmpty(partyId)) {
			return;
		}
		onlineUserService.put(partyId, new Date());
	}
	
	@Override
	public void logout(String partyId) {
		if (StringUtils.isNullOrEmpty(partyId)) {
			return;
		}
		onlineUserService.del(partyId);
	}
	
	@Override
	public boolean isOnline(String partyId) {
		Object object = onlineUserService.get(partyId);
		if (object != null) {
			return true;
		}
		return false;
	}

	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public Map<String, String> getImageCodeCache() {
		return imageCodeCache;
	}

	public void putImageCode(String key, String value) {
		imageCodeCache.put(key, value);
	}

	public String cacheImageCode(String key) {
		return imageCodeCache.get(key);
	}

	public void cacheRemoveImageCode(String key) {
		imageCodeCache.remove(key);
	}

	public void putRandKey(String key, String value) {
		imageCodeCache.put(key, value);
	}

	public IdentifyingCodeTimeWindowService getIdentifyingCodeTimeWindowService() {
		return identifyingCodeTimeWindowService;
	}

	public void setIdentifyingCodeTimeWindowService(IdentifyingCodeTimeWindowService identifyingCodeTimeWindowService) {
		this.identifyingCodeTimeWindowService = identifyingCodeTimeWindowService;
	}

	public void setOnlineUserService(OnlineUserService onlineUserService) {
		this.onlineUserService = onlineUserService;
	}
}
