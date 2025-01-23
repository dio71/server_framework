package s2.adapi.framework.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 메소드 호출 로그 출력용 MethodInterceptor의 구현
 * @author 김형도
 *
 */
public class SimpleLogInterceptor implements MethodInterceptor {

	private boolean showArgument = false;
	private boolean showReturn = false;
	
	public void setShowArgument(boolean flag) {
		showArgument = flag;
	}
	
	public void setShowReturn(boolean flag) {
		showReturn = flag;
	}
	
	public Object invoke(MethodInvocation invocation) throws Throwable 
	{
		long stime = System.currentTimeMillis() ;
		Logger log = LoggerFactory.getLogger(invocation.getThis().getClass());
		//System.out.println(invocation.getThis().getClass().getName()+":"+invocation.getMethod().getName()+"() starts.");
		if(log.isInfoEnabled()) {
			log.info(invocation.getMethod().getName()+"() starts.");
			if (showArgument) {
				Object[] args = invocation.getArguments();
				if (args != null) {
					for(int i = 0; i < args.length; i++) {
						log.info(String.valueOf(invocation.getArguments()[i]));
					}
				} else {
					log.info("No Input Argument...");
				}
			}
		}
		Object retObj;
		try {
			retObj = invocation.proceed();
		}
		catch (Throwable e) {
			long etime = System.currentTimeMillis() ;
			log.error(invocation.getMethod().getName()+"() errors. [" + e.toString() + "](" 
					  + (etime-stime) + " msecs}");
			throw e;
		}

		long etime = System.currentTimeMillis() ;
		if(log.isInfoEnabled()) {
			if (showReturn) {
				log.info(String.valueOf(retObj));
			}
			log.info(invocation.getMethod().getName()+"() ends.(" + (etime-stime) + " msecs}");
		}
		
		return retObj;
	}

}
