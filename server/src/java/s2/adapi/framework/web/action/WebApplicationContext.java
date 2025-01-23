package s2.adapi.framework.web.action;

import javax.servlet.ServletContext;

import s2.adapi.framework.container.ServiceContainer;
import s2.adapi.framework.context.ApplicationContext;
import s2.adapi.framework.resources.Messages;

/**
 * WebAction이 실행될 때 필요한 정보들을 제공한다.
 * @author 김형도
 * @since 4.0
 */
public class WebApplicationContext implements ApplicationContext {

	private String applicationName = null;
	private ServletContext servletContext = null;
	private ServiceContainer serviceContainer = null;
	private Messages messages = null;
	
	public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = "s2adapi.web.context.attribute";
	
	public WebApplicationContext(String appName) {
		applicationName = appName;
	}
	
	public void setServletContext(ServletContext ctx) {
		servletContext = ctx;
	}
	
	public ServletContext getServletContext() {
		return servletContext;
	}
	
	public String getApplicationName() {
		return applicationName;
	}
	
	public void setServiceContainer(ServiceContainer svcContainer) {
		serviceContainer = svcContainer;
	}
	
	public ServiceContainer getServiceContainer() {
		return serviceContainer;
	}
	
	public void setMessages(Messages msg) {
		messages = msg;
	}
	
	public Messages getMessages() {
		return messages;
	}
	
	public void clear() {
		messages = null;
		serviceContainer = null;
		servletContext = null;
	}
}
