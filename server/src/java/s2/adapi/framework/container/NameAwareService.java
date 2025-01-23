package s2.adapi.framework.container;

/**
 * 서비스 명을 전달받기 위하여 구현해야하는 Interface 이다.
 * @author kimhd
 *
 */
public interface NameAwareService {

	public void setServiceName(String name);
}
