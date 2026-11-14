package project.c2c;

import java.util.List;

import kernel.web.Page;

public interface C2cOrderService {
	
	public C2cOrder get(String order_no);
	
	public List<C2cOrder> getByPayId(String payId);

	public Page pagedQuery(int pageNo, int pageSize, String direction, String state, String loginPartyId, boolean isC2cUser,String startTime, String endTime);

//	public C2cOrder findById(String id);
//
//	public C2cOrder findByOrderNo(String orderNo);
//
//	public C2cOrder findByOrderNo(String orderNo, String loginPartyId);

	public void saveOpen(C2cOrder c2cOrder,String remark);

//	public void saveOpenQuick(C2cOrder c2cOrder);
	
	public void saveOpenQuickApply(C2cOrder c2cOrder);

//	public Map<String, Object> getBindOne(C2cOrder entity);
	
	public void save(C2cOrder entity);

	public void update(C2cOrder entity);
	
	/*
	 * 用户未结束订单数量（0未付款/1已付款/2申诉中/5已超时）减1
	 */
	public void updateNofinishOrderCount(C2cOrder entity);
	
	/*
	 * 获取 用户未结束订单数量
	 */
	public Long getNofinishOrderCount(String partyId,String direction);
	
//	public void delete(String order_no);

	public void saveOrderCancel(C2cOrder c2cOrder, String role);

	public void saveOrderPass(C2cOrder c2cOrder);

	/**
	 * 今日C2C订单
	 */
	public List<C2cOrder> findByPartyIdAndToday(String partyId, String direction, String state);

	/**
	 * 未结束的卖币订单
	 */
	public List<C2cOrder> findNoEndingSellOrders(String loginPartyId);
	
	/*
	 * 查询未完结订单数量，根据广告ID
	 */
	public Long findNoEndingOrdersCountByAdvertId(String ctcAdvertId);
	
	/*
	 * C2C发送短信和邮件通知
	 */
	public void c2cSendMessageByState(C2cOrder order, String state);
	
}
