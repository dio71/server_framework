package s2.adapi.framework.dao.types;

import java.sql.Types;

/**
 * Jdbc에서 Null 객체인 경우에도 그 sql type을 필요로 하기때문에
 * 이를 표현하기 위한 클래스이다.
 * 
 * @author 김형도
 * @since 4.0
 */
public class JdbcNull {

	private int jdbcType = Types.VARCHAR;
	
	public JdbcNull(int type) {
		jdbcType = type;
	}
	
	public int getType() {
		return jdbcType;
	}
	
	public String toString() {
		return null;
	}
}
