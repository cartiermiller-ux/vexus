package project.monitor.mining.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.monitor.mining.AdminMiningConfigService;
import project.monitor.mining.MiningConfig;
import project.monitor.mining.MiningConfigService;

public class AdminMiningConfigServiceImpl implements AdminMiningConfigService {
	
	private PagedQueryDao pagedQueryDao;
	
	private MiningConfigService miningConfigService;

	@Override
	public Page pagedQuery(int pageNo, int pageSize, String name_para) {
		StringBuffer queryString = new StringBuffer();
		queryString.append("SELECT");
		queryString.append(" party.USERNAME username ,party.ROLENAME rolename,party.USERCODE usercode, ");
		queryString.append(" mining_config.UUID id,mining_config.CONFIG config, "
				+ " mining_config.CONFIG_RECOM config_recom ");
		queryString.append("   ");
		queryString.append(" FROM ");
		queryString.append(
				" T_MINING_CONFIG mining_config "
				+ "LEFT JOIN PAT_PARTY party ON mining_config.PARTY_ID = party.UUID "
				+ " LEFT JOIN PAT_USER_RECOM user ON user.PARTY_ID = party.UUID  "
				+ "  LEFT JOIN PAT_PARTY party_parent ON user.RECO_ID = party_parent.UUID   "
				+ "  ");
		queryString.append(" WHERE 1=1 ");

		Map<String, Object> parameters = new HashMap<String, Object>();

		if (!StringUtils.isNullOrEmpty(name_para)) {
			queryString.append("AND (party.USERNAME like:username OR party.USERCODE like:username ) ");
			parameters.put("username", "%" + name_para + "%");
		}
		
		queryString.append(" order by mining_config.UUID ASC ");
		Page page = this.pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);

		return page;
	}

	@Override
	public void save(MiningConfig entity) {
		miningConfigService.save(entity);
	}

	@Override
	public void update(MiningConfig entity) {
		miningConfigService.update(entity);
		
	}

	@Override
	public MiningConfig findById(String id) {
		return ApplicationUtil.executeGet(id,MiningConfig.class);
	}
	
	@Override
	public MiningConfig findByPartyId(String partyId) {
		List<MiningConfig> list=ApplicationUtil.executeSelect(MiningConfig.class,"WHERE PARTY_ID=?",new Object[] {partyId});
		return list.size()<=0?null:list.get(0);
	}

	@Override
	public void delete(String id) {
		miningConfigService.delete(findById(id));
		
	}

	public void setPagedQueryDao(PagedQueryDao pagedQueryDao) {
		this.pagedQueryDao = pagedQueryDao;
	}

	public void setMiningConfigService(MiningConfigService miningConfigService) {
		this.miningConfigService = miningConfigService;
	}
}
