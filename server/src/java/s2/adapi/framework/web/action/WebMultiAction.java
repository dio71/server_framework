package s2.adapi.framework.web.action;

import javax.servlet.http.HttpServletRequest;

/**
 * 하나의 WebAction 객체에서 여러 request 처리 구현을 위한 WebMultiAction 인터페이스이다.
 * @author 김형도
 * @since 4.0
 */
public interface WebMultiAction extends WebAction {

    /**
     * 주어진 HttpServletRequest의 request parameter 값을 사용하여
     * 호출할 Method 명을 찾아내어 리턴한다.
     * @param request
     * @return 메소드 명
     */
    public String getMethodName(HttpServletRequest request);
}
