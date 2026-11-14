package project.user.kyc;

import java.io.Serializable;

import kernel.web.Page;

public interface AdminKycService {
	public Page pagedQuery(int pageNo, int pageSize, String name_para, String status_para,String rolename_para, String checkedPartyId,String idnumber_para,String email_para);

	public Kyc find(Serializable partyId);

	public void savePassed(String partyId);

	public void saveFailed(String partyId, String msg);
	
	public void saveKycPic(String partyId, String imgId, String img);
}
