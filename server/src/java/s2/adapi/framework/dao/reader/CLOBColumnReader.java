package s2.adapi.framework.dao.reader;

import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *  {@link Clob}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 char[]에 value를 담아서<br>
 *  return 한다.
 */
public class CLOBColumnReader implements ColumnReader {
	
	StreamReader streamReader = new StreamReader();
	/**
	 *  {@link Clob}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 byte[]에 value를 담아서<br>
	 *  return 한다.
	 * @return {@link Clob}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 byte[]에 value를 담은 
	 * @see s2.adapi.framework.dao.reader.ColumnReader#read(int, java.sql.ResultSet)
	 */
	public Object read(int index, ResultSet rs) throws SQLException {
		Reader reader = null;
		Clob value = rs.getClob(index);
		if (value != null) {
			reader = value.getCharacterStream();
		}
		return streamReader.read(reader);
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
