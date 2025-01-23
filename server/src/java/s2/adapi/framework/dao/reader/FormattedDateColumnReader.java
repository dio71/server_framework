package s2.adapi.framework.dao.reader;

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class FormattedDateColumnReader implements ColumnReader {
	
	private String format = null;
	
	public FormattedDateColumnReader() {
		format = "yyyyMMdd";
	}
	
	public FormattedDateColumnReader(String format) {
		this.format = format;
	}
	
	public Object read(int index, ResultSet rs) throws SQLException {
		Date value = rs.getDate(index);
		if (value != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.format(value);
		} else {
			return null;
		}
	}
	
	public Object read(int index, CallableStatement cstmt) throws SQLException {
		Date value = cstmt.getDate(index);
		if (value != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.format(value);
		} else {
			return null;
		}
	}
}
