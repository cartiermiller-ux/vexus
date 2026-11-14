package kernel.web;

import java.util.Map;

public interface PagedQueryDao {
	/**
	 * SQL分页 不计算分页标签
	 */
	public Page pagedQuerySQL(int pageNo, int pageSize, String queryString, Map<String, Object> parameters);
}
