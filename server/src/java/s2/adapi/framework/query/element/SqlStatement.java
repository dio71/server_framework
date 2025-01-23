package s2.adapi.framework.query.element;

import java.util.List;

/**
 * Sql Query 정의 XML 파일에서 하나의 sql 문장을 정의하는 노드인 &lt;statement&gt; 노드를 표현하는 객체이다.
 * @author 김형도
 * @since 4.0
 */
public class SqlStatement {
	private List<SqlText> sqls = null;
	private String id = null;
	private String resultMapId = null;
	
	public SqlStatement(String id, List<SqlText> sqls, String resultMapId, String advice) {
		this.id = id;
		this.sqls = sqls;
		this.resultMapId = resultMapId;
	}
	
	public String getId() {
		return id;
	}
	
	public String getResultMapId() {
		return resultMapId;
	}
	
	public List<SqlText> getSqlList()	{
		return sqls;
	}
	
	public String getRawText() {
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<sqls.size();i++) {
			sqls.get(i).appendSql(null,sb,null,-1);
		}
		
		return sb.toString();
	}
	
	public String toString() {
		return "[id="+id+",statement="+sqls+",resultmap="+resultMapId+"]";
	}

}
