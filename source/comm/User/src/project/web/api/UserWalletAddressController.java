package project.web.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import io.netty.util.internal.StringUtil;
import kernel.web.ResultObject;
import project.user.UserWalletAddress;
import project.user.UserWalletAddressService;

/**
 * 用户钱包地址
 *
 */
@RestController
@CrossOrigin
public class UserWalletAddressController {
	
	@Autowired
	UserWalletAddressService userWalletAddressService;

	private final String action = "/api/user!";
	
	/**
	 * 未生成钱包地址的用户
	 */
	@RequestMapping(action + "queryNoWalletAddressUser.action")
	public Object queryNoWalletAddressUser() {
		ResultObject resultObject = new ResultObject();
		// 未生成钱包地址
		String state = "0";
		List<UserWalletAddress> list = userWalletAddressService.queryByState(state);
		List<String> partyIds = new ArrayList<>();
		for(UserWalletAddress wallet : list) {
			partyIds.add(wallet.getPartyId());
		}
		resultObject.setData(partyIds);
		return resultObject;
	}
	
	/**
	 * 接收生成好的钱包地址
	 */
	@RequestMapping(action + "receiveUserWalletAddress.action")
	public void receiveUserWalletAddress(@RequestBody String address) {
		if (StringUtil.isNullOrEmpty(address)) {
			return;
		}
		
		JSONObject postResult = JSONObject.parseObject(address);
		List<UserWalletAddress> userIds = JSONObject.parseArray(postResult.getJSONArray("address").toJSONString(), UserWalletAddress.class);
		userWalletAddressService.updateBatch(userIds);
	}

}
