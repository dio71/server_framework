package s2.adapi.framework.resources.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import s2.adapi.framework.resources.ResourcesException;

/**
 * <p>
 * {@link s2.adapi.framework.resources.Resources} 구현을 위한 abstract base 클래스이다.
 * {@link s2.adapi.framework.resources.Resources} 구현은 공통된 base URL을 갖는 URL-accessible
 * 리소스 파일에 각 <code>Locale</code>을 지원하기 위한 name-value 매핑을 저장한다.
 * <strong>CollectionResourcesBase</strong>의 서브 클래스는 특정 로케일에 대한 name-value 매핑정보의 로딩을
 * 관리하기 위해 <code>getLocaleMap()</code> 메소드 만을 오버라이드(override)한다.
 * </p>
 *
 * <p>
 * <code>timeZone</code> 파라미터가 있는 경우에는 디폴트 컨텐트 조회 메소드에 의해 무시된다.
 * </p>
 *
 * @author kimhd
 * @since 1.0
 */
public abstract class CollectionResourcesBase extends ResourcesBase {
	
	static final long serialVersionUID = -2602233052938923076L;
	 
    /**
     * <p>
     * 명시된 논리명과 base URL을 기반으로 새로운 {@link s2.adapi.framework.resources.Resources}인스턴스를
     * 생성한다.
     * </p>
     *
     * @param name 새로운 인스턴스의 논리명
     * @param base 해당 {@link s2.adapi.framework.resources.Resources} 인스턴스에 대한
     *             name-value 매핑을 포함하고 로케일과 1:1로 대응하는 리소스 파일의 base URL
     */
    public CollectionResourcesBase(String name, String base) {
        super(name,base);
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * <p>
     * caller에 의해 <code>Locale</code>이 명시되지 않은 경우에 사용되는
     * 디폴트 <code>Locale</code>
     * </p>
     */
    protected Locale defaultLocale = Locale.getDefault();


    /**
     * <p>
     * 미리 정의된 <code>Locale</code> 리스트로, 특정 <code>Locale</code>을 키로 하여
     * <code>getLocaleList()</code>가 리턴한다.
     * </p>
     */
    private Map<Locale,List<Locale>> lists = new HashMap<Locale,List<Locale>>();


    /**
     * <p>
     * name-value 매핑에 대해 미리 정의된 <code>Map</code>들로, <code>Locale</code>을 키로 하여
     * <code>getLocaleMap()</code>가 리턴한다.
     * </p>
     */
    private Map<Locale,Map<Object,Object>> maps = new HashMap<Locale,Map<Object,Object>>();


    // ------------------------------------------------------------- Properties


    /**
     * (non-Javadoc)
     *
     * @see s2.adapi.framework.resources.impl.ResourcesBase#getKeys()
     */
    public Iterator<Object> getKeys() {

        synchronized (maps) {

            Set<Object> results = new HashSet<Object>();
            Iterator<Locale> itor = maps.keySet().iterator();
            while (itor.hasNext()) {
                Locale locale = itor.next();
                Map<Object,Object> map = maps.get(locale);
                results.addAll(map.keySet());
            }
            return (results.iterator());

        }


    }


    // ---------------------------------------------- Content Retrieval Methods


    /**
     * <p>
     * 명시된 <code>key</code>에 해당하는 컨텐트를 특정 <code>locale</code>과/혹은
     * <code>timeZone</code>을 기반으로 로컬화하여 객체로 리턴한다.
     * </p>
     *
     * @param key      요청한 컨텐트에 대한 식별자
     * @param locale   조회된 내용을 로컬화하기 위한 로케일, 디폴트 로케일인 경우 <code>null</code>
     * @param timeZone 조회된 내용을 로컬화하기 위한 타임존, 디폴트 타임존인 경우 <code>null</code>
     * @throws ResourcesException    요청한 컨텐트를 조회하거나 리턴할때 에러가 발생하는 경우
     * @throws ResourcesKeyException 명시된 키에 대한 값이 없고,<code>isReturnNull()</code>이
     *                               <code>false</code>를 리턴하는 경우
     */
    public Object getObject(String key, Locale locale, TimeZone timeZone) {

    	if (locale == null) {
            locale = defaultLocale;
        }

    	// Prepare local variables we will need
        List<Locale> list = getLocaleList(locale);
        int n = list.size();

        // Search through the Locale hierarchy for this resource key
        for (int i = 0; i < n; i++) {
        	Map<Object,Object> map = getLocaleMap(list.get(i));
            if (map.containsKey(key)) {
            	return (map.get(key));
            }
        }

        // No value for this key was located in the entire hierarchy
        if (isReturnNull()) {
            return (null);
        } else {
            throw new ResourcesException(key);
        }

    }

    public long lastModified(Locale locale) {
    	Object obj = getObject(LAST_MODIFIED_KEY,locale, null);
    	if (obj != null && obj instanceof Number) {
    		return ((Number)obj).longValue();
    	} else {
    		return -1;
    	}
    }
    
    public long lastModified() {
    	return lastModified(null);
    }
    
    // ------------------------------------------------------ Lifecycle Methods


    /**
     * <p>
     * 해당 리소스가 더이상 필요하지 않은 경우, 리소스 관리자에 의해 호출된다.
     * 이 메소드가 호출된 이후에는 해당 리소스의 getXxx()메소드를 호출할 수 없다.
     * </p>
     *
     * @throws ResourcesException finalization과정에서 에러가 발생한 경우
     */
    public void destroy() throws ResourcesException {

        synchronized (lists) {
            lists.clear();
        }
        synchronized (maps) {
            maps.clear();
        }

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * <p>
     * 특정 로케일에 대한 리소스의 위치를 파악할때 검색될 로케일의 <code>List</code>를 리턴한다.
     * 리턴된 리스트는 명시된 로케일 자신부터 시작하여 variant, 나라, 혹은 언어 지정자가
     * 생략된 로케일 순으로 구성된다.
     * 예를 들어 <code>en_US_POSIX</code> 로케일을 전달하는 경우 리턴된 리스트는
     * 다음과 같이 나라/언어/variant 의 조합에 대한 로케일 인스턴스를 포함할 것이다.
     * <ul>
     * <li><code>en_US_POSIX</code></li>
     * <li><code>en_US</code></li>
     * <li><code>en</code></li>
     * <li>(zero-length country, language, and variant)</li>
     * </ul>
     * </p>
     * <p>
     * 이 메소드에 의해 정해지는 검색 순서는 표준 자바 클래스인 <code>java.util.ResourceBundle</code>에서
     * 사용하는 계층형 검색 전략과 유사하게 구현되어 {@link s2.adapi.framework.resources.Resources}의 구현을
     * 쉽게 한다.
     * </p>
     *
     * @param locale 리스트 산출에 기본이 되는 로케일
     */
    protected List<Locale> getLocaleList(Locale locale) {

        synchronized (lists) {

            // Optimized lookup of any previously cached Map for this Locale
            List<Locale> list = lists.get(locale);
            if (list != null) {
                return (list);
            }

            // Calculate, cache, and return the list for this Locale
            list = new ArrayList<Locale>();
            String language = locale.getLanguage();
            int languageLength = language.length();
            String country = locale.getCountry();
            int countryLength = country.length();
            String variant = locale.getVariant();
            int variantLength = variant.length();

            list.add(locale);
            if (variantLength > 0) {
                list.add(new Locale(language, country, ""));
            }
            if ((countryLength > 0) && (languageLength > 0)) {
                list.add(new Locale(language, "", ""));
            }
            if ((languageLength > 0) || (countryLength > 0)) {
                list.add(new Locale("", "", ""));
            }
            lists.put(locale, list);
            return (list);

        }

    }


    /**
     * <p>
     * 특정 <code>Locale</code>에 대한 name-value 매핑을 분석하기 위해 사용되는
     * <code>Map</code>을 리턴한다.
     * 캐슁은 각 <code>Locale</code>당 <code>getLocaleMap(base,locale)</code>이 한번 수행되도록
     * 한다.
     * </p>
     *
     * @param locale name-value 매핑 맵을 리턴하기 위해 전달되는 로케일
     */
    protected Map<Object,Object> getLocaleMap(Locale locale) {

        synchronized (maps) {

            // Optimized lookup of any previously cached Map for this Locale
            Map<Object,Object> map = maps.get(locale);
            if (map != null) {
                return (map);
            }

            // Calculate, cache, and return the map for this Locale
            map = getLocaleMap(getBase(), locale);
            maps.put(locale, map);
            return (map);
        }

    }


    /**
     * <p>
     * 특정 base URL과 로케일에 대한 name-value 매핑을 포함하는 <code>Map</code>을 리턴한다.
     * 만일 명시된 <code>Locale</code>에 해당하는 매핑이 존재하지 않는 경우,
     * 빈(empty) <code>Map</code>을 리턴한다.
     * </p>
     * <p>
     * 실질적인 서브클래스는 적절한 룩업(lookup)을 수행하기 위해 이 메소드를 오버라이드
     * 해야 한다. 오버라이딩 된 메소드의 일반적인 구현은 명시된 base URL과 <code>Locale</code>
     * 를 기반으로 하는 절대 URL을 생성할것이다. 또한 명시된 리소스 파일을 조회하여  <code>Map</code>
     * 구조로 파싱할 것이다.
     * </p>
     * <p>
     * 이전에 조회된 <code>Map</code>들에 대한 캐슁은 이 메소드의 caller에 의해 수행된다.
     * 그렇기 때문에 이 메소드는 항상 특정 리소스를 조회하고 적절하게 로드하는 내용을
     * 포함해야 한다.
     * </p>
     *
     * @param baseUrl {@link s2.adapi.framework.resources.Resources} 인스턴스에 대한
     *                리소스파일의 base URL
     * @param locale  name-value 매핑을 얻기 위한 <code>Locale</code>
     */
    protected abstract Map<Object,Object> getLocaleMap(String baseUrl, Locale locale);


    /**
     * <p>
     * 명시된 <code>Locale</code>에 대한 <code>Locale</code>-specific suffix를 리턴한다.
     * 만일 명시된 <code>Locale</code>에 언어와 나라에 대한 컴포넌트가 zero-length인 경우,
     * 리턴되는 suffix도 zero length이다. 그렇지 않은 경우, suffix는 '_'뒤에 언어, 나라, 구분 속성
     * 으로 구성된다. 각 요소는 '_'로 구분된다.
     * </p>
     *
     * @param locale suffix 문자열을 구하기위한 <code>Locale</code>
     */
    protected String getLocaleSuffix(Locale locale) {

        if (locale == null) {
            locale = defaultLocale;
        }
        String language = locale.getLanguage();
        if (language == null) {
            language = "";
        }
        String country = locale.getCountry();
        if (country == null) {
            country = "";
        }
        if ((language.length() < 1) && (country.length() < 1)) {
            return ("");
        }
        StringBuffer sb = new StringBuffer();
        if (language.length() > 0) {
            sb.append('_');
            sb.append(language.toLowerCase());
        }
        if (country.length() > 0) {
            sb.append('_');
            sb.append(country.toUpperCase());
        }
        String variant = locale.getVariant();
        if ((variant != null) && (variant.length() > 0)) {
            sb.append('_');
            sb.append(variant);
        }
        return (sb.toString());

    }


}


