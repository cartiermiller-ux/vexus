package project.c2c;

import kernel.web.Page;

public interface AdminC2cPaymentMethodConfigService {

	public Page pagedQuery(int pageNo, int pageSize, String method_type, String method_name);

}
