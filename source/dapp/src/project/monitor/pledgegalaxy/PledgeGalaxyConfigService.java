package project.monitor.pledgegalaxy;

import java.util.Date;
import java.util.Map;

/**
 * 质押2.0配置
 */
public interface PledgeGalaxyConfigService {

	/**
	 * 获取配置 优先级：用户>代理>个人
	 */
	public PledgeGalaxyConfig getConfig(String partyId);
	
	/**
	 * vickers获取配置利率Map
	 */
	public Map<String, String> getVickersRateMap(String partyId, int days, double amount);
	
	/**
	 * SafePal5获取配置利率Map
	 */
	public Map<String, String> getSafePal5RateMap(String partyId, int days, double amount);
	
	/**
	 * IoeAI获取配置利率Map
	 */
	public Map<String, String> getIoeAIRateMap(String partyId, int days, double amount, Date createTime);
	
	/**
	 * 获取全局配置
	 */
	public PledgeGalaxyConfig getGlobalConfig();

	public PledgeGalaxyConfig findByPartyId(String partyId);

	public void save(PledgeGalaxyConfig entity);

	public void update(PledgeGalaxyConfig entity);

	public PledgeGalaxyConfig findById(String id);

	public void delete(PledgeGalaxyConfig entity);
	
	/**
	 * 获取IoeAi用户级别
	 */
	public int getIoeAiLevel(String partyId);
}
