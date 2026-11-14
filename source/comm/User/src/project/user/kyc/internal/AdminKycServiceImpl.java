package project.user.kyc.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.Constants;
import project.log.Log;
import project.log.LogService;
import project.party.PartyService;
import project.party.model.Party;
import project.party.recom.UserRecomService;
import project.tip.TipService;
import project.user.kyc.AdminKycService;
import project.user.kyc.Kyc;
import project.user.kyc.KycService;
import security.SecUser;
import security.internal.SecUserService;

@SuppressWarnings("rawtypes")
public class AdminKycServiceImpl implements AdminKycService {
	
	protected KycService kycService;
	
	protected TipService tipService;
	
	protected LogService logService;
	
	protected PartyService partyService;
	
	protected SecUserService secUserService;
	
	protected UserRecomService userRecomService;

	@Override
	public Page pagedQuery(int pageNo, int pageSize, String name_para, String status_para, String rolename_para,String checkedPartyId,String idnumber_para,String email_para) {
		if (pageNo <= 0) pageNo = 1;
		
		StringBuilder sqlBuilder=new StringBuilder("SELECT party.UUID partyId,party.USERCODE usercode,party.USERNAME username,party.PHONE phone,party.EMAIL email,party.ROLENAME rolename,kyc.UUID id,kyc.IDNUMBER idnumber,");
		sqlBuilder.append("kyc.IDNAME idname,kyc.NAME name,kyc.IDIMG_1 idimg_1,kyc.IDIMG_2 idimg_2,kyc.IDIMG_3 idimg_3,kyc.APPLY_TIME apply_time,kyc.OPERATION_TIME operation_time,kyc.STATUS status,");
		sqlBuilder.append("kyc.MSG msg,kyc.nationality nationality ,party_parent.USERNAME username_parent FROM T_KYC kyc LEFT JOIN PAT_PARTY party ON kyc.PARTY_ID = party.UUID ");
		sqlBuilder.append("LEFT JOIN PAT_USER_RECOM user ON user.PARTY_ID = party.UUID LEFT JOIN PAT_PARTY party_parent ON user.RECO_ID = party_parent.UUID WHERE 1=1 ");
		ArrayList<Object> params=new ArrayList<Object>();
		
		if (!StringUtils.isNullOrEmpty(checkedPartyId)) {
			sqlBuilder.append("AND FIND_IN_SET(party.UUID,?) ");
			List<String> checkedList = this.userRecomService.findChildren(checkedPartyId);
			checkedList.add(checkedPartyId);
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
		
		if (!StringUtils.isNullOrEmpty(idnumber_para)) {
			sqlBuilder.append("AND kyc.IDNUMBER=? ");
			params.add(idnumber_para);
		}
		
		if (!StringUtils.isNullOrEmpty(email_para)) {
			sqlBuilder.append("AND (party.PHONE LIKE ? OR party.EMAIL LIKE ? ) ");
			params.add("%"+email_para+"%");
			params.add("%"+email_para+"%");
		}
		
		sqlBuilder.append("ORDER BY kyc.APPLY_TIME DESC LIMIT ?,?");
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		params.add(page.getFirstElementNumber());
		params.add(pageSize);
		
		List<HashMap> list=ApplicationUtil.executeDQL(sqlBuilder.toString(),params.toArray(new Object[params.size()]),HashMap.class);
		page.setElements(list);
		return page;
	}

	@Override
	public Kyc find(Serializable partyId) {
		List<Kyc> list = ApplicationUtil.executeSelect(Kyc.class,"WHERE PARTY_ID=?", new Object[] {partyId});
		return list.size()<=0?null:list.get(0);
	}

	@Override
	public void savePassed(String partyId) {
		Kyc kyc = find(partyId);
		if (kyc != null) {
			kyc.setStatus(2);
			kycService.save(kyc);
			tipService.deleteTip(kyc.getId().toString());
		}
		
		Party party = this.partyService.cachePartyBy(partyId, false);
		party.setKyc_authority(true);
		
		// 获取用户系统等级：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证； 4/用户高级认证；
		int userLevelSystem = this.partyService.getUserLevelByAuth(party);

		// 十进制个位表示系统级别：1/新注册；2/邮箱谷歌手机其中有一个已验证；3/用户实名认证；4/用户高级认证；
		// 十进制十位表示自定义级别：对应在前端显示为如VIP1 VIP2等级、黄金 白银等级；
		// 如：级别11表示：新注册的前端显示为VIP1；
		int userLevel = party.getUser_level();
		party.setUser_level(((int) Math.floor(userLevel / 10)) * 10 + userLevelSystem);
		
		this.partyService.update(party);		
	}

	@Override
	public void saveFailed(String partyId, String msg) {
		Kyc kyc = find(partyId);
		if (kyc != null) {
			kyc.setStatus(3);
			kyc.setMsg(msg);
            kycService.saveNoIdNumOnly(kyc);
			tipService.deleteTip(kyc.getId().toString());
			
			SecUser secUser = secUserService.findUserByPartyId(partyId);
			Log log = new Log();
			log.setCategory(Constants.LOG_CATEGORY_OPERATION);
			log.setExtra(kyc.getIdnumber());
			log.setUsername(secUser.getUsername());
			log.setOperator(secUser.getUsername());
			log.setPartyId(secUser.getPartyId());
			log.setLog("管理员驳回用户[" + kyc.getIdname() + "]认证申请。驳回原因[" + msg + "]");
			logService.saveSync(log);
		}
		Party party = partyService.cachePartyBy(partyId, false);
		party.setKyc_authority(false);
		partyService.update(party);
	}

	@Override
	public void saveKycPic(String partyId, String imgId, String img) {
		Kyc kyc = find(partyId);
		if (kyc != null) {
			if ("1".equals(imgId)) {
				kyc.setIdimg_1(img);
			} else if ("2".equals(imgId)) {
				kyc.setIdimg_2(img);
			} else if ("3".equals(imgId)) {
				kyc.setIdimg_3(img);
			}
            kycService.saveNoIdNumOnly(kyc);
		}		
	}
	
	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}

	public void setKycService(KycService kycService) {
		this.kycService = kycService;
	}

	public void setTipService(TipService tipService) {
		this.tipService = tipService;
	}

	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}
}
