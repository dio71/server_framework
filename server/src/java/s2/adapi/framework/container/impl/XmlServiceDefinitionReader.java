package s2.adapi.framework.container.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import s2.adapi.framework.container.ServiceContainerException;
import s2.adapi.framework.container.ServiceReader;
import s2.adapi.framework.container.support.ServiceDefinition;
import s2.adapi.framework.container.support.ServiceRegistry;
import s2.adapi.framework.util.SystemHelper;

public class XmlServiceDefinitionReader implements ServiceReader {

	private static final Logger log = LoggerFactory.getLogger(XmlServiceDefinitionReader.class);

	public void loadServiceDefinition(String[] resNames,
			                          ServiceRegistry svcRegistry, 
			                          boolean ignoreException) throws ServiceContainerException {
		
		for (int i = 0; i < resNames.length; i++) {
			try {
				loadServiceDefinition(resNames[i], svcRegistry);
			} catch (ServiceContainerException e) {
				if (log.isErrorEnabled()) {
					log.error("loading failed for service configuration file ["+resNames[i]+"].", e);
				}
				if (!ignoreException) {
					throw e;
				}
			}
		}
	}

	public void loadServiceDefinition(String resName,
			ServiceRegistry svcRegistry) throws ServiceContainerException {
		if (resName == null) {
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("loading service definition resource : " + resName);
		}

		InputStream is = null;
		try {
			is = SystemHelper.getResourceAsStream(resName);
			loadServiceDefinition(is, svcRegistry, resName);
		} catch (IOException e) {
			throw new ServiceContainerException("resource file(" + resName
					+ ") reading error.", e);
		} finally {
			try {
				if ( is != null ) { 
					is.close();
				}
			} catch (IOException e) {
				if ( log.isErrorEnabled()) {
					log.error("error while closing configuration file("
							  +resName + ").",e);
				}
			}
		}
	}

	public void loadServiceDefinition(File file, ServiceRegistry svcRegistry)
			throws ServiceContainerException {
		if (file == null) {
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("loading service definition file : " + file.getAbsolutePath());
		}

		InputStream is = null;
		try {
			is = new FileInputStream(file);
			
			loadServiceDefinition(is, svcRegistry, file.getPath());
		} catch (FileNotFoundException e) {
			throw new ServiceContainerException("configuration file("
					+ file.getAbsolutePath() + ") is not found.", e);
		} finally {
			try {
				if ( is != null ) {
					is.close();
				}
			} catch (IOException e) {
				if ( log.isErrorEnabled()) {
					log.error("error while closing configuration file("
							  +file.getName() + ").",e);
				}
			}
		}
	}

	public void loadServiceDefinition(File[] file, 
			                          ServiceRegistry svcRegistry, 
			                          boolean ignoreException) throws ServiceContainerException {
		for(int i=0;i<file.length;i++) {
			try {
				loadServiceDefinition(file[i], svcRegistry);
			} catch (ServiceContainerException e) {
				if (log.isErrorEnabled()) {
					log.error("loading failed for service configuration file ["+file[i].getName()+"].", e);
				}
				if (!ignoreException) {
					throw e;
				}
			}
		}
	}
	
	public void loadServiceDefinition(InputStream is,
			ServiceRegistry svcRegistry, String fromInfo) throws ServiceContainerException {

		long stime = System.currentTimeMillis();

		XmlServiceDefinitionParser parser = new XmlServiceDefinitionParser();

		List<ServiceDefinition> svcDefs = null;
		try {
			svcDefs = parser.parse(is);
		} catch (SAXException e) {
			if (log.isErrorEnabled()) {
				log.error("Error while loading service configuration file.");
			}
			throw new ServiceContainerException(e.getMessage(), e);
		}
		
		for (int i = 0; i < svcDefs.size(); i++) {
			try {
				ServiceDefinition sdef = svcDefs.get(i);
				sdef.setFromInfo(fromInfo); // 서비스 파일의 경로를 설정해 놓는다.
				svcRegistry.registerServiceDefinition(sdef);
			} catch(Throwable ex) {
				if (log.isErrorEnabled()) {
					log.error(ex.getMessage());
				}
			}
		}
		
		if (log.isDebugEnabled()) {
			long etime = System.currentTimeMillis();
			log.debug("loading time : " + (etime - stime) + " msec");
		}
	}
}
