package project.c2c;

import java.util.List;

public interface C2cUserService {

	public void save(C2cUser entity);

	public void update(C2cUser entity);

	public void delete(String id);
	
	public C2cUser get(String id);
	
	public C2cUser getByPartyId(String partyId);
	
	public C2cUser findByUsercode(String usercode);
	
	public List<C2cUser> findAll();
	
}
