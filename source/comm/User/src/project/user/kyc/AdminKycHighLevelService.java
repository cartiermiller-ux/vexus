package project.user.kyc;

import java.io.Serializable;

import kernel.web.Page;

public interface AdminKycHighLevelService {
	public Page pagedQuery(int pageNo, int pageSize, String name_para, String status_para,String rolename_para, String checkedPartyId);

	public KycHighLevel findByPartyId(Serializable partyId);

	public void savePassed(String partyId);

	public void saveFailed(String partyId, String msg);
}
