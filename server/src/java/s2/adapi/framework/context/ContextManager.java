package s2.adapi.framework.context;

import java.util.HashMap;
import java.util.Map;

import s2.adapi.framework.config.Configurator;
import s2.adapi.framework.config.ConfiguratorException;
import s2.adapi.framework.config.ConfiguratorFactory;
import s2.adapi.framework.dao.sql.Transaction;

/**
 * 현재 쓰레드와 관련된 컨텍스트 정보를 ThreadLocal 변수에 관리저장 관리하는 기능을 제공한다. 
 * 관리되는 컨텍스트로는 EJBContext, UserTransaction, ServiceContext 그리고 성능 관련 데이터를
 * 관리하기 위한 Diagnostic Map 들이 있다.
 * 
 * @author 김형도
 * @since 4.0
 */
public class ContextManager {

	/**
	 * 설정 파일에서 ContextManager의 Context 타입을 결정하기 위하여 
	 * J2EE Context가 true인지 false인지를 확인하기 위한 키이다.
	 */
	public static final String J2EE_CONTEXT_PROPERTY_KEY = "s2adapi.context.j2ee";
	
	public static final int J2EE_CONTEXT = 0; // ContextManager의 Context Type이 J2EE 환경임을 나타낸다.
	public static final int PLATFORM_CONTEXT = 1; // ContextManager의 Context Type이 일반 Java Application 환경임을 나타낸다.
	
	private static int contextType = J2EE_CONTEXT;
	
	private static ThreadLocal<ServiceContext> svcContext = new ThreadLocal<ServiceContext>();
	private static ThreadLocal<HashMap<String,Object>> diagContext = new ThreadLocal<HashMap<String,Object>>();
	
	static {
		/**
		 * 실행환경이 WAS인지 stand-alone Java application인지를 설정파일에서 읽어와 저장해 놓는다.
		 */
		boolean j2eeContext = true;
		try {
			Configurator config = ConfiguratorFactory.getConfigurator();
			j2eeContext = config.getBoolean(J2EE_CONTEXT_PROPERTY_KEY,true);
		} 
		catch (ConfiguratorException e) {
			j2eeContext = true;
		}
		
		if ( j2eeContext ) {
			contextType = J2EE_CONTEXT;
		}
		else {
			contextType = PLATFORM_CONTEXT;
		}
	}
	
	/**
	 * 실행환경이 WAS인지 stand-alone Java application인지를 리턴한다.
	 * 설정파일을 통하여 설정된 값이다.
	 * @return
	 */
	public static int getContextType() {
		return contextType;
	}
	
	/**
	 * 현재 쓰레드에 ServiceContext 객체를 저장한다.
	 * @param ejbCtx
	 */
	public static void setServiceContext(ServiceContext svcCtx) {
		svcContext.set(svcCtx);
	}
	
	/**
	 * 현재 쓰레드에 저장되어 있는 ServiceContext 객체를 반환한다.
	 * @return
	 */
	public static ServiceContext getServiceContext() {
		ServiceContext svcCtx = svcContext.get();
		if (svcCtx == null) {
			svcCtx = new ServiceContext();
			svcContext.set(svcCtx);
		}
		
		return svcCtx;
	}
	
	/**
	 * 현재 쓰레드에 저장되어 있는 ServiceContext 객체를 쓰레드에서 삭제한다.
	 *
	 */
	public static void clearServiceContext() {
		svcContext.set(null);
	}
	
	/**
	 * 현재 쓰레드의 성능 관련 데이터를 관리하는 Map 객체를 반환한다.
	 * @return
	 */
	public static Map<String,Object> getDiagnosticContext() {
		HashMap<String,Object> diagCtx = diagContext.get();
		if (diagCtx == null) {
			diagCtx = new HashMap<String,Object>();
			diagContext.set(diagCtx);
		}
		
		return diagCtx;
	}
	
	/**
	 * 현재 쓰레드의 성능 관련 데이터를 관리하는 Map 객체를 삭제한다.
	 */
	public static void clearDiagnosticContext() {
		diagContext.set(null);
	}
	
	/**
	 * 현재 디버깅 상태임을 설정한다.
	 * @param debuging
	 */
	public static void setDebugingMode(boolean debuging) {
		getServiceContext().setDebugingMode(debuging);
	}
	
	public static boolean isDebugingMode() {
		return getServiceContext().isDebugingMode();
	}
	
	/**
	 * 현재 쓰레드 수행 중 어플리케이션에서 setRollbackOnly()를 호출했는지 여부를 설정한다.
	 * 실제로 트랜젝션의 상태가 Rollback으로 마크되는 것은 아니다.
	 *
	 */
	public static void setRollbackOnly() {
		//LocalTransaction.current().setRollbackOnly();
		Transaction.current().setRollbackOnly();
	}
	
	/**
	 * 현재 쓰레드에 저장되어 있는 EJBContext, ServiceContext 그리고 UserTransaction 객체들을 
	 * 모두 쓰레드에서 삭제한다.
	 *
	 */
	public static void clearAll() {
		clearServiceContext();
		clearDiagnosticContext();
	}
}
