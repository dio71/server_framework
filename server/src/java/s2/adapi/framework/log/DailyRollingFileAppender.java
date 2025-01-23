package s2.adapi.framework.log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.log4j.Layout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

/**
 * 현재 로그 파일이 날짜 패턴으로 생성되도록 수정한 DaillyRollingFileAppender 이다.
 * 기존 RollingFileAppender 의 문제점
 * - 현재 로그파일에 날짜패턴이 붙지 않는다. WAS 가 종료되었다가 다시 실행되는 경우 잘못된 날짜로 Rolling 될 수 있다.
 * - 현재 로그파일을 Rolling 할때 파일명을 변경하게 되는데 이때 실패할 수 있다.(다중 프로세스에서 하나의 로그파일을 사용할 경우 발생)
 * 이를 해결하기 위하여 처음 생성시 파일명에 날짜 패턴을 붙이고 Rolling 하더라도 기존 로그 파일명을 변경하지 않는 방식으로 처리한다.
 * 
 * @author kimhd
 *
 */
public class DailyRollingFileAppender extends org.apache.log4j.FileAppender {

	private String datePattern = "'.'yyyy-MM-dd";
	
	private String baseFilename = "out.log";
	
	private String currentFilename = null;

	private Date now = new Date();

	private SimpleDateFormat sdf = null;

	private long nextCheck = System.currentTimeMillis() - 1;

	private GregorianCalendar rollingCalendar = new GregorianCalendar();

	private int checkType = TOP_OF_TROUBLE;
	
	// The gmtTimeZone is used only in computeCheckPeriod() method.
	static final TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");

	static final int TOP_OF_TROUBLE = -1;
	static final int TOP_OF_MINUTE = 0;
	static final int TOP_OF_HOUR = 1;
	static final int HALF_DAY = 2;
	static final int TOP_OF_DAY = 3;
	static final int TOP_OF_WEEK = 4;
	static final int TOP_OF_MONTH = 5;

	public DailyRollingFileAppender() {
	}

	public DailyRollingFileAppender(Layout layout, String filename, String datePattern)
			throws IOException {
		super();
		setDatePattern(datePattern);
		setFile(filename);
		setLayout(layout);
		setAppend(true);
		activateOptions();
	}

	public void setDatePattern(String pattern) {
		if (pattern != null) {
			datePattern = pattern;
		}
	}

	public String getDatePattern() {
		return datePattern;
	}

	public void setFile(String filename) {
		if (filename != null) {
			baseFilename = filename;
		}
	}

	public String getFile() {
		return baseFilename;
	}

	public void activateOptions() {
		sdf = new SimpleDateFormat(datePattern);
		checkType = computeCheckPeriod();
		
		now.setTime(System.currentTimeMillis());

		currentFilename = baseFilename + sdf.format(now);
		super.setFile(currentFilename);

		super.activateOptions();
	}

	protected void rollOver() throws IOException {

		// Compute filename, but only if datePattern is specified.
		if (datePattern == null) {
			errorHandler.error("Missing DatePattern option in rollOver().");
			return;
		}
		String datedFilename = baseFilename + sdf.format(now);

		// It is too early to roll over because we are still within the
		// bounds of the current interval.
		if (currentFilename.equals(datedFilename)) {
			return;
		}

		// close current file
		this.closeFile();

		// roll over to new file
		LogLog.debug(currentFilename + " -> " + datedFilename);
		//System.out.println(currentFilename + " -> " + datedFilename);
		currentFilename = datedFilename;

		// this.setFile(currentFilename, true, this.bufferedIO,
		// this.bufferSize);
		activateOptions();
	}

	protected void subAppend(LoggingEvent event) {
		long n = System.currentTimeMillis();
		if (n >= nextCheck) {
			now.setTime(n);
			nextCheck = getNextCheckTime(now); // schedule next time.
			try {
				//System.out.println("rollover()");
				rollOver();
			} catch (IOException ioe) {
				LogLog.error("rollOver() failed.", ioe);
				ioe.printStackTrace();
			}
		}
		super.subAppend(event);
	}

	private int computeCheckPeriod() {
		// set sate to 1970-01-01 00:00:00 GMT
		Date epoch = new Date(0);
		if (datePattern != null) {
			for (int i = TOP_OF_MINUTE; i <= TOP_OF_MONTH; i++) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
						datePattern);
				// simpleDateFormat.setTimeZone(gmtTimeZone); // do all date
				// formatting in GMT
				String r0 = simpleDateFormat.format(epoch);
				Date d2 = getNextCheckDate(epoch, i);
				String r1 = simpleDateFormat.format(d2);
				//System.out.println("Type = " + i + ", r0 = " + r0 + ", r1 = "+ r1);
				if (!r0.equals(r1)) {
					return i;
				}
			}
		}
		return TOP_OF_TROUBLE; // Deliberately head for trouble...
	}

	private long getNextCheckTime(Date d) {
		return getNextCheckDate(d,checkType).getTime();
	}

	private Date getNextCheckDate(Date d, int type) {
		rollingCalendar.setTime(d);

		switch (type) {
		case TOP_OF_MINUTE:
			rollingCalendar.set(Calendar.SECOND, 0);
			rollingCalendar.set(Calendar.MILLISECOND, 0);
			rollingCalendar.add(Calendar.MINUTE, 1);
			break;
		case TOP_OF_HOUR:
			rollingCalendar.set(Calendar.SECOND, 0);
			rollingCalendar.set(Calendar.MILLISECOND, 0);
			rollingCalendar.set(Calendar.MINUTE, 0);
			rollingCalendar.add(Calendar.HOUR_OF_DAY, 1);
			break;
		case HALF_DAY:
			rollingCalendar.set(Calendar.MINUTE, 0);
			rollingCalendar.set(Calendar.SECOND, 0);
			rollingCalendar.set(Calendar.MILLISECOND, 0);
			int hour = rollingCalendar.get(Calendar.HOUR_OF_DAY);
			if (hour < 12) {
				rollingCalendar.set(Calendar.HOUR_OF_DAY, 12);
			} else {
				rollingCalendar.set(Calendar.HOUR_OF_DAY, 0);
				rollingCalendar.add(Calendar.DAY_OF_MONTH, 1);
			}
			break;
		case TOP_OF_DAY:
			rollingCalendar.set(Calendar.HOUR_OF_DAY, 0);
			rollingCalendar.set(Calendar.MINUTE, 0);
			rollingCalendar.set(Calendar.SECOND, 0);
			rollingCalendar.set(Calendar.MILLISECOND, 0);
			rollingCalendar.add(Calendar.DATE, 1);
			break;
		case TOP_OF_WEEK:
			rollingCalendar.set(Calendar.DAY_OF_WEEK, rollingCalendar
					.getFirstDayOfWeek());
			rollingCalendar.set(Calendar.HOUR_OF_DAY, 0);
			rollingCalendar.set(Calendar.SECOND, 0);
			rollingCalendar.set(Calendar.MILLISECOND, 0);
			rollingCalendar.add(Calendar.WEEK_OF_YEAR, 1);
			break;
		case TOP_OF_MONTH:
			rollingCalendar.set(Calendar.DATE, 1);
			rollingCalendar.set(Calendar.HOUR_OF_DAY, 0);
			rollingCalendar.set(Calendar.SECOND, 0);
			rollingCalendar.set(Calendar.MILLISECOND, 0);
			rollingCalendar.add(Calendar.MONTH, 1);
			break;
		default:
			throw new IllegalStateException("");
		}

		return rollingCalendar.getTime();
	}
}