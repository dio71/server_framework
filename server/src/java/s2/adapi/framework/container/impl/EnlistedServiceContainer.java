package s2.adapi.framework.container.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.aop.InterceptorProxy;
import s2.adapi.framework.aop.TargetProxy;
import s2.adapi.framework.aop.auto.AutoProxy;
import s2.adapi.framework.container.NameAwareService;
import s2.adapi.framework.container.ParentAwareService;
import s2.adapi.framework.container.ServiceContainer;
import s2.adapi.framework.container.ServiceContainerException;
import s2.adapi.framework.container.ServiceInfo;
import s2.adapi.framework.container.ServicePostProcessor;
import s2.adapi.framework.container.support.ArgumentHolder;
import s2.adapi.framework.container.support.PropertyHolder;
import s2.adapi.framework.container.support.ServiceDefinition;
import s2.adapi.framework.container.support.ServiceObject;
import s2.adapi.framework.container.support.ServiceRegistry;
import s2.adapi.framework.util.ObjectHelper;
import s2.adapi.framework.util.StringHelper;
import s2.adapi.framework.util.SystemHelper;
import s2.adapi.framework.util.UrlPatternMatcher;
/**
 * 서비스 객체를 생성/관리/제공하는 핵심 기능을 제공하는 ServiceContainer의 구현 클래스이다.
 * 서비스가 정의된 서비스 레지스트리는 외부에서 생성하여 생성자에게 전달해야한다.
 * @author 김형도
 * @since 4.0
 */
public class EnlistedServiceContainer implements ServiceContainer {

    private static final Logger log = LoggerFactory.getLogger(EnlistedServiceContainer.class);
    
    /**
     * <p>
     * 서비스 정의 목록인 서비스 레지스트리이다.
     * </p>
     */
	protected ServiceRegistry svcRegistry = null;
	
	/**
	 * <p>
	 * Singleton으로 설정된 서비스 객체를 저장하기 위한 저장소
	 * </p>
	 */
	private Map<String,ServiceObject> singletonCache = new HashMap<String,ServiceObject>();
	
	/**
	 * 적용할 ServicePostProcessor 들이다.
	 */
	private List<ServicePostProcessor> postProcessors = new ArrayList<ServicePostProcessor>();
	
	/**
	 * 적용할 AutoProxy 들이다.
	 */
	private List<AutoProxy> proxyProcessors = new ArrayList<AutoProxy>();
	
	private String[] patternNames = null;
	
	private boolean isPopulated = false;
	
	private ClassLoader classLoader = null; // 서비스 객체 생성시 사용할 클래스 로더 객체
	
	/**
	 * 서비스 정의 목록인 서비스 레지스트리 객체를 입력으로 받는 생성자이다.
	 * 서비스 객체를 생성하기 위하여 사용할 ClassLoader 객체를 지정할 수 있다.
	 * ClassLoader를 null로 지정하면 Thread의 ContextClassLoader를 사용한다.
	 * @param svcRegistry
	 * @param loader
	 */
	public EnlistedServiceContainer(ServiceRegistry svcRegistry, ClassLoader loader) {
		if (loader == null) {
			classLoader = SystemHelper.getClassLoader();
		} 
		else {
			classLoader = loader;
		}
		
		if ( svcRegistry == null ) {
			this.svcRegistry = new ServiceRegistry();
		} 
		else {
			this.svcRegistry = svcRegistry;
		}
		
		addAutoProxies();
		
		// 패턴 형태의 서비스 명칭은 따로 모아둔다.
		List<String> svcNames = new ArrayList<String>();
        for(String name:svcRegistry.getAllServiceNames()) {
            if (UrlPatternMatcher.isPattern(name)) {
            	svcNames.add(name);
            }
        }
		
		patternNames = svcNames.toArray(new String[svcNames.size()]);
	}
	
	public EnlistedServiceContainer(ServiceRegistry svcRegistry) {
		this(svcRegistry,null);
	}
	
	public ClassLoader getClassLoader() {
		return classLoader;
	}
	
	/**
	 * 주어진 서비스 명이 등록되어 있는지 여부를 반환한다.
	 * @param svcName
	 * @return
	 */
	public boolean containsService(String svcName) {
		if (svcRegistry == null) {
			return false;
		}
		
		return svcRegistry.containsServiceDefinition(svcName);
	}
	
	/**
	 * 등록되어 있는 모든 서비스 명을 배열로 반환한다. 
	 */
	public String[] getAllServiceNames() {
		Set<String> svcNameSet = svcRegistry.getAllServiceNames();
		
		return svcNameSet.toArray(new String[svcNameSet.size()]);
	}
	
	/**
	 * 등록되어 있는 서미스 명 중에서 패턴형태의 이름을 반환한다.
	 * @return
	 */
	public String[] getPatternServiceNames() {
		return patternNames;
	}
	
	public Object getService(String svcName) throws ServiceContainerException 
	{
		return getServiceObject(svcName,false,null).getService();
	}
	
	public void addPostProcessor(ServicePostProcessor postProcessor) {
		if (postProcessor != null) {
			postProcessors.add(postProcessor);
		}
	}
	
	public void close() {
		doClose();
	}
	
	public ServiceInfo[] getServiceInfo() {
		
		String[] svcNames = getAllServiceNames();
		List<ServiceInfo> svcList = new ArrayList<ServiceInfo>();
		for(int i=0;i<svcNames.length;i++) {
			ServiceDefinition sdf = svcRegistry.getServiceDefinition(svcNames[i]);
			
			String className = sdf.getServiceClass();
			String infName = sdf.getServiceInterface();
			boolean singleton = sdf.isSingleton();
			boolean instantiated = singletonCache.containsKey(svcNames[i]);
			ServiceInfo svcInfo = new ServiceInfo(svcNames[i],className,infName,singleton,instantiated,sdf.getFromInfo());
			// 참조하는 reference 서비스들을 추가
			List<PropertyHolder> props = sdf.getProperties();
			for(int k=0;k<props.size();k++) {
				if (props.get(k).hasReference()) {
					svcInfo.addReference(props.get(k).getValue());
				}
			}
			
			svcList.add(svcInfo);
		}
		
		return svcList.toArray(new ServiceInfo[svcList.size()]);
	}
	
	public ServiceInfo getServiceInfo(String svcName) {
		ServiceDefinition sdf = svcRegistry.getServiceDefinition(svcName);
		ServiceInfo svcInfo = null;
		if (sdf != null) {
			String className = sdf.getServiceClass();
			String infName = sdf.getServiceInterface();
			boolean singleton = sdf.isSingleton();
			boolean instantiated = singletonCache.containsKey(svcName);
			svcInfo = new ServiceInfo(svcName,className,infName,singleton,instantiated,sdf.getFromInfo());
			// 참조하는 reference 서비스들을 추가
			List<PropertyHolder> props = sdf.getProperties();
			for(int k=0;k<props.size();k++) {
				if (props.get(k).hasReference()) {
					svcInfo.addReference(props.get(k).getValue());
				}
			}
		}
		return svcInfo;
	}
	
	//
	// 구현용 내부  메소드 들
	//
	
	/**
	 * 
	 * @param svcName
	 * @param returnAny true 이면 모든 상태의 서비스 객체를 리턴하고, false 이면 완전히 초기화된 서비스 객체만 리턴한다.
	 * @param runningCache 이전에 생성된 서비스 객체가 담겨져 있는 캐시.
	 * @return
	 * @throws ServiceContainerException
	 */
	private ServiceObject getServiceObject(String svcName, boolean returnAny, 
			                               Map<String,ServiceObject> runningCache) 
	        throws ServiceContainerException {

		long stime = 0;
		
		if ( log.isDebugEnabled()) {
			stime = System.currentTimeMillis();
			log.debug("getServiceObject(" + svcName + ") starts.");
		}
		
		ServiceDefinition svcDef = getServiceDefinition(svcName);
		
		// 서비스가 activated 상태가 아니라면 exception을 던진다.
		if ( !svcDef.isActivated() ) {
			throw new ServiceContainerException("service is not activated. [" + svcName + "]");
		}
		
		// 이전에 생성되었던 서비스 객체를 확인한다.
		ServiceObject svcObject = null;
		// 우선 runngingCache 에 있는지 확인한다.
		if ( runningCache != null ) {
			svcObject = runningCache.get(svcName);
		}
		
		// Multi-threaded 환경에서 동시에 같은 서비스 요청이 들어 왔을 경우 singleton이어도 2이상의 객체가 생성될 수 있으며
		// 결국에는 마지막에 singletonCache에 저장되는 객체이외는 버려지는 상황이 된다. 
		// 또한 이러한 경우에는 서비스 객체를 요청한 client들은 singleton이라고 생각하지만 실제는 그 순간에 여러번 생성된 서로 다른 
		// 서비스 객체를 가지고 있게되므로 singletone이라고 할 수 없다.
		// 그러므로 singleton으로 서비스 객체가 생성되는 것을 보장하기 위해서는
		// 서비스 정의 객체(svcDef)를 Mutex로 하여 critical section으로 처리해야한다.
		
		if ( svcObject == null ) {
			// runningCache에 없는 경우
			if ( svcDef.isSingleton() ) {
				// runningCache에 없고, singleton인 경우
				synchronized(singletonCache) {
					// singletonCache에 있는 지 확인한다.
					svcObject = singletonCache.get(svcName);
					if ( svcObject == null ) {
						// singletonCache에 없으므로 새로 생성한다.
						svcObject = instantiateServiceObject(svcDef, runningCache, false);
						// 생성된 서비스 객체를 singletonCache에 넣는다.
						singletonCache.put(svcName, svcObject);
					}
				}
			} 
			else {
				// runningCache에 없고 singleton이 아닌 경우, 바로 서비스 객체를 생성
				svcObject = instantiateServiceObject(svcDef, runningCache, false);
			}
		} 
		else {
			// runningCache에 있는 경우

			// 생성 경로 상에 앞서 생성중인 서비스인 경우에도 runningCache에 들어 있게 되므로 
			// 이때에는 서비스 객체의 생성 상태를 확인해야 한다.
			if ( !returnAny && !svcObject.isInitialized()) {
				// 서비스가 초기화되지 않았고, returnAny 요청이 아닌 경우에는 에러발생
				throw new ServiceContainerException(svcName + " has not initialized yet.");
			}

		}
		
		if ( log.isDebugEnabled()) {
			log.debug("getServiceObject(" + svcName + ") ends. (" + (System.currentTimeMillis() - stime) + " msec).");
		}

		return svcObject;
	}
	
	/**
	 * <p>
	 * 새로운 서비스 객체를 생성한다.
	 * 서비스캐시에 이미 존재하는지 여부와 상관없이 항상 새로운 서비스 객체를 생성한다.
	 * 서비스 객체를 생성하기만 할 뿐,서비스 객체를 singletonCache에 넣는 것은 아니다.
	 * </p>
	 * <p>
	 * asProxy를 true로 지정하면 서비스 객체를 생성할 때 생성되는 서비스 객체를 runningCache에 담지 않는다.
	 * 이것은 Proxy의 특성상 매번 새로 객체를 생성해야하기 때문이다.
	 * </p>
	 * @param svcDef 생성할 서비스의 정의가 담긴 서비스 정의 객체
	 * @param runningCache singleton 타입이 아닌 서비스 객체를 생성할 때에는 여기에 임시로 생성된 객체를 담아 놓는다.
	 * @param asInterceptor Interceptor Proxy용 서비스 객체 여부
	 * @return 생성된 서비스 객체
	 * @throws ServiceContainerException
	 */
	private ServiceObject instantiateServiceObject(ServiceDefinition svcDef, 
			           Map<String, ServiceObject> runningCache, boolean asInterceptor) 
		throws ServiceContainerException {
		
		long stime = 0;
		
		if ( log.isDebugEnabled()) {
			stime = System.currentTimeMillis();
			log.debug("instantiateServiceObject(" + svcDef.getServiceName() + ") starts.");
		}
				
		if ( svcDef == null ) {
			throw new ServiceContainerException("invalid service registry.");
		}
		
		String svcName = svcDef.getServiceName();
		String infName = svcDef.getServiceInterface();
		String interceptorRef = svcDef.getServiceInterceptor();
		
		Class<?> inf = null;
		// interface가 지정되었을 경우 그 interface가 실제 interface 클래스인지 확인한다.
		if (!StringHelper.isNull(infName)) {
			try {
				inf = loadClass(infName);
			} 
			catch (ClassNotFoundException e) {
				throw new ServiceContainerException(infName + " class is not found.", e);
			}
			
			if ( !inf.isInterface() ) {
				throw new ServiceContainerException(infName + " is not an interface.");
			}
		}
		
		// interceptor가 지정되었다면 반드시 interface도 지정되어야 한다.
		if (inf == null && !StringHelper.isNull(interceptorRef)) {
			throw new ServiceContainerException("cannot set interceptor proxy without an interface. [" + svcName + "]");
		}
		
		Object orgSvc = null; // 원본 Object
		Object wrappedSvc = null; // Interceptor Proxy를 거친 Object
		
		// 1. 서비스 객체 생성 단계
		
		// 임시 캐시가 지정되지 않았다면 임시캐시를 생성한다.
		if ( runningCache == null ) {
			runningCache = new HashMap<String,ServiceObject>();
		}
		
		// interceptor 용이 아니라면 생성하기 전에 runngingCache에 SERVICE_NOT_CONSTRUCTED 상태로 ServiceObject를 만들어 넣는다.
		if ( !asInterceptor ) {
			runningCache.put(svcName, new ServiceObject(svcName));
		}
		
		// 서비스의 객체를 생성한다.
		orgSvc = constructObject(svcDef, inf, runningCache);
		
		// postProcess를 여기에서 실행한다. (2010.01.07 김형도)
		// postProcess 결과로 원본 객체와 다른 객체가 생성되어 반환될 수 있다.
		orgSvc = postProcess(orgSvc, svcName);
		
		//boolean applyInterceptor = false;
		
		// 2. 명시된 Interceptor Proxy 생성 단계
		if ( StringHelper.isNull(interceptorRef) || asInterceptor ) {
			wrappedSvc = orgSvc;
			//applyInterceptor = false;
		} 
		else {
			// Interceptor Proxy가 지정되었으므로 Proxy 서비스 객체로 원래의 서비스 객체를 감싼다.
			try {
				// Proxy handler 서비스 객체를  생성한다.
				ServiceDefinition proxySvcDef = getServiceDefinition(interceptorRef);
				if (proxySvcDef.isActivated()) {
					if ( log.isDebugEnabled()) {
						log.debug("### apply interceptor [" + interceptorRef + "] to " + svcName);
					}
					
					// Interceptor 객체 생성
					InterceptorProxy proxyHandler = (InterceptorProxy) 
							instantiateServiceObject(proxySvcDef, runningCache, true).getService();
					
					//log.debug("setTarget() with "+svcName + ","+orgSvc);
					// 여기에서 proxy handler의 target 서비스 객체  설정
					proxyHandler.setTarget(svcName, orgSvc);
					
					// Proxy 객체를 생성
					//wrappedSvc = Proxy.newProxyInstance(this.getClass().getClassLoader(),
					//wrappedSvc = Proxy.newProxyInstance(inf.getClassLoader(),
					//		                              new Class[]{inf},
					//		                              proxyHandler);
					wrappedSvc = proxyHandler.newProxyInstance(new Class[]{inf}, classLoader);
					
					//applyInterceptor = true;
				} 
				else {
					// interceptorRef의 서비스가 deactivate되었다면 Interceptor Proxy 객체를 생성하지 않는다.
					wrappedSvc = orgSvc;
					//applyInterceptor = false;
				}
			} 
			catch (IllegalArgumentException e) {
				throw new ServiceContainerException(
						"cannot instantiate " + interceptorRef + " interceptor. [" + svcName + "]",e);
			} 
			catch (ClassCastException e) {
				throw new ServiceContainerException(
						"interceptor must implement InterceptorHandler. [" + interceptorRef + "]",e);
			}
		}
		
		// interface가 지정된 경우 해당 서비스의 class 객체가 지정된 inteface를 implements 했는지 확인한다.
		// 그러나 interceptor가 지정되었을 경우에는 서비스의 class 객체가 지정된 interface를 implements 했는지 확인하지 않는다.
		// 예를 들어 A 서비스를 웹서비스 했을 경우 A 서비스를 호출하는 클라이언트는 A interface를 통해 호출하지만
		// interceptor는 그 호출을 받아 SOAP으로 호출하는 WebServiceInvoker에게 전달하게 된다.
		// WebServiceInvoker는 A Interface를 implement한 것은 아니므로 interceptor가 지정되었을 경우
		// interface의 implement여부는 꼭 확인 할 필요는 없다.
		// 2008.03.06 김형도. 인터페이스 구현여부 확인하지 않도록 아래 코딩 삭제
		/*
		Class<?> svcClass = null;
		if ( !applyInterceptor && inf != null ) {
			svcClass = orgSvc.getClass();
			if ( !inf.isAssignableFrom(svcClass) ) {
				throw new ServiceContainerException(svcClass.getName() + " does not implement " + infName + ".");
			}
		} */

		//
		// 3. AutoProxy 적용 부분
		// wrappedSvc 객체에 autoproxy 적용하여 다시 wrappedSvc로 만들자.
		//
		if (inf != null && !asInterceptor) {
			wrappedSvc = proxyProcess(wrappedSvc, svcDef);
		}
		
		// 여기까지 오면 우선 서비스는 객체가 생성된 상태이다. 
		// 서비스 객체의 상호 참조가 가능하다록 생성된 서비스를 서비스 객체로 감싼 후, 생성된 상태로 임시 캐시에 넣는다.
		// 다만 Interceptor 용 서비스 객체는 임시 캐시에 넣지 않는다.
		ServiceObject svcObject = new ServiceObject(svcName, orgSvc, wrappedSvc);
		if ( !asInterceptor ) {
			runningCache.put(svcName, svcObject);
		}
		
		// 2008.03.06 김형도
		// orgSvc가 Proxy 객체라면 TargetProxy 클래스로 지정된 서비스 객체이므로 
		// 이 경우에는 Proxy 객체가 아닌 그 내부의 TargetProxy 객체를 꺼내와서 
		// postProcess와 setter injection, init-method 가 호출되어야 한다.
		//Object innerObj = getInnerObject(orgSvc);
		Object innerObj = svcObject.getInnerObject();
		
		// postProcess 실행 시점을 위로 올림. 2010.01.07 김형도
		// 3. postProcessor 를 실행한다.
		//innerObj = postProcess(innerObj, svcName);

		// 4. 서비스의 Setter Injection 수행
		injectBySetterMethod(innerObj, svcDef, runningCache);
		//injectBySetterMethod(orgSvc, svcDef, runningCache);
		
		// 5. init-method 를 호출한다.
		//invokeMethod(svcName, innerObj, svcDef.getInitMethod());
		invokeMethod(svcName, orgSvc, svcDef.getInitMethod());
		
		// 6. 서비스 객체 생성이 완료되었다.
		svcObject.setInitialized();
		
		if ( log.isDebugEnabled()) {
			log.debug("instantiateService(" + svcName + ") ends. (" + (System.currentTimeMillis() - stime) + " msec).");
		}
		return svcObject;
	}
	
	/**
	 * 주어진 서비스 클래스의 객체를 생성한다. 객체 생성은 생성자 또는 Factory를 사용한다.
	 * @param svcClass
	 * @param inf
	 * @param svcDef
	 * @param runningCache
	 * @throws ServiceContainerException
	 * @throws IllegalAccessException 조건에 맞는 생성자가 없거나 접근 권한이 없을 경우
	 * @throws InstantiationException 객체 생성에 실패했을 경우
	 * @throws ClassNotFoundException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 */
	private Object constructObject(ServiceDefinition svcDef, Class<?> inf, Map<String,ServiceObject> runningCache) 
			throws ServiceContainerException
	{
		Object returnObj = null;
		boolean createByFactory = false;
		List<ArgumentHolder> args = null;
		// 생성자 또는 Factory의 파라메터로 사용되는 참조 객체 중 ParentAwareService를 구현한 객체들은
		// 아래의 목록에 모아 놓은 후 부모 객체가 생성된 후 setParent() 메소드를 호출해 준다.
		List<ParentAwareService> paSvcs = new ArrayList<ParentAwareService>();
		
		//log.debug("constructObject() starts with "+svcDef.toString()+","+inf.getName());
		
		if ( svcDef != null ) {
			args = svcDef.getArguments();
			createByFactory = svcDef.hasFactory();
		}
		
		Class<?>[] clazz = null;
		Object[] param = null;
		
		//생성 인자의 클래스 목록과 실제 객체 목록을 만든다.
		if ( args != null && args.size() > 0 ) {
			
			clazz = new Class[args.size()];
			param = new Object[args.size()];
			
			for(int i=0;i<args.size();i++) {
				ArgumentHolder ah = args.get(i);
				
				// 클래스 객체 생성
				clazz[i] = ObjectHelper.getPrimitiveClass(ah.getType());
				if ( clazz[i] == null ) {
					try {
						clazz[i] = loadClass(ah.getType());
					} catch (ClassNotFoundException e) {
						throw new ServiceContainerException(
								"argument class(" + ah.getType() + ") not found. [" + svcDef.getServiceName() + "]",e);
					}
				}
				
				// 파라메터 객체 생성
				if ( ah.hasValue() ) {
					Class<?> paramClass = clazz[i];
					if ( paramClass.isPrimitive() ) {
						paramClass = ObjectHelper.getWrapperType(paramClass);
					}
			
					try {
						param[i] = ObjectHelper.instantiate(paramClass, 
								                            new Class[]{String.class},
								                            new Object[]{ah.getValue()});
					}
					catch (InvocationTargetException e) {
						throw new ServiceContainerException(
								"argument class(" + ah.getType() + ") failed to invoke it's constructor. [" +
								svcDef.getServiceName() + "]",e);
					} 
					catch (NoSuchMethodException e) {
						throw new ServiceContainerException(
								"argument class(" + ah.getType() + ") has no matching constructor. [" +
								svcDef.getServiceName() + "]",e);
					} 
					catch (IllegalArgumentException e) {
						throw new ServiceContainerException(
								"argument class(" + ah.getType() + ") invoked constructor with illegal argument. [" +
								svcDef.getServiceName() + "]",e);
					} 
					catch (IllegalAccessException e) {
						throw new ServiceContainerException(
								"argument class(" + ah.getType() + ") cannot access to the constructor. [" +
								svcDef.getServiceName() + "]",e);
					} 
					catch (InstantiationException e) {
						throw new ServiceContainerException(
								"argument class("+ah.getType() + ") failed to instantiate. [" +
								svcDef.getServiceName() + "]",e);
					}
				} 
				else if ( ah.hasReference() ) {
					// 생성자의 파라메터용 서비스 객체를 얻어온다. 상태에 상관없이 가져온다.
					ServiceObject svcObject = getServiceObject(ah.getValue(), true, runningCache);
					// 서비스 객체가 생성전이라면 서비스간에 상호 참조가 존재하는 것이므로 에러를 발생시킨다.
					if ( !svcObject.isConstructed() ) {
						throw new ServiceContainerException(
								"circular dependency is detected at [" + svcDef.getServiceName() + "] <--> [" + ah.getValue() + "]");
					}
					
					// Ref로 참조되는 객체가 ParentAwareService라면 paSvcs 리스트에 담아 놓는다.
					Object innerObj = svcObject.getInnerObject();
					if (innerObj instanceof ParentAwareService) {
						paSvcs.add((ParentAwareService)innerObj);
					}
					
					// 파라메터용 서비스의 객체를 담아둔다.
					param[i] = svcObject.getService();
				} 
				else {
					param[i] = null;
				}
			}
		}
		
		if ( createByFactory ) {
			// Factory를 통하여 생성한다. static factory와 object factory를 구별해야한다.
			String factoryClass = null;
			String factoryMethod = svcDef.getFactoryMethod();
			String factoryRef = svcDef.getFactoryRef();
			Object factoryObject = null;
			Class<?> factory = null;
			Method method = null;
			
			try {
				if ( StringHelper.isNull(factoryRef) ) {
					// static factory 이다.
					factoryObject = null;
					factoryClass = svcDef.getFactoryClass(); 
					factory = loadClass(factoryClass);
				} 
				else {
					// object factory 이다.
					ServiceObject svcObject = getServiceObject(factoryRef, true, runningCache);
					// factory 객체가 생성전이라면 서비스간에 상호 참조가 존재하는 것이므로 에러를 발생시킨다.
					if ( !svcObject.isConstructed() ) {
						throw new ServiceContainerException(
								"circular dependency is detected at [" + svcDef.getServiceName() + "] <--> [" + factoryRef + "]");
					}
					
					factoryObject = svcObject.getService();
					factory = factoryObject.getClass();
					factoryClass = factory.getName();
				}
				
				method = factory.getMethod(factoryMethod, clazz);
				returnObj = method.invoke(factoryObject, param);
			} 
			catch (ClassNotFoundException e) {
				throw new ServiceContainerException(
						"factory class(" + factoryClass + ") not found. [" + svcDef.getServiceName() + "]",e);
			} 
			catch (SecurityException e) {
				throw new ServiceContainerException(
						"factory class(" + factoryClass + ") denied access to the factory method(" + factoryMethod+"()). [" +
						svcDef.getServiceName()+"]",e);
			} 
			catch (NoSuchMethodException e) {
				throw new ServiceContainerException(
						"factory class(" + factoryClass + ") has no matching factory method(" + factoryMethod + "()). [" +
						svcDef.getServiceName() + "]",e);
			} 
			catch (InvocationTargetException e) {
				throw new ServiceContainerException(
						"factory class("+factoryClass + ") failed to invoke it's factory method(" + factoryMethod + "()). ["+
						svcDef.getServiceName() + "]",e);
			} 
			catch (IllegalArgumentException e) {
				throw new ServiceContainerException(
						"factory class("+factoryClass + ") invoked a factory method(" + factoryMethod + "()) with illegal argument. [" +
						svcDef.getServiceName() + "]",e);
			} 
			catch (IllegalAccessException e) {
				throw new ServiceContainerException(
						"factory class(" + factoryClass + ") cannot access to a factory method(" + factoryMethod + "()). [" +
						svcDef.getServiceName() + "]",e);
			}
			
		} 
		else {
			// 생성자를 통하여 생성한다.
			Class<?> svcClass;

			try {
				svcClass = loadClass(svcDef.getServiceClass());
				
				returnObj = ObjectHelper.instantiate(svcClass, clazz, param);
			} 
			catch (ClassNotFoundException e) {
				throw new ServiceContainerException(
						"service class(" + svcDef.getServiceClass() + ") not found. [" + 
						svcDef.getServiceName() + "].",e);
			} 
			catch (InvocationTargetException e) {
				throw new ServiceContainerException(
						"service class(" + svcDef.getServiceClass() + ") failed to invoke it's constructor. [" +
						svcDef.getServiceName() + "]",e);
			} 
			catch (NoSuchMethodException e) {
				throw new ServiceContainerException(
						"service class(" + svcDef.getServiceClass() + ") has no matching constructor. [" +
						svcDef.getServiceName() + "]",e);
			} 
			catch (IllegalArgumentException e) {
				throw new ServiceContainerException(
						"service class(" + svcDef.getServiceClass() + ") invoked the constructor with illegal arguement. [" +
						svcDef.getServiceName() + "]",e);
			} 
			catch (IllegalAccessException e) {
				throw new ServiceContainerException(
						"service class("+svcDef.getServiceClass() + ") cannot access to the constructor. [" +
						svcDef.getServiceName() + "]",e);
			} 
			catch (InstantiationException e) {
				throw new ServiceContainerException(
						"service class("+svcDef.getServiceClass()+") failed to instantiate. [" +
						svcDef.getServiceName() + "] (interface or abstract class or no default constructor)",e);
			}
		}
		
		// 생성된 객체를 paSvcs 리스트에 모아진 객체들의 setParent()로 호출해 준다.
		for(int i = 0; i < paSvcs.size(); i++) {
			ParentAwareService paSvc = paSvcs.get(i);
			paSvc.setParent(returnObj);
		}
		
		// 생성된 객체가 TargetProxy 구현했다면 이것은 Proxy를 생성하여 반환하는 경우이다.(2008.03.06 김형도)
		if (returnObj instanceof TargetProxy) {
			//log.debug("constructObject() returnObj is TargetProxy.");
			if (inf == null) {
				throw new ServiceContainerException(
						"cannot instantiate a proxy object. [" + svcDef.getServiceName() + "] (no interface is given)"); 
			}
			
			// TargetProxy 객체 설정
			TargetProxy targetProxyObj = (TargetProxy)returnObj;
			targetProxyObj.setInterface(inf);
			targetProxyObj.setServiceContainer(this);
			
			try {
				// orgSvc를 Proxy로 다시 감싼다.
				returnObj = Proxy.newProxyInstance(inf.getClassLoader(), 
												new Class[]{inf}, 
												targetProxyObj);
			} 
			catch (IllegalArgumentException e) {
				throw new ServiceContainerException(
						"cannot instantiate a proxy object. [" + svcDef.getServiceName() + "]", e);
			} 
		}
		
		// NameAwareService 처리
		if (returnObj instanceof NameAwareService) {
			((NameAwareService)returnObj).setServiceName(svcDef.getServiceName());
		}
		
		//log.debug("contstructObject() returns "+returnObj.getClass().getCanonicalName());
		return returnObj;
	}
	
	/**
	 * 등록된 SerivcePostProcessor 객체들의 postProcess()를 실행한다.
	 * @param svcObj
	 * @param svcName
	 * @return postProcess가 적용된 결과 객체
	 */
	private Object postProcess(Object svcObj, String svcName) {
		for(int i = 0; i < postProcessors.size(); i++) {
			svcObj = postProcessors.get(i).postProcess(svcObj,svcName);
		}
		
		return svcObj;
	}
	
	/**
	 * 등록된 AutoProxy 객체 들의 proxyProcess()를 실행한다.
	 * @param svcObj
	 * @param svcDef
	 * @return autoproxy가 적용된 결과 객체
	 */
	private Object proxyProcess(Object svcObj, ServiceDefinition svcDef) {
		for(int i = 0; i < proxyProcessors.size(); i++) {
			svcObj = proxyProcessors.get(i).proxyProcess(svcObj,svcDef);
		}
		
		return svcObj;
	}
	
	/**
	 * 서비스 객체에 주어진 Property 목록의 내용대로 설정한다.
	 * Property 설정은 setter injection 방식이다.
	 * @param svcObj Property를 설정할 서비스 객체이다.
	 * @param props PropertyHolder 목록이다.
	 * @param runningCache singleton 타입이 아닌 서비스 객체가 중간에 생성된 경우에 여기에 저장되어 있다..
	 * @throws ServiceContainerException
	 */
	private void injectBySetterMethod(Object svcObj, ServiceDefinition svcDef, 
			                          Map<String,ServiceObject> runningCache)
		throws ServiceContainerException {
		
		if ( svcDef == null ) {
			return;
		}
		
		List<PropertyHolder> props = svcDef.getProperties();
		
		if ( props == null || props.size() == 0 )
			return;
		
		Class<?>  clazz = svcObj.getClass();
		String className = clazz.getName();
		
		// setter method가 가능한 메소드 목록을 생성한다.
		Method[] methods = getSetterMethodCandidates(clazz);
		
		// 각각의 Property 에 대하여 inject 실행
		for(int i = 0; i < props.size(); i++) {
			PropertyHolder ph = props.get(i);
			
			// inject할 setter method를 찾는다.
			Method m = getSetterMethod(methods, ph.getName(), className);

			Class<?> paramClass = null;
			Object paramObj = null;
			try {
				if ( ph.hasValue() ) {
					// setter method의 파라메터를 생성한다.
					paramClass = m.getParameterTypes()[0];
					//System.out.println("setter param type :" + paramClass.getName());
					
					// 파라메터 클래스가 primitive type 인 경우에는 Wrapper Type으로 바꾼다.
					if ( paramClass.isPrimitive() ) {  
						paramClass = ObjectHelper.getWrapperType(paramClass);
					}
					
					// 파라메터 객체를 생성하여 그것으로 Setter 메소드를 호출한다.
					paramObj = ObjectHelper.instantiate(paramClass, 
							                                   new Class[]{String.class},
							                                   new Object[]{ph.getValue()});
					m.invoke(svcObj, new Object[]{paramObj});
				} 
				else if ( ph.hasReference() ) {
					// setter method의 파라메터용 서비스 객체를 얻어온다. 상태에 상관없이 가져온다.
					ServiceObject svcObject = getServiceObject(ph.getValue(), true, runningCache);
					// 서비스 객체가 생성전이라면 서비스간에 상호 참조가 존재하는 것이므로 에러를 발생시킨다.
					if ( !svcObject.isConstructed() ) {
						throw new ServiceContainerException(
								"circular dependency is detected at " + svcDef.getServiceName() + " <->  " + ph.getValue());
					}
					
					// Ref로 참조되는 객체가 ParentAwareService라면 setParent() 호출한다.
					Object innerObj = svcObject.getInnerObject();
					if (innerObj instanceof ParentAwareService) {
						ParentAwareService paSvc = (ParentAwareService)innerObj;
						paSvc.setParent(svcObj);
					}
					
					// 참조 서비스를 파라메터로 Setter 메소드를 호출한다.
					paramObj = svcObject.getService();
					m.invoke(svcObj, new Object[]{paramObj});
				} 
				else {
					m.invoke(svcObj, (Object[])null);
				}
			} 
			catch (IllegalArgumentException e) {
				throw new ServiceContainerException(
						m.getName() + "() method is invoked with an illegal argument. (" 
				        + className + "),(" + paramObj.getClass().getName() + ")", e);
			} 
			catch (SecurityException e) {
				throw new ServiceContainerException(
						m.getName() + "() method is not public. (" + className + ")", e);
			} 
			catch (InstantiationException e) {
				throw new ServiceContainerException(
						"cannot instantiate for setter method's parameter : " + m.getName(), e);
			} 
			catch (IllegalAccessException e) {
				throw new ServiceContainerException(
						"constructor for parameter object is not accessible : " + m.getName(), e);
			} 
			catch (InvocationTargetException e) {
				throw new ServiceContainerException(
						"invocation failed for setter method : " + m.getName(), e);
			} 
			catch (NoSuchMethodException e) {
				throw new ServiceContainerException(
						paramClass.getName()+" has no constructor(String) for the parameter object at " 
				        + m.getName() + "() method. (" + className + ")", e);
			}
		}
	}
	
	/**
	 * 서비스 객체의 메소드를 호출한다.
	 * @param svcName
	 * @param obj
	 * @param methodName
	 * @throws ServiceContainerException
	 */
	private void invokeMethod(String svcName, Object obj, String methodName) 
			throws ServiceContainerException
	{
		Method m;
		
		if ( obj == null || StringHelper.isNull(methodName) ) {
			return;
		}
		
		try {
			m = obj.getClass().getMethod(methodName, (Class[])null);
			m.invoke(obj, (Object[])null);
		} 
		catch (SecurityException e) {
			throw new ServiceContainerException(
					methodName + "() method is not public. ["+svcName+"]",e);
		} 
		catch (IllegalArgumentException e) {
			throw new ServiceContainerException(
					methodName + "() method is invoked with an illegal argument. ["+svcName+"]",e);
		} 
		catch (NoSuchMethodException e) {
			throw new ServiceContainerException(
					methodName+"() method not found. ["+svcName+"]",e);
		} 
		catch (IllegalAccessException e) {
			throw new ServiceContainerException(
					methodName+"() is not accessible. ["+svcName+"]",e);
		} 
		catch (InvocationTargetException e) {
			throw new ServiceContainerException(
					"invocation failed for "+methodName+"(). ["+svcName+"]",e);
		}
	}
	
	/**
	 * property 명에 해당되는 setter method 명을 생성한 후
	 * 주어진 메소드 목록에서 해당되는 메소드를 찾아서여 리턴한다.
	 * 예를 들어 property 명이 name 이면 setter method 명은 setName 이 된다.
	 * @param methods
	 * @param pname
	 * @return
	 */
	private Method getSetterMethod(Method[] methods, String pname, String className) 
			throws ServiceContainerException 
	{
		String setterMethodName = null;
		
		if ( StringHelper.isNull(pname)) {
			throw new ServiceContainerException("property name is not specified for injection.");
		}
		
		// setter method 명을 생성
		StringBuilder sb = new StringBuilder(pname.length()+3);
		sb.append("set");
		sb.append(pname.substring(0, 1).toUpperCase());
		sb.append(pname.substring(1,pname.length()));
		
		setterMethodName = sb.toString();
		
		// 주어진 메소드 목록에서 검색
		Method setterMethod = null;
		boolean found = false;
		for(int i=0;i<methods.length;i++) {
			if ( methods[i].getName().equals(setterMethodName)) {
				if ( found ) {
					throw new ServiceContainerException(
							"more than one "+setterMethodName+"() method is found at " + className);
				} 
				else {
					found = true;
					setterMethod = methods[i];
				}
			}
		}
		
		if ( setterMethod == null ) {
			throw new ServiceContainerException(
					setterMethodName +"() method is not found or not public. (" + className + ")");
		}
		
		return setterMethod;
		
	}
	
	/**
	 * 지정된 클래스에서 setter method가 가능한 후보 method를 뽑아서 배열로 리턴한다.
	 * setter method가 가능한 경우는 "set"으로 시작하는 public 메소드로 입력 파리메터의 개수가 1개인 경우이다.
	 * @param clazz
	 * @return setter method가 가능한 메소드 배열
	 */
	private Method[] getSetterMethodCandidates(Class<?> clazz) {
		Method[] methods = clazz.getMethods();
		ArrayList<Method> mlist = new ArrayList<Method>();
		
		if (log.isDebugEnabled()) {
			log.debug("Get setter methods of class "+clazz.getName());
			if (methods == null) {
				log.debug("Class.getMethods() returns null. ["+clazz.getName()+"]");
			}
		}
		
		for(int i=0;i<methods.length;i++) {
			if ( !methods[i].getName().startsWith("set")) {
				continue;
			}
			
			if ( (methods[i].getModifiers() & Modifier.PUBLIC) == 0 ) {
				continue;
			}
			
			if ( methods[i].getParameterTypes().length != 1 ) {
				continue;
			}
			
			mlist.add(methods[i]);
		}
		
		return mlist.toArray(new Method[mlist.size()]);
	}
	
	private ServiceDefinition getServiceDefinition(String svcName) {
		
		if ( svcRegistry == null ) {
			throw new ServiceContainerException(svcName + " service is not registered.");
		}
		
		ServiceDefinition svcDef = svcRegistry.getServiceDefinition(svcName);
		
		if ( svcDef == null ) {
			throw new ServiceContainerException(svcName + " service is not registered.");
		}
		
		return svcDef;
	}
	
	/**
	 * 등록된 모든 서비스 객체를 singletonCache에서 제거한다.
	 * 서비스 정의 내용은 제거하지 않는다.
	 */
	private void destroyServices() 
	{
		long stime = System.currentTimeMillis();
		if ( log.isInfoEnabled() ) {
			log.info("## destroying all singleton services..");
		}
		
		String[] svcNames = getAllServiceNames();
		ServiceObject svcObject = null;
		synchronized(singletonCache) {
			for(int i=0;i<svcNames.length;i++) {
				svcObject = singletonCache.get(svcNames[i]);
				if (svcObject != null) {
					singletonCache.remove(svcNames[i]);
					destroyService(svcNames[i], svcObject);
				}
			}
		}

		if ( log.isInfoEnabled() ) {
			long etime = System.currentTimeMillis();
			log.info("## done... "+(etime-stime)+" msec.");
		}
	}
	
	/**
	 * 서비스 객체의 소멸 메소드를 호출한다.
	 * @param svcName
	 * @param svcObject
	 */
	private void destroyService(String svcName, ServiceObject svcObject) {
		ServiceDefinition svcDef = svcRegistry.getServiceDefinition(svcName);
		
		String destroyMethod = svcDef.getDestroyMethod();
		if ( StringHelper.isNull(destroyMethod) )  {
			return;
		}
		
		try {
			Object innerObj = svcObject.getInnerObject();
			invokeMethod(svcName,innerObj,destroyMethod);
		} 
		catch (Exception ex) {
			if ( log.isErrorEnabled() ) {
				log.error(ex.getMessage());
			}
		}
	}
	
	public void reload() {
		destroyServices();
		
		if (isPopulated) {
			populateServices();
		}
	}
	
	public Map<String,Object> getServicesOfType(String typeName) throws ServiceContainerException {
		long stime = System.currentTimeMillis();
		Map<String,Object> svcMap = new HashMap<String,Object>();
		
		if ( log.isDebugEnabled() ) {
			log.debug("## find services of type " + typeName);
		}
		
		Set<String> nameSet = svcRegistry.getAllServiceNames();
		
		for(String svcName:nameSet) {
			ServiceDefinition svcDef = svcRegistry.getServiceDefinition(svcName);
			if (svcDef.isActivated() && (typeName.equals(svcDef.getServiceInterface()) 
					|| typeName.equals(svcDef.getServiceClass()))) {
				svcMap.put(svcName, getService(svcName));
			}	
		}
		
		if ( log.isDebugEnabled() ) {
			long etime = System.currentTimeMillis();
			log.debug("## done... " + (etime-stime) + " msec. " + svcMap);
		}
		
		return svcMap;
	}
	
	/**
	 * <code>pre-init</code>, <code>singleton</code>, <code>activate</code> 속성이 모두 true 인 서비스들을
	 * 미리 생성한다.
	 */
	public void populateServices() {	
		long stime = System.currentTimeMillis();
		
		Set<String> nameSet = svcRegistry.getAllServiceNames();
		if ( log.isInfoEnabled() ) {
			log.info("## all service names : " + nameSet);
		}
		
		if ( log.isInfoEnabled() ) {
			log.info("## populates all singleton services with 'pre-init'='true'.");
		}
		
		for(String svcName:nameSet) {
			ServiceDefinition svcDef = svcRegistry.getServiceDefinition(svcName);
			if (svcDef.isPreInit() && svcDef.isSingleton() && svcDef.isActivated()) {
				try {
					getService(svcName);
					if ( log.isInfoEnabled()) {
						log.info("## populates service ["+svcName+"]");
					}
				} 
				catch(Throwable e) {
					if ( log.isErrorEnabled()) {
						log.error("## failed to populate service ["+svcName+"].",e);
					}
				}
				
			}
		}
		
		isPopulated = true;
		
		if ( log.isInfoEnabled() ) {
			long etime = System.currentTimeMillis();
			log.info("## done... "+(etime-stime)+" msec.");
		}
	}
	
	private void doClose() {
		destroyServices();
	}
	
	/**
	 * 서비스 객체를 생성하기 위하여 설정된 클래스로더를 사용하여 주어진 명칭의 클래스를 로딩한다. 
	 * @param className
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Class<?> loadClass(String className) throws ClassNotFoundException { 
		return classLoader.loadClass(className);
	}
	
	/**
	 * 등록된 서비스 목록 중에서 AutoProxy를 구현한 서비스를 찾아서 proxyProcessor로 등록한다.
	 */
	private void addAutoProxies() {
		long stime = System.currentTimeMillis();
		if ( log.isInfoEnabled() ) {
			log.info("## find and register all auto-proxy services.");
		}
		
		Set<String> nameSet = svcRegistry.getAllServiceNames();
		
		for(String svcName:nameSet) {
			ServiceDefinition svcDef = svcRegistry.getServiceDefinition(svcName);
			try {
				if (svcDef.isActivated() && svcDef.isPreInit() && 
						AutoProxy.class.isAssignableFrom(loadClass(svcDef.getServiceClass()))) {
					proxyProcessors.add((AutoProxy)getService(svcName));
					if ( log.isDebugEnabled()) {
						log.debug("## add auto-proxy ["+svcName+"]");
					}
				}
			} 
			catch(Throwable e) {
				if ( log.isErrorEnabled()) {
					log.error("## error occurs on checking auto-proxy ["+svcName+"].",e);
				}
			}
		}
		
		if ( log.isInfoEnabled() ) {
			long etime = System.currentTimeMillis();
			log.info("## done... " + (etime-stime) + " msec.");
		}
	}
	
}
