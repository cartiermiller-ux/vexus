package project.c2c;

import java.util.List;
import java.util.Map;

import kernel.web.Page;

public interface AdminC2cOrderService {

	public Page pagedQuery(int pageNo, int pageSize, Integer status_int, String order_no_para, String user_code_para, String rolename_para, 
			String c2c_user_code_para, Integer c2c_user_type_int, String c2c_user_party_code_para, String direction_para, String loginPartyId);
		
	public void savePass(C2cOrder c2cOrder, String safeword, String operator_username);
	
	public void saveOrderPay(String order_no, String safeword, String operator_username, String payment_method_id_order_pay);
	
	public List<C2cPaymentMethod> getOrderPayments(String order_no);
	
//	public Map<String, Object> getAdPayments(String id);
	
	public Map<String, Object> detail(C2cOrder crder);
	
}
