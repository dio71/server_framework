package s2.adapi.framework.aop;

import java.lang.reflect.InvocationHandler;

import s2.adapi.framework.container.ServiceContainer;

/**
 * Dynamic Proxy 객체를 직접적으로 생성하고자 할때 구현한다.
 * 서비스 설정 파일에서 지정된 class가 TargetProxy를 구현하였다면
 * 지정된 interface를 구현하는 Proxy 객체를 생성하여 이를 서비스 객체로 사용한다.
 * @author kimhd
 * @since 5.0
 */
public interface TargetProxy extends InvocationHandler {

	/**
	 * TargetProxy가 구현하는 interface 클래스 객체를 넘겨준다.
	 * @param inf
	 */
	public void setInterface(Class<?> inf);
	
	/**
	 * TargetProxy를 생성한 ServiceContainer 객체를 넘겨준다.
	 * @param svcContainer
	 */
	public void setServiceContainer(ServiceContainer svcContainer);
}
