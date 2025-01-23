package s2.adapi.framework.query.element.tags;

import s2.adapi.framework.vo.ValueObject;

public class NotEqualTag extends EqualTag {
	
	public NotEqualTag(String propName, String compareValue, int valueType) {
		super(propName,compareValue, valueType);
	}
	
	public boolean isCondition(ValueObject params, int iterationIdx) {
		return !super.isCondition(params,iterationIdx);
	}

}
