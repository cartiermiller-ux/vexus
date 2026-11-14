package project.c2c;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import kernel.web.Page;

public interface AdminC2cUserService  {

	public Page pagedQuery(int pageNo, int pageSize, String c2c_user_id, Integer c2c_user_type_int, String c2c_user_party_id, String c2c_manager_name_para, String loginPartyId);
	
	public void save(C2cUser c2cUser) ;
	
	public void update(C2cUser c2cUser) ;
	
	public void delete(String id);
	
	public C2cUser findById(Serializable id);
	
	/*
	 * 获取C2C管理员下级承兑商
	 */
	public List<Map<String, Object>> getC2cManagerC2cUser(String manager_id);
	
	/*
	 * 获取所有C2C管理员
	 */
	public Map<String, Object> getAllC2cManager();
	
}
