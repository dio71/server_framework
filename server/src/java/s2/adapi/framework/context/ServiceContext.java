package s2.adapi.framework.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import s2.adapi.framework.vo.ValueObject;

/**
 * <p>
 * 현재 수행 중인 서비스 인스턴스와 관련된 각종 정보를 관리한다.
 * 관리되는 정보는 서비스 인스턴스 마다 고유하게 부여되는 ID와 
 * 사용자 정보등을 필요에 따라서 임의로 사용가능한 서비스 롤 정보가 있다.
 * </p>
 * <p>
 * 서비스 인스턴스의 ID는 별도로 생성하여 설정해 주어야 하며, 
 * 서비스 트래이싱을 위하여 로그에 같이 찍어주는 용도 등으로 활용된다.
 * </p>
 * <p>
 * 서비스 롤은 사용자 이름이나 소속부서, 권한 정보등과 같이 필요에 따라서 임의로 정의하여 사용할 수 있다.
 * </p>
 * <p>
 * 서비스 컨텍스트는 EJB Commander 등을 사용하여 원격 호출 시 함께 전달되어야 하며,
 * EJB Commander는 파라메터로 전달된 서비스 컨텍스트 정보를 다시 설정해주어 서비스의 흐름이 이어지는 동안
 * 동일한 서비스 컨텍스트 정보를 유지하도록 구현되어야 한다.
 * </p>
 * @author 김형도
 *
 */
public class ServiceContext implements Serializable {

	private static final long serialVersionUID = 0;
	
    /**
     * 서비스 인스턴스별 고유한 ID
     */
    protected String serviceTrId = null;
    
    /**
     * 현재 처리 트랜잭션이 디버깅 상태인지를 저장한다.
     */
    protected boolean isDebuging = false;
    
    /**
     * 서비스 인스턴스와 관련된 role 정보를 저장한다.
     * role 정보는 임의의 (key, value)로 구성된다.
     */
    protected Map<String,Object> serviceRole = null;
    
    /**
     * 클라이언트에 전달하거나 서비스 간에 전달할 메시지 문자을 설정한다.
     */
    protected String message = null;
    
    /**
     * 처리 결과 코드를 담아둔다.
     */
    protected int returnCode = 0;
    
	/**
	 * 서비스 인스턴스 ID를 저장한다.
	 * @param id
	 */
	public void setServiceTransactionID(String id) {
		serviceTrId = id;
	}
	
	/**
	 * 서비스 인스턴스 ID를 반환한다.
	 * @return
	 */
	public String getServiceTransactionID() {
		return serviceTrId;
	}
	
	public void setDebugingMode(boolean debuging) {
		isDebuging = debuging;
	}
	
	public boolean isDebugingMode() {
		return isDebuging;
	}
	
	/**
	 * key 에 해당되는 서비스 롤 객체를 리턴한다.
	 * @param key
	 * @return
	 */
	public Object getRole(String key) {
		if ( serviceRole != null ) {
			return serviceRole.get(key);
		} else {
			return null;
		}
	}
	
	/**
	 * 모든 서비스 role 값들을 ValueObject에 담아서 반환한다.
	 * 2024.10.23 diokim
	 * @return
	 */
	public ValueObject getRolesAsVO() {
		HashMap<String,Object> cloneMap = new HashMap<String,Object>(serviceRole);

		ValueObject roleVO = new ValueObject();
		roleVO.add(cloneMap);

		return roleVO;
	}

	/**
	 * 서비스 롤을 추가한다.
	 * @param key
	 * @param role
	 */
	public void setRole(String key, Object role) {
		if ( serviceRole == null ) {
			serviceRole = new HashMap<String,Object>();
		}
		
		serviceRole.put(key, role);
	}
	
	/**
	 * Map 객체에 있는 모든 서비스 롤을 추가한다.
	 * @param roleMap
	 */
	public void setRole(Map<? extends String,? extends Object> roleMap) {
		if ( serviceRole == null ) {
			serviceRole = new HashMap<String,Object>();
		}
		
		serviceRole.putAll(roleMap);
	}
	
	/**
	 * 메시지 문자열을 설정한다.
	 * @param msg
	 */
	public void setMessage(String msg) {
		message = msg;
	}
	
	/**
	 * 메시지 문자열을 반환한다.
	 * @return
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * 결과 코드 설정
	 * @param code
	 */
	public void setReturnCode(int code) {
		returnCode = code;
	}
	
	/**
	 * 결과 코드 반환
	 * @return
	 */
	public int getReturnCode() {
		return returnCode;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ServiceTRID:").append(serviceTrId).append(",");
		sb.append("ServiceRole:").append(serviceRole).append(",");
		sb.append("Message:").append(message);
		
		return sb.toString();
	}
}
