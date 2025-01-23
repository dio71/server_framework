package s2.adapi.framework.web.rpc;

import java.io.DataInputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import s2.adapi.framework.util.ObjectHelper;
import s2.adapi.framework.web.rpc.RpcPacketTypes.Traits;

/**
 * Modified AMF3
 * @author kimhd
 *
 */
class RpcPacketInputStream extends DataInputStream implements ObjectInput {
    
    public RpcPacketInputStream(InputStream in) {
		super(in);
		stringRefTable = new ArrayList<String>();
		objectRefTable = new ArrayList<Object>();
		traitsRefTable = new ArrayList<Traits>();
	}
	
    public void reset() {
		stringRefTable.clear();
		objectRefTable.clear();
		traitsRefTable.clear();
	}

	public Object readObject() throws IOException, ClassNotFoundException {
		Object obj = null;
		int type = readByte(); // read U8
		switch (type) {
		case RpcPacketTypes.Markers.STRING_MARKER:
			obj = readString();
			break;
		case RpcPacketTypes.Markers.FALSE_MARKER:
			obj = Boolean.FALSE;
			break;
		case RpcPacketTypes.Markers.TRUE_MARKER:
			obj = Boolean.TRUE;
			break;
		case RpcPacketTypes.Markers.INTEGER_MARKER:
			obj = (readU29()<<3)>>3;
			break;
		case RpcPacketTypes.Markers.LONG_MARKER:
			obj = readLong();
			break;
		case RpcPacketTypes.Markers.DOUBLE_MARKER:
			obj = readDouble();
			break;
		case RpcPacketTypes.Markers.OBJECT_MARKER:
			obj = readTypedObject();
			break;	
		case RpcPacketTypes.Markers.OBJECT_ARRAY_MARKER:
			obj = readObjectArray();
			break;	
		case RpcPacketTypes.Markers.DATE_MARKER:
			obj = readDate();
			break;
		case RpcPacketTypes.Markers.BYTE_ARRAY_MARKER:
			obj = readByteArray();
			break;
		case RpcPacketTypes.Markers.INT_ARRAY_MARKER:
			obj = readIntArray();
			break;
		case RpcPacketTypes.Markers.LONG_ARRAY_MARKER:
			obj = readLongArray();
			break;
		case RpcPacketTypes.Markers.DOUBLE_ARRAY_MARKER:
			obj = readDoubleArray();
			break;
		case RpcPacketTypes.Markers.UNDEFINED_MARKER:
			break;
		case RpcPacketTypes.Markers.NULL_MARKER:
			obj = readNull();
			break;	
		default: // unknown type
			throw new IOException("Unknown message type :"+type);
		}

		return obj;
	}
    
	/**
	 *          (hex)          :    b1       b2       b3       b4
	 * 0x00000000 - 0x0000007F : 0xxxxxxx 
	 * 0x00000080 - 0x00003FFF : 1xxxxxxx 0xxxxxxx 
	 * 0x00004000 - 0x001FFFFF : 1xxxxxxx 1xxxxxxx 0xxxxxxx 
	 * 0x00200000 - 0x3FFFFFFF : 1xxxxxxx 1xxxxxxx 1xxxxxxx xxxxxxxx 
	 * 0x40000000 - 0xFFFFFFFF : throw range exception
	 * @return Amf U29 integer
	 * @throws IOException
	 */
	public int readU29() throws IOException {
		int b1,b2,b3,b4;

		b1 = readByte() & 0xff; // read U8
		if (b1 <= 0x7f) {
			return b1;
		}

		b2 = readByte() & 0xff; // read U8
		if (b2 <= 0x7f) {
			return (((b1&0x7F)<<7) | b2);
		}

		b3 = readByte() & 0xff; // read U8
		if (b3 <= 0x7f) {
			return (((b1&0x7f)<<14) | ((b2&0x7f)<<7) | b3);
		}

		b4 = readByte() & 0xff; // read U8
		return (((b1&0x7f)<<22) | ((b2&0x7f)<<15) | ((b3&0x7f)<<8) | b4);
	}
	
    protected Object readNull() {
    	return null;
    }

    /**
	 * Read Object
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	protected Object readTypedObject() throws IOException, ClassNotFoundException {
		int ref = readU29();
		
		Object object = getStoredObject(ref);
		
		if (object == null) { // not object refernce
			Traits objProps = readObjectTraits(ref);
			String className = objProps.className;
			if (className == null || className.length() ==0) {
				// anonymous object
				Map<String,Object> mapObject = new HashMap<String, Object>();
				objectRefTable.add(mapObject);
				
				int propLength = objProps.propNames.size();
				for (int i = 0; i < propLength; i++) {
					String propName = objProps.propNames.get(i);
					Object value = readObject();
					mapObject.put(propName, value);
				}
				
				object = mapObject;
			} else if (objProps.isExternal) {
				object = instantiateObject(className);
				objectRefTable.add(object);
				
				readExternalizable(object);
			} else {
				throw new IOException("not supported class. "+className);
			}
		}
		
		return object;
	}

	/**
	 * object-type = U29 = 000XXXXX XXXXXXXX XXXXXXXX XXXX????
	 * XXXXXXX0 : Object Reference, XXXXXXX1 : Object Instance
	 * XXXXXX01 : Traits Reference, XXXXXX11 : Traits Instance
	 * XXXXX111 : Externalizable
	 * XXXXX011 : Static
	 */
	protected Traits readObjectTraits(int ref) throws IOException {
		Traits objProps = null;
		
		if ((ref & 0x03) == 1) { // 저장된 traits 가져오기
			objProps = traitsRefTable.get(ref >> 2);
		} else { // traits 만들기
			String className = readString(); // traits 명칭 읽기
			boolean ext = false;
			if ((ref & 0x07) == 0x07) { // XXXXX111 : externalizable
				ext = true;
			}
			objProps = new Traits(className, ext);
			traitsRefTable.add(objProps); // traits 저장
			
			int count = (ref >> 3); // U29
			for (int i = 0; i < count; i++) {
				objProps.propNames.add(readString());
			}
		}
		
		return objProps;
	}
	
	private Object instantiateObject(String classname) throws ClassNotFoundException, IOException {
		try {
			return ObjectHelper.instantiate(classname);
			//return getClass().getClassLoader().loadClass(classname).newInstance();
		} 
		catch (ClassNotFoundException e) {
			throw e;
		} 
		catch (Exception e) {
			throw new IOException("Cannot instantiate class: " + classname);
		}
	}
	
	protected void readExternalizable(Object object) throws ClassNotFoundException, IOException {
        if (object instanceof Externalizable) {
            ((Externalizable)object).readExternal(this);
        } 
        else {
            throw new IOException("Not externalizable class:"+object.getClass().getName());
        }
    }
	
	protected int[] readIntArray() throws IOException {
		int ref = readU29();

		Object obj = getStoredObject(ref);
		
		if (obj == null) {
			int len = (ref >> 1);
			int[] ia = new int[len];
			objectRefTable.add(ia); // add to reference table
			for (int i=0; i<len; i++) {
				ia[i] = readU29();
			}
			
			return ia;
		} else {
			return (int[])obj;
		}
	}

	protected long[] readLongArray() throws IOException {
		int ref = readU29();

		Object obj = getStoredObject(ref);
		
		if (obj == null) {
			int len = (ref >> 1);
			long[] la = new long[len];
			objectRefTable.add(la); // add to reference table
			for (int i=0; i<len; i++) {
				la[i] = readLong();
			}
			
			return la;
		} else {
			return (long[])obj;
		}
	}

	protected double[] readDoubleArray() throws IOException {
		int ref = readU29();

		Object obj = getStoredObject(ref);
		
		if (obj == null) {
			int len = (ref >> 1);
			double[] da = new double[len];
			objectRefTable.add(da); // add to reference table
			for (int i=0; i<len; i++) {
				da[i] = readDouble();
			}
			
			return da;
		} else {
			return (double[])obj;
		}
	}
	
	/**
	 * Array는  Object[] 객체로 반환한다.
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	protected Object readObjectArray() throws IOException, ClassNotFoundException {
		int ref = readU29();

		Object obj = getStoredObject(ref);
		
		if (obj == null) {
			int len = (ref >> 1);

			Object[] oa = new Object[len];
			objectRefTable.add(oa); // add to reference table
			
			for (int i = 0; i < len; i++) {
				oa[i] = readObject();
			}
			return oa;
		} else {
			return obj;
		}
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	protected String readString() throws IOException {
		int ref = readU29();

		String str = getStoredString(ref);
		
		if (str == null) {
			int len = (ref >> 1);

			if (len == 0) {
				str = RpcPacketTypes.EMPTY_STRING;
			} else {
				byte[] buf = getBuffer(len);
				readFully(buf,0,len);
				str = new String(buf,0,len,"utf-8");
				stringRefTable.add(str);
			}
		}
		
		return str;
	}

	protected byte[] readByteArray() throws IOException {
		int ref = readU29();
		
		byte[] ba = (byte[])getStoredObject(ref);
		
		if (ba == null) {
			int len = (ref >> 1);

			ba = new byte[len];
			objectRefTable.add(ba);// add to reference table
			
			readFully(ba, 0, len);
		}
		return ba;
	}

	protected Date readDate() throws IOException {
		int ref = readU29();

		Date d = (Date)getStoredObject(ref);

		if (d == null) {
			long msec = (long)readDouble();
			d = new Date(msec);
			objectRefTable.add(d); // add to reference table
		}
		
		return d;
	}
	
    /**
	 * U29 값이 String reference인 경우에는 stringRefTable에서 찾아 해당 String을 반환하고,
	 * 아닌 경우에는 null을 반환한다.
	 * @param ref
	 * @return
	 */
	protected String getStoredString(int ref) {
		if ((ref & 0x01) == 0) {
			return stringRefTable.get(ref>>1);
		} else {
			return null;
		}
	}
	/**
	 * U29 값이 Object reference인 경우에는 objectRefTable에서 찾아 해당 object를 반환하고,
	 * 아닌 경우에는 null을 반환한다.
	 * @param ref
	 * @return
	 */
	protected Object getStoredObject(int ref) {
		if ((ref & 0x01) == 0) {
			return objectRefTable.get(ref >> 1);
		} else {
			return null;
		}
	}
	
    private List<String> stringRefTable = null;
    private List<Object> objectRefTable = null;
    private List<Traits> traitsRefTable = null;
	
    private byte[] buf = null;
    
    private byte[] getBuffer(int len) {
        if ((buf == null) || (buf.length < len)) {
        	buf = new byte[len*2];
        }
        return buf;
    }
}
