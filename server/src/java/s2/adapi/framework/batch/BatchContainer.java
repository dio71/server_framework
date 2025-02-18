package s2.adapi.framework.batch;

import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.config.Configurator;
import s2.adapi.framework.config.ConfiguratorException;
import s2.adapi.framework.config.ConfiguratorFactory;
import s2.adapi.framework.container.ContainerConfig;
import s2.adapi.framework.container.ServiceContainer;
import s2.adapi.framework.context.ContextManager;
import s2.adapi.framework.dao.sql.Transaction;

/**
 * BatchMain 구현 클래스를 실행시키기 위한 배치용 컨테이너 클래스이다.
 * 로그 출력등의 커스터마이징을 위해서 이 클래스를 상속받아서 preprocess(), postprocess() 를 구현한다.
 * main() 함수도 재구현한다.
 */
public class BatchContainer {

	private static final Logger  log = LoggerFactory.getLogger(BatchContainer.class);

	public ServiceContainer getServiceContainer() {
		
        String defaultName = null;
		try {
			Configurator configurator = ConfiguratorFactory.getConfigurator();
            defaultName = configurator.getString(ContainerConfig.DEFAULT_CONTAINER_NAME_PROPERTY, ContainerConfig.DEFAULT_CONTAINER_NAME);
		}
		catch (ConfiguratorException e) {
			e.printStackTrace();
			return null;
		}
		
		ServiceContainer svcContainer = ContainerConfig.instantiateContainer(defaultName);
		
		return svcContainer;
	}
	
	public Object getService(String serviceName) {
		ServiceContainer svcContainer = getServiceContainer();
		
		if (svcContainer != null) {
			return svcContainer.getService(serviceName);
		}
		else {
            log.error("## Cannot instantiate Service container.");
			return null;
		}
	}

	public static void main(String[] args) {
        BatchContainer container = new BatchContainer();
        container.execute(args);
    }

    public void execute(String[] args) {
		if (args == null || args.length == 0) {
			log.error("## No service name.");
			return;
		}

        String serviceName = args[0];
        BatchMain batchMain = null;

        Map<String,Object> diagMap = ContextManager.getDiagnosticContext(); // Diagnostic Context
        
        long stime = System.currentTimeMillis();
        diagMap.put("diag.batch.action.stime",stime);
        diagMap.put("diag.batch.action.target",serviceName);

        try {
            batchMain = (BatchMain)getService(serviceName);
            if (batchMain == null) {
                log.error("## Batch service not found : " + serviceName);
                return;
            }

            Transaction.current().begin();

            preprocess(args);
            Object retObj = batchMain.execute(args);

            long etime = System.currentTimeMillis();
            diagMap.put("diag.batch.action.etime",etime);
            diagMap.put("diag.batch.action.rtime",(etime-stime));

            postprocess(args, retObj);    //  정상 처리 후 postprocess
            
        }
		catch(Throwable thr) {
            log.error("## Batch execution error.", thr);

            long etime = System.currentTimeMillis();
            diagMap.put("diag.batch.action.etime",etime);
            diagMap.put("diag.batch.action.rtime",(etime-stime));

            postprocess(args, thr);   // 오류 발생시 postprocess

            return;
        }
		finally {
            try {
                Transaction.current().end();
            } 
            catch (SQLException e) {
                log.error("## Batch transaction end() error.", e);
            }
        }
	}

    protected void preprocess(String[] args) {}
    protected void postprocess(String[] args, Object retObj) {}
    protected void postprocess(String[] args, Throwable thr) {}
}