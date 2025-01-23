package s2.adapi.framework.resources.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import s2.adapi.framework.resources.Resources;
import s2.adapi.framework.resources.ResourcesException;
import s2.adapi.framework.resources.ResourcesFactory;

/**
 * <p>
 * {@link s2.adapi.framework.resources.ResourcesFactory}를 편리하게 구현할 수 있도록
 * 제공되는 베이스 클래스이다.
 * 이 클래스를 기반으로 구현된 클래스는 <code>protected</code> 메소드인 <code>createResources()</code>에 의해
 * 리턴된 {@link s2.adapi.framework.resources.Resources} 인스턴스를 캐슁한다.
 * <code>createResources()</code> 메소드는 서브클래스에 의해 구현되어야 한다.
 * </p>
 */
public abstract class ResourcesFactoryBase implements ResourcesFactory {

    /**
     * <p>
     * 해당 {@link ResourcesFactory}에 의해 생성된 {@link Resources} 인스턴스의 집합.
     * 이름을 key로하여 식별된다.
     * </p>
     */
    protected Map<String,Resources> resources = new HashMap<String,Resources>();


    /**
     * <p>
     * 팩토리에 의해 생성된 {@link Resources} 인스턴스에 설정될
     * <code>returnNull</code> 프로퍼티의 값
     * </p>
     */
    protected boolean returnNull = true;


    // ------------------------------------------------------------- Properties


    /**
     * <p>
     * 팩토리에 의해 생성된 {@link Resources} 인스턴스에 설정될
     * <code>returnNull</code> 프로퍼티의 값을 리턴한다.
     * </p>
     */
    public boolean isReturnNull() {

        return (this.returnNull);

    }


    /**
     * <p>
     * 팩토리에 의해 생성된 {@link Resources} 인스턴스에 설정될
     * <code>returnNull</code> 프로퍼티의 값을 설정한다.
     * </p>
     *
     * @param returnNull 설정될 새로운 값
     */
    public void setReturnNull(boolean returnNull) {

        this.returnNull = returnNull;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * <p>
     * 명시된 이름과 디폴트 configuration 정보에 준하여 {@link s2.adapi.framework.resources.Resources} 인스턴스를
     * 생성하여 리턴한다.
     * 기본적으로 이 메소드는 전달된 리소스 인스턴스의 이름을 configuration 문자열로 취급하여
     * 내부적으로 <code>getResources(String,String)</code> 메소드를 호출한다.
     * </p>
     *
     * @param name 리턴되어야 하는 {@link Resources} 인스턴스의 이름(logical name)
     * @throws ResourcesException 명시된 이름의 {@link Resources} 인스턴스가 리턴될 수 없는 경우
     */
    public Resources getResources(String name)
            throws ResourcesException {

        return (getResources(name, null));

    }


    /**
     * <p>
     * 명시된 논리명과 configuration 스트링 기반의 configuration 정보를 가지고
     * {@link s2.adapi.framework.resources.Resources} 인스턴스를 (필요한경우)생성하고 리턴한다.
     * </p>
     *
     * @param name   리턴되어야 하는 {@link Resources} 인스턴스의 논리명
     * @param base 해당 리소스 구현에 대한 Configuration 스트링. 디폴트 설정을 사용하는 경우 <code>null</code>
     * @throws ResourcesException 명시된 논리명을 갖는 {@link Resources}인스턴스가 리턴될 수 없는 경우
     */
    public Resources getResources(String name, String base)
            throws ResourcesException {

        synchronized (resources) {
            Resources instance = resources.get(name);
            if (instance == null) {
                instance = createResources(name, base);
                resources.put(name, instance);
            }
            return (instance);
        }

    }


    /**
     * <p>
     * 이전에 리턴되었던 {@link s2.adapi.framework.resources.Resources}에 대한 내부 레퍼런스를 해제한다.
     * 이때 각 인스턴스들에 대해 <code>destroy()</code> 메소드가 호출된다.
     * </p>
     *
     * @throws ResourcesException 해제 도중에 에러가 발생한 경우
     */
    public void release() throws ResourcesException {

        synchronized (resources) {
            Iterator<String> names = resources.keySet().iterator();
            while (names.hasNext()) {
                String name = names.next();
                resources.get(name).destroy();
            }
            resources.clear();
        }

    }

    public void release(String name) throws ResourcesException {
    	synchronized (resources) {
    		Resources res = resources.get(name);
    		if (res != null) {
    			res.destroy();
    			resources.remove(name);
    		}
    	}
    }

    // ------------------------------------------------------ Protected Methods


    /**
     * <p>
     * 명시된 논리명을 기반으로 새로운 {@link s2.adapi.framework.resources.Resources} 인스턴스를
     * 생성하여 리턴한다.
     * 내부적으로 <code>init()</code> 메소드의 수행 후에 관련 프로퍼티를 위임한다.
     * 실제로 사용될 서브클래스는 이 메소드를 <strong>반드시</strong> 구현해야 한다.
     * </p>
     *
     * @param name   생성할 {@link Resources} 인스턴스에 대한 논리명
     * @throws ResourcesException 명시된 논리명의 {@link Resources} 인스턴스가 생성될 수 없는 경우
     */
    protected abstract Resources createResources(String name, String base)
            throws ResourcesException;


}


