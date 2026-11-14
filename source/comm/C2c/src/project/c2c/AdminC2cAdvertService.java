package project.c2c;

import kernel.web.Page;

public interface AdminC2cAdvertService  {
	
	public Page pagedQuery(int pageNo, int pageSize, String c2c_user_code, Integer c2c_user_type_int, String user_code, 
			String direction, String currency, String symbol, String loginPartyId);
	
}
