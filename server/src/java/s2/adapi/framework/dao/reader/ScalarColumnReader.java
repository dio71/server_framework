package s2.adapi.framework.dao.reader;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * <p>
 * BigDecimal형태로 {@link java.sql.ResultSet}에서 데이터를 fetch한 후 BigDecimal의 Scale 정보를 기준으로
 * 다른 Java Type으로 변경한다.
 * </p>
 *
 */
public class ScalarColumnReader implements ColumnReader {

	public static final int MAX_LONG_PRECISION = 18;
	
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
            if (value.scale() == 0) {
            	if (value.precision() > MAX_LONG_PRECISION) {
            		retObj = value;
            	} else {
            		retObj = Long.valueOf(value.longValue());
            	}
            } else {
                retObj = Double.valueOf(value.doubleValue());
            }
        }
		return retObj;
	}
}
