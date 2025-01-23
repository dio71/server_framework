package s2.adapi.framework.container.support;

public class PropertyHolder {
	/**
	 * 저장된 Property의 종류가 value 형태임을 나타냄
	 */
	public static final int TYPE_VALUE = 0;
	/**
	 * 저장된 Property의 종류가 ref 형태임을 나타냄
	 */	
	public static final int TYPE_REFERENCE = 1;
	
	private String name = null;
	private String value = null;
	private int kind = TYPE_VALUE;
	
	public PropertyHolder(String pname, String pvalue, int pkind) {
		name = new String(pname);
		value = pvalue;
		kind = pkind;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		// 2019.09.25 기능추가 Java System property 참조기능 추가 : ${my.dbname} 형태의 문자열은 해당 프로퍼티 값으로 치환
		return interpreteValue(value);
	}
	
	public boolean hasValue() {
		return (kind == TYPE_VALUE);
	}
	
	public boolean hasReference() {
		return (kind == TYPE_REFERENCE);
	}
	
	public String toString() {
		if ( kind == TYPE_REFERENCE ) {
			return "Property name="+name+", ref=["+value+"]";
		} 
		else {
			return "Property name="+name+", value=["+value+"]";
		}
	}
	
	private static String interpreteValue(String base) {

		int begin = base.indexOf("${");
		int end = base.indexOf("}");

		if (begin >= 0 && end >= 0 && begin < end ) {
			// 변수 존재
			StringBuffer sb = new StringBuffer();
			
			// ${ 앞까지의 문자열 append.
			sb.append(base.substring(0, begin)); 
			
			// ${ 와 } 사이의  문자열 잘라오기
			String variable = base.substring(begin + 2, end); 
			
			String prop = System.getProperty(variable);
			if (prop != null) {
				sb.append(prop);
			}
			// } 뒤의 문자열을 추가
			sb.append(base.substring(end + 1, base.length()));
			
			return interpreteValue(sb.toString());
		} 
		else {
			return base;
		}
	}
}
