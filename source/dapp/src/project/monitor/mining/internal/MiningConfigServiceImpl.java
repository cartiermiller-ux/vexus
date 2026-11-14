package project.monitor.mining.internal;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import kernel.web.ApplicationUtil;
import project.Constants;
import project.monitor.mining.MiningConfig;
import project.monitor.mining.MiningConfigService;
import project.party.PartyService;
import project.party.model.Party;
import project.party.model.UserRecom;

public class MiningConfigServiceImpl implements MiningConfigService {

	protected PartyService partyService;

	public List<MiningConfig> getAll() {
		return ApplicationUtil.executeSelect(MiningConfig.class);
	}

	@Override
	public void save(MiningConfig entity) {
		ApplicationUtil.executeInsert(entity);
	}

	@Override
	public void update(MiningConfig entity) {
		ApplicationUtil.executeUpdate(entity);
	}

	@Override
	public void delete(MiningConfig entity) {
		ApplicationUtil.executeDelete(entity);
	}

	/**
	 * 获取到全局配置
	 * @return
	 */
	public MiningConfig getHoldConfig() {
		List<MiningConfig> list = ApplicationUtil.executeSelect(MiningConfig.class,"WHERE PARTY_ID=''",null);
		return CollectionUtils.isEmpty(list)?null:list.get(0)==null?null:list.get(0);
	}

	/**
	 * 取到应用的收益配置参数
	 * @param partyId
	 * @return
	 */
	public MiningConfig getConfig(String partyId, List<UserRecom> parents, List<MiningConfig> configs) {
		for (int i = 0; i < configs.size(); i++) {
			MiningConfig config = configs.get(i);
			if (partyId.equals(config.getPartyId())) {
				return config;
			}
		}

		/**
		 * 取到代理
		 */
		for (int i = 0; i < parents.size(); i++) {
			Party party = partyService.cachePartyBy(parents.get(i).getReco_id(), true);
			if (!Constants.SECURITY_ROLE_AGENT.equals(party.getRolename()) && !Constants.SECURITY_ROLE_AGENTLOW.equals(party.getRolename())) {
				continue;
			}
			
			for (int j = 0; j < configs.size(); j++) {
				MiningConfig config = configs.get(j);
				if (party.getId().toString().equals(config.getPartyId())) {
					return config;
				}
			}
		}

		/**
		 * 全局配置
		 */
		for (int i = 0; i < configs.size(); i++) {
			MiningConfig config = configs.get(i);
			if (config.getPartyId() == null || "".equals(config.getPartyId().toString())) {
				return config;
			}
		}
		
		return null;
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}
}
