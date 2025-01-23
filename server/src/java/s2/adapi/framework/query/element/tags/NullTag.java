package s2.adapi.framework.query.element.tags;

import s2.adapi.framework.vo.ValueObject;

public class NullTag extends EmptyTag {
	
	public NullTag(String propName, int valueType) {
		super(propName,valueType);
	}
	
	public boolean isCondition(ValueObject params, int iterationIdx) {
		Object value = null;
		if (iterative) {
			value = getCompareValue(params,propName,iterationIdx);
		} else {
			value = getCompareValue(params,propName,-1);
		}

	    if ( value == null ) {
	    	return true;
	    } else {
	    	return false;
	    }
	}
}
