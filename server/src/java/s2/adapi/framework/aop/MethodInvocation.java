package s2.adapi.framework.aop;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;

/**
 * aopalliance의 MethodInvocation의 구현 클래스이다.
 * @author 김형도
 * @since 4.0
 */
public class MethodInvocation implements
		org.aopalliance.intercept.MethodInvocation {

	private Object target; // method invocation의 대상 객체
	private Method method; // invocation할 메소드 객체
	private Object[] arguments; // invocation할 메소드에 전달될 파라메터들
	private List<MethodInterceptor> interceptorList = null; // interceptor chain
	private int currentInterceptorIdx = 0; // 현재 호출 중인 interceptor의 index
	
	public MethodInvocation(Object target, Method method, Object[] args, List<MethodInterceptor> interceptors) {
		this.target = target;
		this.method = method;
		this.arguments = args;
		this.interceptorList = interceptors;
		this.currentInterceptorIdx = 0;
	}
	
	public Method getMethod() {
		return method;
	}

	public Object[] getArguments() {
		return arguments;
	}

	public Object proceed() throws Throwable {
		Object retObj = null;
		try {
			MethodInterceptor interceptor = null;
			if(interceptorList != null && currentInterceptorIdx < interceptorList.size()) {
				// 다음 interceptor 존재
				interceptor = interceptorList.get(currentInterceptorIdx++);
				retObj = interceptor.invoke(this);
			} else {
				// 더 이상 interceptor가 없으므로 target method 호출
				retObj = method.invoke(target,arguments);
			}
		} catch (IllegalArgumentException e) { // target object does not implement the proxied interface.
			throw e;
		} catch (IllegalAccessException e) {
			throw e;
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
		return retObj;
	}

	public Object getThis() {
		return target;
	}

	public AccessibleObject getStaticPart() {
		return method;
	}

}
