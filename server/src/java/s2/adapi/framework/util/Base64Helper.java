package s2.adapi.framework.util;

import java.io.UnsupportedEncodingException;

/**
 * Base64 Encoding/Decoding 기능 제공
 *
 */
public class Base64Helper {
	
	private static final byte[] BASE64_ENC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".getBytes();
	private static final byte[] BASE64_DEC = getDecodeKey(BASE64_ENC);
		
    protected Base64Helper() {
    }

    /**
     * RFC 2045에서 정의한 것과 같이 Base64 encode를 지원한다.
     *
     * @param content Base64 encoding 하고자 하는 Data
     * @return Base64 encoding된 Data
     */
    public static String encode(String content) {
    	return encodeSecure(content, BASE64_ENC);
    }

    /**
     * RFC 2045에서 정의한 것과 같이 Base64 encode를 지원한다.
     * encode할 byte[]를 생성할 때 charset을 지정한다.
     *
     * @param content Base64 encoding 하고자 하는 Data
     * @return Base64 encoding된 Data
     * @throws UnsupportedEncodingException 
     */
    public static String encode(String content, String charset) throws UnsupportedEncodingException {
    	return encodeSecure(content, charset, BASE64_ENC);
    }
    
    /**
     * RFC 2045에서 정의한 것과 같이 Base64 encode를 지원한다.
     *
     * @param content Base64 encoding 하고자 하는 binary의 byte array
     * @return Base64 encoding된 문자열의 byte array
     */
    public static byte[] encode(byte[] content) {
    	return encodeSecure(content, BASE64_ENC);
    }
    
    /**
     * RFC 2045에서 정의한 것과 같이 Base64 decode를 지원한다.
     *
     * @param content Base64 decoding 하고자 하는 Data
     * @return Base64 decoding된 Data
     */
    public static String decode(String content) {
    	return decodeSecure(content, BASE64_DEC);
    }

    /**
     * RFC 2045에서 정의한 것과 같이 Base64 decode를 지원한다.
     * byte[]를 String으로 변환할 때 사용할 charset을 지정한다.
     *
     * @param content Base64 decoding 하고자 하는 Data
     * @return Base64 decoding된 Data
     */
    public static String decode(String content, String charset) throws UnsupportedEncodingException {
    	return decodeSecure(content, charset, BASE64_DEC);
    }
    
    /**
     * RFC 2045에서 정의한 것과 같이 Base64 decode를 지원한다.
     *
     * @param content Base64 decoding 하고자 하는 문자열의 byte array
     * @return Base64 decoding된 binary의 byte array
     */
    public static byte[] decode(byte[] content) {
    	return decodeSecure(content, BASE64_DEC);
    }
    
    /**
     * Base64 encoding된 Data와 실 Data의 비교.
     *
     * @param content         실 Data
     * @param enccodedContent Base64 encoding된 Data
     * @return 검증
     */
    public static boolean isValid(String content, String encodedContent) {
        if (encodedContent.equals(encode(content))) {
            return true;
        } else {
            return false;
        }

    }
    
    /**
     * keycode 배열을 다르게하는 방식으로 암호화 Base64 인코딩을 처리한다.
     * 디코딩할 때에는 같은 keycode 배열을 사용해야 원본 데이터를 얻을 수 있다.
     * @param content
     * @param charset
     * @param encodeKey Base64 인코딩용 문자배열 (65 bytes length)
     * 예)"GHIJuv45h7x6D3NOPQREFyz012STpqUVWXYZabc89+/KLMwdefgABCijklmnorst=".getBytes()
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String encodeSecure(String content, String charset, byte[] encodeKey) 
    		throws UnsupportedEncodingException {

        return new String(encodeSecure(content.getBytes(charset),encodeKey));
    } 
    
    public static String encodeSecure(String content, byte[] encodeKey) {
    	
    	return new String(encodeSecure(content.getBytes(), encodeKey));
    }
    
    public static byte[] encodeSecure(byte[] content, byte[] encodeKey) {
    	if (encodeKey == null || encodeKey.length != 65) {
    		throw new IllegalArgumentException("invalid encode key.");
    	}
    	
    	int iLen = content.length;
    	int oLen = ((iLen+2)/3)*4; // output bytes include padding
    	int oDataLen = (iLen*4+2)/3; // output bytes without padding
    	
    	byte[] out = new byte[oLen];
    	
    	int idx = 0;
    	int odx = 0;

    	while(idx < iLen) {
    		int in0 = content[idx++] & 0xff;
    		int in1 = idx < iLen ? content[idx++] & 0xff : 0;
    		int in2 = idx < iLen ? content[idx++] & 0xff : 0;
    		
    		int out0 = in0 >>> 2;
    		int out1 = ((in0 & 3) << 4) | (in1 >>> 4);
    		int out2 = ((in1 & 15) << 2) | (in2 >>> 6);
    		int out3 = in2 & 63;
    		
    		out[odx++] = encodeKey[out0];
    		out[odx++] = encodeKey[out1];
    		if (odx < oDataLen) {
    			out[odx++] = encodeKey[out2];
    		} else {
    			out[odx++] = encodeKey[64]; // '=';
    		}
    		if (odx < oDataLen) {
    			out[odx++] = encodeKey[out3];
    		} else {
    			out[odx++] = encodeKey[64]; // '=';
    		}
    	}
    	
    	return out;
    }
    
    /**
     * keycode 배열을 다르게하는 방식으로 암호화 Base64 디코딩을 처리한다.
     * 디코딩할 때에는 인코딩할 때와 같은 keycode 배열을 사용해야 원본 데이터를 얻을 수 있다.
     * @param content
     * @param charset
     * @param decodeKey Base64 디코딩용 문자배열 (64 bytes length), 인코딩 시 사용한 Key로부터
     * getDecodeKey() 메소드를 사용하여 생성할 수 있다.
     * 예)Base64Helper.getDecodeKey("GHIJuv45h7x6D3NOPQREFyz012STpqUVWXYZabc89+/KLMwdefgABCijklmnorst".getBytes());
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String decodeSecure(String content, String charset, byte[] decodeKey) 
    		throws UnsupportedEncodingException {
    	
    	return new String(decodeSecure(content.getBytes(),decodeKey),charset);
    }
    
    public static String decodeSecure(String content, byte[] decodeKey) {
    	return new String(decodeSecure(content.getBytes(),decodeKey));
    }
    
    public static byte[] decodeSecure(byte[] content, byte[] decodeKey) {
    	if (decodeKey == null || decodeKey.length != 128) {
    		throw new IllegalArgumentException("invalid decode key.");
    	}
    	
    	content = stripInvalidData(content, decodeKey);
    	
    	int iLen = content.length;
    	
    	if (iLen == 0) {
    		return new byte[0];
    	}
    	int oLen = (iLen*3)/4;
    	byte[] output = new byte[oLen];
		
    	int[] dataBuffer = new int[4];
    	int[] outputBuffer = new int[3];
    	
    	int odx = 0;

    	for (int i=0; i < iLen; i += 4) {
			for (int j = 0; j < 4 && i + j < iLen; j++) {
				dataBuffer[j] = decodeKey[content[i + j]];
			}
  			
			outputBuffer[0] = (dataBuffer[0] << 2) + ((dataBuffer[1] & 0x30) >> 4);
			outputBuffer[1] = ((dataBuffer[1] & 0x0f) << 4) + ((dataBuffer[2] & 0x3c) >> 2);		
			outputBuffer[2] = ((dataBuffer[2] & 0x03) << 6) + dataBuffer[3];
			
			for (int k = 0; k < outputBuffer.length; k++) {
				if (dataBuffer[k+1] == 64) break;
				output[odx++] = (byte)outputBuffer[k];
			}
		}
		
    	byte[] out = new byte[odx];
    	System.arraycopy(output, 0, out, 0, odx);
    	
		return out;
    }
    
    public static byte[] getDecodeKey(String encodeKey) {
    	return getDecodeKey(encodeKey.getBytes());
    }
    
    /**
     * encode용 keycode 테이블로부터 decode용 keycode 테이블을 생성한다.
     * @param encodeKey (64 bytes)
     * @return 128 bytes length 
     */
    public static byte[] getDecodeKey(byte[] encodeKey) {
    	byte[] reverse = new byte[128];
    	for(int i=0; i<reverse.length; i++) {
    		reverse[i] = (byte) -1;
    	}
    	for(int i=0;i<=64;i++) {
    		reverse[encodeKey[i]] = (byte)i;
    	}
    	
    	return reverse;
    }
    
    private static byte[] stripInvalidData(byte[] data, byte[] decodeKey) {
    	byte[] striped = new byte[data.length];
    	int k = 0;
    	for (int i = 0; i < data.length; i++) {
    		if (isValid(data[i], decodeKey)) {
    			striped[k++] = data[i];
    		}
    	}
    	
    	byte[] packed = new byte[k];
    	
    	System.arraycopy(striped, 0, packed, 0, k);
    	
    	return packed;
    }
    private static boolean isValid(byte octect, byte[] decodeKey) {
    	if (decodeKey[octect] == -1	) {
    		return false;
    	}
    	else {
    		return true;
    	}
    }
}
