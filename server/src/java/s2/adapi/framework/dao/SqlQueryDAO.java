package s2.adapi.framework.dao;

import org.slf4j.Logger;

import s2.adapi.framework.container.ParentAwareService;
import s2.adapi.framework.dao.sql.DataSource;
import s2.adapi.framework.exception.ApplicationException;
import s2.adapi.framework.query.SqlQuery;
import s2.adapi.framework.query.SqlQueryPage;
import s2.adapi.framework.vo.ValueObject;
import s2.adapi.framework.vo.ValueObjectAssembler;

/**
 * <p>
 * SqlQuery 기능을 내부에 포함시킨 DAO이며 이 클래스를 상속받아 별도 DAO를 작성하는 방식이 아니라
 * DAO 클래스 작성없이 이 비지니스 로직에서 DAO를 바로 사용하는 방식이다.</p>
 * <p>
 * 아래의 예는 서비스 컨테이너를 사용하지 않고 바로 객체를 생성하여 사용하는 예이다.
 * SqlQueryDAO 생성시 3번째 인자로 BusinessMgtImpl 객체 자신을 파라메터로 전달하고 있는데,
 * 이것은 BusinessMgtImpl 클래스의 패키지를 사용하여 상대경로 지정된 Query Map 파일의 절대 경로를
 * 얻어오기 위한 목적과 BusinessMgtImpl 클래스를 로딩한 클래스로더를 얻어오기 위한 목적으로 사용된다.</p>
 * <pre>
 * public class BusinessMgtImpl extends ContextAwareService implements BusinessMgt {
 * 
 *     SqlQueryDAO dao = new SqlQueryDAO("jdbc/comdb","sqls/businessdao_sqls.xml",this);
 * 
 *     public ValueObject getBusinessLogic(ValueObject pVO) throws ApplicationException {
 *         ...
 *     
 *         ValueObject getVO = dao.executeQuery("getsql",pVO);
 *  
 *         ...
 *     }
 * }
 * </pre>
 * 서비스 컨테이너를 사용하여 Injection하는 방식으로 코딩할 경우는 아래과 같이 작성할 수 있다.
 * 이 경우 SqlQueryDAO가 ParantAwareSerivce이므로 BusinessMgtImpl 객체가 setParent() 
 * 메소드를 통하여 전달되어 진다.
 * <pre>
 * public class BusinessMgtImpl extends ContextAwareService implements BusinessMgt {
 * 
 *     SqlQueryDAO dao = null;
 *     
 *     public void setDao(SqlQueryDAO dao) {
 *         this.dao = dao;
 *     }
 *     
 *     public ValueObject getBusinessLogic(ValueObject pVO) throws ApplicationException {
 *        ...
 *     }
 * }
 * 
 * -서비스 설정 파일
 *   &lt;service name="BusinessDAO"
 *           class="s2.adapi.framework.dao.SqlQueryDAO"
 *           interceptor="system.proxy"
 *           singleton="true"&gt; 
 *       &lt;property name="datasource" ref="jdbc.comdb"/&gt;
 *       &lt;property name="sql" value="sqls/businessdao_sqls.xml"/&gt;
 *   &lt;/service&gt;
 *  
 *   &lt;service name="BusinessMgt"
 *           interface="${package}.businessmgt.BusinessMgt"
 *           class="${package}.businessmgt.BusinessMgtImpl"
 *           interceptor="system.proxy"
 *           singleton="true"&gt; 
 *       &lt;property name="dao" ref="BusinessDAO"/&gt;
 *   &lt;/service&gt;
 * </pre>
 * @author 김형도
 * @since 4.0
 */
public class SqlQueryDAO extends JdbcQueryDAO implements ParentAwareService {

	protected String sqlPath = null;
	
	/**
	 * SqlQueryDAO 객체를 생성하는 객체를 부모객체로 저장해 놓는다.
	 * 부모객체는 classpath 경로로 주어지는 sql 파일의 상대 경로로 부터
	 * sql 파일을 리소스로 읽기 위하여 필요한 클래스객체 및 클래스로더를 얻기위하여
	 * 사용된다.
	 */
	protected Object parentObj = null;
	protected String parentObjectClassName = null;
	
	/**
	 * 한번 파싱된 Sql을 cache 하기 위하여 사용됨
	 */
	protected SqlQueryPage sqls = null;
	
	/**
	 * 기본 생성자
	 */
	public SqlQueryDAO() {
		super();
	}
	
	/**
	 * 생성자이다. SqlQueryDAO 객체를 생성하는 부모객체를 넣는다.
	 * @param parent 부모객체
	 */
	public SqlQueryDAO(Object parent) {
		super();
		setParent(parent);
	}
	
	/**
	 * 생성자이다. 사용할 데이터 소스와 SQL 파일 경로를 지정한다.
	 * @param dsn 데이터소스명
	 * @param sqlpath SQL 파일의 경로(클래스패스 경로)
	 * @param parent 부모객체
	 */
	public SqlQueryDAO(String sqlpath, Object parent) {
		super();
		setParent(parent);
		setSql(sqlpath);
	}
	
	/**
	 * 생성자이다. 사용할 데이터 소스와 SQL 파일 경로를 지정한다.
	 * @param dsn 데이터소스명
	 * @param sqlpath SQL 파일의 경로(클래스패스 경로)
	 */
	public SqlQueryDAO(String sqlpath) {
		super();
		setSql(sqlpath);
	}
	
	/**
	 * 생성자이다. 사용할 데이터 소스 객체를 지정한다.
	 * @param ds 데이터소스 객체
	 * @param parent 부모객체
	 */
	public SqlQueryDAO(DataSource ds, Object parent) {
		super(ds);
		setParent(parent);
	}
	
	/**
	 * 생성자이다. 사용할 데이터 소스 객체를 지정한다.
	 * @param ds 데이터소스 객체
	 */
	public SqlQueryDAO(DataSource ds) {
		super(ds);
	}
	
	/**
	 * 생성자이다. 사용할 데이터 소스 객체와 SQL 파일 경로를 지정한다.
	 * @param ds 데이터소스 객체
	 * @param sqlpath SQL 파일의 경로(클래스패스 경로)
	 * @param parent 부모객체
	 */
	public SqlQueryDAO(DataSource ds, String sqlpath, Object parent) {
		super(ds);
		setParent(parent);
		setSql(sqlpath);
	}
	
	/**
	 * 생성자이다. 사용할 데이터 소스 객체와 SQL 파일 경로를 지정한다.
	 * @param ds 데이터소스 객체
	 * @param sqlpath SQL 파일의 경로(클래스패스 경로)
	 */
	public SqlQueryDAO(DataSource ds, String sqlpath) {
		super(ds);
		setSql(sqlpath);
	}
	
	/**
	 * 이 객체를 생성하는 객체를 지정한다.
	 * ParentAwareService 인터페이스의 setParent() 메소드 구현이다.
	 */
	public void setParent(Object parent) {
		parentObj = parent;
		if (parentObj != null) {
			packagePath = parentObj.getClass().getPackage().getName().replace('.','/')+"/";
			parentObjectClassName = parentObj.getClass().getSimpleName() + " ";
		} else {
			packagePath = "";
			parentObjectClassName = "";
		}
	}
	
	/**
	 * 사용할 SQL 파일의 경로를 지정한다.
	 * @param sqlpath SQL 파일의 경로(클래스패스 경로)
	 */
	public void setSql(String path) {
		sqlPath = path;
	}
	
	/**
	 * SQL 파일의 경로로부터 SqlQueryPage 객체를 생성한다.
	 * @param sqlpath SQL 파일의 경로(클래스패스 경로)
	 */
	private SqlQueryPage getSqlQueryPage() {
		Logger log = getLogger();
		
		// 한번 파싱된 Sql은 cache하여 사용하도록 처리함(2010.09.08 김형도)
		if (sqls == null) {
			if (parentObj != null) {
				// parentObj가 설정되어 있으면 parentObj를 기준으로 한 상대경로를 사용한다.
				// 또한 해당 클래스패스로부터 파일을 읽기 위한 클래스로더를 parentObj의 클래스로더로 사용한다.
				sqls = new SqlQueryPage(packagePath+sqlPath,parentObj.getClass().getClassLoader());
			} else {
				// parentObj가 설정되지 않았으면 주어진 sqlpath를 절대경로로 사용한다.
				sqls = new SqlQueryPage(sqlPath);
			}
		}
		else if (log.isDebugEnabled()) {
			log.debug("using cached sql query file ["+sqlPath+"]...");
		}
		
		return sqls;
	}
	
	/**
	 * <p>
     * 여러건의 update, insert, delete query를 한번의 배치로 수행한다.
     * 배치처리될 SQL의 statement ID와 파라메터들을 담고 있는 ValueObject를 
     * 전달하면 ValueObject내의 각 row에 대하여 처리를 한다.
     * </p>
     * <p>
     * 동적 Sql이 배치처리될 경우 첫번째 파라메터를 기준으로 생성된 Sql이 적용된다. 
     * </p>
	 * @param queryname 실행할 SQL의 statement ID
     * @param paramVO Query 문에 매핑될 파라메터들을 여러건 가지고 있는 ValueObject
     * @return 각 row 별 실행결과의 처리건수들을 담은 배열
	 * @throws ApplicationException
	 */
	public int[] executeBatch(String queryname, ValueObject paramVO)
			throws SqlQueryException {
		Logger log = getLogger();
		
		if (log.isInfoEnabled()) {
			log.info(parentObjectClassName + queryname);
		}
		
		SqlQuery sql = getSqlQueryPage().getQuery(queryname);
		
		return executeBatch(sql,paramVO);
	}
	
	/**
	 * 조회용 SQL 문을 수행한다. 수행할 SQL문의 statement ID와 파라메터를
	 * 담고 있는 ValueObject 객체를 받아서 SQL을 수행한 후 그 결과 데이터를
	 * ValueObject로 리턴한다.
	 * @param queryname 실행할 SQL의 statement ID
	 * @param paramVO 파라메터를 담고 있는 ValueObject
	 * @return ValueObject 조회 결과를 담은 ValueObject
	 * @throws ApplicationException
	 */
	public ValueObject executeQuery(String queryname, ValueObject paramVO)
			throws SqlQueryException {
		Logger log = getLogger();
		
		if (log.isDebugEnabled()) {
			log.debug(parentObjectClassName + queryname);
		}
		
		SqlQuery sql = getSqlQueryPage().getQuery(queryname);
		
		return executeQuery(sql,paramVO);
	}
	
	/**
	 * 주어진 SQL에 대하여 페이지 단위로 조회를 수행한다. 
	 * 수행할 SQL문의 statement ID와 파라메터를
	 * 담고 있는 ValueObject 객체를 받아서 SQL을 수행한 후 그 결과 데이터를
	 * ValueObject로 리턴한다.
	 * @param queryname 실행할 SQL의 statement ID
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
	public ValueObject executeQuery(String queryname, ValueObject paramVO, ValueObject pageVO)
			throws SqlQueryException {
		Logger log = getLogger();
		
		if (log.isDebugEnabled()) {
			log.debug(parentObjectClassName + queryname);
		}
		
		SqlQuery sql = getSqlQueryPage().getQuery(queryname);
		
		return executeQuery(sql,paramVO,pageVO);
	}
	
    /**
     * <p>
     * update, insert, delete Query를 수행한다.
     * 수행할 SQL문의 statement ID와 파라메터를
	 * 담고 있는 ValueObject 객체를 받아서 SQL을 수행한 후 처리된 건 수를 리턴한다.
     * </p>
	 * @param queryname 실행할 SQL의 statement ID
	 * @param paramVO 파라메터를 담고 있는 ValueObject
     * @return INSERT, UPDATE, DELETE 실행결과의 row count 또는 0(DDL SQL 문의 경우)
     */
	public int executeUpdate(String queryname, ValueObject paramVO)
			throws SqlQueryException {
		Logger log = getLogger();
		
		if (log.isDebugEnabled()) {
			log.debug(parentObjectClassName + queryname);
		}
		
		SqlQuery sql = getSqlQueryPage().getQuery(queryname);
		
		return executeUpdate(sql,paramVO);
	}
	
    /**
     * <p>
     * Insert Query를 수행하며, 입력된 결과를 리턴한다.
     * 수행할 SQL문의 statement ID와 파라메터를
	 * 담고 있는 ValueObject 객체를 받아서 SQL을 수행한 후
     * 자동으로 값이 생성되는 컬럼들로 입력된 값을 ValueObject로 담아서 리턴해준다.
     * JDBC 3.0을 지원하는 Driver에서만 사용이 가능하다.
     * </p>
	 * @param queryname 실행할 SQL의 statement ID
	 * @param paramVO 파라메터를 담고 있는 ValueObject
     * @return 실행된 SQL이 Insert 문인 경우에 만 입력된 컬럼 값을 담은 ValueObject를 리턴,
     *         그 외에는 비어있는 ValueObject를 리턴
     */
	public ValueObject executeUpdateReturnKeys(String queryname, ValueObject paramVO)
			throws SqlQueryException {
		Logger log = getLogger();
		
		if (log.isDebugEnabled()) {
			log.debug(parentObjectClassName + queryname);
		}
		
		SqlQuery sql = getSqlQueryPage().getQuery(queryname);
		
		return executeUpdateReturnKeys(sql,paramVO);
	}
	
    /**
     * <p>
     * Insert Query를 수행하며, 입력된 결과를 리턴한다.
     * 수행할 SQL문의 statement ID와 파라메터를
	 * 담고 있는 ValueObject 객체를 받아서 SQL을 수행한 후
     * 테이블에 입력되는 컬럼들 중 테이블의 컬럼위치를 기준으로 처음부터 numKeyCols 만큼 지정된 개수의 컬럼들의 
     * 입력된 값을 ValueObject로 담아서 리턴해준다.
     * JDBC 3.0을 지원하는 Driver에서만 사용이 가능하다.
     * </p>
	 * @param queryname 실행할 SQL의 statement ID
	 * @param paramVO 파라메터를 담고 있는 ValueObject
     * @param numKeyCols 리턴해 줄 테이블 컬럼의 개수를 지정한다.
     * @return 실행된 SQL이 Insert 문인 경우에 만 입력된 컬럼 값을 담은 ValueObject를 리턴,
     *         그 외에는 비어있는 ValueObject를 리턴
     */
	public ValueObject executeUpdateReturnKeys(String queryname, ValueObject paramVO, 
		      int numKeyCols) throws SqlQueryException {
		Logger log = getLogger();
		
		if (log.isDebugEnabled()) {
			log.debug(parentObjectClassName + queryname);
		}
		
		SqlQuery sql = getSqlQueryPage().getQuery(queryname);
		
		return executeUpdateReturnKeys(sql,paramVO,numKeyCols);
	}
	
    /**
     * <p>
     * Insert Query를 수행하며, 입력된 결과를 리턴한다.
     * 수행할 SQL문의 statement ID와 파라메터를
	 * 담고 있는 ValueObject 객체를 받아서 SQL을 수행한 후
     * 테이블에 입력되는 컬럼들 중 columnIndexes로 지정된 위치의 컬럼들의 
     * 입력된 값을 ValueObject로 담아서 리턴해준다.
     * JDBC 3.0을 지원하는 Driver에서만 사용이 가능하다.
     * </p>
	 * @param queryname 실행할 SQL의 statement ID
	 * @param paramVO 파라메터를 담고 있는 ValueObject
     * @param columnIndexes 리턴해 줄 테이블 컬럼들의 인덱스를 담은 배열
     * @return 실행된 SQL이 Insert 문인 경우에 만 입력된 컬럼 값을 담은 ValueObject를 리턴,
     *         그 외에는 비어있는 ValueObject를 리턴
     */
	public ValueObject executeUpdateReturnKeys(String queryname, ValueObject paramVO, 
			  int[] columnIndexes) throws SqlQueryException {
		Logger log = getLogger();
		
		if (log.isDebugEnabled()) {
			log.debug(parentObjectClassName + queryname);
		}
		
		SqlQuery sql = getSqlQueryPage().getQuery(queryname);
		
		return executeUpdateReturnKeys(sql,paramVO,columnIndexes);
	}
	
    /**
     * <p>
     * Insert Query를 수행하며, 입력된 결과를 리턴한다.
     * 수행할 SQL문의 statement ID와 파라메터를
	 * 담고 있는 ValueObject 객체를 받아서 SQL을 수행한 후
     * 테이블에 입력되는 컬럼들 중 columnNames로 지정된 명칭의 컬럼들로 
     * 입력된 값을 ValueObject로 담아서 리턴해준다.
     * JDBC 3.0을 지원하는 Driver에서만 사용이 가능하다.
     * </p>
	 * @param queryname 실행할 SQL의 statement ID
	 * @param paramVO 파라메터를 담고 있는 ValueObject
     * @param columnNames 리턴해 줄 테이블 컬럼들의 이름을 배열
     * @return 실행된 SQL이 Insert 문인 경우에 만 입력된 컬럼 값을 담은 ValueObject를 리턴,
     *         그 외에는 비어있는 ValueObject를 리턴
     */
	public ValueObject executeUpdateReturnKeys(String queryname, ValueObject paramVO, 
			  String[] columnNames)	throws SqlQueryException {
		Logger log = getLogger();
		
		if (log.isDebugEnabled()) {
			log.debug(parentObjectClassName + queryname);
		}
		
		SqlQuery sql = getSqlQueryPage().getQuery(queryname);
		
		return executeUpdateReturnKeys(sql,paramVO,columnNames);
	}
	
	/**
	 * Stored Procedure를 실행한다. 수행할 Procedure 호출문을 담고 있는 SQL문의 statement ID와 
	 * 파라메터를 담고 있는 ValueObject 객체를 받아서 Procedure를 실행 후 그 결과 데이터를
	 * ValueObject로 리턴한다.
	 * Stored Procedure가 ResultSet을 리턴하는 경우 또는 Oracle의 REF_CURSOR를 OUT으로 반환하는
	 * 경우에는 ValueObjectAssembler를 리턴하는 executeCall() 메소드를 사용해야한다.
	 * @param queryname 실행할 SQL의 statement ID
	 * @param paramVO 파라메터를 담고 있는 ValueObject
	 * @return OUT 또는 INOUT 파라메터로 반환된 결과값을 가지고 있는 ValueObject 객체
	 * @throws ApplicationException
	 */
	public ValueObject executeCall(String queryname, ValueObject paramVO) 
			throws SqlQueryException {
		Logger log = getLogger();
		
		if (log.isDebugEnabled()) {
			log.debug(parentObjectClassName + queryname);
		}
		
		SqlQuery sql = getSqlQueryPage().getQuery(queryname);
		
		return executeCall(sql,paramVO);
	}
	
	/**
	 * <p>
	 * ResultSet을 리턴하거는 Oracle의 REF CURSOR를 OUT으로 반환하는 Stored Procedure를 실행한다. 
	 * 수행할 Procedure 호출문을 담고 있는 SQL문의 statement ID와
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
	 * @param queryname 실행할 SQL의 statement ID
	 * @param paramVO 파라메터를 담고 있는 ValueObject
	 * @param rsNames 결과 ValueObject의 이름들을 지정한 배열
	 * @return
	 * @throws ApplicationException
	 */
	public ValueObjectAssembler executeCall(String queryname, ValueObject paramVO, String[] rsNames) 
			throws SqlQueryException {
		Logger log = getLogger();
		
		if (log.isDebugEnabled()) {
			log.debug(parentObjectClassName + queryname);
		}
		
		SqlQuery sql = getSqlQueryPage().getQuery(queryname);
		
		return executeCall(sql,paramVO,rsNames);
	}
}
