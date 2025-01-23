package s2.adapi.framework.dao.supporter;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Derby에서 지원해야 할 jdbc기능을 정의한다.
 * Connection instance를 통해 자신이 알맞은 instance인지 판단하며, 
 * </p>
 *
 * @author 최승일
 * @version $Revision: 1.1 $ $Date: 2009/12/30 08:20:21 $
 * @since 5.0
 */
public class DerbySupporter extends DbmsSupporter{
	
	private static final Logger log = LoggerFactory.getLogger(DerbySupporter.class);
	
	public DerbySupporter() {
		super("derby");
	}
	
	/**
	 * <p>
	 * {@link Connection} instance를 통해 자신이 알맞은 Dbms의 supporter인지 판단한다.
	 * 
	 */
	public boolean match(Connection conn) throws SQLException {
		DatabaseMetaData dbmeta = conn.getMetaData();
		if (dbmeta.getDatabaseProductName().toUpperCase().indexOf("DERBY")>-1) {
			if (log.isDebugEnabled()) {
				String drivername = dbmeta.getDriverName();
				log.debug("Derby JDBC Driver : "+drivername+" v"+dbmeta.getDriverMajorVersion()+"."+dbmeta.getDriverMinorVersion());
				log.debug("Derby Server Product : "+dbmeta.getDatabaseProductName()+" "+dbmeta.getDatabaseProductVersion());
				log.debug("Derby Connection URL : "+dbmeta.getURL()+" ("+dbmeta.getUserName()+")");
				log.debug("Supports Auto-Generated-Keys :"+dbmeta.supportsGetGeneratedKeys());
			}
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String generateTopSql(String fromSql, int topn) {
	    StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM (").append(fromSql);
        sb.append(") FETCH FIRST ").append(topn).append(" ROWS ONLY");
        return sb.toString();
	}

	@Override
	public String getDummyTableName() {
		return "SYSIBM.SYSDUMMY1";
	}

	public PreparedStatement preparePageStatement(Connection conn, String sql,
			List<Object> parameters, int offset, int maxCount,
			Map<String, Object> args) throws SQLException {
        
        if (log.isDebugEnabled()) {
            log.debug("prepare page query : offset="+offset+",max_cnt="+maxCount);
        }
        
        StringBuilder sb = new StringBuilder();

        sb.append("SELECT * FROM (");
        sb.append(sql);
        sb.append(") AS TEMP_");
        if (maxCount > 0 && offset > 0) {
            sb.append(" OFFSET ");
            sb.append(offset);
            sb.append(" ROWS FETCH NEXT ");
            sb.append(offset+maxCount);
            sb.append(" ROWS ONLY ");
        } else if (maxCount > 0){
            sb.append(" FETCH FIRST ");
            sb.append(offset+maxCount);
            sb.append(" ROW ONLY ");  // offset + max_cnt
        } else if (offset > 0) {
            sb.append(" OFFSET ");
            sb.append(offset);
            sb.append(" ROWS ");
        }
        
        PreparedStatement pstmt = conn.prepareStatement(sb.toString());
        setPreparedStatementParam(pstmt, parameters);
        
        return pstmt;
	}
	
}
