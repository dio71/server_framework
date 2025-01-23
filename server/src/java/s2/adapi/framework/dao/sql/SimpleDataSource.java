package s2.adapi.framework.dao.sql;

import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;


/**
 * DriverManager 를 사용한 단순한 DataSource 구현
 *
 * @author 김형도
 */
public class SimpleDataSource implements javax.sql.DataSource {

    // Datasource의 속성들
    protected String jdbcUsername = null;
    protected String jdbcPassword = null;
    protected String jdbcUrl = null;
    protected String jdbcDriver = null;
    protected boolean jdbcAutoCommit = false;
    protected String datasourceName = null;

    public SimpleDataSource() { 
    }
    
    /**
     * <p>
     * 생성자이다. 설정에 필요한 속성값들은 <code>Properties</code> 객체내에 정의한다.
     * </p>
     * @param props 생성시 사용할 속성값들이 정의된 Properties 객체 
     */
    public SimpleDataSource(String dsn, Properties props) {

        //
        // Connection 객체 생성을 위하여 필요한 프로퍼티 들을 저장해 놓는다.
        //
        datasourceName = dsn;
        
        jdbcUrl = props.getProperty("jdbc.connectionurl");
        jdbcDriver = props.getProperty("jdbc.driver");
        jdbcUsername = props.getProperty("jdbc.username");
        jdbcPassword = props.getProperty("jdbc.password");
        jdbcAutoCommit = false; //

    }

    public void setDatasourceName(String dsn) {
    	datasourceName = dsn;
    }
    
    public void setConnectionUrl(String url) {
    	jdbcUrl = url;
    }
    
    public void setDriver(String driver) throws ClassNotFoundException {
    	jdbcDriver = driver;
    }
    
    public void setUsername(String username) {
    	jdbcUsername = username;
    }
    
    public void setPassword(String password) {
    	jdbcPassword = password;
    }
    
    /**
     * <p>
     * Datasouce 명을 리턴한다.
     * </p>
     */
    public String getDatasourceName() {
        return datasourceName;
    }
    
    /**
     * <p>
     * 주어진 사용자 아이디와 암호를 사용하여 데이터베이스에  Connection 객체를 리턴한다.
     * @param username database 연결시 사용할 사용자 ID
     * @param password 사용자 암호
     * @return Connection 객체
     * @throws SQLException 데이터베이스 연결 시 에러가 발생할 때, Datasource 설정과 다른 User/Password를 지정했을 때
     */
    @Override
    public java.sql.Connection getConnection(String username, String password)
            throws SQLException {
        //Connection con = null;
        
    	if (username == null || password == null) {
    		throw new SQLException(SqlMessages.CREDENTIAL_ERROR_MESSAGE);
    	}
    	
        if (!username.equals(jdbcUsername) || !password.equals(jdbcPassword)) {
            throw new SQLException(SqlMessages.CREDENTIAL_ERROR_MESSAGE);
        }

        return getConnection();
    }

    /**
     * <p>
     * Native Connection 객체를 생성한다.
     * </p>
     */
    @Override
    public java.sql.Connection getConnection() throws SQLException {
        java.sql.Connection con = null;
        
        try {
			Class.forName(jdbcDriver);
		} 
        catch (ClassNotFoundException e) {
			throw new SQLException(SqlMessages.JDBC_DRIVER_NOT_FOUND + jdbcDriver);
		}
        
        con = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
        con.setAutoCommit(jdbcAutoCommit);
        
        return con;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }
 
    @Override
    public void setLogWriter(PrintWriter out)
            throws SQLException {
        DriverManager.setLogWriter(out);
    }
  
    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }
   
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        DriverManager.setLoginTimeout(seconds);
    }
    
    @Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

    @Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLException("not wrapper object.");
	}

	@Override
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}
}