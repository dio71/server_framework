package s2.adapi.framework.dao.sql;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory;

import s2.adapi.framework.util.HexEncoder;
import s2.adapi.framework.util.StringHelper;

/**
 * Apache tomcat 의 dbcp 설정 파일(context.xml)의 평문으로 설정되는 user와 password 를 암호화하여 설정할 수 있도록 기능이 추가됨
 * 
 * https://commons.apache.org/proper/commons-dbcp/configuration.html
 * 
 * @author diokim
 *
 */
public class EncryptedDataSourceFactoryV2 extends BasicDataSourceFactory {

	// @Override
	// public Object getObjectInstance(Object object, Name name, Context ctx, Hashtable<?, ?> environment) throws Exception {
	// 	Object obj = super.getObjectInstance(object, name, ctx, environment);
		
	// 	if (obj == null) {
	// 		return null;
	// 	}
			
    //     BasicDataSource ds = (BasicDataSource) obj;
        
    //     String userName = ds.getUsername();
    //     String password = ds.getPassword();
        
    //     if (!StringHelper.allEmpty(userName, password)) {
	// 		ds.setUsername(HexEncoder.decryptAES(ds.getUsername()));
	// 		ds.setPassword(HexEncoder.decryptAES(ds.getPassword()));
			
	// 		return ds;
    //     }
	        
	// 	return null;
	// }
}
