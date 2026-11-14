package project.user.kyc.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.party.PartyService;
import project.party.model.Party;
import project.party.recom.UserRecomService;
import project.tip.TipService;
import project.user.kyc.AdminKycHighLevelService;
import project.user.kyc.KycHighLevel;
import project.user.kyc.KycHighLevelService;

@SuppressWarnings("rawtypes")
public class AdminKycHighLevelServiceImpl implements AdminKycHighLevelService {
	
	private TipService tipService;
	
	private PartyService partyService;
	
	private UserRecomService userRecomService;
	
	private KycHighLevelService kycHighLevelService;

	@Override
	public Page pagedQuery(int pageNo, int pageSize, String name_para, String status_para, String rolename_para,String checkedPartyId) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		StringBuilder sqlBuilder=new StringBuilder("SELECT party.UUID partyId,party.USERCODE usercode,party.USERNAME username,party.ROLENAME rolename,kyc.UUID id,kyc.WORK_PLACE work_place,kyc.HOME_PLACE home_place,");
		sqlBuilder.append("kyc.RELATIVES_RELATION relatives_relation,kyc.APPLY_TIME apply_time,kyc.OPERATION_TIME operation_time,kyc.IDIMG_1 idimg_1,kyc.IDIMG_2 idimg_2,kyc.IDIMG_3 idimg_3,");
		sqlBuilder.append("kyc.RELATIVES_NAME relatives_name,kyc.RELATIVES_PLACE relatives_place,kyc.STATUS status,kyc.MSG msg,kyc.RELATIVES_PHONE relatives_phone,");
		sqlBuilder.append("party_parent.USERNAME username_parent FROM T_KYC_HIGH_LEVEL kyc ");
		sqlBuilder.append("LEFT JOIN PAT_PARTY party ON kyc.PARTY_ID=party.UUID ");
		sqlBuilder.append("LEFT JOIN PAT_USER_RECOM user ON user.PARTY_ID=party.UUID ");
		sqlBuilder.append("LEFT JOIN PAT_PARTY party_parent ON user.RECO_ID=party_parent.UUID WHERE 1=1 ");
		
		ArrayList<Object> params=new ArrayList<Object>();
		
		if (!StringUtils.isNullOrEmpty(checkedPartyId)) {
			List<String> checkedList = this.userRecomService.findChildren(checkedPartyId);
			checkedList.add(checkedPartyId);
			sqlBuilder.append("AND FIND_IN_SET(party.UUID,?) ");
			params.add(String.join(",",checkedList));
		}

		if (!StringUtils.isNullOrEmpty(status_para)) {
			sqlBuilder.append("AND kyc.STATUS=? ");
			params.add(status_para);
		}
		
		if (!StringUtils.isNullOrEmpty(rolename_para)) {
			sqlBuilder.append("AND party.ROLENAME=? ");
			params.add(rolename_para);
		}
		
		if (!StringUtils.isNullOrEmpty(name_para)) {
			sqlBuilder.append("AND (party.USERNAME LIKE ? OR party.USERCODE LIKE ? ) ");
			params.add("%"+name_para+"%");
			params.add("%"+name_para+"%");
		}
		
		sqlBuilder.append("ORDER BY kyc.APPLY_TIME DESC LIMIT ?,?");
		params.add(page.getFirstElementNumber());
		params.add(pageSize);
		
		List<HashMap> list=ApplicationUtil.executeDQL(sqlBuilder.toString(),params.toArray(new Object[params.size()]),HashMap.class);
		page.setElements(list);
		return page;
	}

	@Override
	public KycHighLevel findByPartyId(Serializable partyId) {
		List<KycHighLevel> list = ApplicationUtil.executeSelect(KycHighLevel.class,"WHERE PARTY_ID=?",new Object[] {partyId});
		if(list.size()<=0) return null;
		
		KycHighLevel kycHighLevel = list.get(0);
		kycHighLevel.setUsername(partyService.cachePartyBy(kycHighLevel.getPartyId(),true).getUsername());
		
		return kycHighLevel;
	}

	@Override
	public void savePassed(String partyId) {
		KycHighLevel kycHighLevel = findByPartyId(partyId);
		if(null==kycHighLevel) return;
		
		kycHighLevel.setStatus(2);
		kycHighLevelService.save(kycHighLevel);
		
		Party party = partyService.cachePartyBy(partyId, false);
		party.setKyc_highlevel_authority(true);
		
		// 获取用户系统等级：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证； 4/用户高级认证；
		int userLevelSystem = this.partyService.getUserLevelByAuth(party);

		// 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
		// 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
		// 如：级别11表示：新注册的前端显示为VIP1；
		int userLevel = party.getUser_level();
		party.setUser_level(((int) Math.floor(userLevel / 10)) * 10 + userLevelSystem);
		
		this.partyService.update(party);

		tipService.deleteTip(kycHighLevel.getId().toString());
	}

	@Override
	public void saveFailed(String partyId, String msg) {
		KycHighLevel kycHighLevel = findByPartyId(partyId);
		if(null==kycHighLevel) return;
		
		kycHighLevel.setStatus(3);
		kycHighLevel.setMsg(msg);
		kycHighLevelService.save(kycHighLevel);
		
		Party party = partyService.cachePartyBy(partyId, false);
		party.setKyc_highlevel_authority(false);
		partyService.update(party);
		tipService.deleteTip(kycHighLevel.getId().toString());
	}

	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}

	public void setKycHighLevelService(KycHighLevelService kycHighLevelService) {
		this.kycHighLevelService = kycHighLevelService;
	}

	public void setTipService(TipService tipService) {
		this.tipService = tipService;
	}
}
