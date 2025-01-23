package s2.adapi.framework.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * <p>
 * System 관련 Live Framework의 Helper Class
 * </p>
 *
 * <p>
 * 아래 Method들은 모두 <code>static</code>으로 선언 되었기 때문에 유념하기 바란다.
 * </p>
 *
 * @author kimhd
 * @since 1.0
 */
public class SystemHelper {

	public static final String URL_SPACE = "%20";
	public static final String SYSTEM_SPACE = " ";
	
    /**
     * <p>
     * <strong>SystemHelper</strong>의 default 컨스트럭터(Constructor).
     * </p>
     */
    private SystemHelper() {
    }

    /**
     * <p>
     * {@link java.lang.System}의 <code>getProperty</code> Method의 LiveFramework의 Helper Method
     * </p>
     *
     * @param key          SystemProperty의 정의된 key
     * @param defaultValue SystemProperty의 정의된 key에 해당하는 value가 없는 경우 default값
     * @return SystemProperty의 정의된 key에 해당하는 value
     */
    public static String getSystemProperty(String key, String defaultValue) {
        try {
            return System.getProperty(key, defaultValue);
        } catch (Throwable th) {
            return defaultValue;
        }

    }

    /**
     * <p>
     * {@link java.lang.System}의 <code>setProperty</code> Method의 LiveFramework의 Helper Method
     * </p>
     * @param key System Property로 정의할 key
     * @param value System Property로 정의할 value
     * @return 이전에 정의되어 있던 value
     */
    public static String setSystemProperty(String key, String value) {
    	return System.setProperty(key, value);
    }
    
    /**
     * <p>
     * {@link java.lang.System}의 <code>getProperty</code> Method의 LiveFramework의 Helper Method
     * </p>
     *
     * @param key SystemProperty의 정의된 key
     * @return SystemProperty의 정의된 key에 해당하는 value
     */
    public static String getSystemProperty(String key) {
        return getSystemProperty(key, null);
    }

    /**
     * <p>
     * <code>Classpath</code> 상의 Resource를 {@link java.io.InputStream}으로 리턴한다.
     * </p>
     *
     * @param name 찾기위한 Resource
     * @return Resource의 {@link java.io.InputStream}
     * @throws IOException Resource를 찾을 수 없거나 읽을 수 경우
     */
    public static InputStream getResourceAsStream(String name) throws IOException {
        return getResourceAsStream(getClassLoader(), name);
    }

    /**
     * <p>
     * <code>Classpath</code> 상의 Resource를 {@link java.io.InputStream}으로 리턴한다.
     * </p>
     *
     * @param loader Resource을 찾기 위한 ClassLoader
     * @param name   찾기위한 Resource
     * @return Resource의 {@link java.io.InputStream}
     * @throws IOException Resource를 찾을 수 없거나 읽을 수 경우
     */
    public static InputStream getResourceAsStream(ClassLoader loader,
                                                  String name) throws IOException {
        InputStream in = null;
        if (loader == null) {
        	in = getClassLoader().getResourceAsStream(name);
        } else {
        	in = loader.getResourceAsStream(name);
        } 
        
        if (in == null) {
            throw new IOException("Could not find resource " + name);
        }
        return in;
    }

    /**
     * <p>
     * <code>Classpath</code> 상의 Resource를 {@link java.net.URL}으로 리턴한다.
     * </p>
     *
     * @param name 찾기위한 Resource
     * @return Resource의 {@link java.net.URL}
     * @throws IOException Resource를 찾을 수 없거나 읽을 수 경우
     */
    public static URL getResourceURL(String name) throws IOException {
        return getResourceURL(getClassLoader(), name);
    }

    /**
     * <p>
     * <code>Classpath</code> 상의 Resource들을 {@link java.net.URL}으로 리턴한다.
     * </p>
     *
     * @param name 찾기위한 Resource
     * @return 같은 이름에 해당되는 Resource들의 {@link java.net.URL}을 Enumeration 으로 반환
     * @throws IOException Resource를 찾을 수 없거나 읽을 수 경우
     */
    public static Enumeration<URL> getResourceURLs(String name) throws IOException {
        return getResourceURLs(getClassLoader(), name);
    }
    
    /**
     * <p>
     * <code>Classpath</code> 상의 Resource를 {@link java.net.URL}으로 리턴한다.
     * </p>
     *
     * @param loader Resource을 찾기 위한 ClassLoader
     * @param name   찾기위한 Resource
     * @return Resource의 {@link java.net.URL}
     * @throws IOException Resource를 찾을 수 없거나 읽을 수 경우
     */
    public static URL getResourceURL(ClassLoader loader, String name) throws IOException {
        URL url = null;
        if (loader == null) {
        	url = getClassLoader().getResource(name);
        } else {
            url = loader.getResource(name);
        }
        
        if (url == null) {
            url = ClassLoader.getSystemResource(name);
        }
        if (url == null) {
            throw new IOException("Could not find name " + name);
        }
        return url;
    }

    /**
     * <p>
     * <code>Classpath</code> 상의 Resource들을 {@link java.net.URL}으로 리턴한다.
     * </p>
     *
     * @param loader Resource을 찾기 위한 ClassLoader
     * @param name   찾기위한 Resource
     * @return 같은 이름에 해당되는 Resource들의 {@link java.net.URL}을 Enumeration 으로 반환
     * @throws IOException Resource를 찾을 수 없거나 읽을 수 경우
     */
    public static Enumeration<URL> getResourceURLs(ClassLoader loader, String name) throws IOException {
        Enumeration<URL> urls = null;
        if (loader == null) {
        	urls = getClassLoader().getResources(name);
        } else {
            urls = loader.getResources(name);
        }
        return urls;
    }
    
    /**
     * <p>
     * <code>Classpath</code> 상의 Resource를 {@link java.util.Properties}으로 리턴한다.
     * </p>
     *
     * @param name 찾기위한 Resource
     * @return Resource의 {@link java.util.Properties}
     * @throws IOException Resource를 찾을 수 없거나 읽을 수 경우
     */
    public static Properties getResourceAsProperties(String name)
            throws IOException {
        Properties props = new Properties();
        InputStream in = null;
        String propfile = name;
        in = getResourceAsStream(propfile);
        props.load(in);
        in.close();
        return props;
    }

    /**
     * <p>
     * <code>Classpath</code> 상의 Resource를 {@link java.util.Properties}으로 리턴한다.
     * </p>
     *
     * @param loader Resource을 찾기 위한 ClassLoader
     * @param name   찾기위한 Resource
     * @return Resource의 {@link java.util.Properties}
     * @throws IOException Resource를 찾을 수 없거나 읽을 수 경우
     */
    public static Properties getResourceAsProperties(ClassLoader loader, String name)
            throws IOException {
        Properties props = new Properties();
        InputStream in = null;
        String propfile = name;
        in = getResourceAsStream(loader, propfile);
        props.load(in);
        in.close();
        return props;
    }

    /**
     * <p>
     * <code>Classpath</code> 상의 Resource를 {@link java.io.Reader}으로 리턴한다.
     * </p>
     *
     * @param name 찾기위한 Resource
     * @return Resource의 {@link java.io.Reader}
     * @throws IOException Resource를 찾을 수 없거나 읽을 수 경우
     */
    public static Reader getResourceAsReader(String name) throws IOException {
        return new InputStreamReader(getResourceAsStream(name));
    }

    /**
     * <p>
     * <code>Classpath</code> 상의 Resource를 {@link java.io.Reader}으로 리턴한다.
     * </p>
     *
     * @param loader Resource을 찾기 위한 ClassLoader
     * @param name   찾기위한 Resource
     * @return Resource의 {@link java.io.Reader}
     * @throws IOException Resource를 찾을 수 없거나 읽을 수 경우
     */
    public static Reader getResourceAsReader(ClassLoader loader, String name) throws IOException {
        return new InputStreamReader(getResourceAsStream(loader, name));
    }

    /**
     * <p>
     * <code>Classpath</code> 상의 Resource를 {@link java.io.File}으로 리턴한다.
     * 내부적으로 URL 경로를 가져오는 구조이므로 resource 명에 공백이 있을 경우 %20 으로 치환되며
     * 이 경우에는 실제 파일 경로와 달라지므로 해당 파일을 찾을 수 없게 된다.
     * 따라서 resource 명에 공백이 포함되지 않도록 한다.
     * </p>
     *
     * @param name 찾기위한 Resource
     * @return Resource의 {@link java.io.File}
     * @throws IOException Resource를 찾을 수 없거나 읽을 수 경우
     */
    public static File getResourceAsFile(String name) throws IOException {
        return new File(getResourceURL(name).getFile().replace(URL_SPACE,SYSTEM_SPACE));
    }

    /**
     * <p>
     * <code>Classpath</code> 상의 Resource들을 {@link java.io.File}의 배열으로 리턴한다.
     * </p>
     *
     * @param name 찾기위한 Resource 이름
     * @return 동일한 이름의 Resource들이 담긴 {@link java.io.File}의 배열
     * @throws IOException Resource를 찾을 수 없거나 읽을 수 경우
     */
    public static File[] getResourcesAsFile(ClassLoader loader, String name) throws IOException {
    	Enumeration<URL> urls = getResourceURLs(loader,name);
    	List<File> files = new ArrayList<File>();
    	
    	while(urls.hasMoreElements()) {
    		files.add(new File(urls.nextElement().getFile().replace(URL_SPACE,SYSTEM_SPACE)));
    	}
    	
    	return files.toArray(new File[files.size()]);
    }
    
    /**
     * <p>
     * <code>Classpath</code> 상의 Resource들을 {@link java.io.File}의 배열으로 리턴한다.
     * </p>
     *
     * @param name 찾기위한 Resource 이름
     * @return 동일한 이름의 Resource들이 담긴 {@link java.io.File}의 배열
     * @throws IOException Resource를 찾을 수 없거나 읽을 수 경우
     */
    public static File[] getResourcesAsFile(String name) throws IOException {
    	Enumeration<URL> urls = getResourceURLs(name);
    	List<File> files = new ArrayList<File>();
    	
    	while(urls.hasMoreElements()) {
    		files.add(new File(urls.nextElement().getFile().replace(URL_SPACE,SYSTEM_SPACE)));
    	}
    	
    	return files.toArray(new File[files.size()]);
    }
    /**
     * <p>
     * <code>Classpath</code> 상의 Resource를 {@link java.io.File}으로 리턴한다.
     * </p>
     *
     * @param loader Resource을 찾기 위한 ClassLoader
     * @param name   찾기위한 Resource
     * @return Resource의 {@link java.io.File}
     * @throws IOException Resource를 찾을 수 없거나 읽을 수 경우
     */
    public static File getResourceAsFile(ClassLoader loader, String name) throws IOException {
        return new File(getResourceURL(loader, name).getFile());
    }

    /**
     * <p>
     * <code>URL</code>을 {@link java.io.InputStream}으로 리턴한다.
     * </p>
     *
     * @param urlString - 가져오고자 하는 URL
     * @return URL로 부터 데이터를 읽은 {@link java.io.InputStream}
     * @throws IOException URL을 찾을 수 없거나 읽을 수 경우
     */
    public static InputStream getUrlAsStream(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        return conn.getInputStream();
    }

    /**
     * <p>
     * <code>URL</code>을 {@link java.io.Reader}으로 리턴한다.
     * </p>
     *
     * @param urlString - 가져오고자 하는 URL
     * @return URL로 부터 데이터를 읽은 {@link java.io.Reader}
     * @throws IOException URL을 찾을 수 없거나 읽을 수 경우
     */
    public static Reader getUrlAsReader(String urlString) throws IOException {
        return new InputStreamReader(getUrlAsStream(urlString));
    }

    /**
     * <p>
     * <code>URL</code>을 {@link java.util.Properties}으로 리턴한다.
     * </p>
     *
     * @param urlString - 가져오고자 하는 URL
     * @return URL로 부터 데이터를 읽은 {@link java.util.Properties}
     * @throws IOException URL을 찾을 수 없거나 읽을 수 경우
     */
    public static Properties getUrlAsProperties(String urlString) throws IOException {
        Properties props = new Properties();
        InputStream in = null;
        String propfile = urlString;
        in = getUrlAsStream(propfile);
        props.load(in);
        in.close();
        return props;
    }

    /**
     * 주어진 객체의 package 구조를 경로 형태로 반환한다.
     * @param obj
     * @return
     */
    public static String getPackagePath(Object obj) {
    	if (obj != null) {
    		return obj.getClass().getPackage().getName().replace('.','/');
    	} else {
    		return null;
    	}
    }
    
    /**
     * 주어진 클래스의 package 구조를 경로 형태로 반환한다.
     * @param clazz
     * @return
     */
    public static String getPackagePath(Class<?> clazz) {
    	if (clazz != null) {
    		return clazz.getPackage().getName().replace('.','/');
    	} else {
    		return null;
    	}
    }
    
    /**
     * <p>
     * {@link java.lang.Thread}의 <code>getContextClassLoader</code>를 이용하여 <strong>ClassLoader</strong>을 리턴
     * </p>
     *
     * @return classloader
     */
    public static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static String getHostname() {
    	return getHostname(null);
    }
    
    public static String getHostname(String defName) {
    	String hostname = defName;
    	try {
			InetAddress localMachine = InetAddress.getLocalHost();
			hostname = localMachine.getHostName();
		} catch (UnknownHostException e) {
		}
		
		return hostname;
    }
}