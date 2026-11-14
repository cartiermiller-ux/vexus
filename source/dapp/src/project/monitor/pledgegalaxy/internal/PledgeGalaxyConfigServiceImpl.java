package project.monitor.pledgegalaxy.internal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kernel.util.Arith;
import kernel.web.ApplicationUtil;
import project.Constants;
import project.monitor.pledgegalaxy.PledgeGalaxyConfig;
import project.monitor.pledgegalaxy.PledgeGalaxyConfigService;
import project.monitor.pledgegalaxy.PledgeGalaxyOrder;
import project.monitor.pledgegalaxy.PledgeGalaxyOrderService;
import project.monitor.pledgegalaxy.PledgeGalaxyRedisKeys;
import project.monitor.pledgegalaxy.PledgeGalaxyStatusConstants;
import project.party.PartyService;
import project.party.model.Party;
import project.party.model.UserRecom;
import project.party.recom.UserRecomService;
import project.redis.RedisHandler;

/**
 * 质押2.0配置
 */
public class PledgeGalaxyConfigServiceImpl implements PledgeGalaxyConfigService {
	
	private RedisHandler redisHandler;

	private PartyService partyService;
	
	private UserRecomService userRecomService;
	
	private PledgeGalaxyOrderService pledgeGalaxyOrderService;
	
	/**
	 * 获取配置 优先级：用户>代理>个人
	 */
	public PledgeGalaxyConfig getConfig(String partyId) {
		
		// 获取用户配置
		PledgeGalaxyConfig userConfig = (PledgeGalaxyConfig) this.redisHandler.get(PledgeGalaxyRedisKeys.PLEDGE_GALAXY_CONFIG + partyId);
		if (null != userConfig) {
			return userConfig;
		}
		
		// 获取代理
		List<UserRecom> parents = this.userRecomService.getParents(partyId.toString());
		for (int i = 0; i < parents.size(); i++) {
			
			Party party = this.partyService.cachePartyBy(parents.get(i).getReco_id(), true);

			if (!Constants.SECURITY_ROLE_AGENT.equals(party.getRolename()) && !Constants.SECURITY_ROLE_AGENTLOW.equals(party.getRolename())) {
				// 非代理
				continue;
			}
			
			// 获取代理配置
			PledgeGalaxyConfig agentConfig = (PledgeGalaxyConfig) this.redisHandler.get(PledgeGalaxyRedisKeys.PLEDGE_GALAXY_CONFIG + party.getId().toString());
			if (null != agentConfig) {
				return agentConfig;
			}
		}
		
		// 获取全局配置
		return this.getGlobalConfig();
	}
	
	/**
	 * 获取配置利率Map
	 */
	public Map<String, String> getIoeAIRateMap(String partyId, int days, double amount, Date createTime) {
				
		Map<String, String> rateMap = new HashMap<>();
		PledgeGalaxyConfig config = getConfig(partyId);
		// 静态收益
		String staticIncome = config.getStaticIncomeForceValue();
		String dynamicIncome = config.getDynamicIncomeAssistValue();
		String teamIncome = config.getTeamIncomeProfitRatio();
		rateMap.put("pledgeAmountMin", String.valueOf(config.getPledgeAmountMin()));
		
		if (null != createTime && createTime.before(getIoeAiTimeVersion())) {
			// 老配置 
			staticIncome = staticIncome.split("&")[0];
			dynamicIncome = dynamicIncome.split("&")[0];
			// teamIncome = teamIncome.split("&")[0];
			teamIncome = teamIncome.split("&")[2];
			ioeAiConfig(rateMap, staticIncome, dynamicIncome, teamIncome, days, amount, partyId);
		} else {
			staticIncome = staticIncome.split("&")[1];
			// dynamicIncome = dynamicIncome.split("&")[1];
			// teamIncome = teamIncome.split("&")[1];
			teamIncome = teamIncome.split("&")[2];
			newIoeAiConfig(rateMap, staticIncome, teamIncome, days, amount, partyId);
		}
		return rateMap;
	}
	
	/**
	 * Vickers获取配置利率Map
	 */
	public Map<String, String> getVickersRateMap(String partyId, int days, double amount) {
		
		Map<String, String> rateMap = new HashMap<>();
		PledgeGalaxyConfig config = getConfig(partyId);
		// 静态收益
		String staticIncome = config.getStaticIncomeForceValue();
		String dynamicIncome = config.getDynamicIncomeAssistValue();
		String teamIncome = config.getTeamIncomeProfitRatio();
		rateMap.put("pledgeAmountMin", String.valueOf(config.getPledgeAmountMin()));
		vickersConfig(rateMap, staticIncome, dynamicIncome, teamIncome, days, amount, partyId);
		return rateMap;
	}
	
	/**
	 * SafePal5获取配置利率Map
	 */
	public Map<String, String> getSafePal5RateMap(String partyId, int days, double amount) {
		Map<String, String> rateMap = new HashMap<>();
		PledgeGalaxyConfig config = getConfig(partyId);
		// 静态收益
		String staticIncome = config.getStaticIncomeForceValue();
		String dynamicIncome = config.getDynamicIncomeAssistValue();
		String teamIncome = config.getTeamIncomeProfitRatio();
		rateMap.put("staticIncome", staticIncome);
		// safePal5Config(rateMap, staticIncome, dynamicIncome, teamIncome, days, amount, partyId);
		newSafePal5Config(rateMap, staticIncome, dynamicIncome, teamIncome, days, amount, partyId);
		return rateMap;
	}

	/**
	 * 获取所有配置 查数据库
	 */
	public List<PledgeGalaxyConfig> getAll() {
		return ApplicationUtil.executeSelect(PledgeGalaxyConfig.class);
	}
	
	/**
	 * 获取全局配置 查redis
	 */
	public PledgeGalaxyConfig getGlobalConfig() {
		return (PledgeGalaxyConfig) this.redisHandler.get(PledgeGalaxyRedisKeys.PLEDGE_GALAXY_CONFIG);
	}

	public PledgeGalaxyConfig findByPartyId(String partyId) {
		List<PledgeGalaxyConfig> list = ApplicationUtil.executeSelect(PledgeGalaxyConfig.class,"WHERE PARTY_ID=?",new Object[] {partyId});
		return list.size()<=0?null:list.get(0);
	}

	public void save(PledgeGalaxyConfig entity) {
		ApplicationUtil.executeInsert(entity);
	}

	public void update(PledgeGalaxyConfig entity) {
		ApplicationUtil.executeUpdate(entity);

	}

	public PledgeGalaxyConfig findById(String id) {
		return ApplicationUtil.executeGet(id,PledgeGalaxyConfig.class);
	}

	public void delete(PledgeGalaxyConfig entity) {
		ApplicationUtil.executeDelete(entity);
	}
	
	private Map<String, String> vickersConfig(Map<String, String> rateMap, String staticIncome, String dynamicIncome,
			String teamIncome, int days, double amount, String partyId) {
		// 静态收益
		String[] staticSplit = staticIncome.split("\\|");
		String min = staticSplit[0].split(":")[0].split("-")[0];
		String max = staticSplit[staticSplit.length -1].split(":")[0].split("-")[1];
		if (amount >= Integer.valueOf(min) && Arith.sub(amount, Double.valueOf(max)) <= 0) {
			for (int i = 0; i < staticSplit.length; i++) {
				String value = staticSplit[i];
				String[] valueSplit = value.split(":");
				int amountMin = Integer.valueOf(valueSplit[0].split("-")[0]);
				int amountMax = Integer.valueOf(valueSplit[0].split("-")[1]);
				
				if (amount >= amountMin && amount <= amountMax) {
					String daysArea = valueSplit[1];
					String[] daysSplit = daysArea.split(";");
					for (int j = 0; j < daysSplit.length; j++) {
						String[] rateSplit = daysSplit[j].split("#");
						String day = rateSplit[0];
						if (Integer.valueOf(day) == days) {
							rateMap.put("staticRate", rateSplit[1]);
						}
					}
				}
			}
		}
		// 大于最大值
		else if (Arith.sub(amount, Double.valueOf(max)) > 0) {
			String value = staticSplit[staticSplit.length -1];
			String[] valueSplit = value.split(":");
			String daysArea = valueSplit[1];
			String[] daysSplit = daysArea.split(";");
			for (int j = 0; j < daysSplit.length; j++) {
				String[] rateSplit = daysSplit[j].split("#");
				String day = rateSplit[0];
				if (Integer.valueOf(day) == days) {
					rateMap.put("staticRate", rateSplit[1]);
				}
			}
		}
		
		// 一级代理人数
		List<String> partyLists = userRecomService.findRecomsToPartyId(partyId);
		int level_1_sum = 0;
		if (partyLists.size() >= 3) {
			for (String id : partyLists) {
				Party party = partyService.cachePartyBy(id, false);
				if (null != party) {
					Map<String, PledgeGalaxyOrder> map = (Map<String, PledgeGalaxyOrder>)pledgeGalaxyOrderService.findOrdersByPartyId(id);
					if (null != map && map.size() > 0) {
						for (PledgeGalaxyOrder galaxyOrder : map.values()) {
							if (galaxyOrder.getDays() >= 7 
									&& galaxyOrder.getStatus() == PledgeGalaxyStatusConstants.PLEDGE_SUCCESS) {
								if (Constants.SECURITY_ROLE_GUEST.equals(party.getRolename()) 
										|| party.getKyc_authority()) {
									level_1_sum ++;
									break;
								}
							}
						}
					}
				}
			}
		}
		if(!"0".equals(dynamicIncome)) {
			// 助力收益
			String[] dynamicSplit = dynamicIncome.split("\\|");
			String levelMin = dynamicSplit[0].split(";")[0];
			// 无论直属下级拥有多少人，选择质押1天都不能享受额外的动态收益
			if (days > 1 && level_1_sum >= Integer.valueOf(levelMin)) {
				// 动态收益
				for (int i = 0; i < dynamicSplit.length; i++) {
					String levelNum = dynamicSplit[i].split(";")[0];
					String dynamicRate = dynamicSplit[i].split(";")[1];
					if (level_1_sum >= Integer.valueOf(levelNum)) {
						rateMap.put("dynamicRate", dynamicRate);
					}
				}
			}
		}		
		
		// 团队收益
		rateMap.put("teamRate", teamIncome);
		return rateMap;
	}
	
	private Map<String, String> newSafePal5Config(Map<String, String> rateMap, String staticIncome, String dynamicIncome,
			String teamIncome, int days, double amount, String partyId) {
		// 静态收益
        staticIncome = staticIncome.split("&")[0];
		
		String[] staticSplit = staticIncome.split("\\|");
		String min = staticSplit[0].split(":")[0].split("-")[0];
		String max = staticSplit[staticSplit.length -1].split(":")[0].split("-")[1];
		if (amount >= Integer.valueOf(min) && Arith.sub(amount, Double.valueOf(max)) <= 0) {
			for (int i = 0; i < staticSplit.length; i++) {
				String value = staticSplit[i];
				String[] valueSplit = value.split(":");
				int amountMin = Integer.valueOf(valueSplit[0].split("-")[0]);
				int amountMax = Integer.valueOf(valueSplit[0].split("-")[1]);
				
				if (amount >= amountMin && amount <= amountMax) {
					String daysArea = valueSplit[1];
					String[] daysSplit = daysArea.split(";");
					for (int j = 0; j < daysSplit.length; j++) {
						String[] rateSplit = daysSplit[j].split("#");
						String day = rateSplit[0];
						if (Integer.valueOf(day) == days) {
							rateMap.put("staticRate", rateSplit[1]);
						}
					}
				}
			}
		}
		// 大于最大值
		else if (Arith.sub(amount, Double.valueOf(max)) > 0) {
			String value = staticSplit[staticSplit.length -1];
			String[] valueSplit = value.split(":");
			String daysArea = valueSplit[1];
			String[] daysSplit = daysArea.split(";");
			for (int j = 0; j < daysSplit.length; j++) {
				String[] rateSplit = daysSplit[j].split("#");
				String day = rateSplit[0];
				if (Integer.valueOf(day) == days) {
					rateMap.put("staticRate", rateSplit[1]);
				}
			}
		}
		
		// 一级下级人数
		List<String> partyLists = userRecomService.findRecomsToPartyId(partyId);
		int level_1_sum = 0;
		if (partyLists.size() >= 1) {
			for (String id : partyLists) {
				Map<String, PledgeGalaxyOrder> map = (Map<String, PledgeGalaxyOrder>)pledgeGalaxyOrderService.findOrdersByPartyId(id);
				if (null != map && map.size() > 0) {
					for (PledgeGalaxyOrder galaxyOrder : map.values()) {
						if (galaxyOrder.getDays() >= 10 
								&& galaxyOrder.getStatus() == PledgeGalaxyStatusConstants.PLEDGE_SUCCESS
								&& galaxyOrder.getAmount() >= 100) {
							level_1_sum ++;
							break;
						}
					}
				}
			}
		}
		if(!"0".equals(dynamicIncome)) {
			// 助力收益
			String[] dynamicSplit = dynamicIncome.split("\\|");
			String levelMin = dynamicSplit[0].split(";")[0];
			// 无论直属下级拥有多少人，选择质押1天都不能享受额外的动态收益
			if (days > 1 && level_1_sum >= Integer.valueOf(levelMin)) {
				// 动态收益
				for (int i = 0; i < dynamicSplit.length; i++) {
					String levelNum = dynamicSplit[i].split(";")[0];
					String dynamicRate = dynamicSplit[i].split(";")[1];
					if (level_1_sum >= Integer.valueOf(levelNum)) {
						rateMap.put("dynamicRate", dynamicRate);
					}
				}
			}
		}
		
		
		// 团队收益
		String[] teamSplit = teamIncome.split("#");
		int level1 = Integer.valueOf(teamSplit[0].split(":")[0]);
		int level2 = Integer.valueOf(teamSplit[1].split(":")[0]);
		int level3 = Integer.valueOf(teamSplit[2].split(":")[0]);
		
		List<UserRecom> parents = userRecomService.getParents(partyId);
		if (null != parents && parents.size() > 0) {
			List<String> childrenLists = userRecomService.findRecomsToPartyId(parents.get(0).getReco_id());
			int children_level_1_sum = 0;
			for (String id : childrenLists) {
				Map<String, PledgeGalaxyOrder> map = (Map<String, PledgeGalaxyOrder>)pledgeGalaxyOrderService.findOrdersByPartyId(id);
				if (null != map && map.size() > 0) {
					for (PledgeGalaxyOrder galaxyOrder : map.values()) {
						if (galaxyOrder.getDays() >= 10 
								&& galaxyOrder.getStatus() == PledgeGalaxyStatusConstants.PLEDGE_SUCCESS 
								&& galaxyOrder.getAmount() >= 100) {
							children_level_1_sum ++;
							break;
						}
					}
				}
			}
			
			if (children_level_1_sum >= level1 && children_level_1_sum < level2) {
				System.out.println("一星会员返佣");
				rateMap.put("teamRate", teamSplit[0].split(":")[1]);
				return rateMap;
			}
			
			if (children_level_1_sum >= level2 && children_level_1_sum < level3) {
				System.out.println("二星会员返佣");
				rateMap.put("teamRate", teamSplit[1].split(":")[1]);
				return rateMap;
			}
			
			if (children_level_1_sum >= level3) {
				System.out.println("三星会员返佣");
				rateMap.put("teamRate", teamSplit[2].split(":")[1]);
				return rateMap;
			}
		}

		return rateMap;
	}
	
	
	/**
	 * IoeAi 老配置
	 */
	private Map<String, String> ioeAiConfig(Map<String, String> rateMap, String staticIncome, String dynamicIncome,
			String teamIncome, int days, double amount, String partyId) {
		// 静态收益
		String[] staticSplit = staticIncome.split("\\|");
		String min = staticSplit[0].split(":")[0].split("-")[0];
		String max = staticSplit[staticSplit.length -1].split(":")[0].split("-")[1];
		if (amount >= Integer.valueOf(min) && Arith.sub(amount, Double.valueOf(max)) <= 0) {
			for (int i = 0; i < staticSplit.length; i++) {
				String value = staticSplit[i];
				String[] valueSplit = value.split(":");
				int amountMin = Integer.valueOf(valueSplit[0].split("-")[0]);
				int amountMax = Integer.valueOf(valueSplit[0].split("-")[1]);
				
				if (amount >= amountMin && amount <= amountMax) {
					String daysArea = valueSplit[1];
					String[] daysSplit = daysArea.split(";");
					for (int j = 0; j < daysSplit.length; j++) {
						String[] rateSplit = daysSplit[j].split("#");
						String day = rateSplit[0];
						if (Integer.valueOf(day) == days) {
							rateMap.put("staticRate", rateSplit[1]);
						}
					}
				}
			}
		}
		// 大于最大值
		else if (Arith.sub(amount, Double.valueOf(max)) > 0) {
			String value = staticSplit[staticSplit.length -1];
			String[] valueSplit = value.split(":");
			String daysArea = valueSplit[1];
			String[] daysSplit = daysArea.split(";");
			for (int j = 0; j < daysSplit.length; j++) {
				String[] rateSplit = daysSplit[j].split("#");
				String day = rateSplit[0];
				if (Integer.valueOf(day) == days) {
					rateMap.put("staticRate", rateSplit[1]);
				}
			}
		}
		
		// 助力收益
		double pledgeAmountSum = 0D;
		Map<String, PledgeGalaxyOrder> selfmap = (Map<String, PledgeGalaxyOrder>)pledgeGalaxyOrderService.findOrdersByPartyId(partyId);
		if (null != selfmap && selfmap.size() > 0) {
			for (PledgeGalaxyOrder galaxyOrder : selfmap.values()) {
				if (galaxyOrder.getStatus() == PledgeGalaxyStatusConstants.PLEDGE_SUCCESS) {
					pledgeAmountSum += galaxyOrder.getAmount();
				}
			}
		}
		
		// 伞下代理人数 是 所有下级的
		List<String> partyLists = userRecomService.findChildren(partyId);
		System.out.println("伞下代理人数 是 所有下级的 " + partyLists.size());
		int level_1_sum = 0;
		if (partyLists.size() >= 3) {
			for (String id : partyLists) {
				Party party = partyService.cachePartyBy(id, false);
				if (null != party) {
					Map<String, PledgeGalaxyOrder> map = (Map<String, PledgeGalaxyOrder>)pledgeGalaxyOrderService.findOrdersByPartyId(id);
					if (null != map && map.size() > 0) {
						for (PledgeGalaxyOrder galaxyOrder : map.values()) {
							if (galaxyOrder.getDays() >= 7 
									&& galaxyOrder.getStatus() == PledgeGalaxyStatusConstants.PLEDGE_SUCCESS) {
								if (Constants.SECURITY_ROLE_GUEST.equals(party.getRolename()) 
										|| party.getKyc_authority()) {
									System.out.println(partyId + " 的有效下级 " + party.getUsername());
									level_1_sum ++;
									break;
								}
							}
						}
						
						for (PledgeGalaxyOrder galaxyOrder : map.values()) {
							if (galaxyOrder.getStatus() == PledgeGalaxyStatusConstants.PLEDGE_SUCCESS) {
								pledgeAmountSum += galaxyOrder.getAmount();
							}
						}
					}
				}
			}
		}
		
		System.out.println("伞下代理人 总质押额度 " + pledgeAmountSum);
		if(!"0".equals(dynamicIncome)) {
			String[] dynamicSplit = dynamicIncome.split("\\|");
			
			// 无论直属下级拥有多少人，选择质押1天都不能享受额外的动态收益
			Integer levelMin = Integer.valueOf(dynamicSplit[0].split(";")[0]);
			String[] amountSplit = dynamicSplit[0].split(";")[1].split("-");
			Integer amountMin = Integer.valueOf(amountSplit[0]);
			if (days > 1 && level_1_sum >= levelMin && pledgeAmountSum >= amountMin) {
				// 动态收益
				int levelIndex = 0;
				for (int i = 0; i < dynamicSplit.length; i++) {
					Integer levelNum = Integer.valueOf(dynamicSplit[i].split(";")[0]);
					if (level_1_sum < levelNum) {
						break;
					}
					levelIndex = i;
				}
				
				int amountIndex = 0;
				for (int i = 0; i < dynamicSplit.length; i++) {
					String dynamicAmount = dynamicSplit[i].split(";")[1];
					String[] dynamicAmountSplit = dynamicAmount.split("-");
					Integer dynamicAmountMin = Integer.valueOf(dynamicAmountSplit[0]);
					if (pledgeAmountSum < dynamicAmountMin) {
						break;
					}
					amountIndex = i;
				}
				
				if (levelIndex <= amountIndex) {
					rateMap.put("dynamicRate", dynamicSplit[levelIndex].split(";")[2]);
				} else {
					rateMap.put("dynamicRate", dynamicSplit[amountIndex].split(";")[2]);
				}
			}
		}				
		rateMap.put("teamRate", teamIncome);
		return rateMap;
	}
	
	/**
	 * IoeAi新配置
	 */
	private Map<String, String> newIoeAiConfig(Map<String, String> rateMap, String staticIncome,String teamIncome, int days, double amount, String partyId) { //静态收益
		String[] staticSplit = staticIncome.split("\\|");
		String min = staticSplit[0].split(":")[0].split("-")[0];
		String max = staticSplit[staticSplit.length -1].split(":")[0].split("-")[1];
		if (amount >= Integer.valueOf(min) && Arith.sub(amount, Double.valueOf(max)) <= 0) {
			for (int i = 0; i < staticSplit.length; i++) {
				String value = staticSplit[i];
				String[] valueSplit = value.split(":");
				int amountMin = Integer.valueOf(valueSplit[0].split("-")[0]);
				int amountMax = Integer.valueOf(valueSplit[0].split("-")[1]);
				
				if (amount >= amountMin && amount <= amountMax) {
					String daysArea = valueSplit[1];
					String[] daysSplit = daysArea.split(";");
					for (int j = 0; j < daysSplit.length; j++) {
						String[] rateSplit = daysSplit[j].split("#");
						String day = rateSplit[0];
						if (Integer.valueOf(day) == days) {
							rateMap.put("staticRate", rateSplit[1]);
						}
					}
				}
			}
		}
		// 大于最大值
		else if (Arith.sub(amount, Double.valueOf(max)) > 0) {
			String value = staticSplit[staticSplit.length -1];
			String[] valueSplit = value.split(":");
			String daysArea = valueSplit[1];
			String[] daysSplit = daysArea.split(";");
			for (int j = 0; j < daysSplit.length; j++) {
				String[] rateSplit = daysSplit[j].split("#");
				String day = rateSplit[0];
				if (Integer.valueOf(day) == days) {
					rateMap.put("staticRate", rateSplit[1]);
				}
			}
		}
		
		// 助力收益 无
		
		rateMap.put("teamRate", teamIncome);
		return rateMap;
	}
	
	/**
	 * 获取IoeAi用户级别
	 */
	public int getIoeAiLevel(String partyId) {
		
		PledgeGalaxyConfig config = getConfig(partyId);
		
		Map<String, Double> pledgeAmountMap = new HashMap<>();
		// 伞下代理人数 是 所有下级的
		List<String> partyLists = userRecomService.findChildren(partyId);
		System.out.println(partyId + " 伞下所有下级人数 " + partyLists.size());
		if (partyLists.size() < 3) {
			return -1;
		}
		
		for (String id : partyLists) {
			Party party = partyService.cachePartyBy(id, false);
			if (null != party) {
				Map<String, PledgeGalaxyOrder> map = (Map<String, PledgeGalaxyOrder>)pledgeGalaxyOrderService.findOrdersByPartyId(id);
				if (null != map && map.size() > 0) {
					for (PledgeGalaxyOrder galaxyOrder : map.values()) {
						if (galaxyOrder.getDays() >= 7 
								&& galaxyOrder.getStatus() == PledgeGalaxyStatusConstants.PLEDGE_SUCCESS) {
							if (Constants.SECURITY_ROLE_GUEST.equals(party.getRolename()) 
									|| party.getKyc_authority()) {
								double amount = pledgeAmountMap.containsKey(party.getId()) ? pledgeAmountMap.get(party.getId()) : 0;
								pledgeAmountMap.put(String.valueOf(party.getId()), Arith.add(amount, galaxyOrder.getAmount()));
							}
						}
					}
				}
			}
		}
		
		int lp1 = 0;
		int lp2 = 0;
		int lp3 = 0;
		int lp4 = 0;
		int lp5 = 0;
		String[] teamRateSplit = config.getTeamIncomeProfitRatio().split("&")[1].split("#");
		for (double amount : pledgeAmountMap.values()) {
			for (int i = 0; i < teamRateSplit.length; i++) {
				String value = teamRateSplit[i];
				String[] valueSplit = value.split(";");
				int amountMin = Integer.valueOf(valueSplit[0].split("-")[0]);
				int amountMax = Integer.valueOf(valueSplit[0].split("-")[1]);
				
				if (amount >= amountMin && amount <= amountMax) {
					if (0 == i) {
						lp1 += 1;
					} else if (1 == i) {
						lp2 += 1;
					} else if (2 == i) {
						lp3 += 1;
					} else if (3 == i) {
						lp4 += 1;
					} else if (4 == i) {
						lp5 += 1;
					}
				}
			}
		}
		
		int lp1Temp = lp1 - 3;
		if (lp1Temp < 0) {
			// 无
			return -1;
		} else {
			lp1Temp = (int)Math.floor(lp1Temp / 3); 
			int lp2Temp = lp2 + lp1Temp - 3;
			if (lp2Temp < 0) {
				// 青铜级
				return 0;
			} else {
				lp2Temp = (int)Math.floor(lp2Temp / 3); 
				int lp3Temp = lp3 + lp2Temp - 3;
				if (lp3Temp < 0) {
					// 白银级
					return 1;
				} else {
					lp3Temp = (int)Math.floor(lp3Temp / 3); 
					int lp4Temp = lp4 + lp3Temp - 3;
					if (lp4Temp < 0) {
						// 黄金级
						return 2;
					} else {
						lp4Temp = (int)Math.floor(lp4Temp / 3);
						int lp5Temp = lp5 + lp4Temp - 3;
						if (lp5Temp < 0) {
							// 铂金级
							return 3;
						} else {
							// 钻石级
							return 4;
						}
					}
				}
			}
		}
	}
	
	private Date getIoeAiTimeVersion() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String version = "2022-08-28 00:00:00";
		Date time = new Date();
	    try {
	    	time = sdf.parse(version);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	    return time;
	}
	
	public void setPartyService(PartyService partyService) {
		this.partyService = partyService;
	}

	public void setUserRecomService(UserRecomService userRecomService) {
		this.userRecomService = userRecomService;
	}

	public void setRedisHandler(RedisHandler redisHandler) {
		this.redisHandler = redisHandler;
	}

	public void setPledgeGalaxyOrderService(PledgeGalaxyOrderService pledgeGalaxyOrderService) {
		this.pledgeGalaxyOrderService = pledgeGalaxyOrderService;
	}
}
