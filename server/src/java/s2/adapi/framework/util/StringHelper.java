package s2.adapi.framework.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * String(TEXT) 관련 Live Framework의 Helper Class
 * </p>
 *
 * <p>
 * 아래 Method들은 모두 <code>static</code>으로 선언 되었기 때문에 유념하기 바란다.
 * </p>
 *
 * @author kimhd
 * @since 1.0
 */
public class StringHelper {

    /**
     * <p>
     * <strong>StringHelper</strong>의 default 컨스트럭터(Constructor).
     * </p>
     */
    protected StringHelper() {
    }

    /**
     * <p>
     * 여분의 빈 자리 만큼 왼쪽부터 패딩문자를 채운다.
     * </p>
     *
     * <pre>
     * String source = "300";
     * String result = StringHelper.lPad(source, 5, '#');
     * </pre>
     * <code>result</code>는 <code>"##300"</code> 을 가지게 된다.
     *
     * @param source 원본 문자열
     * @param len    원하고자 하는 문자열의 길이
     * @param pad    덧붙히고자 하는 문자
     * @return 패딩된 문자열
     */
    public static String lpad(String source, int len, char pad) {
        return lpad(source, len, pad, false);
    }

    /**
     * <p>
     * 여분의 빈 자리 만큼 왼쪽부터 패딩문자를 채운다.
     * </p>
     *
     * <pre>
     * String source = "300";
     * String result = StringHelper.lPad(source, 5, '#');
     * </pre>
     * <code>result</code>는 <code>"##300"</code> 을 가지게 된다.
     *
     * @param source 원본 문자열
     * @param len    원하고자 하는 문자열의 길이
     * @param pad    덧붙히고자 하는 문자
     * @param isTrim 문자열 trim 여부
     * @return 패딩된 문자열
     */
    public static String lpad(String source, int len, char pad, boolean isTrim) {

        if (isTrim) {
            source = source.trim();
        }

        for (int i = source.length(); i < len; i++) {
            source = pad + source;
        }
        return source;
    }


    /**
     * <p>
     * 여분의 빈 자리 만큼 오른쪽부터 패딩문자를 채운다.
     * </p>
     *
     * <pre>
     * String source = "300";
     * String result = StringHelper.rPad(source, 5, '#');
     * </pre>
     * <code>result</code>는 <code>"300##"</code> 을 가지게 된다.
     *
     * @param source 원본 문자열
     * @param len    원하고자 하는 문자열의 길이
     * @param pad    덧붙히고자 하는 문자
     * @return 패딩된 문자열
     */
    public static String rpad(String source, int len, char pad) {
        return rpad(source, len, pad, false);
    }

    /**
     * <p>
     * 여분의 빈 자리 만큼 오른쪽부터 패딩문자를 채운다.
     * </p>
     *
     * <pre>
     * String source = "300";
     * String result = StringHelper.rPad(source, 5, '#');
     * </pre>
     * <code>result</code>는 <code>"300##"</code> 을 가지게 된다.
     *
     * @param source 원본 문자열
     * @param len    원하고자 하는 문자열의 길이
     * @param pad    덧붙히고자 하는 문자
     * @param isTrim 문자열의 trim 여부
     * @return 패딩된 문자열
     */
    public static String rpad(String source, int len, char pad, boolean isTrim) {

        if (isTrim) {
            source = source.trim();
        }

        for (int i = source.length(); i < len; i++) {
            source = source + pad;
        }
        return source;
    }

    /**
     * <p>
     * 해당 문자열의 왼쪽 WhiteSpace({@link java.lang.Character#isWhitespace})를 지운다.
     * </p>
     *
     * @param source 문자열
     * @return 왼쪽 공백이 제거된 문자열
     */
    public static String ltrim(String source) {
        int strIdx = 0;
        char[] val = source.toCharArray();
        int lenIdx = val.length;

        while ((strIdx < lenIdx) && Character.isWhitespace(val[strIdx])) {
            strIdx++;
        }

        return (strIdx >= 0) ? source.substring(strIdx) : source;
    }


    /**
     * <p>
     * 해당 문자열의 오른쪽 WhiteSpace({@link java.lang.Character#isWhitespace})를 지운다.
     * </p>
     *
     * @param source 문자열
     * @return 오른쪽 공백이 제거된 문자열
     */
    public static String rtrim(String source) {
        int strIdx = 0;
        char[] val = source.toCharArray();
        int count = val.length;
        int lenIdx = count;

        while ((strIdx < lenIdx) && Character.isWhitespace(val[lenIdx - 1])) {
            lenIdx--;
        }

        return (lenIdx >= 0) ? source.substring(strIdx, lenIdx) : source;
    }

    /**
     * <p>
     * 문자열의 제일 처음글자를 대문자화 한다.
     * </p>
     *
     * <pre>
     * String source = "abcdefg";
     * String result = StringHelper.capitalize(source);
     * </pre>
     * <code>result</code>는 <code>"Abcdefg"</code> 을 가지게 된다.
     *
     * @param source 원본 문자였
     * @return 대문자화 된 문자열
     */
    public static String capitalize(String source) {
        return !isNull(source) ? source.substring(0, 1).toUpperCase() + source.substring(1).toLowerCase() : source;
    }

    /**
     * <p>
     * 대상문자열(source)에서 지정문자열(target)이 검색된 횟수를,
     * 지정문자열이 없으면 0 을 반환한다.
     * </p>
     *
     * <pre>
     * String source = "ar";
     * String target = "StringHelper Class &ar; search";
     * int result = StringHelper.search(source, target);
     * </pre>
     * <code>result</code>는 <code>2</code>을 가지게 된다.
     *
     * @param source 대상문자열
     * @param target 검색할 문자열
     * @return 지정문자열이 검색되었으면 검색된 횟수를, 검색되지 않았으면 0 을 반환한다.
     */
    public static int search(String source, String target) {
        int result = 0;
        String strCheck = new String(source);
        for (int i = 0; i < source.length();) {
            int loc = strCheck.indexOf(target);
            if (loc == -1) {
                break;
            } else {
                result++;
                i = loc + target.length();
                strCheck = strCheck.substring(i);
            }
        }
        return result;
    }

    /**
     * <p>
     * 배열을 받아 연결될 문자열로 연결한다. 이때 각 엘레멘트 사이에 구분문자열을 추가한다.
     * </p>
     *
     * <pre>
     * String[] source = new String[] {"AAA","BBB","CCC"};
     * String result = StringHelper.join(source,"+");
     * </pre>
     * <code>result</code>는 <code>"AAA+BBB+CCC"</code>를 가지게 된다.
     *
     * @param aryObj 문자열로 만들 배열
     * @param delim  각 엘레멘트의 구분 문자열
     * @return 연결된 문자열
     */
    public static String join(Object aryObj[], String delim) {
        StringBuilder sb = new StringBuilder();
        int i = aryObj.length;
        if (i > 0) {
            sb.append(aryObj[0].toString());
        }
        for (int j = 1; j < i; j++) {
            sb.append(delim);
            sb.append(aryObj[j].toString());
        }

        return sb.toString();
    }

    /**
     * <p>
     * 문자열의 byte 길이 체크 한다. (한글은 2byte 취급)
     * </p>
     *
     * <pre>
     * String source = "A123456BB";
     * int result = StringHelper.getByteLength(source);
     * </pre>
     * <code>result</code>는 <code>10</code>을 가지게 된다.
     *
     * @param source 문자열
     * @return 문자열의 길이
     */
    public static int getByteLength(String source) {
        return source.getBytes().length;
    }

    /**
     * <p>
     * 문자열의 Null 이나 공백 문자열 여부를 판단한다.
     * isTrim = true;
     * </p>
     *
     * <pre>
     * String source = "";
     * boolean result = StringHelper.isNull(source);
     * </pre>
     * <code>result</code>는 <code>true</code> 을 가지게 된다.
     *
     * @param source 문자열
     * @return NULL("", null) 여부
     */
    public static boolean isNull(String source) {
    	return (source == null || source.length() == 0);
    }

    public static boolean isEmpty(String source) {
    	return (source == null || source.length() == 0);
    }
    
    public static boolean isNotEmpty(String source) {
    	return (source != null && source.length() > 0);
    }
    
    /**
     * 모두 empty 일때만 true 를 리턴한다.
     * @param str1
     * @param str2
     * @return
     */
    public static boolean allEmpty(String str1, String str2) {
    	return ((str1 == null || str1.length() == 0) && (str2 == null || str2.length() == 0));
    }
    
    /**
     * 모두 empty 일때만 true 를 리턴한다.
     * @param str1
     * @param str2
     * @param str3
     * @return
     */
    public static boolean allEmpty(String str1, String str2, String str3) {
    	return ((str1 == null || str1.length() == 0) && (str2 == null || str2.length() == 0) && (str3 == null || str3.length() == 0));
    }
    
    /**
     * 하나라도 empty 면 true 를 리턴한다.
     * @param str1
     * @param str2
     * @return
     */
    public static boolean anyEmpty(String str1, String str2) {
    	return (str1 == null || str1.length() == 0 || str2 == null || str2.length() == 0);
    }
    
    /**
     * 하나라도 empty 면 true 를 리턴한다.
     * @param str1
     * @param str2
     * @param str3
     * @return
     */
    public static boolean anyEmpty(String str1, String str2, String str3) {
    	return (str1 == null || str1.length() == 0 || str2 == null || str2.length() == 0 || str3 == null || str3.length() == 0);
    }
    
    /**
     * 첫번째 empty 가 아닌 문자열을 반환한다.
     * @param str1
     * @param str2
     * @return
     */
    public static String coalesce(String str1, String str2) {
    	if (str1 == null || str1.length() == 0) {
    		return str2;
    	}
    	else {
    		return str1;
    	}
    }
    
    /**
     * 첫번째 empty 가 아닌 문자열을 반환한다.
     * @param str1
     * @param str2
     * @param str3
     * @return
     */
    public static String coalesce(String str1, String str2, String str3) {
    	if (str1 == null || str1.length() == 0) {
    		if (str2 == null || str2.length() == 0) {
    			return str3;
    		}
    		else {
    			return str2;
    		}
    	}
    	else {
    		return str1;
    	}
    }
    
    /**
     * <p>
     * 문자열의 Null 이나 공백 문자열 여부를 판단한다.
     * </p>
     *
     * <pre>
     * String source = "";
     * boolean result = StringHelper.isNull(source);
     * </pre>
     * <code>result</code>는 <code>true</code> 을 가지게 된다.
     *
     * @param source 문자열
     * @param isTrim Trim 여부 (default는 true);
     * @return NULL("",null) 여부
     */
    public static boolean isNull(String source, boolean isTrim) {
        boolean isNullString = false;
        if (isTrim && source != null) {
            source = source.trim();
        }
        if (source == null || "".equals(source)) {
            isNullString = true;
        }
        return isNullString;
    }

    /**
     * <p>
     * 문자열의 배열을 정렬한다.
     * </p>
     *
     * <pre>
     * String[] source = new String[]{"CCC","BBB","DDD","AAA"};
     * StringHelper.sortStringArray(source);
     * </pre>
     * <code>source</code>는 <code>[AAA,BBB,CCC,DDD]</code>
     *
     * @param source 정렬할 문자열의 배열
     */
    public static void sort(String[] source) {
        java.util.Arrays.sort(source);
    }

    /**
     * <p>
     * 문자열을 받아 null이나 문자열이 space로 이뤄진
     * "   " - 문자열 일 경우 ""으로 변환 한다.
     * 해당 사항 없을 경우 원본 문자열을 리턴 한다.
     * </p>
     *
     * <pre>
     * String source = null;
     * String result = StringHelper.null2void(source);
     * </pre>
     * <code>result</code>는 <code>""</code>를 가지게 된다.
     *
     * @param source 문자열
     * @return source 변환된 문자열
     */
    public static String null2void(Object source) {
    	return null2string(source,"");
    }

    /**
     * <p>
     * 파라미터가 null이나 "", " " 이면 0 리턴
     * (Trim 여부 true).
     * </p>
     *
     * <pre>
     * String source = null;
     * int result = StringHelper.null2zero(source);
     * </pre>
     * <code>result</code>는 <code>0</code>를 가지게 된다.
     *
     * @param source 문자열
     * @return 변환된 int형
     */
    public static int null2int(Object source) {
    	String str = (source == null)?null:String.valueOf(source);
        if (isNull(str)) {
            return 0;
        }
        return Integer.parseInt(str);
    }

    /**
     * <p>
     * 파라미터가 null이나 "", " " 이면 0.0F 리턴
     * (Trim 여부 true).
     * </p>
     *
     * <pre>
     * String source = null;
     * float result = StringHelper.null2float(source);
     * </pre>
     * <code>result</code>는 <code>0.0F</code>를 가지게 된다.
     *
     * @param source 문자열
     * @return 변환된 float형
     */
    public static float null2float(Object source) {
    	String str = (source == null)?null:String.valueOf(source);
        if (isNull(str)) {
            return 0.0F;
        }
        return Float.parseFloat(str);
    }

    /**
     * <p>
     * 파라미터가 null이나 "", " " 이면 0.0D 리턴
     * (Trim 여부 true).
     * </p>
     *
     * <pre>
     * String source = null;
     * double result = StringHelper.null2double(source);
     * </pre>
     * <code>result</code>는 <code>0.0D</code>를 가지게 된다.
     *
     * @param source 문자열
     * @return 변환된 double형
     */
    public static double null2double(Object source) {
    	String str = (source == null)?null:String.valueOf(source);
        if (isNull(str)) {
            return 0.0D;
        }
        return Double.parseDouble(str);
    }

    /**
     * <p>
     * 파라미터가 null이나 "", " " 이면 0L 리턴
     * (Trim 여부 true).
     * </p>
     *
     * <pre>
     * String source = null;
     * long result = StringHelper.null2long(source);
     * </pre>
     * <code>result</code>는 <code>0</code>를 가지게 된다.
     *
     * @param source 문자열
     * @return 변환된 long형
     */
    public static long null2long(Object source) {
    	String str = (source == null)?null:String.valueOf(source);
        if (isNull(str)) {
            return 0L;
        }
        return Long.parseLong(str);
    }

        /**
     * <p>
     * 파라미터가 null이나 "", " " 이면 value를 리턴, 아니면 source를 리턴
     * </p>
     *
     * <pre>
     * String source = null;
     * String result = StringHelper.null2string(source,"0");
     * </pre>
     * <code>result</code>는 <code>"0"</code>를 가지게 된다.
     *
     * @param source 문자열
     * @return 변환된 문자열
     */
    public static String null2string(Object source, String value) {
    	String str = (source == null)?null:String.valueOf(source);
        if (isNull(str)) {
            return value;
        }
        return str;
    }
    
    /**
     * <p>
     * 문장열의 비교시 사용.
     * 기존 <strong>String</strong> Class의 <code>equals()</code> Method의 여러 문제점을 해결 하기 위함 Method이다.
     * <p/>
     *
     * <p>
     * 아래와 같이 두가지의 빈번하고 복잡한 비교 방식을
     * </p>
     * <pre>
     * 1.
     * String source = null;
     *
     * if(source == null || "".equals(source)){
     *     ...
     * }
     *
     * 2.
     * String target = "?"
     *
     * if(source != null && source.equals(target)) {
     *     ...
     * }
     * </pre>
     *
     * <p>
     * <code>strEquals()</code> Method를 이용하여 쉽게 할 수 있다.
     * </p>
     *
     * <pre>
     * 1.
     * String source = null;
     *
     * if(StringHelper.strEquals(source, "")){
     *     ...
     * }
     *
     * 2.
     * String target = "?";
     *
     * if(StringHelper.strEquals(source, target)) {
     *  ...
     * }
     * </pre>
     *
     * @param source 비교 문자열1
     * @param target 비교 문자열2
     * @return boolean 비교 결과 (true, false)
     */
    public static boolean equals(String source, String target) {
        return null2void(source).equals(null2void(target));
    }


    /**
     * <p>
     * 지정된 문자열의 일부분을 리턴함.
     * 기존 <strong>String</strong> Class의 <code>substring()</code>에서 자주 발생하는 <code>NullpointException</code>을
     * 고려하여 만든 Method이다.
     * </p>
     *
     * <pre>
     * String source = "200403"
     * String result = StringHelper.toSubString(source, 4, 8);
     * </pre>
     *
     * <code>result</code>는 <strong>IndexOutOfBoundException</strong>이 발생하지 않고 <code>"03"</code>을 가지게 된다.
     *
     * @param source     원본 문자열
     * @param beginIndex 시작위치
     * @param endIndex   마지막 위치
     * @return 해당 문자열
     */
    public static String substring(String source, int beginIndex, int endIndex) {

        if (equals(source, "")) {
            return source;
        } else if (source.length() < beginIndex) {
            return "";
        } else if (source.length() < endIndex) {
            return source.substring(beginIndex);
        } else {
            return source.substring(beginIndex, endIndex);
        }

    }

    /**
     * <p>
     * 지정된 문자열의 일부분을 리턴함.
     * 기존 <strong>String</strong> Class의 <code>substring()</code>에서 자주 발생하는 <code>NullpointException</code>을
     * 고려하여 만든 Method이다.
     * </p>
     *
     * <pre>
     * String source = "200403"
     * String result = StringHelper.toSubString(source, 8);
     * </pre>
     * <code>result</code>는 <strong>IndexOutOfBoundException</strong>이 발생하지 않고 <code>""</code>을 가지게 된다.
     *
     * @param source     원본 문자열
     * @param beginIndex 시작위치
     * @return 해당 문자열
     */
    public static String toSubString(String source, int beginIndex) {

        if (equals(source, "")) {
            return source;
        } else if (source.length() < beginIndex) {
            return "";
        } else {
            return source.substring(beginIndex);
        }

    }
    
    /**
     * 문자열을 delims를 기준으로 잘라서 List로 반환한다. delims 들 자체는 token으로 반환되지 않는다.
     * 내부적으로 StringTokenizer를 사용한다.
     * @param str
     * @param delims
     * @param returnDelims
     * @return
     */
    public static List<String> tokenize(String str, String delims) {
    	return tokenize(str,delims,false);
    }
    
    /**
     * 문자열을 delims를 기준으로 잘라서 List로 반환한다. delims 들 자체도 token으로 반환시킬지 여부를
     * returnDelims로 지정한다. 내부적으로 StringTokenizer를 사용한다.
     * @param str
     * @param delims
     * @param returnDelims
     * @return
     */
    public static List<String> tokenize(String str, String delims, boolean returnDelims) {
    	if (str == null) {
    		return null;
    	}
    	
    	List<String> tokens = new ArrayList<String>();
    	if (isNull(delims)) {
    		tokens.add(str);
    	} else {
    		StringTokenizer tokenizer = new StringTokenizer(str,delims,returnDelims);
    		while(tokenizer.hasMoreElements()) {
    			tokens.add(tokenizer.nextToken());
    		}
    	}
    	
    	return tokens;
    }
    
    /**
     * <p>
     * str 문자열을 delim을 기준으로 split하여 List에 담아준다.
     * <code>String.split(String regexp)</code>에서는 delimiter를 reqular expression으로
     * 표현하는데 비해 이 함수는 delimiter를 그대로 split하므로 빠르게 동작한다.
     * </p>
     * @param str 분할할  문자열
     * @param delim 문자열을 분할하기 위한 구분자
     * @return List 객체
     */
    public static List<String> split(String str, String delim) {
		if ( str == null ) {
			return null;
		}
		if ( delim == null ) {
			delim = "";
		}
		
		int beginIdx = 0;
		int endIdx = 0;
		List<String> splitList = new ArrayList<String>();
		int strLen = str.length();
		int delimLen = delim.length();
		
		do {
			endIdx = str.indexOf(delim, beginIdx);
			if ( endIdx >= 0 ) {
				splitList.add(str.substring(beginIdx, endIdx));
			} else {
				splitList.add(str.substring(beginIdx,strLen));
			}
			beginIdx = endIdx+delimLen;
		} while(endIdx >= 0);
		
		return splitList;
	}
	
    /**
     * 문자열을 delim 기준으로 split하여 주어진 dest 배열에 담는다. 
     * dest 배열의 크기 만큼 담기게 되며, 그 이상은 무시된다.
     * 만약 배열의 크기 보다 적게 나뉘게 되면 모자라는 부분은 defStr로 채워진다.
     * @param str 분할할 문자
     * @param delim null 이면 공백문자(" ")를 사용한다.
     * @param dest 분할될 문자가 담길 배열 객체
     * @param defStr 분할 개수가 모자랄 경우 채우기 위해 사용할 문자열
     */
    public static void split(String str, String delim, String[] dest, String defStr) {
		if (dest == null || dest.length == 0) {
			return;
		}
		int cnt = dest.length;
		
    	if ( str == null ) {
			str = "";
		}
		if ( delim == null ) {
			delim = " ";
		}
		
		int beginIdx = 0;
		int endIdx = 0;
		
		int strLen = str.length();
		int delimLen = delim.length();
		int count = 0;
		do {
			endIdx = str.indexOf(delim, beginIdx);
			if ( endIdx > 0 ) {
				dest[count] = str.substring(beginIdx, endIdx);
			} else {
				dest[count] = str.substring(beginIdx,strLen);
			}
			beginIdx = endIdx+delimLen;
			count++;
		} while(endIdx > 0 && count<cnt);
		
		for(int i=count;i<cnt;i++) {
			dest[i] = defStr;
		}
    }
    
    /**
     * <p>
     * str 문자열을 delim을 기준으로 split하여 List에 담아준다. 
     * cnt 개수 만큼만 담기며, 그 이상은 무시된다. 만약 cnt 보다 적게 나뉘게 되면 모자라는 부분은
     * defStr로 채워진다.
     * <code>String.split(String regexp)</code>에서는 delimiter를 reqular expression으로
     * 표현하는데 비해 이 함수는 delimiter를 그대로 split하므로 훨씬 빠르게 동작한다.
     * </p> 
     * @param str 분할할 문자열
     * @param delim 문자열을 분할하기 위한 구분자, null 이면 공백문자(" ")를 사용한다.
     * @param cnt 분할 개수
     * @param defStr 분할 개수가 모자랄 경우 채우기 위해 사용할 문자열
     * @return List 객체
     */
	public static List<String> split(String str, String delim, int cnt, String defStr) {
		if ( str == null ) {
			return null;
		}
		if ( delim == null ) {
			delim = " ";
		}
		if ( cnt <= 0 ) {
			return new ArrayList<String>();
		}
		List<String> splitList = new ArrayList<String>(cnt);
		
		int beginIdx = 0;
		int endIdx = 0;
		
		
		int strLen = str.length();
		int delimLen = delim.length();
		int count = 0;
		do {
			endIdx = str.indexOf(delim, beginIdx);
			if ( endIdx > 0 ) {
				splitList.add(str.substring(beginIdx, endIdx));
			} else {
				splitList.add(str.substring(beginIdx,strLen));
			}
			beginIdx = endIdx+delimLen;
			count++;
		} while(endIdx > 0 && count<cnt);
		
		for(int i=count;i<cnt;i++) {
			splitList.add(defStr);
		}
		
		return splitList;
	}
    
	private static final Pattern unicodePattern = Pattern.compile("\\\\u([0-9A-Fa-f]{4})");

	/**
	 * Unicode 로 표현댄 문자들을 실제 문자로 변경한다.
	 * 예를 들어 "Hello\u003aWorld"와 같이 Unicode 표현이 들어간 문자열을 넘기면
	 * 실제 문자열로 변경이된 "Hello:World"로 변경되어 반환된다.
	 * @param s Unicode 표현이 있는 문자열
	 * @return 실제 문자로 변경된 결과
	 */
	public static String unescapeUnicode(String s) {
		String res = s;
		Matcher m = unicodePattern.matcher(res);
		
		while (m.find()) {
			res = res.replaceAll("\\" + m.group(0), Character
					.toString((char) Integer.parseInt(m.group(1), 16)));
		}
		return res;
	}
	
	/**
	 * URLEncoder.encode() 함수의 경우 공백을 + 기호로 바꾸는 문제, Encoding Exception 을 throw 하는 문제가 있어 별도 함수로 제공함
	 * 
	 * @param str
	 * @return
	 */
	public static String encodeURI(String str) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<str.length();i++) {
			char c = str.charAt(i);
			switch (c) {
			case ' ':
				sb.append("%20");
				break;
			case '!':
				sb.append("%21");
				break;
			case '"':
				sb.append("%22");
				break;
			case '#':
				sb.append("%23");
				break;
			case '$':
				sb.append("%24");
				break;
			case '%':
				sb.append("%25");
				break;
			case '&':
				sb.append("%26");
				break;
			case '\'':
				sb.append("%27");
				break;
			case '(':
				sb.append("%28");
				break;
			case ')':
				sb.append("%29");
				break;
			case '+':
				sb.append("%2B");
				break;
			case ',':
				sb.append("%2C");
				break;
			case '/':
				sb.append("%2F");
				break;
			case ':':
				sb.append("%3A");
				break;
			case ';':
				sb.append("%3B");
				break;
			case '<':
				sb.append("%3C");
				break;
			case '=':
				sb.append("%3D");
				break;
			case '>':
				sb.append("%3E");
				break;
			case '?':
				sb.append("%3F");
				break;
			case '@':
				sb.append("%40");
				break;
			case '[':
				sb.append("%5B");
				break;
			case '\\':
				sb.append("%5C");
				break;
			case ']':
				sb.append("%5D");
				break;
			case '^':
				sb.append("%5E");
				break;
			case '`':
				sb.append("%60");
				break;
			case '{':
				sb.append("%7B");
				break;
			case '|':
				sb.append("%7C");
				break;
			case '}':
				sb.append("%7D");
				break;
			case '~':
				sb.append("%7E");
				break;	
			default:
				sb.append(c);
				break;
			}
		}
		
		return sb.toString();
	}
}
