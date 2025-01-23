package s2.adapi.framework.container;

/**
 * 서비스 객체가 생성된후 추가 설정 작업이 필요한 경우 이 인터페이스를 구현하고
 * ServiceContainer의 addPostProcessor()를 사용하여 등록한다.
 * @author kimhd
 *
 */
public interface ServicePostProcessor {

	/**
	 * 서비스 컨테이너에서 서비스 객체가 생성될 때 호출된다.
	 * 서비스 객체의 생성자 또는 Factory를 통한 객체 생성 이후에 호출되며
	 * SetterInjection과 Init-method 이전에 호출된다.
	 * @param svcObject
	 * @param svcName
	 * @param 처리된 서비스 객체
	 */
	public Object postProcess(Object svcObject, String svcName);
}
