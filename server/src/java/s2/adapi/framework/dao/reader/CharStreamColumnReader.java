package s2.adapi.framework.dao.reader;

import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *  char[]형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 반환한다.<br>
 */
public class CharStreamColumnReader implements ColumnReader {
	StreamReader streamReader = new StreamReader();
	/**
	 * char[]형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 <br>
	 *  return 한다.
	 * @return char[] 형태로 {@link java.sql.ResultSet}에서 데이터를 fetch한 값.
	 * @see s2.adapi.framework.dao.reader.ColumnReader#read(int, java.sql.ResultSet)
	 */
	public Object read(int index, ResultSet rs) throws SQLException {
		Reader reader = rs.getCharacterStream(index);
		return this.streamReader.read(reader);
	}
	
	public Object read(int index, CallableStatement cstmt) throws SQLException {
		Reader reader = null;
		Clob value = cstmt.getClob(index);
		if (value != null) {
			reader = value.getCharacterStream();
		}
		return streamReader.read(reader);
	}
}