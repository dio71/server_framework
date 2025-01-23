package s2.adapi.framework.web.action;

import s2.adapi.framework.container.ServicePostProcessor;

/**
 * WebAction 객체가 ServiceContainer에서 생성될 때 WebApplicationContext 객체를
 * 전달해주기위한 ServicePostProcessor의 구현 클래스이다. 
 * @author 김형도
 * @since 4.0
 */
public class WebActionPostProcessor implements ServicePostProcessor {

	private WebApplicationContext context = null;
	
	public WebActionPostProcessor(WebApplicationContext ctx) {
		context = ctx;
	}
	
	public Object postProcess(Object svcObject, String svcName) {
		if (svcObject != null && svcObject instanceof WebAction) {
			WebAction webAction = (WebAction)svcObject;
			webAction.setWebApplicationContext(context);
		}
		
		return svcObject;
	}

}
