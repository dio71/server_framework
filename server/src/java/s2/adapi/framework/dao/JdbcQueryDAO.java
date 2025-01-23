package s2.adapi.framework.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import s2.adapi.framework.dao.reader.ColumnReader;
import s2.adapi.framework.dao.sql.DataSource;
import s2.adapi.framework.exception.ApplicationException;
import s2.adapi.framework.query.SqlQuery;
import s2.adapi.framework.query.SqlQueryPage;
import s2.adapi.framework.query.element.ResultMap;
import s2.adapi.framework.query.element.SqlParameter;
import s2.adapi.framework.vo.ValueObject;
import s2.adapi.framework.vo.ValueObjectAssembler;

/**
 * SqlQuery 기반의 DAO 클래스이다.
 * 
 * @author 김형도
 * @since 4.0
 */
public class JdbcQueryDAO extends JdbcDAO {
	
	/**
	 * DAO 클래스의 클래스패스상의 경로를 저장해 놓는다.
	 * sql 파일의 상대경로로부터 전체 클래스패스 경로를 만들어 내기 위해서 사용한다.
	 */
	protected String packagePath = "";
	
	public JdbcQueryDAO() {
		super();
		initialize();
	}

	public JdbcQueryDAO(DataSource datasource) {
		super(datasource);
		initialize();
	}

	/**
	 * 조회용 SQL 문을 수행한다. 수행할 SQL문을 담고 있는 SqlQuery 객체와 파라메터를
	 * 담고 있는 ValueObject 객체를 받아서 SQL을 수행한 후 그 결과 데이터를
	 * ValueObject로 리턴한다.
	 * @param query 수행할 SQL문을 담고 있는 SqlQuery 객체
	 * @param paramVO 파라메터를 담고 있는 ValueObject
	 * @return ValueObject 조회 결과를 담은 ValueObject
	 * @throws ApplicationException
	 */
	protected ValueObject executeQuery(SqlQuery query, ValueObject paramVO)
			throws SqlQueryException {
		ValueObject getVO = null;
		Connection con = null;
		String sql = null;
		try {
			con = getConnection();

			List<SqlParameter> sqlParams = new ArrayList<SqlParameter>();
			sql = makeSql(query, paramVO, sqlParams); 
			
			getVO = executeQuery(con, sql, 
					getParameterValues(sqlParams,paramVO), 
					null, query.getResultMap(), query.getSqlName());
		}
		finally {
			close(con);
		}

		return getVO;
	}
	
	/**
	 * 주어진 SQL에 대하여 페이지 단위로 조회를 수행한다. 
	 * 수행할 SQL문을 담고 있는 SqlQuery 객체와 파라메터를
	 * 담고 있는 ValueObject 객체를 받아서 SQL을 수행한 후 그 결과 데이터를
	 * ValueObject로 리턴한다.
	 * @param query 수행할 SQL문을 담고 있는 SqlQuery 객체
	 * @param paramVO 파라메터를 담고 있는 ValueObject
     * @param pageVO   페이지 단위 조회를 위한 추가 정보
     *              <ul>
     *              <li>max_cnt 한 번에 담아올 최대 조회 건수(실제로는 1건 더 읽어온다.)
     *              <li>offset  offset 만큼 조회결과를 건너뛰어 그 다음부터 읽어온다.
     *              <li>tot_cnt 전체 조회 건수가 담겨저 리턴된다.
     *              <li>rmn_cnt VO에 담겨지지 않은 남은 조회 건수가 담겨저 리턴된다.
     *              <li>sort_field DB2의 경우 정렬 기준이 되는 컬럼명을 지정한다.
     *              </ul>
	 * @return ValueObject 조회 결과를 담은 ValueObject
	 * @throws ApplicationException
	 */
	protected ValueObject executeQuery(SqlQuery query, ValueObject paramVO, ValueObject pageVO)
			throws SqlQueryException {
		ValueObject getVO = null;
		Connection con = null;
		String sql = null;
		try {
			con = getConnection();

			List<SqlParameter> sqlParams = new ArrayList<SqlParameter>();
			sql = makeSql(query, paramVO, sqlParams); 
			
			getVO = executeQuery(con, sql, 
					getParameterValues(sqlParams,paramVO), 
					pageVO, query.getResultMap(), query.getSqlName());
		}
		finally {
			close(con);
		}

		return getVO;
	}
	
    /**
     * <p>
     * DB update, insert, delete Query를 수행한다.
     * 수행할 SQL문을 담고 있는 SqlQuery 객체와 파라메터를
	 * 담고 있는 ValueObject 객체를 받아서 SQL을 수행한 후 처리된 건 수를 리턴한다.
     * </p>
	 * @param query 수행할 SQL문을 담고 있는 SqlQuery 객체
	 * @param paramVO 파라메터를 담고 있는 ValueObject
     * @return INSERT, UPDATE, DELETE 실행결과의 row count 또는 0(DDL SQL 문의 경우)
     */
	protected int executeUpdate(SqlQuery query, ValueObject paramVO)
    		throws SqlQueryException {
		int count = 0;
		Connection con = null;
		String sql = null;
		try {
			con = getConnection();
			
			List<SqlParameter> sqlParams = new ArrayList<SqlParameter>();
			sql = makeSql(query, paramVO, sqlParams);
			
			count = executeUpdate(con, sql, getParameterValues(sqlParams,paramVO), query.getSqlName());
		}
		finally {
			close(con);
		}
		
		return count;
	}
	
    /**
     * <p>
     * DB Insert Query를 수행하며, 입력된 결과를 리턴한다.
     * 수행할 SQL문을 담고 있는 SqlQuery 객체와 파라메터를
	 * 담고 있는 ValueObject 객체를 받아서 SQL을 수행한 후
     * 테이블에 입력되는 컬럼들 중 테이블의 컬럼위치를 기준으로 처음부터 numKeyCols 만큼 지정된 개수의 컬럼들의 
     * 입력된 값을 ValueObject로 담아서 리턴해준다.
     * JDBC 3.0을 지원하는 Driver에서만 사용이 가능하다.
     * </p>
	 * @param query 수행할 SQL문을 담고 있는 SqlQuery 객체
	 * @param paramVO 파라메터를 담고 있는 ValueObject
     * @param numKeyCols 리턴해 줄 테이블 컬럼의 개수를 지정한다.
     * @return 실행된 SQL이 Insert 문인 경우에 만 입력된 컬럼 값을 담은 ValueObject를 리턴,
     *         그 외에는 비어있는 ValueObject를 리턴
     */
	protected ValueObject executeUpdateReturnKeys(SqlQuery query, 
				ValueObject paramVO, int numKeyCols) throws SqlQueryException {
		ValueObject rsVO = null;
		Connection con = null;
		String sql = null;
		try {
			con = getConnection();

			List<SqlParameter> sqlParams = new ArrayList<SqlParameter>();
			sql = makeSql(query, paramVO, sqlParams); 
			
			rsVO = executeUpdateReturnKeys(con, sql, 
					getParameterValues(sqlParams,paramVO), 
					numKeyCols, query.getResultMap(), query.getSqlName());
		}
		finally {
			close(con);
		}

		return rsVO;
	}
	
    /**
     * <p>
     * DB Insert Query를 수행하며, 입력된 결과를 리턴한다.
     * 수행할 SQL문을 담고 있는 SqlQuery 객체와 파라메터를
	 * 담고 있는 ValueObject 객체를 받아서 SQL을 수행한 후
     * 테이블에 입력되는 컬럼들 중 columnIndexes로 지정된 위치의 컬럼들의 
     * 입력된 값을 ValueObject로 담아서 리턴해준다.
     * JDBC 3.0을 지원하는 Driver에서만 사용이 가능하다.
     * </p>
	 * @param query 수행할 SQL문을 담고 있는 SqlQuery 객체
	 * @param paramVO 파라메터를 담고 있는 ValueObject
     * @param columnIndexes 리턴해 줄 테이블 컬럼들의 인덱스를 담은 배열
     * @return 실행된 SQL이 Insert 문인 경우에 만 입력된 컬럼 값을 담은 ValueObject를 리턴,
     *         그 외에는 비어있는 ValueObject를 리턴
     */
	protected ValueObject executeUpdateReturnKeys(SqlQuery query, 
				ValueObject paramVO, int[] columnIndexes) throws SqlQueryException {
		ValueObject rsVO = null;
		Connection con = null;
		String sql = null;
		try {
			con = getConnection();
			
			List<SqlParameter> sqlParams = new ArrayList<SqlParameter>();
			sql = makeSql(query, paramVO, sqlParams); 
			
			rsVO = executeUpdateReturnKeys(con, sql, 
					getParameterValues(sqlParams, paramVO), 
					columnIndexes, query.getResultMap(), query.getSqlName());
		}
		finally {
			close(con);
		}
		
		return rsVO;
	}
	
    /**
     * <p>
     * DB Insert Query를 수행하며, 입력된 결과를 리턴한다.
     * 수행할 SQL문을 담고 있는 SqlQuery 객체와 파라메터를
	 * 담고 있는 ValueObject 객체를 받아서 SQL을 수행한 후
     * 테이블에 입력되는 컬럼들 중 columnNames로 지정된 명칭의 컬럼들로 
     * 입력된 값을 ValueObject로 담아서 리턴해준다.
     * JDBC 3.0을 지원하는 Driver에서만 사용이 가능하다.
     * </p>
	 * @param query 수행할 SQL문을 담고 있는 SqlQuery 객체
	 * @param paramVO 파라메터를 담고 있는 ValueObject
     * @param columnNames 리턴해 줄 테이블 컬럼들의 이름을 배열
     * @return 실행된 SQL이 Insert 문인 경우에 만 입력된 컬럼 값을 담은 ValueObject를 리턴,
     *         그 외에는 비어있는 ValueObject를 리턴
     */
	protected ValueObject executeUpdateReturnKeys(SqlQuery query, 
			  ValueObject paramVO, String[] columnNames) throws SqlQueryException {
		ValueObject rsVO = null;
		Connection con = null;
		String sql = null;
		try {
			con = getConnection();
			
			List<SqlParameter> sqlParams = new ArrayList<SqlParameter>();
			sql = makeSql(query, paramVO, sqlParams);
			
			rsVO = executeUpdateReturnKeys(con, sql, 
					getParameterValues(sqlParams,paramVO), 
					columnNames, query.getResultMap(), query.getSqlName());
		}
		finally {
			close(con);
		}
		
		return rsVO;
	}
	
    /**
     * <p>
     * DB Insert Query를 수행하며, 입력된 결과를 리턴한다.
     * 수행할 SQL문을 담고 있는 SqlQuery 객체와 파라메터를
	 * 담고 있는 ValueObject 객체를 받아서 SQL을 수행한 후
     * 자동으로 값이 생성되는 컬럼들로 입력된 값을 ValueObject로 담아서 리턴해준다.
     * JDBC 3.0을 지원하는 Driver에서만 사용이 가능하다.
     * </p>
	 * @param query 수행할 SQL문을 담고 있는 SqlQuery 객체
	 * @param paramVO 파라메터를 담고 있는 ValueObject
     * @return 실행된 SQL이 Insert 문인 경우에 만 입력된 컬럼 값을 담은 ValueObject를 리턴,
     *         그 외에는 비어있는 ValueObject를 리턴
     */
	protected ValueObject executeUpdateReturnKeys(SqlQuery query, 
			  ValueObject paramVO) throws SqlQueryException {
		ValueObject rsVO = null;
		Connection con = null;
		String sql = null;
		try {
			con = getConnection();
			
			List<SqlParameter> sqlParams = new ArrayList<SqlParameter>();
			sql = makeSql(query, paramVO, sqlParams); 
			
			rsVO = executeUpdateReturnKeys(con, sql, 
					getParameterValues(sqlParams,paramVO), 
					query.getResultMap(), query.getSqlName());
		}
		finally {
			close(con);
		}
		
		return rsVO;
	}
	
    /**
     * <p>
     * 여러건의 update, insert, delete query를 한번의 배치로 수행한다.
     * 배치처리될 SqlQuery 객체와 파라메터들을 담고 있는 ValueObject를 
     * 전달하면 ValueObject내의 각 row에 대하여 처리를 한다.
     * </p>
     * <p>
     * 동적 Sql이 배치처리될 경우 첫번째 파라메터를 기준으로 생성된 Sql이 적용된다. 
     * </p>
     *
     * @param query  수행할 SqlQuery 객체
     * @param paramVO Query 문에 매핑될 파라메터들을 여러건 가지고 있는 ValueObject
     * @return INSERT, UPDATE, DELETE 실행결과의 row count 또는 0(DDL SQL 문의 경우)
     */
	protected int[] executeBatch(SqlQuery query, ValueObject paramVO)
    		throws SqlQueryException {
		int paramCount = (paramVO == null?0:paramVO.size());
		List<?>[] paramsList = new ArrayList<?>[paramCount];
		
		// paramVO의 첫번째 row를 기준으로 Sql과 파라메터 목록을 생성한다. 
		List<SqlParameter> sqlParams = new ArrayList<SqlParameter>();
		String sql = makeSql(query, paramVO, sqlParams);
		
		for(int i=0;i<paramCount;i++) {
			paramsList[i] = getParameterValues(i,sqlParams,paramVO);
		}
		
		int[] counts = null;
		Connection con = getConnection();
		try {
			counts = executeBatch(con, sql, paramsList, query.getSqlName());
		}
		finally {
			close(con);
		}
		
		return counts;
	}
	
	/**
	 * Stored Procedure를 실행한다. 수행할 Procedure 호출문을 담고 있는 SqlQuery 객체와 
	 * 파라메터를 담고 있는 ValueObject 객체를 받아서 Procedure를 실행 후 그 결과 데이터를
	 * ValueObject로 리턴한다.
	 * Stored Procedure가 ResultSet을 리턴하는 경우 또는 Oracle의 REF_CURSOR를 OUT으로 반환하는
	 * 경우에는 ValueObjectAssembler를 리턴하는 executeCall() 메소드를 사용해야한다.
	 * @param query
	 * @param paramVO
	 * @return
	 * @throws ApplicationException
	 */
	protected ValueObject executeCall(SqlQuery query, ValueObject paramVO) 
			throws SqlQueryException {
		Logger log = getLogger();
		
		ValueObject retVO = null;
		Connection con = null;
		String callStatement = null;
		CallableStatement cstmt = null;
		StringBuilder sb = new StringBuilder();
		
        long stime = System.currentTimeMillis(); // 수행 시간 계산용
        long etime = 0;
		
		try {
			con = getConnection();
			
			List<SqlParameter> sqlParams = new ArrayList<SqlParameter>();
			query.appendSql(paramVO,sb,sqlParams);
			callStatement = sb.toString().trim();
			
			cstmt = con.prepareCall(callStatement);
			
			sb.setLength(0);
			Map<String,Object> retMap = executeCallInternal(cstmt,sqlParams,paramVO,
					query.getResultMap(),sb);
			
			retVO = new ValueObject();
			
			// Map의 값들을 모두 VO에 담는다.
			Iterator<String> itor = retMap.keySet().iterator();
			while(itor.hasNext()) {
				String keyName = itor.next();
				retVO.set(keyName,retMap.get(keyName));
			}
		}
		catch (SQLException ex) {
            if (log.isErrorEnabled()) {
                etime = System.currentTimeMillis();
                log.info(query.getSqlName() + " execute call failed. |" + Long.toString(etime - stime) + " msec|param=" 
                		+ sb.toString() + "|" + ex.getMessage() + "| " + callStatement + " |" 
                		+ Long.toString(etime - stime) + " msec");
            }
			throw new SqlQueryException(ex);
		}
		finally {
			close(con,cstmt,null);
		}
		
        if (log.isInfoEnabled()) {
            etime = System.currentTimeMillis();
            log.info(query.getSqlName() + " execute call succeeded. |" + Long.toString(etime - stime) + " msec|param=" 
            		+ sb.toString()	+ callStatement + " |" 
            		+ Long.toString(etime - stime) + " msec");
        }
		return retVO;
	}
	
	/**
	 * <p>
	 * ResultSet을 리턴하거는 Oracle의 REF CURSOR를 OUT으로 반환하는 Stored Procedure를 실행한다. 
	 * 수행할 Procedure 호출문을 담고 있는 SqlQuery 객체와 
	 * 파라메터를 담고 있는 ValueObject 객체를 받아서 Procedure를 실행 후 그 결과 데이터를
	 * 각각 ValueObject로 생성한 후 다시 ValueObjectAssembler 객체에 담아서 리턴한다.
	 * </p>
	 * <p>
	 * 각 결과 ValueObject의 이름은 파라메터로 주어진 rsNames를 사용하여 명명된다.
	 * rsNames[0]는 OUT 또는 INOUT 파라메터 결과를 담을 ValueObject 이름이며 
	 * rsNames[1]부터는 ResultSet으로 부터 결과를 담을 ValueObject의 이름으로 사용된다.
	 * Stored Procedure에서 리턴하는 ResultSet의 개수가 n개라면 rsNames[]의 length는 n+1개가 되어야 한다.
	 * </p>
	 * <p>
	 * 또한 OUT으로 반환되는 값 중에 Oracle의 REF CURSOR(타입은 ORACLECURSOR로 지정함)가 있으면 Cursor로 부터 조회된 값들은 별도의 
	 * ValueObject 로 생성되어 반환되는 ValueObjectAssembler 객체에 담겨진다.
	 * </p>
	 * @param query
	 * @param paramVO
	 * @param rsNames 
	 * @return
	 * @throws ApplicationException
	 */
	protected ValueObjectAssembler executeCall(SqlQuery query, ValueObject paramVO, String[] rsNames) 
			throws SqlQueryException {
		Logger log = getLogger();
		
		ValueObjectAssembler retVOs = null;
		Connection con = null;
		String callStatement = null;
		CallableStatement cstmt = null;
		StringBuilder sb = new StringBuilder();
		
        long stime = System.currentTimeMillis();    // 수행 시간 계산용
        long etime = 0;
		
		try {
			con = getConnection();
			
			List<SqlParameter> sqlParams = new ArrayList<SqlParameter>();
			query.appendSql(paramVO,sb,sqlParams);
			callStatement = sb.toString().trim();
			
			cstmt = con.prepareCall(callStatement);
			
			sb.setLength(0);
			Map<String,Object> retMap = executeCallInternal(cstmt,sqlParams,paramVO,
					query.getResultMap(),sb);
			
			retVOs = new ValueObjectAssembler();
			ValueObject outVO = new ValueObject(); // 기본 OUT 값을 담아놓을 VO
			
			Iterator<String> itor = retMap.keySet().iterator();
			while(itor.hasNext()) {
				String keyName = itor.next();
				Object obj = retMap.get(keyName);
				if ( obj instanceof ValueObject ) { // VO 이면 VOs에 담는다.
					retVOs.set(keyName,(ValueObject)obj);
				} else { // VO가 아니면 기본 OUT 값이므로 outVO에 담는다.
					outVO.set(keyName,obj);
				}
			}
			
			retVOs.set(rsNames[0],outVO); // outVO를 VOs에 담는다.
			
			// resultset을 리턴하는 경우에는 각 resultset에서 VO를 생성하여 VOs에 담는다.
			ResultSet rs = null;
			for(int i=1;i<rsNames.length;i++) {
				rs = cstmt.getResultSet();
				outVO = new ValueObject();
				fetchResultSet(rs, outVO, null);
				rs.close();
				retVOs.set(rsNames[i],outVO);
				cstmt.getMoreResults();
			}
			
		}
		catch (SQLException ex) {
            if (log.isErrorEnabled()) {
                etime = System.currentTimeMillis();
                log.info(query.getSqlName() + " execute call failed. |" + Long.toString(etime - stime) + " msec|param=" 
                		+ sb.toString() + "|" + ex.getMessage() + "| " + callStatement + " |" 
                		+ Long.toString(etime - stime) + " msec");
            }
			throw new SqlQueryException(ex);
		}
		finally {
			close(con,cstmt,null);
		}
		
        if (log.isInfoEnabled()) {
            etime = System.currentTimeMillis();
            log.info(query.getSqlName() + " execute call succeeded. |" + Long.toString(etime - stime) + " msec|param=" 
            		+ sb.toString()	+ callStatement + " |" 
            		+ Long.toString(etime - stime) + " msec");
        }
		return retVOs;
		
	}
	
	private Map<String,Object> executeCallInternal(CallableStatement cstmt, 
			List<SqlParameter> sqlParams, ValueObject paramVO, ResultMap rmap, 
			StringBuilder logsb) throws SQLException, SqlQueryException {
		Logger log = getLogger();
		
		SqlParameter sqlParam = null;
		
		// 파라메터 set 또는 Out 파라메터 등록 작업
		for(int i=0;i<sqlParams.size();i++) {
			sqlParam = sqlParams.get(i);
			if ( log.isInfoEnabled() ) { // 로그 출력용..
				logsb.append(sqlParam.toString());
			}
			if ( sqlParam.getMode() != SqlParameter.MODE_OUT ) {
				// IN or INOUT mode : set In parameter value
				//Object paramObj = paramVO.get(sqlParam.getName());
				try {
					Object paramObj = sqlParam.getParameterValue(paramVO,0);
					getSupporter().setCallableStatementParam(cstmt,i+1,paramObj);
					if ( log.isInfoEnabled() ) { // 로그 출력용..
						logsb.append("[");
						logsb.append(paramObj);
						logsb.append("]");
					}
				} catch (ParseException e) {
					throw new SqlQueryException("90008");
				}
			}
			if ( sqlParam.getMode() != SqlParameter.MODE_IN ) {
				// OUT or INOUT mode : register Out parameter
				if (sqlParam.getType() == Types.ARRAY) { 
					//2010.03.18 Stored Procedure의 Array 타입 처리(김형도)
					cstmt.registerOutParameter(i+1,sqlParam.getType(),sqlParam.getFormat());
				} else {
					cstmt.registerOutParameter(i+1,sqlParam.getType());
				}
			}
			if ( log.isInfoEnabled() ) { // 로그 출력용..
				logsb.append(",");
			}
		}
		
		// debug용 로그 출력
		if (log.isDebugEnabled()) {
			log.debug("execute call begins. |param="+logsb.toString()+ cstmt);
		}
		
		// execute
		cstmt.execute();
		
		// get Out result value
		//ValueObject retVO = new ValueObject();
		Map<String,Object> retMap = new HashMap<String,Object>();
		ColumnReader reader = null;
		for(int i=0;i<sqlParams.size();i++) {
			sqlParam = sqlParams.get(i);
			if ( sqlParam.getMode() != SqlParameter.MODE_IN ) {
				// OUT or INOUT mode : get Out result value
				//System.out.println("COLUMN READER:"+sqlParam.getName()+"."+sqlParam.getType());
				reader = getColumnReader(sqlParam.getName(),sqlParam.getType(),null,rmap);
				//ColumnReader columnReader = supporter.getColumnReader(sqlParam.getType());
				retMap.put(sqlParam.getName(), reader.read(i+1, cstmt));
			}
		}
		
		return retMap;
	}
	
	/**
	 * 자신의 패키지 디렉토리 위치를 기준으로 하위 경로를 주면 전체 경로를 생성하여
	 * 해당 SQL 파일을 사용하여 SqlQueryPage 객체를 생성하여 반환한다.
	 * @param relativePath
	 * @return
	 */
	protected SqlQueryPage getQueryPage(String relativePath) {
		// 실제 DAO 클래스를 로딩한 클래스로더를 사용하여 SQL 파일을 로딩하도록 해야한다.
		return new SqlQueryPage(packagePath+relativePath, this.getClass().getClassLoader());
	}
	
	/**
	 * 이 클래스에서 수행하는 SQL 문장에 추가할 prefix 문장을 설정해 놓는다.
	 * 또한 이 클래스의 패키지명을 클래스패스로 바꾸어 저장해 놓는다.
	 * 이것은 getQueryPage() 내에서 상대경로를 절대경로로 변경하기 위하여 사용된다.
	 *
	 */
	private void initialize() {
		packagePath = this.getClass().getPackage().getName().replace('.','/')+"/";
	}
	
	/**
	 * 실행할 Query 문장을 생성한다.
	 * @param query
	 * @param paramVO
	 * @param sqlParams
	 * @return
	 * @throws ApplicationException
	 */
	private String makeSql(SqlQuery query, ValueObject paramVO, List<SqlParameter> sqlParams) {
		StringBuilder sb = new StringBuilder();
		query.appendSql(paramVO,sb,sqlParams);
		
		return sb.toString();
	}
	
	/**
	 * ValueObject의 첫번째 row에 담겨 있는 값들을 파라메터 순서대로 꺼내어 리스트에 담아 반환한다.
	 * @param valueVO
	 * @return
	 * @throws ApplicationException 
	 */
	private List<Object> getParameterValues(List<SqlParameter> sqlParams, ValueObject valueVO) {
		return getParameterValues(0, sqlParams, valueVO);
	}
	
	/**
	 * ValueObject의 idx 번째 row에 담겨 있는 값들을 주어진 파라메터 순서대로 꺼내어 리스트에 담아 반환한다.
	 * @param valueVO
	 * @return
	 * @throws ApplicationException 
	 */
	private List<Object> getParameterValues(int idx, List<SqlParameter> sqlParams, ValueObject valueVO) 
			throws SqlQueryException {
		//long stime = System.currentTimeMillis();
		List<Object> paraValues = new ArrayList<Object>(sqlParams.size());
		
		for(int i=0;i<sqlParams.size();i++) {
			try {
				paraValues.add(sqlParams.get(i).getParameterValue(valueVO,idx));
			} catch (ParseException ex) {
				throw new SqlQueryException("90008"); // 데이터베이스 처리를 위한 파라메터 매핑 중 형식이 맞지 않는 데이터가 입력되었음
			}
		}
		
		//sqlLog.info("getParameterValues() takes "+(System.currentTimeMillis()-stime)+" msec.");
		
		return paraValues;
	}
}
