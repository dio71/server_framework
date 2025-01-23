package s2.adapi.framework.web.rpc;

import java.io.DataOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import s2.adapi.framework.web.rpc.RpcPacketTypes.Traits;

/**
 * Modified AMF3
 * @author kimhd
 *
 */
class RpcPacketOutputStream extends DataOutputStream implements ObjectOutput {
    
	public RpcPacketOutputStream(OutputStream out) {
		super(out);
		stringRefTable = new HashMap<String, Integer>();
		objectRefTable = new IdentityHashMap<Object, Integer>();
		traitsRefTable = new HashMap<Traits, Integer>();
	}
	
	public void reset() {
		stringRefTable.clear();
		objectRefTable.clear();
		traitsRefTable.clear();
	}

	public void writeObject(Object obj) throws IOException {
		if (obj == null) {
			write(RpcPacketTypes.Markers.NULL_MARKER);
		} 
		else if (obj instanceof String) {
			writeString((String)obj,true);
		} 
		else if (obj instanceof Character) {
			writeString(obj.toString(),true);
		} 
		else if (obj instanceof Number) {
			writeNumber((Number)obj);
		} 
		else if (obj instanceof Boolean) {
			writeBoolean((Boolean)obj);
		} 
		else if (obj instanceof Date) {
			writeDate((Date)obj);
		} 
		else if (obj instanceof char[]) {
			writeCharArray((char[])obj);
		} 
		else if (obj instanceof byte[]) {
			writeByteArray((byte[])obj);
		} 
		else if (obj instanceof int[]) {
			writeIntArray((int[])obj);
		} 
		else if (obj instanceof short[]) {
			writeShortArray((short[])obj);
		} 
		else if (obj instanceof long[]) {
			writeLongArray((long[])obj);
		} 
		else if (obj instanceof float[]) {
			writeFloatArray((float[])obj);
		} 
		else if (obj instanceof double[]) {
			writeDoubleArray((double[])obj);
		} 
		else if (obj instanceof boolean[]) {
			writeBooleanArray((boolean[])obj);
		} 
		else if (obj instanceof Object[]) {
			writeObjectArray((Object[])obj);
		} 
		else if (obj instanceof Externalizable) {
			writeExternalObject((Externalizable)obj);
		} 
		else if (obj instanceof Collection<?>) {
			writeCollection((Collection<?>)obj);
		} 
		else if (obj instanceof Map<?,?>) {
			writeMapObject((Map<?,?>)obj);
		} 
		else {
			throw new IOException("Not supported object type: " + obj.getClass().getName());
		}
	}
	
	protected void writeNumber(Number num) throws IOException {
		if (num instanceof BigDecimal) {
			writeString(num.toString(),true);
		} else if (num instanceof Double || num instanceof Float) {
			writeDouble(num.doubleValue(), true);
		} else {
			if (num.longValue() < RpcPacketTypes.MIN_U29_VALUE || num.longValue() > RpcPacketTypes.MAX_U29_VALUE) {
				// out of range for U29
				writeLong(num.longValue(), true);
			} else {
				writeInt(num.intValue(), true);
			}
		}
	}
	
	protected void writeIntArray(int[] a) throws IOException {
		write(RpcPacketTypes.Markers.INT_ARRAY_MARKER);
		
		if (!writeObjectReference(a)) {
			writeU29((a.length << 1) | 1); // U29A-value
			for (int i = 0; i < a.length; i++) {
				writeU29(a[i] & 0x1FFFFFFF);
			}
		}
	}
	
	protected void writeShortArray(short[] a) throws IOException {
		write(RpcPacketTypes.Markers.INT_ARRAY_MARKER);
		
		if (!writeObjectReference(a)) {
			writeU29((a.length << 1) | 1); // U29A-value
			for (int i = 0; i < a.length; i++) {
				writeU29(a[i] & 0x1FFFFFFF);
			}
		}
	}
	
	protected void writeLongArray(long[] a) throws IOException {
		write(RpcPacketTypes.Markers.LONG_ARRAY_MARKER);
		
		if (!writeObjectReference(a)) {
			writeU29((a.length << 1) | 1); // U29A-value
			for (int i = 0; i < a.length; i++) {
				writeLong(a[i]);
			}
		}
	}
	
	protected void writeFloatArray(float[] a) throws IOException {
		write(RpcPacketTypes.Markers.DOUBLE_ARRAY_MARKER);
		
		if (!writeObjectReference(a)) {
			writeU29((a.length << 1) | 1); // U29A-value
			for (int i = 0; i < a.length; i++) {
				writeDouble(a[i]);
			}
		}
	}
	
	protected void writeDoubleArray(double[] a) throws IOException {
		write(RpcPacketTypes.Markers.DOUBLE_ARRAY_MARKER);
		
		if (!writeObjectReference(a)) {
			writeU29((a.length << 1) | 1); // U29A-value
			for (int i = 0; i < a.length; i++) {
				writeDouble(a[i]);
			}
		}
	}
	
	protected void writeBooleanArray(boolean[] a) throws IOException {
		write(RpcPacketTypes.Markers.OBJECT_ARRAY_MARKER);
		
		if (!writeObjectReference(a)) {
			writeU29((a.length << 1) | 1); // U29A-value
			for (int i = 0; i < a.length; i++) {
				if (a[i]) {
					write(RpcPacketTypes.Markers.TRUE_MARKER);
				} else {
					write(RpcPacketTypes.Markers.FALSE_MARKER);
				}
			}
		}
	}
	
	protected void writeByteArray(byte[] ba) throws IOException {
		write(RpcPacketTypes.Markers.BYTE_ARRAY_MARKER);

		if (!writeObjectReference(ba)) {
			int length = ba.length;
			writeU29((length << 1) | 1); // U29B-value
			write(ba, 0, length);
		}
	}

	/**
	 * String 형태로 전송한다.
	 * @param ca
	 * @throws IOException
	 */
	protected void writeCharArray(char[] ca) throws IOException {
		writeString(new String(ca), true);
	}

	protected void writeObjectArray(Object[] oa) throws IOException {
		write(RpcPacketTypes.Markers.OBJECT_ARRAY_MARKER);

		if (!writeObjectReference(oa)) {
			writeU29((oa.length << 1) | 1); // U29A-value
			for (int i=0;i<oa.length;++i) {
				writeObject(oa[i]);
			}
		}
	}

	protected void writeCollection(Collection<?> c) throws IOException {
		// Collection 객체들은 항상 Array 객체로 전송한다.
		writeObjectArray(c.toArray(new Object[c.size()]));
	}

	/**
	 * AMF3 Spec의 string-type으로 전송한다.
	 * 
	 * @param s
	 * @throws IOException
	 */
	protected void writeString(String s, boolean marker) throws IOException {
		if (marker) {
			write(RpcPacketTypes.Markers.STRING_MARKER);
		}

		if (s == null || s.length() == 0) {
            writeU29(RpcPacketTypes.UTF8_EMPTY);
		} else if (!writeStringReference(s)) {
			writeUTF8(s);
		}
	}

	/**
	 * AMF3 Spec의 U29S-value *(UTF8-char) 타입 또는 U29X-value *(UTF8-char) 타입으로
	 * 전송한다.
	 * 
	 * @param s
	 * @throws IOException
	 */
	protected void writeUTF8(String s) throws IOException {
		int strlen = s.length();
		byte[] buf = getBuffer(strlen*3);
		
		int utflen = encodeUTF(s,buf);

		writeU29((utflen << 1) | 1); // U29S
		write(buf, 0, utflen); // 실제 데이터
	}
	
	protected void writeBoolean(Boolean b) throws IOException {
		if (b) {
			write(RpcPacketTypes.Markers.TRUE_MARKER);
		} else {
			write(RpcPacketTypes.Markers.FALSE_MARKER);
		}
	}

	protected void writeDate(Date d) throws IOException {
		write(RpcPacketTypes.Markers.DATE_MARKER);

		if (!writeObjectReference(d)) {
			// write U29D-value
			writeU29(1);

			// write the time in msec
			writeDouble((double)d.getTime());
		}
	}

	protected void writeDouble(double d, boolean mark) throws IOException {
		if (mark) {
			write(RpcPacketTypes.Markers.DOUBLE_MARKER);
		}
		writeDouble(d);
	}

	protected void writeInt(int i, boolean mark) throws IOException {
		if (i >= RpcPacketTypes.MIN_U29_VALUE && i <= RpcPacketTypes.MAX_U29_VALUE) {
			if (mark) {
				write(RpcPacketTypes.Markers.INTEGER_MARKER);
			}
			writeU29(i & 0x1FFFFFFF);
		} else {
			writeLong(i, mark);
		}
	}

	protected void writeLong(long l, boolean mark) throws IOException {
		if (mark) {
			write(RpcPacketTypes.Markers.LONG_MARKER);
		}
		writeLong(l);
	}
	
	protected void writeExternalObject(Externalizable obj) throws IOException {
		write(RpcPacketTypes.Markers.OBJECT_MARKER);

		if (!writeObjectReference(obj)) {
			String className = obj.getClass().getName();
			Traits objProps = new Traits(className, true);
			if (!writeTraitsReference(objProps)) {
				writeU29(0x07); // XXXXXX111 , exteranlizable marker
				writeString(className, false);
			}
			
			// write object itself.
			obj.writeExternal(this);
		}
	}
	
	protected void writeMapObject(Map<?,?> obj) throws IOException {
		write(RpcPacketTypes.Markers.OBJECT_MARKER);
		
		if (!writeObjectReference(obj)) {
			Traits objProps = new Traits();
			List<String> propertyNames = getPropertyNames(obj);
			int propLength = propertyNames.size();
			objProps.propNames = propertyNames;
			
			if (!writeTraitsReference(objProps)) {
				writeU29(0x03 | (propLength << 3)); //
				writeString(objProps.className, false);
				for (int i = 0; i < propLength; i++) {
					String propName = propertyNames.get(i);
					writeString(propName, false);
				}
			}
			
			// write object itself
			if (propLength > 0) {
				for (int i = 0; i < propLength; i++) {
					String propName = propertyNames.get(i);
					Object value = obj.get(propName);
					writeObject(value);
				}
			}
		}
	}

	/**
	 * 해당 문자열의 reference를 전송하고 true를 반환한다. 
	 * 해당 문자열이 없으면 stringRefTable에 저장하고 false를 반환한다.
	 * @param s
	 * @return
	 * @throws IOException
	 */
	protected boolean writeStringReference(String s) throws IOException {
		Integer ref = stringRefTable.get(s);
		
		if (ref != null) {
			// 문자열이 기존에 존재하는 경우이다. ref 값을 전송한다. (U29S)
			writeU29(ref.intValue() << 1);
			return true;
		} else {
			// 문자열이 처음 사용되는 경우이다. stringTable에 저장한다.
			stringRefTable.put(s, stringRefTable.size());
			return false;
		}
	}
	
	/**
	 * 객체의 Traits 정보에 대한 reference를 전송하고 true를 반환한다. 
	 * 해당 traits가 없으면 traitsRefTable에 저장하고 false를 반환한다.
	 * @param objProps
	 * @return
	 * @throws IOException
	 */
	protected boolean writeTraitsReference(Traits objProps) throws IOException {
		Integer ref = traitsRefTable.get(objProps);

		if (ref != null) {
			writeU29((ref.intValue() << 2) | 1);
			return true;
		} else {
			traitsRefTable.put(objProps, traitsRefTable.size());
			return false;
		}
	}

	/**
	 * 객체가 이미 사용된 경우에는 객체의 reference를 전송하고 true를 반환한다. 
	 * 그렇지 않은 경우엔 objectRefTable에 저장하고 false를 반환한다.
	 * 
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	protected boolean writeObjectReference(Object obj) throws IOException {
		Integer ref = objectRefTable.get(obj);

		if (ref != null) {
			writeU29(ref.intValue() << 1);
			return true;
		} else {
			objectRefTable.put(obj, objectRefTable.size());
			return false;
		}
	}

	protected List<String> getPropertyNames(Map<?,?> obj) {
		List<String> list = new ArrayList<String>();
		Object[] keys = obj.keySet().toArray(new Object[obj.size()]);
		for(int i=0;i<keys.length;i++) {
			list.add(String.valueOf(keys[i]));
		}
		return list;
	}

	protected Object getValue(Object obj, String name) {
		if (obj instanceof Map<?,?>) {
			return ((Map<?,?>)obj).get(name);
		}
		return null;
	}
    
	/**
	 * AMF U29 타입을 전송한다.
	 * <ul>
	 * <li>0x00000000 - 0x0000007F : 0xxxxxxx
	 * <li>0x00000080 - 0x00003FFF : 1xxxxxxx 0xxxxxxx
	 * <li>0x00004000 - 0x001FFFFF : 1xxxxxxx 1xxxxxxx 0xxxxxxx
	 * <li>0x00200000 - 0x3FFFFFFF : 1xxxxxxx 1xxxxxxx 1xxxxxxx xxxxxxxx
	 * <li>0x40000000 - 0xFFFFFFFF : range exception
	 * </ul>
	 * 
	 * @param ref
	 * @throws IOException
	 */
	public void writeU29(int ref) throws IOException {
		if (ref < 0x80) {
			write(ref);
		} else if (ref < 0x4000) {
			write(((ref >> 7) & 0x7F) | 0x80);
			write(ref & 0x7F);
		} else if (ref < 0x200000) {
			write(((ref >> 14) & 0x7F) | 0x80);
			write(((ref >> 7) & 0x7F) | 0x80);
			write(ref & 0x7F);
		} else if (ref < 0x40000000) {
			write(((ref >> 22) & 0x7F) | 0x80);
			write(((ref >> 15) & 0x7F) | 0x80);
			write(((ref >> 8) & 0x7F) | 0x80);
			write(ref & 0xFF);
		} else {
			throw new IOException("U29 out of range: " + ref);
		}
	}
	
    /**
     * 문자열을 UTF8 byte 배열에 담아 준다. byte 배열은 충분히 커야한다.
     * @param s encoding 대상 문자열
     * @param buf encoding 결과를 담아둘 byte 배열
     * @return 실제 encode된 byte 수
     * @throws IOException
     */
    protected int encodeUTF(String s, byte[] buf) throws IOException {
    	int strlen = s.length();
    	int count=0;
    	int c;
		for (int i = 0; i < strlen;) {
			//c = s.charAt(i);
			c = s.codePointAt(i);
			if (c < 0x80) {
				buf[count++] = (byte)c;
			} else if (c < 0x800) {
				buf[count++] = (byte)(0xc0 | ((c>>6) & 0x1f));
				buf[count++] = (byte)(0x80 | (c & 0x3f));
			} else if (c < 0x10000) {
				buf[count++] = (byte)(0xe0 | ((c>>12) & 0x0f));
				buf[count++] = (byte)(0x80 | ((c>>6) & 0x3f));
				buf[count++] = (byte)(0x80 | (c & 0x3f));
			} else {
				buf[count++] = (byte)(0xF0 | ((c>>18) & 0x07));
				buf[count++] = (byte)(0x80 | ((c>>12) & 0x3f));
				buf[count++] = (byte)(0x80 | ((c>>6) & 0x3f));
				buf[count++] = (byte)(0x80 | (c & 0x3f));
			}
			
			i += Character.charCount(c);
		}
		return count;
    }

    private Map<String, Integer> stringRefTable = null;
    private Map<Object, Integer> objectRefTable = null;
    private Map<Traits, Integer> traitsRefTable = null;
	
	private byte[] buf = null;
	
    protected byte[] getBuffer(int len) {
        if ((buf == null) || (buf.length < len)) {
        	buf = new byte[len*2];
        }
        return buf;
    }
}
