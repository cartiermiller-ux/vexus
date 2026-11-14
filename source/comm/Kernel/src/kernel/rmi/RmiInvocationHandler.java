package kernel.rmi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.rmi.Naming;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.WebApplicationContext;

import kernel.web.ApplicationUtil;
import kernel.web.RmiProxyFactoryBean;

/**
 * @author JORGE
 * @description RMI远程调用处理器;
 * SPRING应用上下文中的每一个代理BEAN实例对应一个处理器实例
 */
@SuppressWarnings("unchecked")
public class RmiInvocationHandler implements InvocationHandler{
	/**
	 * 当前代理的远程接口
	 */
	private RmiFacade rmiFacade;
	
	/**
	 * 远端Bean名称
	 */
	private String remoteBeanName;
	
	/**
	 * 远端接口字段类型
	 */
	private String interfaceClass;
	
	/**
	 * 当前类全名
	 */
	private String currentClassName;
	
	/**
	 * 字段注解配置的远程上下文路径
	 */
	private String remoteContextPath;
	
	/**
	 * 当前SERVLET上下文路径
	 */
	private String currentContextPath;
	
	/**
	 * 常量提示(未找到配置的SERVLET上下文路径)
	 */
	private static final String NOT_FOUND_CONTEXT="RMI-Client: Not Found Configed Context Path: %s";
	
	/**
	 * 常量提示(未实现远程接口)
	 */
	private static final String NOT_IMPLEMENTED_INTERFACE="RMI-Client: Not Implemented Remote Interface: %s";
	
	/**
	 * 常量提示(未找到当前的SERVLET上下文路径)
	 */
	private static final String CONTEXT_PATH_NOT_EXISTS="RMI-Client: Not Found Current Context Path,Not Web Application Or Root Web Application?";
	
	public RmiInvocationHandler(String interfaceClass) throws Exception{
		this(null,interfaceClass,null);
	}
	
	public RmiInvocationHandler(String proxyBeanName,String interfaceClass) throws Exception{
		this(proxyBeanName,interfaceClass,null);
	}
	
	public RmiInvocationHandler(String remoteBeanName,String interfaceClass,String remoteContextPath) throws Exception{
		this.remoteBeanName=remoteBeanName;
		this.interfaceClass=interfaceClass;
		this.remoteContextPath=remoteContextPath;
		this.currentClassName=this.getClass().getName();
		
		String contextPath=ApplicationUtil.getContextPath();
		if(null==contextPath) {
			throw new RuntimeException(CONTEXT_PATH_NOT_EXISTS);
		}else {
			this.currentContextPath=contextPath;
		}
	}
	
	@Override
	public Object invoke(Object proxy, Method localMethod, Object[] localMethodArgs) throws Throwable {
		if(ReflectionUtils.isObjectMethod(localMethod)) {
			return localMethod.invoke(this, localMethodArgs);
		}
		
		if(null!=rmiFacade) {
			RmiEntity rmiEntity=rmiFacade.invoke(interfaceClass, remoteBeanName, localMethod.getName(), new RmiEntity(localMethod,localMethodArgs));
			return rmiEntity.getResult(localMethod);
		}
		
		boolean remoteContextPathEmpty=remoteContextPath.isEmpty();
		
		if(!remoteContextPathEmpty && !RmiProxyFactoryBean.servletContextMaps.containsKey(remoteContextPath)) {
			throw new RuntimeException(String.format(Locale.ENGLISH,NOT_FOUND_CONTEXT,remoteContextPath));
		}
		
		if(remoteContextPathEmpty) {
			ServletContext remoteServletContext=null;
			for(Map.Entry<String, ServletContext> scEntry:RmiProxyFactoryBean.servletContextMaps.entrySet()) {
				String remotePath=scEntry.getKey();
				if(currentContextPath.equals(remotePath)) continue;
				if(null==(remoteServletContext=scEntry.getValue())) continue;
				if(hasRemoteBean(remoteServletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE))) {
					this.remoteContextPath=remotePath;
					break;
				}
			}
		}
		
		if(remoteContextPath.isEmpty()) {
			throw new RuntimeException(String.format(Locale.ENGLISH,NOT_IMPLEMENTED_INTERFACE,interfaceClass));
		}
		
		String requestURL=String.format(Locale.ENGLISH,RmiProxyFactoryBean.baseURL,RmiProxyFactoryBean.rmiPortMap.get(remoteContextPath));
		RmiEntity rmiEntity=(this.rmiFacade=(RmiFacade)Naming.lookup(requestURL)).invoke(interfaceClass, remoteBeanName, localMethod.getName(), new RmiEntity(localMethod,localMethodArgs));
		
		return rmiEntity.getResult(localMethod);
	}
	
	/**
	 * 判断参数上下文中是否存在当前RMI接口的远程BEAN实现
	 * @param remoteApplicationContext 远程应用上下文
	 * @return 是否存在远程BEAN实现
	 * @throws Exception
	 */
	private final boolean hasRemoteBean(Object remoteApplicationContext) throws Exception{
		if(null==remoteApplicationContext) return false;
		
		Class<?> remoteAppContextClass=remoteApplicationContext.getClass();
		Method getBeansOfTypeMethod=ReflectionUtils.findMethod(remoteAppContextClass,"getBeansOfType",Class.class);
		Map<String,Object> beanMap=(Map<String,Object>)getBeansOfTypeMethod.invoke(remoteApplicationContext,remoteAppContextClass.getClassLoader().loadClass(interfaceClass));
		
		if(null==beanMap || beanMap.isEmpty()) return false;
		
		Set<Object> targetBeanSet=beanMap.values().stream()
				.filter(targetBean->{
					if(null==targetBean) return false;
					String beanInfo=targetBean.toString();
					int index=beanInfo.indexOf("@");
					if(-1==index) return false;
					return !currentClassName.equals(beanInfo.substring(0,index));
					})
				.collect(Collectors.toSet());
		
		return !targetBeanSet.isEmpty();
	}
}
