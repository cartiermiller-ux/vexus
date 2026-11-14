package project.blockchain;

import kernel.web.Page;

/**
 * 后台区块链地址查询
 *
 */
public interface AdminChannelBlockchainService {

	/**
	 * 代理分页查询 name_para链名称，coin_para币种名称
	 */
	public Page pagedQuery(int pageNo, int pageSize, String name_para, String coin_para);
	
	/**
	 * 查找用户自定义的充值地址表
	 * @param pageNo 页号
	 * @param pageSize 页面尺寸
	 * @param userName 用户名
	 * @param roleName 账户类型
	 * @param chainName 链名
	 * @param coinSymbol 币种
	 * @param address 充值地址
	 * @return 列表页面
	 */
	public Page pagedQuery(int pageNo, int pageSize, String userName, String roleName,String chainName,String coinSymbol,String address);
}
