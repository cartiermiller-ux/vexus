package project.cms.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.cms.AdminBannerService;
import project.cms.Banner;

public class AdminBannerServiceImpl implements AdminBannerService {

	public Page pagedQuery(int pageNo, int pageSize, String language) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		StringBuilder whereBuilder=new StringBuilder("WHERE 1=1 ");
		ArrayList<Object> params=new ArrayList<Object>();
		
		if (StringUtils.isNotEmpty(language)) {
			whereBuilder.append("AND LANGUAGE=? ");
			params.add(language);
		}
		
		whereBuilder.append("ORDER BY SORT_INDEX ASC,CREATE_TIME DESC LIMIT ?,?");
		params.add(page.getFirstElementNumber());
		params.add(pageSize);
		
		List<Banner> list=ApplicationUtil.executeSelect(Banner.class,whereBuilder.toString(),params.toArray(new Object[params.size()]));
		page.setElements(list);
		return page;
	}
}
