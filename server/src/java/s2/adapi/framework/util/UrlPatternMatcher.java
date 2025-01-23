package s2.adapi.framework.util;

import java.util.List;

/**
 * URL 문자열에 대한 패턴 매칭 기능을 제공한다. 다음은 URL 패턴에 대한 예이다.
 * <pre>
 *
 * - Example Set of Patterns
 * -------------------------------------------------------
 * path pattern            no
 * -------------------------------------------------------
 * /foo/bar/*              1
 * /baz/*                  2
 * /catalog                3
 * *.bop                   4
 * -------------------------------------------------------
 *
 * - Incoming Paths applied to the Patterns
 * -------------------------------------------------------
 * incoming path           no
 * -------------------------------------------------------
 * /foo/bar/index.html     1
 * /foo/bar/index.bop      1
 * /baz                    2
 * /baz/index.html         2
 * /catalog                3
 * /catalog/racecar.bop    4
 * /index.bop              4
 * -------------------------------------------------------
 * </pre>
 * @author 김형도
 * @since 4.0
 */
public class UrlPatternMatcher {

	/**
	 * 문자열이 패턴형태인지 여부를 반환한다.
	 * @param pattern
	 * @return
	 */
	public static boolean isPattern(String pattern) {
		if (pattern.charAt(0) == '*' || 
			(pattern.charAt(0) == '/' && pattern.charAt(pattern.length()-1) == '*')) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 주어진 URL 문자열이 pattern에 매치되는지 여부를 반환한다.
	 * @param url
	 * @param pattern
	 * @return
	 */
	public static boolean match(String url,String pattern) {
    	if ( pattern.charAt(0) == '*' ) {
    		return url.endsWith(pattern.substring(1));
    	} else if ( pattern.charAt(pattern.length()-1) == '*') {
    		return url.startsWith(pattern.substring(0,pattern.length()-1));
    	} else {
    		return url.equals(pattern);
    	}
	}
	
	/**
	 * 주어진 패턴 목록에서 url과 매치되는 첫번째 패턴을 반환한다.
	 * @param url
	 * @param patterns
	 * @return
	 */
	public static String getFirstMatched(String url, List<String> patterns) {
    	if ( patterns != null ) {
    		for(int i=0;i<patterns.size();i++) {
    			String pattern = patterns.get(i);
    			if ( match(url,pattern) ) {
    				return pattern;
    			}
    		}
    	}
    	return null;
	}
	
	/**
	 * 주어진 패턴 목록에서 url과 가장 길게 매치되는 패턴을 반환한다.
	 * @param url
	 * @param patterns
	 * @return
	 */
	public static String getBestMatched(String url, List<String> patterns) {
		//System.out.println("getBestMatched : " + url + ", " + patterns.toString());
		String bestMatched = null;
		
    	if ( patterns != null ) {
    		for(int i=0;i<patterns.size();i++) {
    			String pattern = patterns.get(i);
    			//System.out.println("check pattern : " + pattern);
    			
    			if ( match(url,pattern) ) {
    				//System.out.println("match!!!! ");
    				if ( bestMatched == null || bestMatched.length() < pattern.length()) {
    					bestMatched = pattern;
    				}
    			}
    		}
    	}
    	
    	return bestMatched;
	}
	
	public static String getBestMatched(String url, String[] patterns) {
		String bestMatched = null;
		
    	if ( patterns != null ) {
    		for(String pattern:patterns) {
    			if ( match(url,pattern) ) {
    				if ( bestMatched == null || bestMatched.length() < pattern.length()) {
    					bestMatched = pattern;
    				}
    			}
    		}
    	}
    	
    	return bestMatched;
	}
}
