package s2.adapi.framework.dao.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

/**
 * <p>
 *  {@link Time}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 반환한다.<br>
 * </p>
 *
 */
public class TimeColumnReader implements ColumnReader {
	/**
	 * {@link Time}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 <br>
	 *  return 한다.
	 * @return {@link Time}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch한 값.
	 * @see s2.adapi.framework.dao.reader.ColumnReader#read(int, java.sql.ResultSet)
	 */
	public Object read(int index, ResultSet rs) throws SQLException {
		Time value = rs.getTime(index);
		return value;
	}
	
	public Object read(int index, CallableStatement cstmt) throws SQLException {
		Time value = cstmt.getTime(index);
		return value;
	}
}
