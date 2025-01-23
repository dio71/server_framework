package s2.adapi.framework.query.element;

import java.util.List;

import s2.adapi.framework.vo.ValueObject;

/**
 * 문자열 치환방식의 파라메터 하나를 표현하는 SqlText 클래스이다.
 * SQL 문장에서 $...$ 방식으로 표현되는 부분이다. 
 * ~로 시작하면 ' 를 '' 로 escaping 시켜준다.
 * 예) name을 key로하는 문자열 값이 kim's name 이라면 $~name$ --> kim''s name 이된다. 
 * @author 김형도
 * @since 4.0
 */
public class ArgumentSqlText implements SqlText {

	private SqlParameter arg = null;
	private boolean escaping = false;
	
	public ArgumentSqlText(String param) {
		if (param != null && param.startsWith("~")) {
			// escaping on
			param = param.substring(1);
			escaping = true;
		}
		
		this.arg = new SqlParameter(param);
	}
	
	public void appendSql(ValueObject params, StringBuilder sb,
			List<SqlParameter> sqlParams, int iterationIdx) {
		
		SqlParameter argTemp = null;
		if (iterationIdx < 0) {
			argTemp = arg;
		} else {
			argTemp = arg.clone().setIteration(iterationIdx);
		}
		
		Object paramObj = argTemp.getParameterObject(params,0);
		if (paramObj != null) {
			if (escaping) {
				sb.append(escapeLiteral(String.valueOf(paramObj)));
			} else {
				sb.append(String.valueOf(paramObj));
			}
		}
	}
	
	private String escapeLiteral(String arg) {
		if (arg == null) {
			return null;
		}
		if (arg.indexOf("'") < 0) {
			return arg;
		} else {
			int curIdx = 0;
			StringBuilder sb = new StringBuilder(arg.length()+10);
			for(int i=0;i<arg.length();i++) {
				char chr = arg.charAt(i);
				if (chr == '\'') {
					sb.append(arg.substring(curIdx,i));
					sb.append("''");
					curIdx = i+1;
				}
			}
			sb.append(arg.substring(curIdx));
			return sb.toString();
		}
	}
}
