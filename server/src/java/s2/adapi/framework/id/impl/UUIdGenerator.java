package s2.adapi.framework.id.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.id.IdGenerator;
import s2.adapi.framework.util.StringHelper;


/**
 * UUID를 생성하는 클래스이다. UUID의 구조는 다음과 같다.
 * <pre>
 *  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31
 * | currentTimeMillis  |    IP Address        |  Unique Object Hashcode  |     Random number     |
 * </pre>
 * @author 김형도
 * @since 4.0
 */
public class UUIdGenerator implements IdGenerator {
    /**
     * <p>
     * 에러나 이벤트와 관련된 각종 메시지를 로깅하기 위한 Log 오브젝트
     * </p>
     */
    private static final Logger log = LoggerFactory.getLogger(UUIdGenerator.class);
    
    private static UUIdGenerator singleInstance = new UUIdGenerator();
    
	private SecureRandom seeder;
	
	private String hexInetAddress = null;
	private String thisHashCode = null;
	
	public UUIdGenerator() {
		hexInetAddress = toHexString(0,8);
		thisHashCode = toHexString(System.identityHashCode(this),8);
		seeder = new SecureRandom();
		seeder.setSeed(System.currentTimeMillis());
		seeder.nextInt();
		
		try {
			InetAddress inet = InetAddress.getLocalHost();
			byte [] bytes = inet.getAddress();
			hexInetAddress = toHexString(intValue(bytes),8);
			
		} catch (UnknownHostException e) {
			if ( log.isErrorEnabled() ) {
				log.error("",e);
			}
		}
	}
	
	public static UUIdGenerator getInstance() {
		return singleInstance;
	}
	
	public String getNextId() {
		return getNextId(Long.valueOf(System.currentTimeMillis()), true);
	}
	
	public String getNextId(Object key) {
		return getNextId(key,true);
	}
	
	/**
	 * UUID를 생성하여 리턴한다. {@link s2.adapi.framework.id.IdGenerator} 인터페이스의 구현 메소드이다.
	 * 
	 */
	public String getNextId(Object key, boolean format) {
		String keyHashCode = null;
		if ( key == null) {
			keyHashCode = thisHashCode;
		} 
		else {
			keyHashCode = toHexString(System.identityHashCode(key),8);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(toHexString((int)(System.currentTimeMillis() & 0xffffffff),8));
		sb.append(hexInetAddress);
		sb.append(keyHashCode);
		sb.append(toHexString(seeder.nextInt(),8));
		
		if (format) {
			sb.insert(8,"-");
	        sb.insert(13,"-");
	        sb.insert(18,"-");
	        sb.insert(23,"-");
		}
        
		return sb.toString().toUpperCase();
	}

	/**
	 * 정수값을 16진수 표현으로 변환한다. 빈자리는 0으로 채운다.
	 * @param i
	 * @param len
	 * @return
	 */
	private String toHexString(int i, int len) {
		String hex = Integer.toHexString(i);
		return StringHelper.lpad(hex,len, '0');
	}
	
	/**
	 * 4 byte를 32-bit 정수형으로 변환한다.
	 * @param b
	 * @return
	 */
	private int intValue(byte b[]) {
		int ival = 0;
		int shift = 0;
		for(int i=0;i<4;i++) {
			ival += (b[i] & 0xff)<<shift;
			shift += 8;
		}
		return ival;
	}
}
