package s2.adapi.framework.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import s2.adapi.framework.container.NameAwareService;

public abstract class RestfulMultiWebAction extends AbstractWebAction implements WebMultiAction, NameAwareService {

    protected String serviceName = null;
    
    public void setServiceName(String name) {
        //System.out.println("set servicename = "+ name);
        serviceName = name;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    // Restful API 에서 action 및 그 이후 부분들을 array 로 담아 반환한다.
    protected String[] getPayload(HttpServletRequest request) {
        String path = request.getServletContext().getContextPath();
        String uri = request.getRequestURI();
        
        String actionStr = uri.substring(path.length() + serviceName.length() - 1);
        
        return actionStr.split("\\/");
    }
    
    @Override
    public String getMethodName(HttpServletRequest request) {
        String path = request.getServletContext().getContextPath();
        String uri = request.getRequestURI();
        
        if (serviceName.endsWith("*")) {
            String actionStr = uri.substring(path.length() + serviceName.length() - 1);
            return actionStr.split("\\/")[0];
        }
        else {
            return null;
        }
    }

    @Override
    public WebActionForward execute(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        throw new Exception("execute() not implemented.");
    }

}
