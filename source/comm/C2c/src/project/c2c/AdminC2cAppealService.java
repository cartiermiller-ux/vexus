package project.c2c;

import kernel.web.Page;

public interface AdminC2cAppealService {

	public Page pagedQuery(int pageNo, int pageSize, Integer status_int, String order_no_para, String user_code_para, String rolename_para, 
			String c2c_user_code_para, Integer c2c_user_type_int, String c2c_user_party_code_para, String loginPartyId);
	
	public void handled(C2cAppeal c2cAppeal, String operatorUsername, String partyId);
	
	public C2cAppeal get(String order_no);
	
}
