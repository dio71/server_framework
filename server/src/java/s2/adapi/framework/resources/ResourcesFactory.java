package s2.adapi.framework.resources;


/**
 * <p>
 * {@link ResourcesFactory}는 팩토리 패턴 인터페이스이다.
 * 이 팩토리에 전달되는 논리명을 기반으로 {@link Resources} 인스턴스가
 * 생성된다. 같은 이름으로 {@link Resources} 인스턴스를 반복적으로
 * 요청하게되면, 매번 같은 {@link Resources} 인스턴스를 리턴하게 될
 * 것이다.
 * </p>
 *
 * <p>
 * {@link ResourcesFactory}의 구현은
 * 팩토리의 인스턴스가 동적으로 생성될수 있기 때문에 <strong>반드시</strong>
 * 인자가 없는 기본 생성자를 포함해야 한다. 따라서 configuration 문자열 이외의
 * 설정 정보(configuration information)는 일반적으로 {@link ResourcesFactory} 구현
 * 클래스에서 제공하는 JavaBean 프로퍼티 setter 메소드를 통해 정해질 것이다.
 * </p>
 *
 * @author kimhd
 * @since 1.0
 */
public interface ResourcesFactory {

    /**
     * <p>
     * 팩토리에 의해 생성된 {@link Resources}인스턴스에 설정된
     * <code>returnNull</code> 프로퍼티의 값을 리턴한다.
     * </p>
     */
    public boolean isReturnNull();


    /**
     * <p>
     * 팩토리에 의해 생성된 {@link Resources}인스턴스에
     * 설정될 <code>returnNull</code> 프로퍼티 값을 지정한다.
     * </p>
     *
     * @param returnNull The new value to delegate위임될 새로운 값
     */
    public void setReturnNull(boolean returnNull);


    // --------------------------------------------------------- Public Methods


    /**
     * <p>
     * 명시된 논리명에 해당하는 {@link Resources}인스턴스를
     * 리턴한다. 필요한 경우 기본 설정 정보를 기반으로 생성하여 리턴한다.
     * </p>
     *
     * @param name 리턴될 {@link Resources} 인스턴스의 논리명
     * @throws ResourcesException 명시된 논리명의 {@link Resources}인스턴스가 리턴될 수 없는 경우
     */
    public Resources getResources(String name)
            throws ResourcesException;


    /**
     * <p>
     * 명시된 논리명에 해당하는 {@link Resources}인스턴스를
     * 리턴한다. 필요한 경우 주어진 설정 정보를 기반으로 생성하여 리턴한다.
     * </p>
     *
     * @param name   리턴될 {@link Resources} 인스턴스의 논리명
     * @param config 해당 리소스에 대한 Configuration 스트링 (사용된 {@link ResourcesFactory}
     *               구현에 종속적인 의미). 기본설정을 위해서는 <code>null</code>을 지정한다.
     * @throws ResourcesException 명시된 논리명에 해당하는 {@link Resources}
     *                            인스턴스가 리턴될 수 없는 경우
     */
    public Resources getResources(String name, String config)
            throws ResourcesException;


    /**
     * <p>
     * 이전에 리턴되었던  {@link Resources}인스턴스에 대한
     * 내부 레퍼런스를 해제한다. 각 인스턴스의 <code>destroy()</code>메소드가 호출된다.
     * </p>
     *
     * @throws ResourcesException 레퍼런스 해제시에 에러가 발생한 경우
     */
    public void release() throws ResourcesException;

    /**
     * 이전에 리턴되었던 {@link Resources}인스턴스에 대한 내부 레퍼런스를 해제한다. 
     * name으로 주어진 해당 인스턴스의  <code>destroy()</code>메소드가 호출된다.
     * @param name
     * @throws ResourcesException
     */
    public void release(String name) throws ResourcesException;

}


