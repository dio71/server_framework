package s2.adapi.framework.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import s2.adapi.framework.exception.ApplicationException;
import s2.adapi.framework.vo.ValueObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ValueObject를 생성하는 다양한 Utility 기능을 제공한다.
 * 
 * @author 김형도
 * @version 3.0
 */
public class ValueObjectUtil {
	private static final Logger log = LoggerFactory.getLogger(ValueObjectUtil.class);

	/**
	 * 주어진 객체가 ValueObject로 변환이 가능한지 여부를 반환한다.
	 * 변환이 가능한 경우는 List&lt;Map&gt; 또는 Map[] 인 경우이다.
	 * @param from
	 * @return
	 */
	static public boolean isAssignable(Object from) {
		if (from == null) {
			return true;
		} else if (from instanceof ValueObject) {
			return true;
		} else if (from instanceof Object[]) {
			Object[] oa = (Object[])from;
			if (oa.length == 0) {
				return true;
			} else {
				Object o = oa[0]; // 첫번째 element만 확인한다.
				if (o instanceof Map<?,?>) {
					return true;
				} else {
					log.debug("assigns ValueObject from Object[], "+
							"but first element is not instance of Map: "+
							o.getClass().getName());
					return false;
				}
			}
		} else if (from instanceof List<?>) {
			List<?> l = (List<?>)from;
			if (l.size() == 0) {
				return true;
			} else {
				Object o = l.get(0);
				if (o instanceof Map<?,?>) {
					return true;
				} else {
					log.debug("assigns ValueObject from List, "+
							"but first element is not instance of Map: "+
							o.getClass().getName());
					return false;
				}
			}
		} else if (from instanceof Map<?,?>){
			return true;
		} else {
			log.debug("assigns ValueObject from Object, "
					+"but incompatible type: "+
					from.getClass().getName());
			return false;
		}
	}
	
	/**
	 * 주어진 객체로부터 ValueObject를 생성하여 반환한다.
	 * @param from
	 * @return
	 * @throws ClassCastException 변환 실패시
	 */
	@SuppressWarnings("unchecked")
	static public ValueObject build(Object from) {
		if (from instanceof Object[]) {
			Object[] oa = (Object[])from;
			if (oa.length == 0) {
				return new ValueObject(); // return empty VO
			} else {
				// 배열의 각각을 Map으로 casting 하여 VO에 추가
				ValueObject vo = new ValueObject();
				for(int i=0;i<oa.length;i++) {
					vo.add((Map<String,Object>)oa[i]);
				}
				return vo;
			}
		} else if (from instanceof List) {
			List<?> l = (List<?>)from;
			if (l.size() == 0) {
				return new ValueObject(); // return empty VO
			} else {
				// List의 elemement들을 Map으로 casting 하여 VO에 추가
				ValueObject vo = new ValueObject();
				for(int i=0;i<l.size();i++) {
					vo.add((Map<String,Object>)l.get(i));
				}
				return vo;
			}
		} else if (from instanceof Map) {
			// row가 1건인 ValueObject를 생성하여 리턴
			Map<String,Object> m = (Map<String,Object>)from;
			ValueObject vo = new ValueObject();
			vo.add(m);
			return vo;
		} else {
			return (ValueObject)from; // 무조건 VO로 casting하여 반환
		}
	}
	
	/**
	 * ValueObject의 복사본을 생성한다. ValueObject 내부에서 사용하는 ArrayList와 HashMap은 복사되지만
	 * HashMap에 담겨져 있는 Value는 복사되지 않고 참조된다.
	 * 
	 * @param srcVO ValueObject
	 * @return srcVO의 복사본
	 */
	static public ValueObject clone(ValueObject srcVO) {
		ValueObject retVO = new ValueObject(srcVO.getName());
		for (int i = 0; i < srcVO.size(); i++) {
			retVO.add(new HashMap<String,Object>(srcVO.get(i)));
		}

		return retVO;
	}

	/**
	 * ValueObject에 있는 데이터들의 컬러명들을 리스트로 반환한다.
	 * @param srcVO
	 * @param row 기준이 되는 row
	 * @return 컬럼명 배열
	 */
	static public String[] getColumnNames(ValueObject srcVO, int row) {
		List<String> columnNames = new ArrayList<String>();
		if (srcVO != null && row >= 0 && srcVO.size() > row) {
			Iterator<String> itor = srcVO.get(0).keySet().iterator();
			while(itor.hasNext()) {
				columnNames.add(itor.next());
			}
		}
		return columnNames.toArray(new String[columnNames.size()]);
	}
	
	/**
	 * ValueObject에 있는 데이터들의 컬러명들을 리스트로 반환한다. 
	 * 첫번째 열을 기준으로 뽑아낸다.
	 * @param srcVO
	 * @param row
	 * @return 컬럼명 배열
	 */
	static public String[] getColumnNames(ValueObject srcVO) {
		return getColumnNames(srcVO,0);
	}
	
	/**
	 * <p>
	 * 주어진 byte[]에서 문자열을 읽어들여(시스템의 default encoding을 사용)
	 * 필드 구분자로 잘라내어 ValueObject 객체를 생성한다. 
	 * 필드 구분자와 ValueObject을 작성할 때 사용하는 Key는 파라메터로 입력된다. 
	 * 하나의 문자열 라인의 필드 수가 파라메터로 전달된 필드명
	 * 수보다 많을 경우 남는 필드 데이터는 무시된다. 반대로 필드명의 수가 많을 경우 모자라는 필드 값들은 공백 문자열로 처리된다.
	 * </p>
	 * 
	 * @param is 문자열을 읽어들일 InputStream
	 * @param fieldKeys ValueObject 작성시 사용되는 필드명 리스트
	 * @param token 필드 구분자
	 * @param max 작성되는 최대 Row 수 제한 값, 0 이면 파일 끝까지 처리
	 * @return ValueObject
	 * @throws ApplicationException  파일을 읽을때 에러가 발생한경우("service.error.07001")
	 */
	static public ValueObject build(byte[] data, String[] fieldKeys,
			String token, int max) throws ApplicationException {
		return build(new ByteArrayInputStream(data),fieldKeys,token,max);
	}
	
	/**
	 * <p>
	 * 주어진 byte[]에서 문자열을 읽어들여(시스템의 default encoding을 사용)
	 * 필드 구분자로 잘라내어 ValueObject 객체를 생성한다. 
	 * 필드 구분자와 ValueObject을 작성할 때 사용하는 Key는 파라메터로 입력된다. 
	 * 하나의 문자열 라인의 필드 수가 파라메터로 전달된 필드명
	 * 수보다 많을 경우 남는 필드 데이터는 무시된다. 반대로 필드명의 수가 많을 경우 모자라는 필드 값들은 공백 문자열로 처리된다.
	 * </p>
	 * 
	 * @param data 문자열을 읽어들일 byte[]
	 * @param fieldKeys ValueObject 작성시 사용되는 필드명 리스트
	 * @param token 필드 구분자
	 * @return 생성된 ValueObject 객체
	 * @throws ApplicationException  파일을 읽을때 에러가 발생한경우("service.error.07001")
	 */
	static public ValueObject build(byte[] data, String[] fieldKeys,
			String token) throws ApplicationException {
		return build(new ByteArrayInputStream(data),fieldKeys,token,0);
	}
	
	/**
	 * <p>
	 * 주어진 InputStream에서 문자열을 읽어들여(시스템의 default encoding을 사용)
	 * 필드 구분자로 잘라내어 ValueObject 객체를 생성한다. 
	 * 필드 구분자와 ValueObject을 작성할 때 사용하는 Key는 파라메터로 입력된다. 
	 * 하나의 문자열 라인의 필드 수가 파라메터로 전달된 필드명
	 * 수보다 많을 경우 남는 필드 데이터는 무시된다. 반대로 필드명의 수가 많을 경우 모자라는 필드 값들은 공백 문자열로 처리된다.
	 * </p>
	 * 
	 * @param is 문자열을 읽어들일 InputStream
	 * @param fieldKeys ValueObject 작성시 사용되는 필드명 리스트
	 * @param token 필드 구분자
	 * @return ValueObject
	 * @throws ApplicationException  파일을 읽을때 에러가 발생한경우("service.error.07001")
	 */
	static public ValueObject build(InputStream is, String[] fieldKeys,	String token) {
		return build(is,fieldKeys,token,0);
	}
	
	/**
	 * <p>
	 * 주어진 InputStream에서 문자열을 읽어들여(시스템의 default encoding을 사용)
	 * 필드 구분자로 잘라내어 ValueObject 객체를 생성한다. 
	 * 필드 구분자와 ValueObject을 작성할 때 사용하는 Key는 파라메터로 입력된다. 
	 * 하나의 문자열 라인의 필드 수가 파라메터로 전달된 필드명
	 * 수보다 많을 경우 남는 필드 데이터는 무시된다. 반대로 필드명의 수가 많을 경우 모자라는 필드 값들은 공백 문자열로 처리된다.
	 * </p>
	 * 
	 * @param is 문자열을 읽어들일 InputStream
	 * @param fieldKeys ValueObject 작성시 사용되는 필드명 리스트
	 * @param token 필드 구분자
	 * @param max 작성되는 최대 Row 수 제한 값, 0 이면 파일 끝까지 처리
	 * @return 생성된 ValueObject 객체
	 * @throws ApplicationException  파일을 읽을때 에러가 발생한경우("service.error.07001")
	 */
	static public ValueObject build(InputStream is, String[] fieldKeys, String token, int max) {
		InputStreamReader reader = new InputStreamReader(is);
		ValueObject buildVO = null;
		try {
			buildVO = build(reader,fieldKeys,token,max);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}
		
		return buildVO;
	}
	
	/**
	 * <p>
	 * 주어진 Reader에서 문자열을 읽어 들여 필드 구분자로 잘라내어 ValueObject 객체를 생성한다. 필드 구분자와
	 * ValueObject을 작성할 때 사용하는 Key는 파라메터로 입력된다. 하나의 문자열 라인의 필드 수가 파라메터로 전달된 필드명
	 * 수보다 많을 경우 남는 필드 데이터는 무시된다. 반대로 필드명의 수가 많을 경우 모자라는 필드 값들은 공백 문자열로 처리된다.
	 * </p>
	 * 
	 * @param input 문자열을 읽어들일 Reader
	 * @param fieldKeys ValueObject 작성시 사용되는 필드명 리스트
	 * @param token 필드 구분자
	 * @param max 작성되는 최대 Row 수 제한 값, 0 이면 파일 끝까지 처리
	 * @return ValueObject
	 * @throws ApplicationException  파일을 읽을때 에러가 발생한경우("service.error.07001")
	 */
	static public ValueObject build(Reader input, String[] fieldKeys, String token, int max) {
		ValueObject retVO = new ValueObject();
		String oneLine = null;

		BufferedReader br = null;
		if (input instanceof BufferedReader) {
			br = (BufferedReader)input;
		} else {
			br = new BufferedReader(input);
		}
		
		boolean loop = true;
		int lineCount = 0;
		try {
			while (loop) {
				oneLine = br.readLine();

				if (oneLine != null) {
					String[] strs = oneLine.split(token);
					retVO.add(parseStringArrayToValueRow(strs, fieldKeys));
					lineCount++;
				} else {
					loop = false;
				}
				if (max > 0 && lineCount >= max) {
					loop = false;
				}
			}
		} catch (IOException ex) {
			if (log.isErrorEnabled()) {
				log.error("IOException while reading lines..", ex);
			}
			throw new ApplicationException("service.error.07001", ex); // 파일 처리 중 오류가 발생하였습니다.
		}
		finally {
			if (br != null) {
				try {
					br.close();
				} 
				catch (IOException e) {
				}
			}
		}
		
		return retVO;
	}

	/**
	 * <p>
	 * 주어진 Reader에서 문자열을 읽어 들여 필드 구분자로 잘라내어 ValueObject 객체를 생성한다.
	 * </p>
	 * 
	 * @param input 문자열을 읽어들일 BufferedReader
	 * @param fieldKeys ValueObject 작성시 사용되는 필드명 리스트
	 * @param token 필드 구분자
	 * @return ValueObject
	 * @throws ApplicationException 파일을 읽을때 에러가 발생한경우("service.error.07001")
	 */
	static public ValueObject build(Reader input, String[] fieldKeys, String token) {
		return build(input, fieldKeys, token, 0);
	}

	/**
	 * <p>
	 * 주어진 파일에서 문자열을 읽어 들여 필드 구분자로 잘라내어 ValueObject 객체를 생성한다.
	 * </p>
	 * 
	 * @param file 문자열을 읽어들일 File 객체
	 * @param fieldKeys ValueObject 작성시 사용되는 필드명 리스트
	 * @param token 필드 구분자
	 * @return ValueObject
	 * @throws ApplicationException 해당파일이 없을 경우("service.error.07002"), 
	 * 파일을 읽을때 에러가 발생한경우("service.error.07001")
	 */
	static public ValueObject build(File file, String[] fieldKeys, String token) {
		ValueObject retVO = null;
		Reader rf = null;
		try {
			FileInputStream in = new FileInputStream(file);
			rf = new InputStreamReader(in, "utf-8");
			retVO = build(rf, fieldKeys, token, 0);
		} 
		catch (FileNotFoundException ex) {
			throw new ApplicationException("service.error.07002", file.getAbsoluteFile(), ex); // 존재하지 않는 파일입니다. ({0})
		} 
		catch (UnsupportedEncodingException ex) {
			throw new ApplicationException("service.error.07003", "utf-8", ex); // {0}은 시스템이 지원하지 않는 인코딩 방식 입니다.
		} 
		catch (ApplicationException ex) {
			throw ex;
		} 
		finally {
			try {
				if (rf != null ) rf.close();
			} catch (IOException ex) {
				log.debug("File close error : ", ex);
			}
		}
		
		return retVO;
	}

	/**
	 * <p>
	 * 주어진 Reader에서 문자열을 읽어 들여 일정 길이(byte 수)로 ValueObject 객체를 생성한다. 잘라낼 길이와
	 * ValueObject을 작성할 때 사용하는 Key는 파라메터로 입력된다. 하나의 문자열 라인의 필드 수가 파라메터로 전달된 필드명
	 * 수보다 많을 경우 남는 필드 데이터는 무시된다. 반대로 필드명의 수가 많을 경우 모자라는 필드 값들은 공백 문자열로 처리된다.
	 * </p>
	 * 
	 * @param input 문자열을 읽어들일 BufferedReader
	 * @param fieldKeys ValueObject 작성시 사용되는 필드명 리스트
	 * @param flength 컬럼별 길이
	 * @param max 작성되는 최대 Row 수 제한 값, 0인 경우 파일끝까지 처리
	 * @return ValueObject
	 * @throws ApplicationException 파일을 읽을때 에러가 발생한경우("service.error.07001")
	 */
	static public ValueObject build(Reader input, String[] fieldKeys,
			int[] flength, int max) {
		ValueObject retVO = new ValueObject();
		String oneLine = null;

		BufferedReader br = null;
		if (input instanceof BufferedReader) {
			br = (BufferedReader)input;
		} else {
			br = new BufferedReader(input);
		}
		
		boolean loop = true;
		int lineCount = 0;
		try {
			while (loop) {
				oneLine = br.readLine();
				if (oneLine != null) {
					String[] strs = splitByFixedLength(oneLine, flength);
					retVO.add(parseStringArrayToValueRow(strs, fieldKeys));
					lineCount++;
				} else {
					loop = false;
				}
				if (max > 0 && lineCount >= max) {
					loop = false;
				}
			}
		} 
		catch (IOException ex) {
			throw new ApplicationException("service.error.07001", ex);
		}
		finally {
			if (br != null) {
				try {
					br.close();
				} 
				catch (IOException e) {
				}
			}
		}
		
		return retVO;
	}

	/**
	 * <p>
	 * byte[]로 부터 문자열을 읽어들여(시스템의 default encoding을 사용)
	 * 일정 길이(byte 수)를 기준으로 ValueObject 객체를 생성한다.
	 * </p>
	 * 
	 * @param data 문자열을 읽어들일 byte[]
	 * @param fieldKeys ValueObject 작성시 사용되는 필드명 리스트
	 * @param flength 컬럼별 길이
	 * @return ValueObject
	 * @throws ApplicationException 파일을 읽을때 에러가 발생한경우("com.error.00013")
	 */
	static public ValueObject build(byte[] data, String[] fieldKeys, int[] flength) {
		return build(new ByteArrayInputStream(data),fieldKeys,flength);
	}
	
	/**
	 * <p>
	 * 주어진 InputStream으로부터 문자열을 읽어들여(시스템의 default encoding을 사용)
	 * 일정 길이(byte 수)를 기준으로 ValueObject 객체를 생성한다.
	 * </p>
	 * 
	 * @param is 문자열을 읽어들일 InputStream
	 * @param fieldKeys ValueObject 작성시 사용되는 필드명 리스트
	 * @param flength 컬럼별 길이
	 * @return ValueObject
	 * @throws ApplicationException 파일을 읽을때 에러가 발생한경우("com.error.00013")
	 */
	static public ValueObject build(InputStream is, String[] fieldKeys,
			int[] flength) {
		InputStreamReader reader = new InputStreamReader(is);
		ValueObject buildVO = null;
		try {
			buildVO = build(reader,fieldKeys,flength);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}
		
		return buildVO;
	}
	
	/**
	 * <p>
	 * 주어진 Reader에서 문자열을 읽어들여(시스템의 default encoding을 사용)
	 * 일정 길이(byte 수)를 기준으로 ValueObject 객체를 생성한다.
	 * </p>
	 * 
	 * @param input 문자열을 읽어들일 Reader
	 * @param fieldKeys ValueObject 작성시 사용되는 필드명 리스트
	 * @param flength 컬럼별 길이
	 * @return ValueObject
	 * @throws ApplicationException 파일을 읽을때 에러가 발생한경우("com.error.00013")
	 */
	static public ValueObject build(Reader input, String[] fieldKeys,
			int[] flength) {
		return build(input, fieldKeys, flength, 0);
	}

	/**
	 * <p>
	 * 주어진 파일에서 문자열을 읽어들여(시스템의 default encoding을 사용)
	 * 일정 길이(byte 수)를 기준으로 ValueObject 객체를 생성한다.
	 * </p>
	 * 
	 * @param input 문자열을 읽어들일 BufferedReader
	 * @param fieldKeys ValueObject 작성시 사용되는 필드명 리스트
	 * @param flength 컬럼별 길이
	 * @return ValueObject
	 * @throws ApplicationException 해당파일이 없을 경우("service.error.07002"), 
	 * 파일을 읽을때 에러가 발생한경우("service.error.07001")
	 */
	static public ValueObject build(File file, String[] fieldKeys, int[] flength) {
		ValueObject retVO = null;
		Reader rf = null;
		try {
			FileInputStream in = new FileInputStream(file);
			rf = new InputStreamReader(in);
			retVO = build(rf, fieldKeys, flength, 0);
		} catch (FileNotFoundException ex) {
			throw new ApplicationException("service.error.07002", file.getAbsolutePath(), ex);
		} catch (ApplicationException ex) {
			throw ex;
		} finally {
			try {
				if (rf != null) rf.close();
			} catch (IOException ex) {
				log.error("File close error : ", ex);
			}
		}
		return retVO;
	}
	
	/**
	 * key[,index]:value 형태의 문자열 배열로부터 ValueObject 객체를 생성한다.
	 * 이러한 형태가 아닌 문자열은 무시한다.
	 * @param args
	 * @return
	 */
	static public ValueObject buildFromArgs(String[] args) {
		ValueObject argsVO = new ValueObject();

		for (int i = 0; i < ObjectHelper.getLength(args); i++) {
			String arg = StringHelper.null2void(args[i]);
			
			if (StringHelper.isNull(arg))
				continue; // arg is null

			String[] str = arg.split(":");
			if (str.length < 2)
				continue; // value is empty

			int voIdx = 0;
			String[] str2 = str[0].split(",");
			if (str2.length > 1) {
				voIdx = Integer.parseInt(str2[1]);
			}

			argsVO.set(voIdx, str2[0], str[1]);
		}
		
		return argsVO;
	}

	/**
	 * <p>
	 * "status▦aa▦bb▦cc▩I▦1▦2▦3▩I▦7▦8▦9▩" 와 같은 형태의 문자열을 받아서
	 * ValueObject를 생성한다. 여기서 ▦는 행구분자이며 ▩는 열구분자이다.
	 * 문자열의 첫번재 열 구분자까지는 컬럼명을 담고 있으며 그 이후부터는 실제 데이터가 담기는 구조이다.
	 * 이 문자열을 열구분자로 분리하고 다시 각각을 행구분자로 분리하여 ValueObject를 생성한다.
	 * </p>
	 * <p>
	 * ValueObject를 생성할 때 실제 컬럼 데이터가 컬럼 명의 개수보다 많으면 그 데이터는 무시되며
	 * 실제 컬럼 데이터가 컬럼 명의 개수보다 적으면 모자라는 컬럼 데이터는 "null" 로 채워진다.
	 * </p>
	 * <p>
	 * 아래는 실제 사용 예이며 그림과 같이 ValueObject 객체가 만들어진다.
	 * </p>
	 * <pre>
	 * String str = "status▦aa▦bb▦cc▩I▦1▦2▦3▩I▦7▦8▦9▩";
	 * ValueObject getVO = ValueObjectUtil.build(str,"▦","▩");
	 *  <table border=1 cellspacing=1 cellpadding=0>
	 *      <tr bgcolor="#ccccff" align=center>
	 *           <th>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 *           <th>status
	 *           <th>aa&nbsp;
	 *           <th>bb&nbsp;
	 *           <th>cc&nbsp;
	 *      </tr>     
	 *      <tr valign=top align=center>
	 *           <th bgcolor="#ccccff">row=0
	 *           <td>I
	 *           <td>1
	 *           <td>2
	 *           <td>3
	 *      </tr>     
	 *      <tr valign=top align=center>
	 *           <th bgcolor="#ccccff">row=1
	 *           <td>I
	 *           <td>7
	 *           <td>8
	 *           <td>9
	 *      </tr>
	 *  </table>
	 * </pre>
	 * @param reqStr 문자열
	 * @param colSep 행 구분자
	 * @param rowSep 열 구분자
	 * @return
	 */
	
	static public ValueObject build(String reqStr, String colSep, String rowSep) {
		ValueObject retVO = new ValueObject();
		
		if ( reqStr == null ) {
			return retVO;
		}

		List<String> rows = StringHelper.split(reqStr,rowSep);
		
		if ( rows.size() <= 1) {
			return retVO;
		}
		
		if ( (rows.get(rows.size()-1)).length() == 0 ) {
			// 마지막 행이 empty 문자열이면 제거한다.
			rows.remove(rows.size()-1);
		}
		
		// 첫번째 row는 컬럼명이 담겨 있으므로 이를 분리하여 List 로 담아 놓는다.
		String columnLine = rows.get(0);
		List<String> colNames = StringHelper.split(columnLine,colSep);
		//System.out.println("column="+colNames);
		int columnCount = colNames.size(); // 컬럼수
		for(int i=1;i<rows.size();i++) {
			List<String> row = StringHelper.split(rows.get(i),colSep,columnCount,null);
			//System.out.println("row["+i+"="+row);
			for(int j=0;j<columnCount;j++) {
				retVO.set(i-1,colNames.get(j),row.get(j));
			}
		}
		
		return retVO;
	}
	
	/**
	 * ValueObject에서 colName으로 주어진 컬럼들의 값을 pattern으로 매칭하여 해당되는 
	 * row 들만 ValueObject에 담아서 반환한다.
	 * @param pVO
	 * @param colName
	 * @param patter
	 * @param exact pattern과 똑같은 경우에만 select되는 경우는 true, 
	 * 	그렇지 않은 경우 pattern 문자가 *str*, *str 또는 str* 형태로 가정하고 like 검색함.
	 * @return 해당되는 row들만 담은 ValueObject 객체를 반환한다. 해당되는 row가 없은 경우에는 빈 ValueObject 객체를 반환한다.
	 */
	static public ValueObject select(ValueObject pVO, String colName, String pattern, boolean exact) {
		if (pVO == null || pVO.size() == 0 || StringHelper.isNull(colName) || StringHelper.isNull(pattern)) {
			return new ValueObject();
		} else if (exact) {
			return selectExactmatch(pVO,colName,pattern);
		} else {
			if (pattern.startsWith("*") && pattern.endsWith("*")) {
				return selectAnymatch(pVO,colName,pattern.substring(1,pattern.length()-1));
			} else if (pattern.startsWith("*")) {
				return selectPostmatch(pVO,colName,pattern.substring(0,pattern.length()-1));
			} else if (pattern.endsWith("*")) {
				return selectPrematch(pVO,colName,pattern.substring(1,pattern.length()));
			} else {
				return selectExactmatch(pVO,colName,pattern);
			}
		}
	}
	
	static public ValueObject select(ValueObject pVO, String colName, String pattern) {
		return select(pVO,colName,pattern,true);
	}
	
	static private ValueObject selectPrematch(ValueObject pVO, String colName, String str) {
		ValueObject retVO = new ValueObject();
		String value = null;
		for(int i=0;i<pVO.size();i++) {
			value = pVO.getString(i, colName, null);
			if (value != null && value.startsWith(str)) {
				retVO.add(pVO.get(i));
			}
		}
		
		return retVO;
	}
	
	static private ValueObject selectPostmatch(ValueObject pVO, String colName, String str) {
		ValueObject retVO = new ValueObject();
		String value = null;
		for(int i=0;i<pVO.size();i++) {
			value = pVO.getString(i, colName, null);
			if (value != null && value.endsWith(str)) {
				retVO.add(pVO.get(i));
			}
		}
		
		return retVO;
	}
	
	static private ValueObject selectAnymatch(ValueObject pVO, String colName, String str) {
		ValueObject retVO = new ValueObject();
		String value = null;
		for(int i=0;i<pVO.size();i++) {
			value = pVO.getString(i, colName, null);
			if (value != null && value.contains(str)) {
				retVO.add(pVO.get(i));
			}
		}
		
		return retVO;
	}
	
	static private ValueObject selectExactmatch(ValueObject pVO, String colName, String str) {
		ValueObject retVO = new ValueObject();
		String value = null;
		for(int i=0;i<pVO.size();i++) {
			value = pVO.getString(i, colName, null);
			if (str.equals(value)) {
				retVO.add(pVO.get(i));
			}
		}
		
		return retVO;
	}
	
	// 주어진 문자열을 flegnth에 주어진 길이대로 잘라서 배열에 담는다.
	static private String[] splitByFixedLength(String text, int[] flength) {
		String[] strs = new String[flength.length];
		byte[] byteBuf = text.getBytes();
		int byteBufLen = byteBuf.length;
		int begin = 0;
		int end = 0;

		for (int i = 0; i < flength.length; i++) {
			end = begin + flength[i];
			if (end > byteBufLen) {
				end = byteBufLen;
			}
			strs[i] = new String(byteBuf, begin, end - begin);
			begin = end;
		}

		return strs;
	}

	// 주어진 문자열 배열을 fieldKeys에 주어진 key 명으로 Hashmap에 담아 리턴한다.
	static private Map<String,Object> parseStringArrayToValueRow(String[] strs,
			String[] fieldKeys) {
		Map<String,Object> row = new HashMap<String,Object>();

		for (int i = 0; i < fieldKeys.length; i++) {
			if (i < strs.length) {
				row.put(fieldKeys[i], strs[i].trim());
			} else {
				row.put(fieldKeys[i], "");
			}
		}

		return row;
	}
	
	/**
	 * ValueObject의 모든 row에 컬럼을 추가한다.
	 * @param pVO 컬럼을 추가할 VO 객체
	 * @param key 컬럼 명
	 * @param value 컬럼 값
	 */
	public static void addColumn(ValueObject pVO, String key, Object value) {
		if (pVO != null && pVO.size() > 0) {
			for(int i=0;i<pVO.size();i++) {
				pVO.set(i,key,value);
			}
		}
	}
	
	/**
	 * ValueObject의 모든 row에 Map 객체에 담긴 값들을 컬럼으로 추가한다.
	 * @param pVO 컬럼을 추가할 VO 객체
	 * @param columns 추가할 컬럼 값들이 담긴 Map 객체
	 */
	public static void addColumn(ValueObject pVO, Map<String,Object> columns) {
		if (pVO != null && pVO.size() > 0) {
			for(int i=0;i<pVO.size();i++) {
				pVO.get(i).putAll(columns);
			}
		}
	}
	
	/**
	 * 주어진 ValueObject를 오름차순으로 정렬한다. 
	 * 정렬할 때 기준으로 사용할 컬럼명을 두번째 파라메터로 전달한다.
	 * @param pVO
	 * @param sortField 정렬 기준 컬럼 명
	 */
	public static void sort(ValueObject pVO, String sortField) {
		MapComparator comparator = new MapComparator(sortField);
		Collections.sort(pVO, comparator);
	}
	
	/**
	 * 주어진 ValueObject를 오름차순 또는 내림차순으로 정렬한다. 
	 * 정렬할 때 기준으로 사용할 컬럼명을 두번째 파라메터로 전달한다.
	 * @param pVO
	 * @param sortField 정렬 기준 컬럼 명
	 * @param asc true면 오름차순, false면 내림차순으로 정렬
	 */
	public static void sort(ValueObject pVO, String sortField, boolean asc) {
		MapComparator comparator = new MapComparator(sortField,asc);
		Collections.sort(pVO, comparator);
	}
	
	/**
	 * 주어진 ValueObject를 오름차순으로 정렬한다. 
	 * 정렬할 때 기준으로 사용할 컬럼명을 두번째와 세번째 파라메터로 전달한다.
	 * @param pVO
	 * @param sortField1 첫번째 정렬 기준 컬럼 명
	 * @param sortField2 두번째 정렬 기준 컬럼 명
	 * @throws java.lang.ClassCastException 비교 대상이 되는 값들의 클래스가 서로 변환이 불가능한경우
	 */
	public static void sort(ValueObject pVO, String sortField1, String sortField2) {
		MapComparator comparator = new MapComparator(sortField1,sortField2);
		Collections.sort(pVO, comparator);
	}
	
	/**
	 * 주어진 ValueObject를 오름차순 또는 내림차순으로 정렬한다. 
	 * 정렬할 때 기준으로 사용할 컬럼명을 두번째와 세번째 파라메터로 전달한다.
	 * @param pVO
	 * @param sortField1 첫번째 정렬 기준 컬럼 명
	 * @param sortField2 두번째 정렬 기준 컬럼 명
	 * @param asc true면 오름차순, false면 내림차순으로 정렬
	 * @throws java.lang.ClassCastException 비교 대상이 되는 값들의 클래스가 서로 변환이 불가능한경우
	 */
	public static void sort(ValueObject pVO, String sortField1, String sortField2, boolean asc) {
		MapComparator comparator = new MapComparator(sortField1,sortField2, asc);
		Collections.sort(pVO, comparator);
	}
	
	/**
	 * 주어진 ValueObject의 searchField 컬럼에 searchValue 값이 존재하는지 검색한다.
	 * 해당 값이 존재하는 경우 존재하는 row의 인덱스를 반환하며, 존재하지 않는 경우에는 -1을 반환한다.
	 * 
	 * @param pVO 검색 대상이 되는 ValueObject 객체
	 * @param searchField 검색하고자하는 컬럼명
	 * @param searchValue 검색하고자하는 객체값
	 * @return 해당 값이 존재하는 첫번째 row의 인덱스, 존재하지 않는 경우는 -1
	 * @throws java.lang.ClassCastException 비교 대상이 되는 값들의 클래스가 서로 변환이 불가능한경우
	 */
	public static int search(ValueObject pVO, String searchField, Object searchValue) {
		return search(pVO,searchField,searchValue,0);
	}

	/**
	 * 주어진 ValueObject의 searchField 컬럼에 searchValue 값이 존재하는지 검색한다.
	 * 해당 값이 존재하는 경우 존재하는 row의 인덱스를 반환하며, 존재하지 않는 경우에는 -1을 반환한다.
	 * 
	 * @param pVO 검색 대상이 되는 ValueObject 객체
	 * @param searchField 검색하고자하는 컬럼명
	 * @param searchValue 검색하고자하는 객체값
	 * @param startidx 검색 시작할 ValueObject의 row index
	 * @return 해당 값이 존재하는 첫번째 row의 인덱스, 존재하지 않는 경우는 -1
	 * @throws java.lang.ClassCastException 비교 대상이 되는 값들의 클래스가 서로 변환이 불가능한경우
	 */
	public static int search(ValueObject pVO, String searchField, Object searchValue, int startidx) {
		if (pVO == null || pVO.size() == 0) {
			return -1;
		}
		
		// 검색을 위한 Comparator 객체 생성
		MapComparator comparator = new MapComparator(searchField,null);
		
		// 검색할 값을 사용하여 검색용 Map 객체를 생성한다.
		Map<String,Object> searchRow = new HashMap<String,Object>();
		searchRow.put(searchField, searchValue);
		int size = pVO.size();
		for(int i=startidx;i<size;i++) {
			Map<String,Object> row = pVO.get(i);
			if (comparator.compare(row, searchRow) == 0	) {
				return i;
			}
		}
		return -1;
	}
	
	private static class MapComparator implements Comparator<Map<String,Object>> {
		private String sortKey1 = null;
		private String sortKey2 = null;
		private boolean ascend = true;
		
		public MapComparator(String key, boolean asc) {
			sortKey1 = key;
			ascend = asc;
		}
		
		public MapComparator(String key) {
			sortKey1 = key;
		}
		
		public MapComparator(String key1, String key2, boolean asc) {
			sortKey1 = key1;
			sortKey2 = key2;
			ascend = asc;
		}
		
		public MapComparator(String key1, String key2) {
			sortKey1 = key1;
			sortKey2 = key2;
		}
		
		public int compare(Map<String,Object> src, Map<String,Object> dest) {
			int comValue = 0;
			String srcComp = null;
			String destComp = null;
			
			if (sortKey1 != null) {
				srcComp = toString(src.get(sortKey1), null);
				destComp = toString(dest.get(sortKey1), null);
				comValue = compareItem(srcComp,destComp);
			}
			
			if (comValue == 0 && sortKey2 != null) {
				srcComp = toString(src.get(sortKey2), null);
				destComp = toString(dest.get(sortKey2), null);
				comValue = compareItem(srcComp,destComp);
			}
			
			return comValue;
		}
		
		private int compareItem(String src, String dest) {
			if (src == null && dest == null) {
				return 0;
			}
			if (ascend) {
				if (src == null) {
					return -1;
				} else if (dest == null) {
					return 1;
				} else {
					return src.compareTo(dest);
				}
			} else {
				if (dest == null) {
					return -1;
				} else if (src == null) {
					return 1;
				} else {
					return dest.compareTo(src);
				}
			}
		}
		
		private String toString(Object obj, String defaultValue) {
			if (obj == null) {
				return defaultValue;
			} else {
				return String.valueOf(obj);
			}
		}
	}
}


			