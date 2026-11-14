package project.c2c;

import java.util.List;
import java.util.Map;

public interface C2cPaymentMethodConfigService {
	
	public C2cPaymentMethodConfig get(String id);
	
	public C2cPaymentMethodConfig getC2cPaymentMethodConfig(String id);
	
	public Map<String, String> getMethodConfigIdTypeMap();

	public void save(C2cPaymentMethodConfig entity);
	
	public void delete(String id);
	
	public boolean update(C2cPaymentMethodConfig entity);
	
	public List<C2cPaymentMethodConfig> getAll();
	
	public Map<String, String> getMethodConfigMap();
	
}
