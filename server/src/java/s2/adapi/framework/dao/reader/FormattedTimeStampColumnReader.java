package s2.adapi.framework.dao.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class FormattedTimeStampColumnReader implements ColumnReader {

	private String format = null;
	
	public FormattedTimeStampColumnReader() {
		format = "yyyyMMddHHmmssSSS";
	}
	
	public FormattedTimeStampColumnReader(String format) {
		this.format = format;
	}
	
	public Object read(int index, ResultSet rs) throws SQLException {
		Timestamp value = rs.getTimestamp(index);
		if (value != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.format(value);
		} else {
			return null;
		}
	}
	
	public Object read(int index, CallableStatement cstmt) throws SQLException {
		Timestamp value = cstmt.getTimestamp(index);
		if (value != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.format(value);
		} else {
			return null;
		}
	}
}
