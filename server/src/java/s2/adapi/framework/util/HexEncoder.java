package s2.adapi.framework.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 간단한 String encoding 및 암호화 기능들을 제공한다.
 * @author kimhd
 *
 */
public class HexEncoder {
	private static final String CHECK_CODE = "s2_adapi_AD_2024";
	private static final char[] HEX_CODE = new char[] {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	private static final long[] ASC_CODE = new long[] {0,1,2,3,4,5,6,7,8,9,0,0,0,0,0,0,0,10,11,12,13,14,15,
			0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,10,11,12,13,14,15};
	
	@Deprecated
	public String getPrivateKey(long n) {
		String code = CHECK_CODE + n;
		return DigesterHelper.md5Hex(code);
	}
	
	@Deprecated
	public boolean verifyPrivateKey(long n, String checkCode) {
		String code = getPrivateKey(n);
		if (code.equals(checkCode)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * PPI 등록된 Application ID를 생성하고 확인하기 위하여 사용된다.
	 * decode는 encode()를 통하여 생성된 문자열로 부터 원래의 long 값을 추출한다. 
	 * 
	 * Deprecated 되었다. 대신에 decodeApplicationHexId() 사용한다.
	 * @param a
	 * @return
	 */
	@Deprecated
	public long decode(String a) {
		long ret = 0L;
		long chk = 0L;
		
		if (a != null && a.length() == 32) {
			ret |= ASC_CODE[(int)a.charAt(0) - 48] ;
			chk |= ASC_CODE[(int)a.charAt(1) - 48] << 60;
			ret |= ASC_CODE[(int)a.charAt(2) - 48] << 4;
			chk |= ASC_CODE[(int)a.charAt(3) - 48] << 56;
			ret |= ASC_CODE[(int)a.charAt(4) - 48] << 8;
			chk |= ASC_CODE[(int)a.charAt(5) - 48] << 52;
			ret |= ASC_CODE[(int)a.charAt(6) - 48] << 12;
			chk |= ASC_CODE[(int)a.charAt(7) - 48] << 48;
			ret |= ASC_CODE[(int)a.charAt(8) - 48] << 16;
			chk |= ASC_CODE[(int)a.charAt(9) - 48] << 44;
			ret |= ASC_CODE[(int)a.charAt(10) - 48] << 20;
			chk |= ASC_CODE[(int)a.charAt(11) - 48] << 40;
			ret |= ASC_CODE[(int)a.charAt(12) - 48] << 24;
			chk |= ASC_CODE[(int)a.charAt(13) - 48] << 36;
			ret |= ASC_CODE[(int)a.charAt(14) - 48] << 28;
			chk |= ASC_CODE[(int)a.charAt(15) - 48] << 32;
			ret |= ASC_CODE[(int)a.charAt(16) - 48] << 32;
			chk |= ASC_CODE[(int)a.charAt(17) - 48] << 28;
			ret |= ASC_CODE[(int)a.charAt(18) - 48] << 36;
			chk |= ASC_CODE[(int)a.charAt(19) - 48] << 24;
			ret |= ASC_CODE[(int)a.charAt(20) - 48] << 40;
			chk |= ASC_CODE[(int)a.charAt(21) - 48] << 20;
			ret |= ASC_CODE[(int)a.charAt(22) - 48] << 44;
			chk |= ASC_CODE[(int)a.charAt(23) - 48] << 16;
			ret |= ASC_CODE[(int)a.charAt(24) - 48] << 48;
			chk |= ASC_CODE[(int)a.charAt(25) - 48] << 12;
			ret |= ASC_CODE[(int)a.charAt(26) - 48] << 52;
			chk |= ASC_CODE[(int)a.charAt(27) - 48] << 8;
			ret |= ASC_CODE[(int)a.charAt(28) - 48] << 56;
			chk |= ASC_CODE[(int)a.charAt(29) - 48] << 4;
			ret |= ASC_CODE[(int)a.charAt(30) - 48] << 60;
			chk |= ASC_CODE[(int)a.charAt(31) - 48];
		}
		
		return (ret - chk);
	}
	
	/**
	 * PPI 등록된 Application ID를 생성하고 확인하기 위하여 사용된다.
	 * encode 는 long 값을 문자열로 변환한다. 내부적으로 dummy 코드로 현재 시간값을 포함시켜 random 하게 생성되게 한다.  
	 * Deprecated 되었다. 대신에 encodeApplicationHexId()를 사용한다.
	 * 
	 * @param n 인코딩할 long 값
	 * @return
	 */
	@Deprecated
	public String encode(long n) {
		StringBuilder sb = new StringBuilder(32);
		long d = System.currentTimeMillis();
		
		n += d;
		int i;
		
		i = (int)(n & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>60) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((n>>4) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>56) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((n>>8) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>52) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((n>>12) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>48) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((n>>16) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>44) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((n>>20) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>40) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((n>>24) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>36) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((n>>28) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>32) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((n>>32) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>28) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((n>>36) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>24) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((n>>40) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>20) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((n>>44) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>16) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((n>>48) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>12) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((n>>52) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>8) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((n>>56) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>4) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((n>>60) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d) & 0x0f);
		sb.append(HEX_CODE[i]);
		
		return sb.toString();
	}
	
	/**
	 * PPI로 등록할 Application Hex ID를 생성하고 확인하기 위하여 사용된다.
	 * decode는 encode()를 통하여 생성된 Hex 문자열로 부터 원래의 long 값을 추출한다. 
	 * @param a
	 * @return
	 */
	public static long decodeApplicationHexId(String a) {
		long ret = 0L;
		long chk = 0L;
		
		if (a != null && a.length() == 32) {
			ret |= ASC_CODE[(int)a.charAt(0) - 48] ;
			chk |= ASC_CODE[(int)a.charAt(1) - 48] << 60;
			ret |= ASC_CODE[(int)a.charAt(2) - 48] << 4;
			chk |= ASC_CODE[(int)a.charAt(3) - 48] << 56;
			ret |= ASC_CODE[(int)a.charAt(4) - 48] << 8;
			chk |= ASC_CODE[(int)a.charAt(5) - 48] << 52;
			ret |= ASC_CODE[(int)a.charAt(6) - 48] << 12;
			chk |= ASC_CODE[(int)a.charAt(7) - 48] << 48;
			ret |= ASC_CODE[(int)a.charAt(8) - 48] << 16;
			chk |= ASC_CODE[(int)a.charAt(9) - 48] << 44;
			ret |= ASC_CODE[(int)a.charAt(10) - 48] << 20;
			chk |= ASC_CODE[(int)a.charAt(11) - 48] << 40;
			ret |= ASC_CODE[(int)a.charAt(12) - 48] << 24;
			chk |= ASC_CODE[(int)a.charAt(13) - 48] << 36;
			ret |= ASC_CODE[(int)a.charAt(14) - 48] << 28;
			chk |= ASC_CODE[(int)a.charAt(15) - 48] << 32;
			ret |= ASC_CODE[(int)a.charAt(16) - 48] << 32;
			chk |= ASC_CODE[(int)a.charAt(17) - 48] << 28;
			ret |= ASC_CODE[(int)a.charAt(18) - 48] << 36;
			chk |= ASC_CODE[(int)a.charAt(19) - 48] << 24;
			ret |= ASC_CODE[(int)a.charAt(20) - 48] << 40;
			chk |= ASC_CODE[(int)a.charAt(21) - 48] << 20;
			ret |= ASC_CODE[(int)a.charAt(22) - 48] << 44;
			chk |= ASC_CODE[(int)a.charAt(23) - 48] << 16;
			ret |= ASC_CODE[(int)a.charAt(24) - 48] << 48;
			chk |= ASC_CODE[(int)a.charAt(25) - 48] << 12;
			ret |= ASC_CODE[(int)a.charAt(26) - 48] << 52;
			chk |= ASC_CODE[(int)a.charAt(27) - 48] << 8;
			ret |= ASC_CODE[(int)a.charAt(28) - 48] << 56;
			chk |= ASC_CODE[(int)a.charAt(29) - 48] << 4;
			ret |= ASC_CODE[(int)a.charAt(30) - 48] << 60;
			chk |= ASC_CODE[(int)a.charAt(31) - 48];
		}
		
		return (ret - chk);
	}
	
	/**
	 * PPI로 등록할 Application Hex ID를 생성하고 확인하기 위하여 사용된다.
	 * encode는 long 값을 문자열로 변환한다. 내부적으로 dummy 코드로 현재 시간값을 포함시켜 random하게 생성되게 한다.  
	 * @param appId 인코딩할 appId (long 값)
	 * @return
	 */
	public String encodeApplicationHexId(long appId) {
		StringBuilder sb = new StringBuilder(32);
		long d = System.currentTimeMillis();
		
		appId += d;
		int i;
		
		i = (int)(appId & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>60) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((appId>>4) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>56) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((appId>>8) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>52) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((appId>>12) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>48) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((appId>>16) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>44) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((appId>>20) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>40) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((appId>>24) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>36) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((appId>>28) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>32) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((appId>>32) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>28) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((appId>>36) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>24) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((appId>>40) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>20) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((appId>>44) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>16) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((appId>>48) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>12) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((appId>>52) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>8) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((appId>>56) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d>>4) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((appId>>60) & 0x0f);
		sb.append(HEX_CODE[i]);
		i = (int)((d) & 0x0f);
		sb.append(HEX_CODE[i]);
		
		return sb.toString();
	}
	
	/**
	 * Application Id 값에서  Application Key 를 생성한다.
	 * @param appId
	 * @return
	 */
	public static String getApplicationKey(long appId) {
		String code = CHECK_CODE + appId;
		return DigesterHelper.md5Hex(code);
	}
	
	public static boolean verifyApplicationKey(long appId, String checkCode) {
		String code = getApplicationKey(appId);
		if (code.equals(checkCode)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * hex to byte[]
	 * @param hex
	 * @return
	 */
	public static byte[] hexToByteArray(String hex) {
		if (hex == null || hex.length() == 0) {
			return null;
		}
		
		byte[] ba = new byte[hex.length()/2];
		int j=0;
		for (int i = 0; i < ba.length; i++) {
			ba[i] = (byte)((int)((ASC_CODE[hex.charAt(j++) - 48] << 4) & 0xf0) 
					          | ((ASC_CODE[hex.charAt(j++) - 48]) & 0x0f));
		}
		return ba;
	} 
	
	/**
	 * byte[] to hex
	 * @param ba
	 * @return
	 */
	public static String byteArrayToHex(byte[] ba, int offset, int len) {
	    if (ba == null || len == 0 || ba.length < offset + len) {
	    	return null;
	    }
	    
	    StringBuilder sb = new StringBuilder(len * 2);
		for (int x = offset; x < offset + len; x++) {
			sb.append(HEX_CODE[(ba[x] >> 4) & 0x0f]);
			sb.append(HEX_CODE[ba[x] & 0x0f]);
		}
		
		return sb.toString();
	}
	
	public static String byteArrayToHex(byte[] ba) {
		if (ba == null) {
			return null;
		}
		
		return byteArrayToHex(ba, 0, ba.length);
	}
	
	/*
	 * 단말기 고유 식별자를 암호화하여 UDID 를 만든다.
	 * UDID 는  userid 와는 다르나 외부 시스템 과 연계할 경우 단말기 고유식별자를 전달해야 할 경우 사용된다.
	 * 단말기 고유 식별자는 다음과 같다.
	 * Android 폰 : getDeviceId()
	 * Android wifi 기기 : "wf" + macaddress에서 ":" 제거한 값, 소문자 사용
	 * iOS : macaddress를 그대로 Hex String으로 만든 값, 소문자 사용 (예시 : "30303a43363a31303a34463a31313a3131")
	 */
	@Deprecated
	public static String makeUDID(String deviceId) {
		if (deviceId.startsWith("TNK") || deviceId.startsWith("tnk")) {
			return deviceId;
		}
		
		String udid = deviceId;
		try {
			int len = deviceId.length();
			
			StringBuilder sb = new StringBuilder();
			sb.append(HEX_CODE[(len>>4)&0x0f]).append(HEX_CODE[len&0x0f]);
			sb.append("A").append(deviceId);
			
			if (len < 34) {
				sb.append(DigesterHelper.md5Hex(deviceId));
			}
			
			udid = encrypt(sb.toString().substring(0, 37));
		} catch (Exception ex) {
		}
		
		return udid;
	}
	
	@Deprecated
	public static String extractDeviceId(String udid) {
		if (udid.startsWith("TNK") || udid.startsWith("tnk")) {
			return udid;
		}
		
		String exId = decrypt(udid);
		
		int idlen = Integer.parseInt(exId.substring(0,2), 16);
		
		return exId.substring(3, 3+idlen);
	}
	
	public static String extractUserId(String udid) {
		return extractDeviceId(udid).toLowerCase();
	}
	
	/**
	 * MDID : MD5 해시 기반의 새로운 사용자 구분값
	 * - 안드로이드
	 *  p + MD5(IMEI)
	 *  w + MD5(mac address 대문자)
	 *  g + MD5(android ID)
	 *  t + temp ID (Emulator 용)
	 * - 아이폰
	 *  i + IDfA 의 "-" 제거 및 소문자
	 *  m + MD5(mac address 대문자)
	 *   
	 * @param udid
	 * @return
	 */
	@Deprecated
	public static String makeMDID(String udid) {
		String deviceId = HexEncoder.extractDeviceId(udid);
		
		if (deviceId.length() == 34) { // hex bytes of mac address
			// ios deviceId (mac-address)
			deviceId = new String(HexEncoder.hexToByteArray(deviceId));
			return "m" + DigesterHelper.md5Hex(deviceId.toUpperCase());
		}
		else if (deviceId.length() == 32) {  // ios deviceId (IdfA)
			// "i" + idfa 값이 그대로 mdid로 사용된다.
			return "i" + deviceId;
		}
		else if (deviceId.startsWith("wf")) { // mac-address
			// android wifi device
			StringBuilder sb = new StringBuilder();
			sb.append(deviceId.substring(2, 4)).append(":");
			sb.append(deviceId.substring(4, 6)).append(":");
			sb.append(deviceId.substring(6, 8)).append(":");
			sb.append(deviceId.substring(8, 10)).append(":");
			sb.append(deviceId.substring(10, 12)).append(":");
			sb.append(deviceId.substring(12, 14));
			
			deviceId = sb.toString().toUpperCase();
			return "w" + DigesterHelper.md5Hex(deviceId);
		}
		else if (deviceId.startsWith("aid")) { // google android Id
			return "g" + DigesterHelper.md5Hex(deviceId.substring(3));
		}
		else if (deviceId.startsWith("tnk") || deviceId.startsWith("TNK")) { // tnk's temporary generated id for emulator
			return "t" + deviceId.substring(3);
		}
		else {
			// android phone (IMEI)
			return "p" + DigesterHelper.md5Hex(deviceId);
		}	
	}
	
	//----------------------------------
	// 문자열 교환방식의 암호화 로직
	//----------------------------------
	private static final String BASE_DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final String ENC_DATA  = "WxyXYZ12vwqrstuDE86fghijFG90abopPzABC345HIdeJKLMNO7klmnRSTQcUV";
	private static final String DEC_DATA  = "R67bcdIoHQYZaFGOPefijklmnWwtuvyz0345STxghJKLMNpqrsUVABCDE8912X";
	
	/**
	 * Encrypt simple alpha-numeric String
	 * @param hex
	 * @return
	 */
	public static String encrypt(String hex) {
		if (hex == null) {
			return null;
		}
		byte[] data = hex.trim().getBytes();
		for(int i=0; i<data.length; i++) {
			data[i] = encodeAt(data[i],i);
		}
		
		return new String(data);
	}
	
	/**
	 * Decrypt simple alpha-numeric String
	 * @param hex
	 * @return
	 */
	public static String decrypt(String hex) {
		if (hex == null) {
			return null;
		}
		byte[] data = hex.trim().getBytes();
		for(int i=0; i<data.length; i++) {
			data[i] = decodeAt(data[i],i);
		}
		
		return new String(data);
	}
	
	/**
	 * char to integer within 0 ~ 62 (BASE_DATA의 순서대로)
	 * @param i
	 * @return
	 */
	private static int getIndex(int i) {
		if (i >= 48 && i <= 57) {
			return (i-48);
		}
		else if (i >= 65 && i <= 90) {
			return (i-65+10);
		}
		else {
			return (i-97+10+26);
		}
	}
	
	private static byte encodeAt(byte b, int i) {
		int index = getIndex(b);
		return (byte)ENC_DATA.charAt((index+i)%62);
	}
	
	private static byte decodeAt(byte b, int i) {
		int index = getIndex(b);
		b = (byte)DEC_DATA.charAt(index);
		
		index = (getIndex(b) - i + 62)%62;
		
		return (byte)BASE_DATA.charAt(index);
	}
	
	//------------------------------
	// AES 방식의 암호화 로직
	//------------------------------
    public static String key = "t9ndklfoawcltoohrwyeahdtztzelge1";
    
	/**
	 * AES 방식으로 문자열을 암호화한다.
	 * @param message
	 * @return
	 * @throws Exception
	 */
    public static byte[] encryptAESBytes(String message) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        
        return cipher.doFinal(message.getBytes());      
    }

    public static String encryptAES(String message) throws Exception {
        return byteArrayToHex(encryptAESBytes(message));        
    }
    
    /**
     * AES 방식으로 문자열을 복호화한다.
     * @param encrypted
     * @return
     * @throws Exception
     */
    public static String decryptAESBytes(byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        
        byte[] original = cipher.doFinal(encrypted);
        String originalString = new String(original);
        
        return originalString;
    }
    
    public static String decryptAES(String encrypted) throws Exception {
        return decryptAESBytes(hexToByteArray(encrypted));
    }
}
