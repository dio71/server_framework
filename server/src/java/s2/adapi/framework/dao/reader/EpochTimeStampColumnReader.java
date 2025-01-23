package s2.adapi.framework.dao.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * {@link TimeStamp} 컬럼을 읽어서 1970.01.01 기준으로 milli-second 값으로 반환한다.
 * @author kimhd
 */
public class EpochTimeStampColumnReader implements ColumnReader {

	public Object read(int index, ResultSet rs) throws SQLException {
		Timestamp value = rs.getTimestamp(index);
		if (value != null) {
			return value.getTime();
		} else {
			return 0L;
		}
	}
	
	public Object read(int index, CallableStatement cstmt) throws SQLException {
		Timestamp value = cstmt.getTimestamp(index);
		if (value != null) {
			return value.getTime();
		} else {
			return 0L;
		}
	}
}
