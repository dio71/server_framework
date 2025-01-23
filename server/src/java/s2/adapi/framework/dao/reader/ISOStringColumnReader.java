package s2.adapi.framework.dao.reader;

import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DBMS의 charset을 ISO8859_1 과 같이 1 byte charset으로 지정하고 여기에
 * 한글을 저장한 경우 JDBC에서 조회를 해오면 한글이 깨지게 된다.
 * 이 클래스는 경우 강제적으로 charset 변환을 수행하여 한글이 깨지지 않고 읽을 수 있도록 하였다.
 */
public class ISOStringColumnReader implements ColumnReader {
	/**
	 * {@link String}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 <br>
	 *  return 한다.
	 * @return {@link String}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch한 값.
	 * @see s2.adapi.framework.dao.reader.ColumnReader#read(int, java.sql.ResultSet)
	 */
	public Object read(int index, ResultSet rs) throws SQLException {
		String value = null;
		try {
			value = rs.getString(index);
			if (value != null) {
				value = new String(value.getBytes("8859_1"),"euc-kr");
			}
		} catch (UnsupportedEncodingException ex) {
			throw (SQLException)new SQLException("unsupported charset.").initCause(ex);
		}
		return value;
	}
	
	public Object read(int index, CallableStatement cstmt) throws SQLException {
		String value  = null;
		try {
			value = cstmt.getString(index);
			if (value != null) {
				value = new String(value.getBytes("8859_1"),"euc-kr");
			}
		} catch (UnsupportedEncodingException ex) {
			throw (SQLException)new SQLException("unsupported charset.").initCause(ex);
		};
		return value;
	}
}