package s2.adapi.framework.dao.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *  {@link Boolean}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 반환한다.<br>
 */
public class BooleanColumnReader implements ColumnReader {
	/**
	 * {@link Boolean}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 <br>
	 *  return 한다.
	 * @return {@link Boolean}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch한 값.
	 * @see s2.adapi.framework.dao.reader.ColumnReader#read(int, java.sql.ResultSet)
	 */
	public Object read(int index, ResultSet rs) throws SQLException {
		boolean value = rs.getBoolean(index);
		return Boolean.valueOf(value);
	}
	
	public Object read(int index, CallableStatement cstmt) throws SQLException {
		boolean value = cstmt.getBoolean(index);
		return Boolean.valueOf(value);
	}
}
