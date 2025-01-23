package s2.adapi.framework.aop.auto;

import s2.adapi.framework.aop.InterceptorProxy;
import s2.adapi.framework.container.support.ServiceDefinition;

/**
 * 모든 서비스에 자동으로 적용되는 InterceptorProxy의 확장 인터페이스이다.
 * AutoProxy를 구현한 서비스는 ServiceContainer에 의하여 자동으로 autoproxy로 등록된다.
 * @author 김형도
 *
 */
public interface AutoProxy extends InterceptorProxy {
	public Object proxyProcess(Object svcObject, ServiceDefinition svcDef);
}
