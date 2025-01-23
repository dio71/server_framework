package s2.adapi.framework.resources;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resources 들을 주기적으로 갱신하는 기능을 제공한다.
 * @author 김형도
 * @since 1.0
 */
public class ResourcesReloader extends Thread {

	private static Logger log = LoggerFactory.getLogger(ResourcesReloader.class);
	
	private Map<String,ReloadInfo> reloadInfos = new HashMap<String,ReloadInfo>();
	
	private long delay = 60 * 1000; // 1 minute
	
	private long minutesElapsed = 0;
	
    /**
     * 실제 Thread가 중지 상태인지 설정
     */
    private Boolean threadStop = true;
    
    /**
     * threadStop 변수 모니터용
     */
    private Object threadStopMonitor = new Object();
    
    /**
     * 중단 여부 설정
     */
    private boolean interrupted = false;
    
    private static ResourcesReloader singleton = new ResourcesReloader();
    
    private ResourcesReloader() {}
    
    public static ResourcesReloader instance() {
    	return singleton;
    }
    
	/**
	 * 재로딩 대상이 되는 ResourcesFactory 객체를 등록한다.
	 * @param factory
	 * @param reload
	 */
	public void addResource(String name, ResourcesFactory factory, int min) {
		if (log.isInfoEnabled()) {
			log.info("Add "+name+" into resource reloading list with interval of "+min+" minutes.");
		}
		synchronized(reloadInfos) {
			reloadInfos.put(name, new ReloadInfo(factory, min));
		}
	}
	
    /**
     * 재로딩을 중지하고 등록된 정보를 삭제한다.
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
    	
    	synchronized(reloadInfos) {
    		reloadInfos.clear();
    	}
    }
    
    /**
     * 주기적으로 재로딩할 Resources들을 확인하고 해당 Resources를 재로딩한다.
     */
    public void run() {
    	if (log.isInfoEnabled()) {
    		log.info("ResourcesReloader started.");
    	}
    	
    	minutesElapsed = 0;
        threadStop = false;
    	try {
		    	while (!interrupted) {
		            try {
		                Thread.sleep(delay);
		            } catch (InterruptedException ignored) {
		            }
		            if (!interrupted) {
		            	checkAndReload();
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
        		log.info("ResourcesReloader stopped.");
        	}
        }
    }
    
    /**
     * Resources의 재로딩 확인 및 재로딩 실행
     */
    private void checkAndReload() {
    	minutesElapsed++;
    	//System.out.println("Resources reloader check and reload.");
    	
    	if (reloadInfos.size() == 0) {
    		return;
    	}
    	
    	synchronized(reloadInfos) {
    		Iterator<String> itor = reloadInfos.keySet().iterator();
    		while(itor.hasNext()) {
    			String name = itor.next();
    			ReloadInfo reloadInfo = reloadInfos.get(name);
    			
    			if (minutesElapsed%reloadInfo.getReloadInterval() == 0) {
    				// release() the resourceFactoyr
    				reloadInfo.getResourcesFactory().release(name);
    				if (log.isInfoEnabled()) {
    					log.info("Resources '"+name+"' is released.");
    				}
    			}
    		}
    	}
    }
    
    private static class ReloadInfo {
    	private ResourcesFactory factory = null;
    	private int interval = 0;
    	
    	public ReloadInfo(ResourcesFactory factory, int interval) {
    		this.factory = factory;
    		this.interval = interval;
    	}
    	
    	public ResourcesFactory getResourcesFactory() {
    		return this.factory;
    	}
    	
    	public int getReloadInterval() {
    		return this.interval;
    	}
    }
}
