package project.c2c.internal;

import java.util.HashMap;
import java.util.Map;

import kernel.util.StringUtils;
import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.c2c.AdminC2cAdvertService;

public class AdminC2cAdvertServiceImpl implements AdminC2cAdvertService {

	private PagedQueryDao pagedDao;

	public Page pagedQuery(int pageNo, int pageSize, String c2c_user_code, Integer c2c_user_type_int, String user_code, 
			String direction, String currency, String symbol, String loginPartyId) {

		StringBuffer queryString = new StringBuffer();
		
		queryString.append(" SELECT ");
		queryString.append(" advert.UUID id, advert.DIRECTION direction, advert.CURRENCY currency, advert.SYMBOL symbol, advert.SYMBOL_CLOSE symbol_close, advert.PAY_RATE pay_rate, advert.PAY_TYPE pay_type, ");
		queryString.append(" advert.SYMBOL_VALUE symbol_value, advert.COIN_AMOUNT coin_amount, advert.INVESTMENT_MIN investment_min, advert.INVESTMENT_MAX investment_max, ");
		queryString.append(" advert.DEPOSIT deposit, advert.DEPOSIT_OPEN deposit_open, advert.ON_SALE on_sale, advert.CLOSED closed, advert.SORT_INDEX sort_index, ");
		queryString.append(" advert.EXPIRE_TIME expire_time, advert.TRANSACTION_TERMS transaction_terms, advert.ORDER_MSG order_msg, ");
		queryString.append(" advert.REMARK remark, advert.CREATE_TIME create_time, advert.UPDATE_TIME update_time, ");
		queryString.append(" c2c_user.UUID c2c_user_id, c2c_user.NICK_NAME nick_name, c2c_user.HEAD_IMG head_img, c2c_user.C2C_USER_TYPE c2c_user_type, c2c_user.C2C_USER_CODE c2c_user_code, ");
		queryString.append(" party.UUID party_id, party.USERCODE user_code, party.USERNAME user_name ");
		
		queryString.append(" FROM T_C2C_ADVERT advert ");
		queryString.append(" LEFT JOIN T_C2C_USER c2c_user ON c2c_user.UUID = advert.C2C_USER_ID ");
		queryString.append(" LEFT JOIN PAT_PARTY party ON party.UUID = c2c_user.C2C_USER_PARTY_ID ");		
		queryString.append(" WHERE 1=1 ");

		Map<String, Object> parameters = new HashMap<String, Object>();

		if (StringUtils.isNotEmpty(c2c_user_code)) {
			queryString.append(" AND (c2c_user.NICK_NAME like :c2c_user_code OR c2c_user.C2C_USER_CODE like :c2c_user_code) ");
			parameters.put("c2c_user_code", "%" + c2c_user_code + "%");
		}

		if (c2c_user_type_int != null) {
			queryString.append(" and c2c_user.C2C_USER_TYPE = :c2c_user_type_int  ");
			parameters.put("c2c_user_type_int", c2c_user_type_int);
		}

		if (StringUtils.isNotEmpty(user_code)) {
			queryString.append(" AND (party.USERNAME like :user_code OR party.USERCODE like :user_code) ");
			parameters.put("user_code", "%" + user_code + "%");
		}
		
		if (StringUtils.isNotEmpty(direction)) {
			queryString.append(" AND advert.DIRECTION = :direction ");
			parameters.put("direction", direction);
		}
		
		if (StringUtils.isNotEmpty(currency)) {
			queryString.append(" AND advert.CURRENCY = :currency ");
			parameters.put("currency", currency);
		}
		
		if (StringUtils.isNotEmpty(symbol)) {
			queryString.append(" AND advert.SYMBOL = :symbol ");
			parameters.put("symbol", symbol);
		}

		if (StringUtils.isNotEmpty(loginPartyId)) {
			queryString.append(" AND c2c_user.C2C_MANAGER_PARTY_ID = :loginPartyId ");
			parameters.put("loginPartyId", loginPartyId);
		}
		
		queryString.append(" order by advert.SORT_INDEX desc,advert.CREATE_TIME desc ");

		Page page = this.pagedDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		
		return page;
	}
	
	public void setPagedDao(PagedQueryDao pagedDao) {
		this.pagedDao = pagedDao;
	}

}
