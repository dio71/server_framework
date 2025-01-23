package s2.adapi.framework.dao.reader;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * byte[] 형태로 {@link java.sql.ResultSet}에서 데이터를 fetch한다.
 */
public class BinaryStreamColumnReader implements ColumnReader{
	
	private StreamReader streamReader = new StreamReader();
	
	/**
	 * {@link java.sql.ResultSet}에서 index에 해당하는 row의 data를 byte[]<br>
	 * 형태로 반환한다. 
	 * @return byte[] data를  {@link java.sql.ResultSet}에서 fetch한 값 
	 * @see s2.adapi.framework.dao.reader.ColumnReader#read(int, java.sql.ResultSet)
	 */
	public Object read(int index, ResultSet rs) throws SQLException {
		InputStream is = rs.getBinaryStream(index);
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
