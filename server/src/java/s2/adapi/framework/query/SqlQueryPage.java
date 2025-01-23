package s2.adapi.framework.query;

import java.io.IOException;

import org.xml.sax.SAXException;

import s2.adapi.framework.dao.SqlQueryException;
import s2.adapi.framework.exception.ApplicationException;
import s2.adapi.framework.query.element.ResultMap;
import s2.adapi.framework.query.element.SqlStatement;
import s2.adapi.framework.query.element.Sqls;
import s2.adapi.framework.util.SystemHelper;

/**
 * 하나의 XML 파일내에 정의된 SQL 문장들을 파싱하여 SQL 문장과 대응되는 SqlQuery 객체를 제공한다.
 * @author 김형도
 * @since 4.0
 */
public class SqlQueryPage {

	/**
	 * SQL 문장을 정의한 XML 파일의 클래스 패스 경로
	 */
	private String path = null;
	
	private ClassLoader classLoader = null;
	
	/**
	 * SQL 문장을 파싱한 Sqls 객체
	 */
	private Sqls sqls =  null;
	
	/**
	 * 생성자이다. 
	 * SqlQuery 파일의 클래스 패스를 지정한다.
	 * 
	 * @param path
	 */
	public SqlQueryPage(String path) {
		this(path,null);
	}
	
	/**
	 * 생성자이다.
	 * SqlQuery 파일의 클래스 패스와 
	 * 그 클래스 패스로부터 파일을 로딩하기 위한 클래스로더를 지정한다.
	 * @param path
	 * @param loader
	 */
	public SqlQueryPage(String path, ClassLoader loader) {
		this.path = path;
		if (loader == null) {
			classLoader = SystemHelper.getClassLoader();
		} else {
			classLoader = loader;
		}
	}
	
	/**
	 * 요청한 ID에 해당되는 SQL 문장을 표현하는 SqlQuery 객체를 생성하여 반환한다.
	 * 내부적으로 SqlQueryReader 객체를 사용하여 해당 XML 파일을 파싱한다.
	 * @param id
	 * @return
	 * @throws ApplicationException 해당 파일을 
	 */
	public SqlQuery getQuery(String id) throws SqlQueryException  {
		checkSqls();
		
		SqlStatement stmt = sqls.getStatement(id);
		if ( stmt == null ) {
			// 해당 Statement의 ID가 존재하지 않음
			throw new SqlQueryException(SqlQueryException.NO_QUERY_STATEMENT,path,id);
		}

		// SqlStatement가 참조하는 ResultMap이 있으면 찾아서 설정해준다.
		String resultMapId = stmt.getResultMapId();
		ResultMap resultMap = null;
		if ( resultMapId != null ) {
			resultMap = sqls.getResultMap(resultMapId);
		}
		SqlQuery query = new SqlQuery(stmt.getSqlList(),resultMap,id,path);
		
		return query;
	}
	
	/**
	 * 요청한 ID에 해당되는 ResultMap 객체를 반환한다.
	 * @param id
	 * @return
	 * @throws SqlQueryException
	 */
	public ResultMap getResultMap(String id) throws SqlQueryException {
		checkSqls();
		
		return sqls.getResultMap(id);
	}
	
	/**
	 * Sqls 객체가 만들어져 있는지 확인하고 없으면 Sqls 객체를 생성한다.
	 * @throws SqlQueryException
	 */
	private void checkSqls() throws SqlQueryException {
		if (sqls == null) {
			// SQL 파일 파싱
			try {
				sqls = SqlQueryReader.getReader().getQuery(path, classLoader);
			} catch(IOException ex) {
				// 해당 파일을 찾을 수 없는 경우
				throw new SqlQueryException(SqlQueryException.QUERY_FILE_NOTFOUND,path,ex);
			} catch(SAXException ex) {
				// 파싱중 오류가 발생한 경우
				throw  new SqlQueryException(SqlQueryException.QUERY_PARSING_ERROR,path,ex);
			}
		}
	}
}
