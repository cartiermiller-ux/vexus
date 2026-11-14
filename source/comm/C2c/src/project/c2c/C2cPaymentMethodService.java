package project.c2c;

import java.util.List;
import java.util.Map;

public interface C2cPaymentMethodService {
	
	public C2cPaymentMethod get(String id);
	
	public C2cPaymentMethod getC2cPaymentMethod(String id);
	
	public Map<String, C2cPaymentMethod> getByPartyId(String partyId);
	
	public Map<String, String> getMethodConfigMapByPartyId(String partyId);
	
	public List<C2cPaymentMethod> getMethodConfigListByPartyId(String partyId);
	
	public Map<String, String> getMethodIdTypeMap();

	public void save(C2cPaymentMethod entity);
	
	public void delete(String id);
	
	public boolean update(C2cPaymentMethod entity);
	
}
