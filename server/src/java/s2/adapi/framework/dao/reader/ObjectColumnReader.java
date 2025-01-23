package s2.adapi.framework.dao.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p>
 *  {@link Object}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 반환한다.<br>
 * </p>
 *
 * @author 최승일
 * @version $Revision: 1.1 $ $Date: 2006/09/26 08:20:12 $
 * @since 4.0
 */
public class ObjectColumnReader implements ColumnReader {
	/**
	 * {@link Object}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 <br>
	 *  return 한다.
	 * @return {@link Double}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch한 값.
	 * @see s2.adapi.framework.dao.reader.ColumnReader#read(int, java.sql.ResultSet)
	 */
	public Object read(int index, ResultSet rs) throws SQLException {
		Object value = rs.getObject(index);
		return value;
	}

	public Object read(int index, CallableStatement cstmt) throws SQLException {
		Object value = cstmt.getObject(index);
		return value;
	}
}
