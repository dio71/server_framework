package s2.adapi.framework.web.rpc;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RpcPacket implements Externalizable {
	private static final long serialVersionUID = 3L;
	
	public static final String PACKET_SALT_KEY = "rpc_s2api_j409!#";
	public static final String PACKET_ENC_KEY = "430k!-t398@";
	
	public static final int PACKET_VERSION = 3;
	
	public static final String HEADER_PACKET_TYPE = "p";
	
	public static final int HEADER_PACKET_INVOKE = 1;
	public static final int HEADER_PACKET_ACKMSG = 10;
	public static final int HEADER_PACKET_ERRMSG = 11;
	
	public static final String HEADER_INVOKE_TARGET = "t";
	public static final String HEADER_INVOKE_METHOD = "m";
	
	public static final String HEADER_SESSION_RPC_KEY = "r";
	public static final String HEADER_SESSION_RPC_DATE = "d";
	public static final String HEADER_SESSION_USERNAME = "n";
	public static final String HEADER_SESSION_LANG_CD = "l";
	
	public static final String HEADER_SESSION_VERIFY_KEY = "v";
	
	private Map<String,Object> header;
	
	private List<Object> message;
	
	public RpcPacket() {
		header = new HashMap<String,Object>();
		message = new ArrayList<Object>();
	}
	
	public void putHeader(String name, Object value) {
		header.put(name, value);
	}

	public long getLongHeader(String name) {
		return getLongHeader(name,0L);
	}
	
	public long getLongHeader(String name, long defaultValue) {
		Object value = header.get(name);
		if (value == null) {
			return defaultValue;
		} else if (value instanceof Number) {
			return ((Number)value).longValue();
		} else {
			return defaultValue;
		}
	}
	
	public int getIntHeader(String name) {
		return getIntHeader(name,0);
	}
	
	public int getIntHeader(String name, int defautValue) {
		Object value = header.get(name);
		if (value == null) {
			return defautValue;
		} else if (value instanceof Number) {
			return ((Number)value).intValue();
		} else {
			return defautValue;
		}
	}

	public double getDoubleHeader(String name) {
		Object value = header.get(name);
		if (value == null) {
			return 0.0;
		} else if (value instanceof Number) {
			return ((Number)value).doubleValue();
		} else {
			return 0.0;
		}
	}
	
	public String getStringHeader(String name) {
		return getStringHeader(name,null);
	}
	
	public String getStringHeader(String name, String defaultValue) {
		Object value = header.get(name);
		if (value == null) {
			return defaultValue;
		} else if (value instanceof String) {
			return (String)value;
		} else {
			return defaultValue;
		}
	}
	
	public Date getDateHeader(String name) {
		return getDateHeader(name,null);
	}
	
	public Date getDateHeader(String name, Date defaultValue) {
		Object value = header.get(name);
		if (value == null) {
			return defaultValue;
		} else if (value instanceof Date) {
			return (Date)value;
		} else {
			return defaultValue;
		}
	}
	
	public Object getHeader(String name) {
		return header.get(name);
	}
	
	public void addMessage(Object msg) {
		message.add(msg);
	}
	
	public int messageCount() {
		return message.size();
	}
	
	public Object getMessage(int index) {
		if (index < message.size()) {
			return message.get(index);
		}
		else {
			return null;
		}
	}
	
	public Object getMessage() {
		return getMessage(0);
	}
	
	public Object[] getMessages() {
		return message.toArray(new Object[message.size()]);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(header);
		sb.append("=");
		sb.append(message);
		
		return sb.toString();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		int version = in.readUnsignedShort();
		
		if (version != PACKET_VERSION) {
			throw new IOException("Version mismatched. " + version);
		}
		
		// read header
		int headerCount = in.readUnsignedShort();
		for(int i=0; i<headerCount; i++) {
			String name = in.readUTF();
			Object data = in.readObject();
			header.put(name, data);
		}
		
		// read body
		int bodyCount = in.readUnsignedShort();
        for (int i = 0; i < bodyCount; ++i) {
            message.add(in.readObject());
        }
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeShort(PACKET_VERSION);
		
		// write header
		Set<String> nameSet = header.keySet();
		String[] names = nameSet.toArray(new String[nameSet.size()]);
		int headerCount = names.length;
		out.writeShort(headerCount);
		for(int i=0; i<headerCount; i++) {
			out.writeUTF(names[i]);
			out.writeObject(header.get(names[i]));
		}
		
		// write message
		int bodyCount = message.size();
		out.writeShort(bodyCount);
		for (int i=0; i<bodyCount; i++) {
			out.writeObject(message.get(i));
		}
	}
}
