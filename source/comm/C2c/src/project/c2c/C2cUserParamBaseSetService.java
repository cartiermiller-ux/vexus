package project.c2c;

public interface C2cUserParamBaseSetService {

	public void save(C2cUserParamBaseSet entity);

	public void update(C2cUserParamBaseSet entity);

	public void delete(String c2c_user_party_id);
	
	public C2cUserParamBaseSet getByPartyId(String c2c_user_party_id);
	
}
