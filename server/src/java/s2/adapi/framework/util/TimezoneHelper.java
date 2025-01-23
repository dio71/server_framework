package s2.adapi.framework.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import s2.adapi.framework.Constants;
import s2.adapi.framework.config.ConfiguratorFactory;

public class TimezoneHelper {

	private static final long ONE_HOUR_MILLIS = 1000L*60*60;
	
	private static int systemTimezone = 0;
	
	private static Timezone timezoneSingleton = null;
    static {
        configure();
    }

    protected static void configure() {
        //
        // Configurator 오브젝트를 통하여 구성화일명을 얻는다.
        //
        try {
        	timezoneSingleton = new Timezone();
        	
        	TimeZone tz = TimeZone.getDefault();
    		
    		long millis  = tz.getOffset(System.currentTimeMillis());
    		systemTimezone = (int)(millis/(1000L*60*60)); // 우선 시스템의 타임존으로 설정한다.
    		
    		// 별도 설정파일로 정의된 타임존이 있는 경우 그 값으로 변경한다.
    		systemTimezone = ConfiguratorFactory.getConfigurator()
            		.getInt(Constants.CONFIG_SYSTEM_TIMEZONE_KEY, systemTimezone); 
        }
        catch (Exception ex) {
        }
    }
    
    /**
     * 현재 설정되어 사용중인 시스탬의 타임존 값이다.  (KST : 9, UCT : 0  등)
     * @return
     */
    public static int getTimezone() {
    	return systemTimezone;
    }
    
    /**
     * 현지 시간 값으로 시스템의 Timestamp 값을 구한다.
     * 예) 시스템 타임존이 +9 (한국)일때  영국(UTC)에서 20150617 자정시간의  System.millis  값 구하기. 
     * 
     * 20150617  시간의 millis  를 구하면 이 값은 한국시간으로 자정이다. 영국시간으로 자정은 한국시간으로는 +9시이므로 9시간을 더해주어야한다.
     * @param dateString 현지의 시간
     * @param formatString
     * @param timezone 현지의 타임존
     * @return
     * @throws ParseException
     */
    public static long getSystemMillis(String dateString, String formatString, int timezone) throws ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat(formatString);
    	Date date = sdf.parse(dateString);
    	
    	int timezoneDiff = systemTimezone - timezone; 
    	long millis = date.getTime() + timezoneDiff*ONE_HOUR_MILLIS; 
    	
    	return millis;
    }
    
    /**
     * 시스템의 millis 값으로 현지시간 기준의 날짜 문자열을 생성한다.
     * @param systemMillis 시스템의 시간(millis)
     * @param formatString
     * @param timezone 현지의 타임존
     */
    public static String getDateString(long systemMillis, String formatString, int timezone) {
    	SimpleDateFormat sdf = new SimpleDateFormat(formatString);
    	
    	int timezoneDiff = systemTimezone - timezone; 
    	long millis = systemMillis - timezoneDiff*ONE_HOUR_MILLIS;
    	
    	return sdf.format(new Date(millis));
    }
    
    /**
     * 현지의 시(hour)을 시스템의 시(hour)로 변환한다.
     * @param hour 현지의 시
     * @param timezone 현지의 타임존
     * @return
     */
    public static int getSystemHour(int hour, int timezone) {
    	return (hour + (systemTimezone - timezone) + 24)%24;
    }
    
    /**
     * 시스템의 시(hour)를 현지의 시(hour)로 변환한다.
     * @param hour 시스템의 시간
     * @param timezone 현지의 타임존
     * @return
     */
    public static int getTimezoneHour(int hour, int timezone) {
    	return (hour - (systemTimezone - timezone) + 24)%24;
    }
    
    /**
     * 현지의 현재시(hour) 을 반환한다.
     * 
     * @param timezone 현지의 타임존
     * @return
     */
    public static int getCurrentHour(int timezone) {
    	int hour = (int)(System.currentTimeMillis()/(1000*60*60))%24;
		int tzhour = (hour + timezone + 24)%24;
		
		return tzhour;
    }
    
    /**
     * 시스템의 타임존 기준으로 현재시를 반환한다.
     * @return
     */
    public static int getCurrentHour() {
    	return getCurrentHour(systemTimezone);
    }
    
    /**
     * 주어진 타임존의 현재 요일을 반환한다.
     * @param tzInt
     * @param startAtMonday : 한주의 시작이 월요일인 경우 true, 일요일이라면 false
     * @return 1(월), 2(화) ~ 7(일) 또는 1(일), 2(월) ~ 6(토), -1은 오류발생
     */
	public static int getCurrentWeek(int tzInt, boolean startAtMonday)
    {
		String[] tzIds = TimeZone.getAvailableIDs(tzInt * 60 * 60 * 1000);
		if (tzIds.length == 0) {
			return -1;
		}
	 
//		// 테스트 코드 : 타임존 출력
//		for(int i = 0; i < tzIds.length; i++) {
//		   System.out.println(tzIds[i]);
//		}
		 
	    TimeZone tz = TimeZone.getTimeZone(tzIds[0]);
	     
//	    // 테스트 코드 : offset 출력
//	    int rawOffset = tz.getRawOffset()/(60*60*1000);
//	    System.out.println(rawOffset);
	     
	    Calendar cal = Calendar.getInstance(tz);
	    
	    // Calendar 에서의 리턴값은 일요일이 1 이다.
	    if (startAtMonday) {
	    	return (cal.get(Calendar.DAY_OF_WEEK) + 5)%7 + 1;
	    }
	    else {
	    	return cal.get(Calendar.DAY_OF_WEEK);
	    }
	     
    }
	
	/**
	 * 한주의 시작을 월요일로하여 주어진 타임존에서 현재 요일을 반환한다.
	 * @param tzInt
	 * @return
	 */
	public static int getCurrentWeek(int tzInt) {
		return getCurrentWeek(tzInt, true);
	}
	
	/**
	 * 시스템 타임존으로 현재 요일을 반환한다.
	 * @return
	 */
	public static int getCurrentWeek() {
		return getCurrentWeek(systemTimezone, true);
	}
	
    /**
     * 
     * 특정 타임존에서의 특정시간이 다른 타임존 지역에서는 몇시인지 반환한다.
     * 날짜 offset 값도 같이 계산하여 반환된다.
     * 
     * @param fromHour
     * @param fromTimezone
     * @param targetTimezone
     * @return
     */
    public static HourInfo getTimezoneHourInfo(int fromHour, int fromTimezone, int targetTimezone) {
    	return timezoneSingleton.getTimezoneHourInfo(fromHour, fromTimezone, targetTimezone);
    }
        
	/**
	 * 주어진 시간과 타임존이 시스템타임존 기준으로 몇시인지 반환한다.
	 * 날짜 offset 값도 같이 계산하여 반환된다.
	 * 
	 * @param localHour
	 * @param localTimezone
	 * @return
	 */
    public static HourInfo getSystemHourInfo(int localHour, int localTimezone) {
		return timezoneSingleton.getTimezoneHourInfo(localHour, localTimezone, systemTimezone);
	}
	
    /**
     * 시스템 시간이 localTimezone 에서는 몇시인지 반환한다.
     * 날짜 offset 값도 같이 계산하여 반환된다.
     * 
     * @param sysHour
     * @param localTimezone
     * @return
     */
	public static HourInfo getLocalHourInfo(int sysHour, int localTimezone) {
		return timezoneSingleton.getTimezoneHourInfo(sysHour, systemTimezone, localTimezone);
	}
	
    // 시간과 해당 시간이 기준일의 전날(-1)인지 같은 날(0)인지 다음날(1)인지를 저장
	public static class HourInfo {
		public int hour;
		public int bias;
		
		public HourInfo(int h, int b) {
			hour = h;
			bias = b;
		}
		
		public String toString() {
			String mark = "";
			switch (bias) {
			case -2:
				mark = "--";
				break;
			case -1:
				mark = "-";
				break;
			case 1:
				mark = "+";
				break;
			case 2:
				mark = "++";
				break;
			case 0:
				mark = "";
				break;
			default:
				mark = "(" + bias + ")";
				break;
			}
			
			return mark + hour;
		}
	}
	
	// 내부적으로 사용되는 클래스이다. Singletone 으로 관리된다.
	
    private static class Timezone {
    	
    	private static final int TIMEZONE_START = -12;
    	
    	private HourInfo[][] TIMEZONE_TABLE = new HourInfo[][] {
    		
    		// UTC 기준 시간 0 ~ 23 에 해당 되는 각 타임존에서의 시간값
    /* tz = -12 */	{b(12), b(13), b(14), b(15), b(16), b(17), b(18), b(19), b(20), b(21), b(22), b(23), n(0), n(1), n(2), n(3), n(4), n(5), n(6), n(7), n(8), n(9), n(10), n(11) },		
    /* tz = -11 */	{b(13), b(14), b(15), b(16), b(17), b(18), b(19), b(20), b(21), b(22), b(23), n(0), n(1), n(2), n(3), n(4), n(5), n(6), n(7), n(8), n(9), n(10), n(11), n(12) },
    /* tz = -10 */	{b(14), b(15), b(16), b(17), b(18), b(19), b(20), b(21), b(22), b(23), n(0), n(1), n(2), n(3), n(4), n(5), n(6), n(7), n(8), n(9), n(10), n(11), n(12), n(13) },
    /* tz =  -9 */	{b(15), b(16), b(17), b(18), b(19), b(20), b(21), b(22), b(23), n(0), n(1), n(2), n(3), n(4), n(5), n(6), n(7), n(8), n(9), n(10), n(11), n(12), n(13), n(14) },
    /* tz =  -8 */	{b(16), b(17), b(18), b(19), b(20), b(21), b(22), b(23), n(0), n(1), n(2), n(3), n(4), n(5), n(6), n(7), n(8), n(9), n(10), n(11), n(12), n(13), n(14), n(15) },
    /* tz =  -7 */	{b(17), b(18), b(19), b(20), b(21), b(22), b(23), n(0), n(1), n(2), n(3), n(4), n(5), n(6), n(7), n(8), n(9), n(10), n(11), n(12), n(13), n(14), n(15), n(16) },
    /* tz =  -6 */	{b(18), b(19), b(20), b(21), b(22), b(23), n(0), n(1), n(2), n(3), n(4), n(5), n(6), n(7), n(8), n(9), n(10), n(11), n(12), n(13), n(14), n(15), n(16), n(17) },
    /* tz =  -5 */	{b(19), b(20), b(21), b(22), b(23), n(0), n(1), n(2), n(3), n(4), n(5), n(6), n(7), n(8), n(9), n(10), n(11), n(12), n(13), n(14), n(15), n(16), n(17), n(18) },
    /* tz =  -4 */	{b(20), b(21), b(22), b(23), n(0), n(1), n(2), n(3), n(4), n(5), n(6), n(7), n(8), n(9), n(10), n(11), n(12), n(13), n(14), n(15), n(16), n(17), n(18), n(19) },
    /* tz =  -3 */	{b(21), b(22), b(23), n(0), n(1), n(2), n(3), n(4), n(5), n(6), n(7), n(8), n(9), n(10), n(11), n(12), n(13), n(14), n(15), n(16), n(17), n(18), n(19), n(20) },
    /* tz =  -2 */	{b(22), b(23), n(0), n(1), n(2), n(3), n(4), n(5), n(6), n(7), n(8), n(9), n(10), n(11), n(12), n(13), n(14), n(15), n(16), n(17), n(18), n(19), n(20), n(21) },
    /* tz =  -1 */	{b(23), n(0), n(1), n(2), n(3), n(4), n(5), n(6), n(7), n(8), n(9), n(10), n(11), n(12), n(13), n(14), n(15), n(16), n(17), n(18), n(19), n(20), n(21), n(22) },
    /* tz =   0 */	{n(0), n(1), n(2), n(3), n(4), n(5), n(6), n(7), n(8), n(9), n(10), n(11), n(12), n(13), n(14), n(15), n(16), n(17), n(18), n(19), n(20), n(21), n(22), n(23) },
    /* tz =   1 */	{n(1), n(2), n(3), n(4), n(5), n(6), n(7), n(8), n(9), n(10), n(11), n(12), n(13), n(14), n(15), n(16), n(17), n(18), n(19), n(20), n(21), n(22), n(23), a(0) },
    /* tz =   2 */	{n(2), n(3), n(4), n(5), n(6), n(7), n(8), n(9), n(10), n(11), n(12), n(13), n(14), n(15), n(16), n(17), n(18), n(19), n(20), n(21), n(22), n(23), a(0), a(1) },
    /* tz =   3 */	{n(3), n(4), n(5), n(6), n(7), n(8), n(9), n(10), n(11), n(12), n(13), n(14), n(15), n(16), n(17), n(18), n(19), n(20), n(21), n(22), n(23), a(0), a(1), a(2) },
    /* tz =   4 */	{n(4), n(5), n(6), n(7), n(8), n(9), n(10), n(11), n(12), n(13), n(14), n(15), n(16), n(17), n(18), n(19), n(20), n(21), n(22), n(23), a(0), a(1), a(2), a(3) },
    /* tz =   5 */	{n(5), n(6), n(7), n(8), n(9), n(10), n(11), n(12), n(13), n(14), n(15), n(16), n(17), n(18), n(19), n(20), n(21), n(22), n(23), a(0), a(1), a(2), a(3), a(4) },
    /* tz =   6 */	{n(6), n(7), n(8), n(9), n(10), n(11), n(12), n(13), n(14), n(15), n(16), n(17), n(18), n(19), n(20), n(21), n(22), n(23), a(0), a(1), a(2), a(3), a(4), a(5) },
    /* tz =   7 */	{n(7), n(8), n(9), n(10), n(11), n(12), n(13), n(14), n(15), n(16), n(17), n(18), n(19), n(20), n(21), n(22), n(23), a(0), a(1), a(2), a(3), a(4), a(5), a(6) },
    /* tz =   8 */	{n(8), n(9), n(10), n(11), n(12), n(13), n(14), n(15), n(16), n(17), n(18), n(19), n(20), n(21), n(22), n(23), a(0), a(1), a(2), a(3), a(4), a(5), a(6), a(7) },
    /* tz =   9 */	{n(9), n(10), n(11), n(12), n(13), n(14), n(15), n(16), n(17), n(18), n(19), n(20), n(21), n(22), n(23), a(0), a(1), a(2), a(3), a(4), a(5), a(6), a(7), a(8) },
    /* tz =  10 */	{n(10), n(11), n(12), n(13), n(14), n(15), n(16), n(17), n(18), n(19), n(20), n(21), n(22), n(23), a(0), a(1), a(2), a(3), a(4), a(5), a(6), a(7), a(8), a(9) },
    /* tz =  11 */	{n(11), n(12), n(13), n(14), n(15), n(16), n(17), n(18), n(19), n(20), n(21), n(22), n(23), a(0), a(1), a(2), a(3), a(4), a(5), a(6), a(7), a(8), a(9), a(10) },
    /* tz =  12 */	{n(12), n(13), n(14), n(15), n(16), n(17), n(18), n(19), n(20), n(21), n(22), n(23), a(0), a(1), a(2), a(3), a(4), a(5), a(6), a(7), a(8), a(9), a(10), a(11) },
    /* tz =  13 */	{n(13), n(14), n(15), n(16), n(17), n(18), n(19), n(20), n(21), n(22), n(23), a(0), a(1), a(2), a(3), a(4), a(5), a(6), a(7), a(8), a(9), a(10), a(11), a(12) },
    /* tz =  14 */	{n(14), n(15), n(16), n(17), n(18), n(19), n(20), n(21), n(22), n(23), a(0), a(1), a(2), a(3), a(4), a(5), a(6), a(7), a(8), a(9), a(10), a(11), a(12), a(13) }
    				};
    	
    	private Map<Integer, LocalHours> lhmap = new HashMap<Integer, LocalHours>();
    	private Map<Integer, UtcHours> uhmap = new HashMap<Integer, UtcHours>();
    	
    	public Timezone() {
    		for(int tz = TIMEZONE_START; tz < 15; tz++) {
    			lhmap.put(tz, new LocalHours(tz));
    		}
    		
    		for(int hr = 0; hr < 24; hr++) {
    			uhmap.put(hr, new UtcHours(hr));
    		}
    	}

    	// 특정 타임존에서의 특정시간이 다른 타임존 지역에서는 몇시인지 반환한다.
    	public HourInfo getTimezoneHourInfo(int fromHour, int fromTimezone, int targetTimezone) {
    		// UTC 시간을 구한다.
    		int utcHour = getUtcHour(fromHour, fromTimezone);
    		
    		// 해당 UTC 시간에서 각 타임존에서의 시간 정보를 가져온다.
    		HourInfo hourFrom = getLocalHour(utcHour, fromTimezone);
    		HourInfo hourHere = getLocalHour(utcHour, targetTimezone);
    		
    		// 날짜차이를 계산하여 반환한다.
    		return new HourInfo(hourHere.hour, hourHere.bias - hourFrom.bias);
    	}
    	
    	// localTimezone 지역에서 localHour 가 UTC 에서는 몇시인지 가져오기
    	private int getUtcHour(int localHour, int localTimezone) {
    		return lhmap.get(localTimezone).getUtcHour(localHour);
    	}
    	
    	// UTC 특정 시간에서 locaTimezone 지역의 시간 정보 가져오기
    	private HourInfo getLocalHour(int utcHour, int localTimezone) {
    		return uhmap.get(utcHour).getLocalHour(localTimezone);
    	}
    	
    	// 오늘 일자 시간 생성
    	private HourInfo n(int h) {
    		return new HourInfo(h,0);
    	}
    	
    	// 어제 일자 시간 생성
    	private HourInfo b(int h) {
    		return new HourInfo(h,-1);
    	}
    	
    	// 내일 일자 시간 생성
    	private HourInfo a(int h) {
    		return new HourInfo(h,1);
    	}
    	
    	// 특정 타임존 지역에서 특정 시간이 UTC로 몇시인지 표현하기 위한 구조
    	public class LocalHours {
    		int timezone;
    		Map<Integer, Integer> map = new HashMap<Integer, Integer>(); // <로컬 시간, UTC 시간> 
    		
    		public LocalHours(int tz) {
    			timezone = tz;
    			
    			for(int i=0; i < 24; i++) {
    				HourInfo th = TIMEZONE_TABLE[timezone - TIMEZONE_START][i];
    				map.put(th.hour, i);
    			}
    		}
    		
    		/**
    		 * 자신의 타임존에서의 시간 hour 가 UTC 에서는 몇시인지를 반환한다.(index)
    		 * @param localHour
    		 * @return 0 ~ 23 의 정수, -1은 에러
    		 */
    		public int getUtcHour(int localHour) {
    			Integer index = map.get(localHour);
    			if (index == null) {
    				return -1;
    			}
    			else {
    				return index;
    			}
    		}
    		
    		public String toString() {
    			StringBuilder sb = new StringBuilder();
    			
    			sb.append("[timezone = ").append(timezone);
    			sb.append("] ").append(map);
    			
    			return sb.toString();
    		}
    	}
    	
    	//UTC 기준 특정 시간에 다른 타임존에서는 몇시인지 찾기위한 구조
    	public class UtcHours {
    		int utcHour; // UTC 기준 시간값
    		Map<Integer, HourInfo> map = new HashMap<Integer, HourInfo>(); // <timezone, TzHour>
    		
    		public UtcHours(int utc) {
    			utcHour = utc;
    			
    			for (int i=0; i < 27; i++) {
    				map.put(i + TIMEZONE_START, TIMEZONE_TABLE[i][utcHour]);
    			}
    		}
    		
    		public HourInfo getLocalHour(int tz) {
    			return map.get(tz);
    		}
    		
    		public String toString() {
    			StringBuilder sb = new StringBuilder();
    			
    			sb.append("UTC ").append(utcHour).append(" : ");
    			for (int i=0; i < 27; i++) {
    				sb.append(map.get(i-11)).append(" ");
    			}
    			
    			return sb.toString();
    		}
    	}
    }
}
