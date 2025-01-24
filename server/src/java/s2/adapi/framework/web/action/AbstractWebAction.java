package s2.adapi.framework.web.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.util.StringHelper;
import s2.adapi.framework.vo.ValueObject;
import s2.adapi.framework.web.upload.FormFile;
import s2.adapi.framework.web.upload.MultipartRequestWrapper;

/**
 * WebAction을 구현시 필요한 WebActionForward 설정 및 조회 기능을 제공하는 클래스이다.
 * WebAction은 다음과 같이 구현한다.
 * <ol>
 *   <li> 액션 클래스를 구현한다. 이때 AbstractWebAction을 extends 해야한다.
 *   <li> 서비스 구성파일 작성한다. 서비스의 "interface"는 "s2.adapi.framework.web.action.WebAction"으로 설정하고, 
 *   "forward"와 필요시 "prefix", "suffix" property를 설정하도록 한다.
 * </ol>
 * 작성 예)
 * <pre>
 * 
 * public class TestWebActionImpl extends AbstractWebAction {
 *
 *	private static final Log log = LogFactory.getLog(TestWebActionImpl);
 *	
 *	public WebActionForward execute(HttpServletRequest request,
 *			HttpServletResponse response) throws Exception {
 *		log.error("This is TestWebAction...");
 *		return createForward("default").addModel("code","execute");
 *	}
 * }
 * 
 *     &lt;service name="test.do"
 *             interface="s2.adapi.framework.web.action.WebAction"
 *             class="${package}.TestWebActionImpl"
 *             interceptor="system.proxy"
 *             singleton="true"&gt; 
 *         &lt;property name="forward" value="default:=/testweb/jsp/test.jsp"/&gt;
 *         &lt;property name="prefix" value="/webapps/tmp"/&gt;
 *     &lt;/service&gt;
 * </pre>
 * @author 김형도
 * @since 4.0
 */

public abstract class AbstractWebAction implements WebAction {

	protected static Logger log = LoggerFactory.getLogger(AbstractWebAction.class);
	
	/**
	 * 본 WebAction이 수행되는 WebActionContext 객체를 담아 놓는다.
	 */
	protected WebApplicationContext context = null;
	
	/**
	 * 설정된 Forward 정보를 저장해 놓기 위한 Map 객체
	 */
	private Map<String, WebActionForward> forwardMap = new HashMap<String, WebActionForward>();
	
	/**
	 * Forward 정보를 찾을 수 없을 경우 WebActionForward를 생성하기 위한 정보
	 */
	private String forwardPrefix = null;
	private String forwardSuffix = null;
	
	abstract public WebActionForward execute(HttpServletRequest request,
			HttpServletResponse response) throws Exception;

	public void setWebApplicationContext(WebApplicationContext ctx) {
		context = ctx;
	}
	
	public WebApplicationContext getWebApplicationContext() {
		return context;
	}
	
	public ServletContext getServletContext() {
		return context.getServletContext();
	}
	
	protected ValueObject getRequestParamAsVO(HttpServletRequest request) {
		ValueObject paramVO = new ValueObject();
		
		Map<String,String[]> params = request.getParameterMap();

		for(String name: params.keySet()) {
			addParameters(name, params.get(name), paramVO);
		}
		
		return paramVO;
	}
	
	protected ValueObject getAttachFileAsVO(HttpServletRequest request, String name) {
		ValueObject fileVO = new ValueObject();
		Map<String,FormFile[]> fileMap = MultipartRequestWrapper.getFileMap(request);
		if (fileMap != null) {
			FormFile[] files = fileMap.get(name);
			if (files != null) {
				for(int i=0; i<files.length; i++) {
					fileVO.set(i, "type", files[i].getContentType());
					fileVO.set(i, "name", files[i].getFileName());
					fileVO.set(i, "size", files[i].getFileSize());
					fileVO.set("data", files[i].getFileData());
				}
			}
		}
		
		return fileVO;
	}
	
	private void addParameters(String name, String[] values, ValueObject paramVO) {
		for(int i=0; i<values.length; i++) {
			paramVO.set(i, name, values[i]);
		}
	}
	
	public void showConfiguration(HttpServletResponse response)
			throws IOException {
		PrintWriter out = response.getWriter();
		out.print("<p>");
		out.print(this.getClass().getName());
		out.println(" Configurations<br>");
		
		out.println("<ul>");
		out.println("<li> Forward List : <ul>");
		Set<String> keys = forwardMap.keySet();
		Iterator<String> itor = keys.iterator();
		String key = null;
		WebActionForward forward = null;
		while(itor.hasNext()) {
			key = itor.next();
			forward = forwardMap.get(key);
			out.print("<li>");
			out.print(key);
			out.print("=");
			out.println(forward.getViewURL());
		}
		out.println("</ul>");
		out.println("<li> Forward Prefix : " + forwardPrefix);
		out.println("<li> Forward Suffix : " + forwardSuffix);
		out.println("</ul></p>");
	}
	
	/**
	 * WebActionForward를 추가한다.
	 * @param forwardString foward를 정의하는 문자열이다. 문자열 형식은 다음과 같아야 한다.
	 * forward명:=[forward:|redirect:]URL경로
	 */
	public void setForward(String forwardString) {
		if ( StringHelper.isNull(forwardString) ) {
			return;
		}
		
		List<String> split = StringHelper.split(forwardString,":=");
		if ( split.size() != 2 ) {
			if ( log.isErrorEnabled() ) {
				log.error("forward string is invalid format. ["+forwardString+"]");
			}
			return;
		}
		String forwardName = split.get(0);
		String forwardValue = split.get(1);
		WebActionForward forward = null;
		if ( forwardValue.startsWith(WebActionForward.FORWARD_TYPE)) {
			forward = new WebActionForward(forwardName,
					forwardValue.substring(WebActionForward.FORWARD_TYPE.length()),true);
		} else if ( forwardValue.startsWith(WebActionForward.REDIRECT_TYPE)) {
			forward = new WebActionForward(forwardName,
					forwardValue.substring(WebActionForward.REDIRECT_TYPE.length()),false);
		} else {
			forward = new WebActionForward(forwardName,forwardValue,true);
		}
		
		synchronized(forwardMap) {
			forwardMap.put(forwardName,forward);
		}
	}
	
	/**
	 * WebActionForward를 생성할 때 사용할 prefix 문자열을 지정한다.
	 * @param prefix
	 */
	public void setPrefix(String prefix) {
		forwardPrefix = prefix;
	}
	
	/**
	 * WebActionForward를 생성할 때 사용할 suffix 문자열을 지정한다.
	 * @param suffix
	 */
	public void setSuffix(String suffix) {
		forwardSuffix = suffix;
	}
	
	/**
	 * 주어진 이름에 해당되는 WebActionForward를 객체를 생성한다.
	 * 해당되는 이름을 찾지 못한 경우에는 이름의 앞뒤에 각각 prefix와 suffix 문자열을 붙여서 리턴한다.
	 * 주어진 이름이 null이거나 비어 있는 문자열인 경우에는 null을 반환한다.
	 * @param name
	 * @return
	 */
	public WebActionForward createForward(String name) {
		String forwardURL = null;
		boolean isForward = true;
		
		if ( StringHelper.isNull(name)) {
			return null;
		}
		
		// 기존에 등록되어 있는 WebActionForward를 찾는다.
		WebActionForward forward = forwardMap.get(name);
		
		if ( forward == null ) {
			// 기존에 없으면 포워드 명을 포워드 URL로 사용한다.
			forwardURL = name;
			isForward = true;
		} else {
			// 등록되어 있으면 WebActionForward 생성을 위한 정보를 
			// 등록되어 있는 WebActionForward 객체에서 얻어온다.
			forwardURL = forward.getViewURL();
			isForward = forward.isForward();
		}
		
		// Prefix와 suffix를 추가한다.
		if ( !StringHelper.isNull(forwardPrefix) ) {
			forwardURL = forwardPrefix + forwardURL;
		}
		if ( !StringHelper.isNull(forwardSuffix) ) {
			forwardURL = forwardURL + forwardSuffix;
		}
		
		return new WebActionForward(name,forwardURL,isForward);
	}
	
	// TODO : setMessage(), setError()
}
