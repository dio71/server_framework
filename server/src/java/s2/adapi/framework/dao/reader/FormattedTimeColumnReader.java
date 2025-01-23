package s2.adapi.framework.dao.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;

public class FormattedTimeColumnReader implements ColumnReader {

	private String format = null;
	
	public FormattedTimeColumnReader() {
		format = "HHmmss";
	}
	
	public FormattedTimeColumnReader(String format) {
		this.format = format;
	}
	
	public Object read(int index, ResultSet rs) throws SQLException {
		Time value = rs.getTime(index);
		if (value != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.format(value);
		} else {
			return null;
		}
	}
	
	public Object read(int index, CallableStatement cstmt) throws SQLException {
		Time value = cstmt.getTime(index);
		if (value != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.format(value);
		} else {
			return null;
		}
	}

}
