package project.c2c.internal;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.c2c.AdminC2cPaymentMethodConfigService;

public class AdminC2cPaymentMethodConfigServiceImpl implements AdminC2cPaymentMethodConfigService {
	
	private PagedQueryDao pagedDao;

	public Page pagedQuery(int pageNo, int pageSize, String method_type, String method_name) {
		
		StringBuffer queryString = new StringBuffer();
		
		queryString.append(" SELECT ");
		queryString.append(" cfg.UUID id, cfg.METHOD_TYPE method_type, cfg.METHOD_NAME method_name, cfg.METHOD_IMG method_img, ");
		queryString.append(" cfg.PARAM_NAME1 param_name1, cfg.PARAM_NAME2 param_name2, cfg.PARAM_NAME3 param_name3, cfg.PARAM_NAME4 param_name4, cfg.PARAM_NAME5 param_name5, ");
		queryString.append(" cfg.PARAM_NAME6 param_name6, cfg.PARAM_NAME7 param_name7, cfg.PARAM_NAME8 param_name8, cfg.PARAM_NAME9 param_name9, cfg.PARAM_NAME10 param_name10, ");
		queryString.append(" cfg.PARAM_NAME11 param_name11, cfg.PARAM_NAME12 param_name12, cfg.PARAM_NAME13 param_name13, cfg.PARAM_NAME14 param_name14, cfg.PARAM_NAME15 param_name15, ");
		queryString.append(" cfg.CREATE_TIME create_time, cfg.UPDATE_TIME update_time ");
		
		queryString.append(" FROM T_C2C_PAYMENT_METHOD_CONFIG cfg ");
		queryString.append(" WHERE 1=1 ");

		Map<String, Object> parameters = new HashMap<String, Object>();
		
		if (StringUtils.isNotEmpty(method_type)) {
			queryString.append(" AND cfg.METHOD_TYPE =:method_type ");
			parameters.put("method_type", method_type);
		}
		
		if (StringUtils.isNotEmpty(method_name)) {
			queryString.append(" AND cfg.METHOD_NAME like :method_name ");
			parameters.put("method_name", "%" + method_name + "%");
		}

		queryString.append(" order by cfg.CREATE_TIME desc ");
		
		Page page = this.pagedDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		return page;
	}

	public void setPagedDao(PagedQueryDao pagedDao) {
		this.pagedDao = pagedDao;
	}

}
