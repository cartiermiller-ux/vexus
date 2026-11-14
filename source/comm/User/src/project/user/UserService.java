package project.user;

import java.util.Map;

import security.SecUser;

public interface UserService {
	
	/**
	 * 登录
	 */
	public SecUser login(String username, String password);
	
	/**
	 * 验证码登录
	 */
	public SecUser login_idcode(String username, String verifcode);
	
	public boolean isOnline(String partyId);
	
	/**
	 * 退出登录
	 */
	public void logout(String partyId);

	/**
	 * 图片验证码缓存
	 */
	public Map<String, String> getImageCodeCache();
	
	/**
	 * 设置玩家在线
	 */
	public void online(String partyId);

	public void putImageCode(String key, String value);

	public String cacheImageCode(String key);

	public void cacheRemoveImageCode(String key);

}
