package s2.adapi.framework.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.Constants;
import s2.adapi.framework.config.Configurator;
import s2.adapi.framework.config.ConfiguratorException;
import s2.adapi.framework.config.ConfiguratorFactory;

/**
 * <p>
 * Property 관련 Framework의 Helper Class 프로퍼티 Key가 계층적으로 구성되어 있는 프로퍼티 파일 사용을
 * 위한 기능을 제공한다. 또한 "${" 와 "}" 로 둘러싸인 형태의 참조 변수를 사용할 수 있다.
 * </p>
 * <p>
 * 한번 생성된 Properties 객체들은 caching 하여 재사용된다.
 * </p>
 * 
 * 프로퍼티 사용 예
 * 
 * <pre>
 *  ###### Common ########
 *  oracle.driver.simple=oracle.jdbc.driver.OracleDriver
 *  
 *  ########################################################
 *  # KHFC ZZ DB1
 *  ########################################################
 *  zzdb1.jdbc.driver=${oracle.driver.simple}
 *  zzdb1.jdbc.connectionurl=jdbc:oracle:thin:@172.17.200.13:1521:thns1
 *  zzdb1.jdbc.username=zz01
 *  zzdb1.jdbc.password=zz01
 *  zzdb1.jdbc.defaultautocommit=false
 *  
 *  ########################################################
 *  # KHFC SE DB1
 *  ########################################################
 *  sedb1.jdbc.driver=${oracle.driver.simple}
 *  sedb1.jdbc.connectionurl=jdbc:oracle:thin:@172.17.200.13:1521:thns1
 *  sedb1.jdbc.username=se01
 *  sedb1.jdbc.password=se01
 *  sedb1.jdbc.defaultautocommit=false
 *  
 *  ...
 *  
 *  // 프로그램 작성 예
 *  PropertyHelper ph = new PropertyHelper("s2adapi.jdbc.properties");
 *  Properties zzdb1Props = ph.getProperties("zzdb1");
 *  
 *  String driver = zzdb1Props.getProperty("jdbc.driver");  // oracle.jdbc.driver.OracleDriver
 * </pre>
 * 
 * <p>
 * 위와 같이 "zzdb1" 이나 "sedb1" 을 root key로 하여 그 하위 키를 접근하기 위한 기능을
 * 제공한다.
 * </p>
 * 
 * @author kimhd
 * @since 1.0
 */
public class PropertyHelper {

	/**
	 * <p>
	 * 에러나 이벤트와 관련된 각종 메시지를 로깅하기 위한 Log 오브젝트
	 * </p>
	 */
	private static final Logger log = LoggerFactory.getLogger(PropertyHelper.class);

	/**
	 * <p>
	 * PropertyHelper에서 만들어지는 모든 Properties 객체를 담아놓고 요청시 이 Hashtable에서 우선 검색하여
	 * 없는 경우에만 새로 생성한다.
	 * </p>
	 */
	private static Hashtable<String,Properties> allProperties = new Hashtable<String,Properties>();

	/**
	 * Properties 생성시 해당 파일의 LastModified 시간을 저장해놓는다.
	 */
	private static Hashtable<String,Long> allPropertiesModifiedTime = new Hashtable<String,Long>();
	
	/**
	 * property filename to be loaded.
	 */
	private String propertyFilename = null;
	
	/**
	 * config file 에 설정된 Properties 파일 경로를 사용하여 PropertyHelper 객체를 생성한다.
	 * @param configKey properties 파일 경로를 가져오기 위한 config key 이름
	 * @return
	 */
	public static PropertyHelper getInstanceFromConfig(String configKey) {
		try {
			Configurator config = ConfiguratorFactory.getConfigurator();
			String configValue = config.getString(Constants.APPLICATION_CONFIG_KEY + configKey); // property file path

			return new PropertyHelper(configValue);
		} 
		catch (ConfiguratorException e) {
			log.error("Cannot create Properties from Config : " + configKey, e);
			return null;
		}
	}

	/**
	 * 기본 config file 을 읽어서 PropertyHelper 객체를 생성한다.
	 * @return
	 */
	public static PropertyHelper getInstanceFromConfig() {
		try {
			Configurator config = ConfiguratorFactory.getConfigurator();
			String configKey = config.getString(Constants.APPLICATION_CONFIG_DEFAULT_NAME); // property file path

			if (configKey == null) {
				log.error("Default config key name (" + Constants.APPLICATION_CONFIG_DEFAULT_NAME + ") not given.");
				return null;
			}

			String configValue = config.getString(Constants.APPLICATION_CONFIG_KEY + configKey);
			return new PropertyHelper(configValue);
		} 
		catch (ConfiguratorException e) {
			log.error("Cannot create Properties from default Config.", e);
			return null;
		}
	}

	/**
	 * 프로퍼티 파일명을 인자로 받는다.
	 */
	public PropertyHelper(String filename) {
		propertyFilename = filename;
	}

	/**
	 * <p>
	 * "rootkey.subkey"를 키로하여 해당 Value를 String형으로 리턴한다.
	 * </p>
	 * 
	 * @param rootKey 원하고자하는 rootkey값.
	 * @param subKey rootkey의 하위 key 값
	 * @return 원하고자하는 key값에 해당하는 value.
	 */
	public String getString(String rootKey, String subKey) {
		String tmp = getProperties(rootKey).getProperty(subKey);
		return tmp;
	}

	/**
	 * <p>
	 * "rootkey.subkey"를 키로하여 해당 Value를 String형으로 리턴한다. 해당되는 키가 없을 경우
	 * defaultValue를 리턴한다.
	 * </p>
	 * 
	 * @param rootKey 원하고자하는 rootkey값.
	 * @param subKey rootkey의 하위 key 값
	 * @param defaultValue 해당되는 값을 없을 경우 리턴할 디폴트 값
	 * @return 원하고자하는 key값에 해당하는 value.
	 */
	public String getString(String rootKey, String subKey, String defaultValue) {
		String tmp = getProperties(rootKey).getProperty(subKey);
		if (StringHelper.isNull(tmp)) {
			return defaultValue;
		} else {
			return tmp;
		}
	}

	/**
	 * <p>
	 * "rootkey.subkey"를 키로하여 해당 Value를 정수형으로 리턴한다.
	 * </p>
	 * 
	 * @param rootKey 원하고자하는 rootkey값.
	 * @param subKey rootkey의 하위 key 값
	 * @return 원하고자하는 key값에 해당하는 value.
	 */
	public int getInt(String rootKey, String subKey) {
		String tmp = getProperties(rootKey).getProperty(subKey);
		return Integer.parseInt(tmp);
	}

	/**
	 * <p>
	 * "rootkey.subkey"를 키로하여 해당 Value를 정수형으로 리턴한다. 해당되는 키가 없을 경우 defaultValue를
	 * 리턴한다.
	 * </p>
	 * 
	 * @param rootKey 원하고자하는 rootkey값.
	 * @param subKey rootkey의 하위 key 값
	 * @param defaultValue 해당되는 값을 없을 경우 리턴할 디폴트 값
	 * @return 원하고자하는 key값에 해당하는 value.
	 */
	public int getInt(String rootKey, String subKey, int defaultValue) {
		String tmp = getProperties(rootKey).getProperty(subKey);
		try {
			return Integer.parseInt(tmp);
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	/**
	 * <p>
	 * "rootkey.subkey"를 키로하여 해당 Value를 Long 정수형으로 리턴한다.
	 * </p>
	 * 
	 * @param rootKey
	 *            원하고자하는 rootkey값.
	 * @param subKey
	 *            rootkey의 하위 key 값
	 * @return 원하고자하는 key값에 해당하는 value.
	 */
	public long getLong(String rootKey, String subKey) {
		String tmp = getProperties(rootKey).getProperty(subKey);
		return Long.parseLong(tmp);
	}

	/**
	 * <p>
	 * "rootkey.subkey"를 키로하여 해당 Value를 Long 정수형으로 리턴한다. 해당되는 키가 없을 경우
	 * defaultValue를 리턴한다.
	 * </p>
	 * 
	 * @param rootKey
	 *            원하고자하는 rootkey값.
	 * @param subKey
	 *            rootkey의 하위 key 값
	 * @param defaultValue
	 *            해당되는 값을 없을 경우 리턴할 디폴트 값
	 * @return 원하고자하는 key값에 해당하는 value.
	 */
	public long getLong(String rootKey, String subKey, long defaultValue) {
		String tmp = getProperties(rootKey).getProperty(subKey);
		try {
			return Long.parseLong(tmp);
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	/**
	 * <p>
	 * "rootkey.subkey"를 키로하여 해당 Value를 boolean형으로 리턴한다.
	 * </p>
	 * 
	 * @param rootKey
	 *            원하고자하는 rootkey값.
	 * @param subKey
	 *            rootkey의 하위 key 값
	 * @return 원하고자하는 key값에 해당하는 value.
	 */
	public boolean getBoolean(String rootKey, String subKey) {
		String tmp = getProperties(rootKey).getProperty(subKey);
		return Boolean.valueOf(tmp).booleanValue();
	}

	/**
	 * <p>
	 * "rootkey.subkey"를 키로하여 해당 Value를 boolean형으로 리턴한다. 해당되는 키가 없을 경우
	 * defaultValue를 리턴한다.
	 * </p>
	 * 
	 * @param rootKey
	 *            원하고자하는 rootkey값.
	 * @param subKey
	 *            rootkey의 하위 key 값
	 * @param defaultValue
	 *            해당되는 값을 없을 경우 리턴할 디폴트 값
	 * @return 원하고자하는 key값에 해당하는 value.
	 */
	public boolean getBoolean(String rootKey, String subKey,
			boolean defaultValue) {
		String tmp = getProperties(rootKey).getProperty(subKey);
		try {
			return Boolean.valueOf(tmp).booleanValue();
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	/**
	 * <p>
	 * <code>rootkey.*.*</code> 형태의 모든 프로퍼티를 모아서 하나의 Properties 객체로 리턴한다. 이때
	 * 리턴되는 Properties 객체는 rootkey를 제외한 하위 키를 사용하여 구성된다.
	 * 또한 Property 값들은 참조 변수가 모두 치환된 값들로 저장되어진다.
	 * </p>
	 * 
	 * @param rootKey
	 * @return Properties rootKey의 하위 키로 구성된 Properties 객체
	 */
	public Properties getProperties(String rootKey) {
		//
		// allProperties 객체에서 Properties 객체를 찾기위한 Key를 생성
		//
		String file_root_key = propertyFilename + "_" + rootKey;

		//
		// 기존에 이미 생성되었던 프로퍼티인지를 확인하여 있으면 그것을 리턴한다.
		//
		Properties prop = allProperties.get(file_root_key);

		if (prop != null) {
			return prop;
		}

		//
		// 처음으로 생성되는 경우이다. 우선 파일전체 프로퍼티 객체를 가져온다.
		//
		Properties fileProp = getProperties();

		//
		// 파일 프로퍼티 객체에서 rootKey로 시작되는 프로퍼티 Key를 찾은 후,
		// 새로운 프로퍼티 객체에 값을 저장한다.
		//
		prop = findProperties(rootKey,fileProp);
		
		if (log.isInfoEnabled()) {
			log.info("Build Properties for rootkey [" + rootKey + "]");
		}
		
		if (log.isDebugEnabled()) {
			log.debug(String.valueOf(prop));
		}

		//
		// 전체 프로퍼티 객체 저장용 Hashtable에 생성한 프로퍼티 객체를 저장해 놓는다.
		//
		allProperties.put(file_root_key, prop);

		return prop;
	}

	/**
	 * <p>
	 * 생성시에 설정된 프로퍼티 파일 키를 사용하여 해당 파일에 설정된 모든 Property 값들을 담은 Properties 객체를 리턴한다.
	 * 기존에 만들어진 Properties 객체가 있을 경우 이를 리턴하고,
	 * 처음 만들 경우에는 프로퍼티 파일 명을 Configurator를 통하여 가져온후 프로퍼티 파일을 사용하여
	 * Properties 객체를 생성하여 리턴한다.
	 * </p>
	 * 
	 * @return Properties 프로퍼티 파일의 모든 프로퍼티 항목을 가지고 있는 Properties 객체, 
	 *         오류 발생 시에는 비어있는 Properties 객체를 리턴한다.
	 */
	public Properties getProperties() {

		//
		// 기존에 이미 생성되었던 파일 전체 프로퍼티가 있는지를 확인하여 있으면 그것을 리턴한다.
		//
		Properties prop = allProperties.get(propertyFilename);

		if (prop != null) {
			return prop;
		}

		//
		// 파일 프로퍼티가 처음으로 생성되는 경우이다.
		//
		InputStream is = null;
		
		prop = new Properties(); // 비어있는 Properties 객체 생성
		long lastModifiedTime = -1;
		try {
			File pFile = new File(propertyFilename);
			lastModifiedTime = pFile.lastModified();
			
			is = new FileInputStream(pFile);
			if (propertyFilename.endsWith(".xml")) {
				prop.loadFromXML(is);
			} 
			else {
				prop.load(is);
			}
			
			if (log.isInfoEnabled()) {
				log.info("Loading properties file : " + propertyFilename);
			}
			
			if (log.isDebugEnabled()) {
				log.debug(String.valueOf(prop));
			}
		} 
		catch (Exception ex) {
			if (log.isErrorEnabled()) {
				log.error("Configurator Exception while loading properties : "
						+ propertyFilename, ex);
			}
		}
		finally {
			if (is != null) {
				try {
					is.close();
				} 
				catch (IOException ignore) {
				}
			}
		}

		allProperties.put(propertyFilename, prop);
		allPropertiesModifiedTime.put(propertyFilename, lastModifiedTime);
		
		return prop;
	}
	
	/**
	 * 해당 프로퍼티 파일이 마지막으로 변경된 시간을 반환함.
	 * @return
	 */
	public long lastModified() {
		return allPropertiesModifiedTime.get(propertyFilename);
	}
	
	/**
	 * Properties에서 rootKey로 시작하는 항목만 찾아서 새로운 Properties 객체에 담아 반환한다.
	 * 참조 변수는 실제 값으로 치환되어진다.
	 * @param rootKey
	 * @param prop
	 * @return
	 */
	public static Properties findProperties(String rootKey, Properties prop) {
		Properties cprop = new Properties();
		String matchKey = rootKey + ".";
		
		String tempKey = null;
		String tempValue = null;
		Iterator<Object> itor = prop.keySet().iterator();
		while (itor.hasNext()) {
			tempKey = (String) itor.next();
			if (tempKey.startsWith(matchKey)) {
				tempValue = prop.getProperty(tempKey);
				tempValue = interpreteValue(tempValue, prop); // 참조 변수 치환
				cprop.setProperty(tempKey.substring(matchKey.length()), tempValue);
			}
		}

		return cprop;
	}
	
	/**
	 * <p>
	 * 참조 변수가 있는 문자열을 실제 값으로 치환화여 리턴한다.
	 * </p>
	 * <p>
	 * 예)
	 * 
	 * <pre>
	 * Propreties props = new Properties();
	 * props.put(&quot;service.dir&quot;, &quot;/web/service&quot;);
	 * props.put(&quot;service.log.dir&quot;, &quot;${service.dir}/log&quot;);
	 * 
	 * String value = PropertyHelper.interpreteValue(&quot;${service.log.dir}/common&quot;, props);
	 * System.out.println(value); //  /web/service/log/common 
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param value 문자열
	 * @param prop 치환 시 참조 변수값을 얻어오기 위한 Properties 객체
	 * @return
	 */
	public static String interpreteValue(String value, Properties props) {
		return interpreteValue(value, props, new HashSet<String>());
	}

	/**
	 * <p>
	 * Properties에서 주어진 키에 해당되는 값을 반환하며, 반환 값에 참조 변수가 있다면 이를을 실제 값으로 치환화여 반환한다.
	 * </p>
	 * <p>
	 * 예)
	 * 
	 * <pre>
	 * Propreties props = new Properties();
	 * props.put(&quot;service.dir&quot;, &quot;/web/service&quot;);
	 * props.put(&quot;service.log.dir&quot;, &quot;${service.dir}/log&quot;);
	 * props.put(&quot;service.log.common.dir&quot;, &quot;${service.log.dir}/common&quot;);
	 * 
	 * String value = PropertyHelper
	 * 		.getInterpretedString(&quot;service.log.common.dir&quot;, props);
	 * System.out.println(value); //  /web/service/log/common 
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param value 문자열
	 * @param prop 치환 시 참조 변수값을 얻어오기 위한 Properties 객체
	 * @return
	 */
	public static String getInterpretedString(String key, Properties props) {
		String value = props.getProperty(key);
		Set<String> priorRefs = new HashSet<String>();
		priorRefs.add(key);

		return interpreteValue(value, props, priorRefs);
	}

	private static final String REF_START_TOKEN = "${";

	private static final String REF_END_TOKEN = "}";

	private static final int REF_START_TOKEN_LEN = REF_START_TOKEN.length();

	private static final int REF_END_TOKEN_LEN = REF_END_TOKEN.length();

	/**
	 * 주어진 base 문자열에 존재하는 참조 변수를 실제 값으로 치환하여 반환한다.
	 * 
	 * @param base 치환 대상 문자열
	 * @param props 참조 변수들의 실제 문자열이 들어 있는 Properties 객체
	 * @param priorRefs 치환 중에 현재까지 참조했던 참조 변수 집합
	 * @return 참조 변수가 실제 값으로 치환된 문자열
	 */
	protected static String interpreteValue(String base, Properties props,
			Set<String> priorRefs) {

		if (StringHelper.isNull(base)) {
			return "";
		}

		int begin = base.indexOf(REF_START_TOKEN);
		int end = base.indexOf(REF_END_TOKEN);

		if (begin >= 0 && end >= 0 && begin < end ) {
			// 변수 존재
			StringBuffer sb = new StringBuffer();
			
			// ${ 앞까지의 문자열 append.
			sb.append(base.substring(0, begin)); 
			
			// ${ 와 } 사이의  문자열 잘라오기
			String variable = base.substring(begin + REF_START_TOKEN_LEN, end); 
			
			if (priorRefs.contains(variable)) {
				// inifinit loop
				throw new IllegalStateException("Interpretation failed, circular reference detected : "
						                        + variable + ")");
			} 
			else {
				priorRefs.add(variable);
			}
			
			sb.append(interpreteValue(getProperty(variable,props), props,
					priorRefs)); // 참조 결과 문자열을 추가, recurssion
			priorRefs.remove(variable);
			
			// { 뒤의 문자열을 추가
			sb.append(base.substring(end + REF_END_TOKEN_LEN, base.length())); 
			return interpreteValue(sb.toString(), props, priorRefs);
		} 
		else {
			return base;
		}
	}
	
	/**
	 * Properties 를 검색할때 System Properties 도 같이 검색한다.
	 * (2016.04.18)
	 * @param key
	 * @param props
	 * @return
	 */
	private static String getProperty(String key, Properties props) {
		String value = props.getProperty(key);
		if (value == null) {
			value = System.getProperty(key);
		}
		
		return value;
	}
}