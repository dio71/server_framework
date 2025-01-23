package s2.adapi.framework.query.element;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import s2.adapi.framework.config.KeyStore;
import s2.adapi.framework.context.ContextManager;
import s2.adapi.framework.dao.types.JdbcArray;
import s2.adapi.framework.dao.types.JdbcNull;
import s2.adapi.framework.dao.types.JdbcXML;
import s2.adapi.framework.util.StringHelper;
import s2.adapi.framework.vo.ValueObject;

/**
 * SQL 문장내의 inline 파라메터를 파싱한 결과를 표현하기 위한 클래스이다.
 * inline 파라메터는 #name?default:mode:type@format# 형태이다.
 * @author 김형도
 * @since 4.0
 */
public class SqlParameter {

	/**
	 * Stored Procedure의 파라메터로 사용될 때 입력용 
	 */
	public static final int MODE_IN = 1;
	
	/**
	 * Stored Procedure의 파라메터로 사용될 때 출력용
	 */
	public static final int MODE_OUT = 2;
	
	/**
	 * Stored Procedure의 파라메터로 사용될 때 입력과 출력 모두 사용
	 */
	public static final int MODE_INOUT = 3;
	
	/**
	 * 알 수 없는 파라메터 종류
	 */
	public static final int UNKNONW_VALUE_KIND = -1;
	
	/**
	 * 실제 값을 입력된 ValueObject에서 찾아오는 파라메터 종류
	 */
	public static final int VO_VALUE_KIND = 0;
	
	/**
	 * 실제 값을 현재 ServiceContext의 getRole()을 통해서 찾아오는 파라메터 종류
	 */
	public static final int ROLE_VALUE_KIND = 1;
	
	/**
	 * 실제 값을 KeyStore 에서 찾아오는 파라메터 종
	 */
	public static final int KEY_VALUE_KIND = 2;
	
	/**
	 * 파라메터 종류 구분을 위한 구분자
	 */
	public static final String ROLE_KIND_DELIM = "%"; // 세션에서 값을 가져옴
	
	/**
	 * 파라메터 종류 구분을 위한 구분자
	 */
	public static final String KEY_KIND_DELIM = "!"; // KeyStore 에서 값을 가져옴
	
	/**
	 * 타입이 DATE인 경우 주어진 문자열을 java.sql.Date 객체로 변환 할때 사용되는 포멧 문자열의 디폴트값.
	 */
	public static final String DEFAULT_DATE_FORMAT = "yyyyMMdd";
	
	/**
	 * 타입이 TIME인 경우 주어진 문자열을 java.sql.Time 객체 로 변환 할때 사용되는 포멧 문자열의 디폴트값.
	 */
	public static final String DEFAULT_TIME_FORMAT = "HHmmss";
	
	/**
	 * 타입이 TIMESTAMP 경우 주어진 문자열을 java.sql.Timestamp 객체로 변환 할때 사용되는 포멧 문자열의 디폴트값.
	 */
	public static final String DEFAULT_TIMESTAMP_FORMAT = "yyyyMMddHHmmssSSS";
	
	/**
	 * 파라메터 모드(mode) 문자열을 모드 숫자 값으로 변환하기 위한 맵 객체.
	 */
	private static Map<String,Integer> modeMap = new HashMap<String,Integer>();
	
	private String name = null;
	private int mode = MODE_IN;
	private int type = Types.VARCHAR;
	private String format = null;
	private int kind = VO_VALUE_KIND;
	private Object defaultValue = null;
	
	// 파라메터 값을 꺼내올때 iteration 적용여부를 나타낸다.
	boolean iterative = false;
	
	// iteration 적용시 사용할 인덱스 값이다.
	private int iteration = 0;
	
	static {
		modeMap.put("IN",MODE_IN);
		modeMap.put("OUT",MODE_OUT);
		modeMap.put("INOUT",MODE_INOUT);
		modeMap.put("",MODE_IN); // default mode
	}
	
	public SqlParameter(String name, int mode, int type, String format, int kind, 
			boolean iterative, int iteration, Object defValue) {
		this.name = name;
		this.mode = mode;
		this.type = type;
		this.format = format;
		this.kind = kind;
		this.iterative = iterative;
		this.iteration = iteration;
		this.defaultValue = defValue;
	}
	
	/**
	 * "name?default:mode:type" 형태의 문자열을 파싱하여 SqlParameter를 생성한다.
	 * @param inlineParam
	 */
	public SqlParameter(String inlineParam) {
		if ( StringHelper.isNull(inlineParam) ) {
			throw new IllegalArgumentException("empty inline Sql parameter.");
		}
		int atpos = -1;
		
		// name 파트 파싱
		atpos = inlineParam.indexOf(":");
		String paramName = null;
		if (atpos >= 0) {
			paramName = inlineParam.substring(0,atpos);
			inlineParam = inlineParam.substring(atpos+1);
		} else {
			paramName = inlineParam;
			inlineParam = "";
		}
		
		atpos = paramName.indexOf("?");
		
		if (atpos >= 0) { // default 값이 정의되어 있으므로 이를 잘라내어 설정한다.
			defaultValue = StringHelper.unescapeUnicode(paramName.substring(atpos+1));
			paramName = paramName.substring(0,atpos);
		}
		
		if (paramName.endsWith("[]")) {  // Iterative Sql parameter
			paramName = paramName.substring(0,paramName.length()-2);
			iterative = true;
		} else {
			iterative = false;
		}
		
		if (paramName.startsWith(ROLE_KIND_DELIM)) { // ROLE_VALUE_KIND
			name = paramName.substring(1);
			kind = ROLE_VALUE_KIND;
		}
		else if (paramName.startsWith(KEY_KIND_DELIM)) { // KEY_VALUE_KIND
			name = paramName.substring(1);
			kind = KEY_VALUE_KIND;
		}
		else {	// VO_VALUE_KIND
			name = paramName;
			kind = VO_VALUE_KIND;
		}
		
		if (StringHelper.isNull(name)) {
			throw new IllegalArgumentException("parameter name not given.");
		}
		
		// mode 부분 파싱
		atpos = inlineParam.indexOf(":");
		String modeName = null;
		if (atpos >= 0) {
			modeName = inlineParam.substring(0,atpos);
			inlineParam = inlineParam.substring(atpos+1);
		} else {
			modeName = inlineParam;
			inlineParam = "";
		}
		
		Integer modeObj = modeMap.get(modeName);
		if ( modeObj != null ) {
			mode = modeObj.intValue();
		} else {
			throw new IllegalArgumentException("unknown parameter mode ["+modeName+"].");
		}
		
		// type 부분 파싱
		atpos = inlineParam.indexOf("@");
		String typeStr = null;
		if ( atpos >= 0 ) { // format 문자열이 존재하므로 잘라내어 설정한다.
			typeStr = inlineParam.substring(0,atpos);
			format = inlineParam.substring(atpos+1);
		} else {
			typeStr = inlineParam;
		}
		if (!StringHelper.isNull(typeStr)) {
			Integer typeObj = SqlTypes.getType(typeStr);
		
			if ( typeObj != null ) {
				type = typeObj.intValue();
			} else {
				throw new IllegalArgumentException("unknown parameter type ["+typeStr+"].");
			}
		}

	}
	
	public SqlParameter clone() {
		return new SqlParameter(name,mode,type,format,kind,iterative,iteration,defaultValue);
	}
	
	public SqlParameter setIteration(int count) {
		iteration = count;
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public int getMode() {
		return mode;
	}
	
	public int getType() {
		return type;
	}
	
	public int getKind() {
		return kind;
	}
	
	public String getFormat() {
		return format;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * 파라메터의 종류에 따라서 ValueObject또는 ServiceContext에서 값을 찾는다.
	 * @param paramVO
	 * @param voIdx
	 * @return
	 */
	public Object getParameterObject(ValueObject paramVO, int voIdx) {
		Object nativeObj = null;
		
		// parameter kind에 따라서 Value 객체를 가져온다. 
		
		if( kind == VO_VALUE_KIND ) {
			if (paramVO == null || paramVO.size() == 0) {
				nativeObj = null;
			} else {
				nativeObj = paramVO.get(voIdx,name);
			}
		} 
		else if (kind == ROLE_VALUE_KIND) {
			nativeObj = ContextManager.getServiceContext().getRole(name);
		}
		else if (kind == KEY_VALUE_KIND) {
			nativeObj = KeyStore.instance().getConfig(name);
		}
		else {
			// 정의되지 않은 KIND
			return null;
		}
		
		Object returnObj = null;
		
		if (iterative && nativeObj != null) { // iteration
			if (nativeObj instanceof List<?>) {
				List<?> listObj = (List<?>)nativeObj;
				if (iteration < listObj.size()) {
					returnObj = listObj.get(iteration);
				} else {
					returnObj = null;
				}
			} else if (nativeObj instanceof Object[]) {
				Object[] arrObj = (Object[])nativeObj;
				if (iteration < arrObj.length) {
					returnObj = arrObj[iteration];
				} else {
					returnObj = null;
				}
			} else { // List 또는 배열 타입이 아니라면 그대로 리턴한다.
				returnObj = nativeObj;
			}
		} else { // no iteration
			returnObj = nativeObj;
		}
		
		// 리턴값이 null 인경우에는 디폴트로 설정된 값을 반환한다.
		if (returnObj == null || String.valueOf(returnObj).equals("")) {
			returnObj = defaultValue;
		}
		
		return returnObj;
	}
	
	/**
	 * 주어진 paramVO의 voIdx 번째 row 값들 중에서 자신의 컬럼명에 해당되는 값을 가져와
	 * 자신의 type@format 에 맞추어 타입 변환 후 그 객체를 반환한다.
	 * @param paramVO
	 * @param voIdx
	 * @return
	 * @throws ParseException
	 */
	public Object getParameterValue(ValueObject paramVO, int voIdx) throws ParseException {
		Object paramObj = getParameterObject(paramVO, voIdx);
		
		if (paramObj == null) {
			switch(type) {
			case SqlTypes.XMLTYPE: // Jdbc 표준 타입이 아닌 것들은 표준 타입 중 하나를 선택하여 JdbcNull 객체 생성
				paramObj = new JdbcNull(Types.VARCHAR);
				break;
			default:
				paramObj = new JdbcNull(type);
				break;
			}
		} else {
			// Type 변환 로직 처리.
			switch(type) {
			case Types.DATE:
				if (paramObj instanceof String) {
					String strObj = (String)paramObj;
					
					String dateFormat = format;
					if ( dateFormat == null ) {
						dateFormat = DEFAULT_DATE_FORMAT;
					}
					SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
					paramObj = new Date(sdf.parse(strObj).getTime());
				} else if (paramObj instanceof java.util.Date) {
					java.util.Date dateObj = (java.util.Date)paramObj;
					paramObj = new Date(dateObj.getTime());
				}
				break;
			case Types.TIME:
				if (paramObj instanceof String) {
					String strObj = (String)paramObj;
					
					String dateFormat = format;
					if ( dateFormat == null ) {
						dateFormat = DEFAULT_TIME_FORMAT;
					}
					SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
					paramObj = new Time(sdf.parse(strObj).getTime());
				} else if (paramObj instanceof java.util.Date) {
					java.util.Date dateObj = (java.util.Date)paramObj;
					paramObj = new Time(dateObj.getTime());
				}
				break;
			case Types.TIMESTAMP:
				if (paramObj instanceof String) {
					String strObj = (String)paramObj;
					
					String dateFormat = format;
					if ( dateFormat == null ) {
						dateFormat = DEFAULT_TIMESTAMP_FORMAT;
					}
					SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
					paramObj = new Timestamp(sdf.parse(strObj).getTime());
				} else if (paramObj instanceof java.util.Date) {
					java.util.Date dateObj = (java.util.Date)paramObj;
					paramObj = new Timestamp(dateObj.getTime());
				}
				break;
			case Types.CLOB:
				if (paramObj instanceof String) {
					String strObj = (String)paramObj;
					paramObj = strObj.toCharArray();
				} else if (paramObj instanceof char[]) {
					// noting to do
				} else {
					String strObj = paramObj.toString();
					paramObj = strObj.toCharArray();
				}
				break;
			case Types.ARRAY:
				// DB 내 타입 명이 format 문자열로 전달되므로 이를 함께 JdbcArray 객체로 감싼다.
				paramObj = new JdbcArray(paramObj, format);
				break;
			case SqlTypes.XMLTYPE:
				paramObj = new JdbcXML(paramObj.toString());
				break;
			default:
				break;
			}
		}
		
		return paramObj;
	}
	
	public String toString() {
		return name+":"+mode+":"+type;
	}
}
