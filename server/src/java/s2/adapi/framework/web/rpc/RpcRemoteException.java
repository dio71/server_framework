package s2.adapi.framework.web.rpc;

public class RpcRemoteException extends RuntimeException {

	private static final long serialVersionUID = -1967971600500045643L;

	public RpcRemoteException(String msg) {
		super(msg);
	}
	
	public RpcRemoteException(Throwable thr) {
		super(thr);
	}
	
	public RpcRemoteException(String msg, Throwable thr) {
		super(msg,thr);
	}
}
