package s2.adapi.framework.util;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * File Watch Dog 클래스
 * </p>
 *
 * @author 김형도
 * @since 3.0
 */
public class FileWatchdog extends Thread {

    /**
     * <p>
     * 에러나 이벤트와 관련된 각종 메시지를 로깅하기 위한 Log 오브젝트
     * </p>
     */
    private static final Logger log = LoggerFactory.getLogger(FileWatchdog.class);

    /**
     * <p>
     * 해당 파일의 변경 유무를 체크하기 위한 Default 초의 static final 변수, 기본 적용은 값은 30초
     * </p>
     */
    public static final long DEFAULT_DELAY = 3000;

    /**
     * 감시 쓰레드 명
     */
    public static final String WATCHDOG_THREAD_NAME = "Live.FileWatchDog";
    
    /**
     * <p>
     * 해당 파일의 변경 유무를 체크하기 위한 Default 초의 static final 변수, 기본 적용은 값은 30초{@link
     * #DEFAULT_DELAY}.
     * </p>
     */
    private long delay = DEFAULT_DELAY;

    /**
     * 변경 확인 대상 파일 객체 또는 디렉토리
     */
    private File targetFile = null;
    
    /**
     * 감시 중단 여부 설정
     */
    private boolean interrupted = false;

    /**
     * 실제 Thread가 중지 상태인지 설정
     */
    private Boolean threadStop = true;
    
    /**
     * threadStop 변수 모니터용
     */
    private Object threadStopMonitor = new Object();
    
    /**
     * 파일 변경 이벤트를 전달해줄 리스너 객체
     */
    private FileWatchdogListener listener = null;
    
    /**
     * 변경 확인 대상 파일 목록과 최근 수정 시간을 저장
     */
    private Map<String,Long> watchFiles = null;
    
    /**
     * 모니터 작업을 진행하고 있는지 여부, 외부에서 모니터 작업을 중지시킬지 여부 등을
     * 알리기 위하여 사용하는 파일 
     */
    private File lockFile = null;
    
    /**
     * 감시하고자 하는 파일을 파라메타로 받는 기본 생성자.
     *
     * @param filename
     */
    public FileWatchdog(File file) {
        targetFile = file;
        watchFiles = buildFileList();
        // 감시 대상 파일 명을 사용하여 쓰레드 명칭을 생성한다.
        if (targetFile != null) {
        	setName(WATCHDOG_THREAD_NAME+":"+targetFile.getName());
        } else {
        	setName(WATCHDOG_THREAD_NAME);
        }
    }
    
    /**
     * 감시를 중단한다.
     */
    public void quit() {
    	interrupted = true;
    	// sleep상태의 쓰레드를 깨우기 위하여 interrupt()를 호출한다.
    	this.interrupt();
    	
    	// 실제로 thread가 중지될 때까지 기다린다.
    	synchronized(threadStopMonitor) {
    		while(!threadStop) {
    			try {
    				threadStopMonitor.wait(500);
				} catch (InterruptedException e) {
				}
    		}
    	}
    }
    
    /**
     * 감시 주기를 설정한다.
     *
     * @param delay 감시주기 (초)
     */
    public void setDelay(long delay) {
        this.delay = delay;
    }

    /**
     * Lock 파일 객체를 지정한다.
     * @param file
     */
    public void setLockFile(File file) {
    	if (file != null) {
    		file.deleteOnExit();
    	}
    	this.lockFile = file;
    }
    
    /**
     * 파일 변경시 이벤트를 전달할 리스너 객체를 설정한다.
     * 이전에 설정된 리스너가 있을 경우에는 이를 리턴한다.
     * @param lstnr 설정할 리스너 객체
     * @return 이전에 설정된 리스너가 있을 경우 리턴한다.
     */
    public FileWatchdogListener setListener(FileWatchdogListener lstnr) {
    	FileWatchdogListener prevListener = listener;
    	listener = lstnr;
    	
    	return prevListener;
    }
    
    /**
     * <p>
     * 파일의 변경 유무를 체크하는 메소드
     * </p>
     */
    protected void checkAndConfigure() {
    	//System.out.println(targetFile.getName()+" check and configure");
    	if (lockFile != null && lockFile.exists()) {
    		log.trace("lock file exists. " + lockFile.getName());
    	}
    	else {
    		try {
    			if (lockFile != null) {
        			lockFile.createNewFile();
        			log.trace("lock file created. " + lockFile.getName());
        		}
    			
        		Map<String,Long> fileMap = buildFileList();
        		boolean changed = !compareFiles(fileMap);
        		watchFiles = fileMap;
        		if (changed && listener != null && !interrupted) {
        			listener.fileChanged();
        		}
        	} catch (Exception ex) {
        	} finally {
        		if (lockFile != null) {
        			lockFile.delete();
        			log.trace("lock file deleted. " + lockFile.getName());
        		}
        	}
    	}
    	
    }

    /**
     * <p>
     * 파일의 변경 유무의 체크를 주기적으로 실행시키는 메소드
     * </p>
     */
    public void run() {
    	if (log.isInfoEnabled()) {
    		log.info("FileWatchDog starts. ["+targetFile.getPath()+"]");
    	}
        threadStop = false;
    	try {
		    	while (!interrupted) {
		            try {
		                Thread.sleep(delay);
		            } catch (InterruptedException ignored) {
		            }
		            if (!interrupted) {
		            	checkAndConfigure();
		            }
		        }
		    	
		    	synchronized(threadStopMonitor) {
		    		// 쓰레드를 중지상태로 설정한다.
		    		threadStop = true;
		    		// 쓰레드가 중지되었음을 알린다.
		    		threadStopMonitor.notify();
		    	}
        } finally {
        	if (log.isInfoEnabled()) {
        		log.info("FileWatchDog stop. ["+targetFile.getPath()+"]");
        	}
        }
    }

    /**
     * 대상 파일 또는 대상 디렉토리의 하위 파일들의 목록을 생성한다.
     * @return
     */
    private Map<String,Long> buildFileList() {
    	Map<String,Long> fMap = new HashMap<String,Long>();
    	
    	if (targetFile != null && targetFile.exists()) {
    		if (targetFile.isFile()) {
    			// 대상이 파일이므로 바로 Map에 담는다.
    			fMap.put(targetFile.getName(), targetFile.lastModified());
    			//System.out.println("PUT " + targetFile.getName());
    		} else {
    			// 대상이 디렉토리이므로 하위 파일들을 Map에 담는다.
    			File[] files = targetFile.listFiles();
    			for(int i=0;i<files.length;i++) {
    				fMap.put(files[i].getName(),files[i].lastModified());
    				//System.out.println("PUT "+ files[i].getName());
    			}
    		}
    	} else {
    		// 대상 파일이 존재하지 않으므로 아무것도 넣지 않는다.
    	}
    	
    	return fMap;
    }
    
    /**
     * 기존에 작성되어 있는 파일 목록과 주어진 파일 목록을 비교한다.
     * 파일 목록의 갯수가 다르거나, 기존에 있던 파일이 주어진 파일 목록에 없거나,
     * 있더라도 파일의 최종 수정 시간이 다르다면 변경된것으로 보고 false를 반환한다.
     * @param current
     * @return true if nothing is changed, false if any files have been changed.
     */
    private boolean compareFiles(Map<String,Long> curFiles) {
    	if (watchFiles.size() != curFiles.size()) {
    		//System.out.println("File count is changed.");
    		return false;
    	}
    	
    	// 기존 파일 목록을 기준으로 비교
    	Set<String> keySet = watchFiles.keySet();
    	Iterator<String> itor = keySet.iterator();
    	String fname = null;
    	Long mtime = 0L;
    	Long mtime2 = 0L;
    	while(itor.hasNext()) {
    		fname = itor.next();
    		mtime = watchFiles.get(fname);
    		
    		// fname을 curFiles에서 찾는다.
    		mtime2 = curFiles.get(fname);
    		if (mtime == null || mtime2 == null || mtime.longValue() != mtime2.longValue()) {
    			//System.out.println("Changed "+fname);
    			return false;
    		}
    	}
    	
    	return true;
    }
}
