package s2.adapi.framework.web.action;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import s2.adapi.framework.util.StringHelper;

/**
 * WebMultiAction을 구현시 필요한 호출 메소드 설정 및 조회 기능을 구현한 클래스이다.
 * WebMultiAction은 다음과 같이 구현한다.
 * <ol>
 *   <li> 구현할 멀티 액션 인터페이스를 정의한다. 이때 WebMultiAction 인터페이스를 extends한다.
 *   <li> 멀티 액션 클래스를 구현한다. 이때 AbstractWebMultiAction을 extends 하고 위에서 정의한
 *   인터페이스를 implements 해야한다.
 *   <li> 서비스 구성파일 작성한다. 이때 "selector"와 "defaultMethod" property를 설정하도록 한다.
 * </ol>
 * 작성 예)
 * <pre>
 * public interface TestWebMultiAction extends WebMultiAction {
 *    
 *    WebActionForward display(HttpServletRequest request, HttpServletResponse response) throws Exception;
 *    WebActionForward update(HttpServletRequest request, HttpServletResponse response) throws Exception;
 *    ... 중략 ...
 * }
 * 
 * public class TestWebMultiActionImpl extends AbstractWebMultiAction 
 *         implements TestWebMultiAction {
 *
 *    private static final Log log = LogFactory.getLog(TestWebMultiActionImpl.class);
 *    
 *    public WebActionForward display(HttpServletRequest request,
 *            HttpServletResponse response) throws Exception {
 *        log.error("This is TestWebMultiAction.display()...");
 *        return createForward("default").addModel("code","display");
 *    }
 *
 *    public WebActionForward update(HttpServletRequest request,
 *            HttpServletResponse response) throws Exception {
 *        log.error("This is TestWebMultiAction.update()...");
 *        return createForward("default").addModel("code","update");
 *    }
 * }
 * 
 *     &lt;service name="testmulti.do"
 *             interface="${package}.TestWebMultiAction"
 *             class="${package}.TestWebMultiActionImpl"
 *             interceptor="system.proxy"
 *             singleton="true"&gt; 
 *         &lt;property name="selector" value="method"/&gt;
 *         &lt;property name="defaultMethod" value="display"/&gt;
 *         &lt;property name="forward" value="default:=/ganhogibonweb/jsp/test.jsp"/&gt;
 *         &lt;property name="prefix" value="/webapps/tmp"/&gt;
 *     &lt;/service&gt;
 * </pre>
 * @author 김형도
 * @since 4.0
 */
public abstract class AbstractWebMultiAction extends AbstractWebAction implements WebMultiAction {
    
    /**
     * 호출할 메소드 명을 얻기위한 reqeust parameter 명
     */
    protected String methodSelector = null;
    
    /**
     * 호출할 메소드 명을 찾지 못할 경우 사용할 디폴트 메소드 명
     */
    protected String defaultMethodName = null;
    
    public WebActionForward execute(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        throw new Exception("execute() not implemented.");
    }

    public void showConfiguration(HttpServletResponse response)
            throws IOException {
        super.showConfiguration(response);
        
        PrintWriter out = response.getWriter();
        out.print("<p>");
        
        out.println("<ul>");
        out.println("<li> Method Selector : " + methodSelector);
        out.println("<li> Default Method : " + defaultMethodName);
        out.println("</ul></p>");
    }
    
    /**
     * 호출할 메소드 명을 얻기 위하여 사용할 파라메터의 이름(Selector)을 지정한다.
     * Selector가 지정되지 않은 경우 <code>getMethodName()</code>는 항상 null을 리턴한다.
     * @param selectorName
     */
    public void setSelector(String selectorName) {
        this.methodSelector = selectorName;
    }
    
    /**
     * 호출할 메소드 명을 찾지 못했을 경우 사용할 디폴트 메소드 명을 지정한다.
     * @param defaultName
     */
    public void setDefaultMethod(String defaultName) {
        this.defaultMethodName = defaultName;
    }
    
    /**
     * HttpServletRequest의 request parameter 중에서 selector로 지정된 파라메터 명을 사용하여 호출할 메소드 명을 얻어온다. 
     * Selector로 지정된 파라메터명에 해당되는 값이 존재하지 않으면 디폴트로 설정한 메소드 명을 반환한다. Selector나 디폴트 메소드명이 지정되지 않은 경우에는 null을 반환한다.
     * @param request
     * @param defaultSelector 설정된 Selector가 없을 경우에 사용할 디폴트 selector 값
     * @return 호출할 메소드명
     */
    public String getMethodName(HttpServletRequest request) {
        String methodName = null;

        if ( methodSelector != null ) {
            methodName = StringHelper.null2string(request.getParameter(methodSelector),defaultMethodName);
        }

        return methodName;
    }
}
