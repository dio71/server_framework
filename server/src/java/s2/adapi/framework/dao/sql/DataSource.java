package s2.adapi.framework.dao.sql;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.config.Configurator;
import s2.adapi.framework.config.ConfiguratorException;
import s2.adapi.framework.config.ConfiguratorFactory;
import s2.adapi.framework.context.ContextManager;
import s2.adapi.framework.util.PropertyHelper;

/**
 * <p>
 * WAS가 제공하는 Datasource와 Framework 가 제공하는 Datasource를 하나로 통합하는 역활을 한다.
 * ServiceContainer에서 사용할 수 있는 형태로 만들었다.
 * </p>
 * @author 김형도
 * @since 4.0
 */
public class DataSource implements javax.sql.DataSource {

    private static final Logger log = LoggerFactory.getLogger(DataSource.class);
    
    private static final String JNDI_ENV_PREFIX = "java:comp/env/";
    
	private javax.sql.DataSource ds = null; // wrapped datasource

	private String datasourceName = null;
	
	public void setDsn(String dsn) {
		datasourceName = dsn;
	}
	
	public String getDsn() {
		return datasourceName;
	}
	
	//
	// jakarta.sql.DataSource 의 implementation이다.
	// 내부 Datasource 객체의 각 해당 메소드를 다시 호출해준다.
	//
	public Connection getConnection() throws SQLException {
		if ( ds == null ) {
			ds = getDataSource();
		}
		
		long stime = System.currentTimeMillis();
		Connection con = ds.getConnection();
		long rtime = System.currentTimeMillis() - stime;
		
		if (log.isDebugEnabled() && rtime > 100) {
			log.debug("getConnection() takes "+ rtime + " msec.");
		}
		
		return con;
	}

	public Connection getConnection(String username, String password)
			throws SQLException {
		if ( ds == null ) {
			ds = getDataSource();
		}
		
		long stime = System.currentTimeMillis();
		Connection con = ds.getConnection();
		long rtime = System.currentTimeMillis() - stime;
		
		if (log.isDebugEnabled() && rtime > 100) {
			log.debug("getConnection() takes "+ rtime + " msec.");
		}
		
		return con;
	}

	public PrintWriter getLogWriter() throws SQLException {
		if ( ds == null ) {
			ds = getDataSource();
		}
		
		return ds.getLogWriter();
	}

	public int getLoginTimeout() throws SQLException {
		if ( ds == null ) {
			ds = getDataSource();
		}
		
		return ds.getLoginTimeout();
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		if ( ds == null ) {
			ds = getDataSource();
		}
		
		ds.setLogWriter(out);

	}

	public void setLoginTimeout(int seconds) throws SQLException {
		if ( ds == null ) {
			ds = getDataSource();
		}
		
		ds.setLoginTimeout(seconds);
	}

	//
	// 실제 사용할 Datasource를 생성하는 내부 루틴들
	//
	
	private javax.sql.DataSource getDataSource() throws SQLException {
		
		javax.sql.DataSource returnDataSource = null;
		
        if (ContextManager.getContextType() == ContextManager.J2EE_CONTEXT) {
        	log.debug("datasource [" + datasourceName + "] is requested in WebApplication context");
        	
        	returnDataSource = getDataSourceWebApplication();
        }
        else {
        	log.debug("datasource [" + datasourceName + "] is requested in Platform context");
        	
        	returnDataSource = getDataSourcePlatform();
        }

        return returnDataSource;
	}
	
	private javax.sql.DataSource getDataSourceWebApplication() throws SQLException {
		
		try {
        	InitialContext cxt = new InitialContext();
        	String dsn = datasourceName;
        	if (!dsn.startsWith(JNDI_ENV_PREFIX)) {
            	dsn = JNDI_ENV_PREFIX + dsn;
            }
        	return (javax.sql.DataSource)cxt.lookup(dsn);

		} 
        catch (NamingException ex) {
			throw new SQLException("cannot lookup datasource("+datasourceName+").",ex);
		} 
	}
	
	/**
	 * Batch 에서 사용할 수 있는 framework platform datasource 를 반환한다.
	 * Batch 환경에서 사용하는 DataSource는 기본 기능만 제공하는 SimpleDataSource이다.
	 * @return
	 * @throws SQLException
	 */
	private javax.sql.DataSource getDataSourcePlatform() throws SQLException {

		try {
			Properties props = getProperties(datasourceName);
			return new SimpleDataSource(datasourceName, props);
		} 
		catch (ConfiguratorException ex) {
			throw new SQLException("db configuration failed.", ex);
		}
	}
	
    /**
     * <p>
     * Datasource의 속성 값이 담긴 프로퍼티를 리턴한다.
     * 속성 값은 Datasource의 속성파일에서 정의된 내용이다.
     * </p>
     * @param dsn 얻고자하는 datasource 명
     * @return dsn의 프로퍼티
     * @throws ConfiguratorException 
     * @throws SQLException 
     */
    public static Properties getProperties(String dsn) throws ConfiguratorException, SQLException  {
	    // datasource 프로퍼티를 읽기 위한 PropertyHelpr 객체 생성
		Configurator config = ConfiguratorFactory.getConfigurator();
		
		String dbfileName = config.getPath("s2adapi.jdbc.config", "batch_db.properties");
        
		if (dbfileName == null) {
			throw new SQLException("db config file not found : " + dbfileName);
		}
		
    	PropertyHelper ph = new PropertyHelper(dbfileName);
        
        return ph.getProperties(dsn);    	
    }
    
	public String toString() {
		return datasourceName;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLException("not wrapper object.");
	}

	// 2015.06.15 for Java7
	public java.util.logging.Logger getParentLogger()
			throws SQLFeatureNotSupportedException {
		if (ds == null) {
			throw new SQLFeatureNotSupportedException("no datasource assigned.");
		}
		
		return ds.getParentLogger();
	}
}
