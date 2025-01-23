package s2.adapi.framework.util.json;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import s2.adapi.framework.util.Base64Helper;
import s2.adapi.framework.util.StringHelper;
import s2.adapi.framework.vo.ValueObject;


public class JsonWriter {
	private SimpleDateFormat sdf = new SimpleDateFormat(JsonToken.DATE_FORMAT);
	
	public void write(ValueObject pVO, StringBuilder sb) {
		
		for(int i=0;i<pVO.size();i++) {
			write(pVO.get(i),sb);
		}
	}
	
	public void write(Map<?,?> value, StringBuilder sb) {
		Iterator<?> itor = value.keySet().iterator();
		sb.append(JsonToken.BEGIN_OBJECT);
		boolean isFirst = true;
		String key = null;
		Object obj = null;
		while(itor.hasNext()) {
			key = String.valueOf(itor.next());
			obj = value.get(key);
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(JsonToken.OBJECT_SEPERATOR);
			}
			write(key,obj,sb);
		}
		sb.append(JsonToken.END_OBJECT);
	}
	
	/**
	 * key:value pair 형태로 출력한다.
	 * @param key
	 * @param value
	 * @param sb
	 */
	private void write(String key, Object value, StringBuilder sb) {
		write(key,sb);
		sb.append(JsonToken.PAIR_SEPERATOR);
		write(value,sb);
	}
	
	/**
	 * 주어진 객체를 type에 맞추어 출력한다.
	 * @param value
	 * @param sb
	 */
	private void write(Object value, StringBuilder sb) {
		if (value == null) {
			sb.append("null");
		} else if (value instanceof String) {
			write((String)value,sb);
		} else if (value instanceof Number) {
			write((Number)value,sb);
		} else if (value instanceof byte[]) {
			write((byte[])value,sb);
		} else if (value instanceof Date) {
			write((Date)value,sb);
		} else if (value instanceof char[]) {
			write((char[])value,sb);
		} else if (value instanceof Map<?,?>) {
			write((Map<?,?>)value,sb);
		} else if (value instanceof Object[]) {
			write((Object[])value,sb);
		} else if (value instanceof List<?>) {
			write((List<?>)value,sb);
		} else {
			write(String.valueOf(value),sb);
		}
	}
	
	/**
	 * List 객체를 [element1,element2,...] 와 같이 배열 포멧으로 출력한다.
	 * @param value
	 * @param sb
	 */
	private void write(List<?> value, StringBuilder sb) {
		sb.append(JsonToken.BEGIN_ARRAY);
		boolean isFirst = true;
		for(int i=0;i<value.size();i++) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(JsonToken.ARRAY_SEPERATOR);
			}
			write((Object)value.get(i),sb);
		}
		sb.append(JsonToken.END_ARRAY);
	}
	
	/**
	 * 배열 객체를 [element1,element2,...] 와 같이 배열 포멧으로 출력한다.
	 * @param value
	 * @param sb
	 */
	private void write(Object[] value, StringBuilder sb) {
		sb.append(JsonToken.BEGIN_ARRAY);
		boolean isFirst = true;
		for(int i=0;i<value.length;i++) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(JsonToken.ARRAY_SEPERATOR);
			}
			write(value[i],sb);
		}
		sb.append(JsonToken.END_ARRAY);
	}
	
	/**
	 * 문자열을 출력한다.
	 * @param value
	 * @param sb
	 */
	private void write(String value, StringBuilder sb) {
		sb.append(JsonToken.BEGIN_STRING);
		escape(value,sb);
		sb.append(JsonToken.END_STRING);
	}
	
	/**
	 * 숫자를 출력한다.
	 * @param num
	 * @param sb
	 */
	private void write(Number num, StringBuilder sb) {
		sb.append(num);
	}
	
	/**
	 * byte[](binary) 데이터를 출력한다.
	 * @param b
	 * @param sb
	 */
	private void write(byte[] b, StringBuilder sb) {
		sb.append(JsonToken.BEGIN_BYTE);
		sb.append(new String(Base64Helper.encode(b)));
		sb.append(JsonToken.END_BYTE);
	}
	
	/**
	 * char[]를 문자열로 출력한다.
	 * @param c
	 * @param sb
	 */
	private void write(char[] c, StringBuilder sb) {
		write(new String(c),sb);
	}
	
	/**
	 * 날짜형 데이터를 출력한다.
	 * @param d
	 * @param sb
	 */
	private void write(Date d, StringBuilder sb) {
		sb.append(JsonToken.BEGIN_DATE);
		sb.append(sdf.format(d));
		sb.append(JsonToken.END_DATE);
	}
	
	/**
	 * 문자열 중 json에서 정의한 특수문자를 escape 처리한다.
	 * @param value
	 */
	private void escape(String value, StringBuilder sb) {
		for(int i=0;i<value.length();i++) {
			char c = value.charAt(i);
			switch(c) {
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '/':
				sb.append("\\/");
				break;
			case '\b': // backspace
				sb.append("\\b");
				break;
			case '\f': // formfeed
				sb.append("\\f");
				break;
			case '\n': // newline
				sb.append("\\n");
				break;
			case '\r': // carriage return
				sb.append("\\r");
				break;
			case '\t': // tab
				sb.append("\\t");
				break;
			default:
				if (c >= '\u0000' && c <= '\u001F') { // special characters
					sb.append("\\u");
					sb.append(StringHelper.lpad(Integer.toHexString(c), 4, '0'));
				} else {
					sb.append(c);
				}
			}
		}
	}
}
