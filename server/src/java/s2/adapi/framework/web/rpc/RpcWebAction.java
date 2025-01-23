package s2.adapi.framework.web.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.context.ContextManager;
import s2.adapi.framework.context.ServiceContext;
import s2.adapi.framework.dao.sql.Transaction;
import s2.adapi.framework.exception.ApplicationException;
import s2.adapi.framework.util.DigesterHelper;
import s2.adapi.framework.vo.ValueObject;
import s2.adapi.framework.web.action.AbstractWebAction;
import s2.adapi.framework.web.action.WebActionForward;

/**
 * 외부 서버에서 원격 RPC 호출을 받아주는 WebAction 이다.
 * @author kimhd
 *
 */
public class RpcWebAction extends AbstractWebAction {

	private byte[] encrtyptionKey = RpcPacket.PACKET_ENC_KEY.getBytes(); // default key;
	
	private static final String ENCRYPTION_METHOD = "RC4";
	private static final String ENCRYPTION_TRANSFORMATION = "RC4";
	
	private static final Logger log = LoggerFactory.getLogger(RpcWebAction.class);
		
	/**
	 * 실제 RPC 서비스를 제공하는 클래스로 각 프로젝트별로 구현하여 설정한다.
	 */
	private RpcService rpcService = null;
	
	public void setRpcService(RpcService rpc) {
		rpcService = rpc;
	}
	
	/**
	 * 통신시 사용할 암호화 키를 변경하고자 할 경우에 설정한다.
	 * 클라이언트와 암호화 키는 동일해야한다.
	 * @param key
	 */
	public void setEncryptionKey(String key) {
		encrtyptionKey = key.getBytes();
	}
	
	public RpcWebAction() {
	}
	
	@Override
	public WebActionForward execute(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String,Object> diagMap = ContextManager.getDiagnosticContext();
		
		long stime = System.currentTimeMillis();
		diagMap.put("diag.web.action.stime", stime);
		
		String ipAddr = request.getRemoteAddr();
		ContextManager.getServiceContext().setRole("ipaddr", ipAddr);
		
		try {
			RpcPacket packet = readPacket(request);
			
			if (log.isDebugEnabled()) {
				log.info("## RPC packet = " + packet.toString());
			}
			
			// check session
			String rpcKey = packet.getStringHeader(RpcPacket.HEADER_SESSION_RPC_KEY);
			long rpcMillis = packet.getLongHeader(RpcPacket.HEADER_SESSION_RPC_DATE);
			String userName = packet.getStringHeader(RpcPacket.HEADER_SESSION_USERNAME);
			String langCode = packet.getStringHeader(RpcPacket.HEADER_SESSION_LANG_CD, "en");
			
			String verifyKey = packet.getStringHeader(RpcPacket.HEADER_SESSION_VERIFY_KEY);
			
			// verify check code
			StringBuilder sb = new StringBuilder();
			sb.append(rpcKey).append(rpcMillis).append(userName).append(RpcPacket.PACKET_SALT_KEY);
			
			String mdCheck = DigesterHelper.md5Hex(sb.toString());
			
			long curMillis = System.currentTimeMillis();
			
			if (rpcKey != null) {
				MDC.put("userid", rpcKey); // 
			}
			
			if (userName != null) {
				MDC.put("mduname", userName);
			}
			
			// verifycheck , timestamp 범위 확인 (1분)
			if (!mdCheck.equals(verifyKey) || Math.abs(rpcMillis - curMillis) > 60*1000L) {
				log.error("check code : " + verifyKey + " : " + mdCheck);
				log.error("timestamp : " + Math.abs(rpcMillis - curMillis));
				
				String errorMessage = "not authorized request.";
				writeError(response,errorMessage);
				
				diagMap.put("diag.web.action.errmsg", errorMessage);
			}
			else {
				
				diagMap.put("diag.web.action.type", "R"); // RPC request
				diagMap.put("diag.web.action.retcount","");
				diagMap.put("diag.web.action.svc.errmsg","");
				
				// put to session
				ServiceContext serviceContext = ContextManager.getServiceContext(); 
				
				serviceContext.setRole("rpckey", rpcKey); 
				serviceContext.setRole("mduname", userName); // 원격지 사용자 구분값
				serviceContext.setRole("lang", langCode); // 언어코드
				
				// check packet type
				int packetType = packet.getIntHeader(RpcPacket.HEADER_PACKET_TYPE);
				
				Object retObject = null;
				RpcPacket returnPacket = new RpcPacket();
				
				switch(packetType) {
				case RpcPacket.HEADER_PACKET_INVOKE:
					retObject = invokeService(packet);
					if (retObject instanceof ValueObject) { // 처리 건수
						ValueObject retVO = (ValueObject)retObject;
						diagMap.put("diag.web.action.retcount",retVO.size());
					}
					
					returnPacket.putHeader(RpcPacket.HEADER_PACKET_TYPE, RpcPacket.HEADER_PACKET_ACKMSG);
					returnPacket.addMessage(retObject);
					break;
				}
				
				writePacket(response,returnPacket);
			}
		} 
		catch (Throwable thr) {
			thr.printStackTrace();
			Transaction.current().setRollbackOnly();
			
			writeError(response, thr.toString());
			if (thr instanceof ApplicationException) {
				log.error(((ApplicationException)thr).getLocalizedMessage());
			} else {
				log.error(thr.toString());
			}
			diagMap.put("diag.web.action.errmsg", thr.toString());
		} 
		finally {
			long etime = System.currentTimeMillis();
			diagMap.put("diag.web.action.etime", etime);
			diagMap.put("diag.web.action.rtime", (etime-stime));
			
			log.info("### RPC Packet request end : " + (etime-stime));
		}
		
		return null;
	}
	
	private Object invokeService(RpcPacket packet) throws Throwable {
		Map<String,Object> diagMap = ContextManager.getDiagnosticContext();
		
		String target = packet.getStringHeader(RpcPacket.HEADER_INVOKE_TARGET);
		String method = packet.getStringHeader(RpcPacket.HEADER_INVOKE_METHOD);
		Object[] args = packet.getMessages();
		
		diagMap.put("diag.web.action.target", target);
		diagMap.put("diag.web.action.method", method);
		
		Object retObject = rpcService.invokeService(target, method, args);
		
		// transaction 을 최대한 빨리 끝내기 위해서 여기에서 transaction 종료한다. (2012.03.30)
		try {
			Transaction.current().end();
			log.debug("## transaction - end");
		} catch (SQLException e) {
			log.debug("## transaction - exception", e);
		}
		
		return retObject;
	}
	
	protected RpcPacket readPacket(HttpServletRequest request) 
			throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException 
	{
		long stime = System.currentTimeMillis();
		
		// 암호화 스트림 생성
		SecretKeySpec seckey = new SecretKeySpec(encrtyptionKey, ENCRYPTION_METHOD);
		Cipher cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION);
		cipher.init(Cipher.DECRYPT_MODE, seckey);
		CipherInputStream cin = new CipherInputStream(request.getInputStream(),cipher);
		InputStream in = new InflaterInputStream(cin);
		
		RpcPacket packet = new RpcPacket();
		packet.readExternal(new RpcPacketInputStream(in));
		
		long etime = System.currentTimeMillis();
		log.debug("## Read packet time : " + (etime - stime));
		
		return packet;
	}
	
	protected void writePacket(HttpServletResponse response, RpcPacket packet) 
			throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException 
	{
		long stime = System.currentTimeMillis();
		
		SecretKeySpec seckey = new SecretKeySpec(encrtyptionKey, ENCRYPTION_METHOD);
		Cipher cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION);
		cipher.init(Cipher.ENCRYPT_MODE, seckey);
		CipherOutputStream cout = new CipherOutputStream(response.getOutputStream(),cipher);
		OutputStream out = new DeflaterOutputStream(cout);
		
		packet.writeExternal(new RpcPacketOutputStream(out));
		
		out.close(); // 반드시 해야함.
		
		long etime = System.currentTimeMillis();
		log.debug("## Write packet time : " + (etime - stime));
	}
	
	private void writeError(HttpServletResponse response, String errorMessage) 
			throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException 
	{
		// 에러인 경우 해당 내용을 tr.log에 출력하기위하여 diagnosticContext에 설정
		Map<String,Object> diagMap = ContextManager.getDiagnosticContext();
		diagMap.put("diag.web.action.svc.errmsg", errorMessage);
		
		RpcPacket errorPacket = new RpcPacket();
		errorPacket.putHeader(RpcPacket.HEADER_PACKET_TYPE, RpcPacket.HEADER_PACKET_ERRMSG);
		errorPacket.addMessage(errorMessage);
		
		writePacket(response,errorPacket);

	}
}
