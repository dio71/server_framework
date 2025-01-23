package s2.adapi.framework.aop.target;

import java.lang.reflect.Method;

/**
 * ThreadLocal 변수를 사용하여 쓰레드 별로 target 객체를 생성하는 TargetProxy의 구현클래스이다.
 * @author kimhd
 * @since 5.0
 */
public class ThreadLocalTargetProxy extends AbstractTargetProxy {
    protected static ThreadLocal<Object> targetObject = new ThreadLocal<Object>();
    protected String targetName = null;
    
    public void setTargetName(String name) {
        targetName = name;
    }
    
    public Object invoke(Method method, Object[] args)
            throws Throwable {
        Object svcObject = targetObject.get();
        if (svcObject == null) {
            svcObject = getServiceContainer().getService(targetName);
            targetObject.set(svcObject);
        }
        return method.invoke(svcObject, args);
    }

}
