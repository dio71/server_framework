package s2.adapi.framework.web.action;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import s2.adapi.framework.vo.ValueObjectAssembler;

/**
 * WebAction 객체가 생성한 모델 데이터와 포워드할 View의 URL 정보를 가진다.
 * @author 김형도
 * @since 4.0
 */
public class WebActionForward {

	public static final String FORWARD_TYPE = "forward:";
	public static final String REDIRECT_TYPE = "redirect:";
	
	private String name = null;
	private String url = null;
	private boolean isForward = true;
	private Map<String,Object> modelMap = null;
	
	public WebActionForward(String name, String url, boolean forward) {
		this.name = name;
		this.url = url;
		this.isForward = forward;
	}
	
	public WebActionForward(String name, String url) {
		this.name = name;
		this.url = url;
		this.isForward = true;
	}
	
    public String getViewURL() {
    	return url;
    }
    
    public String getViewName() {
    	return name;
    }
    
    public boolean isForward() {
    	return this.isForward;
    }
    
    /**
     * Model Data를 추가한다. 추가된 Model Data는 포워드된 View에서
     * <code>request.getAttribute(modelName)</code> 을 사용하여 다시 참조할 수 있다.
     * @param mname
     * @param data
     */
    public WebActionForward addModel(String modelName, Object modelData) {
    	if ( modelMap == null ) {
    		modelMap = new HashMap<String,Object>();
    	}
    	
    	modelMap.put(modelName,modelData);
    	
    	return this;
    }
    
    /**
     * ValueObjectAssembler에 들어 있는 ValueObject 들을 각각 Model Data로 추가한다.
     * Model Data로 추가된 ValueObject들은 포워드된 View에서 각각 <code>request.getAttribute()</code>를
     * 사용하여 다시 참조할 수 있다.
     * @param vos
     * @return
     */
    public WebActionForward addModel(ValueObjectAssembler vos) {
    	if ( vos == null || vos.size() <= 0 ) {
    		return this;
    	}
    	
    	if ( modelMap == null ) {
    		modelMap = new HashMap<String,Object>();
    	}
    	
    	Iterator<String> itor = vos.getKeys().iterator();
    	while(itor.hasNext()) {
    		String name = itor.next();
    		addModel(name, vos.get(name));
    	}
    	
    	return this;
    }
    
    /**
     * Model Data를 얻어온다.
     * @param modelName
     * @return
     */
    public Object getModel(String modelName) {
    	if ( modelMap != null ) {
    		return modelMap.get(modelName);
    	} else {
    		return null;
    	}
    }
    
    /**
     * Model Data의 이름 목록을 얻어온다.
     * @return
     */
    public Set<String> getModelNames() {
    	if ( modelMap == null ) {
    		return new HashSet<String>();
    	} else {
    		return modelMap.keySet();
    	}
    }
    
    /**
     * 설정된 View의 URL로 forward 또는 redirect 한다.
     * @param request
     * @param response
     * @return
     * @throws ServletException
     * @throws IOException
     */
    public void sendView(HttpServletRequest request, HttpServletResponse response) 
    		throws ServletException, IOException {
    	if (isForward()) {
    		processForward(request,response);
    	} else {
    		processRedirect(response);
    	}
    }
    
    /**
     * 설정된 View의 URL로 forward한다. Model 데이터는 request의 setAttribute()를 사용하여 저장한다.
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private void processForward(HttpServletRequest request, HttpServletResponse response) 
    		throws ServletException, IOException {
    	
		// Model data를 request의 setAttribute()를 사용하여 저장
		Iterator<String> itor = getModelNames().iterator();
		String name = null;
		while(itor.hasNext()) {
			name = itor.next();
			request.setAttribute(name,getModel(name));
		}
		
		request.getRequestDispatcher(getViewURL()).forward(request,response);
    }
    
    /**
     * 설정된 View의 URL로 redirect한다. Model 데이터는 URL의 query 문자열로 지정한다.
     * @param response
     * @throws IOException
     */
    private void processRedirect(HttpServletResponse response) 
    		throws IOException {
		// Model data를 request의 query string으로 포함시킨다.
		Iterator<String> itor = getModelNames().iterator();
		StringBuffer redirectUrl = new StringBuffer();
		
		redirectUrl.append(getViewURL());
		boolean isFirst = (getViewURL().indexOf('?') < 0);
		while(itor.hasNext()) {
			if (isFirst) {
				redirectUrl.append('?');
				isFirst = false;
			} else {
				redirectUrl.append('&');
			}
			
			String name = itor.next();
			Object value = getModel(name);
			
			redirectUrl.append(encodeUrl(name));
			if (value != null) {
				redirectUrl.append('=').append(encodeUrl(value.toString()));
			}
		}
		
		response.sendRedirect(redirectUrl.toString());
    }
    
    /**
     * 문자열을 URL encoding한다.
     * @param url
     * @return
     */
    private String encodeUrl(String url) {
    	String encoded = null;
    	try {
			encoded = URLEncoder.encode(url,"utf-8");
		} catch (UnsupportedEncodingException e) {
			encoded = url;
		}
		
		return encoded;
    }
}
