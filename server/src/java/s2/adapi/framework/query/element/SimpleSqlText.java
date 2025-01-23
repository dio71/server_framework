package s2.adapi.framework.query.element;

import java.util.List;

import s2.adapi.framework.vo.ValueObject;

/**
 * Text로만 이루어진 SQL 문장을 표현한다.
 * @author 김형도
 * @since 4.0
 */
public class SimpleSqlText implements SqlText {
	
	protected String text = null;
	protected List<SqlParameter> params = null;
	
	public SimpleSqlText(String text, List<SqlParameter> params) {
		this.text = text;
		this.params = params;
	}
	
	public void appendSql(ValueObject paramVO, StringBuilder sb, List<SqlParameter> sqlParams, 
			int iterationIdx) {
		sb.append(text);
		if (sqlParams != null && params != null) {
			if (iterationIdx < 0) {
				sqlParams.addAll(params);
			} else {
				// paramIdx 값이 0 이상이라면 iteration 중이므로 그 값을 사용하여 SqlParamter를 복제하여 넣어야 한다.
				for(int i=0;i<params.size();i++) {
					sqlParams.add(params.get(i).clone().setIteration(iterationIdx)); 
				}
			}
		}
	}
	
	public String toString() {
		return "[text="+text+"]";
	}
}
