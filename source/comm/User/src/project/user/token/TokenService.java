package project.user.token;

public interface TokenService {

	public String savePut(String partyId,String name);

	public String cacheGet(String token);
	
	public String getUserName(String token,String url);

	public void delete(String token);
	
	public Token find(String partyId) ;
}
