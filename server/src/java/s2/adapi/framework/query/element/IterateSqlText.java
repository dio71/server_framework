package s2.adapi.framework.query.element;

import java.util.List;

import s2.adapi.framework.vo.ValueObject;

/**
 * 컬럼 항목의 개수에 따라서 SQL이 생성되는 기능을 제공한다.
 * @author 김형도
 * @since 4.0
 */
public class IterateSqlText extends DynamicSqlText {

	private String propName = null;
	private String openTag = null;
	private String closeTag = null;
	private String conTag = null;
	
	public IterateSqlText(String prop, String open, String close, String conjunction) {
		propName = prop;
		openTag = open;
		closeTag = close;
		conTag = conjunction;
	}
	
	public void appendSql(ValueObject params, StringBuilder sb,
			List<SqlParameter> sqlParams,int paramIdx) {
		int count = getIterationCount(params);
		
		// 반복 회수 만큼 처리한다.
		for(int k=0;k<count;k++) {
			if (k == 0) { // iteration 이 들어가는 경우에만 opentag 를 붙인다.
				sb.append(openTag);
			}
			
			for(int i=0;i<children.size();i++) {
				children.get(i).appendSql(params,sb,sqlParams,k);
			}
			
			// tag 붙이기
			if (k < count-1) { // iteration 중간, conjunction을 추가
				sb.append(conTag);
			} 
			else { // 마지막 iteration, close를 추가
				sb.append(closeTag); 
			}
		}
	}

	/**
	 * 주어진 파라메터값들로부터 반복 회수를 구한다.
	 * @param params
	 * @return
	 */
	private int getIterationCount(ValueObject params) {
		int count = 0;
		
		Object propObj = null;
		if (params != null && params.size() > 0) {
			propObj = params.get(propName);
		}
		
		if (propObj == null) {
			count = 0;
		} 
		else {
			if (propObj instanceof List<?>) {
				count = ((List<?>)propObj).size();
			} 
			else if (propObj instanceof Object[]) {
				count = ((Object[])propObj).length;
			} 
			else {
				count = 1;
			}
		}
		
		return count;
	}
}
