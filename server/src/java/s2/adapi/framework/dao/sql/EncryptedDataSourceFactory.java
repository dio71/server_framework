package s2.adapi.framework.dao.sql;

import java.util.Properties;

import javax.naming.Context;

import org.apache.tomcat.jdbc.pool.DataSourceFactory;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.XADataSource;

import s2.adapi.framework.util.HexEncoder;

/**
 * Apache tomcat 의 dbcp 설정 파일(context.xml)의 평문으로 설정되는 user와 password 를 암호화하여 설정할 수 있도록 기능이 추가됨
 * @author diokim
 *
 */
public class EncryptedDataSourceFactory extends DataSourceFactory {
	@Override
	public javax.sql.DataSource createDataSource(Properties properties, Context context, boolean XA) throws Exception {
	
		PoolConfiguration poolProperties = DataSourceFactory.parsePoolProperties(properties);
		
		String userName = HexEncoder.decryptAES(poolProperties.getUsername());
		String password = HexEncoder.decryptAES(poolProperties.getPassword());
		poolProperties.setUsername(userName);
		poolProperties.setPassword(password);
		
		// The rest of the code is copied from Tomcat's DataSourceFactory.
        if (poolProperties.getDataSourceJNDI() != null && poolProperties.getDataSource() == null) {
            performJNDILookup(context, poolProperties);
        }
        org.apache.tomcat.jdbc.pool.DataSource dataSource = XA ? new XADataSource(poolProperties)
                : new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);
        dataSource.createPool();

        return dataSource;
	}
}
