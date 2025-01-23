package s2.adapi.framework.resources;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.Constants;
import s2.adapi.framework.context.ContextManager;

/**
 * 특정 세션값을 사용하여 국제화 메시지를 제공하는 클래스이다.
 * 언어 설정 값은 
 * 1. ContextManager 의 ServiceContext.getRole("lang") 의 값을 우선 가져옴
 * 2. 값이 없으면 Request 의 attribute 에서 가져옴
 * 3. 값이 없으면 마지막으로 HttpSession 에서 값을 가져옴
 * 
 * 이 클래스를 가져오기 위해서는 사전에 최소한 위 3가지 중 한군데에는 언어 코드를 설정해 놓아야한다.
 * 주의)  JSP 에서 사용할 경우 ServiceContext  값은  clear 되어 있으므로 ServiceContext 에 설정된 값은 사용할 수 없다.
 * @author kimhd
 *
 */
public class SessionLocaleMessages {

	private static final Logger log = LoggerFactory.getLogger(SessionLocaleMessages.class);
		
	private Locale sessionLocale = null;
	private Messages messages = null;
	
	public SessionLocaleMessages(HttpServletRequest request) {
		this(request, null);
	}

	public SessionLocaleMessages(HttpServletRequest request, String name) {
		String localeName = null;
		
		// 우선 컨텍스트에 설정된 언어 설정 값을 확인한다.
		localeName = (String)ContextManager.getServiceContext().getRole(Constants.CONTEXT_SESSION_LOCALE_NAME);
		
		if (localeName == null) {
			// 컨텍스트에 없으면 다음으로 Request의 attribute에서 언어 설정 값을 확인한다.
			localeName = (String)request.getAttribute(Constants.SERVLET_REQUEST_LOCALE_NAME);
		}
		
		if (localeName == null) {
			// 마지막으로 Servlet 세션에서 언어 설정 값을 확인한다.
			HttpSession session = request.getSession();
			localeName = (String)session.getAttribute(Constants.SERVLET_SESSION_LOCALE_NAME);
		}
		
		// 언어설정 값이 확인되면 이를 사용하여 Locale 객체를 생성해둔다.
		if (localeName != null) {
			sessionLocale = new Locale(localeName);
		}
		
		if (log.isDebugEnabled()) {
			log.debug("#### locale = " + String.valueOf(sessionLocale));
		}
		
		if (name == null) {
			messages = Messages.getMessages();
		}
		else {
			messages = Messages.getMessages(name);
		}
	} 
	
	public String getLanguage() {
		if (sessionLocale != null) {
			return sessionLocale.getLanguage();
		}
		else {
			return Locale.getDefault().getLanguage();
		}
	}
	
	public String getCountry() {
		if (sessionLocale != null) {
			return sessionLocale.getCountry();
		}
		else {
			return Locale.getDefault().getCountry();
		}
	}
	
	public String getMessage(String key) {
		return messages.getMessage(sessionLocale, key);
	}
	
	public String getMessage(String key, Object param) {
		return messages.getMessage(sessionLocale, key, param);
	}
	
	public String getMessage(String key, Object[] params) {
		return messages.getMessage(sessionLocale, key, params);
	}
}
