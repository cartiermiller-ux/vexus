package project.monitor.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kernel.web.ApplicationUtil;
import project.Constants;
import project.monitor.AutoMonitorAutoTransferFromConfigService;
import project.monitor.model.AutoMonitorAutoTransferFromConfig;
import project.party.PartyService;
import project.party.model.Party;
import project.party.model.UserRecom;
import project.party.recom.UserRecomService;

public class AutoMonitorAutoTransferFromConfigServiceImpl implements AutoMonitorAutoTransferFromConfigService {

	private PartyService partyService;
	
	private UserRecomService userRecomService;
	
	private volatile Map<String, AutoMonitorAutoTransferFromConfig> cache = new ConcurrentHashMap<String, AutoMonitorAutoTransferFromConfig>();

	public void init() {
		List<AutoMonitorAutoTransferFromConfig> all = getAll();
		for (AutoMonitorAutoTransferFromConfig entity : all) {
			cache.put(entity.getPartyId(), entity);
		}
	}

	@Override
	public List<AutoMonitorAutoTransferFromConfig> getAll() {
		return ApplicationUtil.executeSelect(AutoMonitorAutoTransferFromConfig.class);
	}

	public List<AutoMonitorAutoTransferFromConfig> cacheAll() {
		return new ArrayList<AutoMonitorAutoTransferFromConfig>(cache.values());
	}

	public Map<String, AutoMonitorAutoTransferFromConfig> cacheAllMap() {
		return new HashMap<String, AutoMonitorAutoTransferFromConfig>(cache);
	}

	@Override
	public void save(AutoMonitorAutoTransferFromConfig entity) {
		ApplicationUtil.executeInsert(entity);
		cache.put(entity.getPartyId(), entity);
		
	}

	@Override
	public void update(AutoMonitorAutoTransferFromConfig entity) {
		ApplicationUtil.executeUpdate(entity);
		cache.put(entity.getPartyId(), entity);
	}

	public void delete(AutoMonitorAutoTransferFromConfig entity) {
		ApplicationUtil.executeDelete(entity);
		cache.remove(entity.getPartyId());
	}

	@Override
	public AutoMonitorAutoTransferFromConfig findById(String id) {
		return ApplicationUtil.executeGet(id,AutoMonitorAutoTransferFromConfig.class);
	}

	public AutoMonitorAutoTransferFromConfig findByPartyId(String partyId) {
		List<AutoMonitorAutoTransferFromConfig> list=ApplicationUtil.executeSelect(AutoMonitorAutoTransferFromConfig.class,"WHERE PARTY_ID=?",new Object[] {partyId});
		return list.size()<=0?null:list.get(0);
	}

	/**
	 * 取到 ETH 增加时是否自动归集的配置
	 * 
	 * @param partyId
	 * @return
	 */
	@Override
	public AutoMonitorAutoTransferFromConfig getConfig(String partyId) {
		List<UserRecom> parents = userRecomService.getParents(partyId.toString());
		List<AutoMonitorAutoTransferFromConfig> configs = this.cacheAll();

		/**
		 * 该用户直接配置
		 */
		for (int i = 0; i < configs.size(); i++) {
			AutoMonitorAutoTransferFromConfig config = configs.get(i);
			if (partyId.equals(config.getPartyId())) {
				/*
				 * 找到返回
				 */
				return config;
			}
		}

		/**
		 * 取到代理
		 */
		for (int i = 0; i < parents.size(); i++) {
			Party party = partyService.cachePartyBy(parents.get(i).getReco_id(), true);
			if (!Constants.SECURITY_ROLE_AGENT.equals(party.getRolename()) && !Constants.SECURITY_ROLE_AGENTLOW.equals(party.getRolename())) {
				/**
				 * 非代理
				 */
				continue;
			}

			for (int j = 0; j < configs.size(); j++) {
				AutoMonitorAutoTransferFromConfig config = configs.get(j);
				if (party.getId().toString().equals(config.getPartyId())) {
					return config;
				}
			}

		}

		/**
		 * 全局配置
		 *
		 */
		for (int i = 0; i < configs.size(); i++) {
			AutoMonitorAutoTransferFromConfig config = configs.get(i);
			if (config.getPartyId() == null || "".equals(config.getPartyId().toString())) {
				return config;
			}
		}
		return null;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}
}
