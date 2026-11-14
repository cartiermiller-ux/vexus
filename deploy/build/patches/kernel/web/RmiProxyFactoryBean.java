package kernel.web;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.StandardContext;

import kernel.rmi.RmiFacadeImpl;
import kernel.rmi.RmiInvocationHandler;

/**
 * 远程调用代理工厂（已针对 Tomcat 9 修复：
 *  1) 原实现用 findChildren() 的 HashMap 迭代顺序分配 20001+ 端口，
 *     多个 Web 应用在同一 Tomcat 依次启动时会算出相同端口，
 *     第二个 createRegistry 抛 "ObjID already in use"，导致第 3 个应用启动失败；
 *  2) 原实现的 servletContextMaps 只在类初始化时快照一次，
 *     先启动的应用看不到后启动的 context，跨应用远程调用会找不到 Bean。
 *
 * 修复方式：
 *  - 端口改为按 contextPath 确定性哈希分配（与启动顺序、context 数量无关）；
 *  - servletContextMaps 改为动态视图，每次查找/迭代时重新读取 Tomcat 当前 children；
 *  - ROOT context（contextPath 为空）给固定端口 20999，不再 NPE。
 *
 * 本文件为补丁源文件，编译产物用于替换 WAR 中的 kernel/web/RmiProxyFactoryBean.class。
 */
public class RmiProxyFactoryBean {
	/**
	 * HTTP基础路径
	 */
	public static String baseURL;

	/**
	 * RMI路径名称
	 */
	private static final String RMI_PATH_NAME = "rmi";

	/**
	 * RMI远程端口字典表（get 时对未知 contextPath 做确定性兜底）
	 */
	public static LinkedHashMap<String, Integer> rmiPortMap;

	/**
	 * TOMCAT容器上下文字典表（动态刷新视图）
	 */
	public static LinkedHashMap<String, ServletContext> servletContextMaps;

	/**
	 * 当前 Tomcat Host 容器（用于动态枚举同容器 context）
	 */
	private static Container hostContainer;

	/**
	 * RMI 端口基数（与原实现一致）
	 */
	private static final int RMI_PORT_BASE = 20001;

	/**
	 * ROOT context 固定端口（原实现会 NPE）
	 */
	private static final int ROOT_RMI_PORT = 20999;

	/**
	 * contextPath -> RMI 端口：确定性哈希，保证任一应用在任何启动顺序下算出同一端口。
	 */
	private static int portOf(String contextPath) {
		if (contextPath == null || contextPath.trim().isEmpty()) {
			return ROOT_RMI_PORT;
		}
		int h = contextPath.hashCode() % 800;
		if (h < 0) {
			h = -h;
		}
		return RMI_PORT_BASE + h;
	}

	/**
	 * 从当前 Tomcat Host 容器枚举所有非空 contextPath 的应用上下文
	 */
	private static LinkedHashMap<String, ServletContext> snapshotContexts() {
		LinkedHashMap<String, ServletContext> map = new LinkedHashMap<String, ServletContext>();
		if (hostContainer == null) {
			return map;
		}
		try {
			Container[] children = hostContainer.findChildren();
			if (children == null) {
				return map;
			}
			for (Container container : children) {
				if (!(container instanceof Context)) {
					continue;
				}
				ServletContext sc = ((Context) container).getServletContext();
				if (sc == null) {
					continue;
				}
				String cp = sc.getContextPath();
				if (cp == null || cp.trim().isEmpty()) {
					continue;
				}
				map.put(cp, sc);
			}
		} catch (Throwable t) {
			// 容器尚未完全初始化时返回空表，后续调用会再次刷新
		}
		return map;
	}

	/**
	 * 动态上下文表：containsKey/get/entrySet 前先刷新，解决应用启动先后顺序问题
	 */
	private static class DynamicContextMap extends LinkedHashMap<String, ServletContext> {
		private static final long serialVersionUID = 1L;

		private synchronized void refresh() {
			super.clear();
			super.putAll(snapshotContexts());
		}

		@Override
		public boolean containsKey(Object key) {
			refresh();
			return super.containsKey(key);
		}

		@Override
		public ServletContext get(Object key) {
			refresh();
			return super.get(key);
		}

		@Override
		public Set<Map.Entry<String, ServletContext>> entrySet() {
			refresh();
			return super.entrySet();
		}
	}

	/**
	 * 端口表：未知 contextPath 时按确定性哈希兜底
	 */
	private static class DeterministicPortMap extends LinkedHashMap<String, Integer> {
		private static final long serialVersionUID = 1L;

		@Override
		public Integer get(Object key) {
			Integer value = super.get(key);
			if (value != null) {
				return value;
			}
			if (key instanceof String) {
				return portOf((String) key);
			}
			return ROOT_RMI_PORT;
		}
	}

	static {
		Integer tomcatPort = ApplicationUtil.getLocalPort();
		if (null == tomcatPort) {
			throw new RuntimeException("Not Found Tomcat HTTP/1.1 Port");
		}
		baseURL = new StringBuilder("rmi://127.0.0.1:%d/").append(RMI_PATH_NAME).toString();

		ServletContext currentContext;
		try {
			Method getContextMethod = ApplicationContext.class.getDeclaredMethod("getContext");
			Field contextField = ApplicationContextFacade.class.getDeclaredField("context");
			getContextMethod.setAccessible(true);
			contextField.setAccessible(true);

			currentContext = ApplicationUtil.getServletContext();
			StandardContext standardContext = (StandardContext) getContextMethod
					.invoke(contextField.get(currentContext));
			hostContainer = standardContext.getParent();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		servletContextMaps = new DynamicContextMap();
		rmiPortMap = new DeterministicPortMap();
		Iterator<String> contextPaths = snapshotContexts().keySet().iterator();
		while (contextPaths.hasNext()) {
			String contextPath = contextPaths.next();
			rmiPortMap.put(contextPath, portOf(contextPath));
		}

		int currentRmiPort = portOf(currentContext.getContextPath());
		try {
			Registry registry = LocateRegistry.createRegistry(currentRmiPort);
			registry.bind(RMI_PATH_NAME, new RmiFacadeImpl());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Object getProxyBean(String remoteBeanName, String remoteInterface, String remoteContextPath)
			throws Exception {
		if (null == remoteInterface || (remoteInterface = remoteInterface.trim()).isEmpty()) {
			throw new RuntimeException("Remote Interface Class Name Can Not Be NULL Or Empty!");
		}

		if (null != remoteBeanName) {
			remoteBeanName = remoteBeanName.trim();
			if (remoteBeanName.isEmpty()) {
				remoteBeanName = null;
			}
		}

		remoteContextPath = null == remoteContextPath ? "" : remoteContextPath.trim();

		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				new Class<?>[] { Class.forName(remoteInterface) },
				new RmiInvocationHandler(remoteBeanName, remoteInterface, remoteContextPath));
	}

	public static Object getProxyBean(String remoteBeanName, String remoteInterface) throws Exception {
		return getProxyBean(remoteBeanName, remoteInterface, "");
	}

	public static Object getProxyBean(String remoteInterface) throws Exception {
		return getProxyBean(null, remoteInterface, "");
	}
}
