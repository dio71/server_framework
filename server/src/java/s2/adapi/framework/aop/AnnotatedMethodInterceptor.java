package s2.adapi.framework.aop;

import java.util.HashSet;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Annotation 으로 적용여부를 판단할 수 있도록 기능을 제공하는 MethodInterceptor 구현 클래스이다.
 * @author kimhd
 *
 */
public abstract class AnnotatedMethodInterceptor implements MethodInterceptor {

	// AOP 대상이되는 tag 를 담아둔다.
	private Set<String> tagSet = new HashSet<String>();
	
	public void setTag(String tag) {
		tagSet.add(tag);
	}
	
	protected boolean isTargetInvocation(MethodInvocation invocation) {
		Interception interception = invocation.getMethod().getAnnotation(Interception.class);
		if (interception == null) {
			// Annotation 이 설정되지 않은 Method 이다. 대상이 아니다.
			return false;
		}
		
		// 설정된 tag 가 없으면 무조건 AOP 대상
		if (tagSet == null || tagSet.size() == 0) {
			return true;
		}
		
		// Annotation 으로 설정된 tag 값들(, 로 나열) 중에 tagSet 에 포함된 것이 있으면 AOP 대상이다.
		String[] tags = interception.tag().split(",");
		for(String tag:tags) {
			if (tagSet.contains(tag)) {
				return true;
			}
		}
		
		return false;
	}
}
