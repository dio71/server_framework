package s2.adapi.framework.resources.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.config.Configurator;
import s2.adapi.framework.config.ConfiguratorException;
import s2.adapi.framework.config.ConfiguratorFactory;
import s2.adapi.framework.resources.Resources;
import s2.adapi.framework.resources.ResourcesException;

/**
 * <p>
 * {@link s2.adapi.framework.resources.Resources}를 편리하게 구현할 수 있도록
 * 제공되는 베이스 클래스이다.
 * </p>
 *
 * <p>
 * <code>getObject()</code>를 제외한 나머지 컨텐트 조회 메소드들에 대해서는 기본적인 구현이 제공된다.
 * 다른 컨텐트 조회에 대한 기본적인 메소드의 구현은 <code>getString()</code>의
 * 방식으로 코딩된다.
 * </p>
 *
 */
public abstract class ResourcesBase implements Resources {
	
	private static final long serialVersionUID = -4300912549631752778L;

	private static final Logger log = LoggerFactory.getLogger(ResourcesBase.class);
	
	/**
	 * 리소스 명으로부터 리소스 베이스 값을 얻기 위하여여 필요한 키를 생성할 때 사용하는 prefix 
	 */
	public static final String RESOURCES_KEY_PREFIX = "s2adapi.resources.";
	
    // ----------------------------------------------------------- Constructors
    
    /**
     * <p>
     * 논리적인 이름을 갖는 새 {@link s2.adapi.framework.resources.Resources} 인스턴스를
     * 생성한다.
     * </p>
     *
     * @param name 새로운 인스턴스의 논리적인 이름
     */
	public ResourcesBase(String name) {
		this(name,null);
	}
	
    /**
     * <p>
     * 논리적인 이름을 갖는 새 {@link s2.adapi.framework.resources.Resources} 인스턴스를
     * 생성한다.
     * </p>
     *
     * @param name 새로운 인스턴스의 논리적인 이름
     * @param base 리소스 생성을 위한 설정 정보
     */
    public ResourcesBase(String name, String base) {
        
		try {
			Configurator configurator = ConfiguratorFactory.getConfigurator();
			this.name = name;
			
			if (base == null) {
				// 리소스 정보를 가져올 Key를 생성한다.
				String baseKey = RESOURCES_KEY_PREFIX + name;
				
				// 리소스 정보를 가져온다.
				this.base = configurator.getString(baseKey,this.name);
			} else {
				this.base = base;
			}
		} catch (ConfiguratorException e) {
			log.error("cannot get resources configuration.["+this.name+"]",e);
		}
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * <p>
     * {@link s2.adapi.framework.resources.Resources} 인스턴스의 논리적인 이름
     * </p>
     */
    private String name = null;


    /**
     * 실제 Resources를 가져오기 위한 파일 경로 또는 SQL 등의 설정 내용
     */
    private String base = null;
    
    /**
     * <p>
     * 리소스의 getter 메소드가 잘못된 키값에 대해 (예외를 throw하는 대신에)
     * <code>null</code>을 리턴 할지에 대한 여부를 나타내는 플래그
     * </p>
     */
    private boolean returnNull = true;


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * <p>
     * 해당 {@link s2.adapi.framework.resources.Resources}인스턴스의 데이터 컨텐트를 초기화
     * 하기 위해 호출되는 메소드이다. 이 메소드는 <code>getXxx()</code> 메소드가 호출되기
     * 전에 수행되어야 한다.
     * </p>
     * <p>
     * 이 메소드에 대한 기본적인 구현은 제공되지 않는다.
     * </p>
     *
     * @throws ResourcesException 초기화 과정중에 에러가 발생한 경우
     */
    public void init() throws ResourcesException {

        // The default implementation does nothing

    }


    /**
     * <p>
     * 리소스의 관리자가 더이상 리소스를 필요로하지 않는 경우에 호출되는 메소드이다.
     * 이 메소드가 수행된 이후에는 어떠한 <code>getXxx()</code> 메소드도 호출될 수
     * 없다.
     * </p>
     *
     * <p>
     * 이 메소드에 대한 기본적인 구현은 제공되지 않는다.
     * </p>
     *
     * @throws ResourcesException finalization시에 에러가 발생한 경우
     */
    public void destroy() throws ResourcesException {

        // The default implementation does nothing

    }


    // ------------------------------------------------------------- Properties


    public abstract Iterator<Object> getKeys();


    /**
     * <p>
     * 해당 {@link s2.adapi.framework.resources.Resources} 인스턴스에 대한 논리적인
     * 이름을 리턴한다.
     * </p>
     */
    public String getName() {

        return (this.name);

    }


    /**
     * 해당 {@link s2.adapi.framework.resources.Resources} 인스턴스를 생성하기 위한 정보를 리턴한다.
     * @return
     */
    public String getBase() {
    	return (this.base);
    }
    
    /**
     * <p>
     * 리소스의 getter 메소드가 잘못된 키 값에 대해 예외를 throw 하지않고,
     * <code>null</code>을 리턴할 경우, 이 메소드는 <code>true</code>를
     * 리턴한다.
     * </p>
     */
    public boolean isReturnNull() {

        return (this.returnNull);

    }


    /**
     * <p>
     * 리소스 getter 메소드가 잘못된 키 값에 대해 예외를 throw 하는 대신에,
     * <code>null</code>을 리턴할지에 대한 여부를 정의하는 플래그를 설정한다.
     * </p>
     *
     * @param returnNull 새롭게 정의되는 플래그 값
     */
    public void setReturnNull(boolean returnNull) {

        this.returnNull = returnNull;

    }


    // ---------------------------------------------- Content Retrieval Methods


    /**
     * <p>
     * 주어진 <code>key</code>에 대한 컨텐트를 바이트 배열형태로 리턴한다.
     * 리턴되는 컨텐트는 명시된 <code>locale</code>과/혹은 <code>timeZone</code>을
     * 기반으로 로컬화(localize)된다.
     * </p>
     * <p>
     * <code>getString()</code>을 호출하고 해당 값을 바이트 배열로 변환하는 내용이
     * 디폴트로 구현되어있다.
     * </p>
     *
     * @param key      요청한 컨텐트에 대한 식별자
     * @param locale   로컬화를 수행하기 위한 로케일. 디폴트 로케일인 경우 <code>null</code>
     * @param timeZone 로컬화를 수행하기 위한 타임존. 디폴트 타임존인 경우 <code>null</code>
     * @throws ResourcesException 요청한 컨텐트를 조회하거나 리턴할때 에러가 발생하는 경우
     * @throws s2.adapi.framework.resources.ResourcesKeyException
     *                            주어진 키에 대한 값이 존재하지 않고,
     *                            <code>isReturnNull()</code> 이 <code>false</code>를 리턴하는 경우
     */
    public byte[] getBytes(String key, Locale locale, TimeZone timeZone) {

        String value = getString(key, locale, timeZone);
        if (value == null) {
            return (null);
        } else {
            return (value.getBytes());
        }

    }


    /**
     * <p>
     * 인자로 받은 특정 <code>key</code> 에 대한 컨텐트를 InputStream형태로 리턴한다.
     * 이 컨텐트는 명시된 <code>locale</code>과/혹은 <code>timeZone</code>에 의해 로컬화된다.
     * </p>
     * <p>
     * 디폴트로 <code>getsBytes()</code>를 호출하고나서 결과로 얻은 바이트 배열을 input stream으로
     * 리턴하게끔 제공된다.
     * </p>
     *
     * @param key      요청한 컨텐트에 대한 식별자
     * @param locale   조회결과를 로컬화하기 위한 로케일, 디폴트 로케일을 사용할 경우 <code>null</code>
     * @param timeZone 조회결과를 로컬화하기 위한 타임존, 디폴트 타임존을 사용할 경우 <code>null</code>
     * @throws ResourcesException 요청한 컨텐트를 조회하거나 리턴할때 에러가 발생하는 경우
     * @throws s2.adapi.framework.resources.ResourcesKeyException
     *                            명시된 키에 해당하는 값이 존재하지 않고
     *                            <code>isReturnNull()</code>이 <code>false</code>를 리턴하는 경우
     */
    public InputStream getInputStream(String key, Locale locale,
                                      TimeZone timeZone) {

        byte bytes[] = getBytes(key, locale, timeZone);
        if (bytes == null) {
            bytes = new byte[0];
        }
        return (new ByteArrayInputStream(bytes));

    }


    /**
     * <p>
     * 인자로 받은 특정 <code>key</code> 에 대한 컨텐트를 Object형태로 리턴한다.
     * 이 컨텐트는 명시된 <code>locale</code>과/혹은 <code>timeZone</code>에 의해 로컬화된다.
     * </p>
     * <p>
     * 이 메소드는 기본적인 구현을 제공하지 않으므로, 실제 서브클래스가 구체적인 구현내용을 작성해야한다.
     * </p>
     *
     * @param key      요청한 컨텐트에 대한 식별자
     * @param locale   조회결과를 로컬화하기 위한 로케일, 디폴트 로케일을 사용할 경우 <code>null</code>
     * @param timeZone 조회결과를 로컬화하기 위한 타임존, 디폴트 타임존을 사용할 경우 <code>null</code>
     * @throws ResourcesException 요청한 컨텐트를 조회하거나 리턴할때 에러가 발생하는 경우
     * @throws s2.adapi.framework.resources.ResourcesKeyException
     *                            명시된 키에 해당하는 값이 존재하지 않고
     *                            <code>isReturnNull()</code>이 <code>false</code>를 리턴하는 경우
     */
    public abstract Object getObject(String key, Locale locale, TimeZone timeZone);


    /**
     * <p>
     * 인자로 받은 특정 <code>key</code> 에 대한 컨텐트를 Reader형태로 리턴한다.
     * 이 컨텐트는 명시된 <code>locale</code>과/혹은 <code>timeZone</code>에 의해 로컬화된다.
     * </p>
     * <p>
     * 기본적으로 <code>getString()</code>을 호출하고 결과로 얻은 캐릭터들로 부터
     * reader를 생성하여 리턴하는 기능을 제공한다.
     * </p>
     *
     * @param key      요청한 컨텐트에 대한 식별자
     * @param locale   조회결과를 로컬화하기 위한 로케일, 디폴트 로케일을 사용할 경우 <code>null</code>
     * @param timeZone 조회결과를 로컬화하기 위한 타임존, 디폴트 타임존을 사용할 경우 <code>null</code>
     * @throws ResourcesException 요청한 컨텐트를 조회하거나 리턴할때 에러가 발생하는 경우
     * @throws s2.adapi.framework.resources.ResourcesKeyException
     *                            명시된 키에 해당하는 값이 존재하지 않고
     *                            <code>isReturnNull()</code>이 <code>false</code>를 리턴하는 경우
     */
    public Reader getReader(String key, Locale locale, TimeZone timeZone) {

        String value = getString(key, locale, timeZone);
        if (value == null) {
            return (null);
        } else {
            return (new StringReader(value));
        }

    }


    /**
     * <p>
     * 인자로 받은 특정 <code>key</code> 에 대한 컨텐트를 String형태로 리턴한다.
     * 이 컨텐트는 명시된 <code>locale</code>과/혹은 <code>timeZone</code>에 의해 로컬화된다.
     * </p>
     * <p>
     * 기본적으로 <code>getObject()</code>를 호출하고 필요한 경우, 결과를 String을 변환하도록
     * 구현된다.
     * </p>
     *
     * @param key      요청한 컨텐트에 대한 식별자
     * @param locale   조회결과를 로컬화하기 위한 로케일, 디폴트 로케일을 사용할 경우 <code>null</code>
     * @param timeZone 조회결과를 로컬화하기 위한 타임존, 디폴트 타임존을 사용할 경우 <code>null</code>
     * @throws ResourcesException 요청한 컨텐트를 조회하거나 리턴할때 에러가 발생하는 경우
     * @throws s2.adapi.framework.resources.ResourcesKeyException
     *                            명시된 키에 해당하는 값이 존재하지 않고
     *                            <code>isReturnNull()</code>이 <code>false</code>를 리턴하는 경우
     */
    public String getString(String key, Locale locale, TimeZone timeZone) {

        Object value = getObject(key, locale, timeZone);
        if (value == null) {
            return (null);
        } else if (value instanceof String) {
            return ((String) value);
        } else {
            return (value.toString());
        }

    }

}


