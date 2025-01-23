package s2.adapi.framework.web.rpc;

import java.lang.reflect.Method;

import s2.adapi.framework.aop.target.AbstractTargetProxy;

/**
 * 원격사이트에 있는 서비스를 로컬에서 처럼 호출할 수 있도록 기능을 제공해주는 TargetProxy 구현이다.
 * @author kimhd
 *
 */
public class RpcCallTargetProxy extends AbstractTargetProxy {
	
	private RpcClient rpcClient = null;
	
	public void setRpcClient(RpcClient client) {
		rpcClient = client;
	}
		
	/**
	 * 호출할 원격지의 서비스 객체의 서비스 등록명(원격 서버에 등록된 명칭)
	 */
	private String targetName = null;
	
	public void setTargetName(String name) {
		targetName = name;
	}
	
	@Override
	public Object invoke(Method method, Object[] args) throws Throwable {
		String methodName = method.getName();
		
		return rpcClient.invoke(targetName, methodName, args);
	}
}
