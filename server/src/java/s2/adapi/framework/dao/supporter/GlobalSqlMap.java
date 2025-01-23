package s2.adapi.framework.dao.supporter;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import s2.adapi.framework.config.Configurator;
import s2.adapi.framework.config.ConfiguratorFactory;
import s2.adapi.framework.query.SqlQueryReader;
import s2.adapi.framework.query.element.ResultMap;
import s2.adapi.framework.query.element.SqlStatement;
import s2.adapi.framework.query.element.Sqls;

/**
 * Global로 정의된 SQL Query XML 파일을 파싱하고 해당 정보를 제공하는 클래스이다.
 * @author 김형도
 *
 */
public class GlobalSqlMap {
	private static final Logger log = LoggerFactory.getLogger(GlobalSqlMap.class);
	
	public static final String GLOBAL_RESULTMAP_PROPERTY = "s2adapi.dao.globalmap";
	
	public static final String DEFAULT_GLOBAL_RESULTMAP_FILE = "resources/dao_globalmap.xml";
	
	protected String globalSqlMapPath = null;
	
	private Sqls globalSqls = null;
	private String[] sqlIds = null;
	
	protected static GlobalSqlMap instance = null;
	
	/**
	 * 단일 객체(singleton)를 생성하여 반환하는 메소드
	 * @return
	 */
	protected static GlobalSqlMap getInstance() {
		if (instance == null) {
			instance = new GlobalSqlMap();
		}
		return instance;
	}
	
	protected GlobalSqlMap() {
		Configurator configurator = null;
        try {
        	// Global Mapping file 경로를 얻어온다.
        	configurator = ConfiguratorFactory.getConfigurator();
            globalSqlMapPath = configurator.getString(GLOBAL_RESULTMAP_PROPERTY, 
            		DEFAULT_GLOBAL_RESULTMAP_FILE);
            if (log.isInfoEnabled()) {
                log.info("Global sql file : " + globalSqlMapPath);
            }
        } catch (Exception ex) {
            if (log.isErrorEnabled()) {
                log.error("Global sql file configuration error.");
            }
        }
        
        if (globalSqlMapPath != null) {
			try {
				// Global Mapping file을 파싱한다.
				globalSqls = SqlQueryReader.getReader().getQuery(globalSqlMapPath);
			} catch(IOException ex) {
	            if (log.isErrorEnabled()) {
	                log.error("Global result mapping file not found: "+globalSqlMapPath);
	            }
			} catch(SAXException ex) {
	            if (log.isErrorEnabled()) {
	                log.error("Global sql mapping file has invalid format: "+globalSqlMapPath);
	            }
			}
        }
        
        if (globalSqls == null) {
        	sqlIds = new String[0];
        }
        else {
        	Set<String> ids = globalSqls.getStatementKeySet();
        	sqlIds = ids.toArray(new String[ids.size()]);
        }
	}
	
	/**
	 * Global Sql 파일에 정의된 ResultMap을 반환한다.
	 * @param vendor
	 * @return
	 */
	public ResultMap getResultMap(String vendor) {
		if (globalSqls == null) {
			return null;
		}
		else {
			return globalSqls.getResultMap(vendor);
		}
	}
	
	/**
	 * Global Sql 파일에 정의된 Statement들의 id를 Set으로 반환한다.
	 * @return
	 */
	public String[] getStatementIds() {
		return sqlIds;
	}
	
	/**
	 * Global Sql 파일에 정의된 Statement들 중 주어진 id의 Statement를 반환한다.
	 * @param id
	 * @return
	 */
	public SqlStatement getStatement(String id) {
		return globalSqls.getStatement(id);
	}

}
