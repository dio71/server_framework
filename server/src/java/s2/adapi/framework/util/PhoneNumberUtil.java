package s2.adapi.framework.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import s2.adapi.framework.vo.ValueObject;

public class PhoneNumberUtil {
	private static final String KOREAN_NUMBER = "+82";
	
	private static int[] code0 = new int[] {20,12,29,22,11,9,34,20,10,2,12,35,20,23,16,9};
	private static int[] code1 = new int[] {15,33,25,11,36,39,24,13,36,24,16,13,37,38,1,5};
	private static int[] code2 = new int[] {11,34,22,26,12,26,36,3,25,18,20,18,4,26,17,25};
	private static int[] code3 = new int[] {2,27,19,4,4,6,38,29,12,6,18,17,24,36,25,14};
	private static int[] code4 = new int[] {36,27,3,21,25,28,38,9,13,4,37,24,20,1,19,29};
	private static int[] code5 = new int[] {19,34,23,39,19,5,35,16,17,17,8,33,4,1,20,20};
	private static int[] code6 = new int[] {11,37,38,30,9,35,29,15,33,27,23,11,2,27,20,4};
	private static int[] code7 = new int[] {18,33,27,12,30,6,28,7,38,15,32,33,30,30,28,20};
	private static int[] code8 = new int[] {25,2,3,30,24,35,7,9,22,29,14,27,13,39,30,4};
	private static int[] code9 = new int[] {32,31,9,4,16,4,21,34,28,7,36,26,21,17,18,10};
	private static int[] code10 = new int[] {13,36,12,37,14,20,12,19,22,29,14,27,34,27,18,25};
	private static int[] code11 = new int[] {28,38,22,31,1,38,34,24,14,9,9,32,5,27,18,29};
	private static int[] code12 = new int[] {19,34,35,38,28,19,34,15,39,11,10,36,12,30,32,24};
	private static int[] code13 = new int[] {6,29,18,12,9,22,18,2,8,1,15,14,39,28,20,26};
	private static int[] code14 = new int[] {23,36,10,25,37,18,23,24,9,35,7,6,35,24,27,17};
	private static int[] code15 = new int[] {11,16,32,4,17,7,6,27,14,23,33,21,18,5,11,25};
	private static String[] codeKey = new String[] {"$A","$B","$C","$D","$E","$F","$G","$H","$I","$J","$K","$L","$M","$N","$O","$P"};
	
	private static Map<String,int[]> codeMap = new HashMap<String,int[]>();
	
	private static Set<String> koreanMobilePhoneCode = new HashSet<String>();
	
	static {
		koreanMobilePhoneCode.add("010");
		koreanMobilePhoneCode.add("011");
		koreanMobilePhoneCode.add("016");
		koreanMobilePhoneCode.add("017");
		koreanMobilePhoneCode.add("018");
		koreanMobilePhoneCode.add("019");
	
		codeMap.put(codeKey[0], code0);
		codeMap.put(codeKey[1], code1);
		codeMap.put(codeKey[2], code2);
		codeMap.put(codeKey[3], code3);
		codeMap.put(codeKey[4], code4);
		codeMap.put(codeKey[5], code5);
		codeMap.put(codeKey[6], code6);
		codeMap.put(codeKey[7], code7);
		codeMap.put(codeKey[8], code8);
		codeMap.put(codeKey[9], code9);
		codeMap.put(codeKey[10], code10);
		codeMap.put(codeKey[11], code11);
		codeMap.put(codeKey[12], code12);
		codeMap.put(codeKey[13], code13);
		codeMap.put(codeKey[14], code14);
		codeMap.put(codeKey[15], code15);
	}
	
	public static boolean isKoreanMobilePhone(String phoneNumber) {
		if (phoneNumber == null || phoneNumber.length() < 10) {
			return false;
		} else {
			String phoneCode = phoneNumber.substring(0, 3);
			return koreanMobilePhoneCode.contains(phoneCode);
		}
	}
	
	/** 
	 * +82 로 시작하면 0 으로 바꿔 반환한다.
	 * @param phoneNumber
	 * @return
	 */
	public static String canonicalPhoneNumber(String phoneNumber) {
		if (phoneNumber == null) {
			return "";
		}
		if (phoneNumber.startsWith(KOREAN_NUMBER)) {
			return phoneNumber.replace(KOREAN_NUMBER, "0").replace("-", "");
		} else {
			return phoneNumber.replace("-", "");
		}
	}
	
	/**
	 * 숫자 이외의 문자는 제거한다.
	 * @param phoneNumber
	 * @return
	 */
	public static String stripPhoneNumber(String phoneNumber) {
		return phoneNumber.replace("-", "");
	}
	
	public static String formattedPhoneNumber(String phoneNumber) {
		String str = canonicalPhoneNumber(phoneNumber);
		if (str.length() < 3) {
			return str;
		}
		String firstdigit = null;
		if (str.startsWith("02")) {
			firstdigit = str.substring(0,2);
			str = str.substring(2);
		} else {
			firstdigit = str.substring(0,3);
			str = str.substring(3);
		}
		
		if (str.length() < 7) {
			return firstdigit + "-" + str;
		}
		String enddigit = str.substring(str.length()-4);
		String middigit = str.substring(0, str.length()-4);
		return firstdigit + "-" + middigit + "-" + enddigit;
	}
	
	public static String encode(long k, String number) {
		if (number == null) {
			return null;
		}
		int key = (int)(k%codeKey.length);
		String ckey = codeKey[key];
		int[] code = codeMap.get(ckey);
		
		byte[] b = new byte[number.length()+2];
		b[0] = (byte)ckey.charAt(0);
		b[1] = (byte)ckey.charAt(1);
		for(int i=0;i<number.length();i++) {
			b[i+2] = (byte)(((int)number.charAt(i)) + code[i%code.length]);
		}
		
		return new String(b);
	}
	
	public static String encode(String number) {
		return encode(System.currentTimeMillis(),number);
	}
	
	public static String decode(String number) {
		if (number == null || number.length() < 2) {
			return number;
		}
		String ckey = number.substring(0,2);
		int[] code = codeMap.get(ckey);
		if (code == null) {
			return number;
		} else {
			byte[] b = new byte[number.length()-2];
			for(int i=0;i<number.length()-2;i++) {
				b[i] = (byte)(((int)number.charAt(i+2)) - code[i%code.length]);
			}
			
			return new String(b);
		}
	}
	
	/**
	 * ValueObject의 주어진 컬럼을 decode 하여 담는다.
	 * @param paramVO
	 * @param fieldName
	 * @return
	 */
	public static void decodeValueObject(ValueObject paramVO, String fieldName) {
		for(int i=0; i<paramVO.size(); i++) {
			String phoneNumber = paramVO.getString(i,fieldName);
			paramVO.set(i, fieldName, decode(phoneNumber));
		}
	}
	
	/**
	 * ValueObject의 주어진 컬럼을 encode 하여 담는다.
	 * @param paramVO
	 * @param fieldName
	 */
	public static void encodeValueObject(ValueObject paramVO, String fieldName) {
		for(int i=0; i<paramVO.size(); i++) {
			String phoneNumber = paramVO.getString(i,fieldName);
			paramVO.set(i, fieldName, encode(i,phoneNumber));
		}
	}
}
