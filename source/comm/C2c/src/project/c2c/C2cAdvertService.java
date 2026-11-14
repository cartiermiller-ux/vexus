package project.c2c;

import java.util.List;
import java.util.Map;

import kernel.web.Page;

public interface C2cAdvertService {
	
	public Page pagedQuery(int page_no, int page_size, String c2c_user_id, String direction, String currency, String symbol, String amount, Integer on_sale, Integer closed, boolean is_c2c_user);
	
	public Page pagedQueryQuick(int pageNo, int pageSize, String currency, String symbol, String direction, double amount, double coinAmount, String method_config_id);

	public Map<String, Map<String, Object>> getBestPriceAdvert(String currency, String symbol, String direction, String order_type, double amount, double coinAmount, List<C2cPaymentMethod> cpmList);
	
	public C2cAdvert get(String id);
	
	public List<C2cAdvert> getByC2cUserId(String c2c_user_id);

	public void save(C2cAdvert entity);

	public void update(C2cAdvert entity);

	public void delete(String id);

	public C2cAdvert findById(String id);
	
	/**
	 * 获取 支付币种Map
	 */
	public Map<String, String> getCurrencyMap();

	/**
	 * 获取 上架币种Map
	 */
	public Map<String, String> getSymbolMap();
	
	public Map<String, Object> getComputeValue(double all_deposit, String currency, String symbol, double coin_amount, double symbol_value);
	
	/**
	 * 获取所有上架币种单价
	 */
	public Map<String, String> getAllSymbolPrice(String currency);
	
	/*
	 * 获取 C2C支付方式类型、C2C支付币种配置、C2C上架币种配置、C2C广告支付时效
	 */
	public Map<String, String> getC2cSyspara(String syspara);
	
	/*
	 * 获取 语种说明
	 */
	public String getLanguageIntro();
	
	/*
	 * 获取 支付方式类型说明
	 */
	public String getMethodTypeIntro();
	
}
