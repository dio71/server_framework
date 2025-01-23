package s2.adapi.framework.dao.reader;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p>
 *  {@link Date}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 반환한다.<br>
 * </p>
 *
 */
public class DateColumnReader implements ColumnReader {
	/**
	 * {@link Date}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 <br>
	 *  return 한다.
	 * @return {@link Date}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch한 값.
	 * @see s2.adapi.framework.dao.reader.ColumnReader#read(int, java.sql.ResultSet)
	 */
	public Object read(int index, ResultSet rs) throws SQLException {
		Date value = rs.getDate(index);
		return value;
	}
	
	public Object read(int index, CallableStatement cstmt) throws SQLException {
		Date value = cstmt.getDate(index);
		return value;
	}
}