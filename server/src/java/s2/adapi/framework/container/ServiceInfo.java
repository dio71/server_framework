package s2.adapi.framework.container;

import java.util.ArrayList;
import java.util.List;

/**
 * 현재 ServiceContainer에 등록되어 있는 서비스의 설정 정보를 표현하는 인터페이스이다.
 * @author kimhd
 * @since 4.0
 */
public class ServiceInfo {

	private String fromInfo = null;
	private String serviceName = null;
	private String serviceClassName = null;
	private String serviceInfName = null;
	private List<String> references = new ArrayList<String>();
	
	private boolean isSingleton = false;
	private boolean isInstantiated = false;
	
	public ServiceInfo(String svcName, String className, String infName, boolean singleton, boolean instantiated, String from) {
		serviceName = svcName;
		serviceClassName = className;
		serviceInfName = infName;
		isSingleton = singleton;
		isInstantiated = instantiated;
		fromInfo = from;
	}
	
	public String getFromInfo() {
		return fromInfo;
	}
	
	public String getName() {
		return serviceName;
	}
	
	public String getClassName() {
		return serviceClassName;
	}
	
	public String getInterfaceName() {
		return serviceInfName;
	}
	
	public boolean isInstantiated() {
		return isInstantiated;
	}
	
	public boolean isSingleton() {
		return isSingleton;
	}
	
	public void addReference(String refName) {
		references.add(refName);
	}
	
	public List<String> getReferences() {
		return references;
	}
}
