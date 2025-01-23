package s2.adapi.framework.util;

import java.util.zip.CRC32;

/**
 * Url 관련 기능들을 제공한다.
 * 
 * @author kimhd
 *
 */
public class UrlHelper {
	
	private static final int BASE62_ADDUM = 123456;
	
	private static final byte[] BASE64_URL_ENC = "QREFyz012STpqUGO-PnoXHIJuv45h7x6D3NYZabc89_KLMwdVWgABCijklmefrst=".getBytes();
	private static final byte[] BASE64_URL_DEC = Base64Helper.getDecodeKey(BASE64_URL_ENC);

    /**
     * URL 에서 사용할 수 있도록 Base64 인코딩한다. (= 제거 및 별도 encodeKey 와 CRC checksum 을 사용한다.)
     * @param content
     * @return
     */
    public static String encodeParameterSecure(byte[] contentBytes) {
    	if (contentBytes == null || contentBytes.length == 0) {
    		return null;
    	}
    	
    	try {
	    	
	    	// get checksum
	    	long checksum = getCRC32Checksum(contentBytes,0);
	    	
	    	// add checksum
	    	byte[] checkedBytes = new byte[contentBytes.length + 4];
	    	
	    	checkedBytes[0] = (byte)(checksum & 0xff);
	    	checkedBytes[1] = (byte)(checksum>>8 & 0xff);
	    	checkedBytes[2] = (byte)(checksum>>16 & 0xff);
	    	checkedBytes[3] = (byte)(checksum>>24 & 0xff);
	    	
	    	System.arraycopy(contentBytes, 0, checkedBytes, 4, contentBytes.length);
	    	
	    	byte[] encoded = Base64Helper.encodeSecure(checkedBytes, BASE64_URL_ENC);
	    	
	    	String encodedString = new String(encoded);
	    	// remove =
	    	int index = encodedString.indexOf("=");
	    	if (index > 0) {
	    		encodedString = encodedString.substring(0,index);
	    	}
	    	
	    	return encodedString;
    	}
    	catch(Exception ex) {
    		
    		return null;
    	}
    }
    
    /**
     * URL 에서 사용할 수 있도록 Base64 인코딩한다. (= 제거 및 별도 encodeKey 와 CRC checksum 을 사용한다.)
     * @param content
     * @return
     */
    public static String encodeParameterSecure(String content) {
    	if (content == null || content.length() == 0) {
    		return null;
    	}
    	
    	try {
	    	byte[] contentBytes = content.getBytes("utf-8");
	    	
	    	return encodeParameterSecure(contentBytes);
    	}
    	catch(Exception ex) {
    		
    		return null;
    	}
    }
    
    public static byte[] decodeParameterSecureBytes(String content) {
    	if (content == null || content.length() == 0) {
    		return null;
    	}
    	
    	int pad = content.length()%4;
    	switch(pad) {
    	case 1:
    		content = content + "===";
    		break;
    	case 2:
    		content = content + "==";
    		break;
    	case 3:
    		content = content + "=";
    		break;
    	}
    	
    	try {
	    	byte[] decoded = Base64Helper.decodeSecure(content.getBytes(), BASE64_URL_DEC);
	    	long checksum = ((long)decoded[0] & 0xff) 
							| (((long)decoded[1] & 0xff)<<8) 
							| (((long)decoded[2] & 0xff)<<16) 
							| (((long)decoded[3] & 0xff)<<24);
	    	long checksumMade = getCRC32Checksum(decoded, 4);
	    	
	    	//System.out.println("#  " + checksum);
	    	//System.out.println("## " + checksumMade);
	    	
	    	if (checksum != checksumMade) {
	    		return null;
	    	}
	    	
	    	byte[] resultBytes = new byte[decoded.length-4];
	    	System.arraycopy(decoded, 4, resultBytes, 0, resultBytes.length);
	    	
	    	return resultBytes;
    	}
    	catch(Exception ex) {
    		return null;
    	}
    }
    
    public static String decodeParameterSecure(String content) {
    	
    	byte[] resultBytes = decodeParameterSecureBytes(content);
    	
    	try {
	    	if (resultBytes != null) {
	    		return new String(resultBytes, "utf-8");
	    	}
	    	else {
	    		return null;
	    	}
    	}
    	catch(Exception ex) {
    		return null;
    	}
    }
    
    public static long decodeBase62Long(String value) {
		char checksumChar = value.charAt(0);
		int checksum = getIntegerOf(checksumChar);
		//System.out.println("checksum char = " + checksumChar + ", value = " + checksum);
		
		long sum = 0;
		long multiply = 1;
		for(int i=1; i<value.length(); i++) {
			char valueChar = value.charAt(i);
			int ivalue = getIntegerOf(valueChar);
			
			sum += ivalue * multiply;
			multiply = multiply * 62;
		}
		
		//System.out.println("sum = " + sum);
		
		sum = sum - BASE62_ADDUM;
		
		//System.out.println("decoded = " + sum);
		
		int checkverify = getChecksumForBase62Long(sum);
		if (checksum != checkverify) {
			return -1;
		}
		else {
			return sum;
		}
	}
    
    public static int decodeBase62Integer(String value) {
		char checksumChar = value.charAt(0);
		int checksum = getIntegerOf(checksumChar);
		//System.out.println("checksum char = " + checksumChar + ", value = " + checksum);
		
		int sum = 0;
		int multiply = 1;
		for(int i=1; i<value.length(); i++) {
			char valueChar = value.charAt(i);
			int ivalue = getIntegerOf(valueChar);
			
			sum += ivalue * multiply;
			multiply = multiply * 62;
		}
		
		//System.out.println("sum = " + sum);
		
		sum = sum - BASE62_ADDUM;
		
		//System.out.println("decoded = " + sum);
		
		int checkverify = getChecksumForBase62Integer(sum);
		if (checksum != checkverify) {
			return -1;
		}
		else {
			return sum;
		}
	}
	
	public static String encodeBase62Long(long value) {
		StringBuilder sb = new StringBuilder();
		
		// check sum 계산
		int checksum = getChecksumForBase62Long(value);
		sb.append(getCharacterOf(checksum));
		
		long division = value + BASE62_ADDUM;
		
		int modular = (int)(division%62);
		
		sb.append(getCharacterOf(modular));
		while(division >= 62) {
			division = division/62;
			
			modular = (int)(division%62);
			sb.append(getCharacterOf(modular));
			//System.out.println(modular);
		}
		
		return sb.toString();
	}
	
	public static String encodeBase62Integer(int value) {
		StringBuilder sb = new StringBuilder();
		
		// check sum 계산
		int checksum = getChecksumForBase62Integer(value);
		sb.append(getCharacterOf(checksum));
		
		int division = value + BASE62_ADDUM;
		
		int modular = division%62;
		
		sb.append(getCharacterOf(modular));
		while(division >= 62) {
			division = division/62;
			
			modular = division%62;
			sb.append(getCharacterOf(modular));
			//System.out.println(modular);
		}
		
		return sb.toString();
	}
	
	// 단축 URL용 checksum
	private static int getChecksumForBase62Long(long value) {
		long division = value;
		int checksum = 0;
		for(int i=0; i<19; i++) {
			checksum += division%10;
			division = division/10;
			if (division == 0) {
				break;
			}
		}
		
		return (checksum%62);
	}
	
	// 단축 URL용 checksum
	private static int getChecksumForBase62Integer(int value) {
		int division = value;
		int checksum = 0;
		for(int i=0; i<6; i++) {
			checksum += division%10;
			division = division/10;
			if (division == 0) {
				break;
			}
		}
		
		return checksum;
	}
	
	// 정수값에 해당되는 char
	private static char getCharacterOf(int value) {
		if (value < 10) {
			return (char)(48 + value); // '0' base
		}
		else if (value < 36) {
			return (char)(65 + (value-10)); // 'A' base
		}
		else {
			return (char)(97 + (value-36)); // 'a' base
		}
	}
	
	// char 의 정수값 
	private static int getIntegerOf(char value) {
		int ivalue = (int)value;
		if (ivalue >= 48 && ivalue <= 57) {
			return (ivalue - 48);
		}
		else if (ivalue >= 65 && ivalue <= 90) {
			return (ivalue - 65) + 10;
		}
		else if (ivalue >= 97 && ivalue <= 122) {
			return (ivalue - 97) + 36;
		}
		else {
			return -1;
		}
	}
	
    // CRC32 checksum 을 생성한다.
    private static long getCRC32Checksum(byte[] data, int offset) {
    	CRC32 checksum = new CRC32();
    	checksum.update(data, offset, data.length - offset);
    	
    	long crc = checksum.getValue();
    	//System.out.println(crc);
    	
    	return crc;
    }
}
