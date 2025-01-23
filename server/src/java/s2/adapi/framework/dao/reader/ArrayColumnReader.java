package s2.adapi.framework.dao.reader;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArrayColumnReader implements ColumnReader {

	public Object read(int index, ResultSet rs) throws SQLException {
		Array array = rs.getArray(index);
		return readFromArray(array);
	}

	public Object read(int index, CallableStatement cstmt)
			throws SQLException {
		Array array = cstmt.getArray(index);
		return readFromArray(array);
	}

	private Object readFromArray(Array array) throws SQLException {
		if (array == null) {
			return null;
		}
		
		Object obj = array.getArray();
		//array.free(); // Oracle에서 AbstractMethodError 발생함.
		
		return obj;
	}
}
