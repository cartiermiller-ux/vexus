package kernel.web;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

public class PagedQueryDaoImpl implements PagedQueryDao {
	
	private NamedParameterJdbcOperations namedParameterJdbcTemplate;
	
	public Page pagedQuerySQL(int pageNo, int pageSize, String queryString, Map<String, Object> parameters) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		queryString = queryString + "   limit " + (pageNo - 1) * pageSize + "," + pageSize;
		List<Map<String, Object>> list = namedParameterJdbcTemplate.queryForList(queryString.toString(), parameters);
		page.setElements(list);
		
		return page;
	}

	public void setNamedParameterJdbcTemplate(NamedParameterJdbcOperations namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}
}
