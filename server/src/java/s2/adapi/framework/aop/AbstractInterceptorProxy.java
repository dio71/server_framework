package s2.adapi.framework.aop;

import java.lang.reflect.Proxy;

import s2.adapi.framework.util.SystemHelper;

/**
 * InterceptorProxy의 공통 기능을 구현한 클래스이다. 
 * Service Container에 정의된 service들의 interceptor 용으로 구현되는 클래스는 
 * 이 클래스를 확장하여 구현하도록 한다.
 * @author 김형도 
 * @since 4.0
 */
public abstract class AbstractInterceptorProxy implements InterceptorProxy {
	/**
	 * Proxy가 처리하는 서비스 객체의 서비스 명칭
	 */
	protected String serviceName;
	
	/**
	 * Proxy가 처리하는 서비스 객체
	 */
	protected Object serviceTarget;
	
	/**
	 * Proxy의 대상이 되는 서비스 객체를 설정한다.
	 * @param target
	 */
	public void setTarget(String svcName, Object target) {
		serviceName = svcName;
		serviceTarget = target;
	}
	
	public String getServiceName() {
		return serviceName;
	}
	
	public Object getServiceTarget() {
		return serviceTarget;
	}
	
	public Object newProxyInstance(Class<?>[] inf, ClassLoader classLoader) {
		if (classLoader == null) {
			if (inf != null && inf.length > 0) {
				classLoader = inf[0].getClassLoader();
			} else {
				classLoader = SystemHelper.getClassLoader();
			}
		}
			
		return Proxy.newProxyInstance(classLoader, inf, this);
	}
}
