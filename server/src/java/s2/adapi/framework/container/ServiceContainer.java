package s2.adapi.framework.container;

import java.util.Map;

/**
 * 등록된 서비스 정의 목록으로부터 서비스 객체를 생성하고 요청된 서비스 객체를 반환해주는 서비스 컨테이너 구현을 위한
 * 인터페이스이다.
 * @author 김형도
 *
 */
public interface ServiceContainer {
    
    /**
     * 주어진 서비스 명에 해당되는 서비스 객체를 반환한다. 서비스 명이 서비스 정의 파일에 없다면 <code>ServiceContainerException</code>이 발생된다.
     * @param svcName 얻고자하는 서비스 명
     * @return 해당 서비스 객체
     * @throws ServiceContainerException 서비스가 정의되지 않았거나, 서비스 객체 생성 시 오류가 발생했을 경우
     */
    public Object getService(String svcName) throws ServiceContainerException;

    /**
     * 주어진 타입명에 해당되는 서비스 객체  목록을 반환한다. 
     * 하위클래스는 여부는 고려하지 않고 타입명이 정확히 일치하는 경우만 찾아서 반환한다.
     * 타입 명 비교 시에 서비스의 interface와 class 설정 값을 모두 확인한다.  
     * @param typeName 얻고자하는 서비스의 클래스 명(전체 패키지)
     * @return 해당 서비스 객체를 담고 있는 Map 객체
     * @throws ServiceContainerException 서비스 객체 생성 시 오류가 발생했을 경우
     */
    public Map<String,Object> getServicesOfType(String typeName) throws ServiceContainerException;
    
    /**
     * 서비스 컨테이너에 등록된 모든 서비스 명을 배열로 리턴한다.
     * @return String[] 서비스 명 배열
     */
    public String[] getAllServiceNames();
    
    /**
     * 서비스 컨테이너에 등록된 서비스 명 중에서 패턴형태의 명칭들을 배열로 반환한다.
     * @return
     */
    public String[] getPatternServiceNames();
    
    /**
     * 주어진 서비스 명이 등록되어 있는지 여부를 반환한다.
     * @param svcName
     * @return
     */
    public boolean containsService(String svcName);
    
    /**
     * 서비스 객체 생성후 후처리를 진행할 ServicePostProcessor 객체를 등록한다.
     * @param svcpost
     */
    public void addPostProcessor(ServicePostProcessor svcpost);
    
    /**
     * <code>pre-init</code>, <code>singleton</code>, <code>activate</code> 속성이 모두 true 인 서비스들을
     * 미리 생성한다.
     *
     */
    public void populateServices();
    
    /**
     * 서비스 객체들을 다시 생성한다.
     */
    public void reload();
    
    /**
     * 컨테이너를 종료한다. 필요한 리소스를 반환하고 서비스들을 제거하는 작업들을 수행한다.
     *
     */
    public void close();
    
    /**
     * 등록된 서비스들에 대한 정보 목록을 반환한다.
     * @return ServiceInfo[]
     */
    public ServiceInfo[] getServiceInfo();
    
    /**
     * 특정 서비스에 대한 정보를 반환한다.
     * @param svcName
     * @return
     */
    public ServiceInfo getServiceInfo(String svcName);
    
    /**
     * 서비스 클래스를 로딩할 때 사용하는 클래스로더 객체를 반환한다.
     * @return
     */
    public ClassLoader getClassLoader();
}
