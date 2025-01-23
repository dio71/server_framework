package s2.adapi.framework.dao.sql;

/**
 * SQLException 용 에러 메시지들을 정의한다.
 * @author 김형도
 * @since 3.0
 */
public class SqlMessages {
    
    /**
     * <p>
     * 종료된 Connection에 작업을 할 경우 발생하는 SQLException 메시지
     * </p>
     */    
    public static final String CLOSED_CONNECTION_ERROR_MESSAGE = "Closed Connection";
    /**
     * <p>
     * 생성자로 정의된 사용자 아이디와 암호와 다르게 Connection 객체 생성을 요청할 경우에 발생되는 SQLException 메시지
     * </p>
     */
    public static final String CREDENTIAL_ERROR_MESSAGE =
      "User credentials doesn't match the existing ones.";

    /**
     * 지정한 Jdbc driver 클래스가 없는 경우
     */
    public static final String JDBC_DRIVER_NOT_FOUND = "Jdbc driver not found : ";
    
    /**
     * <p>
     * 분산 트랜젝션이 지원되지 않을 경우 발생하는 SQLException 메시지
     * </p>
     */             
    public static final String XA_NOT_SUPPORT_ERROR_MESSAGE = 
      "A distributed transaction is not supported.";
      
    /**
     * <p>
     * 분산 트랜젝션이 Rollback으로 설정되었을 경우 SQLException 메시지
     * </p>
     */             
    public static final String MARKED_ROLLBACK_ERROR_MESSAGE =       
      "The transaction has been marked for rollback only.";
      
    /**
     * <p>
     * 분산 트랜젝션이 상태가 Active 상태가 아닐 경우 SQLException 메시지
     * </p>
     */             
    public static final String NOT_ACTIVE_TRANSACTION_ERROR_MESSAGE = 
      "The transaction is not in an active state.";
      
    /**
     * <p>
     * 분산 트랜젝션 진행 중 XAResource를 Transaction에 enlist 하지 못했을 경우 SQLException 메시지
     * </p>
     */             
    public static final String CANNOT_ENLIST_XARESOURCE_ERROR_MESSAGE = 
      "Cannot enlist the xa-resource object.";
    
    /**
     * <p>
     * JDBC Url 문자열의 형식이 JDBC Driver에서 정의한 형식과 다를 경우
     * </p>
     */             
    public static final String INVALID_JDBC_URL_ERROR_MESSAGE = 
      "Invalid JDBC Url string.";
    
    /**
     * <p>
     * Paging Query 자동 생성이 지원되지 않는 경우
     * </p>
     */             
    public static final String PAGING_QUERY_NOT_SUPPORTED_ERROR_MESSAGE = 
      "Paging query is not supported.";
}