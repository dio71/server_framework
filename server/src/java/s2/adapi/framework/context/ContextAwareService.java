package s2.adapi.framework.context;

import java.util.Map;

import s2.adapi.framework.resources.Messages;

/**
 * <p>
 * 현재 Application이 실행환경이 WAS나 stand-alone Java application인지 상관없이
 * 동일하게 Transaction 관련 기능을 사용할 수 있도록 관련 기능들을 제공한다.
 * 또한 추가적인 사용자 정의 컨텍스트(ServiceContext)를 접근할 수 있는 기능을 제공한다.
 * </p>
 * @author 김형도
 * @since 4.0
 */
public class ContextAwareService {

	private ApplicationContext applicationContext = null;
	
	/**
	 * <p>
	 * 현재 쓰레드에 연결된 트랜젝션을 롤백상태로 마크한다. 
	 * <code>ContextManager</code>로부터 현재 쓰레드의 EJBContext와 UserTransaction 객체를 얻어온 후, 
	 * UserTransaction이 설정되어 있다면 UserTransaction의 setRollbackOnly()를 호출하고, 	 
	 * UserTransaction이 설정되어 있지 않다면 EJBContext의 setRollbackOnly()를 호출한다.
	 * </p>
	 * <p>
	 * 또한 어플리케이션에서 setRollbackOnly() 가 호출되어 트랜젝션이 rollback으로 마킹이 되었는지 아니면
	 * 트랜젝션 타임아웃등 시스템 적으로 rollback이 마킹되었는지 구분하기 위하여 ContextManager.setRollbackOnly()를
	 * 호출한다.
	 */
	protected void setRollbackOnly() throws IllegalStateException 
	{
		ContextManager.setRollbackOnly();
	}
	
	/**
	 * 현재 쓰레드에 연결된 서비스 컨텍스트 객체를 반환한다.
	 * @return
	 */
	protected ServiceContext getServiceContext() {
		return ContextManager.getServiceContext();
	}
	
	/**
	 * 현재 쓰레드에 연결된 Diagnostic 컨텍스트 객체를 반환한다.
	 * @return
	 */
	protected Map<String,Object> getDiagnosticContext() {
		return ContextManager.getDiagnosticContext();
	}
	
	/**
	 * 현재 쓰레드에 연결된 서비스 컨텍스트의 서버 메시지를 설정한다.
	 * @param msgKey 메시지 번호
	 */
	protected void setMessage(String msgKey) {
		getServiceContext().setMessage(
				Messages.getMessages().getMessage(msgKey));
	}
	
	/**
	 * 현재 쓰레드에 연결된 서비스 컨텍스트의 서버 메시지를 설정한다.
	 * @param msgKey 메시지 번호
	 * @param param1 메시지 파라메터
	 */
	protected void setMessage(String msgKey, Object param1) {
		getServiceContext().setMessage(
				Messages.getMessages().getMessage(msgKey,param1));
	}
	
	/**
	 * 현재 쓰레드에 연결된 서비스 컨텍스트의 서버 메시지를 설정한다.
	 * @param msgKey 메시지 번호
	 * @param param1 메시지 파라메터 1
	 * @param param2 메시지 파라메터 2
	 */
	protected void setMessage(String msgKey, Object param1, Object param2) {
		getServiceContext().setMessage(
				Messages.getMessages().getMessage(msgKey, 
						new Object[]{param1,param2}));
	}
	
	/**
	 * 현재 쓰레드에 연결된 서비스 컨텍스트의 서버 메시지를 설정한다.
	 * @param msgKey 메시지 번호
	 * @param param1 메시지 파라메터 1
	 * @param param2 메시지 파라메터 2
	 * @param param3 메시지 파라메터 3
	 */
	protected void setMessage(String msgKey, Object param1, Object param2, Object param3) {
		getServiceContext().setMessage(
				Messages.getMessages().getMessage(msgKey, 
						new Object[]{param1,param2,param3}));
	}
	
	/**
	 * 현재 쓰레드에 연결된 서비스 컨텍스트의 서버 메시지를 설정한다.
	 * @param msgKey 메시지 번호
	 * @param params 메시지 파라메터 배열
	 */
	protected void setMessage(String msgKey, Object[] params) {
		getServiceContext().setMessage(
				Messages.getMessages().getMessage(msgKey, params));
	}
	
	/**
	 * 어플리케이션 컨텍스트 객체를 설정한다. ContextPostProcessor를 통하여 호출된다.
	 * @param ctx
	 */
	void setApplicationContext(ApplicationContext ctx) {
		applicationContext = ctx;
	}
	
	/**
	 * 현재 쓰레드에 연결된 어플리케이션 컨텍스트 객체를 반환한다.
	 * @return
	 */
	protected ApplicationContext getApplicationContext() {
		return applicationContext;
	}
}
