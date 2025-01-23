package s2.adapi.framework.aop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;

/**
 * MethodInterceptor를 사용하는 InterceptorProxy의 구현
 * @author 김형도
 * @since 4.0
 */
public class MethodInterceptorProxy extends AbstractInterceptorProxy {

	protected List<MethodInterceptor> interceptorList = new ArrayList<MethodInterceptor>();

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		MethodInvocation invocation = 
				new MethodInvocation(getServiceTarget(), method, args, interceptorList);
		
		return invocation.proceed();
	}

	/**
	 * interceptor로 사용할 MethodInterceptor 객체를 설정한다.
	 * @param interceptor
	 */
	public void setInterceptor(MethodInterceptor interceptor) {
		interceptorList.add(interceptor);
	}

}
