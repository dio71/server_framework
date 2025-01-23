package s2.adapi.framework.query.element.tags;

import s2.adapi.framework.vo.ValueObject;

public class NotEmptyTag extends EmptyTag {
	
	public NotEmptyTag(String propName,int valueType) {
		super(propName,valueType);
	}
	
	public boolean isCondition(ValueObject params, int iterationIdx) {
		return !super.isCondition(params,iterationIdx);
	}

}
