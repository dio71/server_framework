package s2.adapi.framework.vo;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class ValueObject implements List<Map<String,Object>>, Externalizable {

	/**
	 * Name of this ValueObject
	 */
	private String voName = "";
	
	private List<Map<String,Object>> tbl = new ArrayList<Map<String,Object>>();
	
	public ValueObject() {
	}
	
	public ValueObject(String name) {
		voName = name;
	}
	
	public void setName(String name) {
		voName = name;
	}
	
	public String getName() {
		return voName;
	}
	
	public int size() {
		return tbl.size();
	}

	public boolean isEmpty() {
		return tbl.isEmpty();
	}

	public boolean contains(Object o) {
		return tbl.contains(o);
	}

	public Iterator<Map<String, Object>> iterator() {
		return tbl.iterator();
	}

	public Object[] toArray() {
		return tbl.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return tbl.toArray(a);
	}

	public boolean add(Map<String, Object> e) {
		return tbl.add(e);
	}
	
	public void add(int index, Map<String, Object> element) {
		tbl.add(index, element);
	}
	
	public void add(ValueObject vo) {
		if (vo != null) {
			for(int i=0; i<vo.size(); i++) {
				tbl.add(vo.get(i));
			}
		}
	}
	
	public boolean remove(Object o) {
		return tbl.remove(o);
	}

	public boolean containsAll(Collection<?> c) {
		return tbl.containsAll(c);
	}

	public boolean addAll(Collection<? extends Map<String, Object>> c) {
		return tbl.addAll(c);
	}

	public boolean addAll(int index, Collection<? extends Map<String, Object>> c) {
		return tbl.addAll(index, c);
	}

	public boolean removeAll(Collection<?> c) {
		return tbl.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return tbl.retainAll(c);
	}

	public void clear() {
		tbl.clear();
	}

	public Map<String, Object> get(int index) {
		return tbl.get(index);
	}

	public ValueObject getRowAsVo(int index) {
		ValueObject vo = new ValueObject();
		vo.add(get(index));
		
		return vo;
	}
	
	public Object get(int index, String key, Object defaultValue) {
		Object ret = tbl.get(index).get(key);
		return (ret == null)?defaultValue:ret;
	}
		
	public Object get(int index, String key) {
		return tbl.get(index).get(key);
	}
	
	public Object get(String key) {
		return tbl.get(0).get(key);
	}
	
	public boolean getBoolean(int index, String key, boolean defaultValue) {
		Object obj = tbl.get(index).get(key);
		if (obj == null) {
            return defaultValue;
        } else if (obj instanceof Boolean) {
            return ((Boolean)obj).booleanValue();
        } else {
        	String str = String.valueOf(obj);
        	return Boolean.parseBoolean(str);
        }
	}
	
	public boolean getBoolean(int index, String key) {
		return getBoolean(index, key, false);
	}
	
	public boolean getBoolean(String key) {
		return getBoolean(0, key, false);
	}
	
	public boolean getBoolean(String key, boolean defaultValue) {
		return getBoolean(0, key, defaultValue);
	}
	
	public int getInt(int index, String key, int defaultValue) {
		Object obj = tbl.get(index).get(key);
		if (obj == null) {
            return defaultValue;
        } else if (obj instanceof Number){
            return ((Number)obj).intValue();
        } else {
        	String str = String.valueOf(obj);
        	if (str.length() == 0) {
        		return defaultValue;
        	} else {
        		return Integer.parseInt(str);
        	}
        }
	}
	
	public int getInt(int index, String key) {
		return getInt(index, key, 0);
	}
	
	public int getInt(String key, int defaultValue) {
		return getInt(0, key, defaultValue);
	}
	
	public int getInt(String key) {
		return getInt(0, key, 0);
	}
	
	public long getLong(int index, String key, long defaultValue) {
		Object obj = tbl.get(index).get(key);
		if (obj == null) {
            return defaultValue;
        } else if (obj instanceof Number){
            return ((Number)obj).longValue();
        } else {
        	String str = String.valueOf(obj);
        	if (str.length() == 0) {
        		return defaultValue;
        	} else {
        		return Long.parseLong(str);
        	}
        }
	}
	
	public long getLong(int index, String key) {
		return getLong(index, key, 0L);
	}
	
	public long getLong(String key, long defaultValue) {
		return getLong(0, key, defaultValue);
	}
	
	public long getLong(String key) {
		return getLong(0, key, 0L);
	}
	
	public float getFloat(int index, String key, float defaultValue) {
		Object obj = tbl.get(index).get(key);
		if (obj == null) {
            return defaultValue;
        } else if (obj instanceof Number){
            return ((Number)obj).floatValue();
        } else {
        	String str = String.valueOf(obj);
        	if (str.length() == 0) {
        		return defaultValue;
        	} else {
        		return Float.parseFloat(str);
        	}
        }
	}
	
	public float getFloat(int index, String key) {
		return getFloat(index, key, 0.0F);
	}
	
	public float getFloat(String key, float defaultValue) {
		return getFloat(0, key, defaultValue);
	}
	
	public float getFloat(String key) {
		return getFloat(0, key, 0.0F);
	}
	
	public double getDouble(int index, String key, double defaultValue) {
		Object obj = tbl.get(index).get(key);
		if (obj == null) {
            return defaultValue;
        } else if (obj instanceof Number){
            return ((Number)obj).doubleValue();
        } else {
        	String str = String.valueOf(obj);
        	if (str.length() == 0) {
        		return defaultValue;
        	} else {
        		return Double.parseDouble(str);
        	}
        }
	}
	
	public double getDouble(int index, String key) {
		return getDouble(index, key, 0.0);
	}
	
	public double getDouble(String key, double defaultValue) {
		return getDouble(0, key, defaultValue);
	}
	
	public double getDouble(String key) {
		return getDouble(0, key, 0.0);
	}
	
	public String getString(int index, String key, String defaultValue) {
		Object obj = tbl.get(index).get(key);
		if (obj == null) {
            return defaultValue;
        } else if (obj instanceof Double || obj instanceof Float){
        	// 지수 표현식으로 반환되는 것을 막기 위하여 BigDecimal로 중간 변환하여 처리함.
        	return BigDecimal.valueOf(((Number)obj).doubleValue()).toString();
        } else if (obj instanceof Long || obj instanceof Integer) {
        	return BigDecimal.valueOf(((Number)obj).longValue()).toString();
        } else {
        	return String.valueOf(obj);
        }
	}
	
	public String getString(int index, String key) {
		return getString(index, key, null);
	}
	
	public String getString(String key, String defaultValue) {
		return getString(0, key, defaultValue);
	}
	
	public String getString(String key) {
		return getString(0, key, null);
	}
	
	public Map<String, Object> set(int index, Map<String, Object> element) {
		return tbl.set(index, element);
	}

	public void set(int index, String key, Object value) {
        Map<String,Object> row = null;
        if (index < tbl.size() && tbl.get(index) != null) {
            row = tbl.get(index);
        } else {
            row = new HashMap<String,Object>();
            add(index, row);
        }
        row.put(key, value);
    }
	
	public void set(String key, Object value) {
		set(0, key, value);
	}
	
    public void set(int index, String key, boolean value) {
        set(index, key, Boolean.valueOf(value));
    }
    
    public void set(String key, boolean value) {
        set(0, key, Boolean.valueOf(value));
    }
    
    public void set(int index, String key, int value) {
        set(index, key, Integer.valueOf(value));
    }
    
    public void set(String key, int value) {
        set(0, key, Integer.valueOf(value));
    }
    
    public void set(int index, String key, long value) {
        set(index, key, Long.valueOf(value));
    }
    
    public void set(String key, long value) {
        set(0, key, Long.valueOf(value));
    }
    
    public void set(int index, String key, float value) {
        set(index, key, Float.valueOf(value));
    }
    
    public void set(String key, float value) {
        set(0, key, Float.valueOf(value));
    }
    
    public void set(int index, String key, double value) {
        set(index, key, Double.valueOf(value));
    }
    
    public void set(String key, double value) {
        set(0, key, Double.valueOf(value));
    }
    
	public Map<String, Object> remove(int index) {
		return tbl.remove(index);
	}

	public int indexOf(Object o) {
		return tbl.indexOf(o);
	}

	public int lastIndexOf(Object o) {
		return tbl.lastIndexOf(o);
	}

	public ListIterator<Map<String, Object>> listIterator() {
		return tbl.listIterator();
	}

	public ListIterator<Map<String, Object>> listIterator(int index) {
		return tbl.listIterator(index);
	}

	public List<Map<String, Object>> subList(int fromIndex, int toIndex) {
		return tbl.subList(fromIndex, toIndex);
	}


	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(voName);
		Object[] arr = tbl.toArray(new Object[tbl.size()]);
		out.writeObject(arr);
	}

	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException,
		ClassNotFoundException {
		voName = in.readUTF();
		Object[] arr = (Object[])in.readObject();
		tbl.clear();
		for(int i=0; i<arr.length; i++) {
			tbl.add((Map<String,Object>)arr[i]);
		}
	}
	
	public String toString() {
		return tbl.toString();
	}
}
