package project.user.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.security.providers.encoding.PasswordEncoder;

import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.party.PartyService;
import project.party.model.Party;
import project.party.recom.UserRecomService;
import project.tip.TipService;
import project.user.AdminUserSafewordApplyService;
import project.user.UserSafewordApply;
import project.user.UserSafewordApplyService;
import project.user.kyc.Kyc;
import project.user.kyc.KycService;
import security.SecUser;
import security.internal.SecUserService;

@SuppressWarnings("rawtypes")
public class AdminUserSafewordApplyServiceImpl implements AdminUserSafewordApplyService {
	
	private TipService tipService;
	
	protected KycService kycService;
	
	protected PartyService partyService;
	
	private SecUserService secUserService;
	
	private PasswordEncoder passwordEncoder;
	
	protected UserRecomService userRecomService;
	
	protected UserSafewordApplyService userSafewordApplyService;

	@Override
	public Page pagedQuery(int pageNo, int pageSize, String name_para, Integer status_para, String rolename_para,String checkedPartyId, Integer operate) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		StringBuilder sqlBuilder=new StringBuilder("SELECT party.UUID partyId, party.USERCODE usercode, party.USERNAME username, party.ROLENAME rolename, party.USER_LEVEL user_level,");
		sqlBuilder.append("kyc.UUID kyc_id, kyc.IDNAME kyc_idname, kyc.NAME kyc_name, kyc.IDIMG_1 kyc_idimg_1, kyc.IDIMG_2 kyc_idimg_2,kyc.IDIMG_3 kyc_idimg_3, kyc.STATUS kyc_status,");
		sqlBuilder.append("apply.UUID id, apply.IDCARD_PATH_FRONT idimg_1, apply.IDCARD_PATH_BACK idimg_2, apply.IDCARD_PATH_HOLD idimg_3,apply.MSG msg, apply.STATUS status,");
		sqlBuilder.append("apply.CREATE_TIME create_time, apply.OPERATE operate, apply.REMARK remark FROM T_USER_SAFEWORD_APPLY apply ");
		sqlBuilder.append("LEFT JOIN T_KYC kyc ON kyc.PARTY_ID=apply.PARTY_ID LEFT JOIN PAT_PARTY party ON kyc.PARTY_ID=party.UUID WHERE 1=1 ");
		ArrayList<Object> params=new ArrayList<Object>();
		
		if (!StringUtils.isNullOrEmpty(checkedPartyId)) {
			List<String> checkedList = this.userRecomService.findChildren(checkedPartyId);
			checkedList.add(checkedPartyId);
			sqlBuilder.append("AND FIND_IN_SET(party.UUID,?) ");
			params.add(String.join(",",checkedList));
		}

		if (status_para != null) {
			sqlBuilder.append("AND apply.STATUS=? ");
			params.add(status_para);
		}
		
		if (!StringUtils.isNullOrEmpty(rolename_para)) {
			sqlBuilder.append("AND party.ROLENAME=? ");
			params.add(rolename_para);
		}
		
		if (!StringUtils.isNullOrEmpty(name_para)) {
			sqlBuilder.append("AND (party.USERNAME LIKE ? OR party.USERCODE LIKE ?) ");
			params.add("%"+name_para+"%");
			params.add("%"+name_para+"%");
		}
		
		if (operate != null) {
			sqlBuilder.append("AND apply.OPERATE=? ");
			params.add(operate);
		}
		
		sqlBuilder.append("ORDER BY apply.CREATE_TIME DESC LIMIT ?,?");
		params.add(page.getFirstElementNumber());
		params.add(pageSize);
		
		List<HashMap> list=ApplicationUtil.executeDQL(sqlBuilder.toString(),params.toArray(new Object[params.size()]),HashMap.class);
		page.setElements(list);
		return page;
	}

	@Override
	public void savePassed(String id, String operatorUsername, String safeword) {
		UserSafewordApply apply = this.userSafewordApplyService.findById(id);
		
		if (null == apply) {
			throw new BusinessException("申请不存在，或请刷新重试");
		}
		
		if (apply.getStatus() != 1) {
			throw new BusinessException("当前申请已处理");
		}
		
		if (!Arrays.asList(0, 1, 2, 3).contains(apply.getOperate())) {
			throw new BusinessException("操作类型不正确");
		}
		
		this.checkLoginSafeword(operatorUsername, safeword);
		
		Kyc kyc = this.kycService.get(apply.getPartyId().toString());
		if (null == kyc || kyc.getStatus() != 2) {
			throw new BusinessException("认证尚未通过，无法重置");
		}
		
		apply.setApply_time(new Date());
		apply.setStatus(2);
		
		this.userSafewordApplyService.update(apply);
				
		Party party = this.partyService.cachePartyBy(apply.getPartyId(), false);
				
		// 操作类型 operate:	 0/修改资金密码；1/取消谷歌绑定；2/取消手机绑定；3/取消邮箱绑定；
		switch (apply.getOperate()) {
			case 0:
				party.setSafeword(apply.getSafeword());
				this.partyService.update(party);
				break;
			case 1:
				SecUser secUser = this.secUserService.findUserByLoginName(party.getUsername());
				if (null == secUser) {
					throw new BusinessException("用户不存在");
				}
				if (!secUser.isGoogle_auth_bind()) {
					throw new BusinessException("用户未绑定，无需解绑");
				}				
				secUser.setGoogle_auth_bind(false);
				this.secUserService.update(secUser);
				break;
			case 2:
				party.setPhone_authority(false);
				this.partyService.update(party);
				break;
			case 3:
				party.setEmail_authority(false);
				this.partyService.update(party);
				break;
		}
		
		this.tipService.deleteTip(apply.getId().toString());
	}

	@Override
	public void saveFailed(String id, String msg) {
		UserSafewordApply apply = this.userSafewordApplyService.findById(id);
		
		if (null == apply) {
			throw new BusinessException("申请不存在，或请刷新重试");
		}
		
		if (apply.getStatus() != 1) {
			throw new BusinessException("当前申请已处理");
		}
		
		Kyc kyc = this.kycService.get(apply.getPartyId().toString());
		if (null == kyc || kyc.getStatus() != 2) {
			throw new BusinessException("认证尚未通过，无法重置");
		}
		
		apply.setApply_time(new Date());
		apply.setStatus(3);
		apply.setMsg(msg);
		
		this.userSafewordApplyService.update(apply);
		
		this.tipService.deleteTip(apply.getId().toString());
	}

	/**
	 * 验证登录人资金密码
	 * 
	 * @param operatorUsername
	 * @param loginSafeword
	 */
	private void checkLoginSafeword(String operatorUsername, String loginSafeword) {
		SecUser sec = this.secUserService.findUserByLoginName(operatorUsername);
		String sysSafeword = sec.getSafeword();
		String safeword_md5 = passwordEncoder.encodePassword(loginSafeword, operatorUsername);
		if (!safeword_md5.equals(sysSafeword)) {
			throw new BusinessException("登录人资金密码错误");
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

	public void setUserSafewordApplyService(UserSafewordApplyService userSafewordApplyService) {
		this.userSafewordApplyService = userSafewordApplyService;
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public void setSecUserService(SecUserService secUserService) {
		this.secUserService = secUserService;
	}

	public void setTipService(TipService tipService) {
		this.tipService = tipService;
	}
}
