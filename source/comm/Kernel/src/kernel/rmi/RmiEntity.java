package kernel.rmi;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.alibaba.fastjson.JSON;

import kernel.web.ApplicationUtil;

/**
 * @author JORGE
 * @description RMI实体
 */
public class RmiEntity implements Serializable{
	/**
	 * 调用结果对象
	 */
	private Object result;
	
	/**
	 * 方法参数列表
	 */
	private Object[] params;
	
	/**
	 * 传输对象是否被序列化
	 */
	public boolean serialed;
	
	/**
	 * 方法参数类型
	 */
	public Class<?>[] paramTypes;
	
	/**
	 * 序列化版本ID
	 */
	private static final long serialVersionUID = -5588644542304795465L;
	
	public RmiEntity(Object result,boolean... forces) {
		if(null==result) return;
		boolean force=(null==forces || 0==forces.length)?false:forces[0];
		
		if(force) {
			this.serialed=true;
			this.result=JSON.toJSONString(result);
			return;
		}
		
		if(result instanceof Serializable) {
			this.result=result;
		}else {
			this.serialed=true;
			this.result=JSON.toJSONString(result);
		}
	}
	
	public RmiEntity(Method method,Object[] params,boolean... forces) {
		if(0==method.getParameterCount()) return;
		
		this.paramTypes=method.getParameterTypes();
		boolean force=(null==forces || 0==forces.length)?false:forces[0];
		
		if(force) {
			this.serialed=true;
			this.params=Arrays.stream(params).map(ele->null==ele?null:JSON.toJSONString(ele)).toArray();
			return;
		}
		
		int len=params.length;
		boolean requireSerialize=false;
		for(int i=0;i<len;i++) {
			if(! (params[i] instanceof Serializable)) {
				requireSerialize=true;
				break;
			}
		}
		
		if(requireSerialize) {
			this.serialed=true;
			this.params=Arrays.stream(params).map(ele->null==ele?null:JSON.toJSONString(ele)).toArray();
			return;
		}else {
			this.params=params;
		}
	}

	public Object getResult(Method method) throws Throwable {
		if(null==result) return null;
		Object retObject=!serialed?result:JSON.parseObject((String)result,method.getGenericReturnType());
		if(retObject instanceof Throwable) {
			throw ApplicationUtil.getDeclaredCause((Throwable)retObject, method);
		}else {
			return retObject;
		}
	}

	public Object[] getParams() {
		if(!serialed) return params;
		if(null==paramTypes) return null;
		
		int len=paramTypes.length;
		Object[] args=new Object[len];
		for(int i=0;i<len;args[i]=JSON.parseObject((String)params[i],paramTypes[i++]));
		
		return args;
	}
}
