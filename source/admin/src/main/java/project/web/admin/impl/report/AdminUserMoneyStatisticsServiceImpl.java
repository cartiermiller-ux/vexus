package project.web.admin.impl.report;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import kernel.util.Arith;
import kernel.util.StringUtils;
import project.Constants;
import project.data.DataService;
import project.data.model.Realtime;
import project.party.recom.UserRecomService;
import project.web.admin.service.report.AdminUserMoneyStatisticsService;

public class AdminUserMoneyStatisticsServiceImpl implements AdminUserMoneyStatisticsService{

	private DataService dataService;
	
	private UserRecomService userRecomService;
	
	private NamedParameterJdbcOperations namedParameterJdbcTemplate;
	
	private static final Logger logger=LoggerFactory.getLogger(AdminUserMoneyStatisticsServiceImpl.class);
	
	public List<Map<String,Object>> getAll(String loginPartyId){
		Map<String,Object> parameters = new HashMap<String,Object>();
		StringBuffer queryString = new StringBuffer();
		queryString.append("SELECT we.WALLETTYPE AS wallettype,IFNULL(SUM(we.AMOUNT),0) AS amount FROM T_WALLET_EXTEND we ");
		queryString.append("LEFT JOIN  PAT_PARTY party ON party.UUID=we.PARTY_ID ");
		queryString.append("WHERE 1=1 ");
		queryString.append("AND party.ROLENAME ='"+Constants.SECURITY_ROLE_MEMBER+"' ");
		if (!StringUtils.isNullOrEmpty(loginPartyId)) {
			List children = this.userRecomService.findChildren(loginPartyId);
			if (children.size() == 0) {//拓展钱包为0时，usdt依旧要计算
				List<Map<String, Object>> queryForList = new ArrayList<Map<String,Object>>();
				computeList(queryForList,loginPartyId);
				return queryForList;
			}
			queryString.append(" and we.PARTY_ID in (:children) ");
			parameters.put("children", children);
		}
		queryString.append("GROUP BY we.WALLETTYPE ");
		List<Map<String, Object>> queryForList = this.namedParameterJdbcTemplate.queryForList( queryString.toString(), parameters);
		computeList(queryForList,loginPartyId);
		return queryForList;
	}
	
	private void computeList(List<Map<String, Object>> datas,String loginPartyId) {
		if(!CollectionUtils.isEmpty(datas)) {
			for(Map<String, Object> data:datas) {
				data.put("amount",new BigDecimal(data.get("amount").toString()).setScale(8, RoundingMode.FLOOR).toPlainString());
			}
		}
		
		double sum = this.getSumWalletByMember(loginPartyId);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("wallettype", "usdt");
		map.put("amount", new BigDecimal(sum).setScale(8, RoundingMode.FLOOR).toPlainString());
		map.put("usdt_amount", sum);
		datas.add(0, map);
	}
	
	
	private List<Map<String,Object>> compute(List<Object[]> datas) {
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		for(Object[] data:datas) {
			if("usdt".equalsIgnoreCase(data[0].toString())) continue;
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("wallettype", data[0]);
			map.put("amount",data[1]);
			map.put("usdt_amount",computeUsdt(data[0].toString(),new Double(data[1].toString())));
			result.add(map);
		}
		return result;
	}
	
	private Double computeUsdt(String symbol,double amount) {
		if("usdt".equalsIgnoreCase(symbol)) return amount;
		try {
			List<Realtime> realtimes = this.dataService.realtime(symbol);
			if(CollectionUtils.isNotEmpty(realtimes)) {
				return Arith.mul(realtimes.get(0).getClose(), amount);
			}
		}catch(Exception e) {
			logger.error("compute fail ,symbol:{"+symbol+"},amount:{"+amount+"},e:",e);
		}
		return null;
	}
	
	/**
	 * 计算总金额
	 * 
	 */
	public Map<String,Object> totleDatas(List<Map<String,Object>> list){
		try {
			Map<String,Object> result = new HashMap<String,Object>();
			double sum_usdt_amount = 0D;
			for(Map<String,Object> data:list) {
				sum_usdt_amount=Arith.add(sum_usdt_amount,new Double(data.get("usdt_amount").toString()));
			}
			result.put("sum_usdt_amount", sum_usdt_amount);
			return result;
		}catch(Exception e) {
			logger.error("compute fail ,e:",e);
		}
		return null;
	}
	
	public double getSumWalletByMember(String loginPartyId) {
		Map<String,Object> parameters = new HashMap<String,Object>();
		StringBuffer queryString = new StringBuffer();
		queryString.append("SELECT IFNULL(SUM(w.MONEY),0) AS totle_money ");
		queryString.append("FROM T_WALLET w ");
		queryString.append("LEFT JOIN PAT_PARTY p ON p.UUID=w.PARTY_ID ");
		queryString.append("WHERE 1=1 ");
		queryString.append("AND p.ROLENAME ='"+Constants.SECURITY_ROLE_MEMBER+"' ");
		if (!StringUtils.isNullOrEmpty(loginPartyId)) {
			List children = this.userRecomService.findChildren(loginPartyId);
			if (children.size() == 0) {
				return 0;
			}
			queryString.append(" and w.PARTY_ID in (:children) ");
			parameters.put("children", children);
		}
		List<Map<String, Object>> queryForList = this.namedParameterJdbcTemplate.queryForList( queryString.toString(), parameters);
		return CollectionUtils.isEmpty(queryForList)?0D:new Double(queryForList.get(0).get("totle_money").toString());
	}
	public String loadExportData(HttpServletResponse response,String loginPartyId) throws IOException {
		//生成数据信息
		int sheetNum = 0;
		// 生成表头
		Integer i = 0;
		// 在内存中保持100行，超过100行将被刷新到磁盘
		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Sheet sheet = wb.createSheet(); // 表，一个表包含多个行
		Drawing patriarch = sheet.createDrawingPatriarch();
		CellStyle style = wb.createCellStyle();
		Row row = null;// 行，一行包括多个单元格
		Cell cell = null;// 单元格
		return "";
	}
	
	public List<Object[]> dataBachHandel(List<Map<String,Object>> list){
		List<Object[]> result = new ArrayList<Object[]>();
		for(Map<String,Object> data:list) {
			Object[] objs = new Object[3];
			objs[0] = data.get("wallettype");
			objs[1] = data.get("amount");
			objs[2] = data.get("usdt_amount");
			result.add(objs);
		}
		return result;
	}

	public void setNamedParameterJdbcTemplate(NamedParameterJdbcOperations namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}
}
