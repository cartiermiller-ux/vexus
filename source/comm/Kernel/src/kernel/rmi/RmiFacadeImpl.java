package kernel.rmi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;

import kernel.web.ApplicationUtil;

/**
 * @author JORGE
 * @description RMI通用接口实现;
 * 本类在RMI架构中缺省为单例实现,即每一个应用类装载器对应一个RMI通用接口实例
 */
@SuppressWarnings({"serial","unused","unchecked"})
public class RmiFacadeImpl extends UnicastRemoteObject implements RmiFacade{
	/**
	 * 常量提示(未实现远程接口)
	 */
	private static final String NOT_IMPLEMENTED_INTERFACE="RMI-Server: %s : Not Implemented Remote Interface: %s";
	
	/**
	 * 常量提示(未实现远程方法)
	 */
	private static final String NOT_IMPLEMENTED_METHOD="RMI-Server: Remote Method Not Implemented For Interface: %s";
	
	/**
	 * BeanMethod映射字典
	 */
	private static final ConcurrentHashMap<String,BeanMethod> beanMethodMap = new ConcurrentHashMap<String,BeanMethod>();
	
	/**
	 * @throws RemoteException
	 */
	public RmiFacadeImpl() throws RemoteException {}
	
	/**
	 * 通用远程接口实现
	 * @param interfaceClass 远程接口类型
	 * @param beanName BEAN名称
	 * @param methodName 方法名
	 * @param rmiEntity RMI实体
	 * @return RMI实体
	 */
	public RmiEntity invoke(String interfaceClass,String beanName,String methodName,RmiEntity rmiEntity) throws Exception {
		String cacheKey=null;
		Object[] methodArgs=rmiEntity.getParams();
		Class<?>[] paramTypes=rmiEntity.paramTypes;
		boolean emptyParams=(null==paramTypes || 0==paramTypes.length);
		String paramTypeStrs=emptyParams?"()":Arrays.stream(paramTypes).map(paramType->paramType.getName()).collect(Collectors.joining(",","(",")"));
		
		BeanMethod beanMethod=beanMethodMap.get(cacheKey=new StringBuilder(interfaceClass).append(".")
		.append(beanName).append(".").append(methodName).append(paramTypeStrs).toString());
		if(null!=beanMethod) return beanMethod.invoke(methodArgs);
		
		ApplicationContext appContext=ApplicationUtil.getApplicationContext();
		Class<?> intetrfaceType=appContext.getClass().getClassLoader().loadClass(interfaceClass);
		Map<String,Object> beanMap=(Map<String,Object>)appContext.getBeansOfType(intetrfaceType);
		
		if(null==beanMap || beanMap.isEmpty()) {
			throw new RuntimeException(String.format(Locale.ENGLISH,NOT_IMPLEMENTED_INTERFACE,ApplicationUtil.getContextPath(),interfaceClass));
		}
		
		Method targetMethod=emptyParams?findMethod(intetrfaceType,methodName):findMethod(intetrfaceType,methodName,paramTypes);
		
		if(null==targetMethod) {
			throw new RuntimeException(String.format(Locale.ENGLISH,NOT_IMPLEMENTED_METHOD,cacheKey));
		}
		
		Object beanImpl=beanMap.values().iterator().next();
		beanMethodMap.put(cacheKey,beanMethod=new BeanMethod(null==beanName?beanImpl:beanMap.getOrDefault(beanName,beanImpl),targetMethod));
		
		return beanMethod.invoke(methodArgs);
	}
	
	/**
	 * 查找本地方法
	 * @param beanClass BEAN类型
	 * @param methodName 方法名
	 * @return 本地方法对象
	 */
	private static final Method findMethod(Class<?> beanClass,String methodName) {
		while (null!=beanClass) {
			Method[] targetMethods = beanClass.getDeclaredMethods();
			for (Method targetMethod : targetMethods) {
				int mod=targetMethod.getModifiers();
				if(!Modifier.isPublic(mod) || Modifier.isStatic(mod)) continue;
				if (!methodName.equals(targetMethod.getName())) continue;
				if(0!=targetMethod.getParameterCount()) continue;
				return targetMethod;
			}
			beanClass = beanClass.getSuperclass();
		}
		return null;
	}
	
	/**
	 * 查找本地方法
	 * @param beanClass BEAN类型
	 * @param methodName 方法名
	 * @param paramTypes 参数类型表
	 * @return 本地方法对象
	 */
	private static final Method findMethod(Class<?> beanClass,String methodName,Class<?>[] paramTypes) {
		while (null!=beanClass) {
			Method[] targetMethods = beanClass.getDeclaredMethods();
			out:for (Method targetMethod : targetMethods) {
				int mod=targetMethod.getModifiers();
				if(!Modifier.isPublic(mod) || Modifier.isStatic(mod)) continue;
				if (!methodName.equals(targetMethod.getName())) continue;
				if(0==targetMethod.getParameterCount()) continue;
				
				Class<?>[] targetMethodArgs=targetMethod.getParameterTypes();
				if(paramTypes.length!=targetMethodArgs.length) continue;
				
				for(int i=0;i<paramTypes.length;i++) {
					if(ApplicationUtil.compatible(targetMethodArgs[i],paramTypes[i])) {
						continue;
					}else {
						continue out;
					}
				}
				
				return targetMethod;
			}
			beanClass = beanClass.getSuperclass();
		}
		return null;
	}
	
	/**
	 * @author JORGE
	 * @description BeanMethod
	 */
	private static class BeanMethod{
		/**
		 * BEAN实现
		 */
		private Object targetBean;
		
		/**
		 * 实现方法
		 */
		private Method targetMethod;
		
		public BeanMethod(Object targetBean,Method targetMethod) {
			this.targetBean=targetBean;
			this.targetMethod=targetMethod;
		}
		
		public RmiEntity invoke(Object[] methodArgs) throws Exception {
			Object result=null;
			try {
				result=targetMethod.invoke(targetBean, methodArgs);
			}catch(InvocationTargetException e) {
				result=e.getCause();
				((Throwable)result).printStackTrace();
			}catch(Throwable e) {
				result=e;
				e.printStackTrace();
			}
			
			return new RmiEntity(result);
		}
	}
}
