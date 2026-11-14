package project.hobi.internal;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.data.model.Symbols;
import project.hobi.AdminContractSymbolsService;
import project.hobi.HobiDataService;;

public class AdminContractSymbolsServiceImpl implements AdminContractSymbolsService {
	
	private HobiDataService hobiDataService;
	
	@Override
	public void saveReload() {
		List<Symbols> list = hobiDataService.symbols();
		if (list.size() <=0) return;
		
		ApplicationUtil.executeSQL("TRUNCATE TABLE T_SYMBOLS");
		
		list.forEach(symbols->{if(null==symbols.getId())symbols.setId(ApplicationUtil.getCurrentTimeUUID());});
		ApplicationUtil.executeBatchInsert(list);
	}

	@Override
	public Page pagedQuery(int pageNo, int pageSize, String quote_currency, String base_currency) {
		if (StringUtils.isNullOrEmpty(quote_currency)) {
			String tmp=null;
			List<String> list=ApplicationUtil.getBean(JdbcTemplate.class).queryForList("SELECT DISTINCT(QUOTE_CURRENCY) FROM T_SYMBOLS LIMIT 1",String.class);
			if(null!=list && !list.isEmpty() && null!=(tmp=list.get(0)) && !(tmp=tmp.trim()).isEmpty()) quote_currency=tmp;
		}
		
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		List<Symbols> list=ApplicationUtil.executeSelect(Symbols.class,"WHERE QUOTE_CURRENCY=? AND BASE_CURRENCY LIKE ? LIMIT ?,?",
				new Object[] {quote_currency,"%"+base_currency+"%",page.getFirstElementNumber(),pageSize});
		page.setElements(list);
		
		return page;
	}

	public List<String> getQuoteList() {
		return ApplicationUtil.getBean(JdbcTemplate.class).queryForList("SELECT DISTINCT(QUOTE_CURRENCY) FROM T_SYMBOLS", String.class);
	}

	public void setHobiDataService(HobiDataService hobiDataService) {
		this.hobiDataService = hobiDataService;
	}
}
