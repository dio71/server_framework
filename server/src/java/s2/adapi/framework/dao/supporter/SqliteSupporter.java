package s2.adapi.framework.dao.supporter;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.dao.reader.ColumnReader;

public class SqliteSupporter extends DbmsSupporter {

	private static final Logger log = LoggerFactory.getLogger(SqlServerSupporter.class);
	
	public SqliteSupporter() {
		super("sqlite");
	}
	
	public boolean match(Connection conn) throws SQLException {
		DatabaseMetaData dbmeta = conn.getMetaData();
		String drivername = dbmeta.getDriverName();
		if (drivername.toUpperCase().indexOf("SQLITE") > -1) {
			if (log.isDebugEnabled()) {
				log.debug("SqlLite JDBC Driver : "+drivername+" v"+dbmeta.getDriverMajorVersion()+"."+dbmeta.getDriverMinorVersion());
				log.debug("SqlLite Product : "+dbmeta.getDatabaseProductName()+" "+dbmeta.getDatabaseProductVersion());
				log.debug("SqlLite Connection URL : "+dbmeta.getURL());
				log.debug("Supports Auto-Generated-Keys :"+dbmeta.supportsGetGeneratedKeys());
			}
			return true;
		} else {
			return false;
		}
	}

	public ColumnReader getDefaultColumnReader(int type, String typeName) {
		// global에 정의되지 않았으므로 디폴트 ColumnReader 가져오기
		switch (type) {
			case java.sql.Types.CHAR :
			case java.sql.Types.VARCHAR :
			case java.sql.Types.LONGVARCHAR :
				return this.STRING;
			case java.sql.Types.NUMERIC :
			case java.sql.Types.DECIMAL :
				return this.SCALAR; // SimpleDouble
			case java.sql.Types.BIT :
				return this.BOOLEAN;
			case java.sql.Types.TINYINT :
				return this.BYTE;
			case java.sql.Types.SMALLINT :
				return this.SIMPLEINT;
			case java.sql.Types.INTEGER :
				return this.SIMPLEINT;
			case java.sql.Types.BIGINT : 
				return this.LONG;  // SimpleLong
			case java.sql.Types.REAL :
				return this.REAL;  // SimpleDouble
			case java.sql.Types.FLOAT :
			case java.sql.Types.DOUBLE :
				return this.DOUBLE;  // SimpleDouble
			case java.sql.Types.BINARY :
			case java.sql.Types.VARBINARY :
			case java.sql.Types.LONGVARBINARY :
			case java.sql.Types.BLOB :
				return this.BINARY_STREAM;
			case java.sql.Types.CLOB :
				return this.CHAR_STREAM;
			case java.sql.Types.DATE :
				return this.DATE;
			case java.sql.Types.TIME :
				return this.TIME;
			case java.sql.Types.TIMESTAMP :
				return this.TIMESTAMP;
			default :
				return this.OBJECT;
		}
	}
	/**
	 * 페이지 단위 조회를 위한 SQL 문장을 생성한다. 
	 * 
	 * @param conn
	 * @param sql SQL 원문장
	 * @param parameters 원 SQL 문장에 bind될 파라메터 리스트
	 * @param offset 건너뛸 건수
	 * @param maxCount 최대 조회 건수
	 * @param args 벤더별로 다른 parameter들에 대한 정의.
	 * @return
	 * @throws SQLException 
	 */
	public PreparedStatement preparePageStatement(Connection conn, String sql, List<Object> parameters, 
				int offset, int maxCount, Map<String,Object> args) throws SQLException {
		
		if (log.isDebugEnabled()) {
			log.debug("prepare page query : offset="+offset+",max_cnt="+maxCount);
		}
		
		// select * from TBJBD47 LIMIT 4,3
		StringBuilder sb = new StringBuilder();

        if (maxCount > 0) {
        	sb.append(sql);
        	sb.append(" LIMIT ");
        	
    		// 옵센만 단독으로  따로 지정할수 없다..
        	if (offset > 0) {
        		sb.append(offset+",");
            }
        	sb.append(maxCount);
        	
        } else {
        	sb.append(sql);
        }
        
		PreparedStatement pstmt = conn.prepareStatement(sb.toString());
		setPreparedStatementParam(pstmt, parameters);
		return pstmt;
	}

	public String getDummyTableName() {
		return "";
	}
	
	public String generateTopSql(String fromSql, int topn) {
		StringBuilder sb = new StringBuilder();

		sb.append(fromSql);
		sb.append(" LIMIT ").append(topn);
		
		return sb.toString();
	}
}
