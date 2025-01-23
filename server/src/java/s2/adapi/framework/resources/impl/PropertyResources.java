package s2.adapi.framework.resources.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.util.SystemHelper;

/**
 * <p>
 * {@link s2.adapi.framework.resources.Resources}에 대한 구현 클래스이다.
 * <strong>PropertyResources</strong> 클래스는 <code>Locale</code>과 1:1로 대응되는 프로퍼티 파일의 집합을
 * 래핑한다. 각 프로퍼티 파일은 '베이스 URL + 해당 도큐먼트의 메시지에 적용될 <code>Locale</code>을
 * 반영하는 name suffix'을 갖는다.
 * 리소스는 프로퍼티 파일내의 계층구조에서 <code>java.util.ResourceBundle.getBundle().</code>과
 * 동일한 방식으로 lookup된다.
 * </p>
 *
 * <p>
 * 생성자에 전달되는 베이스 URL은 프로퍼티 파일 그룹의 베이스 네임을 포함해야 한다.
 * 예를 들어 configuration URL이 <code>http://localhost/foo/Bar</code>로 전달되면
 * <code>en_US</code> 로케일에 대한 리소스는 <code>http://localhost/foo/Bar_en_US.properties</code>로
 * 저장될 것이다. 디폴트 리소스는 <code>http://localhost/foo/Bar.properties</code>로
 * 저장된다.
 * </p>
 *
 */
public class PropertyResources extends CollectionResourcesBase {

	static final long serialVersionUID = 8121943118886275038L;
	
    /**
     * <p>
     * 로그를 남기기 위해 사용되는 <code>Log</code> 인스턴스
     * </p>
     */
    private static final Logger log = LoggerFactory.getLogger(PropertyResources.class);

    // ----------------------------------------------------------- Constructors

    /**
     * <p>
     * 명시된 논리명과 베이스 URL을 기반으로 새로운 {@link s2.adapi.framework.resources.Resources}
     * 인스턴스를 생성한다.
     * </p>
     *
     * @param name 새로운 인스턴스의 논리명
     * @param base 리소스의 키와 값을 포함하는 프로퍼티 파일 집합의
     *             베이스 URL
     */
    public PropertyResources(String name, String base) {
        super(name, base);
    }


    // ------------------------------------------------------ Protected Methods


    /**
     * <p>
     * 명시된 베이스 URL과 <code>Locale</code>에 해당하는 name-value 매핑을
     * 포함하는 <code>Map</code>을 리턴한다. 만일 요청한 <code>Locale</code>에
     * 대한 매핑이 정의되어 있지 않은 경우에는 빈(empty) <code>Map</code>을 리턴한다.
     * </p>
     *
     * <p>
     * 실질적인 서브클래스들은 이 메소드를 오버라이드(override)하여
     * 적절한 룩업(lookup)을 수행한다. 일반적으로 구현은 명시된 base URL과
     * <code>Locale</code>를 기반으로 절대 URL을 생성하여 명시된 리소스 파일을 조회하고
     * <code>Map</code> 구조로 파싱한다.
     * </p>
     *
     * <p>이전에 조회된 <code>Map</code>들에 대한 caching은 이 메소드의 caller에 의해 수행되어야
     * 한다. 따라서 이 메소드는 특정 리소스에 대한 조회와 적절한 로드(load)를 항상 시도해야 한다.
     * </p>
     *
     * @param baseUrl 해당 {@link s2.adapi.framework.resources.Resources} 인스턴스에 대한
     *                베이스 URL
     * @param locale  name-value 매핑에 필요한 <code>Locale</code>
     */
    protected Map<Object,Object> getLocaleMap(String baseUrl, Locale locale) {

        if (log.isDebugEnabled()) {
            log.debug("Loading locale '" + locale + "' resources from base '" +
                    baseUrl + "'");
        }

        Properties props = new Properties();
        String name = baseUrl + getLocaleSuffix(locale) + ".properties";
        InputStream stream = null;

        try {

            // Open an input stream to the URL for this locale (if any)
            if (log.isTraceEnabled()) {
                log.trace("Absolute URL is '" + name + "'");
            }
            URL url = null;
            
            try {
                // name이 URL spec 형식의 스트링이라고 가정하고 바로 URL을 생성한다.
                url = new URL(name);
            } catch (MalformedURLException ex) {
                // name이 URL spec 형식의 스트링이 아니므로 classpath에서 해당 파일을 찾아 URL을 생성한다.
                url = SystemHelper.getResourceURL(name);
            }
            
            // 최종 수정일자를 가져온다. (2009.11.30 김형도)
            long lastModifiedTime = url.openConnection().getLastModified(); 
            
            stream = url.openStream();

            // Parse the input stream and populate the name-value mappings map
            if (log.isTraceEnabled()) {
                log.trace("Parsing input resource");
            }
            props.load(stream);
            
            // 최종수정일자를 리소스 내용으로 추가 (2009.11.30 김형도)
            props.put(LAST_MODIFIED_KEY, lastModifiedTime);

        } catch (FileNotFoundException e) {

            // Log and swallow this exception
            if (log.isDebugEnabled()) {
                log.debug("No resources for locale '" + locale +
                        "' from base '" + baseUrl + "'");
            }
            props.clear();

        } catch (IOException e) {

            log.warn("IOException loading locale '" + locale +
                    "' from base '" + baseUrl + "'", e);
            props.clear();

        } finally {

            // Close the input stream that was opened earlier
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    log.error("Error closing stream.", e);
                }
                stream = null;
            }

        }

        // Return the populated (or empty) properties
        return (props);

    }


}


