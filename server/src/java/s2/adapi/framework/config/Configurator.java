package s2.adapi.framework.config;

import java.io.InputStream;
import java.util.Set;

/**
 * <p>
 * <strong>Configurator</strong>의 인터페이스이다.
 * Configurator는 구성 파일에 설정된 값과 시스템 프로퍼티에 설정된 값을 제공한다.
 * 요청된 key 값에 대하여 시스템 프로퍼티에 값이 존재할 경우 그 값을 우선적으로 반환하며, 
 * 시스템 프로퍼티에 값이 없을 경우에는 구성 파일에 설정된 값을 반환한다.
 * </p>
 * <p>
 * Configurator의 구현 클래스는 Configurator 객체가 생성될 때 시스템 프로퍼티의 값들을 복사하여 저장해 놓아
 * 나중에 시스템 프로퍼티의 값이 바뀌어도 Configurator는 원래의 값을 반환하도록 구현해야 한다.
 * </p>
 * <p>
 * 위 Interface을 이용한 구성 화일의 로드하는 예제는 아래와 같다.
 * </p>
 *
 * <pre>
 *     Configurator configurator = ConfiguratorFactory.getInstance().getConfigurator();
 * </pre>
 */
public interface Configurator {

    /**
     * <p>
     * <strong>ConfiguratorFactory</strong>로 부터 파라메터로 받은 configuration file의
     * <code>InputStream</code>을 읽어서 메모리에 적재하는 초기화 로직.
     * </p>
     *
     * @param stream configuration file의 stream 오브젝트.
     * @throws ConfiguratorException 읽혀지는 <code>InputStream</code>의 IOException 발생시.
     */
	public void doConfigure(InputStream stream) throws ConfiguratorException;

    /**
     * <p>
     * 시스템 프로퍼티 또는 <strong>Configurator</strong>에 의해 메모리에 로드된 configuration file의 원하고자 하는 특정 key를 인자로 하여
     * 해당 Value를 String형으로 리턴한다.
     * </p>
     *
     * @param key 원하고자하는 key값
     * @return 원하고자하는 key값에 해당하는 value
     * @throws ConfiguratorException key값이 configuration file에 존재하지 않을 경우 발생
     */
    public String getString(String key) throws ConfiguratorException;

    /**
     * <p>
     * 시스템 프로퍼티 또는 <strong>Configurator</strong>에 의해 메모리에 로드된 configuration file의 원하고자 하는 특정 key를 인자로 하여
     * 해당 Value를 String형으로 리턴한다.
     * </p>
     *
     * @param key          원하고자하는 key값
     * @param defaultValue 값이 없을 경우 default값
     * @return 원하고자하는 key값에 해당하는 value
     */
    public String getString(String key, String defaultValue);

    /**
     * <p>
     * <시스템 프로퍼티 또는 strong>Configurator</strong>에 의해 메모리에 로드된 configuration file의 원하고자 하는 특정 key를 인자로 하여
     * 해당 Value를 int형(primitive)으로 리턴한다.
     * </p>
     *
     * @param key 원하고자하는 key값.
     * @return 원하고자하는 key값에 해당하는 value.
     * @throws ConfiguratorException value값이 int형으로 가져올 수 없는 데이터 형 일 경우 : <code>NumberFormatExcetion</code>.
     */
    public int getInt(String key) throws ConfiguratorException;

    /**
     * <p>
     * 시스템 프로퍼티 또는 <strong>Configurator</strong>에 의해 메모리에 로드된 configuration file의 원하고자 하는 특정 key를 인자로 하여
     * 해당 Value를 int형(primitive)으로 리턴한다.
     * </p>
     *
     * @param key          원하고자하는 key값.
     * @param defaultValue 값이 없을 경우 default값
     * @return 원하고자하는 key값에 해당하는 value.
     * @throws ConfiguratorException value값이 int형으로 가져올 수 없는 데이터 형 일 경우 : <code>NumberFormatExcetion</code>.
     */
    public int getInt(String key, int defaultValue);

    /**
     * <p>
     * 시스템 프로퍼티 또는 <strong>Configurator</strong>에 의해 메모리에 로드된 configuration file의 원하고자 하는 특정 key를 인자로 하여
     * 해당 Value를 boolean형(primitive)으로 리턴한다.
     * </p>
     *
     * @param key 원하고자하는 key값.
     * @return 원하고자하는 key값에 해당하는 value.
     * @throws ConfiguratorException value값이 boolean형으로 가져올 수 없는 데이터 형 일 경우.
     */
    public boolean getBoolean(String key) throws ConfiguratorException;

    /**
     * <p>
     * 시스템 프로퍼티 또는 <strong>Configurator</strong>에 의해 메모리에 로드된 configuration file의 원하고자 하는 특정 key를 인자로 하여
     * 해당 Value를 boolean형(primitive)으로 리턴한다.
     * </p>
     *
     * @param key          원하고자하는 key값.
     * @param defaultValue 값이 없을 경우 default값
     * @return 원하고자하는 key값에 해당하는 value.
     * @throws ConfiguratorException value값이 boolean형으로 가져올 수 없는 데이터 형 일 경우.
     */
    public boolean getBoolean(String key, boolean defaultValue);

    /**
     * <p>
     * 시스템 프로퍼티 또는 <strong>Configurator</strong>에 의해 메모리에 로드된 configuration file의 원하고자 하는 특정 key를 인자로 하여
     * 해당 Value를 long형(primitive)으로 리턴한다.
     * </p>
     *
     * @param key 원하고자하는 key값.
     * @return 원하고자하는 key값에 해당하는 value.
     * @throws ConfiguratorException value값이 long형으로 가져올 수 없는 데이터 형 일 경우.
     */
    public long getLong(String key) throws ConfiguratorException;

    /**
     * <p>
     * 시스템 프로퍼티 또는 <strong>Configurator</strong>에 의해 메모리에 로드된 configuration file의 원하고자 하는 특정 key를 인자로 하여
     * 해당 Value를 long형(primitive)으로 리턴한다.
     * </p>
     *
     * @param key          원하고자하는 key값.
     * @param defaultValue 값이 없을 경우 default값
     * @return 원하고자하는 key값에 해당하는 value.
     * @throws ConfiguratorException value값이 long형으로 가져올 수 없는 데이터 형 일 경우.
     */
    public long getLong(String key, long defaultValue);

    /**
     * <p>
     * 시스템 프로퍼티 또는 <strong>Configurator</strong>에 의해 메모리에 로드된 configuration file의 원하고자 하는 특정 key를 인자로 하여
     * 해당 Value를 파일 경로 문자열로 리턴한다. 문자열에 포함된 디렉토리 분리 문자를 OS에 맞는 문자로 통일시킨다.
     * </p>
     *
     * @param key          원하고자하는 key값
     * @param defaultValue 값이 없을 경우 default값
     * @return 원하고자하는 key값에 해당하는 value의 경로 문자열
     */
    public String getPath(String key, String defaultPath);
    
    /**
     * <p>
     * Configuration으로 정의된 항목들의 Key 집합을 리턴한다.
     * </p>
     * @return key 집합
     */
    public Set<Object> getKeySet();
    
    /**
     * 설정 정보 clear
     */
    public void clear();
    
}