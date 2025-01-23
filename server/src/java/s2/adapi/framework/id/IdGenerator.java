package s2.adapi.framework.id;

/**
 * ID 생성 서비스의 인터페이스 이다.
 * @author 김형도
 * @since 4.0
 */
public interface IdGenerator {

	/**
	 * 아이디를 생성하는 메소드이다.
	 * @param key 구현에 따라서 추가적인 정보가 필요할 경우 사용한다.
	 * @return 생성된 ID 정보를 담고 있는 객체이다.
	 */
	Object getNextId(Object key);
}
