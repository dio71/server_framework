package s2.adapi.framework.dao.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 여러개의 DB 연결을 처리하기 위한 기능을 제공 (기존의 LocalTransaction 을 대체한다.)
 * 분산트랜젝션을 지원하는 것은 아님
 * @author kimhd
 *
 */
public class Transaction {

	private static final Logger log = LoggerFactory.getLogger(Transaction.class);
	
    final static int STATE_ACTIVE = 1;
    final static int STATE_INACTIVE = 0;
    
	private static ThreadLocal<Transaction> currentTr = new ThreadLocal<Transaction>();
	
	public static Transaction current() {
    	if (currentTr.get() == null) {
    		currentTr.set(new Transaction());
    	}
    	
    	return currentTr.get();
    }
	
	private Map<String, Connection> connectionMap = new HashMap<String, Connection>();;
	
	private int state = Transaction.STATE_INACTIVE;
	private boolean markRollback = false;
	
    public boolean isActive() {
    	return (state == Transaction.STATE_ACTIVE);
    }
    
	public void begin() {
		log.info("## transaction begin()");

        int nextState = checkState(Transaction.STATE_ACTIVE);
        
        state = nextState;
        markRollback = false;
	}
	
	/**
	 * 등록된 모든 LocalTransaction() 을 종료하고 비운다.
	 */
	public void end() throws SQLException {
		
		if (markRollback) {
			rollback();
		}
		else {
			commit();
		}
		
		log.info("## transaction end()");
	}
	
	/**
	 * 등록된 모든 LocalTransaction() 을 commit() 하고 비운다.
	 */
	public void commit() throws SQLException {
		StringBuilder sb = new StringBuilder();
		
		sb.append("## commit ").append(connectionMap.size()).append(" : ");
        
        int nextState = checkState(Transaction.STATE_INACTIVE);
        
        try {
        	if (connectionMap.size() > 0) {
        		
        		// 예외처리를 위한 부분
        		
        		Exception lastException = null;
        		
	        	for(String dsn:connectionMap.keySet()) {
	        		Connection con = connectionMap.get(dsn);
	        		
	        		try {	        			
	        			con.commit();
	        			
	        			sb.append(dsn);
	        		}
	        		catch(Exception ex) {
	        			sb.append(dsn).append(" error [").append(ex.getMessage()).append("] ");
	        			
	        			lastException = ex;
	        		}
	        		finally {
	        			try {
	        				con.close();
	        				sb.append(" closed,");
	        			}
	        			catch(Exception ex) {
	        				sb.append(" close failed [").append(ex.getMessage()).append("],");
	        			}
	        		}
	        	}
	        	
	        	// 예외가 발생되었다면 이를 알리기 위하여 SQLException 을 생성하여 throw 한다.
	        	// 여러개의 예외가 발생되었다면 마지막 에외를 caused exception 으로 설정한다.
	        	if (lastException != null) {
	        		SQLException sqlException = new SQLException(sb.toString());
	        		sqlException.initCause(lastException);
	        		
	        		throw sqlException;
	        	}
        	}
        }
        finally {
        	state = nextState;
        	connectionMap.clear();
        	
        	currentTr.set(null);
        	
        	sb.append(" done.");
        	log.info(sb.toString());
        }
	}
	
	public void rollback() throws SQLException {        
		StringBuilder sb = new StringBuilder();
		
		sb.append("## rollback ").append(connectionMap.size()).append(" : ");
		
        int nextState = checkState(Transaction.STATE_INACTIVE);
        
        try {
        	if (connectionMap.size() > 0) {
        		
        		// 예외처리를 위한 부분
        		
        		Exception lastException = null;
        		
	        	for(String dsn:connectionMap.keySet()) {
	        		Connection con = connectionMap.get(dsn);
	        		
	        		try {
	        			con.rollback();
	        			
	        			sb.append(dsn);
	        		}
	        		catch(Exception ex) {
	        			sb.append(dsn).append(" error [").append(ex.getMessage()).append("] ");
	        			
	        			lastException = ex;	        			
	        		}
	        		finally {
	        			try {
	        				con.close();
	        				sb.append(" closed");
	        			}
	        			catch(Exception ex) {
	        				sb.append(" close failed [").append(ex.getMessage()).append("],");
	        			}
	        			
	        		}
	        	}
	        	
	        	// 예외가 발생되었다면 이를 알리기 위하여 SQLException 을 생성하여 throw 한다.
	        	// 여러개의 예외가 발생되었다면 마지막 에외를 caused exception 으로 설정한다.
	        	if (lastException != null) {
	        		SQLException sqlException = new SQLException(sb.toString());
	        		sqlException.initCause(lastException);
	        		
	        		throw sqlException;
	        	}
        	}
        }
        finally {
        	state = nextState;
        	connectionMap.clear();
        	
        	currentTr.set(null);
        	
        	sb.append(" done.");
        	log.info(sb.toString());
        }
	}
	
    public void setRollbackOnly() {
    	markRollback = true;
    }
	
    /**
     * dsn 으로 연결된 Connection 객체를 찾는다.
     * 있으면 바로 그 connection 객체를 반환한다.
     * 없으면 새로운 connection 객체를 ds 에서 받아와서 이를 connectionMap 에 저장하고 반환한다.
     * @param ds
     * @return
     * @throws SQLException 
     */
    public Connection getConnection(DataSource ds) throws SQLException {
    	
    	String dsn = ds.getDsn();
    	
    	// 2014.10.20 transaction active 상태가 아니면 Exception 을 던진다.
        if ( state != Transaction.STATE_ACTIVE ) {
        	log.info("### no active transaction for connection : " + dsn);
            throw new SQLException("no active transaction.");
        }
    	
    	Connection connection = connectionMap.get(dsn);
    	
    	if (connection == null) {
    		// 이전에 요청된 connection 이 없으므로 새로 connection 을 받아온다.
    		connection = ds.getConnection();
        	connection.setAutoCommit(false);
        	
        	connectionMap.put(dsn, connection);
    	}
    	
    	return connection;
    	
    }
    
    /**
     * 주어진 con 객체가 connectionMap 에 있다면 아무런 작업도 하지 않는다.
     * 없다면 현재 Transaction 과 상관없으므로 Exception 을 throw 한다.
     * @param con
     * @throws SQLException
     */
    public void closeConnection(Connection con) throws SQLException {
    	if (!connectionMap.containsValue(con)) {
    		log.info("### cannot close connection not owned by a transaction.");
    		throw new SQLException("cannot close connection not owned by a transaction.");
    		//con.close();
    	}
    }
    
    /**
     * 현재 상태에서 target으로 변경할 수 있는 지 여부를 확인한다.
     * 변경할 수 없는 상태라면 IllegalStateException을 던진다.
     * 결과로 입력된 target을 리턴한다.
     */
    private int checkState(int target) {
        boolean valid;
        
        switch(state) {
        case Transaction.STATE_ACTIVE:
            valid = (target == Transaction.STATE_INACTIVE);
            break;
        case Transaction.STATE_INACTIVE:
            valid = (target == Transaction.STATE_ACTIVE);
            break;
        default:
            valid = false;
            break;
        }
        
        if ( !valid ) {
            throw new IllegalStateException("Illegal state of a transaction. [" + state + "].");
        }  
        
        return target; 
    }  
}
