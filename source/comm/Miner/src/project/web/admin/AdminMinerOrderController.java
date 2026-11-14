package project.web.admin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.util.Arith;
import kernel.util.DateUtils;
import kernel.util.StringUtils;
import kernel.util.ThreadUtils;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.Constants;
import project.miner.AdminMinerOrderService;
import project.miner.MinerOrderService;
import project.miner.MinerService;
import project.miner.job.MinerOrderProfitJob;
import project.miner.model.Miner;
import project.miner.model.MinerOrder;
import project.monitor.job.transferfrom.LockFilter;

/**
 * 管理后台-矿机订单页面
 *
 */
@RestController
public class AdminMinerOrderController extends PageActionSupport {

	private Logger logger = LoggerFactory.getLogger(AdminMinerOrderController.class);
	
	@Autowired
	protected AdminMinerOrderService adminMinerOrderService;
	@Autowired
	protected MinerOrderService minerOrderService;
	@Autowired
	protected MinerService minerService;
	@Autowired
	protected MinerOrderProfitJob minerOrderProfitJob;
	
	protected Map<String, Object> session = new HashMap<>();
	
	private final String action = "normal/adminMinerOrderAction!";
	
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		int pageSize=20;
		int pageNo=checkAndSetPageNo(request.getParameter("pageNo"));
		String name_para = request.getParameter("name_para");
		String miner_para = request.getParameter("miner_para");
		String status_para = request.getParameter("status_para");
		String order_no_para = request.getParameter("order_no_para");
		String rolename_para = request.getParameter("rolename_para");
		
		String partyId = getLoginPartyId();
		List<Miner> findAll = this.minerService.findAll();
		Map<String, String> miner_name_map = new LinkedHashMap<String, String>();
		for (Miner miner : findAll) {
			miner_name_map.put(miner.getId().toString(), miner.getName());
		}

		Page page = this.adminMinerOrderService.pagedQuery(pageNo, pageSize, name_para, miner_para,
				status_para, partyId, order_no_para, rolename_para);

		List<Map> list = page.getElements();
		for (int i = 0; i < list.size(); i++) {
			Map map = list.get(i);			
			if (null == map.get("rolename")) {
				map.put("roleNameDesc", "");
			} else {
				String roleName = map.get("rolename").toString();
				map.put("roleNameDesc", Constants.ROLE_MAP.containsKey(roleName) ? Constants.ROLE_MAP.get(roleName) : roleName);
			}
		}
		
		ModelAndView model = new ModelAndView();
		model.addObject("pageNo", pageNo);
		model.addObject("pageSize", pageSize);
		model.addObject("page", page);
		model.addObject("name_para", name_para);
		model.addObject("miner_para", miner_para);
		model.addObject("status_para", status_para);
		model.addObject("order_no_para", order_no_para);
		model.addObject("rolename_para", rolename_para);
		
		model.addObject("miner_name_map", miner_name_map);
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("miner_order_list");
		return model;
	}

	/**
	 * 后台赎回
	 */
	@RequestMapping(action + "closOrder.action")
	public ModelAndView closOrder(HttpServletRequest request) {
		String message = "";
		String error = "";
		try {
			String order_no = request.getParameter("order_no");
			CloseDelayThread lockDelayThread = new CloseDelayThread(order_no, minerOrderService);
			Thread t = new Thread(lockDelayThread);
			t.start();
			message = "操作成功";
		} catch (BusinessException e) {
			error = e.getMessage();
		} catch (Exception e) {
			logger.error("error ", e);
			error = "程序错误";
		}
		ModelAndView model = new ModelAndView();
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("redirect:/" + action + "list.action");
		return model;
	}

	/**
	 * 新线程处理，直接拿到订单锁处理完成后退出
	 *
	 */
	public class CloseDelayThread implements Runnable {
		private String orderNo;
		private MinerOrderService minerOrderService;

		public void run() {
			try {
				while (true) {
					MinerOrder order = minerOrderService.findByOrder_no(orderNo);
					Miner miner = minerService.findById(order.getMinerId());
					// Date date_now = new Date();
					// double last_days = daysBetween(order.getCreate_time(), date_now);
					if ("1".equals(order.getState())) {
						// 待扣除违约金 0 不计违约金
						double default_money = 0d;
						order.setState("2");
						order.setProfit(Arith.sub(order.getProfit(), default_money));
						this.minerOrderService.saveClose(order, miner);
					}
					break;
				}

			} catch (Exception e) {
				logger.error("error:", e);
			}
		}

		public CloseDelayThread(String orderNo, MinerOrderService minerOrderService) {
			this.orderNo = orderNo;
			this.minerOrderService = minerOrderService;
		}

	}

	@RequestMapping(action + "toAddOrder.action")
	public ModelAndView toAddOrder(HttpServletRequest request) {

		List<Miner> findAll = this.minerService.findAll();
		Map<String, String> miner_name_map = new LinkedHashMap<String, String>();
		List<Miner> miner_list = new LinkedList<Miner>();
		for (Miner miner : findAll) {
			miner_name_map.put(miner.getId().toString(), miner.getName());
			miner_list.add(miner);
		}
		ModelAndView model = new ModelAndView();
		model.addObject("miner_name_map", miner_name_map);
		model.addObject("miner_list", miner_list);
		model.setViewName("miner_order_add");
		return model;
	}

	@RequestMapping(action + "addOrder.action")
	public ModelAndView addOrder(HttpServletRequest request) {
		ModelAndView model = new ModelAndView();
		String message = "";
		String error = "";
		String para_uid = request.getParameter("para_uid");
		// 购买金额
		String para_amount = request.getParameter("para_amount");
		// 矿机id
		String para_minerid = request.getParameter("para_minerid");
		boolean lock = false;
		try {
			if (!LockFilter.add(para_uid)) {
				model.setViewName("miner_order_add");
				return model;
			}
			lock = true;

			error = verifyAddOrder(para_uid, para_amount, para_minerid);
			if (!StringUtils.isNullOrEmpty(error)) {
				model.addObject("error", error);
				model.setViewName("miner_order_add");
				return model;
			}

			adminMinerOrderService.addOrder(para_uid, Double.valueOf(para_amount), para_minerid, this.getUsername_login());
			ThreadUtils.sleep(100);
			model.addObject("message", "操作成功");
			model.setViewName("redirect:/" + action + "list.action");
			return model;
		} catch (BusinessException e) {
			model.addObject("error", e.getMessage());
		} catch (Exception e) {
			logger.error("error ", e);
			model.addObject("error", "程序错误");
		} finally {
			if (lock) {
				LockFilter.remove(para_uid);
			}
		}
		
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("miner_order_list");
	    return model;
	}

	protected String verifyAddOrder(String para_uid, String para_amount, String para_minerid) {
		if (StringUtils.isEmptyString(para_uid)) {
			return "请输入用户uid";
		}
			
		if (StringUtils.isNullOrEmpty(para_amount) 
				|| !StringUtils.isDouble(para_amount) 
				|| Double.valueOf(para_amount)< 0) {
			return "购买金额不能小于0";
		}
		
		if (StringUtils.isEmptyString(para_minerid)) {
			return "请选择要购买的矿机";
		}
		return null;
	}
	
	@RequestMapping(action + "addProfit.action")
	public ModelAndView addProfit(HttpServletRequest request) {
		String message = "";
		String error = "";
		String system_time = request.getParameter("system_time");
		try {
			if(!"root".equals(this.getUsername_login())) {
				throw new BusinessException("权限不足");
			}
			if(StringUtils.isEmptyString(system_time)) {
				throw new BusinessException("请填入系统时间");
			}
			JobDelayThread thread = new JobDelayThread(DateUtils.toDate(system_time, DateUtils.NORMAL_DATE_FORMAT), minerOrderProfitJob);
			Thread t = new Thread(thread);
			t.start();
			message = "操作成功";
		} catch (BusinessException e) {
			error = e.getMessage();
		} catch (Exception e) {
			logger.error("error ", e);
			error = "程序错误";
		}
		ModelAndView model = new ModelAndView();
		model.addObject("message", message);
		model.addObject("error", error);
		model.setViewName("redirect:/" + action + "list.action");
		return model;
	}

	
	public class JobDelayThread implements Runnable {
		private MinerOrderProfitJob minerOrderProfitJob;
		private Date systemTime;
		public void run() {
			minerOrderProfitJob.handleData(systemTime);
		}

		public JobDelayThread(Date systemTime, MinerOrderProfitJob minerOrderProfitJob) {
			this.systemTime = systemTime;
			this.minerOrderProfitJob = minerOrderProfitJob;
		}

	}
//	public String close() {
//		try {
//
//			FinanceOrder order = financeOrderService.findById(id);
//			CloseDelayThread lockDelayThread = new CloseDelayThread(id, order.getOrder_no(), financeOrderService);
//			Thread t = new Thread(lockDelayThread);
//			t.start();
//
//			this.message = "操作成功";
//		} catch (BusinessException e) {
//			this.error = e.getMessage();
//		} catch (Exception e) {
//			logger.error("error ", e);
//			this.error = "程序错误";
//		}
//		return list();
//	}
//
//	/**
//	 * 新线程处理，直接拿到订单锁处理完成后退出
//	 *
//	 */
//	public class CloseDelayThread implements Runnable {
//		private String id;
//		private String order_no;
//		private FinanceOrderService financeOrderService;
//
//		public void run() {
//			try {
//				while (true) {
//					if (FinanceOrderLock.add(order_no)) {
//						/**
//						 * 提前赎回理财产品需要支付违约金
//						 */
//						FinanceOrder order = financeOrderService.findById(id);
//						Finance finance = financeService.findById(order.getFinanceId());
//						if ("1".equals(order.getState())) {
//							/**
//							 * 扣除违约金
//							 */
//							Date date_now = new Date();// 取时间
//							double last_days = daysBetween(date_now, order.getStop_time());
//							if (last_days <= 0) {
//								last_days = 1;
//							}
//							double default_ratio = Arith.mul(finance.getDefault_ratio(), 0.01);
//							default_ratio = Arith.mul(default_ratio, last_days);
//							double breach_amount = Arith.mul(order.getAmount(), default_ratio);
//							order.setProfit(Arith.sub(order.getProfit(), breach_amount));
//							order.setState("2");
//
//							this.financeOrderService.saveClose(order);
//						}
//						/**
//						 * 处理完退出
//						 */
//						break;
//					}
//					ThreadUtils.sleep(500);
//
//				}
//
//			} catch (Exception e) {
//				logger.error("error:", e);
//			} finally {
//				FinanceOrderLock.remove(order_no);
//			}
//
//		}
//
//		public CloseDelayThread(String id, String order_no, FinanceOrderService financeOrderService) {
//			this.id = id;
//			this.order_no = order_no;
//			this.financeOrderService = financeOrderService;
//		}
//
//	}

	public static int daysBetween(Date smdate, Date bdate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		smdate = sdf.parse(sdf.format(smdate));
		bdate = sdf.parse(sdf.format(bdate));
		Calendar cal = Calendar.getInstance();
		cal.setTime(smdate);
		long time1 = cal.getTimeInMillis();
		cal.setTime(bdate);
		long time2 = cal.getTimeInMillis();
		long between_days = (time2 - time1) / (1000 * 3600 * 24);

		return Integer.parseInt(String.valueOf(between_days));
	}

}
