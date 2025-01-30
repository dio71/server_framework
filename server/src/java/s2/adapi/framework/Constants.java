package s2.adapi.framework;

/**
 * This class defines global constants.
 *
 * @author kimhd
 * @since 1.0
 */
public class Constants {

	// v1.10 2020.10.15
	// - deprecated method 정리
	// v1.11 2020.10.21
	// - TimezoneHelper에 getCurrentWeek() 함수 추가
	
	public static final String VERSION = "1.11";
	
    /**
     * Base package.
     */
    public static final String Package = "s2.adapi.framework";

    /**
     * Key for a configuration file path
     */
    public static final String CONFIG_FILE_PATH_KEY = "s2adapi.config.path";

    /**
     * Key for a parent path of the configuration file (auto-generated)
     */
    public static final String CONFIG_PARENT_BASEPATH_KEY = "s2adapi.config.parent.base";
    
    /**
     * Key for a parent path of parent path of the configuration file (auto-generated)
     */
    public static final String CONFIG_PARENT_PARENT_BASEPATH_KEY = "s2adapi.config.parent.parent.base";
    
    /**
     * Key for a basename of path of the configuration file (auto-generated)
     */
    public static final String CONFIG_FILE_BASEPATH_KEY = "s2adapi.config.base";
    
    /**
     * Key for a absolute path of the configuration file (auto-generated)
     */
    public static final String CONFIG_FILE_ABSOLUTE_BASEPATH_KEY = "s2adapi.config.base.absolute";
    
    /**
     * Key for a root path of the web application context (auto-generated)
     */
    public static final String CONFIG_WEBCONTEXT_ABSOLUTE_ROOTPATH_KEY = "s2adapi.web.context.root";
    
    public static final String CONFIG_KEY_STORE_KEY = "s2adapi.keystore.";
    
    /**
     * Base Package of applications
     */
    public static final String CONFIG_FQCN_PREFIX_KEY = "s2adapi.java.fqcn.prefix.name";
    
    /**
     * CONFIG_FQCN_PREFIX_KEY 으로 정의된 설정 값을 찾을 수 없을 경우 사용할 디폴트 값.
     */
    public static final String CONFIG_FQCN_PREFIX_DEFAULT = "s2.adapi";
    
    
    /**
     * WebAction 에서 HttpServletRequest 에 setAttribute 로 값을 전달할 때 사용하는 키 값들을 정의
     */
    public static final String WEB_ATTRIBUTE_SERVICE_NAME = "__s2adapi_service_name__";
    
    /**
     * 현재 세션의 언어 설정값을 ServiceContext 에 저장하기 위하여 사용되는 키값
     */
    public static final String CONTEXT_SESSION_LOCALE_NAME = "lang";
    
    /**
     * 현재 세션의 언어 설정값을 request 의 attribute 에 저장하기 위하여 사용되는 키값
     */
    public static final String SERVLET_REQUEST_LOCALE_NAME = "__s2sdapi_request_locale__"; 
    
    /**
     * 현재 세션의 언어 설정값을 HttpSession에 저장하기 위하여 사용되는 키값
     */
    public static final String SERVLET_SESSION_LOCALE_NAME = "__s2adapi_session_locale__";
    
    /**
     * 디버깅 상태에서 추가적인 로그를 찍을 경우 사용하는 Logger 명칭
     */
    public static final String DEBUG_LOGGER_NAME = "dev";
    
    /**
     * 시스템 타임존 가져오기 위한 키값
     * 
     */
    public static final String CONFIG_SYSTEM_TIMEZONE_KEY = "s2adapi.system.timezone";

    /**
     * Application 설정 파일 관련 키값
     */
    public static final String APPLICATION_CONFIG_KEY = "application.config.";
    public static final String APPLICATION_CONFIG_DEFAULT_NAME = APPLICATION_CONFIG_KEY + "default.name";
}
