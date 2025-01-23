package s2.adapi.framework.web.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import s2.adapi.framework.context.ContextManager;
import s2.adapi.framework.crypto.Cryptor;
import s2.adapi.framework.crypto.DecryptInputStream;
import s2.adapi.framework.crypto.EncryptOutputStream;
import s2.adapi.framework.crypto.RC4Cryptor;
import s2.adapi.framework.util.DigesterHelper;

public class RpcClient {
	
	private static final Logger log = LoggerFactory.getLogger(RpcClient.class);
	
	private int requestConnectTimeout = 5000;
	private int requestReadTimeout = 30000;
	
	private byte[] encrtyptionKey = RpcPacket.PACKET_ENC_KEY.getBytes(); // default key;
	
	public void setConnectionTimeout(int msec) {
		requestConnectTimeout = msec;
	}
	
	public void setReadTimeout(int msec) {
		requestReadTimeout = msec;
	}
	
	/**
	 * 암호화 키 다른 것 사용시
	 * @param key
	 */
	public void setEncryptionKey(String key) {
		encrtyptionKey = key.getBytes();
	}
	
	private String rpcKey = null;
	
	public void setRpcKey(String key) {
		rpcKey = key;
	}
	
	private String serviceUrl = null;
	
	public void setServiceUrl(String url) {
		serviceUrl = url;
	}
	
	private String userKey = "logn_id";
	
	// session 에서 어떤 값을 사용할지 지정한다.
	public void setUserKey(String key) {
		userKey = key;
	}
	
	/**
	 * RPC 호출을 수행함
	 * @param serviceName
	 * @param operationName
	 * @param args
	 * @param optionParams 추가 파라메터
	 * @return
	 */
	public Object invoke(String serviceName, String operationName, Object[] args) {
		log.debug("invoke " + serviceName + ":" + operationName);
		
		String userName = String.valueOf(ContextManager.getServiceContext().getRole(userKey));
		String langCode = "en"; 
		if (ContextManager.getServiceContext().getRole("lang") != null) {
			langCode = String.valueOf(ContextManager.getServiceContext().getRole("lang"));
		}
		
		Object retObj = null;
		
		try {
			retObj = invokeInternal(serviceName, operationName, args, userName, langCode);
		} 
		catch (RpcRemoteException ex) {
			throw ex;
		} 
		catch (Exception ex) {
			// 단말에러
			log.error("invoke() error " + ex.toString());
			throw new RpcRemoteException("Rpc Request failed.", ex);
		} 
		
		return retObj;
	}
	
	/**
	 * RPC 호출을 수행함
	 * @param serviceName
	 * @param operationName
	 * @param args
	 * @return
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyException 
	 */
	private Object invokeInternal(String serviceName, String operationName, Object[] args, String userName, String langCode) 
			throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchPaddingException, KeyException 
	{

		Object retObj = null;
				
		URL url = new URL(serviceUrl);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection(); 
		
		try {
			
			connection.setConnectTimeout(requestConnectTimeout);
			connection.setReadTimeout(requestReadTimeout);
			connection.setDoOutput(true);
			
			// 암호화 스트림 생성
			Cryptor cryptor = new RC4Cryptor(encrtyptionKey);
			EncryptOutputStream cout = new EncryptOutputStream(connection.getOutputStream(),cryptor.init());
			OutputStream out = new DeflaterOutputStream(cout);
			
			RpcPacket packet = new RpcPacket();
			packet.putHeader(RpcPacket.HEADER_PACKET_TYPE, RpcPacket.HEADER_PACKET_INVOKE);
			packet.putHeader(RpcPacket.HEADER_INVOKE_TARGET, serviceName);
			packet.putHeader(RpcPacket.HEADER_INVOKE_METHOD, operationName);
			
			long currentMillis = System.currentTimeMillis();
			packet.putHeader(RpcPacket.HEADER_SESSION_RPC_KEY, rpcKey);
			packet.putHeader(RpcPacket.HEADER_SESSION_RPC_DATE, currentMillis);
			packet.putHeader(RpcPacket.HEADER_SESSION_USERNAME, userName); 
			packet.putHeader(RpcPacket.HEADER_SESSION_LANG_CD, langCode);
			
			StringBuilder sb = new StringBuilder();
			sb.append(rpcKey).append(currentMillis).append(userName).append(RpcPacket.PACKET_SALT_KEY);
			String mdCheck = DigesterHelper.md5Hex(sb.toString());
			
			packet.putHeader(RpcPacket.HEADER_SESSION_VERIFY_KEY, mdCheck);
			
			if (args != null) 
			{
				for(int i=0; i<args.length; i++) 
				{
					packet.addMessage(args[i]);
				}
			}
			
			packet.writeExternal(new RpcPacketOutputStream(out));
			
			out.close();
			
			// 암호화 스트림 생성
			DecryptInputStream cin = new DecryptInputStream(connection.getInputStream(),cryptor.init());
			InputStream in = new InflaterInputStream(cin);
			
			RpcPacket returnPacket = new RpcPacket();
			returnPacket.readExternal(new RpcPacketInputStream(in));
			
			in.close();
			
			int returnType = returnPacket.getIntHeader(RpcPacket.HEADER_PACKET_TYPE);
			if (returnType == RpcPacket.HEADER_PACKET_ERRMSG) {
				// 서버 에러
				String errMessage = String.valueOf(returnPacket.getMessage());
				throw new RpcRemoteException(errMessage);
			} 
			else {
				// 정상 처리
				retObj = returnPacket.getMessage();
			}

			return retObj;
		}
		finally {
			connection.disconnect();
		}
	}
}
