package s2.adapi.framework.dao.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;


public class TimeStampColumnReader implements ColumnReader {

	public Object read(int index, ResultSet rs) throws SQLException {
		Timestamp value = rs.getTimestamp(index);
		return value;
	}
	
	public Object read(int index, CallableStatement cstmt) throws SQLException {
		Timestamp value = cstmt.getTimestamp(index);
		return value;
	}
}
