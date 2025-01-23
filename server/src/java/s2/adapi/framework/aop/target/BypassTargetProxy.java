package s2.adapi.framework.aop.target;

import java.lang.reflect.Method;

/**
 * 메소드 호출을 그대로 target 객체로 넘긴다. 
 * @author kimhd
 * @since 5.0
 */
public class BypassTargetProxy extends AbstractTargetProxy {

	private Object target = null;
	
	public Object invoke(Method method, Object[] args)
			throws Throwable {
		return method.invoke(target, args);
	}

	public void setTarget(Object target) {
		this.target = target;
	}
}
