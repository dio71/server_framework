package s2.adapi.framework.dao.types;

/**
 * DBMS와 독립적인 Array 객체를 표현하기 위한 클래스이다.
 * @author kimhd
 *
 */
public class JdbcArray {
	private Object arrayObject = null;
	private String arrayTypeName = null;
	
	public JdbcArray(Object obj, String typeName) {
		arrayObject = obj;
		arrayTypeName = typeName;
	}
	
	public Object getObject() {
		return arrayObject;
	}
	
	public String getTypeName() {
		return arrayTypeName;
	}
}
