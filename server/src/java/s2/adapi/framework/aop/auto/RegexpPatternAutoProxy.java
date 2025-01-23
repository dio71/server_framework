package s2.adapi.framework.aop.auto;

import java.util.regex.Pattern;

import s2.adapi.framework.container.support.ServiceDefinition;

/**
 * 서비스의 구현 클래스 패키지 명과 서비스의 명칭을 Regexp 패턴 기반으로 매칭하여
 * auto-proxy를 적용여부를 결정하는 AutoProxy의 구현 클래스이다.
 * 아래와 같이 서비스 설정 파일에 등록하면 자동으로 auto-proxy로 등록되어 적용된다.
 * <pre>
 *  &lt;service name="autoproxy"
 *           class="s2.adapi.framework.aop.auto.RegexpPatternAutoProxy"
 *           activate="true"
 *           pre-init="true"
 *           singleton="true"&gt;
 *       &lt;property name="interceptor" ref="logInterceptor"/&gt;
 *       &lt;property name="packagePattern" value="phis\..*"/&gt;
 *  &lt;/service&gt;
 * </pre>
 * @author 김형도
 *
 */
public class RegexpPatternAutoProxy extends AbstractAutoProxy {

	public static final int OPERATOR_AND = 0;
	public static final int OPERATOR_OR = 1;
	
	private Pattern packagePattern = null;
	private Pattern namePattern = null;
	private int opCode = OPERATOR_AND;
	
	/**
	 * auto-proxy를 적용할 구현 클래스의 패키지 패턴을 regexp 문자열로 지정한다.
	 * 지정하지 않으면 모든 패키지에대하여 매칭되는 것으로 처리된다.
	 * @param pkgPattern
	 */
	public void setPackagePattern(String pkgPattern) {
		if (pkgPattern != null) {
			packagePattern = Pattern.compile(pkgPattern);
		}
	}
	
	/**
	 * auto-proxy를 적용할 서비스 명칭의 패턴을 regexp 문자열로 지정한다.
	 * 지정하지 않으면 모든 패키지에대하여 매칭되는 것으로 처리된다.
	 * @param pkgPattern
	 */
	public void setNamePattern(String nmPattern) {
		if (nmPattern != null) {
			namePattern = Pattern.compile(nmPattern);
		}
	}
	
	/**
	 * 패키지 패턴과 서비스 명칭의 패턴 조건을 OR 또는 AND 로 지정한다.
	 * 지정하지 않으면 AND 조건으로 적용된다.
	 * @param opName
	 */
	public void setOperator(String opName) {
		if (opName != null && opName.equalsIgnoreCase("or")) {
			opCode = OPERATOR_OR;
		}
	}
	
	protected boolean isMatch(ServiceDefinition svcDef) {
		boolean matchPackage = true;
		boolean matchName = true;
		
		if (packagePattern != null) {
			matchPackage = packagePattern.matcher(svcDef.getServiceClass()).matches();
		}
		
		if (namePattern != null) {
			matchName = namePattern.matcher(svcDef.getServiceName()).matches();
		}
		
		boolean match = false;
		switch(opCode) {
		case OPERATOR_OR:
			match = matchPackage || matchName;
			break;
		default:
			match = matchPackage && matchName;
			break;
		}

		return match;
	}

}
