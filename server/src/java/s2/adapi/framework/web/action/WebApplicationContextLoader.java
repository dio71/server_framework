package s2.adapi.framework.web.action;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.apache.log4j.LogManager;

import s2.adapi.framework.Constants;
import s2.adapi.framework.config.ConfiguratorException;
import s2.adapi.framework.config.ConfiguratorFactory;
import s2.adapi.framework.config.KeyStore;
import s2.adapi.framework.container.ContainerConfig;
import s2.adapi.framework.container.ServiceContainer;
import s2.adapi.framework.context.ContextPostProcessor;
import s2.adapi.framework.resources.Messages;
import s2.adapi.framework.resources.ResourcesReloader;
import s2.adapi.framework.util.SystemHelper;

/**
 * <p>
 * Servlet 컨테이너가 초기화되는 시점과 Servlet 컨테이너가 중지되는 시점에 호출되는
 * ServletContextListener의 구현 클래스이다.
 * 해당 ServletContext에 WebApplicatinContext 객체를 생성하여 담아 놓는다.
 * WebApplicationContext는 Servlet 프로그램이나 WebAction 클래스에서 사용이 가능하며
 * ServiceContainer와 Message resources 등의 WebApplication 수행을 위한 
 * 정보를 제공한다.
 * <p> web.xml 에 아래와 같이 &lt;listener&gt; 를 설정하여야 한다. &lt;listener&gt;는 web.xml 내에서
 * &lt;filter-mapping&gt;와 &lt;servlet&gt; 사이에 위치한다.
 * <pre>
 *    &lt;listener&gt;
 *      &lt;listener-class>s2.adapi.framework.web.action.WebApplicationContextLoader&lt;/listener-class&gt;
 *    &lt;/listener&gt;
 * </pre>
 * <p> WebApplicationContextLoader는 web.xml의 &lt;context-param&gt;를 사용하여 다음의 파라메터를 지정할 수 있다.
 * <ul>
 * <li> s2adapi.web.default.resource : 사용할 메시지 Resource 명을 지정한다.  &lt;context-param&gt;로 지정되지 않은 경우에는
 * framework 설정 파일(s2adapi-config.properties)에서 s2adapi.resources.default.name로 지정된 값을 사용하며
 *  이것도 지정되지 않은 경우에는 "default"를 사용한다. 메시지 리소스는 WebApplicationContext의 
 *  getMessages() 메소드를 사용하여 얻어올 수 있다.
 * </ul>
 * <p> 아래는 Context parameter을 지정하는 예시이다. &lt;context-param&gt; 는 &lt;listener&gt; 전에 정의한다.
 * <pre>
 *     &lt;context-param&gt;
 *        &lt;param-name&gt;
 *           s2adapi.web.default.resource
 *        &lt;/param-name&gt;
 *        &lt;param-value&gt;
 *            bundle
 *        &lt;/param-value&gt;
 *     &lt;/context-param&gt;
 * </pre>
 * @author 김형도
 * @since 4.0
 */
public class WebApplicationContextLoader implements ServletContextListener {

	private static final String WEB_DEFAULT_RESOURCE_KEY = "s2adapi.web.default.resource";
	
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		String applicationName = ctx.getServletContextName();
		String contextRoot = ctx.getRealPath("/");
		ctx.log("\nContext Root: "+contextRoot);
		SystemHelper.setSystemProperty(Constants.CONFIG_WEBCONTEXT_ABSOLUTE_ROOTPATH_KEY, contextRoot);
		
		//ctx.log("\n"+getVersionMessage());
		
		ctx.log("Loading WebApplicationContext for "+applicationName);
		
		// Configurator 클래스를 로딩한다. (이 시점에 시점에 log4j 설정 파일이 지정된다.)
    	try {
			ConfiguratorFactory.getConfigurator();
			
	    	// Message resource를 로딩할 때 사용할 명칭, 지정되지 않으면 시스템 default를 사용한다.
	    	String messageName = null;
	  
			// Default message resource 명칭이 지정되었다면 그 값을 사용한다.
			ctx.log("Loading Message resource...");
			Messages messages = null;
			if (ctx.getInitParameter(WEB_DEFAULT_RESOURCE_KEY) != null) {
				messageName = ctx.getInitParameter(WEB_DEFAULT_RESOURCE_KEY);
				messages = Messages.getMessages(messageName);
			} else {
				messages = Messages.getMessages();
			}
			
			// ResourcesReloader를 실행한다.
			ResourcesReloader.instance().start();
			
			// KeyStore 설정 (2019.12.05 암호화 키 저장)
			KeyStore.instance().addConfig(applicationName);
			
			//ctx.log("## key " + KeyStore.instance().getConfig(applicationName).keyValue());
			
			// Application 명칭으로 ServiceContainer와 WebApplicationContext 생성
			ctx.log("Loading ServiceContainer... [" + applicationName + "]");
			//ServiceContainer svcContainer = ServiceClient.getServiceContainer(applicationName);
			ServiceContainer svcContainer = ContainerConfig.instantiateContainer(applicationName);
			
			WebApplicationContext webappContext = 
					new WebApplicationContext(applicationName);
			
			// WebApplicationContext 속성 설정
			webappContext.setServletContext(ctx);
			webappContext.setServiceContainer(svcContainer);
			webappContext.setMessages(messages);
			
			// 서비스 컨테이너에 WebAction용 후처리기 등록
			svcContainer.addPostProcessor(new WebActionPostProcessor(webappContext));
			
			// 서비스 컨테이너에 ContextAwareService용 후처리기 등록
			svcContainer.addPostProcessor(new ContextPostProcessor(webappContext));
			
			// pre-intialize 되는 객체들 생성
			svcContainer.populateServices();
			
			// ServletContext에 WebApplicationContext 객체를 attribute로 넣는다.
			ctx.setAttribute(WebApplicationContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE, webappContext);
		} 
    	catch (ConfiguratorException e) {
			ctx.log(e.getMessage());
		}
	}

	public void contextDestroyed(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		String applicationName = ctx.getServletContextName();
		
		ctx.log("Destroying WebApplicationContext of "+applicationName);
		
		// close ServiceContainer
		WebApplicationContext webappContext = (WebApplicationContext)ctx
				.getAttribute(WebApplicationContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		if (webappContext != null) {
			webappContext.getServiceContainer().close();
			webappContext.clear();
		}
		
		ctx.removeAttribute(WebApplicationContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		
		//ServiceClient.removeServiceContainer(applicationName);
		ContainerConfig.removeContainer(applicationName);
		
		ConfiguratorFactory.removeConfigurator();
		
		// ResourcesReloader 종료
		ResourcesReloader.instance().quit();
		
		LogManager.shutdown(); // explicitly close log files. (Log4j)
	}
	
}
