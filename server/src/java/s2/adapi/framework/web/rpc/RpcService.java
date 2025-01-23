package s2.adapi.framework.web.rpc;

public interface RpcService {

    /**
     * 호출할 서비스 명칭과 메소드 명칭 그리고 메소드에 전달할 파라메터들을 배열로 받는다. 실제 서비스를 찾아서 호출해야하며 그 결과를 반환하도록 구현해야한다.
     * 
     * @param serviceName 호출할 서비스 명칭
     * @param methodName 호출할 메소드 명칭
     * @param params 메소드에 전달할 파라메터 들
     * @return 서비스 호출 결과
     * @throws Throwable
     */
    public Object invokeService(String serviceName, String methodName, Object[] params) throws Throwable;
}
