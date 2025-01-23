package s2.adapi.framework.dao.reader;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class FormattedNumberColumnReader implements ColumnReader {

	private String format = null;
	
	public FormattedNumberColumnReader() {
		format = "#.#";
	}
	
	public FormattedNumberColumnReader(String format) {
		this.format = format;
	}
	
	public Object read(int index, ResultSet rs) throws SQLException {
		BigDecimal value = rs.getBigDecimal(index);
		if (value != null) {
			DecimalFormat df = new DecimalFormat(format);
			return df.format(value);
		} else {
			return null;
		}
	}
	
	public Object read(int index, CallableStatement cstmt) throws SQLException {
		BigDecimal value = cstmt.getBigDecimal(index);
		if (value != null) {
			DecimalFormat df = new DecimalFormat(format);
			return df.format(value);
		} else {
			return null;
		}
	}

}
