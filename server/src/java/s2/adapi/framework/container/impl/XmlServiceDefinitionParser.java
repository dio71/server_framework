package s2.adapi.framework.container.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import s2.adapi.framework.container.ServiceContainerException;
import s2.adapi.framework.container.support.ArgumentHolder;
import s2.adapi.framework.container.support.PropertyHolder;
import s2.adapi.framework.container.support.ServiceDefinition;
import s2.adapi.framework.util.StringHelper;
import s2.adapi.framework.util.SystemHelper;

/**
 * 하나의 서비스 구성파일을 파싱한다. 
 * 다중쓰레드를 지원하지 않으므로 매번 XmlServiceParser 객체를 생성하여 사용해야 한다.
 * 
 * @author 김형도
 * @since 4.0
 */
public class XmlServiceDefinitionParser extends DefaultHandler {

	/**
	 * <p>
	 * 에러나 이벤트와 관련된 각종 메시지를 로깅하기 위한 Log 오브젝트
	 * </p>
	 */
	private static final Logger log = LoggerFactory.getLogger(XmlServiceDefinitionParser.class);

	protected static final String PUBLIC_ID = "-//S2 API//DTD s2adapi Services Config 0.1//EN";

	protected static final String DTD_RESOURCE = "s2/adapi/framework/container/service-config.dtd";

	protected static final String ROOT_NODE = "services";

	protected static final String ATTR_ROOT_MODULE = "module";

	protected static final String ATTR_ROOT_PACKAGE = "package";

	protected static final String ATTR_ROOT_RESOURCE = "resource";

	protected static final String ATTR_ROOT_PREINIT = "pre-init";

	protected static final String SERVICE_NODE = "service";

	protected static final String ATTR_SERVICE_NAME = "name";

	protected static final String ATTR_SERVICE_CLASS = "class";

	protected static final String ATTR_SERVICE_INF = "interface";

	protected static final String ATTR_SERVICE_INTERCEPTOR = "interceptor";

	protected static final String ATTR_SERVICE_INIT_METHOD = "init-method";

	protected static final String ATTR_SERVICE_DESTROY_METHOD = "destroy-method";

	protected static final String ATTR_SERVICE_SINGLETON = "singleton";

	protected static final String ATTR_SERVICE_ACTIVATE = "activate";

	protected static final String ATTR_SERVICE_PREINIT = "pre-init";

	protected static final String ATTR_SERVICE_LOADPRIORITY = "load-priority";
	
	protected static final String PROPERTY_NODE = "property";

	protected static final String ATTR_PROPERTY_NAME = "name";

	protected static final String ATTR_PROPERTY_VALUE = "value";

	protected static final String ATTR_PROPERTY_REF = "ref";

	protected static final String CONSTRUCTOR_NODE = "constructor";

	protected static final String FACTORY_NODE = "factory";

	protected static final String ATTR_FACTORY_CLASS = "class";

	protected static final String ATTR_FACTORY_METHOD = "method";

	protected static final String ATTR_FACTORY_REF = "ref";

	protected static final String ARGUMENT_NODE = "arg";

	protected static final String ATTR_ARGUMENT_TYPE = "type";

	protected static final String ATTR_ARGUMENT_VALUE = "value";

	protected static final String ATTR_ARGUMENT_REF = "ref";

	// 현재 파싱 중인 서비스 구성 파일의 Root 엘리먼트의 속성들
	private String moduleName = null;

	private String packageName = null;

	private String resourceName = null;

	private String preInit = null;

	// 현재 파싱 중인 서비스 정의 객체
	private ServiceDefinition curSvcDef = null;

	// 파싱된 서비스 정의 객체를 담아 놓을 리스트 객체
	private List<ServiceDefinition> svcDefs = null;

	/**
	 * 서비스 구성 파일을 파싱하여 파싱된 ServiceDefinition 객체들을 List에 담아 리턴한다.
	 * 
	 * @param is
	 * @param svcRegistry
	 * @return
	 * @throws ServiceContainerException
	 */
	public synchronized List<ServiceDefinition> parse(InputStream is) 
			throws SAXException {

		svcDefs = new ArrayList<ServiceDefinition>();

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);

		try {
			SAXParser parser = factory.newSAXParser();

			parser.parse(is, this);
		} catch (SAXException e) {
			throw e;
		} catch (IOException e) {
			throw new SAXException(e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			throw new SAXException(e.getMessage(), e);
		}

		return svcDefs;
	}

	/**
	 * SAXParser의 EntityResolver 재정의
	 */
	public InputSource resolveEntity(String publicId, String systemId) {
		try {
			//log.debug("## resolveEntity : " + publicId);
			return new InputSource(SystemHelper.getResourceAsStream(DTD_RESOURCE));
		} 
		catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("", e);
			}
			return null;
		}
	}

	//
	// SAXParser의 DefaultHandler 재정의
	//

	public void startDocument() throws SAXException {
		if (log.isTraceEnabled()) {
			log.trace("parsing an service configuration file...");
		}
	}

	public void endDocument() throws SAXException {
		if (log.isTraceEnabled()) {
			log.trace("parsing has completed.");
		}
	}

	public void startElement(String namespaceURI, String sName, String qName,
			Attributes attrs) throws SAXException {
		// System.out.println("start elem " + sName + "," + qName +
		// ","+namespaceURI);

		if (ROOT_NODE.equalsIgnoreCase(qName)) {
			startRootElement(attrs);
		} else if (SERVICE_NODE.equalsIgnoreCase(qName)) {
			startServiceElement(attrs);
		} else if (PROPERTY_NODE.equalsIgnoreCase(qName)) {
			startPropertyElement(attrs);
		} else if (CONSTRUCTOR_NODE.equalsIgnoreCase(qName)) {
			startConstructorElement(attrs);
		} else if (FACTORY_NODE.equalsIgnoreCase(qName)) {
			startFactoryElement(attrs);
		} else if (ARGUMENT_NODE.equalsIgnoreCase(qName)) {
			startArgumentElement(attrs);
		} else {
			throw new SAXException("unknown element <" + qName + ">.");
		}

	}

	public void endElement(String namespaceURI, String sName, String qName)
			throws SAXException {
		// System.out.println("end elem " + sName + "," + qName);

		if (SERVICE_NODE.equalsIgnoreCase(qName)) {
			endServiceElement();
		}
	}

	private void startRootElement(Attributes attrs) throws SAXException {

		// 서비스 구성 파일 파싱시 사용될 root element의 속성을 저장해 놓는다.
		moduleName = StringHelper.null2void(attrs.getValue(ATTR_ROOT_MODULE));
		packageName = StringHelper.null2void(attrs.getValue(ATTR_ROOT_PACKAGE));
		resourceName = StringHelper.null2string(attrs.getValue(ATTR_ROOT_RESOURCE), null);
		preInit = StringHelper.null2string(attrs.getValue(ATTR_ROOT_PREINIT),"false");

		if (log.isTraceEnabled()) {
			log.trace("Services [module=" + moduleName + ", package="
					+ packageName + ", resource=" + resourceName
					+ ", lazy-init=" + preInit + "]");
		}

		if (!StringHelper.isNull(moduleName)) {
			moduleName = moduleName + ".";
		}

	}

	private void startServiceElement(Attributes attrs) throws SAXException {

		// service Element의 속성 값을 읽어 온다.
		String svcName = 
			StringHelper.null2string(attrs.getValue(ATTR_SERVICE_NAME), null);
		String svcClass = 
			StringHelper.null2string(attrs.getValue(ATTR_SERVICE_CLASS), null);
		String svcInf = 
			StringHelper.null2string(attrs.getValue(ATTR_SERVICE_INF), null);
		String svcProxy = 
			StringHelper.null2string(attrs.getValue(ATTR_SERVICE_INTERCEPTOR), null);
		String svcSingleton = 
			StringHelper.null2string(attrs.getValue(ATTR_SERVICE_SINGLETON), "true");
		String svcInitMethod = 
			StringHelper.null2string(attrs.getValue(ATTR_SERVICE_INIT_METHOD), null);
		String svcDestroyMethod = 
			StringHelper.null2string(attrs.getValue(ATTR_SERVICE_DESTROY_METHOD), null);
		String svcActivate = 
			StringHelper.null2string(attrs.getValue(ATTR_SERVICE_ACTIVATE), "true");
		String svcPre = 
			StringHelper.null2string(attrs.getValue(ATTR_SERVICE_PREINIT), preInit);
		String svcLoadPriority = 
			StringHelper.null2string(attrs.getValue(ATTR_SERVICE_LOADPRIORITY),"0");
		
		boolean isSingleton = Boolean.valueOf(svcSingleton).booleanValue();
		boolean isActivated = Boolean.valueOf(svcActivate).booleanValue();
		boolean isPreInit = Boolean.valueOf(svcPre).booleanValue();
		
		if (log.isTraceEnabled()) {
			log.trace("Service [name=" + svcName + ", interface=" + svcInf
					+ ", class=" + svcClass + ", proxy=" + svcProxy
					+ ", init-method=" + svcInitMethod + ", destroy-method="
					+ svcDestroyMethod + ", singleton=" + svcSingleton
					+ ", activate=" + isActivated + ", pre-init=" + isPreInit
					+ ", load-priority=" + svcLoadPriority
					+ "]");
		}

		if (svcName == null) {
			throw new SAXException("'name' not defined for <service>.");
		}

		// 서비스 명을 모듈명.서비스명 으로 변경
		svcName = moduleName + svcName;

		int loadPriority = 0;
		try {
			loadPriority = Integer.valueOf(svcLoadPriority).intValue();
		} catch(NumberFormatException ex) {
			throw new SAXException("'load-priority' must have a numeric value. (" + svcName + ")");
		}
		
		// 참조하는 서비스 명을 모듈명.서비스명으로 변경한다.
		svcProxy = getFullRefName(svcProxy);

		// 클래스명과 인터페이스 명을 각각 패키지명.클래스명과 패키지명.인터페이스명으로 변경
		svcClass = getFullPackageName(svcClass);
		svcInf = getFullPackageName(svcInf);

		// 서비스 정의 객체를 생성한다.
		curSvcDef = new ServiceDefinition(svcName, svcInf, svcClass, svcProxy,
										  svcInitMethod, svcDestroyMethod, 
										  isSingleton, isActivated,
										  isPreInit, loadPriority);

	}

	private void endServiceElement() throws SAXException {
		if (curSvcDef == null) {
			throw new SAXException("unexpected </service> element.");
		}

		// factory 방식이 아니라면 반드시 서비스 클래스가 정의되어 있어야 한다.
		if (curSvcDef.getServiceClass() == null && !curSvcDef.hasFactory()) {
			throw new SAXException("'class' not defined. ("
					               + curSvcDef.getServiceName()+")");
		}

		// 현재 서비스 정의 객체를 List에 담는다.
		svcDefs.add(curSvcDef);
		curSvcDef = null;
	}

	private void startPropertyElement(Attributes attrs) throws SAXException {

		String propName = StringHelper.null2string(attrs
				.getValue(ATTR_PROPERTY_NAME), null);
		String propValue = StringHelper.null2string(attrs
				.getValue(ATTR_PROPERTY_VALUE), null);
		String propRef = StringHelper.null2string(attrs
				.getValue(ATTR_PROPERTY_REF), null);

		if (log.isTraceEnabled()) {
			log.trace("  Property [name=" + propName + ", value=" + propValue
					+ ", ref=" + propRef + "]");
		}

		if (propName == null) {
			// 에러(프로퍼티 이름은 반드시 설정되야함
			throw new SAXException("property's 'name' not defined. ("+
					               curSvcDef.getServiceName()+")");
		}

		if (propValue == null && propRef == null) {
			// 에러(둘 중 하나는 설정되야함)
			throw new SAXException("'value' or 'ref' not defined for property "
					               + propName+". ("+curSvcDef.getServiceName()+")");
		}

		if (propValue != null && propRef != null) {
			// 에러(하나만 설정되야함)
			throw new SAXException("both 'value' and 'ref' defined for property "
					               + propName+". ("+curSvcDef.getServiceName()+")");
		}

		if (propRef == null) {
			curSvcDef.addProperty(new PropertyHolder(propName, 
													 propValue,
										             PropertyHolder.TYPE_VALUE));
		} else {
			propRef = getFullRefName(propRef);
			curSvcDef.addProperty(new PropertyHolder(propName, 
					                                 propRef,
									                 PropertyHolder.TYPE_REFERENCE));
		}
	}

	private void startFactoryElement(Attributes attrs) throws SAXException {

		// 이미 constructor 가 설정되었는지 확인한다.
		if (curSvcDef.hasConstructor()) {
			throw new SAXException("both <factory> and <constructor> defined. ("
					               + curSvcDef.getServiceName()+")");
		}

		String factoryClass = 
			StringHelper.null2string(attrs.getValue(ATTR_FACTORY_CLASS), null);
		String factoryRef = 
			StringHelper.null2string(attrs.getValue(ATTR_FACTORY_REF), null);
		String factoryMethod = 
			StringHelper.null2string(attrs.getValue(ATTR_FACTORY_METHOD), null);

		if (factoryClass == null && factoryRef == null) {
			// 모두 설정되어 있어야 한다.
			throw new SAXException("'class' or 'ref' not defined for factory. ("
					               + curSvcDef.getServiceName()+")");
		}
		if (factoryMethod == null) {
			// 메소드가  설정되어 있어야 한다.
			throw new SAXException("'method' not defined for factory. ("
					               + curSvcDef.getServiceName()+")");
		}

		factoryClass = getFullPackageName(factoryClass);
		factoryRef = getFullRefName(factoryRef);
		curSvcDef.setFactory(factoryClass, factoryRef, factoryMethod);

		if (log.isTraceEnabled()) {
			log.trace("  Factory [class=" + factoryClass + ", ref="
					+ factoryRef + ", method=" + factoryMethod + "]");
		}

	}

	private void startConstructorElement(Attributes attrs) throws SAXException {

		// 이미 factory 가 설정되었는지 확인한다.
		if (curSvcDef.hasFactory()) {
			throw new SAXException("both <factory> and <constructor> defined. ("
					               + curSvcDef.getServiceName()+")");
		}
		if (log.isTraceEnabled()) {
			log.trace("  Constructor []");
		}

	}

	private void startArgumentElement(Attributes attrs) throws SAXException {
		String argType = 
			StringHelper.null2string(attrs.getValue(ATTR_ARGUMENT_TYPE), null);
		String argValue = 
			StringHelper.null2string(attrs.getValue(ATTR_ARGUMENT_VALUE), null);
		String argRef = 
			StringHelper.null2string(attrs.getValue(ATTR_ARGUMENT_REF), null);

		if (log.isTraceEnabled()) {
			log.trace("      Argument [type=" + argType + ", value=" + argValue
					+ ", ref=" + argRef + "]");
		}

		if (StringHelper.isNull(argType)) {
			// 에러(인자의 타입은 반드시 설정되야함
			throw new SAXException("'type' not defined for argument. ("
					               + curSvcDef.getServiceName()+")");
		}

		if (StringHelper.isNull(argValue) && StringHelper.isNull(argRef)) {
			// 에러(둘 중 하나는 설정되야함)
			throw new SAXException("'value' or 'ref' not defined for argument. ("
					               + curSvcDef.getServiceName()+")");
		}

		if (!StringHelper.isNull(argValue) && !StringHelper.isNull(argRef)) {
			// 에러(하나만 설정되야함)
			throw new SAXException("both 'value' and 'ref' defined for argument. ("
					               + curSvcDef.getServiceName()+")");
		}

		argType = getFullPackageName(argType);
		if (argRef == null) {
			curSvcDef.addArgument(new ArgumentHolder(argType, 
													 argValue,
													 ArgumentHolder.TYPE_VALUE));
		} else {
			argRef = getFullRefName(argRef);
			curSvcDef.addArgument(new ArgumentHolder(argType, 
									                 argRef,
									                 ArgumentHolder.TYPE_REFERENCE));
		}
	}

	/**
	 * 모듈명이 포함된 전체 서비스 명을 생성한다.
	 */
	private String getFullRefName(String ref) {
		String fullRef = null;
		if (StringHelper.isNull(ref)) {
			fullRef = ref;
		} else if (ref.indexOf(".") >= 0) {
			// 이미 전체 서비스 명이므로 그대로 리턴한다.
			fullRef = ref;
		} else {
			// 서비스 명 앞에 모듈명을 붙여서 생성한다.
			fullRef = moduleName + ref;
		}

		return fullRef;
	}

	/**
	 * 패키지 명이 포함된 전체 클래스 명을 생성한다.
	 */
	private String getFullPackageName(String pkg) {
		String fullPkgName = null;
		if ( StringHelper.isNull(pkg) ) {
			fullPkgName = pkg;
		} else {
			fullPkgName = pkg.replaceAll("\\$\\{package\\}", packageName);
		}
		
		return fullPkgName;
	}
}
