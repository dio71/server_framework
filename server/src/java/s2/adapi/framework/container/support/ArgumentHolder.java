package s2.adapi.framework.container.support;

public class ArgumentHolder {
	/**
	 * 저장된 Argument의 종류가 value 형태임을 나타냄
	 */
	public static final int TYPE_VALUE = 0;
	/**
	 * 저장된 Argument의 종류가 ref 형태임을 나타냄
	 */	
	public static final int TYPE_REFERENCE = 1;
	
	private String type = null;
	private String value = null;
	private int kind = TYPE_VALUE;
	
	public ArgumentHolder(String ptype, String pvalue, int pkind) {
		type = new String(ptype);
		value = pvalue;
		kind = pkind;
	}
	
	public String getType() {
		return type;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean hasValue() {
		return (kind==TYPE_VALUE);
	}
	
	public boolean hasReference() {
		return (kind==TYPE_REFERENCE);
	}
	
	public String toString() {
		if ( kind == TYPE_REFERENCE ) {
			return "Argument type="+type+", ref=["+value+"]";
		} else {
			return "Argumentr type="+type+", value=["+value+"]";
		}
	}
}
