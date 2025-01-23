package s2.adapi.framework.query.element.tags;

import java.lang.reflect.Array;
import java.util.List;

import s2.adapi.framework.context.ContextManager;
import s2.adapi.framework.query.element.SqlParameter;
import s2.adapi.framework.vo.ValueObject;

/**
 * SQL 사용여부를 동적으로 결정하는 여러가지 Tag 클래스들이 구현할 Interface.
 * @author 김형도
 * @since 4.0
 */
public abstract class Tag {

	protected int valueType;
	
	protected Tag() {	
		valueType = SqlParameter.UNKNONW_VALUE_KIND;
	}
	
	protected Tag(int type) {
		valueType = type;
	}
	
	/**
	 * 동적으로 SQL사용 여부를 결정하는 메소드이다.
	 * @param params
	 * @return
	 */
	abstract public boolean isCondition(ValueObject params, int iterationIdx);
	
	protected Object getCompareValue(ValueObject params, String propName, int iterationIdx) {
		return getCompareValue(params,propName,iterationIdx,valueType);
	}
	
	/**
	 * 주어진 파라메터들로부터 비교 대상 객체를 꺼내온다.
	 * @param params
	 * @param propName
	 * @param iterationIdx
	 * @return
	 */
	protected Object getCompareValue(ValueObject params, String propName, int iterationIdx, 
			int vtype) {
		Object comObj = null;
		if (vtype == SqlParameter.VO_VALUE_KIND) { 
			if (params != null && params.size() > 0) {
				// ValueObject에서 주어진 propName을 사용하여 객체를 꺼내온다.
				comObj = params.get(propName);
				if (iterationIdx >= 0) { 
					// iteration 이 지정되었다면 객체가 List 또는 배열인 경우에만 
					// iterationIdx 번째 객체를 다시 꺼내온다.
					if (comObj != null) {
						if (comObj instanceof List<?>) {
							List<?> comListObj = (List<?>)comObj;
							if (iterationIdx < comListObj.size()) {
								comObj = comListObj.get(iterationIdx);
							} else {
								comObj = null;
							}
						} else if (comObj.getClass().isArray()) {
							if (iterationIdx < Array.getLength(comObj)) {
								comObj = Array.get(comObj,iterationIdx);
							} else {
								comObj = null;
							}
						}
					}
				}
			}
		}
		else if (vtype == SqlParameter.ROLE_VALUE_KIND) {
			comObj = ContextManager.getServiceContext().getRole(propName);
		}
		
		return comObj;
	}
	
	/**
	 * 비교할 값의 타입에 따라서 비교대상 문자열을 변환하여 비교한다.
	 * @param compared
	 * @param compareTo
	 * @return
	 */
	protected int compare(Object compared, String compareTo) {
		
		if (compared == null || compareTo == null) {
			return -1;
		}
		
		if (compared instanceof String) {
			return ((String)compared).compareTo(compareTo);
		}
		else if (compared instanceof char[]) {
			return new String((char[])compared).compareTo(compareTo);
		}
		else {
			return compared.toString().compareTo(compareTo);
		}
	}
}
