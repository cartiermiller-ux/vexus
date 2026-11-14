package project.c2c;

import java.util.List;

public interface C2cTranslateService {
	
	public C2cTranslate get(String content, String language);
	
	public void save(C2cTranslate entity);

	public void update(C2cTranslate entity);

	public void delete(String content, String language);
	
	public void saveTranslate(String content, String langTransStr);
	
	public String getTranslate(String content);
	
	public C2cPaymentMethodConfig translatePmc(C2cPaymentMethodConfig config, String language);
	
	public C2cPaymentMethod translatePm(C2cPaymentMethod cpm, String language);
	
	public C2cOrder translateOrder(C2cOrder order, String language);
	
	public List<String> getAllPaymentMethodTypeName();
	
}
