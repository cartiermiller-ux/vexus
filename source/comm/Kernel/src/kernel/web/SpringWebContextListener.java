package kernel.web;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author JORGE
 * @description WEB应用监听器
 */
public class SpringWebContextListener extends ContextLoader implements ServletContextListener {
	/**
	 * SLF4J日志工具
	 */
	private static final Logger logger=LoggerFactory.getLogger(SpringWebContextListener.class);
	
	public SpringWebContextListener(WebApplicationContext context) {
		super(context);
	}

	public SpringWebContextListener() {
		super.setContextInitializers(new ApplicationContextInitializer<ConfigurableApplicationContext>() {
			public void initialize(ConfigurableApplicationContext applicationContext) {
				if(applicationContext instanceof WebApplicationContext) {
					ApplicationUtil.setApplicationContext((WebApplicationContext)applicationContext);
				}
			}
		});
	}
	
	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext servletContext=event.getServletContext();
		if (null==servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE)) {
			super.initWebApplicationContext(servletContext);
		}
		ApplicationUtil.setServletContext(servletContext);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		ServletContext servletContext=event.getServletContext();
		closeWebApplicationContext(servletContext);
		
		Enumeration<String> attrNames = servletContext.getAttributeNames();
		while (attrNames.hasMoreElements()) {
			String attrName = attrNames.nextElement();
			if (!attrName.startsWith("org.springframework.")) continue;
			Object attrValue = servletContext.getAttribute(attrName);
			if (!(attrValue instanceof DisposableBean)) continue;
			try {
				((DisposableBean) attrValue).destroy();
			}catch (Throwable e) {
				if (logger.isWarnEnabled()) {
					logger.warn("Invocation of destroy method failed on ServletContext "+"attribute with name '" + attrName + "'", e);
				}
			}
		}
	}
	
}
