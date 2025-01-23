package s2.adapi.framework.dao.sql;

import java.util.HashMap;
import java.util.Map;

/**
 * DB Sharding 을 위하여 여러개의 DataSource 를 관리하는 기능을 제공한다.
 * SqlQueryDAOSet 클래스와 함깨 사용된다.
 * (2016.10.26)
 * 
 * @author hdkim
 *
 */
public class DataSourceSet {

	private int dsCount = 0;
	private Map<Integer, DataSource> dsMap = new HashMap<Integer, DataSource>();
	
	/**
	 * DataSource 를 추가한다. 추가되는 순서대로 index 0 부터 설정된다.
	 * @param ds
	 */
	public void setDatasource(DataSource ds) {
		dsMap.put(dsCount++, ds);
	}
	
	public DataSource getDatasource(int index) {
		return dsMap.get(index);
	}
	
	public int size() {
		return dsCount;
	}
}
