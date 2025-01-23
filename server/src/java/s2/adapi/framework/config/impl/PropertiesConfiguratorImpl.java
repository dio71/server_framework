package s2.adapi.framework.config.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import s2.adapi.framework.Constants;
import s2.adapi.framework.config.Configurator;
import s2.adapi.framework.config.ConfiguratorException;
import s2.adapi.framework.util.PropertyHelper;
import s2.adapi.framework.util.StringHelper;


/**
 * <p>
 * <strong>Configurator</strong>의 Property로 구성된 파일을 처리하는 구현 클래스이다.
 * Property 형태로 구성 파일을 정의할 때 "${" 와 "}" 로 둘러싸인 형태의 참조변수 정의가 가능하다.
 * 참조 변수는 실제 값으로 치환되어 반환된다.
 * </p>
 *
 */
public class PropertiesConfiguratorImpl implements Configurator {

    /**
     * <p>
     * Framework의 구성 파일을 메모리에 적재하기 위한 property 오브젝트.
     * </p>
     */
    protected static Properties prop = new Properties();

    public void doConfigure(InputStream stream) throws ConfiguratorException {
        try {
            prop.clear();
            
            // 프로퍼티 설정을 로딩한다.
            if ( stream != null ) {
            	prop.load(stream);
            }
        } 
        catch (IOException e) {
            throw new ConfiguratorException("Check the configuration file[" + Constants.CONFIG_FILE_PATH_KEY + "] : doConfigure", e);
        } 
        finally {
            // System Propertes를 추가한다.
            prop.putAll(System.getProperties());
        }
    }

    public void clear() {
    	prop.clear();
    }
    
    public String getString(String key) throws ConfiguratorException {
    	String tmp = PropertyHelper.getInterpretedString(key,prop);
        if (StringHelper.isNull(tmp)) {
            throw new ConfiguratorException("Check the configuration file : [" + key + "] is not defined.");
        } else {
            return tmp;
        }
    }

    public String getString(String key, String defaultValue) {
    	String tmp = PropertyHelper.getInterpretedString(key,prop);
        if (StringHelper.isNull(tmp)) {
            return defaultValue;
        } else {
            return tmp;
        }
    }

    public int getInt(String key) throws ConfiguratorException {
        String tmp = null;
        try {
            tmp = getString(key);
            return Integer.parseInt(tmp);
        } catch (NumberFormatException nmex) {
            throw new ConfiguratorException("Check the configuration file : Illegal Integer Value : " + tmp, nmex);
        }
    }

    public int getInt(String key, int defaultValue) {
        String tmp = null;
        try {
            tmp = getString(key);
            return Integer.parseInt(tmp);
        } catch (Exception nmex) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key) throws ConfiguratorException {
        String tmp = null;
        try {
            tmp = getString(key);
            return Boolean.valueOf(tmp).booleanValue();
        } catch (Exception e) {
            throw new ConfiguratorException("Check the configuration file : Illegal Boolean Value : " + tmp, e);
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String tmp = null;
        try {
            tmp = getString(key);
            return Boolean.valueOf(tmp).booleanValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public long getLong(String key) throws ConfiguratorException {
        String tmp = null;
        try {
            tmp = getString(key);
            return Long.valueOf(tmp).longValue();
        } catch (Exception e) {
            throw new ConfiguratorException("Check the configuration file : Illegal Long Value : " + tmp, e);
        }
    }

    public long getLong(String key, long defaultValue) {
        String tmp = null;
        try {
            tmp = getString(key);
            return Long.valueOf(tmp).longValue();
        } catch (Exception ex) {
            return defaultValue;
        }
    }
    
    public String getPath(String key, String defaultPath) {
    	String rawPath = getString(key,defaultPath);
    	if (rawPath == null) {
    		return null;
    	}
    	String path = null;
    	char fsep = File.separatorChar;
    	switch(fsep) {
    	case '\\': // Windows
    		path = rawPath.replace('/','\\');
    		break;
    	case '/' : // Unix
    		path = rawPath.replace('\\','/');
    		break;
    	default : // never occurs.
    		path = rawPath;
    		break;
    	}
    	File file = new File(path);
    	try {
			path = file.getCanonicalPath();
		} catch (IOException e) {
		}
		return path;
    }
    
    public Set<Object> getKeySet() {
    	return prop.keySet();
    }
}
