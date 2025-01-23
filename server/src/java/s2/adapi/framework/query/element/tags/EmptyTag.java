package s2.adapi.framework.query.element.tags;

import s2.adapi.framework.vo.ValueObject;

/**
 * 파라메터 명에 해당되는 데이터가 없거나 Empty String이면 true를 리턴한다.
 * @author 김형도
 * @since 4.0
 */
public class EmptyTag extends Tag {

	protected String propName = null;
	protected boolean iterative = false;
	
	public EmptyTag(String propName, int valueType) {
		super(valueType);
		if (propName != null && propName.endsWith("[]")) {
			// iterative 명칭이다.
			iterative = true;
			this.propName = propName.substring(0,propName.length()-2);
		} else {
			iterative = false;
			this.propName = propName;
		}
	}
	
	public boolean isCondition(ValueObject params, int iterationIdx) {
		Object value = null;
		if (iterative) {
			value = getCompareValue(params,propName,iterationIdx);
		} else {
			value = getCompareValue(params,propName,-1);
		}

	    if ( value == null || String.valueOf(value).equals("")) {
	    	return true;
	    } else {
	    	return false;
	    }
	}
}
