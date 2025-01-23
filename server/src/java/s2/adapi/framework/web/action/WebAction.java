package s2.adapi.framework.web.action;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Web MVC Framework의 WebAction 인터페이스이다. 
 * @author 김형도
 * @since 4.0
 */
public interface WebAction {

    /**
     * WebActionDispatcher 서블릿이 호출하는 메소드이다.
     * 이 인터페이스를 구현하는 클래스는 이 메소드에서 업무처리를 수행하고 forward할 View의 URL을
     * WebActionForward로 리턴한다. null을 리턴할 경우에는 forward하지 않는다.
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public WebActionForward execute(HttpServletRequest request, HttpServletResponse response)
            throws Exception;
    
    /**
     * WebAction의 설정값을 출력하는 메소드이다.
     * @param request
     * @param response
     * @throws Exception
     */
    public void showConfiguration(HttpServletResponse response) throws IOException;
    
    /**
     * WebAction 객체가 실행되는 WebActionDispatcher의 WebApplicationtContext 값을 전달한다.
     * WebActionDispatcher에서 사용하는 ServiceContainer의 WebActionPostProcessor를 통하여
     * 처음 WebAction 객체가 생성될 때 호출된다.
     * @param ctx
     */
    public void setWebApplicationContext(WebApplicationContext ctx);
}
