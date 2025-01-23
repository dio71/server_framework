package s2.adapi.framework.aop;

/**
 * Service Container에 정의된 service들의 interceptor 용으로 구현되는 클래스는 반드시 이 Interface를 구현해야 한다.
 * @author 김형도 
 * @since 4.0
 */
public interface InterceptorProxy extends java.lang.reflect.InvocationHandler {
	
	/**
	 * Proxy의 대상이 되는 서비스 객체를 설정한다.
	 * @param target
	 */
	public void setTarget(String svcName, Object target);

	//public void setId(String id);
	
	/**
	 * 구현할 inf를 받아서 Proxy 객체를 생성하여 반환한다.
	 * @param inf the list of interfaces for the proxy class to implement
	 * @param classLoader the class loader to define the proxy class
	 * @return
	 */
	public Object newProxyInstance(Class<?>[] inf, ClassLoader classLoader);
}
