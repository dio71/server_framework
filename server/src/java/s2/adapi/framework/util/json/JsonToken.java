package s2.adapi.framework.util.json;

public class JsonToken {
	public static final String DATE_FORMAT = "yyyyMMddHHmmssSSS";
	public static final char BEGIN_OBJECT = '{';
	public static final char END_OBJECT = '}';
	public static final char BEGIN_STRING = '"';
	public static final char END_STRING = '"';
	public static final char BEGIN_BYTE = '\'';
	public static final char END_BYTE = '\'';
	public static final char BEGIN_DATE = '(';
	public static final char END_DATE = ')';
	public static final char BEGIN_ARRAY = '[';
	public static final char END_ARRAY = ']';
	
	public static final char ESCAPER = '\\';
	public static final char PAIR_SEPERATOR = ':';
	public static final char OBJECT_SEPERATOR = ',';
	public static final char ARRAY_SEPERATOR = ',';
	
	public static final char LINE_SEPARATOR = Character.LINE_SEPARATOR;
	
	public static final int EOF = -1;
	
	public static boolean isWhitespace(int cp) {
		return Character.isWhitespace(cp);
	}
}
