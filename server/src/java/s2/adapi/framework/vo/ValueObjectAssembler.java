package s2.adapi.framework.vo;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * <strong>ValueObjectAssembler</strong>는 여러 ValueObject를 한데 모아 전송 할 수 있는 기능을 제공한다.
 * 내부는 Map Pattern과 같이 Key, Value 구조로 이루어져있다.
 * 
 */
public class ValueObjectAssembler implements Map<String,ValueObject>, Externalizable {
	
	private static final long serialVersionUID = -5192938845078830965L;

	/**
     * <p/>
     * <strong>ValueObject</strong>의 저장하는 HashMap 오브젝트
     * </p>
     */
    private Map<String,ValueObject> vos = new HashMap<String,ValueObject>();

    /**
     * <p/>
     * <strong>ValueObjectAssembler</strong>의 default 컨스트럭터(Constructor).
     * </p>
     */
    public ValueObjectAssembler() {
    }

    /**
     * <p/>
     * <strong>ValueObjectAssembler</strong> 오브젝트에 <strong>ValueObject</strong>를 특정 Key로 해서 저장한다.
     * 이미 해당 Key로 저장되어 있는 ValueObject가 있다면 overwrite한다.
     * </p>
     *
     * @param key 저장하는 ValueObject의 Key값
     * @param vo  저장하고자 하는 ValueObject
     */
    public void set(String key, ValueObject vo) {
        this.vos.put(key, vo);
    }

    /**
     * <p>
     * 주어진 ValueObjectAssembler 객체내의 ValueObject 들을 추가한다. 
     * 이때 기존에 같은 Key로 저장되어 있던 ValueObject들은 overwrite 한다. 
     * @param voa
     */
    public void add(ValueObjectAssembler voa) {
		Iterator<String> itor = voa.getKeys().iterator();
		while(itor.hasNext()) {
			String key = itor.next();
			this.vos.put(key,voa.get(key));
		}
    }
    
    /**
     * <p/>
     * <strong>ValueObjectAssembler</strong>에서 해당 key의 <strong>ValueObject</strong>를 리턴한다.
     * </p>
     *
     * @param key 리턴받고자 하는 ValueObject의 Key
     * @return Key에 해당하는 ValueObject
     */
    public ValueObject get(String key) {
        ValueObject obj = this.vos.get(key);
        if (obj == null) {
            return null;
        } else {
            return obj;
        }
    }

    /**
     * <p/>
     * <strong>ValueObjectAssembler</strong>의 <code>valueObjects</code>를 리턴한다.
     * </p>
     *
     * @return valueObjects 오브젝트
     */
    public Map<String,ValueObject> getValueObjects() {
        return this.vos;
    }

    /**
     * <p/>
     * <strong>ValueObjectAssembler</strong>의 ValueSet을 리턴한다.
     * </p>
     *
     * @return collection 형태의 valueset
     */
    public Collection<ValueObject> getValues() {
        return this.vos.values();
    }

    /**
     * <p/>
     * <strong>ValueObjectAssembler</strong>의 KeySet을 리턴한다.
     * </p>
     *
     * @return collection 형태의 keyset
     */
    public Collection<String> getKeys() {
        return this.vos.keySet();
    }

    /**
     * <p/>
     * <strong>ValueObjectAssembler</strong>의 <strong>ValueObject</strong> 개수를 리턴한다.
     * </p>
     *
     * @return valueobject 개수
     */
    public int size() {
        return this.vos.size();
    }

    /**
     * <p/>
     * <strong>ValueObjectAssembler</strong>안에 모든 <strong>ValueObject</strong>를 삭제 한다.
     * </p>
     */
    public void clear() {
        this.vos.clear();
    }

    /**
     * <p/>
     * <strong>ValueObjectAssembler</strong>안이 비어 있는지 여부를 리턴한다.
     * </p>
     *
     * @return 비어 있는지 여부
     */
    public boolean isEmpty() {
        return (this.vos.isEmpty());
    }

    /**
     * <p/>
     * Key를 이용하여 <strong>ValueObject</strong>의 존재 여부를 리턴한다.
     * </p>
     *
     * @param key 확인하고자하는 valueobject의 key
     * @return 존재여부
     */
    public boolean isExist(String key) {
        return (this.vos.containsKey(key));
    }

	public boolean containsKey(Object key) {
		return vos.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return vos.containsValue(value);
	}

	public Set<java.util.Map.Entry<String, ValueObject>> entrySet() {
		return vos.entrySet();
	}

	public ValueObject get(Object key) {
		return vos.get(key);
	}

	public Set<String> keySet() {
		return vos.keySet();
	}

	public ValueObject put(String key, ValueObject value) {
		return vos.put(key, value);
	}

	public void putAll(Map<? extends String, ? extends ValueObject> m) {
		vos.putAll(m);
	}

	public ValueObject remove(Object key) {
		return vos.remove(key);
	}

	public Collection<ValueObject> values() {
		return vos.values();
	}

    public String toString() {
        return this.vos.toString();
    }

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(vos);	
	}

	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		vos = (Map<String,ValueObject>)in.readObject();
	}
}
