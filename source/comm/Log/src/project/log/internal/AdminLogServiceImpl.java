package project.log.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.log.AdminLogService;
import project.party.recom.UserRecomService;

@SuppressWarnings("rawtypes")
public class AdminLogServiceImpl implements AdminLogService {
	
	private UserRecomService userRecomService;
	
	@Override
	public Page pagedQueryMoneyLog(int pageNo, int pageSize, String log_para,String name_para,String loginPartyId,String rolename_para,String startTime,String endTime) {
		if (pageNo<=0) pageNo=1;
		Page page = new Page(pageNo,pageSize,Integer.MAX_VALUE);
		
		ArrayList<Object> paramList=new ArrayList<Object>();
		StringBuilder sqlBuilder = new StringBuilder("SELECT party.USERNAME username,party.ROLENAME rolename,party.USERCODE usercode,party_parent.USERNAME username_parent,");
		sqlBuilder.append("money.UUID money_id,money.LOG log,money.SHOW_STATUS showStatus,money.WALLETTYPE wallettype,money.AMOUNT_BEFORE amount_before,money.AMOUNT amount,money.AMOUNT_AFTER amount_after,money.CREATE_TIME createTime,money.WALLETTYPE wallettype ");
		sqlBuilder.append("FROM T_MONEY_LOG money LEFT JOIN PAT_PARTY party ON money.PARTY_ID = party.UUID LEFT JOIN PAT_USER_RECOM user ON user.PARTY_ID = party.UUID LEFT JOIN PAT_PARTY party_parent ON user.RECO_ID = party_parent.UUID WHERE 1=1 ");
		
		if (!StringUtils.isNullOrEmpty(log_para)) {
			sqlBuilder.append("AND money.LOG LIKE ? ");
			paramList.add("%"+log_para+"%");
		}
		
		if (!StringUtils.isNullOrEmpty(name_para)) {
			sqlBuilder.append("AND (party.USERNAME=? OR party.USERCODE=?) ");
			paramList.add(name_para);
			paramList.add(name_para);
		}
		
		if (!StringUtils.isNullOrEmpty(rolename_para)) {
			sqlBuilder.append("AND party.ROLENAME=? ");
			paramList.add(rolename_para);
		}
		
		if (!StringUtils.isNullOrEmpty(startTime)) {
			sqlBuilder.append("AND DATE(money.CREATE_TIME)>=DATE('"+startTime+" 00:00:00') ");
		}
		
		if (!StringUtils.isNullOrEmpty(endTime)) {
			sqlBuilder.append("AND DATE(money.CREATE_TIME)<=DATE('"+endTime+" 23:59:59') ");
		}

		if (!StringUtils.isNullOrEmpty(loginPartyId)) {
			sqlBuilder.append("AND FIND_IN_SET(party.UUID,?) ");
			List<String> childrens = userRecomService.findChildren(loginPartyId);
			childrens.add(loginPartyId);
			paramList.add(String.join(",", childrens));
		}
		
		sqlBuilder.append("ORDER BY money.CREATE_TIME DESC,money.UUID DESC LIMIT ?,?");
		paramList.add(page.getFirstElementNumber());
		paramList.add(pageSize);
		
		List<HashMap> list=ApplicationUtil.executeDQL(sqlBuilder.toString(),paramList.toArray(new Object[paramList.size()]),HashMap.class);
		page.setElements(list);
		return page;
	}

	public Page pagedQueryLog(int pageNo, int pageSize, String log_para, String name_para, String category,String operator, Date createTime_begin, Date createTime_end, String loginPartyId,String loginUsername, String rolename_para) {
		StringBuilder sqlBuilder = new StringBuilder("SELECT party.NAME name,party.ROLENAME rolename,party.USERCODE usercode,");
		sqlBuilder.append("log.UUID log_id,log.LOG log,log.USERNAME username,log.OPERATOR operator,log.CATEGORY category,log.CREATE_TIME createTime ");
		sqlBuilder.append("FROM T_LOG log LEFT JOIN PAT_PARTY party ON log.USERNAME = party.USERNAME WHERE 1=1 ");
		ArrayList<Object> paramList=new ArrayList<Object>();
		if (pageNo<=0) pageNo=1;
		
		if (!StringUtils.isNullOrEmpty(log_para)) {
			sqlBuilder.append("AND log.LOG LIKE ? ");
			paramList.add("%"+log_para+"%");
		}
		
		if (!StringUtils.isNullOrEmpty(name_para)) {
			sqlBuilder.append("AND (party.USERNAME=? OR party.USERCODE=? OR log.USERNAME=?) ");
			paramList.add(name_para);
			paramList.add(name_para);
			paramList.add(name_para);
		}
		
		if (!StringUtils.isNullOrEmpty(category)) {
			sqlBuilder.append("AND log.CATEGORY=? ");
			paramList.add(category);
		}
		
		if (!StringUtils.isNullOrEmpty(operator)) {
			sqlBuilder.append("AND log.OPERATOR=? ");
			paramList.add(operator);
		}
		
		if (!"root".equals(loginUsername)) {
			sqlBuilder.append("AND log.USERNAME!='root' AND (log.OPERATOR!='root' OR log.OPERATOR IS NULL) ");
		}
		
		if (null!=createTime_begin) {
			sqlBuilder.append("AND log.CREATE_TIME>=? ");
			paramList.add(createTime_begin);
		}

		if (null!=createTime_begin) {
			sqlBuilder.append("AND log.CREATE_TIME<? ");
			paramList.add(createTime_end);
		}
		
		if (!StringUtils.isNullOrEmpty(loginPartyId)) {
			sqlBuilder.append("AND log.PARTY_ID=? ");
			paramList.add(loginPartyId);
		}
		
		if (!StringUtils.isNullOrEmpty(rolename_para)) {
			sqlBuilder.append("AND party.ROLENAME=? ");
			paramList.add(rolename_para);
		}
		
		sqlBuilder.append("ORDER BY log.CREATE_TIME DESC LIMIT ?,?");
		Page page = new Page(pageNo,pageSize,Integer.MAX_VALUE);
		paramList.add(page.getFirstElementNumber());
		paramList.add(pageSize);
		
		List<HashMap> list=ApplicationUtil.executeDQL(sqlBuilder.toString(),paramList.toArray(new Object[paramList.size()]),HashMap.class);
		page.setElements(list);
		return page;
	}
	
	public  Page pagedQueryCodeLog(int pageNo, int pageSize, String log_para,String name_para,String target,Date createTime_begin,Date createTime_end, String loginPartyId,String loginUsername,String id_para) {
		StringBuilder sqlBuilder = new StringBuilder("SELECT party.NAME name,log.UUID log_id,log.LOG log,log.USERNAME username,log.TARGET target,log.CREATE_TIME createTime ");
		sqlBuilder.append("FROM T_CODE_LOG log LEFT JOIN PAT_PARTY party ON log.PARTY_ID = party.UUID WHERE 1=1 ");
		ArrayList<Object> paramList=new ArrayList<Object>();
		if (pageNo<=0) pageNo=1;
		
		if (!StringUtils.isNullOrEmpty(id_para)) {
			sqlBuilder.append("AND log.UUID=? ");
			paramList.add(id_para);
		}

		if (!StringUtils.isNullOrEmpty(log_para)) {
			sqlBuilder.append("AND log.LOG LIKE ? ");
			paramList.add("%"+log_para+"%");
		}
		
		if (!StringUtils.isNullOrEmpty(name_para)) {
			sqlBuilder.append("AND (party.NAME=? OR log.USERNAME=?) ");
			paramList.add(name_para);
			paramList.add(name_para);
		}

		if (!StringUtils.isNullOrEmpty(target)) {
			sqlBuilder.append("AND log.TARGET LIKE ? ");
			paramList.add("%"+target+"%");
		}

		if (null!=createTime_begin) {
			sqlBuilder.append("AND log.CREATE_TIME>=? ");
			paramList.add(createTime_begin);
		}

		if (null!=createTime_end) {
			sqlBuilder.append("AND log.CREATE_TIME<? ");
			paramList.add(createTime_end);
		}
		
		if (!StringUtils.isNullOrEmpty(loginPartyId)) {
			sqlBuilder.append("AND log.PARTY_ID=? ");
			paramList.add(loginPartyId);
		}
		
		sqlBuilder.append("ORDER BY log.CREATE_TIME DESC LIMIT ?,?");
		Page page = new Page(pageNo,pageSize,Integer.MAX_VALUE);
		paramList.add(page.getFirstElementNumber());
		paramList.add(pageSize);
		
		List<HashMap> list=ApplicationUtil.executeDQL(sqlBuilder.toString(),paramList.toArray(new Object[paramList.size()]),HashMap.class);
		page.setElements(list);
		return page;
	}
	
	

	
	@Override
	public Page pagedQuerySysLog(int pageNo, int pageSize, String log_para, String level_para, String category_para,Date createTime_begin, Date createTime_end) {
		StringBuilder sqlBuilder = new StringBuilder("SELECT log.UUID log_id,log.LOG log,log.LEVEL level,log.CATEGORY category,log.EXTRA extra,log.CREATE_TIME createTime FROM T_SYSLOG log WHERE 1=1 ");
		ArrayList<Object> paramList=new ArrayList<Object>();
		if (pageNo<=0) pageNo=1;
		
		if (!StringUtils.isNullOrEmpty(log_para)) {
			sqlBuilder.append("AND log.LOG LIKE ? ");
			paramList.add("%"+log_para+"%");
		}
		
		if (!StringUtils.isNullOrEmpty(level_para)) {
			sqlBuilder.append("AND log.LEVEL=? ");
			paramList.add(level_para);
		}

		if (!StringUtils.isNullOrEmpty(category_para)) {
			sqlBuilder.append("AND log.CATEGORY=? ");
			paramList.add(category_para);
		}
		
		if (null!=createTime_begin) {
			sqlBuilder.append("AND log.CREATE_TIME>=? ");
			paramList.add(createTime_begin);
		}

		if (null!=createTime_end) {
			sqlBuilder.append("AND log.CREATE_TIME<? ");
			paramList.add(createTime_end);
		}
		
		sqlBuilder.append("ORDER BY log.CREATE_TIME DESC LIMIT ?,?");
		Page page = new Page(pageNo,pageSize,Integer.MAX_VALUE);
		paramList.add(page.getFirstElementNumber());
		paramList.add(pageSize);
		
		List<HashMap> list=ApplicationUtil.executeDQL(sqlBuilder.toString(),paramList.toArray(new Object[paramList.size()]),HashMap.class);
		page.setElements(list);
		return page;
	}
	
	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}
}
