package s2.adapi.framework.dao.reader;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * BigDecimal형태로 {@link java.sql.ResultSet}에서 데이터를 fetch한다.
 */
public class BigDecimalColumnReader implements ColumnReader {
	/**
	 * {@link java.sql.ResultSet}에서 index에 해당하는 row의 data를 {@link java.math.BigDecimal}<br>
	 * 형태로 반환한다. 
	 * @return {@link java.math.BigDecimal} data를  {@link java.sql.ResultSet}에서 fetch한 값 
	 * @see s2.adapi.framework.dao.reader.ColumnReader#read(int, java.sql.ResultSet)
	 */
	public Object read(int index, ResultSet rs) throws SQLException {
		BigDecimal value = rs.getBigDecimal(index);
		return value;
	}
	
	public Object read(int index, CallableStatement cstmt) throws SQLException {
		BigDecimal value = cstmt.getBigDecimal(index);
		return value;
	}
}
