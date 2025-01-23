package s2.adapi.framework.dao.reader;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <p>
 *  {@link Double}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 반환한다.<br>
 * </p>
 *
 */
public class DoubleColumnReader implements ColumnReader {
	/**
	 * {@link Double}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 <br>
	 *  return 한다.
	 * @return {@link Double}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch한 값.
	 * @see s2.adapi.framework.dao.reader.ColumnReader#read(int, java.sql.ResultSet)
	 */
	public Object read(int index, ResultSet rs) throws SQLException {
		BigDecimal value = rs.getBigDecimal(index);
		return parse(value);
	}

	public Object read(int index, CallableStatement cstmt) throws SQLException {
		BigDecimal value = cstmt.getBigDecimal(index);
		return parse(value);
	}
	
	private Object parse(BigDecimal value) {
		Object retObj = null;
        if (value == null) {
            retObj = null;
        } else {
            retObj = Double.valueOf(value.doubleValue());
        }
		return retObj;
	}
}
