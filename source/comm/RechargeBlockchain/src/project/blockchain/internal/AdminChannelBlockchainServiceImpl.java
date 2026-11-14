package project.blockchain.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import kernel.web.PagedQueryDao;
import project.Constants;
import project.blockchain.AdminChannelBlockchainService;

@SuppressWarnings("rawtypes")
public class AdminChannelBlockchainServiceImpl implements AdminChannelBlockchainService {
	
	private PagedQueryDao pagedQueryDao;

	public Page pagedQuery(int pageNo, int pageSize, String name_para, String coin_para) {
		StringBuffer queryString = new StringBuffer(
				" SELECT channelblockchain.UUID id,channelblockchain.BLOCKCHAIN_NAME blockchain_name,"
						+ "channelblockchain.IMG img ,channelblockchain.COIN coin,  "
						+ " channelblockchain.ADDRESS address ");

		queryString.append(" FROM T_CHANNEL_BLOCKCHAIN channelblockchain WHERE 1 = 1 ");
		Map<String, Object> parameters = new HashMap<>();
		if (!StringUtils.isNullOrEmpty(name_para)) {
			queryString.append(" and  channelblockchain.BLOCKCHAIN_NAME like :name ");
			parameters.put("name", "%" + name_para + "%");
		}
		if (!StringUtils.isNullOrEmpty(coin_para)) {
			queryString.append(" and  channelblockchain.COIN like :coin ");
			parameters.put("coin", "%" + coin_para + "%");
		}
		Page page = this.pagedQueryDao.pagedQuerySQL(pageNo, pageSize, queryString.toString(), parameters);
		return page;
	}

	public void setPagedQueryDao(PagedQueryDao pagedQueryDao) {
		this.pagedQueryDao = pagedQueryDao;
	}
	
	/**
	 * 查找用户自定义的充值地址表
	 * @param pageNo 页号
	 * @param pageSize 页面尺寸
	 * @param userName 用户名
	 * @param chainName 链名
	 * @param coinSymbol 币种
	 * @param auto 自动/手动到账
	 * @return 列表页面
	 */
	public Page pagedQuery(int pageNo, int pageSize, String userName, String roleName,String chainName,String coinSymbol,String address) {
		if (pageNo <= 0) pageNo = 1;
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		
		ArrayList<Object> paramList=new ArrayList<Object>();
		StringBuilder sqlBuilder=new StringBuilder("SELECT PB.*,PP.USERCODE,PP.ROLENAME FROM T_PARTY_BLOCKCHAIN PB INNER JOIN PAT_PARTY PP ON PB.USER_NAME=PP.USERNAME WHERE 1=1 ");
		
		if(null!=address && !(address=address.trim()).isEmpty()) {
			sqlBuilder.append("AND ADDRESS=? ");
			paramList.add(address);
		}
		
		if(null!=roleName && !(roleName=roleName.trim()).isEmpty()) {
			sqlBuilder.append("AND ROLENAME=? ");
			paramList.add(roleName);
		}
		
		if(null!=chainName && !(chainName=chainName.trim()).isEmpty()) {
			sqlBuilder.append("AND CHAIN_NAME=? ");
			paramList.add(chainName);
		}
		
		if(null!=coinSymbol && !(coinSymbol=coinSymbol.trim()).isEmpty()) {
			sqlBuilder.append("AND COIN_SYMBOL=? ");
			paramList.add(coinSymbol);
		}
		
		if(null!=userName && !(userName=userName.trim()).isEmpty()) {
			sqlBuilder.append("AND (USER_NAME LIKE ? OR USERCODE=?) ");
			paramList.add("%"+userName+"%");
			paramList.add(userName);
		}
		
		sqlBuilder.append("ORDER BY CREATE_TIME DESC LIMIT ?,?");
		paramList.add(page.getFirstElementNumber());
		paramList.add(pageSize);
		
		List<HashMap> list=ApplicationUtil.executeDQL(sqlBuilder.toString(),paramList.toArray(new Object[paramList.size()]),HashMap.class);
		list.forEach(map->{
			Object roleNameKey=map.get("ROLENAME");
			String roleNameDesc=Constants.ROLE_MAP.get(roleNameKey);
			map.put("roleNameDesc",null==roleNameDesc?roleNameKey:roleNameDesc);
		});
		
		page.setElements(list);
		return page;
	}
}
