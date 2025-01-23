package s2.adapi.framework.container;

import java.io.File;
import java.io.InputStream;

import s2.adapi.framework.container.support.ServiceRegistry;


/**
 * <p>
 * XML 이나 Property 파일로부터 서비스 설정 내용을 읽어 그 결과를 서비스 레지스트리에 저장하는 클래스들이
 * 구현해야 할 인터페이스이다.
 * </p>
 * <p>
 * s2.adapi.framework.container.ServiceContainer를 구현하는 클래스는 서비스 설정파일로부터 서비스 내용을 읽기위하여
 * 이 Interface를 구현한 클래스를 사용한다.
 * @author 김형도
 *
 */
public interface ServiceReader {
	
    /**
     * 주어진 리소스 배열 내의 모든 리소스들로부터 그 내용을 읽어 주어진 <code>ServiceRegisitry</code>에 저장한다.
     * @param resNames 리소스 명의 배열
     * @param svcRegistry 등록할 서비스 레지스트리
     * @param ignoreException 개별 리소스에서 에러가 날 경우 무시하고 다음 리소스로 계속 진행할 지 여부
     * @throws ServiceContainerException
     */
	void loadServiceDefinition(String[] resNames, ServiceRegistry svcRegistry, boolean ignoreException);
	
	/**
	 * 주어진 리소스 클래스 패스에서 찾은 후 그 내용을 읽어 그 파싱된 결과를 주어진
	 * <code>ServiceRegistry</code>에 저장한다.
	 * @param resName 리소스 명
	 * @param svcRegistry 서비스 설정내용을 저장할 서비스 레지스트리
	 * @throws ServiceContainerException
	 */
	void loadServiceDefinition(String resName, ServiceRegistry svcRegistry)
			throws ServiceContainerException;
	
	/**
	 * <code>InputStream</code>으로부터 서비스 설정 내용을 읽어 그 파싱된 결과를 주어진
	 * <code>ServiceRegistry</code>에 저장한다.
	 * @param is 입력 스트림
	 * @param svcRegistry 서비스 설정내용을 저장할 서비스 레지스트리
	 * @param fromInfo 서비스가 정의된 파일 경로등의  정보
	 * @throws ServiceContainerException
	 */
	void loadServiceDefinition(InputStream is, ServiceRegistry svcRegistry, String fromInfo) 
			throws ServiceContainerException;
	
	/**
	 * 주어진 서비스 설정 파일 배열 내의 모든 파일들을 읽어 그 파싱된 결과를 주어진
	 * <code>ServiceRegistry</code>에 저장한다.
	 * @param file 서비스 설정파일 배열
	 * @param svcRegistry 서비스 설정내용을 저장할 서비스 레지스트리
	 * @param ignoreException 개별 파일에서 에러가 날 경우 무시하고 다음 파일로 계속 진행할 지 여부
	 * @throws ServiceContainerException
	 */
	public void loadServiceDefinition(File[] file, 
            ServiceRegistry svcRegistry, 
            boolean ignoreException) throws ServiceContainerException;
	
	/**
	 * 서비스 설정 파일에서 그 내용을 읽어 그 파싱된 결과를 주어진
	 * <code>ServiceRegistry</code>에 저장한다.
	 * @param file 서비스 설정 파일
	 * @param svcRegistry 서비스 설정내용을 저장할 서비스 레지스트리
	 * @throws ServiceContainerException
	 */
	void loadServiceDefinition(File file, ServiceRegistry svcRegistry) 
			throws ServiceContainerException;
}
