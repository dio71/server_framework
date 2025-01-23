package s2.adapi.framework.dao;

import java.sql.SQLException;

import s2.adapi.framework.exception.ApplicationException;
import s2.adapi.framework.resources.Messages;
import s2.adapi.framework.util.StringHelper;

public class SqlQueryException extends ApplicationException {

	private static final long serialVersionUID = 7369275948851102909L;

	public static final String NOT_SUPPORTED_DBMS = "sql.error.90001";
	public static final String CONNECTION_OPEN_ERROR = "sql.error.90002";
	public static final String CONNECTION_CLOSE_ERROR = "sql.error.90003";
	public static final String STATEMENT_CLOSE_ERROR = "sql.error.90004";
	public static final String SELECT_QUERY_ERROR = "sql.error.90005";
	public static final String UPDATE_QUERY_ERROR = "sql.error.90006";
	public static final String TOO_MANY_ROWS = "sql.error.90007";
	public static final String PARAM_TYPE_ERROR = "sql.error.90008";
	public static final String READ_TIMEOUT_ERROR = "sql.error.90009";
	public static final String DUPLICATED_KEY_ERROR = "sql.error.90010";
	public static final String QUERY_FILE_NOTFOUND = "sql.error.90011";
	public static final String QUERY_PARSING_ERROR = "sql.error.90012";
	public static final String NO_QUERY_STATEMENT = "sql.error.90013";
	
	/**
     * SQLException 발생시 생성할 메시지 포멧 스트링 키의 Prefix
     */
    private static final String SQL_FORMAT_STRING_PREFIX = "sql.error.";
    
    /**
     * 메시지 파일에 정의되지 SQL Exception의 경우에 사용하는 키
     */
    private static final String SQL_QUERY_ERROR = "sql.error.99999";

    
	/**
     * 에러나 이벤트의 원인이 SQLException인 경우 그 SQLException을 인자로 갖는 생성자
     * SQLException의 getErrorCode()를 사용하여 메시지 포멧 스트링의 키를 생성하며,
     * 생성된 키를 사용하여 메시지 포맷 스트링을 가져와 저장한다. 
     *
     * @param cause 에러나 이벤트의 원인이 되는 SQLException
     */
    public SqlQueryException(SQLException cause) {
    	super(SQL_FORMAT_STRING_PREFIX.concat(StringHelper.lpad(String.valueOf(cause.getErrorCode()),5,'0')), cause);
    	String key = super.getMessage();

        formatString = Messages.getMessages().getMessage(key);
        if (formatString.equals("???" + key + "???")) {
            // SQL 에러에 대한 메시지 문자열이 정해지지 않은 경우
            formatString = Messages.getMessages().getMessage(SQL_QUERY_ERROR);
        }
    }
    
    public SqlQueryException(String key) {
    	super(key);
    }
    
    public SqlQueryException(String key, Object param) {
    	super(key, param);
    }
    
    public SqlQueryException(String key, Object param1, Object param2) {
    	super(key, param1, param2);
    }
    
    public SqlQueryException(String key, Exception cause) {
    	super(key, cause);
    }
    
    public SqlQueryException(String key, Object param, Exception cause) {
    	super(key, param, cause);
    }
}
