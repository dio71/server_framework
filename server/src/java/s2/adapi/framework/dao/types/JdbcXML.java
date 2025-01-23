package s2.adapi.framework.dao.types;

/**
 * DBMS와 독립적인 XML 객체를 표현하기 위한 클래스이다.
 * @author kimhd
 *
 */
public class JdbcXML {

	private String xmlString = null;
	
	public JdbcXML(String xmlStr) {
		xmlString = xmlStr;
	}
	
	public JdbcXML(char[] xmlStr) {
		xmlString = new String(xmlStr);
	}
	
	public String toString() {
		return xmlString;
	}
}
