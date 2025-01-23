package s2.adapi.framework.web.rpc;

import java.util.ArrayList;
import java.util.List;

/**
 * Modified AMF3
 * @author kimhd
 *
 */
class RpcPacketTypes {
	public static final long MAX_U29_VALUE = 268435455; // 2^28 - 1
    public static final long MIN_U29_VALUE = -268435456; // -2^28 

    public static final String EMPTY_STRING = "";
    public static final int UTF8_EMPTY = 0x01; // for empty string
    
	public static class Markers {
	    public static final int UNDEFINED_MARKER  = 0x00;
	    public static final int NULL_MARKER       = 0x01;
	    public static final int FALSE_MARKER      = 0x02;
	    public static final int TRUE_MARKER       = 0x03;
	    public static final int INTEGER_MARKER    = 0x04;
	    public static final int DOUBLE_MARKER     = 0x05;
	    public static final int STRING_MARKER     = 0x06;
	    public static final int LONG_MARKER       = 0x07;
	    public static final int DATE_MARKER       = 0x08;
	    public static final int OBJECT_ARRAY_MARKER  = 0x09;
	    public static final int OBJECT_MARKER     = 0x0a;
	    public static final int BYTE_ARRAY_MARKER = 0x0c;
	    public static final int INT_ARRAY_MARKER = 0x0d;
	    public static final int LONG_ARRAY_MARKER = 0x0e;
	    public static final int DOUBLE_ARRAY_MARKER = 0x0f;
	}
	
	public static class Traits {

		public String className;
		public boolean isExternal = false;
	    public List<String> propNames = null;
	    
	    public Traits(String name, boolean ext) {
	    	className = name;
	    	isExternal = ext;
	    	propNames = new ArrayList<String>();
	    }
	    
	    public Traits() {
	    	className = EMPTY_STRING;
	    	isExternal = false;
	    	propNames = null;
	    }
	}
	
    
}
