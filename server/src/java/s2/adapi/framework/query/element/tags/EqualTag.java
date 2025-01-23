package s2.adapi.framework.query.element.tags;

import s2.adapi.framework.vo.ValueObject;

public class EqualTag extends Tag {

	private String compareValue = null;
	private String propName = null;
	private boolean iterative = false;
	
	public EqualTag(String propName, String compareValue, int valueType) {
		super(valueType);
		if (propName != null && propName.endsWith("[]")) {
			// iterative 명칭이다.
			iterative = true;
			this.propName = propName.substring(0,propName.length()-2);
		} else {
			iterative = false;
			this.propName = propName;
		}
		this.compareValue = compareValue;
	}
	
	public boolean isCondition(ValueObject params, int iterationIdx) {
		Object value = null;
		
		if (iterative) {
			value = getCompareValue(params,propName,iterationIdx);
		} else {
			value = getCompareValue(params,propName,-1);
		}
		
		int result = compare(value,compareValue);
		
		return (result == 0);
	}

}
