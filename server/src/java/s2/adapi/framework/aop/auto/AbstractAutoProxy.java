package s2.adapi.framework.aop.auto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.aop.MethodInterceptorProxy;
import s2.adapi.framework.container.support.ServiceDefinition;
import s2.adapi.framework.util.ObjectHelper;

/**
 * MethodInterceptorProxy를 자동으로 적용해주는 AutoProxy의 추상 클래스이다.
 * 하위 클래스는 isMatch()메소드를 구현해야한다.
 * @author 김형도
 *
 */
public abstract class AbstractAutoProxy extends MethodInterceptorProxy implements AutoProxy {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractAutoProxy.class);
	
	/**
	 * svcObject를 감싸는 Proxy 객체를 생성하여 반환한다.
	 */
	public Object proxyProcess(Object svcObject, ServiceDefinition svcDef) {
		if (svcObject instanceof AutoProxy || !isMatch(svcDef)) { // autoproxy 적용하지 않음.
			return svcObject;
		} else {
			if ( log.isDebugEnabled()) {
				log.debug("### apply auto-proxy [" + this.getClass().getName() + "] to " + svcDef.getServiceName());
			}
			MethodInterceptorProxy proxy = createInterceptorProxy();
			proxy.setTarget(svcDef.getServiceName(), svcObject);
			
			return proxy.newProxyInstance(ObjectHelper.getAllInterfacesOf(svcObject.getClass()), 
					svcObject.getClass().getClassLoader());
		}
	}

	/**
	 * 적용될 서비스 여부를 반환함.
	 * @param svcDef
	 * @return
	 */
	abstract protected boolean isMatch(ServiceDefinition svcDef);
	
	/**
	 * 자신의 interceptor 목록을 동일하게 갖는 InterceptorProxy객체를 생성하여 반환한다.
	 * @return
	 */
	protected MethodInterceptorProxy createInterceptorProxy() {
		MethodInterceptorProxy proxy = new MethodInterceptorProxy();
		for(int i=0; i<this.interceptorList.size();i++) {
			proxy.setInterceptor(this.interceptorList.get(i));
		}
		
		return proxy;
	}
}
