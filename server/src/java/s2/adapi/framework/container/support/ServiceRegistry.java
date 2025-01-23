package s2.adapi.framework.container.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.container.ServiceContainerException;
import s2.adapi.framework.util.StringHelper;

/**
 * 서비스 정의 내역을 등록 및 조회하기 위한 클래스이다.
 * @author 김형도
 * @since 4.0
 */
public class ServiceRegistry {
	private Logger log = LoggerFactory.getLogger(ServiceRegistry.class);
	
	private Map<String,ServiceDefinition> serviceDefs = 
		new HashMap<String,ServiceDefinition>();
	
	/**
	 * 서비스 레지스트리에 등록된 모든 서비스 이름들의 집합을 리턴한다.
	 * @return
	 */
	public Set<String> getAllServiceNames() 
	{
		Set<String> nameSet = null;
		synchronized(serviceDefs) {
			nameSet = new HashSet<String>(serviceDefs.keySet());
		}
		return nameSet;
	}
	
	/**
	 * 서비스 정의를 등록한다.
	 * @param svcDef
	 */
	public void registerServiceDefinition(ServiceDefinition svcDef) 
			throws ServiceContainerException
	{
		if ( svcDef == null ) return;
		
		String svcName = svcDef.getServiceName();
		
		// 등록할 서비스 정의의 이름이 반드시 있어야 한다.
		if ( StringHelper.isNull(svcName) ) {
			throw new ServiceContainerException("invalid ServiceDefinition.");
		}
		
		if ( serviceDefs.containsKey(svcName) ) {
			// 기존에 이미 동일한 이름의 서비스 정의가 등록되어 있다.
			// 우선 순위를 비교하여 높은 우선순위의 서비스 정의를 재등록한다.
			ServiceDefinition prevDef = serviceDefs.get(svcName);
			if (prevDef.getLoadPriority() == svcDef.getLoadPriority()) {
				// 동일한 우선 순위이다. Exception을 던진다.
				throw new ServiceContainerException(svcName+" with same priority is already registered.");	
			} else if (prevDef.getLoadPriority() < svcDef.getLoadPriority()) {
				// 이미 등록되어 있는 서비스 정의의 우선순위가 더 낮으므로 새로운 서비스 정의로 교체한다.
				serviceDefs.put(svcName, svcDef);
				if (log.isInfoEnabled()) {
					log.info(svcName+" is replaced with higher priority("+svcDef.getLoadPriority()+")");
				}
			} else {
				// 이미 등록되어 있는 서비스 정의의 우선순위가 더 높으므로 그대로 둔다.
				if (log.isInfoEnabled()) {
					log.info(svcName+" has lower priority("+svcDef.getLoadPriority()+") and ignored.");
				}
			}
			
		} else {
			serviceDefs.put(svcName, svcDef);
		}
	}
	
	public ServiceDefinition getServiceDefinition(String svcName) {
		ServiceDefinition svcDef = null;
		
		svcDef = serviceDefs.get(svcName);
		
		return svcDef;
	}
	
	public int getServiceDefinitionCount() {
		return serviceDefs.size();
	}
	
	public boolean containsServiceDefinition(String svcName) {
		return serviceDefs.containsKey(svcName);
	}
	
	public String toString() {
		return serviceDefs.toString();
	}
}
