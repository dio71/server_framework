package s2.adapi.framework.container;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import s2.adapi.framework.util.StringHelper;
import s2.adapi.framework.util.ValueObjectUtil;
import s2.adapi.framework.vo.ValueObject;
import s2.adapi.framework.vo.ValueObjectAssembler;
import s2.adapi.framework.web.action.AbstractWebAction;
import s2.adapi.framework.web.action.WebActionForward;

/**
 * 현재 서비스 상태를 조회할 수 있는 기능을 제공하는 WebAction 구현 클래스.
 * @author 김형도
 * @since 5.0
 */
public class ServiceWebAction extends AbstractWebAction {

	public static final String NAME_PARAM = "name";
	
	public WebActionForward execute(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		String svcName = StringHelper.null2void(request.getParameter(NAME_PARAM));
		
		if (StringHelper.isNull(svcName)) {
			// show all service list
			ValueObject svcVO = getServiceList();
			return showServiceList(response,request.getRequestURI(),svcVO);
		} else {
			// show the service info
			ValueObjectAssembler svcInfoVOs = getServiceInfo(svcName);
			return showServiceInfo(response,request.getRequestURI(), svcName,
					svcInfoVOs.get("service"),svcInfoVOs.get("refs"),svcInfoVOs.get("ops"));
		}
	}

	/**
	 * 현재 등록된 전체 서비스 목록을 반환한다.
	 * @return
	 * <ul>
	 * <li> svcname: 서비스 명
	 * <li> infname: 인터페이스 명
	 * <li> classname: 구현 클래스명
	 * <li> instantiated: 현재 서비스 객체의 생성여부(true/false)
	 * </ul>
	 */
	private ValueObject getServiceList() {
		ValueObject svcVO = new ValueObject();
		try {
			ServiceContainer svcContainer = getWebApplicationContext().getServiceContainer();
			
			ServiceInfo[] svcInfo = svcContainer.getServiceInfo();
			
			for(int i=0;i<svcInfo.length;i++) {
				svcVO.set(i,"svcname",svcInfo[i].getName());
				svcVO.set(i,"infname",svcInfo[i].getInterfaceName());
				svcVO.set(i,"classname", svcInfo[i].getClassName());
				svcVO.set(i,"instantiated",svcInfo[i].isInstantiated());
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return svcVO;
	}
	
	/**
	 * 주어진 이름의 서비스의 현재 정보를 반환한다.
	 * @param svcName
	 * @return
	 * <ul> service
	 * <li> svcname: 서비스 명
	 * <li> infname: 인터페이스 명
	 * <li> classname: 구현 클래스명
	 * <li> instantiated: 현재 서비스 객체의 생성여부(true/false)
	 * </ul>
	 * <ul> refs
	 * <li> ref: 참조하는 서비스 명
	 * </ul>
	 */
	private ValueObjectAssembler getServiceInfo(String svcName) {
		ValueObject refVO = new ValueObject(); // 해당 서비스가 사용하는 서비스 목록
		ValueObject svcVO = new ValueObject();
		
		ValueObjectAssembler svcInfoVOs = new ValueObjectAssembler();
		svcInfoVOs.set("service", svcVO);
		svcInfoVOs.set("refs", refVO);
		
		try {
			ServiceContainer svcContainer = getWebApplicationContext().getServiceContainer();
			
			// 해당 서비스 정보 가져오기
			ServiceInfo svcInfo = svcContainer.getServiceInfo(svcName);
			if (svcInfo != null) {
				svcVO.set("svcname",svcInfo.getName());
				svcVO.set("infname",svcInfo.getInterfaceName());
				svcVO.set("classname", svcInfo.getClassName());
				svcVO.set("instantiated",svcInfo.isInstantiated());
				svcVO.set("from", svcInfo.getFromInfo());
				
				// 사용하는 서비스 목록 가져오기
				List<String> refs = svcInfo.getReferences();
				for(int i=0;i<refs.size();i++) {
					refVO.set(i,"ref",refs.get(i));
				}
				
				// 오퍼레이션 목록 가져오기
				String className = svcInfo.getInterfaceName();
				if (StringHelper.isNull(className)) {
					className = svcInfo.getClassName();
				}
				
				svcInfoVOs.set("ops", getServiceOperations(className));
			}
			
		} 
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return svcInfoVOs;
	}
	
	private ValueObject getServiceOperations(String className) {
		ValueObject opVO = new ValueObject();
		try {
			//Class<?> svcClass = this.getClass().getClassLoader().loadClass(className);
			Class<?> svcClass = getWebApplicationContext().getServiceContainer().getClassLoader().loadClass(className);
			Method[] methods = svcClass.getMethods();
			for(int i=0;i<methods.length;i++) {
				opVO.set(i,"op", methods[i].toString());
			}
		} 
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return opVO;
	}
	/**
	 * 서비스 전체 목록을 출력하는 기능을 제공한다. 출력 모양을 변경하고자 한다면 아래 메소드를 상속받아 재정의한다.
	 * @param response
	 * @param requestURI
	 * @param svcVO
	 * @throws IOException
	 */
	protected WebActionForward showServiceList(HttpServletResponse response, String requestURI, ValueObject svcVO)
			throws IOException {
		PrintWriter out = response.getWriter();
		
		ValueObjectUtil.sort(svcVO, "svcname");
		
		int instantiatedCount = 0;
		for(int i=0;i<svcVO.size();i++) {
			if (svcVO.getBoolean(i,"instantiated")) {
				instantiatedCount++;
			}
		}
		
		out.print("<p>! Total ");
		out.print(svcVO.size());
		out.print(" services (");
		out.print(instantiatedCount);
		out.println(" running services.)</p>");
		
		out.print("<ul>");
		for(int i=0;i<svcVO.size();i++) {
			out.print("<li><a href=\"");
			out.print(requestURI);
			out.print("?"+NAME_PARAM+"=");
			out.print(svcVO.get(i,"svcname"));
			out.print("\">");
			out.print(svcVO.get(i,"svcname"));
			out.println("</a>");
			if (svcVO.getBoolean(i,"instantiated")) {
				out.println(" (*)");
			}
		}
		out.print("</ul>");
		
		return null;
	}
	
	/**
	 * 특정 서비스의 정보를 출력하는 기능을 제공한다. 출력 모양을 변경하고자 한다면 아래 메소드를 상속받아 재정의한다.
	 * @param response
	 * @param requestURI
	 * @param svcVO 서비스 정보를 담은 VO
	 * @param refVO 참조하는 서비스 목록을 담은 VO
	 * @throws IOException
	 */
	protected WebActionForward showServiceInfo(HttpServletResponse response, String requestURI, 
			String svcName, ValueObject svcVO, ValueObject refVO, ValueObject opVO)
			throws IOException {
		PrintWriter out = response.getWriter();
		
		if (svcVO == null || svcVO.size() == 0) {
			out.print("<p> No service for ");
			out.print(svcName);
			out.println("</p>");
		} else {
			out.print("<p>");
			out.print(svcVO.get("svcname"));
			out.println("</p>");
			
			// service info
			out.print("<ul>");
			//out.println("<li> Service Name: ");
			//out.print(svcVO.get("svcname"));
			out.println("<li> Interface Name: ");
			out.print(svcVO.get("infname"));
			out.println("<li> Implementation: ");
			out.print(svcVO.get("classname"));
			out.println("<li> Instantiated: ");
			out.println(svcVO.get("instantiated"));
			out.println("<li> Service from: ");
			out.println(svcVO.get("from"));
			
			// operation 명에서 full package 부분을 제거하기 위하여 사용
			String infName = svcVO.getString("infname",null);
			if (StringHelper.isNull(infName)) {
				infName = svcVO.getString("classname","");
			}
			infName = infName + ".";
			
			if (refVO.size() > 0) {
	
				// reference info
				out.print("<li> Reference");
				out.println("<ul>");
				for(int i=0;i<refVO.size();i++) {
					out.print("<li><a href=\"");
					out.print(requestURI);
					out.print("?"+NAME_PARAM+"=");
					out.print(refVO.get(i,"ref"));
					out.print("\">");
					out.print(refVO.get(i,"ref"));
					out.println("</a>");
				}
				out.print("</ul>");
			}
			if (opVO.size() > 0) {
				
				// reference info
				out.print("<li> Operations");
				out.println("<ul>");
				
				String opInfo = null;
				for(int i=0;i<opVO.size();i++) {
					out.print("<li>");
					opInfo = opVO.getString(i,"op","");
					opInfo = opInfo.replace("s2.adapi.framework.vo.", "");
					opInfo = opInfo.replace("s2.adapi.framework.exception.", "");
					opInfo = opInfo.replace(infName, "");
					opInfo = opInfo.replace("public abstract ", "");
					opInfo = opInfo.replace("java.lang.", "");
					out.println(opInfo);
				}
				out.print("</ul>");
			}
			out.print("</ul>");
		}
		
		return null;
	}
}
