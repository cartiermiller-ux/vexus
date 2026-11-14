package project.c2c;

import java.io.Serializable;

import kernel.web.Page;

public interface AdminC2cPaymentMethodService {

	public C2cPaymentMethod findById(Serializable id);

	public void save(C2cPaymentMethod entity);

	public void delete(String id);

	public void update(C2cPaymentMethod entity);

	public Page pagedQuery(int pageNo, int pageSize, String user_code, Integer method_type_int, String method_name, String loginPartyId);

}
