package kernel.rmi;

import java.rmi.Remote;

/**
 * @author JORGE
 * @description RMI通用接口
 */
public interface RmiFacade extends Remote {
	/**
	 * RMI通用接口函数
	 * @param interfaceClass 接口类型
	 * @param beanName BEAN名称
	 * @param methodName 方法名称
	 * @param rmiEntity RMI参数实体
	 * @return RMI结果实体
	 * @throws Exception 系统编译异常
	 */
	public RmiEntity invoke(String interfaceClass,String beanName,String methodName,RmiEntity rmiEntity) throws Exception;
}
