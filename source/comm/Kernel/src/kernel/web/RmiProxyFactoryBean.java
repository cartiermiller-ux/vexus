package kernel.web;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.StandardContext;

import kernel.rmi.RmiFacadeImpl;
import kernel.rmi.RmiInvocationHandler;

/**
 * @author JORGE
 * @description 远程调用代理工厂;
 * 每一个应用类装载器对应一个代理工厂类,本代理工厂类不会创建代理工厂实例本身,它创建的每一个代理BEAN实例绑定一个对应的处理器实例
 */
public class RmiProxyFactoryBean {
	/**
	 * HTTP基础路径
	 */
	public static String baseURL;
	
	/**
	 * RMI路径名称
	 */
	private static final String RMI_PATH_NAME="rmi";
	
	/**
	 * RMI远程端口字典表
	 */
	public static LinkedHashMap<String,Integer> rmiPortMap;
	
	/**
	 * TOMCAT容器上下文字典表
	 */
	public static LinkedHashMap<String,ServletContext> servletContextMaps;
	
	static {
		Integer tomcatPort = ApplicationUtil.getLocalPort();
		if(null==tomcatPort) new RuntimeException("Not Found Tomcat HTTP/1.1 Port");
		baseURL=new StringBuilder("rmi://127.0.0.1:%d/").append(RMI_PATH_NAME).toString();
		
		ServletContext currentContext=null;
		try {
			Method getContextMethod=ApplicationContext.class.getDeclaredMethod("getContext");
			Field contextField=ApplicationContextFacade.class.getDeclaredField("context");
			getContextMethod.setAccessible(true);
			contextField.setAccessible(true);
			
			currentContext=ApplicationUtil.getServletContext();
			StandardContext standardContext=(StandardContext)getContextMethod.invoke(contextField.get(currentContext));
			servletContextMaps=Arrays.stream(standardContext.getParent().findChildren())
					.map(container->((Context)container).getServletContext())
					.filter(sc->null!=sc && !sc.getContextPath().trim().isEmpty())
					.collect(Collectors.toMap(sc->sc.getContextPath(),sc->sc,(oldSc,newSc)->oldSc,LinkedHashMap::new));
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		rmiPortMap=new LinkedHashMap<String,Integer>();
		Iterator<String> contextPaths=servletContextMaps.keySet().iterator();
		for(int rmiPort=20001;contextPaths.hasNext();rmiPortMap.put(contextPaths.next(),rmiPort++));
		
		int currentRmiPort=rmiPortMap.get(currentContext.getContextPath());
		try {
			Registry registry= LocateRegistry.createRegistry(currentRmiPort);
	        registry.bind(RMI_PATH_NAME,new RmiFacadeImpl());
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Object getProxyBean(String remoteBeanName,String remoteInterface,String remoteContextPath) throws Exception {
		if(null==remoteInterface || (remoteInterface=remoteInterface.trim()).isEmpty()) {
			throw new RuntimeException("Remote Interface Class Name Can Not Be NULL Or Empty!");
		}
		
		if(null!=remoteBeanName) {
			remoteBeanName=remoteBeanName.trim();
			if(remoteBeanName.isEmpty()) remoteBeanName=null;
		}
		
		remoteContextPath=null==remoteContextPath?"":remoteContextPath.trim();
		
		return Proxy.newProxyInstance(
				Thread.currentThread().getContextClassLoader(),
				new Class<?>[] {Class.forName(remoteInterface)},
				new RmiInvocationHandler(remoteBeanName,remoteInterface,remoteContextPath));
	}
	
	public static Object getProxyBean(String remoteBeanName,String remoteInterface) throws Exception {
		return getProxyBean(remoteBeanName,remoteInterface,"");
	}
	
	public static Object getProxyBean(String remoteInterface) throws Exception {
		return getProxyBean(null,remoteInterface,"");
	}
}
