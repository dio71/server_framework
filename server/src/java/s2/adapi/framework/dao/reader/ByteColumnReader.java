package s2.adapi.framework.dao.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *  {@link Byte}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 반환한다.<br>
 */
public class ByteColumnReader implements ColumnReader {
	/**
	 * {@link Byte}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 <br>
	 *  return 한다.
	 * @return {@link Byte}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch한 값.
	 * @see s2.adapi.framework.dao.reader.ColumnReader#read(int, java.sql.ResultSet)
	 */
	public Object read(int index, ResultSet rs) throws SQLException {
		byte value = rs.getByte(index);
		return Byte.valueOf(value); // new Byte(value);
	}
	
	public Object read(int index, CallableStatement cstmt) throws SQLException {
		byte value = cstmt.getByte(index);
		return Byte.valueOf(value); // new Byte(value);
	}
}
