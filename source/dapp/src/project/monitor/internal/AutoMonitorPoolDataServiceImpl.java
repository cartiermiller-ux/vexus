package project.monitor.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kernel.util.Arith;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import project.data.DataService;
import project.data.model.Realtime;
import project.monitor.AutoMonitorPoolDataService;
import project.monitor.model.AutoMonitorPoolData;
import project.monitor.report.DAppData;
import project.monitor.report.DAppUserDataSumService;
import project.syspara.Syspara;
import project.syspara.SysparaService;
import util.RandomUtil;

public class AutoMonitorPoolDataServiceImpl implements AutoMonitorPoolDataService {

	private DataService dataService;
	
	protected SysparaService sysparaService;
	
	private DAppUserDataSumService dAppUserDataSumService;
	
	private static final Logger logger = LoggerFactory.getLogger(AutoMonitorPoolDataServiceImpl.class);
	
	@Override
	public void save(AutoMonitorPoolData entity) {
		ApplicationUtil.executeInsert(entity);
	}

	@Override
	public void update(AutoMonitorPoolData entity) {
		ApplicationUtil.executeUpdate(entity);
	}

	@Override
	public AutoMonitorPoolData findById(String id) {
		return ApplicationUtil.executeGet(id,AutoMonitorPoolData.class);
	}

	/**
	 * 默认数据
	 */
	public AutoMonitorPoolData findDefault() {
		List<AutoMonitorPoolData> list = ApplicationUtil.executeSelect(AutoMonitorPoolData.class);
		return (null==list || list.isEmpty())?null:list.get(0);
	}

	@Override
	public List<Map<String, Object>> createNotice(String noticeRate) {
		String[] noticeRates = noticeRate.split(":"); 
		double level1 = Double.valueOf(noticeRates[0].split("-")[0]);
		double level2 = Double.valueOf(noticeRates[0].split("-")[1]);
		double level3 = Double.valueOf(noticeRates[1].split("-")[0]);
		double level4 = Double.valueOf(noticeRates[1].split("-")[1]);
		double level5 = Double.valueOf(noticeRates[2].split("-")[0]);
		double level6 = Double.valueOf(noticeRates[2].split("-")[1]);
		double level7 = Double.valueOf(noticeRates[3].split("-")[0]);
		double level8 = Double.valueOf(noticeRates[3].split("-")[1]);
		List<Map<String, Object>> list = new ArrayList<>();
		for (int i = 0; i < 15; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			double money = RandomUtil.randomFloat(level1, level2, 6);
			map.put("address", hideAddress(randomAddress(), 6));
			map.put("money", money);
			list.add(map);
		}
		
		for (int i = 0; i < 6; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			double money = RandomUtil.randomFloat(level3, level4, 6);
			map.put("address", hideAddress(randomAddress(), 6));
			map.put("money", money);
			list.add(map);
		}
		
		for (int i = 0; i < 6; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			double money = RandomUtil.randomFloat(level5, level6, 6);
			map.put("address", hideAddress(randomAddress(), 6));
			map.put("money", money);
			list.add(map);
		}
		
		for (int i = 0; i < 3; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			double money = RandomUtil.randomFloat(level7, level8, 6);
			map.put("address", hideAddress(randomAddress(), 6));
			map.put("money", money);
			list.add(map);
		}
		return list;
	}

	private String randomAddress() {
		char[] all = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		Random random = new Random();
		StringBuilder sb = new StringBuilder("0x");
		for (int i = 0; i < 40; i++) {
			sb.append(all[random.nextInt(all.length)]);
		}
		return sb.toString();
	}

	/**
	 * 隐藏地址
	 */
	public String hideAddress(String address, int hideLength) {
		if (StringUtils.isEmptyString(address)) return address;
		if (address.length() > hideLength * 2) {
			return address.substring(0, hideLength) + "****" + address.substring(address.length() - hideLength);
		}
		return address;
	}
	
	/**
	 * 矿池产生收益时数据处理
	 */
	public void updateDefaultOutPut(double outPut) {
		try {
			List<Realtime> realtime_list = this.dataService.realtime("eth");
			Realtime realtime = null;
			if (realtime_list.size() > 0) {
				realtime = realtime_list.get(0);
			}
			Double close = realtime.getClose();
			DAppData cacheGetData = dAppUserDataSumService.cacheGetData(new Date());
	
			AutoMonitorPoolData findDefault = findDefault();
			//总产量=矿机产生的eth*（参与者/实际参与者）
			double totle = Arith.mul(outPut, Arith.div(findDefault.getVerifier(),cacheGetData.getUsdt_user_count() == 0 ? 1 : cacheGetData.getUsdt_user_count()));
			double userProfit = Arith.mul(totle, close);
			Syspara syspara = sysparaService.find("total_output");
			if(ObjectUtils.isEmpty(syspara)) {
				findDefault.setTotal_output(Arith.add(findDefault.getTotal_output(), totle));
				//用户收益=总产量*eth行情
				findDefault.setUser_revenue(Arith.add(findDefault.getUser_revenue(), userProfit));
			}else {
				if("1".equals(syspara.getValue())) {
					findDefault.setTotal_output(Arith.add(findDefault.getTotal_output(), totle));
					//用户收益=总产量*eth行情
					findDefault.setUser_revenue(Arith.add(findDefault.getUser_revenue(), userProfit));
				}
			}
			this.update(findDefault);
		}catch (Exception e) {
			logger.error("AutoMonitorPoolDataServiceImpl.updateDefaultOutPut fail e:",e);
			e.printStackTrace();
		}
	}

	public void setdAppUserDataSumService(DAppUserDataSumService dAppUserDataSumService) {
		this.dAppUserDataSumService = dAppUserDataSumService;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	public void setSysparaService(SysparaService sysparaService) {
		this.sysparaService = sysparaService;
	}
}
