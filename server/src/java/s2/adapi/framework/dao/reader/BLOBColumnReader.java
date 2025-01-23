package s2.adapi.framework.dao.reader;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *  {@link Blob}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 byte[]에 value를 담아서<br>
 *  return 한다.
 */
public class BLOBColumnReader implements ColumnReader {
	private StreamReader streamReader = new StreamReader();
	/**
	 *  {@link Blob}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 byte[]에 value를 담아서<br>
	 *  return 한다.
	 * @return {@link Blob}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 byte[]에 value를 담은 
	 * @see s2.adapi.framework.dao.reader.ColumnReader#read(int, java.sql.ResultSet)
	 */
	public Object read(int index, ResultSet rs) throws SQLException {
		InputStream is = null;
		Blob value = rs.getBlob(index);
		if (value != null) {
			is = value.getBinaryStream();
		}
		return streamReader.read(is);
	}
	
	public Object read(int index, CallableStatement cstmt) throws SQLException {
		InputStream is = null;
		Blob value = cstmt.getBlob(index);
		if (value != null) {
			is = value.getBinaryStream();
		}
		return streamReader.read(is);
	}
}
