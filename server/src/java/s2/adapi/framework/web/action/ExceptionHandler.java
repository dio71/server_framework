package s2.adapi.framework.web.action;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * WebActionDispatcher에서 WebAction 처리 중 Exception이 발생했을 경우
 * 이를 처리하기 위한 인터페이스이다.
 * 이를 구현한 후 WebActionDispatcher 서블릿 등록시 <code>s2adapi.web.exception.handler</code>를 사용하여
 * 실제 구현 클래스명을 서블릿 초기화 파라메터로 등록한다.
 * 
 * @author 김형도
 */
public interface ExceptionHandler {

	/**
	 * Throwable 발생 시 이를 처리하기 위한 로직을 구현한다.
	 * @param request
	 * @param response
	 * @param thr
	 * @throws ServletException
	 * @throws IOException
	 */
	public void handle(Throwable thr, 
			HttpServletRequest request, 
			HttpServletResponse response) throws ServletException, IOException;
}
