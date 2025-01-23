package s2.adapi.framework.query.element;

import java.util.List;

import s2.adapi.framework.query.element.tags.Tag;
import s2.adapi.framework.vo.ValueObject;

/**
 * 조건에 따라서 동적으로 조립되는 SQL 기능을 제공한다.
 * @author 김형도
 * @since 4.0
 */
public class ConditionalSqlText extends DynamicSqlText {
	
	protected Tag tag = null;
	
	public ConditionalSqlText(Tag tag) {
		this.tag = tag;
	}
	
	/**
	 * 동적조건이 만족되면 child로 가지고 있는 모든 SqlText들을 모두 append하고,
	 * 만족되지 않으면 그냥 리턴한다.
	 */
	public void appendSql(ValueObject params, StringBuilder sb, List<SqlParameter> sqlParams, 
			int iterationIdx) {
		if ( tag.isCondition(params,iterationIdx)) {
			for(int i=0;i<children.size();i++) {
				children.get(i).appendSql(params,sb,sqlParams,iterationIdx);
			}
		} else {
			return;
		}
	}
	
	public String toString() {
		return children.toString();
	}
}
