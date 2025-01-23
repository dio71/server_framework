package s2.adapi.framework.container.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.container.ContainerConfig;
import s2.adapi.framework.container.ServiceContainer;
import s2.adapi.framework.container.ServiceContainerException;
import s2.adapi.framework.container.ServiceInfo;
import s2.adapi.framework.container.ServicePostProcessor;
import s2.adapi.framework.container.ServiceReader;
import s2.adapi.framework.container.support.ServiceRegistry;
import s2.adapi.framework.util.FileUtil;
import s2.adapi.framework.util.SystemHelper;

/**
 * XML 파일로 정의된 서비스 구성을 읽어서 서비스 객체를 생성/관리/제공하는 ServiceContainer 의 구현 클래스이다.
 * @author 김형도
 *
 */
public class XmlConfiguredServiceContainer implements ServiceContainer {

	private static final Logger log = LoggerFactory.getLogger(XmlConfiguredServiceContainer.class);
	
	protected ServiceContainer svcContainer = null;

	protected String containerName = null;
	
	/**
	 * 설정 파일의 디폴트 설정값을 기준으로 컨테이너 객체를 생성하는 디폴트 생성자이다.
	 */
	public XmlConfiguredServiceContainer() {
		this("");
	}
	
	/**
	 * 설정 파일에서 주어진 이름의 설정값을 기준으로 컨테이너 객체를 생성한다.
	 * @param containerName
	 */
	public XmlConfiguredServiceContainer(String cname) {
		containerName = cname;
		String svcDir = ContainerConfig.getServiceConfigPath(cname);
		String[] patterns = ContainerConfig.getServiceFilePatterns(cname);

		svcContainer = buildContainer(svcDir, patterns);
	}
	
	/**
	 * 서비스 설정 파일 디렉토리와 설정 파일 패턴 목록을 주면 해당 파일들을 찾아서
	 * 서비스 정의 항목을 로딩한다.
	 * @param svcDir
	 * @param patterns
	 */
	public XmlConfiguredServiceContainer(String svcDir, String[] patterns) {
		svcContainer = buildContainer(svcDir, patterns);
	}
	
	/**
	 * 주어진 서비스 설정 파일로 서비스 정의 항목을 로딩한다.
	 * @param svcFile 서비스 설정 파일
	 */
	public XmlConfiguredServiceContainer(File svcFile) {
		svcContainer = buildContainer(new File[]{svcFile});
		
	}
	
	/**
	 * 주어진 서비스 설정 파일 목록으로 서비스 정의 항목을 로딩한다.
	 * @param svcFiles 서비스 설정 파일 목록
	 */
	public XmlConfiguredServiceContainer(File[] svcFiles) {
		svcContainer = buildContainer(svcFiles);
	}
	
	public ClassLoader getClassLoader() {
		return svcContainer.getClassLoader();
	}
	
	/**
	 * 서비스 설정파일 디렉토리와 설정 파일 패턴 목록을 사용하여 
	 * 서비스 컨테이너를 생성한다.
	 * @param svcDir
	 * @param patterns
	 */
	protected ServiceContainer buildContainer(String svcDir, String[] patterns) {
		File[] svcFiles = null;
		try {
			File[] rootDirs = SystemHelper.getResourcesAsFile(svcDir);
			Set<File> svcFileSet = FileUtil.getFilesOfPattern(rootDirs, patterns, false);
			svcFiles = svcFileSet.toArray(new File[svcFileSet.size()]);
		} catch (IOException e) {
			 if (log.isErrorEnabled()) {
	                log.error(svcDir+" for service configuration files is not accessible.", e);
	         }
		}
		ServiceRegistry svcRegistry = buildServiceRegistry(svcFiles);
		return new EnlistedServiceContainer(svcRegistry);
	}
	
	/**
	 * 주어진 서비스 설정 파일들을 사용하여 서비스 컨테이너를 생성한다.
	 * @param svcFiles
	 */
	protected ServiceContainer buildContainer(File[] svcFiles) {
		ServiceRegistry svcRegistry = buildServiceRegistry(svcFiles);
		return new EnlistedServiceContainer(svcRegistry);
	}
	
	/**
	 * 주어진 파일 목록에 있는 파일들을 서비스 정의 항목으로 로딩한다. 
	 * @param files
	 */
	protected ServiceRegistry buildServiceRegistry(File[] files) {
		ServiceReader svcReader = new XmlServiceDefinitionReader();
		ServiceRegistry svcRegistry = new ServiceRegistry();
		
		if( files != null && files.length > 0 ) {
			svcReader.loadServiceDefinition(files, svcRegistry, true);
		}
		return svcRegistry;
	}
	
	public boolean containsService(String svcName) {
		return svcContainer.containsService(svcName);
	}
	
	public String[] getAllServiceNames() {
		return svcContainer.getAllServiceNames();
	}
	
	public String[] getPatternServiceNames() {
		return svcContainer.getPatternServiceNames();
	}
	
	public Object getService(String svcName) throws ServiceContainerException {
		return svcContainer.getService(svcName);
	}
	
	public Map<String,Object> getServicesOfType(String typeName) throws ServiceContainerException {
		return svcContainer.getServicesOfType(typeName);
	}
	
	public void addPostProcessor(ServicePostProcessor postProcessor) {
		svcContainer.addPostProcessor(postProcessor);
	}
	
	public void populateServices() {
		svcContainer.populateServices();
	}
	
	public void reload() {
		svcContainer.reload();
	}
	
	public void close() {
		svcContainer.close();
	}
	
	public ServiceInfo[] getServiceInfo() {
		return svcContainer.getServiceInfo();
	}
	
	public ServiceInfo getServiceInfo(String svcName) {
		return svcContainer.getServiceInfo(svcName);
	}
}
