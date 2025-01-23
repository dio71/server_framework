package s2.adapi.framework.query.element;

import java.util.ArrayList;
import java.util.List;

import s2.adapi.framework.util.StringHelper;

/**
 * <p>
 * Inline parameter가 포함되어 있는 SQL 문장을 받아서 SqlText 객체들의 리스트로 파싱한다.
 * 파라메터 구분자는 #과 $를 사용하며 구분자를 문자열로 입력하기 위하여 각각 ##과 $$를 사용한다.
 * </p>
 * <p>
 * 다중 쓰레드에서 동기화는 보장하지 않는다.
 * </p>
 * @author 김형도
 * @since 4.0
 */
public class SqlTextParser {
	
	/**
	 * 문자열 치환 방식의 파라메터 구분자.
	 */
	private static final String REPLACE_PARAM_DELIM ="$";
	
	/**
	 * 바인딩 방식의 파라메터 구분자.
	 */
	private static final String BIND_PARAM_DELIM = "#";
	
	/**
	 * 두가지 방식의 파라메터 구분자 문자열로 StringHelper.tokenize()로 Token으로 쪼갤때 사용
	 */
	private static final String PARAMETER_DELIMS = "$#";
	
	/**
	 * 파싱 상태값. 현재 SQL 문장에 대한 토큰을 처리 중임을 나타냄
	 */
	private static final int IN_STATEMENT = 0;
	
	/**
	 * 파싱 상태값. 현재 바인딩 방식의 파라메터 내용을 처리 중임을 나타냄
	 */
	private static final int IN_BIND_PARAM = 1;
	
	/**
	 * 파싱 상태값. 현재 문자열 치환 방식의 파라메터 내용을 처리 중임을 나타냄
	 */
	private static final int IN_REPLACE_PARAM =2;
	
	/**
	 * 파싱되어 생성된 SqlText 객체 들을 순서대로 담아 놓는다.
	 */
	private List<SqlText> sqls = null;
	
	/**
	 * 현재 파싱 중인 Sql 문장을 담아 놓는다.
	 */
	private StringBuilder stmtText = null;
	
	/**
	 * 현재 파싱 중인 SqlParameter 객체를 순서대로 담아 놓는다.
	 */
	private List<SqlParameter> bindParams = null;
	
	/**
	 * 주어진 Sql 문장을 파싱하여 SqlText 객체의 배열로 반환한다.
	 * @param rawText
	 * @return
	 */
	public List<SqlText> parse(String rawText) {
		sqls = new ArrayList<SqlText>();
		stmtText = new StringBuilder();
		bindParams = null;
		
		List<String> tokens = StringHelper.tokenize(rawText,PARAMETER_DELIMS,true);
				
		StringBuilder sb = new StringBuilder();

		String token = null;
		String nextToken = null;
		int state = IN_STATEMENT;
		
		int tokenCount = tokens.size(); // excluding empty token added
		tokens.add(null); // adds a null string in order to making a next-token-dispatching-logic simple.
		
		for(int i=0;i<tokenCount;i++) {
			// get token & nextToken values
			token = tokens.get(i);
			nextToken = tokens.get(i+1);
			
			if (BIND_PARAM_DELIM.equals(token)) { // # delimeter
				if (BIND_PARAM_DELIM.equals(nextToken)) {	// escaped, handle as normal character
					sb.append(token);
					i++;
				} else {
					// token is delimeter
					if (state == IN_BIND_PARAM) { // within bind parameter --> change state to in-statement
						processToken(state,sb);
						state = IN_STATEMENT;
					} else if (state == IN_REPLACE_PARAM) { // within replace param --> treat it as a normal string
						sb.append(token);
					} else {// within statement --> change to bind param
						processToken(state,sb);
						state = IN_BIND_PARAM;
					}
				}
			} else if (REPLACE_PARAM_DELIM.equals(token)) { // $ delimiter
				if (REPLACE_PARAM_DELIM.equals(nextToken)) { // escaped, handle as normal character
					sb.append(token);
					i++;
				} else {
					// token is delimeter
					if (state == IN_BIND_PARAM) { // within bind parameter --> keep the state
						sb.append(token);
					} else if (state == IN_REPLACE_PARAM) { // within replace parameter --> change state to in-statement
						processToken(state,sb);
						state = IN_STATEMENT;
					} else { // within statement --> change state to replace-param
						processToken(state,sb);
						state = IN_REPLACE_PARAM;
					}
				}
			} else {
				sb.append(token);
			}
		}
		
		completeToken(state,sb); 
		
		return sqls;
	}

	private void processToken(int state, StringBuilder sb) {
		if (state == IN_STATEMENT) {
			stmtText.append(sb.toString());
		} else if (state == IN_BIND_PARAM){
			stmtText.append("?");
			if (bindParams == null) {
				bindParams = new ArrayList<SqlParameter>();
			}
			bindParams.add(new SqlParameter(sb.toString()));
		} else { // IN_REPLACE_PARAM
			// stmtBuffer 와 bindParams 로 SimpleSql 생성
			if ( stmtText.length() > 0 || bindParams != null) {
				sqls.add(new SimpleSqlText(stmtText.toString(),bindParams));
			}
			stmtText.setLength(0);
			bindParams = null;
			
			// sb의 내용으로 ArgumentSqlText 생성
			sqls.add(new ArgumentSqlText(sb.toString()));
		}
		
		sb.setLength(0);
	}
	
	private void completeToken(int state, StringBuilder sb) {
		if (state == IN_STATEMENT) {
			stmtText.append(sb.toString());
		}
		
		if ( stmtText.length() > 0 || bindParams != null) {
			sqls.add(new SimpleSqlText(stmtText.toString(),bindParams));
		}
	}
}
