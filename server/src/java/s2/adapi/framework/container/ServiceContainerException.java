package s2.adapi.framework.container;

public class ServiceContainerException extends RuntimeException {
	
	/**
	 * <p>
	 * serialVersionUID를 지정하지 않으면 런타임에 정해진 알고리즘에 따라서 값을 생성된다.
	 * 그러나 이렇게 JVM이 생성하는 값은 클래서의 세부사항을 민감하게 반영하기 때문에 컴파일러에 따라서
	 * 달라질 수 있어 deserialization 과정에서 예기치 않게 InvalidClassException이 발생할 수 있다.
	 * 그러므로 서로 다른 자바 컴파일러 사이에서도 동일한 serialVersionUID 값을 보장하기 위해서는
	 * 명시적으로 serialVersionUID 값을 선언해야 한다.
	 * </p>
	 * <p>
	 * JVM이 생성하는 serialVersionUID 값은 "serialver -classpath ... 클래스명" 명령으로 알수 있다.
	 * </p>
	 * <p>
	 * serialVersionUID 값을 지정해놓은 경우에 추후 클래스의 구현이 바뀌어 
	 * 이전 버전과 호환(serialization/deserialization)이 되지 않아야 한다면
	 * 지정된 serialVersionUID 값을 변경하여야 한다.
	 * </p>
	 */
	private static final long serialVersionUID = -2176226771073271243L;
	
    /**
     * <p>
     * 아무런 인자도 갖지 않는 디폴트 <code>ServiceContainerException</code> 컨스트럭터
     * </p>
     */
    public ServiceContainerException() {
        super();
    }
    
    /**
     * <p>
     * <code>Throwable</code> 타입의 <code>cause</code> 인자를 갖는 <code>ServiceContainerException</code> 컨스트럭터
     * </p>
     *
     * @param cause 에러나 이벤트의 원인이 되는 <code>Throwable</code> 오브젝트
     */
    public ServiceContainerException(Throwable cause) {
        super(cause);
    }
    
    /**
     * <P>
     * <code>String</code> 타입의 <code>msg</code> 인자와 <code>Throwable</code> 타입의 <code>cause</code> 인자를
     * 갖는 <code>ServiceContainerException</code> 컨스트럭터
     * </p>
     *
     * @param msg   발생된  에러나 이벤트를 나타내는 메시지 스트링
     * @param cause 에러나 이벤트의 원인이 되는  <code>Throwable</code> 오브젝트
     */
	public ServiceContainerException(String msg, Throwable cause) {
		super(msg,cause);
	}
	
    /**
     * <p>
     * <code>String</code> 타입의 <code>msg</code> 인자를 갖는 <code>ServiceContainerException</code> 컨스트럭터
     * </p>
     *
     * @param msg 발생된  에러나 이벤트를 나타내는 메시지 스트링
     */
	public ServiceContainerException(String msg) {
		super(msg);
	}
}
