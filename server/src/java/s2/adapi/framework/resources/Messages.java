package s2.adapi.framework.resources;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.config.Configurator;
import s2.adapi.framework.config.ConfiguratorException;
import s2.adapi.framework.config.ConfiguratorFactory;
import s2.adapi.framework.resources.impl.ResourcesFactoryBase;
import s2.adapi.framework.util.ObjectHelper;
import s2.adapi.framework.util.StringHelper;

/**
 * <p>
 * {@link Resources} 인스턴스로 부터 메시지 스트링을 룩업(lookup)하고
 * <code>java.text.MessageFormat</code>를 이용하여 파라미터를 변환하기 위한
 * {@link Resources} 인스턴스에 대한 래퍼 클래스이다. 편의를 위해 동일한 기능은
 * {@link Resources} 파라미터를 전달받는 정적(static) 메소드를 통해 이용한다.
 * </p>
 * <p>
 * <code>Locale</code> 인자(argument) 없이 <code>getMessage()</code>메소드 군을 호출하는 것은
 * 현재 JVM의 기본 <code>Locale</code>로 메시지 스트링을 요청한다고 가정한다.
 * <p/>
 * <p>
 * 실제 {@link Resources} 인스턴스에 <code>getString()</code> 호출이 실패하거나 null을 리턴하는 경우,
 * 적절한 에러 메시지 스트링이 리턴된다.
 * </p>
 */
public class Messages implements Serializable {

	static final long serialVersionUID = 1360956379266249574L;
	
    /**
     * <p>
     * Commons Logging 인스턴스.
     * </p>
     */
    private static final Logger log = LoggerFactory.getLogger(Messages.class);

    /**
     * <p>
     * <code>getMessages()</code> 메소드에서 사용할 {@link s2.adapi.framework.resources.ResourcesFactory}
     * </p>
     */
    protected static Map<String,ResourcesFactory> factoryMap = new HashMap<String,ResourcesFactory>();

    /**
     * 메시지를 생성하기 위한 리소스 명을 지정하지 않았을 경우 디폴트 리소스명을 가져오기 위한 Key
     */
    private static final String MESSAGE_DEFAULT_NAME_KEY = "s2adapi.resources.default.name";

    /**
     * 디폴트 리소스 명을 가져오지 못했을 경우 사용할 디폴트 리소스 명칭
     */
    private static final String MESSAGE_DEFAULT_NAME = "default";
    
    /**
     * 리소스명으로부터 Factory 클래스 명을 얻기 위한 Key의 접두어
     */
    private static final String MESSAGE_FACTORTY_PREFIX = "s2adapi.resources.factory.";
    
    /**
     * 리소스명으로부터 재로딩 간격 설정 값을 얻기 위한 Key의 접두어
     */
    private static final String MESSAGE_RELOAD_PREFIX = "s2adapi.resources.reload.";
    
    /**
     * 리소스 명으로 부터 Factory 클래스를 찾지 못했을 경우에 사용할 Factory 클래스
     */
    private static final String MESSAGE_DEFAULT_FACTORTY_CLASS = "s2.adapi.framework.resources.impl.XMLResourcesFactory";
    
    /**
     * <p>
     * 명시된 {@link Resources} 인스턴스를 래핑(wrap)하기위한 {@link Messages}인스턴스의
     * 생성자
     * </p>
     *
     * @param resources 특정 메시지 스트링으로 부터 조회되는 {@link Resources} 인스턴스
     */
    public Messages(Resources resources) {

        this.resources = resources;

    }

    /**
     * <p>
     * 래핑(wrapping)되는 {@link Resources} 인스턴스
     * </p>
     */
    protected Resources resources = null;

    /**
     * <p>
     * 래핑된 {@link Resources} 인스턴스를 리턴한다.
     * </p>
     */
    public Resources getResources() {

        return (this.resources);

    }

    /**
     * <p>
     * 명시된 key에 해당하는 텍스트 메시지를 기본 <code>Locale</code>로 리턴한다.
     * </p>
     *
     * @param key 조회될 메시지 키
     */
    public String getMessage(String key) {

        return (getMessage(resources, key));

    }


    /**
     * <p>
     * 명시된 key에 대한 텍스트 메시지를 명시된 <code>Locale</code>로 리턴한다.
     * </p>
     *
     * @param locale 메시지 조회를 위한 <code>Locale</code>
     * @param key    조회될 메시지 키
     */
    public String getMessage(Locale locale, String key) {

        return (getMessage(resources, locale, key));

    }


    /**
     * <p>
     * 명시된 key에 대해 기본 <code>Locale</code>로 텍스트 메시지를 리턴한다.
     * 메시지 내용에 포함될 대체 내용을 파라미터로 전달한다.
     * </p>
     *
     * @param key  조회를 위한 메시지 키
     * @param args 대체 값들에 대한 배열
     */
    public String getMessage(String key, Object[] args) {
        return getMessage(resources, key, args);
    }


    /**
     * <p>
     * 명시된 key와 <code>Locale</code>로 텍스트 메시지를 리턴한다.
     * 메시지 내용에 포함될 대체 내용을 파라미터로 전달한다.
     * </p>
     *
     * @param locale 메시지를 조회하기 위한 <code>Locale</code>
     * @param key    조회를 위한 메시지 키
     * @param args   대체 값들에 대한 배열
     */
    public String getMessage(Locale locale, String key, Object[] args) {
        return getMessage(resources, locale, key, args);
    }


    /**
     * <p>
     * 명시된 key에 대해 기본 <code>Locale</code>로 텍스트 메시지를 리턴한다.
     * 메시지 내용에 포함될 대체 내용을 파라미터로 전달한다.
     * </p>
     *
     * @param key  조회를 위한 메시지 키
     * @param arg0 개별 파라미터 대체 값
     */
    public String getMessage(String key, Object arg0) {

        return (getMessage(resources, key, arg0));

    }


    /**
     * <p>
     * 명시된 key와 <code>Locale</code>로 텍스트 메시지를 리턴한다.
     * 메시지 내용에 포함될 대체 내용을 파라미터로 전달한다.
     * </p>
     *
     * @param locale 메시지를 조회하기 위한 <code>Locale</code>
     * @param key    조회를 위한 메시지 키
     * @param arg0   개별 파라미터 대체 값
     */
    public String getMessage(Locale locale, String key, Object arg0) {

        return (getMessage(resources, locale, key, arg0));

    }

    /**
     * <p>
     * 명시된 key에 대해 기본 <code>Locale</code>로 텍스트 메시지를 리턴한다.
     * </p>
     *
     * @param resources 메세지 조회를 위한 대상 {@link Resources} 인스턴스
     * @param key       조회를 위한 메시지 키
     */
    public static String getMessage(Resources resources, String key) {

        return (getMessage(resources, (Locale) null, key));

    }


    /**
     * <p>
     * 명시된 key와 <code>Locale</code>로 텍스트 메시지를 리턴한다.
     * </p>
     *
     * @param resources 메세지 조회를 위한 대상 {@link Resources} 인스턴스
     * @param locale    메시지를 조회하기 위한 <code>Locale</code>
     * @param key       조회를 위한 메시지 키
     */
    public static String getMessage(Resources resources,
                                    Locale locale,
                                    String key) {

        String message = null;
        try {
            message = resources.getString(key, locale, null);

        } catch (ResourcesException e) {
            log.debug("Failed retrieving message for key: '" + key + "'.", e);
        }

        if (message == null) {
            message = "???" + key + "???";
        }

        return message;
    }


    /**
     * <p>
     * 명시된 key에 대해 기본 <code>Locale</code>로 텍스트 메시지를 리턴한다.
     * 메시지 내용에 포함될 대체 내용을 파라미터로 전달한다.
     * </p>
     *
     * @param resources 메세지 조회를 위한 대상 {@link Resources} 인스턴스
     * @param key       조회를 위한 메시지 키
     * @param args      대체 값들에 대한 배열
     */
    public static String getMessage(Resources resources,
                                    String key,
                                    Object[] args) {

        return getMessage(resources, (Locale) null, key, args);
    }


    /**
     * <p>
     * 명시된 key와 <code>Locale</code>로 텍스트 메시지를 리턴한다.
     * 메시지 내용에 포함될 대체 내용을 파라미터로 전달한다.
     * </p>
     *
     * @param resources 메세지 조회를 위한 대상 {@link Resources} 인스턴스
     * @param locale    메시지를 조회하기 위한 <code>Locale</code>
     * @param key       조회를 위한 메시지 키
     * @param args      대체 값들에 대한 배열
     */
    public static String getMessage(Resources resources,
                                    Locale locale,
                                    String key,
                                    Object[] args) {
        String message = getMessage(resources, locale, key);
        MessageFormat format = new MessageFormat(message);
        return (format.format(args));
    }


    /**
     * <p>
     * 명시된 key에 대해 기본 <code>Locale</code>로 텍스트 메시지를 리턴한다.
     * 메시지 내용에 포함될 대체 내용을 파라미터로 전달한다.
     * </p>
     *
     * @param resources 메세지 조회를 위한 대상 {@link Resources} 인스턴스
     * @param key       조회를 위한 메시지 키
     * @param arg0      개별 파라미터의 대체 값
     */
    public static String getMessage(Resources resources,
                                    String key, Object arg0) {

        return (getMessage(resources, (Locale) null, key, arg0));

    }

    /**
     * <p>
     * 명시된 key와 <code>Locale</code>로 텍스트 메시지를 리턴한다.
     * 메시지 내용에 포함될 대체 내용을 파라미터로 전달한다.
     * </p>
     *
     * @param resources 메세지 조회를 위한 대상 {@link Resources} 인스턴스
     * @param locale    메시지를 조회하기 위한 <code>Locale</code>
     * @param key       조회를 위한 메시지 키
     * @param arg0      개별 파라미터의 대체 값
     */
    public static String getMessage(Resources resources,
                                    Locale locale,
                                    String key,
                                    Object arg0) {

        return getMessage(resources, locale, key, new Object[]{arg0});
    }

    /**
     * <p>
     * 특정 프로퍼티 파일에 대한 메시지 리소스를 포함하는 {@link Resources}인스턴스를
     * 래핑하는 {@link Messages} 인스턴스를 생성하기 위한 팩토리 메소드이다. 패키지에서 사용하는
     * 리소스는 디폴트 메시지의 경우 <code>LocalStrings.properties</code>과 유사한 이름의,
     * 특정 <code>Locale</code>에 적합하게 로컬화된 메시지의 경우, <code>LocalStrings_en_US.properties</code>과
     * 유사한 이름의 프로퍼티 파일이 패키지 디렉토리내에 존재하는것을 가정한다.
     * </p>
     *
     * @param name 로컬 메시지 리소스로 부터 요구되는 프로퍼티 파일의 패키지+파일 이름
     *             (ie. s2.adapi.framework.resources.LocalStrings).
     */
    public static Messages getMessages(String name) {
    	ResourcesFactory factory = null;
        try {
        	Configurator config = ConfiguratorFactory.getConfigurator();
        	
            String className = config.getString(MESSAGE_FACTORTY_PREFIX + name,
            		MESSAGE_DEFAULT_FACTORTY_CLASS);
            
            factory = factoryMap.get(className);
            if (factory == null) {
            	factory = (ResourcesFactoryBase) ObjectHelper.instantiate(className);
            	synchronized(factoryMap) {
            		factoryMap.put(className, factory);
            	}
            	// 재로딩 등록
            	int reload = config.getInt(MESSAGE_RELOAD_PREFIX + name,0);
            	if (reload > 0) {
            		ResourcesReloader.instance().addResource(name, factory, reload);
            	}
            }
        } 
        catch (ConfiguratorException e) {
            log.error("getMessage() ConfiguratorException ! ", e);
            return null;
        } 
        catch (Throwable e) {
            log.error("getMessage() Class instantiation exception ! ", e);
            return null;
        } 

        try {
            Resources resources = factory.getResources(name);
            return (new Messages(resources));

        } 
        catch (ResourcesException e) {
        	log.error("getMessages() ResourceException ! ",e);
            return null;
        }

    }

    /**
     * <p>
     * 특정 프로퍼티 파일에 대한 메시지 리소스를 포함하는 {@link Resources}인스턴스를
     * 래핑하는 {@link Messages} 인스턴스를 생성하기 위한 팩토리 메소드이다. 패키지에서 사용하는
     * 리소스는 디폴트 메시지의 경우 <code>LocalStrings.properties</code>과 유사한 이름의,
     * 특정 <code>Locale</code>에 적합하게 로컬화된 메시지의 경우, <code>LocalStrings_en_US.properties</code>과
     * 유사한 이름의 프로퍼티 파일이 패키지 디렉토리내에 존재하는것을 가정한다.
     * </p>
     */
    public static Messages getMessages() {

        String name = null;
        try {
            name = ConfiguratorFactory.getConfigurator()
            		.getString(MESSAGE_DEFAULT_NAME_KEY,MESSAGE_DEFAULT_NAME);
        } catch (ConfiguratorException e) {
            log.error("getMesages() ConfiguratorException ! ", e);
        }

        if (StringHelper.isNull(name)) {
        	name = MESSAGE_DEFAULT_NAME;
        }
        
        return Messages.getMessages(name);
    }
}


