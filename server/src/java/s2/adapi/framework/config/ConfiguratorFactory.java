package s2.adapi.framework.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import s2.adapi.framework.Constants;
import s2.adapi.framework.config.impl.PropertiesConfiguratorImpl;
import s2.adapi.framework.util.ObjectHelper;
import s2.adapi.framework.util.SystemHelper;

/**
 * Configurator의 Factory Class 이다.
 * <code>ConfiguratorFactory</code>가 <code>Configurator</code> 객체를 생성하는 순서는 아래와 같다.
 * <ol>
 * <li> System Property에서 "hunited.config.path"로 정의된 설정 값을 얻어온다.
 * <li> System Property에 위 값이 정의되지 않았다면, "hunited-config.properties"를 디폴트 값으로 사용한다.
 * <li> <code>SystemHelper.getResourceAsStream()을 사용하여 앞서 설정된 값을 사용하여 파일을 찾아 로딩한다.
 * <li> 파일이 존재하지 않으면 "resources/" classpath에서 다시 해당 파일을 찾아 로딩한다. 
 * </ol>
 * </p>
 * <p>
 * Configurator 객체가 생성되면 다음 다섯 개의 설정값이 생성된다.
 * <ul>
 * <li> s2adapi.config.path : Configurator 객체 생성시 사용된 실제 설정파일의 클래스 패스 경로(파일명 포함)
 * <li> s2adapi.config.base : 설정 파일이 있는 클래스 패스 경로의 마지막 디렉토리 명
 * <li> s2adapi.config.base.absolute : 설정 파일이 있는 패스의 절대 경로
 * <li> s2adapi.config.parent.base : s2adapi.config.base의 상위 디렉토리 명
 * <li> s2adapi.config.parent.parent.base : s2adapi.config.parent.base의 상위 디렉토리 명
 * </ul>
 * </p>
 * <pre>
 *      ex) -Ds2adapi.config.path=s2adapi.properties
 * </pre>
 * 위 예제와 같이 .properties 확장자를 갖는 속성일 경우 <strong>PropertyConfiguratorImpl</strong>가 활성화 된다.
 * 참고로 현재 <strong>PropertyConfiguratorImpl</strong> 부분만 지원하고 있다.
 * </p>
 *
 * <p>
 * 위 <strong>ConfiguratorFactory</strong>을 이용한 <strong>Configurator</strong> 얻어 오는 방법이다.
 * </p>
 *
 * <pre>
 *     ex) Configurator configurator = ConfiguratorFactory.getInstance().getConfigurator();
 * </pre>
 *
 * <p>
 * 또한 <strong>ConfiguratorFactory</strong>의 메모리에 적재된 <strong>Configurator</strong>를 다시 재적재 하는 방법은  다음과 같다.
 * </p>
 *
 * <pre>
 *     ex) ConfiguratorFactory.getInstance().initialize();
 * </pre>
 *
 * @author kimhd
 */
public class ConfiguratorFactory {
    /**
     * <p>
     * <strong>PropertyConfiguratorImpl</strong> Class 의 
     * FQCN (s2.adapi.configurator.impl.PropertiesConfiguratorImpl)
     * </p>
     */
    private static final String S2API_CONFIGURATOR_IMPL_PROPERTIES =  PropertiesConfiguratorImpl.class.getCanonicalName();

    /**
     * <p>
     * SystemProperty의 "s2adapi.config.path" 의 default value
     * </p>
     */
    private static final String S2API_CONFIGURATION_PROPERTIES_FILE_NAME_VALUE = "s2adapi-config.properties";

    /**
     * 구성파일을 찾을 classpath 경로이다. root classpath에서 우선 찾은 후, 찾지 못할 경우 여기에서 다시 찾는다.
     */
    private static final String S2API_CONFIGURATION_PROPERTIES_FILE_PATH_VALUE = "resources/";

    /**
     * <p>
     * <strong>Configurator</strong> 단일(Single) 인스턴스 패턴 구현을 위한 static 인스턴스.
     * </p>
     */
    private static Configurator configuratorSingleton = null;

    /**
     * <p>
     * 구성 파일 이름의 static 인스턴스
     * </p>
     */
    private static String configFileName = null;

    /**
     * Prevent users from instantiating ConfiguratorFactory.
     */
    private ConfiguratorFactory() {
    }

    public synchronized static void removeConfigurator() {
    	if (configuratorSingleton != null) {
    		configuratorSingleton.clear();
    		configuratorSingleton = null;
    	}
    }
    
    /**
     * <p>
     * <strong>ConfiguratorFactory</strong> 단일(Single) 인스턴스 내부에서 <strong>Configurator</strong>의 단일(Single)
     * 인스턴스을 얻기 위한 Method.
     * </p>
     *
     * @return 단일(Single) 인스턴스 패턴의 <code>Configurator</code> 인스턴스.
     */
    public static Configurator getConfigurator() throws ConfiguratorException {
    	if (configuratorSingleton == null) {
    		configuratorSingleton = newConfiguratorInstance();
    	}
    	
        return configuratorSingleton;
    }

    /**
     * <p>
     * <strong>ConfiguratorFactory</strong> 단일(Single) 인스턴스 내부에서 <strong>Configurator</strong>의 단일(Single)
     * 인스턴스 패턴의 구현을 위한 initialize Method.
     * 구성 파일의 변경 시 Method를 사용하면 구성 파일을 다시 재로딩 할 수 있다.
     * </p>
     *
     * @throws ConfiguratorException 구성 파일을 접근 할 수 없는 경우;<br> 구성 파일의 정보를 SystemProperty에서 잘못 지정한 경우;<br>
     *                               해당 구성 파일을 로드 하기 위한 implement Class를 로드 하지 못할 경우
     */
    private synchronized static Configurator newConfiguratorInstance() throws ConfiguratorException {

        String configClassInstance = null;
        InputStream is = null;
        configFileName = SystemHelper.getSystemProperty(Constants.CONFIG_FILE_PATH_KEY, 
        					      S2API_CONFIGURATION_PROPERTIES_FILE_NAME_VALUE);

        configClassInstance = S2API_CONFIGURATOR_IMPL_PROPERTIES;
        
        String firstConfigFileName = configFileName;
        
        File configFile = null;
        try {
        	configFile = SystemHelper.getResourceAsFile(configFileName);
        } 
        catch (IOException ignored) {
        }

        if (configFile == null) {
        	try {
        		configFileName = S2API_CONFIGURATION_PROPERTIES_FILE_PATH_VALUE + configFileName;
        		configFile = SystemHelper.getResourceAsFile(configFileName);
			} 
            catch (IOException e) {
				throw new ConfiguratorException("Configuration file not found. (neither " 
						+ firstConfigFileName + " and " + configFileName+")",e);
			}
        }
        
        // 구성파일 경로를 시스템 프로퍼티에 재설정
        SystemHelper.setSystemProperty(Constants.CONFIG_FILE_PATH_KEY,configFileName);
        
        // 파일 명을 제외한 구성파일 경로를 시스템 프로퍼티에 설정
        int lastIdx = configFileName.lastIndexOf("/");
        String configFileBasePath = ".";
        if ( lastIdx >= 0 ) {
        	configFileBasePath = configFileName.substring(0,lastIdx);
        }
        SystemHelper.setSystemProperty(Constants.CONFIG_FILE_BASEPATH_KEY,configFileBasePath);
        
        File configDir = configFile.getParentFile();
        // 구성 파일 절대 경로를 시스템 프로퍼티에 설정
        SystemHelper.setSystemProperty(Constants.CONFIG_FILE_ABSOLUTE_BASEPATH_KEY,
        			configDir.getAbsolutePath());
		
        File parentDir = configDir.getParentFile();
        SystemHelper.setSystemProperty(Constants.CONFIG_PARENT_BASEPATH_KEY, 
        		parentDir.getName().replace("%20", " "));
        SystemHelper.setSystemProperty(Constants.CONFIG_PARENT_PARENT_BASEPATH_KEY, 
        		parentDir.getParentFile().getName());
        
        Configurator configurator = null;
        
        try {
        	configurator = (Configurator) ObjectHelper.instantiate(configClassInstance);
            is = new FileInputStream(configFile);
            configurator.doConfigure(is);
        } 
        catch (Throwable e) {
            throw new ConfiguratorException(configClassInstance + " Class Not Instantiated : " + e.getLocalizedMessage());
        } 
        finally {
        	if ( is != null ) {
        		try {
					is.close();
				} 
                catch (IOException e) {
				}
        	}
        }
        
        // log4j -> log4j2 로 변경하면서 아래 기능은 삭제함 (2025.01.24)
        // //
        // // framework-config.properties 파일에 "s2adapi.log4j.config" 항목이 있을 경우에는 log4j를 재설정한다.
        // //
        // String log4jConfig = configurator.getString("s2adapi.log4j.config","");
        // if ( !StringHelper.isNull(log4jConfig) ) {
        // 	try {
		// 		File log4jConfigFile = SystemHelper.getResourceAsFile(log4jConfig);
		// 		if ( log4jConfigFile != null ) {
		// 			PropertyConfigurator.configure(log4jConfigFile.getPath());
		// 		}
		// 	} catch (IOException ignored) {}
        	
        // }

		return configurator;
    }

}
