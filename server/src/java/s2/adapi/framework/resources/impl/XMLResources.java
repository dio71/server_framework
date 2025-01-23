package s2.adapi.framework.resources.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import s2.adapi.framework.util.SystemHelper;

/**
 * <p>
 * <strong> XMLResources </strong> 클래스는 {@link s2.adapi.framework.resources.Resources}에 대한
 * 구현 클래스이다. '기본(base) URL + 각 도큐먼트의 메세지에 적용될
 * <code>Locale</code>을 반영하는 name suffix' 유형의 XML 도큐먼트들은 이 클래스로 래핑된다.
 * 리소스는 계층적인 XML 도큐먼트내에서 <code>java.util.ResourceBundle.getBundle().</code>을 사용하여
 * 동일한 방식으로 lookup된다.
 * <p/>
 * <p>
 * 생성자에 전달되는 기본(base) URL은 XML 도큐먼트 그룹의 base name을 포함해야 한다.
 * 예를 들어, configuration URL이 <code>http://localhost/foo/Bar</code>로 전달되는 경우
 * <code>en_US</code> 로케일에 해당하는 리소스는 URL <code>http://localhost/foo/Bar_en_US.xml</code>
 * 로 저장되며, 디폴트 리소스는  <code>http://localhost/foo/Bar.xml</code>로 저장된다.
 * </p>
 *
 * <p>
 * 요구되는 XML 도큐먼트의 구조는 매우 단순하다.
 * </p>
 * <ul>
 * <li>최상위 레벨(top level)의 element는<code>&lt;resources&gt;</code>이어야 한다.</li>
 * <li>각각의 name-value 쌍은 중첩된 <code>&lt;resource&gt;</code> element로 나타낸다.</li>
 * <li>각 <code>&lt;resource&gt;</code> element는,리소스 키를 포함하는 <code>id</code> 속성과
 * 키에 대한 값에 해당하는 문자열을 포함하는 body로 구성된다.</li>
 * </ul>
 * <pre>
 * &lt;?xml version="1.0" encoding="euc-kr"?&gt;
 * &lt;resources&gt;
 *     &lt;resource id="error.login.InvalidPassword" &gt;&lt;![CDATA[잘못된 ID 또는 패스워드입니다.]] &gt;&lt; /resource&gt;
 *     &lt;resource id="errors.required" &gt;&lt;![CDATA[{0}가 필요합니다.]]&gt;&lt; /resource&gt;
 *     &lt;resource id="errors.email" &gt;&lt;![CDATA[{0}는 유효하지 않은 이메일 주소입니다.]] &gt;&lt; /resource&gt;
 * &lt;/resources&gt;
 * </pre>
 */
public class XMLResources extends CollectionResourcesBase {

	static final long serialVersionUID = -8618200758882023607L;
	
    /**
     * <p>
     * 클래스에서 사용할 <code>Log</code> 인스턴스
     * </p>
     */
    private static final Logger log = LoggerFactory.getLogger(XMLResources.class);

    /**
     * <p>
     * 명시된 논리명(logical name)과 기본(base) 리소스 URL을 기반으로 {@link s2.adapi.framework.resources.Resources}인스턴스를
     * 생성한다.
     * </p>
     *
     * @param name 새로운 인스턴스의 논리명(Logical name)
     * @param base 리소스 키와 값을 포함하는 프로퍼티 파일의 그룹에 대한 기본(base) URL
     */
    public XMLResources(String name, String base) {
        super(name, base);
    }

    /**
     * <p>
     * 특정 base URL과 <code>Locale</code>에 해당하는 name-value 의 매핑내용을
     * <code>Map</code>으로 리턴한다. 명시된 <code>Locale</code>에 해당하는 매핑정보가
     * 정의되어있지 않은 경우, 빈(empty) <code>Map</code>을 리턴한다.
     * </p>
     *
     * <p>
     * 실질적인 서브클래스들은 이 메소드를 오버라이드(override)하여
     * 적절한 룩업(lookup)을 수행한다. 일반적으로 구현은 명시된 base URL과
     * <code>Locale</code>를 기반으로 절대 URL을 생성하여 명시된 리소스 파일을 조회하고
     * <code>Map</code> 구조로 파싱한다.
     * </p>
     *
     * <p>
     * 이전에 조회된 <code>Map</code>들에 대한 caching은 이 메소드의 caller에 의해 수행되어야
     * 한다. 따라서 이 메소드는 특정 리소스에 대한 조회와 적절한 로드(load)를 항상 시도해야 한다.
     * </p>
     *
     * @param baseUrl 해당 {@link s2.adapi.framework.resources.Resources} 인스턴스에 대한
     *                리소스 파일의 Base URL
     * @param locale  name-value 매핑에 요구되는 <code>Locale</code>
     */
    protected Map<Object,Object> getLocaleMap(String baseUrl, Locale locale) {

        if (log.isDebugEnabled()) {
            log.debug("Loading locale '" + locale + "' resources from base '" +
                    baseUrl + "'");
        }

        final Map<Object,Object> map = new HashMap<Object,Object>();
        String name = baseUrl + getLocaleSuffix(locale) + ".xml";
        InputStream stream = null;

        try {

            // 해당 로케일의 URL을 위한 입력 스트림을 open한다.
            if (log.isDebugEnabled()) {
                log.debug("Absolute URL is '" + name + "'");
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

            // 새로운 Digester 인스턴스를 생성하고 설정한다.
            if (log.isTraceEnabled()) {
                log.trace("Creating Digester instance");
            }
            
            // handler that pasres resource files.
            DefaultHandler handler = new DefaultHandler() {
            	private String key = null;
            	private StringBuilder sb = new StringBuilder();
            	
            	public void startElement(String namespaceURI, String sName, String qName,
            			Attributes attrs) throws SAXException {
            		if ("resource".equalsIgnoreCase(qName)) {
            			key = attrs.getValue("id");
            			sb.setLength(0);
            		}
            	}

            	public void endElement(String namespaceURI, String sName, String qName)
            			throws SAXException {
            		if ("resource".equalsIgnoreCase(qName)) {
            			map.put(key, sb.toString().trim());
            			sb.setLength(0);
            		}
            	}
            	
            	public void characters(char buf[], int offset, int len) throws SAXException {
            		sb.append(buf, offset, len);
            	}
            };

            SAXParserFactory.newInstance().newSAXParser().parse(stream, handler);

            // 최종수정일자를 리소스 내용으로 추가 (2009.11.30 김형도)
            map.put(LAST_MODIFIED_KEY, lastModifiedTime);
            
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("error loading locale '" + locale +
                        "' from base '" + baseUrl + "' :" + e.toString());
            }
            map.clear();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    log.error("Error closing stream.", e);
                }
                stream = null;
            }
        }

        return (map);
    }
}


