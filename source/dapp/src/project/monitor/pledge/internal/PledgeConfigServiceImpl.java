package project.monitor.pledge.internal;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.web.ApplicationUtil;
import project.Constants;
import project.monitor.pledge.PledgeConfig;
import project.monitor.pledge.PledgeConfigService;
import project.party.PartyService;
import project.party.model.Party;
import project.party.model.UserRecom;
import project.party.recom.UserRecomService;

public class PledgeConfigServiceImpl implements PledgeConfigService {
	
	private PartyService partyService;
	
	private UserRecomService userRecomService;
	
	private static final Logger logger = LoggerFactory.getLogger(PledgeConfigServiceImpl.class);

	public List<PledgeConfig> getAll() {
		return ApplicationUtil.executeSelect(PledgeConfig.class);
	}
	
	/**
	 * 取到应用的收益配置参数
	 * @param partyId
	 * @return
	 */
	public PledgeConfig getConfig(String partyId) {
		List<UserRecom> parents = userRecomService.getParents(partyId.toString());
		List<PledgeConfig> configs = this.getAll();
		//PledgeConfig pledgeConfig = this.getGlobalConfig2();

		/**
		 * 该用户直接配置
		 */
		for (int i = 0; i < configs.size(); i++) {
			PledgeConfig config = configs.get(i);
			if (partyId.equals(config.getPartyId())) {
				/*
				 * 找到返回
				 */
				logger.info("found config by current user: {}",config);
				return config;
			}

		}

		/**
		 * 取到代理
		 */
		for (int i = 0; i < parents.size(); i++) {
			Party party = partyService.cachePartyBy(parents.get(i).getReco_id(), true);

			if (!Constants.SECURITY_ROLE_AGENT.equals(party.getRolename())
					&& !Constants.SECURITY_ROLE_AGENTLOW.equals(party.getRolename())) {
				/**
				 * 非代理
				 */
				continue;
			}

			for (int j = 0; j < configs.size(); j++) {
				PledgeConfig config = configs.get(j);
				if (party.getId().toString().equals(config.getPartyId())) {
					logger.info("found config by parent user: {}",config);
					return config;
				}
			}

		}
		
		logger.info("final not found config....");
		//return pledgeConfig;
		return null;
	}

	@Override
	public PledgeConfig getGlobalConfig() {
		List<PledgeConfig> list = ApplicationUtil.executeSelect(PledgeConfig.class,"WHERE PARTY_ID IS NULL OR PARTY_ID=''",null);
		return list.size()<=0?null:list.get(0);
	}
	
	@Override
	public PledgeConfig getGlobalConfig2() {
		List<PledgeConfig> list = ApplicationUtil.executeSelect(PledgeConfig.class,"WHERE PARTY_ID IS NULL OR PARTY_ID=''",null);
		return list.size()<=0?null:list.get(0);
	}
	
	
	public PledgeConfig findByPartyId(String partyId) {
		List<PledgeConfig> list = ApplicationUtil.executeSelect(PledgeConfig.class,"WHERE PARTY_ID=?",new Object[] {partyId});
		return list.size()<=0?null:list.get(0);
	}

	public void save(PledgeConfig entity) {
		ApplicationUtil.executeInsert(entity);
	}

	public void update(PledgeConfig entity) {
		ApplicationUtil.executeUpdate(entity);
	}

	public PledgeConfig findById(String id) {
		return ApplicationUtil.executeGet(id,PledgeConfig.class);
	}

	public void delete(PledgeConfig entity) {
		ApplicationUtil.executeDelete(entity);
	}
	
	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}
	
	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}
}
