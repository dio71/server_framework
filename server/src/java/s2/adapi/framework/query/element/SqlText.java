package s2.adapi.framework.query.element;

import java.util.List;

import s2.adapi.framework.vo.ValueObject;

/**
 * SqlStatement를 구성하는 SQL 문장을 표현하기 위한 Interface 이다.
 * @author 김형도
 * @since 4.0
 */
public interface SqlText {

	/**
	 * 파싱된 SQL 문장을 StringBuilder에 append하고 SqlParameter 들은 sqlParams에 add한다.
	 * @param params
	 * @param sb SQL 문장을 여기에 계속 추가한다.
	 * @param sqlParams SqlParameter 객체를 여기에 계속 추가한다.
	 */
	public void appendSql(ValueObject params, StringBuilder sb, List<SqlParameter> sqlParams, 
			int iterationIdx);

}
