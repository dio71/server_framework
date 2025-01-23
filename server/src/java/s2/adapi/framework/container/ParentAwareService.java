package s2.adapi.framework.container;

/**
 * 객체가 Injection되어 다른 객체에서 참조될때 자신을 참조하는 객체를 알고자 할 경우 
 * 이 인터페이스를 구현한다.
 * EnlistedServiceContainer는 객체를 생성하는 도중 이 인터페이스를 구현한 객체라면
 * 그 객체를 참조하는 객체를 부모 객체로서 setParent() 메소드로 전달해준다.
 * 이때 전달되는 부모객체는 Interceptor나 Proxy가 아닌 원 객체를 전달해준다.
 * 
 * @author kimhd
 * @since 5.0
 */
public interface ParentAwareService {

	/**
	 * 자신을 참조하는 객체를 부모객체로 전달한다.
	 * @param parent
	 */
	public void setParent(Object parent);
	
}
