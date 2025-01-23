package s2.adapi.framework.query.element;

import java.util.ArrayList;
import java.util.List;

import s2.adapi.framework.vo.ValueObject;

/**
 * 동적인 SQL 문장을 표현하기 위한 SqlText 인터페이스의 확장이다.
 * @author 김형도
 * @since 4.0
 */
public abstract class DynamicSqlText implements SqlText {
	/**
	 * 하위 Sql 문장들을 저장한다.
	 */
	protected List<SqlText> children = new ArrayList<SqlText>();
	
	public void addSqlText(SqlText text) {
		children.add(text);
	}
	
	public List<SqlText> getSqlList()	{
		return children;
	}

	abstract public void appendSql(ValueObject params, StringBuilder sb, List<SqlParameter> sqlParams, 
			int iterationIdx);
}
