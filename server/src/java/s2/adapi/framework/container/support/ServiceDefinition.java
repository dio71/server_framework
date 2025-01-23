package s2.adapi.framework.container.support;

import java.util.ArrayList;
import java.util.List;

/**
 * 서비스의 정의 내용을 가진다. 서비스 설정파일로부터 서비스 설정 내용을 가져와서
 * 이를 ServiceRegistry에 등록하기 위하여 사용된다.
 * @author 김형도
 * @since 4.0
 */
public class ServiceDefinition {

	/**
	 * 서비스가 정의가 파일의 경로등의 정보를 가지고 있는다.
	 */
	private String fromInfo = null;
	/**
	 * 서비스 객체의 이름을 지정한다.
	 */
	private String name = null;
	
	/**
	 * 서비스 객체가 singleton 객체인지 여부를 지정한다.
	 */
	private boolean singleton = true;
	
	/**
	 * 서비스 객체의 Class 명이다.
	 */
	private String svcClass = null;
	
	/**
	 * 서비스 객체의 Interface 명이다.
	 */
	private String svcInf = null;
	
	/**
	 * 서비스 객체가 사용할 Interceptor 서비스 명을 지정한다.
	 */
	private String interceptorRef = null;
	
	/**
	 * 서비스 객체 초기화 메소드가 있을 경우 그 메소드 명을 지정한다.
	 */
	private String initMethod = null;
	
	/**
	 * 서비스 객체의 소멸 메소드가 있을 경우 그 메소드 명을 지정한다.
	 */
	private String destroyMethod = null;
	
	/**
	 * 서비스 객체를 컨테이너 부팅시 바로 생성할 지 여부를 지정한다.
	 */
	private boolean preInit = true;
	
	/**
	 * 서비스 객체를 활성화 할지 여부를 지정한다.
	 */
	private boolean activate = true;
	
	/**
	 * 동일한 서비스 명이 정의 되었을 경우 어느 것을 적용할지 결정하기 위한 우선순위를 지정한다.
	 */
	private int loadPriority = 0;
	
	/**
	 * 서비스 객체 생성후 setter injection 시 사용할 프로퍼티 리스트를 저장한다.
	 * 프로퍼티는 ProperyHolder 객체에 담겨서 저장된다.
	 */
	private List<PropertyHolder> properties = new ArrayList<PropertyHolder>();

	/**
	 * 서비스 객체 생성시 사용할 생성자 또는 Factory의 인수에 대한 정의를 담아 놓는다.
	 * Argument는 ArgumentHolder 객체에 담겨서 저장된다.
	 */
	private List<ArgumentHolder> arguments = new ArrayList<ArgumentHolder>();
	
	private boolean constructorSet = false;
	
	/**
	 * 서비스 객체 생성시 Static Factory를 사용할 경우에 Factory의 클래스 명을 지정한다.
	 */
	private String factoryClass = null;
	
	/**
	 * 서비스 객체 생성시 Object Factory를 사용할 경우에 Factory Object의 서비스 명을 지정한다.
	 */
	private String factoryRef = null;
	
	/**
	 * 서비스 객체 생성시 Factory를 사용할 경우에 Factory 클래스의 객체 생성 메소드 명을 지정한다.
	 */
	private String factoryMethod = null;
	
	/**
	 * 생성자.
	 * @param sname 서비스 명
	 * @param infName 인터페이스 명
	 * @param className 클래스 명
	 */
	public ServiceDefinition(String sname, String infName, String className) {
		this(sname, infName, className, null, null, null, true, true, true, 0);
	}
	
	/**
	 * 생성자.
	 * @param sname 서비스 명
	 * @param infName 인퍼페이스 명
	 * @param className 클래스 명
	 * @param isSingleton singleton 여부. true이면 singleton 객체로 생성
	 */
	public ServiceDefinition(String sname, String infName, String className, boolean isSingleton) {
		this(sname, infName, className, null, null, null, isSingleton, true, true, 0);
	}
	
	/**
	 * 생성자.
	 * @param sname 서비스 명
	 * @param infName 인퍼페이스 명
	 * @param className 클래스 명
	 * @param interceptor Proxy 서비스 객체명
	 * @param init 초기화 메소드 명
	 * @param destroy 소멸 메소드 명
	 * @param isSingleton singleton 여부. true이면 singleton 객체로 생성
	 */
	public ServiceDefinition(String sname, String infName, String className, String interceptor, 
			                 String init, String destroy, boolean isSingleton, boolean activated, 
			                 boolean pre, int priority) 
	{
		name = sname;
		svcInf = infName;
		svcClass = className;
		interceptorRef = interceptor;
		initMethod = init;
		destroyMethod = destroy;
		singleton = isSingleton;
		activate = activated;
		preInit = pre;
		loadPriority = priority;
	}
	
	public void setFromInfo(String from) {
		fromInfo = from;
	}
	
	public String getFromInfo() {
		return fromInfo;
	}
	
	/**
	 * 서비스의 초기화 메소드 명을 리턴한다.
	 * @return
	 */
	public String getInitMethod() {
		return initMethod;
	}
	
	/**
	 * 서비스의 소멸 메소드 명을 리턴한다.
	 * @return
	 */
	public String getDestroyMethod() {
		return destroyMethod;
	}
	
	/**
	 * Constructor 가 설정되어 있는지 여부를 리턴한다.
	 * @return
	 */
	public boolean hasConstructor() {
		return constructorSet;
	}
	
	/**
	 * Factory가 설정되어 있는지 여부를 리턴한다.
	 * @return
	 */
	public boolean hasFactory() {
		return (factoryMethod != null );
	}
	
	/**
	 * 설정된 Factory의 클래스 명을 리턴한다.
	 * @return
	 */
	public String getFactoryClass() {
		return factoryClass;
	}
	
	/**
	 * 설정된 Factory 클래스의 메소드 명을 리턴한다.
	 * @return
	 */
	public String getFactoryMethod() {
		return factoryMethod;
	}
	
	/**
	 * 설정된 Factory Object의 서비스명을 리턴한다.
	 * @return
	 */
	public String getFactoryRef() {
		return factoryRef;
	}
	
	/**
	 * Factory의 클래스와 메소드 명을 설정한다.
	 * @param className
	 * @param methodName
	 */
	public void setFactory(String className, String refName, String methodName) {
		factoryClass = className;
		factoryRef = refName;
		factoryMethod = methodName;
	}
	
	/**
	 * 서비스 명을 리턴한다.
	 * @return
	 */
	public String getServiceName() {
		return name;
	}
	
	/**
	 * 서비스의 구현 클래스 명을 리턴한다.
	 * @return
	 */
	public String getServiceClass() {
		return svcClass;
	}
	
	/**
	 * 서비스의 인터페이스 명을 리턴한다.
	 * @return
	 */
	public String getServiceInterface() {
		return svcInf;
	}
	
	/**
	 * Interceptor Proxy로 wrapping된 서비스의 경우에는 Interceptor 서비스 명을 리턴한다.
	 * @return
	 */
	public String getServiceInterceptor() {
		return interceptorRef;
	}
	
	/**
	 * 서비스 활성화 여부를 리턴한다.
	 * @return
	 */
	public boolean isActivated() {
		return activate;
	}
	
	/**
	 * 사전에 미리 생성할 지 여부를 리턴한다.
	 * @return
	 */
	public boolean isPreInit() {
		return preInit;
	}
	
	/**
	 * 적용 우선 순위 값을 리턴한다.
	 * @return
	 */
	public int getLoadPriority() {
		return loadPriority;
	}
	
	/**
	 * Setter Injection 을 위한 Property 목록을 리턴한다.
	 * @return
	 */
	public List<PropertyHolder> getProperties() {
		return properties;
	}
	
	/**
	 * Constructor Injection 또는 Factory Instantiation을 위한 인수 목록을 리턴한다.
	 * @return
	 */
	public List<ArgumentHolder> getArguments() {
		return arguments;
	}
	
	/**
	 * idx 번째의 Property 항목을 리턴한다.
	 * @param idx
	 * @return
	 */
	public PropertyHolder getProperty(int idx) {
		return properties.get(idx);
	}
	
	/**
	 * Property 목록 내의 개수를 리턴한다.
	 * @return
	 */
	public int getPropertyCount() {
		return properties.size();
	}
	
	/**
	 * 서비스가 singleton 인지 여부를 리턴한다.
	 * @return
	 */
	public boolean isSingleton() {
		return singleton;
	}
	
	/**
	 * Property 목록에 하나의 property를 추가한다.
	 * @param ph 추가할 property
	 */
	public void addProperty(PropertyHolder ph) {
		if ( ph != null ) {
			properties.add(ph);
		}
	}
	
	/**
	 * 생성자 또는 Factory의 Argument 목록에 argument를 추가한다.
	 * @param ah 추가할 argument
	 */
	public void addArgument(ArgumentHolder ah) {
		if ( ah != null ) {
			arguments.add(ah);
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[name=").append(name);
		sb.append(",inf=").append(svcInf);
		sb.append(",impl=").append(svcClass);
		sb.append(",singleton=").append(singleton);
		sb.append("]");
		
		return sb.toString();
	}
}
