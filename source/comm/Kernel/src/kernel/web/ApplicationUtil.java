package kernel.web;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.FieldFilter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.beust.jcommander.Strings;
import com.google.common.collect.Sets;

import kernel.bo.RecordObjectMapper;

/**
 * @author JORGE
 * @description 应用工具
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class ApplicationUtil {
	/**
	 * Servlet上下文
	 */
	private static ServletContext servletContext;
	
	/**
	 * Spring上下文
	 */
	private static WebApplicationContext applicationContext;
	
	/**
	 * 空白正则式
	 */
	private static final Pattern BLANK_REGEX=Pattern.compile("\\s+");
	
	/**
	 * 八字节缓冲
	 */
	private static final ByteBuffer EIGHT_BUFFER = ByteBuffer.allocate(8);
	
	/**
	 * 简单类型
	 */
	private static LinkedHashSet<Class<?>> SIMPLE_CLASSES=new LinkedHashSet<Class<?>>();
	
	/**
	 * 全局应用值字典
	 */
	private static final ConcurrentHashMap<String,Object> VALUE_MAP=new ConcurrentHashMap<String,Object>();
	
	/**
	 * 增删改DML操作
	 */
	private static final HashSet<String> DML_OPTIONS=new HashSet<String>(Arrays.asList("DELETE","UPDATE","INSERT"));
	
	static {
		SIMPLE_CLASSES.add(java.lang.String.class);
		SIMPLE_CLASSES.add(Integer.class);
		SIMPLE_CLASSES.add(Double.class);
		SIMPLE_CLASSES.add(Boolean.class);
		SIMPLE_CLASSES.add(Long.class);
		
		SIMPLE_CLASSES.add(java.sql.Date.class);
		SIMPLE_CLASSES.add(java.sql.Time.class);
		SIMPLE_CLASSES.add(java.util.Date.class);
		SIMPLE_CLASSES.add(java.sql.Timestamp.class);
		SIMPLE_CLASSES.add(java.util.Calendar.class);
		SIMPLE_CLASSES.add(Character.class);
		SIMPLE_CLASSES.add(Float.class);
		
		SIMPLE_CLASSES.add(int.class);
		SIMPLE_CLASSES.add(double.class);
		SIMPLE_CLASSES.add(boolean.class);
		SIMPLE_CLASSES.add(long.class);
		SIMPLE_CLASSES.add(char.class);
		SIMPLE_CLASSES.add(float.class);
		SIMPLE_CLASSES.add(byte.class);
		SIMPLE_CLASSES.add(short.class);
		SIMPLE_CLASSES.add(Byte.class);
		SIMPLE_CLASSES.add(Short.class);
	}
	
	/**
	 * 获取客户端会话对象
	 * @return 会话对象
	 */
	public static HttpSession getHttpSession(boolean... creates) {
		boolean create=(null==creates || 0==creates.length)?true:creates[0];
		HttpServletRequest request=getHttpServletRequest();
		return null==request?null:request.getSession(create);
	}
	
	/**
	 * 获取当前请求对象
	 * @return 请求对象
	 */
	public static HttpServletRequest getHttpServletRequest() {
		ServletRequestAttributes sra=getServletRequestAttributes();
		return null==sra?null:sra.getRequest();
	}
	
	/**
	 * 获取当前响应对象
	 * @return 响应对象
	 */
	public static HttpServletResponse getHttpServletResponse() {
		ServletRequestAttributes sra=getServletRequestAttributes();
		return null==sra?null:sra.getResponse();
	}
	
	/**
	 * 当前系统是否为Windows
	 */
	public static final boolean isWindows() {
		return ManagementFactory.getOperatingSystemMXBean()
				.getName().trim()
				.toLowerCase()
				.startsWith("win")?true:false;
	}
	
	/**
	 * 获取当前上下文路径
	 * @return 上下文路径
	 */
	public static String getContextPath() {
		ServletContext currentContext=getServletContext();
		return null==currentContext?null:currentContext.getContextPath();
	}
	
	/**
	 * 获取异常根因
	 * @return 异常对象
	 */
	public static Throwable getRootCause(Throwable e) {
		Throwable cause=e.getCause();
		if(null==cause) return e;
		return getRootCause(cause);
	}
	
	/**
	 * 获取异常根信息
	 * @return 异常信息
	 */
	public static String getRootMessage(Throwable e) {
		if(null==e) return null;
		return getRootCause(e).getMessage();
	}
	
	/**
	 * 是否为声明式异常类型
	 * @param e 异常实例
	 * @param method 声明方法
	 * @return 是否为声明式异常类型
	 */
	public static final boolean isDeclaredException(Throwable e,Method method) {
		if(e instanceof Error) return true;
		if(e instanceof RuntimeException) return true;
		for(Class<?> type:method.getExceptionTypes()) {
			if(type.isInstance(e)) return true;
		}
		return false;
	}
	
	/**
	 * 获取声明原因
	 * @param e 异常实例
	 * @return 声明原因
	 */
	public static final Throwable getDeclaredCause(Throwable e,Method method) {
		if(e instanceof Error) return e;
		if(e instanceof RuntimeException) return e;
		for(Class<?> type:method.getExceptionTypes()) {
			if(type.isInstance(e)) return e;
		}
		return getDeclaredCause(e.getCause(),method);
	}
	
	/**
	 * 获取当前SERVLET请求上下文
	 * @return SERVLET请求上下文
	 */
	public static ServletRequestAttributes getServletRequestAttributes() {
		RequestAttributes requestAttributes=null;
		try {
			requestAttributes=RequestContextHolder.currentRequestAttributes();
		}catch(IllegalStateException e) {
			e.printStackTrace();
		}
		
		if(null==requestAttributes) return null;
		
		return (requestAttributes instanceof ServletRequestAttributes)?(ServletRequestAttributes)requestAttributes:null;
	}
	
	/**
	 * 获取SERVLET上下文
	 * @return SERVLET上下文
	 */
	public static ServletContext getServletContext() {
		if(null==ApplicationUtil.servletContext) {
			if(null==applicationContext) return null;
			Map<String,ServletContext> scMap=applicationContext.getBeansOfType(ServletContext.class);
			if(null==scMap || scMap.isEmpty()) return null;
			if(scMap.size()>1) throw new RuntimeException("Found Multi-ServletContext In Spring ApplicationContext!");
			ApplicationUtil.servletContext=scMap.values().iterator().next();
		}
		
		return ApplicationUtil.servletContext;
	}

	/**
	 * 获取应用上下文
	 * @return 应用上下文
	 */
	public static ApplicationContext getApplicationContext() {
		if(null==ApplicationUtil.applicationContext) {
			if(null==servletContext) return null;
			Object attr = servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
			
			if (null==attr) return null;
			if (attr instanceof Error) throw (Error) attr;
			if (attr instanceof RuntimeException) throw (RuntimeException) attr;
			if (attr instanceof Exception) throw new IllegalStateException((Exception) attr);
			if (!(attr instanceof WebApplicationContext)) throw new IllegalStateException("Context attribute is not of type WebApplicationContext: " + attr);
			
			ApplicationUtil.applicationContext=(WebApplicationContext) attr;
		}
		
		return ApplicationUtil.applicationContext;
	}
	
	/**
	 * 获取应用SPRING上下文类装载器
	 * @return 类装载器
	 */
	public static ClassLoader getApplicationClassLoader() {
		ApplicationContext appContext=getApplicationContext();
		return null==appContext?null:appContext.getClass().getClassLoader();
	}
	
	/**
	 * 获取Spring应用上下文类
	 * @param classFullName 全类名
	 * @return 应用上下文类
	 */
	public static Class<?> getApplicationClass(String classFullName) {
		ClassLoader classLoader=getApplicationClassLoader();
		try {
			return null==classLoader?null:classLoader.loadClass(classFullName);
		} catch (ClassNotFoundException e) {
			System.err.println(e.getMessage());
			return null;
		}
	}
	
	/**
	 * 获取本地HTTP端口
	 * @return 本地HTTP端口
	 */
	public static Integer getLocalPort() {
		Set<ObjectName> objectNames=null;
        MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"), Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
        } catch (MalformedObjectNameException e) {
        	e.printStackTrace();
            return null;
        }
        
        if(null==objectNames || objectNames.isEmpty()) return null;
        
        String port=null;
        try {
            port = objectNames.iterator().next().getKeyProperty("port");
        } catch (NoSuchElementException e) {
        	e.printStackTrace();
            return null;
        }
        
        return (null==port || (port=port.trim()).isEmpty())?null:Integer.parseInt(port);
    }
	
	/**
	 * 设置SPRING上下文
	 * @param applicationContext SPRING上下文
	 */
	public static void setApplicationContext(WebApplicationContext applicationContext) {
		Assert.notNull(applicationContext, "ApplicationContext must not be null");
		ApplicationUtil.applicationContext = applicationContext;
	}

	/**
	 * 设置SERVLET上下文
	 * @param servletContext SERVLET上下文
	 */
	public static void setServletContext(ServletContext servletContext) {
		Assert.notNull(servletContext, "ServletContext must not be null");
		ApplicationUtil.servletContext = servletContext;
	}
	
	public static <T> T getAttribute(String key) {
		return (T)servletContext.getAttribute(key);
	}
	
	public static void removeAttribute(String key) {
		servletContext.removeAttribute(key);
	}
	
	public static void setAttribute(String key,Object value) {
		servletContext.setAttribute(key, value);
	}
	
	public static <T> T getBean(String beanId) {
		return (T)applicationContext.getBean(beanId);
	}
	
	public static <T> T getBean(Class<T> beanType) {
		return applicationContext.getBean(beanType);
	}
	
	public static <T> Map<String,T> getBeanOfMap(Class<T> beanType) {
		return applicationContext.getBeansOfType(beanType,true,true);
	}
	
	public static final void setValue(String key,Object value) {
		VALUE_MAP.put(key, value);
	}
	
	public static final <T> T getValue(String key) {
		return (T)VALUE_MAP.get(key);
	}
	
	public static final void delValue(String key) {
		VALUE_MAP.remove(key);
	}
	
	public static final void delAllValues() {
		VALUE_MAP.clear();
	}
	
	public static Object getBeanWithClassName(String classFullName) {
		ApplicationContext appContext=getApplicationContext();
		if(null==appContext) return null;
		ClassLoader classLoader=appContext.getClass().getClassLoader();
		if(null==classLoader) return null;
		
		Class<?> loadedClass=null;
		try {
			loadedClass = classLoader.loadClass(classFullName);
		} catch (ClassNotFoundException e) {
			return null;
		}
		
		return appContext.getBean(loadedClass);
	}
	
	public static Map<String,?> getBeanOfMapWithClassName(String classFullName) {
		ApplicationContext appContext=getApplicationContext();
		if(null==appContext) return null;
		ClassLoader classLoader=appContext.getClass().getClassLoader();
		if(null==classLoader) return null;
		
		Class<?> loadedClass=null;
		try {
			loadedClass = classLoader.loadClass(classFullName);
		} catch (ClassNotFoundException e) {
			return null;
		}
		
		return appContext.getBeansOfType(loadedClass,true,true);
	}
	
	/**
	 * 根据JVM实例时间获取UUID字串序列
	 * @description 不同JVM例程的随机值不同,同一个JVM例程随机值相同
	 * @return UUID字串序列
	 */
	public static final String getRandomJvmTimeUUID(String... prexfixs) {
		EIGHT_BUFFER.clear();
		EIGHT_BUFFER.putLong(System.nanoTime());
		EIGHT_BUFFER.flip();
		
		byte[] b=new byte[8];
		EIGHT_BUFFER.get(b);
		EIGHT_BUFFER.clear();
		
		String prefix=(null==prexfixs || 0==prexfixs.length)?"":prexfixs[0];
		prefix=(null==prefix || (prefix=prefix.trim()).isEmpty())?"":prefix;
		
		return prefix+UUID.nameUUIDFromBytes(b).toString().replace("-", "");
	}
	
	/**
	 * 根据操作系统时间获取UUID字串序列
	 * @return UUID字串序列
	 */
	public static final synchronized String getCurrentTimeUUID(String... prexfixs) {
		EIGHT_BUFFER.clear();
		EIGHT_BUFFER.putLong(System.currentTimeMillis());
		EIGHT_BUFFER.flip();
		
		byte[] b=new byte[8];
		EIGHT_BUFFER.get(b);
		EIGHT_BUFFER.clear();
		
		String prefix=(null==prexfixs || 0==prexfixs.length)?"":prexfixs[0];
		prefix=(null==prefix || (prefix=prefix.trim()).isEmpty())?"":prefix;
		String uuid=prefix+UUID.nameUUIDFromBytes(b).toString().replace("-", "");
		
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return uuid;
	}
	
	/**
	 * 判断给定的基类型superType是否可以兼容到指定的子类型childType
	 * @param superType 基类型
	 * @param childType 子类型
	 * @return 是否兼容
	 */
	public static boolean compatible(Class<?> superType,Class<?> childType){
		if(superType==childType) return true;
		if(null==superType && null!=childType) return false;
		if(null!=superType && null==childType) return false;
		if(superType.isAssignableFrom(childType)) return true;
		try{
			if(superType.isPrimitive() && superType==childType.getField("TYPE").get(null)) return true;
			if(childType.isPrimitive() && childType==superType.getField("TYPE").get(null)) return true;
			return false;
		}catch(Exception e){
			return false;
		}
	}
	
	/**
	 * 判断给定的基类型superTypes数组是否可以兼容到指定的子类型数组childTypes
	 * @param superTypes 基类型数组
	 * @param childTypes 子类型数组
	 * @return 数组类型是否兼容
	 */
	public static boolean compatible(Class<?>[] superTypes,Class<?>[] childTypes){
		if(superTypes==childTypes) return true;
		if(null==superTypes && null!=childTypes) return false;
		if(null!=superTypes && null==childTypes) return false;
		if(superTypes.length!=childTypes.length) return false;
		try{
			for(int i=0;i<superTypes.length;i++){
				if(superTypes[i]==childTypes[i] || superTypes[i].isAssignableFrom(childTypes[i])) continue;
				if(superTypes[i].isPrimitive() && superTypes[i]==childTypes[i].getField("TYPE").get(null)) continue;
				if(childTypes[i].isPrimitive() && childTypes[i]==superTypes[i].getField("TYPE").get(null)) continue;
				return false;
			}
			return true;
		}catch(Exception e){
			return false;
		}
	}
	
	/**
	 * 将实体对象映射为字典对象
	 * @param mappedObject 实体对象
	 * @param excludeBooleanFieldss 不需要做转换的boolean类型字段集
	 * @return 字典对象
	 */
	public static final HashMap<String,Object> beanToMap(Object mappedObject,Set<String>... excludeBooleanFieldss) {
		if(null==mappedObject) return null;
		Set<String> excludeBooleanFields=(null==excludeBooleanFieldss || 0==excludeBooleanFieldss.length)?null:excludeBooleanFieldss[0];
		
		Class<?> mappedClass=mappedObject.getClass();
		HashMap<String,Object> retMap=new HashMap<String,Object>();
		ReflectionUtils.doWithFields(mappedClass, new FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				Object fieldValue=field.get(mappedObject);
				if(null==fieldValue) return;
				
				Column column=field.getAnnotation(Column.class);
				String fieldName=field.getName();
				String mappedKey=null;
				if(null==column) {
					mappedKey=fieldName;
				}else {
					mappedKey=column.name();
					if(null==mappedKey || (mappedKey=mappedKey.trim()).isEmpty()) mappedKey=fieldName;
				}
				
				Class<?> fieldType=field.getType();
				if((boolean.class!=fieldType&&Boolean.class!=fieldType) || (null!=excludeBooleanFields&&excludeBooleanFields.contains(fieldName))) {
					retMap.put(mappedKey, fieldValue);
				}else {
					retMap.put(mappedKey, ((Boolean)fieldValue).booleanValue()?"Y":"N");
				}
			}
		}, new FieldFilter() {
			public boolean matches(Field field) {
				ReflectionUtils.makeAccessible(field);
				return true;
			}
		});
		
		return retMap;
	}
	
	/**
	 * 将字典对象映射为实体对象
	 * @param <T> 实体泛型
	 * @param map 字典对象
	 * @param mappedClass 实体类型
	 * @param excludeBooleanFieldss 不需要做转换的boolean类型字段集
	 * @return 实体对象
	 */
	public static final <T> T mapToBean(Map<String,Object> map,Class<T> mappedClass,Set<String>... excludeBooleanFieldss) {
		if(null==map || map.isEmpty() || null==mappedClass) return null;
		Set<String> excludeBooleanFields=(null==excludeBooleanFieldss || 0==excludeBooleanFieldss.length)?null:excludeBooleanFieldss[0];
		
		T t=null;
		try {
			t=mappedClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		BeanWrapperImpl beanWrapper=new BeanWrapperImpl();
		beanWrapper.setBeanInstance(t);
		beanWrapper.setConversionService(DefaultConversionService.getSharedInstance());
		
		ReflectionUtils.doWithFields(mappedClass, new FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				Column column=field.getAnnotation(Column.class);
				String fieldName=field.getName();
				Object fieldValue=null;
				
				if(null==column) {
					fieldValue=map.get(fieldName);
				}else {
					String columnName=column.name();
					if(null==columnName || (columnName=columnName.trim()).isEmpty()) columnName=fieldName;
					fieldValue=map.get(columnName);
				}
				
				if(null==fieldValue) return;
				
				Class<?> fieldType=field.getType();
				if((boolean.class!=fieldType&&Boolean.class!=fieldType) || (null!=excludeBooleanFields&&excludeBooleanFields.contains(fieldName))) {
					beanWrapper.setPropertyValue(fieldName, fieldValue);
				}else {
					if(fieldValue instanceof Boolean) {
						beanWrapper.setPropertyValue(fieldName, ((Boolean)fieldValue).booleanValue());
					}else if(fieldValue instanceof CharSequence) {
						beanWrapper.setPropertyValue(fieldName, RecordObjectMapper.TRUE_VALUES.contains(fieldValue.toString())?true:false);
					}else{
						beanWrapper.setPropertyValue(fieldName, (1==((Number)fieldValue).intValue())?true:false);
					}
				}
			}
		}, new FieldFilter() {
			public boolean matches(Field field) {
				ReflectionUtils.makeAccessible(field);
				return true;
			}
		});
		
		return t;
	}
	
	/**
	 * 数据库是否存在指定的数据表
	 * @param mappedClass 映射实体类
	 * @return 是否存在
	 */
	public static final boolean hasTable(Class<?> mappedClass) {
		Table table=mappedClass.getAnnotation(Table.class);
		
		String tabName=table.name();
		if(null==tabName || (tabName=tabName.trim()).isEmpty()) {
			throw new RuntimeException("Unknown Table Name For: "+mappedClass.getName());
		}
		
		return hasTable(tabName,true);
	}
	
	/**
	 * 数据库是否存在指定的数据表
	 * @param databases 数据库(默认来自于JDBC链接)
	 * @return 是否存在
	 */
	public static final boolean hasTable(String tabName,boolean ignoreCase,String... databases) {
		if(null==tabName || (tabName=tabName.trim()).isEmpty()) return false;
		final String tableName=tabName;
		
		String database=(null==databases || 0==databases.length)?null:databases[0];
		database=(null==database || (database=database.trim()).isEmpty())?null:database;
		String sqlStat=null==database?"show tables;":new StringBuilder("use ").append(database).append(";show tables;").toString();
		
		return getBean(JdbcTemplate.class).execute(new StatementCallback<Boolean>() {
			public Boolean doInStatement(Statement stmt) throws SQLException, DataAccessException {
				ResultSet res=stmt.executeQuery(sqlStat);
				if(ignoreCase) {
					while(res.next()) if(tableName.equalsIgnoreCase(res.getString(1))) return true;
				}else {
					while(res.next()) if(tableName.equals(res.getString(1))) return true;
				}
				return false;
			}
		}).booleanValue();
	}
	
	/**
	 * 执行通用SQL语句
	 * @param sql SQL语句
	 * @param sqlParams SQL参数列表
	 * @return SQL执行结果
	 */
	public static final Object executeSQL(String sql,Object... sqlParams) {
		if(null==sql || (sql=sql.trim()).isEmpty()) return null;
		String prexfix=BLANK_REGEX.split(sql)[0].toUpperCase(Locale.SIMPLIFIED_CHINESE);
		
		JdbcTemplate jdbcTemplate=getBean(JdbcTemplate.class);
		
		if("SELECT".equals(prexfix)) return jdbcTemplate.query(sql, RecordObjectMapper.newInstance(HashMap.class),sqlParams);
		if(DML_OPTIONS.contains(prexfix)) return jdbcTemplate.update(sql,sqlParams);
		
		jdbcTemplate.execute(sql);
		return null;
	}
	
	/**
	 * 执行通用DML语句
	 * @param dmlSql DML-SQL语句
	 * @param sqlParams SQL参数列表
	 * @return 影响行数
	 */
	public static final int executeDML(String dmlSql,Object... sqlParams) {
		return getBean(JdbcTemplate.class).update(dmlSql,sqlParams);
	}
	
	/**
	 * 批量执行通用DML语句
	 * @param dmlSqls DML-SQL语句列表
	 * @return 影响行数列表
	 */
	public static final int[] executeBatchDML(String... dmlSqls) {
		if(null==dmlSqls || 0==dmlSqls.length) return null;
		return getBean(JdbcTemplate.class).batchUpdate(dmlSqls);
	}
	
	/**
	 * 批量执行通用DML语句
	 * @param dmlSql DML-SQL语句
	 * @param sqlParams SQL参数列表
	 * @return 影响行数列表
	 */
	public static final int[] executeBatchDML(String dmlSql,List<Object[]> batchSqlParams) {
		return getBean(JdbcTemplate.class).batchUpdate(dmlSql, batchSqlParams);
	}
	
	/**
	 * 执行通用DQL语句(无预编译参数的查询操作)
	 * @param dqlSql DQL-SQL语句
	 * @param mappedClass 映射类型
	 * @param primitivesDefaultedForNullValues 是否对NULL值映射到基本类型抛出异常
	 * @return 字典或实体列表
	 */
	public static final <T> List<T> executeDQL(String dqlSql,Class<T> mappedClass,boolean... primitivesDefaultedForNullValues) {
		return executeDQL(dqlSql,mappedClass, null, primitivesDefaultedForNullValues);
	}
	
	/**
	 * 执行通用DQL语句(无预编译参数的查询操作)
	 * @param dqlSql DQL-SQL语句
	 * @param mappedClass 映射类型
	 * @param excludeBooleanFields 不需要做转换的boolean类型字段集
	 * @param primitivesDefaultedForNullValues 是否对NULL值映射到基本类型抛出异常
	 * @return 字典或实体列表
	 */
	public static final <T> List<T> executeDQL(String dqlSql,Class<T> mappedClass,Set<String> excludeBooleanFields,boolean... primitivesDefaultedForNullValues) {
		return executeDQL(dqlSql,null,mappedClass,excludeBooleanFields,primitivesDefaultedForNullValues);
	}
	
	/**
	 * 执行通用DQL语句
	 * @param dqlSql DQL-SQL语句
	 * @param sqlParams SQL语句参数
	 * @param mappedClass 映射类型
	 * @param primitivesDefaultedForNullValues 是否对NULL值映射到基本类型抛出异常
	 * @return 字典或实体列表
	 */
	public static final <T> List<T> executeDQL(String dqlSql,Object[] sqlParams,Class<T> mappedClass,boolean... primitivesDefaultedForNullValues) {
		return executeDQL(dqlSql,sqlParams,mappedClass,null,primitivesDefaultedForNullValues);
	}
	
	/**
	 * 执行通用DQL语句
	 * @param dqlSql DQL-SQL语句
	 * @param sqlParams SQL语句参数
	 * @param mappedClass 映射类型
	 * @param excludeBooleanFields 不需要做转换的boolean类型字段集
	 * @param primitivesDefaultedForNullValues 是否对NULL值映射到基本类型抛出异常
	 * @return 字典或实体列表
	 */
	public static final <T> List<T> executeDQL(String dqlSql,Object[] sqlParams,Class<T> mappedClass,Set<String> excludeBooleanFields,boolean... primitivesDefaultedForNullValues) {
		JdbcTemplate jdbcTemplate=getBean(JdbcTemplate.class);
		if(SIMPLE_CLASSES.contains(mappedClass)) return jdbcTemplate.queryForList(dqlSql, mappedClass, sqlParams);
		
		if(null==sqlParams || 0==sqlParams.length) {
			return jdbcTemplate.query(dqlSql,RecordObjectMapper.newInstance(mappedClass, excludeBooleanFields, primitivesDefaultedForNullValues));
		}else {
			return jdbcTemplate.query(dqlSql,RecordObjectMapper.newInstance(mappedClass, excludeBooleanFields, primitivesDefaultedForNullValues),sqlParams);
		}
	}
	
	/**
	 * 执行Insert语句
	 * @param mappedObject 映射实体对象
	 * @param excludeBooleanFields 无需做转换处理的Boolean类型集
	 * @return 主键对象
	 */
	public static final <T> T executeInsert(Object mappedObject,String... excludeBooleanFields) {
		Object[] jdbcParams=getInsertStatement(mappedObject,excludeBooleanFields);
		getBean(JdbcTemplate.class).update((String)jdbcParams[0], (Object[])jdbcParams[1]);
		return (T)((Object[])jdbcParams[3])[0];
	}
	
	/**
	 * 批量执行Insert语句
	 * @param mappedObjects 映射实体对象列表
	 * @param excludeBooleanFields 无需做转换处理的Boolean类型集
	 * @return 主键对象列表
	 */
	public static final <T> Object[] executeBatchInsert(List<?> mappedObjects,String... excludeBooleanFields) {
		ArrayList<Object[]> batchArgs=new ArrayList<Object[]>();
		HashSet<String> insertSqls=new HashSet<String>();
		ArrayList<Object> pks=new ArrayList<Object>();
		for(Object mappedObject:mappedObjects) {
			Object[] jdbcParams=getInsertStatement(mappedObject,excludeBooleanFields);
			insertSqls.add((String)jdbcParams[0]);
			batchArgs.add((Object[])jdbcParams[1]);
			pks.add(((Object[])jdbcParams[3])[0]);
		}
		
		if(1!=insertSqls.size()) throw new RuntimeException("Check Batch SQL Failure!");
		getBean(JdbcTemplate.class).batchUpdate(insertSqls.iterator().next(), batchArgs);
		return pks.toArray(new Object[pks.size()]);
	}
	
	/**
	 * 根据主键执行Delete语句
	 * @param mappedObject 映射实体对象
	 * @return 影响行数
	 */
	public static final int executeDelete(Object mappedObject) {
		Object[] idWhereParams=getPKWhereStatement(mappedObject);
		Object[] jdbcParams=getDeleteStatement(mappedObject.getClass(),(String)idWhereParams[0],(Object[])idWhereParams[1]);
		return getBean(JdbcTemplate.class).update((String)jdbcParams[0], (Object[])jdbcParams[1]);
	}
	
	/**
	 * 根据主键批量执行Delete语句
	 * @param mappedObjects 映射实体对象列表
	 * @return 影响行数列表
	 */
	public static final int[] executeBatchDelete(List<?> mappedObjects) {
		ArrayList<Object[]> batchArgs=new ArrayList<Object[]>();
		HashSet<String> deleteSqls=new HashSet<String>();
		for(Object mappedObject:mappedObjects) {
			Object[] idWhereParams=getPKWhereStatement(mappedObject);
			Object[] jdbcParams=getDeleteStatement(mappedObject.getClass(),(String)idWhereParams[0],(Object[])idWhereParams[1]);
			deleteSqls.add((String)jdbcParams[0]);
			batchArgs.add((Object[])jdbcParams[1]);
		}
		
		if(1!=deleteSqls.size()) throw new RuntimeException("Check Batch SQL Failure!");
		return getBean(JdbcTemplate.class).batchUpdate(deleteSqls.iterator().next(), batchArgs);
	}
	
	/**
	 * 根据主键执行Delete语句
	 * @param primaryKey 主键值
	 * @param mappedClass 映射实体类型
	 * @return 影响行数
	 * @description
	 * 仅支持单个主键,对于复合主键存在主键值顺序匹配问题,请使用其它重载
	 */
	public static final int executeDel(Object primaryKey,Class<?> mappedClass) {
		ArrayList<Object[]> pkList=getPrimaryKeyInfos(mappedClass);
		if(null==pkList || pkList.isEmpty() || pkList.size()>1) {
			throw new RuntimeException("Not Found Primary Key OR Found Multi-Key By Annotation: @Id For Class: "+mappedClass.getName());
		}
		
		return executeDelete(mappedClass,new StringBuilder("WHERE ").append((String)(pkList.get(0)[0])).append("=?").toString(),new Object[] {primaryKey});
	}
	
	/**
	 * 根据主键列表批量执行Delete语句
	 * @param primaryKeys 主键值列表
	 * @param mappedClass 映射实体类型
	 * @return 影响行数数组
	 * @description
	 * 仅支持单个主键,对于复合主键存在主键值顺序匹配问题,请使用其它重载
	 */
	public static final int[] executeBatchDel(List<Object> primaryKeys,Class<?> mappedClass) {
		ArrayList<Object[]> pkList=getPrimaryKeyInfos(mappedClass);
		if(null==pkList || pkList.isEmpty() || pkList.size()>1) {
			throw new RuntimeException("Not Found Primary Key OR Found Multi-Key By Annotation: @Id For Class: "+mappedClass.getName());
		}
		
		return executeBatchDelete(mappedClass,new StringBuilder("WHERE ").append((String)(pkList.get(0)[0])).append("=?").toString(),primaryKeys.stream().map(pk->new Object[] {pk}).collect(Collectors.toList()));
	}
	
	/**
	 * 执行Delete语句
	 * @param mappedClass 映射实体类型
	 * @param whereSubStatement 自定义的where子句
	 * @param whereParams 自定义的where子句参数列表
	 * @return 影响行数
	 * @description 
	 * 需要确保whereParams中的参数传值顺序, 
	 * 一般不会修改整表,如果需要修改整表记录则:
	 * whereSubStatement参数可传递值: 'where 1=1'
	 * whereParams参数可传递值: new Object[0]
	 */
	public static final int executeDelete(Class<?> mappedClass,String whereSubStatement,Object[] whereParams) {
		Object[] jdbcParams=getDeleteStatement(mappedClass,whereSubStatement,whereParams);
		return getBean(JdbcTemplate.class).update((String)jdbcParams[0], (Object[])jdbcParams[1]);
	}
	
	/**
	 * 批量执行Delete语句
	 * @param mappedClass 映射实体类型
	 * @param whereSubStatement 自定义的where子句
	 * @param whereParames 自定义的where子句参数列表
	 * @return 影响行数数组
	 * @description 
	 * 需要确保whereParams中的参数传值顺序, 
	 * 一般不会修改整表,如果需要修改整表记录则:
	 * whereSubStatement参数可传递值: 'where 1=1'
	 * whereParams参数可传递值: new Object[0]
	 */
	public static final int[] executeBatchDelete(Class<?> mappedClass,String whereSubStatement,List<Object[]> whereParames) {
		ArrayList<Object[]> batchArgs=new ArrayList<Object[]>();
		HashSet<String> deleteSqls=new HashSet<String>();
		for(Object[] whereParams:whereParames) {
			Object[] jdbcParams=getDeleteStatement(mappedClass,whereSubStatement,whereParams);
			deleteSqls.add((String)jdbcParams[0]);
			batchArgs.add((Object[])jdbcParams[1]);
		}
		
		if(1!=deleteSqls.size()) throw new RuntimeException("Check Batch SQL Failure!");
		return getBean(JdbcTemplate.class).batchUpdate(deleteSqls.iterator().next(), batchArgs);
	}
	
	/**
	 * 根据主键执行Update语句
	 * @param mappedObject 映射实体对象
	 * @param excludeBooleanFields 无需做转换处理的Boolean类型集
	 * @return 影响行数
	 */
	public static final int executeUpdate(Object mappedObject,String... excludeBooleanFields) {
		Object[] idWhereParams=getPKWhereStatement(mappedObject);
		return executeUpdate(mappedObject,(String)idWhereParams[0],(Object[])idWhereParams[1]);
	}
	
	/**
	 * 根据主键执行Update语句
	 * @param mappedObject 映射实体对象
	 * @param excludeFields 排除字段集
	 * @param excludeBooleanFields 无需做转换处理的Boolean类型集
	 * @return 影响行数
	 */
	public static final int executeUpdate(Object mappedObject,Set<String> excludeFields,String... excludeBooleanFields) {
		Object[] idWhereParams=getPKWhereStatement(mappedObject);
		return executeUpdate(mappedObject,excludeFields,(String)idWhereParams[0],(Object[])idWhereParams[1]);
	}
	
	/**
	 * 执行Update语句
	 * @param mappedObject 映射实体对象
	 * @param whereSubStatement 自定义的where子句
	 * @param whereParams 自定义的where子句参数列表
	 * @param excludeBooleanFields 无需做转换处理的Boolean类型集
	 * @return 影响行数
	 * @description 
	 * 需要确保whereParams中的参数传值顺序, 
	 * 一般不会修改整表,如果需要修改整表记录则:
	 * whereSubStatement参数可传递值: 'where 1=1'
	 * whereParams参数可传递值: new Object[0]
	 */
	public static final int executeUpdate(Object mappedObject,String whereSubStatement,Object[] whereParams,String... excludeBooleanFields) {
		Object[] jdbcParams=getUpdateStatement(mappedObject,null,whereSubStatement,whereParams,excludeBooleanFields);
		return getBean(JdbcTemplate.class).update((String)jdbcParams[0], (Object[])jdbcParams[1]);
	}
	
	/**
	 * 执行Update语句
	 * @param mappedObject 映射实体对象
	 * @param excludeFields 排除字段集
	 * @param whereSubStatement 自定义的where子句
	 * @param whereParams 自定义的where子句参数列表
	 * @param excludeBooleanFields 无需做转换处理的Boolean类型集
	 * @return 影响行数
	 * @description 
	 * 需要确保whereParams中的参数传值顺序, 
	 * 一般不会修改整表,如果需要修改整表记录则:
	 * whereSubStatement参数可传递值: 'where 1=1'
	 * whereParams参数可传递值: new Object[0]
	 */
	public static final int executeUpdate(Object mappedObject,Set<String> excludeFields,String whereSubStatement,Object[] whereParams,String... excludeBooleanFields) {
		Object[] jdbcParams=getUpdateStatement(mappedObject,excludeFields,whereSubStatement,whereParams,excludeBooleanFields);
		return getBean(JdbcTemplate.class).update((String)jdbcParams[0], (Object[])jdbcParams[1]);
	}
	
	/**
	 * 根据主键判断执行Insert(不存在)或Update(存在)语句
	 * @param mappedObject 映射实体对象
	 * @param excludeBooleanFields 无需做转换处理的Boolean类型集
	 * @description 按@Id注解标识的主键生成Where修改条件
	 * @return 影响行数
	 */
	public static final int executeSaveOrUpdate(Object mappedObject,String... excludeBooleanFields) {
		Object[] idWhereParams=getPKWhereStatement(mappedObject);
		return executeInsertOrUpdate(mappedObject,(String)idWhereParams[0], (Object[])idWhereParams[1]);
	}
	
	/**
	 * 执行Insert(不存在)或Update(存在)语句
	 * @param mappedObject 映射实体对象
	 * @param whereSubStatement 自定义的where子句
	 * @param whereParams 自定义的where子句参数列表
	 * @param excludeBooleanFields 无需做转换处理的Boolean类型集
	 * @return 影响行数
	 * @description 
	 * 需要确保whereParams中的参数传值顺序,
	 * 一般不会修改整表,如果需要修改整表记录则:
	 * whereSubStatement参数可传递值: 'where 1=1'
	 * whereParams参数可传递值: new Object[0]
	 */
	public static final int executeInsertOrUpdate(Object mappedObject,String whereSubStatement,Object[] whereParams,String... excludeBooleanFields) {
		JdbcTemplate jdbcTemplate=getBean(JdbcTemplate.class);
		
		Object[] jdbcParams=getUpdateStatement(mappedObject,null,whereSubStatement,whereParams,excludeBooleanFields);
		int count=jdbcTemplate.update((String)jdbcParams[0], (Object[])jdbcParams[1]);
		
		if(count>0) return count;
		
		jdbcParams=getInsertStatement(mappedObject,excludeBooleanFields);
		return jdbcTemplate.update((String)jdbcParams[0], (Object[])jdbcParams[1]);
	}
	
	/**
	 * 执行Insert(不存在)或Update(存在)语句
	 * @param mappedObject 映射实体对象
	 * @param excludeFields 排除字段集
	 * @param whereSubStatement 自定义的where子句
	 * @param whereParams 自定义的where子句参数列表
	 * @param excludeBooleanFields 无需做转换处理的Boolean类型集
	 * @return 影响行数
	 * @description 
	 * 需要确保whereParams中的参数传值顺序,
	 * 一般不会修改整表,如果需要修改整表记录则:
	 * whereSubStatement参数可传递值: 'where 1=1'
	 * whereParams参数可传递值: new Object[0]
	 */
	public static final int executeInsertOrUpdate(Object mappedObject,Set<String> excludeFields,String whereSubStatement,Object[] whereParams,String... excludeBooleanFields) {
		JdbcTemplate jdbcTemplate=getBean(JdbcTemplate.class);
		
		Object[] jdbcParams=getUpdateStatement(mappedObject,excludeFields,whereSubStatement,whereParams,excludeBooleanFields);
		int count=jdbcTemplate.update((String)jdbcParams[0], (Object[])jdbcParams[1]);
		
		if(count>0) return count;
		
		jdbcParams=getInsertStatement(mappedObject,excludeBooleanFields);
		return jdbcTemplate.update((String)jdbcParams[0], (Object[])jdbcParams[1]);
	}
	
	/**
	 * 根据唯一字段值获取实体对象
	 * @param <T> 实体类型
	 * @param mappedClass 映射类型
	 * @param uniqueFieldName 唯一字段名条件
	 * @param uniqueFieldValue 唯一字段值条件
	 * @param primitivesDefaultedForNullValues 是否对NULL值映射到基本类型抛出异常
	 * @return 实体类型列表
	 */
	public static final <T> T getByUniqueField(Class<T> mappedClass,String uniqueFieldName,Object uniqueFieldValue,boolean... primitivesDefaultedForNullValues) {
		List<T> list=getBySingleField(mappedClass,uniqueFieldName,uniqueFieldValue,primitivesDefaultedForNullValues);
		if(null==list || list.isEmpty()) return null;
		if(list.size()>1) throw new RuntimeException("Found Multi-Record For Unique Field: "+uniqueFieldName);
		return list.get(0);
	}
	
	/**
	 * 根据单个字段名获取实体记录
	 * @param <T> 实体类型
	 * @param mappedClass 映射类型
	 * @param fieldName 条件字段名
	 * @param fieldValue 条件字段值
	 * @param primitivesDefaultedForNullValues 是否对NULL值映射到基本类型抛出异常
	 * @return 实体类型列表
	 */
	public static final <T> List<T> getBySingleField(Class<T> mappedClass,String fieldName,Object fieldValue,boolean... primitivesDefaultedForNullValues) {
		return executeSelect(mappedClass,new StringBuilder("WHERE ").append(fieldName).append("=?").toString(),new Object[] {fieldValue},null,primitivesDefaultedForNullValues);
	}
	
	/**
	 * 通过唯一字段值判断是否存在
	 * @param tabName 表名(可以含库名)
	 * @param fieldName 条件字段名
	 * @param fieldValue 条件字段值
	 * @return 是否存在记录
	 */
	public static final boolean existByUniqueField(String tabName,String fieldName,Object fieldValue) {
		Map dict=findByUniqueFields(tabName,fieldName,fieldValue,fieldName);
		return !(null==dict || dict.isEmpty());
	}
	
	/**
	 * 通过唯一字段值获取单个字段值
	 * @param tabName 表名(可以含库名)
	 * @param fieldName 条件字段名
	 * @param fieldValue 条件字段值
	 * @param uniqueFieldName 选择的字段名
	 * @return 单字段值
	 */
	public static final <T> T findByUniqueField(String tabName,String fieldName,Object fieldValue,String uniqueFieldName) {
		Map dict=findByUniqueFields(tabName,fieldName,fieldValue,uniqueFieldName);
		if(null==dict || dict.isEmpty()) return null;
		return (T)dict.get(uniqueFieldName);
	}
	
	/**
	 * 通过唯一字段值获取字典记录
	 * @param tabName 表名(可以含库名)
	 * @param fieldName 条件字段名
	 * @param fieldValue 条件字段值
	 * @param selectFieldNames 选择的字段名列表
	 * @return 字典类型列表
	 */
	public static final Map findByUniqueFields(String tabName,String fieldName,Object fieldValue,String... selectFieldNames) {
		List<Map> dictList=findBySingleFields(tabName,fieldName,fieldValue,selectFieldNames);
		if(null==dictList || dictList.isEmpty()) return null;
		return dictList.get(0);
	}
	
	/**
	 * 通过单个字段名获取字典记录
	 * @param tabName 表名(可以含库名)
	 * @param fieldName 条件字段名
	 * @param fieldValue 条件字段值
	 * @param selectFieldNames 选择的字段名列表
	 * @return 字典类型列表
	 */
	public static final List<Map> findBySingleFields(String tabName,String fieldName,Object fieldValue,String... selectFieldNames) {
		if(null==selectFieldNames || 0==selectFieldNames.length) return null;
		
		StringBuilder sqlBuilder=new StringBuilder("SELECT ");
		for(int i=0;i<selectFieldNames.length;sqlBuilder.append(selectFieldNames[i++]).append(" "));
		String selectSql=sqlBuilder.append("FROM ").append(tabName).append(" WHERE ").append(fieldName).append("=?").toString();
		
		return getBean(JdbcTemplate.class).query(selectSql,RecordObjectMapper.newInstance(Map.class, null, null),new Object[] {fieldValue});
	}
	
	/**
	 * 执行Select语句(查询所有记录)
	 * @param mappedClass 映射实体类型
	 * @param primitivesDefaultedForNullValues 是否对NULL值映射到基本类型抛出异常
	 * @return 实体或字典列表
	 */
	public static final <T> List<T> executeSelect(Class<T> mappedClass,boolean... primitivesDefaultedForNullValues) {
		return executeSelect(mappedClass,null,primitivesDefaultedForNullValues);
	}
	
	/**
	 * 执行Select语句(条件查询)
	 * @param mappedClass 映射实体类型
	 * @param whereSubStatement 自定义的where子句
	 * @param whereParams 自定义的where子句参数列表
	 * @param primitivesDefaultedForNullValues 是否对NULL值映射到基本类型抛出异常
	 * @return 实体或字典列表
	 * @description 
	 * 需要确保whereParams中的参数传值顺序, 
	 * 一般不会查询整表,如果需要查询整表记录则:
	 * whereSubStatement参数可传递值: 'where 1=1'
	 * whereParams参数可传递值: new Object[0]
	 */
	public static final <T> List<T> executeSelect(Class<T> mappedClass,String whereSubStatement,Object[] whereParams,boolean... primitivesDefaultedForNullValues) {
		return executeSelect(mappedClass,whereSubStatement,whereParams,null,primitivesDefaultedForNullValues);
	}
	
	/**
	 * 根据主键执行Select语句
	 * @param primaryKey 主键值
	 * @param mappedClass 映射实体类型
	 * @param primitivesDefaultedForNullValues 是否对NULL值映射到基本类型抛出异常
	 * @return 实体或字典列表
	 * @description
	 * 仅支持单个主键,对于复合主键的情况请使用executeSelect
	 */
	public static final <T> T executeGet(Object primaryKey,Class<T> mappedClass,boolean... primitivesDefaultedForNullValues) {
		return executeSelect(primaryKey,mappedClass,null,primitivesDefaultedForNullValues);
	}
	
	/**
	 * 根据主键执行Select语句
	 * @param primaryKey 主键值
	 * @param mappedClass 映射实体类型
	 * @param excludeBooleanFields 不需要做转换的boolean类型字段集
	 * @param primitivesDefaultedForNullValues 是否对NULL值映射到基本类型抛出异常
	 * @return 实体或字典列表
	 * @description
	 * 仅支持单个主键,对于复合主键存在主键值顺序匹配问题,请使用其它重载
	 */
	public static final <T> T executeSelect(Object primaryKey,Class<T> mappedClass,Set<String> excludeBooleanFields,boolean... primitivesDefaultedForNullValues) {
		ArrayList<Object[]> pkList=getPrimaryKeyInfos(mappedClass);
		if(null==pkList || pkList.isEmpty() || pkList.size()>1) {
			throw new RuntimeException("Not Found Primary Key OR Found Multi-Key By Annotation: @Id For Class: "+mappedClass.getName());
		}
		
		List<T> list=executeSelect(mappedClass,new StringBuilder("WHERE ").append((String)(pkList.get(0)[0])).append("=?").toString(),new Object[] {primaryKey},excludeBooleanFields,primitivesDefaultedForNullValues);
		if(null==list || list.isEmpty()) return null;
		
		if(list.size()>1) throw new RuntimeException("Found Multi-Record For Primary Key: "+primaryKey);
		return list.get(0);
	}
	
	/**
	 * 执行Select语句(查询所有记录)
	 * @param mappedClass 映射实体类型
	 * @param excludeBooleanFields 不需要做转换的boolean类型字段集
	 * @param primitivesDefaultedForNullValues 是否对NULL值映射到基本类型出异常
	 * @return 实体或字典列表
	 * @description 
	 * 需要确保whereParams中的参数传值顺序, 抛
	 * 一般不会查询整表,如果需要查询整表记录则:
	 * whereSubStatement参数可传递值: 'where 1=1'
	 * whereParams参数可传递值: new Object[0]
	 */
	public static final <T> List<T> executeSelect(Class<T> mappedClass,Set<String> excludeBooleanFields,boolean... primitivesDefaultedForNullValues) {
		Object[] jdbcParams=getSelectStatement(mappedClass);
		return getBean(JdbcTemplate.class).query((String)jdbcParams[0], RecordObjectMapper.newInstance(mappedClass, excludeBooleanFields, primitivesDefaultedForNullValues),(Object[])jdbcParams[1]);
	}
	
	
	/**
	 * 执行Select语句(条件查询)
	 * @param mappedClass 映射实体类型
	 * @param whereSubStatement 自定义的where子句
	 * @param whereParams 自定义的where子句参数列表
	 * @param excludeBooleanFields 不需要做转换的boolean类型字段集
	 * @param primitivesDefaultedForNullValues 是否对NULL值映射到基本类型抛出异常
	 * @return 实体或字典列表
	 * @description 
	 * 需要确保whereParams中的参数传值顺序, 
	 * 一般不会查询整表,如果需要查询整表记录则:
	 * whereSubStatement参数可传递值: 'where 1=1'
	 * whereParams参数可传递值: new Object[0]
	 */
	public static final <T> List<T> executeSelect(Class<T> mappedClass,String whereSubStatement,Object[] whereParams,Set<String> excludeBooleanFields,boolean... primitivesDefaultedForNullValues) {
		Object[] jdbcParams=getSelectStatement(mappedClass,whereSubStatement,whereParams);
		return getBean(JdbcTemplate.class).query((String)jdbcParams[0], RecordObjectMapper.newInstance(mappedClass, excludeBooleanFields, primitivesDefaultedForNullValues),(Object[])jdbcParams[1]);
	}
	
	/**
	 * 提取Insert语句
	 * @param mappedObject 映射实体对象
	 * @param excludeBooleanFields 无需做转换处理的Boolean类型集
	 * @return Insert-SQL
	 */
	public static final Object[] getInsertStatement(Object mappedObject,String... excludeBooleanFields) {
		Class<?> mappedClass=mappedObject.getClass();
		final Set<String> excludeBooleanFieldList=(null==excludeBooleanFields || 0==excludeBooleanFields.length)?null:Sets.newHashSet(excludeBooleanFields).stream()
			.filter(fieldName->null!=fieldName&&!fieldName.trim().isEmpty()).map(fieldName->fieldName.trim()).collect(Collectors.toSet());
		
		Table table=mappedClass.getAnnotation(Table.class);
		String tabName=table.name();
		if(null==tabName || (tabName=tabName.trim()).isEmpty()) {
			throw new RuntimeException("Unknown Table Name For: "+mappedClass.getName());
		}
		
		StringBuilder insertSql=new StringBuilder("INSERT INTO ").append(tabName).append("(");
		
		ArrayList<Object> primaryKeys=new ArrayList<Object>();
		ArrayList<String> columnNameList=new ArrayList<String>();
		ArrayList<Object> fieldValueList=new ArrayList<Object>();
		ArrayList<String> placeHolderList=new ArrayList<String>();
		ReflectionUtils.doWithFields(mappedClass, new FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				Column column=field.getAnnotation(Column.class);
				String fieldName=field.getName();
				String columnName=column.name();
				if(null==columnName || (columnName=columnName.trim()).isEmpty()) columnName=fieldName;
				columnNameList.add(columnName);
				placeHolderList.add("?");
				
				Class<?> fieldType=field.getType();
				Object fieldValue=ReflectionUtils.getField(field, mappedObject);
				if(null!=field.getAnnotation(Id.class)) {
					if(null==fieldValue) ReflectionUtils.setField(field,mappedObject,fieldValue=getCurrentTimeUUID());
					primaryKeys.add(fieldValue);
				}
				
				if(null==fieldValue && null!=field.getAnnotation(Id.class)) ReflectionUtils.setField(field,mappedObject,fieldValue=getCurrentTimeUUID());
				
				if((boolean.class!=fieldType&&Boolean.class!=fieldType) || (null!=excludeBooleanFieldList&&excludeBooleanFieldList.contains(fieldName))) {
					fieldValueList.add(fieldValue);
				}else if(null==fieldValue){
					fieldValueList.add(fieldValue);
				}else {
					fieldValueList.add(((Boolean)fieldValue).booleanValue()?"Y":"N");
				}
			}
		}, new FieldFilter() {
			public boolean matches(Field field) {
				if(null==field.getAnnotation(Column.class)) return false;
				ReflectionUtils.makeAccessible(field);
				return true;
			}
		});
		
		if(columnNameList.isEmpty()) {
			throw new RuntimeException("Not Found Any Field For Insert Statement: "+mappedClass.getName());
		}
		
		if(fieldValueList.size()!=columnNameList.size() || columnNameList.size()!=placeHolderList.size()) {
			throw new RuntimeException("Check Field Size And PlaceHolder Size Is Failure For "+mappedClass.getName());
		}
		
		insertSql.append(Strings.join(",", columnNameList)).append(") VALUES(").append(Strings.join(",", placeHolderList)).append(")");
		
		return new Object[] {insertSql.toString(),fieldValueList.toArray(new Object[fieldValueList.size()]),columnNameList.toArray(new String[columnNameList.size()]),primaryKeys.toArray(new Object[primaryKeys.size()])};
	}
	
	/**
	 * 提取Delete语句
	 * @param mappedClass 映射实体类型
	 * @param whereSubStatement 自定义的where子句
	 * @param whereParams 自定义的where子句参数列表
	 * @return Delete-SQL
	 * @description 
	 * 需要确保whereParams中的参数传值顺序, 
	 * 一般不会修改整表,如果需要修改整表记录则:
	 * whereSubStatement参数可传递值: 'where 1=1'
	 * whereParams参数可传递值: new Object[0]
	 */
	public static final Object[] getDeleteStatement(Class<?> mappedClass,String whereSubStatement,Object[] whereParams) {
		if(null==whereSubStatement || (whereSubStatement=whereSubStatement.trim()).isEmpty()) {
			throw new RuntimeException("Where Clause Not Be Null Or Empty For Delete Statement!");
		}
		
		Table table=mappedClass.getAnnotation(Table.class);
		
		String tabName=table.name();
		if(null==tabName || (tabName=tabName.trim()).isEmpty()) {
			throw new RuntimeException("Unknown Table Name For: "+mappedClass.getName());
		}
		
		String deleteSql=new StringBuilder("DELETE FROM ").append(tabName).append(" ").append(whereSubStatement).toString();
		
		return new Object[] {deleteSql,whereParams};
	}
	
	/**
	 * 提取Update语句
	 * @param mappedObject 映射实体对象
	 * @param whereSubStatement 自定义的where子句
	 * @param whereParams 自定义的where子句参数列表
	 * @param excludeBooleanFields 无需做转换处理的Boolean类型集
	 * @return Update-SQL
	 * @description 
	 * 需要确保whereParams中的参数传值顺序, 
	 * 一般不会修改整表,如果需要修改整表记录则:
	 * whereSubStatement参数可传递值: 'where 1=1'
	 * whereParams参数可传递值: new Object[0]
	 */
	public static final Object[] getUpdateStatement(Object mappedObject,String whereSubStatement,Object[] whereParams,String... excludeBooleanFields) {
		return getUpdateStatement(mappedObject,null,whereSubStatement,whereParams,excludeBooleanFields);
	}
	
	/**
	 * 提取Update语句
	 * @param mappedObject 映射实体对象
	 * @param excludeFields 需要排除修改的字段
	 * @param whereSubStatement 自定义的where子句
	 * @param whereParams 自定义的where子句参数列表
	 * @param excludeBooleanFields 无需做转换处理的Boolean类型集
	 * @return Update-SQL
	 * @description 
	 * 需要确保whereParams中的参数传值顺序, 
	 * 一般不会修改整表,如果需要修改整表记录则:
	 * whereSubStatement参数可传递值: 'where 1=1'
	 * whereParams参数可传递值: new Object[0]
	 */
	public static final Object[] getUpdateStatement(Object mappedObject,Set<String> excludeFields,String whereSubStatement,Object[] whereParams,String... excludeBooleanFields) {
		if(null==whereSubStatement || (whereSubStatement=whereSubStatement.trim()).isEmpty()) {
			throw new RuntimeException("Where Clause Not Be Null Or Empty For Update Statement!");
		}
		
		Class<?> mappedClass=mappedObject.getClass();
		final Set<String> excludeBooleanFieldList=(null==excludeBooleanFields || 0==excludeBooleanFields.length)?null:Sets.newHashSet(excludeBooleanFields).stream()
			.filter(fieldName->null!=fieldName&&!fieldName.trim().isEmpty()).map(fieldName->fieldName.trim()).collect(Collectors.toSet());
		
		Table table=mappedClass.getAnnotation(Table.class);
		String tabName=table.name();
		if(null==tabName || (tabName=tabName.trim()).isEmpty()) {
			throw new RuntimeException("Unknown Table Name For: "+mappedClass.getName());
		}
		
		StringBuilder updateSql=new StringBuilder("UPDATE ").append(tabName).append(" SET ");
		
		Set<String> excludeFieldSet=null==excludeFields?new HashSet<String>():excludeFields;
		ArrayList<String> columnNamePlaceholderList=new ArrayList<String>();
		ArrayList<Object> fieldValueList=new ArrayList<Object>();
		ReflectionUtils.doWithFields(mappedClass, new FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				Column column=field.getAnnotation(Column.class);
				String fieldName=field.getName();
				String columnName=column.name();
				if(null==columnName || (columnName=columnName.trim()).isEmpty()) columnName=field.getName();
				columnNamePlaceholderList.add(columnName+"=?");
				
				Class<?> fieldType=field.getType();
				Object fieldValue=ReflectionUtils.getField(field, mappedObject);
				if((boolean.class!=fieldType&&Boolean.class!=fieldType) || (null!=excludeBooleanFieldList&&excludeBooleanFieldList.contains(fieldName))) {
					fieldValueList.add(fieldValue);
				}else if(null==fieldValue){
					fieldValueList.add(fieldValue);
				}else {
					fieldValueList.add(((Boolean)fieldValue).booleanValue()?"Y":"N");
				}
			}
		}, new FieldFilter() {
			public boolean matches(Field field) {
				if(excludeFieldSet.contains(field.getName())) return false;
				if(null==field.getAnnotation(Column.class)) return false;
				ReflectionUtils.makeAccessible(field);
				return true;
			}
		});
		
		if(columnNamePlaceholderList.isEmpty()) {
			throw new RuntimeException("Not Found Any Field For Update Statement: "+mappedClass.getName());
		}
		
		if(fieldValueList.size()!=columnNamePlaceholderList.size()) {
			throw new RuntimeException("Check Field Size And ColumnNamePlaceHolder Size Is Failure For "+mappedClass.getName());
		}
		
		updateSql.append(Strings.join(",", columnNamePlaceholderList)).append(" ").append(whereSubStatement);
		Object[] setParams=fieldValueList.toArray(new Object[fieldValueList.size()]);
		Object[] allParams=new Object[setParams.length+whereParams.length];
		System.arraycopy(setParams, 0, allParams, 0, setParams.length);
		System.arraycopy(whereParams, 0, allParams, setParams.length, whereParams.length);
		
		return new Object[] {updateSql.toString(),allParams};
	}
	
	/**
	 * 提取Select语句
	 * @param mappedClass 映射实体类型
	 * @param whereSubStatement 自定义的where子句
	 * @param whereParams 自定义的where子句参数列表
	 * @return Select-SQL
	 * @description 
	 * 需要确保whereParams中的参数传值顺序, 
	 * 一般不会查询整表,如果需要查询整表记录则:
	 * whereSubStatement参数可传递值: 'where 1=1'
	 * whereParams参数可传递值: new Object[0]
	 */
	public static final Object[] getSelectStatement(Class<?> mappedClass,String whereSubStatement,Object[] whereParams) {
		if(null==whereSubStatement || (whereSubStatement=whereSubStatement.trim()).isEmpty()) {
			throw new RuntimeException("Where Clause Not Be Null Or Empty For Select Statement!");
		}
		
		Table table=mappedClass.getAnnotation(Table.class);
		
		String tabName=table.name();
		if(null==tabName || (tabName=tabName.trim()).isEmpty()) {
			throw new RuntimeException("Unknown Table Name For: "+mappedClass.getName());
		}
		
		String deleteSql=new StringBuilder("SELECT * FROM ").append(tabName).append(" ").append(whereSubStatement).toString();
		
		return new Object[] {deleteSql,whereParams};
	}
	
	/**
	 * 提取Select语句
	 * @param mappedClass 映射实体类型
	 * @return Select-SQL
	 */
	public static final Object[] getSelectStatement(Class<?> mappedClass) {
		Table table=mappedClass.getAnnotation(Table.class);
		
		String tabName=table.name();
		if(null==tabName || (tabName=tabName.trim()).isEmpty()) {
			throw new RuntimeException("Unknown Table Name For: "+mappedClass.getName());
		}
		
		String deleteSql=new StringBuilder("SELECT * FROM ").append(tabName).toString();
		
		return new Object[] {deleteSql,new Object[0]};
	}
	
	/**
	 * 提取基于主键的条件语句
	 * @param mappedObject 映射实体对象
	 * @return Select-SQL
	 * @description 
	 * 基于注解@Id注解标识获取条件查询语句
	 */
	public static final Object[] getPKWhereStatement(Object mappedObject) {
		Class<?> entityClass=mappedObject.getClass();
		ArrayList<Object[]> pkList=getPrimaryKeyInfos(entityClass);
		if(pkList.isEmpty()) throw new RuntimeException("Not Found Primary Key By Annotation: @Id For Class: "+entityClass.getName());
		
		StringBuilder whereBuilder=new StringBuilder("WHERE 1=1 ");
		ArrayList<String> columnNameList=new ArrayList<String>();
		ArrayList<String> propertyNameList=new ArrayList<String>();
		ArrayList<Object> paramList=new ArrayList<Object>();
		for(Object[] pk:pkList) {
			columnNameList.add((String)pk[0]);
			propertyNameList.add((String)pk[1]);
			paramList.add(ReflectionUtils.getField((Field)pk[2], mappedObject));
			whereBuilder.append("AND ").append((String)pk[0]).append("=").append("? ");
		}
		
		return new Object[] {whereBuilder.toString(),paramList.toArray(new Object[paramList.size()]),columnNameList.toArray(new String[columnNameList.size()]),propertyNameList.toArray(new String[propertyNameList.size()])};
	}
	
	/**
	 * 获取映射主键信息
	 * @param mappedClass 映射实体类
	 * @return 主键数组表(数据库列名、实体属性名和属性对象)
	 */
	public static final ArrayList<Object[]> getPrimaryKeyInfos(Class<?> mappedClass) {
		ArrayList<Object[]> primaryKeyList=new ArrayList<Object[]>();
		ReflectionUtils.doWithFields(mappedClass, new FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				Column column=field.getAnnotation(Column.class);
				Object[] pks=new Object[3];
				primaryKeyList.add(pks);
				pks[1]=field.getName();
				pks[0]=column.name();
				pks[2]=field;
			}
		}, new FieldFilter() {
			public boolean matches(Field field) {
				if(null==field.getAnnotation(Column.class) || null==field.getAnnotation(Id.class)) return false;
				ReflectionUtils.makeAccessible(field);
				return true;
			}
		});
		
		return primaryKeyList;
	}
}
