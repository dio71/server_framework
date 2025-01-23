package s2.adapi.framework.container.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.container.ContainerConfig;
import s2.adapi.framework.container.ServiceContainer;
import s2.adapi.framework.container.ServicePostProcessor;
import s2.adapi.framework.container.support.ServiceRegistry;
import s2.adapi.framework.util.FileUtil;
import s2.adapi.framework.util.FileWatchdog;
import s2.adapi.framework.util.FileWatchdogListener;
import s2.adapi.framework.util.JarUtil;
import s2.adapi.framework.util.StringHelper;
import s2.adapi.framework.util.SystemHelper;

/**
 * Hot deploy 가 가능한 ServiceContainer의 구현 클래스이다.
 * 내부적으로 별도의 URLClassLoader를 생성하여 이로부터 EnlistedServiceContainer 객체를 생성하여 사용한다.
 * reload() 메소드가 호출되면 새로운 URLClassLoader와 이를 사용한 EnlistedServiceContainer 객체를 다시 생성하는 방식으로
 * Hot deploy 기능을 구현한다.
 * @author kimhd
 * @since 5.0
 */
public class ReloadableXmlServiceContainer extends XmlConfiguredServiceContainer
		implements FileWatchdogListener {
	
	private static final Logger log = LoggerFactory.getLogger(ReloadableXmlServiceContainer.class);
	
	private ClassLoader classLoader = null;
	
	private String svcDirectory = null;
	private String[] svcFilePatterns = null;
	
	private Long reloadInterval = 0L;
	
	/**
	 * 적용할 ServicePostProcessor 들이다.
	 */
	private List<ServicePostProcessor> postProcessors = new ArrayList<ServicePostProcessor>();
	
	private boolean isPopulated = false;
	
	private FileWatchdog watchDog = null;
	
	/**
	 * 설정 파일의 디폴트 설정값을 기준으로 컨테이너 객체를 생성하는 디폴트 생성자이다.
	 */
	public ReloadableXmlServiceContainer() {
		this("");
	}
	
	/**
	 * 설정 파일에서 주어진 이름의 설정값을 기준으로 컨테이너 객체를 생성한다.
	 * @param containerName
	 */
	public ReloadableXmlServiceContainer(String containerName) {
		super(containerName);
		svcDirectory = ContainerConfig.getServiceConfigPath(containerName);
		svcFilePatterns = ContainerConfig.getServiceFilePatterns(containerName);
		
		reloadInterval = ContainerConfig.getReloadInterval(containerName);
		runReloader();
	}
	
	public ReloadableXmlServiceContainer(String svcDir, String[] patterns) {
		super(svcDir,patterns);
		svcDirectory = svcDir;
		svcFilePatterns = patterns;
		
		runReloader();
	}
	
	protected void runReloader() {
		if (reloadInterval > 0L) {
			File moduleDir = new File(ContainerConfig.getModuleDirectory(containerName));
			
			watchDog = new FileWatchdog(moduleDir);
			watchDog.setListener(this);
			watchDog.setDelay(reloadInterval);
			String lockFile = ContainerConfig.getReloadLockFile(containerName);
			if (!StringHelper.isNull(lockFile)) {
				watchDog.setLockFile(new File(lockFile));
			}
			
			watchDog.start();
		}
	}
	
	protected ServiceContainer buildContainer(String svcDir, String[] patterns) {

		File moduleDir = new File(ContainerConfig.getModuleDirectory(containerName));
		File classDir = new File(ContainerConfig.getClassDirectory(containerName));
		
		classLoader = reloadClassLoader(moduleDir,classDir);

		File[] svcFiles = null;
		try {
			File[] rootDirs = SystemHelper.getResourcesAsFile(classLoader,svcDir);
			log.info("container root dir = "+rootDirs.length);
			for(int i=0;i<rootDirs.length;i++) {
				log.info(rootDirs[i].getAbsolutePath());
			}
			List<File> svcFileSet = FileUtil.getFilesOfPatternAsPatternOrder(rootDirs, patterns, false);
			svcFiles = svcFileSet.toArray(new File[svcFileSet.size()]);
		} catch (IOException e) {
			 if (log.isErrorEnabled()) {
	                log.error(svcDir+" for service configuration files is not accessible.", e);
	         }
		}
		
		ServiceRegistry svcRegistry = buildServiceRegistry(svcFiles);
		return new EnlistedServiceContainer(svcRegistry,classLoader);
	}
	
	public synchronized void reload() {

		ServiceContainer newContainer = buildContainer(svcDirectory,svcFilePatterns);
		
		// PostProcessor를 재등록한다.
		for(int i=0;i<postProcessors.size();i++) {
			newContainer.addPostProcessor(postProcessors.get(i));
		}
		
		// 이전 서비스 컨테이너가 Populated 되어 있었다면 새로 생성한 서비스 컨테이너를  populate 시킨다.
		if (isPopulated) {
			newContainer.populateServices();
		}
		
		ServiceContainer oldContainer = svcContainer; // close() 호출을 위하여 담아둔다. (2016.02.24)
		
		svcContainer = newContainer;
		
		oldContainer.close(); // 이전 컨테이너 리소스 clear (2016.02.24)
	}

	public void close() {
		if (watchDog != null) {
			//System.out.println("WATCHDOG QUIT");
			watchDog.quit();
		}
		svcContainer.close();
	}
	
	public void addPostProcessor(ServicePostProcessor postProcessor) {
		if (postProcessor != null) {
			postProcessors.add(postProcessor);
			svcContainer.addPostProcessor(postProcessor);
		}
	}
	
	public void populateServices() {
		svcContainer.populateServices();
		isPopulated = true;
	}
	
	private ClassLoader reloadClassLoader(File moduleDir, File classDir) {
		List<File> jarFiles = getJarFiles(moduleDir);
		updateWorkDirectory(jarFiles,classDir);
		return constructClassLoader(classDir.listFiles());
		
	}
	
	/**
	 * 주어진 classPath들을 사용하여 URLClassLoader 객체를 생성한다.
	 * @param classPath
	 * @return
	 */
	private ClassLoader constructClassLoader(File[] classPath) {
		// File 배열을 URL 배열로 변환
		List<URL> urlList = new ArrayList<URL>();
		for(int i=0;i<classPath.length;i++) {
			try {
				urlList.add(classPath[i].toURI().toURL());
			} catch (MalformedURLException ex) {
				log.error("classpath cannot be parsed as URL. ["+classPath[i].getPath()+"]",ex);
			}
		}
		
		URL[] urls = urlList.toArray(new URL[urlList.size()]);
		
		// framework JAR 파일이 시스템 클래스 패스에 있을 경우 this의 클래스로더를 사용하면 
		// Servlet의 클래스 패스는 보이지 않으므로 아래와 같이 Thread의 Context ClassLoader를 사용하도록 수정함
		// 2008.04.26 - 김형도
		//ClassLoader loader = new URLClassLoader(urls,this.getClass().getClassLoader());
		ClassLoader loader = new URLClassLoader(urls,SystemHelper.getClassLoader());
		return loader;
	}
	
	/**
	 * 주어진 디렉토리 아래에 있는 Jar 파일들의 목록을 구하여 File 리스트로 반환한다.
	 */
	private List<File> getJarFiles(File jarDir) {
		List<File> jarFiles = new ArrayList<File>();
		
		if (jarDir == null || !jarDir.isDirectory()) {
			return jarFiles;
		}
		
		File[] files = jarDir.listFiles();
		for(int i=0;i<files.length;i++) {
			if (files[i].isFile() && files[i].getName().endsWith(".jar")) {
				jarFiles.add(files[i]);
			}
		}
		
		return jarFiles;
	}
	
	/**
	 * 갱신된 Jar 파일들이 있으면 이를 work 디렉토리에 반영한다.
	 */
	private void updateWorkDirectory(List<File> jarFiles, File classDir) {
		// 클래스 디렉토리 없으면 생성하기
		try {
			if (!classDir.exists()) {
				classDir.mkdirs();
			}
		} catch(Exception ex) {
			log.error("cannot create the class directory ["+classDir.getAbsolutePath()+"]",ex);
		}
		
		// work directory에 있는 파일들의 맵을 작성한다.
		File[] workDirFiles = classDir.listFiles();
		if (workDirFiles == null) {
			log.error("Invalid class directory ["+classDir.getAbsolutePath()+"].");
			return;
		}
		Map<String,File> workFileMap = new HashMap<String,File>();
		for(int i=0;i<workDirFiles.length;i++) {
			workFileMap.put(workDirFiles[i].getName(), workDirFiles[i]);
		}
		//System.out.println("Class Dirs:"+workFileMap);
		// JAR 파일들과 work Directory의 파일들의 맵을 비교한다.
		File jarFile = null;
		File workFile = null;
		List<File> updatedJarFiles = new ArrayList<File>();
		
		for(int i=0;i<jarFiles.size();i++) {
			jarFile = jarFiles.get(i);
			workFile = workFileMap.remove(jarFile.getName());
			if (workFile == null) {
				log.info("New Module : " + jarFile.getName());
				// jarFile is new One.
				updatedJarFiles.add(jarFile);
			} else {
				// compare the modified date.
				if (jarFile.lastModified() > workFile.lastModified()) {
					// jarFile is updated.
					// delete workFile and its subDirectories.
					log.info("Updated : "+jarFile.getName());
					FileUtil.deleteDirectory(workFile, true);
					updatedJarFiles.add(jarFile);
				} else {
					log.info("Not Modified : " + jarFile.getName());
				}
			}
		}
		// workFileMap에 남아 있는 것은 해당 Jar 파일이 없는것(삭제된 것)이므로 해당 디렉토리를 삭제함
		// 2009.12.03 김형도
		Iterator<File> itor = workFileMap.values().iterator();
		while(itor.hasNext()) {
			workFile = itor.next();
			log.info("Deleted : " + workFile.getName());
			FileUtil.deleteDirectory(workFile, true);
		}
		
		// unjar all updated jar files to work directory
		for(int i=0;i<updatedJarFiles.size();i++) {
			jarFile = updatedJarFiles.get(i);
			try {
				log.info("Unjar : " + jarFile.getName());
				JarUtil.unjar(jarFile, new File(classDir,jarFile.getName()), true);
			} catch (IOException ex) {
				log.error("cannot extract jar files, so retry after 1 sec. ["+jarFile.getPath()+"]");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignored) {}
				try {
					log.info("Unjar : " + jarFile.getName());
					JarUtil.unjar(jarFile, new File(classDir,jarFile.getName()), true);
					log.info("Unjar successful.");
				} catch (IOException ex2) {
					log.error("cannot extract jar files again. ["+jarFile.getPath()+"]",ex2);
				}
			}
		}
	}

	public void fileChanged() {
		//System.out.println("Reload");
		reload();
	}
}
