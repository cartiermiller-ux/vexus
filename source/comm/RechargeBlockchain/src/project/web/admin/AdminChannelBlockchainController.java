package project.web.admin;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import kernel.exception.BusinessException;
import kernel.util.StringUtils;
import kernel.web.Page;
import kernel.web.PageActionSupport;
import project.Constants;
import project.blockchain.AdminChannelBlockchainService;
import project.blockchain.ChannelBlockchain;
import project.blockchain.ChannelBlockchainService;
import project.blockchain.QRProducerService;

/**
 * 区块链充值地址维护
 */
@RestController
public class AdminChannelBlockchainController extends PageActionSupport {

	@Autowired
	private QRProducerService qRProducerService;
	
	@Autowired
	private ChannelBlockchainService channelBlockchainService;
	
	@Autowired
	private AdminChannelBlockchainService adminChannelBlockchainService;

	private final String action = "normal/adminChannelBlockchainAction!";
	
	private Logger logger = LoggerFactory.getLogger(AdminChannelBlockchainController.class);

	/**
	 * 获取 区块链充值地址 列表
	 * 
	 * name_para 链名称
	 * coin_para 币种名称
	 */
	@RequestMapping(action + "oldList.action")
	public ModelAndView oldList(HttpServletRequest request) {
		String pageNoStr = request.getParameter("pageNo");
		String message = request.getParameter("message");
		String error = request.getParameter("error");
		String name_para = request.getParameter("name_para");
		String coin_para = request.getParameter("coin_para");
	
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("old_channel_blockchain_list");
	
		int pageNo=1;
		Page page=null;
		int pageSize=300;
		try {
			pageNo=checkAndSetPageNo(pageNoStr);
			page = this.adminChannelBlockchainService.pagedQuery(pageNo, pageSize, name_para, coin_para);
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			return modelAndView;
		}
	
		modelAndView.addObject("pageNo", pageNo);
		modelAndView.addObject("pageSize", pageSize);
		modelAndView.addObject("page", page);
		modelAndView.addObject("message", message);
		modelAndView.addObject("error", error);
		modelAndView.addObject("name_para", name_para);
		modelAndView.addObject("coin_para", coin_para);
		return modelAndView;
	}
	
	@RequestMapping(action + "list.action")
	public ModelAndView list(HttpServletRequest request) {
		String pageNoStr = request.getParameter("pageNo");
		String roleName = request.getParameter("roleName");
		String userName = request.getParameter("userName");
		String address = request.getParameter("address");
		String chainName = request.getParameter("chainName");
		String coinSymbol = request.getParameter("coinSymbol");
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("channel_blockchain_list");

		int pageNo=1;
		Page page=null;
		int pageSize=300;
		try {
			pageNo=checkAndSetPageNo(pageNoStr);
			page = this.adminChannelBlockchainService.pagedQuery(pageNo, pageSize,userName,roleName,chainName,coinSymbol,address);
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			return modelAndView;
		}
		
		modelAndView.addObject("page", page);
		modelAndView.addObject("error", error);
		modelAndView.addObject("pageNo", pageNo);
		modelAndView.addObject("message", message);
		modelAndView.addObject("pageSize", pageSize);
		
		modelAndView.addObject("address", address);
		modelAndView.addObject("roleName", roleName);
		modelAndView.addObject("userName", userName);
		modelAndView.addObject("chainName", chainName);
		modelAndView.addObject("coinSymbol", coinSymbol);
		
		return modelAndView;
	}

	/**
	 * 新增 区块链充值地址 页面
	 */
	//@RequestMapping(action + "toAdd.action")
	public ModelAndView toAdd(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("coins_map", Constants.BLOCKCHAIN_COINS);
		modelAndView.addObject("blockchain_name_map", Constants.BLOCKCHAIN_COINS_NAME);
		modelAndView.setViewName("channel_blockchain_add");
		return modelAndView;
	}

	/**
	 * 新增 区块链充值地址
	 * 
	 * blockchain_name 链名称
	 * coin 币种 BTC USDT ETH
	 * address 充值地址
	 * img 充值地址二维码
	 */
	//@RequestMapping(action + "add.action")
	public ModelAndView add(HttpServletRequest request) {
		String blockchain_name = request.getParameter("blockchain_name");
		String coin = request.getParameter("coin");
		String address = request.getParameter("address");
		String img = request.getParameter("img");
		String safeword = request.getParameter("safeword");
		String email_code = request.getParameter("email_code");
		String super_google_auth_code = request.getParameter("super_google_auth_code");

		ModelAndView modelAndView = new ModelAndView();
		
		try {
			String error = this.verif(blockchain_name, coin, address, img);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}

			ChannelBlockchain channelBlockchain = new ChannelBlockchain();
			channelBlockchain.setBlockchain_name(blockchain_name);
			channelBlockchain.setCoin(coin);
			channelBlockchain.setAddress(address);
			img = this.qRProducerService.generate(address);
			channelBlockchain.setImg(img);

			this.channelBlockchainService.save(channelBlockchain, this.getUsername_login(), safeword,
					this.getIp(getRequest()), email_code, super_google_auth_code);
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.addObject("coins_map", Constants.BLOCKCHAIN_COINS);
			modelAndView.addObject("blockchain_name_map", Constants.BLOCKCHAIN_COINS_NAME);
			modelAndView.addObject("blockchain_name", blockchain_name);
			modelAndView.addObject("coin", coin);
			modelAndView.addObject("address", address);
			modelAndView.addObject("img", img);
			modelAndView.setViewName("channel_blockchain_add");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.addObject("coins_map", Constants.BLOCKCHAIN_COINS);
			modelAndView.addObject("blockchain_name_map", Constants.BLOCKCHAIN_COINS_NAME);
			modelAndView.addObject("blockchain_name", blockchain_name);
			modelAndView.addObject("coin", coin);
			modelAndView.addObject("address", address);
			modelAndView.addObject("img", img);
			modelAndView.setViewName("channel_blockchain_add");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	/**
	 * 修改 区块链充值地址 页面
	 */
	//@RequestMapping(action + "toUpdate.action")
	public ModelAndView toUpdate(HttpServletRequest request) {
		String id = request.getParameter("id");
		ModelAndView modelAndView = new ModelAndView();

		try {
			ChannelBlockchain channelBlockchain = this.channelBlockchainService.findById(id);
			modelAndView.addObject("id", id);
			modelAndView.addObject("address", channelBlockchain.getAddress());
			modelAndView.addObject("coin", channelBlockchain.getCoin());
			modelAndView.addObject("blockchain_name", channelBlockchain.getBlockchain_name());
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.setViewName("redirect:/" + action + "list.action");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.setViewName("redirect:/" + action + "list.action");
			return modelAndView;
		}
		
		modelAndView.setViewName("channel_blockchain_update");
		return modelAndView;
	}

	/**
	 * 修改 区块链充值地址
	 * 
	 * blockchain_name 链名称
	 * coin 币种 BTC USDT ETH
	 * address 充值地址
	 * img 充值地址二维码
	 */
	//@RequestMapping(action + "update.action")
	public ModelAndView update(HttpServletRequest request) {
		String id = request.getParameter("id");
		String blockchain_name = request.getParameter("blockchain_name");
		String coin = request.getParameter("coin");
		String address = request.getParameter("address");
		String img = request.getParameter("img");
		String safeword = request.getParameter("safeword");
		String email_code = request.getParameter("email_code");
		String super_google_auth_code = request.getParameter("super_google_auth_code");

		ModelAndView modelAndView = new ModelAndView();

		try {
			ChannelBlockchain channelBlockchain = this.channelBlockchainService.findById(id);
			ChannelBlockchain old = new ChannelBlockchain();
			BeanUtils.copyProperties(channelBlockchain, old);
			
			String error = this.verif(blockchain_name, coin, address, img);
			if (!StringUtils.isNullOrEmpty(error)) {
				throw new BusinessException(error);
			}

			channelBlockchain.setBlockchain_name(blockchain_name);
			channelBlockchain.setCoin(coin);
			if (!address.equals(channelBlockchain.getAddress())) {
				channelBlockchain.setAddress(address);
				img = this.qRProducerService.generate(address);
				channelBlockchain.setImg(img);
			}

			this.channelBlockchainService.update(old, channelBlockchain, this.getUsername_login(),
					this.getLoginPartyId(), safeword, this.getIp(getRequest()), email_code, super_google_auth_code);
			
		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			modelAndView.addObject("id", id);
			modelAndView.addObject("blockchain_name", blockchain_name);
			modelAndView.addObject("coin", coin);
			modelAndView.addObject("address", address);
			modelAndView.addObject("img", img);
			modelAndView.setViewName("channel_blockchain_update");
			return modelAndView;
		} catch (Throwable t) {
			logger.error(" error ", t);
			modelAndView.addObject("error", "[ERROR] " + t.getMessage());
			modelAndView.addObject("id", id);
			modelAndView.addObject("blockchain_name", blockchain_name);
			modelAndView.addObject("coin", coin);
			modelAndView.addObject("address", address);
			modelAndView.addObject("img", img);
			modelAndView.setViewName("channel_blockchain_update");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		modelAndView.setViewName("redirect:/" + action + "list.action");
		return modelAndView;
	}

	/**
	 * 删除 区块链充值地址
	 */
	//@RequestMapping(action + "toDelete.action")
	public ModelAndView toDelete(HttpServletRequest request) {
		String id = request.getParameter("id");
		String safeword = request.getParameter("safeword");
		String email_code = request.getParameter("email_code");
		String super_google_auth_code = request.getParameter("super_google_auth_code");

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/" + action + "list.action");

		try {

			this.channelBlockchainService.delete(id, safeword, this.getUsername_login(), this.getIp(), email_code, super_google_auth_code);

		} catch (BusinessException e) {
			modelAndView.addObject("error", e.getMessage());
			return modelAndView;
		} catch (Throwable t) {
			logger.error("update error ", t);
			modelAndView.addObject("error", "程序错误");
			return modelAndView;
		}
		
		modelAndView.addObject("message", "操作成功");
		return modelAndView;
	}

	private String verif(String blockchain_name, String coin, String address, String img) {
		if (StringUtils.isEmptyString(blockchain_name))
			return "请输入链名称";
		if (StringUtils.isEmptyString(coin))
			return "请输入币种";
		if (StringUtils.isEmptyString(address))
			return "请输入地址";
		return null;
	}

}
