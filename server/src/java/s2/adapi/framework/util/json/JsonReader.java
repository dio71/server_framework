package s2.adapi.framework.util.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import s2.adapi.framework.util.Base64Helper;

public class JsonReader {
	private final int BUFFER_SIZE = 1024;
	
	private SimpleDateFormat sdf = new SimpleDateFormat(JsonToken.DATE_FORMAT);
	private BufferedReader in = null;
	private char[] buffer = new char[BUFFER_SIZE];
	private int ahead = JsonToken.LINE_SEPARATOR; // 다음 문자값을 담아 놓는곳
	private int bufferIdx = 0;
	private int bufferRead = 0;
	private int curLine = 1;
	private StringBuilder sb = new StringBuilder(BUFFER_SIZE);
	
	public JsonReader(Reader reader) {
		in = new BufferedReader(reader);
	}
	
	public void close() throws IOException {
		if (in != null) {
			in.close();
		}
	}
	
	//============================================================
	// parseXXX()
	// XXX에 해당되는 객체를 파싱하여 반환
	// 시작위치까지는 skipWhite() 되어 있는 상태에서 진행
	// 자신의 파싱 대상에 해당되는 문자열은 모두 지난 상태에서 리턴되어야함
	//============================================================
	
	/**
	 * {name:value,...} 형태의 문자열을 파싱하여 Map 객체를 생성한다.
	 */
	public Map<String,Object> parseObject() throws IOException {
		
		skipWhite();
		if (ahead == JsonToken.EOF) {
			return null;
		}
		
		// 객체들을 담아둘 map 생성
		Map<String,Object> map = new HashMap<String,Object>();
		
		expect(JsonToken.BEGIN_OBJECT);
		advance();
		while(true) {
			skipWhite();
			parsePair(map);
			skipWhite();
			if (ahead == JsonToken.OBJECT_SEPERATOR) {
				advance();
			} 
			else if (ahead == JsonToken.END_OBJECT) {
				advance();
				return map;
			} 
			else {
				throw new IOException("expected '}' or ',' but '"+(char)ahead+"' at line "+curLine);
			}
		}
		
	}
	
	/**
	 * 하나의 name:value 를 파싱하여 map 객체에 담는다.
	 * @param map
	 * @throws IOException
	 */
	private void parsePair(Map<String,Object> map) throws IOException {
		// name 문자열 파싱
		String name = parseString().trim();
		
		// name:value 구분자
		skipWhite();
		expect(JsonToken.PAIR_SEPERATOR);
		advance();
		
		// value 파싱
		skipWhite();
		Object value = parseValue();
		
		map.put(name, value);
	}
	
	private Object parseValue() throws IOException {
		Object obj = null;
		switch(ahead) {
		case JsonToken.BEGIN_OBJECT:
			obj = parseObject();
			break;
		case JsonToken.BEGIN_STRING:
			obj = parseString();
			break;
		case JsonToken.BEGIN_BYTE:
			obj = parseByte();
			break;
		case JsonToken.BEGIN_DATE:
			obj = parseDate();
			break;
		case JsonToken.BEGIN_ARRAY:
			obj = parseArray();
			break;
		default : // assume number
			obj = parseDefault();
			break;
		}
		
		return obj;
	}
	
	/**
	 * [item,...] 형태의 배열 객체를 파싱한다. 배열은 List<Object> 객체로 반환한다.
	 * @return
	 * @throws IOException
	 */
	private List<Object> parseArray() throws IOException {
		List<Object> list = new ArrayList<Object>();
		expect(JsonToken.BEGIN_ARRAY);
		advance();
		Object obj = null;
		while(true) {
			skipWhite();
			obj = parseValue();
			list.add(obj);
			
			skipWhite();
			if (ahead == JsonToken.ARRAY_SEPERATOR) {
				advance();
			} 
			else if (ahead == JsonToken.END_ARRAY) {
				advance();
				return list;
			} 
			else {
				throw new IOException("expected ']' or ',' but '"+(char)ahead+"' at line "+curLine);
			}
		}
	}
	
	/**
	 * 숫자형값 또는 true, false, null 문자값을 파싱한다.
	 * @return
	 * @throws IOException
	 */
	private Object parseDefault() throws IOException {
		// no expectation
		sb.setLength(0);
		while(ahead != JsonToken.ARRAY_SEPERATOR && ahead != JsonToken.END_ARRAY && 
			  ahead != JsonToken.OBJECT_SEPERATOR && ahead != JsonToken.END_OBJECT &&
			  !JsonToken.isWhitespace(ahead)) {
			if (ahead == JsonToken.EOF) {
				throw new IOException("unexpected eof at line "+curLine);
			} 
			else {
				sb.append((char)ahead);
				advance();
			}
		}
		
		String defStr = sb.toString();
		Object retObj = null;
		if (defStr.equalsIgnoreCase("true")) {
			retObj = Boolean.valueOf(true);
		} 
		else if (defStr.equalsIgnoreCase("false")) {
			retObj = Boolean.valueOf(false);
		} 
		else if (defStr.equalsIgnoreCase("null")) {
			retObj = null;
		} 
		else { // assume as number
			try {
				retObj = Double.parseDouble(sb.toString());
			} 
			catch (NumberFormatException ex) {
				throw new IOException("number format error at line "+curLine);
			}
		}
		
		return retObj;
	}
	
	/**
	 * (yyyyMMddHHmmssSSS) 형태의 날짜를 파싱한다.
	 * @return
	 * @throws IOException
	 */
	private Date parseDate() throws IOException {
		expect(JsonToken.BEGIN_DATE);
		advance();
		sb.setLength(0);
		while(ahead != JsonToken.END_DATE) {
			if (ahead == JsonToken.EOF) {
				throw new IOException("unexpected eof at line "+curLine);
			} 
			else {
				sb.append((char)ahead);
				advance();
			}
		}
		advance(); // ) 다음으로 진행
		try {
			return sdf.parse(sb.toString());
		} 
		catch (ParseException e) {
			throw new IOException("date is not 'yyyyMMddHHmmssSSS' format at line "+curLine);
		}
	}
	
	/**
	 * 'base64인코딩문자' 형태를 파싱한다.
	 */
	private byte[] parseByte() throws IOException {
		expect(JsonToken.BEGIN_BYTE);
		advance();
		sb.setLength(0);
		while(ahead != JsonToken.END_BYTE) {
			if (ahead == JsonToken.EOF) {
				throw new IOException("unexpected eof at line "+curLine);
			} else {
				sb.append((char)ahead);
				advance();
			}
		}
		advance(); // ' 다음으로 진행
		return Base64Helper.decode(sb.toString()).getBytes();
	}
	
	/**
	 * "문자열" 형태를 파싱한다.
	 * @throws IOException 
	 */
	private String parseString() throws IOException {
		
		expect(JsonToken.BEGIN_STRING);
		advance();
		boolean escape = false;
		sb.setLength(0);
		while(ahead != JsonToken.END_STRING || escape) {
			if (ahead == JsonToken.EOF) {
				throw new IOException("unexpected eof at line "+curLine);
			} 
			else if (ahead == JsonToken.ESCAPER) {
				escape = true;
			} 
			else {
				if (escape) {
					switch(ahead) {
					case '"':
						sb.append('"');
						break;
					case '\\':
						sb.append('\\');
						break;
					case '/':
						sb.append('/');
						break;
					case 'b':
						sb.append('\b');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'n':
						sb.append('\n');
						break;
					case 'r':
						sb.append('\n');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'u':
						// \uAD11  형태 파싱 기능 추가 (2015.0520)
						{
							String str = advance4();
							if (str == null) {
								throw new IOException("unexpected eof at line "+curLine);
							}
							sb.append((char)Integer.parseInt(str, 16));
						}
						break;
					default:
						throw new IOException("invalid escaping sequence(\\"+ahead+") at line "+curLine);
					}
					escape = false;
				} 
				else {
					sb.append((char)ahead);
				}
			}
			advance();
		}
		
		advance(); // " 문자 다음으로 진행
		return sb.toString();
	}
	
	/**
	 * 기대하는 문자가 다음에 존재하는지 여부를 확인한다.
	 * @param expc
	 * @throws IOException 기대한 문자가 오지 않을 때
	 */
	private void expect(char expc) throws IOException {
		if (ahead == JsonToken.EOF) {
			throw new IOException("unexpected eof at line "+curLine);
		} 
		else if (ahead != expc) {
			throw new IOException("expected '"+expc+"' but '"+(char)ahead+"' at line "+curLine);
		}
	}
	
	private String advance4() throws IOException {
		// 4글자를 읽어서 반환하고 그 다음 문자로 advance 한다.
		char[] str = new char[4];
		
		int next = advance();
		if (next < 0) {
			return null;
		}
		str[0] = (char)next;
		
		next = advance();
		if (next < 0) {
			return null;
		}
		str[1] = (char)next;
		
		next = advance();
		if (next < 0) {
			return null;
		}
		str[2] = (char)next;
		
		next = advance();
		if (next < 0) {
			return null;
		}
		str[3] = (char)next;
		
		return new String(str);
	}
	
	/**
	 * reader에서 다음 문자를 ahead에 담고 ahead 값을 반환한다.
	 * @return ahead에 담긴 문자를 반환한다. 파일의 끝이면 -1을 반환한다.
	 * @throws IOException
	 */
	private int advance() throws IOException {
		if (ahead == JsonToken.EOF) {  // eof already.
			return -1;
		}
		if (bufferIdx < bufferRead) {
			ahead = buffer[bufferIdx++];
		} 
		else { // end of buffer, read from reader
			int n = in.read(buffer, 0, BUFFER_SIZE);
			if (n == -1) { // eof
				ahead = JsonToken.EOF;
			} 
			else {
				bufferIdx = 0;
				bufferRead = n;
				if (bufferIdx < bufferRead) {
					ahead = buffer[bufferIdx++];
				} 
				else {
					throw new IOException("read error.");
				}
			}
		}
		if (ahead == JsonToken.LINE_SEPARATOR) {
			curLine++;
		}
		//System.out.println((char)ahead);
		return ahead;
	}
	
	/**
	 * white 문자가 나오지 않을때까지 진행시킨다.
	 * @throws IOException
	 */
	private void skipWhite() throws IOException {
		while(ahead != JsonToken.EOF && JsonToken.isWhitespace(ahead)) {
			advance();
		}
	}
}
