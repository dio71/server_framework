package s2.adapi.framework.web.upload;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Multipart Request를 일반 HttpServletRequest 처럼 사용하기 위한 클래스이다.
 * @author 김형도
 * @since 4.0
 */
public class MultipartRequestWrapper implements HttpServletRequest {

	private static Logger log = LoggerFactory.getLogger(MultipartRequestWrapper.class);
	
	protected String characterEncoding = null;
	
	/**
	 * multipart request용 parameter 저장을 위한 Map
	 */
	protected Map<String,String[]> parameters;
	
	/**
	 * multipart request내 파일 데이터 저장을 위한 Map
	 */
	protected Map<String,FormFile[]> files;
	
	/**
	 * wrapping되는 HttpServletRequest 객체
	 */
	protected HttpServletRequest request;
	
	/**
	 * HttpServletReqeust를 감싸는 MultipartRequestWrapper 객체를 생성한다.
	 * encoding 값을 지정하지 않으면 "euc-kr"을 사용한다.
	 * @param request
	 * @param encoding
	 */
	public MultipartRequestWrapper(HttpServletRequest request, String encoding) {
		this.request = request;
		this.parameters = new HashMap<String,String[]>();
		this.files = new HashMap<String,FormFile[]>();
		
		if (encoding == null) {
			characterEncoding = "euc-kr";
		} else {
			characterEncoding = encoding;
		}
		
		try {
			this.request.setCharacterEncoding(characterEncoding);
		} catch (UnsupportedEncodingException e) {
			if (log.isErrorEnabled()) {
				log.error("Unsupported encoding["+characterEncoding+"] used for Multipart-request.");
			}
		}
		
		// request 내의 parameter들을 뽑아내어 Map에 저장
        Enumeration<?> baseParams = request.getParameterNames();
        String[] baseParamValues = null;
        String baseParamName = null;
        while (baseParams.hasMoreElements()) {
        	baseParamName = (String)baseParams.nextElement();
        	baseParamValues = request.getParameterValues(baseParamName);
        	if ( baseParamValues != null && baseParamValues.length > 0 ) {
        		parameters.put(baseParamName, baseParamValues);
        	}
        }
	}
	
	public MultipartRequestWrapper(HttpServletRequest request) {
		this(request,null);
	}
	
	public static boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        if ((contentType != null) &&
            contentType.startsWith("multipart/form-data")) {
            return true;
        } else {
            return false;
        }
	}
	
	/**
	 * 내부 HttpServletRequest 객체를 반환한다.
	 * @return
	 */
	public HttpServletRequest getRequest() {
		return request;
	}
	/**
	 * multipart request용 parameter를 세팅한다. 내부 HttpServletRequest 객체의 
	 * parameter와는 별개이다. 
	 * @param name
	 * @param value
	 */
	public void setParameter(String name, String value) {
        String[] mValue = parameters.get(name);
        if (mValue == null) {
            mValue = new String[0];
        }
        String[] newValue = new String[mValue.length + 1];
        System.arraycopy(mValue, 0, newValue, 0, mValue.length);
        newValue[mValue.length] = value;
        
        parameters.put(name, newValue);
	}
	
	public String getAuthType() {
		return request.getAuthType();
	}

	public String getContextPath() {
		return request.getContextPath();
	}

	public Cookie[] getCookies() {
		return request.getCookies();
	}

	public long getDateHeader(String name) {
		return request.getDateHeader(name);
	}

	public String getHeader(String name) {
		return request.getHeader(name);
	}

	public Enumeration<String> getHeaderNames() {
		return request.getHeaderNames();
	}

	public Enumeration<String> getHeaders(String name) {
		return request.getHeaders(name);
	}

	public int getIntHeader(String name) {
		return request.getIntHeader(name);
	}

	public String getMethod() {
		return request.getMethod();
	}

	public String getPathInfo() {
		return request.getPathInfo();
	}

	public String getPathTranslated() {
		return request.getPathTranslated();
	}

	public String getQueryString() {
		return request.getQueryString();
	}

	public String getRemoteUser() {
		return request.getRemoteUser();
	}

	public String getRequestURI() {
		return request.getRequestURI();
	}

	public StringBuffer getRequestURL() {
		return request.getRequestURL();
	}

	public String getRequestedSessionId() {
		return request.getRequestedSessionId();
	}

	public String getServletPath() {
		return request.getServletPath();
	}

	public HttpSession getSession() {
		return request.getSession();
	}

	public HttpSession getSession(boolean create) {
		return request.getSession(create);
	}

	public Principal getUserPrincipal() {
		return request.getUserPrincipal();
	}

	public boolean isRequestedSessionIdFromCookie() {
		return request.isRequestedSessionIdFromCookie();
	}

	public boolean isRequestedSessionIdFromURL() {
		return request.isRequestedSessionIdFromURL();
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public boolean isRequestedSessionIdFromUrl() {
		return request.isRequestedSessionIdFromURL();
	}

	public boolean isRequestedSessionIdValid() {
		return request.isRequestedSessionIdValid();
	}

	public boolean isUserInRole(String user) {
		return request.isUserInRole(user);
	}

	public Object getAttribute(String name) {
		return request.getAttribute(name);
	}

	public Enumeration<String> getAttributeNames() {
		return request.getAttributeNames();
	}

	public String getCharacterEncoding() {
		return request.getCharacterEncoding();
	}

	public int getContentLength() {
		return request.getContentLength();
	}

	public String getContentType() {
		return request.getContentType();
	}

	public ServletInputStream getInputStream() throws IOException {
		return request.getInputStream();
	}

	public Locale getLocale() {
		return request.getLocale();
	}

	public Enumeration<Locale> getLocales() {
		return request.getLocales();
	}

	public String getParameter(String name) {
        String value = null;
        String[] mValue = parameters.get(name);
        if ((mValue != null) && (mValue.length > 0)) {
            value = mValue[0];
        }

        return value;
	}

	public Map<String,String[]> getParameterMap() {
        return parameters;
	}

	/**
	 * parameter들의 이름들을 반환한다. 
	 * 반환되는 파라메터 이름들은 내부 HttpServletRequest 객체의 파라메터 이름들과
	 * setParameter()로 설정된 파라메터 이름들을 모두 포함한다.
	 */
	public Enumeration<String> getParameterNames() {
        Set<String> multipartParams = parameters.keySet();
        return Collections.enumeration(multipartParams);
	}

	/**
	 * 내부 HttpServletRequest 객체와 setParameter()로 설정된 parameter들을 모두 포함하여 리턴한다.
	 */
	public String[] getParameterValues(String name) {
        String[] value = parameters.get(name);

        return value;
	}

	public String getProtocol() {
		return request.getProtocol();
	}

	public BufferedReader getReader() throws IOException {
		return request.getReader();
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public String getRealPath(String path) {
		return request.getRealPath(path);
	}

	public String getRemoteAddr() {
		return request.getRemoteAddr();
	}

	public String getRemoteHost() {
		return request.getRemoteHost();
	}

	public RequestDispatcher getRequestDispatcher(String path) {
		return request.getRequestDispatcher(path);
	}

	public String getScheme() {
		return request.getScheme();
	}

	public String getServerName() {
		return request.getServerName();
	}

	public int getServerPort() {
		return request.getServerPort();
	}

	public boolean isSecure() {
		return request.isSecure();
	}

	public void removeAttribute(String name) {
		request.removeAttribute(name);
	}

	public void setAttribute(String name, Object obj) {
		request.setAttribute(name, obj);
	}

	public void setCharacterEncoding(String encoding)
			throws UnsupportedEncodingException {
		request.setCharacterEncoding(encoding);
	}

	// Servlet 2.4 method, not implemented
    public String getLocalAddr() {
        return null;
    }

    public String getLocalName() {
        return null;
    }

    public int getLocalPort() {
        return 0;
    }
    
    public int getRemotePort() {
        return 0;
    }
    
    /**
     * <p>
     * multipart내의 form field와 form file 들을 파싱하여 form field는 setParameter()로 저장하고
     * form file은 <code>FileItem[]</code> 형태로 files Map 객체에 저장한다.
     * upload 가능한 최대 파일크기는 100MB 이며, 
     * 파일 업로드시 임시 디렉토리는 "java.io.tmpdir" 시스템 프로퍼티 값을 사용한다.
     * </p>
     * @return multipart의 파싱이 완료된 MultipartRequestWrapper 자기 자신
     */
    public MultipartRequestWrapper parseMultipart() 
			throws ServletException 
	{
    	return parseMultipart((ServletContext)null, 100*1024*1024);
	}
    
    /**
     * <p>
     * multipart내의 form field와 form file 들을 파싱하여 form field는 setParameter()로 저장하고
     * form file은 <code>FileItem[]</code> 형태로 files Map 객체에 저장한다.
     * upload 가능한 최대 파일크기는 sizeLimit 파라메터로 지정한다. 
     * 파일 업로드시 임시 디렉토리는 "java.io.tmpdir" 시스템 프로퍼티 값을 사용한다.
     * </p>
     * @param sizeLimit 업로드 가능한 파일의 최대 크기(bytes)
     * @return multipart의 파싱이 완료된 MultipartRequestWrapper 자기 자신
     */
    public MultipartRequestWrapper parseMultipart(long sizeLimit) 
    		throws ServletException 
    {
    	return parseMultipart((ServletContext)null, sizeLimit);
    }
	
    /**
     * <p>
     * multipart내의 form field와 form file 들을 파싱하여 form field는 setParameter()로 저장하고
     * form file은 <code>FileItem[]</code> 형태로 files Map 객체에 저장한다.
     * upload 가능한 최대 파일크기는 sizeLimit 파라메터로 지정한다. 
     * </p>
     * <p>
     * 파일 업로드시 임시 디렉토리는 ServetContext의 "javax.servlet.context.tempdir" 속성값을 사용한다.
     * 주어진 ServetContext 객체가 null 인 경우에는 "java.io.tmpdir" 시스템 프로퍼티 값을 사용한다.
     * </p>
     * @param ServletContext 임시 디렉토리 정보를 얻기 위한 파라메터
     * @param sizeLimit 업로드 가능한 파일의 최대 크기(bytes)
     * @return multipart의 파싱이 완료된 MultipartRequestWrapper 자기 자신
     */
    public MultipartRequestWrapper parseMultipart(ServletContext ctx, long sizeLimit) 
    		throws ServletException 
    {
    	String tempDir = null;
    	if ( ctx != null ) {
    		File tempDirFile = (File)ctx.getAttribute("javax.servlet.context.tempdir");
    		tempDir = tempDirFile.getAbsolutePath();
    	} else {
    		tempDir = System.getProperty("java.io.tmpdir");
    	}
    	return parseMultipart(tempDir, sizeLimit);
    }
    
    /**
     * <p>
     * multipart내의 form field와 form file 들을 파싱하여 form field는 setParameter()로 저장하고
     * form file은 <code>FileItem[]</code> 형태로 files Map 객체에 저장한다.
     * upload 가능한 최대 파일크기는 sizeLimit 파라메터로 지정한다. 
     * </p>
     * <p>
     * 파일 업로드시 임시 디렉토리는 tempDir 파라메터로 명시적으로 지정한다.
     * </p>
     * @param tempDir 임시 디렉토리의 절대 Path
     * @param sizeLimit 업로드 가능한 파일의 최대 크기(bytes)
     * @return multipart의 파싱이 완료된 MultipartRequestWrapper 자기 자신
     */
    public MultipartRequestWrapper parseMultipart(String tempDir, long sizeLimit) 
    		throws ServletException 
    {
    	
    	try {
    		DiskFileItemFactory factory = new DiskFileItemFactory();
    		factory.setRepository(new File(tempDir));
    		factory.setSizeThreshold(10*1024*1024); // 10MB
    		
    		ServletFileUpload upload = new ServletFileUpload(factory);
    		upload.setSizeMax(sizeLimit);
    		List<?> items = upload.parseRequest(request);
    		
    		for(int i=0;i<items.size();i++) {
    			FileItem item = (FileItem)items.get(i);
    			if ( item.isFormField() ) {
    				addParameter(item);
    			} else {
    				addFile(item);
    			}
    		}

    	} catch (SizeLimitExceededException e) {
    		if ( log.isErrorEnabled() ) {
    			log.error("file size exceeded.",e);
    		}
    		throw new ServletException("첨부된 파일이 최대 사이즈("+sizeLimit+")를 초과했습니다.",e);
    	} catch (FileUploadException e) {
    		if ( log.isErrorEnabled() ) {
    			log.error("file upload exception.",e);
    		}
    		throw new ServletException("첨부된 파일 처리 중 오류가 발생했습니다.",e);
    	} catch (Exception e) {
    		if ( log.isErrorEnabled() ) {
    			log.error("fail to parse multipart request.",e);
    		}
    		throw new ServletException("첨부된 파일 처리 중 오류가 발생했습니다.",e);
    	}
    	
    	return this;
    }
    
    /**
     * Multipart request 내 file data가 담겨져 있는 Map 객체를 반환한다. 
     * parseMultipart()를 호출한 후에 사용한다. 
     * @return
     */
    public Map<String,FormFile[]> getFileMap() {
    	return files;
    }
    
    /**
     * 주어진 HttpServletRequest가 MultipartRequestWrapper 인 경우 해당 getFileMap()을 호출하여
     * 그 결과로 얻어진 <code>Map<String,FormFile[]></code> 객체를 반환한다.
     * MultipartRequestWrapper 객체가 아니라면 null을 반환한다.
     * @param request
     * @return
     */
    public static Map<String,FormFile[]> getFileMap(HttpServletRequest request) {
    	Map<String,FormFile[]> fileMap = null;
    	
    	if ( request instanceof MultipartRequestWrapper ) {
    	    fileMap = ((MultipartRequestWrapper)request).getFileMap();	
    	}
    	
    	return fileMap;
    }
    
    private void addParameter(FileItem item) {
    	String name = item.getFieldName();
    	String value = null;
		try {
			value = item.getString(characterEncoding);
		} catch (UnsupportedEncodingException e) {
			value = item.getString();
		}
    	
    	setParameter(name, value);
    }
    
    private void addFile(FileItem item) {
    	FormFile formFile = new FormFile(item);
    	String name = formFile.getFieldName();
    	
    	FormFile[] prevFiles = files.get(name);
    	if ( prevFiles == null ) {
    		prevFiles = new FormFile[0];
    	}
    	
    	FormFile[] newFiles = new FormFile[prevFiles.length + 1];
    	System.arraycopy(prevFiles, 0, newFiles, 0, prevFiles.length);
    	newFiles[prevFiles.length] = formFile;
    	
    	files.put(name, newFiles);
    }

	@Override
	public AsyncContext getAsyncContext() {
		return request.getAsyncContext();
	}

	@Override
	public DispatcherType getDispatcherType() {
		return request.getDispatcherType();
	}

	@Override
	public ServletContext getServletContext() {
		return request.getServletContext();
	}

	@Override
	public boolean isAsyncStarted() {
		return request.isAsyncStarted();
	}

	@Override
	public boolean isAsyncSupported() {
		return request.isAsyncSupported();
	}

	@Override
	public AsyncContext startAsync() {
		return request.startAsync();
	}

	@Override
	public AsyncContext startAsync(ServletRequest req, ServletResponse res) {
		return request.startAsync(req, res);
	}

	@Override
	public boolean authenticate(HttpServletResponse res) throws IOException,
			ServletException {
		return request.authenticate(res);
	}

	@Override
	public Part getPart(String name) throws IOException, IllegalStateException,
			ServletException {
		return request.getPart(name);
	}

	@Override
	public Collection<Part> getParts() throws IOException,
			IllegalStateException, ServletException {
		return request.getParts();
	}

	@Override
	public void login(String username, String password) throws ServletException {
		request.login(username, password);
		
	}

	@Override
	public void logout() throws ServletException {
		request.logout();
		
	}

	@Override
	public long getContentLengthLong() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getContentLengthLong'");
	}

	@Override
	public String changeSessionId() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'changeSessionId'");
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'upgrade'");
	}
    
}
