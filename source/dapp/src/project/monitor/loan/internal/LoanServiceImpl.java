package project.monitor.loan.internal;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.jdbc.core.JdbcTemplate;

import kernel.exception.BusinessException;
import kernel.web.ApplicationUtil;
import kernel.web.Page;
import project.Constants;
import project.monitor.loan.LoanParam;
import project.monitor.loan.LoanService;
import project.monitor.loan.SimpleLoanOrder;
import project.party.model.Party;

/**
 * @author JORGE
 * @description 借贷服务接口实现类
 */
@SuppressWarnings("unchecked")
public class LoanServiceImpl implements LoanService{
	/**
	 * 借贷状态字典
	 */
	private static HashMap<Integer,String> stateMap;
	
	/**
	 * 付款方式字典
	 */
	private static HashMap<Integer,String> repayments;
	
	/**
	 * 逗号正则表达式
	 */
	private static Pattern commaPattern=Pattern.compile(",");
	
	/**
	 * 冒号正则表达式
	 */
	private static Pattern colonPattern=Pattern.compile(":");
	
	/**
	 * 放款机构字典
	 */
	private static HashMap<Integer,String> lendingInstitutions;
	
	public LoanServiceImpl() {
		stateMap=new HashMap<Integer,String>();
		repayments=new HashMap<Integer,String>();
		lendingInstitutions=new HashMap<Integer,String>();
		
		stateMap.put(1, "未审");
		stateMap.put(2, "待还款");
		stateMap.put(3, "驳回");
		stateMap.put(4, "已逾期");
		stateMap.put(5, "已还款");
		
		repayments.put(1,"到期还本息");
		repayments.put(2,"到期还本金");
		repayments.put(3,"到期还利息");
		
		lendingInstitutions.put(1,"LOAN1");
		lendingInstitutions.put(2,"LOAN2");
		lendingInstitutions.put(3,"LOAN3");
	}

	@Override
	public Boolean addLoanOrder(SimpleLoanOrder simpleLoanOrder) {
		Object primaryKey=ApplicationUtil.executeInsert(simpleLoanOrder);
		return null!=primaryKey;
	}
	
	@Override
	public List<LoanParam> getLoanParamList(String uuid){
		List<LoanParam> loanParamList = new ArrayList<>();
		if(StringUtils.isBlank(uuid)){
		loanParamList = ApplicationUtil.executeSelect(LoanParam.class);
		}else {
			loanParamList = ApplicationUtil.executeSelect(LoanParam.class,"WHERE UUID=?",new Object[] {uuid});
	}
	return loanParamList;
}

	@Override
	public HashMap<String,Object> getLoanParams(String uuid) {
		LoanParam loanParam=null;
		if(null==uuid || (uuid=uuid.trim()).isEmpty()) {
			List<LoanParam> loanParamList=ApplicationUtil.executeSelect(LoanParam.class);
			loanParam=(null==loanParamList || loanParamList.isEmpty())?null:loanParamList.get(0);
		}else {
			loanParam=ApplicationUtil.executeGet(uuid, LoanParam.class);
		}
		
		if(null==loanParam) return null;
		
		HashMap<String,Object> paramMap=new HashMap<String,Object>(BeanMap.create(loanParam));
		String term=(String)paramMap.get("term");
		if(null==term || (term=term.trim()).isEmpty()) {
			paramMap.put("termList",new Object[0]);
		}else {
			paramMap.put("termList",commaPattern.splitAsStream(term).map(tr->new String[] {tr,tr}).collect(Collectors.toList()));
		}
		
		String repayCycle=(String)paramMap.get("repayCycle");
		if(null==repayCycle || (repayCycle=repayCycle.trim()).isEmpty()) {
			paramMap.put("repayCycleList",new Object[0]);
		}else {
			paramMap.put("repayCycleList",commaPattern.splitAsStream(repayCycle).map(rc->new String[] {rc,rc}).collect(Collectors.toList()));
		}
		
		String dailyRate=(String)paramMap.get("dailyRate");
		if(null==dailyRate || (dailyRate=dailyRate.trim()).isEmpty()) {
			paramMap.put("dailyRateList",new Object[0]);
		}else {
			paramMap.put("dailyRateList",commaPattern.splitAsStream(dailyRate).map(dr->new String[] {dr,dr}).collect(Collectors.toList()));
		}
		
		String repayment=(String)paramMap.get("repayment");
		if(null==repayment || (repayment=repayment.trim()).isEmpty()) {
			paramMap.put("repaymentList",new Object[0]);
		}else {
			paramMap.put("repaymentList",commaPattern.splitAsStream(repayment).map(rep->colonPattern.split(rep)).collect(Collectors.toList()));
		}
		
		String lendingInstitution=(String)paramMap.get("lendingInstitution");
		if(null==lendingInstitution || (lendingInstitution=lendingInstitution.trim()).isEmpty()) {
			paramMap.put("lendingList",new Object[0]);
		}else {
			paramMap.put("lendingList",commaPattern.splitAsStream(lendingInstitution).map(led->colonPattern.split(led)).collect(Collectors.toList()));
		}
		
		paramMap.put("stateList",commaPattern.splitAsStream("1:未审,2:通过,3:驳回").map(stat->colonPattern.split(stat)).collect(Collectors.toList()));
		
		return paramMap;
	}

	@Override
	public Page pagedQuery(Map<String, Object> queryParams) {
		int pageSize=(Integer)queryParams.get("pageSize");
		int pageNo=(Integer)queryParams.get("pageNo");
		if (pageNo <= 0) pageNo = 1;
		
		Page page = new Page(pageNo, pageSize, Integer.MAX_VALUE);
		String userName=(String)queryParams.get("userName");
		String orderNo=(String)queryParams.get("orderNo");
		int status=(Integer)queryParams.get("status");
		
		ArrayList<Object> paramList=new ArrayList<Object>();
		StringBuilder whereStatement=new StringBuilder("WHERE 1=1 ");
		
		if(0!=status) {
			whereStatement.append("AND STATE=? ");
			paramList.add(status);
		}
		
		if(null!=orderNo && !(orderNo=orderNo.trim()).isEmpty()) {
			whereStatement.append("AND UUID=? ");
			paramList.add(orderNo);
		}
		
		if(null!=userName && !(userName=userName.trim()).isEmpty()) {
			whereStatement.append("AND PARTY_ID=(SELECT UUID FROM PAT_PARTY WHERE USERNAME=?) ");
			paramList.add(userName);
		}
		
		whereStatement.append("ORDER BY CREATE_TIME DESC LIMIT ?,?");
		paramList.add(page.getFirstElementNumber());
		paramList.add(pageSize);
		
		List<SimpleLoanOrder> orderList=ApplicationUtil.executeSelect(SimpleLoanOrder.class,whereStatement.toString(),paramList.toArray(new Object[paramList.size()]));
		if(null==orderList || orderList.isEmpty()) return page;
		String partyIds=orderList.stream().map(order->order.getPartyId()).collect(Collectors.joining(","));
		List<Party> partyList=ApplicationUtil.executeSelect(Party.class,"WHERE FIND_IN_SET(UUID,?)",new Object[] {partyIds});
		Map<?,?> idToNameMap=partyList.stream().collect(Collectors.toMap(party->party.getId(),party->party.getUsername(),(oldname,newname)->newname));
		Map<?,?> rolenameMap=partyList.stream().collect(Collectors.toMap(party->party.getId(),party->party.getRolename(),(oldname,newname)->newname));
			
		Date endDay = new Date();
		Long endTimel = endDay.getTime();		
		List<HashMap<String,Object>> transferList=orderList.stream().map(order->{
			Integer state=order.getState();
			Integer rep=order.getRepayment();
			String houseImgs=(String)order.getHouseImgs();
			Integer led=order.getLendingInstitution();
			houseImgs=null==houseImgs?null:(houseImgs=houseImgs.trim()).isEmpty()?null:houseImgs;
			HashMap<String,Object> transferMap=new HashMap<String,Object>(BeanMap.create(order));
			transferMap.put("state", new Object[] {state,stateMap.get(state)});
			transferMap.put("userName", idToNameMap.get(order.getPartyId()));
			transferMap.put("rolename", rolenameMap.get(order.getPartyId()));
			if(ObjectUtils.isNotEmpty(transferMap.get("rolename"))) {
				String roleName = transferMap.get("rolename").toString();
				transferMap.put("roleNameDesc", Constants.ROLE_MAP.containsKey(roleName) ? Constants.ROLE_MAP.get(roleName) : roleName);
			}

			transferMap.put("repayment", new Object[] {rep,repayments.get(rep)});
			transferMap.put("houseImgs", null==houseImgs?new String[0]:commaPattern.split(houseImgs));
			transferMap.put("lendingInstitution", new Object[] {led,order.getLendingName()});
			
			
			Long starTimel = order.getCreateTime().getTime();
			Long num = endTimel - starTimel;
			Long remainQuota = num/24/60/60/1000;
			order.setTotalInterest(BigDecimal.ZERO);
			order.setRemainQuota(0);
			if(order.getState().intValue()==2||order.getState().intValue()==4) {
				BigDecimal totalInterest = order.getDailyRate().multiply(new BigDecimal(remainQuota)).multiply(order.getQuota());
				order.setTotalInterest(totalInterest);
				order.setRemainQuota(order.getTerm() - remainQuota.intValue());
				transferMap.put("totalInterest", totalInterest);
			}		
			
			return transferMap;
		}).collect(Collectors.toList());
		page.setElements(transferList);
		
		return page;
	}

	@Override
	public boolean deleteLoanOrder(String orderId) {
		if(null==orderId || (orderId=orderId.trim()).isEmpty()) {
			throw new BusinessException("申请单ID不能为空!");
		}
		
		int count=ApplicationUtil.executeDel(orderId,SimpleLoanOrder.class);
		if(1!=count) {
			throw new BusinessException("根据申请单ID删除了多条记录,说明申请单ID重复!");
		}
		
		return count==1?true:false;
	}

	@Override
	public boolean updateLoanOrderState(String orderId, String status,String reason) {
		if(null==orderId || (orderId=orderId.trim()).isEmpty()) {
			throw new BusinessException("申请单ID不能为空!");
		}
		
		if(null==status || (status=status.trim()).isEmpty()) {
			throw new BusinessException("审核状态不能为空!");
		}
		
		int state=Integer.parseInt(status);
		int count=0;
		if(StringUtils.isNotBlank(reason)) {
			count = ApplicationUtil.getBean(JdbcTemplate.class).update("UPDATE T_SIMPLE_LOAN_ORDER SET state=?,REASON=? WHERE UUID=?",state,reason,orderId);
		}else {
			count = ApplicationUtil.getBean(JdbcTemplate.class).update("UPDATE T_SIMPLE_LOAN_ORDER SET state=? WHERE UUID=?",state,orderId);
		}
		if(1!=count) {
			throw new BusinessException("根据申请单ID修改了多条记录,说明申请单ID重复!");
		}
		
		return true;
	}

	@Override
	public Boolean modLoanOrder(SimpleLoanOrder simpleLoanOrder) {
		Serializable id=simpleLoanOrder.getId();
		if(null==id) {
			throw new BusinessException("申请单ID不能为空!");
		}
		
		Set<String> excludeFields=new HashSet<String>();
		excludeFields.add("id");
		excludeFields.add("partyId");
		excludeFields.add("state");
		excludeFields.add("incomeImg");
		excludeFields.add("incomeImg");
		excludeFields.add("lendingInstitution");
		
		int count=ApplicationUtil.executeUpdate(simpleLoanOrder,excludeFields);
		if(1!=count) {
			throw new BusinessException("根据申请单ID修改了多条记录,说明申请单ID重复!");
		}
		
		return true;
	}
	
	@Override
	public SimpleLoanOrder getLoanOrder(String partyId,String orderId) {
		if(null==orderId || (orderId=orderId.trim()).isEmpty()) {
			throw new BusinessException("申请单ID不能为空!");
		}
		
		ArrayList<Object> paramList=new ArrayList<Object>();
		StringBuilder whereStatement=new StringBuilder("WHERE UUID=? ");
		paramList.add(orderId);
		
		if(null!=partyId) {
			whereStatement.append("AND PARTY_ID=? ");
			paramList.add(partyId);
		}
		
		List<SimpleLoanOrder> loanOrders=ApplicationUtil.executeSelect(SimpleLoanOrder.class,whereStatement.toString(),paramList.toArray(new Object[paramList.size()]));
		if(null==loanOrders || 0==loanOrders.size()) return null;
		
		SimpleLoanOrder loanOrder=loanOrders.get(0);
		String houseImgs=(String)loanOrder.getHouseImgs();
		loanOrder.setHouseImgs(null==houseImgs?new String[0]:commaPattern.split(houseImgs));
		Date endDay = new Date();
		Long endTimel = endDay.getTime();
		Long starTimel = loanOrder.getCreateTime().getTime();
		Long num = endTimel - starTimel;
		Long remainQuota = num/24/60/60/1000;
		loanOrder.setTotalInterest(BigDecimal.ZERO);
		loanOrder.setRemainQuota(0);
		if(loanOrder.getState().intValue()==2||loanOrder.getState().intValue()==4) {
			BigDecimal totalInterest = loanOrder.getDailyRate().multiply(new BigDecimal(remainQuota)).multiply(loanOrder.getQuota());
			loanOrder.setTotalInterest(totalInterest);	
			loanOrder.setRemainQuota(loanOrder.getTerm() - remainQuota.intValue());
		}		
		return loanOrder;
	}
	
	@Override
	public List<SimpleLoanOrder> getUserOrders(Map<String,Object> paramMap) {
		String state=(String)paramMap.get("state");
		String symbol=(String)paramMap.get("symbol");
		String partyId=(String)paramMap.get("partyId");
		String repayment=(String)paramMap.get("repayment");
		String endTimeStr=(String)paramMap.get("endTime");
		String startTimeStr=(String)paramMap.get("startTime");
		String lendingInstitution=(String)paramMap.get("lendingInstitution");
		
		Integer status=(null==state || (state=state.trim()).isEmpty())?null:new Integer(state);
		Timestamp startTime=(null==startTimeStr || (startTimeStr=startTimeStr.trim()).isEmpty())?null:Timestamp.valueOf(startTimeStr);
		
		Timestamp endTime=null;
		if(null!=startTime) {
			endTime=(null==endTimeStr || (endTimeStr=endTimeStr.trim()).isEmpty())?null:Timestamp.valueOf(endTimeStr);
		}
		
		ArrayList<Object> paramList=new ArrayList<Object>();
		StringBuilder whereStatement=new StringBuilder("WHERE PARTY_ID=? ");
		paramList.add(partyId);
		
		if(null!=status) {
			whereStatement.append("AND STATE=? ");
			paramList.add(status);
		}
		
		if(null!=symbol && !(symbol=symbol.trim()).isEmpty()) {
			whereStatement.append("AND SYMBOL=? ");
			paramList.add(symbol);
		}
		
		if(null!=repayment && !(repayment=repayment.trim()).isEmpty()) {
			whereStatement.append("AND REPAYMENT=? ");
			paramList.add(repayment);
		}
		
		if(null!=lendingInstitution && !(lendingInstitution=lendingInstitution.trim()).isEmpty()) {
			whereStatement.append("AND LENDING_INSTITUTION=? ");
			paramList.add(lendingInstitution);
		}
		
		if(null!=startTime) {
			if(null==endTime) {
				whereStatement.append("AND CREATE_TIME>=? ");
				paramList.add(startTime);
			}else {
				whereStatement.append("AND CREATE_TIME>=? AND CREATE_TIME<=? ");
				paramList.add(startTime);
				paramList.add(endTime);
			}
		}
		whereStatement.append("ORDER BY CREATE_TIME DESC ");
		List<SimpleLoanOrder> simpleLoanOrderList = ApplicationUtil.executeSelect(SimpleLoanOrder.class,whereStatement.toString(),paramList.toArray(new Object[paramList.size()]));
		
		//DateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
		
		Date endDay = new Date();
		Long endTimel = endDay.getTime();
		for(SimpleLoanOrder simpleLoanOrder :simpleLoanOrderList) {
			Long starTimel = simpleLoanOrder.getCreateTime().getTime();
			Long num = endTimel - starTimel;
			Long remainQuota = num/24/60/60/1000;
			simpleLoanOrder.setTotalInterest(BigDecimal.ZERO);
			simpleLoanOrder.setRemainQuota(0);
			if(simpleLoanOrder.getState().intValue()==2||simpleLoanOrder.getState().intValue()==4) {
				BigDecimal totalInterest = simpleLoanOrder.getDailyRate().multiply(new BigDecimal(remainQuota)).multiply(simpleLoanOrder.getQuota());
				simpleLoanOrder.setTotalInterest(totalInterest);
				simpleLoanOrder.setRemainQuota(simpleLoanOrder.getTerm() - remainQuota.intValue());
			}						
		}
		return simpleLoanOrderList;
	}
}
