package s2.adapi.framework.dao;

import java.util.HashMap;
import java.util.Map;

import s2.adapi.framework.container.ParentAwareService;
import s2.adapi.framework.dao.sql.DataSourceSet;

/**
 * DB Sharding 을 지원하기 위하여 한번에 여러 DB에 접근하는 DAO를 제공한다.
 * (2016.10.26)
 * 
 * @author hdkim
 *
 */
public class SqlQueryDAOSet implements ParentAwareService {

	private Object parentObject = null;
	
	private Map<Integer, SqlQueryDAO> daoMap = new HashMap<Integer, SqlQueryDAO>();
	private DataSourceSet datasourceSet = null;
	private String sqlPath = null;
	
	// ParentAwareService 메소드 구현이다.
	@Override
	public void setParent(Object parent) {
		parentObject = parent;
	}

	public void setDatasourceSet(DataSourceSet dsSet) {
		datasourceSet = dsSet;
	}
	
	/**
	 * 사용할 SQL 파일의 경로를 지정한다. 생성되는 모든 DAO가 같은 Sql 파일을 사용한다.
	 * @param sqlpath SQL 파일의 경로(클래스패스 경로)
	 */
	public void setSql(String path) {
		sqlPath = path;
	}
	
	/**
	 * index 번째의 DataSource를 사용하는 SqlQueryDAO 객체를 반환한다.
	 * Sql 파일은 동일한 것으로 설정된다.
	 * @param index
	 * @return
	 */
	public SqlQueryDAO getDao(int index) {
		//
		if (index < 0 || index >= datasourceSet.size()) {
			return null;
		}
		
		SqlQueryDAO dao = daoMap.get(index);
		if (dao == null) {
			synchronized (daoMap) {
				if (!daoMap.containsKey(index)) {
					dao = new SqlQueryDAO();
					dao.setDatasource(datasourceSet.getDatasource(index));
					dao.setSql(sqlPath);
					dao.setParent(parentObject);
					
					daoMap.put(index, dao);
				}
				else {
					dao = daoMap.get(index);
				}
			}
		}
		
		return dao;
	}
	
	/**
	 * 제공되는 DAO 갯수
	 * @return
	 */
	public int size() {
		return datasourceSet.size();
	}
}
