package s2.adapi.framework.resources.impl;

import s2.adapi.framework.resources.Resources;
import s2.adapi.framework.resources.ResourcesException;

/**
 * <p>
 *
 * {@link s2.adapi.framework.resources.Resources} 인스턴스를 생성하는 {@link s2.adapi.framework.resources.ResourcesFactory}에
 * 대한 구현 클래스이다. {@link s2.adapi.framework.resources.Resources} 인스턴스는
 * '베이스 URL + 각 도큐먼트의 메세지에 적용될 <code>Locale</code>을 반영하는 name suffix' 유형의
 * 프로퍼티 파일의 그룹을 래핑한다. 리소스는 도큐먼트내에서 <code>java.util.ResourceBundle.getBundle().</code>에 의해 수행되는것과
 * 동일한 방식으로 lookup된다.
 * </p>
 *
 * <p>
 * <code>createResources()</code> 메소드를 통해 전달되는 configuration 변수는
 * 프로퍼티 파일 그룹의 베이스 네임 URL이어야 한다.
 * 예를 들어, configuration URL이 <code>http://localhost/foo/Bar</code>로 전달된다면
 * <code>en_US</code> 로케일에 대한 리소스는 <code>http://localhost/foo/Bar_en_US.properties</code>에
 * 저장되고, 기본 리소스는 <code>http://localhost/foo/Bar.properties</code>에 저장된다.
 * </p>
 *
 */
public class PropertyResourcesFactory extends ResourcesFactoryBase {

    /**
     * <p>
     * 명시된 논리명을 기반으로 새로운 {@link s2.adapi.framework.resources.Resources} 인스턴스를
     * 생성하여 리턴한다. <code>init()</code> 메소드가 호출되고 나서 관련 프로퍼티를 위임한다.
     * </p>
     *
     * @param name   생성될 {@link s2.adapi.framework.resources.Resources}인스턴스의 논리명
     * @param base 해당 리소스에 대한 Configuration 스트링
     * @throws ResourcesException 명시된 논리명의 {@link s2.adapi.framework.resources.Resources}
     *                            인스턴스가 생성될 수 없는 경우
     */
    protected Resources createResources(String name, String base)
            throws ResourcesException {

        Resources res = new PropertyResources(name, base);
        res.setReturnNull(isReturnNull());
        res.init();
        return (res);

    }


}


