package s2.adapi.framework.dao.supporter;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.sql.BatchUpdateException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.config.KeyConfig;
import s2.adapi.framework.dao.SqlQueryException;
import s2.adapi.framework.dao.reader.ArrayColumnReader;
import s2.adapi.framework.dao.reader.BLOBColumnReader;
import s2.adapi.framework.dao.reader.BigDecimalColumnReader;
import s2.adapi.framework.dao.reader.BinaryStreamColumnReader;
import s2.adapi.framework.dao.reader.BooleanColumnReader;
import s2.adapi.framework.dao.reader.ByteColumnReader;
import s2.adapi.framework.dao.reader.CLOBColumnReader;
import s2.adapi.framework.dao.reader.CharStreamColumnReader;
import s2.adapi.framework.dao.reader.ColumnReader;
import s2.adapi.framework.dao.reader.DateColumnReader;
import s2.adapi.framework.dao.reader.DoubleColumnReader;
import s2.adapi.framework.dao.reader.IntColumnReader;
import s2.adapi.framework.dao.reader.LongColumnReader;
import s2.adapi.framework.dao.reader.ObjectColumnReader;
import s2.adapi.framework.dao.reader.RealColumnReader;
import s2.adapi.framework.dao.reader.ScalarColumnReader;
import s2.adapi.framework.dao.reader.ShortColumnReader;
import s2.adapi.framework.dao.reader.SimpleIntegerColumnReader;
import s2.adapi.framework.dao.reader.StringColumnReader;
import s2.adapi.framework.dao.reader.TimeColumnReader;
import s2.adapi.framework.dao.reader.TimeStampColumnReader;
import s2.adapi.framework.dao.types.JdbcNull;
import s2.adapi.framework.dao.types.JdbcXML;
import s2.adapi.framework.query.element.ResultMap;
import s2.adapi.framework.query.element.ResultMapItem;
/**
 * <p>
 * 특정 Dbms를 지원하는 DbmsSupporter의 abstract class.
 * {@link Connection} instance로 자신이 알맞은 supporter인지 판단하고, 판단이 되면 데이터 fetch시 {@link ResultSet}<br>
 * 을 통해 각 Column별로 알맞은 Column Reading방식을 제공한다. LOB이나 database에 의존적인 type의 Column속성이 있다면, 해당 벤더의<br>
 * JDBC Type Mapping문서를 참조하여 createColumnReader를 구현해야 한다.
 * </p>
 *
 * @author 최승일
 * @version $Revision: 1.1 $ $Date: 2006/09/26 08:20:21 $
 * @since 4.0
 */
public abstract class DbmsSupporter {
	
	private static final Logger log = LoggerFactory.getLogger(DbmsSupporter.class);
	
	private static DbmsSupporter[] supporters = null;
	
	/**
	 * 실행 대상 DB 명칭
	 */
	private String vendorName = null;
	
	/**
	 * 대상 DB용 Global ResultMap을 저장함. 
	 */
	protected ResultMap globalResultMap = null;
	
	// 사용되는 ColumnReader 클래스들을 생성해 놓는다.
	protected final ColumnReader BINARY_STREAM = new BinaryStreamColumnReader();
	protected final ColumnReader CHAR_STREAM = new CharStreamColumnReader();
	protected final ColumnReader DATE = new DateColumnReader();
	protected final ColumnReader INT = new IntColumnReader();
	protected final ColumnReader STRING = new StringColumnReader();
	protected final ColumnReader BOOLEAN = new BooleanColumnReader();
	protected final ColumnReader BIG_DECIMAL = new BigDecimalColumnReader();
	protected final ColumnReader BYTE = new ByteColumnReader();
	protected final ColumnReader SHORT = new ShortColumnReader();
	protected final ColumnReader LONG = new LongColumnReader();
	protected final ColumnReader REAL = new RealColumnReader();
	protected final ColumnReader DOUBLE = new DoubleColumnReader();
	protected final ColumnReader TIME = new TimeColumnReader();
	protected final ColumnReader TIMESTAMP = new TimeStampColumnReader();
	protected final ColumnReader OBJECT = new ObjectColumnReader();
	protected final ColumnReader CLOB = new CLOBColumnReader();
	protected final ColumnReader BLOB = new BLOBColumnReader();
	protected final ColumnReader SCALAR = new ScalarColumnReader();
	protected final ColumnReader SIMPLEINT = new SimpleIntegerColumnReader();
	protected final ColumnReader ARRAY = new ArrayColumnReader();
	
	static {
		createSupporters();
	}
	
	private static void createSupporters() {
		List<DbmsSupporter> list = new ArrayList<DbmsSupporter>();
		
		try {
			list.add(new MySqlSupporter());
		} catch (Throwable e) {
			if ( log.isErrorEnabled() ) {
				log.error("MySqlSupport initialization failed. ("+e.toString()+")");
			}
		}
		
		try {
			list.add(new PostgresSupporter());
		} catch (Throwable e) {
			if ( log.isErrorEnabled() ) {
				log.error("PostgresSupporter initialization failed. ("+e.toString()+")");
			}
		}
		
		try {
			list.add(new SqlServerSupporter());
		} catch (Throwable e) {
			if ( log.isErrorEnabled() ) {
				log.error("SqlServerSupporter initialization failed. ("+e.toString()+")");
			}
		}
		
		try {
			list.add(new SqliteSupporter());
		} catch (Throwable e) {
			if ( log.isErrorEnabled() ) {
				log.error("SqliteSupporter initialization failed. ("+e.toString()+")");
			}
		}
		
		try {
            list.add(new DerbySupporter());
        } catch (Throwable e) {
            if ( log.isErrorEnabled() ) {
                log.error("DerbySupporter initialization failed. ("+e.toString()+")");
            }
        }
        
        try {
			list.add(new AccessSupporter());
		} catch (Throwable e) {
			if ( log.isErrorEnabled() ) {
				log.error("AccessSupporter initialization failed. ("+e.toString()+")");
			}
		}
		
		supporters = list.toArray(new DbmsSupporter[list.size()]);
	}
	
	/**
	 * 생성자이다. 하위 클래스에서 해당 Vendor 명을 넣어서 호출한다.
	 * @param vendor
	 */
	protected DbmsSupporter(String vendor) {
		vendorName = vendor;
        
        // global column reader mapping 정보를 저장해놓는다.
        globalResultMap = GlobalSqlMap.getInstance().getResultMap(vendor);

	}
	
	public String getVendorName() {
		return vendorName;
	}
	
	/**
	 * {@link Connection}을 통해 {@link DbmsSupporter}의 하위구현 class가 해당 {@link Connection}을 제공한 벤더의 dbms를<br>
	 * 지원가능한지 확인한다. {@link Connection}을 통해 {@link java.sql.ResultSetMetaData}를 얻어와서 판별하도록 구현한다. 
	 * @param conn
	 * @return {@link Connection}에 해당하는 Dbms를 support 가능한지의 여부.
	 * @throws SQLException
	 */
	public abstract boolean match(Connection conn) throws SQLException;
	
	/**
	 * 더미 테이블 명을 반환한다. (예 : oracle은 "DUAL")
	 * @return
	 */
	public abstract String getDummyTableName();
	
	/**
	 * 첫번째 topn 개의 row 만 조회하는 SQL 문장을 생성한다.
	 * @param fromSql 조회대상 테이블 명 또는 SQL문
	 * @param topn 조회 건
	 * @return
	 */
	public abstract String generateTopSql(String fromSql, int topn);
	
	/**
	 * Dbms마다 paging용 sql과 PreparedStatement에 설정해야 하는 값도 다르다. parameters에 설정된 key:value들은 Dbms<br>
	 * 마다 다른 parameter들의 규약이기 때문에 해당 벤더의 supporter를 구현하는 경우에는 parameters에 설정될 값들에 대해서 정의해 주어야 한다.
	 * @param conn
	 * @param sql 
	 * @param parameters 
	 * @param offset 
	 * @param maxCount 
	 * @param args 벤더별로 다른 parameter들에 대한 정의.
	 * @return
	 * @throws SQLException
	 */
	public abstract PreparedStatement preparePageStatement(Connection conn, String sql, 
				List<Object> parameters, int offset, int maxCount, Map<String,Object> args) throws SQLException;
	
	public PreparedStatement prepareStatement(Connection conn, String sql, List<?> parameters) throws SQLException {
		
		PreparedStatement pstmt = conn.prepareStatement(sql);
		setPreparedStatementParam(pstmt, parameters);
		
		return pstmt;
	}
	
	public void setPreparedStatementParam(PreparedStatement pstmt, List<?> parameters) throws SQLException{
		if (parameters == null) {
			return;
		}
		
		for (int parameterIndex=0; parameterIndex<parameters.size(); parameterIndex++){
			Object obj = null;
			try {
				obj = parameters.get(parameterIndex);
				if(obj instanceof char[]){
					char[] value = (char[])obj;
					StringReader sr = new StringReader(String.valueOf(value));
					pstmt.setCharacterStream(parameterIndex+1, sr, value.length);
				} 
				else if(obj instanceof byte[]){
					byte[] value = (byte[])obj;
					ByteArrayInputStream baos = new ByteArrayInputStream(value);
					pstmt.setBinaryStream(parameterIndex+1, baos, value.length);
				} 
				else if (obj instanceof KeyConfig) {
					String keyValue = ((KeyConfig)obj).keyValue();
					setDefaultPreparedStatementParam(pstmt,parameterIndex+1,keyValue);
				}
				else {
					// default set
					setDefaultPreparedStatementParam(pstmt,parameterIndex+1,obj);
				}
			} 
			catch (SQLException ex) {
                if (log.isErrorEnabled()) {
                    log.error("Set SQL parameter failed...[" + parameterIndex + "," + obj + "," + ((obj==null)?"null":obj.getClass().getName()) + "]",ex);
                }
                throw ex;
			}
		}
	}
	
	protected void setDefaultPreparedStatementParam(PreparedStatement pstmt, int idx, Object param) 
			throws SQLException {

		if (param == null) {
			pstmt.setNull(idx,Types.VARCHAR);
		} else if (param instanceof JdbcNull) {
			pstmt.setNull(idx,((JdbcNull)param).getType());
		} else if (param instanceof String) {
			pstmt.setString(idx,(String)param);
		} else if (param instanceof Integer) {
			pstmt.setInt(idx,((Integer)param).intValue());
		} else if (param instanceof Long) {
			pstmt.setLong(idx,((Long)param).longValue());
		} else if (param instanceof Double) {
			pstmt.setDouble(idx,((Double)param).doubleValue());
		} else if (param instanceof Float) {
			pstmt.setFloat(idx,((Float)param).floatValue());
		} else if (param instanceof Short) {
			pstmt.setShort(idx,((Short)param).shortValue());
		} else if (param instanceof Byte) {
			pstmt.setDouble(idx,((Byte)param).byteValue());
		} else if (param instanceof Double) {
			pstmt.setDouble(idx,((Double)param).doubleValue());
		} else if (param instanceof Character) {
			pstmt.setString(idx,String.valueOf(param));
		} else if (param instanceof Time) {
			pstmt.setTime(idx, (Time)param);
		} else if (param instanceof Date) {
			pstmt.setDate(idx, (Date)param);
		} else if (param instanceof Timestamp) {
			pstmt.setTimestamp(idx, (Timestamp)param);
		} else if (param instanceof JdbcXML) {
			String paramStr = ((JdbcXML)param).toString();
			if (paramStr == null) {
				pstmt.setNull(idx,Types.VARCHAR);
			} else {
				pstmt.setString(idx,((JdbcXML)param).toString());
			}
		} else {
			pstmt.setObject(idx, param);
		}
	}
	
	public void setCallableStatementParam(CallableStatement cstmt, int idx, Object param) 
			throws SQLException {
		setDefaultCallableStatementParam(cstmt,idx,param);
	}
	
	protected void setDefaultCallableStatementParam(CallableStatement cstmt, int idx, Object param) 
			throws SQLException {
		if (param == null) {
			cstmt.setNull(idx,Types.VARCHAR);
		} else if (param instanceof JdbcNull) {
			cstmt.setNull(idx,((JdbcNull)param).getType());
		} else {
			cstmt.setObject(idx,param);
		}
	}
	
	public ColumnReader getColumnReader(int type, String typeName) {
		// global 먼저 확인
		if (globalResultMap != null) {
			ResultMapItem item = globalResultMap.getItem(type);
			if (item != null) {
				return item.getReader();
			}
		}
		
		return getDefaultColumnReader(type,typeName);
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
				return this.SCALAR;
			case java.sql.Types.BIT :
				return this.BOOLEAN;
			case java.sql.Types.TINYINT :
				return this.BYTE;
			case java.sql.Types.SMALLINT :
				return this.SHORT;
			case java.sql.Types.INTEGER :
				return this.INT;
			case java.sql.Types.BIGINT : 
				return this.LONG;
			case java.sql.Types.REAL :
				return this.REAL;
			case java.sql.Types.FLOAT :
			case java.sql.Types.DOUBLE :
				return this.DOUBLE;
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
			case java.sql.Types.ARRAY:
				return this.ARRAY;
			default :
				return this.OBJECT;
		}
	}
	
	public static DbmsSupporter getInstance(Connection conn) throws SQLException {
		for (int i = 0; i < supporters.length; i++) {
			DbmsSupporter supporter = supporters[i];
			if(supporter.match(conn)) return supporter;
		}
		return null;
	}
	
	/**
	 * DBMS 별로 차이 있는 SQLException을 처리하여 SqlQueryException으로 반환한다.
	 * @param sqlex
	 * @return SqlQueryException
	 */
	public SqlQueryException handleSqlException(SQLException sqlex) {
		if (sqlex instanceof BatchUpdateException) {
			SQLException nex = sqlex.getNextException();
	        if (nex == null) {
	        	nex = sqlex;
	        }
	        return new SqlQueryException(nex);
		} else {
			return new SqlQueryException(sqlex);
		}
		
	}
}