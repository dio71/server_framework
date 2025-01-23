package s2.adapi.framework.context;

import s2.adapi.framework.container.ServiceContainer;

/**
 * 어플리케이션 실행관련 정보를 제공하기 위한 인터페이스이다.
 * @author kimhd
 *
 */
public interface ApplicationContext {

	/**
	 * 어플리케이션의 논리적인 이름을 반환한다.
	 * @return
	 */
	public String getApplicationName();
	
	/**
	 * 어플리케이션에서 사용하는 서비스 컨테이너 객체를 반환한다.
	 * @return
	 */
	public ServiceContainer getServiceContainer();
}
