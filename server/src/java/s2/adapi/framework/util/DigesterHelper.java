package s2.adapi.framework.util;

import java.security.MessageDigest;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * <p>
 * 다시 데이터를 복원 할수 없는 데이터를 만들때 사용.
 * </p>
 *
 */
public class DigesterHelper {

    /**
     * <p>
     * md5 암호화.
     * </p>
     *
     * @param content 실 데이터
     * @return 암호화된
     */
    public static byte[] md5(String content) {
        return DigestUtils.md5(content);
    }

    /**
     * <p>
     * md5Hex 암호화
     * </p>
     *
     * @param content
     * @return .
     */
    public static String md5Hex(String content) {
        return DigestUtils.md5Hex(content);
    }

    /**
     * <p>
     * sha256 암호화.
     * </p>
     *
     * @param content 실 데이터
     * @return 암호화된
     */
    public static byte[] sha256(String content) {
        return DigestUtils.sha256(content);
    }

    /**
     * <p>
     * sha256Hex 암호화
     * </p>
     *
     * @param content
     * @return .
     */
    public static String sha256Hex(String content) {
        return DigestUtils.sha256Hex(content);
    }
    
    /**
     * SHA-1 암호화
     * @param content
     * @return
     */
    public static String sha1Hex(String content) {
    	try {
    		MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
    		byte[] hashed =  messageDigest.digest(content.getBytes());
    	
    		return HexEncoder.byteArrayToHex(hashed);
    	}
    	catch(Exception ex) {
    		return null;
    	}
    }
}
