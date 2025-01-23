package s2.adapi.framework.resources;

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <p>
 * 국제화된 리소스들에 대한 추상적 표현으로, 이 리소스들은 고유한 문자열
 * <code>key</code>에 의해 식별되는 임의의 오브젝트이다.
 * </p>
 * <p>
 * 로케일(Locale)과 타임존(TimeZone)파라미터(선택)를 기반으로 로컬화된 버전의 자원은
 * 다양한 형식으로 조회될 수 있다.
 * </p>
 * <p>
 * 부적절한 <code>key</code>값을 사용하는 경우, 리소스의 getter 메소드는 해당 {@link Resources} 인스턴스의
 * <code>returnNull</code> 속성의 설정값을 기반으로 상이하게 동작한다.
 * </p>
 * <p>
 * 만일 속성 값이 <code>true</code>인 경우, getter 메소드는 <code>null</code>을 리턴한다.
 * (이 경우, <code>null</code>이라는 값이 리소스의 정상적인 값에 속한다면 모호해질 수 있다.)
 * 만일 속성 값이 <code>false</code>인 경우, <code>ResourcesKeyException</code>이 throw 된다.
 * </p>
 * <p>
 * {@link Resources} 인터페이스의 각기 다른 구현(implementation)은 key에 의해 식별(represent)되는
 * 데이터 컨텐트를 획득하기 위해 다양한 메커니즘을 지원한다.
 * (예: 프로퍼티 파일, XML 파일, 데이터베이스, 웹 애플리케이션 리소스, 또는 다른 특화된 접근법)
 * </p>
 * <p>
 * {@link Resources} 인터페이스의 각기 다른 구현은 로컬화를 수행하기위해 필요한
 * <code>locale</code>과 <code>timeZone</code> 속성의 사용을 위해 서로 다른 의미론(semantic)을 적용할 수 있다.
 * 구체적인 구현을 위해서는 특정 {@link Resources} 구현을 위한 문서를 참고하라.
 * </p>
 * <p>
 * 개발자들이 {@link Resources}를 구현하기위해서 {@link s2.adapi.framework.resources.impl.ResourcesBase} 클래스를
 * 상속받아 정의하고, 해당 인터페이스에서 발생할 수 있는 변화에 대한 영향을 최소화하기 위해
 * 필요한 메소드는 오버라이드(override)하여 정의한다.
 * </p>
 */


public interface Resources extends Serializable {
	
	public static final String LAST_MODIFIED_KEY = "Last-Modified";
	
    // ------------------------------------------------------ Lifecycle Methods


    /**
     * <p>
     * getXxx() 메소드가 호출되기 전, 해당 {@link Resources} 인스턴스의 데이터를 초기화시키기 위해 호출된다.
     * </p>
     *
     * @throws ResourcesException 초기화 과정에서 에러가 발생하는 경우
     */
    public void init() throws ResourcesException;


    /**
     * <p>
     * 해당 리소스가 더이상 필요하지 않은 경우, 리소스 관리자에 의해 호출된다.
     * 이 메소드가 호출된 이후에는 해당 리소스의 getXxx()메소드를 호출할 수 없다.
     * </p>
     *
     * @throws ResourcesException finalization과정에서 에러가 발생한 경우
     */
    public void destroy() throws ResourcesException;


    // ------------------------------------------------------------- Properties


    /**
     * <p>
     * 해당 {@link Resources} 인스턴스에 정의된 키들을 <code>Iterator</code>로 리턴한다.
     * </p>
     */
    public Iterator<Object> getKeys();


    /**
     * <p>
     * 해당 {@link Resources} 인스턴스의 논리적인 이름을 리턴한다.
     * </p>
     */
    public String getName();


    /**
     * <p>
     * 리소스의 getter 메소드가 부적절한 키 값에 대해 Exception을 throw하지 않고
     * <code>null</code>을 리턴하는 경우, <code>true</code>를 리턴한다.
     * </p>
     */
    public boolean isReturnNull();


    /**
     * <p>
     * 리소스의 getter 메소드가 부적절한 키 값에 대해 exception을 throw하는 대신에
     * <code>null</code>을 리턴할지 여부를 정의하는 플래그를 설정한다.
     * </p>
     *
     * @param returnNull 새로운 플래그 값
     */
    public void setReturnNull(boolean returnNull);


    // ---------------------------------------------- Content Retrieval Methods


    /**
     * <p>
     * 명시된 <code>locale</code>과(혹은) <code>timeZone</code>의 내용을 토대로 <code>key</code>에
     * 해당하는 내용을 로컬화(localize)하여 바이트 배열 형식으로 리턴한다.
     * </p>
     *
     * @param key      요청한 내용에 대한 식별자
     * @param locale   리소스의 내용을 로컬화하여 조회하기 위한 로케일.
     *                 기본 로케일인 경우 <code>null</code>
     * @param timeZone 리소스의 내용을 로컬화하여 조회하기 위한 위한 타임존.
     *                 기본 타임존인 경우 <code>null</code>
     * @throws ResourcesException    요청한 내용을 조회하거나 리턴할때 에러가 발생하는 경우
     * @throws ResourcesKeyException 명시된 키에 대한 값이 없고 <code>isReturnNull()</code>이
     *                               <code>false</code>를 리턴하는 경우
     */
    public byte[] getBytes(String key, Locale locale, TimeZone timeZone);


    /**
     * <p>
     * 명시된 <code>locale</code>과(혹은) <code>timeZone</code>의 내용을 토대로
     * <code>key</code>에 해당하는 내용을 로컬화(localize)하여 InputStream 으로 리턴한다.
     * </p>
     *
     * @param key      요청한 내용에 대한 식별자
     * @param locale   리소스의 내용을 로컬화하여 조회하기 위한 로케일.
     *                 기본 로케일인 경우 <code>null</code>
     * @param timeZone 리소스의 내용을 로컬화하여 조회하기 위한 타임존.
     *                 기본 타임존인 경우 <code>null</code>
     * @throws ResourcesException    요청한 내용을 조회하거나 리턴할때 에러가 발생하는 경우
     * @throws ResourcesKeyException 명시된 키에 대한 값이 없고 <code>isReturnNull()</code>이
     *                               <code>false</code>를 리턴하는 경우
     */
    public InputStream getInputStream(String key, Locale locale,
                                      TimeZone timeZone);


    /**
     * <p>
     * 명시된 <code>locale</code>과(혹은) <code>timeZone</code>의 내용을 토대로
     * <code>key</code>에 해당하는 내용을 로컬화(localize)하여 Object 로 리턴한다.
     * </p>
     *
     * @param key      요청한 내용에 대한 식별자
     * @param locale   리소스의 내용을 로컬화하여 조회하기 위한 로케일.
     *                 기본 로케일인 경우 <code>null</code>
     * @param timeZone 리소스의 내용을 로컬화하여 조회하기 위한 타임존.
     *                 기본 타임존인 경우 <code>null</code>
     * @throws ResourcesException    요청한 내용을 조회하거나 리턴할때 에러가 발생하는 경우
     * @throws ResourcesKeyException 명시된 키에 대한 값이 없고 <code>isReturnNull()</code>이
     *                               <code>false</code>를 리턴하는 경우
     */
    public Object getObject(String key, Locale locale, TimeZone timeZone);


    /**
     * <p>
     * 명시된 <code>locale</code>과(혹은) <code>timeZone</code>의 내용을 토대로
     * <code>key</code>에 해당하는 내용을 로컬화(localize)하여 Reader 로 리턴한다.
     * </p>
     *
     * @param key      요청한 내용에 대한 식별자
     * @param locale   리소스의 내용을 로컬화하여 조회하기 위한 로케일.
     *                 기본 로케일인 경우 <code>null</code>
     * @param timeZone 리소스의 내용을 로컬화하여 조회하기 위한 타임존.
     *                 기본 타임존인 경우 <code>null</code>
     * @throws ResourcesException    요청한 내용을 조회하거나 리턴할때 에러가 발생하는 경우
     * @throws ResourcesKeyException 명시된 키에 대한 값이 없고 <code>isReturnNull()</code>이
     *                               <code>false</code>를 리턴하는 경우
     */
    public Reader getReader(String key, Locale locale, TimeZone timeZone);


    /**
     * <p>
     * 명시된 <code>locale</code>과(혹은) <code>timeZone</code>의 내용을 토대로
     * <code>key</code>에 해당하는 내용을 로컬화(localize)하여 String으로 리턴한다.
     * </p>
     *
     * @param key      요청한 내용에 대한 식별자
     * @param locale   리소스의 내용을 로컬화하여 조회하기 위한 로케일.
     *                 기본 로케일인 경우 <code>null</code>
     * @param timeZone 리소스의 내용을 로컬화하여 조회하기 위한 타임존.
     *                 기본 타임존인 경우 <code>null</code>
     * @throws ResourcesException    요청한 내용을 조회하거나 리턴할때 에러가 발생하는 경우
     * @throws ResourcesKeyException 명시된 키에 대한 값이 없고 <code>isReturnNull()</code>이
     *                               <code>false</code>를 리턴하는 경우
     */
    public String getString(String key, Locale locale, TimeZone timeZone);

    /**
     * 해당 리소스가 마지막에 수정된 시간을 반환한다.
     * @return
     */
    public long lastModified(Locale locale);
    
    public long lastModified();
}




