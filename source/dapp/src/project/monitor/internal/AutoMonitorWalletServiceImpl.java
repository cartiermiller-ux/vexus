package project.monitor.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import project.Constants;
import project.monitor.AutoMonitorWalletService;
import project.monitor.model.AutoMonitorWallet;
import project.party.PartyService;
import project.party.model.Party;
import project.party.recom.UserRecomService;

public class AutoMonitorWalletServiceImpl implements AutoMonitorWalletService {

	protected PartyService partyService;
	
	protected UserRecomService userRecomService;
	
	public AutoMonitorWallet findById(String id) {
		return ApplicationUtil.executeGet(id,AutoMonitorWallet.class);
	}

	public void update(AutoMonitorWallet entity) {
		ApplicationUtil.executeUpdate(entity);
	}
	
	public List<AutoMonitorWallet> findAllSucceeded_0() {
		return ApplicationUtil.executeSelect(AutoMonitorWallet.class, "WHERE SUCCEEDED=0",null);
	}
	
	public List<AutoMonitorWallet> findAllBySucceeded(Integer succeeded){
		StringBuilder whereBuilder=new StringBuilder("WHERE 1=1 ");
		if(succeeded!=null) whereBuilder.append(" AND SUCCEEDED="+succeeded.toString());
		return ApplicationUtil.executeSelect(AutoMonitorWallet.class,whereBuilder.toString(),null);
	}
	
	public List<AutoMonitorWallet> findAllRoleMember() {
		return ApplicationUtil.executeSelect(AutoMonitorWallet.class,"WHERE ROLENAME=? AND SUCCEEDED IN('1','-5')",new Object[] {Constants.SECURITY_ROLE_MEMBER});
	}

	public void save(AutoMonitorWallet entity) {
		ApplicationUtil.executeInsert(entity);
	}

	public AutoMonitorWallet findBy(String address) {
		List<AutoMonitorWallet> list = ApplicationUtil.executeSelect(AutoMonitorWallet.class,"WHERE ADDRESS=?",new Object[] {address});
		return CollectionUtils.isEmpty(list)?null:list.get(0)==null?null:list.get(0);
	}

	public List<AutoMonitorWallet> findByUsercode(String usercode) {
		List<AutoMonitorWallet> list = new ArrayList<AutoMonitorWallet>();
		StringBuilder whereBuilder=new StringBuilder("WHERE ROLENAME=? AND SUCCEEDED='1' ");
		
		//如果UID是空的就全部返回
		if (StringUtils.isNullOrEmpty(usercode)) {
			return ApplicationUtil.executeSelect(AutoMonitorWallet.class,whereBuilder.toString(),new Object[] {Constants.SECURITY_ROLE_MEMBER});
		}
		
		//如果UID不是空的就定向查询
		Party party = this.partyService.findPartyByUsercode(usercode);
		if (Constants.SECURITY_ROLE_MEMBER.equals(party.getRolename())) {
			whereBuilder.append("AND PARTY_ID=?");
			return ApplicationUtil.executeSelect(AutoMonitorWallet.class,whereBuilder.toString(),new Object[] {Constants.SECURITY_ROLE_MEMBER,party.getId()});
		} else if (Constants.SECURITY_ROLE_AGENT.equals(party.getRolename()) || Constants.SECURITY_ROLE_AGENTLOW.equals(party.getRolename())) {
			String findChildrensIds = userRecomService.findChildrensIds(party.getId().toString());
			if (StringUtils.isEmptyString(findChildrensIds)) return list;
			whereBuilder.append("AND PARTY_ID IN(").append(findChildrensIds).append(")");
			return ApplicationUtil.executeSelect(AutoMonitorWallet.class,whereBuilder.toString(),new Object[] {Constants.SECURITY_ROLE_MEMBER});
		}else {
			return list;
		}
	}
	
	/**
	 * 获取AutoMonitorWallet
	 */
	public AutoMonitorWallet getAutoMonitorWalletByPartyId(String partyId) {
		List<AutoMonitorWallet> list = ApplicationUtil.executeSelect(AutoMonitorWallet.class,"WHERE PARTY_ID=? AND SUCCEEDED='1'",new Object[] {partyId});
		return CollectionUtils.isEmpty(list) ? null : list.get(0) == null ? null : (AutoMonitorWallet) list.get(0);
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}

	@Override
	public AutoMonitorWallet getByPartyId(String partyId) {
		List<AutoMonitorWallet> list = ApplicationUtil.executeSelect(AutoMonitorWallet.class,"WHERE PARTY_ID=?",new Object[] {partyId});
		return CollectionUtils.isEmpty(list)?null:list.get(0)==null?null:(AutoMonitorWallet) list.get(0);
	}
}
