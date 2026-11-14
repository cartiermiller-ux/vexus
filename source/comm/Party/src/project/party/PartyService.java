package project.party;

import java.io.Serializable;
import java.util.List;

import project.party.model.Party;

/**
 * Party CRUD
 */
public interface PartyService {

	/**
	 * 获取用户系统等级：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证； 4/用户高级认证；
	 */
	public int getUserLevelByAuth(Party party);

	/**
	 * 
	 * @param partyId
	 * @param localcache 是否读本地缓存
	 * @return
	 */
	public Party cachePartyBy(Serializable partyId, boolean localcache);

	/**
	 * 
	 * @param username
	 * @param localcache 是否读本地缓存
	 * @return
	 */
	public Party cachePartyByUsername(String username, boolean localcache);

	public Party save(Party entity);

	public void update(Party entity);

	/**
	 * 根据用户名
	 */
	public Party findPartyByUsername(String username);
	
	/**
	 * 获取PAT_PARTY 根据已验证的电话号码
	 */
	public Party findPartyByVerifiedPhone(String phone);
	
	/**
	 * 获取PAT_PARTY 根据已验证的邮箱
	 */
	public Party findPartyByVerifiedEmail(String email);

	/**
	 * 根据用户名
	 */
	public Party findPartyByUsercode(String usercode);

	public List<Party> getAll();

	public void updateSafeword(Party party, String safeword);

	/**
	 * 验证资金密码
	 * 
	 * @param safeword
	 * @param partyId
	 * @return
	 */
	public boolean checkSafeword(String safeword, String partyId);

}
