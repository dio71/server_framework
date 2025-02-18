package s2.adapi.framework.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.config.Configurator;
import s2.adapi.framework.config.ConfiguratorException;
import s2.adapi.framework.config.ConfiguratorFactory;
import s2.adapi.framework.container.ServiceContainer;
import s2.adapi.framework.context.ContextManager;
import s2.adapi.framework.resources.Messages;
import s2.adapi.framework.util.ObjectHelper;
import s2.adapi.framework.util.StringHelper;
import s2.adapi.framework.util.UrlPatternMatcher;
import s2.adapi.framework.web.upload.MultipartRequestWrapper;


/**
 * Web Framework의 MVC 아키텍처 구현을 위한 Controller 클래스로서,
 * HttpServletRequest를 받아서 해당 WebAction 객체로 전달해주는 역활을 수행한다.
 * 이 Servlet이 동작하기 위하여 WebApplicationContext 객체가 필요하며 이것은
 * {@link WebApplicationContextLoader} 통하여 Servlet 컨테이너가 초기화되는 시점에
 * 생성된다.
 * <p>
 * WebActionDispatcher는 그대로 사용할 수 있으나 실제 적용시에는 WebActionDispatcher를 상속받아,
 * preProcess()와 postProcess()에 필요한 기능을 구현하여 사용하도록 한다.
 * <pre>작성예)
 * package himed.his.cmc.web.action;
 * public class WebActionDispatcher extends s2.adapi.framework.web.action.WebActionDispatcher {
 *     protected void preProcess(HttpServletRequest request, HttpServletResponse response) {
 *         // 세션정보로부터 ServiceContext 객체를 생성
 *         ServiceContext svcCtx = new ServiceContext();
 *         svcCtx.setRole("...","...");
 *         ...
 *
 *         // 서비스별로 고유한 트랜젝션 ID를 설정한다.
 *         svcCtx.setServiceTransactionID(idgen.getNextId(this).toString());
 *
 *         // 서비스 컨텍스트를 설정한다.
 *         ContextManager.setServiceContext(svcCtx);
 *     }
 *
 *     protected void postProcess(HttpServletRequest request, HttpServletResponse response,
 *			WebActionForward forward) {
 *
 *         // 처리에 대한 로깅 정보를 남긴다.
 *         logDiagnostics();
 *
 *         // 서비스 컨텍스트를 초기화한다.
 *         ContextManager.clearAll();
 *     }
 * }
 * </pre>
 * <p>
 * 작성된 WebActionDispatcher는 web.xml에 아래와 같이 &lt;servlet&gt;으로 등록하여 사용하도록 하며
 * 이때 다음의 파라메터를 설정할 수 있다.
 * <ul>
 * <li>s2adapi.web.attach.file.maxsize : 첨부파일의 최대 크기를 지정한다. (디폴트 값: 10000000)
 *                                    - 만약 값을 0이하로 주면 Framework의 파일 업로드 기능을 사용하지 않음.
 * <li>s2adapi.web.exception.handler : Servlet처리 중 exception이 발생하였을 경우 처리할 ExceptionHandler 클래스명을 지정한다.
 * ExceptionHandler 클래스는 s2.adapi.framework.web.exception.handler.ExceptionHandler 인터페이스를 구현하여야 한다.
 * <li>s2adapi.web.default.encoding : HttpServletRequest에 character encoding 값이 지정되지 않았을 경우
 * 적용할 encoding 값을 설정한다. 설정하지 않으면 s2adapi-config.properties 파일에 s2adapi.web.default.encoding 으로
 * 지정된 값을 사용한다.
 * <li>s2adapi.web.upload.encoding : Multipart request의 경우 적용할 encoding 값을 설정한다. 설정하지않으면
 *  s2adapi-config.properties 파일에 s2adapi.web.upload.encoding 으로 지정된 값을 사용한다.
 * </ul>
 * 또한, &lt;servlet-mapping&gt;으로 처리할 URI 패턴을 지정하여 특정 URI 패턴에 대하여 처리하도록 설정한다.
 * <pre>작성예)
 *  &lt;servlet&gt;
 *     	&lt;servlet-name&gt;dispatcher&lt;/servlet-name&gt;
 *     	&lt;servlet-class&gt;s2.adapi.pub.web.action.WebActionDispatcher&lt;/servlet-class&gt;
 *     	&lt;init-param&gt;
 *     		&lt;param-name&gt;s2adapi.attach.file.maxsize&lt;/param-name&gt;
 *     		&lt;param-value&gt;100000000&lt;/param-value&gt;
 *     	&lt;/init-param&gt;
 *     	&lt;init-param&gt;
 *     		&lt;param-name&gt;s2adapi.web.exception.handler&lt;/param-name&gt;
 *     		&lt;param-value&gt;himed.his.cmc.web.action.ExceptionHandler&lt;/param-value&gt;
 *     	&lt;/init-param&gt;
 *     	&lt;init-param&gt;
 *     		&lt;param-name&gt;s2adapi.web.default.encoding&lt;/param-name&gt;
 *     		&lt;param-value&gt;utf-8&lt;/param-value&gt;
 *     	&lt;/init-param&gt;
 *       &lt;init-param&gt;
 *     		&lt;param-name&gt;s2adapi.web.upload.encoding&lt;/param-name&gt;
 *     		&lt;param-value&gt;utf-8&lt;/param-value&gt;
 *     	&lt;/init-param&gt;
 *   &lt;/servlet&gt;
 *
 *   &lt;servlet-mapping&gt;
 *     	&lt;servlet-name&gt;dispatcher&lt;/servlet-name&gt;
 *     	&lt;url-pattern&gt;*.svc&lt;/url-pattern&gt;
 *   &lt;/servlet-mapping&gt;
 *
 *   &lt;servlet-mapping&gt;
 *     	&lt;servlet-name&gt;dispatcher&lt;/servlet-name&gt;
 *     	&lt;url-pattern&gt;*.xrw&lt;/url-pattern&gt;
 *   &lt;/servlet-mapping&gt;
 *
 *   &lt;servlet-mapping&gt;
 *     	&lt;servlet-name&gt;dispatcher&lt;/servlet-name&gt;
 *     	&lt;url-pattern&gt;*.rex&lt;/url-pattern&gt;
 *   &lt;/servlet-mapping&gt;
 * </pre>
 * <p>
 * 이 클래스에서는 다음의 Diagnostic Context 값들을 생성한다.<br>
 * <ul>
 * <li>diag.web.action.uri : WebActionDispatcher를 호출하게 한 HttpServletRequest의 URI 값
 * <li>diag.web.action.svcname : URI에 따른 WebAction 서비스 명
 * <li>diag.web.action.stime : WebActionDisaptcher의 service() 진입 시간
 * <li>diag.web.action.etime : WebActionDisaptcher의 service() 종료 시간
 * <li>diag.web.action.rtime : WebActionDisaptcher의 service() 실행 시간
 * </ul>
 * </p>
 * @author 김형도
 * @since 4.0
 * @see WebApplicationContextLoader
 */
public class WebActionDispatcher extends HttpServlet {

    private static final long serialVersionUID = 7172530507104568001L;

    private static Logger log = LoggerFactory.getLogger(WebActionDispatcher.class);

    /**
     * HttpServletRequest의 encoding이 설정되어 있지 않을 경우 적용할 인코딩 값을 지정하는 키값
     */                                                      
    private static final String WEB_DEFAULT_ENCODING_KEY = "s2adapi.web.default.encoding";

    /**
     * HttpServletRequest의 encoding이 설정되어 있지 않을 경우 적용할 디폴트 인코딩 값
     */
    protected static String defaultCharacterEncoding = "euc-kr";

    /**
     * Multipart HttpServletRequest의 encoding이 설정되어 있지 않을 경우 적용할 인코딩 값을 지정하는 키값
     */                                                    
    private static final String WEB_UPLOAD_ENCODING_KEY = "s2adapi.web.upload.encoding";

    /**
     * Multipart HttpServletRequest의 encoding이 설정되어 있지 않을 경우 적용할 디폴트 인코딩 값
     */
    protected static String defaultUploadEncoding = "euc-kr";

    /**
     * 에러 발생시 이를 처리할 ExceptionHanlder 클래스를 지정하는 키값.
     */
    public static final String EXCEPTION_HANDLER_KEY = "exception.handler";

    private static ExceptionHandler exceptionHandler = null;

    /**
     * 첨부파일의 최대 크기를 설정하기 위한 초기화 파라메터 설정 키값
     */
    public static final String ATTACH_FILE_MAXSIZE_KEY = "attach.maxsize";

    private static final int DEFAULT_ATTACH_FILE_MAXSIZE = 10000000;

    private int attachFileSizeLimit = DEFAULT_ATTACH_FILE_MAXSIZE;

    /**
     * WebActionDispatcher 명령들 중 현재 설정 상태를 출력하는 명령이다.
     */
    public static final String COMMAND_SHOW_CONFIGURATION ="showConfiguration";

    /**
     * WebActionDispatcher 명령들 중 ServiceContainer를 reload하는 명령이다.
     */
    public static final String COMMAND_RELOAD_CONTAINER ="reloadContainer";

    /**
     * WebActionDispatcher 명령들 중 System.gc()를 수행하는 명령이다.
     */
    public static final String COMMAND_GC_JVM = "systemGC";

    /**
     * The set of argument type classes for the reflected method call.
     * These are the same for all calls, so calculate them only once.
     */
    private static Class<?>[] argumentTypes = {HttpServletRequest.class,
                                              HttpServletResponse.class};

    /**
     * WebApplicationContext 객체이다.
     */
    private WebApplicationContext webappContext = null;

    private String contextPath = null;

    public void init() throws ServletException {

        log.info("Initializing WebActionDispatcher for "+getServletContext().getServletContextName());

        contextPath = getServletContext().getContextPath();
        
        // 첨부파일의 최대 크기 설정
        String attachFileMaxSize = getInitParameter(ATTACH_FILE_MAXSIZE_KEY);
        if (!StringHelper.isNull(attachFileMaxSize)) {
            try {
                attachFileSizeLimit = Integer.parseInt(attachFileMaxSize);
            } 
            catch (Exception ex) {
                log.error("failed to set attach file size, use default value.",ex);
                attachFileSizeLimit = DEFAULT_ATTACH_FILE_MAXSIZE;
            }
        }
        log.info("attach file max size : "+attachFileSizeLimit);

        // Exception 처리 클래스 등록
        String exceptionHandlerClass = getInitParameter(EXCEPTION_HANDLER_KEY);
        if (!StringHelper.isNull(exceptionHandlerClass)) {
            try {
                exceptionHandler = (ExceptionHandler)ObjectHelper.instantiate(exceptionHandlerClass);
            } 
            catch (Exception ex) {
                log.error("cannot instantiate exception handler class :"+exceptionHandlerClass,ex);
            }
        }
        try {
            Configurator configurator = ConfiguratorFactory.getConfigurator();
            // 2024.10.15 euc-kr -> utf-8 로 변경
            defaultCharacterEncoding = configurator.getString(WEB_DEFAULT_ENCODING_KEY,"utf-8");
            defaultUploadEncoding = configurator.getString(WEB_UPLOAD_ENCODING_KEY,"utf-8");
        } 
        catch (ConfiguratorException e) {
        }

        // default character encoding 값 및 default upload encoding 설정
        String encoding = null;
        encoding = getInitParameter(WEB_DEFAULT_ENCODING_KEY);
        if (!StringHelper.isNull(encoding)) {
            // Init 파라메터로 지정되었으면 그 값으로 설정한다.
            defaultCharacterEncoding = encoding;
        }

        encoding = getInitParameter(WEB_UPLOAD_ENCODING_KEY);
        if (!StringHelper.isNull(encoding)) {
            // Init 파라메터로 지정되었으면 그 값으로 설정한다.
            defaultUploadEncoding = encoding;
        }

        // temp directory for fileupload

        // WebApplicationContext 받아 놓기
        webappContext = (WebApplicationContext)getServletContext()
                .getAttribute(WebApplicationContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE);

        if (webappContext == null) {
            log.error("WebApplicationContext not initialized. Check <listener> configuration in web.xml file.");
        } 
//        else {
//            // 서비스 명 중에서 Url pattern 형태의 서비스 명을 담아놓는다.
//            String[] svcNames = webappContext.getServiceContainer().getAllServiceNames();
//            for(int i=0;i<svcNames.length;i++) {
//            	//System.out.println("service name: " + svcNames[i]);
//                if (UrlPatternMatcher.isPattern(svcNames[i])) {
//                	//System.out.println("is pattern!!!");
//                    urlPatternServiceNames.add(svcNames[i]);
//                }
//            }
//        }
    }

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            process(request, response);
        } 
        catch(Exception ex) {
            if (exceptionHandler != null) {
                exceptionHandler.handle(ex,request,response);
            } 
            else {
                handleException(ex,request,response);
            }
            log.error("",ex);
        }
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            process(request, response);
        } 
        catch(Exception ex) {
            if (exceptionHandler != null) {
                exceptionHandler.handle(ex,request,response);
            } 
            else {
                handleException(ex,request,response);
            }
            log.error("",ex);
        }
    }

    /**
     * ExceptionHandler가 지정되지 않았을 경우 사용되는 exception 처리 루틴
     * @param thr
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void handleException(Throwable thr,
            HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        if (thr instanceof ServletException) {
            throw (ServletException)thr;
        } 
        else if (thr instanceof IOException) {
            throw (IOException)thr;
        } 
        else {
            throw new ServletException(thr);
        }
    }

    /**
     * HttpServletRequest를 받아서 호출된 URI path의 마지막 항목을 WebAction 객체의 서비스 명으로삼는다.
     * ServiceContainer로부터 호출할 해당 객체를 얻어온 후 객체가 WebMultiAction 타입이라면 해당 객체에
     * <code>getMethodName()</code>를 사용하여 호출할 메소드 명을 얻어와 다시 메소드를 호출한다.
     * ServiceContainer로부터 얻어온 객체가 WebAction 타입이라면 해당 객체의 <code>execute()</code>
     * 메소드를 호출한다.
     * <p>
     * WebAction 또는 WebMultiAction 객체의 메소드 호출 결과로 리턴된
     * WebActionForward 객체의 <code>getModel()</code>를 사용하여 얻어진 데이터는
     * request의 <code>setAttribute()</code>를 통하여 request에 저장한 후 WebActionForward 객체의
     * <code>getViewURL()</code>로 얻어진 URL로 포워드한다.
     * </p>
     * <p>
     * 리턴된 WebActionForward 객체가 null 이라면 포워드 하지 않고 종료한다.
     * </p>
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void process(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        long stime = System.currentTimeMillis();
        Object svcObject = null;
        String svcName = null;
        WebActionForward forward = null;

        Map<String,Object> diagMap = ContextManager.getDiagnosticContext(); // Diagnostic Context

        // multipart request 인 경우에 업로드된 파일을 FormFile 객체로 처리하여 request내에 저장한다.
        // 2010.10.20 노준훈 수정 : multipart request max fileupload size limit가 0 이하인 경우 MultipartRequestWrapper을 사용하지 않음.
        if (MultipartRequestWrapper.isMultipart(request)&& 0 < attachFileSizeLimit) {
            request = new MultipartRequestWrapper(request,defaultUploadEncoding)
                    .parseMultipart(getServletConfig().getServletContext(),
                            attachFileSizeLimit);
        } else {
            // default character set 인코딩을 설정한다.
            if (request.getCharacterEncoding() == null) {
                request.setCharacterEncoding(defaultCharacterEncoding);
                if (log.isDebugEnabled()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("no characterset : ").append(request.getRequestURI());
                    String queryString = request.getQueryString();
                    if (queryString != null) {
                        sb.append("&").append(queryString);
                    }
                    log.debug(sb.toString());
                }
            }
        }

        // request URI로부터 호출할 WebAction 객체의 서비스 명을 찾은 후 해당 서비스 객체를 가져온다.
        String uriValue = request.getRequestURI();
        svcName = getServiceName(uriValue);
        
        // Diagnostic Context 설정
        diagMap.put("diag.web.action.uri",uriValue);
        diagMap.put("diag.web.action.target",svcName);
        diagMap.put("diag.web.action.stime",stime);

        try {
            // preProcess() 처리
            preProcess(request, response);

            try {
                if (webappContext == null) {
                    throw new ServletException("No WebApplicationContext provided. Check web.xml or license key.");
                }

                ServiceContainer serviceContainer = webappContext.getServiceContainer();
                
                if (serviceContainer.containsService(svcName)) {
                    // 정확한 서비스가 있으면 그것을 가져온다.
                    svcObject = serviceContainer.getService(svcName);
                } 
                else {
                    // 패턴으로 일치되는 서비스 명을 찾아온다.
                	// 우선 context root path 제거한다. (2012.12.5 REST API 패턴 처리를 위하여 수정함)
                	uriValue = uriValue.substring(contextPath.length());
                	
                	svcName = UrlPatternMatcher.getBestMatched(uriValue, serviceContainer.getPatternServiceNames());
                    if (svcName != null) {
                        svcObject = serviceContainer.getService(svcName);
                    } else {
                        // no matched pattern for uri
                        throw new ServletException(Messages.getMessages().
                                getMessage("service.error.09000",uriValue));
                    }
                }

                if (svcObject == null) {
                    // service not found
                    throw new ServletException(Messages.getMessages().
                            getMessage("service.error.09001",svcName));
                } 
                else if ( !(svcObject instanceof WebAction) ) {
                    // target service is not WebAction.
                    throw new ServletException(Messages.getMessages().
                            getMessage("service.error.09002",svcName));
                }

                WebAction webAction = (WebAction)svcObject;
                if ( log.isDebugEnabled() ) {
                    log.debug("WebAction requested. ["+svcName+"]");
                }

                // WebActionDispatcher의 명령어인 경우 명령어 처리를 한다.
                /*
                if (COMMAND_SHOW_CONFIGURATION.equals(request.getQueryString())) {
                    webAction.showConfiguration(response);
                    showConfiguration(response);
                    return;
                } else if (COMMAND_RELOAD_CONTAINER.equals(request.getQueryString())) {
                    webappContext.getServiceContainer().reload();
                    //SqlQueryReader.getReader().clear();
                    return;
                } else if (COMMAND_GC_JVM.equals(request.getQueryString())) {
                    System.gc();
                    return;
                }*/


                // 서비스 객체의 종류에 따라서 호출 처리한다.
                if ( webAction instanceof WebMultiAction ) {
                    // multi action 처리
                    forward = processWebMultiAction((WebMultiAction)webAction, request, response, diagMap);

                }
                else {
                    // single action 처리
                    forward = processWebAction(webAction, request, response, diagMap);
                }
            }
            finally {
                // Diagnostic Context 설정
                long etime = System.currentTimeMillis();
                diagMap.put("diag.web.action.etime",etime);
                diagMap.put("diag.web.action.rtime",(etime-stime));
            }

            // 정상 처리시의 postProcess() 실행
            postProcess(request,response,forward);

        } 
        catch (Throwable e) {
            try {
                // 오류시의 postProcess() 실행
                forward = postProcess(request,response,e);
            }
            catch (Throwable thr) {
                if (thr instanceof ServletException) {
                    throw (ServletException)thr;
                } 
                else if (thr instanceof IOException){
                    throw (IOException)thr;
                } 
                else {
                    // failed to execute WebAction.
                    if (log.isErrorEnabled()) {
                        log.error("",e);
                    }
                    throw new ServletException(Messages.getMessages().
                            getMessage("service.error.09003",svcName),e);
                }
            }
        }

        // postProcess() 처리
        if ( forward != null ) {
            forward.sendView(request, response);
            if (log.isDebugEnabled()) {
                if (forward.isForward()) {
                    log.debug("WebAction forwarded to "+forward.getViewURL()+"["+svcName+"]");
                } else {
                    log.debug("WebAction redirected to "+forward.getViewURL()+"["+svcName+"]");
                }
            }
        }
    }

    /**
     * WebAction의 execute()나 WebMultiAction()의 method 호출 전에 처리할
     * 전처리 로직을 구현한다.
     * @return
     */
    protected void preProcess(HttpServletRequest request,
            HttpServletResponse response) throws Throwable {
    }

    /**
     * WebAction의 execute()나 WebMultiAction()의 method 호출이
     * 정상 처리되었을 경우의 후처리 로직을 구현한다.
     * @param request
     * @param response
     * @param forward
     */
    protected void postProcess(HttpServletRequest request,
            HttpServletResponse response, WebActionForward forward) {
    }

    /**
     * WebAction의 execute()나 WebMultiAction()의 method 호출 시
     * Exception 발생하였을 경우의 후처리 로직으로
     * 전달된 Exception에 맞는 처리를 한 후 Exception을 다시 던지거나
     * 에러 처리용 WebActionForward를 리턴하는 방식으로 구현한다.
     * @param request
     * @param response
     * @param exeception
     * @return WebActionForward
     */
    protected WebActionForward postProcess(HttpServletRequest request,
            HttpServletResponse response, Throwable thr) throws Throwable {
        throw thr;
    }

    /**
     * WebAction에 대한 처리를 수행한다.
     * 주어진 WebAction 서비스 객체의 execute() 메소들 호출한다.
     * @param webAction
     * @param svcName
     * @param request
     * @param response
     * @return
     * @throws ServletException
     */
    protected WebActionForward processWebAction(WebAction webAction,
            HttpServletRequest request,	HttpServletResponse response, Map<String,Object> diagMap) throws Throwable {
    	
    	diagMap.put("diag.web.action.method","execute");
        
    	return webAction.execute(request,response);
    }

    /**
     * WebMultiAction에 대한 처리를 수행한다.
     * 주어진 WebMultiAction 서비스 객체의 메소드를 호출하며,
     * 호출할 메소드 이름은 WebMultiAction 객체의 getMethodname()을 호출하여 얻어온다.
     * @param multiAction
     * @param svcName
     * @param request
     * @param response
     * @return
     * @throws ServletException
     */
    protected WebActionForward processWebMultiAction(WebMultiAction multiAction,
            HttpServletRequest request, HttpServletResponse response, Map<String,Object> diagMap) throws Throwable {

        WebActionForward forward = null;

        // multi action 객체로부터 호출할 Method를 얻어온다. (method name이 null 이면 execute() 메소드 실행)
        String methodName = StringHelper.null2string(multiAction.getMethodName(request),"execute");
        diagMap.put("diag.web.action.method",methodName);
        
        // method 명에 해당되는 메소드 객체를 찾는다.
        Method method = null; 
        try {
        	method = multiAction.getClass().getMethod(methodName,argumentTypes);
        } catch (NoSuchMethodException ex) {
        	// 지정된 메소드가 없으면 execute 를 실행한다.
        	method = multiAction.getClass().getMethod("execute",argumentTypes);
        }

        // method를 호출한다.
        Object args[] = {request, response};
        try {
            forward = (WebActionForward)method.invoke(multiAction,args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (Throwable e) {
            throw e;
        }

        return forward;
    }

    /**
     * request URI로 부터 호출할 WebAction 객체의 서비스 명을 찾는다.
     * @param uri
     * @return
     */
    private String getServiceName(String uri) {
        String svcName = null;

        // Exact Match 되는 서비스 명을 찾는다.
        int lastIdx = uri.lastIndexOf('/');
        if ( lastIdx >= 0 ) {
            svcName = uri.substring(lastIdx+1);
        } 
        else {
            svcName = uri;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Request URI : "+uri+", Service Name : "+svcName);
        }

        return svcName;
    }
    
    /**
     * WebActionDispatcher, Framework 및 System 설정 내역을 출력한다.
     * @param response
     * @throws IOException
     */
    protected void showConfiguration(HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<p>WebActionDispatcher Configurations<br>");
        out.println("<ul>");
        out.println("<li> Class : " + this.getClass().getName());
        out.println("<li> Attached File Size Limit : " + attachFileSizeLimit);
        out.println("</ul></p>");

        out.println("<p>Framework Configurations<br>");

        Set<Object> keys = null;
        TreeSet<Object> sortedKeys = null;
        String key = null;
        String value = null;
        try {
            Configurator config = ConfiguratorFactory.getConfigurator();
            out.println("<ul>");

            keys = config.getKeySet();
            sortedKeys = new TreeSet<Object>(keys);

            Iterator<Object> itor = sortedKeys.iterator();

            while(itor.hasNext()) {
                key = (String)itor.next();
                if (key.startsWith("s2adapi.")) {
                    value = config.getString(key,"");
                    out.print("<li>");
                    out.print(key);
                    out.print("=");
                    out.println(value);
                }
            }
            out.println("</ul></p>");

        } catch (ConfiguratorException e) {
            out.println("Configurator Exception : " + e.toString());
        }

        Properties sysProps = System.getProperties();
        out.println("<p>System Properties<br>");
        out.println("<ul>");

        keys = sysProps.keySet();
        sortedKeys = new TreeSet<Object>(keys);

        Iterator<Object> itor = sortedKeys.iterator();

        while(itor.hasNext()) {
            key = (String)itor.next();
            value = sysProps.getProperty(key);
            out.print("<li>");
            out.print(key);
            out.print("=");
            out.println(value);
        }
        out.println("</ul></p>");

        Map<String,String> envMap = System.getenv();
        out.println("<p>Environment Variables<br>");
        out.println("<ul>");

        Set<String> envKeys = envMap.keySet();
        TreeSet<String> envSortedKeys = new TreeSet<String>(envKeys);

        Iterator<String> envItor = envSortedKeys.iterator();

        while(envItor.hasNext()) {
            key = envItor.next();
            value = envMap.get(key);
            out.print("<li>");
            out.print(key);
            out.print("=");
            out.println(value);
        }
        out.println("</ul></p>");
    }
}
