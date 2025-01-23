package s2.adapi.framework.context;

import s2.adapi.framework.container.ServicePostProcessor;

public class ContextPostProcessor implements ServicePostProcessor {

	private ApplicationContext context = null;
	
	public ContextPostProcessor(ApplicationContext ctx) {
		context = ctx;
	}
	
	public Object postProcess(Object svcObject, String svcName) {
		if (svcObject != null && svcObject instanceof ContextAwareService) {
			ContextAwareService ctxAwareService = (ContextAwareService)svcObject;
			ctxAwareService.setApplicationContext(context);
		}
		
		return svcObject;
	}
}
