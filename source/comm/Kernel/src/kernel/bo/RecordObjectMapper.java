package kernel.bo;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author JORGE
 * @description 记录映射器
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class RecordObjectMapper<T> implements RowMapper<T>{
	/**
	 * 映射的JAVA类型
	 * 可以是实体或字典
	 */
	private Class<T> mappedClass;
	
	/**
	 * 是否是实体类型
	 */
	private boolean isEntity=true;
	
	/**
	 * 字典入值函数
	 */
	private static Method PUT_METHOD;
	
	/**
	 * 实体类型属性集合
	 */
	private Set<String> mappedProperties;
	
	/**
	 * 是否执行严格校验
	 */
	private boolean checkFullyPopulated=false;
	
	/**
	 * 不需要做转换的boolean类型字段集
	 */
	private Set<String> excludeBooleanFields;
	
	/**
	 * 数据库字段名到实体属性名的映射表
	 */
	private Map<String, PropertyDescriptor> mappedFields;
	
	/**
	 * 如果返回映射到基本类型字段的null值则抛出异常,
	 * 设置为false则打印警告日志并忽略赋值操作(此时基本类型属性将保持固有属性值)
	 */
	private boolean primitivesDefaultedForNullValue=false;
	
	/**
	 * 能转换为逻辑值true的所有字串集合
	 */
	public static final LinkedHashSet<String> TRUE_VALUES = new LinkedHashSet<String>();
	
	/**
	 * SLF日志工具
	 */
	private static final Logger logger=LoggerFactory.getLogger(RecordObjectMapper.class);
	
	/**
	 * 从数据库字段类型到实体属性类型的类型转换服务
	 */
	public ConversionService conversionService = DefaultConversionService.getSharedInstance();
	
	static {
		TRUE_VALUES.add("Y");
		TRUE_VALUES.add("y");
		TRUE_VALUES.add("YES");
		TRUE_VALUES.add("yes");
		TRUE_VALUES.add("TRUE");
		TRUE_VALUES.add("true");
		TRUE_VALUES.add("OK");
		TRUE_VALUES.add("ok");
		TRUE_VALUES.add("1");
		
		try {
			PUT_METHOD=Map.class.getMethod("put", Object.class,Object.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	public RecordObjectMapper(Class<T> mappedClass,Set<String> excludeBooleanFields,boolean primitivesDefaultedForNullValue) {
		this.mappedClass = mappedClass;
		this.excludeBooleanFields=excludeBooleanFields;
		this.primitivesDefaultedForNullValue=primitivesDefaultedForNullValue;
		
		if(Map.class.isAssignableFrom(mappedClass)) {
			this.isEntity=false;
		}else {
			this.isEntity=true;
			this.initialize();
		}
	}
	
	/**
	 * 初始化实体映射类型
	 * @param mappedClass 实体映射类型
	 */
	private void initialize() {
		this.mappedProperties = new HashSet<String>();
		this.mappedFields = new HashMap<String,PropertyDescriptor>();
		for (PropertyDescriptor pd:BeanUtils.getPropertyDescriptors(this.mappedClass)) {
			if (null==pd.getWriteMethod()) continue;
			String fieldName=pd.getName();
			this.mappedProperties.add(fieldName);
			this.mappedFields.put(fieldName, pd);
			this.mappedFields.put(underscoreName(fieldName), pd);
			
			Field field=ReflectionUtils.findField(this.mappedClass, fieldName);
			if(null==field) continue;
			
			Column column=field.getAnnotation(Column.class);
			if(null==column) continue;
			
			String columnName=column.name();
			if(null==columnName || (columnName=columnName.trim()).isEmpty()) continue;
			
			this.mappedFields.put(columnName, pd);
		}
	}
	
	public final Class<T> getMappedClass() {
		return this.mappedClass;
	}
	
	public boolean isCheckFullyPopulated() {
		return this.checkFullyPopulated;
	}
	
	public void setMappedClass(Class<T> mappedClass) {
		this.mappedClass = mappedClass;
		initialize();
	}
	
	public ConversionService getConversionService() {
		return this.conversionService;
	}
	
	public boolean isPrimitivesDefaultedForNullValue() {
		return this.primitivesDefaultedForNullValue;
	}
	
	public void setCheckFullyPopulated(boolean checkFullyPopulated) {
		this.checkFullyPopulated = checkFullyPopulated;
	}
	
	public void setConversionService(@Nullable ConversionService conversionService) {
		this.conversionService = conversionService;
	}
	
	public void setPrimitivesDefaultedForNullValue(boolean primitivesDefaultedForNullValue) {
		this.primitivesDefaultedForNullValue = primitivesDefaultedForNullValue;
	}
	
	public static <T> RecordObjectMapper<T> newInstance(Class<T> mappedClass,boolean... primitivesDefaultedForNullValues) {
		boolean primitivesDefaultedForNullValue=(null==primitivesDefaultedForNullValues || 0==primitivesDefaultedForNullValues.length)?true:primitivesDefaultedForNullValues[0];
		return new RecordObjectMapper<T>(mappedClass,null,primitivesDefaultedForNullValue);
	}
	
	public static <T> RecordObjectMapper<T> newInstance(Class<T> mappedClass,Set<String> excludeBooleanFields,boolean... primitivesDefaultedForNullValues) {
		boolean primitivesDefaultedForNullValue=(null==primitivesDefaultedForNullValues || 0==primitivesDefaultedForNullValues.length)?true:primitivesDefaultedForNullValues[0];
		return new RecordObjectMapper<T>(mappedClass,excludeBooleanFields,primitivesDefaultedForNullValue);
	}
	
	/**
	 * 将JAVA中驼峰命名法字段名转换为下划线命名法的数据库列名
	 * @param name 实体类中的驼峰命名法字段名
	 * @return 下划线命名法的数据库列名
	 */
	private String underscoreName(String name) {
		if (!StringUtils.hasLength(name)) return "";

		StringBuilder result = new StringBuilder();
		result.append(Character.toLowerCase(name.charAt(0)));
		for (int i = 1; i < name.length(); i++) {
			char c = name.charAt(i);
			if (Character.isUpperCase(c)) {
				result.append('_').append(Character.toLowerCase(c));
			}else {
				result.append(c);
			}
		}
		
		return result.toString();
	}
	
	/**
	 * 需要忽略映射的实体类属性名
	 * @param propertyName 实体类属性名
	 */
	public void suppressProperty(String propertyName) {
		if (null==this.mappedFields) return;
		this.mappedFields.remove(propertyName);
		this.mappedFields.remove(underscoreName(propertyName));
		
		Field field=ReflectionUtils.findField(this.mappedClass, propertyName);
		if(null==field) return;
		
		Column column=field.getAnnotation(Column.class);
		if(null==column) return;
		
		String columnName=column.name();
		if(null==columnName || (columnName=columnName.trim()).isEmpty()) return;
		
		this.mappedFields.remove(columnName);
	}
	
	/**
	 * 创建实体类实例
	 * @return 实体类实例
	 * @throws SQLException
	 */
	private T constructMappedInstance() throws SQLException  {
		Assert.state(this.mappedClass != null, "Mapped class was not specified");
		return BeanUtils.instantiateClass(this.mappedClass);
	}
	
	/**
	 * 初始化Bean包装器
	 * @param bw Bean包装器
	 */
	private void initBeanWrapper(BeanWrapper bw) {
		if(null==this.conversionService) return;
		bw.setConversionService(this.conversionService);
	}
	
	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		try {
			if(isEntity) {
				return mapToEntity(rs,rowNum);
			}else {
				return mapToDict(rs,rowNum);
			}
		}catch(Exception e) {
			throw new SQLException(e);
		}
	}
	
	/**
	 * 映射到原始字典类型
	 * @param rs 结果集
	 * @param rowNum 当前行号
	 * @return 字典对象
	 * @throws SQLException
	 */
	private T mapToDict(ResultSet rest, int rowNum) throws Exception {
		T dictInst=null;
		if(mappedClass.isInterface()) {
			dictInst=(T)new HashMap();
		}else {
			dictInst=mappedClass.newInstance();
		}
		
		ResultSetMetaData rsmd = rest.getMetaData();
		int columnCount = rsmd.getColumnCount();
		
		for (int index=1;index<=columnCount;PUT_METHOD.invoke(dictInst,JdbcUtils.lookupColumnName(rsmd, index),JdbcUtils.getResultSetValue(rest,index++)));
		
		return dictInst;
	}
	
	/**
	 * 映射到实体类型
	 * @param rs 结果集
	 * @param rowNum 当前行号
	 * @return 实体对象
	 * @throws SQLException
	 */
	private T mapToEntity(ResultSet rest, int rowNum) throws SQLException {
		BeanWrapperImpl beanWrapper = new BeanWrapperImpl();
		initBeanWrapper(beanWrapper);

		T mappedObject = constructMappedInstance();
		beanWrapper.setBeanInstance(mappedObject);

		ResultSetMetaData rsmd = rest.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Set<String> populatedProperties = checkFullyPopulated?new HashSet<String>():null;

		for (int index = 1; index <= columnCount; index++) {
			String columnName = JdbcUtils.lookupColumnName(rsmd, index);
			PropertyDescriptor pd = (this.mappedFields != null ? this.mappedFields.get(StringUtils.delete(columnName, " ")):null);
			
			if (null==pd) continue;
			if (0==rowNum) logger.debug("Mapping column '"+columnName+"' to property '"+pd.getName()+"' of type '"+ClassUtils.getQualifiedName(pd.getPropertyType())+"'");
			
			String fieldName=pd.getName();
			Class<?> fieldType=pd.getPropertyType();
			try{
				if((boolean.class!=fieldType&&Boolean.class!=fieldType) || (null!=excludeBooleanFields&&excludeBooleanFields.contains(fieldName))) {
					beanWrapper.setPropertyValue(fieldName, JdbcUtils.getResultSetValue(rest,index,fieldType));
				}else {
					Object enableVal=rest.getObject(index);
					if(null==enableVal) {
						if(boolean.class==fieldType) throw new TypeMismatchException(enableVal,fieldType);
						beanWrapper.setPropertyValue(fieldName, enableVal);
					}else if(enableVal instanceof Boolean) {
						beanWrapper.setPropertyValue(fieldName, ((Boolean)enableVal).booleanValue());
					}else if(enableVal instanceof CharSequence) {
						beanWrapper.setPropertyValue(fieldName, TRUE_VALUES.contains(enableVal.toString())?true:false);
					}else{
						beanWrapper.setPropertyValue(fieldName, (1==((Number)enableVal).intValue())?true:false);
					}
				}
				if (populatedProperties != null) populatedProperties.add(fieldName);
			}catch (TypeMismatchException e) {
				if(primitivesDefaultedForNullValue) throw e;
				logger.warn("Intercepted TypeMismatchException for row "+rowNum+" and column '"+columnName + 
						"' with null value when setting property '"+fieldName+"' of type '"+ClassUtils.getQualifiedName(fieldType)+"' on object: "+mappedObject, e);
			}catch (NotWritablePropertyException e) {
				throw new DataRetrievalFailureException("Unable to map column '"+columnName+"' to property '"+fieldName+"'", e);
			}
		}

		if (populatedProperties != null && !populatedProperties.equals(this.mappedProperties)) {
			throw new InvalidDataAccessApiUsageException("Given ResultSet does not contain all fields "+"necessary to populate object of "+this.mappedClass+": "+this.mappedProperties);
		}
		
		return mappedObject;
	}
}
