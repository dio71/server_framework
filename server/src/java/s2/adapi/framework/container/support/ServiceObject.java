package s2.adapi.framework.container.support;

import java.lang.reflect.Proxy;

/**
 * 생성되었거나 생성 중인 서비스 객체를 그 상태 정보와 함께 담아두기 위한 클래스이다.
 * @author 김형도
 * @since 4.0
 */
public class ServiceObject {
	
	/**
	 * 서비스 객체가 현재 생성되기전 상태이다. 아직 객체의 생성자가 호출되지 않았다.
	 */
	public static final int SERVICE_NOT_CONSTRUCTED = -1;
	
	/**
	 * 서비스 객체가 생성되었으나 추가적인 Setter 메소드가 호출되지 않은 상태이다.
	 */
	public static final int SERVICE_NOT_INITIALIZED = 0;
	
	/**
	 * 서비스 객체가 생성되었고 필요한 Setter 메소드가 호출되었다. 사용가능한 상태이다.
	 */
	public static final int SERVICE_INITIALIZED = 1;
	
	/**
	 * 서비스 객체를 감싸고 있는 Proxy 객체를 저장한다.
	 */
	private Object proxyServiceObject;
	
	/**
	 * 원래의 서비스 객체를 저장한다.
	 */
	private Object orgServiceObject;
	
	/**
	 * 서비스 명이다.
	 */
	private String serviceName;
	
	/**
	 * 현재 서비스 객체의 상태이다.
	 */
	private int state = SERVICE_NOT_CONSTRUCTED;
	private int serviceCount = 0;
	
	/**
	 * 생성자이다. 서비스 객체가 생성되지 않은 상태(<code>SERVICE_NOT_CONSTRUCTED</code>)로 생성한다.
	 *
	 */
	public ServiceObject(String svcName) {
		serviceName = svcName;
		proxyServiceObject = null;
		orgServiceObject = null;
		state = SERVICE_NOT_CONSTRUCTED;
		serviceCount = 0;
	}
	
	/**
	 * 생성자이다. 원 서비스 객체와 Proxy 서비스 객체를 동일하게 설정한다.
	 * 서비스 객체의 초기화가 아직 끝나지 않은 상태(<code>SERVICE_NOT_INITIALIZED</code>)로 생성한다.
	 * @param svcObj
	 */
	public ServiceObject(String svcName, Object svcObj) {
		serviceName = svcName;
		proxyServiceObject = svcObj;
		orgServiceObject = svcObj;
		state = SERVICE_NOT_INITIALIZED;
		serviceCount = 0;
	}
	
	/**
	 * 생성자이다. 원 서비스 객체와 Proxy 서비스 객체를 별도로 설정한다.
	 * @param orgSvcObj
	 * @param proxySvcObj
	 */
	public ServiceObject(String svcName, Object orgSvcObj, Object proxySvcObj) {
		serviceName = svcName;
		proxyServiceObject = proxySvcObj;
		orgServiceObject = orgSvcObj;
		state = SERVICE_NOT_INITIALIZED;
		serviceCount = 0;
	}
	
	/**
	 * 서비스 명을 반환한다.
	 * @return
	 */
	public String getServiceName() {
		return serviceName;
	}
	
	/**
	 * Proxy 서비스 객체를 리턴한다.
	 * @return
	 */
	public Object getService() {
		if ( isInitialized() ) {
			increaseServiceCount();
		}
		return proxyServiceObject;
	}
	
	/**
	 * 원 서비스 객체를 리턴한다.
	 * @return
	 */
	public Object getOriginalService() {
		return orgServiceObject;
	}
	
	/**
	 * 원 서비스 객체가 Proxy로 생성된 객체라면 그 내부의 객체를 반환한고,
	 * 그렇지 않다면 원 서비스 객체를 반환한다.
	 */
	public Object getInnerObject() {
		if (orgServiceObject == null) {
			return null;
		}
		
		Object innerObj = null;
		if (Proxy.isProxyClass(orgServiceObject.getClass())) {
			innerObj = Proxy.getInvocationHandler(orgServiceObject);
		} else {
			innerObj = orgServiceObject;
		}
		return innerObj;
	}
	
	/**
	 * 현재 서비스 객체의 상태를 반환한다.
	 * @return
	 */
	public int getState() {
		return state;
	}
	
	public synchronized void increaseServiceCount() {
		serviceCount++;
	}
	
	public int getServiceCount() {
		return serviceCount;
	}
	
	/**
	 * 서비스 객체의 상태를 생성되었으나 아직 초기화가 끝나지 않은 상태로 설정한다.
	 *
	 */
	public void setContructed() {
		state = SERVICE_NOT_INITIALIZED;
	}
	
	/**
	 * 서비스 객체의 상태를 초기화까지 완료되어 사용가능한 상태로 설정한다.
	 *
	 */
	public void setInitialized() {
		state = SERVICE_INITIALIZED;
	}
	
	/**
	 * 서비스의 생성자가 호출된 이후에 Setter 메소드까지 호출되었는지 여부를 리턴한다.
	 * @return
	 */
	public boolean isInitialized() {
		return (state == SERVICE_INITIALIZED);
	}
	
	/**
	 * 서비스의 생성자가 호출되었는지 여부를 리턴한다. 
	 * 아직 생성자가 호출되기 전이라면 false를 리턴하고, 생성자를 호출한 이후라면 true를 리턴한다.
	 * @return
	 */
	public boolean isConstructed() {
		return (state != SERVICE_NOT_CONSTRUCTED);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("[");
		sb.append(proxyServiceObject);
		sb.append(",");
		sb.append(state);
		sb.append(",");
		sb.append(serviceCount);
		sb.append("]");
		
		return sb.toString();
	}
}
