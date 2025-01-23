package s2.adapi.framework.query.element;

import java.util.List;

import s2.adapi.framework.vo.ValueObject;

/**
 * 정적으로 고정된 SQL 문을 표현하는 클래스이다. 내부적으로 SqlText의 배열을 가지고 있다.
 * @author 김형도
 * @since 4.0
 */
public class StaticSqlText implements SqlText {

	protected List<SqlText> subSqls = null;
	private String rawText = null;
	
	public StaticSqlText(String rawText) {
		this.rawText = rawText;
		SqlTextParser parser = new SqlTextParser();
		subSqls = parser.parse(rawText);
	}
	
	public void appendSql(ValueObject params, StringBuilder sb, List<SqlParameter> sqlParams, 
			int iterationIdx) {
		for(int i=0;i<subSqls.size();i++) {
			subSqls.get(i).appendSql(params,sb,sqlParams,iterationIdx);
		}
	}
	
	public String toString() {
		return "[text="+rawText+"]";
	}

}
