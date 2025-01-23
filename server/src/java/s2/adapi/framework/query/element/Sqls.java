package s2.adapi.framework.query.element;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Sql Query 정의 XML 파일에서 root 노드인 &lt;sqls&gt; 노드를 표현하는 객체이다.
 * 하위 노드인 &lt;statement&gt;를 표현하는 SqlStatement 객체를 맵으로 가진다.
 * @author 김형도
 * @since 4.0
 */
public class Sqls {
	
	private String xmlPath = null;
	
	private Map<String,SqlStatement> stmtMap = new HashMap<String,SqlStatement>();

	private Map<String,ResultMap> resultMaps = new HashMap<String,ResultMap>();
	
	public void setSqlPath(String path) {
		xmlPath = path;
	}
	
	public String getFilePath() {
		return xmlPath;
	}
	
	public void addSqls(Sqls sqls) {
		// resultmap 추가
		Set<String> keys = sqls.getResultMapKeySet();
		Iterator<String> itor = keys.iterator();
		while(itor.hasNext()) {
			String id = itor.next();
			addResultMap(id, sqls.getResultMap(id));
		}
		
		// statement 추가
		keys = sqls.getStatementKeySet();
		itor = keys.iterator();
		while(itor.hasNext()) {
			String id = itor.next();
			addStatement(sqls.getStatement(id));
		}
	}
	
	public void addStatement(SqlStatement stmt) {
		stmtMap.put(stmt.getId(), stmt);
	}
	
	public SqlStatement getStatement(String id) {
		return stmtMap.get(id);
	}
	
	public Set<String> getStatementKeySet()	 {
		return stmtMap.keySet();
	}
	
	public void addResultMap(String id, ResultMap rmap) {
		if (id!=null && rmap!=null) {
			if (resultMaps == null) {
				resultMaps = new HashMap<String,ResultMap>();
			}
			resultMaps.put(id,rmap);
		}
	}
	
	public ResultMap getResultMap(String id) {
		if (resultMaps == null) {
			return null;
		}
		else {
			return resultMaps.get(id);
		}
	}
	
	public Set<String> getResultMapKeySet() {
		if (resultMaps == null) {
			return new HashSet<String>();
		}
		else {
			return resultMaps.keySet();
		}
	}
	
	public String toString() {
		return "{" + stmtMap.toString() + "," + 
			resultMaps.toString() + "}";
	}
}
