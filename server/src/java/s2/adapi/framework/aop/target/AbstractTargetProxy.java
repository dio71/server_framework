package s2.adapi.framework.aop.target;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import s2.adapi.framework.aop.TargetProxy;
import s2.adapi.framework.container.ServiceContainer;

public abstract class AbstractTargetProxy implements TargetProxy {

	protected Class<?> targetInterface = null;
	private ServiceContainer serviceContainer = null;
	
	public void setInterface(Class<?> inf) {
		targetInterface = inf;
	}

	protected Class<?> getInterface() {
		return targetInterface;
	}
	
	public void setServiceContainer(ServiceContainer svcContainer) {
		serviceContainer = svcContainer;
	}
	
	protected ServiceContainer getServiceContainer() {
		return serviceContainer;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		// assertion check
		if (this != Proxy.getInvocationHandler(proxy)) {
			throw new IllegalArgumentException("Invocation from an invalid proxy object.");
		}
		
		return invoke(method, args);
	}

	/**
	 * 실제 메소드 호출을 처리하는 로직을 구현한다.
	 * @param method
	 * @param args
	 * @return 메소드 호출 반환 값
	 * @throws Throwable
	 */
	abstract public Object invoke(Method method, Object[] args) throws Throwable;
}
