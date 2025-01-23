package s2.adapi.framework.query.element;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * java.sql.Types에 정의된 JDBC type 값과 그 이름들을 매핑하여
 * 그 정보를 제공하는 클래스이다.
 * @author 김형도
 * @since 4.0
 */
public class SqlTypes {
	
	public static final int XMLTYPE = -91999;
	
	private static Map<String,Integer> nameMap = null;
	private static Map<Integer,String> typeMap = null;
	
	static {
		nameMap = new HashMap<String,Integer>();
		typeMap = new HashMap<Integer,String>();
		
		nameMap.put("ARRAY",Types.ARRAY);
		nameMap.put("BIGINT",Types.BIGINT);
		nameMap.put("BINARY",Types.BINARY);
		nameMap.put("BIT",Types.BIT);
		nameMap.put("BLOB",Types.BLOB);
		nameMap.put("BOOLEAN",Types.BOOLEAN);
		nameMap.put("CHAR",Types.CHAR);
		nameMap.put("CLOB",Types.CLOB);
		nameMap.put("DATALINK",Types.DATALINK);
		nameMap.put("DATE",Types.DATE);
		nameMap.put("DECIMAL",Types.DECIMAL);
		nameMap.put("DISTINCT",Types.DISTINCT);
		nameMap.put("DOUBLE",Types.DOUBLE);
		nameMap.put("FLOAT",Types.FLOAT);
		nameMap.put("INTEGER",Types.INTEGER);
		nameMap.put("JAVA_OBJECT",Types.JAVA_OBJECT);
		nameMap.put("LONGVARBINARY",Types.LONGVARBINARY);
		nameMap.put("LONGVARCHAR",Types.LONGVARCHAR);
		nameMap.put("NULL",Types.NULL);
		nameMap.put("NUMERIC",Types.NUMERIC);
		nameMap.put("OTHER",Types.OTHER);
		nameMap.put("REAL",Types.REAL);
		nameMap.put("REF",Types.REF);
		nameMap.put("SMALLINT",Types.SMALLINT);
		nameMap.put("STRUCT",Types.STRUCT);
		nameMap.put("TIME",Types.TIME);
		nameMap.put("TIMESTAMP",Types.TIMESTAMP);
		nameMap.put("TINYINT",Types.TINYINT);
		nameMap.put("VARBINARY",Types.VARBINARY);
		nameMap.put("VARCHAR",Types.VARCHAR);
		
		typeMap.put(Types.ARRAY,"ARRAY");
		typeMap.put(Types.BIGINT,"BIGINT");
		typeMap.put(Types.BINARY,"BINARY");
		typeMap.put(Types.BIT,"BIT");
		typeMap.put(Types.BLOB,"BLOB");
		typeMap.put(Types.BOOLEAN,"BOOLEAN");
		typeMap.put(Types.CHAR,"CHAR");
		typeMap.put(Types.CLOB,"CLOB");
		typeMap.put(Types.DATALINK,"DATALINK");
		typeMap.put(Types.DATE,"DATE");
		typeMap.put(Types.DECIMAL,"DECIMAL");
		typeMap.put(Types.DISTINCT,"DISTINCT");
		typeMap.put(Types.DOUBLE,"DOUBLE");
		typeMap.put(Types.FLOAT,"FLOAT");
		typeMap.put(Types.INTEGER,"INTEGER");
		typeMap.put(Types.JAVA_OBJECT,"JAVA_OBJECT");
		typeMap.put(Types.LONGVARBINARY,"LONGVARBINARY");
		typeMap.put(Types.LONGVARCHAR,"LONGVARCHAR");
		typeMap.put(Types.NULL,"NULL");
		typeMap.put(Types.NUMERIC,"NUMERIC");
		typeMap.put(Types.OTHER,"OTHER");
		typeMap.put(Types.REAL,"REAL");
		typeMap.put(Types.REF,"REF");
		typeMap.put(Types.SMALLINT,"SMALLINT");
		typeMap.put(Types.STRUCT,"STRUCT");
		typeMap.put(Types.TIME,"TIME");
		typeMap.put(Types.TIMESTAMP,"TIMESTAMP");
		typeMap.put(Types.TINYINT,"TINYINT");
		typeMap.put(Types.VARBINARY,"VARBINARY");
		typeMap.put(Types.VARCHAR,"VARCHAR");
		
		// JdbcXML 객체로 처리됨
		nameMap.put("XML",SqlTypes.XMLTYPE);
		typeMap.put(SqlTypes.XMLTYPE,"XML");
		
	}
	
	public static String getName(int type) {
		return typeMap.get(type);
	}
	
	public static Integer getType(String name) {
		return nameMap.get(name);
	}
}
