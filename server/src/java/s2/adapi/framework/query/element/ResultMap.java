package s2.adapi.framework.query.element;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * ResultMap은 Sql의 조회 결과로 컬럼의 데이터를 읽을 때 어떤 ColumnReader를 사용하여 읽을지을
 * 지정하기 위한 클래스이다.
 * ColumnReader를 지정할 때 조회된 컬럼 명을 기준으로 ColumnReader를 지정하거나, 또는
 * 조회된 컬럼의 타입을 기준으로 ColumnReader를 지정할 수 있다.
 * ColumnReader를 지정할 때 컬럼명과 컬럼타입에 대하여 중복되어 지정되었을 경우 컬럼명에 대한
 * ColumnReader가 먼저 지정된다.</p>
 * <p>
 * 프레임워크에서 기본적으로 제공하는 ColumnReader는 s2.adapi.framework.dao.reader.* 패키지 구조
 * 아래에 다음과 같은 ColumnReader들이 있다.
 * <ul>
 * <li>BigDecimalColumnReader
 * <li>BinaryStreamColumnReader
 * <li>BLOBColumnReader
 * <li>BooleanColumnReader
 * <li>ByteColumnReader
 * <li>CharStreamColumnReader
 * <li>CLOBColumnReader
 * <li>DateColumnReader
 * <li>DoubleColumnReader
 * <li>IntColumnReader
 * <li>LongColumnReader
 * <li>ObjectColumnReader
 * <li>OracleCursorReader
 * <li>RealColumnReader
 * <li>ScalarColumnReader
 * <li>ShortColumnReader
 * <li>StreamReader
 * <li>StringColumnReader
 * <li>TimeColumnReader
 * <li>TimeStampColumnReader
 * </ul>
 * </p>
 * @author 김형도
 * @since 4.0
 */
public class ResultMap {
	private Map<String,ResultMapItem> resultColumnMap = null;
	private Map<String,ResultMapItem> resultTypeMap = null;
	
	public void addResultMap(ResultMap rmap) {
		if (rmap == null) {
			return;
		}
		if (rmap.resultColumnMap != null) {
			if (resultColumnMap == null) {
				resultColumnMap = new HashMap<String,ResultMapItem>();
			}
			resultColumnMap.putAll(rmap.resultColumnMap);
		}
		if (rmap.resultTypeMap != null) {
			if (resultTypeMap == null) {
				resultTypeMap = new HashMap<String,ResultMapItem>();
			}
			resultTypeMap.putAll(rmap.resultTypeMap);
		}
	}
	
	public void addItem(ResultMapItem item) {
		if ( item == null ) {
			return;
		}
		
		if (item.isColumnItem()) {
			if ( resultColumnMap == null ) {
				resultColumnMap = new HashMap<String,ResultMapItem>();
			}
			resultColumnMap.put(item.getName(),item);
		} else {
			if (resultTypeMap == null) {
				resultTypeMap = new HashMap<String,ResultMapItem>();
			}
			resultTypeMap.put(item.getName(),item);
		}
	}
	
	/**
	 * Column 명 기준으로 등록된 ResultMapItem을 반환한다.
	 * @param name 컬럼명
	 * @return
	 */
	public ResultMapItem getItem(String name) {
		if (resultColumnMap == null) {
			return null;
		} else {
			return resultColumnMap.get(name);
		}
	}
	
	/**
	 * Type 기준으로 등록된 ResultMapItem을 반환한다.
	 * @param type java.sql.Types에 정의된 JDBC type 값
	 * @return
	 */
	public ResultMapItem getItem(int type) {
		if (resultTypeMap == null) {
			return null;
		} else {
			return resultTypeMap.get(SqlTypes.getName(type));
		}
	}
	
	public String toString() {
		return "[resultColumnMap="+resultColumnMap+",resultTypeMap="+resultTypeMap+"]";
	}
}
