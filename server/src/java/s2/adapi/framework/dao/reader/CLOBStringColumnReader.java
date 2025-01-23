package s2.adapi.framework.dao.reader;

import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  {@link Clob}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 char[]에 value를 담아서<br>
 *  return 한다.
 */
public class CLOBStringColumnReader implements ColumnReader {
	private static final Logger log = LoggerFactory.getLogger(CLOBStringColumnReader.class);
	
	/**
	 *  {@link Clob}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 String에 value를 담아서<br>
	 *  return 한다.
	 * @return {@link Clob}형태로 {@link java.sql.ResultSet}에서 데이터를 fetch하여 String에 value를 담은 
	 * @see s2.adapi.framework.dao.reader.ColumnReader#read(int, java.sql.ResultSet)
	 */
	public Object read(int index, ResultSet rs) throws SQLException {
		Object retObj = null;
		Clob value = rs.getClob(index);
		if(value != null) {
			retObj = value.getSubString(1L, (int)value.length());
		}
		if(log.isDebugEnabled()) log.debug("ResultSet CLOB Data : " + retObj);
		
		return retObj;
	}

	public Object read(int index, CallableStatement cstmt) throws SQLException {
		Object retObj = null;
		Clob value = cstmt.getClob(index);
		if(value != null) {
			retObj = value.getSubString(1L, (int)value.length());
		}
		if(log.isDebugEnabled()) log.debug("CallableStatement CLOB Data : " + retObj);
		
		return retObj;
	}
}
