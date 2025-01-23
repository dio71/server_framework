package s2.adapi.framework.query.element.tags;

import s2.adapi.framework.vo.ValueObject;

/**
 * 항상 true를 리턴한다.
 * @author 김형도
 * @since 4.0
 */
public class AlwaysTag extends Tag {

	public boolean isCondition(ValueObject params, int iterationIdx) {
		return true;
	}

}
