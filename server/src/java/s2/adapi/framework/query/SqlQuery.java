package s2.adapi.framework.query;

import java.util.ArrayList;
import java.util.List;

import s2.adapi.framework.query.element.ResultMap;
import s2.adapi.framework.query.element.SqlParameter;
import s2.adapi.framework.query.element.SqlText;
import s2.adapi.framework.query.element.StaticSqlText;
import s2.adapi.framework.vo.ValueObject;

/**
 * PreparedStatement 또는 CallableStatement에서 요구하는 형태의 SQL 문장과 파라메터 목록을 가지고 있는
 * 객체이다. SQL 문장을 XML 파일로 정의하여 이를 SqlQueryPage 객체를 통하여 얻어오거나,
 * 생성자를 사용하여 직접 SqlQuery 객체를 생성하여 사용할 수 있다.
 * @author 김형도
 * @since 4.0
 */
public class SqlQuery {
	private List<SqlText> sqls = null;
	private ResultMap resultMap = null;
	private String name = null;
	private String path = null;
	
	/**
	 * SqlQuery 객체를 생성한다. SqlQueryPage에서 사용한다.
	 * @param sqls SqlText 목록
	 * @param resultMap ResultMap 객체 
	 * @param name query 이름
	 * @param path query가 정의된 파일의 경로
	 */
	public SqlQuery(List<SqlText> sqls, ResultMap resultMap, String name, String path) {
		// 입력으로 들어온 SqlText 리스트의 각 SqlText을 사용하여 새로운 리스트를 구성한다.
		// 입력으로 들어온 SqlText 리스트 자체는 SqlStatement의 것이므로 참조만 해야한다.
		this.sqls = new ArrayList<SqlText>(sqls);
		this.resultMap = resultMap;
		this.name = name;
		this.path = path;
	}

	/**
	 * inline 파라메터가 포함된 SQL 문장을 받아서 생성한다.
	 * <pre>
	 * SqlQuery query = new SqlQuery("select a, b, c from table where a = #name#","getname");
	 * </pre>
	 * @param rawStmt
	 * @param name Query의 이름(식별용)
	 */
	public SqlQuery(String rawStmt, String name) {
		this.sqls = new ArrayList<SqlText>();

		// SimpleSqlText를 만들어 추가한다.
		sqls.add(new StaticSqlText(rawStmt));
		
		this.resultMap = null;
		this.name = name;
		this.path = null;
	}
	
	public SqlQuery(String rawStmt) {
		this(rawStmt,null);
	}
	
	/**
	 * 주어진 SqlQuery 객체의 SqlText들과 resultMap들을 추가한다. advice와 definition들은 추가되지 않는다.
	 * @param query
	 */
	public void addQuery(SqlQuery query) {
		sqls.addAll(query.sqls);
		if (query.getResultMap() != null) {
			if (resultMap == null) {
				resultMap = new ResultMap();
			}
			resultMap.addResultMap(query.getResultMap());
		}
	}
	
	/**
	 * Query 에서 사용하는 ResultMap 객체를 지정한다.
	 * @param rmap
	 */
	public void setResultMap(ResultMap rmap) {
		resultMap = rmap;
	}
	
	/**
	 * 설정된 ResultMap 객체를 반환한다.
	 * @return
	 */
	public ResultMap getResultMap() {
		return resultMap;
	}
	
	public String getSqlName() {
		return name;
	}
	
	public String getFilePath() {
		return path;
	}
	
	public void appendSql(ValueObject params, StringBuilder sb, List<SqlParameter> sqlParams) {
		for(int i=0;i<sqls.size();i++) {
			sqls.get(i).appendSql(params,sb,sqlParams,-1);
		}
	}
}
