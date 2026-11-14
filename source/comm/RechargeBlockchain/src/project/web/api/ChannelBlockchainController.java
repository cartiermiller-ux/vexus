package project.web.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import kernel.bo.RecordObjectMapper;
import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.ApplicationUtil;
import kernel.web.BaseAction;
import kernel.web.ResultObject;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import project.Constants;
import project.blockchain.ChannelBlockchain;
import project.blockchain.ChannelBlockchainService;
import project.blockchain.MD5;
import project.party.PartyRedisKeys;
import project.party.model.Party;
import project.redis.RedisHandler;
import project.syspara.SysparaService;

/**
 * 区块链
 */
@RestController
@CrossOrigin
public class ChannelBlockchainController extends BaseAction {
	
	@Autowired
	private SysparaService sysparaService;

	private final String action = "/api/channelBlockchain!";

	@Autowired
	private ChannelBlockchainService channelBlockchainService;
	
	private OkHttpClient okHttpClient = new OkHttpClient();

	private final String threedUrl = "https://api.star-pay.vip/api/gateway/pay";
	
	private Logger logger = LoggerFactory.getLogger(ChannelBlockchainController.class);
	
	/**
	 * 获取所有链地址
	 */
	@RequestMapping(action + "list.action")
	public Object list() throws IOException {
		List<ChannelBlockchain> data = new ArrayList<ChannelBlockchain>();

		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			resultObject.setData(data);
			return resultObject;
		}
		
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		RedisHandler redisHandler=ApplicationUtil.getBean(RedisHandler.class);
		
		String partyId=this.getLoginPartyId();
		Party party = (Party) redisHandler.get(PartyRedisKeys.PARTY_ID + partyId);
		List<HashMap> list=jdbcTemplate.query("SELECT * FROM T_PARTY_BLOCKCHAIN WHERE USER_NAME=?",RecordObjectMapper.newInstance(HashMap.class),party.getUsername());
		
		if(null!=list && !list.isEmpty()) {
			data=list.stream().map(dict->{
				String qrImage=(String)dict.get("QR_IMAGE");
				String chainAddress=(String)dict.get("ADDRESS");
				String chainName=(String)dict.get("CHAIN_NAME");
				String coinSymbol=(String)dict.get("COIN_SYMBOL");
				String autoStr=(String)dict.get("AUTO");
				boolean auto=autoStr.equals("Y")?true:false;
				
				ChannelBlockchain cbc=new ChannelBlockchain();
				cbc.setBlockchain_name(chainName);
				cbc.setAddress(chainAddress);
				cbc.setCoin(coinSymbol);
				cbc.setAuto(auto);
				cbc.setImg(qrImage);
				
				return cbc;
			}).collect(Collectors.toList());
		}
		
		try {
			if(data.isEmpty()) data=this.channelBlockchainService.findAll();
			
			for (int i = 0; i < data.size(); i++) {				
				if (1 == this.sysparaService.find("can_recharge").getInteger()) {
					
					// 允许在线充值，展示二维码
					if (!StringUtils.isNullOrEmpty(data.get(i).getImg())) {
						String path = Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + data.get(i).getImg();
						data.get(i).setImg(path);
					}
				} else {
					data.get(i).setImg(null);
					data.get(i).setAddress(null);
				}
			}
			
			resultObject.setData(data);
			
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}
	
	/**
	 * 根据币种获取链地址
	 */
	@RequestMapping(action + "getBlockchainName.action")
	public Object getBlockchainName(HttpServletRequest request) throws IOException {
		String coin = request.getParameter("coin");

		List<ChannelBlockchain> data = new ArrayList<ChannelBlockchain>();
		
		ResultObject resultObject = new ResultObject();
		resultObject = this.readSecurityContextFromSession(resultObject);
		if (!"0".equals(resultObject.getCode())) {
			resultObject.setData(data);
			return resultObject;
		}
		
		JdbcTemplate jdbcTemplate=ApplicationUtil.getBean(JdbcTemplate.class);
		RedisHandler redisHandler=ApplicationUtil.getBean(RedisHandler.class);
		
		String partyId=this.getLoginPartyId();
		Party party = (Party) redisHandler.get(PartyRedisKeys.PARTY_ID + partyId);
		List<HashMap> list=jdbcTemplate.query("SELECT * FROM T_PARTY_BLOCKCHAIN WHERE USER_NAME=? AND COIN_SYMBOL=?",RecordObjectMapper.newInstance(HashMap.class),party.getUsername(),coin);
		
		if(null!=list && !list.isEmpty()) {
			data=list.stream().map(dict->{
				String qrImage=(String)dict.get("QR_IMAGE");
				String chainAddress=(String)dict.get("ADDRESS");
				String chainName=(String)dict.get("CHAIN_NAME");
				String coinSymbol=(String)dict.get("COIN_SYMBOL");
				String autoStr=(String)dict.get("AUTO");
				boolean auto=autoStr.equals("Y")?true:false;
				
				ChannelBlockchain cbc=new ChannelBlockchain();
				cbc.setBlockchain_name(chainName);
				cbc.setAddress(chainAddress);
				cbc.setCoin(coinSymbol);
				cbc.setAuto(auto);
				cbc.setImg(qrImage);
				
				return cbc;
			}).collect(Collectors.toList());
		}
		
		try {
			if(data.isEmpty()) data=this.channelBlockchainService.findByCoin(coin);
			
			for (int i = 0; i < data.size(); i++) {
				if (1 == this.sysparaService.find("can_recharge").getInteger()) {
					
					if (!StringUtils.isNullOrEmpty(data.get(i).getImg())) {
						
						String path = Constants.WEB_URL + "/public/showimg!showImg.action?imagePath=" + data.get(i).getImg();
						data.get(i).setImg_str("/public/showimg!showImg.action?imagePath=" + data.get(i).getImg());
						data.get(i).setImg(path);
					}
				} else {
					data.get(i).setImg(null);
					data.get(i).setImg_str(null);
					data.get(i).setAddress(null);
				}
			}

			resultObject.setData(data);
			
		} catch (BusinessException e) {
			resultObject.setCode("1");
			resultObject.setMsg(e.getMessage());
		} catch (Throwable t) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", t);
		}

		return resultObject;
	}
	
	/**
	 * 根据第三方充值链接
	 */
	@RequestMapping(action + "getThirdUrl.action")
	public Object getThirdUrl(HttpServletRequest request) throws IOException {
		ResultObject resultObject = new ResultObject();
		String key = this.sysparaService.find("third_key").getValue();
		String merchant_no = this.sysparaService.find("third_id").getValue();
		int i=(int)(Math.random()*900)+100;        
        //商户号
		//10位时间戳
        String timestamp = ""+System.currentTimeMillis()/1000;
		String amount = request.getParameter("amount");		
		//法币名称
		String fiat_currency = request.getParameter("fiat_currency");
		//订单号
		String merchant_ref = System.currentTimeMillis()+""+i;
		//产品名称
        String product = "ERC20Buy";
        String sign_type = "MD5";
        Map<String,String> requestparam = new HashMap<>();
        JSONObject params = new JSONObject(new LinkedHashMap<>());
        JSONObject extra = new JSONObject(new LinkedHashMap<>());
        extra.put("fiat_currency", fiat_currency);
        params.put("merchant_ref", merchant_ref);
        params.put("product", product);
        params.put("amount", amount);
        params.put("extra", extra);
        String waitsign = merchant_no+params+sign_type+timestamp+key;
        String sign = MD5.sign(waitsign);
        requestparam.put("merchant_no",merchant_no);
        requestparam.put("params",params.toJSONString());
        requestparam.put("sign",sign);
        requestparam.put("sign_type",sign_type);
        requestparam.put("timestamp",params.toJSONString());
        String result = "";
        RequestBody body = new FormBody.Builder()
				.add("merchant_no", merchant_no)
				.add("timestamp", timestamp)
				.add("sign_type", sign_type)
				.add("params", params.toString())
				.add("sign", sign)
				.build();
		Request requestParm = new Request.Builder()
				.url(threedUrl)
				.method("POST", body)
				.addHeader("Content-Type", "application/x-www-form-urlencoded")
				.build();
		
        try {
            Response response = okHttpClient.newCall(requestParm).execute();
            result = response.body().string();   
		} catch (Exception e) {
        	resultObject.setCode("1");
        	resultObject.setMsg("请求第三方失败");
		}
        JSONObject resultJson = JSON.parseObject(result);
        Integer code = resultJson.getInteger("code");
        if(code == 200) {
        	resultObject.setCode("0");
        	JSONObject resultParams = resultJson.getJSONObject("params");
        	resultObject.setData(resultParams.getString("payurl")); 
        }else {
        	resultObject.setCode("1");
        	resultObject.setMsg(resultJson.getString("message"));
        }
		return resultObject;
	}

}
