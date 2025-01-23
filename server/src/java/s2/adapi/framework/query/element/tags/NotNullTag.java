package s2.adapi.framework.query.element.tags;

import s2.adapi.framework.vo.ValueObject;

public class NotNullTag extends NullTag {
	
	public NotNullTag(String propName, int valueType) {
		super(propName,valueType);
	}
	
	public boolean isCondition(ValueObject params, int iterationIdx) {
		return !super.isCondition(params,iterationIdx);
	}
}
