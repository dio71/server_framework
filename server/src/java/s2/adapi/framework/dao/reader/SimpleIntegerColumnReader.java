package s2.adapi.framework.dao.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SimpleIntegerColumnReader implements ColumnReader {

	public Object read(int index, ResultSet rs) throws SQLException {
		return rs.getInt(index);
	}

	public Object read(int index, CallableStatement cstmt)
			throws SQLException {
		return cstmt.getInt(index);
	}

}
