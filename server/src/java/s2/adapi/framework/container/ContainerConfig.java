package s2.adapi.framework.container;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.config.Configurator;
import s2.adapi.framework.config.ConfiguratorException;
import s2.adapi.framework.config.ConfiguratorFactory;
import s2.adapi.framework.util.ObjectHelper;
import s2.adapi.framework.util.StringHelper;
import s2.adapi.framework.util.SystemHelper;

/** 
 * ServiceContainer의 설정 정보를 제공하는 클래스이다.
 * @author kimhd
 *
 */
public class ContainerConfig {
	
	private static final Logger log = LoggerFactory.getLogger(ContainerConfig.class);
	
	/**
	 * 디폴트 컨테이너 명칭이 지정되지 않았을 경우 사용할 디폴트 명칭
	 */
	public static final String DEFAULT_CONTAINER_NAME = "default";
	
	/**
	 * 컨테이너 명을 지정하지 않았을 경우 사용할 디폴트 명칭을 얻어오기 위한 설정 명
	 */
	public static final String DEFAULT_CONTAINER_NAME_PROPERTY = "s2adapi.container.default.name";
	
	/**
	 * container 관련 설정 명칭의 접두어
	 */
	public static final String CONTAINER_CONFIG_PREFIX = "s2adapi.container.";
    
	/**
	 * <code>Configurator</code> 로부터 container의 서비스 설정 파일 목록이 저장된 클래스패스를 얻기 위한 Property 접미사.
	 */
	public static final String CONTAINTER_SERVICE_CONFIG_PATH_PROPERTY = ".service.config.path";
	
	/**
	 * <code>Configurator</code> 로부터 container의 서비스 설정 파일 목록이 저장된 클래스패스가 지정되지 않았을 경우에 
	 * 사용되는 디폴트 값.
	 */
	public static final String CONTAINTER_SERVICE_CONFIG_PATH_DEFAULT = "svcdefs";
	
	/**
	 * <code>Configurator</code> 로부터 container의 서비스 설정 파일 목록을 얻기위한 Property 접미사
	 */
	public static final String CONTAINER_SERVICE_CONFIG_PROPERTY = ".service.config";
	
	/**
	 * <code>Configurator</code> 로부터 container의 서비스 설정 파일이 담긴 파일 명을 얻기위한 Property 접미사
	 */
	public static final String CONTAINER_SERVICE_CONFIG_FILE_PROPERTY = ".service.config.file";
	
	/**
	 * <code>Configurator</code> 로부터 ReloadableContainer의 클래스패스 경로를 얻기 위한 설정 값 접미사
	 */
	public static final String CONTAINER_CLASSDIR_PROPERTY_KEY = ".dir.class";
	
	/**
	 * ReloadableContainer의 클래스패스 경로를 얻을 수 없을 경우 사용할 디폴트 경로
	 */
	public static final String DEFAULT_CONTAINER_CLASSDIR = "classes";
	
	/**
	 * <code>Configurator</code> 로부터 ReloadableContainer의 모듈 JAR 파일 경로를 얻기 위한 설정 값 접미사
	 */
	public static final String CONTAINER_MODULEDIR_PROPERTY_KEY = ".dir.module";
	
	/**
	 * ReloadableContainer의 모듈 JAR 파일 경로를 얻을 수 없을 경우 사용할 디폴트 경로
	 */
	public static final String DEFAULT_CONTAINER_MODULEDIR = "modules";
	
	/**
	 * <code>Configurator</code> 로부터 ReloadableContainer의 reload 체크 주기를 얻기 위한 설정 값 접미사
	 */
	public static final String CONTAINER_RELOADINTERVAL_PROPERTY_KEY = ".reload.interval";
	
	/**
	 * <code>Configurator</code> 로부터 ReloadableContainer의 reload 시 사용할 Lock File 명을 얻기 위한 설정 값 접미사
	 */
	public static final String CONTAINER_RELOADLOCKFILE_PROPERTY_KEY = ".reload.lockfile";
	
	/**
	 * <code>Configurator</code> 로부터 container의 구현 클래스 명을 얻기위한 설정 값 접미사
	 */
	public static final String CONTAINER_IMPL_PROPERTY_KEY = ".impl";
	
	/**
	 * container 구현 클래스가 지정되지 않았을 경우에 사용되는 디폴트 구현 클래스 명칭
	 */
	public static final String CONTAINER_DEFAULT_IMPL = "s2.adapi.framework.container.impl.XmlConfiguredServiceContainer";
	
	private static String defaultName = DEFAULT_CONTAINER_NAME;
	
	/**
	 * 한번 생성된 컨테이너를 캐싱하기 위한 Map 객체이다.
	 */
	private static Map<String,ServiceContainer> containerCache = new HashMap<String,ServiceContainer>();
	
	static {
		try {
			Configurator configurator = ConfiguratorFactory.getConfigurator();
			
			defaultName = configurator.getString(DEFAULT_CONTAINER_NAME_PROPERTY, DEFAULT_CONTAINER_NAME);
		} catch (ConfiguratorException e) {
			if (log.isErrorEnabled()) {
                log.error("container configuration has failed.", e);
            }
		}
	}
	
	/**
	 * 디폴트 명을 사용하여 서비스 컨테이너 객체를 생성한다.
	 * @return
	 */
	public static ServiceContainer instantiateContainer() {
		return instantiateContainer(defaultName);
	}
	
	/**
	 * 설정파일에 설정된 해당 서비스 컨테이너 명칭의 구현 클래스를 사용하여 
	 * 서비스 컨테이너 객체를 생성한다.
	 * 생성된 서비스 컨테이너 객체는 캐시에 저장되어 다음에 같은 이름으로 요청될 때 반환된다.
	 * @param cname 서비스 컨테이너 명칭
	 * @return
	 */
	public static ServiceContainer instantiateContainer(String cname) {
		ServiceContainer svcContainer = null;
		
		synchronized(containerCache) {
			svcContainer = containerCache.get(cname);
		
			if (svcContainer == null) {
				String implClass = getProperty(cname,CONTAINER_IMPL_PROPERTY_KEY,CONTAINER_DEFAULT_IMPL);
				
				try {
					svcContainer = (ServiceContainer)ObjectHelper.instantiate(implClass,new Class[]{String.class},new Object[] {cname});
					log.info("Container '"+cname+"' has been instantiated. ["+implClass+"]");
					containerCache.put(cname, svcContainer);
				} catch (Exception ex) {
					log.error("Cannot instantiate container '"+cname+"'. ["+implClass+"]",ex);
					ex.printStackTrace();
				}
			}
		}
		
		return svcContainer;
	}
	
	/**
	 * 서비스 컨테이너 캐시에서 주어진 이름의 서비스 컨테이너를 반환한다.
	 * 해당 이름의 컨테이너가 없으면 null을 반환한다.
	 * 
	 * @param cname
	 * @return
	 */
	public static ServiceContainer getContainer(String cname) {
		return containerCache.get(cname);
	}
	
	/**
	 * 서비스 컨테이너 캐시에서 주어진 이름의 서비스 컨테이너를 삭제한다. 
	 * @param cname
	 */
	public static void removeContainer(String cname) {
		synchronized(containerCache) {
			containerCache.remove(cname);
		}
	}
	
	/**
	 * 설정파일에서 서비스 설정파일 디렉토리 설정값을 반환한다.
	 * @param cname 서비스 컨테이너 명칭
	 * @return
	 */
	public static String getServiceConfigPath(String cname) {
		String dir = getProperty(cname,CONTAINTER_SERVICE_CONFIG_PATH_PROPERTY,CONTAINTER_SERVICE_CONFIG_PATH_DEFAULT);
		log.debug("container '"+cname+"' service file path : "+dir);
		return dir;
	}
	
	/**
	 * 설정파일에 정의된 서비스 설정파일 명의 패턴 목록을 반환한다.
	 * @param cname
	 * @return
	 */
	public static String[] getServiceFilePatterns(String cname) {
		if (StringHelper.isNull(cname)) {
			cname = defaultName;
		}
		
		String fileNamePatternListStr = null;
	    List<String> fileNamePatternList = null;
	    String configFileName = null;
	    
	    String[] fileNamePatterns = null;
	    try {
	    	Configurator configurator = ConfiguratorFactory.getConfigurator();
	    
			// 서비스 구성 파일 패턴 목록을 설정 파일에서 얻어온다.
			fileNamePatternListStr = configurator.getString(CONTAINER_CONFIG_PREFIX+cname+CONTAINER_SERVICE_CONFIG_PROPERTY,
															"");
			fileNamePatternList = StringHelper.split(fileNamePatternListStr,",");
			
			// 서비스 구성 파일 패턴 목록이 담긴 파일 명을 설정 파일에서 얻어와 그 내용을 읽어 fileNamePatterns에 추가한다.
			configFileName = configurator.getString(CONTAINER_CONFIG_PREFIX+cname+CONTAINER_SERVICE_CONFIG_FILE_PROPERTY,
													null);
			fileNamePatternList.addAll(parseConfigFile(configFileName));
			
			if (log.isDebugEnabled()) {
				log.debug("container '"+cname+"' service configuration file patterns : "+fileNamePatternList);
			}
		
			fileNamePatterns = fileNamePatternList.toArray(new String[fileNamePatternList.size()]);
	    } catch (ConfiguratorException e) {
			if (log.isErrorEnabled()) {
                log.error("container configuration has failed.", e);
            }
		}
		return fileNamePatterns;
	}
	
	/**
	 * 설정 파일에서 서비스 컨테이너의 클래스패스 값을 읽어 반환한다.
	 * 이 값은 Reloadable Container에서 사용된다.
	 * @param cname
	 * @return
	 */
	public static String getClassDirectory(String cname) {
		String dir = getProperty(cname,CONTAINER_CLASSDIR_PROPERTY_KEY,DEFAULT_CONTAINER_CLASSDIR);
		log.debug("container '"+cname+"' classpath : "+dir);
		return dir;
	}
	
	/**
	 * 설정 파일에서 클래스들의 Jar 파일들이 있는 디렉토리 위치를 읽어 반환한다.
	 * 이 값은 Reloadable Container에서 사용된다.
	 * @param cname
	 * @return
	 */
	public static String getModuleDirectory(String cname) {
		String dir = getProperty(cname,CONTAINER_MODULEDIR_PROPERTY_KEY,DEFAULT_CONTAINER_MODULEDIR);
		log.debug("container '"+cname+"' module path : "+dir);
		return dir;
	}
	
	/**
	 * 설정 파일에서 서비스 컨테이너의 reload 간격 설정 값을 읽어 반환한다.
	 * 이 값은 Reloadable Container에서 사용된다.
	 * @param cname
	 * @return
	 */
	public static long getReloadInterval(String cname) {
		String value = getProperty(cname,CONTAINER_RELOADINTERVAL_PROPERTY_KEY,"0");
		Long interval = Long.parseLong(value);
		return interval*1000L;
	}
	
	/**
	 * 설정 파일에서 클래스들의 reload 시 사용할 lock 파일명을 읽어서 반환한다.
	 * 이 값은 Reloadable Container에서 사용된다.
	 * @param cname
	 * @return
	 */
	public static String getReloadLockFile(String cname) {
		String fname = getProperty(cname,CONTAINER_RELOADLOCKFILE_PROPERTY_KEY,"");
		return fname;
	}
	
	/**
	 * 컨테이너 이름과 프로퍼티명으로 해당 설정값을 반환한다.
	 * @param cname
	 * @param suffix
	 * @param defaultValue
	 * @return
	 */
	public static String getProperty(String cname, String suffix, String defaultValue) {
		if (StringHelper.isNull(cname)) {
			cname = defaultName;
		}

		String propertyValue = null;
		try {
	    	Configurator configurator = ConfiguratorFactory.getConfigurator();
	    	propertyValue = configurator.getString(CONTAINER_CONFIG_PREFIX+cname+suffix, defaultValue);
		} catch (ConfiguratorException e) {
			if (log.isErrorEnabled()) {
                log.error("container configuration has failed.", e);
            }
		}

		return propertyValue;
	}
	
	/**
	 * 주어진 파일명으로 파일을 읽어서 그 내용을 한 줄씩 List<String>에 담아서 반환한다.
	 * @param configFileName
	 * @return
	 */
	private static List<String> parseConfigFile(String configFileName) {
		List<String> fileNamePatterns = new ArrayList<String>();
		
		if (configFileName == null) {
			return fileNamePatterns;
		}
		
		File configFile = null;
		InputStreamReader reader = null;
		BufferedReader br = null;
		
		try {
			configFile = SystemHelper.getResourceAsFile(configFileName);
			reader = new InputStreamReader(new FileInputStream(configFile));
			
			br = new BufferedReader(reader);
			String fileName = null;
			for(fileName = br.readLine();fileName != null;fileName = br.readLine()) {
				fileNamePatterns.add(fileName.trim());
			}
		} catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("service configuration list file is not accessible.", e);
            }
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}

		return fileNamePatterns;
	}
}
