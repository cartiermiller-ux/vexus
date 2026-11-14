package project.hobi.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.data.model.Symbols;
import project.hobi.AdminSymbolsService;
import project.hobi.HobiDataService;;

public class AdminSymbolsServiceImpl implements AdminSymbolsService {
	
	private HobiDataService hobiDataService;
	
	private PagedQueryDao pagedQueryDao;

	@Override
	public void saveReload() {
		List<Symbols> list = hobiDataService.symbols();
		if (list.size() > 0) {
			this.delete(this.getAll());
			for (int i = 0; i < list.size(); i++) {
				ApplicationUtil.executeSaveOrUpdate(list.get(i));
			}
		}
	}

	private List<Symbols> getAll() {
		return ApplicationUtil.executeSelect(Symbols.class);
	}
	
	@Override
	public Page pagedQuery(int pageNo, int pageSize, String quote_currency, String base_currency) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		StringBuilder whereSql=new StringBuilder("WHERE 1=1 ");
		ArrayList<Object> params=new ArrayList<Object>();
		
		if (!StringUtils.isNullOrEmpty(quote_currency)) {
			whereSql.append("AND QUOTE_CURRENCY=? ");
			params.add(quote_currency);
		}

		if (!StringUtils.isNullOrEmpty(base_currency)) {
			whereSql.append("AND BASE_CURRENCY=? ");
			params.add(base_currency);
		}
		
		whereSql.append("LIMIT ?,?");
		params.add(page.getFirstElementNumber());
		params.add(pageSize);
		
		List<Symbols> list=ApplicationUtil.executeSelect(Symbols.class,whereSql.toString(),params.toArray(new Object[] {params.size()}));
		page.setElements(list);
		return page;
	}
	
	@Override
	public Page pagedQuery(int pageNo, int pageSize) {
		StringBuffer queryString = new StringBuffer(" SELECT DISTINCT(QUOTE_CURRENCY)  ");
		queryString.append(" FROM T_SYMBOLS  WHERE 1 = 1 ");
		Map parameters = new HashMap();
		Page page = this.pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		return page;
	}
	
	@Override
	public Page pagedQueryMap(int pageNo, int pageSize) {
		StringBuffer queryString = new StringBuffer(" SELECT DISTINCT(QUOTE_CURRENCY) quote_currency ");
		queryString.append(" FROM T_SYMBOLS  WHERE 1 = 1 ");
		Map parameters = new HashMap();
		Page page = this.pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		return page;
	}

	private void delete(List<Symbols> list) {
		ApplicationUtil.executeBatchDelete(list);
	}

	public void setHobiDataService(HobiDataService hobiDataService) {
		this.hobiDataService = hobiDataService;
	}

	public void setPagedQueryDao(PagedQueryDao pagedQueryDao) {
		this.pagedQueryDao = pagedQueryDao;
	}
}
