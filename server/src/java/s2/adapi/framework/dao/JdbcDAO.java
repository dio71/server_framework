package s2.adapi.framework.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.Constants;
import s2.adapi.framework.config.ConfiguratorException;
import s2.adapi.framework.config.ConfiguratorFactory;
import s2.adapi.framework.config.KeyConfig;
import s2.adapi.framework.context.ContextManager;
import s2.adapi.framework.dao.reader.ColumnReader;
import s2.adapi.framework.dao.sql.DataSource;
import s2.adapi.framework.dao.sql.Transaction;
import s2.adapi.framework.dao.supporter.DbmsSupporter;
import s2.adapi.framework.dao.types.JdbcNull;
import s2.adapi.framework.query.element.ResultMap;
import s2.adapi.framework.query.element.ResultMapItem;
import s2.adapi.framework.vo.ValueObject;

/**
 * <p>
 * JDBC용 DAO 이다.
 * DAO 클래스 선언시 상위 클래스로 사용된다.
 * </p>
 * <p>
 * 다음은 <code>JdbcDAO</code> 사용하여 사용자가 만든 DAO의 한 메소드 예이다.
 * </p>
 * <pre>
 * static final String GET_EMPLOYEE_INFO_SQL =
 *    "  select emp_nm, emp_no, emp_ssn "
 * 	  + "  from tb_zz0_employee         "
 * 	  + " where emp_no       = ?        ";
 *
 * 	public ValueObject getEmployeeBasicInfo(ValueObject pVO)
 * 	{
 * 	  if ( log.isInfoEnabled())
 * 	    log.info("getEmployeeBasicInfo() start...");
 *
 *    ValueObject resultVO  = null;
 *    Connection con = null;
 *    ArrayList params = null;
 *
 *    try	{
 *      con = getConnection();	// Connection 객체를 획득한다.
 *
 *      params.add(pVO.get("empno"));  // SQL 문의 조건 파라메터를 지정한다.
 *
 *      resultVO = executeQuery(con, GET_EMPLOYEE_INFO_SQL, params);
 *
 *    }
 *    finally {
 *      close(con); // Connection을 close()한다.
 *    }
 *
 *    if ( log.isInfoEnabled())
 *      log.info("getEmployeeBasicInfo() end...");
 *
 *    return resultVO;
 *  }
 *
 * </pre>
 *
 * @author kimhd
 * @since 1.0
 */

public class JdbcDAO {
	// Metadata 조회용 상수들
	public static final int META_SCHEMA = 0;
	public static final int META_TABLE = 1;
	public static final int META_VIEW = 2;
	public static final int META_COLUMN = 3;
	
	private static final String MAX_FETCH_LIMIT_KEY = "s2adapi.dao.fetch.limit";
	
	// 디버그 상태에서 별도 SQL 로그 출력을 위한 Logger 객체
	protected static final Logger debugLog = LoggerFactory.getLogger(Constants.DEBUG_LOGGER_NAME);
	
    // 로그를 기록하기 위한 Log 인스턴스 이다.
    // 상속받은 클래스명으로 Log 객체를 생성하기 위하여 생성자에서 초기화한다.
    protected Logger sqlLog = null;

    /**
     * DB Connection 을 얻기 위하여 사용되는 Datasource 객체이다.
     */
    private DataSource datasource = null;
    
    private DbmsSupporter supporter = null;
	
    /**
     * 최대 조회 건수의 제한 값을 설정한다.
     */
    private static int maxFetchLimit = Integer.MAX_VALUE;
    static {
    	try {
    		maxFetchLimit = ConfiguratorFactory.getConfigurator()
            		.getInt(MAX_FETCH_LIMIT_KEY,Integer.MAX_VALUE);
        }
    	catch (ConfiguratorException e) {
        }
    }
    
    /**
     * 사용할 데이터소스 객체를 지정하여 생성한다.
     * @param datasourceName
     * @param ds
     */
    public JdbcDAO(DataSource ds) {
    	sqlLog = LoggerFactory.getLogger(this.getClass());
    	datasource = ds;
    }
    
    /**
     * 디폴트 Constructor, Datasource 명을 지정하지 않았기 때문에 DataSource에서 정한
     * Default 명을 사용한다.
     */
    public JdbcDAO() {
    	this(null);
    }
    
    /**
     * <p>
     * 생성할 때 주어진 Datasource 명을 리턴한다.
     * </p>
     */
    public String getDatasourceName() {
    	if (datasource != null) {
    		return datasource.getDsn();
    	}
    	else {
    		return null;
    	}
    }

    /**
     * 외부에서 생성한 Datasource를 지정한다. 
     * 이때 이전에 이미 Datasource가 설정되어 있다면 다시 지정되지 않는다.
     * @param ds 설정할 Datasource 객체
     */
    public void setDatasource(DataSource ds) {
    	if ( datasource == null ) {
    		datasource = ds;
    	}
    }
    
    /**
     * <p>
     * DB와 Connect하기 위한 Connection 객체를 리턴한다.
     * </p>
     *
     * @throws ApplicationException connection 객체를 생성하다가 에러가 발생하는 경우
     */
    protected Connection getConnection() throws SqlQueryException {
    	Logger log = getLogger();
    	
        Connection con = null;

        try {

        	con = Transaction.current().getConnection(datasource);
        	
        	if( supporter == null ){
        		log.debug("Transaction Isolation="+con.getTransactionIsolation());
            	
        		supporter = DbmsSupporter.getInstance(con);
        		if (supporter == null) {
        			throw new SqlQueryException(SqlQueryException.NOT_SUPPORTED_DBMS);  // 지원하지 않는 DBMS
        		}
        	}
        }
        catch (SQLException ex) {
            // Connection을 생성할 수 없는 경우
            if (log.isErrorEnabled()) {
            	log.error("There was an error while getting a connection [" + datasource.getDsn() + "].", ex);
            }
            throw new SqlQueryException(SqlQueryException.CONNECTION_OPEN_ERROR, ex);
        }

        return con;
    }

    protected DbmsSupporter getSupporter() {
    	return supporter;
    }
    
    /**
     * <p>
     * <code>finally</code> 구문에서 <code>Connection</code> 객체의 연결을 끊어주기 위하여 호출한다.
     * </p>
     *
     * @param con close할 Connection 객체
     * @throws ApplicationException Connection close시 에러가 발생하는 경우
     */
    protected void close(Connection con) throws SqlQueryException {
    	Logger log = getLogger();
    	
        try {
            if (con != null) {
                //con.close();
            	///LocalTransaction.current().closeConnection(con);
            	Transaction.current().closeConnection(con);
            }
        }
        catch (SQLException ex) {
            if (log.isErrorEnabled()) {
            	log.error("There was an error while closing the connection : ", ex);
            }
            throw new SqlQueryException(SqlQueryException.CONNECTION_CLOSE_ERROR, ex);
        }
    }

    /**
     * <p>
     * <code>finally</code> 구문에서 <code>Connection</code> 객체의 연결을 끊어주기 위하여 호출한다.
     * <code>PreparedStatement</code>와 <code>ResultSet</code>을 같이 close하기 위하여 사용된다.
     * </p>
     *
     * @param con close할 Connection 객체
     * @param ps  close할 PreparedStatement 객체, 해당사항이 없을 경우 null을 입력한다.
     * @param rs  close할 ResultSet 객체, 해당사항이 없을 경우 null을 입력한다.
     * @throws ApplicationException close시 에러가 발생하는 경우
     */
    protected void close(Connection con, PreparedStatement ps, ResultSet rs)
            throws SqlQueryException {

    	SqlQueryException exception = null;

        try {
            close(ps, rs);
        }
        catch (SqlQueryException ex) {
            exception = ex;
        }

        try {
            close(con);
        }
        catch (SqlQueryException ex) {
            exception = ex;
        }

        if (exception != null) {
            throw exception;
        }
    }

    /**
     * <p>
     * <code>ResultSet</code>과 <code>PreparedStatement</code> 연결을 끊어주기 위하여 호출한다.
     * </p>
     *
     * @param ps close할 PreparedStatement 객체, 해당사항이 없을 경우 null을 입력한다.
     * @param rs close할 ResultSet 객체, 해당사항이 없을 경우 null을 입력한다.
     * @throws ApplicationException close시 에러가 발생하는 경우
     */
    protected void close(PreparedStatement ps, ResultSet rs) throws SqlQueryException {
    	Logger log = getLogger();
    	
        SQLException sqlException = null;

        try {
            if (rs != null) {
                rs.close();
            }
        }
        catch (SQLException ex) {
            if (log.isErrorEnabled()) {
            	log.error("There was an error while closing the result set : ", ex);
            }
            sqlException = ex;
        }

        try {
            if (ps != null) {
                ps.close();
            }
        }
        catch (SQLException ex) {
            if (log.isErrorEnabled()) {
            	log.error("There was an error while closing the prepared statement : ", ex);
            }

            if (sqlException == null) {
                sqlException = ex;
            }
        }

        if (sqlException != null) {
            throw new SqlQueryException(SqlQueryException.STATEMENT_CLOSE_ERROR, sqlException);
        }
    } 

    /**
     * <p>
     * 여러건의 update, insert, delete query를 한번의 배치로 수행한다.
     * 배치처리될 SQL 문장과 순서대로 매핑될 파라메터 리스트의 배열을
     * 전달하면 한번에 배치처리를 한다.
     * </p>
     * 사용예
     * <pre>
     *    ArrayList[] paramsArray = new ArrayList[pVO.size()];
     *    for(int i = 0; i < pVO.size(); i++) {
     *        paramsArray[i] = new ArrayList();
     *        paramsArray[i].add(pVO.get(i,"emp_no"));
     *    }
     *
     *    rows = executeBatch(con, DELETE_EMPLOYEES_SQL, paramsArray);
     * </pre>
     *
     * @param con    DB에 연결되어 있는 Connection 객체
     * @param sql    수행할 Query 문
     * @param params Query 문에 순서대로 매핑될 파라메터 리스트의 배열
     * @return INSERT, UPDATE, DELETE 실행결과의 row count 또는 0(DDL SQL 문의 경우)
     */
    protected int[] executeBatch(Connection con, String sql, List<?>[] params) throws SqlQueryException {
    	return executeBatch(con, sql, params, null);
    }
    
    protected int[] executeBatch(Connection con, String sql, List<?>[] params, String sqlName)
            throws SqlQueryException {
    	Logger log = getLogger();
    	
    	long stime = System.currentTimeMillis();    // 수행 시간 계산용
        long etime = 0;
        
        PreparedStatement ps = null;
        int[] rowCounts;
        int paramCount = (params == null?0:params.length);

        try {
            ps = con.prepareStatement(sql);

            for (int i = 0; i < paramCount; i++) {
            	supporter.setPreparedStatementParam(ps, params[i]);
            	ps.addBatch();
            	if (log.isInfoEnabled()) {
            		log.info("execute batch param["+i+"]=" + params[i]);
                }
            }
            
            rowCounts = ps.executeBatch();
        }
        catch (SQLException ex) {
            if (log.isErrorEnabled()) {
                etime = System.currentTimeMillis();
                log.error(sql);
                StringBuilder sb = new StringBuilder();
                sb.append(sqlName).append(" execute batch |").append(Long.toString(etime - stime)).append(" msec|error=").append(ex.getMessage());
                log.error(sb.toString());
            }
            throw supporter.handleSqlException(ex);
        }
        catch (Exception ex) {
            if (log.isErrorEnabled()) {
                etime = System.currentTimeMillis();
                log.error(sql);
                StringBuilder sb = new StringBuilder();
                sb.append(sqlName).append(" execute batch |").append(Long.toString(etime - stime)).append(" msec|error=").append(ex.getMessage());
                log.error(sb.toString());
            }
            throw new SqlQueryException(SqlQueryException.UPDATE_QUERY_ERROR, ex);
        }
        finally {
        	close(ps, null);
        }

        etime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append(sqlName).append(" execute batch |").append(Long.toString(etime - stime)).append(" msec|");
        sb.append(rowCounts.length).append(" sqls");
        String sqlResult = sb.toString();

        if (log.isDebugEnabled()) {
        	log.debug(sql);
        }
        log.info(sqlResult);

        return rowCounts;
    }

    /**
     * <p>
     * DB update, insert, delete Query를 수행한다.
     * <code>PreparedStatement</code>에 사용될 SQL 문장과 순서대로 매핑될 파라메터 리스트를
     * 전달하면 내부적으로 해당 SQL 수행한다.
     * </p>
     *
     * @param con   DB에 연결되어 있는 Connection 객체
     * @param sql   수행할 Query 문
     * @param param Query 문에 순서대로 매핑될 파라메터
     * @return INSERT, UPDATE, DELETE 실행결과의 row count 또는 0(DDL SQL 문의 경우)
     */
    protected int executeUpdate(Connection con, String sql, List<Object> param)
            throws SqlQueryException {
    	return executeUpdate(con, sql, param, null);
    }
    
    /**
     * <p>
     * DB update, insert, delete Query를 수행한다.
     * <code>PreparedStatement</code>에 사용될 SQL 문장과 순서대로 매핑될 파라메터 리스트를
     * 전달하면 내부적으로 해당 SQL 수행한다.
     * </p>
     *
     * @param con   DB에 연결되어 있는 Connection 객체
     * @param sql   수행할 Query 문
     * @param param Query 문에 순서대로 매핑될 파라메터
     * @param sqlName Sql 명칭
     * @return INSERT, UPDATE, DELETE 실행 결과의 row count 또는 0(DDL SQL 문의 경우)
     */
    protected int executeUpdate(Connection con, String sql, List<Object> param, String sqlName)
            throws SqlQueryException {
    	Logger log = getLogger();
    	
        PreparedStatement ps = null;
        int rowCount;

        long stime = System.currentTimeMillis();    // 수행 시간 계산용
        long etime = 0;

        try {
        	ps = supporter.prepareStatement(con,sql,param);

            rowCount = ps.executeUpdate();

        }
        catch (SQLException ex) {
            if (log.isErrorEnabled()) {
                etime = System.currentTimeMillis();
                log.error(sql);
                StringBuilder sb = new StringBuilder();
                sb.append(sqlName).append(" execute update |").append(Long.toString(etime - stime)).append(" msec|param=");
                appendForLog(sb,param).append("|error=").append(ex.getMessage());
                log.error(sb.toString());
            }
            throw supporter.handleSqlException(ex);
        }
        catch (Exception ex) {
            if (log.isErrorEnabled()) {
                etime = System.currentTimeMillis();
                log.error(sql);
                StringBuilder sb = new StringBuilder();
                sb.append(sqlName).append(" execute update |").append(Long.toString(etime - stime)).append(" msec|param=");
                appendForLog(sb,param).append("|error=").append(ex.getMessage());
                log.error(sb.toString());
            }
            throw new SqlQueryException(SqlQueryException.UPDATE_QUERY_ERROR, ex);
        }
        finally {
        	close(ps, null);
        }

        etime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append(sqlName).append(" execute update |").append(Long.toString(etime - stime)).append(" msec|param=");
        appendForLog(sb,param).append("|").append(rowCount).append(" records");
        String sqlResult = sb.toString();

        if (log.isDebugEnabled()) {
        	log.debug(sql);
        }
        log.info(sqlResult);

        return rowCount;
    }

    /**
     * <p>
     * DB Insert Query를 수행하며, 입력된 결과를 리턴한다.
     * <code>PreparedStatement</code>에 사용될 SQL 문장과 순서대로 매핑될 파라메터 리스트를
     * 전달하면 내부적으로 해당 SQL 수행한 후,
     * 테이블에 입력되는 컬럼들 중 테이블의 컬럼위치를 기준으로 처음부터 numKeyCols 만큼 지정된 개수의 컬럼들의 
     * 입력된 값을 ValueObject로 담아서 리턴해준다.
     * JDBC 3.0을 지원하는 Driver에서만 사용이 가능하다.
     * </p>
     *
     * @param con        DB에 연결되어 있는 Connection 객체
     * @param sql        수행할 Query 문
     * @param param      Query 문에 순서대로 매핑될 파라메터
     * @param numKeyCols 리턴해 줄 테이블 컬럼의 개수를 지정한다.
     * @return 실행된 SQL이 Insert 문인 경우에 만 입력된 컬럼 값을 담은 ValueObject를 리턴,
     *         그 외에는 비어있는 ValueObject를 리턴
     */
    protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List<Object> param, int numKeyCols)
			throws SqlQueryException {
    	return executeUpdateReturnKeys(con,sql,param,numKeyCols,null,null);
    }

    protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List<Object> param, int numKeyCols, String sqlName)
			throws SqlQueryException {
    	return executeUpdateReturnKeys(con,sql,param,numKeyCols,null,sqlName);
    }
    
    /**
     * executeUpdateReturnKeys() 하는데 ResultMap 객체를 추가적으로 전달한다.
     * @param con        DB에 연결되어 있는 Connection 객체
     * @param sql        수행할 Query 문
     * @param param      Query 문에 순서대로 매핑될 파라메터
     * @param numKeyCols 리턴해 줄 테이블 컬럼의 개수를 지정한다.
     * @param rmap 조회된 결과의 컬럼별로 ColumnReader 를 지정하기 위한 ResultMap 객체
     * @return
     * @throws ApplicationException
     */
    protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List<Object> param, int numKeyCols, ResultMap rmap)
            throws SqlQueryException {
    	return executeUpdateReturnKeys(con, sql, param, numKeyCols, rmap, null);
    }
    
    protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List<Object> param, int numKeyCols, ResultMap rmap, String sqlName)
            throws SqlQueryException {

        // 리턴할 컬럼 인덱스 배열을 생성한다.
        int[] columnIndexes = new int[numKeyCols];
        for (int i = 0; i < numKeyCols; i++) {
            columnIndexes[i] = i + 1;
        }

        return executeUpdateReturnKeys(con, sql, param, columnIndexes, rmap, sqlName);
    }

    /**
     * <p>
     * DB Insert Query를 수행하며, 입력된 결과를 리턴한다.
     * <code>PreparedStatement</code>에 사용될 SQL 문장과 순서대로 매핑될 파라메터 리스트를
     * 전달하면 내부적으로 해당 SQL 수행한 후,
     * columnIndexes[] 로 지정된 위치의 컬럼들의 입력 값을 ValueObject로 담아서 리턴해준다.
     * JDBC 3.0을 지원하는 Driver에서만 사용이 가능하다.
     * </p>
     *
     * @param con           DB에 연결되어 있는 Connection 객체
     * @param sql           수행할 Query 문
     * @param param         Query 문에 순서대로 매핑될 파라메터
     * @param columnIndexes 리턴해 줄 컬럼의 테이블 위치 인덱스를 지정한다.
     * @return 실행된 SQL이 Insert 문인 경우에 만 입력된 컬럼 값을 담은 ValueObject를 리턴,
     *         그 외에는 비어있는 ValueObject를 리턴
     */
    protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List<Object> param, int[] columnIndexes)
    		throws SqlQueryException {
    	return executeUpdateReturnKeys(con,sql,param,columnIndexes,null,null);
    }
    
    protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List<Object> param, int[] columnIndexes, String sqlName)
    		throws SqlQueryException {
    	return executeUpdateReturnKeys(con,sql,param,columnIndexes,null,sqlName);
    }
    
    /**
     * executeUpdateReturnKeys()를 수행하는데 추가적으로 ResultMap 객체를 전달한다.
     * @param con           DB에 연결되어 있는 Connection 객체
     * @param sql           수행할 Query 문
     * @param param         Query 문에 순서대로 매핑될 파라메터
     * @param columnIndexes 리턴해 줄 컬럼의 테이블 위치 인덱스를 지정한다.
     * @param rmap 조회된 결과의 컬럼별로 ColumnReader 를 지정하기 위한 ResultMap 객체
     * @return
     * @throws ApplicationException
     */
    protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List<Object> param, int[] columnIndexes, ResultMap rmap)
            throws SqlQueryException {
    	return executeUpdateReturnKeys(con,sql,param,columnIndexes,rmap,null);
    }
    
    protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List<Object> param, int[] columnIndexes, ResultMap rmap, String sqlName)
            throws SqlQueryException {

        PreparedStatement ps = null;
        ValueObject rsVO = null;

        try {
        	ps = con.prepareStatement(sql, columnIndexes);
        	
        	rsVO = executeUpdateReturnKeysInternal(ps, sql, param, rmap, sqlName);
        }
        catch (SQLException ex) {
        	throw supporter.handleSqlException(ex);
        }

        return rsVO;
    }

    protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List<Object> param, String[] columnNames)
    		throws SqlQueryException {
    	return executeUpdateReturnKeys(con,sql,param,columnNames,null,null);
    }
    
    protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List<Object> param, String[] columnNames, String sqlName)
    		throws SqlQueryException {
    	return executeUpdateReturnKeys(con,sql,param,columnNames,null,sqlName);
    }
    
    /**
     * <p>
     * DB Insert Query를 수행하며, 입력된 결과를 리턴한다.
     * <code>PreparedStatement</code>에 사용될 SQL 문장과 순서대로 매핑될 파라메터 리스트를
     * 전달하면 내부적으로 해당 SQL 수행한 후,
     * columnNames[] 로 지정된 컬럼명에 해당되는 컬럼의 입력 값을 ValueObject로 담아서 리턴해준다.
     * JDBC 3.0을 지원하는 Driver에서만 사용이 가능하다.
     * </p>
     *
     * @param con           DB에 연결되어 있는 Connection 객체
     * @param sql           수행할 Query 문
     * @param param         Query 문에 순서대로 매핑될 파라메터
     * @param columnNames 리턴해 줄 컬럼의 컬럼명을 지정한다.
     * @param rmap 조회된 결과의 컬럼별로 ColumnReader 를 지정하기 위한 ResultMap 객체
     * @return 실행된 SQL이 Insert 문인 경우에 만 입력된 컬럼 값을 담은 ValueObject를 리턴,
     *         그 외에는 비어있는 ValueObject를 리턴
     */
    protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List<Object> param, String[] columnNames, ResultMap rmap)
            throws SqlQueryException {
    	return executeUpdateReturnKeys(con, sql, param, columnNames, rmap, null);
    }
    
    protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List<Object> param, String[] columnNames, 
    		ResultMap rmap, String sqlName) throws SqlQueryException {

        PreparedStatement ps = null;
        ValueObject rsVO = null;

        try {
        	ps = con.prepareStatement(sql, columnNames);
        	
        	rsVO = executeUpdateReturnKeysInternal(ps, sql, param, rmap, sqlName);
        }
        catch (SQLException ex) {
        	throw supporter.handleSqlException(ex);
        }

        return rsVO;
    }

    protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List<Object> param)
    		throws SqlQueryException {
    	return executeUpdateReturnKeys(con,sql,param,(ResultMap)null,null);
    }
    
    protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List<Object> param, String sqlName)
    		throws SqlQueryException {
    	return executeUpdateReturnKeys(con,sql,param,(ResultMap)null,sqlName);
    }
    
    /**
     * <p>
     * DB Insert Query를 수행하며, 입력된 결과를 리턴한다.
     * <code>PreparedStatement</code>에 사용될 SQL 문장과 순서대로 매핑될 파라메터 리스트를
     * 전달하면 내부적으로 해당 SQL 수행한 후,
     * 자동으로 생성되는 컬럼들의 입력 값을 ValueObject로 담아서 리턴해준다.
     * JDBC 3.0을 지원하는 Driver에서만 사용이 가능하다.
     * </p>
     *
     * @param con           DB에 연결되어 있는 Connection 객체
     * @param sql           수행할 Query 문
     * @param param         Query 문에 순서대로 매핑될 파라메터
     * @param rmap
     * @return 실행된 SQL이 Insert 문인 경우에 만 입력된 컬럼 값을 담은 ValueObject를 리턴,
     *         그 외에는 비어있는 ValueObject를 리턴
     */
    protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List<Object> param, ResultMap rmap)
            throws SqlQueryException {
    	return executeUpdateReturnKeys(con, sql, param, rmap, null);
    }
    
    protected ValueObject executeUpdateReturnKeys(Connection con, String sql, List<Object> param, ResultMap rmap, String sqlName)
            throws SqlQueryException {
    	
        PreparedStatement ps = null;
        ValueObject rsVO = null;

        try {
        	ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        	
        	rsVO = executeUpdateReturnKeysInternal(ps, sql, param, rmap, sqlName);
        }
        catch (SQLException ex) {
        	throw supporter.handleSqlException(ex);
        }

        return rsVO;
    }
    
    /**
     * executeUpdateReturnKeys()를 수행하는 내부 메소드이다.
     * @param ps
     * @param sql
     * @param param
     * @param columnIndexes
     * @param rmap
     * @return
     * @throws ApplicationException
     */
    private ValueObject executeUpdateReturnKeysInternal(PreparedStatement ps, String sql, List<Object> param, ResultMap rmap, String sqlName)
            throws SqlQueryException {
    	Logger log = getLogger();
    	
        ResultSet rs = null;

        int rowCount = 0;
        int getCount = 0;

        ValueObject rsVO = null;

        long stime = System.currentTimeMillis();    // 수행 시간 계산용
        long etime = 0;

        try {
            supporter.setPreparedStatementParam(ps, param);
            
            // Insert SQL 실행
            rowCount = ps.executeUpdate();

            // 입력된 컬럼 값을 가져온다.
            rs = ps.getGeneratedKeys();

            rsVO = new ValueObject();
            getCount = fetchResultSet(rs,rsVO,rmap);

        }
        catch (SQLException ex) {
            if (log.isErrorEnabled()) {
                etime = System.currentTimeMillis();
                log.error(sql);
                StringBuilder sb = new StringBuilder();
                sb.append(sqlName).append(" execute update and return keys |").append(Long.toString(etime - stime)).append(" msec|param=");
                appendForLog(sb,param).append("|error=").append(ex.getMessage());
                log.error(sb.toString());
            }
            throw supporter.handleSqlException(ex);
        }
        catch (Exception ex) {
            if (log.isErrorEnabled()) {
                etime = System.currentTimeMillis();
                log.error(sql);
                StringBuilder sb = new StringBuilder();
                sb.append(sqlName).append(" execute update and return keys |").append(Long.toString(etime - stime)).append(" msec|param=");
                appendForLog(sb,param).append("|error=").append(ex.getMessage());
                log.error(sb.toString());
            }
            throw new SqlQueryException(SqlQueryException.UPDATE_QUERY_ERROR, ex);
        }
        finally {
            close(ps, rs);
        }

        etime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append(sqlName).append(" execute update and return keys |").append(Long.toString(etime - stime)).append(" msec|param=");
        appendForLog(sb,param).append("|").append(rowCount).append(" inserted, ").append(getCount).append(" returned");
        String sqlResult = sb.toString();
        
        if (log.isDebugEnabled()) {
        	log.debug(sql);
        }

        log.info(sqlResult);
        
        return rsVO;
    }
    
    /**
     * <p>
     * DB 조회 Query를 수행한다.
     * <code>PreparedStatement</code>에 사용될 SQL 문장과 순서대로 매핑될 파라메터 리스트를
     * 전달하면 내부적으로 해당 질의를 수행하여 그 결과를 <code>ValueObject</code>로 리턴한다.
     * <code>ValueObject</code>에 저장될 때 사용되는 키는 내부적으로 얻어진 <code>ResultSet</code>의
     * <code>getColumnName().toLowerCase()</code>를 호출하여 얻는다.
     * </p>
     *
     * @param con   DB에 연결되어 있는 Connection 객체
     * @param sql   수행할 Query 문
     * @param param Query 문에 순서대로 매핑될 파라메터
     * @return ValueObject 질의결과를 담은 ValueObject
     */
    protected ValueObject executeQuery(Connection con, String sql, List<Object> param)
            throws SqlQueryException {
        return executeQuery(con,sql,param,null,null,null);
    }

    /**
     * <p>
     * DB 조회 Query를 수행한다.
     * <code>PreparedStatement</code>에 사용될 SQL 문장과 순서대로 매핑될 파라메터 리스트를
     * 전달하면 내부적으로 해당 질의를 수행하여 그 결과를 <code>ValueObject</code>로 리턴한다.
     * <code>ValueObject</code>에 저장될 때 사용되는 키는 내부적으로 얻어진 <code>ResultSet</code>의
     * <code>getColumnName().toLowerCase()</code>를 호출하여 얻는다.
     * </p>
     *
     * @param con   DB에 연결되어 있는 Connection 객체
     * @param sql   수행할 Query 문
     * @param param Query 문에 순서대로 매핑될 파라메터
     * @param pageVO   페이지 단위 조회를 위한 추가 정보
     *              <ul>
     *              <li>max_cnt 한 번에 담아올 최대 조회 건수(실제로는 1건 더 읽어온다.)
     *              <li>offset  offset 만큼 조회결과를 건너뛰어 그 다음부터 읽어온다.
     *              <li>tot_cnt 전체 조회 건수가 담겨저 리턴된다.
     *              <li>rmn_cnt VO에 담겨지지 않은 남은 조회 건수가 담겨저 리턴된다.
     *              <li>sort_field DB2의 경우 정렬 기준이 되는 컬럼명을 지정한다.
     *              </ul>
     * @return ValueObject 질의결과를 담은 ValueObject
     */
    protected ValueObject executeQuery(Connection con, String sql, List<Object> param, ValueObject pageVO) throws SqlQueryException {
    	return executeQuery(con,sql,param,pageVO,null,null);
    }
    
    protected ValueObject executeQuery(Connection con, String sql, List<Object> param, ResultMap rmap) throws SqlQueryException {
    	return executeQuery(con,sql,param,null,rmap,null);
    }
    
    /**
     * executeQuery()를 수행하며 이때 ColumnReader 정보를 담은 ResultMap 객체를 전달한다.
     * @param con
     * @param sql
     * @param param
     * @param pageVO
     * @param rmap
     * @return
     * @throws SqlQueryException
     */
	protected ValueObject executeQuery(Connection con, String sql, List<Object> param, ValueObject pageVO, 
    		ResultMap rmap) throws SqlQueryException {
		return executeQuery(con, sql, param, pageVO, rmap, null);
	}
	
    /**
     * executeQuery()를 수행하며 이때 ColumnReader 정보를 담은 ResultMap 객체를 전달한다.
     * @param con
     * @param sql
     * @param param
     * @param pageVO
     * @param rmap
     * @param sqlName
     * @return
     * @throws SqlQueryException
     */
	protected ValueObject executeQuery(Connection con, String sql, List<Object> param, ValueObject pageVO, 
	    		ResultMap rmap, String sqlName) throws SqlQueryException {
		Logger log = getLogger();
	
        int offset = 0;             // 조회 시 처음에 offset 만큼 건너뛰고 읽는다.
        int maxCount = 0;           // 최대 maxCount+1 개 조회한다.
        int totCount = 0;           // 전체 조회 건수를 담는다.
        int rmnCount = 0;           // 실제로 조회 결과로 담고 남은 개수이다.
        int getCount = 0;           // 실제로 조회된 건수이다.
        boolean pageQuery = false;	// 페이지 쿼리 여부이다.
        boolean countQuery = false; // 페이지 쿼리시 전체 건수 조회 여부이다.
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        ValueObject rsVO = null;

        long stime = System.currentTimeMillis();    // 수행 시간 계산용
        long ftime = 0;
        long etime = 0;
        //String sqlID = ""; // SQL 로그 출력용 ID

        // 페이지 조회용 파라메터 추가 및 로그 츨력용이다. 입력된 param의 내용 을 회손하지 않도록 복제본을 만들어 사용한다.
        ArrayList<Object> pageParam = new ArrayList<Object>();
        if (param != null) {
        	for(int i=0;i<param.size();i++) {
        		pageParam.add(param.get(i));
        	}
        }

        // 페이지 쿼리 여부와 페이지 쿼리인 경우에는 페이지 단위 조회 조건을 세팅한다.
        if (pageVO != null) {
        	//pageVO.dumpTable(sqlLog);
            try {
                offset = Integer.parseInt(pageVO.getString("offset"));
            }
            catch (Exception ex) {
                offset = 0;
            }

            try {
                maxCount = Integer.parseInt(pageVO.getString("max_cnt"));
            }
            catch (Exception ex) {
                maxCount = 0;
            }
            //sqlLog.debug(offset+","+maxCount+","+Boolean.valueOf(pageVO.getString("tot_cnt")));
            
            // page query인 경우 디폴트로 count query도 같이 수행하지만
        	// tot_cnt를 false로 설정해서 보내주면 count_query를 수행하지 않는다.
            if ("false".equalsIgnoreCase(pageVO.getString("tot_cnt")) ) {
            	countQuery = false;
            }
            else {
            	countQuery = true;
            }
            
            if (offset > 0 || maxCount > 0) {
            	pageQuery = true;
            }
            else {
            	// page query가 아니라면 count query 역시 하지 않는다.
            	pageQuery = false;
            	countQuery = false;
            }
        }
        else {
        	pageQuery = false;
        	countQuery = false;
        }
        
        // sqlLog.debug("Page query="+pageQuery+", Count query="+countQuery);
        
        try {
        	
            // 전체 조회 건수를 가져온다. 이를 위하여 Count용 query를 만들어 조회를 수행한다.
            if (countQuery) {
                long sstime = System.currentTimeMillis();
                String countSql = "select count(1) from ( " + sql + ") totalcount ";
                
                ps = supporter.prepareStatement(con,countSql,pageParam);
                
                //ps = con.prepareStatement(countSql);
                //supporter.setPreparedStatementParam(ps, pageParam);
                
                rs = ps.executeQuery();
                rs.next();
                totCount = rs.getInt(1);
                close(ps, rs);
                if (log.isDebugEnabled()) {
                    long eetime = System.currentTimeMillis();
                    log.debug("execute count sql takes " + Long.toString(eetime - sstime) + " msec.");
                }
            }

            // offset 이나  max_cnt 값이 지정되었을 경우에는 해당 위치부터 데이터 fetch를 하기 위하여
            // 원본 SQL을 사용하여 page sql을 생성하여 처리한다.     
            if (pageQuery) {
            	// page sql
            	Map<String,Object> args = pageVO.get(0);
                ps = supporter.preparePageStatement(con, sql, pageParam, offset, maxCount, args);
            }
            else {
            	// original sql
            	ps = supporter.prepareStatement(con, sql, pageParam);
            }

            rs = ps.executeQuery();
            
            ftime = System.currentTimeMillis();
            
            rsVO = new ValueObject();
            getCount = fetchResultSet(rs,rsVO,rmap);
            
            // 남은 건수를 구한다.
            if (countQuery) {
            	rmnCount = totCount - (getCount + offset);
                pageVO.set("tot_cnt", String.valueOf(totCount));
                pageVO.set("rmn_cnt", String.valueOf(rmnCount));
                
                if (log.isDebugEnabled()) {
                    log.debug("Total rows = " + totCount + ", last read row = " + (getCount+offset) + ", remainder rows = " + rmnCount);
                }
            }

        }
        catch (SQLException ex) {
            if (log.isErrorEnabled()) {
                etime = System.currentTimeMillis();
                log.error(sql);
                StringBuilder sb = new StringBuilder();
                sb.append(sqlName).append(" execute query |").append(Long.toString(etime - stime)).append(" msec|param=");
                appendForLog(sb,pageParam).append("|error=").append(ex.getMessage());
                log.error(sb.toString());
            }
            throw supporter.handleSqlException(ex);
        }
        catch (SqlQueryException ex) {
            if (log.isErrorEnabled()) {
                etime = System.currentTimeMillis();
                log.error(sql);
                StringBuilder sb = new StringBuilder();
                sb.append(sqlName).append(" execute query |").append(Long.toString(etime - stime)).append(" msec|param=");
                appendForLog(sb,pageParam).append("|error=").append(ex.getMessage());
                log.error(sb.toString());
            }
            throw ex;
        }
        catch (Exception ex) {
            if (log.isErrorEnabled()) {
                etime = System.currentTimeMillis();
                log.error(sql);
                StringBuilder sb = new StringBuilder();
                sb.append(sqlName).append(" execute query |").append(Long.toString(etime - stime)).append(" msec|param=");
                appendForLog(sb,pageParam).append("|error=").append(ex.getMessage());
                log.error(sb.toString());
            }
            throw new SqlQueryException(SqlQueryException.SELECT_QUERY_ERROR, ex);
        }
        finally {
        	close(ps, rs);
        }

        etime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append(sqlName).append(" execute query |").append(Long.toString(ftime-stime)).append("/");
        sb.append(Long.toString(etime - stime)).append(" msec|param=");
        appendForLog(sb,pageParam).append("|").append(getCount).append( "records");
        String sqlResult = sb.toString();

        if (log.isDebugEnabled()) {
        	log.debug(sql);
        }
        log.info(sqlResult);

        return rsVO;
    }

    /**
     * ResultSet으로부터 조회된 결과를 ValueObject에 담아준다.
     * @param rs result set 객체
     * @param intoVO 조회 결과를 담을 ValueObject 객체
     * @param rmap ResultMap
     * @return fetch된 row 수
     * @throws SQLException
     */
    protected int fetchResultSet(ResultSet rs,ValueObject intoVO,ResultMap rmap) throws SQLException, SqlQueryException {
    	ResultSetMetaData rsmd = null;
    	int getCount = 0;
		if ( rs != null ) {
            // ResultSet에서 컬럼 정보을 가져와 컬럼 명(소문자로변환)과 ColumnReader 객체를 목록으로 저장해 놓는다..
            rsmd = rs.getMetaData();
            String colNames[] = new String[rsmd.getColumnCount()];
            ColumnReader readers[] = new ColumnReader[rsmd.getColumnCount()];
            
            for (int k = 0; k < colNames.length; k++) {
                colNames[k] = rsmd.getColumnLabel(k+1).toLowerCase();
                readers[k] = getColumnReader(colNames[k],rsmd.getColumnType(k+1),rsmd.getColumnTypeName(k+1),rmap);
            }

            while (rs.next()) {
                for (int j = 0; j < colNames.length; j++) {
                	int columnIndex = j+1;
                    intoVO.set(getCount, colNames[j], readers[j].read(columnIndex, rs));
                }
                getCount++;
                if (getCount > maxFetchLimit) { // 최대 제한치 초과시
                	throw new SqlQueryException(SqlQueryException.TOO_MANY_ROWS);
                }
            }
		}
		return getCount;
    }
    
    /**
     * ColumnReader 객체를 얻어오는 메소드이다.
     * 주어진 rmap에서 colName에 해당되는 ColumnReader가 있으면 그것을 리턴하고, 없으면
     * type에 해당되는 ColumnReader를 rmap에서 찾아 리턴한다.
     * 그것도 없으면 DbmsSupporter에서 주어진 type에 해당되는 ColumnReader를 찾아서 리턴한다.
     * @param colName
     * @param type
     * @param rmap
     * @return
     */
    protected ColumnReader getColumnReader(String colName, int type, String typeName, ResultMap rmap) {
    	ResultMapItem result = null;
    	ColumnReader reader = null;
        if (rmap != null) {
        	// ResultMap이 지정되었을 경우 컬럼명으로 지정된 ResultItem이 있는지를 우선 확인한다.
        	result = rmap.getItem(colName);
        	if (result == null) {
        		// 컬럼명으로 지정된 ResultItem이 없으므로 타입으로 지정된 ResultItem이 있는지를 다시 확인한다.
        		result = rmap.getItem(type);
        	}
        	if (result != null) {
        		reader = result.getReader();
        	}
        }
        
        if (reader == null) { // ColumnReader가 지정되지 않았다면 supporter를 통하여 설정한다.
        	reader = supporter.getColumnReader(type,typeName);
        }
        
        return reader;
    }
    
    /**
     * Sql 파라메터의 로그 출력용
     * 
     * @param paramList
     * @return
     */
    protected StringBuilder appendForLog(StringBuilder sb, List<Object> paramList) {
    	sb.append("[");
    	for(Object param:paramList) {
    		if (param == null) {
    			sb.append("NULL").append(",");
    		}
    		else if (param instanceof JdbcNull) {
    			sb.append("NULL").append(",");
    		}
    		else if (param instanceof Number) {
    			sb.append(param).append(",");
    		}
    		else if (param instanceof String[]) {
    			// postgresql 에서 Array 파라메터 로그 출력용 : '{"a","b","c"}' 형태임 (2019.09.18 hdkim)
    			sb.append("'{");
    			String[] strs = (String[])param;
    			for(String s:strs) {
    				sb.append("\"").append(s).append("\",");
    			}
    			sb.setLength(sb.length()-1); // 마지막 , 제거
    			sb.append("}',");
    		}
    		else if (param instanceof KeyConfig) {
    			// key 값은 로그에 남기지 않는다.
    			sb.append("'!'").append(",");
    		}
    		else {
    			sb.append("'").append(param).append("'").append(",");
    		}
    	}
    	
    	sb.setLength(sb.length()-1); // remove last "," appended
    	sb.append("]");
    
    	return sb;
    }
   
    /**
     * 디버깅 모드 인지 일반 모드인지 판단하여 해당되는 log 객체를 반환한다.
     * @return
     */
    protected Logger getLogger() {
    	if (ContextManager.isDebugingMode()) {
    		return debugLog;
    	}
    	else {
    		return sqlLog;
    	}
    }
}
