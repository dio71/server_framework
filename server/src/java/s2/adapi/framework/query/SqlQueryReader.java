package s2.adapi.framework.query;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import s2.adapi.framework.query.element.Sqls;

/**
 * SQL Query 정의 파일의 클래스 경로로부터 해당 파일을 파싱하여 
 * 파싱된 결과 객체인 Sqls 객체를 반환해주는 기능을 제공하는 클래스이다.
 * 
 * @author 김형도
 * @since 4.0
 */
public class SqlQueryReader {

	private static final Logger log = LoggerFactory.getLogger(SqlQueryReader.class);
	
	private static SqlQueryReader reader = new SqlQueryReader();
	
	public static SqlQueryReader getReader() {
		return reader;
	}
	
    /**
     * <p>
     * 단일(Single) 인스턴스 패턴 구현을 위한 <code>private</code> 컨스트럭터(Constructor)
     * </p>
     */
	private SqlQueryReader() {
	}
	
	public Sqls getQuery(String path) throws IOException, SAXException {
		return getQuery(path,null);
	}

	/**
	 * Sql Query 정의 파일의 클래스 경로를 지정하면
	 * 해당되는 정의 파일을 파싱하여 Sqls 객체로 반환한다.
	 * @param path Sql Query 정의 파일의 클래스 경로
	 * @param loader 클래스 경로로부터 파일을 로딩하기 위하여 사용되는 클래스로더 객체, 
	 * null 이면 Thread의 ContextClassLoader가 사용된다.
	 * @return 파싱된 결과
	 */
	public Sqls getQuery(String path, ClassLoader loader) throws IOException, SAXException {
		if ( log.isDebugEnabled() ) {
			log.debug("parsing sql query file ["+path+"]...");
		}

		SqlQueryParser parser = new SqlQueryParser(loader);
		Sqls sqls = null;
		
		try {
			sqls = parser.parse(path, null);
			//System.out.println(sqls.toString());
			sqls.setSqlPath(path); // 파일 경로를 설정한다.
		} catch (SAXException e) {
			if ( log.isErrorEnabled()) {
				log.error("sql query file parsing error. ("+path+")",e);
			}
			throw e;
		}
		
		return sqls;
	}
}